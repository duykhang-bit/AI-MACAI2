# Fix Failing Test — Agent Instructions

> **Trigger:** "Test {TÊN} của {CHAIN} fail, fix giúp tôi"

## ⛔ NGUYÊN TẮC SỐ 1 — ĐỌC TRƯỚC KHI FIX

> **Fix để E2E CHẠY ĐƯỢC, KHÔNG sửa assertion để ép test PASS.**
>
> Mục tiêu của fix là làm flow **thao tác được đến cuối** (selector, timing, navigation, hover, popup, data setup). **TUYỆT ĐỐI KHÔNG** sửa/xóa/nới lỏng assertion chỉ để có màu xanh.
>
> **Vì sao:** khi một `assert` fail nghĩa là test đã làm đúng việc của nó — **phát hiện một bug**. Sửa `expected` cho khớp giá trị (sai) hiện tại = **che giấu bug**, làm test mất giá trị.

### Phân biệt 2 loại fail (BẮT BUỘC)

| Loại fail | Bản chất | Hành động đúng |
|-----------|----------|----------------|
| **Step thao tác fail** (click/fill/goto/selector not found, timeout, popup chắn, thiếu hover) | Lỗi test mechanics — KHÔNG phải bug sản phẩm | ✅ Fix: update selector, thêm waitFor/hover, xử lý popup, sửa URL/data |
| **Assertion fail** (`assert` so giá trị/trạng thái mà sai) | **Nghi vấn BUG sản phẩm** | 🚫 GIỮ NGUYÊN assertion. Báo người dùng: "Assertion X fail — có thể là bug. Giá trị mong đợi `A`, thực tế `B`." KHÔNG tự đổi `expected`. |

### Ngoại lệ DUY NHẤT được chỉnh assertion

Chỉ khi assertion fail vì **dữ liệu động hợp lệ** (số tiền/tồn kho/% /ngày thay đổi mỗi lần chạy — KHÔNG phải bug):
- Được phép đổi `assertType: text` (so khớp con số) → `visible` (chỉ verify element hiện), hoặc parameterize bằng `{{var}}`.
- Đây là sửa **test-design** (assertion sai cách ngay từ đầu), KHÁC với việc đổi `expected` cho khớp giá trị sai để mask bug.
- Nếu KHÔNG chắc fail là do data động hay do bug → **GIỮ assertion + hỏi người dùng**, không tự quyết.

---

## Trigger prompt

```
Test "tao-don-hang" của LAB chạy fail. Fix giúp tôi.
```

## Prerequisites

- Test file YAML tồn tại trong flows/
- Test đã run fail ít nhất 1 lần (có screenshot nếu screenshotOnFail=true)

## Agent workflow

### Step 1: Chạy lại test, capture error
```bash
cd ~/work/frt-test-lab
frt-test play --app rsa-lab --env ci tao-don-hang.yaml 2>&1
```

### Step 2: Đọc YAML file
Đọc file YAML, tìm step fail (match error message với step name/selector).

### Step 3: Xem screenshot (nếu có)
```bash
ls screenshots/ | sort -r | head -3
```

### Step 4: Diagnose & fix
Các nguyên nhân phổ biến:
- **Selector stale**: DOM thay đổi → update selector (dùng text selector, data-testid, hoặc XPath)
- **Timing**: element chưa render → thêm `waitFor` step trước
- **Data dependency**: test cần data cụ thể → thêm `variables` + `use: _shared/setup-data.yaml`
- **URL changed**: baseUrl thay đổi → update `config/environments.yaml`
- **Nút chỉ hiện khi hover**: thiếu bước `hover` trước `click` → thêm step `action: hover` (recorder mới tự bắt; flow cũ record trước đây có thể thiếu)
- **Popup chắn click**: runner tự đóng popup mặc định. Nếu test CẦN giữ popup (test popup behavior) → set `config.autoPopupDismiss: false`. Nếu popup nguy hiểm ("Xóa") không tự đóng → xử lý thủ công bằng step click nút đóng.
- **Assert fail**: ⚠️ NGHI VẤN BUG — xem NGUYÊN TẮC SỐ 1. Giữ nguyên assertion, báo người dùng. CHỈ đổi `assertType: text → visible` hoặc parameterize `{{var}}` khi chắc chắn giá trị là **dữ liệu động hợp lệ** (tiền/tồn kho/%/ngày), KHÔNG phải bug.

### Step 5: Sửa YAML file trực tiếp
- Chỉ sửa phần thao tác (selector/wait/hover/popup/navigation/data).
- KHÔNG đụng vào `assert`/`expected` trừ ngoại lệ data động ở NGUYÊN TẮC SỐ 1.

### Step 6: Re-run verify
```bash
frt-test play --app rsa-lab --env ci tao-don-hang.yaml
```
Lặp Step 4-6 cho đến khi **các step thao tác chạy hết**. Nếu chỉ còn `assert` fail và đó là bug → DỪNG, không lặp để ép pass.

### Step 7: Báo tester kết quả
- Nếu fix được lỗi thao tác: "Test đã fix. Nguyên nhân: [selector cũ/timing/hover/popup]. Muốn commit không?"
- Nếu assertion fail do nghi vấn bug: "⚠️ Đã fix flow chạy đến cuối, NHƯNG assertion `X` vẫn fail — mong đợi `A`, thực tế `B`. Khả năng là **bug sản phẩm**. Giữ nguyên assertion để báo bug, KHÔNG ép pass. Bạn xác nhận?"

## Success criteria

- Các **step thao tác** chạy hết (không còn lỗi selector/timing/navigation).
- **Assertion giữ nguyên ý nghĩa gốc** — không đổi `expected`/không xóa `assert` để ép pass.
- Nếu assertion fail là bug thật → test VẪN fail (đúng) + đã báo người dùng; KHÔNG coi đó là "chưa fix xong".
- Selector mới stable hơn (ưu tiên text/role/testId > CSS > XPath).

## Common errors & fix

| Error | Cause | Fix |
|-------|-------|-----|
| `Element not found: #old-id` | ID bị đổi | Tìm element bằng text content hoặc aria-label |
| `Timeout waiting for selector` | Page load chậm | Thêm step `waitForSelector` hoặc `waitForNavigation` trước |
| `Assert failed: expected "ABC" got "XYZ"` | **Có thể là BUG sản phẩm** | 🚫 KHÔNG đổi expected để pass. Giữ assertion, báo người dùng. Chỉ đổi sang `visible`/`{{var}}` NẾU "XYZ" là dữ liệu động hợp lệ (tiền/tồn/ngày), KHÔNG phải bug. |
