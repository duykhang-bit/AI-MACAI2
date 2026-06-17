# Daily Regression — Agent Instructions

> **Trigger:** "Chạy regression toàn chuỗi {CHAIN}"

## Trigger prompt

```
Chạy regression toàn chuỗi LAB, app rsa-lab, env ci
```

## Prerequisites

- Engine đã setup (prompt 01)
- Chain repo đã clone

## Agent workflow

### Step 1: Pull latest tests
```bash
cd ~/work/frt-test-lab
git pull
```

### Step 2: Chạy tất cả headless
```bash
frt-test play --app rsa-lab --env ci --all --headless --html-report
```

> **`--env` bắt buộc** với chuỗi multi-env (schema v2).
> Dùng `FRT_TEST_ENV=ci` trong CI/CD pipeline.

### Step 3: Report
Báo kết quả:
- Tổng tests: N pass / M fail
- Nếu có fail: list tên file + step fail + screenshot path
- HTML report path (nếu có)

> **⛔ Phân loại fail TRƯỚC khi đề xuất fix (xem prompt 06, NGUYÊN TẮC SỐ 1):**
> - **Step thao tác fail** (selector/timing/navigation) → lỗi test, có thể fix.
> - **Assertion fail** → nghi vấn **BUG sản phẩm**. Báo rõ "mong đợi A, thực tế B" và đánh dấu là bug tiềm năng. **KHÔNG đề xuất đổi `expected` để pass.**
> Khi báo cáo regression, tách 2 nhóm này riêng để team biết đâu là bug thật.

## Success criteria

- `git pull` thành công (latest tests)
- Exit 0 = all pass, exit 1 = có fail
- HTML report tạo trong `reports/`

## Common errors & fix

| Error | Cause | Fix |
|-------|-------|-----|
| `git pull` conflict | Local changes | `git stash && git pull && git stash pop` |
| Nhiều test fail cùng lúc | Có thể app down | Verify URL manually trước: `curl -s -o /dev/null -w "%{http_code}" {baseUrl}` |
