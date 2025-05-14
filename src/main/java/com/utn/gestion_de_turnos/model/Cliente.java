package com.utn.gestion_de_turnos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "clientes")
public class Cliente extends Usuario{

    @Column(nullable = true)
    private String nombre_banda;

    @Column(nullable = true)
    private Integer cant_integrantes;
}
