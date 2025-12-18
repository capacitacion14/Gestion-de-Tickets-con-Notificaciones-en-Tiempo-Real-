package com.banco.ticketero.model.dto.response;

public record DashboardResponse(
    long totalTickets,
    long waitingTickets,
    long inProgressTickets,
    long completedTickets,
    long availableAdvisors,
    long busyAdvisors,
    long onBreakAdvisors
) {}
