import json, urllib.request

BASE = "http://ci-be-family-api.lc.frt.local"
FID = "894f0b6c-8553-4c31-90b8-4d5134cf930b"

def post(path, body):
    req = urllib.request.Request(BASE + path, data=json.dumps(body).encode("utf-8"),
                                 headers={"Content-Type": "application/json"}, method="POST")
    try:
        with urllib.request.urlopen(req, timeout=20) as r:
            return r.status, json.loads(r.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8")

# (ten case, person payload, expected)
cases_extra = [
    ("TC8 teen 2009 isTeenager=true, CO SDT hop le",
     {"nationalId": "086209012349", "dateOfBirth": "2009-05-18", "isCaregiver": False, "isTeenager": True, "phoneNumber": "0912345678"},
     "isValid=true (co SDT van OK)"),
    ("TC9 nguoi lon 2007 isTeenager=false, CO SDT hop le",
     {"nationalId": "086207012347", "dateOfBirth": "2007-05-18", "isCaregiver": False, "isTeenager": False, "phoneNumber": "0912345679"},
     "isValid=true"),
    ("TC10 bien duoi: tron 13 hom nay (2013-10-06) isTeenager=true, khong SDT",
     {"nationalId": "086213012340", "dateOfBirth": "2013-10-06", "isCaregiver": False, "isTeenager": True},
     "isValid=true"),
    ("TC11 bien tren: 17t (2008-10-07, mai moi 18) isTeenager=true, khong SDT",
     {"nationalId": "086208012341", "dateOfBirth": "2008-10-07", "isCaregiver": False, "isTeenager": True},
     "isValid=true"),
    ("TC12 bien: tron 18 hom nay (2008-10-06) isTeenager=false, khong SDT",
     {"nationalId": "086208012342", "dateOfBirth": "2008-10-06", "isCaregiver": False, "isTeenager": False},
     "isValid=false, bat buoc SDT"),
    ("TC13 bien: tron 18 hom nay (2008-10-06) GIA isTeenager=true, khong SDT",
     {"nationalId": "086208012342", "dateOfBirth": "2008-10-06", "isCaregiver": False, "isTeenager": True},
     "isValid=false (server phai chan)"),
]

cases_old = [
    ("TC2 teen 2009 isTeenager=true, khong SDT",
     {"nationalId": "086209012349", "dateOfBirth": "2009-05-18", "isCaregiver": False, "isTeenager": True},
     "isValid=true"),
    ("TC3 teen 2009 (CCCD ...348) isTeenager=true, khong SDT",
     {"nationalId": "086209012348", "dateOfBirth": "2009-05-18", "isCaregiver": False, "isTeenager": True},
     "isValid=true"),
    ("TC4 nguoi lon 2007 (19t) isTeenager=false, khong SDT",
     {"nationalId": "086207012347", "dateOfBirth": "2007-05-18", "isCaregiver": False, "isTeenager": False},
     "isValid=false, bat buoc SDT"),
    ("TC5 nguoi lon 2007 (19t) GIA isTeenager=true, khong SDT",
     {"nationalId": "086207012347", "dateOfBirth": "2007-05-18", "isCaregiver": False, "isTeenager": True},
     "isValid=false (server phai chan co gia)"),
    ("TC6 teen 2009 isTeenager=true, SDT sai dinh dang '123'",
     {"nationalId": "086209012349", "dateOfBirth": "2009-05-18", "isCaregiver": False, "isTeenager": True, "phoneNumber": "123"},
     "isValid=false, loi dinh dang SDT"),
    ("TC7 nguoi lon 2007 (CCCD ...346) khong SDT",
     {"nationalId": "086207012346", "dateOfBirth": "2007-05-18", "isCaregiver": False, "isTeenager": False},
     "isValid=false, bat buoc SDT"),
]

import sys
cases = cases_old + cases_extra if "--all" in sys.argv else cases_extra

for name, person, expected in cases:
    code, resp = post("/api/v1/families-person/validate-create-person",
                      {"familyId": FID, "familyPerson": [person]})
    print("=" * 70)
    print(name)
    print("Expected:", expected)
    print("HTTP", code)
    print(json.dumps(resp, ensure_ascii=False) if not isinstance(resp, str) else resp)
