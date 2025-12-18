package com.banco.ticketero.repository;

import com.banco.ticketero.model.QueueType;
import com.banco.ticketero.model.TicketStatus;
import com.banco.ticketero.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findByNumero(String numero);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByQueueTypeAndStatusIn(QueueType queueType, List<TicketStatus> statuses);

    @Query("""
        SELECT COUNT(t) FROM Ticket t
        WHERE t.queueType = :queueType
        AND t.status IN ('EN_ESPERA', 'PROXIMO')
        AND t.createdAt < :createdAt
        """)
    long countPositionInQueue(@Param("queueType") QueueType queueType, 
                              @Param("createdAt") LocalDateTime createdAt);

    @Query("""
        SELECT t FROM Ticket t
        WHERE t.status IN ('EN_ESPERA', 'PROXIMO')
        ORDER BY 
            CASE t.queueType
                WHEN 'GERENCIA' THEN 4
                WHEN 'EMPRESAS' THEN 3
                WHEN 'PERSONAL_BANKER' THEN 2
                WHEN 'CAJA' THEN 1
            END DESC,
            t.createdAt ASC
        """)
    List<Ticket> findNextTicketsToAssign();

    List<Ticket> findByStatusAndExpiresAtBefore(TicketStatus status, LocalDateTime expiresAt);

    long countByStatus(TicketStatus status);

    long countByQueueTypeAndStatus(QueueType queueType, TicketStatus status);

    List<Ticket> findByQueueTypeAndStatusOrderByCreatedAtAsc(QueueType queueType, TicketStatus status);

    Optional<Ticket> findByCodigoReferencia(UUID codigoReferencia);
    
    List<Ticket> findByStatusOrderByCreatedAtAsc(TicketStatus status);
    
    long countByQueueTypeAndStatusAndCreatedAtBefore(QueueType queueType, TicketStatus status, LocalDateTime createdAt);
}
