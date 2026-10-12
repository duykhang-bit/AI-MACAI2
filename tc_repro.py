import json, urllib.request

BASE = "http://ci-be-family-api.lc.frt.local"
FID = "894f0b6c-8553-4c31-90b8-4d5134cf930b"
TEEN = "3a2421c4-38e7-e954-9217-71d41c55675d"   # CCCD 086209012348, goc 2009-05-18, khong SDT
ADULT = "3a2421c5-1ec7-89c1-051e-e5bfeb540243"  # CCCD 086207012347, goc 2007-05-18, SDT 0835089244
OLD1 = "3a242156-b8bb-24b1-c2f2-1979a77fe515"   # CCCD 086208012347
OLD2 = "3a242156-b915-78b0-ae68-4d7fcdc94544"   # CCCD 086207012346

def call(method, path, body=None):
    data = json.dumps(body, ensure_ascii=False).encode("utf-8") if body is not None else None
    req = urllib.request.Request(BASE + path, data=data,
                                 headers={"Content-Type": "application/json"}, method=method)
    try:
        with urllib.request.urlopen(req, timeout=40) as r:
            return r.status, r.read().decode("utf-8")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8")

def short(mid):
    _, r = call("GET", f"/api/v1/families-person/{FID}/member/get-member-by-memberId/{mid}")
    d = json.loads(r)["data"]
    return {k: d.get(k) for k in ("nationalId", "dateOfBirth", "phoneNumber", "isChild", "isTeenager")}

def show_req(method, path, body=None):
    print(f"  REQUEST : {method} {BASE}{path}")
    if body is not None:
        print("  BODY    :", json.dumps(body, ensure_ascii=False))
    code, r = call(method, path, body)
    try:
        j = json.loads(r)
        if "data" in j and isinstance(j["data"], dict) and "data" in j["data"]:
            j = j["data"]["data"]
        elif "data" in j and isinstance(j["data"], dict):
            d = j["data"]
            j = {k: d.get(k) for k in ("id", "nationalId", "dateOfBirth", "phoneNumber", "isChild", "isTeenager")}
        r = json.dumps(j, ensure_ascii=False)
    except Exception:
        pass
    print(f"  RESPONSE: HTTP {code} {r}")
    return code

print("#" * 90)
print("BUG 1 - edit dob teen -> 19 tuoi, khong SDT")
print("  Truoc :", short(TEEN))
p = f"/api/v1/families-person/{FID}/member/edit-member-by-memberId/{TEEN}"
show_req("POST", p, {"dateOfBirth": "2007-05-18", "gender": "MALE", "avatarUrl": ""})
print("  Sau   :", short(TEEN))
call("POST", p, {"dateOfBirth": "2009-05-18", "gender": "MALE", "avatarUrl": ""})
print("  Restore:", short(TEEN))

print("#" * 90)
print("BUG 2 - edit dob lech nam sinh CCCD (CCCD 07 -> dob 2009)")
print("  Truoc :", short(ADULT))
p = f"/api/v1/families-person/{FID}/member/edit-member-by-memberId/{ADULT}"
show_req("POST", p, {"dateOfBirth": "2009-05-18", "gender": "MALE", "avatarUrl": ""})
print("  Sau   :", short(ADULT))
call("POST", p, {"dateOfBirth": "2007-05-18", "gender": "MALE", "avatarUrl": ""})
print("  Restore:", short(ADULT))
print("  Data cu:")
print("   ", short(OLD1))
print("   ", short(OLD2))

print("#" * 90)
print("BUG 3 - validate tin co isTeenager cua client")
p = "/api/v1/families-person/validate-create-person"
for label, person in [
    ("3a. 19 tuoi, gui isTeenager=true, khong SDT  (mong doi: bi chan)",
     {"nationalId": "086207012347", "dateOfBirth": "2007-05-18", "isCaregiver": False, "isTeenager": True}),
    ("3b. 19 tuoi, gui isTeenager=false, khong SDT (doi chung: bi chan)",
     {"nationalId": "086207012347", "dateOfBirth": "2007-05-18", "isCaregiver": False, "isTeenager": False}),
    ("3c. tron 18 hom nay, gui isTeenager=true, khong SDT (mong doi: bi chan)",
     {"nationalId": "086208012342", "dateOfBirth": "2008-10-06", "isCaregiver": False, "isTeenager": True}),
    ("3d. teen 17 tuoi, KHONG gui isTeenager, khong SDT (mong doi: cho qua)",
     {"nationalId": "086209012349", "dateOfBirth": "2009-05-18", "isCaregiver": False}),
]:
    print(" ", label)
    show_req("POST", p, {"familyId": FID, "familyPerson": [person]})
