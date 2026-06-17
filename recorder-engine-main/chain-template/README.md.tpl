# Test E2E — Chuỗi {{CHAIN_NAME}}

YAML test cases cho chuỗi **{{CHAIN_NAME}}**, chạy bằng engine `@ai-test/recorder-engine`.

## Setup engine (1 lần mỗi máy)

Paste prompt sau cho AI agent:

```
Setup engine recorder cho tôi.
Workspace folder: ~/work
```

Hoặc chạy thủ công:

```bash
git clone git@git2.fptshop.com.vn:ai-test/recorder-engine.git ~/work/recorder-engine
cd ~/work/recorder-engine && npm install && npx playwright install chromium && npm link
```

## Apps trong chuỗi

{{APPS_TABLE}}

## Quick start (paste prompt cho AI agent)

```
Record test mới cho chuỗi {{CHAIN_NAME}}, app {{DEFAULT_APP}}, env ci, tên: "Tên test của bạn"
```

```
Chạy regression toàn chuỗi {{CHAIN_NAME}}, app {{DEFAULT_APP}}, env ci
```

## Commands reference

| Việc | Command |
|------|---------|
| Record | `frt-test record --app {app} --env ci -n "Tên test"` |
| Play 1 | `frt-test play --app {app} --env ci {file}.yaml` |
| Play all | `frt-test play --app {app} --env ci --all` |
| List | `frt-test list --app {app} --env ci` |
| Envs status | `frt-test envs` |
| Add env URL | `frt-test add-env --app {app} --env uat --url {URL}` |
| Clone test | `frt-test clone-flow {file}.yaml --app {app} --from ci --to uat` |
| Migrate | `frt-test migrate-config --write` |

Xem thêm: [AGENT_PROMPTS.md](https://git2.fptshop.com.vn/ai-test/recorder-engine/-/blob/main/docs/AGENT_PROMPTS.md)
