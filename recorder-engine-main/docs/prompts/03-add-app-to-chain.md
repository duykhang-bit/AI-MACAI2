# Thêm App vào Chuỗi — Agent Instructions

> **Trigger:** "Thêm app {TÊN} cho chuỗi {CHAIN}, URL ci: {URL}" hoặc "Thêm app {TÊN}, urls: ci={URL},uat={URL}"

## Trigger prompt

```
Thêm app admin-lab cho chuỗi LAB, URL ci: https://admin-ci-lab.frt.vn
```

## Prerequisites

- Chuỗi đã onboard (xem prompt 02)
- Đang ở trong folder chain repo (hoặc agent tự cd vào)

## Agent workflow

### Step 1: cd vào chain repo
```bash
cd ~/work/frt-test-lab
```

### Step 2: Chạy add-app
```bash
frt-test add-app --name admin-lab --urls "ci:https://admin-ci-lab.frt.vn"
```

> **Legacy:** `--url https://...` vẫn hoạt động (assign vào env ci).
> **Multi-URL:** `--urls "ci:url,uat:url"` để set nhiều env cùng lúc.

### Step 3: Commit + push
```bash
git add config/ flows/
git commit -m "add: app admin-lab"
git push
```

## Success criteria

- `config/environments.yaml` chứa entry `admin-lab:` với baseUrl đúng
- `flows/admin-lab/_shared/` tồn tại
- Git commit + push thành công

## Common errors & fix

| Error | Cause | Fix |
|-------|-------|-----|
| `App already exists` | Tên app trùng | Dùng tên khác hoặc sửa URL trong config thủ công |
| `Not a chain repo` | Không ở đúng folder | `cd ~/work/frt-test-{chain}` trước |
