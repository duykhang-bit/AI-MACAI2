# Cấu trúc Git — AI Test Recorder

## Tổng quan

```
git2.fptshop.com.vn/ai-test/
├── recorder-engine          ← Engine code (chung cho tất cả chuỗi)
├── frt-test-bo              ← Chuỗi BO (Back Office)
├── frt-test-data            ← Chuỗi DATA
├── frt-test-ds              ← Chuỗi DS
├── frt-test-ict             ← Chuỗi ICT
├── frt-test-lab             ← Chuỗi LAB (Xét nghiệm)
├── frt-test-pharmacy        ← Chuỗi PHARMACY (Nhà thuốc)
├── frt-test-platform        ← Chuỗi PLATFORM
└── frt-test-vac             ← Chuỗi VAC (Vaccine)
```

## Bảng tra cứu repo theo chuỗi

| Chuỗi | Repo | Clone URL (HTTPS) |
|--------|------|-------------------|
| **Engine** (bắt buộc) | recorder-engine | `https://git2.fptshop.com.vn/ai-test/recorder-engine.git` |
| BO | frt-test-bo | `https://git2.fptshop.com.vn/ai-test/frt-test-bo.git` |
| DATA | frt-test-data | `https://git2.fptshop.com.vn/ai-test/frt-test-data.git` |
| DS | frt-test-ds | `https://git2.fptshop.com.vn/ai-test/frt-test-ds.git` |
| ICT | frt-test-ict | `https://git2.fptshop.com.vn/ai-test/frt-test-ict.git` |
| LAB | frt-test-lab | `https://git2.fptshop.com.vn/ai-test/frt-test-lab.git` |
| PHARMACY | frt-test-pharmacy | `https://git2.fptshop.com.vn/ai-test/frt-test-pharmacy.git` |
| PLATFORM | frt-test-platform | `https://git2.fptshop.com.vn/ai-test/frt-test-platform.git` |
| VAC | frt-test-vac | `https://git2.fptshop.com.vn/ai-test/frt-test-vac.git` |

## Quy tắc onboard

### Luôn clone 2 repo:

1. **Engine** (1 lần duy nhất, dùng chung):
   ```bash
   git clone https://git2.fptshop.com.vn/ai-test/recorder-engine.git ~/work/recorder-engine
   ```

2. **Chain repo** (chọn đúng chuỗi của mình):
   ```bash
   # Ví dụ: tester thuộc chuỗi LAB
   git clone https://git2.fptshop.com.vn/ai-test/frt-test-lab.git ~/work/frt-test-lab
   ```

### Cách xác định repo cần clone

- Bạn thuộc chuỗi nào → clone repo `frt-test-{tên chuỗi}` tương ứng
- Nếu phụ trách nhiều chuỗi → clone nhiều chain repos, engine vẫn chỉ 1

### Sau khi clone

```bash
# Setup engine (1 lần)
cd ~/work/recorder-engine
npm install
npx playwright install chromium
npm link

# Làm việc với chuỗi
cd ~/work/frt-test-lab    # hoặc frt-test-{chuỗi của bạn}
frt-test list             # liệt kê tests hiện có
```

## Phân biệt 2 loại repo

| | Engine (`recorder-engine`) | Chain (`frt-test-{chain}`) |
|---|---|---|
| Chứa gì | Source code (TypeScript) | YAML test cases + config |
| Ai sửa | Core team | Tester của chuỗi |
| Update | `git pull` khi có version mới | `git pull` / `git push` hàng ngày |
| `npm install` | ✅ Cần | ❌ Không cần |
| `node_modules/` | Có | Không có |

## Lưu ý

- Luôn dùng HTTPS (`https://git2.fptshop.com.vn/...`) để clone
- GitLab sẽ hỏi username/password lần đầu (dùng tài khoản GitLab nội bộ)
- Nếu muốn lưu credentials: `git config --global credential.helper store`

## Compatibility matrix — Engine version × Chain schema

| Engine version | Schema v0 | Schema v1 | Schema v2 |
|----------------|-----------|-----------|-----------|
| < 1.0.0 | ✅ | ✅ | ❌ |
| **≥ 1.0.0** | ✅ | ✅ | ✅ |

- Schema v2 yêu cầu engine ≥ 1.0.0.
- Engine ≥ 1.0.0 vẫn đọc được schema v0/v1 (backward-compat 100%).
- Chuỗi chưa migrate vẫn chạy bình thường — migration là opt-in.
- Migrate bằng: `frt-test migrate-config --write` (xem prompt 11).
