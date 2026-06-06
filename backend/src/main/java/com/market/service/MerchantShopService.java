package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.common.BusinessException;
import com.market.common.MerchantAuditStatus;
import com.market.common.ResultCode;
import com.market.entity.MerchantAccount;
import com.market.entity.Shop;
import com.market.mapper.MerchantAccountMapper;
import com.market.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantShopService {

    private final MerchantAccountMapper merchantMapper;
    private final ShopMapper shopMapper;

    public Shop requireApprovedShop(Long userId) {
        MerchantAccount merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<MerchantAccount>()
                        .eq(MerchantAccount::getUserId, userId)
                        .eq(MerchantAccount::getAuditStatus, MerchantAuditStatus.APPROVED.getCode())
                        .orderByDesc(MerchantAccount::getId)
                        .last("LIMIT 1")
        );
        if (merchant == null) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "请先完成商家入驻审核");
        }
        Shop shop = shopMapper.selectOne(
                new LambdaQueryWrapper<Shop>().eq(Shop::getMerchantId, merchant.getId())
        );
        if (shop == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "店铺尚未创建");
        }
        if (shop.getStatus() != null && shop.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "店铺已关闭");
        }
        return shop;
    }
}
