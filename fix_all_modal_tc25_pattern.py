#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Apply TC25 modal pattern to all 30 TCs in Mudsku folder
- Sửa TC04: thêm xử lý button "Để sau" 
- Xóa: xử lý modal ở TC08, TC11, TC12.5, TC13
"""

import os
import re

BASE_DIR = r"c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku"

# TC cần fix: TC2-TC24, TC26-TC30
TC_TO_FIX = [f"TC{i}.java" for i in range(2, 25)] + [f"TC{i}.java" for i in range(26, 31)]

# Pattern TC04 mới (từ TC25)
NEW_TC04 = '''        try {
            WebElement closePopup = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(text(),'Danh s\u00e1ch s\u1ea3n ph\u1ea9m sai \u0111\u1ed1i t\u01b0\u1ee3ng')]]" +
                                    "//button[@aria-label='Close' or contains(@class,'ant-modal-close')]")));
            js.executeScript("arguments[0].click();", closePopup);
            tc04.pass("Da tat popup sai doi tuong");
        } catch (TimeoutException e) {
            tc04.info("Khong co popup sai doi tuong");
        }

        try {
            WebElement btnDeSau = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//button[contains(.,'De sau') or .//span[contains(text(),'De sau')]]")));
            js.executeScript("arguments[0].click();", btnDeSau);
        } catch (TimeoutException e) { }

        Thread.sleep(500);'''

def fix_tc_file(file_path):
    """Fix một TC file"""
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original = content
    
    # STEP 1: Sửa TC04 - thay toàn bộ nội dung giữa "ExtentTest tc04" và "TC05"
    pattern_tc04 = r'(ExtentTest tc04 = test\.createNode\("TC04[^"]*"\);)(.*?)(\/\*\s*=+\s*\n\s*\*\s*TC05)'
    match = re.search(pattern_tc04, content, re.DOTALL)
    if match:
        content = content[:match.end(1)] + '\n\n' + NEW_TC04 + '\n\n        ' + content[match.start(3):]
        print(f"  ✅ TC04: Updated")
    else:
        print(f"  ⚠️  TC04: Pattern not found")
    
    # STEP 2: Xóa xử lý modal sau TC08 (sau tc08b.pass)
    pattern_tc08 = r'(tc08b\.pass\([^)]+\);)\s*\n\s*// Xu ly modal thay doi gia neu xuat hien ngay sau them SP.*?catch \(TimeoutException e\) \{ \}'
    content = re.sub(pattern_tc08, r'\1', content, flags=re.DOTALL)
    
    # STEP 3: Xóa xử lý modal sau TC11 (sau tc11.warning)
    pattern_tc11 = r'(tc11\.warning\([^)]+\);)\s*\}\s*\n\s*// Xu ly modal thay doi gia neu xuat hien ngay sau apply MUD.*?catch \(TimeoutException e\) \{ \}'
    content = re.sub(pattern_tc11, r'\1\n        }', content, flags=re.DOTALL)
    
    # STEP 4: Xóa toàn bộ TC12.5
    pattern_tc12_5 = r'\s*/\*\s*=+\s*\n\s*\*\s*TC12\.5[^\n]*\n\s*\*\s*=+\s*\n\s*\*/\s*ExtentTest tc12_5[^}]+\}\s*catch[^}]+\}\s*\n'
    content = re.sub(pattern_tc12_5, '\n', content, flags=re.DOTALL)
    
    # STEP 5: Xóa xử lý modal TRƯỚC TC13 (sau "ExtentTest tc13" và trước "// Chờ 2s")
    pattern_tc13 = r'(ExtentTest tc13 = test\.createNode\([^)]+\);)\s*\n\s*// Xu ly modal thay doi gia neu xuat hien TRUOC khi click Tao don.*?catch \(TimeoutException e\) \{ \}\s*\n'
    content = re.sub(pattern_tc13, r'\1\n\n', content, flags=re.DOTALL)
    
    if content == original:
        print(f"  ⏭️  No changes needed")
        return False
    
    # Write back
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    
    return True

def main():
    print("=" * 60)
    print("Apply TC25 Modal Pattern to Mudsku TCs")
    print("=" * 60)
    print(f"Target: {len(TC_TO_FIX)} files (TC2-TC24, TC26-TC30)")
    print()
    
    success_count = 0
    skip_count = 0
    
    for tc_file in TC_TO_FIX:
        file_path = os.path.join(BASE_DIR, tc_file)
        
        if not os.path.exists(file_path):
            print(f"⚠️  {tc_file}: File not found - SKIP")
            skip_count += 1
            continue
        
        print(f"Processing {tc_file}:")
        result = fix_tc_file(file_path)
        if result:
            success_count += 1
        else:
            skip_count += 1
        print()
    
    print("=" * 60)
    print(f"SUMMARY:")
    print(f"  ✅ Fixed:   {success_count} files")
    print(f"  ⏭️  Skipped: {skip_count} files")
    print("=" * 60)

if __name__ == "__main__":
    main()
