#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Verify SKU mapping for all 31 TCs in Mudsku package
"""
import os
import re

# Expected SKU mapping (from user's list) - 30 TCs only (TC25 deleted)
EXPECTED_SKU_MAP = {
    "TC1": "00040647",
    "TC2": "00502433",
    "TC3": "00040886",
    "TC4": "00039925",
    "TC5": "00047456",
    "TC6": "00500119",
    "TC7": "00018955",
    "TC8": "00503302",
    "TC9": "00501907",
    "TC10": "00033612",
    "TC11": "00047904",
    "TC12": "00046430",
    "TC13": "00033695",
    "TC14": "00032958",
    "TC15": "00042313",
    "TC16": "00032637",
    "TC17": "00031901",
    "TC18": "00046934",
    "TC19": "00051805",
    "TC20": "00503324",
    "TC21": "00017891",
    "TC22": "00045381",
    "TC23": "00029877",
    "TC24": "00038981",
    # TC25 was deleted - use SKU #25 for TC25new instead
    "TC25new": "00031035",  # SKU #25 from user's list (was for TC25, now for TC25new)
    "TC26": "D042300000205",  # SKU #26
    "TC27": "00503328",  # SKU #27
    "TC28": "00500213",  # SKU #28
    "TC29": "00049347",  # SKU #29
    "TC30": "00053159",  # SKU #30
}

BASE_DIR = r"c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku"

def extract_buy_product_1(file_path):
    """Extract BUY_PRODUCT_1 SKU from TC file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            
        # Pattern: private static final String BUY_PRODUCT_1 = "00040647";
        pattern = r'private\s+static\s+final\s+String\s+BUY_PRODUCT_1\s*=\s*"([^"]+)"'
        match = re.search(pattern, content)
        
        if match:
            return match.group(1)
        else:
            return None
    except Exception as e:
        return f"ERROR: {e}"

def main():
    print("=" * 80)
    print("SKU VERIFICATION REPORT - Mudsku Package (30 TCs)")
    print("=" * 80)
    print()
    
    results = []
    all_pass = True
    
    for tc_name, expected_sku in sorted(EXPECTED_SKU_MAP.items()):
        file_name = f"{tc_name}.java"
        file_path = os.path.join(BASE_DIR, file_name)
        
        if not os.path.exists(file_path):
            status = "❌ FILE NOT FOUND"
            actual_sku = "N/A"
            all_pass = False
        else:
            actual_sku = extract_buy_product_1(file_path)
            
            if actual_sku is None:
                status = "⚠️ PATTERN NOT FOUND"
                all_pass = False
            elif actual_sku == expected_sku:
                status = "✅ PASS"
            else:
                status = f"❌ MISMATCH"
                all_pass = False
        
        results.append({
            "tc": tc_name,
            "expected": expected_sku,
            "actual": actual_sku,
            "status": status
        })
    
    # Print table
    print(f"{'TC':<10} {'Expected SKU':<20} {'Actual SKU':<20} {'Status':<20}")
    print("-" * 80)
    
    for r in results:
        print(f"{r['tc']:<10} {r['expected']:<20} {r['actual'] or 'N/A':<20} {r['status']:<20}")
    
    print()
    print("=" * 80)
    
    if all_pass:
        print("✅ ALL 30 TCs HAVE CORRECT SKU MAPPING!")
    else:
        print("❌ SOME TCs HAVE INCORRECT SKU MAPPING!")
        print()
        print("Failed TCs:")
        for r in results:
            if "❌" in r['status'] or "⚠️" in r['status']:
                print(f"  - {r['tc']}: Expected {r['expected']}, Got {r['actual']} [{r['status']}]")
    
    print("=" * 80)
    
    return 0 if all_pass else 1

if __name__ == "__main__":
    exit(main())
