package com.banco.ticketero.controller;

import com.banco.ticketero.model.dto.request.CreateTicketRequest;
import com.banco.ticketero.model.dto.response.PositionResponse;
import com.banco.ticketero.model.dto.response.TicketResponse;
import com.banco.ticketero.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request) {
        log.info("POST /api/tickets - nationalId: {}, queue: {}", request.nationalId(), request.queueType());
        TicketResponse response = ticketService.create(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{codigoReferencia}")
    public ResponseEntity<TicketResponse> getByCodigoReferencia(@PathVariable UUID codigoReferencia) {
        log.info("GET /api/tickets/{}", codigoReferencia);
        return ticketService.findByCodigoReferencia(codigoReferencia)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/numero/{numero}")
    public ResponseEntity<TicketResponse> getByNumero(@PathVariable String numero) {
        log.info("GET /api/tickets/numero/{}", numero);
        return ticketService.findByNumero(numero)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{codigoReferencia}/position")
    public ResponseEntity<PositionResponse> getPosition(@PathVariable UUID codigoReferencia) {
        log.info("GET /api/tickets/{}/position", codigoReferencia);
        PositionResponse response = ticketService.calculatePosition(codigoReferencia);
        return ResponseEntity.ok(response);
    }
}
