---
inclusion: manual
---

# Role: Delivery Manager / Scrum Master — JQL Patterns & Release Workflow

This steering file provides JQL patterns and release train workflow guidance
for Delivery Managers and Scrum Masters using `jira_search` in JQL mode.

Reference it in chat with `#role-dm`.

---

## When to Use JQL Mode vs Natural Language

| Use Case | Mode | Why |
|----------|------|-----|
| Filter by Fix Version, Sprint, Labels, Components | **JQL** | Exact field matching required |
| Release readiness report / go-no-go | **JQL** | Need total counts, enriched fields |
| Paginate through 50-100+ tickets | **JQL** | Pagination support (start_at, max_results) |
| "Find tickets related to payment timeout" | **Natural Language** | Semantic search, no specific fields |
| Exploratory search, vague intent | **Natural Language** | Vector similarity works better |

**Rule of thumb**: If the user mentions a specific Jira field name (version, sprint,
label, component, status), use JQL mode. Otherwise, use natural language.

---

## JQL Patterns

### 1. fixVersion Filtering — Release Train Scope

```jql
# All tickets in a release
project = "FI" AND fixVersion = "v2.1.0" ORDER BY status ASC

# Multiple versions
project = "FI" AND fixVersion IN ("v2.1.0", "v2.1.1") ORDER BY priority DESC

# Unresolved tickets in a release
project = "FI" AND fixVersion = "v2.1.0" AND resolution = Unresolved ORDER BY priority DESC
```

### 2. Sprint Filtering — Sprint Progress

```jql
# All tickets in current sprint
project = "FI" AND sprint = "Sprint 24" ORDER BY status ASC

# Incomplete tickets in sprint
project = "FI" AND sprint = "Sprint 24" AND status != Done ORDER BY priority DESC

# Sprint + specific issue types
project = "FI" AND sprint = "Sprint 24" AND issuetype IN (Story, Bug) ORDER BY priority DESC
```

### 3. Status-Based Grouping — Progress Tracking

```jql
# Done tickets in release (for completion %)
project = "FI" AND fixVersion = "v2.1.0" AND status = Done

# In Progress tickets
project = "FI" AND fixVersion = "v2.1.0" AND status = "In Progress"

# Blocked / To Do tickets (risk items)
project = "FI" AND fixVersion = "v2.1.0" AND status IN ("To Do", "Blocked") ORDER BY priority DESC
```

### 4. Component Filtering — Team/Module Scope

```jql
# All OMS component tickets in release
project = "FI" AND fixVersion = "v2.1.0" AND component = "OMS"

# Multiple components
project = "FI" AND fixVersion = "v2.1.0" AND component IN ("OMS", "Payment") ORDER BY status ASC

# Component + unresolved
project = "FI" AND component = "Payment" AND resolution = Unresolved ORDER BY updated DESC
```

### 5. Label Filtering — Cross-Cutting Concerns

```jql
# Tickets tagged for a release
project = "FI" AND labels = "release-2.1" ORDER BY status ASC

# Tickets with specific label + status
project = "FI" AND labels = "hotfix" AND status != Done ORDER BY priority DESC

# Multiple labels (OR)
project = "FI" AND labels IN ("payment", "security") ORDER BY priority DESC
```

### 6. Combined Patterns — Common DM Queries

```jql
# Release readiness: unresolved high-priority tickets
project = "FI" AND fixVersion = "v2.1.0" AND resolution = Unresolved AND priority IN (Highest, High) ORDER BY priority ASC

# Sprint burndown: remaining work
project = "FI" AND sprint = "Sprint 24" AND status NOT IN (Done, Closed) ORDER BY priority DESC

# Recently updated tickets in release (activity check)
project = "FI" AND fixVersion = "v2.1.0" AND updated >= -7d ORDER BY updated DESC
```

---

## Go/No-Go Checkpoint Workflow

Use this workflow to assess release readiness using `jira_search` JQL mode.

### Step 1: Get Release Scope (Total)

```python
jira_search(
    query="release scope",
    jql='project = "FI" AND fixVersion = "v2.1.0"',
    max_results=1
)
# → Check response.total for total ticket count
```

### Step 2: Check Completion Rate

```python
# Done tickets
done = jira_search(
    query="done tickets",
    jql='project = "FI" AND fixVersion = "v2.1.0" AND status = Done',
    max_results=1
)

# Completion % = done.total / scope.total * 100
```

### Step 3: Identify Blockers

```python
# Unresolved high-priority tickets
blockers = jira_search(
    query="blockers",
    jql='project = "FI" AND fixVersion = "v2.1.0" AND resolution = Unresolved AND priority IN (Highest, High)',
    max_results=50
)
# → Review blockers.results for risk assessment
```

### Step 4: Check Component Coverage

```python
# Per-component status (repeat for each component)
oms_status = jira_search(
    query="OMS component",
    jql='project = "FI" AND fixVersion = "v2.1.0" AND component = "OMS" AND resolution = Unresolved',
    max_results=50
)
```

### Step 5: Go/No-Go Decision

Summarize findings:
- Total tickets: `scope.total`
- Done: `done.total` (`done.total / scope.total * 100`%)
- Blockers (High/Highest): `blockers.total`
- Unresolved per component: list from Step 4

**Go criteria** (example):
- Completion rate ≥ 95%
- Zero Highest-priority unresolved tickets
- All components have ≤ 2 unresolved tickets

---

## Pagination for Large Result Sets

When a release has 50-100+ tickets, paginate through all results:

```python
# Page 1
page1 = jira_search(
    query="release tickets",
    jql='project = "FI" AND fixVersion = "v2.1.0" ORDER BY key ASC',
    start_at=0,
    max_results=50
)

# Page 2 (if page1.total > 50)
page2 = jira_search(
    query="release tickets",
    jql='project = "FI" AND fixVersion = "v2.1.0" ORDER BY key ASC',
    start_at=50,
    max_results=50
)
```

---

## Tips

- Always include `ORDER BY` in JQL for consistent pagination results.
- Use `max_results=1` when you only need the `total` count (faster).
- Combine fixVersion + status filters for release readiness dashboards.
- Use `updated >= -7d` to find recently active tickets.
- The `max_results` cap is 100 per request — paginate for larger sets.
