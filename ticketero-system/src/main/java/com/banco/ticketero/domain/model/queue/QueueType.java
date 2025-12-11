package com.banco.ticketero.domain.model.queue;

/**
 * Enum que representa los tipos de cola disponibles en el sistema.
 * Ordenado por prioridad: VIP > BUSINESS > PRIORITY > GENERAL
 */
public enum QueueType {
    
    /**
     * Cola VIP - Máxima prioridad, clientes premium.
     */
    VIP(1, 10, 5),
    
    /**
     * Cola Business - Alta prioridad, clientes empresariales.
     */
    BUSINESS(2, 20, 10),
    
    /**
     * Cola Priority - Prioridad media, casos especiales.
     */
    PRIORITY(3, 30, 15),
    
    /**
     * Cola General - Prioridad normal, atención estándar.
     */
    GENERAL(4, 50, 20);
    
    private final int priorityOrder;
    private final int defaultMaxCapacity;
    private final int defaultEstimatedTimeMinutes;
    
    QueueType(int priorityOrder, int defaultMaxCapacity, int defaultEstimatedTimeMinutes) {
        this.priorityOrder = priorityOrder;
        this.defaultMaxCapacity = defaultMaxCapacity;
        this.defaultEstimatedTimeMinutes = defaultEstimatedTimeMinutes;
    }
    
    /**
     * Retorna el orden de prioridad (menor número = mayor prioridad).
     */
    public int getPriorityOrder() {
        return priorityOrder;
    }
    
    /**
     * Retorna la capacidad máxima por defecto para este tipo de cola.
     */
    public int getDefaultMaxCapacity() {
        return defaultMaxCapacity;
    }
    
    /**
     * Retorna el tiempo estimado por defecto en minutos.
     */
    public int getDefaultEstimatedTimeMinutes() {
        return defaultEstimatedTimeMinutes;
    }
    
    /**
     * Verifica si esta cola tiene mayor prioridad que otra.
     */
    public boolean hasHigherPriorityThan(QueueType other) {
        return this.priorityOrder < other.priorityOrder;
    }
    
    /**
     * Verifica si es una cola de alta prioridad (VIP o BUSINESS).
     */
    public boolean isHighPriority() {
        return this == VIP || this == BUSINESS;
    }
    
    /**
     * Retorna el tipo de cola con mayor prioridad entre dos.
     */
    public static QueueType getHigherPriority(QueueType type1, QueueType type2) {
        return type1.hasHigherPriorityThan(type2) ? type1 : type2;
    }
}