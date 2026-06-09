package com.market.controller;

import com.market.common.Result;
import com.market.dto.message.MessageVO;
import com.market.dto.message.UnreadCountVO;
import com.market.security.UserContext;
import com.market.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public Result<List<MessageVO>> list() {
        return Result.ok(messageService.listMine(UserContext.getUserId()));
    }

    @GetMapping("/unread-count")
    public Result<UnreadCountVO> unreadCount() {
        return Result.ok(messageService.unreadCount(UserContext.getUserId()));
    }

    @GetMapping("/{id}")
    public Result<MessageVO> detail(@PathVariable Long id) {
        return Result.ok(messageService.getMine(UserContext.getUserId(), id));
    }

    @PostMapping("/{id}/read")
    public Result<MessageVO> markRead(@PathVariable Long id) {
        return Result.ok(messageService.markRead(UserContext.getUserId(), id));
    }

    @PostMapping("/read-all")
    public Result<Void> markAllRead() {
        messageService.markAllRead(UserContext.getUserId());
        return Result.ok();
    }
}
