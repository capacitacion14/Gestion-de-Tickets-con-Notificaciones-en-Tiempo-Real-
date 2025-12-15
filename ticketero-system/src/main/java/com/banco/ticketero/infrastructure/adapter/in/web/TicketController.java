package com.banco.ticketero.infrastructure.adapter.in.web;

import com.banco.ticketero.application.dto.request.CreateTicketRequest;
import com.banco.ticketero.application.dto.response.TicketResponse;
import com.banco.ticketero.application.usecase.ticket.CreateTicketUseCase;
import com.banco.ticketero.application.usecase.ticket.GetTicketUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {
    
    private final CreateTicketUseCase createTicketUseCase;
    private final GetTicketUseCase getTicketUseCase;
    
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        log.info("Creating ticket for nationalId: {}", request.nationalId());
        TicketResponse response = createTicketUseCase.execute(request);
        return ResponseEntity.status(201).body(response);
    }
    
    @GetMapping("/{ticketCode}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable String ticketCode) {
        log.info("Getting ticket: {}", ticketCode);
        try {
            TicketResponse response = getTicketUseCase.executeByCode(ticketCode);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}