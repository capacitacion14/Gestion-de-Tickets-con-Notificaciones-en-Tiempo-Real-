package com.banco.ticketero.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para la creación de un nuevo ticket.
 */
public record CreateTicketRequest(
    
    @NotBlank(message = "National ID is required")
    @Pattern(regexp = "^[0-9]{8,20}$", message = "National ID must be 8-20 digits")
    String nationalId,
    
    @NotNull(message = "Queue type is required")
    String queueType
    
) {
    
    /**
     * Constructor compacto para validaciones adicionales.
     */
    public CreateTicketRequest {
        if (nationalId != null && !nationalId.trim().isEmpty()) {
            nationalId = nationalId.trim().replaceAll("[^0-9]", "");
        }
        if (queueType != null && !queueType.trim().isEmpty()) {
            queueType = queueType.trim().toUpperCase(java.util.Locale.ROOT);
            // Validate queue type
            if (!isValidQueueType(queueType)) {
                throw new IllegalArgumentException("Invalid queue type: " + queueType + ". Valid types are: GENERAL, VIP, PRIORITY");
            }
        }
    }
    
    private static boolean isValidQueueType(String queueType) {
        return "GENERAL".equals(queueType) || "VIP".equals(queueType) || "PRIORITY".equals(queueType);
    }
}