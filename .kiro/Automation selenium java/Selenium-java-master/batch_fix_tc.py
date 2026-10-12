#!/usr/bin/env python3
"""
Batch fix tất cả TC trong Mudsku để improve logic click "Tạo đơn"
"""
import os
import re
from pathlib import Path

# Pattern cũ cần tìm và replace
OLD_PATTERN_TC13 = r'''(ExtentTest tc13 = test\.createNode\("TC13 - Click Tao don"\);

            WebElement btnTaoDon = wait\.until\(ExpectedConditions\.elementToBeClickable\(
                    By\.xpath\("//button\[contains\(normalize-space\(\.\),'T\u1ea1o \u0111\u01a1n'\)\]"\)\)\);

            String btnInfo = \(String\) js\.executeScript\(
                    "var e=arguments\[0\];" \+
                    "return 'text=\"' \+ \(e\.innerText\|\|''\)\.trim\(\) \+ '\" disabled=' \+ \(e\.disabled===true\);",
                    btnTaoDon\);
            tc13\.info\("Element se click: " \+ btnInfo\);

            js\.executeScript\("arguments\[0\]\.scrollIntoView\(\{block:'center'\}\);", btnTaoDon\);
            Thread\.sleep\(500\);
            try \{
                btnTaoDon\.click\(\);
            \} catch \(Exception clickEx\) \{
                js\.executeScript\("arguments\[0\]\.click\(\);", btnTaoDon\);
            \}
            Thread\.sleep\(3000\);)'''

# Pattern mới (improved)
NEW_PATTERN_TC13 = '''ExtentTest tc13 = test.createNode("TC13 - Click Tao don");

            // Chờ 2s để React render xong button
            Thread.sleep(2000);
            
            // Tìm CHÍNH XÁC nút "Tạo đơn (F4)" - không phải "Lưu đơn (F3)" hay "Huỷ (F8)"
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
            
            if (btnTaoDon == null) {
                attachScreenshot("❌ Không tìm thấy nút Tạo đơn");
                throw new RuntimeException("Không tìm thấy nút 'Tạo đơn (F4)' trên trang");
            }
            
            // Scroll và highlight
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", btnTaoDon);
            js.executeScript("arguments[0].style.border='3px solid green'", btnTaoDon);
            Thread.sleep(500);
            
            String btnInfo = (String) js.executeScript(
                    "var e=arguments[0];" +
                    "return 'text=\"' + (e.innerText||'').trim() + '\" disabled=' + (e.disabled===true);",
                    btnTaoDon);
            tc13.info("✅ Sẽ click button: " + btnInfo);
            
            // Chụp ảnh TRƯỚC KHI click
            attachScreenshot("📸 Trước khi click Tạo đơn");

            // Click native trước; JS click chỉ fallback
            try {
                btnTaoDon.click();
            } catch (Exception clickEx) {
                tc13.info("Native click fail (" + clickEx.getClass().getSimpleName() + ") → fallback JS click");
                js.executeScript("arguments[0].click();", btnTaoDon);
            }
            Thread.sleep(3000);

            // Bắt toast validation của RSA ngay sau click
            try {
                java.util.List<WebElement> toasts = driver.findElements(By.xpath(
                        "//div[contains(@class,'ant-notification-notice') or contains(@class,'ant-message-notice')" +
                        " or contains(@class,'Toastify__toast')]"));
                for (WebElement t : toasts) {
                    String msg = t.getText().replace("\\n", " ").trim();
                    if (!msg.isEmpty()) tc13.warning("⚠️ Thông báo từ app sau khi click Tạo đơn: " + msg);
                }
            } catch (Exception ignore) { }

            // Verify đã sang màn thanh toán
            Thread.sleep(2000);'''

def fix_tc_file(filepath):
    """Fix một TC file"""
    print(f"🔧 Processing: {filepath.name}")
    
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Đếm số lần match
    matches = re.findall(OLD_PATTERN_TC13, content, re.MULTILINE | re.DOTALL)
    if not matches:
        print(f"  ⏭️  Skip - không tìm thấy pattern cũ")
        return False
    
    print(f"  ✅ Tìm thấy {len(matches)} pattern cần fix")
    
    # Replace
    new_content = re.sub(OLD_PATTERN_TC13, NEW_PATTERN_TC13, content, flags=re.MULTILINE | re.DOTALL)
    
    # Write back
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(new_content)
    
    print(f"  💾 Đã fix và lưu file")
    return True

def main():
    mudsku_dir = Path(__file__).parent / "src" / "test" / "java" / "testcases" / "uat" / "Mudsku"
    
    if not mudsku_dir.exists():
        print(f"❌ Thư mục không tồn tại: {mudsku_dir}")
        return
    
    tc_files = sorted(mudsku_dir.glob("TC*.java"))
    print(f"📋 Tìm thấy {len(tc_files)} TC files\n")
    
    fixed_count = 0
    for tc_file in tc_files:
        if fix_tc_file(tc_file):
            fixed_count += 1
    
    print(f"\n✅ Hoàn tất! Đã fix {fixed_count}/{len(tc_files)} files")

if __name__ == "__main__":
    main()
