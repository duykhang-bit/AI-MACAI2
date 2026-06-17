# Commit & Push Tests — Agent Instructions

> **Trigger:** "Commit và push test {TÊN} lên repo {CHAIN}"

## Trigger prompt

```
Commit và push test "tao-don-hang" lên repo LAB
```

## Prerequisites

- Có file YAML mới/sửa trong `flows/`
- Chain repo đã có remote configured

## Agent workflow

### Step 1: cd vào chain repo
```bash
cd ~/work/frt-test-lab
```

### Step 2: Check changes
```bash
git status
```

### Step 3: Stage flow files
```bash
git add flows/ config/
```

### Step 4: Commit
```bash
git commit -m "add: test tao-don-hang cho rsa-lab"
```
Format commit: `add: {mô tả ngắn}` hoặc `fix: {mô tả}` nếu sửa test cũ.

### Step 5: Push
```bash
git push
```

## Success criteria

- `git push` exit 0
- Remote repo có commit mới

## Common errors & fix

| Error | Cause | Fix |
|-------|-------|-----|
| `rejected: non-fast-forward` | Có commit mới trên remote | `git pull --rebase && git push` |
| `nothing to commit` | Chưa có changes | Kiểm tra đúng folder, đúng file |
