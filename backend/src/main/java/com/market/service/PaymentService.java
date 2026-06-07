package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.common.BusinessException;
import com.market.common.OrderStatus;
import com.market.common.PaymentChannel;
import com.market.common.PaymentStatus;
import com.market.common.ResultCode;
import com.market.config.PaymentProperties;
import com.market.dto.order.OrderVO;
import com.market.dto.payment.PayResultVO;
import com.market.dto.payment.PaymentChannelVO;
import com.market.dto.payment.PaymentCallbackRequest;
import com.market.entity.PaymentRecord;
import com.market.entity.UserOrder;
import com.market.mapper.PaymentRecordMapper;
import com.market.mapper.UserOrderMapper;
import com.market.service.payment.PaymentProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRecordMapper paymentRecordMapper;
    private final UserOrderMapper orderMapper;
    private final OrderService orderService;
    private final PaymentProperties paymentProperties;
    private final ObjectMapper objectMapper;
    private final Map<PaymentChannel, PaymentProvider> providerMap;

    public PaymentService(PaymentRecordMapper paymentRecordMapper,
                          UserOrderMapper orderMapper,
                          OrderService orderService,
                          PaymentProperties paymentProperties,
                          ObjectMapper objectMapper,
                          List<PaymentProvider> providers) {
        this.paymentRecordMapper = paymentRecordMapper;
        this.orderMapper = orderMapper;
        this.orderService = orderService;
        this.paymentProperties = paymentProperties;
        this.objectMapper = objectMapper;
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(PaymentProvider::channel, Function.identity()));
    }

    public List<PaymentChannelVO> listChannels() {
        List<PaymentChannelVO> channels = new ArrayList<>();
        if (paymentProperties.isMockEnabled()) {
            channels.add(channelVO(PaymentChannel.MOCK));
        }
        if (paymentProperties.isWechatEnabled()) {
            channels.add(channelVO(PaymentChannel.WECHAT));
        }
        if (paymentProperties.isAlipayEnabled()) {
            channels.add(channelVO(PaymentChannel.ALIPAY));
        }
        return channels;
    }

    @Transactional
    public PayResultVO pay(Long userId, Long orderId, String channelCode) {
        PaymentChannel channel = PaymentChannel.fromCode(channelCode);
        ensureChannelEnabled(channel);
        PaymentProvider provider = requireProvider(channel);

        UserOrder order = requirePendingOrder(userId, orderId);
        PaymentRecord existing = findRecord(orderId, channel.getCode());
        if (existing != null && existing.getPayStatus() == PaymentStatus.SUCCESS.getCode()) {
            return successResult(channel, existing.getTradeNo(), orderService.getMine(userId, orderId));
        }

        String tradeNo = provider.createTradeNo(order);
        PaymentRecord record = existing != null ? existing : new PaymentRecord();
        record.setOrderId(orderId);
        record.setChannel(channel.getCode());
        record.setTradeNo(tradeNo);
        record.setPayAmount(order.getPayAmount());
        record.setPayStatus(PaymentStatus.PENDING.getCode());
        if (record.getId() == null) {
            paymentRecordMapper.insert(record);
        } else {
            paymentRecordMapper.updateById(record);
        }

        if (channel == PaymentChannel.MOCK) {
            return completePayment(userId, channel, tradeNo, order.getPayAmount(), paymentProperties.getSandboxSecret());
        }

        return PayResultVO.builder()
                .status("pending")
                .channel(channel.getCode())
                .channelLabel(channel.getLabel())
                .tradeNo(tradeNo)
                .sandboxHint("开发环境：点击「完成沙箱支付」模拟第三方回调")
                .order(orderService.getMine(userId, orderId))
                .build();
    }

    @Transactional
    public PayResultVO completeSandbox(Long userId, Long orderId, String tradeNo, String channelCode) {
        PaymentChannel channel = PaymentChannel.fromCode(channelCode);
        requirePendingOrder(userId, orderId);
        return completePayment(userId, channel, tradeNo, null, paymentProperties.getSandboxSecret());
    }

    @Transactional
    public void handleCallback(String channelCode, PaymentCallbackRequest request) {
        PaymentChannel channel = PaymentChannel.fromCode(channelCode);
        PaymentProvider provider = requireProvider(channel);
        if (!provider.verifyCallbackSign(request.getTradeNo(), request.getSign(), paymentProperties.getSandboxSecret())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "支付回调签名校验失败");
        }
        UserOrder order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        completePayment(order.getUserId(), channel, request.getTradeNo(), request.getPayAmount(),
                paymentProperties.getSandboxSecret());
    }

    @Transactional
    public PayResultVO completePayment(Long userId, PaymentChannel channel, String tradeNo,
                                       java.math.BigDecimal payAmount, String sign) {
        PaymentProvider provider = requireProvider(channel);
        if (!provider.verifyCallbackSign(tradeNo, sign, paymentProperties.getSandboxSecret())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "支付签名校验失败");
        }

        PaymentRecord record = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getTradeNo, tradeNo)
                        .eq(PaymentRecord::getChannel, channel.getCode())
        );
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "支付记录不存在");
        }

        UserOrder order = orderMapper.selectById(record.getOrderId());
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作该订单");
        }

        if (record.getPayStatus() == PaymentStatus.SUCCESS.getCode()) {
            return successResult(channel, tradeNo, orderService.getMine(userId, order.getId()));
        }

        if (payAmount != null && payAmount.compareTo(record.getPayAmount()) != 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "支付金额不一致");
        }

        if (order.getStatus() != OrderStatus.PENDING_PAY.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "订单状态不允许支付");
        }

        OrderVO orderVO = orderService.completePayment(userId, order.getId());

        record.setPayStatus(PaymentStatus.SUCCESS.getCode());
        record.setPaidAt(LocalDateTime.now());
        try {
            record.setCallbackRaw(objectMapper.writeValueAsString(Map.of(
                    "tradeNo", tradeNo,
                    "channel", channel.getCode(),
                    "completedAt", record.getPaidAt().toString()
            )));
        } catch (Exception ignored) {
            record.setCallbackRaw("{}");
        }
        paymentRecordMapper.updateById(record);

        return successResult(channel, tradeNo, orderVO);
    }

    private PayResultVO successResult(PaymentChannel channel, String tradeNo, OrderVO order) {
        return PayResultVO.builder()
                .status("success")
                .channel(channel.getCode())
                .channelLabel(channel.getLabel())
                .tradeNo(tradeNo)
                .order(order)
                .build();
    }

    private UserOrder requirePendingOrder(Long userId, Long orderId) {
        UserOrder order = orderMapper.selectById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAY.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "订单状态不允许支付");
        }
        return order;
    }

    private PaymentRecord findRecord(Long orderId, String channel) {
        return paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getOrderId, orderId)
                        .eq(PaymentRecord::getChannel, channel)
        );
    }

    private void ensureChannelEnabled(PaymentChannel channel) {
        boolean enabled;
        switch (channel) {
            case MOCK:
                enabled = paymentProperties.isMockEnabled();
                break;
            case WECHAT:
                enabled = paymentProperties.isWechatEnabled();
                break;
            case ALIPAY:
                enabled = paymentProperties.isAlipayEnabled();
                break;
            default:
                enabled = false;
        }
        if (!enabled) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "支付渠道未启用：" + channel.getLabel());
        }
    }

    private PaymentProvider requireProvider(PaymentChannel channel) {
        PaymentProvider provider = providerMap.get(channel);
        if (provider == null) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR);
        }
        return provider;
    }

    private PaymentChannelVO channelVO(PaymentChannel channel) {
        return PaymentChannelVO.builder()
                .code(channel.getCode())
                .label(channel.getLabel())
                .build();
    }
}
