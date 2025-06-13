package com.utn.gestion_de_turnos.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventoCalendarioDTO {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private String title;
    private String description;
}
