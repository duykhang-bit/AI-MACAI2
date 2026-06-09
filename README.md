# AI Native Client — Hướng dẫn sử dụng AI Context Engine

Repo này giúp toàn thể anh em cấu hình IDE (Kiro, Cursor, Antigravity) kết nối vào **AI Context Engine MCP server** của công ty — bộ não trung tâm của AI-Native SDLC pipeline.

Sau khi kết nối, bạn có thể dùng AI agent để: tìm code/docs, sinh requirements & design, tạo branch & MR, chạy quality gate, lên kế hoạch sprint — tất cả qua natural language.

## Mục lục

- [Quick Start (4 bước)](#quick-start-4-bước)
- [Workflow tổng quan](#workflow-tổng-quan)
- [Flow 1 — Plan: Sprint Planning](#flow-1--plan-sprint-planning)
- [Flow 2 — Shape: Sinh Requirements & Design](#flow-2--shape-sinh-requirements--design)
- [Flow 3 — Build: Code → Branch → MR](#flow-3--build-code--branch--mr)
- [Flow 4 — Quality & Deploy](#flow-4--quality--deploy)
- [Flow 5 — Ship: CI → UAT → PROD Pipeline](#flow-5--ship-ci--uat--prod-pipeline)
- [Flow 6 — Tìm kiếm Context](#flow-6--tìm-kiếm-context)
- [Flow 7 — AMS: Incident Resolution (L2 + L3)](#flow-7--ams-incident-resolution-l2--l3)
- [Danh sách MCP Tools](#danh-sách-mcp-tools)
- [Steering — Kiro, Cursor, Antigravity](#steering--kiro-cursor-antigravity)
- [Hướng dẫn lấy Tokens](#hướng-dẫn-lấy-tokens)
- [Lỗi thường gặp](#lỗi-thường-gặp)
- [Bảo mật](#bảo-mật)

---

## Quick Start (4 bước)

> **Cách nhanh nhất**: Chạy script cài đặt — tự động tạo MCP config + copy steering files vào đúng chỗ. Sau đó chỉ cần điền tokens.
>
> | OS | Lệnh |
> |-----|------|
> | **macOS / Linux** | `bash install.sh` |
> | **Windows (PowerShell)** | `powershell -ExecutionPolicy Bypass -File install.ps1` |
> | **Windows (Git Bash)** | `bash install.sh` |

### Bước 1: Lấy tokens

| Token | Lấy ở đâu | Scope |
|-------|-----------|-------|
| `X-API-Key` | DevOps cung cấp | — |
| `X-GitLab-Token` | GitLab → Settings → Access Tokens | `api` |
| `X-Confluence-Token` | Confluence → Profile → Personal Access Tokens | — |
| `X-Jira-Token` | Jira → Profile → Personal Access Tokens | — |

### Bước 2: Cấu hình MCP Server (global — dùng cho mọi workspace)

> **Quan trọng**: Cấu hình ở **user-level (global)** để MCP server hoạt động trong mọi workspace/repo bạn mở, không chỉ repo này.

**Kiro** — tạo file MCP config:

| OS | Đường dẫn |
|----|-----------|
| macOS / Linux | `~/.kiro/settings/mcp.json` |
| Windows | `%USERPROFILE%\.kiro\settings\mcp.json` |

```bash
# macOS / Linux
mkdir -p ~/.kiro/settings
cp mcp.kiro.example.json ~/.kiro/settings/mcp.json
```

Mở `~/.kiro/settings/mcp.json` và điền tokens:

```json
{
  "mcpServers": {
    "ai-context-engine-company": {
      "url": "https://ai-context-mcp.frt.vn/mcp",
      "headers": {
        "X-API-Key": "<api-key-từ-devops>",
        "X-Member-Id": "<email-của-bạn>",
        "X-GitLab-Token": "<gitlab-pat>",
        "X-Confluence-Token": "<confluence-pat>",
        "X-Confluence-Username": "<confluence-username>",
        "X-Jira-Token": "<jira-pat>"
      }
    }
  }
}
```

**Cursor** — tạo file MCP config (cùng format):

```bash
# macOS / Linux
mkdir -p ~/.cursor
cp mcp.cursor.example.json ~/.cursor/mcp.json
```

```powershell
# Windows (PowerShell) — hoặc dùng install.ps1 tự động
mkdir -Force "$env:USERPROFILE\.cursor" | Out-Null
copy mcp.cursor.example.json "$env:USERPROFILE\.cursor\mcp.json"
```

**Antigravity** — xem `mcp.antigravity.example.json`, cấu hình theo hướng dẫn của Antigravity.

> **Lưu ý**: Nếu bạn đã có file config sẵn ở đường dẫn trên, chỉ cần thêm block `"ai-context-engine-company"` vào `mcpServers` — không ghi đè file cũ.

### Bước 3: Cài Steering Files (global — dùng cho mọi workspace)

> Steering files hướng dẫn AI agent tuân theo chuẩn format và workflow của công ty. Cài ở **user-level** để áp dụng cho mọi workspace.

```bash
# macOS / Linux
mkdir -p ~/.kiro/steering
cp .kiro/steering/*.md ~/.kiro/steering/
```

```powershell
# Windows (PowerShell) — hoặc dùng install.ps1 tự động
mkdir -Force "$env:USERPROFILE\.kiro\steering" | Out-Null
copy ".kiro\steering\*.md" "$env:USERPROFILE\.kiro\steering\"
```

Sau khi copy, bạn sẽ có:

```
# macOS / Linux: ~/.kiro/
# Windows: %USERPROFILE%\.kiro\
├── settings/
│   └── mcp.json              # MCP config (global)
└── steering/
    └── *.md                   # Đủ 13 chủ đề (xem mục Steering ở cuối README)
```

**Cursor:** `install.sh` / `install.ps1` cũng copy `.cursor/rules/*.mdc` → `~/.cursor/rules/`.

**Antigravity:** dùng `.agents/rules/` trong repo (workspace rules).

> **Khi team cập nhật steering:** sửa `.kiro/steering/` rồi **cập nhật tay** `.cursor/rules/*.mdc` và `.agents/rules/*.md` cho khớp (nếu dùng hai IDE đó), sau đó `git pull` + `install.sh` / `install.ps1` để copy ra `~/.kiro/steering/` và `~/.cursor/rules/`.

### Bước 4: Verify kết nối

Restart IDE, kiểm tra MCP connected, gọi thử:

```
Tìm code liên quan đến payment flow trong chain VAC
```

Nếu AI trả về kết quả code + docs → kết nối thành công.

---

## Workflow tổng quan

AI Context Engine hỗ trợ toàn bộ vòng đời phát triển phần mềm (AI-Native SDLC):

```
Plan → Intent → Understand → Shape → Review → Build → Verify → Ship → Monitor
                                                                  ↓ (incident)
                                              Triage → Diagnose → Fix → Ship Fix → RCA
```

Mỗi bước có MCP tools tương ứng. Bạn chỉ cần nói yêu cầu bằng ngôn ngữ tự nhiên, AI agent sẽ gọi đúng tools.

### Nguyên tắc quan trọng

1. **Human-in-the-Loop (HITL)**: AI luôn dừng lại hỏi bạn confirm trước khi publish tài liệu hoặc tạo MR. Bạn là người quyết định, AI là người thực thi.
2. **Jira + Confluence là source of truth**: Mọi output AI sinh ra đều publish ngược lại Jira/Confluence — không chỉ lưu local.
3. **chain_id bắt buộc**: Hệ thống phục vụ nhiều chuỗi nghiệp vụ (VAC, LAB, ICT, PHARMACY, BO, PLATFORM, DATA, DS). Khi làm việc, luôn chỉ rõ chain.

### Các chain nghiệp vụ

| Chain | Mô tả | Jira Projects |
|-------|--------|---------------|
| **VAC** | Tiêm chủng Vaccine | FV, VADS, VCR |
| **LAB** | Xét nghiệm | FH, LABCR, LR |
| **PHARMACY** | Nhà thuốc | PHAR1, PHAR2 |
| **ICT** | FPTShop / Điện máy | I25, WF2, FI, AFSP, DMP |
| **BO** | Back Office | GIB, BO |
| **PLATFORM** | CDP / Loyalty / Notification | C26 |
| **DATA** | Data Engineering | DATA |
| **DS** | Digital Space | DSP, DSIP, DSCR |

---

## Flow 1 — Plan: Sprint Planning

> Khi bạn cần lên kế hoạch sprint, ước lượng effort, phân loại backlog.

### Khi nào dùng

- Đầu sprint, cần plan tasks cho team
- Cần AI estimate effort cho từng ticket
- Team chưa estimate story points — AI tự phân tích và đề xuất

### Cách dùng

**Cách 1 — Xem velocity team:**

```
Xem velocity của team chain VAC trong 3 sprints gần nhất
```

AI gọi `get_team_velocity("VAC")` → trả về:
- Velocity trung bình: 45 SP/sprint
- Trend: ổn định
- Committed vs Completed ratio

**Cách 2 — AI estimate từng ticket:**

```
Estimate effort cho ticket FV-23829, chain VAC
```

AI gọi `estimate_ticket("FV-23829", "VAC")` → trả về context cho AI phân tích:
- Ticket details + related code + similar past tickets
- AI đề xuất: Size M, ~5 SP, complexity medium, risk low

**Cách 3 — Phân loại backlog:**

```
Phân loại backlog của chain VAC
```

AI gọi `classify_backlog("VAC")` → phân loại theo:
- Feature area, dependency group, urgency, business impact

**Cách 4 — Đề xuất sprint plan:**

```
Đề xuất sprint plan cho chain VAC, sprint "Sprint 15", capacity 10 ngày
```

AI gọi `smart_sprint_plan("VAC", "Sprint 15", 10)` → đề xuất:
- Items vào sprint (tổng effort ≤ capacity)
- P1/P2 bugs ưu tiên cao nhất
- Carry-over từ sprint trước
- Items overflow (không đủ capacity)
- Risk flags

**Cách 5 — Publish plan lên Confluence:**

```
Publish sprint plan lên Confluence
```

AI gọi `publish_plan_to_confluence` → tạo Confluence page + link Jira.

### Ví dụ thực tế — Full Planning Flow

**Bạn:** `Lên plan sprint 15 cho chain VAC, team có 10 ngày capacity`

**AI:**
1. Gọi `get_team_velocity("VAC")` → velocity 45 SP, trend ổn định
2. Gọi `classify_backlog("VAC")` → 23 tickets trong backlog
3. Gọi `smart_sprint_plan("VAC", "Sprint 15", 10)` → đề xuất:

> **Sprint 15 Plan:**
> - 🔴 P1 Bug: FV-24001 — Login crash on Android (3 SP)
> - 🟡 Carry-over: FV-23829 — Reminder schedule (5 SP)
> - 🟢 New: FV-24010 — Export report PDF (5 SP)
> - 🟢 New: FV-24015 — Notification preferences (3 SP)
> - ⚙️ Tech debt: FV-24020 — Upgrade NestJS v10 (5 SP)
> - **Tổng: 21 SP** (capacity 10 ngày ≈ 20-25 SP)
> - **Overflow:** FV-24025, FV-24030 (chuyển sprint sau)
>
> "Plan này OK không? Muốn điều chỉnh gì không?"

**Bạn:** `OK, publish lên Confluence đi`

**AI:** Gọi `publish_plan_to_confluence` → done.

### Tools liên quan

| Tool | Vai trò |
|------|---------|
| `get_team_velocity` | Xem velocity lịch sử của team |
| `estimate_ticket` | AI estimate effort cho từng ticket |
| `classify_backlog` | Phân loại backlog theo feature area, urgency |
| `smart_sprint_plan` | Đề xuất sprint plan theo capacity |
| `publish_plan_to_confluence` | Publish plan lên Confluence |

---

## Flow 2 — Shape: Sinh Requirements & Design

> Khi bạn nhận một Jira ticket và cần sinh tài liệu requirements + design trước khi code.

### Khi nào dùng

- Nhận ticket mới cần phân tích trước khi code
- Cần sinh BRD/PRD/HLD/LLD cho feature
- Cần publish tài liệu lên Confluence và link vào Jira

### Cách dùng

Nói với AI agent:

```
Thực hiện ticket FV-23829 cho chain VAC
```

AI sẽ tự động chạy Shape Flow với 4 checkpoint dừng lại hỏi bạn:

```
Phase 1: Thu thập context
  ├── jira_get_issue("FV-23829")           → đọc ticket
  ├── get_work_context("FV-23829", "VAC")  → thu thập Jira + Confluence + Git context
  └── get_domain_compliance_rules("VAC")   → lấy compliance rules
  ⏸️ CHECKPOINT 1: "Context đã đủ chưa?"

Phase 2: Sinh Requirements
  └── AI sinh requirements (EARS format, user stories, acceptance criteria)
  ⏸️ CHECKPOINT 2: "Requirements OK chưa?"
  └── publish_requirements_to_confluence("FV-23829", content)

Phase 3: Sinh Design
  └── AI sinh design (Mermaid diagrams, data models, API specs)
  ⏸️ CHECKPOINT 3: "Design OK chưa?"
  └── publish_design_to_confluence("FV-23829", content)

Phase 4: Finalize
  ├── jira_add_comment("FV-23829", links)  → comment links lên Jira
  └── jira_transition("FV-23829", status)  → chuyển trạng thái
  ⏸️ CHECKPOINT 4: "Shape hoàn thành. Tiếp tục?"
```

### Ví dụ thực tế

**Bạn:** `Thực hiện ticket FV-23829 cho chain VAC`

**AI:** Thu thập context xong, trình bày:
- Ticket: FV-23829 — Thêm tính năng quản lý lịch tiêm nhắc
- Confluence docs liên quan: [Vaccine Schedule Design](link), [API Spec v2](link)
- Code liên quan: `vaccine-schedule-be/src/modules/reminder/...`
- Compliance: HIPAA data handling, audit logging required

> "Context đã đủ chưa? Bạn muốn bổ sung thêm gì không?"

**Bạn:** `OK, đủ rồi`

**AI:** Sinh requirements document... trình bày cho bạn review.

> "Requirements đã OK chưa? Cần chỉnh sửa gì không?"

**Bạn:** `Thêm user story cho trường hợp hủy lịch nhắc`

**AI:** Cập nhật requirements, trình bày lại.

**Bạn:** `OK`

**AI:** Publish requirements lên Confluence → sinh design → trình bày → bạn confirm → publish design → comment Jira → done.

### Tools liên quan

| Tool | Vai trò |
|------|---------|
| `jira_get_issue` | Đọc chi tiết Jira ticket |
| `get_work_context` | Thu thập context từ Jira + Confluence + Git |
| `get_domain_compliance_rules` | Lấy compliance rules theo chain |
| `publish_requirements_to_confluence` | Publish requirements lên Confluence |
| `publish_design_to_confluence` | Publish design lên Confluence |
| `jira_add_comment` | Comment links lên Jira ticket |
| `jira_transition` | Chuyển trạng thái Jira ticket |
| `track_hitl_decision` | Ghi lại quyết định HITL vào audit log |

---

## Flow 3 — Build: Code → Branch → MR

> Khi bạn đã có requirements + design (sau Shape) và cần bắt đầu code.

### Khi nào dùng

- Đã hoàn thành Shape (có requirements + design trên Confluence)
- Cần AI hỗ trợ hiểu code hiện tại, tìm chỗ cần sửa
- Cần tạo branch, commit code, tạo MR trên GitLab

### Cách dùng

**Bước 1 — Hiểu code hiện tại (Brownfield):**

```
Phân tích code context cho ticket FV-23829, repo vaccine-schedule-be, chain VAC
```

AI sẽ gọi:
```
get_build_context("FV-23829", source_id, design_doc)
```

Trả về:
- Files liên quan cần sửa (với nội dung thực tế từ repo)
- Change points: chỗ nào cần thêm/sửa code
- Patterns hiện có trong repo (naming, imports, framework conventions)

**Bước 2 — Tạo branch:**

```
Tạo feature branch cho ticket FV-23829
```

AI sẽ gọi:
```
create_feature_branch(source_id, "FV-23829", "reminder-schedule-management")
```

Tạo branch: `features/<tên-bạn>-FV-23829-reminder-schedule-management`

**Bước 3 — Commit code:**

Sau khi bạn và AI cùng viết code xong:

```
Commit code lên branch
```

AI sẽ gọi:
```
commit_changes(source_id, branch_name, files=[
  {"file_path": "src/modules/reminder/reminder.service.ts", "content": "...", "action": "update"},
  {"file_path": "src/modules/reminder/dto/create-reminder.dto.ts", "content": "...", "action": "create"}
])
```

**Bước 4 — Tạo MR:**

```
Tạo merge request
```

AI sẽ gọi:
```
create_merge_request(source_id, branch_name, "FV-23829")
```

Tự động: tạo MR trên GitLab + comment MR link lên Jira ticket.

### Ví dụ thực tế — Full Build Flow

**Bạn:** `Tôi cần implement ticket FV-23829, repo vaccine-schedule-be, chain VAC. Đã có design trên Confluence rồi.`

**AI:** Gọi `get_build_context` → phân tích:
- Repo: NestJS, 45 modules
- Files cần sửa: `reminder.module.ts`, `reminder.service.ts`, `reminder.controller.ts`
- Pattern: dùng TypeORM, DTO validation với class-validator
- Dependencies: `schedule.service.ts` (cần import), `notification.service.ts` (cần gọi)

> "Tôi đã phân tích code context. Bạn muốn tạo branch và bắt đầu code không?"

**Bạn:** `OK, tạo branch đi`

**AI:** Gọi `create_feature_branch` → branch `features/datnm11-FV-23829-reminder-schedule` đã tạo.

*(Bạn và AI cùng viết code...)*

**Bạn:** `Code xong rồi, commit và tạo MR đi`

**AI:** Gọi `commit_changes` → `create_merge_request` → MR đã tạo, link đã comment lên Jira.

### Tools liên quan

| Tool | Vai trò |
|------|---------|
| `get_build_context` | Phân tích code context sâu cho brownfield development |
| `create_feature_branch` | Tạo feature branch trên GitLab |
| `commit_changes` | Commit code changes lên branch |
| `create_merge_request` | Tạo MR + link Jira |

---

## Flow 4 — Quality & Deploy

> Khi code đã xong, cần chạy quality gate và deploy.

### Khi nào dùng

- MR đã tạo, cần chạy quality check
- Cần sinh test skeleton cho code mới
- Cần publish test report lên Confluence
- Cần request approval để deploy UAT/PROD

### Cách dùng

**Chạy quality gate trên diff:**

```
Chạy quality gate cho diff của MR này
```

AI gọi `qge_analyze(diff)` → chạy 8 gates song song:
- Code quality, security, secret detection, dependency, test coverage, performance, API contract, regression

**Sinh test skeleton:**

```
Sinh test cho code mới trong diff
```

AI gọi `qge_generate_tests(diff)` → sinh pytest skeleton.

**Publish test report:**

```
Publish test report cho ticket FV-23829
```

AI gọi `publish_test_report("FV-23829", test_results, gate_results)`:
- Tạo Confluence page với test results + quality gate results
- Comment link lên Jira
- Nếu có CRITICAL findings → tạo approval request cho Quality Reviewer

**Request deploy approval:**

```
Request approval deploy UAT cho ticket FV-23829
```

AI gọi `request_deploy_approval("FV-23829", "uat", ...)`:
- Tổng hợp checklist: CI status, quality gate, test coverage
- Tạo HITL approval request
- Comment lên Jira với deploy request details

### Ví dụ thực tế

**Bạn:** `Code xong rồi, chạy quality gate và publish test report cho FV-23829`

**AI:**
1. Gọi `qge_analyze(diff)` → 8/8 gates passed ✅
2. Gọi `qge_generate_tests(diff)` → sinh 12 test cases
3. Gọi `publish_test_report("FV-23829", ...)` → Confluence page đã tạo

> "Quality gate passed. Test report đã publish: [link]. Bạn muốn request deploy UAT không?"

**Bạn:** `OK, request deploy UAT`

**AI:** Gọi `request_deploy_approval("FV-23829", "uat", ...)` → approval request đã tạo, đang chờ Intent Owner approve.

### Tools liên quan

| Tool | Vai trò |
|------|---------|
| `qge_analyze` | Chạy 8 quality gates trên code diff |
| `qge_generate_tests` | Sinh pytest skeleton từ diff |
| `publish_test_report` | Publish test report lên Confluence + link Jira |
| `request_deploy_approval` | Request approval deploy UAT/PROD |
| `request_shape_review` | Request review tài liệu (business hoặc technical) |

---

## Flow 5 — Ship: CI → UAT → PROD Pipeline

> Khi code đã pass quality gate và cần ship qua CI → UAT → PROD với quality gates và HITL approval ở mỗi bước.

### Khi nào dùng

- Code đã merge, cần trigger CI build
- CI build xong, cần promote lên UAT
- UAT test xong, cần promote lên PROD
- Cần rollback khi phát hiện lỗi sau deploy
- Cần kiểm tra impact trước khi deploy (brownfield)

### Pipeline Flow

```
qge_pre_ci_gate → trigger_ci_pipeline → qge_pre_uat_gate → promote_to_uat
  → qge_pre_prod_gate → promote_to_prod → check_post_deploy_health
```

### Cách dùng

**Bước 1 — Pre-CI Gate (automated, không cần approve):**

```
Chạy pre-CI gate cho repo vaccine-schedule-be, branch features/datnm11-FV-23829
```

AI gọi `qge_pre_ci_gate(source_id, branch, chain_id="VAC")`:
- Kiểm tra unit tests pass, lint clean, no secrets
- Phân tích impact trong chain VAC (bao nhiêu services bị ảnh hưởng)
- Nếu `approved=true` → sẵn sàng trigger CI

**Bước 2 — Trigger CI:**

```
Trigger CI build
```

AI gọi `trigger_ci_pipeline(source_id, branch, "FV-23829")`:
- Trigger Jenkins build
- Comment Jira với build link

**Bước 3 — Pre-UAT Gate (cần Quality Reviewer approve):**

```
Chạy pre-UAT gate cho service vaccine-schedule, build 42, ticket FV-23829
```

AI gọi `qge_pre_uat_gate("vaccine-schedule", 42, "FV-23829")`:
- Kiểm tra CI deployment success, no CRITICAL findings
- Tạo HITL approval cho Quality Reviewer
- Thông báo reviewer qua Jira comment

> "Pre-UAT gate: PASS. Đã tạo approval request cho Quality Reviewer. Approval ID: abc123. Chờ approve trước khi promote."

**Bước 4 — Promote UAT (sau khi reviewer approve):**

```
Promote lên UAT, approval đã được approve
```

AI gọi `promote_to_uat("vaccine-schedule", 42, "FV-23829", "deploy-uat-vaccine", "abc123")`:
- Verify approval → trigger Jenkins deploy UAT → comment Jira

**Bước 5 — Pre-PROD Gate (cần Quality Reviewer + System Operator approve):**

```
Chạy pre-PROD gate
```

AI gọi `qge_pre_prod_gate("vaccine-schedule", 42, "FV-23829", "deploy-prod-vaccine")`:
- Full regression, soak time (30 phút UAT), rollback readiness
- Tạo HITL approval cho cả Quality Reviewer và System Operator

**Bước 6 — Promote PROD:**

```
Promote lên PROD, approval đã approve
```

AI gọi `promote_to_prod(...)` → trigger Jenkins deploy PROD → comment + transition Jira.

**Bước 7 — Health Check sau deploy:**

```
Check health sau deploy PROD
```

AI gọi `check_post_deploy_health("vaccine-schedule", "prod", 42, "FV-23829")`:
- So sánh error rate, latency vs baseline
- Nếu critical → tạo HITL approval cho rollback (PROD không bao giờ auto-rollback)

### Rollback

```
Rollback service vaccine-schedule trên UAT về build 40, lý do: error rate tăng
```

AI gọi `rollback("vaccine-schedule", "uat", 40, "FV-23829", "error rate tăng")`:
- UAT: rollback tự động
- PROD: cần System Operator approve trước

### Impact Analysis (trước khi deploy)

```
Phân tích impact thay đổi repo vaccine-schedule-be trong chain VAC
```

AI gọi `ship_impact_analysis("vaccine-schedule-be", "VAC")`:
- Tìm services bị ảnh hưởng trong cùng chain VAC
- Ví dụ: "3 services bị ảnh hưởng: vaccine-notification, vaccine-report, vaccine-api-gateway"

### Multi-Service Deployment

```
Deploy 3 services theo dependency order: shared-lib trước, rồi service-a và service-b
```

AI gọi `ship_multi_deploy(services=[...], issue_key="FV-23829", jenkins_deploy_job_template="deploy-ci-{service}", dry_run=true)`:
- Preview deployment order (dry_run)
- Bạn confirm → chạy thật

### Ví dụ thực tế — Full Ship Flow

**Bạn:** `Code FV-23829 đã merge, ship lên production đi`

**AI:**
1. Gọi `qge_pre_ci_gate` → ✅ PASS (unit tests, lint, secrets OK)
2. Gọi `trigger_ci_pipeline` → CI build #42 triggered
3. *(chờ CI build xong)*
4. Gọi `qge_pre_uat_gate` → ✅ PASS, approval tạo cho Quality Reviewer

> "Pre-UAT gate passed. Đã thông báo Quality Reviewer. Chờ approve."

**Bạn:** *(sau khi reviewer approve)* `Reviewer đã approve, promote UAT đi`

**AI:**
5. Gọi `promote_to_uat` → ✅ UAT deployed
6. *(chờ soak time 30 phút)*
7. Gọi `qge_pre_prod_gate` → ✅ PASS, soak time OK, approval tạo

> "Pre-PROD gate passed. Cần Quality Reviewer + System Operator approve."

**Bạn:** *(sau khi cả 2 approve)* `Đã approve, promote PROD`

**AI:**
8. Gọi `promote_to_prod` → 🚀 PROD deployed
9. Gọi `check_post_deploy_health` → ✅ healthy, no anomalies

> "PROD deploy thành công. Health check OK. Ticket FV-23829 đã chuyển sang Done."

### Xem pending approvals

```
Xem pending approvals cho quality_reviewer
```

AI gọi `list_pending_reviews("quality_reviewer")` → danh sách approvals đang chờ.

### Tools liên quan

| Tool | Vai trò | HITL? |
|------|---------|-------|
| `qge_pre_ci_gate` | Pre-CI automated gate | Không |
| `trigger_ci_pipeline` | Trigger CI build | Không |
| `qge_pre_uat_gate` | Pre-UAT gate + tạo approval | Quality Reviewer |
| `promote_to_uat` | Promote CI → UAT | Cần approval |
| `qge_pre_prod_gate` | Pre-PROD gate + tạo approval | Quality Reviewer + System Operator |
| `promote_to_prod` | Promote UAT → PROD | Cần approval |
| `check_post_deploy_health` | Health check sau deploy | PROD rollback cần approval |
| `rollback` | Rollback deployment | PROD cần approval |
| `ship_impact_analysis` | Impact analysis intra-chain | Không |
| `list_pending_reviews` | Xem pending approvals | Không |
| `notify_reviewer` | Thông báo reviewer | Không |
| `ship_multi_deploy` | Deploy nhiều services theo dependency | Không |

---

## Flow 6 — Tìm kiếm Context

> Khi bạn cần tìm code, docs, hoặc Jira issues liên quan đến một vấn đề.

### Khi nào dùng

- Cần hiểu business logic của một service
- Cần tìm code example hoặc pattern trong codebase
- Cần tìm Confluence docs liên quan
- Cần tìm Jira tickets tương tự

### Cách dùng

**Tìm kiếm tổng hợp (code + docs + Jira):**

```
Tìm code và docs liên quan đến payment flow trong chain VAC
```

AI gọi `search_context_hybrid(query, chain_id="VAC")` → trả về:
- Code files liên quan (với snippet)
- Confluence docs liên quan
- Jira tickets liên quan

**Tìm kiếm với filter chi tiết:**

```
Tìm code NestJS liên quan đến order service trong chain ICT
```

AI gọi `search_context_hybrid(query, chain_id="ICT", tech_stack="nestjs")`.

**Tìm Jira tickets:**

```
Tìm các bug liên quan đến login trong project FV
```

AI gọi `jira_search("login bug project FV")`.

**Đọc Confluence page:**

```
Đọc trang Confluence về API spec của vaccine schedule
```

AI gọi `get_confluence_page(page_id)`.

**Xem cấu trúc repo:**

```
Xem cấu trúc repo vaccine-schedule-be
```

AI gọi `get_repo_structure(source_id)` → trả về directory layout + build commands.

### Ví dụ thực tế

**Bạn:** `Tôi cần hiểu cách order service xử lý payment trong chain ICT. Tìm code và docs liên quan.`

**AI:** Gọi `search_context_hybrid("order service payment processing", chain_id="ICT")`:

> **Code liên quan:**
> - `ict-order-be/src/modules/payment/payment.service.ts` — xử lý payment flow chính
> - `ict-order-be/src/modules/order/order.service.ts` — gọi payment service khi confirm order
>
> **Confluence docs:**
> - [ICT Payment Integration Design](link) — HLD cho payment gateway
> - [Order Flow Sequence Diagram](link) — sequence diagram đầy đủ
>
> **Jira tickets liên quan:**
> - I25-1234: Implement VNPay payment gateway
> - I25-1456: Fix payment timeout handling

### Các filter hỗ trợ cho hybrid search

| Filter | Giá trị | Ví dụ |
|--------|---------|-------|
| `chain_id` | VAC, LAB, ICT, PHARMACY, BO, PLATFORM, DATA, DS | `chain_id="VAC"` |
| `tech_stack` | nestjs, dotnet_abp, react, java_gradle, python | `tech_stack="nestjs"` |
| `abp_layer` | domain, application, httpapi, efcore, mongodb | `abp_layer="domain"` |
| `nestjs_module` | tên module cụ thể | `nestjs_module="payment"` |
| `doc_type` | confluence_design, jira_story, jira_incident... | `doc_type="confluence_design"` |
| `domain` | domain nghiệp vụ cụ thể | `domain="payment"` |
| `project_key` | Jira project key | `project_key="FV"` |
| `space_key` | Confluence space key | `space_key="P4"` |

### Tools liên quan

| Tool | Vai trò |
|------|---------|
| `search_context` | Tìm kiếm cơ bản (vector search) |
| `search_context_hybrid` | Tìm kiếm nâng cao (graph + vector + re-rank) |
| `jira_search` | Tìm Jira issues bằng natural language |
| `get_confluence_page` | Đọc nội dung Confluence page |
| `get_repo_structure` | Xem cấu trúc repo + build commands |
| `list_sources` | Liệt kê tất cả sources đã index |

---

## Flow 7 — AMS: Incident Resolution (L2 + L3)

> Khi có incident/bug cần xử lý — từ triage đến fix, deploy hotfix, và post-mortem.

### Khi nào dùng

- Nhận incident/bug report từ Jira
- Cần AI hỗ trợ triage, diagnose root cause
- Cần suggest workaround (L2) hoặc tạo hotfix (L3)
- Cần ship hotfix qua CI → UAT → PROD với fast-track cho P1/P2
- Cần sinh RCA report cho P1/P2 incidents

### AMS Flow tổng quan

```
Triage → [Gate] → Diagnose → [Gate] → Fix (L2 hoặc L3)
  → [Gate] → [MR Review] → Ship (CI→UAT→PROD) → Resolved
  → RCA Report → [RCA Review] → Closed
```

### Cách dùng

Nói với AI agent:

```
Xử lý incident FI-12345 cho chain ICT
```

AI sẽ tự động chạy AMS Flow với 6 checkpoint dừng lại hỏi bạn:

**Phase 1 — Triage:**
```
incident_triage("FI-12345", "ICT")        → suggest severity, affected services
ams_post_triage_gate("FI-12345", "ICT")    → validate triage completeness
⏸️ CHECKPOINT: "Severity đúng không? L2 hay L3?"
ams_transition_incident("FI-12345", "Triaged")
```

**Phase 2 — Diagnose:**
```
incident_diagnose("FI-12345", "ICT")       → root cause, evidence, design gaps
⏸️ CHECKPOINT: "Root cause đúng không?"
```

**Phase 3A — L2 Workaround:**
```
suggest_workaround("FI-12345", diagnosis)  → ranked options with rollback plans
⏸️ CHECKPOINT: "Chọn workaround nào?"
→ User tự execute workaround
```

**Phase 3B — L3 Hotfix:**
```
ams_pre_hotfix_gate("FI-12345", "ICT", diagnosis)  → validate diagnosis
start_hotfix("FI-12345", "ICT", source_id, diagnosis) → branch + MR + code context
⏸️ CHECKPOINT: "MR đã tạo. Review code xong chưa?"
```

**Phase 4 — Verify & Ship:**
```
ams_post_hotfix_gate("FI-12345", mr_url)   → validate MR quality
ams_request_hotfix_review("FI-12345", mr_url) → request Solution_Reviewer approve
ams_ship_hotfix("FI-12345", "ci", build_info)  → trigger CI
ams_ship_hotfix("FI-12345", "uat", build_info) → promote UAT (cần approval)
ams_ship_hotfix("FI-12345", "prod", build_info) → promote PROD (cần approval)
⏸️ CHECKPOINT mỗi bước: "CI passed. Promote UAT?" → "UAT OK. Promote PROD?"
```

**Phase 5 — Post-Mortem (bắt buộc P1/P2):**
```
generate_rca_report("FI-12345")            → RCA report + Confluence + action items
ams_request_rca_review("FI-12345", rca)    → request review
⏸️ CHECKPOINT: "RCA OK chưa?"
ams_transition_incident("FI-12345", "Closed")
```

### Ví dụ thực tế — Full AMS Flow

**Bạn:** `Có incident FI-12345 — app crash khi quantity = 0, chain ICT`

**AI:**
1. Gọi `incident_triage("FI-12345", "ICT")`:

> **Triage Result:**
> - Severity suggestion: P2 (production degraded, single service)
> - Affected services: ict-order-be
> - Similar incidents: FI-11234 (null pointer, resolved 2 weeks ago)
> - Recommendation: L3 (code bug, cần fix)
>
> "Severity P2 OK không? L3 code fix đúng không?"

**Bạn:** `OK, P2 L3`

**AI:**
2. Gọi `ams_transition_incident("FI-12345", "Triaged")` → ✅
3. Gọi `incident_diagnose("FI-12345", "ICT")`:

> **Diagnosis:**
> - Immediate cause: Division by zero in `calculateDiscount()`
> - Root cause: Missing input validation for quantity field
> - Code context: `ict-order-be/src/modules/order/order.service.ts:142`
> - Shape improvement: Design spec thiếu edge case quantity=0
>
> "Root cause đúng không?"

**Bạn:** `Đúng, tạo hotfix đi`

**AI:**
4. Gọi `ams_pre_hotfix_gate` → ✅ PASS
5. Gọi `start_hotfix("FI-12345", "ICT", source_id, diagnosis)`:

> Branch: `hotfix/FI-12345-fix-quantity-zero-division`
> MR: [link]
> Code context: file `order.service.ts` line 142, cần thêm validation
> Follow-up story: FI-12346 (update design spec cho quantity validation)
>
> "MR đã tạo. Bạn fix code rồi cho biết khi nào xong."

**Bạn:** *(fix code, push)* `Code xong rồi`

**AI:**
6. Gọi `ams_post_hotfix_gate` → ✅ PASS (CI passed, no security issues)
7. Gọi `ams_request_hotfix_review` → approval tạo cho Solution_Reviewer
8. *(reviewer approve)*
9. Gọi `ams_ship_hotfix("FI-12345", "ci", ...)` → CI build triggered
10. Gọi `ams_ship_hotfix("FI-12345", "uat", ...)` → UAT deployed (fast-track soak 15min cho P2)

> "UAT OK. Promote PROD?"

**Bạn:** `OK`

**AI:**
11. Gọi `ams_ship_hotfix("FI-12345", "prod", ...)` → 🚀 PROD deployed
12. Gọi `generate_rca_report("FI-12345")`:

> **RCA Report** (published to Confluence):
> - Timeline: incident reported → triaged (5min) → diagnosed (15min) → fixed (30min) → deployed (45min)
> - Root cause: Missing input validation
> - Action items: FI-12346 (update design spec), FI-12347 (add input validation tests)
>
> "RCA OK chưa?"

**Bạn:** `OK`

**AI:** Gọi `ams_transition_incident("FI-12345", "Closed")` → ✅ Done.

### Escalation

Nếu incident chưa xử lý kịp SLA:

```
Escalate incident FI-12345, lý do: chưa có người triage sau 20 phút
```

AI gọi `ams_escalate_incident("FI-12345", "chưa có người triage sau 20 phút")`:
- Escalation: L1 → L2 (team lead notified)
- Webhook sent to Slack/Teams
- Jira comment added

Thresholds: P1=15min, P2=30min, P3=2h, P4=8h (configurable).

### State Machine

AI dùng `ams_transition_incident` để track lifecycle:

```
New → Triaged → Diagnosing → Fixing → Verifying → Deploying → Resolved → Post_Mortem → Closed
```

Backward transitions cho phép: Diagnosing→Triaged (re-triage), Verifying→Fixing (fix failed), Deploying→Fixing (rollback).

Gate prerequisites: mỗi transition quan trọng cần gate PASS trước khi cho phép.

### Tools liên quan

| Tool | Phase | Mô tả | HITL? |
|------|-------|-------|-------|
| `incident_triage` | 1 | Triage incident, suggest severity | Confirm severity |
| `incident_diagnose` | 2 | Diagnose root cause | Confirm root cause |
| `suggest_workaround` | 3A | L2 workaround options | Chọn workaround |
| `start_hotfix` | 3B | L3 hotfix branch + MR | Review code |
| `generate_rca_report` | 5 | RCA report + Confluence | Review RCA |
| `ams_post_triage_gate` | 1 | Gate: triage completeness | — |
| `ams_pre_hotfix_gate` | 3 | Gate: diagnosis completeness | — |
| `ams_post_hotfix_gate` | 4 | Gate: MR quality | — |
| `ams_pre_deploy_gate` | 4-5 | Gate: deploy readiness | — |
| `ams_request_hotfix_review` | 4 | Request MR review | Solution_Reviewer |
| `ams_request_deploy_approval` | 5 | Request deploy approval | Quality_Reviewer |
| `ams_request_rca_review` | 5 | Request RCA review | Solution_Reviewer |
| `ams_ship_hotfix` | 5 | Ship: CI/UAT/PROD (fast-track P1/P2) | Deploy approval |
| `ams_transition_incident` | All | State machine transition | — |
| `ams_escalate_incident` | Any | Escalation L1→L2→L3→Mgmt | — |

---

## Workflow End-to-End: Từ Ticket đến Production

Dưới đây là flow đầy đủ khi bạn nhận một Jira ticket và cần đưa lên production:

```
┌─────────────────────────────────────────────────────────────────────┐
│                    AI-NATIVE SDLC PIPELINE                          │
│                                                                     │
│  1. PLAN ─────── smart_sprint_plan, get_team_velocity               │
│     │            "Lên plan sprint 15 cho chain VAC"                 │
│     ▼                                                               │
│  2. INTENT ───── jira_get_issue                                     │
│     │            "Đọc ticket FV-23829"                              │
│     ▼                                                               │
│  3. UNDERSTAND ─ get_work_context, get_build_context                │
│     │            "Thu thập context cho FV-23829"                    │
│     ▼                                                               │
│  4. SHAPE ────── publish_requirements + publish_design              │
│     │            "Sinh requirements & design, publish Confluence"   │
│     │  ⏸️ HITL: Intent Owner + Solution Reviewer approve            │
│     ▼                                                               │
│  5. BUILD ────── create_feature_branch, commit_changes, create_MR   │
│     │            "Code → branch → commit → MR"                     │
│     │  ⏸️ HITL: Solution Reviewer approve MR                        │
│     ▼                                                               │
│  6. VERIFY ───── qge_analyze, publish_test_report                   │
│     │            "Chạy quality gate, publish test report"           │
│     │  ⏸️ HITL: Quality Reviewer approve                            │
│     ▼                                                               │
│  7. SHIP ─────── request_deploy_approval                            │
│     │            "Request deploy UAT → PROD"                       │
│     │  ⏸️ HITL: Intent Owner (UAT) + Quality Reviewer (PROD)        │
│     ▼                                                               │
│  ✅ DONE                                                             │
└─────────────────────────────────────────────────────────────────────┘
```

### Ví dụ: Từ đầu đến cuối

```
Bạn: "Lên plan sprint 15 cho chain VAC, 10 ngày capacity"
  → AI đề xuất plan, bạn approve, publish Confluence

Bạn: "Thực hiện ticket FV-23829 cho chain VAC"
  → AI thu thập context → ⏸️ bạn confirm
  → AI sinh requirements → ⏸️ bạn review & confirm
  → AI sinh design → ⏸️ bạn review & confirm
  → AI publish Confluence + comment Jira

Bạn: "Bắt đầu code cho FV-23829, repo vaccine-schedule-be"
  → AI phân tích code context, tạo branch
  → Bạn và AI cùng code
  → AI commit + tạo MR

Bạn: "Chạy quality gate và publish test report"
  → AI chạy 8 gates → publish report Confluence

Bạn: "Request deploy UAT"
  → AI tạo approval request → chờ approve → deploy
```

---

## Danh sách MCP Tools

### Search & Context

| Tool | Mô tả | Ví dụ prompt |
|------|-------|-------------|
| `search_context` | Tìm kiếm vector cơ bản | "Tìm code liên quan đến payment" |
| `search_context_hybrid` | Tìm kiếm nâng cao: graph + vector + re-rank. Hỗ trợ filter chain_id, tech_stack, domain, doc_type... | "Tìm code NestJS về order trong chain ICT" |
| `list_sources` | Liệt kê sources đã index | "Liệt kê tất cả repos đã index" |
| `get_confluence_page` | Đọc nội dung Confluence page | "Đọc trang Confluence về API spec" |
| `get_repo_structure` | Xem cấu trúc repo + build commands | "Xem cấu trúc repo vaccine-schedule-be" |
| `sync_sources` | Sync lại một source | "Sync lại repo vaccine-schedule-be" |
| `get_task_brief` | Tạo task brief từ context | "Tạo brief cho task upgrade NestJS" |

### Jira

| Tool | Mô tả | Ví dụ prompt |
|------|-------|-------------|
| `jira_get_issue` | Đọc chi tiết Jira issue (raw data) | "Đọc ticket FV-23829" |
| `jira_search` | Tìm Jira issues bằng natural language | "Tìm bugs liên quan login trong FV" |
| `jira_add_comment` | Thêm comment vào Jira issue | "Comment kết quả lên ticket" |
| `jira_get_transitions` | Xem transitions khả dụng | "Xem trạng thái có thể chuyển" |
| `jira_transition` | Chuyển trạng thái Jira issue | "Chuyển ticket sang In Review" |
| `jira_sync_project` | Sync Jira project vào vector store | "Sync project FV vào hệ thống" |
| `jira_create_from_intent` | Tạo Epic/Story/Sub-task từ mô tả | "Tạo story cho tính năng nhắc lịch tiêm" |
| `jira_brief_from_ticket` | Sinh brief đầy đủ từ ticket | "Sinh brief cho FV-23829" |
| `jira_link_dossier` | Liên kết dossier với Jira issue | — |

### Shape (Requirements & Design)

| Tool | Mô tả | Ví dụ prompt |
|------|-------|-------------|
| `get_work_context` | Thu thập context: Jira + Confluence + Git | "Thu thập context cho ticket FV-23829" |
| `get_domain_compliance_rules` | Lấy compliance rules theo chain/domain | "Lấy compliance rules cho chain VAC" |
| `publish_requirements_to_confluence` | Publish requirements lên Confluence + link Jira | *(tự động trong Shape Flow)* |
| `publish_design_to_confluence` | Publish design lên Confluence + link Jira | *(tự động trong Shape Flow)* |
| `request_shape_review` | Request review tài liệu (business/technical) | "Request review design cho FV-23829" |
| `track_hitl_decision` | Ghi lại HITL decision vào audit log | *(tự động)* |

### Build (Code → Branch → MR)

| Tool | Mô tả | Ví dụ prompt |
|------|-------|-------------|
| `get_build_context` | Phân tích code context sâu cho brownfield | "Phân tích code context cho FV-23829" |
| `create_feature_branch` | Tạo feature branch trên GitLab | "Tạo branch cho FV-23829" |
| `commit_changes` | Commit code lên branch | "Commit code changes" |
| `create_merge_request` | Tạo MR + link Jira | "Tạo MR cho FV-23829" |

### Planning

| Tool | Mô tả | Ví dụ prompt |
|------|-------|-------------|
| `get_team_velocity` | Xem velocity lịch sử | "Xem velocity team VAC" |
| `estimate_ticket` | AI estimate effort cho ticket | "Estimate FV-23829" |
| `classify_backlog` | Phân loại backlog | "Phân loại backlog chain VAC" |
| `smart_sprint_plan` | Đề xuất sprint plan | "Plan sprint 15 cho VAC, 10 ngày" |
| `suggest_sprint_plan` | Đề xuất plan theo velocity (legacy) | — |
| `publish_plan_to_confluence` | Publish plan lên Confluence | "Publish plan lên Confluence" |

### Quality & Ship Pipeline

| Tool | Mô tả | Ví dụ prompt |
|------|-------|-------------|
| `qge_analyze` | Chạy 8 quality gates trên diff | "Chạy quality gate cho diff này" |
| `qge_generate_tests` | Sinh pytest skeleton từ diff | "Sinh test cho code mới" |
| `qge_analyze_shape` | Post-Shape gate — requirements completeness, design feasibility, domain compliance | "Kiểm tra chất lượng requirements và design" |
| `qge_pre_ci_gate` | Pre-CI automated gate — unit tests, lint, secrets | "Chạy pre-CI gate cho branch feature/xxx" |
| `qge_pre_uat_gate` | Pre-UAT gate — CI success, regression, HITL approval | "Chạy pre-UAT gate cho service X build 42" |
| `qge_pre_prod_gate` | Pre-PROD gate — full regression, soak time, HITL approval | "Chạy pre-PROD gate cho service X" |
| `trigger_ci_pipeline` | Trigger CI pipeline cho branch | "Trigger CI cho branch feature/xxx" |
| `promote_to_uat` | Promote build CI → UAT với HITL approval | "Promote build 42 lên UAT" |
| `promote_to_prod` | Promote build UAT → PROD với mandatory HITL | "Promote lên PROD" |
| `rollback` | Rollback deployment (HITL cho PROD, automated cho CI/UAT) | "Rollback service X về build 40" |
| `check_post_deploy_health` | Post-deploy health check + anomaly detection | "Check health sau deploy" |
| `list_pending_reviews` | Xem pending approvals theo reviewer role | "Xem pending approvals cho quality_reviewer" |
| `notify_reviewer` | Gửi notification cho reviewer qua Jira/webhook | "Thông báo reviewer về approval mới" |
| `ship_impact_analysis` | Intra-chain impact analysis qua ArangoDB graph | "Phân tích impact thay đổi service X trong chain ICT" |
| `ship_run_eval` | Chạy eval manifest YAML | "Chạy eval cho pre-CI gate" |
| `ship_run_pipeline` | Chạy autonomous pipeline từ YAML | "Chạy ship pipeline" |
| `ship_multi_deploy` | Deploy nhiều services theo dependency order | "Deploy 3 services theo thứ tự dependency" |
| `publish_test_report` | Publish test report Confluence + link Jira | "Publish test report cho FV-23829" |
| `request_deploy_approval` | Request approval deploy UAT/PROD (legacy) | "Request deploy UAT cho FV-23829" |

### AMS — Incident Resolution (L2 + L3)

| Tool | Mô tả | Ví dụ prompt |
|------|-------|-------------|
| `incident_triage` | Triage incident: suggest severity, affected services, L2/L3 | "Triage incident FI-12345 chain ICT" |
| `incident_diagnose` | Diagnose root cause: code + docs + deployments + design gaps | "Diagnose incident FI-12345 chain ICT" |
| `suggest_workaround` | L2 workaround options ranked by risk/effort | "Suggest workaround cho FI-12345" |
| `start_hotfix` | L3 hotfix: branch + MR + code context + shape improvement | "Tạo hotfix cho FI-12345" |
| `generate_rca_report` | RCA report: timeline + root cause + action items → Confluence | "Sinh RCA report cho FI-12345" |
| `ams_post_triage_gate` | Gate: validate triage completeness | "Chạy post-triage gate cho FI-12345" |
| `ams_pre_hotfix_gate` | Gate: validate diagnosis completeness + code context | "Chạy pre-hotfix gate cho FI-12345" |
| `ams_post_hotfix_gate` | Gate: validate MR quality (CI, security, review) | "Chạy post-hotfix gate cho FI-12345" |
| `ams_pre_deploy_gate` | Gate: validate deploy readiness per environment | "Chạy pre-deploy gate cho FI-12345 UAT" |
| `ams_request_hotfix_review` | Request Solution_Reviewer approve hotfix MR | "Request review hotfix MR" |
| `ams_request_deploy_approval` | Request Quality_Reviewer approve deploy | "Request deploy approval cho UAT" |
| `ams_request_rca_review` | Request Solution_Reviewer review RCA report | "Request RCA review" |
| `ams_ship_hotfix` | Ship hotfix CI/UAT/PROD với fast-track P1/P2 | "Ship hotfix FI-12345 lên UAT" |
| `ams_transition_incident` | State machine: enforce lifecycle transitions + gate prerequisites | "Transition FI-12345 sang Triaged" |
| `ams_escalate_incident` | Escalation L1→L2→L3→Management + webhook | "Escalate FI-12345" |

### Removed Tools (đã xoá — dùng Client-Orchestrated Flow thay thế)

Các tools sau đã bị xoá khỏi MCP server. Nếu gặp lỗi "tool not found", dùng flow mới:

| Tool đã xoá | Thay bằng |
|-------------|-----------|
| `rde_generate_requirements` | Client LLM sinh requirements → `publish_requirements_to_confluence` |
| `rde_generate_design` | Client LLM sinh design → `publish_design_to_confluence` |
| `cgp_generate_code` | Client LLM sinh code → `commit_changes` |
| `cgp_decompose_tasks` | Client LLM tự decompose tasks |
| `cgp_run_pipeline` | Client LLM + `create_feature_branch` + `commit_changes` + `create_merge_request` |
| `start_work_from_ticket` | Shape Flow (4 HITL checkpoints) — xem `shape-flow.md` |

---

## Steering — Kiro, Cursor, Antigravity

> **⚠️ Flow Orchestrator Migration (2026-04):** Các steering files về workflow flow (b0-planning, b3-shape, b5-build, b6-verify, b7-ship, c1-ams), format rules (requirements-format, design-format, code-comments), và tool routing (mcp-tool-routing) đã được **xóa khỏi client**. Server-side Flow Orchestrator (`execute_flow`) giờ enforce thứ tự phases, HITL checkpoints, quality gates, và inject format rules tự động. Client chỉ cần gọi `execute_flow` — không cần steering files hướng dẫn flow nữa.

Nguồn chuẩn là **`.kiro/steering/*.md`**. **`.cursor/rules/*.mdc`** và **`.agents/rules/*.md`** giữ cùng nội dung (định dạng khác nhau).

| IDE | Thư mục trong repo | Cài / kích hoạt | Format |
|-----|-------------------|-----------------|--------|
| **Kiro** | `.kiro/steering/*.md` | Copy vào `~/.kiro/steering/` | Markdown + `inclusion: auto` |
| **Cursor** | `.cursor/rules/*.mdc` | Copy vào `~/.cursor/rules/` hoặc để trong repo | MDC + `alwaysApply: true` |
| **Antigravity** | `.agents/rules/*.md` | Workspace rules: mở project có thư mục này | Markdown (tối đa ~12k byte/file) |

### Nội dung steering còn lại (sau cleanup)

| Stem | Mô tả |
|------|-------|
| `role-ba` | Vai trò BA/SM/DM: planning, shaping, Jira/Confluence |
| `role-dev` | Vai trò Developer: MR, chất lượng code, CI |
| `role-dm` | Vai trò DM: team management, sprint oversight |
| `role-ops` | Vai trò Ops/SRE: deploy, incident, pipeline |

### Đã xóa (server-side Flow Orchestrator thay thế)

| Stem đã xóa | Lý do | Server thay thế |
|-------------|-------|-----------------|
| `b0-planning-flow` | Flow Orchestrator enforce | `config/flows/b0_planning.yaml` |
| `b3-shape-flow` | Flow Orchestrator enforce | `config/flows/b3_shape.yaml` |
| `b5-build-flow` | Flow Orchestrator enforce | `config/flows/b5_build.yaml` |
| `b6-verify-flow` | Flow Orchestrator enforce | `config/flows/b6_verify.yaml` |
| `b7-ship-flow` | Flow Orchestrator enforce | `config/flows/b7_ship.yaml` |
| `c1-ams-flow` | Flow Orchestrator enforce | `config/flows/c1_ams.yaml` |
| `requirements-format` | Server inject format rules | `config/format_rules/requirements-format.md` |
| `design-format` | Server inject format rules | `config/format_rules/design-format.md` |
| `code-comments` | Server inject format rules | `config/format_rules/code-comments.md` |
| `mcp-tool-routing` | Server `classify_intent` + `get_tool_catalog` | Intent Classifier (Qwen3.5-4B) |
| `context-optimization` | Server enforce `ENFORCE_CHAIN_ID_SEARCH=true` | Configmap setting |
| `logging-strategy` | Proposal chưa implement | Sẽ làm server-side nếu cần |
| `workspace-automation` | Proposal chưa implement | Sẽ làm server-side MCP tool nếu cần |

### Cách dùng Flow Orchestrator (thay cho steering flows)

Trước đây, steering files hướng dẫn AI agent tuân theo flow. Giờ server enforce:

```
# Trước (client steering — advisory, có thể bỏ qua):
AI đọc b3-shape-flow.md → tự chạy theo hướng dẫn → có thể skip steps

# Sau (server enforce — bắt buộc):
AI gọi execute_flow(flow="b3_shape", issue_key="FV-23829", chain_id="VAC")
  → Server enforce thứ tự phases, HITL checkpoints, quality gates
  → Server inject format rules vào response
  → Không thể skip steps
```

Hoặc dùng `classify_intent` để server tự detect flow:
```
User: "thực hiện ticket FV-23829"
AI gọi classify_intent("thực hiện ticket FV-23829")
  → Server trả: flow=b3_shape, issue_key=FV-23829, chain_id=VAC
AI gọi execute_flow(flow="b3_shape", ...)
```

### Cách cài thủ công (nếu không dùng install script)

**Kiro:**
```bash
# macOS / Linux
mkdir -p ~/.kiro/steering
cp .kiro/steering/*.md ~/.kiro/steering/
```
```powershell
# Windows
mkdir -Force "$env:USERPROFILE\.kiro\steering" | Out-Null
copy ".kiro\steering\*.md" "$env:USERPROFILE\.kiro\steering\"
```

**Cursor:**
```bash
# macOS / Linux
mkdir -p ~/.cursor/rules
cp .cursor/rules/*.mdc ~/.cursor/rules/
```
```powershell
# Windows
mkdir -Force "$env:USERPROFILE\.cursor\rules" | Out-Null
copy ".cursor\rules\*.mdc" "$env:USERPROFILE\.cursor\rules\"
```

**Antigravity:** giữ `.agents/rules/` ở **git root** project bạn mở. Với các file `*-part1.md`, `*-part2.md`, … — bật **Always On** cho **tất cả** phần cùng một stem trong **Customizations → Rules**.

### Cách dùng

Files tự động hoạt động trong mọi workspace — bạn không cần làm gì thêm sau khi copy.

Khi team cập nhật rules mới:
```bash
cd ai-native-client && git pull && bash install.sh          # macOS/Linux
cd ai-native-client; git pull; powershell -File install.ps1  # Windows
```

### Workspace-level vs User-level

| Vị trí | Scope | Khi nào dùng |
|--------|-------|-------------|
| `~/.kiro/steering/` hoặc `~/.cursor/rules/` | Mọi workspace | **Khuyến nghị** cho Kiro/Cursor — cài 1 lần |
| `.kiro/steering/`, `.cursor/rules/`, `.agents/rules/` (trong repo) | Chỉ workspace đó | Project-specific; Antigravity đọc `.agents/rules/` tại repo |

Nếu cùng tên file tồn tại ở cả 2 nơi, workspace-level sẽ override user-level.

---

## Cấu trúc repo

```
ai-native-client/                      # Clone repo này
├── .kiro/
│   └── steering/                      # Nguồn steering — copy vào ~/.kiro/steering/
│       └── *.md                       # 4 steering topics (role-ba, role-dev, role-dm, role-ops)
├── .cursor/
│   └── rules/                         # Cursor — cùng nội dung steering; copy vào ~/.cursor/rules/
│       └── *.mdc
├── .agents/
│   └── rules/                         # Antigravity workspace rules (≤12k/file; có *-partN.md)
│       └── *.md
├── docs/
│   └── jira-workflow-proposal.md      # Đề xuất Jira workflow cho AI-Native SDLC
├── mcp.kiro.example.json             # Mẫu config → copy vào ~/.kiro/settings/mcp.json
├── mcp.cursor.example.json           # Mẫu config → copy vào ~/.cursor/mcp.json
├── mcp.antigravity.example.json      # Mẫu config cho Antigravity
├── install.sh                        # Script cài đặt tự động — macOS/Linux (chạy 1 lần)
├── install.ps1                       # Script cài đặt tự động — Windows PowerShell (chạy 1 lần)
└── README.md
```

> **Lưu ý:** Flow steering files (b0, b3, b5, b6, b7, c1), format rules (requirements-format, design-format, code-comments), và tool routing (mcp-tool-routing) đã được xóa. Server-side Flow Orchestrator thay thế — xem mục [Steering](#steering--kiro-cursor-antigravity).

### Sau khi cài đặt, máy bạn sẽ có:

```
# macOS/Linux: ~/
# Windows: %USERPROFILE%\
├── .kiro/                             # Kiro user-level config (global)
│   ├── settings/
│   │   └── mcp.json                   # MCP server config — dùng cho MỌI workspace
│   └── steering/
│       └── *.md                       # 13 steering topics (sync từ repo)
├── .cursor/
│   ├── mcp.json                       # Cursor MCP config
│   └── rules/
│       └── *.mdc                      # Cursor rules — sync từ repo
└── your-project/                      # Bất kỳ workspace — MCP + rules global hoạt động; Antigravity thêm `.agents/rules/` trong repo
```

---

## Hướng dẫn lấy Tokens

### GitLab token (`X-GitLab-Token`)

1. Mở GitLab nội bộ → Avatar góc phải → **Preferences** → **Access Tokens**
2. Đặt tên: `ai-context-engine-mcp`
3. Scope: chọn `api`
4. Bấm **Create token** → copy ngay (chỉ hiện 1 lần)

### Confluence token (`X-Confluence-Token`)

1. Mở Confluence → **Profile** → **Personal Access Tokens**
2. Tạo token mới: `ai-context-engine-mcp`
3. Copy token
4. `X-Confluence-Username`: điền username đăng nhập Confluence

### Jira token (`X-Jira-Token`)

1. Mở Jira → **Profile** → **Personal Access Tokens**
2. Tạo token mới
3. Copy token

### `X-Member-Id`

Điền email công ty hoặc username. Dùng cho audit/tracing — hệ thống biết ai đang gọi tool.

---

## Lỗi thường gặp

| Lỗi | Nguyên nhân | Cách sửa |
|-----|------------|----------|
| `401 Unauthorized` | Sai `X-API-Key` | Kiểm tra lại key từ DevOps |
| `403 Forbidden` | Token cá nhân thiếu quyền | Tạo lại token với đủ scope |
| Không trả dữ liệu | Token hết hạn hoặc chain_id sai | Tạo token mới, kiểm tra chain_id |
| IDE không nhận config | Cache cũ | Restart IDE hoàn toàn |
| `chain_id required` | Thiếu chain_id trong request | Luôn chỉ rõ chain khi tìm kiếm/thao tác |
| MR tạo fail | Token GitLab thiếu quyền `api` | Tạo lại token với scope `api` |
| Confluence publish fail | Token thiếu quyền tạo/edit page | Kiểm tra quyền trên Confluence space |

---

## Bảo mật

- **KHÔNG** commit token vào git
- **KHÔNG** gửi token qua chat group
- **KHÔNG** chụp màn hình lộ token
- Rotate token định kỳ (3-6 tháng)
- `X-Member-Id` dùng cho audit — mọi action đều được log

---

## Jira Workflow Proposal

File `docs/jira-workflow-proposal.md` chứa đề xuất sửa Jira workflow cho phù hợp AI-Native SDLC:

- **Feature/Story workflow**: Open → In Analysis → In Review → In Development → Code Review → In Testing → Ready for UAT → UAT Approved → Ready for PROD → Done
- **Bug/Incident workflow**: Open → Triaging → Diagnosing → In Fix → Fix Review → Fix Testing → Fix Deployed → Done → Post-Mortem → RCA Done
- **HITL approval gates**: mapping từng gate với Jira transition

Jira Admin cần apply workflow này trước khi team dùng AI-Native SDLC pipeline đầy đủ.
