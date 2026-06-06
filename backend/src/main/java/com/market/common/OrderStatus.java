package com.market.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    PENDING_PAY(10, "待付款"),
    PAID(20, "待发货"),
    SHIPPED(30, "已发货"),
    COMPLETED(40, "已完成"),
    CANCELLED(50, "已取消");

    private final int code;
    private final String label;
}
