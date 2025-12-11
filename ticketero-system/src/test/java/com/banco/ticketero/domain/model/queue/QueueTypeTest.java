package com.banco.ticketero.domain.model.queue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QueueType Enum Tests")
class QueueTypeTest {

    @Test
    @DisplayName("Should have correct priority order")
    void shouldHaveCorrectPriorityOrder() {
        // Then
        assertEquals(1, QueueType.VIP.getPriorityOrder());
        assertEquals(2, QueueType.BUSINESS.getPriorityOrder());
        assertEquals(3, QueueType.PRIORITY.getPriorityOrder());
        assertEquals(4, QueueType.GENERAL.getPriorityOrder());
    }

    @Test
    @DisplayName("Should have correct default capacities")
    void shouldHaveCorrectDefaultCapacities() {
        // Then
        assertEquals(10, QueueType.VIP.getDefaultMaxCapacity());
        assertEquals(20, QueueType.BUSINESS.getDefaultMaxCapacity());
        assertEquals(30, QueueType.PRIORITY.getDefaultMaxCapacity());
        assertEquals(50, QueueType.GENERAL.getDefaultMaxCapacity());
    }

    @Test
    @DisplayName("Should have correct default estimated times")
    void shouldHaveCorrectDefaultEstimatedTimes() {
        // Then
        assertEquals(5, QueueType.VIP.getDefaultEstimatedTimeMinutes());
        assertEquals(10, QueueType.BUSINESS.getDefaultEstimatedTimeMinutes());
        assertEquals(15, QueueType.PRIORITY.getDefaultEstimatedTimeMinutes());
        assertEquals(20, QueueType.GENERAL.getDefaultEstimatedTimeMinutes());
    }

    @Test
    @DisplayName("Should correctly identify higher priority")
    void shouldCorrectlyIdentifyHigherPriority() {
        // VIP has highest priority
        assertTrue(QueueType.VIP.hasHigherPriorityThan(QueueType.BUSINESS));
        assertTrue(QueueType.VIP.hasHigherPriorityThan(QueueType.PRIORITY));
        assertTrue(QueueType.VIP.hasHigherPriorityThan(QueueType.GENERAL));
        
        // BUSINESS has higher priority than PRIORITY and GENERAL
        assertTrue(QueueType.BUSINESS.hasHigherPriorityThan(QueueType.PRIORITY));
        assertTrue(QueueType.BUSINESS.hasHigherPriorityThan(QueueType.GENERAL));
        
        // PRIORITY has higher priority than GENERAL
        assertTrue(QueueType.PRIORITY.hasHigherPriorityThan(QueueType.GENERAL));
        
        // Reverse should be false
        assertFalse(QueueType.GENERAL.hasHigherPriorityThan(QueueType.VIP));
        assertFalse(QueueType.PRIORITY.hasHigherPriorityThan(QueueType.BUSINESS));
    }

    @Test
    @DisplayName("Should identify high priority queues correctly")
    void shouldIdentifyHighPriorityQueuesCorrectly() {
        // High priority queues
        assertTrue(QueueType.VIP.isHighPriority());
        assertTrue(QueueType.BUSINESS.isHighPriority());
        
        // Regular priority queues
        assertFalse(QueueType.PRIORITY.isHighPriority());
        assertFalse(QueueType.GENERAL.isHighPriority());
    }

    @Test
    @DisplayName("Should return higher priority between two queue types")
    void shouldReturnHigherPriorityBetweenTwoQueueTypes() {
        // Test various combinations
        assertEquals(QueueType.VIP, QueueType.getHigherPriority(QueueType.VIP, QueueType.BUSINESS));
        assertEquals(QueueType.VIP, QueueType.getHigherPriority(QueueType.BUSINESS, QueueType.VIP));
        
        assertEquals(QueueType.BUSINESS, QueueType.getHigherPriority(QueueType.BUSINESS, QueueType.PRIORITY));
        assertEquals(QueueType.BUSINESS, QueueType.getHigherPriority(QueueType.PRIORITY, QueueType.BUSINESS));
        
        assertEquals(QueueType.PRIORITY, QueueType.getHigherPriority(QueueType.PRIORITY, QueueType.GENERAL));
        assertEquals(QueueType.PRIORITY, QueueType.getHigherPriority(QueueType.GENERAL, QueueType.PRIORITY));
        
        // Same priority should return first one
        assertEquals(QueueType.VIP, QueueType.getHigherPriority(QueueType.VIP, QueueType.VIP));
    }

    @Test
    @DisplayName("Should have all expected enum values")
    void shouldHaveAllExpectedEnumValues() {
        // Given
        QueueType[] expectedValues = {
            QueueType.VIP,
            QueueType.BUSINESS,
            QueueType.PRIORITY,
            QueueType.GENERAL
        };
        
        // When
        QueueType[] actualValues = QueueType.values();
        
        // Then
        assertEquals(4, actualValues.length);
        assertArrayEquals(expectedValues, actualValues);
    }

    @Test
    @DisplayName("Should maintain enum order by priority")
    void shouldMaintainEnumOrderByPriority() {
        // Given - Expected order by priority (highest to lowest)
        QueueType[] values = QueueType.values();
        
        // Then - Verify priority order
        assertEquals(QueueType.VIP, values[0]);
        assertEquals(QueueType.BUSINESS, values[1]);
        assertEquals(QueueType.PRIORITY, values[2]);
        assertEquals(QueueType.GENERAL, values[3]);
        
        // Verify priority numbers are in ascending order
        for (int i = 0; i < values.length - 1; i++) {
            assertTrue(values[i].getPriorityOrder() < values[i + 1].getPriorityOrder(),
                String.format("Priority order should be ascending: %s should have lower priority number than %s",
                    values[i], values[i + 1]));
        }
    }

    @ParameterizedTest
    @EnumSource(QueueType.class)
    @DisplayName("Should have positive default values for all queue types")
    void shouldHavePositiveDefaultValuesForAllQueueTypes(QueueType queueType) {
        // Then
        assertTrue(queueType.getPriorityOrder() > 0, "Priority order should be positive");
        assertTrue(queueType.getDefaultMaxCapacity() > 0, "Default max capacity should be positive");
        assertTrue(queueType.getDefaultEstimatedTimeMinutes() > 0, "Default estimated time should be positive");
    }

    @Test
    @DisplayName("Should handle enum valueOf correctly")
    void shouldHandleEnumValueOfCorrectly() {
        // Given
        String queueTypeName = "VIP";
        
        // When
        QueueType queueType = QueueType.valueOf(queueTypeName);
        
        // Then
        assertEquals(QueueType.VIP, queueType);
    }

    @Test
    @DisplayName("Should throw exception for invalid valueOf")
    void shouldThrowExceptionForInvalidValueOf() {
        // Given
        String invalidQueueTypeName = "INVALID_QUEUE";
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> QueueType.valueOf(invalidQueueTypeName));
    }

    @Test
    @DisplayName("Should not have higher priority than itself")
    void shouldNotHaveHigherPriorityThanItself() {
        // Given all queue types
        QueueType[] allTypes = QueueType.values();
        
        // Then - No queue type should have higher priority than itself
        for (QueueType queueType : allTypes) {
            assertFalse(queueType.hasHigherPriorityThan(queueType),
                String.format("%s should not have higher priority than itself", queueType));
        }
    }
}