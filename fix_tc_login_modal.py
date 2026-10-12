import os
import re

folder = r'c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku'

# Đọc TC1 để lấy template TC01-TC06
with open(os.path.join(folder, 'TC1.java'), 'r', encoding='utf-8') as f:
    tc1_content = f.read()

# Extract phần từ "TC01 - LOGIN" đến hết "TC06 - NHẬP MÃ INSIDE"
pattern = r'(/\*\s*\n\s*\*\s*=+\s*\n\s*\*\s*TC01 - LOGIN.*?)(/\*\s*\n\s*\*\s*=+\s*\n\s*\*\s*TC07 - NHẬP SĐT)'

match = re.search(pattern, tc1_content, re.DOTALL)
if not match:
    print("❌ Không tìm thấy TC01-TC06 block trong TC1!")
    exit(1)

tc01_to_tc06_template = match.group(1)
print(f"✅ Đã extract TC01-TC06 template từ TC1 ({len(tc01_to_tc06_template)} chars)")

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
    
    # Tìm phần TC01 cũ đến TC07 (hoặc TC05/TC06)
    # Pattern: từ "TC01 - LOGIN" đến "TC07 - NHẬP SĐT" hoặc "TC05 - " hoặc "TC06 - "
    old_pattern = r'/\*\s*\n\s*\*\s*=+\s*\n\s*\*\s*TC01.*?(?=/\*\s*\n\s*\*\s*=+\s*\n\s*\*\s*TC0[567] - NHẬP SĐT)'
    
    match = re.search(old_pattern, content, re.DOTALL)
    if not match:
        print(f'⚠️  {filename} - không tìm thấy TC01 block')
        continue
    
    old_block = match.group(0)
    
    # Replace
    new_content = content.replace(old_block, tc01_to_tc06_template)
    
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f'✅ Fixed {filename} - thêm TC01-TC06 từ TC1')
        files_fixed += 1
    else:
        print(f'⚠️  {filename} - không thay đổi')

print(f'\n✅ Done! Fixed {files_fixed} files.')
