import os, re

folder = r'c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku'

with open(os.path.join(folder, 'TC1.java'), 'r', encoding='utf-8') as f:
    tc1 = f.read()

tc_data = {
    10: {'promo': 'KM-1026-103', 'campaign': '3750', 'product': '00502375', 'qty': 1, 'desc': 'Giam gia 30%'},
    24: {'promo': 'KM-1026-117', 'campaign': '3764', 'product': '00031040', 'qty': 1, 'desc': 'Giam gia 50%'},
}

for tc_num, data in tc_data.items():
    c = tc1
    c = c.replace('public class TC1 ', f'public class TC{tc_num} ')
    c = c.replace('public void TC001()', f'public void TC{tc_num:03d}()')
    constants = f'''private static final String SHOP_CODE       = "80006";
    private static final String INSIDE_CODE     = "00017";
    private static final String CUSTOMER_PHONE  = "0835089254";
    private static final String PROMOTION_CODE  = "{data['promo']}";
    private static final String CAMPAIGN_CODE   = "{data['campaign']}";
    private static final String BUY_PRODUCT_1   = "{data['product']}";
    private static final int    BUY_QTY         = {data['qty']};'''
    c = re.sub(r'private static final String SHOP_CODE.*?private static final int\s+BUY_QTY\s+=\s+\d+;', constants, c, flags=re.DOTALL)
    c = re.sub(r'description = ".*?"', f'description = "{data["promo"]} -- {data["desc"]}"', c)
    c = c.replace('"mud01"', f'"mud{tc_num:02d}"')
    c = c.replace('[TC1]', f'[TC{tc_num}]')
    c = c.replace('TC1 FAIL', f'TC{tc_num} FAIL')
    with open(os.path.join(folder, f'TC{tc_num}.java'), 'w', encoding='utf-8') as f:
        f.write(c)
    print(f'Fixed TC{tc_num}.java')
