<!-- AI Context Client: role-ops part 2/2 — enable all parts Always On. -->

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
