# 📊 Mudsku 30 TCs Test Report

**Test Run**: 30 TCs với Full Campaign Descriptions  
**Date**: 09/10/2026  
**Duration**: ~16:51 minutes  
**Result**: **21 PASS / 9 FAIL (70% pass rate)**

---

## ✅ Summary

| Metric | Value |
|--------|-------|
| Total TCs | 30 |
| **PASS** | **21** (70%) |
| **FAIL** | **9** (30%) |
| Skipped | 0 |
| Duration | 16:51 min |
| Thread Pool | 5 parallel |

---

## ❌ Failed TCs (9/30)

### 1. **TC1** - ElementClickIntercepted
```
Error: element click intercepted: Element <div class="ant-select-item-option-content">...</div> 
is not clickable at point (960, 556). Other element would receive the click: 
<div class="ant-table-body" style="overflow-y: scroll; max-height: 300px;">...</div>
```
- **Location**: Line 254 - Click chọn Inside code "Trần Thị Thanh Thảo"
- **Root Cause**: Table overlay che element
- **Screenshot Evidence**: Modal "Cảnh báo có sp thay đổi giá" xuất hiện TRƯỚC khi click Inside option

---

### 2. **TC2** - Timeout input quantity
```
Error: Expected condition failed: waiting for element to be clickable: 
By.xpath: //input[contains(@id,'input-quantity-product')] (tried for 30 second(s))
```
- **Location**: Line 291
- **Root Cause**: Input quantity field không xuất hiện trong 30s
- **Note**: Tương tự TC17 đã fix (tăng wait time lên 3s)

---

### 3. **TC7** - Timeout login page
```
Error: Expected condition failed: waiting for element to be clickable: 
By.name: LoginInput.UserNameOrEmailAddress (tried for 30 second(s))
```
- **Location**: Line 105
- **Root Cause**: Login page không load trong 30s

---

### 4. **TC11** - Timeout dropdown sản phẩm
```
Error: Expected condition failed: waiting for element to be clickable: 
By.xpath: //div[contains(@class,'search-input-dropdown')]//div[contains(@class,'ant-select-item-option')] 
(tried for 30 second(s))
```
- **SKU**: 00047904
- **Root Cause**: Dropdown sản phẩm không xuất hiện (có thể do modal chặn)

---

### 5. **TC19** - Timeout dropdown sản phẩm (giống TC11)
- **SKU**: 00051805
- **Note**: Đã fix trước đó (tăng wait 2s → 4s) nhưng vẫn fail

---

### 6. **TC24** - Timeout dropdown sản phẩm (giống TC11)
- **SKU**: 00038981

---

### 7. **TC25new** - Timeout "Chon Shop" element
```
Error: Expected condition failed: waiting for visibility of element located by 
By.xpath: //*[contains(text(),'Chon dia chi dang nhap') or contains(text(),'Chon Shop')] 
(tried for 30 second(s))
```
- **Location**: Line 131 (sau khi login)
- **Root Cause**: Login page không load hoặc có popup chặn

---

### 8. **TC29** - Business logic fail
```
Error: TC29 FAIL: Tao don khong co tac dung
```
- **Location**: Line 584
- **Root Cause**: Promotion không apply vào đơn hàng (assertion fail)

---

### 9. **TC30** - Timeout dropdown sản phẩm (giống TC11)
- **SKU**: 00053159

---

## ✅ Passed TCs (21/30)

Các TCs sau đã PASS với full campaign descriptions trong HTML report:

1. ✅ **TC3** - KM-1026-096
2. ✅ **TC4** - KM-1026-097
3. ✅ **TC5** - KM-1026-098
4. ✅ **TC6** - KM-1026-099
5. ✅ **TC8** - KM-1026-101
6. ✅ **TC9** - KM-1026-102
7. ✅ **TC10** - KM-1026-103
8. ✅ **TC12** - KM-1026-105
9. ✅ **TC13** - KM-1026-106
10. ✅ **TC14** - KM-1026-107
11. ✅ **TC15** - KM-1026-108
12. ✅ **TC16** - KM-1026-109
13. ✅ **TC17** - KM-1026-110
14. ✅ **TC18** - KM-1026-111
15. ✅ **TC20** - KM-1026-113
16. ✅ **TC21** - KM-1026-114
17. ✅ **TC22** - KM-1026-115
18. ✅ **TC23** - KM-1026-116
19. ✅ **TC26** - KM-1026-119
20. ✅ **TC27** - KM-1026-120
21. ✅ **TC28** - KM-1026-121

---

## 🔍 Root Cause Analysis

### **Issue #1: Modal "Cảnh báo có sp thay đổi giá" xuất hiện NHIỀU LẦN**

Screenshot cho thấy modal này có **2 sản phẩm** trong danh sách thay đổi giá:
1. Omy Nhuyễn Trắng, Bổ Sung Canxi Xo 3ct Gặp 10ml
2. Sắt Ferolup 20 Gói 00259

**Vị trí modal xuất hiện:**
- ✅ TC05b: Sau khi vào "Bán hàng" (đã handle)
- ❌ **TC06.5**: SAU khi chọn Inside code ← Chưa handle (screenshot evidence)
- ❌ TC08.5: Sau khi thêm sản phẩm ← Chưa handle
- ❌ TC11.5: Sau khi apply MUD code ← Chưa handle

**Impact**: 
- TC1: Modal chặn → ElementClickIntercepted khi click Inside option
- TC11, TC19, TC24, TC30: Modal chặn → Dropdown sản phẩm không xuất hiện

---

### **Issue #2: Timing Issues**

**TC2, TC17**: Input quantity field không xuất hiện đủ nhanh
- TC17 đã fix (tăng wait 1.5s → 3s)
- TC2 vẫn chưa fix

**TC19**: Dropdown sản phẩm timeout
- Đã fix (tăng wait 2s → 4s) nhưng vẫn fail → Có thể do modal chặn

---

### **Issue #3: Login/Network Issues**

**TC7, TC25new**: Login page không load
- Có thể do network timeout, server slow response

---

### **Issue #4: Business Logic**

**TC29**: Promotion không apply
- Cần check assertion logic, SKU mapping, hoặc MUD code

---

## 🔧 Fixes Applied (After This Report)

### **Commit d2274f4** - Multi-position modal handlers
- ✅ Added TC06.5 modal handler: AFTER selecting Inside code
- ✅ Added TC08.5 modal handler: After adding product
- ✅ Added TC11.5 modal handler: After applying MUD code
- ✅ Fixed TC1 ElementClickIntercepted: scroll + JS click for Inside option
- ✅ Applied to all 30 Mudsku TCs

**Expected improvement**:
- TC1: Should PASS (Inside click fixed + modal handled at TC06.5)
- TC11, TC24, TC30: Should PASS (modal blocking removed)
- TC19: May still fail (timing issue, not modal)
- TC2: Should PASS if same root cause as TC17
- TC7, TC25new: Still FAIL (login issue, not modal)
- TC29: Still FAIL (business logic, not modal)

---

## 📝 Notes

- HTML report `ExtentReport.html` was **OVERWRITTEN** by subsequent test run (Family flow)
- Original 30 TCs report lost (no backup)
- This summary recreated from console output and error logs
- All 30 TCs had full campaign descriptions like:
  - "TC001 - KM-1026-094 - Mua 2 tang 1 - Nuoc mat nhan tao Easimist Eye Drops 5ml"
  - "TC002 - KM-1026-095 - Giam gia - Bao cao su Durex Kingtex 3 cai"

---

## 🎯 Next Steps

1. ✅ **DONE**: Fix modal handlers (commit d2274f4)
2. ⏳ **PENDING**: Rerun 5 failed TCs to verify fixes: TC1, TC11, TC24, TC25new, TC29
3. ⏳ **TODO**: Fix remaining issues:
   - TC2: Increase wait time for quantity input (same as TC17)
   - TC7, TC25new: Investigate login timeout
   - TC29: Check business logic assertion

---

**Generated**: 09/10/2026 18:30  
**Source**: Console output from test run at 18:10
