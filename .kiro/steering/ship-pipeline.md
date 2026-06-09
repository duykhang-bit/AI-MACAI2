---
inclusion: auto
---

# Ship Pipeline — Hướng dẫn cho Kiro/Cursor orchestrate CI→UAT→PROD

Steering file này hướng dẫn AI agent (Kiro/Cursor) cách orchestrate ship pipeline flow. Khi developer yêu cầu deploy, ship, promote, hoặc rollback — follow flow này.

## Pipeline Flow Sequence

```
qge_analyze_shape → Build → qge_pre_ci_gate → trigger_ci_pipeline
  → qge_pre_uat_gate → promote_to_uat → qge_pre_prod_gate
  → promote_to_prod → check_post_deploy_health
```

## Step-by-Step Guide

### Step 1: Post-Shape Quality Gate (sau khi có requirements + design)

```
Tool: qge_analyze_shape(requirements_doc, design_doc, chain_id?)
```

- Kiểm tra requirements completeness, design feasibility, traceability
- Khi có `chain_id`: kiểm tra domain compliance (Luật Dược, PCI-DSS, etc.)
- Đọc `overall`, `requirements_findings`, `design_findings`, `compliance_findings`
- Nếu FAIL: báo developer sửa trước khi build
- Nếu PASS/WARN: tiếp tục build

### Step 2: Pre-CI Gate (trước khi trigger CI)

```
Tool: qge_pre_ci_gate(source_id, branch, chain_id?)
```

- Automated — không cần human approval
- Kiểm tra: unit tests pass, lint clean, no secrets
- Khi có `chain_id`: thêm intra-chain impact analysis
- Nếu `approved=true`: tiếp trigger CI
- Nếu `approved=false`: báo developer fix issues trước

### Step 3: Trigger CI Pipeline

```
Tool: trigger_ci_pipeline(source_id, branch, issue_key, jenkins_job?)
```

- Trigger Jenkins build
- Tự động derive job name nếu không truyền `jenkins_job`
- Comment Jira với build link
- Chờ build hoàn thành trước khi tiếp

### Step 4: Pre-UAT Gate (trước khi promote UAT)

```
Tool: qge_pre_uat_gate(service, build_number, issue_key)
```

- Kiểm tra CI deployment success, no CRITICAL findings
- **Luôn tạo HITL approval** cho Quality Reviewer
- Đọc `approval_id` từ response
- Thông báo reviewer: `notify_reviewer(approval_id, "jira")`
- **Chờ approval** trước khi promote

### Step 5: Promote to UAT

```
Tool: promote_to_uat(service, build_number, issue_key, jenkins_deploy_job, approval_id)
```

- Truyền `approval_id` từ Step 4
- Nếu chưa approved: trả error `approval_required`
- Nếu approved: delegate to UAT promoter, comment Jira

### Step 6: Pre-PROD Gate (trước khi promote PROD)

```
Tool: qge_pre_prod_gate(service, build_number, issue_key, jenkins_deploy_job?)
```

- Full regression, soak time, rollback readiness
- **Tạo HITL approval** cho Quality Reviewer + System Operator
- Đọc `approval_id`, thông báo reviewers
- **Chờ approval** trước khi promote

### Step 7: Promote to PROD

```
Tool: promote_to_prod(service, build_number, issue_key, jenkins_deploy_job, approval_id)
```

- `approval_id` bắt buộc — không có thì trả error
- Trigger Jenkins deploy PROD
- Comment + transition Jira

### Step 8: Post-Deploy Health Check

```
Tool: check_post_deploy_health(service, environment, build_number, issue_key)
```

- So sánh metrics vs baseline
- CI/UAT: auto-rollback nếu critical
- PROD: tạo HITL approval cho rollback — **không bao giờ auto-rollback PROD**

## Rollback

```
Tool: rollback(service, environment, to_build_number, issue_key, reason?)
```

- PROD: cần HITL approval
- CI/UAT: automated
- Verify rollback target exists trước khi execute

## Impact Analysis (brownfield)

```
Tool: ship_impact_analysis(source_id, chain_id, max_depth?)
```

- Intra-chain first — tìm affected repos/services trong cùng chain
- Dùng trước khi deploy để biết scope ảnh hưởng
- Best-effort — không block nếu ArangoDB chưa configured

## Reviewer Notification

```
Tool: notify_reviewer(approval_id, "jira")  # hoặc "webhook"
Tool: list_pending_reviews(reviewer_role)
```

- Gửi notification khi có pending approval
- Reviewer roles: `intent_owner`, `solution_reviewer`, `quality_reviewer`, `system_operator`

## Multi-Service Deployment

```
Tool: ship_multi_deploy(services, issue_key, jenkins_deploy_job_template, dry_run?)
```

- Deploy nhiều services theo dependency order (topological sort)
- `dry_run=true` để preview order trước khi execute
- Fail-fast: nếu service A fail, skip dependent services

## Eval Harness

```
Tool: ship_run_eval(manifest_path, context, issue_key?)
```

- Chạy eval manifest YAML để verify gate results
- Track pass@k metrics theo thời gian
- Evidence trail cho compliance audits

## Autonomous Pipeline

```
Tool: ship_run_pipeline(pipeline_path, context)
```

- Chạy pipeline steps từ YAML definition
- Fail-fast: dừng ngay khi step fail
- Template variables: `{{source_id}}`, `{{branch}}`, `{{issue_key}}`

## Gate Summary

| Gate | Automated? | HITL Required? | Who Approves? |
|------|-----------|----------------|---------------|
| Post-Shape | AI check 80% | Human review findings | Intent Owner + Solution Reviewer |
| Pre-CI | 100% automated | No | — |
| Pre-UAT | AI check 90% | Yes, always | Quality Reviewer |
| Pre-PROD | AI check 80% | Yes, always | Quality Reviewer + System Operator |
| Post-Deploy | AI detect anomaly | PROD rollback only | System Operator |
