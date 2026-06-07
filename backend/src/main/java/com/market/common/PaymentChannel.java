package com.market.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentChannel {

    MOCK("mock", "模拟支付"),
    WECHAT("wechat", "微信支付"),
    ALIPAY("alipay", "支付宝");

    private final String code;
    private final String label;

    public static PaymentChannel fromCode(String code) {
        for (PaymentChannel c : values()) {
            if (c.code.equals(code)) {
                return c;
            }
        }
        throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "不支持的支付渠道");
    }
}
