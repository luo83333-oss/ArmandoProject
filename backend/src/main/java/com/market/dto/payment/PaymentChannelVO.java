package com.market.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentChannelVO {

    private String code;
    private String label;
}
