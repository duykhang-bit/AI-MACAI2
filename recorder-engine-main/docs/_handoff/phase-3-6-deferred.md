# Handoff — Deferred Phases (khi có Git repo)

> Tạo bởi execution Phase 1+2. Chạy các phase dưới đây KHI DevOps hoàn tất tạo group `ai-test` trên git2.fptshop.com.vn.

## Status hiện tại

✅ `recorder-engine/` folder ready tại workspace root:
- Engine code (src/ 9 files), bin/frt-test, package.json
- scaffold-chain + add-app commands work
- 10 prompt files, AGENT_PROMPTS.md, README, engine-development.md
- Tests: 10/10 pass
- Security scan: CLEAN

## Phase 3 — Push engine lên GitLab

**Prerequisite**: Repo `git2.fptshop.com.vn/ai-test/recorder-engine` đã tồn tại (empty hoặc README only).

**Prompt cho agent:**
```
Push recorder-engine lên GitLab repo ai-test/recorder-engine.
Remote: git@git2.fptshop.com.vn:ai-test/recorder-engine.git
```

**Agent workflow:**
```bash
cd ~/work/ai-native-client/recorder-engine
git init -b main
git add .
git commit -m "init: @ai-test/recorder-engine v1.0.0"
git remote add origin git@git2.fptshop.com.vn:ai-test/recorder-engine.git
git push -u origin main --force
git tag v1.0.0
git push --tags
```

**Verify:**
- Repo accessible via browser
- `git clone git@git2.fptshop.com.vn:ai-test/recorder-engine.git /tmp/verify-engine && cd /tmp/verify-engine && npm install && ./bin/frt-test --version`
- Cleanup: `rm -rf /tmp/verify-engine`

---

## Phase 4 — Onboard chuỗi LAB (end-to-end test)

**Prerequisite**: Repo `ai-test/frt-test-lab` exists (empty). Engine đã push (Phase 3).

**Prompt cho agent:**
```
Onboard chuỗi LAB cho recorder, 3 apps:
- rsa-lab: https://ci-lab-rsa.frt.vn
- ecom-lab: https://ci-lab-ecom.frt.vn
- portal-lab: https://ci-lab-portal.frt.vn
```

**Verify:**
- Tester khác (KHÔNG phải author) clone + setup + record 1 test + play + commit — chỉ qua prompt, < 20 phút.

---

## Phase 5 — Onboard 7 chuỗi còn lại

**Prerequisite**: 7 repos empty đã tạo trên GitLab.

**Chạy 7 lần prompt #02 (thay tên + apps + URLs):**

| Chain | Repo | Apps (cần user cung cấp URLs) |
|-------|------|------|
| BO | ai-test/frt-test-bo | TBD |
| DATA | ai-test/frt-test-data | TBD |
| DS | ai-test/frt-test-ds | TBD |
| ICT | ai-test/frt-test-ict | TBD |
| PHARMACY | ai-test/frt-test-pharmacy | TBD |
| PLATFORM | ai-test/frt-test-platform | TBD |
| VAC | ai-test/frt-test-vac | TBD |

**Lưu ý**: Mỗi chuỗi cần owner cung cấp:
1. Danh sách apps (tên + URL CI environment)
2. Display name chính thức

---

## Phase 6 — Cleanup workspace

**Prompt:**
```
Dọn dẹp frt-test-recorder-template trong ai-native-client workspace.
Rename thành frt-test-recorder-template.deprecated/ (hoặc xóa nếu user confirm).
Update README.md chính (nếu reference template path cũ).
```

---

## Open items (chưa quyết)

- [ ] Apps + URLs cho 7 chuỗi ngoài LAB — chờ owner mỗi chuỗi cung cấp
- [ ] CI regression (Phase 7 defer) — GitLab CI template khi pattern stable
- [ ] `npm link` trên Windows — cần test thêm với Git Bash / WSL
- [ ] Folder `frt-test-recorder-template/` trong repo này — giữ / rename / xóa (user chưa quyết, D7 note "chưa quyết định ngay")
