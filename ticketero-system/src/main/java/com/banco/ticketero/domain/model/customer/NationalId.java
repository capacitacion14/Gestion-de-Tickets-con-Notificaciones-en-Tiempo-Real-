package com.banco.ticketero.domain.model.customer;

import lombok.Value;

/**
 * Value Object que representa la cédula de identidad nacional.
 * Inmutable y con validaciones de dominio.
 */
@Value
public class NationalId {
    
    String value;
    
    private NationalId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("NationalId cannot be null or empty");
        }
        
        String cleanValue = value.trim().replaceAll("[^0-9]", "");
        
        if (!isValidFormat(cleanValue)) {
            throw new IllegalArgumentException("NationalId must be between 8 and 20 digits");
        }
        
        this.value = cleanValue;
    }
    
    /**
     * Crea un NationalId a partir de un string.
     */
    public static NationalId of(String nationalId) {
        return new NationalId(nationalId);
    }
    
    /**
     * Retorna el valor formateado para visualización.
     */
    public String getFormattedValue() {
        if (value.length() <= 8) {
            return value;
        }
        
        // Formato: XX.XXX.XXX para cédulas de 8+ dígitos
        StringBuilder formatted = new StringBuilder();
        int length = value.length();
        
        for (int i = 0; i < length; i++) {
            if (i > 0 && (length - i) % 3 == 0) {
                formatted.append(".");
            }
            formatted.append(value.charAt(i));
        }
        
        return formatted.toString();
    }
    
    /**
     * Verifica si es una cédula válida (solo formato, no algoritmo de verificación).
     */
    public boolean isValid() {
        return isValidFormat(value);
    }
    
    private boolean isValidFormat(String cleanValue) {
        if (cleanValue == null || cleanValue.isEmpty()) {
            return false;
        }
        
        // Debe tener entre 8 y 20 dígitos
        return cleanValue.length() >= 8 && cleanValue.length() <= 20 && cleanValue.matches("\\d+");
    }
    
    @Override
    public String toString() {
        return value;
    }
}