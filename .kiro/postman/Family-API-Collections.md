# 📋 Family API Collections

---

## 📁 Customer Identity

### GET — check sdt đã có gia đình chưa
```
GET http://ci-customer-identity-application-v2.lc.frt.local/api/v1/internal/customers/identities?identifierType=contact&identifierValue=0901933138
```

### GET — 1 ge cusid
```
GET (chưa có URL)
```

### GET — lấy cccd theo sdt
```
GET (chưa có URL)
```

---

## 📁 Family Core

### GET — get thành viên gia đình
```
GET http://ci-family-core.lc.frt.local/api/v1/families/customerId/{customerId}
```
Headers:
```
accept: text/plain
X-Requested-With: XMLHttpRequest
```

### PUT — xóa từng thành viên
```
PUT http://ci-family-core.lc.frt.local/api/v1/families/{familyId}/members/{memberId}/status/inactivate
```
Headers:
```
accept: text/plain
Content-Type: application/json
X-Requested-With: XMLHttpRequest
```
Body:
```json
{
  "modifiedBy": "string",
  "modifiedByName": "string",
  "reason": "string"
}
```

### PUT — xóa family
```
PUT http://ci-family-core.lc.frt.local/api/v1/families/{familyId}/submit-inactivate
```
Headers:
```
accept: text/plain
Content-Type: application/json
X-Requested-With: XMLHttpRequest
```
Body:
```json
{
  "modifiedBy": "string",
  "modifiedByName": "string",
  "note": "string"
}
```

---

## 📁 Loyalty & Points

### GET — loyalty (điểm gia đình theo token)
```
GET http://uat-ecom-customer-api.lc.frt.local/api/customer-api/v3/token/families/family-loyalty/point?TimeRange=6
```
Headers:
```
accept: application/json
Authorization: Bearer ___0964802464
```

### GET — get điểm gia đình
```
GET http://ci-fpt-loyalty-app-api-lc.frt.local/api/loyalty/{familyId}/point?includeExpiryPoint=false
```
Headers:
```
accept: text/plain
X-Requested-With: XMLHttpRequest
```

### POST — get points list (nhiều customer)
```
POST http://ci-fpt-loyalty-app-api-lc.frt.local/api/loyalty/customers/points/list
```
Headers:
```
accept: text/plain
Content-Type: application/json
X-Requested-With: XMLHttpRequest
```
Body:
```json
{
  "customerIds": [
    "3a1dfe9d-ccd0-874c-8bbd-d2c82aa282d8",
    "5c6a1b07-af7c-af68-b808-3a1dc714712c"
  ]
}
```

### PUT — cho điểm active
```
PUT http://uat-core-customer-service.lc.frt.local/api/customer/{customerId}/subscribe/0
```
Headers:
```
accept: text/plain
Content-Type: application/json
```
Body:
```json
{
  "isZaloLc": true,
  "isApplc": true
}
```

---

## 📁 Gamification

### GET — suggestion
```
GET http://ci-gamification-orchestrator.lc.frt.local/api/gamification/suggestion/item?familyId={familyId}
```
Headers:
```
accept: text/plain
X-Requested-With: XMLHttpRequest
```

### GET — get sl gd từng chặng
```
GET http://uat-be-family-api.lc.frt.local/api/v1/family-package-reward/gameId/00000000-0000-0000-0000-000000000001/familyId/{familyId}/family-game
```
Headers:
```
accept: application/json
```

### GET — get gd từng chặng real
```
GET http://uat-game-core-v2.lc.frt.local/api/v1/family-reward/gameId/00000000-0000-0000-0000-000000000001/familyId/{familyId}/family-game
```
Headers:
```
accept: text/plain
X-Requested-With: XMLHttpRequest
```

---

## 📁 VAC

### GET — check phải gia đình vac k
```
GET http://uat-ecom-customer-api.lc.frt.local/api/customer-api/v3/token/families/check-migrate-vac
```
Headers:
```
accept: application/json
Authorization: Bearer ___0964802464
```

### POST — revert chi tiêu vac
```
POST http://uat-family-metric-core.lc.frt.local/api/family-metrics/event
```
Headers:
```
accept: /
Content-Type: application/json
X-Requested-With: XMLHttpRequest
```
Body:
```json
{
  "famiyId": "608e3865-7d82-4587-a526-afeb762cbdb2",
  "customerId": "3a201340-e176-a59a-19f2-f6b006548ec3",
  "refEventCode": "3a201340-e176-a59a-19f2-f6b006548ec3",
  "refCode": "3a201340-e176-a59a-19f2-f6b006548ec3",
  "eventValue": 200000000,
  "description": "",
  "source": "vac",
  "transactionType": "RETURN"
}
```

---

## 📁 Voucher & Wallet

### GET — get voucher by series-ids
```
GET http://ci-voucher-core.lc.frt.local/api/v2/voucher/series-ids?seriesIds=cd3ab1b1-041e-b980-1149-3a1eecb302bd&voucherType=2
```
Headers:
```
accept: text/plain
```

### PUT — update time 24 USED VC
```
PUT http://uat-wallet-core.lc.frt.local/api/v1/wallet/updated-creationTime-by-voucherid
```
Headers:
```
accept: text/plain
Content-Type: application/json
X-Requested-With: XMLHttpRequest
```
Body:
```json
{
  "time": "2026-04-08",
  "voucherIds": [
    "34ef9c54-d480-4182-af73-7941b3e43b5d"
  ]
}
```

---

## 📁 Fsell

### PUT — update ngày hết hạn fsell
```
PUT http://ci-fpt-loyalty-consumer-api.frt.local/api/common/update-transaction-expire-time
```
Headers:
```
accept: /
Content-Type: application/json
X-Requested-With: XMLHttpRequest
```
Body:
```json
{
  "transactionIds": [
    "53d7c233-13b9-4478-9dd9-02a4b18ae558",
    "a9495750-a156-49b5-bee5-0d04daedbf3b",
    "970dca3a-8551-460e-adfb-286a85bbc85d",
    "e43b55f9-dca1-4465-8e7b-38bb594c1b2e"
  ],
  "expireTime": "2026-03-31"
}
```

---

## 📁 PIM — Sản phẩm

### POST — lấy sản phẩm thuộc tpcp
```
POST http://uat-pim-search-service.lc.frt.local/api/products/ecom/product/search/cate
```
Headers:
```
accept: text/plain
Content-Type: application/json
```
Body:
```json
{
  "skipCount": 0,
  "maxResultCount": 10,
  "sortType": 0,
  "category": ["thuc-pham-chuc-nang"]
}
```
