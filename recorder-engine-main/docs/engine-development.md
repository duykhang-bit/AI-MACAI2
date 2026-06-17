# Engine Development Guide

> Dành cho core team maintain `@ai-test/recorder-engine`.

## Cấu trúc code

```
src/
├── cli.ts          — Commander setup, version banner, subcommands
├── recorder.ts     — Playwright recorder logic (browser → YAML)
├── runner.ts       — YAML parser + Playwright executor
├── reporter.ts     — Console + HTML report output
├── converter.ts    — .spec.ts → YAML converter
├── config.ts       — Load config/environments.yaml (v0/v1/v2 support)
├── types.ts        — Shared TypeScript interfaces
├── scaffold.ts     — scaffold-chain + add-app logic
├── migrate.ts      — v1→v2 config migration
├── add-env.ts      — Set URL cho env
├── envs.ts         — List environments status
├── clone-flow.ts   — Clone YAML giữa env folders
└── *.test.ts       — Unit tests (config, scaffold, migrate, clone-flow)
```

## Thêm subcommand mới

1. Tạo file `src/{command}.ts` — export function + `registerXxxCommands(program)`.
2. Import + call `registerXxxCommands(program)` trong `src/cli.ts` (trước `program.parse()`).
3. Viết tests trong `src/{command}.test.ts`.
4. Thêm prompt file `docs/prompts/NN-{command}.md` hướng dẫn tester dùng.
5. Update `docs/AGENT_PROMPTS.md` index table.

## Release process

1. Sửa code + tests pass.
2. Update `package.json` version (semver).
3. Update CHANGELOG.md (nếu có).
4. Commit: `git commit -m "feat: {mô tả}" && git push`.
5. Tag: `git tag v1.x.x && git push --tags`.
6. Báo tester: "Update engine: `cd ~/work/recorder-engine && git pull`".

## Conventions

- Paths luôn resolve qua `process.cwd()` — engine code KHÔNG hardcode absolute paths.
- KHÔNG add external deps trừ khi thật sự cần — current deps: commander, js-yaml, playwright, tsx, typescript.
- Template rendering dùng simple `{{VAR}}` substitution (no template engine dep).
- Cross-platform: `bin/frt-test` là **Node.js launcher** (`#!/usr/bin/env node` → spawn `tsx` + `src/cli.ts`). `npm link` tự sinh `.cmd`/`.ps1` shim trên Windows → chạy native, KHÔNG cần Git Bash/WSL. Nếu sửa `bin/frt-test`: giữ nguyên Node.js (không quay về bash) để cross-platform.

## Recorder / Runner auto-behaviors

> Hành vi tự động engine áp dụng khi record/play (cập nhật 2026-06-05). Chi tiết design: `docs/design/engine-reliability-improvements.html`.

| Behavior | Where | Mô tả |
|----------|-------|-------|
| No hardcoded delay | `recorder.ts actionToStep()` | KHÔNG chèn `delay` vào step. Runner đã chờ element (visible/attached) trước mỗi action. Field `delay` vẫn giữ cho manual override + backward-compat flow cũ. |
| Popup auto-dismiss | `runner.ts` (addInitScript MutationObserver) | Tự đóng modal/overlay/toast giữa flow. Guard bỏ qua nút nguy hiểm (`xóa/delete`). Tắt bằng `FlowConfig.autoPopupDismiss: false`. |
| Backdrop click skip | `recorder.ts pointerup` | Không ghi step khi user click vào mask/backdrop để tắt popup. |
| Volatile-text assert | `recorder.ts` assert mode | Assert giá trị động (tiền/tồn kho/%/ngày — `isVolatileText`) tự hạ `assertType: text` → `visible`. |
| Hover-to-reveal | `recorder.ts mouseover` + `runner.ts hover` | Recorder track hover (debounce 150ms), emit `hover` step trước `click` khi hover-target là ancestor của click-target trong cửa sổ 1500ms. Runner chờ visible + hover + 300ms cho CSS transition. |

## YAML schema

Xem `src/types.ts` cho flow schema. Breaking changes → bump major version.

## Config schema versioning

Engine hỗ trợ 3 schema cho `config/environments.yaml`:

| Schema | Detection | Mô tả |
|--------|-----------|-------|
| v0 | Có key `environments` ở root | Legacy cũ nhất. Auto-convert thành apps. |
| v1 | Có `apps.{name}.baseUrl: string` | Schema 8 chuỗi ban đầu. Single URL per app. |
| v2 | `schema_version: 2` hoặc `apps.{name}.urls: {...}` | Multi-env. URL per (app, env). |

**Policy:**
- Engine LUÔN đọc được cả 3 schema (backward-compat).
- Chuỗi mới onboard dùng v2 (scaffold tạo v2 mặc định).
- Chuỗi cũ migrate opt-in bằng `frt-test migrate-config`.
- `--env` chỉ required cho schema v2. Schema v1 chạy không cần `--env`.

## Chạy tests

```bash
node --import tsx --test src/config.test.ts src/scaffold.test.ts src/migrate.test.ts src/clone-flow.test.ts
```
