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
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
