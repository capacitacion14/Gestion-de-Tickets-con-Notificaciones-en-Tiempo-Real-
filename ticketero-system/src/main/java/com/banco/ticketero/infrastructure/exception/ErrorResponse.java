package com.banco.ticketero.infrastructure.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        String message,
        int status,
        LocalDateTime timestamp,
        Map<String, String> errors
) {
    public ErrorResponse(String message, int status) {
        this(message, status, LocalDateTime.now(), null);
    }
}