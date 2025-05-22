package com.utn.gestion_de_turnos.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "empleados")
public class Empleado extends Usuario {

    @Column(nullable = false)
    private String legajo;

}
