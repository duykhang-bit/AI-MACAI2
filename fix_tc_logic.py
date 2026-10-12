import os
import re

folder = r'c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku'

# Template TC00 block ĐÚNG (giống TC1)
def get_correct_tc00_block(tc_num):
    mud_key = f"mud{tc_num:02d}"
    return f'''        // ─── STEP 0: LẤY MUD SERIAL từ pool sẵn (KHÔNG gọi API) ─────────────────
        ExtentTest tcGenMud = test.createNode("TC00 - Lấy serial MUD (campaign=" + CAMPAIGN_CODE + ")");
        String mudSerial = null;
        
        // Chỉ dùng PROD pool — không gọi API vì campaign code phải đúng với SĐT
        boolean useProdMode = true; // Set false để dùng UAT pool
        try {{
            mudSerial = utils.MudCodeProvider.getNextMudCode("{mud_key}", useProdMode);
            if (mudSerial == null || mudSerial.isEmpty()) {{
                throw new RuntimeException("❌ PROD pool ({mud_key}) đã hết serial!");
            }}
            tcGenMud.pass("✅ Dùng MUD serial từ " + (useProdMode ? "PROD" : "UAT") + " pool: " + mudSerial);
            System.out.println("[TC{tc_num}] MUD serial from pool: " + mudSerial + " | campaign=" + CAMPAIGN_CODE + " | phone=" + CUSTOMER_PHONE);
        }} catch (Exception e) {{
            tcGenMud.fail("❌ Không lấy được MUD serial từ pool: " + e.getMessage());
            throw new RuntimeException("TC{tc_num} FAIL — không có MUD serial để test!", e);
        }}
        final String mudSerialToApply = mudSerial;'''

files_fixed = 0

for tc_num in range(2, 31):
    if tc_num == 25:
        continue  # Skip TC25 (file gốc)
    
    filename = f'TC{tc_num}.java'
    filepath = os.path.join(folder, filename)
    
    if not os.path.exists(filepath):
        print(f'⚠️  {filename} not found, skip')
        continue
    
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Pattern tìm TC00 block cũ (từ "// ─── STEP 0" hoặc comment khác đến "final String mudSerialToApply")
    # Hoặc từ "ExtentTest tcGenMud" đến "final String mudSerialToApply"
    tc00_pattern = r'(// ─── STEP 0:.*?|ExtentTest tcGenMud =.*?)final String mudSerialToApply = .*?;'
    
    match = re.search(tc00_pattern, content, re.DOTALL)
    if not match:
        print(f'⚠️  {filename} TC00 block not found')
        continue
    
    old_tc00_block = match.group(0)
    new_tc00_block = get_correct_tc00_block(tc_num)
    
    # Replace
    new_content = content.replace(old_tc00_block, new_tc00_block)
    
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f'✅ Fixed {filename} → logic giống TC1 (mud{tc_num:02d})')
        files_fixed += 1
    else:
        print(f'⚠️  {filename} không thay đổi (có thể đã đúng)')

print(f'\n✅ Done! Fixed TC00 logic for {files_fixed} files.')
