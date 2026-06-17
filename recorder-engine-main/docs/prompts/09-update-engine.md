# Update Engine — Agent Instructions

> **Trigger:** "Update engine recorder lên version mới nhất"

## Trigger prompt

```
Update engine recorder lên version mới nhất
```

## Prerequisites

- Engine đã setup trước đó (prompt 01)

## Agent workflow

### Step 1: Pull latest engine code
```bash
cd ~/work/recorder-engine
git pull
```

### Step 2: Install new deps (nếu có)
```bash
npm install
```

### Step 3: Verify
```bash
frt-test --version
```
Version banner phải cập nhật (commit hash mới, date mới).

## Success criteria

- `git pull` thành công
- `frt-test --version` in commit mới (date = today hoặc gần đây)
- Không cần re-link (`npm link` symlink vẫn valid)

## Common errors & fix

| Error | Cause | Fix |
|-------|-------|-----|
| `frt-test: command not found` sau update | npm link bị mất (vd: upgrade Node version) | `cd ~/work/recorder-engine && npm link` |
| `npm install` fail | Package mới cần Node version cao hơn | Upgrade Node: `nvm install --lts` |
