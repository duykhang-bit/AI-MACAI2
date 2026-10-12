#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Rebuild TC25new tu TC26 (dang PASS).

Root cause TC25new FAIL:
  TC25new dung xpath text ASCII khong dau ('Hoan tat', 'Ban hang', 'Xac nhan', 'Ap dung'...)
  nhung UI thuc te la tieng Viet co dau ('Hoan tat' -> 'Hoàn tất')
  => toan bo xpath khong match => timeout tai TC03.

Fix: clone TC26 (da dung \\u escape dung) roi thay constants cua TC25new.
"""

import os
import re

BASE_DIR = os.path.join(
    r'c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master',
    'src', 'test', 'java', 'testcases', 'uat', 'Mudsku'
)

SRC = os.path.join(BASE_DIR, 'TC26.java')
DST = os.path.join(BASE_DIR, 'TC25new.java')

NEW_DESC = ("KM-1026-118 - TK Gia Dinh - Giam gia 50% - "
            "Dung dich ve sinh phu nu Natural Feminine La Trau Khong (100ml)")

# (old, new) - thu tu quan trong
REPLACEMENTS = [
    # class + method
    ('public class TC26 extends BaseTest1', 'public class TC25new extends BaseTest1'),
    ('public void TC026()', 'public void TC025new()'),
    # constants
    ('private static final String PROMOTION_CODE  = "KM-1026-119";',
     'private static final String PROMOTION_CODE  = "KM-1026-118";'),
    ('private static final String CAMPAIGN_CODE   = "3766";',
     'private static final String CAMPAIGN_CODE   = "3765";'),
    ('private static final String BUY_PRODUCT_1   = "D042300000205";',
     'private static final String BUY_PRODUCT_1   = "00031035";'),
    # mud pool
    ('getNextMudCode("mud26"', 'getNextMudCode("mud25"'),
    # logs / messages
    ('"[TC26] MUD serial: "', '"[TC25new] MUD serial: "'),
    ('"TC26 FAIL - khong co MUD serial!"', '"TC25new FAIL - khong co MUD serial!"'),
    ('"TC26 - " + PROMOTION_CODE', '"TC25new - " + PROMOTION_CODE'),
    ('"TC26 FAIL: "', '"TC25new FAIL: "'),
    ('"TC26 xong - KM "', '"TC25new xong - KM "'),
]


def main():
    if not os.path.exists(SRC):
        print(f'ERROR: khong tim thay {SRC}')
        return 1

    with open(SRC, 'r', encoding='utf-8') as f:
        content = f.read()

    applied, missed = [], []
    for old, new in REPLACEMENTS:
        if old in content:
            content = content.replace(old, new)
            applied.append(old[:60])
        else:
            missed.append(old[:60])

    # description cua @Test
    new_content, n = re.subn(
        r'(@Test\(priority = 1, description = ")[^"]*(")',
        r'\g<1>' + NEW_DESC + r'\g<2>',
        content,
        count=1
    )
    if n == 1:
        content = new_content
        applied.append('@Test description')
    else:
        missed.append('@Test description')

    # Bat con sot "TC26" nao chua thay
    leftovers = [
        (i + 1, ln.strip())
        for i, ln in enumerate(content.split('\n'))
        if 'TC26' in ln or 'mud26' in ln
    ]

    with open(DST, 'w', encoding='utf-8') as f:
        f.write(content)

    print(f'Da ghi {DST}')
    print(f'  Applied : {len(applied)}')
    for a in applied:
        print(f'    + {a}')
    if missed:
        print(f'  MISSED  : {len(missed)}')
        for m in missed:
            print(f'    ! {m}')
    if leftovers:
        print(f'  CON SOT TC26/mud26 ({len(leftovers)} dong):')
        for ln, txt in leftovers:
            print(f'    line {ln}: {txt[:100]}')
    else:
        print('  Khong con sot TC26/mud26')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
