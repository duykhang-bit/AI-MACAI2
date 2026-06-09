<!-- AI Context Client: role-tester part 2/2 — enable all parts Always On. -->

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
