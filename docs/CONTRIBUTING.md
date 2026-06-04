# 协作与分支规范

## Git 分支

```
main          ← 仅通过 PR 从 develop 合并，打 tag 发布
develop       ← 日常集成
feature/xxx   ← 单任务分支，命名：feature/NL2-91-git-init
hotfix/xxx    ← 生产紧急修复，从 main 拉出，合并回 main + develop
```

## 提交信息

```
<type>(<scope>): <subject>

类型：feat | fix | docs | chore | refactor | test
scope：backend | user-app | merchant-admin | platform-admin | deploy
```

示例：`feat(backend): add user login API`

## PR 要求

- 关联 Linear Issue（如 `NL2-105`）
- develop 合并前至少 1 人 Review（单人团队可自审 checklist）
- 不提交 `.env`、密钥、证书私钥

## CI（占位）

`.github/workflows/ci.yml` 在 Phase 1 接入真实构建；当前仅校验目录与文档存在性。
