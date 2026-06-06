package com.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shopId;
    private Long categoryId;
    private String title;
    private String mainImageUrl;
    private String detailHtml;
    /** 0下架 1上架 */
    private Integer shelfStatus;
    /** 0正常 1违规待审 */
    private Integer violationFlag;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
