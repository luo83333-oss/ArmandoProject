package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.market.common.BusinessException;
import com.market.common.ResultCode;
import com.market.dto.engagement.FollowShopVO;
import com.market.dto.engagement.FollowStatusVO;
import com.market.entity.Shop;
import com.market.entity.UserShopFollow;
import com.market.mapper.ShopMapper;
import com.market.mapper.UserShopFollowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopFollowService {

    private final UserShopFollowMapper followMapper;
    private final ShopMapper shopMapper;

    @Transactional
    public void follow(Long userId, Long shopId) {
        Shop shop = requireActiveShop(shopId);
        Long count = followMapper.selectCount(
                new LambdaQueryWrapper<UserShopFollow>()
                        .eq(UserShopFollow::getUserId, userId)
                        .eq(UserShopFollow::getShopId, shop.getId())
        );
        if (count > 0) {
            return;
        }
        UserShopFollow follow = new UserShopFollow();
        follow.setUserId(userId);
        follow.setShopId(shop.getId());
        followMapper.insert(follow);
    }

    @Transactional
    public void unfollow(Long userId, Long shopId) {
        followMapper.delete(
                new LambdaQueryWrapper<UserShopFollow>()
                        .eq(UserShopFollow::getUserId, userId)
                        .eq(UserShopFollow::getShopId, shopId)
        );
    }

    public List<FollowShopVO> listFollowing(Long userId) {
        List<UserShopFollow> follows = followMapper.selectList(
                new LambdaQueryWrapper<UserShopFollow>()
                        .eq(UserShopFollow::getUserId, userId)
                        .orderByDesc(UserShopFollow::getCreatedAt)
        );
        List<FollowShopVO> result = new ArrayList<>();
        for (UserShopFollow follow : follows) {
            Shop shop = shopMapper.selectById(follow.getShopId());
            if (shop == null || shop.getStatus() == null || shop.getStatus() == 0) {
                continue;
            }
            result.add(FollowShopVO.builder()
                    .shopId(shop.getId())
                    .shopName(shop.getName())
                    .logoUrl(shop.getLogoUrl())
                    .description(shop.getDescription())
                    .build());
        }
        return result;
    }

    public FollowStatusVO status(Long userId, Long shopId) {
        Long count = followMapper.selectCount(
                new LambdaQueryWrapper<UserShopFollow>()
                        .eq(UserShopFollow::getUserId, userId)
                        .eq(UserShopFollow::getShopId, shopId)
        );
        return new FollowStatusVO(count > 0);
    }

    private Shop requireActiveShop(Long shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null || shop.getStatus() == null || shop.getStatus() == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "店铺不存在");
        }
        return shop;
    }
}
