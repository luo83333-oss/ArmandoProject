package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.common.BusinessException;
import com.market.common.OrderStatus;
import com.market.common.ResultCode;
import com.market.dto.order.AddressRequest;
import com.market.dto.order.CheckoutRequest;
import com.market.dto.order.OrderItemVO;
import com.market.dto.order.OrderVO;
import com.market.entity.CartItem;
import com.market.entity.OrderItem;
import com.market.entity.Product;
import com.market.entity.ProductSku;
import com.market.entity.Shop;
import com.market.entity.UserOrder;
import com.market.mapper.CartItemMapper;
import com.market.mapper.OrderItemMapper;
import com.market.mapper.ProductMapper;
import com.market.mapper.ProductSkuMapper;
import com.market.mapper.ShopMapper;
import com.market.mapper.UserOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.0500");

    private final UserOrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductSkuMapper skuMapper;
    private final ProductMapper productMapper;
    private final ShopMapper shopMapper;
    private final CartService cartService;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<OrderVO> checkout(Long userId, CheckoutRequest request) {
        List<CartItem> selected = cartService.listSelectedForCheckout(userId);
        if (selected.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "请先选择要结算的商品");
        }

        Map<Long, List<CartItem>> byShop = new HashMap<>();
        for (CartItem cartItem : selected) {
            ProductSku sku = skuMapper.selectById(cartItem.getSkuId());
            Product product = productMapper.selectById(sku.getProductId());
            if (product.getShelfStatus() != 1) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "商品已下架：" + product.getTitle());
            }
            if (cartItem.getQuantity() > sku.getStock()) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "库存不足：" + product.getTitle());
            }
            byShop.computeIfAbsent(product.getShopId(), k -> new ArrayList<>()).add(cartItem);
        }

        String addressJson = toJson(request.getAddress());
        List<OrderVO> created = new ArrayList<>();
        for (Map.Entry<Long, List<CartItem>> entry : byShop.entrySet()) {
            created.add(createOrderForShop(userId, entry.getKey(), entry.getValue(), addressJson));
        }
        return created;
    }

    public List<OrderVO> listMine(Long userId) {
        List<UserOrder> orders = orderMapper.selectList(
                new LambdaQueryWrapper<UserOrder>()
                        .eq(UserOrder::getUserId, userId)
                        .orderByDesc(UserOrder::getCreatedAt)
        );
        return orders.stream().map(this::toVO).collect(Collectors.toList());
    }

    public OrderVO getMine(Long userId, Long orderId) {
        UserOrder order = requireUserOrder(userId, orderId);
        return toVO(order);
    }

    @Transactional
    public OrderVO mockPay(Long userId, Long orderId) {
        UserOrder order = requireUserOrder(userId, orderId);
        if (order.getStatus() != OrderStatus.PENDING_PAY.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "订单状态不允许支付");
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
        for (OrderItem item : items) {
            ProductSku sku = skuMapper.selectById(item.getSkuId());
            if (sku.getStock() < item.getQuantity()) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "库存不足，无法支付");
            }
            sku.setStock(sku.getStock() - item.getQuantity());
            skuMapper.updateById(sku);
        }

        order.setStatus(OrderStatus.PAID.getCode());
        order.setPaidAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return toVO(order);
    }

    private OrderVO createOrderForShop(Long userId, Long shopId, List<CartItem> cartItems, String addressJson) {
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            ProductSku sku = skuMapper.selectById(cartItem.getSkuId());
            Product product = productMapper.selectById(sku.getProductId());
            BigDecimal lineAmount = sku.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(lineAmount);

            OrderItem oi = new OrderItem();
            oi.setProductId(product.getId());
            oi.setSkuId(sku.getId());
            oi.setProductTitle(product.getTitle());
            oi.setSpecJson(sku.getSpecJson());
            oi.setQuantity(cartItem.getQuantity());
            oi.setUnitPrice(sku.getPrice());
            orderItems.add(oi);
        }

        BigDecimal commissionAmount = total.multiply(DEFAULT_COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);

        UserOrder order = new UserOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setTotalAmount(total);
        order.setPayAmount(total);
        order.setCommissionRate(DEFAULT_COMMISSION_RATE);
        order.setCommissionAmount(commissionAmount);
        order.setStatus(OrderStatus.PENDING_PAY.getCode());
        order.setAddressJson(addressJson);
        orderMapper.insert(order);

        for (OrderItem oi : orderItems) {
            oi.setOrderId(order.getId());
            orderItemMapper.insert(oi);
        }

        for (CartItem cartItem : cartItems) {
            cartItemMapper.deleteById(cartItem.getId());
        }

        return toVO(order);
    }

    private UserOrder requireUserOrder(Long userId, Long orderId) {
        UserOrder order = orderMapper.selectById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        return order;
    }

    private OrderVO toVO(UserOrder order) {
        Shop shop = shopMapper.selectById(order.getShopId());
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
        );
        OrderStatus status = findStatus(order.getStatus());
        return OrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .shopId(order.getShopId())
                .shopName(shop != null ? shop.getName() : null)
                .totalAmount(order.getTotalAmount())
                .payAmount(order.getPayAmount())
                .status(order.getStatus())
                .statusLabel(status != null ? status.getLabel() : "未知")
                .address(parseAddress(order.getAddressJson()))
                .items(items.stream().map(i -> OrderItemVO.builder()
                        .id(i.getId())
                        .productId(i.getProductId())
                        .skuId(i.getSkuId())
                        .productTitle(i.getProductTitle())
                        .specJson(i.getSpecJson())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build()).collect(Collectors.toList()))
                .paidAt(order.getPaidAt())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private OrderStatus findStatus(int code) {
        for (OrderStatus s : OrderStatus.values()) {
            if (s.getCode() == code) {
                return s;
            }
        }
        return null;
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        return time + rand;
    }

    private String toJson(AddressRequest address) {
        try {
            return objectMapper.writeValueAsString(address);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR);
        }
    }

    private Map<String, Object> parseAddress(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
}
