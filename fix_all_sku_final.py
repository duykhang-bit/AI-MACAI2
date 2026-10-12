#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Fix SKU for ALL 31 TCs in Mudsku package - FINAL VERSION
"""
import os
import re

# Correct SKU mapping from user - 30 TCs (TC25 deleted)
SKU_MAP = {
    "TC1.java": "00040647",
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
    # TC25 was deleted - use SKU #25 for TC25new
    "TC25new.java": "00031035",  # SKU #25 (was for TC25)
    "TC26.java": "D042300000205",  # SKU #26
    "TC27.java": "00503328",  # SKU #27
    "TC28.java": "00500213",  # SKU #28
    "TC29.java": "00049347",  # SKU #29
    "TC30.java": "00053159",  # SKU #30
}

BASE_DIR = r"c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku"

def update_sku_in_file(file_path, new_sku):
    """Update BUY_PRODUCT_1 SKU in TC file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Pattern: private static final String BUY_PRODUCT_1 = "XXXXXXXX";
        pattern = r'(private\s+static\s+final\s+String\s+BUY_PRODUCT_1\s*=\s*")([^"]+)(")'
        
        # Replace with new SKU
        new_content = re.sub(pattern, rf'\g<1>{new_sku}\g<3>', content)
        
        # Check if replacement happened
        if new_content == content:
            return False, "Pattern not found"
        
        # Write back
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(new_content)
        
        return True, "Updated"
    except Exception as e:
        return False, str(e)

def main():
    print("=" * 80)
    print("FIXING SKU FOR ALL 30 TCs - FINAL")
    print("=" * 80)
    print()
    
    success_count = 0
    fail_count = 0
    
    for file_name, new_sku in sorted(SKU_MAP.items()):
        file_path = os.path.join(BASE_DIR, file_name)
        
        if not os.path.exists(file_path):
            print(f"❌ {file_name:15} - FILE NOT FOUND")
            fail_count += 1
            continue
        
        success, msg = update_sku_in_file(file_path, new_sku)
        
        if success:
            print(f"✅ {file_name:15} → {new_sku:20} (Updated)")
            success_count += 1
        else:
            print(f"❌ {file_name:15} → {new_sku:20} (FAILED: {msg})")
            fail_count += 1
    
    print()
    print("=" * 80)
    print(f"SUMMARY: {success_count} updated, {fail_count} failed")
    print("=" * 80)
    
    return 0 if fail_count == 0 else 1

if __name__ == "__main__":
    exit(main())
