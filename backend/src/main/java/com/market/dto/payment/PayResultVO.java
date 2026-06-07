package com.market.dto.payment;

import com.market.dto.order.OrderVO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayResultVO {

    /** pending | success */
    private String status;
    private String channel;
    private String channelLabel;
    private String tradeNo;
    /** 开发沙箱：前端可调用 completeSandboxPay 完成支付 */
    private String sandboxHint;
    private OrderVO order;
}
