package com.banco.ticketero.repository;

import com.banco.ticketero.model.entity.Advisor;
import com.banco.ticketero.model.entity.Advisor.AdvisorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdvisorRepository extends JpaRepository<Advisor, Long> {

    List<Advisor> findByStatus(AdvisorStatus status);

    @Query("""
        SELECT a FROM Advisor a
        WHERE a.status = 'AVAILABLE'
        AND :queueType MEMBER OF a.supportedQueues
        ORDER BY a.assignedTicketsCount ASC, a.lastAssignmentAt ASC NULLS FIRST
        """)
    List<Advisor> findAvailableAdvisorsForQueue(@Param("queueType") String queueType);

    long countByStatus(AdvisorStatus status);
}
