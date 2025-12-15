package com.banco.ticketero.infrastructure.adapter.out.persistence.mapper;

import com.banco.ticketero.domain.model.customer.Customer;
import com.banco.ticketero.domain.model.customer.CustomerId;
import com.banco.ticketero.domain.model.customer.NationalId;
import com.banco.ticketero.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerEntityMapper {
    
    public Customer toDomain(CustomerEntity entity) {
        if (entity == null) return null;
        
        return Customer.builder()
                .id(CustomerId.of(entity.getId()))
                .nationalId(NationalId.of(entity.getNationalId()))
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phone(entity.getPhone())
                .telegramChatId(entity.getTelegramChatId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
    
    public CustomerEntity toEntity(Customer domain) {
        if (domain == null) return null;
        
        return CustomerEntity.builder()
                .id(domain.getId() != null ? domain.getId().getValue() : null)
                .nationalId(domain.getNationalId().getValue())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .phone(domain.getPhone())
                .telegramChatId(domain.getTelegramChatId())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}