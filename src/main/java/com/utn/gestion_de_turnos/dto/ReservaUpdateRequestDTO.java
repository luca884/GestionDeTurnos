package com.utn.gestion_de_turnos.dto;

import com.utn.gestion_de_turnos.model.Reserva;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservaUpdateRequestDTO {
    @NotNull
    private Long id;
    @NotNull
    private Long salaId;
    @NotNull
    private LocalDateTime fechaInicio;
    @NotNull
    private LocalDateTime fechaFinal;
    @NotNull
    private Reserva.TipoPago tipoPago;
}
