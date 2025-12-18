package com.banco.ticketero.model.dto.request;

import com.banco.ticketero.model.QueueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
    @NotBlank(message = "National ID is required")
    @Pattern(regexp = "^\\d{8,12}$", message = "National ID must be 8-12 digits")
    String nationalId,

    @Pattern(regexp = "^(\\+56\\d{9}|\\d{8,12})$", message = "Phone must be Chilean format +56XXXXXXXXX or Telegram chat_id")
    String telefono,

    @NotBlank(message = "Branch office is required")
    @Size(max = 100, message = "Branch office max 100 characters")
    String branchOffice,

    @NotNull(message = "Queue type is required")
    QueueType queueType
) {}
