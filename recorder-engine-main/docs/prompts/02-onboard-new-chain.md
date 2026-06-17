# Onboard Chuỗi Mới — Agent Instructions

> **Trigger:** "Onboard chuỗi {TÊN}, apps: {APP1} ci:{URL1}, {APP2} ci:{URL2}, ..."
> Agent tự làm hết.

## Trigger prompt

```
Onboard chuỗi LAB cho recorder, envs: ci, uat, prod. 3 apps:
- rsa-lab: ci = https://ci-lab-rsa.frt.vn
- ecom-lab: ci = https://ci-lab-ecom.frt.vn
- portal-lab: ci = https://ci-lab-portal.frt.vn
(UAT/PROD URL bổ sung sau bằng frt-test add-env)
```

## Prerequisites

- Engine đã setup (xem prompt 01-setup-engine)
- GitLab repo đã tồn tại (vd: `ai-test/frt-test-lab`) — empty
- User có quyền push lên repo đó

## Agent workflow

### Step 1: Parse apps từ user input
Tách thành pairs `name=url`.

### Step 2: Chạy scaffold
```bash
cd ~/work/recorder-engine
frt-test scaffold-chain \
  --name lab \
  --display "LAB (Xét nghiệm)" \
  --envs ci,uat,prod \
  --apps "rsa-lab=ci:https://ci-lab-rsa.frt.vn" \
  --apps "ecom-lab=ci:https://ci-lab-ecom.frt.vn" \
  --apps "portal-lab=ci:https://ci-lab-portal.frt.vn" \
  --output ~/work/frt-test-lab \
  --remote git@git2.fptshop.com.vn:ai-test/frt-test-lab.git \
  --push
```

> **Multi-URL format:** `"app=ci:url,uat:url,prod:url"`. Env chưa có URL → bỏ qua (sẽ set null).
> **Legacy format:** `"app=url"` vẫn hoạt động — URL tự assign vào env `ci`.

### Step 3: Verify
```bash
cd ~/work/frt-test-lab
frt-test envs
frt-test list --app rsa-lab --env ci
```

### Step 4: Báo user
```
✅ Chuỗi LAB đã onboard (schema v2, multi-env).
Repo: git@git2.fptshop.com.vn:ai-test/frt-test-lab.git
Apps: rsa-lab, ecom-lab, portal-lab
Envs: ci, uat, prod (UAT/PROD URL set sau bằng: frt-test add-env)
Tester dùng: cd ~/work/frt-test-lab && frt-test record --app rsa-lab --env ci -n "Tên test"
```

## Success criteria

- Repo push thành công (git log có commit "init: scaffold lab chain")
- `config/environments.yaml` chứa `schema_version: 2` + 3 apps × 3 envs (CI có URL, UAT/PROD = null)
- `flows/rsa-lab/{ci,uat,prod}/` tồn tại với `.gitkeep`
- `flows/rsa-lab/_shared/` tồn tại
- README.md rendered (không còn `{{...}}`)

## Common errors & fix

| Error | Cause | Fix |
|-------|-------|-----|
| `Output dir not empty` | Folder đã tồn tại | Xóa folder cũ hoặc dùng output path khác |
| `git push rejected` | Repo chưa empty trên GitLab | Xóa README mặc định trên GitLab, hoặc `--force` push |
| `Chain name must be kebab-case` | Tên có uppercase/space | Dùng lowercase + dash: `lab`, `back-office` |
