package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.common.BusinessException;
import com.market.common.MerchantAuditStatus;
import com.market.common.ResultCode;
import com.market.common.UserRole;
import com.market.dto.merchant.MerchantApplyRequest;
import com.market.dto.merchant.MerchantVO;
import com.market.entity.MerchantAccount;
import com.market.entity.Shop;
import com.market.entity.SysUser;
import com.market.mapper.MerchantAccountMapper;
import com.market.mapper.ShopMapper;
import com.market.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantAccountMapper merchantMapper;
    private final ShopMapper shopMapper;
    private final SysUserMapper userMapper;

    @Transactional
    public MerchantVO apply(Long userId, MerchantApplyRequest request) {
        MerchantAccount existing = merchantMapper.selectOne(
                new LambdaQueryWrapper<MerchantAccount>()
                        .eq(MerchantAccount::getUserId, userId)
                        .orderByDesc(MerchantAccount::getId)
                        .last("LIMIT 1")
        );
        if (existing != null) {
            if (existing.getAuditStatus() == MerchantAuditStatus.PENDING.getCode()) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "已有待审核的入驻申请");
            }
            if (existing.getAuditStatus() == MerchantAuditStatus.APPROVED.getCode()) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "您已是入驻商家");
            }
            if (existing.getAuditStatus() == MerchantAuditStatus.FROZEN.getCode()) {
                throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "商家账号已冻结，请联系平台");
            }
        }

        MerchantAccount merchant = new MerchantAccount();
        merchant.setUserId(userId);
        merchant.setCompanyName(request.getCompanyName());
        merchant.setContactName(request.getContactName());
        merchant.setContactPhone(request.getContactPhone());
        merchant.setLicenseNo(request.getLicenseNo());
        merchant.setLicenseFileUrl(request.getLicenseFileUrl());
        merchant.setAuditStatus(MerchantAuditStatus.PENDING.getCode());
        merchantMapper.insert(merchant);
        return toVO(merchant, null);
    }

    public MerchantVO getMine(Long userId) {
        MerchantAccount merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<MerchantAccount>()
                        .eq(MerchantAccount::getUserId, userId)
                        .orderByDesc(MerchantAccount::getId)
                        .last("LIMIT 1")
        );
        if (merchant == null) {
            return null;
        }
        Shop shop = shopMapper.selectOne(
                new LambdaQueryWrapper<Shop>().eq(Shop::getMerchantId, merchant.getId())
        );
        return toVO(merchant, shop);
    }

    public List<MerchantVO> listForPlatform(Integer auditStatus) {
        LambdaQueryWrapper<MerchantAccount> wrapper = new LambdaQueryWrapper<MerchantAccount>()
                .orderByDesc(MerchantAccount::getCreatedAt);
        if (auditStatus != null) {
            wrapper.eq(MerchantAccount::getAuditStatus, auditStatus);
        }
        List<MerchantAccount> merchants = merchantMapper.selectList(wrapper);
        if (merchants.isEmpty()) {
            return List.of();
        }
        List<Long> merchantIds = merchants.stream().map(MerchantAccount::getId).collect(Collectors.toList());
        Map<Long, Shop> shopMap = shopMapper.selectList(
                new LambdaQueryWrapper<Shop>().in(Shop::getMerchantId, merchantIds)
        ).stream().collect(Collectors.toMap(Shop::getMerchantId, s -> s, (a, b) -> a));

        return merchants.stream()
                .map(m -> toVO(m, shopMap.get(m.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public MerchantVO audit(Long merchantId, Integer action, String remark) {
        MerchantAccount merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商家申请不存在");
        }

        if (action == MerchantAuditStatus.APPROVED.getCode()) {
            approve(merchant, remark);
        } else if (action == MerchantAuditStatus.REJECTED.getCode()) {
            merchant.setAuditStatus(MerchantAuditStatus.REJECTED.getCode());
            merchant.setAuditRemark(remark);
            merchant.setAuditedAt(LocalDateTime.now());
            merchantMapper.updateById(merchant);
        } else if (action == MerchantAuditStatus.FROZEN.getCode()) {
            if (merchant.getAuditStatus() != MerchantAuditStatus.APPROVED.getCode()) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "仅已通过的商家可冻结");
            }
            merchant.setAuditStatus(MerchantAuditStatus.FROZEN.getCode());
            merchant.setAuditRemark(remark);
            merchant.setAuditedAt(LocalDateTime.now());
            merchantMapper.updateById(merchant);
            Shop shop = shopMapper.selectOne(
                    new LambdaQueryWrapper<Shop>().eq(Shop::getMerchantId, merchant.getId())
            );
            if (shop != null) {
                shop.setStatus(0);
                shopMapper.updateById(shop);
            }
        } else {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的审核动作");
        }

        Shop shop = shopMapper.selectOne(
                new LambdaQueryWrapper<Shop>().eq(Shop::getMerchantId, merchant.getId())
        );
        return toVO(merchantMapper.selectById(merchantId), shop);
    }

    private void approve(MerchantAccount merchant, String remark) {
        if (merchant.getAuditStatus() == MerchantAuditStatus.APPROVED.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "该申请已通过");
        }
        merchant.setAuditStatus(MerchantAuditStatus.APPROVED.getCode());
        merchant.setAuditRemark(remark);
        merchant.setAuditedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);

        SysUser user = userMapper.selectById(merchant.getUserId());
        if (user != null) {
            user.setRole(UserRole.MERCHANT.getCode());
            userMapper.updateById(user);
        }

        Shop existingShop = shopMapper.selectOne(
                new LambdaQueryWrapper<Shop>().eq(Shop::getMerchantId, merchant.getId())
        );
        if (existingShop == null) {
            Shop shop = new Shop();
            shop.setMerchantId(merchant.getId());
            shop.setName(merchant.getCompanyName());
            shop.setStatus(1);
            shopMapper.insert(shop);
        } else {
            existingShop.setStatus(1);
            existingShop.setName(merchant.getCompanyName());
            shopMapper.updateById(existingShop);
        }
    }

    private MerchantVO toVO(MerchantAccount merchant, Shop shop) {
        MerchantVO.MerchantVOBuilder builder = MerchantVO.builder()
                .id(merchant.getId())
                .userId(merchant.getUserId())
                .companyName(merchant.getCompanyName())
                .contactName(merchant.getContactName())
                .contactPhone(merchant.getContactPhone())
                .licenseNo(merchant.getLicenseNo())
                .licenseFileUrl(merchant.getLicenseFileUrl())
                .auditStatus(merchant.getAuditStatus())
                .auditRemark(merchant.getAuditRemark())
                .auditedAt(merchant.getAuditedAt())
                .createdAt(merchant.getCreatedAt());
        if (shop != null) {
            builder.shopId(shop.getId()).shopName(shop.getName());
        }
        return builder.build();
    }
}
