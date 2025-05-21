package com.utn.gestion_de_turnos.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "salas")
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int numero;

    @Column(name = "cantidad_personas", nullable = false)
    private int cantPersonas;

    @Column(length = 255)
    private String descripcion;
}