---
inclusion: auto
---

# Client-Orchestrated AMS Flow — HITL Protocol

Khi user báo incident hoặc yêu cầu xử lý bug/incident từ Jira (ví dụ: "xử lý incident FI-12345", "triage bug PROJ-999"), bạn PHẢI tuân theo workflow sau với Human-in-the-Loop checkpoints. KHÔNG ĐƯỢC chạy thẳng từ đầu đến cuối.

## Tổng quan Flow

```
Triage (C1) → [Post-Triage Gate] → [Transition: New→Triaged]
  → Diagnose (C2) → [Pre-Hotfix Gate] → [Transition: Triaged→Diagnosing→Fixing]
  → L2 Workaround (C3) HOẶC L3 Hotfix (C4)
  → [Post-Hotfix Gate] → [Request Hotfix Review]
  → [Ship Hotfix: CI→UAT→PROD] → [Transition: Deploying→Resolved]
  → RCA Report (C5) → [Request RCA Review]
  → [Transition: Resolved→Post_Mortem→Closed]
```

## Phase 1: Triage

1. Gọi `incident_triage(issue_key, chain_id)` — AI phân tích incident, suggest severity, affected services, L2/L3 recommendation
2. Gọi `ams_post_triage_gate(issue_key, chain_id)` — validate triage completeness

**⏸️ CHECKPOINT 1 — Review Triage:**
- Trình bày kết quả triage cho user:
  - Severity suggestion (P1–P4)
  - Affected services
  - Similar incidents (nếu có)
  - L2/L3 recommendation
  - Gate result (PASS/WARN/FAIL + checklist)
- Hỏi user: "Triage OK chưa? Severity đúng không? Muốn chỉnh gì không?"
- Nếu user confirm → gọi `ams_transition_incident(issue_key, "Triaged")` và tiếp Phase 2
- **KHÔNG được tiếp tục nếu user chưa confirm severity**

## Phase 2: Diagnose

3. Gọi `ams_transition_incident(issue_key, "Diagnosing")`
4. Gọi `incident_diagnose(issue_key, chain_id)` — AI tổng hợp evidence, phân tích root cause, detect design gaps

**⏸️ CHECKPOINT 2 — Review Diagnosis:**
- Trình bày kết quả diagnosis:
  - Immediate cause
  - Root cause
  - Evidence summary (code context, docs, deployments)
  - Shape improvements (nếu có design gap)
  - Similar past incidents
- Hỏi user: "Root cause đúng không? Cần investigate thêm không?"
- Nếu user muốn re-triage → gọi `ams_transition_incident(issue_key, "Triaged")` và quay Phase 1
- Nếu user confirm → tiếp Phase 3

## Phase 3: Fix (L2 hoặc L3)

### Path A — L2 Workaround

5a. Gọi `suggest_workaround(issue_key, diagnosis)` — AI suggest workaround options ranked by risk/effort
6a. Gọi `ams_pre_hotfix_gate(issue_key, chain_id, diagnosis)` — validate diagnosis completeness

**⏸️ CHECKPOINT 3A — Review Workaround:**
- Trình bày workaround options:
  - Mỗi option: steps, risk level, estimated effort, rollback plan
  - L3 recommendation (nếu không có workaround phù hợp)
- Hỏi user: "Chọn workaround nào? Hay cần L3 code fix?"
- User chọn workaround → user tự execute → document kết quả vào Jira
- User chọn L3 → chuyển sang Path B

### Path B — L3 Hotfix

5b. Gọi `ams_pre_hotfix_gate(issue_key, chain_id, diagnosis)` — validate diagnosis completeness
6b. Gọi `ams_transition_incident(issue_key, "Fixing")`
7b. Gọi `start_hotfix(issue_key, chain_id, source_id, diagnosis)` — tạo hotfix branch + MR + code context

**⏸️ CHECKPOINT 3B — Review Hotfix:**
- Trình bày hotfix details:
  - Branch name, MR URL
  - Code context (relevant code, change points, affected files)
  - Follow-up story (nếu có shape improvement)
- Hỏi user: "MR đã tạo. Bạn review code rồi cho biết khi nào xong?"
- User review + fix code → commit → push
- Khi user confirm code ready → tiếp Phase 4

## Phase 4: Verify & Ship

8. Gọi `ams_post_hotfix_gate(issue_key, mr_url)` — validate MR quality
9. Gọi `ams_request_hotfix_review(issue_key, mr_url)` — request Solution_Reviewer approve MR
10. Gọi `ams_transition_incident(issue_key, "Verifying")`

**⏸️ CHECKPOINT 4 — Review Gate & Approval:**
- Trình bày gate result + approval status
- Chờ MR approval từ Solution_Reviewer
- Khi approved → tiếp ship

11. Gọi `ams_pre_deploy_gate(issue_key, "ci", build_info)` — validate CI readiness
12. Gọi `ams_ship_hotfix(issue_key, "ci", build_info)` — trigger CI build
13. Gọi `ams_transition_incident(issue_key, "Deploying")`

**⏸️ CHECKPOINT 5 — Ship Progress:**
- Trình bày CI build result
- Hỏi user: "CI passed. Promote lên UAT?"
- Nếu user confirm:
  - Gọi `ams_request_deploy_approval(issue_key, "uat")` — request Quality_Reviewer approve
  - Chờ approval → gọi `ams_ship_hotfix(issue_key, "uat", build_info)`
- Hỏi user: "UAT OK. Promote lên PROD?"
- Nếu user confirm:
  - Gọi `ams_request_deploy_approval(issue_key, "prod")` — request Quality_Reviewer approve
  - Chờ approval → gọi `ams_ship_hotfix(issue_key, "prod", build_info)`
- Sau mỗi deploy: gọi health check nếu cần

14. Gọi `ams_transition_incident(issue_key, "Resolved")`

## Phase 5: Post-Mortem (bắt buộc cho P1/P2)

15. Gọi `generate_rca_report(issue_key)` — AI sinh RCA report, publish Confluence, tạo action items
16. Gọi `ams_request_rca_review(issue_key, rca_report)` — request Solution_Reviewer review RCA
17. Gọi `ams_transition_incident(issue_key, "Post_Mortem")`

**⏸️ CHECKPOINT 6 — Review RCA:**
- Trình bày RCA report:
  - Timeline, root cause, fix applied, impact
  - Action items (Jira sub-tasks đã tạo)
  - Confluence page URL
- Hỏi user: "RCA OK chưa? Cần chỉnh sửa gì không?"
- Nếu user confirm → gọi `ams_transition_incident(issue_key, "Closed")`

## Escalation

Tại bất kỳ thời điểm nào, nếu incident chưa được xử lý kịp SLA:
- Gọi `ams_escalate_incident(issue_key, reason)` — escalate lên level tiếp theo
- Thresholds: P1=15min, P2=30min, P3=2h, P4=8h (configurable)
- Escalation levels: L1 → L2 → L3 → Management

## Rollback

Nếu deploy gặp vấn đề:
- Gọi health check → nếu critical/degraded → recommend rollback
- PROD rollback cần HITL approval (Quality_Reviewer)
- CI/UAT rollback tự động
- Sau rollback → `ams_transition_incident(issue_key, "Fixing")` để quay lại fix

## Quy tắc bắt buộc

1. **LUÔN dừng lại hỏi user** sau mỗi checkpoint — không bao giờ tự động tiếp tục
2. **LUÔN hiển thị gate results** — checklist items, blocking items, PASS/WARN/FAIL
3. **LUÔN chờ approval** trước khi ship — không bypass HITL gates
4. **P1/P2 fast-track** — compressed timelines nhưng KHÔNG skip gates hay approvals
5. **Post-Mortem bắt buộc** cho P1/P2 — không được skip
6. **State machine enforcement** — dùng `ams_transition_incident` để track lifecycle
7. Nếu user nói "OK", "được", "tiếp", "lgtm" → coi là confirm
8. Nếu user nói "sửa", "chỉnh", "thêm", "rollback" → iterate lại bước hiện tại

## MCP Tools Reference

| Tool | Phase | Mô tả |
|------|-------|-------|
| `incident_triage` | 1 | Triage incident, suggest severity |
| `incident_diagnose` | 2 | Diagnose root cause |
| `suggest_workaround` | 3A | L2 workaround options |
| `start_hotfix` | 3B | L3 hotfix branch + MR |
| `generate_rca_report` | 5 | RCA report + Confluence |
| `ams_post_triage_gate` | 1 | Gate: triage completeness |
| `ams_pre_hotfix_gate` | 3 | Gate: diagnosis completeness |
| `ams_post_hotfix_gate` | 4 | Gate: MR quality |
| `ams_pre_deploy_gate` | 4-5 | Gate: deploy readiness |
| `ams_request_hotfix_review` | 4 | HITL: MR review |
| `ams_request_deploy_approval` | 5 | HITL: deploy approval |
| `ams_request_rca_review` | 5 | HITL: RCA review |
| `ams_ship_hotfix` | 5 | Ship: CI/UAT/PROD |
| `ams_transition_incident` | All | State machine transition |
| `ams_escalate_incident` | Any | Escalation |
