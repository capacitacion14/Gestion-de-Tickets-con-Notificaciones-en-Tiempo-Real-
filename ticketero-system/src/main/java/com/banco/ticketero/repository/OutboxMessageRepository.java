package com.banco.ticketero.repository;

import com.banco.ticketero.model.entity.OutboxMessage;
import com.banco.ticketero.model.entity.OutboxMessage.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {

    List<OutboxMessage> findByEstadoEnvioAndFechaProgramadaBefore(
        MessageStatus estadoEnvio, 
        LocalDateTime fechaProgramada
    );

    List<OutboxMessage> findByTicketId(UUID ticketId);
    
    List<OutboxMessage> findByChatId(String chatId);
    
    List<OutboxMessage> findByChatIdOrderByIdAsc(String chatId);

    long countByEstadoEnvio(MessageStatus estadoEnvio);
}
