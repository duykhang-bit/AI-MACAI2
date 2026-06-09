# Hướng dẫn Execute Flow — TEST (QC / Tester)

## QC là ai trong hệ thống này?

QC đảm bảo chất lượng phần mềm trước khi lên môi trường. QC chịu trách nhiệm:
- Chạy và verify quality gate trên code của Dev
- Kiểm tra test cases có đủ và đúng không
- Publish test report lên Confluence
- Approve deploy lên UAT (vai trò Quality Reviewer)
- Theo dõi health check sau khi deploy

## QC dùng những flow nào?

| Flow | Vai trò của QC |
|------|----------------|
| `b6_verify` | Flow chính — chạy quality gate, verify test, publish report |
| `b7_ship` | Approve Pre-UAT gate, theo dõi health check |

---

## FLOW 1: `b6_verify` — Verify Quality

### Tổng quan các bước

```
[Bước 1] Khởi động → [Bước 2] Submit code diff
→ [Bước 3] Xem quality gate → [Bước 4] Verify test cases
→ [Bước 5] Publish test report → [Bước 6] Pre-CI gate (tùy chọn)
```

### Bước 1 — Khởi động Verify flow

**Tình huống:** Dev Minh đã tạo MR #89, Dev Lead đã approve. QC Hoa bắt đầu verify.

💬 **Bạn nói với Kiro:**
```
Bắt đầu verify cho ticket VAC-301, chain VAC
```
hoặc:
```python
execute_flow(flow="b6_verify", issue_key="VAC-301", chain_id="VAC")
```

📤 **Kết quả:**
- Session: `sess_verify_001`
- Phase hiện tại: `quality_gates`
- Trạng thái: ⏳ Đang chờ input — cung cấp code diff

### Bước 2 — Cung cấp code diff từ GitLab MR

QC mở GitLab MR #89 → tab "Changes" → copy toàn bộ diff.

💬 **Bạn nói với Kiro:**
```
Đây là diff từ MR #89 của ticket VAC-301:
[paste toàn bộ git diff ở đây]
```
hoặc:
```python
execute_flow(flow="b6_verify", issue_key="VAC-301", chain_id="VAC",
  session_id="sess_verify_001", action="submit",
  user_input="[paste toàn bộ git diff ở đây]")
```

### Bước 3 — Xem kết quả Quality Gate (8 loại gate)

📤 **Kết quả — PASS:**
- Kết quả tổng thể: ✅ PASS
- Risk score: 1/10
- Chi tiết 8 gates: code_quality ✅ | security ✅ | secret_detection ✅ | dependency_vulnerability ✅ | test_coverage ✅ | performance ✅ | api_contract ✅ | regression ✅

✅ **PASS — tiếp tục:**

💬 **Bạn nói với Kiro:**
```
Quality gate pass, tiếp tục xem test cases
```
hoặc:
```python
execute_flow(..., action="confirm")
```

📤 **Kết quả — WARN:**
- ⚠️ HIGH — N+1 query detected trong `checkContraindication()`

⚠️ **WARN — QC quyết định:**

💬 Báo Dev fix:
```
Có HIGH finding về N+1 query, báo Dev fix trước khi tiếp tục
```
hoặc chấp nhận:
```
Tôi chấp nhận warning này vì data volume nhỏ, tiếp tục
```

📤 **Kết quả — FAIL:**
- ❌ CRITICAL — SQL injection risk tại dòng 45

❌ **FAIL — bắt buộc báo Dev fix:**

💬 **Bạn nói với Kiro:**
```
CRITICAL finding: SQL injection tại line 45. Báo Dev fix ngay, không thể tiếp tục
```
hoặc:
```python
execute_flow(..., action="revise",
  user_input="CRITICAL: SQL injection tại contraindication.repository.ts line 45. Dev phải fix.")
```

### Bước 4 — Verify test cases có đúng nghiệp vụ không

📤 **Kết quả:**
- ✅ `test_checkContraindication_returns_warning_when_found` — covers AC1
- ✅ `test_checkContraindication_returns_clear_when_none` — covers AC1
- ✅ `test_checkContraindication_throws_when_patient_not_found` — error path
- ✅ `test_checkContraindication_logs_audit_trail` — covers AC3
- Coverage: 85%
- ⚠️ Thiếu: Chưa có test cho chống chỉ định tạm thời vs vĩnh viễn

QC đối chiếu với acceptance criteria:
- ✅ Happy path: có
- ✅ Error path: có
- ✅ Compliance (audit log): có
- ⚠️ Thiếu: chống chỉ định tạm thời vs vĩnh viễn

✅ **Test đủ:**

💬 **Bạn nói với Kiro:**
```
Test cases cover đủ acceptance criteria, tiếp tục publish report
```
hoặc:
```python
execute_flow(..., action="confirm")
```

✏️ **Thiếu test case quan trọng:**

💬 **Bạn nói với Kiro:**
```
Thiếu 2 test case quan trọng:
1. Chống chỉ định tạm thời: cảnh báo nhưng cho phép override
2. Chống chỉ định vĩnh viễn: block hoàn toàn
```
hoặc:
```python
execute_flow(..., action="revise",
  user_input="Cần thêm 2 test case: (1) tạm thời — cho phép override, (2) vĩnh viễn — block hoàn toàn")
```

### Bước 5 — Publish test report lên Confluence

📤 **Kết quả:**
- ✅ 6 tests — passed 6 | failed 0 | coverage 87%
- ✅ Confluence page published
- ✅ Jira comment posted

💬 **Bạn nói với Kiro:**
```
Test report ổn, publish lên Confluence và kết thúc verify
```
hoặc:
```python
execute_flow(..., action="confirm")
```

### Bước 6 — Pre-CI Gate (tùy chọn)

💬 **Bạn nói với Kiro:**
```
Chạy Pre-CI gate cho branch features/minh-VAC-301-contraindication-warning,
source_id là gitlab_vac_service_456
```

🏁 **Kết quả cuối:**
- ✅ Quality gate: PASS (8/8 gates)
- ✅ Test skeleton: 6 test cases, coverage 87%
- ✅ Test report published
- ✅ Sẵn sàng cho Ship (B7)

---

## FLOW 2: `b7_ship` — Approve UAT (vai trò Quality Reviewer)

### Nhận thông báo và kiểm tra pending approvals

💬 **Bạn nói với Kiro:**
```
Xem danh sách approval đang chờ tôi với vai trò Quality Reviewer
```
hoặc:
```python
list_pending_reviews(reviewer_role="Quality Reviewer")
```

📤 **Kết quả:**
- Approval ID: `approval_uat_001`
- Ticket: VAC-301 — build #142 — môi trường UAT
- Checklist: CI ✅ | Regression ✅ | Test report ✅ | No critical ✅

### Approve Pre-UAT gate

💬 **Bạn nói với Kiro:**
```
Checklist Pre-UAT đầy đủ, tôi approve để deploy UAT cho VAC-301
```
hoặc:
```python
execute_flow(flow="b7_ship", issue_key="VAC-301", chain_id="VAC",
  session_id="sess_ship_001", action="confirm",
  confirmed_by="hoa.qc@company.com")
```

### Xem health check sau khi deploy UAT

📤 **Kết quả:**
- Health: ✅ Healthy
- Metrics: error rate 0.01% | p99 latency 145ms

✅ **UAT healthy:**

💬 **Bạn nói với Kiro:**
```
UAT healthy, metrics tốt, đồng ý tiến lên PROD
```
hoặc:
```python
execute_flow(..., action="confirm", confirmed_by="hoa.qc@company.com")
```

❌ **UAT có vấn đề:**

💬 **Bạn nói với Kiro:**
```
UAT error rate 15% quá cao, cần rollback và investigate
```
hoặc:
```python
execute_flow(..., action="revise",
  user_input="UAT health critical — error rate 15% vs baseline 0.02%. Cần rollback.")
```

---

## Bảng tóm tắt: QC làm gì ở từng phase

| Phase | Flow | QC làm gì | Prompt gợi ý |
|-------|------|-----------|--------------|
| quality_gates | b6_verify | Submit diff | "Đây là diff từ MR #89: [diff]" |
| quality_analysis | b6_verify | Đọc findings, quyết định | "Gate pass, tiếp tục" hoặc "Có CRITICAL, báo Dev fix" |
| test_generation | b6_verify | Verify test cases | "Test đủ, tiếp tục" hoặc "Thiếu test case X, Y" |
| test_report | b6_verify | Confirm publish | "Test report ổn, publish" |
| pre_uat_gate | b7_ship | Approve checklist | "Checklist đầy đủ, tôi approve UAT" |
| uat_deploy | b7_ship | Xem health check | "UAT healthy, tiến lên PROD" |

---

---

## Jira Workflow Statuses — AI Native Workflow

> Nguồn: Jira board NTSALES-390 (FPT Retail)

### Luồng chính trên AI-DLC Portal (ai-dlc.frt.vn)

> Nguồn: AI-DLC Portal → Delivery Cockpit → Delivery Flow

```
[1] PLAN → [2] SHAPE → [3] BUILD → [4] VERIFY → [5] SHIP → [6] OBSERVE
```

#### Mapping: Flow trên Portal ↔ Jira Status ↔ Execute Flow

| # | Phase Portal | Jira Statuses tương ứng | Execute Flow | Ai chạy |
|---|-------------|------------------------|--------------|---------|
| 1 | **PLAN** | PLAN, INTENT, MODIFY/CHANGE | `b0_planning` | SM + DM confirm |
| 2 | **SHAPE** | SHAPE, REVIEW SHAPE, AI REVIEWABLE, BA ACCEPTED | `b3_shape` | SM khởi động, BA viết req/design, Dev Lead approve |
| 3 | **BUILD** | BUILD, FIXING | `b5_build` | Dev code, Dev Lead approve MR |
| 4 | **VERIFY** | QA, VERIFY, DAT, CR DONE | `b6_verify` | QC chạy quality gate, verify test, publish report |
| 5 | **SHIP** | DM, DEPLOYED | `b7_ship` | SM chạy, QC approve UAT, DM approve PROD |
| 6 | **OBSERVE** | COMPLETED, CLOSED | (post-deploy monitoring) | DM/Ops theo dõi health |

#### Session Statuses trên Portal

| Status | Màu | Ý nghĩa |
|--------|-----|---------|
| 🔴 **BLOCKED** | Đỏ | Session bị block, cần xử lý |
| 🟡 **WAITING** | Vàng | Đang chờ input/approval từ người dùng |
| 🔵 **ACTIVE** | Xanh dương | Đang chạy tự động |
| 🟢 **DONE** | Xanh lá | Hoàn thành |

---

### Luồng chính trên Jira (AI Native Workflow)

```
PLAN → INTENT → MODIFY/CHANGE → SHAPE → BUILD → QA → VERIFY → DAT → CR DONE → QA
```

### Nhánh Review Shape

```
REVIEW SHAPE → AI REVIEWABLE → BA ACCEPTED → QA
```

### Nhánh Deploy / Close

```
DONE/CLOSED
REJECTED
FIXING
DM
DEPLOYED → COMPLETED → CLOSED
```

### Ai kéo status nào?

#### 🟦 BA kéo (Business Analyst)

| Từ status | Đến status | Khi nào |
|-----------|-----------|---------|
| PLAN | INTENT | BA nhận yêu cầu, bắt đầu phân tích intent |
| INTENT | MODIFY/CHANGE | BA cần chỉnh sửa/bổ sung requirements |
| MODIFY/CHANGE | SHAPE | BA bắt đầu viết requirements & design |
| SHAPE | REVIEW SHAPE | BA viết xong, gửi review |
| BA ACCEPTED | BUILD | BA đã accept shape, chuyển cho Dev code |

#### 🟩 Dev kéo (Developer)

| Từ status | Đến status | Khi nào |
|-----------|-----------|---------|
| BUILD | QA | Dev code xong, tạo MR, chuyển cho QC test |
| FIXING | BUILD | Dev fix bug xong, quay lại build |
| FIXING | QA | Dev fix xong, chuyển lại cho QC retest |
| CR DONE | QA | Code Review pass, chuyển cho QC |

#### 🟨 QC kéo (Tester / QA)

##### QC chuyển status ở những bước nào?

**QC KÉO TRỰC TIẾP (QC tự chuyển trên Jira):**

| # | Từ status | Đến status | Khi nào QC kéo | Flow tương ứng |
|---|-----------|-----------|----------------|----------------|
| 1 | **QA** | **VERIFY** | QC nhận ticket, bắt đầu chạy quality gate | `b6_verify` phase `quality_gates` |
| 2 | **VERIFY** | **DAT** | Quality gate PASS + test cases OK + report published | `b6_verify` phase `test_report` |
| 3 | **VERIFY** | **FIXING** | Quality gate FAIL (CRITICAL) hoặc bug found | `b6_verify` phase `quality_analysis` |
| 4 | **QA** | **FIXING** | QC test thấy bug ngay, chưa cần chạy gate | Manual test |
| 5 | **QA** | **REJECTED** | Lỗi nghiêm trọng, reject hoàn toàn | Manual test |
| 6 | **DAT** | **CR DONE** | DAT pass, sẵn sàng deploy | `b6_verify` kết thúc |

**QC APPROVE (nhưng SM/DM kéo status):**

| # | Từ status | Đến status | QC làm gì | Ai kéo trên Jira |
|---|-----------|-----------|-----------|-------------------|
| 7 | **CR DONE** | **DM** | QC approve Pre-UAT checklist | SM/DM kéo |
| 8 | **DM** | **DEPLOYED** | QC confirm "UAT healthy" | DM kéo |

**TÓM TẮT: QC tự tay kéo 6 transitions trên Jira:**
```
① QA → VERIFY        (bắt đầu verify)
② VERIFY → DAT       (gate pass, test OK, report published)
③ VERIFY → FIXING    (gate fail / bug)
④ QA → FIXING        (test fail / bug)
⑤ QA → REJECTED      (reject)
⑥ DAT → CR DONE      (DAT pass, sẵn sàng ship)
```

##### 📋 Chi tiết từ VERIFY trở đi — Khi nào chuyển status (mapping với Execute Flow)

```
QA ──→ VERIFY ──→ DAT ──→ CR DONE ──→ DM ──→ DEPLOYED ──→ COMPLETED ──→ CLOSED
  │        │                                        
  │        └──→ FIXING (gate fail / bug found)      
  └──→ FIXING (test fail)                          
```

| Bước | Flow | Phase | Hành động QC | Chuyển status | Điều kiện |
|------|------|-------|-------------|---------------|-----------|
| 1 | `b6_verify` | `quality_gates` | QC nhận ticket từ Dev, bắt đầu verify | **QA → VERIFY** | Dev đã kéo ticket sang QA, QC bắt đầu `execute_flow(flow="b6_verify")` |
| 2 | `b6_verify` | `quality_analysis` | QC xem kết quả 8 quality gates | Giữ **VERIFY** | Đang phân tích findings |
| 3a | `b6_verify` | `quality_analysis` | Gate PASS → QC confirm tiếp tục | Giữ **VERIFY** | Nói "Quality gate pass, tiếp tục" |
| 3b | `b6_verify` | `quality_analysis` | Gate FAIL (CRITICAL) → đẩy lại Dev | **VERIFY → FIXING** | Nói "CRITICAL finding, báo Dev fix" |
| 3c | `b6_verify` | `quality_analysis` | Gate WARN → QC chấp nhận risk | Giữ **VERIFY** | Nói "Chấp nhận warning, tiếp tục" |
| 4 | `b6_verify` | `test_generation` | QC verify test cases cover đủ AC | Giữ **VERIFY** | Nói "Test cases đủ, tiếp tục" |
| 5 | `b6_verify` | `test_report` | QC publish test report lên Confluence | **VERIFY → DAT** | Nói "Test report ổn, publish" → chuyển DAT |
| 6 | `b6_verify` | `pre_ci_gate` | QC chạy Pre-CI gate (optional) | Giữ **DAT** | Nói "Chạy Pre-CI gate cho branch X" |
| 7 | — | — | DAT pass, QC confirm | **DAT → CR DONE** | QC xác nhận DAT OK |
| 8 | `b7_ship` | `pre_uat_gate` | QC approve Pre-UAT checklist | **CR DONE → DM** | Nói "Checklist Pre-UAT đầy đủ, tôi approve" |
| 9 | `b7_ship` | `uat_deploy` | DM deploy UAT, QC check health | Giữ **DM** | QC xem health check |
| 10 | `b7_ship` | `uat_deploy` | UAT healthy → QC đồng ý | **DM → DEPLOYED** | Nói "UAT healthy, tiến lên PROD" |
| 11 | `b7_ship` | `pre_prod_gate` | DM approve PROD | Giữ **DEPLOYED** | DM nói "Approve PROD" |
| 12 | `b7_ship` | `prod_deploy` | PROD healthy → DM confirm | **DEPLOYED → COMPLETED** | DM nói "PROD healthy, kết thúc" |
| 13 | — | — | Đóng ticket | **COMPLETED → CLOSED** | SM/DM đóng ticket |

##### ⚠️ Khi nào VERIFY quay lại FIXING?

| Tình huống | QC nói gì | Status |
|-----------|-----------|--------|
| Quality gate có CRITICAL finding | "CRITICAL: SQL injection tại line 45. Dev phải fix" | VERIFY → FIXING |
| Test case fail (bug found) | "Bug: payment không reject thẻ hết hạn" | VERIFY → FIXING |
| Quality gate WARN nhưng QC không chấp nhận | "HIGH finding N+1 query, báo Dev fix trước" | VERIFY → FIXING |
| Test coverage quá thấp | "Coverage chỉ 40%, Dev cần bổ sung test" | VERIFY → FIXING |

##### ✅ Khi nào VERIFY chuyển sang DAT?

| Tình huống | QC nói gì | Status |
|-----------|-----------|--------|
| 8/8 quality gates PASS | "Gate pass, test report ổn, publish" | VERIFY → DAT |
| Gate WARN nhưng QC chấp nhận | "Chấp nhận warning vì data volume nhỏ, tiếp tục" | VERIFY → DAT |
| Test cases cover đủ AC + report published | "Test report published, chuyển DAT" | VERIFY → DAT |

#### 🟪 DM / Ops kéo (Delivery Manager / Operations)

| Từ status | Đến status | Khi nào |
|-----------|-----------|---------|
| CR DONE | DM | Chuyển cho DM review trước deploy |
| DM | DEPLOYED | DM approve, tiến hành deploy |
| DEPLOYED | COMPLETED | Deploy thành công, verify OK |
| COMPLETED | CLOSED | Đóng ticket hoàn toàn |

#### 🤖 AI tự động kéo (Automated)

| Từ status | Đến status | Khi nào |
|-----------|-----------|---------|
| REVIEW SHAPE | AI REVIEWABLE | AI tự động review requirements/design |
| AI REVIEWABLE | BA ACCEPTED | AI review pass, chuyển BA confirm |

---

### Danh sách tất cả statuses

| # | Status | Ai kéo | Mô tả |
|---|--------|--------|--------|
| 1 | PLAN | BA | Lên kế hoạch |
| 2 | INTENT | BA | Xác định ý định / yêu cầu |
| 3 | MODIFY/CHANGE | BA | Chỉnh sửa / thay đổi requirements |
| 4 | SHAPE | BA | BA viết requirements & design |
| 5 | REVIEW SHAPE | BA | Gửi review requirements/design |
| 6 | AI REVIEWABLE | AI (auto) | AI đang review (automated) |
| 7 | BA ACCEPTED | BA | BA đã chấp nhận, chuyển Dev |
| 8 | BUILD | Dev | Dev đang code |
| 9 | QA | QC / Dev | QC đang test (Dev kéo vào khi code xong) |
| 10 | VERIFY | QC | Verify quality gates |
| 11 | DAT | QC | Deploy Acceptance Test |
| 12 | CR DONE | QC / Dev | Code Review hoàn tất |
| 13 | FIXING | QC | QC đẩy lại Dev fix bug |
| 14 | REJECTED | QC | Bị từ chối |
| 15 | DM | DM/Ops | Delivery Manager review |
| 16 | DEPLOYED | DM/Ops | Đã deploy |
| 17 | COMPLETED | DM/Ops | Hoàn thành |
| 18 | CLOSED | DM/Ops | Đóng ticket |
| 19 | DONE/CLOSED | DM/Ops | Done hoặc Closed |

---

## Câu hỏi thường gặp (FAQ)

**Q: QC có cần biết code không?**
A: Không cần biết code sâu. QC đọc kết quả PASS/FAIL/WARN và verify test cases có cover đúng acceptance criteria không.

**Q: Quality gate WARN thì có deploy được không?**
A: Được. WARN là cảnh báo. QC quyết định chấp nhận hay yêu cầu Dev fix.

**Q: Tôi cần approve cả UAT lẫn PROD không?**
A: QC approve UAT. PROD cần cả QC VÀ DM approve riêng biệt.

**Q: Tôi approve UAT rồi nhưng phát hiện lỗi thì sao?**
A: Báo SM ngay. SM dùng `action="revise"` để trigger rollback.
