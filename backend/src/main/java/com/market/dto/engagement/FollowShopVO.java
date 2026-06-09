package com.market.dto.engagement;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowShopVO {

    private Long shopId;
    private String shopName;
    private String logoUrl;
    private String description;
}
