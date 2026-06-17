# Clone Test Giữa Env — Agent Instructions

> **Trigger:** "Clone test {TÊN} từ CI sang UAT cho app {APP}" hoặc "Copy test sang env khác"

## Trigger prompt

```
Clone test "tao-don-hang-ban-tai-quay" từ CI sang UAT cho app rsa, chuỗi Pharmacy
```

## Prerequisites

- Chain đã migrate sang schema v2 (prompt 11)
- File source tồn tại trong `flows/{app}/{from_env}/`

## Agent workflow

### Step 1: cd vào chain repo
```bash
cd ~/work/frt-test-pharmacy
```

### Step 2: Clone flow
```bash
frt-test clone-flow tao-don-hang-ban-tai-quay.yaml --app rsa --from ci --to uat
```

### Step 3: Báo user
```
✅ Cloned: flows/rsa/ci/tao-don-hang-ban-tai-quay.yaml → flows/rsa/uat/tao-don-hang-ban-tai-quay.yaml
⚠️  URL trong steps GIỮ NGUYÊN (vẫn CI URL). Cần sửa thủ công hoặc re-record.
⚠️  DOM/flow UAT có thể khác CI — verify lại trước khi commit.
```

## Lưu ý quan trọng

- **KHÔNG tự động replace URL** — test CI và UAT là 2 file độc lập
- User phải verify trên env đích trước khi dùng
- Nếu DOM khác nhiều → nên record lại từ đầu thay vì clone

## Success criteria

- File mới tạo tại `flows/{app}/{to_env}/{file}`
- `recorded_against.env` = env đích
- URL trong steps = giữ nguyên (không auto-replace)

## Common errors & fix

| Error | Cause | Fix |
|-------|-------|-----|
| `Source file not found` | File không tồn tại ở env nguồn | Kiểm tra `frt-test list --app X --env ci` |
| `File already exists` | Dest đã có file cùng tên | Thêm `--overwrite` hoặc đổi tên |
| `App not found` | Tên app sai | Kiểm tra `frt-test envs` |
