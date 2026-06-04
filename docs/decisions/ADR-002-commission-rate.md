# ADR-002：平台抽佣比例

**状态**：已采纳（待法务确认） | **日期**：2026-06-05

## 决策

- 默认抽佣比例：**5%**（按订单实付金额 `pay_amount` 计算）
- 写入 `user_order.commission_rate` 快照，避免后续改配置影响历史订单
- 配置键：`platform_config.commission.rate = 0.05`

## 公式

```
commission_amount = pay_amount × commission_rate
商家结算金额 = pay_amount - commission_amount
```

## 待办

- [ ] 写入《商家入驻协议》正式条款
- [ ] 总控台是否支持按类目/商家单独费率（V2 考虑）
