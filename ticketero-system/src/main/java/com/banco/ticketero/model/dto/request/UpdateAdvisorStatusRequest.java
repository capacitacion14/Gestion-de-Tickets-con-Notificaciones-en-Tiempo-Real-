package com.banco.ticketero.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateAdvisorStatusRequest(
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "AVAILABLE|BUSY|ON_BREAK", message = "Status must be AVAILABLE, BUSY, or ON_BREAK")
    String status
) {}