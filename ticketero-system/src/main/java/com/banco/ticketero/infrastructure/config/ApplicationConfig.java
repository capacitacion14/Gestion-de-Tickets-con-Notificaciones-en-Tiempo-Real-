package com.banco.ticketero.infrastructure.config;

import com.banco.ticketero.application.usecase.queue.GetQueueStatusUseCase;
import com.banco.ticketero.application.usecase.ticket.CreateTicketUseCase;
import com.banco.ticketero.application.usecase.ticket.GetTicketUseCase;
import com.banco.ticketero.domain.repository.CustomerRepository;
import com.banco.ticketero.domain.repository.QueueRepository;
import com.banco.ticketero.domain.repository.TicketRepository;
import com.banco.ticketero.domain.service.QueueDomainService;
import com.banco.ticketero.domain.service.TicketDomainService;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EntityScan("com.banco.ticketero.infrastructure.adapter.out.persistence.entity")
@EnableJpaRepositories("com.banco.ticketero.infrastructure.adapter.out.persistence.repository")
@ComponentScan("com.banco.ticketero")
public class ApplicationConfig {
    
    @Bean
    public CreateTicketUseCase createTicketUseCase(
            TicketRepository ticketRepository,
            CustomerRepository customerRepository,
            QueueRepository queueRepository,
            TicketDomainService ticketDomainService,
            QueueDomainService queueDomainService) {
        return new CreateTicketUseCase(ticketRepository, customerRepository, queueRepository, 
                ticketDomainService, queueDomainService);
    }
    
    @Bean
    public GetTicketUseCase getTicketUseCase(TicketRepository ticketRepository) {
        return new GetTicketUseCase(ticketRepository);
    }
    
    @Bean
    public GetQueueStatusUseCase getQueueStatusUseCase(
            QueueRepository queueRepository,
            TicketRepository ticketRepository) {
        return new GetQueueStatusUseCase(queueRepository, ticketRepository);
    }
    
    @Bean
    public TicketDomainService ticketDomainService(QueueDomainService queueDomainService) {
        return new TicketDomainService(queueDomainService);
    }
    
    @Bean
    public QueueDomainService queueDomainService() {
        return new QueueDomainService();
    }
}