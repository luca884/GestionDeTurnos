package com.utn.gestion_de_turnos.model;

public class Sala {
    private int Id;
    private int numero;
    private int cantPersonas;
    private boolean disponibilidad;
    private String descripcion;

    public Sala(int cantPersonas, int numero, int id, boolean disponibilidad, String descripcion) {
        this.cantPersonas = cantPersonas;
        this.numero = numero;
        Id = id;
        this.disponibilidad = disponibilidad;
        this.descripcion = descripcion;
    }

    public int getId() {
        return Id;
    }

    public Sala setId(int id) {
        Id = id;
        return this;
    }

    public int getNumero() {
        return numero;
    }

    public Sala setNumero(int numero) {
        this.numero = numero;
        return this;
    }

    public int getCantPersonas() {
        return cantPersonas;
    }

    public Sala setCantPersonas(int cantPersonas) {
        this.cantPersonas = cantPersonas;
        return this;
    }

    public boolean isDisponibilidad() {
        return disponibilidad;
    }

    public Sala setDisponibilidad(boolean disponibilidad) {
        this.disponibilidad = disponibilidad;
        return this;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Sala setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }
}
