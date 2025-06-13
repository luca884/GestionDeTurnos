package com.utn.gestion_de_turnos.dto;

import com.utn.gestion_de_turnos.model.Reserva;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReservaResponseDTO {
    private Long id;
    private Integer salaNumero;
    private Integer salaCapacidad;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFinal;
    private Reserva.TipoPago tipoPago;
    private String estado;
    private String clienteEmail;
}
