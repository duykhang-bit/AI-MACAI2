import json, urllib.request
from tc_addremove import call, FID, OWNER, OWNER_NAME, TEEN

WHO = {"modifiedBy": OWNER, "modifiedByName": OWNER_NAME}
GET = f"/api/v1/families-person/{FID}/member/get-member-by-memberId/{TEEN}"

print("1) GET khi ACTIVE")
c, r = call("GET", GET); print("   HTTP", c, r[:250])

print("2) INACTIVE teen")
c, r = call("POST", f"/api/v1/families-person/inactive-member/{FID}/member/{TEEN}", {**WHO, "reason": "test FP-1244"})
print("   HTTP", c, "status=", json.loads(r)["data"]["status"])

print("3) GET khi INACTIVE  <-- bug")
c, r = call("GET", GET); print("   HTTP", c, r)

print("4) RSA members/detail khi INACTIVE (doi chung)")
c, r = call("GET", f"/api/v1/rsa/families/{FID}/members/detail")
print("   HTTP", c, "so member:", len(json.loads(r)["data"]), "| co teen?", "086209012348" in r)

print("5) full-members/by-customer khi INACTIVE (doi chung)")
c, r = call("GET", f"/api/v1/families/full-members/by-customer/{OWNER}")
for m in json.loads(r)["data"]["members"]:
    if m["nationalId"] == "086209012348":
        print("   ", {k: m.get(k) for k in ("id", "nationalId", "status", "isTeenager", "isChild")})

print("6) ACTIVE lai")
c, r = call("POST", f"/api/v1/families-person/active-member/{FID}/member/{TEEN}", {**WHO, "reason": "restore"})
print("   HTTP", c, "status=", json.loads(r)["data"]["status"])
c, r = call("GET", GET); print("   GET lai: HTTP", c, r[-120:])
