package com.banco.ticketero.model;

public enum TicketStatus {
    EN_ESPERA("Esperando asignación", true),
    PROXIMO("Próximo a ser atendido", true),
    ATENDIENDO("Siendo atendido", true),
    COMPLETADO("Atención finalizada", false),
    CANCELADO("Cancelado", false),
    NO_ATENDIDO("Cliente no se presentó", false),
    VENCIDO("Expirado por tiempo", false);

    private final String descripcion;
    private final boolean esActivo;

    TicketStatus(String descripcion, boolean esActivo) {
        this.descripcion = descripcion;
        this.esActivo = esActivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isActivo() {
        return esActivo;
    }

    public static TicketStatus[] getEstadosActivos() {
        return new TicketStatus[]{EN_ESPERA, PROXIMO, ATENDIENDO};
    }
}