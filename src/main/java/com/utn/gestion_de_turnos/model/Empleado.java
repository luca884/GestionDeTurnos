package com.utn.gestion_de_turnos.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "empleados")
public class Empleado extends Usuario {

    @Column(nullable = false)
    private int legajo;

}
