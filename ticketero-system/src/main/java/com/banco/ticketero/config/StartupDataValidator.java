package com.banco.ticketero.config;

import com.banco.ticketero.repository.AdvisorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupDataValidator implements ApplicationRunner {

    private final AdvisorRepository advisorRepository;

    @Override
    public void run(ApplicationArguments args) {
        long availableAdvisors = advisorRepository.countByStatus("AVAILABLE");
        long totalAdvisors = advisorRepository.count();
        
        log.info("=== STARTUP DATA VALIDATION ===");
        log.info("Total advisors in database: {}", totalAdvisors);
        log.info("Available advisors: {}", availableAdvisors);
        
        if (availableAdvisors == 0) {
            log.error("⚠️  NO AVAILABLE ADVISORS FOUND! Tickets won't be processed.");
        } else {
            log.info("✅ System ready - {} advisors available to take tickets", availableAdvisors);
        }
        log.info("===============================");
    }
}