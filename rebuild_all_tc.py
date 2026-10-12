import os, re

folder = r'c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master\src\test\java\testcases\uat\Mudsku'

# Doc TC1 lam template - KHONG SUA TC1
with open(os.path.join(folder, 'TC1.java'), 'r', encoding='utf-8') as f:
    tc1 = f.read()

# Data: tc_num -> (promo, campaign, product, qty, mud_key)
tc_data = {
    2:  ('KM-1026-095', '3742', '00502433', 2,  'mud02'),
    3:  ('KM-1026-096', '3743', '00502450', 2,  'mud03'),
    4:  ('KM-1026-097', '3744', '00502451', 2,  'mud04'),
    5:  ('KM-1026-098', '3745', '00502448', 1,  'mud05'),
    6:  ('KM-1026-099', '3746', '00502449', 1,  'mud06'),
    7:  ('KM-1026-100', '3747', '00019093', 1,  'mud07'),
    8:  ('KM-1026-101', '3748', '00019097', 1,  'mud08'),
    9:  ('KM-1026-102', '3749', '00019098', 1,  'mud09'),
    10: ('KM-1026-103', '3750', '00502375', 1,  'mud10'),
    11: ('KM-1026-104', '3751', '00502376', 1,  'mud11'),
    12: ('KM-1026-105', '3752', '00502377', 1,  'mud12'),
    13: ('KM-1026-106', '3753', '00016989', 1,  'mud13'),
    14: ('KM-1026-107', '3754', '00016991', 1,  'mud14'),
    15: ('KM-1026-108', '3755', '00016992', 1,  'mud15'),
    16: ('KM-1026-109', '3756', '00502441', 1,  'mud16'),
    17: ('KM-1026-110', '3757', '00502442', 1,  'mud17'),
    18: ('KM-1026-111', '3758', '00502443', 1,  'mud18'),
    19: ('KM-1026-112', '3759', '00031033', 1,  'mud19'),
    20: ('KM-1026-113', '3760', '00031034', 1,  'mud20'),
    21: ('KM-1026-114', '3761', '00031036', 1,  'mud21'),
    22: ('KM-1026-115', '3762', '00031038', 1,  'mud22'),
    23: ('KM-1026-116', '3763', '00031039', 1,  'mud23'),
    24: ('KM-1026-117', '3764', '00031040', 1,  'mud24'),
    26: ('KM-1026-119', '3766', '00018754', 1,  'mud26'),
    27: ('KM-1026-120', '3767', '00018755', 1,  'mud27'),
    28: ('KM-1026-121', '3768', '00015682', 1,  'mud28'),
    29: ('KM-1026-122', '3769', '00015683', 1,  'mud29'),
    30: ('KM-1026-123', '3770', '00015684', 1,  'mud30'),
}

for tc_num, (promo, campaign, product, qty, mud_key) in tc_data.items():
    c = tc1

    # class name
    c = c.replace('public class TC1 extends', 'public class TC{} extends'.format(tc_num))

    # method name
    c = c.replace('public void TC001()', 'public void TC{:03d}()'.format(tc_num))

    # constants
    c = re.sub(r'PROMOTION_CODE  = "KM-1026-094"', 'PROMOTION_CODE  = "{}"'.format(promo), c)
    c = re.sub(r'CAMPAIGN_CODE   = "3741"',         'CAMPAIGN_CODE   = "{}"'.format(campaign), c)
    c = re.sub(r'BUY_PRODUCT_1   = "00040647"',     'BUY_PRODUCT_1   = "{}"'.format(product), c)
    c = re.sub(r'BUY_QTY         = 2',              'BUY_QTY         = {}'.format(qty), c)

    # description
    c = c.replace('description = "KM-1026-094 -- Mua 2 tang 1 - Nuoc mat nhan tao Easimist Eye Drops 5ml"',
                  'description = "{}"'.format(promo))

    # mud key
    c = c.replace('"mud01"', '"{}"'.format(mud_key))

    # log prefix
    c = c.replace('[TC1]', '[TC{}]'.format(tc_num))
    c = c.replace('TC1 FAIL', 'TC{} FAIL'.format(tc_num))
    c = c.replace('"TC1 - " + PROMOTION_CODE', '"TC{} - " + PROMOTION_CODE'.format(tc_num))
    c = c.replace('"TC1 xong', '"TC{} xong'.format(tc_num))

    out = os.path.join(folder, 'TC{}.java'.format(tc_num))
    with open(out, 'w', encoding='utf-8') as f:
        f.write(c)
    print('OK TC{} | {} | {} | qty={} | {}'.format(tc_num, promo, product, qty, mud_key))

print('Done! {} files.'.format(len(tc_data)))
