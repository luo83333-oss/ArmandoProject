package com.market.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "market.jwt")
public class JwtProperties {

    private String secret = "market-dev-secret-change-in-production-32chars";
    private long expireHours = 72;
}
