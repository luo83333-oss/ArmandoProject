package com.market.service;

import com.market.common.BusinessException;
import com.market.common.OrderStatus;
import com.market.common.ResultCode;
import com.market.entity.OrderStatusLog;
import com.market.mapper.OrderStatusLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderStateMachine {

    private final OrderStatusLogMapper statusLogMapper;

    public void validateTransition(int from, int to) {
        boolean allowed;
        if (from == OrderStatus.PENDING_PAY.getCode()) {
            allowed = to == OrderStatus.PAID.getCode() || to == OrderStatus.CANCELLED.getCode();
        } else if (from == OrderStatus.PAID.getCode()) {
            allowed = to == OrderStatus.SHIPPED.getCode();
        } else if (from == OrderStatus.SHIPPED.getCode()) {
            allowed = to == OrderStatus.COMPLETED.getCode();
        } else {
            allowed = false;
        }
        if (!allowed) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "订单状态不允许此操作");
        }
    }

    public void logTransition(Long orderId, Integer from, int to, String operatorType, Long operatorId, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setOperatorType(operatorType);
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        statusLogMapper.insert(log);
    }

    public static String labelOf(int code) {
        for (OrderStatus s : OrderStatus.values()) {
            if (s.getCode() == code) {
                return s.getLabel();
            }
        }
        return "未知";
    }
}
