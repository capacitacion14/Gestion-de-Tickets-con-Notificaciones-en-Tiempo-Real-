package com.banco.ticketero.infrastructure.adapter.in.web;

import com.banco.ticketero.application.dto.response.QueueStatusResponse;
import com.banco.ticketero.application.usecase.queue.GetQueueStatusUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/queues")
@RequiredArgsConstructor
@Slf4j
public class QueueController {
    
    private final GetQueueStatusUseCase getQueueStatusUseCase;
    
    @GetMapping("/{queueType}/status")
    public ResponseEntity<QueueStatusResponse> getQueueStatus(@PathVariable String queueType) {
        log.info("Getting queue status for: {}", queueType);
        QueueStatusResponse response = getQueueStatusUseCase.execute(queueType);
        return ResponseEntity.ok(response);
    }
}