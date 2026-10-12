import json, urllib.request, sys

BASE = "http://ci-be-family-api.lc.frt.local"
FID = "894f0b6c-8553-4c31-90b8-4d5134cf930b"
OWNER = "7abdd140-df46-6600-4d93-3a02ca11bce6"
OWNER_NAME = "0835089254"
OCR = "1eb1a09e-d65b-4a75-8928-05d8b1d03f21"
TEEN = "3a2421c4-38e7-e954-9217-71d41c55675d"    # 086209012348
ADULT = "3a2421c5-1ec7-89c1-051e-e5bfeb540243"   # 086207012347
WHO = {"modifiedBy": OWNER, "modifiedByName": OWNER_NAME}

def call(method, path, body=None):
    data = json.dumps(body, ensure_ascii=False).encode("utf-8") if body is not None else None
    req = urllib.request.Request(BASE + path, data=data,
                                 headers={"Content-Type": "application/json"}, method=method)
    try:
        with urllib.request.urlopen(req, timeout=60) as r:
            return r.status, r.read().decode("utf-8")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8")
    except Exception as e:
        return 0, str(e)

def req(title, method, path, body=None, cut=700):
    print(f"\n>> {title}")
    print(f"   {method} {path}")
    if body is not None:
        print("   BODY:", json.dumps(body, ensure_ascii=False))
    code, r = call(method, path, body)
    print(f"   HTTP {code}: {r[:cut]}")
    return code, r

def member(mid):
    _, r = call("GET", f"/api/v1/families-person/{FID}/member/get-member-by-memberId/{mid}")
    try:
        d = json.loads(r)["data"]
        return {k: d.get(k) for k in ("nationalId", "dateOfBirth", "phoneNumber", "status", "isChild", "isTeenager")}
    except Exception:
        return r[:300]

def family():
    _, r = call("GET", f"/api/v1/families/get-by-customer/{OWNER}")
    d = json.loads(r)["data"]
    print(f"   [family] totalMember={d.get('totalMember')}")
    for m in d.get("members", []):
        print(f"     {m['nationalId']} dob={m['dateOfBirth']} phone='{m['phoneNumber']}' "
              f"status={m.get('status')} isChild={m.get('isChild')} isTeenager={m.get('isTeenager')} id={m['id']}")
    return d

step = (sys.argv[1] if len(sys.argv) > 1 else "all") if __name__ == "__main__" else "none"

if step in ("all", "s1"):
    print("#" * 90, "\nS1 - ADD teen that 086209012349 (2009-05-18), khong SDT")
    family()
    req("validate truoc khi add", "POST", "/api/v1/families-person/validate-create-person",
        {"familyId": FID, "familyPerson": [{"nationalId": "086209012349", "dateOfBirth": "2009-05-18",
                                            "isCaregiver": False, "isTeenager": True}]})
    req("create", "POST", f"/api/v1/families-person/{FID}",
        {"createdBy": OWNER, "createdByName": OWNER_NAME, "ocrId": OCR, "source": "App",
         "familyPerson": [{"reason": "test FP-1244", "fullName": "LÊ DUY KHANG", "dateOfBirth": "2009-05-18",
                           "gender": "MALE", "nationalId": "086209012349", "isCaregiver": False,
                           "isChild": False, "isTeenager": True, "role": ""}]}, cut=1500)
    family()
    req("invitation session (teen khong SDT -> khong duoc co loi moi)", "GET",
        f"/api/v1/families/invitation/session/{FID}", cut=1500)

if step in ("all", "s2"):
    print("#" * 90, "\nS2 - INACTIVE teen 086209012348 roi ACTIVE lai")
    print("   truoc:", member(TEEN))
    req("inactive teen", "POST", f"/api/v1/families-person/inactive-member/{FID}/member/{TEEN}",
        {**WHO, "reason": "test FP-1244 roi gia dinh"})
    print("   sau inactive:", member(TEEN))
    family()
    _, r = call("GET", f"/api/v1/families/get-by-customer/{OWNER}")
    req("check-inactive", "GET", f"/api/v1/families-person/check-inactive?customerIds=3a2421c4-376a-2989-fe49-8a21d87c6435")
    req("validate add lai teen vua roi (khong SDT)", "POST", "/api/v1/families-person/validate-create-person",
        {"familyId": FID, "familyPerson": [{"nationalId": "086209012348", "dateOfBirth": "2009-05-18",
                                            "isCaregiver": False, "isTeenager": True}]})
    req("active lai teen", "POST", f"/api/v1/families-person/active-member/{FID}/member/{TEEN}",
        {**WHO, "reason": "test FP-1244 active lai"})
    print("   sau active:", member(TEEN))
    family()

if step in ("all", "s3"):
    print("#" * 90, "\nS3 - INACTIVE nguoi lon 086207012347 (vao qua loi moi) roi ACTIVE lai")
    print("   truoc:", member(ADULT))
    req("inactive adult", "POST", f"/api/v1/families-person/inactive-member/{FID}/member/{ADULT}",
        {**WHO, "reason": "test FP-1244 roi gia dinh"})
    print("   sau inactive:", member(ADULT))
    req("invitation session sau inactive", "GET", f"/api/v1/families/invitation/session/{FID}", cut=1500)
    req("active lai adult", "POST", f"/api/v1/families-person/active-member/{FID}/member/{ADULT}",
        {**WHO, "reason": "test FP-1244 active lai"})
    print("   sau active:", member(ADULT))
    family()
