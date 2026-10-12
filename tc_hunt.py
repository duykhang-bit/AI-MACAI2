import json, urllib.request, sys

BASE = "http://ci-be-family-api.lc.frt.local"
FID = "894f0b6c-8553-4c31-90b8-4d5134cf930b"
OWNER = "7abdd140-df46-6600-4d93-3a02ca11bce6"

def call(method, path, body=None):
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(BASE + path, data=data,
                                 headers={"Content-Type": "application/json"}, method=method)
    try:
        with urllib.request.urlopen(req, timeout=40) as r:
            return r.status, r.read().decode("utf-8")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8")
    except Exception as e:
        return 0, str(e)

def walk(obj, path=""):
    """Tim moi object co nationalId, in cac field lien quan tuoi."""
    if isinstance(obj, dict):
        if "nationalId" in obj or "dateOfBirth" in obj:
            keys = ["id", "nationalId", "dateOfBirth", "age", "phoneNumber", "isChild", "isTeenager"]
            print("   ", path or "/", {k: obj.get(k, "<MISSING>") for k in keys})
        for k, v in obj.items():
            walk(v, f"{path}.{k}")
    elif isinstance(obj, list):
        for i, v in enumerate(obj):
            walk(v, f"{path}[{i}]")

def show(title, path, dump=False):
    code, resp = call("GET", path)
    print("=" * 80)
    print(title, "->", path)
    print("HTTP", code)
    try:
        j = json.loads(resp)
        walk(j)
        if dump:
            print(json.dumps(j, ensure_ascii=False)[:3000])
        return j
    except Exception:
        print(resp[:1500])
        return None

mode = sys.argv[1] if len(sys.argv) > 1 else "get"

if mode == "get":
    j = show("1. get-by-customer", f"/api/v1/families/get-by-customer/{OWNER}", dump=True)
    code_ = None
    if j:
        s = json.dumps(j)
        import re
        m = re.search(r'"(?:familyCode|code)"\s*:\s*"([^"]+)"', s)
        code_ = m.group(1) if m else None
    print("\nfamilyCode detect:", code_)
    if code_:
        show("2. family-info by code", f"/api/v1/families/code/{code_}/family-info")
    show("3. invitation history", f"/api/v1/families/invitation/history/{FID}", dump=True)
    show("4. invitation session", f"/api/v1/families/invitation/session/{FID}", dump=True)
    show("5. full-members by customer", f"/api/v1/families/full-members/by-customer/{OWNER}")
    show("6. info-by-customer", f"/api/v1/families/info-by-customer/{OWNER}")
    show("7. rsa members/detail", f"/api/v1/rsa/families/{FID}/members/detail")
    show("8. rsa members", f"/api/v1/rsa/families/{FID}/members")
