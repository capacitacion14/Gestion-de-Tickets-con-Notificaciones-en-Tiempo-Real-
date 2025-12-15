package com.banco.ticketero.infrastructure.adapter.out.persistence.repository;

import com.banco.ticketero.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {
    
    Optional<CustomerEntity> findByNationalId(String nationalId);
    
    boolean existsByNationalId(String nationalId);
}