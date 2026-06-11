# Agent Skill: AI Project Report Manager

**Mục đích**: Tự động hóa quy trình report dự án AI trên Confluence — từ setup page chính, tạo daily progress, đến xuất AI report cuối dự án.

**Trigger prompt**:
- SM: **"Setup AI report"** / **"Xuất AI report"** / **"SM update"** / **"Sprint review AI"**
- Member: **"Daily update"** ← không cần khai mã CR, AI tự detect tất cả CR trong session
- (Biến thể: "tạo page AI report", "update daily progress", "tổng hợp AI report")

**Áp dụng cho**: SM (Scrum Master), toàn bộ team members (BA, Dev, Tester, TechLead)

> 💡 **Nguyên tắc thiết kế**: Member làm ít nhất có thể — chỉ gõ `"Daily update"`, AI tự làm phần còn lại. SM là người gõ các lệnh tổng hợp.

---

## 🎯 Workflow Tổng Quan

```
[BƯỚC 1] SM setup page chính
         ↓
[BƯỚC 2] Team members update daily (mỗi ngày)
         → AI tự tạo sub-page nếu chưa có
         → AI tổng hợp effort qua Hybrid Time Tracking
           (Jira transition + session + manual input)
         ↓
[BƯỚC 2.5] SM cập nhật page chính cuối ngày
         → Đọc daily progress + Jira epic
         → Update SM Dashboard + Daily Progress Log
         ↓
[BƯỚC 3] Sprint Review (cuối mỗi sprint)
         ↓
[BƯỚC 4] SM xuất AI report cuối dự án
```

---

## 📋 BƯỚC 1: SM Setup Page Chính

### Trigger

```
User (SM): "Setup AI report cho CR [mã CR]"
```

### Input cần thiết

| Thông tin | Bắt buộc | Ví dụ |
|-----------|----------|-------|
| Mã CR | ✅ | `LCR25-1084` |
| Tên CR | ✅ | `Tính năng tạo QR code trên màn hình thanh toán` |
| DM/SM phụ trách | ✅ | `ToanLBK` |
| Thời gian triển khai | ✅ | `01/04/2026 → 30/04/2026` |
| Loại dự án | ✅ | `New build / Enhancement / Integration / Data / AI feature` |
| Confluence parent page | ✅ **Cố định** | `271195648` — 1. Nhà thuốc Report (không cần khai) |

### Quy trình xử lý

1. **Hỏi thông tin thiếu** (nếu user chỉ gõ "Setup AI report"):

```
📋 **Để setup AI Project Report, bạn cung cấp:**

① Mã CR *(Bắt buộc)*
   Ví dụ: `LCR25-1084`

② Tên CR *(Bắt buộc)*
   Ví dụ: "Tính năng tạo QR code trên màn hình thanh toán"

③ DM/SM phụ trách *(Bắt buộc)*
   Ví dụ: `ToanLBK`

④ Thời gian triển khai *(Bắt buộc)*
   Ví dụ: `01/04/2026 → 30/04/2026`

⑤ Loại dự án *(Bắt buộc)*
   Chọn: New build / Enhancement / Integration / Data / AI feature

⑥ Confluence parent page *(Tùy chọn)*
   Page ID hoặc URL để tạo page con. Nếu không có, tôi sẽ hỏi.
```

2. **Tạo page chính** với cấu trúc chuẩn:

> 📌 **Quy tắc bắt buộc về vị trí và tên page:**
> - **Parent page cố định**: `271195648` — [1. Nhà thuốc Report](https://confluence.frt.vn/spaces/AIN/pages/271195648/1.+Nh%C3%A0+thu%E1%BB%91c+Report)
> - **Format tên page**: `[Mã CR] - [Tên SM] - [Tên CR/dự án]`
> - Ví dụ: `LCR25-1084 - ToanLBK - Tính năng tạo QR code trên màn hình thanh toán`
> - Không dùng `—` (em dash), dùng `-` (hyphen) để phân cách

```python
rde_publish_to_confluence(
    title="[Mã CR] - [Tên SM] - [Tên CR/dự án]",
    content=<template page chính — xem bên dưới>,
    space_key="AIN",
    parent_page_id="271195648"  # Cố định — 1. Nhà thuốc Report
)
```

### Template Page Chính

```markdown
# [Mã CR] - [Tên SM] - [Tên CR/dự án]

---

## 1. 🎯 Thông tin chung

| Field | Nội dung |
|-------|----------|
| Chuỗi/ Team | Nhà Thuốc (NT) / [Tên team] |
| Dự án/ CR | [Mã CR] — [Tên CR] |
| DM/ SM phụ trách | [Tên DM/SM] |
| Thời gian triển khai | [Ngày bắt đầu] → [Ngày golive] |
| Loại dự án | [New build / Enhancement / Integration / Data / AI feature] |

---

## 2. 🧠 Mục tiêu áp dụng AI-DLC

**Pain point trước khi áp dụng:**
_(SM điền sau — mô tả vấn đề trước khi dùng AI)_

**Kỳ vọng cải thiện:**
_(SM điền sau — mô tả kỳ vọng)_

**Scope áp dụng:**
_(SM điền sau — phase/feature được áp dụng AI)_

---

## 3. 🔁 AI-DLC Workflow đã áp dụng

### 3.1 Tổng quan flow
- Các phase đã dùng: _(SM điền sau)_
- Khác gì so với chuẩn: _(SM điền sau)_
- Lý do thay đổi: _(SM điền sau)_

### 3.2 Chi tiết từng bước

> Chi tiết từng bước được ghi nhận theo ngày — bấm vào link để xem đầy đủ.

| Ngày | Người thực hiện | Epic Status | Tóm tắt | Chi tiết |
|------|----------------|-------------|---------|---------|
| _(Sẽ được update daily)_ | | | | |

---

## 4. ⚠️ Vấn đề gặp phải

| Loại | Mô tả |
|------|-------|
| Prompt chưa hiệu quả | _(Điền khi gặp)_ |
| AI hallucination | _(Điền khi gặp)_ |
| Team resistance | _(Điền khi gặp)_ |
| Tool limitation | _(Điền khi gặp)_ |

---

## 5. 💡 Đề xuất cải tiến

| Area | Đề xuất |
|------|---------|
| Workflow | _(TechLead điền cuối dự án)_ |
| Prompt | _(TechLead điền cuối dự án)_ |
| Tool | _(TechLead điền cuối dự án)_ |
| Training | _(TechLead điền cuối dự án)_ |

---

## 6. 🔁 Playbook đề xuất

_(TechLead điền cuối dự án — step-by-step đề xuất để reuse cho CR tương tự)_

**Khả năng reuse:** Yes/No — _(Lý do)_

---

## 7. ⭐ Đánh giá maturity (self-assessment)

| Level | Mô tả | Chọn |
|-------|-------|------|
| Level 1 | Có dùng AI rời rạc | |
| Level 2 | Có workflow rõ ràng | |
| Level 3 | Có prompt chuẩn + reuse | |
| Level 4 | Có đo lường + optimize | |
| Level 5 | Scale được toàn program | |

_(TechLead điền cuối dự án)_

---

## 📊 SM Dashboard

> SM update hàng ngày — tổng hợp từ các daily progress

### Epic Status Flow

```
ANALYZING → IMPLEMENTING → TESTING → DONE
     ↓           ↓            ↓
  BLOCKED    BLOCKED      BLOCKED
```

**Điều kiện chuyển trạng thái:**
- `ANALYZING → IMPLEMENTING`: Requirements đã được approve
- `IMPLEMENTING → TESTING`: Code đã merge vào ci, build success
- `TESTING → DONE`: Test pass, deploy production success
- `* → BLOCKED`: Có blocker nghiêm trọng (P1 bug, dependency issue, resource issue)
- `BLOCKED → [previous state]`: Blocker đã được resolve

| Metric | Giá trị | Cập nhật |
|--------|---------|----------|
| Epic Status | [ANALYZING / IMPLEMENTING / TESTING / BLOCKED / DONE] | DD/MM/YYYY |
| Fix Version | [version] — còn X ngày | DD/MM/YYYY |
| Total Tasks | [X] tasks | DD/MM/YYYY |
| Completed | [Y] tasks ([Z]%) | DD/MM/YYYY |
| Bugs Open | P1:[X] P2:[Y] P3:[Z] | DD/MM/YYYY |
| Blocker hiện tại | [Danh sách blocker] | DD/MM/YYYY |
| Rủi ro | [Mô tả rủi ro nếu có] | DD/MM/YYYY |
| AI Adoption Rate | [X]% CR members dùng AI hôm nay ([N]/[Total CR members]) | DD/MM/YYYY |
| AI Time (hôm nay) | [X]h tổng toàn team | DD/MM/YYYY |
| Velocity Trend | [↑ tăng / → ổn định / ↓ giảm] so sprint trước | DD/MM/YYYY |

---

## 📅 Daily Progress Log

> Bấm vào link từng ngày để xem chi tiết. Không xóa nội dung ngày trước.

| Ngày | Người thực hiện | Epic Status | Tóm tắt | Chi tiết |
|------|----------------|-------------|---------|---------|
| _(Sẽ được update daily)_ | | | | |

---

---

## 🤖 AI Metadata

| Field | Value |
|-------|-------|
| Generated by | Kiro AI-Native |
| Username | [username] |
| Date | [DD/MM/YYYY] |
| Session type | setup-ai-report |
| chain_id | PHARMACY |
| CR | [Mã CR] |

*Generated by Kiro AI-Native | chain_id: PHARMACY | Space: AIN*
```

### Thông báo kết quả

```
✅ AI Project Report page đã được tạo!

📄 Tiêu đề: [Mã CR] — [Tên người] — CR [Tên CR]
🔗 Link: [URL Confluence page]
📅 Ngày tạo: [DD/MM/YYYY]

📋 Cấu trúc:
  • Mục 1-7: AI-DLC report (SM điền dần)
  • SM Dashboard: Tổng hợp metrics
  • Daily Progress Log: Link đến sub-pages

📌 Next steps:
  1. SM điền Mục 2 (Mục tiêu áp dụng AI-DLC)
  2. SM tạo template daily (gõ "Tạo template daily [mã CR]")
  3. Team members update daily (gõ "Daily update AI [mã CR]")
```

---

## 👥 BƯỚC 2: Team Members Update Daily

### Trigger

```
User (bất kỳ role): "Daily update"
```

> ⚠️ **Không cần khai mã CR.** AI tự detect tất cả CR member đã làm trong session, group theo CR, publish vào đúng sub-page của từng CR.
>
> ⚠️ **Không cần SM tạo template trước.** AI tự kiểm tra sub-page ngày hôm nay đã có chưa — nếu chưa thì tự tạo.

### Input cần thiết

| Thông tin | Bắt buộc | Ví dụ |
|-----------|----------|-------|
| Mã CR | ❌ **Không cần** | AI tự detect từ session — không cần khai |
| Username | ⚙️ **AI tự detect** | Từ session, branch, Jira — không cần khai |
| Task đã làm | ❌ **Không cần** | AI tổng hợp qua Hybrid Time Tracking |
| Jira ticket | ❌ **Không cần** | AI tự detect từ session + Jira |
| Thời gian task | ❌ **Không cần** | AI tính từ Jira transition hoặc session |
| Công việc ngoài AI | ⚪ **Tùy chọn** | Họp, review, giao tiếp — user nhập thêm nếu muốn |

> ⚠️ **Username được AI tự detect** — không cần khai trong prompt.
>
> **Thứ tự AI tự detect username:**
> 1. Tìm trong conversation history: branch name (`feature/FC-XXXX-HuyNT83-...`), commit message, Jira assignee
> 2. Đọc `AGENTS.md` → lấy danh sách team → hỏi user chọn (1 click)
> 3. Nếu vẫn không xác định được → hỏi user nhập tay

### Quy trình xử lý

#### STEP 1: Detect tất cả CR trong session

```
AI scan toàn bộ conversation history để tìm:

① Mã CR/ticket xuất hiện (pattern: LCR\d+-\d+, FC-\d+, NTSALES-\d+, v.v.)

② Resolve ticket → CR (BẮT BUỘC):
   - Nếu ticket là mã CR trực tiếp (LCR25-XXXX) → dùng luôn
   - Nếu ticket là Epic/Story (NTSALES-XXX, FC-XXXX):
     → Tìm CR tương ứng qua: epic_link, label, hoặc linked_issues
     → Cách tìm:
       a. Đọc ticket: jira_get_issue(issue_key="NTSALES-537")
       b. Kiểm tra field "Epic Link" → nếu epic có label LCR25-XXXX → đó là CR
       c. Kiểm tra labels của ticket → tìm pattern LCR25-XXXX
       d. Kiểm tra linked_issues → tìm link type "Cloners" hoặc "is caused by" tới LCR25-XXXX
       e. Nếu không tìm được → hỏi user: "Ticket NTSALES-537 thuộc CR nào?"
   
   ⚠️ KHÔNG TỰ Ý tạo page daily theo Epic/Story.
   Luôn resolve về mã CR (LCR25-XXXX) rồi mới tạo sub-page daily dưới page chính của CR đó.

③ Group tasks theo CR tương ứng:
   - Task liên quan NTSALES-537 → resolve → thuộc CR LCR25-1459
   - Task liên quan FC-52405 → resolve → thuộc CR LCR25-1084
   - Task không resolve được → hỏi user thuộc CR nào (hoặc "Khác")

④ Nếu không detect được CR nào → hỏi user:
   "Bạn đã làm CR nào hôm nay? (ví dụ: LCR25-1084)"
```

> ⚠️ **QUY TẮC QUAN TRỌNG — Resolve Epic/Story → CR**
>
> Member thường prompt theo Epic (NTSALES-390) hoặc Story (NTSALES-537) được gán cho CR.
> AI **PHẢI** resolve về mã CR (LCR25-XXXX) trước khi tạo page daily.
> **KHÔNG BAO GIỜ** tạo page daily theo Epic/Story — luôn tạo dưới page chính của CR.
>
> **Ví dụ**:
> - Member prompt: "Daily update NTSALES-537"
>   → AI đọc NTSALES-537 → tìm epic_link hoặc label → phát hiện thuộc LCR25-1459
>   → Tạo sub-page daily dưới page chính LCR25-1459 (KHÔNG tạo page "Daily — NTSALES-537")
>
> - Member prompt: "Daily update NTSALES-390"
>   → AI đọc NTSALES-390 → đây là Epic → tìm linked CR → LCR25-1462
>   → Tạo sub-page daily dưới page chính LCR25-1462

#### STEP 2: Tổng hợp effort qua Hybrid Time Tracking

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⏱️ HYBRID TIME TRACKING — Thứ tự ưu tiên
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Nguồn 1 — Jira Transition Timestamps (chính xác nhất, ưu tiên cao nhất)
  Jira ghi lại timestamp mỗi lần chuyển trạng thái ticket.
  Dùng để tính Total Effort thực tế của từng task.

  Công thức:
    Total Effort = timestamp(Done) - timestamp(In Progress)
                   - trừ weekend (thứ 7, chủ nhật)
                   - trừ giờ ngoài working hours (trước 8h30 và sau 18h)

  Mapping trạng thái:
    Open → In Progress       = bắt đầu làm
    In Progress → In Review  = xong code/test
    In Review → Testing      = dev xong, tester bắt đầu
    Testing → Done           = hoàn thành

  Cách lấy:
    jira_get_issue(issue_key="FC-XXXX")
    → changelog.histories → field: status → created timestamp

  Ưu điểm: Không phụ thuộc session, tự động, khách quan
  Dùng khi: Ticket có đủ transition history

Nguồn 2 — Session Logs (bổ sung, dùng khi Jira không đủ data)
  Tổng hợp từ conversation history trong session hiện tại.
  Chỉ tính AI time (thời gian AI thực sự xử lý) — không phải total effort.

  Định nghĩa AI time:
    = thời gian từ lúc nhận prompt đến lúc trả response
    KHÔNG tính: thời gian user chờ, user idle, user đi làm việc khác

  Cách tính:
    - Nhìn vào từng response trong conversation
    - Ước tính thời gian AI xử lý thực tế mỗi response
    - AI time của 1 task = tổng thời gian xử lý của tất cả responses thuộc task đó
    - Không dùng công thức cứng — ước tính dựa trên nội dung thực tế

  Dùng khi: Session còn active, ticket chưa có đủ transition history

Nguồn 3 — Manual Input (cho công việc ngoài AI)
  User tự khai báo thời gian cho các công việc không qua AI:
  họp, review, giao tiếp, đọc tài liệu, v.v.

  AI hỏi optional khi tổng hợp:
    "Ngoài AI session, bạn có thêm thời gian nào không?
     (họp, review, giao tiếp, v.v.)
     Ví dụ: '30p họp planning, 1h review spec với BA'
     [Nhập hoặc bỏ qua]"

  Dùng khi: Luôn hỏi — để capture effort thực tế ngoài AI

Nguồn 4 — Fallback: Ước tính theo loại task (khi không có 3 nguồn trên)

  | Loại task | AI time | Manual estimate |
  |-----------|---------|-----------------|
  | Tạo test plan (30 TC) | ~40p | ~3h |
  | Gen test case (10 TC) | ~15p | ~1h |
  | Log bug + phân tích | ~10p | ~30p |
  | Viết spec/requirements | ~30p | ~2h |
  | Setup Confluence page | ~10p | ~45p |
  | Review & cải tiến skill | ~45p | ~2h |
  | Tạo/sửa hook | ~20p | ~1h |
  | Publish lên Confluence | ~5p | ~20p |

  Nếu task không có trong bảng → ước tính Manual = AI time × 3 đến × 5
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

#### STEP 3: Hiển thị preview grouped theo CR

```
📋 Tổng hợp session của bạn — [DD/MM/YYYY]
👤 Người: [username]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📁 CR: LCR25-1084 — QR code thanh toán
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  1. ✅ Tạo test plan API QR code (FC-52405)
     AI time: ~40p | Total effort: ~2h (từ Jira transition)
  2. 🔄 Log bug API 500 (FC-52410)
     AI time: ~10p | Total effort: ~30p

  Subtotal: 2 tasks | AI time: ~50p | Total effort: ~2h 30p

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📁 CR: LCR25-1090 — Payment refund
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  1. ✅ Gen test case payment (FC-52407)
     AI time: ~15p | Total effort: ~1h (từ Jira transition)

  Subtotal: 1 task | AI time: ~15p | Total effort: ~1h

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⏱️ Công việc ngoài AI (tùy chọn):
  Bạn có thêm thời gian nào không? (họp, review, giao tiếp)
  Ví dụ: "30p họp planning" hoặc bỏ qua
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📊 Tổng ngày: 3 tasks | AI time: ~1h 5p | Total effort: ~3h 30p

Bạn muốn:
A) Publish vào cả 2 CR (mặc định)
B) Chỉnh sửa trước khi publish
```

#### STEP 4: Publish vào đúng sub-page của từng CR — APPEND-ONLY

```python
# Với MỖI CR được detect:
for cr in detected_crs:
    # 1. Tìm page chính của CR
    cr_main_page = search_context_hybrid(
        query=f"{cr} daily progress",
        chain_id="PHARMACY",
        space_key="AIN"
    )

    # 2. Tìm hoặc tạo sub-page daily của ngày hôm nay
    daily_page = find_or_create_daily_subpage(
        parent_page_id=cr_main_page.id,
        date=today,
        cr_code=cr,
        title_prefix="[AIN]"  # ← Prefix bắt buộc cho mọi page AI tạo
    )

    # 3. APPEND-ONLY — đọc trước, thêm dòng mới, publish lại
    existing = get_confluence_page(page_id=daily_page.id)
    new_rows = build_task_rows(tasks_for_cr=cr_tasks[cr], username=username)
    merged = existing["content"] + new_rows
    rde_publish_to_confluence(page_id=daily_page.id, content=merged, ...)
```

> **Kết quả**: 1 lệnh `"Daily update"` → AI tự split và ghi vào đúng sub-page của từng CR.
> Nhiều session trong ngày → mỗi session append thêm → không bị mất, không bị overwrite.

### Ví dụ prompt

```
Daily update
```

Chỉ vậy thôi. AI tự làm phần còn lại.

### Template Sub-page Daily

```markdown
# [AIN] 📆 DD/MM/YYYY — Daily Progress — [Mã CR]

> Hướng dẫn: Mỗi người thêm task của mình vào bảng. Không xóa dòng của người khác.

---

## 🧑‍💼 SM Summary

| Hạng mục | Nội dung |
|----------|----------|
| Epic Status | [ANALYZING / IMPLEMENTING / TESTING / DONE] |
| Fix Version | [version] — còn X ngày |
| Bugs mới | [số lượng + mức độ] |
| Blocker | [danh sách blocker] |
| Rủi ro | [mô tả rủi ro nếu có] |
| Action ngày mai | [action items] |

---

## 📋 Tasks trong ngày

> Mỗi người thêm dòng của mình. Cột Người để phân biệt.
> - **Thời gian AI**: thời gian AI thực sự xử lý (từ lúc nhận prompt đến lúc trả response, không tính thời gian user chờ/idle)
> - **Manual estimate**: ước tính thời gian nếu tự làm không dùng AI

| Người | Task | Jira | Thời gian AI | Manual estimate | Trạng thái | Ghi chú |
|-------|------|------|-------------|-----------------|-----------|---------|
| _(Thêm dòng của bạn)_ | | | | | | |

---

## ⏱️ Tổng hợp thời gian dùng AI

> Tự động tổng hợp từ bảng Tasks — SM không cần điền thủ công.

| Người | Tổng thời gian AI | Tổng manual estimate | Số tasks | Time saved |
|-------|------------------|---------------------|----------|------------|
| _(Tổng hợp cuối ngày)_ | | | | |

---

## 🐛 Bugs phát hiện hôm nay

| Bug | Jira | Severity | Priority | Assignee | Mô tả ngắn |
|-----|------|----------|----------|----------|------------|
| _(Không có bug mới — nếu không có)_ | | | | | |

---

## 📝 Notes / Cải tiến

_(Ghi chú tự do — cải tiến quy trình, phát hiện mới, v.v.)_

---

## ⚡ AI Friction hôm nay

> Ghi ngay khi gặp — SM tổng hợp vào Mục 4 page chính. Không cần điền nếu không có vấn đề.

| Loại | Mô tả ngắn | Người gặp |
|------|-----------|-----------|
| Prompt chưa hiệu quả | _(ví dụ: AI gen sai test case do AC mơ hồ)_ | |
| AI hallucination | _(ví dụ: AI tạo ticket với thông tin sai)_ | |
| Tool limitation | _(ví dụ: không search được page cũ hơn 30 ngày)_ | |
| Khác | | |

---

---

## 🤖 AI Metadata

| Field | Value |
|-------|-------|
| Generated by | Kiro AI-Native |
| Username | [username] |
| Date | [DD/MM/YYYY] |
| Session type | daily-update |
| chain_id | PHARMACY |
| CR | [Mã CR] |

*Generated by Kiro AI-Native | chain_id: PHARMACY*
```

### Thông báo kết quả

```
✅ Daily update đã được thêm vào!

📆 Ngày: DD/MM/YYYY
👤 Người: [username]
📋 Task: [Task đã làm]
🎫 Jira: [FC-XXXX]
⏱️ Thời gian: [~Xp]
✅ Trạng thái: [Done/WIP]

🔗 Link: [URL sub-page daily]
```

---

## � BƯỚC 2.5: SM Cập Nhật Page Chính Cuối Ngày

### Trigger

```
User (SM): "SM update [mã CR]"
```

> Thực hiện cuối mỗi ngày làm việc, sau khi team members đã update daily progress.

### Quy trình xử lý

1. **Đọc sub-page daily hôm nay** để lấy thông tin team đã làm:

```python
get_confluence_page(page_id="<sub_page_id_hôm_nay>")
```

2. **Đọc Jira epic** để lấy trạng thái mới nhất:

```python
jira_get_issue(issue_key="[mã CR]")
# Lấy: epic status, bugs open, fix version, blockers từ comments
```

2.5. **Xác định CR Members (Dynamic từ Jira Epic)**:

> ⚠️ **Quan trọng**: CR Members ≠ Team Members. Mỗi CR chỉ có 3-5 người làm, không phải toàn bộ 19+ người trong team.

```python
# Query tất cả sub-tasks của epic
sub_tasks = jira_search(
    jql=f'parent = {epic_key} OR "Epic Link" = {epic_key}',
    chain_id="PHARMACY",
    project_key="NTSALES",
    max_results=100
)

# Extract unique assignees = CR Members
cr_members = set()
for task in sub_tasks["results"]:
    if task["assignee"]:
        cr_members.add(task["assignee"])

# Thêm reporter của epic (SM) vào danh sách
epic = jira_get_issue(issue_key=epic_key)
if epic["reporter"]:
    cr_members.add(epic["reporter"])

# Cross-reference với AGENTS.md để lấy role, company
# → Dùng cho AI Adoption Rate và hiển thị trong SM Dashboard
```

**Quy tắc:**
- **CR Members** = unique(sub_task.assignee for all sub-tasks) + epic.reporter
- **AI Adoption Rate** = (CR members có daily update hôm nay) / (total CR members) × 100%
- Không dùng toàn bộ team trong AGENTS.md — chỉ lấy người thực sự có task trong epic
- Nếu có người mới được assign sub-task → tự động thêm vào CR members (dynamic)
- Hiển thị danh sách CR Members trong SM Dashboard để SM biết ai chưa update

**Ví dụ:**
```
Epic NTSALES-390 có 13 sub-tasks:
  - duypb2 (Design) — 3 tasks
  - Lê Bảo Khánh (FE) — 5 tasks
  - phuctt17 (BE) — 5 tasks
  + lanttp (Reporter/SM)

→ CR Members = 4 người (không phải 19 người toàn team)
→ AI Adoption Rate = (members có daily update) / 4 × 100%
```

3. **Tổng hợp và cập nhật SM Dashboard** trên page chính:
   - Epic Status: lấy từ Jira + **tự động transition nếu đủ điều kiện**
   - Total Tasks / Completed: đếm từ sub-page daily + lịch sử
   - Bugs Open: lấy từ Jira (P1/P2/P3)
   - Blocker: tổng hợp từ SM Summary trong sub-page daily
   - AI Adoption Rate: % **CR members** (từ Jira epic) có update trong ngày — KHÔNG dùng toàn team
   - AI Time: tổng từ bảng "Tổng hợp thời gian dùng AI" trong sub-page daily
   - Velocity Trend: so sánh AI time hôm nay vs hôm qua

3.5. **Kiểm tra và transition Epic Status** (nếu đủ điều kiện):

```python
# Kiểm tra điều kiện chuyển trạng thái
epic = jira_get_issue(issue_key="[mã CR]")
current_status = epic["status"]

# ANALYZING → IMPLEMENTING: khi có sub-task đầu tiên chuyển sang In Progress
if current_status == "ANALYZING":
    in_progress_tasks = jira_search(
        jql=f'parent = "{mã_CR}" AND status = "In Progress"',
        chain_id="PHARMACY"
    )
    if in_progress_tasks:
        jira_transition(issue_key="[mã CR]", transition_name="Start Implementation")

# IMPLEMENTING → TESTING: khi tất cả dev tasks Done, có CI build success
if current_status == "IMPLEMENTING":
    dev_tasks_done = check_all_dev_tasks_done(mã_CR)
    if dev_tasks_done:
        jira_transition(issue_key="[mã CR]", transition_name="Start Testing")

# Nếu có P1 bug open → transition sang BLOCKED
p1_bugs = jira_search(
    jql=f'parent = "{mã_CR}" AND issuetype = Bug AND priority = Highest AND status != Done',
    chain_id="PHARMACY"
)
if p1_bugs and current_status not in ["BLOCKED", "DONE"]:
    jira_transition(issue_key="[mã CR]", transition_name="Block")
    # Ghi chú lý do block
    jira_add_comment(
        issue_key="[mã CR]",
        comment_body=f"🚫 Epic bị BLOCKED do P1 bug: {[b['key'] for b in p1_bugs]}"
    )
```

4. **APPEND-ONLY trên page chính** — đọc `get_confluence_page` trước, giữ nguyên toàn bộ nội dung, chỉ cập nhật các phần sau:

   a. **SM Dashboard**: overwrite bảng metrics (chỉ SM được làm)

   b. **Mục 3.2 Chi tiết từng bước**: append 1 dòng mới:
   ```markdown
   | DD/MM/YYYY | [SM username] (SM) | [Tóm tắt các bước hôm nay] | [Link sub-page](URL) |
   ```

   c. **Daily Progress Log**: append 1 dòng mới:
   ```markdown
   | DD/MM/YYYY | [SM username] (SM) | [Epic Status] | [Tóm tắt 1 dòng] | [Link sub-page](URL) |
   ```

   d. **Mục 4 Vấn đề gặp phải**: append các AI Friction mới từ sub-page daily (nếu có, không duplicate với dòng đã có)

5. **Điền SM Summary** trong sub-page daily (nếu chưa điền):

```markdown
| Epic Status | [từ Jira] |
| Bugs mới | [từ Jira] |
| Blocker | [từ sub-page daily của team] |
| Action ngày mai | [SM quyết định] |
```

### Thông báo kết quả

```
✅ SM Dashboard đã được cập nhật!

📊 Summary DD/MM/YYYY — [Mã CR]:
  Epic Status  : [OLD] → [NEW] [nếu có transition]
  Completed    : [X]/[Y] tasks
  Bugs Open    : P1:[X] P2:[Y] P3:[Z]
  AI Adoption  : [X]% ([N]/[Total] members)
  AI Time      : [X]h tổng team hôm nay
  Velocity     : [↑/→/↓] so hôm qua
  Blocker      : [có/không]

[Nếu có epic transition]:
🔄 Epic đã chuyển sang [NEW_STATUS]
   Lý do: [Điều kiện đã đáp ứng]
   ⚠️ Action tiếp theo: [Gợi ý action cho SM]

🔗 Page chính: [URL]
📆 Daily: [URL sub-page]
```

---

### Trigger

```
User (SM): "Sprint review AI [mã CR]"
```

### Mục đích

Checkpoint giữa dự án — không cần đợi đến cuối mới review. Phù hợp cho dự án dài 1-2 tháng.

### Quy trình xử lý

1. **Đọc tất cả sub-pages daily** từ đầu sprint đến nay
2. **Tổng hợp nhanh**:
   - Số tasks hoàn thành vs kế hoạch
   - AI time tổng cộng toàn team trong sprint
   - Top 3 friction points từ bảng "AI Friction hôm nay"
   - Velocity so với sprint trước
3. **Tạo sub-page Sprint Review**:

```python
rde_publish_to_confluence(
    title="🔄 Sprint [X] Review — [Mã CR]",
    content=<template sprint review>,
    space_key="AIN",
    parent_page_id="<page_id chính>"
)
```

### Template Sprint Review

```markdown
# 🔄 Sprint [X] Review — [Mã CR]

## 📊 Kết quả Sprint

| Metric | Kế hoạch | Thực tế | Ghi chú |
|--------|----------|---------|---------|
| Tasks hoàn thành | [X] | [Y] | |
| AI time tổng | — | [X]h | |
| Bugs phát sinh | — | [X] | |
| AI Adoption Rate | ≥ 80% | [X]% | |

## ⚡ Top Friction Points

1. [Friction 1 — tổng hợp từ AI Friction daily]
2. [Friction 2]
3. [Friction 3]

## 💡 Điều chỉnh cho Sprint tiếp theo

- [Điều chỉnh 1]
- [Điều chỉnh 2]

*Generated by Kiro AI-Native | chain_id: PHARMACY*
```

---

## 📄 BƯỚC 4: SM Xuất AI Report Cuối Dự Án

### Trigger

```
User (SM): "Xuất AI report [mã CR]"
```

### Input cần thiết

| Thông tin | Bắt buộc | Ví dụ |
|-----------|----------|-------|
| Mã CR | ✅ | `LCR25-1084` |

### Quy trình xử lý

1. **Tìm và đọc page chính** của CR — dùng query kết hợp nhiều keywords:

```python
# Bước 1: Query kết hợp (ưu tiên)
search_context_hybrid(
    query="[mã CR] [tên tính năng] [tên SM] AI report",
    chain_id="PHARMACY",
    space_key="AIN"
)
# Nếu tìm thấy → lấy page_id → get_confluence_page(page_id="...")
# Nếu không thấy → xem mục "Confluence Search Strategy" để xử lý
```

2. **Kiểm tra đầy đủ**:
   - Mục 2 (Mục tiêu) đã điền chưa?
   - Mục 3.1 (Tổng quan flow) đã điền chưa?
   - Mục 5 (Đề xuất) đã điền chưa?
   - Mục 6 (Playbook) đã điền chưa?
   - Mục 7 (Maturity) đã điền chưa?

3. **Nếu thiếu** → hỏi SM điền, kèm deadline gợi ý:

```
⚠️ Một số mục chưa được điền đầy đủ:
  ❌ Mục 2: Mục tiêu áp dụng AI-DLC  → SM điền
  ❌ Mục 5: Đề xuất cải tiến          → TechLead điền (deadline: hôm nay)
  ❌ Mục 6: Playbook đề xuất          → TechLead điền (deadline: hôm nay)
  ❌ Mục 7: Đánh giá maturity         → TechLead điền (deadline: hôm nay)

Bạn muốn:
A) Điền ngay (tôi sẽ hỏi từng mục)
B) Xuất report với thông tin hiện có (có thể thiếu)
C) Nhắc TechLead qua Jira comment
```

4. **Tạo sub-page AI Report** dưới page chính:

```python
rde_publish_to_confluence(
    title="📊 AI Report — [Mã CR]",
    content=<tổng hợp từ page chính + phân tích>,
    space_key="AIN",
    parent_page_id="<page_id chính>"
)
```

### Template AI Report (Sub-page)

```markdown
# 📊 AI Report — [Mã CR]

> Tổng hợp cuối dự án — Generated by Kiro AI-Native

---

## 1. 🎯 Thông tin dự án

| Field | Nội dung |
|-------|----------|
| Chuỗi/ Team | [từ page chính] |
| Dự án/ CR | [từ page chính] |
| DM/ SM phụ trách | [từ page chính] |
| Thời gian triển khai | [từ page chính] |
| Loại dự án | [từ page chính] |

---

## 2. 🧠 Mục tiêu & Kết quả

**Pain point trước khi áp dụng:**
[từ page chính]

**Kỳ vọng cải thiện:**
[từ page chính]

**Scope áp dụng:**
[từ page chính]

**Kết quả đạt được:**
_(Tự động tổng hợp từ SM Dashboard)_
- Total Tasks: [X]
- Completed: [Y] ([Z]%)
- Bugs Fixed: [X]
- Time saved: [ước tính từ số task × thời gian]

---

## 3. 🔁 AI-DLC Workflow đã áp dụng

### 3.1 Tổng quan flow
[từ page chính]

### 3.2 Chi tiết từng bước
[Tổng hợp từ các daily progress — top 5 highlights]

---

## 4. ⚠️ Vấn đề gặp phải

[từ page chính]

---

## 5. 💡 Đề xuất cải tiến

[từ page chính]

---

## 6. 🔁 Playbook đề xuất

[từ page chính]

**Khả năng reuse:** [từ page chính]

---

## 7. ⭐ Đánh giá maturity

[từ page chính]

---

## 📊 Metrics Summary

| Metric | Giá trị |
|--------|---------|
| Total Days | [X] ngày |
| Total Tasks | [X] |
| Completed | [Y] ([Z]%) |
| Bugs Fixed | [X] |
| Total AI time | [X]h (tổng toàn team) |
| Time saved (estimated) | [X]h |
| Team members involved | [X] người |

### ⏱️ AI Time Per Member

| Người | Role | Tổng thời gian dùng AI | Số tasks | Trung bình / task |
|-------|------|----------------------|----------|-------------------|
| [username] | [BA/Dev/Tester/TechLead] | [Xh] | [X] | [~Xp] |

---

## 📅 Timeline

| Phase | Thời gian | Highlights |
|-------|-----------|------------|
| [Phase 1] | [DD/MM → DD/MM] | [Highlights] |
| [Phase 2] | [DD/MM → DD/MM] | [Highlights] |

---

*Generated by Kiro AI-Native | chain_id: PHARMACY | Date: DD/MM/YYYY*
```

### Thông báo kết quả

```
✅ AI Report đã được xuất!

📊 Report: [Mã CR]
🔗 Link: [URL sub-page AI Report]
📅 Ngày xuất: DD/MM/YYYY

📋 Nội dung:
  • Thông tin dự án
  • Mục tiêu & Kết quả
  • Workflow đã áp dụng
  • Vấn đề & Đề xuất
  • Playbook & Maturity
  • Metrics Summary

📌 Next steps:
  1. Review AI Report
  2. Share với team
  3. Archive vào knowledge base
```

---

## 🔍 Confluence Search Strategy

> Áp dụng cho tất cả các bước cần tìm page chính hoặc sub-page daily của CR.

### Vấn đề

Page mới tạo trên Confluence có thể **chưa được index** vào vector store ngay lập tức. Nếu chỉ search bằng mã CR đơn lẻ, có thể không tìm thấy — đặc biệt với page vừa tạo trong ngày.

### Chiến lược search theo thứ tự ưu tiên

**Bước 0 — Đọc trực tiếp page theo page_id (nếu đã biết)** ⚡ NHANH NHẤT:

```python
# Nếu đã biết page_id (từ lần trước, từ parent page, từ user cung cấp)
# → Đọc trực tiếp, KHÔNG cần qua vector search
get_confluence_page(page_id="284426372")
# Luôn hoạt động — kể cả page vừa tạo 1 giây trước
# Performance: <1s, không phụ thuộc index
```

> ⚠️ **Quan trọng**: `get_confluence_page` gọi thẳng Confluence API → luôn trả về nội dung mới nhất, kể cả page chưa được index vào vector store.

**Bước 1 — Query kết hợp nhiều keywords** (khi chưa biết page_id):

```python
# Kết hợp mã CR + tên tính năng + tên người phụ trách
search_context_hybrid(
    query="LCR25-1084 QR code thanh toán ToanLBK daily progress",
    chain_id="PHARMACY",
    space_key="AIN"
)
```

Nguyên tắc: càng nhiều keywords đặc trưng → càng dễ match dù index chưa đầy đủ.

**Bước 2 — Nếu Bước 1 không tìm thấy VÀ page có thể mới tạo hôm nay** → đọc trực tiếp page chính của CR rồi tìm sub-page:

```python
# Đọc page chính của CR (page_id đã biết từ lần setup)
main_page = get_confluence_page(page_id="<page_id_chính_của_CR>")

# Từ page chính → tìm sub-page daily theo naming convention
# Title convention: "[AIN] 📆 DD/MM/YYYY — Daily Progress — [Mã CR]"
# Nếu biết page_id của sub-page → đọc trực tiếp
```

> 💡 **Tại sao**: Pages mới tạo trong ngày chưa được index vào vector store (có thể mất vài giờ hoặc đến ngày hôm sau). Nhưng `get_confluence_page` luôn đọc được ngay vì gọi thẳng Confluence API.

**Bước 3 — Nếu Bước 1 không tìm thấy VÀ không biết page_id** → thử query rộng hơn:

```python
search_context_hybrid(
    query="LCR25-1084 AI report",
    chain_id="PHARMACY",
    space_key="AIN"
)
```

**Bước 4 — Nếu vẫn không tìm thấy** → force re-index space AIN:

```python
# ⚠️ SLOW (>30s) — chỉ dùng khi thực sự cần
sync_sources(source_id="confluence:AIN")
```

Sau khi sync xong → quay lại Bước 1.

**Bước 5 — Fallback cuối cùng** → hỏi user cung cấp page ID trực tiếp:

```
⚠️ Không tìm thấy page của CR [mã CR] trong index.
Bạn có thể cung cấp Page ID hoặc URL Confluence không?
Ví dụ: https://confluence.frt.vn/pages/viewpage.action?pageId=271209537
```

Sau khi có page ID → dùng `get_confluence_page(page_id="...")` để đọc trực tiếp, không cần qua search.

### Quy tắc đặt query hiệu quả

| ❌ Kém | ✅ Tốt |
|--------|--------|
| `"LCR25-1084"` | `"LCR25-1084 QR code ToanLBK"` |
| `"daily progress"` | `"LCR25-1084 daily progress 04/05/2026"` |
| `"AI report"` | `"LCR25-1084 AI report QR code thanh toán"` |

### Khi nào cần sync_sources

| Tình huống | Cần sync? | Giải pháp thay thế |
|-----------|-----------|---------------------|
| Page tạo > 1 ngày trước | ❌ Không cần — đã index | — |
| Page tạo trong ngày, search không thấy | ⚠️ Thử `get_confluence_page` trước | Dùng page_id trực tiếp nếu biết |
| Nhiều page mới tạo cùng lúc | ✅ Cần sync 1 lần | Hoặc đọc từng page bằng page_id |
| Search thấy kết quả cũ, không có page mới | ✅ Cần sync | — |
| Chỉ cần đọc 1 page cụ thể đã biết page_id | ❌ Không cần | `get_confluence_page(page_id=...)` |

> ⚠️ `sync_sources` mất >30s — không gọi thường xuyên. Ưu tiên dùng `get_confluence_page` với page_id khi có thể.

### ⏱️ Index Timing — Khi nào page mới xuất hiện trong search?

| Phương thức | Thời gian chờ | Ghi chú |
|-------------|---------------|---------|
| `get_confluence_page(page_id=...)` | **0s** — ngay lập tức | Gọi thẳng Confluence API, không qua index |
| `sync_sources(source_id="confluence:AIN")` | **~30-60s** | Force re-index, page xuất hiện trong search ngay sau |
| Auto sync (scheduled) | **Vài giờ → 1 ngày** | Server MCP chạy batch sync định kỳ |
| `search_context_hybrid(...)` | Chỉ tìm thấy page **đã được index** | Page mới tạo hôm nay có thể chưa thấy |

**Quy tắc vàng**: Khi cần đọc page mới tạo trong ngày → **luôn dùng `get_confluence_page` với page_id** thay vì search. Search chỉ hiệu quả cho pages đã index (thường > 1 ngày tuổi).

---

## 🚫 Anti-Patterns

### ❌ Không làm
- Tạo page chính mà không hỏi đầy đủ thông tin (mã CR, tên, thời gian, v.v.)
- Tạo sub-page daily mà không link với page chính
- Update daily mà không ghi rõ người thực hiện (username)
- Xuất AI report khi các mục quan trọng chưa điền (Mục 2, 5, 6, 7)
- Xóa nội dung daily của người khác khi update
- Không update SM Dashboard sau mỗi ngày

### ✅ Luôn làm
- Kiểm tra đầy đủ thông tin trước khi tạo page
- Tạo sub-page daily dưới đúng page chính (parent_page_id)
- Ghi rõ username khi update daily
- Hỏi SM điền đầy đủ trước khi xuất AI report
- **Append-only**: chỉ thêm dòng mới vào bảng — không xóa, không sửa dòng của người khác
- Update SM Dashboard hàng ngày

### 🔒 Quy tắc Append-Only — BẮT BUỘC

> Đây là quy tắc quan trọng nhất khi nhiều người cùng update vào 1 sub-page daily.

**Khi update sub-page daily:**
1. **Đọc nội dung hiện tại** của page trước: `get_confluence_page(page_id="...")`
2. **Chỉ thêm dòng mới** vào bảng Tasks — không xóa, không sửa dòng của người khác
3. **Merge nội dung**: giữ nguyên toàn bộ nội dung cũ + append nội dung mới
4. **Publish lại** với nội dung đã merge

```python
# ✅ ĐÚNG — Append-only workflow
existing = get_confluence_page(page_id="<sub_page_id>")
new_row = "| lanttp | Task mới | FC-XXXX | ~1h | ~20p | ✅ Done | |"
merged_content = existing["content"] + new_row  # Giữ nguyên + thêm mới
rde_publish_to_confluence(page_id="<sub_page_id>", content=merged_content, ...)

# ❌ SAI — Overwrite toàn bộ
rde_publish_to_confluence(page_id="<sub_page_id>", content=only_new_content, ...)
```

**Khi nào được phép overwrite:**
- Chỉ SM mới được overwrite SM Dashboard trên page chính
- Không ai được overwrite bảng Tasks trong sub-page daily
- Không ai được xóa sub-page daily đã tạo

---

## 📋 Checklist Theo Role

### SM (Scrum Master)

**Đầu dự án:**
- [ ] Setup page chính: `Setup AI report [mã CR]`
- [ ] Điền Mục 2 (Mục tiêu áp dụng AI-DLC)

**Hàng ngày:**
- [ ] Điền SM Summary trong sub-page daily
- [ ] Gõ: `SM update [mã CR]` — AI tổng hợp từ daily progress + Jira epic → update page chính
- [ ] Kiểm tra AI Adoption Rate — nhắc CR members nếu < 80%

**Cuối mỗi sprint:**
- [ ] Sprint review: `Sprint review AI [mã CR]`

**Cuối dự án:**
- [ ] Nhắc TechLead điền Mục 5, 6, 7
- [ ] Xuất AI report: `Xuất AI report [mã CR]`

---

### BA / Dev / Tester / TechLead

**Hàng ngày — chỉ cần 1 lệnh:**
- [ ] Cuối session gõ: `Daily update` — AI tự detect tất cả CR, tự tổng hợp, tự publish
- [ ] Confirm preview (1 click)
- [ ] Nếu gặp AI friction → điền vào bảng "AI Friction hôm nay" trong sub-page daily

> ✅ Không cần biết mã CR. Không cần gõ nhiều lần. Nhiều session trong ngày → gõ nhiều lần `"Daily update"` → AI append thêm, không overwrite.

**Cuối dự án (TechLead):**
- [ ] Điền Mục 5 (Đề xuất cải tiến) — trước khi SM xuất report
- [ ] Điền Mục 6 (Playbook đề xuất) — trước khi SM xuất report
- [ ] Điền Mục 7 (Đánh giá maturity) — trước khi SM xuất report

---

## 🤖 AI Prompt Mẫu

### Prompt setup page chính

```
Setup AI report cho CR LCR25-1084.
Tên CR: "Tính năng tạo QR code trên màn hình thanh toán"
DM/SM: ToanLBK
Thời gian: 01/04/2026 → 30/04/2026
Loại dự án: New build
chain_id: PHARMACY
```

### Prompt tạo template daily

```
Tạo template daily LCR25-1084 cho ngày 18/04/2026.
chain_id: PHARMACY
```

### Prompt update daily (member)

```
Daily update
```

AI tự detect tất cả CR trong session, group tasks theo CR, publish vào đúng sub-page. Không cần khai thêm gì.

### Prompt SM update cuối ngày

```
SM update LCR25-1084
```

AI đọc daily progress hôm nay + Jira epic → tự động update SM Dashboard và Daily Progress Log trên page chính.

### Prompt sprint review

```
Sprint review AI LCR25-1084
chain_id: PHARMACY
```

### Prompt xuất AI report

```
Xuất AI report LCR25-1084.
chain_id: PHARMACY
```

---

## 📊 Success Metrics

| Metric | Target | Cách đo |
|--------|--------|---------|
| Page setup time | ≤ 5 phút | Time from trigger to page created |
| Daily update time | ≤ 2 phút | Time from trigger to update done |
| Report completeness | 100% | All 7 sections filled |
| Team adoption | ≥ 80% | % CR members (từ Jira epic) update daily |
| Report export time | ≤ 10 phút | Time from trigger to AI report created |

---

## 📎 Tài Liệu Liên Quan

| Skill | Trigger | Dùng khi |
|---|---|---|
| `test-plan-writer.md` | "Tạo test plan" | Tạo test plan cho CR |
| `daily-update.md` | "Daily update" | Báo cáo test hàng ngày |
| `ai-project-report.md` | "Setup AI report" | Setup report dự án AI |
| `team-effort-report.md` | "Team effort report" | SM tổng hợp effort cross-CR theo người/sprint |

---

**Version**: 1.3
**Last Updated**: 2026-05-13
**Owner**: AI Context Engine Team — Nhà Thuốc (NT) / PHARMACY
