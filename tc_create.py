import json, urllib.request

BASE = "http://ci-be-family-api.lc.frt.local"
FID = "894f0b6c-8553-4c31-90b8-4d5134cf930b"
OWNER = "7abdd140-df46-6600-4d93-3a02ca11bce6"

def call(method, path, body=None):
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(BASE + path, data=data,
                                 headers={"Content-Type": "application/json"}, method=method)
    try:
        with urllib.request.urlopen(req, timeout=30) as r:
            return r.status, r.read().decode("utf-8")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8")

body = {
    "createdBy": OWNER,
    "createdByName": "0835089254",
    "ocrId": "",
    "source": "App",
    "familyPerson": [{
        "reason": "test FP-1244 teenager",
        "fullName": "LÊ DUY KHANG",
        "dateOfBirth": "2009-05-18",
        "gender": "MALE",
        "nationalId": "086209012349",
        "isCaregiver": False,
        "isChild": False,
        "isTeenager": True,
        "role": ""
    }]
}
print("=== CREATE teen 086209012349, khong SDT ===")
code, resp = call("POST", f"/api/v1/families-person/{FID}", body)
print("HTTP", code)
print(resp)

print("\n=== GET members/detail sau khi add ===")
code, resp = call("GET", f"/api/v1/rsa/families/{FID}/members/detail")
members = json.loads(resp).get("data", [])
for m in members:
    c2, r2 = call("GET", f"/api/v1/families-person/{FID}/member/get-member-by-memberId/{m['id']}")
    d = json.loads(r2).get("data", {})
    print(f"{d.get('nationalId')} | dob={d.get('dateOfBirth')} | phone='{d.get('phoneNumber')}' | "
          f"isChild={d.get('isChild')} | isTeenager={d.get('isTeenager')} | status={d.get('status')} | id={d.get('id')}")
