package com.banco.ticketero.domain.repository;

import com.banco.ticketero.domain.model.customer.Customer;
import com.banco.ticketero.domain.model.customer.CustomerId;
import com.banco.ticketero.domain.model.customer.NationalId;
import com.banco.ticketero.domain.model.notification.Notification;
import com.banco.ticketero.domain.model.notification.NotificationId;
import com.banco.ticketero.domain.model.notification.NotificationStatus;
import com.banco.ticketero.domain.model.notification.NotificationType;
import com.banco.ticketero.domain.model.queue.Queue;
import com.banco.ticketero.domain.model.queue.QueueId;
import com.banco.ticketero.domain.model.queue.QueueType;
import com.banco.ticketero.domain.model.ticket.Ticket;
import com.banco.ticketero.domain.model.ticket.TicketCode;
import com.banco.ticketero.domain.model.ticket.TicketId;
import com.banco.ticketero.domain.model.ticket.TicketStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests de contrato para validar las interfaces de repositorio.
 * Verifica que las interfaces están bien definidas y pueden ser mockeadas.
 */
@DisplayName("Repository Contract Tests")
class RepositoryContractTest {

    @Test
    @DisplayName("CustomerRepository should have all required methods")
    void customerRepositoryShouldHaveAllRequiredMethods() {
        // Given
        CustomerRepository repository = mock(CustomerRepository.class);
        Customer customer = Customer.create(NationalId.of("12345678"), "John", "Doe");
        CustomerId customerId = CustomerId.generate();
        NationalId nationalId = NationalId.of("87654321");
        
        // When - Configure mocks
        when(repository.save(any(Customer.class))).thenReturn(customer);
        when(repository.findById(any(CustomerId.class))).thenReturn(Optional.of(customer));
        when(repository.findByNationalId(any(NationalId.class))).thenReturn(Optional.of(customer));
        when(repository.findByTelegramChatId(anyLong())).thenReturn(Optional.of(customer));
        when(repository.existsByNationalId(any(NationalId.class))).thenReturn(true);
        when(repository.findVipCustomers()).thenReturn(List.of(customer));
        when(repository.findCustomersWithNotificationCapability()).thenReturn(List.of(customer));
        when(repository.count()).thenReturn(1L);
        
        // Then - Verify all methods can be called
        assertNotNull(repository.save(customer));
        assertTrue(repository.findById(customerId).isPresent());
        assertTrue(repository.findByNationalId(nationalId).isPresent());
        assertTrue(repository.findByTelegramChatId(123456789L).isPresent());
        assertTrue(repository.existsByNationalId(nationalId));
        assertFalse(repository.findVipCustomers().isEmpty());
        assertFalse(repository.findCustomersWithNotificationCapability().isEmpty());
        assertEquals(1L, repository.count());
        
        // Verify void methods don't throw
        assertDoesNotThrow(() -> repository.deleteById(customerId));
    }

    @Test
    @DisplayName("QueueRepository should have all required methods")
    void queueRepositoryShouldHaveAllRequiredMethods() {
        // Given
        QueueRepository repository = mock(QueueRepository.class);
        Queue queue = Queue.create(QueueType.GENERAL);
        QueueId queueId = QueueId.generate();
        
        // When - Configure mocks
        when(repository.save(any(Queue.class))).thenReturn(queue);
        when(repository.findById(any(QueueId.class))).thenReturn(Optional.of(queue));
        when(repository.findByQueueType(any(QueueType.class))).thenReturn(Optional.of(queue));
        when(repository.findActiveQueuesOrderedByPriority()).thenReturn(List.of(queue));
        when(repository.findAll()).thenReturn(List.of(queue));
        when(repository.existsByQueueType(any(QueueType.class))).thenReturn(true);
        when(repository.findHighPriorityActiveQueues()).thenReturn(List.of(queue));
        when(repository.countActiveQueues()).thenReturn(1L);
        
        // Then - Verify all methods can be called
        assertNotNull(repository.save(queue));
        assertTrue(repository.findById(queueId).isPresent());
        assertTrue(repository.findByQueueType(QueueType.GENERAL).isPresent());
        assertFalse(repository.findActiveQueuesOrderedByPriority().isEmpty());
        assertFalse(repository.findAll().isEmpty());
        assertTrue(repository.existsByQueueType(QueueType.GENERAL));
        assertFalse(repository.findHighPriorityActiveQueues().isEmpty());
        assertEquals(1L, repository.countActiveQueues());
        
        // Verify void methods don't throw
        assertDoesNotThrow(() -> repository.deleteById(queueId));
    }

    @Test
    @DisplayName("TicketRepository should have all required methods")
    void ticketRepositoryShouldHaveAllRequiredMethods() {
        // Given
        TicketRepository repository = mock(TicketRepository.class);
        Ticket ticket = Ticket.create(CustomerId.generate(), QueueType.GENERAL, TicketCode.fromSequence(1001));
        TicketId ticketId = TicketId.generate();
        CustomerId customerId = CustomerId.generate();
        
        // When - Configure mocks
        when(repository.save(any(Ticket.class))).thenReturn(ticket);
        when(repository.findById(any(TicketId.class))).thenReturn(Optional.of(ticket));
        when(repository.findByTicketCode(any(TicketCode.class))).thenReturn(Optional.of(ticket));
        when(repository.findByCustomerId(any(CustomerId.class))).thenReturn(List.of(ticket));
        when(repository.findByStatus(any(TicketStatus.class))).thenReturn(List.of(ticket));
        when(repository.findByQueueTypeAndStatus(any(QueueType.class), any(TicketStatus.class))).thenReturn(List.of(ticket));
        when(repository.findPendingTicketsByQueueTypeOrderedByPosition(any(QueueType.class))).thenReturn(List.of(ticket));
        when(repository.findNextTicketToCall(any(QueueType.class))).thenReturn(Optional.of(ticket));
        when(repository.findActiveTicketsByCustomerId(any(CustomerId.class))).thenReturn(List.of(ticket));
        when(repository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(ticket));
        when(repository.countByStatus(any(TicketStatus.class))).thenReturn(1L);
        when(repository.countByQueueTypeAndStatus(any(QueueType.class), any(TicketStatus.class))).thenReturn(1L);
        when(repository.findExpiredTickets()).thenReturn(List.of(ticket));
        when(repository.existsByTicketCode(any(TicketCode.class))).thenReturn(true);
        when(repository.findMaxPositionInQueue(any(QueueType.class))).thenReturn(Optional.of(5));
        
        // Then - Verify all methods can be called
        assertNotNull(repository.save(ticket));
        assertTrue(repository.findById(ticketId).isPresent());
        assertTrue(repository.findByTicketCode(TicketCode.fromSequence(1001)).isPresent());
        assertFalse(repository.findByCustomerId(customerId).isEmpty());
        assertFalse(repository.findByStatus(TicketStatus.PENDING).isEmpty());
        assertFalse(repository.findByQueueTypeAndStatus(QueueType.GENERAL, TicketStatus.PENDING).isEmpty());
        assertFalse(repository.findPendingTicketsByQueueTypeOrderedByPosition(QueueType.GENERAL).isEmpty());
        assertTrue(repository.findNextTicketToCall(QueueType.GENERAL).isPresent());
        assertFalse(repository.findActiveTicketsByCustomerId(customerId).isEmpty());
        assertFalse(repository.findByCreatedAtBetween(LocalDateTime.now().minusHours(1), LocalDateTime.now()).isEmpty());
        assertEquals(1L, repository.countByStatus(TicketStatus.PENDING));
        assertEquals(1L, repository.countByQueueTypeAndStatus(QueueType.GENERAL, TicketStatus.PENDING));
        assertFalse(repository.findExpiredTickets().isEmpty());
        assertTrue(repository.existsByTicketCode(TicketCode.fromSequence(1001)));
        assertTrue(repository.findMaxPositionInQueue(QueueType.GENERAL).isPresent());
        
        // Verify void methods don't throw
        assertDoesNotThrow(() -> repository.deleteById(ticketId));
    }

    @Test
    @DisplayName("NotificationRepository should have all required methods")
    void notificationRepositoryShouldHaveAllRequiredMethods() {
        // Given
        NotificationRepository repository = mock(NotificationRepository.class);
        Notification notification = Notification.createImmediate(
            TicketId.generate(), 
            CustomerId.generate(), 
            NotificationType.TICKET_CREATED, 
            "Test message"
        );
        NotificationId notificationId = NotificationId.generate();
        TicketId ticketId = TicketId.generate();
        CustomerId customerId = CustomerId.generate();
        
        // When - Configure mocks
        when(repository.save(any(Notification.class))).thenReturn(notification);
        when(repository.findById(any(NotificationId.class))).thenReturn(Optional.of(notification));
        when(repository.findByTicketId(any(TicketId.class))).thenReturn(List.of(notification));
        when(repository.findByCustomerId(any(CustomerId.class))).thenReturn(List.of(notification));
        when(repository.findByStatus(any(NotificationStatus.class))).thenReturn(List.of(notification));
        when(repository.findByType(any(NotificationType.class))).thenReturn(List.of(notification));
        when(repository.findPendingNotificationsReadyToSend()).thenReturn(List.of(notification));
        when(repository.findFailedNotificationsForRetry()).thenReturn(List.of(notification));
        when(repository.findByScheduledAtBefore(any(LocalDateTime.class))).thenReturn(List.of(notification));
        when(repository.findUrgentPendingNotifications()).thenReturn(List.of(notification));
        when(repository.findByCustomerIdAndType(any(CustomerId.class), any(NotificationType.class))).thenReturn(List.of(notification));
        when(repository.countByStatus(any(NotificationStatus.class))).thenReturn(1L);
        when(repository.countFailedNotificationsByCustomerId(any(CustomerId.class))).thenReturn(1L);
        when(repository.findExpiredNotifications()).thenReturn(List.of(notification));
        when(repository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of(notification));
        
        // Then - Verify all methods can be called
        assertNotNull(repository.save(notification));
        assertTrue(repository.findById(notificationId).isPresent());
        assertFalse(repository.findByTicketId(ticketId).isEmpty());
        assertFalse(repository.findByCustomerId(customerId).isEmpty());
        assertFalse(repository.findByStatus(NotificationStatus.PENDING).isEmpty());
        assertFalse(repository.findByType(NotificationType.TICKET_CREATED).isEmpty());
        assertFalse(repository.findPendingNotificationsReadyToSend().isEmpty());
        assertFalse(repository.findFailedNotificationsForRetry().isEmpty());
        assertFalse(repository.findByScheduledAtBefore(LocalDateTime.now()).isEmpty());
        assertFalse(repository.findUrgentPendingNotifications().isEmpty());
        assertFalse(repository.findByCustomerIdAndType(customerId, NotificationType.TICKET_CREATED).isEmpty());
        assertEquals(1L, repository.countByStatus(NotificationStatus.PENDING));
        assertEquals(1L, repository.countFailedNotificationsByCustomerId(customerId));
        assertFalse(repository.findExpiredNotifications().isEmpty());
        assertFalse(repository.findByCreatedAtBetween(LocalDateTime.now().minusHours(1), LocalDateTime.now()).isEmpty());
        
        // Verify void methods don't throw
        assertDoesNotThrow(() -> repository.deleteById(notificationId));
        assertDoesNotThrow(() -> repository.deleteExpiredNotifications());
    }

    @Test
    @DisplayName("All repository interfaces should be properly defined")
    void allRepositoryInterfacesShouldBeProperlyDefined() {
        // Verify interfaces exist and can be instantiated as mocks
        assertDoesNotThrow(() -> mock(CustomerRepository.class));
        assertDoesNotThrow(() -> mock(QueueRepository.class));
        assertDoesNotThrow(() -> mock(TicketRepository.class));
        assertDoesNotThrow(() -> mock(NotificationRepository.class));
    }
}