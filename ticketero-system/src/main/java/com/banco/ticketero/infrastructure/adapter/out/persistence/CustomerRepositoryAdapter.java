package com.banco.ticketero.infrastructure.adapter.out.persistence;

import com.banco.ticketero.domain.model.customer.Customer;
import com.banco.ticketero.domain.model.customer.CustomerId;
import com.banco.ticketero.domain.model.customer.NationalId;
import com.banco.ticketero.domain.repository.CustomerRepository;
import com.banco.ticketero.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import com.banco.ticketero.infrastructure.adapter.out.persistence.mapper.CustomerEntityMapper;
import com.banco.ticketero.infrastructure.adapter.out.persistence.repository.CustomerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepository {
    
    private final CustomerJpaRepository jpaRepository;
    private final CustomerEntityMapper mapper;
    
    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = mapper.toEntity(customer);
        CustomerEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        return jpaRepository.findById(customerId.getValue())
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Customer> findByNationalId(NationalId nationalId) {
        return jpaRepository.findByNationalId(nationalId.getValue())
                .map(mapper::toDomain);
    }
    

    
    @Override
    public boolean existsByNationalId(NationalId nationalId) {
        return jpaRepository.existsByNationalId(nationalId.getValue());
    }
    
    @Override
    public Optional<Customer> findByTelegramChatId(Long telegramChatId) {
        return jpaRepository.findAll()
                .stream()
                .filter(entity -> telegramChatId.equals(entity.getTelegramChatId()))
                .findFirst()
                .map(mapper::toDomain);
    }
    
    @Override
    public List<Customer> findVipCustomers() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .filter(Customer::isVip)
                .toList();
    }
    
    @Override
    public List<Customer> findCustomersWithNotificationCapability() {
        return jpaRepository.findAll()
                .stream()
                .filter(entity -> entity.getTelegramChatId() != null)
                .map(mapper::toDomain)
                .toList();
    }
    
    @Override
    public long count() {
        return jpaRepository.count();
    }
    
    @Override
    public void deleteById(CustomerId customerId) {
        jpaRepository.deleteById(customerId.getValue());
    }
}