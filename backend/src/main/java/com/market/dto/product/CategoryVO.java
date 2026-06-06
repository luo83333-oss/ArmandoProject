package com.market.dto.product;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryVO {

    private Long id;
    private Long parentId;
    private String name;
    private List<CategoryVO> children;
}
