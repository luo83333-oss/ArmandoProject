package com.market.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.common.BusinessException;
import com.market.common.ResultCode;
import com.market.dto.message.MessageVO;
import com.market.dto.message.UnreadCountVO;
import com.market.entity.SysMessage;
import com.market.mapper.SysMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final SysMessageMapper messageMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void sendOrderNotice(Long userId, String msgType, String title, Long orderId, String body) {
        if (userId == null) {
            return;
        }
        SysMessage message = new SysMessage();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(encodeContent(orderId, body));
        message.setMsgType(msgType);
        message.setReadFlag(0);
        messageMapper.insert(message);
    }

    public List<MessageVO> listMine(Long userId) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<SysMessage>()
                        .eq(SysMessage::getUserId, userId)
                        .orderByDesc(SysMessage::getCreatedAt)
        ).stream().map(this::toVO).collect(Collectors.toList());
    }

    public MessageVO getMine(Long userId, Long messageId) {
        SysMessage message = requireMessage(userId, messageId);
        return toVO(message);
    }

    public UnreadCountVO unreadCount(Long userId) {
        Long count = messageMapper.selectCount(
                new LambdaQueryWrapper<SysMessage>()
                        .eq(SysMessage::getUserId, userId)
                        .eq(SysMessage::getReadFlag, 0)
        );
        return new UnreadCountVO(count);
    }

    @Transactional
    public MessageVO markRead(Long userId, Long messageId) {
        SysMessage message = requireMessage(userId, messageId);
        if (message.getReadFlag() == null || message.getReadFlag() == 0) {
            message.setReadFlag(1);
            messageMapper.updateById(message);
        }
        return toVO(message);
    }

    @Transactional
    public void markAllRead(Long userId) {
        messageMapper.update(
                null,
                new LambdaUpdateWrapper<SysMessage>()
                        .eq(SysMessage::getUserId, userId)
                        .eq(SysMessage::getReadFlag, 0)
                        .set(SysMessage::getReadFlag, 1)
        );
    }

    private SysMessage requireMessage(Long userId, Long messageId) {
        SysMessage message = messageMapper.selectById(messageId);
        if (message == null || !userId.equals(message.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "消息不存在");
        }
        return message;
    }

    private MessageVO toVO(SysMessage message) {
        ContentPayload payload = decodeContent(message.getContent());
        return MessageVO.builder()
                .id(message.getId())
                .title(message.getTitle())
                .content(payload.body)
                .msgType(message.getMsgType())
                .read(message.getReadFlag() != null && message.getReadFlag() == 1)
                .orderId(payload.orderId)
                .createdAt(message.getCreatedAt())
                .build();
    }

    private String encodeContent(Long orderId, String body) {
        Map<String, Object> map = new HashMap<>();
        if (orderId != null) {
            map.put("orderId", orderId);
        }
        map.put("body", body);
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.warn("Failed to encode message content, fallback to plain text", e);
            return body;
        }
    }

    private ContentPayload decodeContent(String raw) {
        ContentPayload payload = new ContentPayload();
        if (raw == null) {
            return payload;
        }
        try {
            Map<String, Object> map = objectMapper.readValue(raw, Map.class);
            Object orderId = map.get("orderId");
            if (orderId instanceof Number) {
                payload.orderId = ((Number) orderId).longValue();
            }
            Object body = map.get("body");
            if (body != null) {
                payload.body = String.valueOf(body);
            } else {
                payload.body = raw;
            }
        } catch (Exception e) {
            payload.body = raw;
        }
        return payload;
    }

    private static class ContentPayload {
        private Long orderId;
        private String body;
    }
}
