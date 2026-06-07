package com.market.service.payment;

import com.market.common.PaymentChannel;
import com.market.entity.UserOrder;

public interface PaymentProvider {

    PaymentChannel channel();

    String createTradeNo(UserOrder order);

    boolean verifyCallbackSign(String tradeNo, String sign, String expectedSecret);
}
