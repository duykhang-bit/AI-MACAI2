import json, urllib.request

BASE = "http://ci-be-family-api.lc.frt.local"
FID = "894f0b6c-8553-4c31-90b8-4d5134cf930b"
TEEN = "3a2421c4-38e7-e954-9217-71d41c55675d"   # 086209012348, goc 2009-05-18
ADULT = "3a2421c5-1ec7-89c1-051e-e5bfeb540243"  # 086207012347, goc 2007-05-18

def call(method, path, body=None):
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(BASE + path, data=data,
                                 headers={"Content-Type": "application/json"}, method=method)
    try:
        with urllib.request.urlopen(req, timeout=40) as r:
            return r.status, r.read().decode("utf-8")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8")

def get(mid):
    _, r = call("GET", f"/api/v1/families-person/{FID}/member/get-member-by-memberId/{mid}")
    d = json.loads(r)["data"]
    return {k: d.get(k) for k in ("nationalId", "dateOfBirth", "phoneNumber", "isChild", "isTeenager")}

def edit(mid, dob):
    code, r = call("POST", f"/api/v1/families-person/{FID}/member/edit-member-by-memberId/{mid}",
                   {"dateOfBirth": dob, "gender": "MALE", "avatarUrl": ""})
    return code, r[:400]

steps = [
    ("E1 teen -> doi dob thanh 2007 (19t), van khong co SDT", TEEN, "2007-05-18"),
    ("E1 restore teen ve 2009", TEEN, "2009-05-18"),
    ("E2 adult -> doi dob thanh 2009 (17t)", ADULT, "2009-05-18"),
    ("E2 restore adult ve 2007", ADULT, "2007-05-18"),
    ("E3 teen -> dob 2015 (11t, child)", TEEN, "2015-05-18"),
    ("E3 restore teen ve 2009", TEEN, "2009-05-18"),
]

for title, mid, dob in steps:
    print("=" * 80)
    print(title)
    print("  before:", get(mid))
    print("  edit  :", edit(mid, dob))
    print("  after :", get(mid))
