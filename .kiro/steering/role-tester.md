# Role-Specific Tools: Tester / QA

**Mục đích**: Lọc và hiển thị chỉ 20 công cụ MCP phù hợp với vai trò Tester/QA để tránh choáng ngợp bởi 85 công cụ.

**Áp dụng cho**: Users có role = "tester" hoặc "qa" hoặc "quality_assurance"

---

## 🎯 Tester Workflow

```
Ticket → Test Plan → Test Cases → Execute → Report → Bug → Verify → Close
```

Tester cần công cụ để:
1. **Tìm context** - Hiểu requirements, acceptance criteria, existing test cases
2. **Đọc Jira** - Lấy ticket details, tạo bug reports
3. **Chạy test** - Postman/Newman, quality gates
4. **Publish report** - Publish test report lên Confluence
5. **Track bugs** - Tạo bug ticket, track fix, verify

---

## ✅ 20 Công Cụ Cho Tester

### 1. Context Tools (4 tools) - Tìm Requirements & Test Context

**Mục đích**: Tìm requirements, acceptance criteria, existing test cases, design docs.

| Tool | Khi Nào Dùng | Performance |
|------|--------------|-------------|
| `search_context_hybrid` ⭐ | Tìm test cases, requirements với filters | FAST (<1s) |
| `get_confluence_page` | Đọc requirements/design doc từ Confluence | FAST (<1s) |
| `get_work_context` | Aggregate context từ Jira + Confluence | FAST (<1s) |
| `get_task_brief` | Lấy brief đầy đủ cho ticket cần test | FAST (<1s) |

**Best Practices**:
- Luôn dùng `search_context_hybrid` với `role="tester"` + `doc_type="confluence_ba"` để tìm requirements
- Dùng `get_work_context` để hiểu đầy đủ scope trước khi viết test cases
- Đọc design doc trước khi test để biết expected behavior

**Ví dụ**:
```python
# Tìm requirements và acceptance criteria
search_context_hybrid(
    query="payment validation acceptance criteria",
    chain_id="ICT",
    domain="ict_oms",
    role="tester",
    doc_type="confluence_ba"
)

# Lấy full context cho ticket cần test
get_work_context(
    issue_key="FI-12345",
    chain_id="ICT",
    query="payment validation test scenarios"
)
```

---

### 2. Jira Tools (7 tools) - Quản Lý Tickets & Bug Reports

**Mục đích**: Đọc tickets, tạo bug reports, update test status, track fixes.

| Tool | Khi Nào Dùng | Performance |
|------|--------------|-------------|
| `jira_get_issue` | Đọc chi tiết ticket cần test | FAST (<1s) |
| `jira_search` | Tìm related tickets, existing bugs | FAST (<1s) |
| `jira_create_issue` | Tạo bug report với full control | MEDIUM (1-2s) |
| `jira_create_from_intent` | Tạo bug nhanh từ mô tả | MEDIUM (1-2s) |
| `jira_add_comment` | Add test result comment vào ticket | FAST (<1s) |
| `jira_update_issue` | Update ticket (severity, status, assignee) | FAST (<1s) |
| `jira_transition` | Chuyển ticket sang In Testing / Done / Reopen | FAST (<1s) |

**Best Practices**:
- Dùng `jira_create_issue` để tạo bug với đầy đủ: steps to reproduce, expected vs actual, severity
- Luôn link bug với story/epic gốc qua `epic_key` hoặc `parent_key`
- Dùng `jira_add_comment` để update test progress (pass/fail count, blockers)
- Dùng `jira_transition` để chuyển ticket sang "In Testing" khi bắt đầu test

**Ví dụ**:
```python
# Đọc ticket cần test
jira_get_issue(issue_key="FI-12345")

# Tạo bug report
jira_create_issue(
    project_key="FI",
    summary="[BUG] Payment validation không reject thẻ hết hạn",
    issue_type="Bug",
    description="""
**Steps to Reproduce:**
1. Vào trang checkout
2. Nhập thẻ hết hạn (MM/YY < current date)
3. Click "Thanh toán"

**Expected:** Hiển thị lỗi "Thẻ đã hết hạn"
**Actual:** Hệ thống cho phép tiếp tục

**Severity:** High
**Environment:** UAT
**Build:** #123
    """,
    priority="High",
    labels=["bug", "payment", "regression"]
)

# Update test progress
jira_add_comment(
    issue_key="FI-12345",
    comment_body="🧪 Test Progress: 15/20 cases passed. 2 bugs found: FI-12350, FI-12351. Blocking: payment expired card validation."
)

# Chuyển ticket sang In Testing
jira_transition(issue_key="FI-12345", transition_name="In Testing")
```

---

### 3. QGE Tools (2 tools) - Quality Gates & Test Generation

**Mục đích**: Chạy quality gates, generate test skeleton từ code diff.

| Tool | Khi Nào Dùng | Performance |
|------|--------------|-------------|
| `qge_generate_tests` | Generate test skeleton từ code diff | FAST (<1s) |
| `qge_analyze` | Chạy quality gates trên diff | FAST (<1s) |

**Best Practices**:
- Dùng `qge_generate_tests` để tạo test skeleton nhanh từ code thay đổi
- Dùng `qge_analyze` để review code quality trước khi test

**Ví dụ**:
```python
# Generate test skeleton từ diff
qge_generate_tests(diff="...")

# Analyze quality
qge_analyze(diff="...", collection="knowledge")
```

---

### 4. Publish Tools (3 tools) - Publish Test Reports

**Mục đích**: Publish test report lên Confluence, track HITL decisions.

| Tool | Khi Nào Dùng | Performance |
|------|--------------|-------------|
| `publish_test_report` | Publish test report + link Jira + HITL nếu critical | MEDIUM (1-2s) |
| `rde_publish_to_confluence` | Publish test plan/report thủ công | MEDIUM (1-2s) |
| `track_hitl_decision` | Track QA sign-off decisions | MEDIUM (1-2s) |

**Best Practices**:
- Dùng `publish_test_report` sau mỗi test cycle để có report chuẩn
- Track QA sign-off bằng `track_hitl_decision` trước khi promote lên PROD
- Publish test plan lên Confluence trước khi bắt đầu test

**Ví dụ**:
```python
# Publish test report
publish_test_report(
    issue_key="FI-12345",
    test_results={
        "total": 20,
        "passed": 18,
        "failed": 2,
        "skipped": 0,
        "bugs": ["FI-12350", "FI-12351"]
    },
    gate_results={
        "regression": "PASS",
        "smoke": "PASS",
        "performance": "WARN"
    },
    space_key="ICT"
)

# Track QA sign-off
track_hitl_decision(
    issue_key="FI-12345",
    step="qa_sign_off",
    decision="approved",
    feedback="18/20 passed. 2 minor bugs logged. OK to release.",
    decided_by="tester@company.com"
)
```

---

### 5. Deployment Check Tools (4 tools) - Verify After Deploy

**Mục đích**: Kiểm tra deployment status, health check sau khi deploy lên UAT/PROD.

| Tool | Khi Nào Dùng | Performance |
|------|--------------|-------------|
| `check_post_deploy_health` | Verify health sau deploy | MEDIUM (1-2s) |
| `jenkins_get_build_status` | Check build status | MEDIUM (1-2s) |
| `jenkins_get_deployments` | Xem deployment history | MEDIUM (1-2s) |
| `list_pending_reviews` | Xem pending approvals cần QA sign-off | FAST (<1s) |

**Best Practices**:
- Luôn chạy smoke test sau khi deploy lên UAT
- Dùng `check_post_deploy_health` để verify hệ thống ổn định trước khi test
- Check `jenkins_get_build_status` để biết build nào đang chạy trên UAT

**Ví dụ**:
```python
# Check health sau deploy UAT
check_post_deploy_health(
    service="ict-oms-service",
    environment="uat",
    build_number=123,
    issue_key="FI-12345"
)

# Check build status
jenkins_get_build_status(
    job_name="ict-oms-service-ci",
    build_number=123
)

# Xem pending reviews cần QA
list_pending_reviews(reviewer_role="quality_reviewer")
```

---

## 🚫 Công Cụ KHÔNG Ưu Tiên Cho Tester

Tester **KHÔNG NÊN** dùng các công cụ sau trong workflow thông thường.

**LƯU Ý**: Đây là **gợi ý**, không phải **hard block**. Nếu thực sự cần, vẫn có thể dùng với confirmation.

### Dev Tools (không ưu tiên cho Tester)
- `create_feature_branch` - Tạo branch (Dev làm)
- `commit_changes` - Commit code (Dev làm)
- `create_merge_request` - Tạo MR (Dev làm)
- `get_build_context` - Deep code context (Dev làm)
- `trigger_ci_pipeline` - Trigger CI (Dev/Ops làm)

### Ops Tools (không ưu tiên cho Tester)
- `promote_to_prod` - Deploy PROD (Ops làm) - **Exception**: Tester có thể request promote sau khi sign-off
- `rollback` - Rollback (Ops làm)
- `ams_*` tools - AMS incident workflow (Ops làm) - **Exception**: Tester có thể đọc incident reports
- `incident_triage` / `incident_diagnose` - AMS tools (Ops làm)

### BA Tools (không ưu tiên cho Tester)
- `publish_requirements_to_confluence` - Publish requirements (BA làm)
- `publish_design_to_confluence` - Publish design (BA làm)
- `smart_sprint_plan` - Sprint planning (BA/DM làm)
- `rde_analyze_impact` - Impact analysis (BA làm)

---

## 📋 Tester Workflow Checklist

### Phase 1: Prepare
- [ ] Đọc ticket: `jira_get_issue`
- [ ] Lấy full context: `get_work_context`
- [ ] Đọc requirements/design: `get_confluence_page`
- [ ] Tìm existing test cases: `search_context_hybrid`
- [ ] Chuyển ticket sang In Testing: `jira_transition`

### Phase 2: Write Test Plan
- [ ] Viết test plan (với client LLM)
- [ ] Generate test skeleton: `qge_generate_tests`
- [ ] Publish test plan: `rde_publish_to_confluence`

### Phase 3: Execute Tests
- [ ] Check build/deploy status: `jenkins_get_build_status`
- [ ] Verify health: `check_post_deploy_health`
- [ ] Chạy test cases (manual/automation)
- [ ] Log bugs: `jira_create_issue`
- [ ] Update progress: `jira_add_comment`

### Phase 4: Report & Sign-off
- [ ] Publish test report: `publish_test_report`
- [ ] Track QA sign-off: `track_hitl_decision`
- [ ] Chuyển ticket sang Done/Reopen: `jira_transition`

---

## 🎯 Quick Reference - Top 10 Tools Cho Tester

1. **`search_context_hybrid`** ⭐ - Tìm requirements, test cases
2. **`get_work_context`** - Aggregate context cho ticket
3. **`jira_get_issue`** - Đọc ticket cần test
4. **`jira_create_issue`** - Tạo bug report
5. **`jira_add_comment`** - Update test progress
6. **`publish_test_report`** - Publish test report
7. **`qge_generate_tests`** - Generate test skeleton
8. **`check_post_deploy_health`** - Verify sau deploy
9. **`track_hitl_decision`** - QA sign-off
10. **`jira_transition`** - Chuyển trạng thái ticket

---

## 💡 Tips & Tricks

### Tip 1: Tester Test Flow
```
jira_get_issue (đọc ticket) →
get_work_context (hiểu scope) →
get_confluence_page (đọc design) →
[Viết test cases] →
check_post_deploy_health (verify UAT ready) →
[Execute tests] →
jira_create_issue (log bugs) →
publish_test_report (báo cáo) →
track_hitl_decision (sign-off)
```

### Tip 2: Bug Report Template
```python
jira_create_issue(
    project_key="FI",
    summary="[BUG] <Feature> - <Mô tả ngắn>",
    issue_type="Bug",
    description="""
**Steps to Reproduce:**
1. ...
2. ...

**Expected Result:** ...
**Actual Result:** ...

**Severity:** Critical/High/Medium/Low
**Environment:** UAT/PROD
**Build:** #xxx
**Screenshot/Log:** [đính kèm]
    """,
    priority="High",
    labels=["bug", "regression"]
)
```

### Tip 3: Always Filter role="tester"
```
✅ GOOD:
search_context_hybrid("payment test cases", chain_id="ICT", role="tester", doc_type="confluence_ba")

❌ BAD:
search_context_hybrid("payment test cases")  # Trả về cả code, không liên quan
```

### Tip 4: Sign-off Trước Khi Release
```python
# QA sign-off bắt buộc trước khi promote PROD
track_hitl_decision(
    issue_key="FI-12345",
    step="qa_sign_off",
    decision="approved",  # hoặc "rejected"
    feedback="All critical test cases passed. Ready for PROD.",
    decided_by="qa_lead@company.com"
)
```

---

## 📊 Tester Success Metrics

| Metric | Target | Cách Đo |
|--------|--------|---------|
| Test coverage | ≥ 90% | Test cases / Acceptance criteria |
| Bug detection rate | ≥ 85% | Bugs found in QA / Total bugs in PROD |
| Test execution time | ≤ 2 ngày/sprint | Actual vs planned |
| Bug reopen rate | ≤ 10% | Reopened bugs / Total bugs closed |
| Sign-off time | ≤ 4h sau test xong | Time from last test to sign-off |

---

## 🔄 Tester Continuous Improvement

```
Test → Report → Bug → Fix → Retest → Sign-off → Release → Monitor
  ↑                                                            ↓
  └────────────────────────────────────────────────────────────┘
```

1. **Collect**: Thu thập bugs, test results, production incidents
2. **Analyze**: Phân tích root cause của bugs bị miss
3. **Improve**: Cải thiện test cases, automation coverage
4. **Deploy**: Update test suites, regression scripts
5. **Monitor**: Theo dõi bug escape rate, lặp lại cycle

---

**Version**: 1.0
**Last Updated**: 2026-05-11
**Owner**: AI Context Engine Team
