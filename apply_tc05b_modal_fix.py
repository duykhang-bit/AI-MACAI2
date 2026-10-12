#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Apply TC05b modal fix to TC2-TC30 (excluding TC25/TC25new)
"""

import os
import re

BASE_DIR = r"c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku"

TC_TO_FIX = [f"TC{i}.java" for i in range(2, 25)] + [f"TC{i}.java" for i in range(26, 31)]

TC05B_CODE = '''
        /*
         * =========================
         * TC05b - XU LY MODAL "CANH BAO THAY DOI GIA" (SAU KHI VAO BAN HANG)
         * =========================
         */
        ExtentTest tc05b = test.createNode("TC05b - Xu ly modal canh bao thay doi gia");
        
        try {
            WebElement modalThayDoiGia = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(text(),'Cảnh báo có sp thay đổi giá')]]")));
            
            tc05b.info("Phat hien modal 'Canh bao co sp thay doi gia' - click 'De sau'");
            
            WebElement btnDeSau = modalThayDoiGia.findElement(
                    By.xpath(".//button[contains(.,'Để sau') or contains(.,'De sau')]"));
            js.executeScript("arguments[0].click();", btnDeSau);
            Thread.sleep(1500);
            
            tc05b.pass("Da click 'De sau' tren modal thay doi gia");
        } catch (TimeoutException e) {
            tc05b.info("Khong co modal canh bao thay doi gia");
        }'''

def apply_fix(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Check if already has TC05b
    if "TC05b" in content and "Xu ly modal canh bao thay doi gia" in content:
        print(f"  ⏭️  Already has TC05b - SKIP")
        return False
    
    # Find TC05 pass statement
    pattern = r'(tc05\.pass\([^\)]+\);)'
    match = re.search(pattern, content)
    
    if not match:
        print(f"  ❌ Cannot find 'tc05.pass' statement")
        return False
    
    # Insert TC05b after tc05.pass
    new_content = content[:match.end()] + TC05B_CODE + content[match.end():]
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    
    print(f"  ✅ TC05b added")
    return True

def main():
    print("=" * 60)
    print("Apply TC05b Modal Fix")
    print("=" * 60)
    
    success = 0
    skip = 0
    fail = 0
    
    for tc_file in TC_TO_FIX:
        file_path = os.path.join(BASE_DIR, tc_file)
        
        if not os.path.exists(file_path):
            print(f"⚠️  {tc_file}: Not found")
            skip += 1
            continue
        
        print(f"{tc_file}:")
        result = apply_fix(file_path)
        if result:
            success += 1
        else:
            if "Already" in str(result):
                skip += 1
            else:
                fail += 1
    
    print("=" * 60)
    print(f"✅ Success: {success}")
    print(f"⏭️  Skipped: {skip}")
    print(f"❌ Failed:  {fail}")
    print("=" * 60)

if __name__ == "__main__":
    main()
