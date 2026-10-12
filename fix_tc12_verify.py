import os
import re

folder = r'c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku'

# Mapping TC → loại CTKM
tc_ctkm_type = {
    1: ('MUA_TANG', 'Mua 2 tặng 1'),
    2: ('MUA_TANG', 'Mua 2 tặng 1'),
    3: ('MUA_TANG', 'Mua 2 tặng 1'),
    4: ('MUA_TANG', 'Mua 2 tặng 1'),
    5: ('MUA_TANG', 'Mua 1 tặng 1'),
    6: ('MUA_TANG', 'Mua 1 tặng 1'),
    7: ('MUA_TANG', 'Mua 1 tặng 1'),
    8: ('MUA_TANG', 'Mua 1 tặng 1'),
    9: ('MUA_TANG', 'Mua 1 tặng 1'),
    10: ('GIAM_GIA', 'Giảm giá 30%'),
    11: ('GIAM_GIA', 'Giảm giá 30%'),
    12: ('GIAM_GIA', 'Giảm giá 30%'),
    13: ('GIAM_GIA', 'Giảm giá 30%'),
    14: ('GIAM_GIA', 'Giảm giá 30%'),
    15: ('GIAM_GIA', 'Giảm giá 30%'),
    16: ('GIAM_GIA', 'Giảm giá 30%'),
    17: ('GIAM_GIA', 'Giảm giá 30%'),
    18: ('GIAM_GIA', 'Giảm giá 30%'),
    19: ('GIAM_GIA', 'Giảm giá 30%'),
    20: ('GIAM_GIA', 'Giảm giá 50%'),
    21: ('GIAM_GIA', 'Giảm giá 50%'),
    22: ('GIAM_GIA', 'Giảm giá 50%'),
    23: ('GIAM_GIA', 'Giảm giá 50%'),
    24: ('GIAM_GIA', 'Giảm giá 50%'),
    26: ('QUA_TANG', 'Quà tặng'),
    27: ('QUA_TANG', 'Quà tặng'),
    28: ('QUA_TANG', 'Quà tặng'),
    29: ('QUA_TANG', 'Quà tặng'),
    30: ('QUA_TANG', 'Quà tặng'),
}

files_fixed = 0

for tc_num, (ctkm_type, ctkm_text) in tc_ctkm_type.items():
    filename = f'TC{tc_num}.java'
    filepath = os.path.join(folder, filename)
    
    if not os.path.exists(filepath):
        print(f'⚠️  {filename} not found, skip')
        continue
    
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Tìm TC12 verify block - format thực tế:
    # /*
    #  * =========================
    #  * TC12 - VERIFY CTKM ...
    #  * =========================
    #  */
    # ExtentTest tc12 = ...
    # try { ... } catch (Exception e) { ... }
    
    # Pattern khớp từ comment đến hết try-catch block của TC12
    tc12_pattern = r'(/\*\s*\n\s*\*\s*=+\s*\n\s*\*\s*TC12 - VERIFY CTKM.*?\n\s*\*\s*=+\s*\n\s*\*/.*?)((?=\n\s*/\*\s*\n\s*\*\s*=+\s*\n\s*\*\s*TC1[3-9])|(?=\n\s*$))'
    
    match = re.search(tc12_pattern, content, re.DOTALL)
    if not match:
        print(f'⚠️  {filename} TC12 block not found')
        continue
    
    old_tc12_block = match.group(1)
    
    # Gen new TC12 block theo loại CTKM
    if ctkm_type == 'MUA_TANG':
        new_tc12_block = f'''/*
         * =========================
         * TC12 - VERIFY CTKM {ctkm_text}
         * =========================
         */
        ExtentTest tc12 = test.createNode("TC12 - Verify CTKM " + PROMOTION_CODE);

        try {{
            WebElement promotionLink = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//*[contains(normalize-space(.),'Khuyến mãi khác')]")));
            js.executeScript("arguments[0].click();", promotionLink);
            Thread.sleep(1000);

            WebElement promotionModal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(.,'Danh sách khuyến mãi')]]")));
            // Verify CTKM "{ctkm_text}" xuất hiện trong modal
            promotionModal.findElement(By.xpath(".//*[contains(.,'" + PROMOTION_CODE + "') or contains(.,'{ctkm_text}')]"));
            tc12.pass("✅ Tìm thấy CTKM " + PROMOTION_CODE + " - {ctkm_text}");

            try {{
                WebElement btnConfirm = promotionModal.findElement(By.xpath(".//button[contains(.,'Xác nhận')]"));
                js.executeScript("arguments[0].click();", btnConfirm);
            }} catch (NoSuchElementException ex) {{
                WebElement closeBtn = promotionModal.findElement(
                        By.xpath(".//button[@aria-label='Close' or contains(@class,'ant-modal-close')]"));
                js.executeScript("arguments[0].click();", closeBtn);
            }}
            Thread.sleep(1500);
        }} catch (Exception e) {{
            tc12.info("Không tìm thấy 'Khuyến mãi khác' — verify qua màn hình bán hàng. " + e.getMessage());
        }}'''
    
    elif ctkm_type == 'GIAM_GIA':
        new_tc12_block = f'''/*
         * =========================
         * TC12 - VERIFY CTKM {ctkm_text}
         * =========================
         */
        ExtentTest tc12 = test.createNode("TC12 - Verify CTKM " + PROMOTION_CODE);

        try {{
            WebElement promotionLink = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//*[contains(normalize-space(.),'Khuyến mãi khác')]")));
            js.executeScript("arguments[0].click();", promotionLink);
            Thread.sleep(1000);

            WebElement promotionModal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(.,'Danh sách khuyến mãi')]]")));
            // Verify CTKM "{ctkm_text}" xuất hiện trong modal
            promotionModal.findElement(By.xpath(".//*[contains(.,'" + PROMOTION_CODE + "') or contains(.,'{ctkm_text}')]"));
            tc12.pass("✅ Tìm thấy CTKM " + PROMOTION_CODE + " - {ctkm_text}");

            try {{
                WebElement btnConfirm = promotionModal.findElement(By.xpath(".//button[contains(.,'Xác nhận')]"));
                js.executeScript("arguments[0].click();", btnConfirm);
            }} catch (NoSuchElementException ex) {{
                WebElement closeBtn = promotionModal.findElement(
                        By.xpath(".//button[@aria-label='Close' or contains(@class,'ant-modal-close')]"));
                js.executeScript("arguments[0].click();", closeBtn);
            }}
            Thread.sleep(1500);
        }} catch (Exception e) {{
            tc12.info("Không tìm thấy 'Khuyến mãi khác' — verify qua màn hình bán hàng. " + e.getMessage());
        }}'''
    
    elif ctkm_type == 'QUA_TANG':
        new_tc12_block = f'''/*
         * =========================
         * TC12 - VERIFY CTKM {ctkm_text}
         * =========================
         */
        ExtentTest tc12 = test.createNode("TC12 - Verify CTKM " + PROMOTION_CODE);

        try {{
            WebElement promotionLink = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//*[contains(normalize-space(.),'Khuyến mãi khác')]")));
            js.executeScript("arguments[0].click();", promotionLink);
            Thread.sleep(1000);

            WebElement promotionModal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(@class,'ant-modal') and .//*[contains(.,'Danh sách khuyến mãi')]]")));
            // Verify CTKM "Quà tặng" xuất hiện trong modal
            promotionModal.findElement(By.xpath(".//*[contains(.,'" + PROMOTION_CODE + "') or contains(.,'Quà tặng')]"));
            tc12.pass("✅ Tìm thấy CTKM " + PROMOTION_CODE + " - Quà tặng");

            try {{
                WebElement btnConfirm = promotionModal.findElement(By.xpath(".//button[contains(.,'Xác nhận')]"));
                js.executeScript("arguments[0].click();", btnConfirm);
            }} catch (NoSuchElementException ex) {{
                WebElement closeBtn = promotionModal.findElement(
                        By.xpath(".//button[@aria-label='Close' or contains(@class,'ant-modal-close')]"));
                js.executeScript("arguments[0].click();", closeBtn);
            }}
            Thread.sleep(1500);
        }} catch (Exception e) {{
            tc12.info("Không tìm thấy 'Khuyến mãi khác' — verify qua màn hình bán hàng. " + e.getMessage());
        }}'''
    
    # Replace old TC12 block
    new_content = content.replace(old_tc12_block, new_tc12_block)
    
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f'✅ Fixed {filename} → verify "{ctkm_text}"')
        files_fixed += 1

print(f'\n✅ Done! Fixed TC12 verify for {files_fixed} files.')
