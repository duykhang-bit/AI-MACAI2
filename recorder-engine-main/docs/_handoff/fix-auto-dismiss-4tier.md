# Fix Auto-Dismiss 4-Tier — Execution Prompt

> Auto-generated from plan: `docs/plans/fix-auto-dismiss-4tier.html`
> Stages: 3 | Total deliverables: 8

## TASK

Refactor `autoPopupDismiss` trong recorder-engine runner.ts: chuyển từ "đóng bừa mọi modal" sang hệ thống 4 tầng (denylist → protect → reactive → heuristic). Default mới: `junk-only`.

## WORKING DIR

`/Users/thinhph/Work/FSOFT/AIContext/ai-test/recorder-engine`

## CONTEXT

- Engine file chính: `src/runner.ts` (960 LOC) — logic auto-dismiss ở dòng 718–774.
- Types: `src/types.ts` — `FlowConfig.autoPopupDismiss` hiện là `boolean`.
- Intercept handler (retry with force): **HAI** chỗ — click ở dòng 297, fill ở dòng 334.
- Test files cùng thư mục `src/`: `*.test.ts` pattern (xem `src/config.test.ts`).
- **⚠️ Popup-dismiss logic chạy trong BROWSER** via `page.addInitScript` (serialize function, không thấy biến Node).

## ⚠️ EXECUTION CONTEXT — Browser vs Node (đọc trước khi code)

Đây là ràng buộc kiến trúc quyết định cách viết code (chi tiết: plan §3.5):

| Tầng | Context | Cơ chế |
|------|---------|--------|
| 1 — junk observer | **Browser** | `addInitScript` — KHÔNG truy cập biến Node |
| 2 — protected mark | Node→Browser | `page.evaluate` (chỉ aggressive) |
| 3 — reactive | **Node** | trong catch intercept, gọi `page.evaluate` dò overlay |
| 4 — heuristic | **Cả hai** | observer (browser) + reactive (Node evaluate) |

- **Config mode** phải truyền qua tham số: `addInitScript((mode) => {...}, resolvedMode)`. KHÔNG đọc `config.x` trong closure (ReferenceError).
- **Heuristic `isLikelyFunctionalModal`** KHÔNG extract thành module Node import được cho init-script. Pattern: 1 bản canonical trong `src/popup-dismiss.ts` (Node import + test), 1 bản inline trong init-script (phải khớp logic).

## CONSTRAINTS

- **Backward-compatible**: `autoPopupDismiss: true` → `"aggressive"`, `false` → `"off"`, undefined → `"junk-only"` (default mới).
- **Default mới**: `"junk-only"` — modal Ant KHÔNG bị đóng chủ động (không cần Tầng 2 cho default path).
- **Không refactor ngoài scope**: chỉ đụng popup-dismiss + intercept handler. Không sửa recorder, không sửa CLI.
- **runner.ts đã 960 LOC** (>500 guideline) → **bắt buộc** extract popup concern sang `src/popup-dismiss.ts`.
- **Tầng 3 reactive**: chèn TRƯỚC force-retry hiện có, KHÔNG thay thế (giữ xử lý combobox `opacity:0`). DRY: 1 helper dùng chung cho cả click(:297)+fill(:334).
- **Test runner**: dùng framework có sẵn (xem `package.json` + `src/config.test.ts`).
- **TypeScript strict**: giữ type safety, không `any` mới.

## EXECUTION — 3-STAGE CHAIN

> ⚠️ Đây là orchestrator prompt. Không implement trực tiếp.
> Chạy từng stage tuần tự. Mỗi stage = 1 sub-agent DAG blocking.

---

### Stage 1: Foundation — Config + Junk-Only Observer + Protected Tracking

**Sub-agents**: 1 agent (sequential deliverables có dependency)
- Agent 1 (implementer): D6, D1, D3, D2 — Config type mở rộng → refactor selectors → junk-only observer → protected modal Set

**Context7 validation** (agent tự chạy trước khi code):
- `resolve-library-id` + `query-docs` cho `@playwright/test` — verify MutationObserver inject API (`page.addInitScript`)
- Verify Playwright `locator.evaluate` / `page.evaluate` API mới nhất
- Ghi kết quả vào SDD

**SDD**: Ghi design decisions vào `docs/design/fix-auto-dismiss-4tier.html` (song song với code)

**Key files to read** (agent tự đọc):
- `src/types.ts` (FlowConfig interface — dòng 11–20)
- `src/runner.ts` dòng 690–780 (toàn bộ popup dismiss block)
- `src/runner.ts` dòng 1–20 (imports)
- `package.json` (test runner, dependencies)

**Implementation details**:

1. **D6 — Config type**:
   - `src/types.ts`: đổi `autoPopupDismiss?: boolean` → `autoPopupDismiss?: boolean | 'junk-only' | 'off' | 'aggressive'`
   - `src/runner.ts`: hàm resolve mode — `true` → `'aggressive'`, `false` → `'off'`, undefined → `'junk-only'`, string → giữ nguyên

2. **D1 — Extract `src/popup-dismiss.ts`** (BẮT BUỘC — runner.ts đã 960 LOC):
   - Tạo file mới chứa: `JUNK_SELECTORS`, `AGGRESSIVE_SELECTORS` (selectors cũ), `isLikelyFunctionalModal` (canonical), helper build init-script
   - `JUNK_SELECTORS`:
     ```
     .ant-notification-notice, .ant-message-notice,
     .onesignal-slidedown-container, [class*="onesignal"],
     [class*="cookie-"], [class*="ad-overlay"], [id*="ad-"]
     ```
   - `AGGRESSIVE_SELECTORS` = chuỗi POPUP_SELECTORS gốc (giữ cho mode aggressive)
   - JUNK_SELECTORS **KHÔNG** chứa `.ant-modal-wrap/.ant-modal-mask/.modal-backdrop`

3. **D3 — Observer theo mode** (truyền mode vào addInitScript):
   - `mode === 'off'` → không inject observer nào
   - `mode === 'junk-only'` (default) → observer CHỈ match JUNK_SELECTORS → đóng
   - `mode === 'aggressive'` → giữ hành vi cũ (AGGRESSIVE_SELECTORS + trySafeClose)
   - ⚠️ Pattern đúng: `await page.addInitScript((mode, junkSel, aggrSel) => {...}, resolvedMode, JUNK_SELECTORS, AGGRESSIVE_SELECTORS)` — KHÔNG đọc biến Node trong closure

4. **D2 — Protected modal tracking (CHỈ aggressive — safety net, KHÔNG trên default path)**:
   - Chỉ áp dụng khi `mode === 'aggressive'`
   - Trước mỗi step: nếu selector target `.ant-modal` ancestor → `page.evaluate` gắn `data-frt-protected="true"` lên modal wrap
   - Aggressive observer skip element có `[data-frt-protected]`
   - Ghi rõ trong SDD: best-effort, async marking có thể đua observer — đây là lý do default dùng junk-only (không cần D2)

**Exit gate**:
- [ ] `npx tsc --noEmit` pass (type check)
- [ ] Existing tests still pass (chạy test runner)
- [ ] `src/popup-dismiss.ts` tồn tại; runner.ts giảm LOC (popup logic đã extract)
- [ ] `autoPopupDismiss: "aggressive"` → observer inject AGGRESSIVE_SELECTORS (hành vi cũ)
- [ ] `autoPopupDismiss: "junk-only"` (default) → observer KHÔNG match `.ant-modal-wrap`
- [ ] `autoPopupDismiss: "off"` → không inject observer
- [ ] Mode được truyền qua tham số addInitScript (không có ReferenceError với biến Node)
- [ ] D2 chỉ kích hoạt khi mode === aggressive
- [ ] Scope completeness: D1 ✅, D2 ✅, D3 ✅, D6 ✅

**Handoff**: Ghi vào `docs/_handoff/stage-1-done.md`:
- Files created/modified (list paths) — gồm `src/popup-dismiss.ts`
- Config resolve-mode logic location (file:line)
- JUNK_SELECTORS + AGGRESSIVE_SELECTORS location
- Cách mode được truyền vào addInitScript (signature)
- Protected modal attribute name + điều kiện kích hoạt (aggressive only)
- Test count after stage

---

### Stage 2: Reactive Dismiss + Heuristic (Tầng 3 + 4)

**Reads**: `docs/_handoff/stage-1-done.md`
**Assumes done** (không re-implement):
- Config type + resolve-mode (D6)
- `src/popup-dismiss.ts` với JUNK_SELECTORS + AGGRESSIVE_SELECTORS + `isLikelyFunctionalModal` canonical (D1)
- Observer theo mode, truyền qua tham số addInitScript (D3)
- Protected modal tracking — aggressive only (D2)

**Sub-agents**: 1 agent (implementer)
- Agent 1 (implementer): D4, D5 — Reactive dismiss helper (trước force-retry) + content heuristic

**Key files to read**:
- `src/runner.ts` dòng 285–350 (intercept retry: click :297 + fill :334)
- `src/popup-dismiss.ts` (heuristic canonical đã có từ Stage 1)
- `docs/_handoff/stage-1-done.md` (biết vị trí code mới)

**Implementation details**:

1. **D5 — `isLikelyFunctionalModal`** (đã có canonical từ Stage 1):
   ```typescript
   // src/popup-dismiss.ts — canonical (Node import + test)
   export function isLikelyFunctionalModal(el: Element): boolean {
     return !!el.querySelector('input,select,textarea,[role="combobox"],table,.ant-form');
   }
   ```
   - Stage 2 dùng bản này trong reactive dismiss (Node-side `page.evaluate` — truyền selector list vào evaluate)

2. **D4 — Reactive dismiss helper (chèn TRƯỚC force-retry, KHÔNG thay thế)**:
   - Tạo 1 helper dùng chung: `private async tryReactiveDismiss(page, targetLoc): Promise<boolean>` — trả `true` nếu đã đóng được overlay rác (caller retry step), `false` nếu nên fall through.
   - Trong cả 2 catch block (click :297, fill :334), khi error chứa `intercept`/`overlay`:
     1. **Gọi `tryReactiveDismiss` TRƯỚC** force-retry.
     2. Helper dùng `page.evaluate`: tìm overlay topmost-at-point đang phủ target.
        - `[data-frt-protected]` → throw lỗi rõ: `"Protected modal đang chặn step '${step.name}' — không tự đóng"`
        - matches JUNK_SELECTORS → đóng overlay đó → return `true`
        - `isLikelyFunctionalModal(overlay)` === true → throw lỗi rõ: `"Modal lạ có form đang chặn step '${step.name}' — nghi flow thay đổi, không tự đóng"`
        - else (chỉ ảnh/text/close, không form) → đóng → return `true`
     3. Nếu helper return `true` → retry step (loc.click/fill lại).
     4. Nếu helper return `false` (không xác định được overlay) → **fall through** xuống force-retry cũ (giữ xử lý combobox `opacity:0`).
   - ⚠️ KHÔNG xoá nhánh `{ force: true }` hiện có — combobox dựa vào nó.

**Exit gate**:
- [ ] `npx tsc --noEmit` pass
- [ ] Existing tests pass
- [ ] 1 helper `tryReactiveDismiss` dùng chung cho cả click(:297)+fill(:334) — không copy-paste
- [ ] Reactive dismiss chạy TRƯỚC force-retry; force-retry `{ force: true }` vẫn còn (combobox không regress)
- [ ] Khi overlay rác chặn step → tự đóng → step retry thành công
- [ ] Khi modal có form chặn step → fail với message rõ ràng (không nuốt, không force qua)
- [ ] Khi protected modal chặn step → fail với "Protected modal" message
- [ ] Scope completeness: D4 ✅, D5 ✅

**Handoff**: `docs/_handoff/stage-2-done.md`
- Vị trí helper `tryReactiveDismiss` (file:line)
- Cách 2 handler click+fill gọi helper trước force-retry
- Error messages format (protected / functional-modal)
- Xác nhận force-retry combobox còn nguyên
- Test count

---

### Stage 3: Tests + Integration Verify + Senior Review + Security Scan

**Reads**: `docs/_handoff/stage-2-done.md`
**Assumes done**: D1–D6 implemented and type-checked

**Sub-agents**: 2 parallel + 1 senior reviewer
- Agent 1 (test writer): D7 — Unit tests cho popup dismiss logic
- Agent 2 (verifier): D8 — Integration verify `tao-phieu-nk.yaml`
- Agent 3 (senior reviewer): Review toàn bộ changes

**Key files to read**:
- `src/popup-dismiss.ts` (hoặc relevant section trong `runner.ts`)
- `src/config.test.ts` (xem test pattern/framework đang dùng)
- `package.json` → test script
- `docs/_handoff/stage-2-done.md`

**Implementation details**:

1. **D7 — Unit tests** (`src/popup-dismiss.test.ts`):
   - Test resolve-mode: `true` → aggressive, `false` → off, undefined → junk-only, string values giữ nguyên
   - Test `isLikelyFunctionalModal` (canonical): element có `input/select/textarea/[role=combobox]/table/.ant-form` → true; element chỉ có text/img/close-button → false
   - Test JUNK_SELECTORS **KHÔNG** chứa `.ant-modal-wrap/.ant-modal-mask/.modal-backdrop`
   - Test AGGRESSIVE_SELECTORS vẫn chứa `.ant-modal-*` (backward compat)
   - Test logic phân loại reactive dismiss: junk → đóng; functional (form) → throw; protected → throw
   - Lưu ý: test bản **canonical** của heuristic; nếu có bản inline trong init-script → assert 2 bản khớp (so sánh string source hoặc shared constant)

2. **D8 — Integration verify** (⚠️ tiêu chí đặc biệt):
   - Chạy `frt-test play flows/web-eho/ci/tao-phieu-nk.yaml --env ci` (hoặc dry-run/log-trace nếu không có browser/env)
   - **Tín hiệu thành công của fix engine**: modal "Thêm sản phẩm" giữ mở qua bước chọn kho (KHÔNG bị auto-close)
   - **Test tổng thể VẪN fail** — nhưng điểm fail phải **dịch sang assert danh sách kho (bug FE)**, KHÔNG còn fail ở bước "modal biến mất"
   - ⛔ **TUYỆT ĐỐI KHÔNG** sửa/nới/xoá assertion kho để ép test pass (rule #3 frt-test — assert fail = bug sản phẩm). Bug FE giữ nguyên, báo team FE.
   - Nếu không chạy được E2E thật trong sandbox → verify bằng log trace: confirm observer không match `.ant-modal-wrap` + reactive không đóng modal có form. Ghi rõ "E2E unverified — logic-trace only" trong handoff.

**Senior Review** (bắt buộc — spawn riêng 1 sub-agent):
```
TASK: Senior review + patch + scope completeness + technology freshness
ROLE: Senior architect / code reviewer
FILES TO REVIEW: src/runner.ts, src/types.ts, src/popup-dismiss.ts (nếu có), src/popup-dismiss.test.ts
PLAN DOCUMENT: docs/plans/fix-auto-dismiss-4tier.html
SDD: docs/design/fix-auto-dismiss-4tier.html
WORKING DIR: /Users/thinhph/Work/FSOFT/AIContext/ai-test/recorder-engine
REQUIREMENTS:
1. Read PLAN DOCUMENT — list every deliverable/exit criteria
2. Read all changed files
3. For each deliverable: verify code exists, is wired, has test, behavior works
4. Identify bugs, edge cases, cross-platform issues, missing error handling
5. Fix each issue directly. Tag severity [P0]–[P3]
6. If scope gaps: implement missing pieces or report SCOPE GAP clearly
7. Context7: verify @playwright/test (latest version? deprecated API?)
8. Run tests after fixes. If code changed → rerun review (loop until 0 P0–P1)
9. Report: scope checklist, tech freshness table, issues+severity, fixes, test results
DO NOT: Add features beyond plan scope, refactor beyond scope
```

**Security Scan** (bắt buộc trước commit):
- Scan toàn bộ diff (all stages combined) cho secrets/PII/credentials
- Pattern-based + semantic-based detection
- [BLOCK] → không commit, báo user. [WARN] → confirm. [INFO] → proceed

**Exit gate**:
- [ ] All unit tests pass
- [ ] Integration verify: modal "Thêm sản phẩm" stays open through kho-selection step
- [ ] Failure point dịch sang assert danh sách kho (bug FE) — KHÔNG fail ở modal-vanished step
- [ ] Assertion kho GIỮ NGUYÊN (không sửa để ép pass)
- [ ] `autoPopupDismiss: "aggressive"` backward compat verified
- [ ] Senior review: 0 P0–P1 findings remaining
- [ ] Tech freshness: no deprecated APIs in new code
- [ ] Security scan: CLEAN or NEEDS_CONFIRMATION (no BLOCK)
- [ ] SDD complete at `docs/design/fix-auto-dismiss-4tier.html`
- [ ] `src/popup-dismiss.ts` extracted; runner.ts không tăng quá mức (popup logic moved out)

---

## ORCHESTRATOR INSTRUCTIONS

1. **Đọc lại plan document** (`docs/plans/fix-auto-dismiss-4tier.html`) trước khi bắt đầu (W6 — stay on track)
2. Chạy Stage 1 bằng `subagent` tool (blocking mode, role: kiro_default)
   - Sub-agent prompt PHẢI include: "Dùng Context7 (resolve-library-id + query-docs) verify Playwright API trước khi code"
3. Verify exit gate (chạy `npx tsc --noEmit` + test runner) + scope completeness check (W3)
4. PASS → ghi handoff file → proceed Stage 2
5. FAIL → fix trong context, re-verify, KHÔNG skip
6. Lặp cho đến hết stages
7. Stage 3: senior review sub-agent (W4) + security scan (W5) trước commit

## ERROR RECOVERY

- Sub-agent fail / partial output → đọc output, identify missing items, re-run stage với scope thu hẹp
- Context approaching limit → ghi progress vào handoff file, báo user resume point
- Test fail sau stage → fix trong stage đó trước khi proceed
- Context7 unavailable → proceed with Playwright docs knowledge, flag in SDD as "unverified"
- Senior review finds P0/P1 → fix immediately, rerun tests, rerun review (loop)
- runner.ts vượt 500 LOC → extract `src/popup-dismiss.ts` ngay trong stage đang chạy

## FINAL EXIT CRITERIA

From plan document (source of truth):

1. [ ] `tao-phieu-nk.yaml` chạy với mặc định mới → modal "Thêm sản phẩm" giữ mở qua bước chọn kho; **test tổng thể vẫn fail nhưng điểm fail dịch sang assert danh sách kho (bug FE)**, không còn fail ở modal-vanished. Assertion giữ nguyên.
2. [ ] Toast/notification/OneSignal vẫn tự biến mất, không chắn step
3. [ ] Không còn false-pass; deterministic cho modal chức năng (junk dismiss vẫn async best-effort, nhưng không đua step)
4. [ ] Có test engine: modal chức năng (có form) không bị đóng; toast bị đóng; overlay lạ có form chặn step → fail rõ ràng
5. [ ] `autoPopupDismiss: aggressive` vẫn hoạt động giống hành vi cũ (backward compat); `true→aggressive`, `false→off`

**Workflow gates (bắt buộc):**
- [ ] SDD exists at `docs/design/fix-auto-dismiss-4tier.html` — complete, not placeholder
- [ ] Context7 tech freshness verified — no deprecated Playwright APIs in new code
- [ ] Senior review verdict: APPROVED (0 P0–P1, scope 100%)
- [ ] Security scan: CLEAN (no [BLOCK] findings in final diff)
- [ ] All tests pass after senior review patches
- [ ] `src/popup-dismiss.ts` extracted (popup concern tách khỏi runner.ts). Lưu ý: runner.ts vẫn >500 LOC do pre-existing — full split runner.ts là ngoài scope task này; chỉ tách phần popup.
