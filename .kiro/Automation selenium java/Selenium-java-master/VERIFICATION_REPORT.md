# 📋 Verification Report - 31 TCs Mudsku Package

## 🎯 Mục tiêu
Verify logic toàn bộ 31 TCs trong `testcases.uat.Mudsku` để đảm bảo run 1 lần được luôn, không fail.

## ✅ Improvements Đã Apply

### 1. **Headless Mode Control** (BaseTest1.java + config.properties)
- ✅ Config `headless=false` → Chrome hiển thị để debug
- ✅ Config `headless=true` → Chrome ẩn để chạy nhanh
- ✅ Đọc từ ConfigReader, dễ switch không cần sửa code

### 2. **TC1 Mudsku - Click "Tạo đơn" Logic** ✅ FIXED
**Vấn đề cũ:**
- XPath `contains(normalize-space(.),'Tạo đơn')` → có thể khớp sai button
- Không có screenshot trước/sau click
- Không bắt toast error từ app

**Fix áp dụng:**
```java
// ✅ Tìm ALL buttons có text "đơn", filter trong Java
java.util.List<WebElement> allButtons = driver.findElements(
        By.xpath("//button[contains(.,'đơn') or contains(.,'Đơn')]"));

WebElement btnTaoDon = null;
for (WebElement btn : allButtons) {
    String btnText = btn.getText().toLowerCase().trim();
    tc13.info("🔍 Button tìm thấy: '" + btn.getText() + "'");
    if (btnText.contains("tạo đơn") && !btnText.contains("lưu")) {
        btnTaoDon = btn;
        break;
    }
}

// ✅ Throw error nếu không tìm thấy
if (btnTaoDon == null) {
    attachScreenshot("❌ Không tìm thấy nút Tạo đơn");
    throw new RuntimeException("Không tìm thấy nút 'Tạo đơn (F4)' trên trang");
}

// ✅ Highlight button màu xanh để dễ debug
js.executeScript("arguments[0].style.border='3px solid green'", btnTaoDon);

// ✅ Screenshot TRƯỚC KHI click
attachScreenshot("📸 Trước khi click Tạo đơn");

// ✅ Bắt toast error từ app
java.util.List<WebElement> toasts = driver.findElements(By.xpath(
        "//div[contains(@class,'ant-notification-notice')]"));
for (WebElement t : toasts) {
    String msg = t.getText().replace("\n", " ").trim();
    if (!msg.isEmpty()) tc13.warning("⚠️ Thông báo: " + msg);
}

// ✅ Screenshot SAU KHI click + verify navigation
attachScreenshot(onPayment ? "✅ Đã sang màn thanh toán" : "❌ KHÔNG sang màn thanh toán");
```

### 3. **TC1 Family - Nhập số lượng + Click Tạo đơn** ✅ FIXED
**Fix nhập số lượng:**
- Tìm ô cuối cùng trong list inputs (mới thêm nhất)
- Highlight border đỏ để dễ thấy
- Dùng native sendKeys thay vì JS manipulation
- **Verify giá trị** sau khi nhập

**Fix click Tạo đơn:**
- Same pattern như TC1 Mudsku (filter buttons trong Java)
- Screenshot trước/sau
- Bắt toast

## 📊 Status 31 TCs

| TC File | Status | Pattern Click "Tạo đơn" | Notes |
|---------|--------|-------------------------|-------|
| TC1.java | ✅ FIXED | Improved (filter in Java) | Master pattern |
| TC2.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC3.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC4.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC5.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC6.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC7.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC8.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC9.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC10.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC11.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC12.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC13.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC14.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC15.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC16.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC17.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC18.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC19.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC20.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC21.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC22.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC23.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC24.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC25.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC25new.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC26.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC27.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC28.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC29.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |
| TC30.java | ⚠️ NEED VERIFY | ? | Chưa kiểm tra |

## 🔍 Quick Verification Strategy

**Approach:** Check 30 TCs còn lại (TC2-TC30 + TC25new) xem có dùng same pattern như TC1 không.

**Nếu YES** → Logic đã OK từ đầu, chỉ cần improve thêm screenshots
**Nếu NO** → Cần apply fix pattern từ TC1

## 🎯 Next Steps

1. ✅ **DONE** - Fix TC1 Mudsku với improved pattern
2. ⏳ **TODO** - Grep search pattern trong 30 TCs còn lại
3. ⏳ **TODO** - Apply batch fix nếu cần
4. ⏳ **TODO** - Compile all TCs
5. ⏳ **TODO** - Run 1 lần toàn bộ 31 TCs

## 🏆 Expected Outcome

- ✅ Tất cả 31 TCs pass trong 1 lần chạy
- ✅ Report chi tiết với screenshots tại các điểm quan trọng
- ✅ Easy debug nếu có lỗi (nhờ screenshot + toast messages)
- ✅ Không click nhầm button
- ✅ Verify navigation sau mỗi bước quan trọng

---
*Generated: 2026-10-09*
*By: Kiro AI Assistant*
