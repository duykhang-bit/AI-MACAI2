import os
import re

folder = r'c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku'

# Đọc TC1 làm template
with open(os.path.join(folder, 'TC1.java'), 'r', encoding='utf-8') as f:
    tc1_template = f.read()

print('Loaded TC1 template')

# Mapping TC → constants (từ bảng data ban đầu)
tc_data = {
    2: {'promo': 'KM-1026-095', 'campaign': '3742', 'product': '00502433', 'qty': 2, 'desc': 'Mua 2 tang 1 - Cao dan giam dau Chi Thong Cao 20 goi'},
    3: {'promo': 'KM-1026-096', 'campaign': '3743', 'product': '00502450', 'qty': 2, 'desc': 'Mua 2 tang 1 - Cao dan giam dau Hoa Binh Vuong 20 goi'},
    4: {'promo': 'KM-1026-097', 'campaign': '3744', 'product': '00502451', 'qty': 2, 'desc': 'Mua 2 tang 1 - Cao dan giam dau Tinh Chat Xanh 20 goi'},
    5: {'promo': 'KM-1026-098', 'campaign': '3745', 'product': '00502448', 'qty': 1, 'desc': 'Mua 1 tang 1 - Cao dan giam dau Xanh Da Tinh Dau 20 goi'},
    6: {'promo': 'KM-1026-099', 'campaign': '3746', 'product': '00502449', 'qty': 1, 'desc': 'Mua 1 tang 1 - Cao dan giam dau Bac Ha Xanh 20 goi'},
    7: {'promo': 'KM-1026-100', 'campaign': '3747', 'product': '00019093', 'qty': 1, 'desc': 'Mua 1 tang 1 - Vien sui Mekong Pharma 500mg 100 vien'},
    8: {'promo': 'KM-1026-101', 'campaign': '3748', 'product': '00019097', 'qty': 1, 'desc': 'Mua 1 tang 1 - Vien sui Mekong Pharma 500mg 50 vien'},
    9: {'promo': 'KM-1026-102', 'campaign': '3749', 'product': '00019098', 'qty': 1, 'desc': 'Mua 1 tang 1 - Vien sui Mekong Pharma 500mg 20 vien'},
    10: {'promo': 'KM-1026-103', 'campaign': '3750', 'product': '00502375', 'qty': 1, 'desc': 'Giam gia 30% - Vien sui vang Mekong Pharma 500mg 100 vien'},
    11: {'promo': 'KM-1026-104', 'campaign': '3751', 'product': '00502376', 'qty': 1, 'desc': 'Giam gia 30% - Vien sui vang Mekong Pharma 500mg 50 vien'},
    12: {'promo': 'KM-1026-105', 'campaign': '3752', 'product': '00502377', 'qty': 1, 'desc': 'Giam gia 30% - Vien sui vang Mekong Pharma 500mg 20 vien'},
    13: {'promo': 'KM-1026-106', 'campaign': '3753', 'product': '00016989', 'qty': 1, 'desc': 'Giam gia 30% - Vien sui trang Mekong Pharma 500mg 100 vien'},
    14: {'promo': 'KM-1026-107', 'campaign': '3754', 'product': '00016991', 'qty': 1, 'desc': 'Giam gia 30% - Vien sui trang Mekong Pharma 500mg 50 vien'},
    15: {'promo': 'KM-1026-108', 'campaign': '3755', 'product': '00016992', 'qty': 1, 'desc': 'Giam gia 30% - Vien sui trang Mekong Pharma 500mg 20 vien'},
    16: {'promo': 'KM-1026-109', 'campaign': '3756', 'product': '00502441', 'qty': 1, 'desc': 'Giam gia 30% - Vien sui xanh Mekong Pharma 500mg 100 vien'},
    17: {'promo': 'KM-1026-110', 'campaign': '3757', 'product': '00502442', 'qty': 1, 'desc': 'Giam gia 30% - Vien sui xanh Mekong Pharma 500mg 50 vien'},
    18: {'promo': 'KM-1026-111', 'campaign': '3758', 'product': '00502443', 'qty': 1, 'desc': 'Giam gia 30% - Vien sui xanh Mekong Pharma 500mg 20 vien'},
    19: {'promo': 'KM-1026-112', 'campaign': '3759', 'product': '00031033', 'qty': 1, 'desc': 'Giam gia 30% - Dung dich ve sinh phu nu Natural Feminine La Trau Khong 100ml'},
    20: {'promo': 'KM-1026-113', 'campaign': '3760', 'product': '00031034', 'qty': 1, 'desc': 'Giam gia 50% - Dung dich ve sinh phu nu Natural Feminine La Trau Do 100ml'},
    21: {'promo': 'KM-1026-114', 'campaign': '3761', 'product': '00031036', 'qty': 1, 'desc': 'Giam gia 50% - Dung dich ve sinh phu nu Natural Feminine La Trau Hong 100ml'},
    22: {'promo': 'KM-1026-115', 'campaign': '3762', 'product': '00031038', 'qty': 1, 'desc': 'Giam gia 50% - Dung dich ve sinh phu nu Natural Feminine La Trau Xanh 100ml'},
    23: {'promo': 'KM-1026-116', 'campaign': '3763', 'product': '00031039', 'qty': 1, 'desc': 'Giam gia 50% - Dung dich ve sinh phu nu Natural Feminine La Trau Vang 100ml'},
    24: {'promo': 'KM-1026-117', 'campaign': '3764', 'product': '00031040', 'qty': 1, 'desc': 'Giam gia 50% - Dung dich ve sinh phu nu Natural Feminine La Trau Xanh La 100ml'},
    26: {'promo': 'KM-1026-119', 'campaign': '3766', 'product': '00018754', 'qty': 1, 'desc': 'Qua tang - Vitamin tong hop 21st Century Sentry 200 vien'},
    27: {'promo': 'KM-1026-120', 'campaign': '3767', 'product': '00018755', 'qty': 1, 'desc': 'Qua tang - Vitamin tong hop 21st Century Sentry 130 vien'},
    28: {'promo': 'KM-1026-121', 'campaign': '3768', 'product': '00015682', 'qty': 1, 'desc': 'Qua tang - Vitamin tong hop Cerovite Silver 60 vien'},
    29: {'promo': 'KM-1026-122', 'campaign': '3769', 'product': '00015683', 'qty': 1, 'desc': 'Qua tang - Vitamin tong hop Cerovite Silver 120 vien'},
    30: {'promo': 'KM-1026-123', 'campaign': '3770', 'product': '00015684', 'qty': 1, 'desc': 'Qua tang - Vitamin tong hop Cerovite Silver 180 vien'},
}

files_created = 0

for tc_num, data in tc_data.items():
    # Clone TC1
    new_content = tc1_template
    
    # Replace class name
    new_content = new_content.replace('public class TC1 extends BaseTest1', f'public class TC{tc_num} extends BaseTest1')
    
    # Replace method name
    new_content = new_content.replace('public void TC001()', f'public void TC{tc_num:03d}()')
    
    # Replace constants (tìm block constants từ TC1, thay thế toàn bộ)
    constants_pattern = r'(private static final String SHOP_CODE.*?private static final int\s+BUY_QTY\s+=\s+\d+;)'
    
    new_constants = f'''private static final String SHOP_CODE       = "80006";
    private static final String INSIDE_CODE     = "00017";
    private static final String CUSTOMER_PHONE  = "0835089254";
    private static final String PROMOTION_CODE  = "{data['promo']}";
    private static final String CAMPAIGN_CODE   = "{data['campaign']}";
    private static final String BUY_PRODUCT_1   = "{data['product']}";
    private static final int    BUY_QTY         = {data['qty']};'''
    
    new_content = re.sub(constants_pattern, new_constants, new_content, flags=re.DOTALL)
    
    # Replace description
    new_content = re.sub(
        r'description = ".*?"',
        f'description = "{data["promo"]} -- {data["desc"]}"',
        new_content
    )
    
    # Replace mud key
    old_mud = '"mud01"'
    new_mud = f'"mud{tc_num:02d}"'
    new_content = new_content.replace(old_mud, new_mud)
    
    # Replace log messages
    new_content = new_content.replace('[TC1]', f'[TC{tc_num}]')
    new_content = new_content.replace('TC1 FAIL', f'TC{tc_num} FAIL')
    
    # Write file
    output_path = os.path.join(folder, f'TC{tc_num}.java')
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    
    print(f'OK: Created TC{tc_num}.java from TC1 template')
    files_created += 1

print(f'Done! Cloned TC1 to {files_created} files.')
