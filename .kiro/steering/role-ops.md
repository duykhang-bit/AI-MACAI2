# Role-Specific Tools: Operations (Ops)

**Mục đích**: Lọc và hiển thị chỉ 40 công cụ MCP phù hợp với vai trò Operations để tránh choáng ngợp bởi 85 công cụ.

**Áp dụng cho**: Users có role = "ops" hoặc "operations" hoặc "devops"

---

## 🎯 Operations Workflow

```
Monitor → Triage → Diagnose → Fix → Deploy → Verify → Post-Mortem
```

Operations cần công cụ để:
1. **Tìm context** - Runbooks, logs, deployments, similar incidents
2. **AMS** - Triage, diagnose, workaround, hotfix, RCA
3. **Ship** - Deploy CI/UAT/PROD, rollback, health check
4. **Jenkins** - Trigger pipelines, analyze logs, track deployments
5. **Jira** - Read/update incidents, transitions

---

## ✅ 40 Công Cụ Cho Operations

### 1. Context Tools (5 tools) - Tìm Runbooks & Logs

**Mục đích**: Tìm runbooks, deployment history, similar incidents.

| Tool | Khi Nào Dùng | Performance |
|------|--------------|-------------|
| `search_context_hybrid` ⭐ | Tìm runbooks, logs với filters | FAST (<1s) |
| `get_confluence_page` | Đọc runbook từ Confluence | FAST (<1s) |
| `trace_source` | Trace source lifecycle | FAST (<1s) |
| `trace_related_sources` | Trace related sources | FAST (<1s) |
| `ship_impact_analysis` | Intra-chain impact analysis | FAST (<1s) |

**Best Practices**:
- Luôn dùng `search_context_hybrid` với `role="ops"` + `doc_type="runbook"`
- Dùng `trace_source` để xem deployment history
- Dùng `ship_impact_analysis` để xem services bị ảnh hưởng

**Ví dụ**:
```python
# Tìm runbook cho payment gateway timeout
search_context_hybrid(
    query="payment gateway timeout troubleshooting",
    chain_id="ICT",
    domain="ict_payment",
    role="ops",
    doc_type="runbook"
)

# Trace deployment history
trace_source(source_id="git:ict-payment-gateway")

# Impact analysis
ship_impact_analysis(
    source_id="git:ict-payment-gateway",
    chain_id="ICT",
    max_depth=2
)
```

---

### 2. AMS Tools (15 tools) - L2 + L3 Incident Management

**Mục đích**: Triage, diagnose, fix incidents (L2 workaround, L3 hotfix).

| Tool | Khi Nào Dùng | Performance |
|------|--------------|-------------|
| `incident_triage` | C1 - Classify incident, suggest severity | SLOW (>5s) ⚠️ |
| `incident_diagnose` | C2 - Aggregate evidence, root cause | SLOW (>10s) ⚠️ |
| `suggest_workaround` | C3 - L2 workaround options | SLOW (>5s) ⚠️ |
| `start_hotfix` | C4 - L3 hotfix branch + MR | SLOW (>10s) ⚠️ |
| `generate_rca_report` | C5 - RCA report + Confluence | SLOW (>10s) ⚠️ |
| `ams_post_triage_gate` | C7 - Post-triage quality gate | MEDIUM (>3s) |
| `ams_pre_hotfix_gate` | C7 - Pre-hotfix quality gate | MEDIUM (>3s) |
| `ams_post_hotfix_gate` | C7 - Post-hotfix quality gate | MEDIUM (>3s) |
| `ams_pre_deploy_gate` | C7 - Pre-deploy quality gate | MEDIUM (>3s) |
| `ams_request_hotfix_review` | C8 - Request hotfix MR review | MEDIUM (>3s) |
| `ams_request_deploy_approval` | C8 - Request deploy approval | MEDIUM (>3s) |
| `ams_request_rca_review` | C8 - Request RCA review | MEDIUM (>3s) |
| `ams_ship_hotfix` | C9 - Ship hotfix CI/UAT/PROD | SLOW (>30s) ⚠️ |
| `ams_transition_incident` | C10 - Transition incident state | MEDIUM (>3s) |
| `ams_escalate_incident` | C11 - Escalate L1→L2→L3→Mgmt | MEDIUM (>3s) |

**Best Practices**:
- Luôn chạy `incident_triage` trước khi diagnose
- Chạy quality gates trước mỗi bước (triage, hotfix, deploy)
- Request HITL approval cho hotfix MR và deploy
- Generate RCA report cho P1/P2 incidents (mandatory)

**Ví dụ**:
```python
# C1: Triage incident
incident_triage(
    issue_key="FI-12345",
    chain_id="ICT"
)

# C2: Diagnose
incident_diagnose(
    issue_key="FI-12345",
    chain_id="ICT"
)

# C3: Suggest L2 workaround
suggest_workaround(
    issue_key="FI-12345",
    diagnosis={...}
)

# C4: Start L3 hotfix
start_hotfix(
    issue_key="FI-12345",
    chain_id="ICT",
    source_id="git:ict-payment-gateway",
    diagnosis={...}
)

# C5: Generate RCA report
generate_rca_report(issue_key="FI-12345")

# C9: Ship hotfix
ams_ship_hotfix(
    issue_key="FI-12345",
    environment="ci",
    build_info={
        "source_id": "git:ict-payment-gateway",
        "branch": "hotfix/FI-12345-payment-timeout"
    }
)
```

---

### 3. Ship Tools (16 tools) - Deployment Pipeline

**Mục đích**: Deploy CI/UAT/PROD, quality gates, rollback, health check.

| Tool | Khi Nào Dùng | Performance |
|------|--------------|-------------|
| `qge_analyze_shape` | Post-Shape quality gate | MEDIUM (1-2s) |
| `qge_pre_ci_gate` | Pre-CI automated gate | MEDIUM (1-2s) |
| `qge_pre_uat_gate` | Pre-UAT quality gate | MEDIUM (1-2s) |
| `qge_pre_prod_gate` | Pre-PROD quality gate | MEDIUM (1-2s) |
| `trigger_ci_pipeline` | Trigger CI pipeline | SLOW (>10s) ⚠️ |
| `promote_to_uat` | Promote CI → UAT | SLOW (>30s) ⚠️ |
| `promote_to_prod` | Promote UAT → PROD | SLOW (>60s) ⚠️ |
| `rollback` | Rollback deployment | SLOW (>10s) ⚠️ |
| `check_post_deploy_health` | Post-deploy health check | MEDIUM (1-2s) |
| `list_pending_reviews` | List pending approvals | FAST (<1s) |
| `notify_reviewer` | Notify reviewer | FAST (<1s) |
| `ship_impact_analysis` | Intra-chain impact | FAST (<1s) |
| `ship_run_eval` | Run eval manifest | MEDIUM (1-3s) |
| `ship_run_pipeline` | Run autonomous pipeline | MEDIUM (1-3s) |
| `ship_multi_deploy` | Multi-service deployment | SLOW (>30s) ⚠️ |
| `ship_learning_insights` | Self-learning insights | FAST (<1s) |

**Best Practices**:
- Luôn chạy quality gates trước mỗi deployment
- Request HITL approval cho UAT/PROD deployments
- Chạy health check sau mỗi deployment
- Rollback ngay nếu health check FAIL

**Ví dụ**:
```python
# Pre-CI gate
qge_pre_ci_gate(
    source_id="git:ict-oms-service",
    branch="features/datnm11-FI-12345-payment",
    chain_id="ICT"
)

# Trigger CI
trigger_ci_pipeline(
    source_id="git:ict-oms-service",
    branch="features/datnm11-FI-12345-payment",
    issue_key="FI-12345"
)

# Pre-UAT gate
qge_pre_uat_gate(
    service="ict-oms-service",
    build_number=123,
    issue_key="FI-12345"
)

# Promote to UAT
promote_to_uat(
    service="ict-oms-service",
    build_number=123,
    issue_key="FI-12345",
    jenkins_deploy_job="ict-oms-deploy-uat"
)

# Health check
check_post_deploy_health(
    service="ict-oms-service",
    environment="uat",
    build_number=123,
    issue_key="FI-12345"
)

# Rollback if needed
rollback(
    service="ict-oms-service",
    environment="uat",
    to_build_number=120,
    issue_key="FI-12345",
    reason="Health check failed"
)
```

---

### 4. Jenkins Tools (4 tools) - CI/CD Operations

**Mục đích**: Trigger pipelines, check build status, analyze logs, track deployments.

| Tool | Khi Nào Dùng | Performance |
|------|--------------|-------------|
| `jenkins_trigger_pipeline` | Trigger Jenkins job | SLOW (>5s) ⚠️ |
| `jenkins_get_build_status` | Check build status | MEDIUM (1-2s) |
| `jenkins_analyze_build_log` | Analyze build log | MEDIUM (1-2s) |
| `jenkins_get_deployments` | Get deployment history | MEDIUM (1-2s) |

**Best Practices**:
- Dùng `jenkins_analyze_build_log` để tìm root cause khi build fail
- Track deployment history bằng `jenkins_get_deployments`

**Ví dụ**:
```python
# Trigger Jenkins job
jenkins_trigger_pipeline(
    job_name="ict-oms-service-ci",
    parameters={"branch": "features/datnm11-FI-12345-payment"}
)

# Check build status
jenkins_get_build_status(
    job_name="ict-oms-service-ci",
    build_number=123
)

# Analyze build log
jenkins_analyze_build_log(
    job_name="ict-oms-service-ci",
    build_number=123
)

# Get deployment history
jenkins_get_deployments(
    service="ict-oms-service",
    environment="prod",
    limit=10
)
```

---

## 🚫 Công Cụ KHÔNG Ưu Tiên Cho Ops

Ops **KHÔNG NÊN** dùng các công cụ sau trong workflow thông thường (dành cho Dev, BA).

**LƯU Ý**: Đây là **gợi ý**, không phải **hard block**. Nếu Ops thực sự cần (ví dụ: hotfix code, tạo incident ticket), vẫn có thể dùng với confirmation.

### Dev Tools (không ưu tiên cho Ops)
- `create_feature_branch` - Tạo branch (Dev làm) - **Exception**: Ops có thể tạo hotfix branch
- `commit_changes` - Commit code (Dev làm) - **Exception**: Ops có thể commit hotfix
- `create_merge_request` - Tạo MR (Dev làm) - **Exception**: Ops có thể tạo hotfix MR
- `get_build_context` - Deep code context (Dev làm) - **Exception**: Ops có thể debug code issues

### BA Tools (không ưu tiên cho Ops)
- `jira_create_from_intent` - Tạo Epic/Story (BA làm) - **Exception**: Ops có thể tạo Incident/Bug
- `publish_requirements_to_confluence` - Publish requirements (BA làm)
- `publish_design_to_confluence` - Publish design (BA làm) - **Exception**: Ops có thể publish runbooks
- `rde_*` tools - Requirements & Design Engine (BA làm)
- `smart_sprint_plan` - Sprint planning (BA/SM làm)

---

## 📋 Ops Workflow Checklist

### AMS L2 Workflow (Workaround)
- [ ] Triage incident: `incident_triage`
- [ ] Post-triage gate: `ams_post_triage_gate`
- [ ] Diagnose: `incident_diagnose`
- [ ] Suggest workaround: `suggest_workaround`
- [ ] Execute workaround (manual)
- [ ] Verify fix
- [ ] Generate RCA (P1/P2): `generate_rca_report`

### AMS L3 Workflow (Hotfix)
- [ ] Triage incident: `incident_triage`
- [ ] Diagnose: `incident_diagnose`
- [ ] Pre-hotfix gate: `ams_pre_hotfix_gate`
- [ ] Start hotfix: `start_hotfix`
- [ ] Post-hotfix gate: `ams_post_hotfix_gate`
- [ ] Request MR review: `ams_request_hotfix_review`
- [ ] Ship hotfix: `ams_ship_hotfix`
- [ ] Generate RCA: `generate_rca_report`

### Ship Workflow (Normal Deployment)
- [ ] Pre-CI gate: `qge_pre_ci_gate`
- [ ] Trigger CI: `trigger_ci_pipeline`
- [ ] Pre-UAT gate: `qge_pre_uat_gate`
- [ ] Promote to UAT: `promote_to_uat`
- [ ] Health check UAT: `check_post_deploy_health`
- [ ] Pre-PROD gate: `qge_pre_prod_gate`
- [ ] Promote to PROD: `promote_to_prod`
- [ ] Health check PROD: `check_post_deploy_health`

---

## 🎯 Quick Reference - Top 10 Tools Cho Ops

1. **`incident_triage`** ⭐ - Triage incident
2. **`incident_diagnose`** - Diagnose root cause
3. **`ams_ship_hotfix`** - Ship hotfix fast-track
4. **`promote_to_prod`** - Deploy PROD
5. **`rollback`** - Rollback deployment
6. **`check_post_deploy_health`** - Health check
7. **`jenkins_analyze_build_log`** - Analyze build log
8. **`search_context_hybrid`** - Tìm runbooks
9. **`ship_impact_analysis`** - Impact analysis
10. **`generate_rca_report`** - RCA report

---

## 💡 Tips & Tricks

### Tip 1: AMS Fast-Track for P1/P2
```
P1/P2 incidents:
- Reduced soak times (CI: 5min, UAT: 15min, PROD: 30min)
- Fast-track HITL approval (4h expiry)
- Mandatory RCA report
- Auto-escalate if not resolved in threshold time
```

### Tip 2: Always Run Quality Gates
```python
# Pre-deploy gate cho mỗi environment
ams_pre_deploy_gate(
    issue_key="FI-12345",
    environment="prod",
    build_info={...}
)
```

### Tip 3: Health Check After Every Deploy
```python
# Health check sau deploy
result = check_post_deploy_health(
    service="ict-oms-service",
    environment="prod",
    build_number=123,
    issue_key="FI-12345"
)

# Auto-rollback nếu FAIL
if result["overall"] == "FAIL":
    rollback(
        service="ict-oms-service",
        environment="prod",
        to_build_number=120,
        issue_key="FI-12345",
        reason="Health check failed"
    )
```

### Tip 4: Search Runbooks First
```python
# Tìm runbook trước khi troubleshoot
search_context_hybrid(
    query="payment gateway timeout troubleshooting",
    chain_id="ICT",
    role="ops",
    doc_type="runbook"
)
```

---

## 📊 Ops Success Metrics

| Metric | Target | Cách Đo |
|--------|--------|---------|
| MTTR (Mean Time To Resolve) | ≤ 2h (P1), ≤ 4h (P2) | Time from triage to resolved |
| Deployment success rate | ≥ 95% | Successful deploys / Total deploys |
| Rollback rate | ≤ 5% | Rollbacks / Total deploys |
| Health check pass rate | ≥ 99% | Health checks passed / Total |
| RCA completion rate | 100% (P1/P2) | RCAs completed / P1/P2 incidents |

---

## 🚨 Incident Severity & SLA

| Severity | Description | MTTR Target | Escalation Threshold |
|----------|-------------|-------------|---------------------|
| **P1** | Production down, revenue impact | ≤ 2h | 15 min |
| **P2** | Major feature broken, workaround exists | ≤ 4h | 30 min |
| **P3** | Minor feature broken, low impact | ≤ 8h | 2h |
| **P4** | Cosmetic issue, no impact | ≤ 24h | 8h |

---

## 🔄 Ops Continuous Improvement

```
Incident → Triage → Diagnose → Fix → Deploy → Verify → RCA → Learn
     ↑                                                            ↓
     └────────────────────────────────────────────────────────────┘
```

1. **Collect**: Thu thập incidents, deployments, health checks
2. **Analyze**: Phân tích root causes, patterns, trends
3. **Improve**: Cải thiện runbooks, automation, monitoring
4. **Deploy**: Update runbooks, deploy improvements
5. **Monitor**: Theo dõi metrics, lặp lại cycle

---

## 🎓 Ops Best Practices

### 1. Always Have Rollback Plan
- Verify rollback target exists before deploy
- Test rollback in UAT first
- Document rollback steps in runbook

### 2. Monitor Everything
- APM metrics (latency, error rate, throughput)
- Infrastructure metrics (CPU, memory, disk)
- Business metrics (orders, payments, bookings)

### 3. Automate Repetitive Tasks
- Use `ship_run_pipeline` for autonomous workflows
- Use `ship_multi_deploy` for multi-service deployments
- Use `ship_learning_insights` to learn from patterns

### 4. Document Everything
- Update runbooks after every incident
- Generate RCA for P1/P2 incidents
- Share learnings with team

---

**Version**: 1.0  
**Last Updated**: 2026-04-16  
**Owner**: AI Context Engine Team
