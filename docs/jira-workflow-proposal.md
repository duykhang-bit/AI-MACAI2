# Đề xuất Jira Workflow cho AI-Native SDLC

> Cập nhật: 2026-04-11
> Mục tiêu: Sửa Jira workflow cho phù hợp với AI-Native SDLC pipeline, nơi AI Context Engine tự động transition tickets qua các bước.

---

## 1. Vấn đề hiện tại

- Jira workflow hiện tại không có các trạng thái phù hợp với AI-Native SDLC
- AI agent cần `jira_transition()` để chuyển trạng thái ticket, nhưng mỗi project có workflow khác nhau
- Không có trạng thái cho: Shape (sinh tài liệu), Review Shape, Build (code), Verify (quality gate), Ship (deploy)
- HITL approval gates không có trạng thái tương ứng trong Jira

---

## 2. Workflow đề xuất — Feature/Story

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                   │
│  Open ──→ In Analysis ──→ In Review ──→ In Development ──→       │
│                                                                   │
│  ──→ Code Review ──→ In Testing ──→ Ready for UAT ──→            │
│                                                                   │
│  ──→ UAT Approved ──→ Ready for PROD ──→ Done                    │
│                                                                   │
│  (Bất kỳ trạng thái nào) ──→ Blocked / Cancelled                │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

### Chi tiết trạng thái

| # | Trạng thái | Category | AI trigger | Human trigger | Mô tả |
|---|-----------|----------|------------|---------------|-------|
| 1 | **Open** | To Do | — | Tạo ticket | Ticket mới, chưa bắt đầu |
| 2 | **In Analysis** | In Progress | `start_work_from_ticket` / `get_work_context` | — | AI đang thu thập context + sinh requirements/design |
| 3 | **In Review** | In Progress | `request_shape_review` | — | Chờ review tài liệu (requirements + design) |
| 4 | **In Development** | In Progress | Sau khi Shape approved | Developer bắt đầu code | AI/Developer đang code, tạo branch, commit |
| 5 | **Code Review** | In Progress | `create_merge_request` | — | MR đã tạo, chờ Solution Reviewer approve |
| 6 | **In Testing** | In Progress | Sau MR merged | — | Quality gates đang chạy, test report đang sinh |
| 7 | **Ready for UAT** | In Progress | `request_deploy_approval(env="uat")` | — | Chờ Intent Owner approve deploy UAT |
| 8 | **UAT Approved** | In Progress | — | Intent Owner approve | UAT đã deploy, đang soak test |
| 9 | **Ready for PROD** | In Progress | `request_deploy_approval(env="prod")` | — | Chờ Quality Reviewer approve deploy PROD |
| 10 | **Done** | Done | — | Sau deploy PROD thành công | Hoàn thành |
| 11 | **Blocked** | In Progress | — | Khi bị block | Đang chờ dependency/issue khác |
| 12 | **Cancelled** | Done | — | Khi hủy | Không thực hiện nữa |

### Transitions

| Từ | Đến | Ai trigger | Điều kiện |
|----|-----|-----------|-----------|
| Open | In Analysis | AI (`jira_transition`) | — |
| In Analysis | In Review | AI (`request_shape_review`) | Có link Confluence (requirements + design) |
| In Review | In Development | Human (approve shape) | Shape approved |
| In Review | In Analysis | Human (reject shape) | Cần sửa lại |
| In Development | Code Review | AI (`create_merge_request`) | Có MR link |
| Code Review | In Development | Human (reject MR) | Cần sửa code |
| Code Review | In Testing | Human (merge MR) | MR merged |
| In Testing | Ready for UAT | AI (`request_deploy_approval`) | Quality gates pass |
| In Testing | In Development | AI/Human | Quality gates fail |
| Ready for UAT | UAT Approved | Human (Intent Owner) | Approve UAT deploy |
| UAT Approved | Ready for PROD | AI (`request_deploy_approval`) | UAT soak OK |
| Ready for PROD | Done | Human (Quality Reviewer) | Approve PROD deploy |
| * | Blocked | Human | Bất kỳ lúc nào |
| * | Cancelled | Human | Bất kỳ lúc nào |
| Blocked | (trạng thái trước) | Human | Unblock |

---

## 3. Workflow đề xuất — Bug/Incident

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                   │
│  Open ──→ Triaging ──→ Diagnosing ──→ In Fix ──→                 │
│                                                                   │
│  ──→ Fix Review ──→ Fix Testing ──→ Fix Deployed ──→ Done        │
│                                                                   │
│  (Nếu P1/P2) Done ──→ Post-Mortem ──→ RCA Done                  │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

### Chi tiết trạng thái

| # | Trạng thái | Category | AI trigger | Human trigger | Mô tả |
|---|-----------|----------|------------|---------------|-------|
| 1 | **Open** | To Do | — | Report incident | Incident mới |
| 2 | **Triaging** | In Progress | `incident_triage` (tương lai) | — | AI đang phân tích severity, affected services |
| 3 | **Diagnosing** | In Progress | `incident_diagnose` (tương lai) | — | AI đang tìm root cause |
| 4 | **In Fix** | In Progress | `start_hotfix` (tương lai) / manual | Developer fix | L2: workaround, L3: hotfix code |
| 5 | **Fix Review** | In Progress | Tạo hotfix MR | — | Chờ review hotfix MR |
| 6 | **Fix Testing** | In Progress | Merge hotfix | — | Quality gates trên hotfix |
| 7 | **Fix Deployed** | In Progress | Deploy hotfix | — | Hotfix đã deploy, monitoring |
| 8 | **Done** | Done | — | Confirm fix OK | Incident resolved |
| 9 | **Post-Mortem** | In Progress | `generate_rca_report` (tương lai) | — | Sinh RCA report (bắt buộc P1/P2) |
| 10 | **RCA Done** | Done | — | Approve RCA | RCA published, action items tracked |

---

## 4. Workflow đề xuất — Sprint Planning

Không cần workflow riêng — dùng Sprint board + AI tools:

| Bước | AI Tool | Output |
|------|---------|--------|
| Xem velocity | `get_team_velocity` | Velocity stats |
| Estimate tickets | `estimate_ticket` | Context cho LLM estimate |
| Phân loại backlog | `classify_backlog` | Backlog items + metadata |
| Đề xuất sprint plan | `smart_sprint_plan` | Plan + capacity analysis |
| Publish plan | `publish_plan_to_confluence` | Confluence page + Jira comment |

---

## 5. Áp dụng cho các Jira Projects

### Phase 1 — Pilot (VAC chain)
- Projects: FV, VADS, VCR
- Áp dụng Feature/Story workflow mới
- Test với AI agent: `jira_transition` qua các trạng thái

### Phase 2 — Mở rộng
- Projects: LAB, PHARMACY, ICT, BO, PLATFORM, DATA
- Copy workflow từ Phase 1
- Customize nếu cần (mỗi chain có thể có đặc thù)

### Lưu ý khi sửa workflow

1. **Giữ backward compatible**: Thêm trạng thái mới, KHÔNG xóa trạng thái cũ ngay
2. **Transition names phải consistent**: AI dùng `jira_get_transitions()` để lấy danh sách, rồi match theo tên
3. **Tên trạng thái tiếng Anh**: AI agent match tên transition, nên dùng tiếng Anh cho consistency
4. **Test trước khi apply**: Dùng `jira_get_transitions(issue_key)` để verify transitions available

---

## 6. Mapping AI Tools → Jira Transitions

| AI Tool | Transition trigger | Từ → Đến |
|---------|-------------------|-----------|
| `get_work_context` | Bắt đầu phân tích | Open → In Analysis |
| `publish_requirements_to_confluence` | Sinh requirements xong | (giữ In Analysis) |
| `publish_design_to_confluence` | Sinh design xong | (giữ In Analysis) |
| `request_shape_review` | Yêu cầu review | In Analysis → In Review |
| `create_feature_branch` | Bắt đầu code | In Development (manual) |
| `create_merge_request` | Tạo MR | In Development → Code Review |
| `publish_test_report` | Test xong | In Testing (sau merge) |
| `request_deploy_approval(uat)` | Yêu cầu deploy UAT | In Testing → Ready for UAT |
| `request_deploy_approval(prod)` | Yêu cầu deploy PROD | UAT Approved → Ready for PROD |

---

## 7. HITL Approval Gates trong Jira

Mỗi HITL gate tương ứng với một transition cần Human approve:

| Gate | Trạng thái chờ | Ai approve | Transition khi approve |
|------|----------------|------------|----------------------|
| Shape Review (Business) | In Review | Intent Owner | In Review → In Development |
| Shape Review (Technical) | In Review | Solution Reviewer | In Review → In Development |
| Code Review | Code Review | Solution Reviewer | Code Review → In Testing |
| UAT Deploy | Ready for UAT | Intent Owner | Ready for UAT → UAT Approved |
| PROD Deploy | Ready for PROD | Quality Reviewer | Ready for PROD → Done |
| Hotfix Review | Fix Review | Solution Reviewer | Fix Review → Fix Testing |
| Hotfix Deploy (P1/P2) | Fix Testing | Quality Reviewer (fast-track) | Fix Testing → Fix Deployed |
