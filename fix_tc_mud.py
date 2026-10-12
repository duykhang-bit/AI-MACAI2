import os
import re

folder = r'c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku'

# Mapping TC → mud key
tc_to_mud = {}
for i in range(1, 31):
    tc_to_mud[i] = f'mud{i:02d}'

files_fixed = 0

for tc_num in range(2, 31):  # TC2-TC30
    filename = f'TC{tc_num}.java'
    filepath = os.path.join(folder, filename)
    
    if not os.path.exists(filepath):
        print(f'⚠️  {filename} not found, skip')
        continue
    
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    mud_key = tc_to_mud[tc_num]
    
    # Pattern: tìm từ "// ─── STEP 0:" đến "final String mudSerialToApply"
    old_pattern = r'// ─── STEP 0:.*?final String mudSerialToApply = mudSerial;'
    
    new_code = f'''// ─── STEP 0: LẤY MUD SERIAL từ pool sẵn (KHÔNG gọi API) ─────────────────
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
    
    # Replace with re.DOTALL to match across newlines
    new_content = re.sub(old_pattern, new_code, content, flags=re.DOTALL)
    
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f'✅ Fixed {filename} → use {mud_key}')
        files_fixed += 1
    else:
        print(f'⚠️  {filename} pattern not matched')

print(f'\n✅ Done! Fixed {files_fixed} files.')
