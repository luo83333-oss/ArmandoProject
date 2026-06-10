# Phase 0 启动检查清单

需在业务/运维侧完成，代码仓库无法代劳。

## 法务与资质（Epic 0.1）

| 项 | 负责人 | 状态 | Linear |
|----|--------|------|--------|
| 注册公司并取得营业执照 | 创始人/法务 | ⬜ 待办 | [NL2-85](https://linear.app/code2michael/issue/NL2-85) |
| 《商家入驻协议》法务定稿 | 产品+法务 | 🟡 草案见 `docs/legal/` | [NL2-81](https://linear.app/code2michael/issue/NL2-81) |
| 确定抽佣 5% 并写入协议 | 产品 | ✅ 已决策 | [NL2-82](https://linear.app/code2michael/issue/NL2-82) |
| 申请微信/支付宝商户号 | 财务/运营 | ⬜ 待办 | [NL2-88](https://linear.app/code2michael/issue/NL2-88) |
| 支付资质风险方案 | 产品 | ✅ ADR-001 | [NL2-90](https://linear.app/code2michael/issue/NL2-90) |

## 基础设施（Epic 0.2）

| 项 | 负责人 | 状态 | Linear |
|----|--------|------|--------|
| Git 仓库 + README + CI 占位 | 研发 | ✅ 本仓库 | [NL2-91](https://linear.app/code2michael/issue/NL2-91) |
| Docker Compose MySQL/Redis/Nginx | 研发 | ✅ `docker compose up` | [NL2-86](https://linear.app/code2michael/issue/NL2-86) |
| 购买域名 + SSL | 运维 | ⬜ 待办 | [NL2-83](https://linear.app/code2michael/issue/NL2-83) |
| 采购 ECS 4核8G + 安全组 | 运维 | ⬜ 待办 | [NL2-84](https://linear.app/code2michael/issue/NL2-84) |

## 需求与架构（Epic 0.3）

| 项 | 负责人 | 状态 | Linear |
|----|--------|------|--------|
| PRD v0.1 | 产品 | ✅ `docs/PRD-v0.1.md` | [NL2-87](https://linear.app/code2michael/issue/NL2-87) |
| ER + DDL 草案 | 后端 | ✅ `docs/database/` + `backend/sql/` | [NL2-89](https://linear.app/code2michael/issue/NL2-89) |

## Phase 0 完成标准

- [x] 研发侧文档与本地基建可一键启动
- [ ] 营业执照、域名、ECS、商户号（至少明确「mock 继续」书面决策）

完成后可将 Milestone **Phase 0** 标为完成，进入 **Phase 1 W1**。

## 上线（Phase 4 / NL2-161）

生产 go-live 勾选与回滚：见 [`docs/deploy/LAUNCH_CHECKLIST.md`](../deploy/LAUNCH_CHECKLIST.md)。
