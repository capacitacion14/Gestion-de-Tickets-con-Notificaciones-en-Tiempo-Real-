package com.banco.ticketero.controller;

import com.banco.ticketero.model.dto.request.UpdateAdvisorStatusRequest;
import com.banco.ticketero.model.dto.response.DashboardResponse;
import com.banco.ticketero.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        log.info("GET /api/admin/dashboard");
        DashboardResponse response = adminService.getDashboard();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/queues/{type}")
    public ResponseEntity<?> getQueueDetail(@PathVariable String type) {
        log.info("GET /api/admin/queues/[SANITIZED]");
        
        try {
            var tickets = adminService.getQueueByType(type);
            return ResponseEntity.ok(tickets);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid queue type requested");
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/advisors/{id}/status")
    public ResponseEntity<Map<String, String>> updateAdvisorStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateAdvisorStatusRequest request
    ) {
        log.info("PUT /api/admin/advisors/{}/status", id);
        
        try {
            adminService.updateAdvisorStatus(id, request.status());
            return ResponseEntity.ok(Map.of(
                "id", id.toString(),
                "status", request.status(),
                "message", "Status updated successfully"
            ));
        } catch (RuntimeException e) {
            log.error("Error updating advisor status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/advisors/stats")
    public ResponseEntity<?> getAdvisorStats() {
        log.info("GET /api/admin/advisors/stats");
        var advisors = adminService.getAllAdvisors();
        return ResponseEntity.ok(advisors);
    }
}
