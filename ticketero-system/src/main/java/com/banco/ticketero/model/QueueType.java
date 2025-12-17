package com.banco.ticketero.model;

public enum QueueType {
    CAJA(60, 5, 1, "C"),
    PERSONAL_BANKER(120, 15, 2, "P"),
    EMPRESAS(180, 20, 3, "E"),
    GERENCIA(240, 30, 4, "G");

    private final int vigenciaMinutos;
    private final int tiempoPromedioMinutos;
    private final int prioridad;
    private final String prefijo;

    QueueType(int vigenciaMinutos, int tiempoPromedioMinutos, int prioridad, String prefijo) {
        this.vigenciaMinutos = vigenciaMinutos;
        this.tiempoPromedioMinutos = tiempoPromedioMinutos;
        this.prioridad = prioridad;
        this.prefijo = prefijo;
    }

    public int getVigenciaMinutos() {
        return vigenciaMinutos;
    }

    public int getTiempoPromedioMinutos() {
        return tiempoPromedioMinutos;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public String getPrefijo() {
        return prefijo;
    }

    public int calculateEstimatedTime(int position) {
        return position * tiempoPromedioMinutos;
    }
}