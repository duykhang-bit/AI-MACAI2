#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Update SKU (BUY_PRODUCT_1) for each TC2-TC30
"""

import os
import re

BASE_DIR = r"c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku"

# SKU mapping cho từng TC
SKU_MAP = {
    "TC2.java": "00502433",
    "TC3.java": "00040886",
    "TC4.java": "00039925",
    "TC5.java": "00047456",
    "TC6.java": "00500119",
    "TC7.java": "00018955",
    "TC8.java": "00503302",
    "TC9.java": "00501907",
    "TC10.java": "00033612",
    "TC11.java": "00047904",
    "TC12.java": "00046430",
    "TC13.java": "00033695",
    "TC14.java": "00032958",
    "TC15.java": "00042313",
    "TC16.java": "00032637",
    "TC17.java": "00031901",
    "TC18.java": "00046934",
    "TC19.java": "00051805",
    "TC20.java": "00503324",
    "TC21.java": "00017891",
    "TC22.java": "00045381",
    "TC23.java": "00029877",
    "TC24.java": "00038981",
    "TC25new.java": "D042300000205",  # TC25
    "TC26.java": "00503328",
    "TC27.java": "00500213",
    "TC28.java": "00049347",
    "TC29.java": "00053159",
    "TC30.java": "00031035",
}

def update_sku(file_path, new_sku):
    """Update BUY_PRODUCT_1 SKU in a TC file"""
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Pattern: private static final String BUY_PRODUCT_1   = "00040647";
    pattern = r'(private\s+static\s+final\s+String\s+BUY_PRODUCT_1\s*=\s*")[^"]+(")'
    
    match = re.search(pattern, content)
    if not match:
        print(f"  ❌ Pattern not found")
        return False
    
    old_sku = content[match.start(1)+len(match.group(1)):match.end(2)-len(match.group(2))]
    
    if old_sku == new_sku:
        print(f"  ⏭️  Already {new_sku} - SKIP")
        return False
    
    new_content = re.sub(pattern, r'\g<1>' + new_sku + r'\g<2>', content)
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    
    print(f"  ✅ {old_sku} → {new_sku}")
    return True

def main():
    print("=" * 60)
    print("Update SKU for TC2-TC30")
    print("=" * 60)
    
    success = 0
    skip = 0
    fail = 0
    
    for tc_file, sku in SKU_MAP.items():
        file_path = os.path.join(BASE_DIR, tc_file)
        
        if not os.path.exists(file_path):
            print(f"⚠️  {tc_file}: Not found")
            skip += 1
            continue
        
        print(f"{tc_file} → {sku}:")
        result = update_sku(file_path, sku)
        if result:
            success += 1
        else:
            skip += 1
    
    print("=" * 60)
    print(f"✅ Updated: {success}")
    print(f"⏭️  Skipped: {skip}")
    print(f"❌ Failed:  {fail}")
    print("=" * 60)

if __name__ == "__main__":
    main()
