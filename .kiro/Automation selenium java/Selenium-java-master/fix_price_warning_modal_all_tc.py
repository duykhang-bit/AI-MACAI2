#!/usr/bin/env python3
"""
Fix: Add multiple price warning modal handlers to all 30 Mudsku TCs
- TC05b: After entering "Bán hàng" (existing)
- TC06.5: AFTER selecting Inside code (NEW - from screenshot evidence)
- TC08.5: After adding product (NEW)
- TC11.5: After applying MUD code (NEW)

Also fix TC1 ElementClickIntercepted issue with JS click + scroll for Inside selection
"""

import os
import re

# Path to TC files
TC_DIR = r"c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku"

# List all 30 TC files (TC1-TC30, TC25new exists)
TC_FILES = [
    "TC1.java", "TC2.java", "TC3.java", "TC4.java", "TC5.java",
    "TC6.java", "TC7.java", "TC8.java", "TC9.java", "TC10.java",
    "TC11.java", "TC12.java", "TC13.java", "TC14.java", "TC15.java",
    "TC16.java", "TC17.java", "TC18.java", "TC19.java", "TC20.java",
    "TC21.java", "TC22.java", "TC23.java", "TC24.java", "TC25new.java",
    "TC26.java", "TC27.java", "TC28.java", "TC29.java", "TC30.java"
]

# Modal handler code template
MODAL_HANDLER_TEMPLATE = """
        /*
         * =========================
         * {step_name} - XU LY MODAL "CANH BAO THAY DOI GIA"
         * =========================
         */
        ExtentTest {step_var} = test.createNode("{step_name} - Xu ly modal canh bao thay doi gia");
        
        try {{
            WebElement modalThayDoiGia = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(text(),'Cảnh báo có sp thay đổi giá')]]")));
            
            {step_var}.info("Phat hien modal 'Canh bao co sp thay doi gia' - click 'De sau'");
            
            WebElement btnDeSau = modalThayDoiGia.findElement(
                    By.xpath(".//button[contains(.,'Để sau') or contains(.,'De sau')]"));
            js.executeScript("arguments[0].click();", btnDeSau);
            Thread.sleep(1500);
            
            {step_var}.pass("Da click 'De sau' tren modal thay doi gia");
        }} catch (TimeoutException e) {{
            {step_var}.info("Khong co modal canh bao thay doi gia");
        }}
"""

def fix_tc1_inside_click(content):
    """
    Fix TC1 ElementClickIntercepted issue:
    - Add scroll + JS click for Inside option selection
    """
    
    # Pattern: Find the insideOption.click() line
    pattern = r'(WebElement insideOption = wait\.until\(ExpectedConditions\.elementToBeClickable\(\s*By\.xpath\("[^"]+"\)\)\);)\s*(insideOption\.click\(\);)'
    
    replacement = r'''\1
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", insideOption);
            Thread.sleep(500);
            js.executeScript("arguments[0].click();", insideOption);'''
    
    if re.search(pattern, content):
        content = re.sub(pattern, replacement, content)
        print("  ✓ Fixed TC1 Inside option click (scroll + JS click)")
    
    return content

def add_modal_handler_after_marker(content, marker, step_name, step_var):
    """
    Add modal handler code after a specific marker in the content
    """
    if marker not in content:
        return content, False
    
    # Check if this modal handler already exists
    if step_var in content:
        return content, False
    
    handler_code = MODAL_HANDLER_TEMPLATE.format(step_name=step_name, step_var=step_var)
    content = content.replace(marker, marker + handler_code)
    
    return content, True

def process_tc_file(filepath):
    """Process a single TC file"""
    
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    changes = []
    
    # Fix 1: TC1 specific - Inside option ElementClickIntercepted
    if "TC1.java" in filepath:
        content = fix_tc1_inside_click(content)
        if content != original_content:
            changes.append("Inside option JS click")
            original_content = content
    
    # Fix 2: Add TC06.5 modal handler AFTER "tc06.pass" line
    # Find marker: tc06.pass("Da nhap ma inside " + INSIDE_CODE);
    marker_tc06 = 'tc06.pass("Da nhap ma inside " + INSIDE_CODE);'
    content, added = add_modal_handler_after_marker(content, marker_tc06, "TC06.5", "tc06c")
    if added:
        changes.append("TC06.5 modal handler")
    
    # Fix 3: Add TC08.5 modal handler AFTER "tc08b.pass" line
    # Find marker: tc08b.pass("Da nhap SL " + BUY_QTY);
    marker_tc08 = 'tc08b.pass("Da nhap SL " + BUY_QTY);'
    content, added = add_modal_handler_after_marker(content, marker_tc08, "TC08.5", "tc08c")
    if added:
        changes.append("TC08.5 modal handler")
    
    # Fix 4: Add TC11.5 modal handler AFTER "tc11.pass" line
    # Find marker: tc11.pass("Da apply MUD " + mudSerialToApply);
    marker_tc11 = 'tc11.pass("Da apply MUD " + mudSerialToApply);'
    content, added = add_modal_handler_after_marker(content, marker_tc11, "TC11.5", "tc11c")
    if added:
        changes.append("TC11.5 modal handler")
    
    # Write back if changes made
    if content != original_content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        return True, changes
    
    return False, []

def main():
    print("=" * 80)
    print("Fix: Add multiple price warning modal handlers to all 30 Mudsku TCs")
    print("=" * 80)
    
    updated_count = 0
    
    for tc_file in TC_FILES:
        filepath = os.path.join(TC_DIR, tc_file)
        
        if not os.path.exists(filepath):
            print(f"⚠ SKIP {tc_file} - file not found")
            continue
        
        print(f"\nProcessing {tc_file}...")
        updated, changes = process_tc_file(filepath)
        
        if updated:
            print(f"  ✅ UPDATED: {', '.join(changes)}")
            updated_count += 1
        else:
            print(f"  ℹ No changes needed (already fixed or no matching patterns)")
    
    print("\n" + "=" * 80)
    print(f"Summary: Updated {updated_count}/{len(TC_FILES)} TC files")
    print("=" * 80)
    
    if updated_count > 0:
        print("\n📋 Next steps:")
        print("1. Compile: mvn clean test-compile -DskipTests")
        print("2. Rerun 5 failed TCs: TC1, TC11, TC24, TC25new, TC29")
        print("   mvn test -Dtest=TC1,TC11,TC24,TC25new,TC29")

if __name__ == "__main__":
    main()
