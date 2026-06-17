# List & Cleanup Tests — Agent Instructions

> **Trigger:** "Liệt kê tests của {CHAIN}" hoặc "Xóa test {TÊN} khỏi {CHAIN}"

## Trigger prompt

```
Liệt kê tất cả tests của chuỗi LAB
```

```
Xóa test "login-cu" khỏi app rsa-lab của LAB
```

## Prerequisites

- Engine đã setup, chain repo đã clone

## Agent workflow

### List tests
```bash
cd ~/work/frt-test-lab
frt-test list                    # Toàn chuỗi
frt-test list --app rsa-lab      # 1 app
```

### Xóa test
```bash
cd ~/work/frt-test-lab
rm flows/rsa-lab/login-cu.yaml
git add -A && git commit -m "remove: test login-cu (obsolete)" && git push
```

### Rename / move test
```bash
mv flows/rsa-lab/old-name.yaml flows/rsa-lab/new-name.yaml
git add -A && git commit -m "rename: old-name → new-name" && git push
```

## Success criteria

- `frt-test list` output đúng (không hiện test đã xóa)
- Git clean after push

## Common errors & fix

| Error | Cause | Fix |
|-------|-------|-----|
| Test vẫn hiện sau xóa | Git cache | `git rm flows/{app}/{file}` thay vì `rm` |
