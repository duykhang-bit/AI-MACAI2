<!-- AI Context Client: role-ba part 1/2 — enable all parts Always On. -->

# Role-Specific Tools: Business Analyst (BA)

**Mục đích**: Lọc và hiển thị chỉ 25 công cụ MCP phù hợp với vai trò Business Analyst để tránh choáng ngợp bởi 85 công cụ.

**Áp dụng cho**: Users có role = "ba" hoặc "business_analyst"

---

## 🎯 Business Analyst Workflow

```
Intent → Requirements → Design → Publish → Review → Approve
```

Business Analyst cần công cụ để:
1. **Tìm context** - Hiểu business rules, existing designs
2. **Tạo Jira** - Tạo Epic/Story từ intent
3. **Viết requirements** - Generate và publish requirements
4. **Viết design** - Generate và publish design
5. **Planning** - Sprint planning, estimation

---

## ✅ 25 Công Cụ Cho Business Analyst

### 1. Context Tools (5 tools) - Tìm Business Context

**Mục đích**: Tìm business rules, existing designs, related documents.

| Tool | Khi Nào Dùng | Performance |
|------|--------------|-------------|
| `search_context_hybrid` ⭐ | Tìm business docs với filters | FAST (<1s) |
| `get_confluence_page` | Đọc existing design/requirements | FAST (<1s) |
| `get_work_context` | Aggregate context từ Jira + Confluence | FAST (<1s) |
| `get_task_brief` | Tạo brief cho task | FAST (<1s) |
| `get_domain_compliance_rules` | Lấy compliance rules theo domain | FAST (<1s) |

**Best Practices**:
- Luôn dùng `search_context_hybrid` với `role="ba"` + `doc_type="confluence_ba"`
- Dùng `get_work_context` để aggregate context trước khi viết requirements
- Dùng `get_domain_compliance_rules` để đảm bảo compliance

**Ví dụ**:
```python
# Tìm business rules về payment
search_context_hybrid(
    query="payment business rules",
    chain_id="ICT",
    domain="ict_oms",
    role="ba",
    doc_type="confluence_ba"
)

# Lấy work context cho ticket
get_work_context(
    issue_key="FI-12345",
    chain_id="ICT",
    query="payment validation requirements"
)

# Lấy compliance rules cho Pharmacy domain
get_domain_compliance_rules(
    chain_id="PHARMACY",
    domain="pharmacy_retail"
)
```

---

### 2. Jira Tools (7 tools) - Tạo và Quản Lý Tickets

**Mục đích**: Tạo Epic/Story/Sub-task, link dossier, update tickets.

| Tool | Khi Nào Dùng | Performance |
|------|--------------|-------------|
| `jira_create_from_intent` | Tạo Epic/Story/Sub-task từ intent | MEDIUM (1-2s) |
| `jira_create_issue` | Tạo issue với full field control | MEDIUM (1-2s) |
| `jira_get_issue` | Đọc chi tiết ticket | FAST (<1s) |
| `jira_update_issue` | Update ticket fields | FAST (<1s) |
| `jira_add_comment` | Add comment vào ticket | FAST (<1s) |
| `jira_link_dossier` | Link task dossier với Jira | FAST (<1s) |
| `jira_search` | Tìm related tickets | FAST (<1s) |

**Best Practices**:
- Dùng `jira_create_from_intent` để tạo Epic/Story nhanh từ ý tưởng
- Dùng `jira_create_issue` khi cần control đầy đủ các fields
- Luôn link dossier với Jira bằng `jira_link_dossier`

**Ví dụ**:
```python
# Tạo Epic từ intent
jira_create_from_intent(
    intent="Implement payment validation for OMS",
    project_key="FI",
    issue_type="epic",
    components=["OMS", "Payment"],
    labels=["payment", "validation"]
)

# Tạo Story với full control
jira_create_issue(
    project_key="FI",
    summary="Add payment card validation",
    issue_type="Story",
    description="As a customer, I want to validate my payment card...",
    epic_key="FI-10000",
    story_points=5,
    priority="High"
)

# Link dossier với Jira
jira_link_dossier(
    task_id="2026/2026-04/task-abc123",
    issue_key="FI-12345"
)
```

---

### 3. RDE Tools (3 tools) - Requirements & Design Engine

**Mục đích**: Analyze impact, check compliance, publish documents.

| Tool | Khi Nào Dùng | Performance |
|------|--------------|-------------|
| `rde_analyze_impact` | Phân tích impact của requirements | FAST (<1s) |
| `rde_check_compliance` | Kiểm tra compliance theo domain | FAST (<1s) |
| `rde_publish_to_confluence` | Publish document lên Confluence | MEDIUM (1-2s) |

**Best Practices**:
- Chạy `rde_analyze_impact` trước khi finalize requirements
- Chạy `rde_check_compliance` để đảm bảo tuân thủ quy định
- Dùng `rde_publish_to_confluence` để publish requirements/design

**Ví dụ**:
```python
# Analyze impact
rde_analyze_impact(
    requirements_doc="...",
    collection="knowledge"
)

# Check compliance cho Pharmacy domain
rde_check_compliance(
    document="...",
    document_type="requirements",
    domains=["pharmacy"]
)

# Publish requirements
rde_publish_to_confluence(
    title="Payment Validation Requirements",
    content="...",
    space_key="ICT",
    parent_page_id="123456"
)
```

---

### 4. Publish Tools (4 tools) - Publish Requirements & Design

**Mục đích**: Publish requirements và design lên Confluence, track HITL decisions.

| Tool | Khi Nào Dùng | Performance |
|------|--------------|-------------|
| `publish_requirements_to_confluence` | Publish requirements (Kiro-style EARS) | MEDIUM (1-2s) |
| `publish_design_to_confluence` | Publish design (Kiro-style) | MEDIUM (1-2s) |
| `get_domain_compliance_rules` | Lấy compliance rules | FAST (<1s) |
| `track_hitl_decision` | Track human approval decisions | MEDIUM (1-2s) |

**Best Practices**:
- Dùng `publish_requirements_to_confluence` sau khi finalize requirements
- Dùng `publish_design_to_confluence` sau khi finalize design
- Track mọi HITL decision bằng `track_hitl_decision`

**Ví dụ**:
```python
# Publish requirements
publish_requirements_to_confluence(
    issue_key="FI-12345",
    content_markdown="...",
    space_key="ICT",
    chain_id="ICT"
)

# Publish design
publish_design_to_confluence(
    issue_key="FI-12345",
    content_markdown="...",
    space_key="ICT",
    chain_id="ICT"
)

# Track HITL decision
track_hitl_decision(
    issue_key="FI-12345",
    step="requirements_review",
    decision="approved",
    decided_by="product_owner@company.com"
)
```

---

### 5. Planning Tools (6 tools) - Sprint Planning & Estimation

**Mục đích**: Sprint planning, velocity tracking, ticket estimation.

| Tool | Khi Nào Dùng | Performance |
|------|--------------|-------------|
| `get_team_velocity` | Lấy historical velocity | FAST (<1s) |
| `suggest_sprint_plan` | Suggest sprint plan dựa trên velocity | MEDIUM (1-2s) |
| `estimate_ticket` | Gather context để estimate ticket | FAST (<1s) |
| `classify_backlog` | Classify backlog items | FAST (<1s) |
| `smart_sprint_plan` | AI-assisted sprint planning | MEDIUM (1-3s) |
| `publish_plan_to_confluence` | Publish sprint plan lên Confluence | MEDIUM (1-2s) |

**Best Practices**:
- Dùng `get_team_velocity` trước khi plan sprint
- Dùng `smart_sprint_plan` cho teams không có story points
- Publish sprint plan lên Confluence sau khi finalize

**Ví dụ**:
```python
# Lấy team velocity
get_team_velocity(
    chain_id="ICT",
    num_sprints=3
)

# Suggest sprint plan
suggest_sprint_plan(
    chain_id="ICT",
    sprint_name="Sprint 24",
    capacity_override=40.0
)

# Smart sprint planning (cho teams không có story points)
smart_sprint_plan(
    chain_id="ICT",
    sprint_name="Sprint 24",
    capacity_days=80.0  # 4 devs × 10 days × 2 SP/day
)

# Publish sprint plan
publish_plan_to_confluence(
    issue_key="FI-12345",
    plan_data={
        "sprint_name": "Sprint 24",
        "selected_items": [...],
        "capacity": 40
    },
    space_key="ICT"
)
```

---

## 🚫 Công Cụ KHÔNG Ưu Tiên Cho BA

BA **KHÔNG NÊN** dùng các công cụ sau trong workflow thông thường (dành cho Dev, Ops).

**LƯU Ý**: Đây là **gợi ý**, không phải **hard block**. Nếu BA thực sự cần (ví dụ: review code, check deployment status), vẫn có thể dùng với confirmation.

### Dev Tools (không ưu tiên cho BA)
- `create_feature_branch` - Tạo branch (Dev làm) - **Exception**: BA có thể tạo branch cho documentation
- `commit_changes` - Commit code (Dev làm) - **Exception**: BA có thể commit docs/specs
- `create_merge_request` - Tạo MR (Dev làm) - **Exception**: BA có thể tạo MR cho docs
- `qge_analyze` - Quality gates (Dev làm) - **Exception**: BA có thể review quality
- `trigger_ci_pipeline` - Trigger CI (Dev làm)

### Ops Tools (không ưu tiên cho BA)
- `promote_to_uat` - Deploy UAT (Ops làm) - **Exception**: BA có thể check UAT status
- `promote_to_prod` - Deploy PROD (Ops làm)
- `rollback` - Rollback deployment (Ops làm)
- `ams_*` tools - AMS workflow (Ops làm) - **Exception**: BA có thể read incident reports
- `jenkins_*` tools - Jenkins operations (Ops làm) - **Exception**: BA có thể check build status

---

## 📋 BA Workflow Checklist

### Phase 1: Gather Context
- [ ] Tìm business rules: `search_context_hybrid`
- [ ] Đọc existing designs: `get_confluence_page`
- [ ] Aggregate context: `get_work_context`
- [ ] Lấy compliance rules: `get_domain_compliance_rules`

### Phase 2: Create Jira
- [ ] Tạo Epic: `jira_create_from_intent`
- [ ] Tạo Stories: `jira_create_issue`
- [ ] Link dossier: `jira_link_dossier`

### Phase 3: Write Requirements
- [ ] Generate requirements (với client LLM)
- [ ] Check compliance: `rde_check_compliance`
- [ ] Analyze impact: `rde_analyze_impact`
- [ ] Publish requirements: `publish_requirements_to_confluence`
- [ ] Track approval: `track_hitl_decision`

### Phase 4: Write Design
- [ ] Generate design (với client LLM)
- [ ] Check compliance: `rde_check_compliance`
- [ ] Publish design: `publish_design_to_confluence`
- [ ] Track approval: `track_hitl_decision`

### Phase 5: Sprint Planning
- [ ] Lấy velocity: `get_team_velocity`
- [ ] Classify backlog: `classify_backlog`
- [ ] Plan sprint: `smart_sprint_plan`
- [ ] Publish plan: `publish_plan_to_confluence`

---

## 🎯 Quick Reference - Top 10 Tools Cho BA

1. **`search_context_hybrid`** ⭐ - Tìm business docs
2. **`get_work_context`** - Aggregate context
3. **`jira_create_from_intent`** - Tạo Epic/Story
4. **`publish_requirements_to_confluence`** - Publish requirements
5. **`publish_design_to_confluence`** - Publish design
6. **`rde_check_compliance`** - Check compliance
7. **`smart_sprint_plan`** - AI sprint planning
8. **`get_domain_compliance_rules`** - Lấy compliance rules
9. **`track_hitl_decision`** - Track approvals
10. **`jira_add_comment`** - Update progress

---

## 💡 Tips & Tricks

### Tip 1: Client-Orchestrated Shape Flow
```
get_work_context (Step 1) →
get_domain_compliance_rules (Step 2) →
[Client LLM generates requirements] (Step 3) →
publish_requirements_to_confluence (Step 4) →
[Client LLM generates design] (Step 5) →
publish_design_to_confluence (Step 6) →
jira_add_comment (Step 7) →
jira_transition (Step 8)
```

### Tip 2: Always Filter role="ba"
```
✅ GOOD:
search_context_hybrid("payment rules", chain_id="ICT", role="ba", doc_type="confluence_ba")

❌ BAD:
search_context_hybrid("payment rules", chain_id="ICT", doc_type="git_repo")  # Trả về code
```

### Tip 3: Check Compliance Early
```python
# Check compliance trước khi publish
rde_check_compliance(
    document="...",
    document_type="requirements",
    domains=["pharmacy"]  # Luật Dược, TT 09/2023
)
```

### Tip 4: Track All HITL Decisions
```python
# Track mọi approval decision
track_hitl_decision(
    issue_key="FI-12345",
    step="requirements_review",
    decision="approved",
    feedback="Looks good, approved",
    decided_by="product_owner@company.com"
)
```

---

