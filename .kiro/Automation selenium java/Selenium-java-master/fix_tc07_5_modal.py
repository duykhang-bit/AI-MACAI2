#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Fix TC10, TC11, TC19, TC24, TC29, TC30 bằng cách thêm TC07.5 modal handler
(sau khi nhập SDT, trước khi searchAndAddProduct)

Root cause: Modal "Cảnh báo có sp thay đổi giá" xuất hiện trước khi dropdown search product kịp hiển thị
→ searchAndAddProduct timeout chờ dropdown

Solution: Thêm modal handler ở TC07.5 (sau TC07 nhập SDT, trước TC08 search product)
"""

import os
import re

# CÁC TC CẦN FIX (fail ở searchAndAddProduct do dropdown timeout)
TC_LIST = ['TC10', 'TC11', 'TC19', 'TC24', 'TC29', 'TC30']

BASE_DIR = r'c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku'

# MODAL HANDLER CODE (thêm sau TC07)
TC07_5_HANDLER = '''
        /*
         * =========================
         * TC07.5 - XU LY MODAL "CANH BAO THAY DOI GIA" (TRUOC KHI SEARCH PRODUCT)
         * =========================
         */
        ExtentTest tc07c = test.createNode("TC07.5 - Xu ly modal canh bao thay doi gia");
        
        try {
            WebElement modalThayDoiGia = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(text(),'Cảnh báo có sp thay đổi giá')]]")));
            
            tc07c.info("Phat hien modal 'Canh bao co sp thay doi gia' - click 'De sau'");
            
            WebElement btnDeSau = modalThayDoiGia.findElement(
                    By.xpath(".//button[contains(.,'Để sau') or contains(.,'De sau')]"));
            js.executeScript("arguments[0].click();", btnDeSau);
            Thread.sleep(1500);
            
            tc07c.pass("Da click 'De sau' tren modal thay doi gia");
        } catch (TimeoutException e) {
            tc07c.info("Khong co modal canh bao thay doi gia");
        }

'''

def fix_tc_file(tc_name):
    """
    Thêm TC07.5 modal handler vào TC file
    Thêm sau TC07 (nhập SDT), trước TC08 (search product)
    """
    file_path = os.path.join(BASE_DIR, f'{tc_name}.java')
    
    if not os.path.exists(file_path):
        print(f'❌ {tc_name}: File không tồn tại: {file_path}')
        return False
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Check đã có TC07.5 chưa
    if 'TC07.5 - Xu ly modal canh bao thay doi gia' in content:
        print(f'✅ {tc_name}: Đã có TC07.5 modal handler')
        return True
    
    # Tìm vị trí sau TC07 (nhập SDT) - sau dòng tc07.pass(...)
    # Pattern: tc07.pass("Da nhap SDT " + CUSTOMER_PHONE);
    
    pattern = r'(tc07\.pass\("Da nhap SDT " \+ CUSTOMER_PHONE\);)\s*\n'
    
    if not re.search(pattern, content):
        print(f'❌ {tc_name}: Không tìm thấy tc07.pass để insert')
        return False
    
    # Insert TC07.5 handler sau tc07.pass
    new_content = re.sub(
        pattern,
        r'\1' + '\n' + TC07_5_HANDLER,
        content,
        count=1
    )
    
    if new_content == content:
        print(f'❌ {tc_name}: Insert thất bại (content không đổi)')
        return False
    
    # Write back
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    
    print(f'✅ {tc_name}: Đã thêm TC07.5 modal handler')
    return True

def main():
    print('🔧 Fix TC07.5 modal handler cho các TCs fail ở searchAndAddProduct...\n')
    
    success = 0
    fail = 0
    
    for tc_name in TC_LIST:
        if fix_tc_file(tc_name):
            success += 1
        else:
            fail += 1
    
    print(f'\n📊 Tổng kết: {success} TC đã fix, {fail} TC thất bại')
    
    if success > 0:
        print('\n✅ Chạy lệnh compile:')
        print('   mvn clean test-compile -DskipTests')

if __name__ == '__main__':
    main()
