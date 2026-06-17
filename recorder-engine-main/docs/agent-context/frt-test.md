# frt-test — Workspace Playbook (agent steering)

> File này được copy vào **steering của workspace** lúc setup engine, để BẤT KỲ AI agent nào
> mở workspace cũng tự biết: workspace này dùng `@ai-test/recorder-engine` (record/replay E2E test bằng YAML),
> và playbook hướng dẫn nằm ở repo `recorder-engine`. Nguồn gốc: `recorder-engine/docs/agent-context/frt-test.md`.

## Đồ chơi đã có sẵn trong workspace này

- **CLI**: `frt-test` (đã `npm link` global). Verify: `frt-test --version`.
- **Engine + playbook đầy đủ** nằm ở repo `recorder-engine`. Cách tìm engine khi cần:
  - Thường là thư mục cạnh bên: `../recorder-engine` hoặc `~/work/recorder-engine`.
  - Hoặc resolve từ symlink global: `readlink -f "$(which frt-test)"` → `.../recorder-engine/bin/frt-test`.
  - Hoặc `npm ls -g --link 2>/dev/null | grep recorder-engine`.
- **Playbook prompt (đọc trước khi làm)**: `<engine>/docs/AGENT_PROMPTS.md` + `<engine>/docs/prompts/*.md`.
- **Guide cho người dùng**: `<engine>/docs/guide-vi.html`, onboard: `<engine>/docs/guide-onboard-chain-vi.html`.

## Khi user yêu cầu việc gì → ĐỌC prompt tương ứng TRƯỚC khi chạy

| User muốn | Đọc file trong `<engine>/docs/prompts/` |
|-----------|------------------------------------------|
| Setup engine | `01-setup-engine.md` |
| Onboard chuỗi mới | `02-onboard-new-chain.md` |
| Thêm app vào chuỗi | `03-add-app-to-chain.md` |
| Record test | `04-record-test.md` |
| Play test | `05-play-test.md` |
| Fix test fail | `06-fix-failing-test.md` |
| Regression | `07-daily-regression.md` |
| Commit & push | `08-commit-and-push.md` |
| Update engine | `09-update-engine.md` |
| List & cleanup | `10-list-and-cleanup-tests.md` |
| Migrate multi-env | `11-migrate-multi-env.md` |
| Clone test giữa env | `12-clone-flow.md` |

## Luật BẮT BUỘC (high-signal — nhớ kể cả khi chưa đọc prompt)

1. **Multi-env (schema v2)**: mọi lệnh `record`/`play`/`list` cần `--env {ci|uat|prod}`. Chuỗi cũ (v1) thì bỏ qua được.
2. **Cấu trúc flow**: `flows/{app}/{env}/{tên-test}.yaml`; flow dùng chung ở `flows/{app}/_shared/`.
3. **⛔ Fix test fail**: chỉ sửa để E2E **chạy được** (selector/timing/hover/popup/navigation/URL/data).
   **KHÔNG** sửa/xóa/nới lỏng `assert` để ép test pass — assertion fail = **bug sản phẩm** test bắt được → giữ nguyên assertion + báo người dùng.
   *Ngoại lệ duy nhất*: assertion so **dữ liệu động hợp lệ** (tiền/tồn kho/%/ngày — đổi mỗi lần chạy, không phải bug) → mới đổi `assertType: text` sang `visible` hoặc parameterize `{{var}}`.
4. **Clone repo** (engine/chain): dùng **HTTPS** (`https://git2.fptshop.com.vn/...`), KHÔNG dùng SSH.
5. **Recorder behaviors** (khỏi cấu hình tay): không tự chèn `delay` cứng (runner đã wait element); tự dismiss popup khi play (`config.autoPopupDismiss: false` để tắt); tự ghi bước `hover` khi hover-to-reveal; assert giá trị động tự thành `visible`.

## Nếu chưa chắc

- Không rõ env/app → đọc `config/environments.yaml` của chain repo, hoặc chạy `frt-test envs`.
- Không tìm thấy engine → hỏi user vị trí `recorder-engine`, hoặc chạy `frt-test --version` để xác nhận đã cài.
