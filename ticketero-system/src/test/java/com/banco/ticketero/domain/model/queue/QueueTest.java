package com.banco.ticketero.domain.model.queue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Queue Aggregate Root Tests")
class QueueTest {

    @Test
    @DisplayName("Should create queue with default configuration")
    void shouldCreateQueueWithDefaultConfiguration() {
        // When
        Queue queue = Queue.create(QueueType.GENERAL);
        
        // Then
        assertNotNull(queue);
        assertNotNull(queue.getId());
        assertEquals(QueueType.GENERAL, queue.getQueueType());
        assertEquals(50, queue.getMaxCapacity()); // Default for GENERAL
        assertEquals(20, queue.getEstimatedTimeMinutes()); // Default for GENERAL
        assertEquals(4, queue.getPriorityOrder()); // GENERAL priority
        assertTrue(queue.isActive());
        assertNotNull(queue.getCreatedAt());
    }

    @Test
    @DisplayName("Should create queue with custom configuration")
    void shouldCreateQueueWithCustomConfiguration() {
        // Given
        int customCapacity = 25;
        int customTime = 15;
        
        // When
        Queue queue = Queue.create(QueueType.VIP, customCapacity, customTime);
        
        // Then
        assertNotNull(queue);
        assertEquals(QueueType.VIP, queue.getQueueType());
        assertEquals(customCapacity, queue.getMaxCapacity());
        assertEquals(customTime, queue.getEstimatedTimeMinutes());
        assertEquals(1, queue.getPriorityOrder()); // VIP priority
        assertTrue(queue.isActive());
    }

    @Test
    @DisplayName("Should throw exception when queue type is null")
    void shouldThrowExceptionWhenQueueTypeIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Queue.create(null)
        );
        
        assertEquals("Queue type is required", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    @DisplayName("Should throw exception for invalid capacity")
    void shouldThrowExceptionForInvalidCapacity(int invalidCapacity) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Queue.create(QueueType.GENERAL, invalidCapacity, 15)
        );
        
        assertEquals("Max capacity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when capacity exceeds limit")
    void shouldThrowExceptionWhenCapacityExceedsLimit() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Queue.create(QueueType.GENERAL, 201, 15)
        );
        
        assertEquals("Max capacity cannot exceed 200", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5})
    @DisplayName("Should throw exception for invalid estimated time")
    void shouldThrowExceptionForInvalidEstimatedTime(int invalidTime) {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Queue.create(QueueType.GENERAL, 50, invalidTime)
        );
        
        assertEquals("Estimated time must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when estimated time exceeds limit")
    void shouldThrowExceptionWhenEstimatedTimeExceedsLimit() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Queue.create(QueueType.GENERAL, 50, 121)
        );
        
        assertEquals("Estimated time cannot exceed 120 minutes", exception.getMessage());
    }

    @Test
    @DisplayName("Should update max capacity")
    void shouldUpdateMaxCapacity() {
        // Given
        Queue queue = Queue.create(QueueType.GENERAL);
        int newCapacity = 75;
        
        // When
        Queue updatedQueue = queue.withMaxCapacity(newCapacity);
        
        // Then
        assertEquals(newCapacity, updatedQueue.getMaxCapacity());
        // Verify immutability
        assertEquals(50, queue.getMaxCapacity()); // Original unchanged
        // Verify other fields remain the same
        assertEquals(queue.getId(), updatedQueue.getId());
        assertEquals(queue.getQueueType(), updatedQueue.getQueueType());
    }

    @Test
    @DisplayName("Should update estimated time")
    void shouldUpdateEstimatedTime() {
        // Given
        Queue queue = Queue.create(QueueType.GENERAL);
        int newTime = 25;
        
        // When
        Queue updatedQueue = queue.withEstimatedTime(newTime);
        
        // Then
        assertEquals(newTime, updatedQueue.getEstimatedTimeMinutes());
        // Verify immutability
        assertEquals(20, queue.getEstimatedTimeMinutes()); // Original unchanged
    }

    @Test
    @DisplayName("Should update active status")
    void shouldUpdateActiveStatus() {
        // Given
        Queue queue = Queue.create(QueueType.GENERAL);
        
        // When
        Queue inactiveQueue = queue.withActiveStatus(false);
        Queue activeQueue = inactiveQueue.withActiveStatus(true);
        
        // Then
        assertTrue(queue.isActive()); // Original
        assertFalse(inactiveQueue.isActive()); // Deactivated
        assertTrue(activeQueue.isActive()); // Reactivated
    }

    @Test
    @DisplayName("Should determine if queue can accept tickets")
    void shouldDetermineIfQueueCanAcceptTickets() {
        // Given
        Queue activeQueue = Queue.create(QueueType.GENERAL, 10, 15);
        Queue inactiveQueue = activeQueue.withActiveStatus(false);
        
        // Then
        assertTrue(activeQueue.canAcceptTickets(5)); // Under capacity
        assertFalse(activeQueue.canAcceptTickets(10)); // At capacity
        assertFalse(activeQueue.canAcceptTickets(15)); // Over capacity
        assertFalse(inactiveQueue.canAcceptTickets(5)); // Inactive
    }

    @Test
    @DisplayName("Should determine if queue is full")
    void shouldDetermineIfQueueIsFull() {
        // Given
        Queue queue = Queue.create(QueueType.GENERAL, 10, 15);
        
        // Then
        assertFalse(queue.isFull(5)); // Under capacity
        assertFalse(queue.isFull(9)); // Just under capacity
        assertTrue(queue.isFull(10)); // At capacity
        assertTrue(queue.isFull(15)); // Over capacity
    }

    @Test
    @DisplayName("Should calculate estimated wait time")
    void shouldCalculateEstimatedWaitTime() {
        // Given
        Queue queue = Queue.create(QueueType.GENERAL, 50, 20);
        
        // Then
        assertEquals(0, queue.calculateEstimatedWaitTime(0)); // No wait
        assertEquals(0, queue.calculateEstimatedWaitTime(-1)); // Invalid position
        assertEquals(20, queue.calculateEstimatedWaitTime(1)); // First in queue
        assertEquals(40, queue.calculateEstimatedWaitTime(2)); // Second in queue
        assertEquals(100, queue.calculateEstimatedWaitTime(5)); // Fifth in queue
    }

    @Test
    @DisplayName("Should compare queue priorities correctly")
    void shouldCompareQueuePrioritiesCorrectly() {
        // Given
        Queue vipQueue = Queue.create(QueueType.VIP);
        Queue businessQueue = Queue.create(QueueType.BUSINESS);
        Queue priorityQueue = Queue.create(QueueType.PRIORITY);
        Queue generalQueue = Queue.create(QueueType.GENERAL);
        
        // Then
        assertTrue(vipQueue.hasHigherPriorityThan(businessQueue));
        assertTrue(vipQueue.hasHigherPriorityThan(priorityQueue));
        assertTrue(vipQueue.hasHigherPriorityThan(generalQueue));
        
        assertTrue(businessQueue.hasHigherPriorityThan(priorityQueue));
        assertTrue(businessQueue.hasHigherPriorityThan(generalQueue));
        
        assertTrue(priorityQueue.hasHigherPriorityThan(generalQueue));
        
        // Reverse should be false
        assertFalse(generalQueue.hasHigherPriorityThan(vipQueue));
        assertFalse(priorityQueue.hasHigherPriorityThan(businessQueue));
    }

    @Test
    @DisplayName("Should identify high priority queues")
    void shouldIdentifyHighPriorityQueues() {
        // Given
        Queue vipQueue = Queue.create(QueueType.VIP);
        Queue businessQueue = Queue.create(QueueType.BUSINESS);
        Queue priorityQueue = Queue.create(QueueType.PRIORITY);
        Queue generalQueue = Queue.create(QueueType.GENERAL);
        
        // Then
        assertTrue(vipQueue.isHighPriority());
        assertTrue(businessQueue.isHighPriority());
        assertFalse(priorityQueue.isHighPriority());
        assertFalse(generalQueue.isHighPriority());
    }

    @Test
    @DisplayName("Should be immutable")
    void shouldBeImmutable() {
        // Given
        Queue queue = Queue.create(QueueType.GENERAL);
        QueueId originalId = queue.getId();
        QueueType originalType = queue.getQueueType();
        
        // When - Perform operations that return new instances
        Queue withCapacity = queue.withMaxCapacity(75);
        Queue withTime = queue.withEstimatedTime(25);
        Queue withStatus = queue.withActiveStatus(false);
        
        // Then - Original queue should remain unchanged
        assertEquals(originalId, queue.getId());
        assertEquals(originalType, queue.getQueueType());
        assertEquals(50, queue.getMaxCapacity());
        assertEquals(20, queue.getEstimatedTimeMinutes());
        assertTrue(queue.isActive());
        
        // New instances should have different values
        assertNotSame(queue, withCapacity);
        assertNotSame(queue, withTime);
        assertNotSame(queue, withStatus);
    }

    @Test
    @DisplayName("Should handle boundary values correctly")
    void shouldHandleBoundaryValuesCorrectly() {
        // Given boundary values
        int minCapacity = 1;
        int maxCapacity = 200;
        int minTime = 1;
        int maxTime = 120;
        
        // When & Then - Should not throw exceptions
        assertDoesNotThrow(() -> Queue.create(QueueType.GENERAL, minCapacity, minTime));
        assertDoesNotThrow(() -> Queue.create(QueueType.GENERAL, maxCapacity, maxTime));
        
        Queue queue = Queue.create(QueueType.GENERAL, minCapacity, minTime);
        assertEquals(minCapacity, queue.getMaxCapacity());
        assertEquals(minTime, queue.getEstimatedTimeMinutes());
    }
}