# Play Test — Agent Instructions

> **Trigger:** "Play test {TÊN} của {CHAIN}, env {ENV}" hoặc "Chạy test {file} cho app {APP} env {ENV}"

## Trigger prompt

```
Play test "tao-don-hang" của chuỗi LAB, env ci
```

```
Chạy tất cả test của app rsa-lab, env ci
```

## Prerequisites

- Engine đã setup (prompt 01)
- Chain repo đã clone, có flows/

## Agent workflow

### Step 1: cd vào chain repo
```bash
cd ~/work/frt-test-lab
```

### Step 2: Chạy test

**1 file cụ thể:**
```bash
frt-test play --app rsa-lab --env ci tao-don-hang.yaml
```

**Tất cả tests 1 app trên 1 env:**
```bash
frt-test play --app rsa-lab --env ci --all
```

**Tất cả tests toàn chuỗi (cần --env):**
```bash
frt-test play --app rsa-lab --env uat --all
```

**Với options:**
```bash
frt-test play --app rsa-lab --env ci --all --headless --html-report
```

> **`--env` bắt buộc** với chuỗi multi-env (schema v2). Thiếu → lỗi rõ ràng.
> Dùng `FRT_TEST_ENV=ci` env var cho CI/CD pipeline.

### Step 3: Report kết quả
Báo tester: bao nhiêu pass / fail, file nào fail + screenshot path.

## Success criteria

- Exit code 0 nếu all pass
- Exit code 1 nếu có fail (+ screenshot tự lưu `screenshots/`)
- Terminal output rõ step nào fail

## Common errors & fix

| Error | Cause | Fix |
|-------|-------|-----|
| `No flows found` | Folder flows/ rỗng hoặc app sai | Kiểm tra `frt-test list --app {app}` |
| Timeout | Element chưa render kịp | Tăng timeout trong YAML: `timeout: 60000` |
| Selector not found | DOM thay đổi sau khi record | Xem prompt 06-fix-failing-test |
