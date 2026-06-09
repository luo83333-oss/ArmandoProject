package com.market.dto.product;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ProductSaveRequest {

    @NotBlank(message = "商品标题不能为空")
    private String title;

    @NotNull(message = "类目不能为空")
    private Long categoryId;

    private String mainImageUrl;

    /** 辅图 URL，不含主图，最多 5 张 */
    private List<String> galleryUrls;

    private String detailHtml;

    @NotEmpty(message = "至少需要一个 SKU")
    @Valid
    private List<SkuRequest> skus;
}
