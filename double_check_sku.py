#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Double-check SKU mapping - Read actual values from files
"""
import os
import re

# Expected SKU from user (30 SKUs for 30 TCs)
EXPECTED = {
    1: "00040647", 2: "00502433", 3: "00040886", 4: "00039925", 5: "00047456",
    6: "00500119", 7: "00018955", 8: "00503302", 9: "00501907", 10: "00033612",
    11: "00047904", 12: "00046430", 13: "00033695", 14: "00032958", 15: "00042313",
    16: "00032637", 17: "00031901", 18: "00046934", 19: "00051805", 20: "00503324",
    21: "00017891", 22: "00045381", 23: "00029877", 24: "00038981", 
    25: "00031035",  # TC25new (TC25 deleted)
    26: "D042300000205", 27: "00503328", 28: "00500213", 29: "00049347", 30: "00053159"
}

BASE = r"c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku"

def extract_sku(file_path):
    """Extract BUY_PRODUCT_1 from TC file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            for line in f:
                # Match: private static final String BUY_PRODUCT_1 = "XXXXXXXX";
                if 'BUY_PRODUCT_1' in line and '=' in line:
                    match = re.search(r'"([^"]+)"', line)
                    if match:
                        return match.group(1)
        return None
    except Exception as e:
        return f"ERROR: {e}"

def main():
    print("=" * 100)
    print("DOUBLE-CHECK SKU MAPPING - DETAILED REPORT")
    print("=" * 100)
    print()
    print(f"{'TC':<12} {'Expected SKU':<20} {'Actual SKU':<20} {'Match':<10} {'File'}")
    print("-" * 100)
    
    all_pass = True
    mismatches = []
    
    # TC1-TC24
    for i in range(1, 25):
        tc_name = f"TC{i}"
        file_name = f"{tc_name}.java"
        file_path = os.path.join(BASE, file_name)
        
        expected = EXPECTED[i]
        actual = extract_sku(file_path) if os.path.exists(file_path) else "FILE_NOT_FOUND"
        match = "✅ PASS" if actual == expected else "❌ FAIL"
        
        if match == "❌ FAIL":
            all_pass = False
            mismatches.append((tc_name, expected, actual))
        
        print(f"{tc_name:<12} {expected:<20} {actual or 'N/A':<20} {match:<10} {file_name}")
    
    # TC25new (special case - TC25 was deleted)
    tc_name = "TC25new"
    file_name = "TC25new.java"
    file_path = os.path.join(BASE, file_name)
    expected = EXPECTED[25]
    actual = extract_sku(file_path) if os.path.exists(file_path) else "FILE_NOT_FOUND"
    match = "✅ PASS" if actual == expected else "❌ FAIL"
    
    if match == "❌ FAIL":
        all_pass = False
        mismatches.append((tc_name, expected, actual))
    
    print(f"{tc_name:<12} {expected:<20} {actual or 'N/A':<20} {match:<10} {file_name}")
    
    # TC26-TC30
    for i in range(26, 31):
        tc_name = f"TC{i}"
        file_name = f"{tc_name}.java"
        file_path = os.path.join(BASE, file_name)
        
        expected = EXPECTED[i]
        actual = extract_sku(file_path) if os.path.exists(file_path) else "FILE_NOT_FOUND"
        match = "✅ PASS" if actual == expected else "❌ FAIL"
        
        if match == "❌ FAIL":
            all_pass = False
            mismatches.append((tc_name, expected, actual))
        
        print(f"{tc_name:<12} {expected:<20} {actual or 'N/A':<20} {match:<10} {file_name}")
    
    print()
    print("=" * 100)
    
    if all_pass:
        print("✅✅✅ CHẮC CHẮN! ALL 30 TCs HAVE CORRECT SKU! ✅✅✅")
        print()
        print("Danh sách đầy đủ 30 SKUs đang dùng:")
        for i in range(1, 31):
            if i == 25:
                print(f"  TC25new: {EXPECTED[25]}")
            else:
                print(f"  TC{i}: {EXPECTED[i]}")
    else:
        print("❌ PHÁT HIỆN LỖI! SOME SKUs MISMATCH!")
        print()
        print("Chi tiết lỗi:")
        for tc, exp, act in mismatches:
            print(f"  {tc}: Expected={exp}, Actual={act}")
    
    print("=" * 100)
    
    return 0 if all_pass else 1

if __name__ == "__main__":
    exit(main())
