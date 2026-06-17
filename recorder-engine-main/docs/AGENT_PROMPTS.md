# Agent Prompts — Hướng dẫn sử dụng qua AI Agent

> Tester paste prompt vào AI agent (Kiro / Cursor / Claude / Codex). Agent tự gọi shell commands để thực hiện.

## Quick Start — Top 5 Prompts

### 1. Record test mới
```
Record test mới cho chuỗi LAB, app rsa-lab, env ci, tên: "Tạo đơn hàng dịch vụ XN máu"
```

### 2. Play 1 test
```
Play test "tao-don-hang" của chuỗi LAB, env ci
```

### 3. Chạy regression
```
Chạy regression chuỗi LAB, app rsa-lab, env ci
```

### 4. Fix test fail
```
Test "tao-don-hang" của LAB chạy fail. Fix giúp tôi.
```

### 5. Commit + push
```
Commit và push test "tao-don-hang" lên repo LAB
```

---

## Tất cả Prompts

| # | Task | Trigger phrase | File |
|---|------|----------------|------|
| 01 | Setup engine lần đầu | "Setup engine recorder cho tôi" | [01-setup-engine.md](prompts/01-setup-engine.md) |
| 02 | Onboard chuỗi mới | "Onboard chuỗi {TÊN}, apps: ..." | [02-onboard-new-chain.md](prompts/02-onboard-new-chain.md) |
| 03 | Thêm app vào chuỗi | "Thêm app {TÊN} cho chuỗi {CHAIN}, URL: ..." | [03-add-app-to-chain.md](prompts/03-add-app-to-chain.md) |
| 04 | Record test mới | "Record test mới cho {CHAIN}, app {APP}, tên: ..." | [04-record-test.md](prompts/04-record-test.md) |
| 05 | Play test | "Play test {TÊN} của {CHAIN}" | [05-play-test.md](prompts/05-play-test.md) |
| 06 | Fix test fail | "Test {TÊN} fail, fix giúp tôi" | [06-fix-failing-test.md](prompts/06-fix-failing-test.md) |
| 07 | Regression hàng ngày | "Chạy regression toàn chuỗi {CHAIN}" | [07-daily-regression.md](prompts/07-daily-regression.md) |
| 08 | Commit & push | "Commit và push test {TÊN} lên repo {CHAIN}" | [08-commit-and-push.md](prompts/08-commit-and-push.md) |
| 09 | Update engine | "Update engine recorder lên version mới nhất" | [09-update-engine.md](prompts/09-update-engine.md) |
| 10 | List & cleanup | "Liệt kê tests của {CHAIN}" | [10-list-and-cleanup-tests.md](prompts/10-list-and-cleanup-tests.md) |
| 11 | Migrate multi-env | "Migrate chuỗi {CHAIN} sang multi-env" | [11-migrate-multi-env.md](prompts/11-migrate-multi-env.md) |
| 12 | Clone test giữa env | "Clone test {TÊN} từ CI sang UAT cho {APP}" | [12-clone-flow.md](prompts/12-clone-flow.md) |

---

## Lưu ý

- Prompts **agent-agnostic** — hoạt động với bất kỳ AI agent nào có quyền chạy shell commands.
- Thay `{CHAIN}` = tên chuỗi (lab, vac, ict, pharmacy, ...), `{APP}` = tên app trong config.
- Nếu agent stuck → mở file prompt tương ứng, đọc "Common errors & fix".
- **Steering (đồ chơi sẵn cho agent):** setup engine (prompt 01) tự copy playbook `docs/agent-context/frt-test.md` vào steering của workspace (`.kiro/steering/` · `AGENTS.md` · `CLAUDE.md` · `.cursor/rules/`). Workspace/chuỗi đã onboard từ trước mà chưa có → chạy: **"Cài playbook frt-test vào steering của workspace này"**. Sau đó mở workspace là agent tự biết dùng frt-test.
