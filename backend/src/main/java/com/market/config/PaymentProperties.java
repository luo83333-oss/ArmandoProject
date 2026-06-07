package com.market.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "market.payment")
public class PaymentProperties {

    private boolean mockEnabled = true;
    private boolean wechatEnabled = false;
    private boolean alipayEnabled = false;
    /** 开发环境沙箱回调密钥，生产需替换为真实签名校验 */
    private String sandboxSecret = "dev-sandbox-secret";
}
