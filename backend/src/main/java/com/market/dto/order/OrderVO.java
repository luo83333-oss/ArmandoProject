package com.market.dto.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long shopId;
    private String shopName;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer status;
    private String statusLabel;
    private Map<String, Object> address;
    private List<OrderItemVO> items;
    private String logisticsNo;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    /** 售后入口占位：已完成订单可展示 */
    private Boolean afterSaleAvailable;
    /** 是否已评价 */
    private Boolean reviewed;
}
