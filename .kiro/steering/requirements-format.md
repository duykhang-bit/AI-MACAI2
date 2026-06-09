---
inclusion: auto
---

# Requirements Document Format

Khi sinh requirements document (cho Shape flow hoặc Kiro spec), PHẢI tuân theo format sau.

## Cấu trúc bắt buộc

```markdown
# Requirements Document

## Introduction
- Mô tả bối cảnh, vấn đề cần giải quyết
- Phụ thuộc (dependencies) với modules/specs khác
- Nguyên tắc thiết kế (design principles)

## Glossary
- Định nghĩa tất cả thuật ngữ, viết tắt, tên module, tên class
- Format: **Term**: Mô tả ngắn gọn, chính xác
- Bao gồm: tên file, tên class, tên tool, tên config, tên concept

## Requirements
### Requirement N: Tên requirement

**User Story:** Là một [role], tôi muốn [action], để [benefit].

#### Acceptance Criteria
1. WHEN [condition], THE [component] SHALL [behavior].
2. IF [condition], THEN THE [component] SHALL [behavior].
```

## EARS Format (Easy Approach to Requirements Syntax)

Dùng EARS keywords cho acceptance criteria:

| Keyword | Dùng khi | Ví dụ |
|---------|----------|-------|
| WHEN | Event-driven behavior | WHEN user clicks submit, THE system SHALL validate input |
| IF...THEN | Conditional behavior | IF input is empty, THEN THE system SHALL return error |
| SHALL | Bắt buộc | THE API SHALL return HTTP 200 on success |
| SHALL NOT | Cấm | THE system SHALL NOT expose internal errors to client |
| WHILE | State-driven | WHILE processing, THE UI SHALL show loading indicator |

## Quy tắc

1. Mỗi requirement có User Story + Acceptance Criteria
2. Acceptance criteria dùng EARS format, đánh số thứ tự
3. Mỗi criterion phải testable — có thể viết test case verify được
4. Dùng tên component/module cụ thể (không dùng "the system" chung chung)
5. Glossary phải đầy đủ — reader không cần đoán nghĩa
6. Nếu có error handling → viết requirement riêng cho error cases
7. Nếu có config → viết requirement cho config fields (tên env var, default value)
8. Cross-reference giữa requirements bằng số (Requirement 1, Requirement 2...)

## Ví dụ tham khảo

Dưới đây là ví dụ rút gọn từ các spec đã hoàn thành trong dự án AI Context Engine.

### Ví dụ 1: Requirement cho MCP tool (quality gate)

```markdown
### Requirement 1: Post-Triage Quality Gate

**User Story:** As a System_Operator, I want the system to validate triage
completeness before I proceed to diagnosis, so that I can be confident all
necessary triage data has been collected.

#### Acceptance Criteria

1. WHEN `ams_post_triage_gate(issue_key, chain_id)` is called with a valid
   issue_key and chain_id, THE AMS_Gate_Engine SHALL return a Gate_Result
   containing overall status (PASS/WARN/FAIL), a checklist of validation
   items, and any blocking items.
2. THE AMS_Gate_Engine SHALL validate the following checklist items:
   severity assigned (P1–P4), affected services identified, similar
   incidents searched, L2/L3 recommendation made, triage comment exists.
3. IF any checklist item has status "fail", THEN THE AMS_Gate_Engine SHALL
   set the overall gate status to FAIL.
4. IF issue_key or chain_id is empty, THEN THE AMS_Gate_Engine SHALL return
   a validation error without performing any gate checks.
5. THE AMS_Gate_Engine SHALL record an audit log entry with trace_id.
```

### Ví dụ 2: Requirement cho state machine

```markdown
### Requirement 5: State Transition with Gate Prerequisites

**User Story:** As a System_Operator, I want the state machine to block
transitions when required quality gates have not passed, so that I cannot
skip mandatory quality checks.

#### Acceptance Criteria

1. WHEN `ams_transition_incident(issue_key, target_state)` is called,
   THE AMS_State_Machine SHALL validate that the transition is allowed.
2. THE AMS_State_Machine SHALL check gate prerequisites: Triaged→Diagnosing
   requires Post_Triage_Gate PASS or WARN, Diagnosing→Fixing requires
   Pre_Hotfix_Gate PASS or WARN.
3. IF a required gate has status FAIL or not found, THEN THE AMS_State_Machine
   SHALL reject the transition with a message identifying the blocking gate.
4. THE AMS_State_Machine SHALL record an audit log entry with trace_id.
```

### Ví dụ 3: Glossary entry

```markdown
## Glossary

- **AMS_Gate_Engine**: Quality gate subsystem validating completeness at
  each transition point (C7)
- **Fast_Track**: Reduced approval expiry for P1/P2 incidents (4h instead
  of 24h) — approval still mandatory, only timeline compressed
- **chain_id**: Business chain identifier (VAC, LAB, PHARMACY, ICT, BO,
  PLATFORM, DATA, DS), mandatory for all context operations
- **trace_id**: Unique identifier for end-to-end traceability via audit log
```
