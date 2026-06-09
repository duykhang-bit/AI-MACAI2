---
inclusion: auto
---

# Client-Orchestrated Shape Flow — HITL Protocol

Khi user yêu cầu thực hiện một Jira ticket (ví dụ: "thực hiện ticket FV-23829"), bạn PHẢI tuân theo workflow sau với Human-in-the-Loop checkpoints. KHÔNG ĐƯỢC chạy thẳng từ đầu đến cuối.

## Flow với HITL Checkpoints

### Phase 1: Thu thập context
1. Gọi `jira_get_issue(issue_key)` — lấy chi tiết ticket
2. Gọi `get_work_context(issue_key, chain_id)` — thu thập context từ Jira + Confluence + Git
3. Gọi `get_domain_compliance_rules(chain_id)` — lấy compliance rules

**⏸️ CHECKPOINT 1 — Review Context:**
- Trình bày tóm tắt context đã thu thập cho user:
  - Ticket summary, status, assignee
  - Jira URL
  - Confluence docs liên quan (có link)
  - Code files liên quan
  - Compliance rules áp dụng
- Hỏi user: "Context đã đủ chưa? Bạn muốn bổ sung thêm gì không?"
- Nếu user bổ sung → ghi nhận thêm context
- Nếu user confirm → tiếp Phase 2
- **KHÔNG được tiếp tục nếu user chưa confirm**

### Phase 2: Sinh Requirements
4. Dựa trên context đã confirm, sinh requirements document (Kiro-style EARS format)
5. Trình bày requirements cho user review

**⏸️ CHECKPOINT 2 — Review Requirements:**
- Hiển thị đầy đủ requirements document cho user
- Hỏi user: "Requirements đã OK chưa? Cần chỉnh sửa gì không?"
- Nếu user yêu cầu sửa → sửa và trình bày lại
- Nếu user confirm → gọi `publish_requirements_to_confluence` và tiếp Phase 3
- **KHÔNG được publish nếu user chưa confirm**

### Phase 3: Sinh Design
6. Dựa trên requirements đã confirm, sinh design document (Kiro-style format)
7. Trình bày design cho user review

**⏸️ CHECKPOINT 3 — Review Design:**
- Hiển thị đầy đủ design document cho user
- Hỏi user: "Design đã OK chưa? Cần chỉnh sửa gì không?"
- Nếu user yêu cầu sửa → sửa và trình bày lại
- Nếu user confirm → gọi `publish_design_to_confluence` và tiếp Phase 4
- **KHÔNG được publish nếu user chưa confirm**

### Phase 4: Finalize
8. Gọi `jira_add_comment` — comment tổng hợp links Requirements + Design
9. Gọi `jira_transition` — chuyển trạng thái ticket (nếu phù hợp)
10. Gọi `track_hitl_decision` — ghi lại toàn bộ HITL decisions

**⏸️ CHECKPOINT 4 — Confirm hoàn thành:**
- Trình bày tổng kết: links Confluence, Jira status mới
- Hỏi user: "Shape phase hoàn thành. Bạn muốn tiếp tục Review Shape (B4) không?"

## Quy tắc bắt buộc

1. **LUÔN dừng lại hỏi user** sau mỗi checkpoint — không bao giờ tự động tiếp tục
2. **LUÔN hiển thị links** cho mọi resource tạo mới (Confluence page, Jira comment)
3. **LUÔN cho phép user sửa** trước khi publish — iterate cho đến khi user hài lòng
4. **LUÔN ghi lại HITL decisions** bằng `track_hitl_decision` hoặc Jira comment
5. Nếu user nói "OK", "được", "tiếp", "lgtm" → coi là confirm, tiếp checkpoint tiếp theo
6. Nếu user nói "sửa", "chỉnh", "thêm", "bớt" → iterate lại bước hiện tại

## Áp dụng cho

- Shape Flow (B3) — requirements + design
- Build Flow (B5) — code changes (khi có D2)
- AMS Flow — diagnosis + fix (khi có D4)
- Bất kỳ workflow nào có output cần human review
