#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Update @Test descriptions for all 30 TCs with full campaign names
"""
import os
import re

# Mapping SKU -> Full description
DESCRIPTIONS = {
    "00040647": "KM-1026-094 - TK Gia Dinh - Mua 2 tang 1 - Nuoc mat nhan tao Easimist Eye Drops 5ml",
    "00502433": "KM-1026-095 - TK Gia Dinh - Mua 2 tang 1 - Cao dan giam dau Chi Thong Cao 20 goi",
    "00040886": "KM-1026-096 - TK Gia Dinh - Mua 2 tang 1 - Tra Thao Moc Atiso Datino Premium Tea 20 tui",
    "00039925": "KM-1026-097 - TK Gia Dinh - Mua 2 tang 1 - Vien sui Kudos Vitamin C 20 vien",
    "00047456": "KM-1026-098 - TK Gia Dinh - Mua 1 tang 1 - Vien uong bo nao Ich Tri Gold 60v",
    "00500119": "KM-1026-099 - TK Gia Dinh - Mua 1 tang 1 - Vien uong bo sung canxi Dr.Caci OCAVILL 60v",
    "00018955": "KM-1026-100 - TK Gia Dinh - Mua 1 tang 1 - Vien an ngon ngu ngon Tat Thanh New 30v",
    "00503302": "KM-1026-101 - TK Gia Dinh - Mua 1 tang 1 - Kem boi ham ta, cham sua, bong, kich ung da Procream 30g",
    "00501907": "KM-1026-102 - TK Gia Dinh - Mua 1 tang 1 - Khau trang 3 lop 5D mask (10 cai) Nam Anh",
    "00033612": "KM-1026-103 - TK Gia Dinh - Giam gia 30% - Tinh dau Thao Nguyen huong sa chanh 30ml",
    "00047904": "KM-1026-104 - TK Gia Dinh - Giam gia 30% - Xit thom mieng Aro Mouth 10ml",
    "00046430": "KM-1026-105 - TK Gia Dinh - Giam gia 30% - Siro Bo Phe Labebe (120ml)",
    "00033695": "KM-1026-106 - TK Gia Dinh - Giam gia 30% - Bot Nuoc Mat Datino Herbal Drink Powder (10 goi x 8g)",
    "00032958": "KM-1026-107 - TK Gia Dinh - Giam gia 30% - Sua tam goi cho be La Beauty Baby Gentle Wash (250ml)",
    "00042313": "KM-1026-108 - TK Gia Dinh - Giam gia 30% - Sua tam Neo Acnes giam mun lung tinh chat tram tra (250ml)",
    "00032637": "KM-1026-109 - TK Gia Dinh - Giam gia 30% - Tam chi ke rang Jomi Disposable Flosser (80 cay)",
    "00031901": "KM-1026-110 - TK Gia Dinh - Giam gia 30% - Vien uong bo sung vitamin C 500mg Nature's Bounty (100 vien)",
    "00046934": "KM-1026-111 - TK Gia Dinh - Giam gia 30% - Nuoc suc mieng Pearlie White Fluorinze Anti-Bacterial Fluoride (750ml)",
    "00051805": "KM-1026-112 - TK Gia Dinh - Giam gia 30% - Hop phan lieu thuoc 6 Ngan Inochi Okina",
    "00503324": "KM-1026-113 - TK Gia Dinh - Giam gia 50% - Sua rua mat tay trang Reihaku Hatomugi W Cleansing Foam (130g)",
    "00017891": "KM-1026-114 - TK Gia Dinh - Giam gia 50% - Siro Boi Mau Forte Mom And Baby (125ml)",
    "00045381": "KM-1026-115 - TK Gia Dinh - Giam gia 50% - Vien uong Omexxel Ginkgo 120 Premium (60 vien)",
    "00029877": "KM-1026-116 - TK Gia Dinh - Giam gia 50% - Vien uong Omega-3 for kids 300mg (100 vien)",
    "00038981": "KM-1026-117 - TK Gia Dinh - Giam gia 50% - Vien uong dep da VwhiteSkin Ocavill (30 vien)",
    "00031035": "KM-1026-118 - TK Gia Dinh - Giam gia 50% - Dung dich ve sinh phu nu Natural Feminine La Trau Khong (100ml)",
    "D042300000205": "KM-1026-119 - TK Gia Dinh - Qua Tang - Khau trang tre em Dolphin Mask 3D cho tre tu 2-6 tuoi (10 cai)",
    "00503328": "KM-1026-120 - TK Gia Dinh - Qua Tang - Kem duong da tay Reihaku Hatomugi Hand Cream (65g)",
    "00500213": "KM-1026-121 - TK Gia Dinh - Qua Tang - Bao cao su Safefit Freezer Max S52 (3 cai)",
    "00049347": "KM-1026-122 - TK Gia Dinh - Qua Tang - Com vi sinh Lacto Biomin Gold+ New (20 goi x 5g)",
    "00053159": "KM-1026-123 - TK Gia Dinh - Qua Tang - Khan giay uot Kohnan (goi 100 to)",
}

# TC number -> SKU mapping
TC_SKU_MAP = {
    1: "00040647", 2: "00502433", 3: "00040886", 4: "00039925", 5: "00047456",
    6: "00500119", 7: "00018955", 8: "00503302", 9: "00501907", 10: "00033612",
    11: "00047904", 12: "00046430", 13: "00033695", 14: "00032958", 15: "00042313",
    16: "00032637", 17: "00031901", 18: "00046934", 19: "00051805", 20: "00503324",
    21: "00017891", 22: "00045381", 23: "00029877", 24: "00038981", 
    25: "00031035",  # TC25new
    26: "D042300000205", 27: "00503328", 28: "00500213", 29: "00049347", 30: "00053159"
}

BASE_DIR = r"c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku"

def update_tc_description(tc_num, sku):
    """Update description in TC file"""
    if tc_num == 25:
        file_name = "TC25new.java"
    else:
        file_name = f"TC{tc_num}.java"
    
    file_path = os.path.join(BASE_DIR, file_name)
    
    if not os.path.exists(file_path):
        return False, "File not found"
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Find @Test annotation with description
        pattern = r'(@Test\([^)]*description\s*=\s*")[^"]*(")'
        
        new_desc = DESCRIPTIONS.get(sku, f"KM-UNKNOWN - SKU {sku}")
        
        # Replace
        new_content = re.sub(pattern, rf'\g<1>{new_desc}\g<2>', content)
        
        if new_content == content:
            return False, "Pattern not found or already updated"
        
        # Write back
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(new_content)
        
        return True, "Updated"
    except Exception as e:
        return False, str(e)

def main():
    print("=" * 100)
    print("UPDATING @Test DESCRIPTIONS FOR ALL 30 TCs")
    print("=" * 100)
    print()
    
    success_count = 0
    fail_count = 0
    
    for tc_num, sku in sorted(TC_SKU_MAP.items()):
        success, msg = update_tc_description(tc_num, sku)
        
        tc_name = f"TC{tc_num}" if tc_num != 25 else "TC25new"
        desc = DESCRIPTIONS.get(sku, "UNKNOWN")[:80] + "..."
        
        if success:
            print(f"✅ {tc_name:<10} → {desc}")
            success_count += 1
        else:
            print(f"❌ {tc_name:<10} → FAILED: {msg}")
            fail_count += 1
    
    print()
    print("=" * 100)
    print(f"SUMMARY: {success_count} updated, {fail_count} failed")
    print("=" * 100)
    
    if fail_count == 0:
        print("✅ All 30 TCs updated successfully!")
        print("Next: Compile and run tests to see full descriptions in HTML report")
    
    return 0 if fail_count == 0 else 1

if __name__ == "__main__":
    exit(main())
