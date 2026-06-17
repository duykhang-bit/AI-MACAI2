# Engine Reliability Improvements — Execution Prompt

> Auto-generated from plan: `docs/plans/engine-reliability-improvements.html`
> Stages: 3 | Total deliverables: 5 (issues #1–#5)
> Platform: Kiro CLI (Variant A — subagent tool)

## TASK
Cải thiện 5 vấn đề độ ổn định của recorder-engine: (1) bỏ delay hardcode thừa, (2) auto-dismiss popup bất ngờ, (3) vá gap bắt text động, (4) detect hover-to-reveal, (5) Windows CLI cross-platform.

## WORKING DIR
`/Users/thinhph/Work/FSOFT/AIContext/ai-test/recorder-engine`

## CONTEXT (≤ 5 dòng)
- TS/Node project. Entry: `src/cli.ts` (qua `bin/frt-test` → tsx). Core: `src/recorder.ts` (record, inject script chạy trong browser), `src/runner.ts` (replay Playwright), `src/types.ts` (schema).
- Recorder ĐÃ có hệ xử lý text động mạnh: `isVolatileText`, `classifyAnchor`, auto-variabilize `{{var}}`, VERIFY→AUTO-PICK lazy tiering. KHÔNG build lại — chỉ vá gap.
- Runner ĐÃ wait element trước mỗi action (click chờ visible, fill click trước, selectOption chờ attached). `delay:1000` chỉ là padding thừa.
- Test: `node --import tsx --test src/<file>.test.ts`. Pattern dùng `node:test` + `node:assert`. Test files hiện có: scaffold/migrate/clone-flow/config.

## CONSTRAINTS
- Backward compat: flow YAML cũ có `delay` PHẢI vẫn chạy (giữ field trong schema + runner support).
- KHÔNG đổi public API signature của FlowRunner/FlowRecorder.
- Inject script (`addInitScript`) chạy trong browser context — KHÔNG dùng Node API trong đó; nhớ polyfill `__name`/`__publicField` đã có.
- File > 500 LOC không tách thêm trong scope này (recorder.ts đã lớn) — chỉ sửa tại chỗ, không refactor ngoài scope.
- Mỗi thay đổi trace được về issue # trong plan.
- Comment tiếng Việt theo style hiện có của codebase.

## EXECUTION — 3-STAGE CHAIN

> ⚠️ Orchestrator prompt. Không implement trực tiếp. Chạy tuần tự, mỗi stage = 1 subagent DAG blocking.

---

### Stage 1: Quick wins (Windows CLI + bỏ delay + vá text động)
**Sub-agents**: 2 parallel (tách theo file để tránh conflict)

- **Agent 1 (kiro_default)** — Issues #1 + #3 (đều sửa `src/recorder.ts`):
  - #1: Trong `actionToStep()` (≈ dòng 1546–1610), XÓA `delay: 1000` mặc định ở 4 case: click, fill, selectOption, keyboard. Giữ field `delay` trong types (manual override).
  - #3a: Assert handler (≈ dòng 1090–1150, trong `pointerup` assert mode): khi `expectedValue` thỏa `isVolatileText()` và `assertType` là `text` → tự đổi sang `assertType: 'visible'` (chỉ verify tồn tại) HOẶC parameterize thành `{{var}}` đẩy vào pending vars. Chọn `visible` làm default an toàn; log quyết định.
  - #3b: Mở rộng `isVolatileText()` (≈ dòng 774): thêm pattern `$`/USD/€, `\d+%`, ngày `\d{1,2}[/\-.]\d{1,2}[/\-.]\d{2,4}`.

- **Agent 2 (kiro_default)** — Issue #5 (`bin/frt-test` + `README.md`):
  - Rewrite `bin/frt-test` từ bash → Node.js launcher (`#!/usr/bin/env node`, spawn `node_modules/tsx/dist/cli.mjs` với `src/cli.ts` qua `process.execPath`, `stdio:'inherit'`, exit với status con).
  - `package.json` bin giữ nguyên `"frt-test": "./bin/frt-test"`.
  - README: bỏ hướng dẫn tạo `frt-test-win`; ghi rõ `npm link` chạy được cả Windows (npm tự sinh `.cmd`/`.ps1`).

**Context7 validation** (mỗi agent chạy trước khi code):
- `resolve-library-id` + `query-docs` cho: Playwright (`assertType visible` semantics, locator API), npm `bin` shim behavior (cross-platform shim auto-gen), tsx (cách invoke cli.mjs).
- Ghi version + API verified vào SDD.

**SDD**: Ghi vào `docs/design/engine-reliability-improvements.html` (Apple 2026 style, inline CSS, light/dark). Mỗi agent ghi section riêng: Context, Requirements, Design Decisions, File Changes, Verification, Autonomous Decisions.

**Key files to read** (agent tự đọc):
- `src/recorder.ts` (actionToStep, isVolatileText, pointerup assert handler)
- `src/types.ts` (FlowStep.delay, assertType)
- `bin/frt-test`, `package.json`, `README.md`

**Exit gate** (verify ALL):
- [ ] `grep -n "delay: 1000" src/recorder.ts` → 0 kết quả
- [ ] `head -1 bin/frt-test` → `#!/usr/bin/env node`
- [ ] `node bin/frt-test list` chạy được (in danh sách flow, không lỗi)
- [ ] `node --import tsx --test src/*.test.ts` → all pass
- [ ] `isVolatileText` test: "12.5%", "$99.00", "04/06/2026" đều true (thêm 1 test nhỏ hoặc verify thủ công qua node REPL)
- [ ] Scope: #1, #3, #5 mỗi cái có code + verify + wired

**Handoff**: Ghi `docs/_handoff/stage-1-done.md`:
- Files modified (list paths)
- `bin/frt-test` launcher format confirmed
- delay removal confirmed (grep result)
- isVolatileText patterns added
- assert dynamic-value handling chosen (visible vs parameterize)
- Test count after stage
- Context7 versions verified

---

### Stage 2: Popup auto-dismiss (Issue #2)
**Reads**: `docs/_handoff/stage-1-done.md`
**Assumes done** (không re-implement): #1, #3, #5.

**Sub-agents**: 1 (kiro_default)
- Issue #2 (`src/runner.ts` + `src/recorder.ts` + `src/types.ts`):
  - Runner: thêm `addInitScript` inject MutationObserver theo dõi popup/modal/overlay phổ biến (`.ant-modal-mask/.ant-modal-wrap`, `.modal-backdrop`, `[role=dialog]`, `[role=alertdialog]`, `.ant-notification`, `.ant-message`, `[class*=cookie]`, `[class*=consent]`). Khi xuất hiện → tìm nút close (✕/"Đóng"/"Close"/"Cancel"/"Bỏ qua") click; toast/notification → để tự mất.
  - QUAN TRỌNG (an toàn): KHÔNG auto-dismiss popup chứa action nguy hiểm ("Xóa"/"Delete"/"Xác nhận xóa"). Chỉ dismiss popup thông tin/quảng cáo/consent.
  - Recorder: trong `pointerup`, nếu click target là backdrop/mask (`.ant-modal-mask`, `.modal-backdrop`) → KHÔNG ghi step "click vào vùng trống"; log `[skip] popup backdrop`.
  - Types: thêm `autoPopupDismiss?: boolean` vào `FlowConfig` (default true). Runner đọc config để bật/tắt.

**Context7 validation**: Playwright `addInitScript`, MutationObserver best practice (disconnect/throttle), Ant Design modal/notification DOM class hiện hành.

**SDD**: bổ sung section Issue #2 vào `docs/design/engine-reliability-improvements.html`.

**Key files to read**:
- `src/runner.ts` (phần `addInitScript` OneSignal/notification ≈ dòng 530–578 — pattern inject sẵn có)
- `src/recorder.ts` (`pointerup` handler ≈ dòng 1053–1190)
- `src/types.ts` (FlowConfig)

**Exit gate**:
- [ ] `grep -n "autoPopupDismiss" src/types.ts src/runner.ts` → có
- [ ] Inject script không dùng Node API (chỉ DOM); có guard không dismiss nút "Xóa/Delete"
- [ ] `node --import tsx --test src/*.test.ts` → all pass
- [ ] (Smoke nếu có URL test) record/play 1 flow có popup → step sau popup không bị block
- [ ] Scope: #2 = code + wired + config toggle

**Handoff**: `docs/_handoff/stage-2-done.md` (files, popup patterns list, danger-guard logic, config default, test count)

---

### Stage 3: Hover-to-reveal detect (Issue #4) + Senior Review + Security Scan
**Reads**: `docs/_handoff/stage-2-done.md`
**Assumes done**: #1, #2, #3, #5.

**Sub-agents**: 1 (kiro_default) + 1 senior reviewer

- **Agent (kiro_default)** — Issue #4 (`src/recorder.ts` + `src/runner.ts`):
  - Recorder inject: thêm `mouseover` listener (debounced ~150ms), lưu last-hovered element + timestamp. Trong `pointerup`: nếu click target (hoặc ancestor gần) trước đó KHÔNG visible và CHỈ visible sau hover ancestor → emit thêm `hover` action TRƯỚC `click`. Nếu KHÔNG chắc → KHÔNG emit (tránh false-positive).
  - `buildFlow()`/`actionToStep()`: render hover action thành step `action: 'hover'` (đã hỗ trợ trong types + runner).
  - Runner `hover` case (≈ dòng 288): sau hover, thêm implicit short wait cho element con visible trước khi sang step tiếp.
  - FALLBACK: nếu auto-detect khó tin cậy → thêm hotkey thủ công đánh dấu hover (ví dụ Ctrl+Shift+H) ghi 1 hover step trên element đang trỏ. Ghi rõ lựa chọn vào SDD.

**Context7 validation**: Playwright `locator.hover()`, mouseover/visibility detection patterns.

**SDD**: bổ sung section Issue #4 + section "Autonomous Decisions" tổng hợp.

**Key files to read**:
- `src/recorder.ts` (`pointerdown/pointerup` ≈ 1049–1190, `findInteractiveTarget`, assert banner hotkey pattern cho fallback hotkey)
- `src/runner.ts` (`hover` case ≈ 288)
- `src/types.ts` (action 'hover')

**Senior Review** (spawn riêng 1 subagent, role senior architect, model cao nhất):
```
TASK: Senior review + patch + scope completeness + technology freshness
ROLE: Senior architect / code reviewer
FILES TO REVIEW: tất cả file đã đổi qua 3 stages (recorder.ts, runner.ts, types.ts, bin/frt-test, README.md, package.json)
PLAN DOCUMENT: docs/plans/engine-reliability-improvements.html
SDD: docs/design/engine-reliability-improvements.html
WORKING DIR: /Users/thinhph/Work/FSOFT/AIContext/ai-test/recorder-engine
REQUIREMENTS:
1. Đọc PLAN — liệt kê từng deliverable/exit criteria của 5 issues
2. Đọc tất cả file đã đổi
3. Mỗi deliverable: verify code tồn tại, wired, có test/verify, user dùng được
4. Tìm bug, edge case, cross-platform (Windows path/shim), inject-script an toàn (không Node API), missing error handling
5. Fix trực tiếp mỗi issue. Tag [P0]–[P3]
6. Nếu scope gap → implement hoặc report SCOPE GAP rõ ràng
7. Context7: verify Playwright/tsx/npm-bin (version latest? API deprecated?)
8. Chạy test sau fix. Nếu đổi code → rerun review (loop tới 0 P0–P1)
9. Report: scope checklist (✅/❌ per issue), tech freshness table, issues+severity, fixes, test results
DO NOT: thêm feature ngoài plan, refactor ngoài scope, đổi public API
```

**Security Scan** (bắt buộc trước commit — spawn subagent security reviewer):
- Scan toàn bộ diff 3 stages cho secrets/credentials/PII/internal-infra/private-key.
- Pattern-based (regex §catalog) + semantic. Đặc biệt check: URL nội bộ trong README/test, không hardcode token.
- [BLOCK] → không commit, báo user. [WARN] → confirm. [INFO]/CLEAN → proceed.

**Exit gate**:
- [ ] Recorder emit `hover` step khi hover-reveal; KHÔNG emit hover thừa cho click thường (verify bằng record thử hoặc unit logic test)
- [ ] Runner hover case có wait revealed element
- [ ] `node --import tsx --test src/*.test.ts` → all pass
- [ ] Senior review: 0 P0–P1 remaining
- [ ] Tech freshness: no deprecated API
- [ ] Security scan: CLEAN hoặc NEEDS_CONFIRMATION (no BLOCK)
- [ ] SDD complete tại `docs/design/engine-reliability-improvements.html`

**Handoff**: `docs/_handoff/stage-3-done.md` (final summary, review verdict, scan verdict)

---

## ORCHESTRATOR INSTRUCTIONS (Variant A — Kiro CLI)
1. Đọc lại `docs/plans/engine-reliability-improvements.html` (W6 — stay on track).
2. Chạy Stage 1 bằng `subagent` tool (blocking, role kiro_default, 2 agents parallel).
   - Sub-agent prompt PHẢI include: "Dùng Context7 (resolve-library-id + query-docs) verify dependencies trước khi code".
3. Verify exit gate (chạy grep/test/`node bin/frt-test list`) + scope completeness (W3).
4. PASS → ghi `stage-1-done.md` → Stage 2. FAIL → fix trong context, re-verify, KHÔNG skip.
5. Lặp Stage 2, Stage 3.
6. Stage 3: include senior review subagent (W4 review loop) + security scan subagent (W5) TRƯỚC commit.
7. Final verify: check ALL exit criteria từ plan.
8. Commit: chạy security scan trước. BLOCK nếu có secrets. KHÔNG `--no-verify`.

## ERROR RECOVERY
- Subagent fail/partial → đọc output, identify missing, re-run với scope thu hẹp.
- Context gần limit → ghi progress vào handoff, báo resume point.
- Test fail sau stage → fix trong stage trước khi proceed.
- Context7 unavailable → proceed best-known version, flag "unverified" trong SDD.
- Hover auto-detect (#4) quá flaky → chuyển fallback hotkey thủ công (đã định trong plan), ghi SDD.

## FINAL EXIT CRITERIA (source of truth = plan)
- [ ] #1: recorder không sinh `delay` mặc định; flow cũ có delay vẫn pass; flow mới nhanh hơn, không tăng fail.
- [ ] #2: runner tự dismiss popup/modal/toast (trừ popup nguy hiểm); recorder không ghi click backdrop; config toggle.
- [ ] #3: assert giá trị động không ghi literal (visible/`{{var}}`); `isVolatileText` bắt %/$/ngày; regression text tĩnh OK.
- [ ] #4: hover-reveal → hover+click 2 steps; replay pass; không hover thừa (hoặc fallback hotkey hoạt động).
- [ ] #5: Windows `npm link` → `frt-test --help` chạy không cần WSL/Git Bash; macOS/Linux không đổi; không còn frt-test-win.

**Workflow gates (thêm vào exit criteria):**
- [ ] SDD complete tại `docs/design/engine-reliability-improvements.html` (không placeholder)
- [ ] Context7 tech freshness verified — no deprecated API
- [ ] Senior review: APPROVED (0 P0–P1, scope 100%)
- [ ] Security scan: CLEAN (no [BLOCK] in final diff)
- [ ] All tests pass sau senior review patches
