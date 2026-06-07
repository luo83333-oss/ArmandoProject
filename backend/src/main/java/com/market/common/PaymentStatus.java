package com.market.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    PENDING(0),
    SUCCESS(1),
    FAILED(2);

    private final int code;
}
