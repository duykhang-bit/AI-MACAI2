import json, sys, urllib.request

BASE = "http://uat-be-family-api.lc.frt.local"
PHONE = "0835089254"
IMAGES = ["test_ocr/khang99.png", "test_ocr/khang09_1.png", "test_ocr/Khang07.png"]

def call(method, path, body=None):
    data = json.dumps(body, ensure_ascii=False).encode("utf-8") if body is not None else None
    req = urllib.request.Request(BASE + path, data=data,
                                 headers={"Content-Type": "application/json"}, method=method)
    try:
        with urllib.request.urlopen(req, timeout=120) as r:
            return r.status, r.read().decode("utf-8")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8")
    except Exception as e:
        return 0, str(e)

body = {"phoneNumber": PHONE, "residenceCertificateUrls": IMAGES, "marriageORBirthCertificateUrls": []}
print("STEP 1 ocr-create-family")
print("BODY:", json.dumps(body, ensure_ascii=False))
code, r = call("POST", "/api/v1/families/ocr-create-family", body)
print("HTTP", code)
print(r)
open("uat_ocr_result.json", "w", encoding="utf-8").write(r)
