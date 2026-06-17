# AI Test Recorder Restructure — Execution Prompt

> Auto-generated from plan: `docs/plans/ai-test-recorder-restructure.html`
> Stages: 3 | Total deliverables: 18
> Scope: Phase 1 + Phase 2 của plan (Phase 3-6 defer — cần GitLab repos tồn tại + user thao tác browser)
> Variant: A (Kiro CLI có sub-agent tool)

## TASK

Refactor `frt-test-recorder-template/` thành standalone engine package `recorder-engine/` (parallel folder, không destroy template). Engine có:
- CLI binary `frt-test` (npm link compatible)
- Subcommand `scaffold-chain` + `add-app` để onboard chuỗi mới tự động
- 10 prompt files trong `docs/prompts/` cho AI agent (tester paste prompt thay vì gõ shell)
- README mới + chain-template subfolder + engine-development.md

Output: folder `recorder-engine/` ở workspace root, ready để user push lên `git2.fptshop.com.vn/ai-test/recorder-engine` khi IT tạo xong group.

## WORKING DIR

`/Users/thinhph/Work/FSOFT/AIContext/ai-native-client/`

## CONTEXT (≤ 5 dòng)

Source: `frt-test-recorder-template/` (Node.js + tsx + Playwright, CLI dùng commander, 7 src files). Engine code đã viết theo pattern thin-CLI (paths qua `process.cwd()`) → có thể tách package mà KHÔNG cần refactor logic. Tester là user cuối, không gõ shell — họ paste prompt cho AI agent. Plan chốt: D1=git+npm link distribution, D2=raw TS+tsx (no build), D3=scaffold qua CLI subcommand, D7=chain-template inside engine, D8=docs primary là prompt cookbook.

## CONSTRAINTS

- KHÔNG xóa / sửa folder `frt-test-recorder-template/` — tạo `recorder-engine/` parallel.
- Engine giữ raw TypeScript + tsx runtime (không build `dist/`).
- Mọi paths trong engine resolve qua `process.cwd()` — đừng hardcode `__dirname`.
- Prompts phải agent-agnostic — KHÔNG reference Kiro-specific tools (subagent, MCP). Mọi agent có shell + file edit access đều execute được.
- Backward compat: command surface giữ nguyên (`record`, `play`, `list`, `convert` với same flags).
- Cross-platform: macOS / Linux primary. Windows qua Git Bash hoặc WSL — flag bất kỳ Windows-specific issue trong SDD.
- Output dir validation cho scaffold: refuse nếu output dir tồn tại và non-empty.
- Engine version banner phải in mỗi run, warn nếu commit local cũ > 14 ngày so với HEAD remote (cached 1 lần/ngày).

## EXECUTION — 3-STAGE CHAIN

> ⚠️ Đây là orchestrator prompt. Không implement trực tiếp.
> Chạy từng stage tuần tự bằng `subagent` tool (blocking mode). Mỗi stage = 1 hoặc 2 sub-agents.

---

### Stage 1: Engine Refactor

**Sub-agents**: 1
- Agent (kiro_default, role: Node.js engineer): D1–D5 — tạo `recorder-engine/` folder với engine code, bin wrapper, version banner, .gitignore.

**Context7 validation** (agent tự chạy trước khi sửa code):
- `resolve-library-id` + `query-docs` cho: tsx (4.x), commander (12.x), js-yaml (4.x), Playwright (1.44+), TypeScript (5.9+).
- Verify API hiện tại trong `src/cli.ts`, `src/runner.ts`, `src/recorder.ts` không dùng deprecated patterns.
- **Nếu Context7 unavailable**: proceed với version hiện tại trong `frt-test-recorder-template/package.json`, flag "unverified" trong SDD.

**SDD**: Ghi vào `docs/design/ai-test-recorder-restructure.html` (Apple 2026 style, light/dark, inline CSS) — section "Stage 1 — Engine Refactor" với:
- Files copied / created / modified
- Decisions taken (vd: version banner format, staleness threshold 14 ngày)
- Context7 verified versions table

**Key files để agent đọc** (agent tự đọc, KHÔNG paste vào prompt):
- `frt-test-recorder-template/src/cli.ts`
- `frt-test-recorder-template/src/config.ts`
- `frt-test-recorder-template/package.json`
- `frt-test-recorder-template/tsconfig.json`
- `docs/plans/ai-test-recorder-restructure.html` (section 4 Architecture, section 7 Engine changes)

**Deliverables**:

| ID | Mô tả | Output |
|----|-------|--------|
| D1 | Tạo folder `recorder-engine/` parallel với template, copy `src/`, `tsconfig.json` y nguyên | `recorder-engine/src/{cli,config,converter,recorder,reporter,runner,types}.ts`, `recorder-engine/tsconfig.json` |
| D2 | Tạo `recorder-engine/package.json` mới với name=`@ai-test/recorder-engine`, bin entry, scripts, deps copy từ template | `recorder-engine/package.json` |
| D3 | Tạo `recorder-engine/bin/frt-test` shell wrapper (executable, BSD-readlink-compatible cho macOS, dùng `node --import tsx`) | `recorder-engine/bin/frt-test` (mode 0755) |
| D4 | Add version banner + staleness warning đầu `src/cli.ts` (in trước `program.parse()`, dùng `git log -1` + `git rev-list HEAD..@{u}` cached qua file `.engine-version-cache.json` ở engine root) | `recorder-engine/src/cli.ts` updated |
| D5 | Tạo `recorder-engine/.gitignore` (node_modules/, .engine-version-cache.json, screenshots/*, !screenshots/.gitkeep, reports/*, !reports/.gitkeep, .env, .DS_Store) | `recorder-engine/.gitignore` |

**Exit gate** (verify ALL trước khi sang Stage 2):

- [ ] `cd recorder-engine && npm install` exit 0
- [ ] `node --import tsx recorder-engine/src/cli.ts --version` in version banner (format: `@ai-test/recorder-engine v1.0.0 — commit <hash> (<date>)`)
- [ ] `recorder-engine/bin/frt-test --version` runs (executable mode + shebang OK)
- [ ] `ls recorder-engine/` KHÔNG có folders: `flows/`, `config/`, `screenshots/`, `reports/`, `examples/`
- [ ] `recorder-engine/package.json` chứa `"name": "@ai-test/recorder-engine"` + `"bin": { "frt-test": "./bin/frt-test" }`
- [ ] All template src files copied đầy đủ (compare `wc -l recorder-engine/src/*.ts` vs template)

**Handoff**: Ghi vào `docs/_handoff/stage-1-done.md`:
- Files created/modified (full path list)
- `frt-test --version` output (paste literal)
- Context7 versions verified table (hoặc "Context7 unavailable, using template versions")
- Engine entry point confirmed (`bin/frt-test` → `node --import tsx src/cli.ts`)

---

### Stage 2: Scaffold Command + Prompt Cookbook

**Reads**: `docs/_handoff/stage-1-done.md`

**Assumes done** (KHÔNG re-implement):
- `recorder-engine/` folder exists với src/, package.json, bin/, tsconfig.json, .gitignore
- `frt-test --version` works
- Engine deps installed (npm install đã chạy)

**Sub-agents**: 2 parallel

**Sub-agent 2A** (kiro_default, role: Node.js engineer — Code):

Deliverables:

| ID | Mô tả | Output |
|----|-------|--------|
| D6 | Tạo `chain-template/` folder với templates: `README.md.tpl` (placeholders `{{CHAIN_NAME}}`, `{{CHAIN_LOWER}}`, `{{APPS_TABLE}}`, `{{DEFAULT_APP}}`); `config/environments.yaml.tpl` (placeholders `{{APPS_YAML}}`, `{{DEFAULT_APP}}`); `.gitignore`; `flows/_shared/login-placeholder.yaml` (copy từ template hiện tại); `screenshots/.gitkeep`; `reports/.gitkeep` | `recorder-engine/chain-template/` (5+ files) |
| D7 | Implement `src/scaffold.ts` với functions exported: `scaffoldChain(opts)`, `addAppToChain(opts)`, `renderTemplate(content, vars)`, `copyTemplateDir(src, dst, vars)`, `execGit(cwd, args)`. Dùng built-in modules (`fs`, `path`, `child_process`) — KHÔNG add dep mới | `recorder-engine/src/scaffold.ts` |
| D8 | Wire `scaffold-chain` + `add-app` subcommands trong `src/cli.ts` qua commander. Flags: scaffold-chain --name --display --apps "k=v,k=v" --output --remote --push; add-app --name --url. Validate: name kebab-case, apps format `name=url`, output dir empty | `recorder-engine/src/cli.ts` updated |
| D9 | Unit tests cho scaffold logic (Node.js native test runner `node --test`): renderTemplate substitutes correctly, copyTemplateDir creates files với content rendered, scaffoldChain validation (refuse non-empty output dir), addAppToChain refuses duplicate app | `recorder-engine/src/scaffold.test.ts` |

Context7 validation (2A): verify Node.js `node:test` API + `child_process.execFile` Promise pattern (hoặc dùng `util.promisify`).

**Sub-agent 2B** (kiro_default, role: Technical writer — Documentation):

Deliverables:

| ID | Mô tả | Output |
|----|-------|--------|
| D10 | Viết 10 prompt files trong `docs/prompts/01..10-*.md` theo format chuẩn (xem plan section 6: trigger / prerequisites / agent workflow steps / success criteria / common errors table). Files: `01-setup-engine.md`, `02-onboard-new-chain.md`, `03-add-app-to-chain.md`, `04-record-test.md`, `05-play-test.md`, `06-fix-failing-test.md`, `07-daily-regression.md`, `08-commit-and-push.md`, `09-update-engine.md`, `10-list-and-cleanup-tests.md` | `recorder-engine/docs/prompts/*.md` (10 files) |
| D11 | Viết `docs/AGENT_PROMPTS.md` — index 10 prompts (table) + inline content cho top 5 phổ biến (record, play, regression, fix, commit) | `recorder-engine/docs/AGENT_PROMPTS.md` |
| D12 | Viết mới `README.md` cho engine repo: tóm tắt 80 dòng, top 5 quick start prompts inline, link tới `docs/AGENT_PROMPTS.md` + `docs/prompts/` cho chi tiết, prerequisites (Node 18+, Git, AI agent), troubleshooting npm link issues (Windows note) | `recorder-engine/README.md` |
| D13 | Viết `docs/engine-development.md` cho core team: cách add subcommand mới, cách extend YAML schema, cách release version mới (git tag + CHANGELOG), unit test workflow | `recorder-engine/docs/engine-development.md` |
| D14 | Move `frt-test-recorder-template/examples/ict/*` → `recorder-engine/docs/examples/ict/` làm reference (giữ nguyên YAML files), thêm README.md ngắn giải thích đây là examples không phải tests active | `recorder-engine/docs/examples/ict/*.yaml` + `recorder-engine/docs/examples/README.md` |

Cả 2 sub-agents ghi section riêng vào SDD `docs/design/ai-test-recorder-restructure.html` ("Stage 2A — Scaffold Code", "Stage 2B — Documentation").

**Key files để agents đọc**:
- `recorder-engine/src/cli.ts` (kết quả Stage 1, để biết wire subcommand vào đâu)
- `frt-test-recorder-template/flows/_shared/login-placeholder.yaml` (copy sang chain-template)
- `frt-test-recorder-template/docs/onboarding-new-chain.md` (reference format prompt cũ)
- `docs/plans/ai-test-recorder-restructure.html` (section 6 Documentation cookbook, section 8 Chain template, section 9 Scaffold command)

**Exit gate** (verify ALL):

- [ ] `cd recorder-engine && node --import tsx src/cli.ts scaffold-chain --name dummy-test --apps "test-app=https://example.com" --output /tmp/dummy-test-chain --display "Dummy Test"` exit 0
- [ ] `/tmp/dummy-test-chain/config/environments.yaml` chứa `test-app:` với baseUrl đúng
- [ ] `/tmp/dummy-test-chain/README.md` rendered (không còn placeholders `{{...}}`)
- [ ] `/tmp/dummy-test-chain/flows/test-app/_shared/` exists
- [ ] `cd recorder-engine && node --import tsx --test src/scaffold.test.ts` all tests pass
- [ ] 10 prompt files exist trong `recorder-engine/docs/prompts/` — mỗi file có đủ 5 sections (Trigger / Prerequisites / Agent workflow / Success criteria / Common errors)
- [ ] `recorder-engine/README.md` exists, ≤ 100 dòng, có top 5 prompts inline
- [ ] `recorder-engine/docs/AGENT_PROMPTS.md` exists với 10-row index table
- [ ] `recorder-engine/docs/engine-development.md` exists
- [ ] `recorder-engine/docs/examples/ict/` chứa YAML files copy từ template
- [ ] Cleanup: `rm -rf /tmp/dummy-test-chain` đã chạy

**Handoff**: Ghi vào `docs/_handoff/stage-2-done.md`:
- Files created (full path list, count by type: .ts, .md, .tpl, .yaml)
- Scaffold dummy test output (paste tree of /tmp/dummy-test-chain trước khi cleanup)
- Test count + pass/fail (`node --test` output summary)
- 10 prompt files với word count mỗi file
- SDD path

---

### Stage 3: Senior Review + Tech Freshness + Security Scan

**Reads**: `docs/_handoff/stage-1-done.md`, `docs/_handoff/stage-2-done.md`

**Assumes done**:
- All Stage 1 deliverables (D1–D5)
- All Stage 2 deliverables (D6–D14)
- All tests pass

**Sub-agents**: 1 senior reviewer

**Sub-agent 3** (kiro_default, role: Senior architect — model cao nhất available):

```
TASK: Senior review + patch + scope completeness check + technology freshness check + security scan
ROLE: Senior architect / code reviewer / security reviewer
FILES TO REVIEW: tất cả files trong recorder-engine/ (sau Stage 1 + 2)
PLAN DOCUMENT: /Users/thinhph/Work/FSOFT/AIContext/ai-native-client/docs/plans/ai-test-recorder-restructure.html
SDD: /Users/thinhph/Work/FSOFT/AIContext/ai-native-client/docs/design/ai-test-recorder-restructure.html
WORKING DIR: /Users/thinhph/Work/FSOFT/AIContext/ai-native-client/
CONTEXT: Engine package extracted from frt-test-recorder-template, prepared for ai-test/recorder-engine repo. Phase 1+2 only (Phase 3-6 defer).

REQUIREMENTS:

1. Đọc PLAN DOCUMENT — list từng deliverable D1–D14, verify code/file tồn tại + đúng spec.

2. Đọc tất cả files trong recorder-engine/ — đặc biệt:
   - src/scaffold.ts (logic mới, high risk)
   - src/cli.ts (version banner, subcommand wiring)
   - bin/frt-test (shell wrapper, BSD compat)
   - chain-template/ (template rendering correctness)
   - 10 prompt files (clarity, agent-agnostic)
   - README.md (top 5 prompts hợp lý)

3. Identify bugs / edge cases / cross-platform issues / missing error handling. Tag mỗi finding [P0]–[P3].

4. Fix mỗi issue trực tiếp (KHÔNG chỉ comment).

5. Scope completeness check (W3 — bắt buộc):
   - Cho mỗi deliverable D1–D14 trong plan: ✅ Done hoặc ❌ Missing
   - Đặc biệt verify: scaffold-chain end-to-end works, prompt files đủ 5 sections, version banner format đúng
   - Nếu có ❌ → implement luôn (không skip)

6. Technology freshness (W4 — Context7):
   - Verify mỗi dependency trong package.json: latest stable? API deprecated?
   - tsx (4.x), commander (12.x), js-yaml (4.x), playwright (1.44+), @types/node, typescript (5.9+)
   - Nếu deprecated API → patch sang latest
   - Nếu Context7 unavailable → ghi "unverified" trong report
   - Output bảng: library | version-used | latest | status (OK/OUTDATED/DEPRECATED) | action

7. Security scan (W5 — bắt buộc trước commit):
   - Scan toàn bộ recorder-engine/ folder cho: AI keys (sk-, ghp_, glpat-), AWS keys, private keys, real PII, real internal hostnames, hardcoded passwords
   - Pattern-based + semantic-based
   - Tag [BLOCK] / [WARN] / [INFO]
   - Đặc biệt check: prompt files có chứa example credentials không, chain-template/.tpl có placeholder thật không (vd email công ty thật trong README.md.tpl)

8. Run tests sau mỗi fix. Nếu code thay đổi → rerun review (loop) cho đến 0 P0–P1 + security CLEAN.

9. Cập nhật SDD docs/design/ai-test-recorder-restructure.html với section "Stage 3 — Senior Review":
   - Findings table (severity / file / line / description / fix taken)
   - Tech freshness table
   - Scope completeness checklist (D1–D14 ✅/❌)
   - Security scan summary

REPORT FORMAT:
## SCOPE COMPLETENESS
| Deliverable | Status | Note |
|-------------|--------|------|
| D1 — recorder-engine/src/ copied | ✅ | ... |
| ... | | |

## TECH FRESHNESS
| Library | Used | Latest | Status | Action |
|---------|------|--------|--------|--------|

## FINDINGS & FIXES
| ID | Severity | File:Line | Issue | Fix Applied |
|----|----------|-----------|-------|-------------|

## SECURITY SCAN
- Total files: N
- [BLOCK]: count + list
- [WARN]: count + list
- [INFO]: count
- Verdict: BLOCKED / NEEDS_CONFIRMATION / CLEAN

## FINAL VERDICT
APPROVED (0 P0-P1, scope 100%, security CLEAN) hoặc INCOMPLETE (list reasons)

DO NOT: Add features beyond plan scope (Phase 3-6), refactor logic core của engine code (cli/runner/recorder), change YAML schema.
```

**Exit gate**:

- [ ] 0 P0–P1 findings remaining (P2–P3 OK nếu document trong SDD)
- [ ] Tech freshness: no deprecated APIs in new code (D7 scaffold.ts, D8 cli.ts changes)
- [ ] Security scan: CLEAN hoặc NEEDS_CONFIRMATION (no BLOCK)
- [ ] Scope completeness: 14/14 deliverables ✅
- [ ] All tests pass: `cd recorder-engine && node --test` exit 0
- [ ] SDD complete tại `docs/design/ai-test-recorder-restructure.html` — đầy đủ 3 sections (Stage 1, Stage 2A+2B, Stage 3)
- [ ] Final verdict: APPROVED

**Handoff**: Ghi vào `docs/_handoff/stage-3-done.md` — final report (paste senior review output).

---

## ORCHESTRATOR INSTRUCTIONS

1. **Đọc lại plan document** trước khi bắt đầu (W6 — stay on track):
   - `docs/plans/ai-test-recorder-restructure.html`
   - List 14 deliverables (D1–D14) trong scope (Phase 1+2 only).

2. **Stage 1**: Chạy bằng `subagent` tool (blocking mode, role: kiro_default).
   - Sub-agent prompt PHẢI include: "Dùng Context7 (resolve-library-id + query-docs) verify dependencies trước khi code. Nếu Context7 unavailable, proceed với versions hiện tại + flag 'unverified' trong SDD."
   - Sau khi sub-agent return → orchestrator chạy exit gate verify (commands trong section "Exit gate" của Stage 1) trực tiếp.
   - Scope completeness check (W3): đọc plan, verify D1–D5 = ✅.
   - Nếu PASS → ghi `docs/_handoff/stage-1-done.md` → proceed Stage 2.
   - Nếu FAIL → fix trong context hiện tại, re-verify, KHÔNG skip.

3. **Stage 2**: Chạy 2 sub-agents parallel (1 lần `subagent` call với 2 stages, depends_on: [Stage 1]).
   - Mỗi sub-agent prompt include Context7 instruction.
   - Sub-agent 2A focus code (scaffold.ts, chain-template, tests).
   - Sub-agent 2B focus docs (10 prompts, AGENT_PROMPTS.md, README, engine-development.md, examples).
   - Sau khi cả 2 return → exit gate verify + scope check D6–D14.
   - Cleanup `/tmp/dummy-test-chain` trong exit gate.
   - PASS → ghi `stage-2-done.md` → proceed Stage 3.

4. **Stage 3**: Chạy 1 sub-agent senior reviewer.
   - Sub-agent dùng prompt block đầy đủ (xem Stage 3 section trên).
   - Sub-agent có thẩm quyền tự patch + rerun tests + loop.
   - Sau return → orchestrator verify exit gate (0 P0-P1, security CLEAN, scope 100%).
   - Nếu verdict APPROVED → ghi `stage-3-done.md`.
   - Nếu INCOMPLETE → identify gap, re-run Stage 3 với scope thu hẹp.

5. **Final verify**: Check ALL exit criteria từ plan document gốc (Definition of Done, section 11):
   - recorder-engine/ folder structure đúng
   - Engine code chỉ tồn tại ở recorder-engine/ (frt-test-recorder-template/ vẫn nguyên — Phase 6 defer)
   - 10 prompt files trong docs/prompts/
   - README quick start với top 5 prompts
   - SDD complete tại docs/design/

6. **NO COMMIT**: Phase 1+2 KHÔNG commit/push lên git. User sẽ tự push lên `ai-test/recorder-engine` khi IT process xong group. Nếu user yêu cầu commit local sau khi review → chạy security scan trước (W5).

## ERROR RECOVERY

- Sub-agent fail / partial output → đọc output, identify missing items, re-run stage với scope thu hẹp (vd: chỉ D7 nếu D6, D8, D9 đã done).
- Context approaching limit → ghi progress vào handoff file, báo user resume point.
- Test fail sau Stage 2 → fix trong Stage 2 trước khi proceed Stage 3.
- Context7 unavailable → proceed với versions trong template package.json, flag in SDD as "unverified, used template versions".
- Senior review finds [P0]/[P1] → fix immediately, rerun tests, rerun review (loop).
- Security scan [BLOCK] → DỪNG, báo user. KHÔNG tự xóa credentials. User phải rotate + remove + re-scan.

## FINAL EXIT CRITERIA (Definition of Done — Phase 1+2 scope)

(Copy từ plan document section 11, filtered cho Phase 1+2)

- [ ] `recorder-engine/` folder exists ở workspace root, structure đúng plan section 4
- [ ] Engine code chỉ ở `recorder-engine/src/` — folder `frt-test-recorder-template/src/` vẫn nguyên (chưa cleanup, Phase 6 defer)
- [ ] 10 prompt files trong `recorder-engine/docs/prompts/` đầy đủ format
- [ ] `recorder-engine/README.md` có top 5 quick start prompts
- [ ] `recorder-engine/docs/AGENT_PROMPTS.md` exists
- [ ] `recorder-engine/chain-template/` exists với templates
- [ ] `frt-test scaffold-chain` works end-to-end (verified bằng dummy chain trong Stage 2 exit gate)
- [ ] All tests pass

**Workflow gates (bắt buộc thêm vào exit criteria — W1-W5):**

- [ ] SDD exists tại `docs/design/ai-test-recorder-restructure.html` — complete (3 sections), không placeholder
- [ ] Context7 tech freshness verified hoặc flagged "unverified" — no deprecated APIs in new code
- [ ] Senior review verdict: APPROVED (0 P0-P1, scope 100%)
- [ ] Security scan: CLEAN (no [BLOCK] findings in recorder-engine/)
- [ ] All tests pass after senior review patches

**Defer (NOT in this execution):**
- Phase 3 (push lên ai-test/recorder-engine GitLab) — chờ IT process group
- Phase 4 (onboard LAB end-to-end với tester thật record browser test)
- Phase 5 (onboard 7 chuỗi còn lại)
- Phase 6 (cleanup `frt-test-recorder-template/` cũ trong workspace)
- Phase 7 (CI regression integration)

User sẽ chạy các phase trên manually / qua prompt riêng sau khi:
1. IT tạo group `ai-test` + 9 repos trên git2.fptshop.com.vn
2. User review `recorder-engine/` folder + approve
3. User push `recorder-engine/` lên `ai-test/recorder-engine`
4. User onboard từng chuỗi qua prompt #02 trong `docs/prompts/02-onboard-new-chain.md`
