package com.market.dto.product;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProductVO {

    private Long id;
    private Long shopId;
    private String shopName;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String mainImageUrl;
    /** 辅图 URL 列表，不含主图 */
    private List<String> galleryUrls;
    private String detailHtml;
    private Integer shelfStatus;
    private Integer violationFlag;
    private BigDecimal minPrice;
    private Integer totalStock;
    private List<SkuVO> skus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
