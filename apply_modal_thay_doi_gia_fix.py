#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Apply modal 'Cảnh báo có sp thay đổi giá' fix to TC2-TC30 (excluding TC25new)
"""

import os
import re

BASE_DIR = r"c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku"

# TC cần fix: TC2-TC24, TC26-TC30
TC_TO_FIX = [f"TC{i}.java" for i in range(2, 25)] + [f"TC{i}.java" for i in range(26, 31)]

MODAL_FIX = '''        /*
         * =========================
         * TC12.5 - XU LY MODAL "CANH BAO CO SP THAY DOI GIA"
         * =========================
         */
        ExtentTest tc12_5 = test.createNode("TC12.5 - Xu ly modal canh bao thay doi gia");
        
        try {
            WebElement modalThayDoiGia = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(text(),'Cảnh báo có sp thay đổi giá')]]")));
            
            tc12_5.info("Phat hien modal 'Canh bao co sp thay doi gia' - click 'De sau'");
            
            WebElement btnDeSau = modalThayDoiGia.findElement(
                    By.xpath(".//button[contains(.,'Để sau') or contains(.,'De sau')]"));
            js.executeScript("arguments[0].click();", btnDeSau);
            Thread.sleep(1500);
            
            tc12_5.pass("Da click 'De sau' tren modal thay doi gia");
        } catch (TimeoutException e) {
            tc12_5.info("Khong co modal canh bao thay doi gia");
        }

'''

def apply_modal_fix(file_path):
    """Apply modal thay doi gia fix to a TC file"""
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Check if already has TC12.5
    if "TC12.5 - Xu ly modal canh bao thay doi gia" in content:
        print(f"  ⏭️  {os.path.basename(file_path)}: Already has TC12.5 fix - SKIP")
        return False
    
    # Find position to insert (before "TC13 - TAO DON")
    # Match exactly: "        /*\n         * =========================\n         * TC13 - TAO DON\n         * =========================\n         */"
    pattern = r'(\s*/\*\s*\n\s*\*\s*=+\s*\n\s*\*\s*TC13\s*-\s*TAO DON\s*\n\s*\*\s*=+\s*\n\s*\*/)'
    match = re.search(pattern, content, re.IGNORECASE)
    
    if not match:
        print(f"  ❌ {os.path.basename(file_path)}: Cannot find 'TC13 - TAO DON' marker")
        return False
    
    # Insert modal fix before TC13
    new_content = content[:match.start()] + MODAL_FIX + content[match.start():]
    
    # Write back
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    
    print(f"  ✅ {os.path.basename(file_path)}: Modal fix applied")
    return True

def main():
    print("=" * 60)
    print("Apply Modal 'Cảnh báo có sp thay đổi giá' Fix")
    print("=" * 60)
    print(f"Target: {len(TC_TO_FIX)} files (TC2-TC24, TC26-TC30)")
    print()
    
    success_count = 0
    skip_count = 0
    fail_count = 0
    
    for tc_file in TC_TO_FIX:
        file_path = os.path.join(BASE_DIR, tc_file)
        
        if not os.path.exists(file_path):
            print(f"  ⚠️  {tc_file}: File not found - SKIP")
            skip_count += 1
            continue
        
        result = apply_modal_fix(file_path)
        if result:
            success_count += 1
        elif "Already has TC12.5" in str(result):
            skip_count += 1
        else:
            fail_count += 1
    
    print()
    print("=" * 60)
    print(f"SUMMARY:")
    print(f"  ✅ Applied:  {success_count} files")
    print(f"  ⏭️  Skipped:  {skip_count} files")
    print(f"  ❌ Failed:   {fail_count} files")
    print("=" * 60)

if __name__ == "__main__":
    main()
