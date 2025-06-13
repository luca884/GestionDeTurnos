package com.utn.gestion_de_turnos.dto;

import com.utn.gestion_de_turnos.model.Reserva;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservaRequestByEmpleadoDTO {

    @NotNull(message = "empleadoId es obligatorio")
    private Long clienteId;

    @NotNull(message = "salaId es obligatorio")
    private Long salaId;

    @NotNull(message = "fechaInicio es obligatorio")
    private LocalDateTime fechaInicio;

    @NotNull(message = "fechaFinal es obligatorio")
    private LocalDateTime fechaFinal;

    @NotNull(message = "tipoPago es obligatorio")
    private Reserva.TipoPago tipoPago;
}
