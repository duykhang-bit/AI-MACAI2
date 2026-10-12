#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Flow tao tai khoan gia dinh qua 3 API (UAT):
  1. POST /api/v1/families/ocr-create-family   -> OCR 4 anh
  2. POST /api/v1/families/check-before-create -> kiem tra truoc khi tao
  3. POST /api/v1/families                     -> tao gia dinh

Cach chay:
  1. Cai thu vien:   pip install requests
  2. Set token:      (PowerShell)  $env:FAMILY_TOKEN="eyJ..."    [KHONG dan token vao code]
  3. Chinh IMAGES o duoi: dan 4 link S3 hoac 4 duong dan file local
  4. Chay:           python create_family_flow.py

Ghi chu: Script phai chay tren may trong mang noi bo (reach duoc host .lc.frt.local).
"""

import os
import sys
import json
import mimetypes

try:
    import requests
except ImportError:
    sys.exit("Thieu thu vien 'requests'. Chay: pip install requests")

# ====================== CAU HINH ======================
BASE_URL = "http://uat-be-family-api.lc.frt.local"

OCR_URL = f"{BASE_URL}/api/v1/families/ocr-create-family"
CHECK_URL = f"{BASE_URL}/api/v1/families/check-before-create"
CREATE_URL = f"{BASE_URL}/api/v1/families"

# SDT chu ho
HEAD_PHONE = "0835089254"

# Token lay tu bien moi truong (KHONG hardcode token vao file)
TOKEN = os.environ.get("FAMILY_TOKEN", "").strip()

# 4 anh OCR: co the la link S3/URL hoac duong dan file local.
# Dan link that cua ban vao day:
IMAGES = [
    # "https://<bucket>.s3.amazonaws.com/.../khang99.png",
    # "https://<bucket>.s3.amazonaws.com/.../khang89.png",
    # "https://<bucket>.s3.amazonaws.com/.../khang09_2.png",
    # "https://<bucket>.s3.amazonaws.com/.../khang09_1.png",
    "test_ocr/khang99.png",
    "test_ocr/khang89.png",
    "test_ocr/khang09_2.png",
    "test_ocr/khang09_1.png",
]

# Cach API OCR nhan anh: "multipart" (upload file) hoac "json_url" (gui list URL trong body)
# Neu khong chac, thu "multipart" truoc; neu API tra loi loi field, doi sang "json_url".
OCR_MODE = "multipart"

# Ten field file khi upload multipart (tuy API, thuong la "files" hoac "images")
OCR_FILE_FIELD = "files"
# ======================================================


def headers(json_body=True):
    # Token la tuy chon: chi gui header Authorization neu co
    h = {"Authorization": f"Bearer {TOKEN}"} if TOKEN else {}
    if json_body:
        h["Content-Type"] = "application/json"
    return h


def load_bytes(src):
    """Tai anh ve dang bytes, du la URL hay file local."""
    if src.startswith("http://") or src.startswith("https://"):
        r = requests.get(src, timeout=60)
        r.raise_for_status()
        name = src.split("/")[-1].split("?")[0] or "image.png"
        return name, r.content
    # file local
    if not os.path.isfile(src):
        sys.exit(f"Khong tim thay file: {src}")
    with open(src, "rb") as f:
        return os.path.basename(src), f.read()


def step1_ocr():
    print("== BUOC 1: OCR create family ==")
    if OCR_MODE == "multipart":
        files = []
        opened = []
        for src in IMAGES:
            name, data = load_bytes(src)
            ctype = mimetypes.guess_type(name)[0] or "image/png"
            files.append((OCR_FILE_FIELD, (name, data, ctype)))
            opened.append(name)
        print(f"  Gui {len(files)} anh (multipart): {opened}")
        r = requests.post(OCR_URL, headers=headers(json_body=False), files=files, timeout=120)
    else:  # json_url
        body = {"images": IMAGES, "headPhone": HEAD_PHONE}
        print(f"  Gui JSON url list: {IMAGES}")
        r = requests.post(OCR_URL, headers=headers(), json=body, timeout=120)

    print(f"  Status: {r.status_code}")
    r.raise_for_status()
    data = r.json()
    print("  OCR response:", json.dumps(data, ensure_ascii=False, indent=2)[:2000])
    return data


def step2_check(ocr_data):
    print("== BUOC 2: Check before create ==")
    # Lay phan du lieu OCR tra ve de dua vao check. Field cu the tuy API.
    payload = ocr_data.get("data", ocr_data) if isinstance(ocr_data, dict) else ocr_data
    body = {"headPhone": HEAD_PHONE, "data": payload}
    r = requests.post(CHECK_URL, headers=headers(), json=body, timeout=60)
    print(f"  Status: {r.status_code}")
    r.raise_for_status()
    data = r.json()
    print("  Check response:", json.dumps(data, ensure_ascii=False, indent=2)[:2000])
    return data


def step3_create(ocr_data, check_data):
    print("== BUOC 3: Create family ==")
    payload = ocr_data.get("data", ocr_data) if isinstance(ocr_data, dict) else ocr_data
    body = {"headPhone": HEAD_PHONE, "data": payload}
    r = requests.post(CREATE_URL, headers=headers(), json=body, timeout=60)
    print(f"  Status: {r.status_code}")
    r.raise_for_status()
    data = r.json()
    print("  Create response:", json.dumps(data, ensure_ascii=False, indent=2))
    return data


def main():
    if not TOKEN:
        print("(Khong co token - goi API khong kem Authorization)")
    try:
        ocr = step1_ocr()
        chk = step2_check(ocr)
        res = step3_create(ocr, chk)
        print("\n== HOAN TAT: da tao gia dinh ==")
    except requests.HTTPError as e:
        print("\nLOI HTTP:", e)
        if e.response is not None:
            print("Body:", e.response.text[:2000])
        sys.exit(1)
    except requests.RequestException as e:
        print("\nLOI KET NOI (co the do khong reach duoc host noi bo):", e)
        sys.exit(1)


if __name__ == "__main__":
    main()
