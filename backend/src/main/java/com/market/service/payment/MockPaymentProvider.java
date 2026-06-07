package com.market.service.payment;

import com.market.common.PaymentChannel;
import com.market.entity.UserOrder;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public PaymentChannel channel() {
        return PaymentChannel.MOCK;
    }

    @Override
    public String createTradeNo(UserOrder order) {
        return "MOCK" + order.getOrderNo() + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    @Override
    public boolean verifyCallbackSign(String tradeNo, String sign, String expectedSecret) {
        return expectedSecret != null && expectedSecret.equals(sign);
    }
}
