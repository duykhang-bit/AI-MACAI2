---
inclusion: auto
---

# Bilingual Code Comments (EN/VI)

Mọi code thêm mới hoặc sửa đổi bởi AI PHẢI có inline comment song ngữ Anh-Việt.

## Format

```python
# English description / Mô tả tiếng Việt
code_here()
```

## Quy tắc

1. Comment cho function/class: docstring song ngữ
```python
def calculate_capacity(days: float, norm_sp: float) -> float:
    """Calculate sprint capacity from dev-days.
    Tính capacity sprint từ số ngày làm việc.
    """
```

2. Comment cho logic block: inline comment phía trên
```python
# Validate input is not empty / Kiểm tra input không rỗng
if not value.strip():
    raise ValueError("Empty input")

# Compute capacity using formula / Tính capacity theo công thức
capacity = days * norm_sp * (availability / 100)
```

3. Comment cho error handling / edge case
```python
# Fallback to capacity mode when no velocity data / Dùng capacity mode khi không có velocity
if average_velocity <= 0:
    planning_mode = "capacity"
```

## KHÔNG comment cho

- Import statements
- Closing brackets / parentheses
- Code hiển nhiên (ví dụ: `return result`)
- Boilerplate code (ví dụ: `if __name__ == "__main__"`)

## Áp dụng cho

- Tất cả file code mới hoặc sửa đổi, bất kể ngôn ngữ:
  - Python (`.py`) — `app/`, `tests/`, `scripts/`
  - TypeScript/JavaScript (`.ts`, `.tsx`, `.js`, `.jsx`)
  - Java (`.java`)
  - C# (`.cs`)
  - Go (`.go`)
  - Shell scripts (`.sh`)
  - SQL (`.sql`)
  - Dockerfile
- Mỗi ngôn ngữ dùng comment syntax tương ứng:
  - Python: `# English / Tiếng Việt`
  - TS/JS/Java/C#/Go: `// English / Tiếng Việt`
  - SQL: `-- English / Tiếng Việt`
  - Shell: `# English / Tiếng Việt`
- KHÔNG áp dụng cho config files, markdown, JSON, YAML
