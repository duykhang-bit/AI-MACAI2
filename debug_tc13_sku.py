#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Debug TC13 SKU issue - Check if SKU exists on UAT
"""
import requests
import json

SKU = "00033695"
UAT_API = "https://uat-api.frt.vn"  # Adjust to actual UAT API endpoint

def check_sku_on_uat():
    """
    Check if SKU exists and is active on UAT
    You may need to adjust the endpoint based on actual API
    """
    print("=" * 80)
    print(f"DEBUGGING TC13 - SKU {SKU}")
    print("=" * 80)
    print()
    
    # Option 1: Check via product search API
    print(f"🔍 Checking SKU {SKU} on UAT...")
    print()
    
    # TODO: Add actual API call if you have endpoint
    # Example:
    # response = requests.get(f"{UAT_API}/products/{SKU}")
    # if response.status_code == 200:
    #     product = response.json()
    #     print(f"✅ Product found: {product.get('name')}")
    #     print(f"   Status: {product.get('status')}")
    #     print(f"   Stock: {product.get('stock')}")
    # else:
    #     print(f"❌ Product not found (HTTP {response.status_code})")
    
    print("📋 Manual checks needed:")
    print()
    print(f"1. Login to UAT: https://uat-rsa-web.frt.vn/")
    print(f"2. Go to 'Bán hàng' screen")
    print(f"3. Search for SKU: {SKU}")
    print(f"4. Check if product appears in search results")
    print(f"5. Try to add product to cart manually")
    print()
    print("⚠️ Common reasons TC13 fails with this SKU:")
    print()
    print("   a) SKU không tồn tại trên UAT")
    print("   b) SKU hết hàng (stock = 0)")
    print("   c) SKU không active (status = inactive)")
    print("   d) SKU không thuộc campaign MUD KM-1026-106 (campaign 3753)")
    print("   e) SKU bị chặn bởi promotion rules")
    print()
    print("💡 SUGGESTED FIX:")
    print()
    print("   If SKU 00033695 doesn't work, try replacing with another SKU")
    print("   that you know exists and is active on UAT.")
    print()
    print("   Edit TC13.java line 40:")
    print(f'   From: private static final String BUY_PRODUCT_1 = "{SKU}";')
    print('   To:   private static final String BUY_PRODUCT_1 = "00040647"; // Working SKU from TC1')
    print()
    print("=" * 80)

if __name__ == "__main__":
    check_sku_on_uat()
