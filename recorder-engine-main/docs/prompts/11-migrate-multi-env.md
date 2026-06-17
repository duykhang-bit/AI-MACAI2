# Migrate Chuỗi Sang Multi-Env — Agent Instructions

> **Trigger:** "Migrate chuỗi {CHAIN} sang multi-env" hoặc "Chuyển {CHAIN} sang schema v2"

## Trigger prompt

```
Migrate chuỗi Pharmacy sang schema multi-env.
Hiện tại chỉ có URL CI, UAT/PROD sẽ bổ sung sau.
```

## Prerequisites

- Engine v1.0.0+ (có lệnh `migrate-config`)
- Chain repo đã clone, có `config/environments.yaml` schema v1

## Agent workflow

### Step 1: cd vào chain repo
```bash
cd ~/work/frt-test-pharmacy
```

### Step 2: Dry-run xem trước
```bash
frt-test migrate-config
```
→ In diff: config v1 → v2, list files sẽ move.

### Step 3: Apply migration
```bash
frt-test migrate-config --write
```
→ Ghi config v2, git mv files vào `ci/`, tạo `uat/` + `prod/` folders.

### Step 4: Verify
```bash
frt-test envs
frt-test list --app rsa --env ci
```

### Step 5: Commit
```bash
git add -A
git commit -m "chore: migrate to schema v2 (multi-env support)"
git push
```

### Step 6: Báo user
```
✅ Chuỗi Pharmacy đã migrate sang multi-env.
- CI: URLs giữ nguyên, tests đã move vào flows/{app}/ci/
- UAT/PROD: chưa có URL. Set bằng: frt-test add-env --app rsa --env uat --url <URL>
```

## Success criteria

- `config/environments.yaml` có `schema_version: 2`
- Files YAML nằm trong `flows/{app}/ci/`
- `flows/{app}/uat/.gitkeep` + `flows/{app}/prod/.gitkeep` tồn tại
- `frt-test play --app rsa --env ci --all` vẫn pass (URL CI không đổi)
- Backup file `config/environments.yaml.bak` tồn tại

## Common errors & fix

| Error | Cause | Fix |
|-------|-------|-----|
| `Config đã là schema v2` | Đã migrate rồi | Không cần làm gì |
| `Config not found` | Không ở đúng folder | `cd ~/work/frt-test-{chain}` |
| git mv fail | File chưa tracked | Tự động fallback fs.rename, commit bình thường |
