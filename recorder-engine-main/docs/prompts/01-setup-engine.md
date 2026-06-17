# Setup Engine Recorder — Agent Instructions

> **Trigger:** "Setup engine recorder cho tôi"
> Agent tự làm hết.

## Trigger prompt

```
Setup engine recorder cho tôi.
Workspace folder: ~/work
```

## Prerequisites

- Máy có Node.js >= 18, Git, npm
- Tài khoản GitLab có quyền đọc repo (clone qua **HTTPS** — KHÔNG cần SSH key). Nếu bật 2FA → tạo Personal Access Token (scope `read_repository`).
- AI agent có quyền chạy shell commands

## Agent workflow

### Step 1: Verify prerequisites
```bash
node -v    # >= 18
git --version
npm -v
```
Nếu thiếu → báo user cài trước, KHÔNG tiếp tục.

### Step 2: Clone engine (qua HTTPS)
```bash
# Dùng HTTPS — KHÔNG dùng SSH. Git sẽ hỏi user/password (hoặc Personal Access Token nếu bật 2FA).
git clone https://git2.fptshop.com.vn/ai-test/recorder-engine.git ~/work/recorder-engine
```

### Step 3: Install dependencies
```bash
cd ~/work/recorder-engine
npm install
```

### Step 4: Install Playwright browser
```bash
npx playwright install chromium
```

### Step 5: Register global command
```bash
npm link
```

### Step 6: Verify
```bash
frt-test --version
```
Phải in: `@ai-test/recorder-engine v1.x.x — commit <hash> (<date>)`

### Step 7: Cài playbook vào STEERING của workspace (BẮT BUỘC — để agent tự biết "đồ chơi")

> Mục tiêu: sau setup, mở workspace bằng BẤT KỲ agent nào cũng tự biết có `frt-test` + playbook,
> KHÔNG cần user nhắc. Copy file playbook chuẩn của engine vào steering của workspace user đang đứng.

1. Xác định **WORKSPACE** = thư mục user mở agent để làm việc (vd `~/work`, hoặc 1 chain repo như `~/work/frt-test-lab`). Hỏi user nếu chưa rõ.
2. Xác định **ENGINE** = nơi vừa clone (vd `~/work/recorder-engine`). File nguồn: `<ENGINE>/docs/agent-context/frt-test.md`.
3. Copy playbook vào steering theo agent user đang dùng (làm theo agent có sẵn; nếu chưa rõ → tạo cả `AGENTS.md` + `.kiro/steering/`):

```bash
ENGINE=~/work/recorder-engine
WORKSPACE=.            # thư mục user đang đứng

# Kiro (auto-load .kiro/steering/*.md)
mkdir -p "$WORKSPACE/.kiro/steering"
cp "$ENGINE/docs/agent-context/frt-test.md" "$WORKSPACE/.kiro/steering/frt-test.md"

# Cross-agent chuẩn chung (Codex & nhiều agent đọc AGENTS.md)
cp "$ENGINE/docs/agent-context/frt-test.md" "$WORKSPACE/AGENTS.md"

# Claude Code (đọc CLAUDE.md)
cp "$ENGINE/docs/agent-context/frt-test.md" "$WORKSPACE/CLAUDE.md"

# Cursor (đọc .cursor/rules/*.mdc) — tuỳ chọn
mkdir -p "$WORKSPACE/.cursor/rules"
cp "$ENGINE/docs/agent-context/frt-test.md" "$WORKSPACE/.cursor/rules/frt-test.mdc"
```

4. Báo user: "Đã cài playbook vào steering của workspace. Mở lại workspace → agent tự biết cách dùng frt-test."

> 💡 **Đã setup engine từ trước (retrofit)?** Chỉ cần chạy riêng Step 7 này cho workspace đang đứng — không phải clone/install lại. Trigger: "Cài playbook frt-test vào steering của workspace này."

## Success criteria

- `frt-test --version` in version banner (exit 0)
- `frt-test --help` liệt kê: record, play, convert, list, scaffold-chain, add-app
- `which frt-test` trỏ về symlink trong npm global bin
- **Workspace có file steering** (`.kiro/steering/frt-test.md` và/hoặc `AGENTS.md`/`CLAUDE.md`) trỏ tới playbook engine → agent mở workspace tự biết dùng frt-test

## Common errors & fix

| Error | Cause | Fix |
|-------|-------|-----|
| `Permission denied` khi npm link | npm global dir cần sudo | `sudo npm link` hoặc fix npm prefix: `npm config set prefix ~/.npm-global` + add PATH |
| `Authentication failed` / `403` khi clone | Sai user/password hoặc thiếu quyền repo | Dùng HTTPS + Personal Access Token (GitLab → Settings → Access Tokens, scope `read_repository`). Kiểm tra account có quyền đọc repo. |
| `git: 'remote-https' is not a command` | Git thiếu HTTPS support (hiếm) | Cài lại Git bản đầy đủ (Git for Windows / `apt install git`) |
| `node: command not found` | Node.js chưa cài | Cài qua nvm: `nvm install 18` |
| `Cannot find module 'tsx'` | npm install chưa chạy | `cd ~/work/recorder-engine && npm install` |
