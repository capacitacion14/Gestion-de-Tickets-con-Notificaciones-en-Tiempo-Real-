package com.banco.ticketero.controller;

import com.banco.ticketero.model.entity.OutboxMessage;
import com.banco.ticketero.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/outbox")
@RequiredArgsConstructor
public class TestController {

    private final OutboxMessageRepository outboxRepository;

    @GetMapping("/{chatId}")
    public List<OutboxMessage> getMessagesByChatId(@PathVariable String chatId) {
        return outboxRepository.findByChatIdOrderByIdAsc(chatId);
    }

    @GetMapping("/all")
    public List<OutboxMessage> getAllMessages() {
        return outboxRepository.findAll();
    }
}