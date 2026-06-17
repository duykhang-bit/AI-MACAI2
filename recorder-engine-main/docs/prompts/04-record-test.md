# Record Test Mới — Agent Instructions

> **Trigger:** "Record test mới cho {CHAIN}, app {APP}, env {ENV}, tên: {TÊN}"

## Trigger prompt

```
Record test mới cho chuỗi LAB, app rsa-lab, env ci, tên: "Tạo đơn hàng dịch vụ XN máu"
```

## Prerequisites

- Engine đã setup (prompt 01)
- Chain repo đã clone (prompt 02)

## Agent workflow

### Step 1: cd vào chain repo
```bash
cd ~/work/frt-test-lab
```

### Step 2: Verify app tồn tại
```bash
frt-test list --app rsa-lab
```

### Step 3: Bắt đầu record
```bash
frt-test record --app rsa-lab --env ci -n "Tạo đơn hàng dịch vụ XN máu"
```

> **`--env` bắt buộc** với chuỗi đã migrate multi-env (schema v2).
> Chuỗi cũ (schema v1) vẫn chạy không cần `--env`.

### Step 4: Hướng dẫn tester thao tác
Báo tester:
- Browser sẽ mở. Thao tác **chậm, rõ ràng**.
- Sau khi chọn dropdown → **đợi 1-2 giây** trước khi click tiếp.
- **Hover-to-reveal:** nếu nút chỉ hiện khi hover (icon/row) → cứ hover rồi click, recorder tự ghi bước `hover` trước `click`.
- **Popup bất ngờ:** không cần click tắt popup/quảng cáo thủ công — runner tự đóng khi play (trừ popup nguy hiểm như "Xóa").
- **Assert mode:** Ctrl+Shift+A → click element → nhập expected value. Với giá trị động (tiền/tồn kho/%/ngày) recorder tự chuyển thành kiểm tra `visible` để khỏi flaky.
- Xong → Ctrl+C để dừng recording.

> **Lưu ý:** recorder KHÔNG còn chèn `delay` cứng vào mỗi step (runner đã tự chờ element). YAML output sẽ gọn hơn.

### Step 5: Verify file tạo thành công
```bash
ls flows/rsa-lab/ | grep -i "xn-mau\|don-hang"
```

### Step 6: Hỏi tester
"File YAML đã tạo. Muốn play lại để verify không?"

## Success criteria

- File `.yaml` mới tạo trong `flows/{app}/{env}/` (schema v2) hoặc `flows/{app}/` (schema v1)
- File có đúng format (name, steps, tags, recorded_against metadata)
- Không có lỗi khi record (exit 0)

## Common errors & fix

| Error | Cause | Fix |
|-------|-------|-----|
| Browser không mở | Playwright chưa install chromium | `npx playwright install chromium` |
| `Cannot find app` | App name sai / chưa có trong config | Kiểm tra `config/environments.yaml` |
| Selector ghi nhận sai | Thao tác quá nhanh | Record lại, thao tác chậm hơn |
