# @ai-test/recorder-engine

Record & Replay E2E tests bằng YAML — điều khiển qua AI Agent prompts.

## Setup (1 lần mỗi máy)

Paste vào AI agent:
```
Setup engine recorder cho tôi. Workspace folder: ~/work
```

Hoặc thủ công:
```bash
git clone git@git2.fptshop.com.vn:ai-test/recorder-engine.git ~/work/recorder-engine
cd ~/work/recorder-engine && npm install && npx playwright install chromium && npm link
```

## Quick Start — Top 5 Prompts

| # | Việc | Paste cho agent |
|---|------|----------------|
| 1 | Record test | `Record test mới cho chuỗi LAB, app rsa-lab, env ci, tên: "Tạo đơn hàng"` |
| 2 | Play test | `Play test "tao-don-hang" của chuỗi LAB, env ci` |
| 3 | Regression | `Chạy regression toàn chuỗi LAB, env ci` |
| 4 | Fix test | `Test "tao-don-hang" của LAB fail, fix giúp tôi` |
| 5 | Commit | `Commit và push test "tao-don-hang" lên repo LAB` |

📖 Đầy đủ 12 prompts: [docs/AGENT_PROMPTS.md](docs/AGENT_PROMPTS.md)

## Architecture

```
ai-test/recorder-engine   ← Repo này (engine code, maintain 1 chỗ)
ai-test/frt-test-{chain}  ← 8 chain repos (chỉ YAML tests + config)
```

Tester clone engine + chain repo. Engine = source code chung, chain = data riêng.
Update engine: `cd ~/work/recorder-engine && git pull`.

## Prerequisites

- Node.js >= 18
- Git
- AI agent (Kiro / Cursor / Claude Code / Codex)

## Troubleshooting

| Issue | Fix |
|-------|-----|
| `frt-test: command not found` | `cd ~/work/recorder-engine && npm link` |
| `Cannot find module 'tsx'` | `cd ~/work/recorder-engine && npm install` |
| Windows: npm link | Hoạt động trực tiếp — npm tự tạo .cmd/.ps1 shims |
| Engine cũ (warning hiện) | `cd ~/work/recorder-engine && git pull` |

## For core team

Xem [docs/engine-development.md](docs/engine-development.md) để maintain engine code.
