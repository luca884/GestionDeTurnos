package com.utn.gestion_de_turnos.controller.api;


import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.security.CustomUserDetails;
import com.utn.gestion_de_turnos.service.ReservaService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/calendario")
public class CalendarioController {

    @Autowired
    private final ReservaService reservaService;

    public CalendarioController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping("/eventos")
    public List<EventoCalendarioDto> obtenerEventos(Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Long clienteId = user.getId();
        List<Reserva> todasReservas = reservaService.findAllActivas();
        return todasReservas.stream()
                .map(reserva -> {
                    EventoCalendarioDto dto = new EventoCalendarioDto();
                    dto.setId(reserva.getId());
                    dto.setStart(reserva.getFechaInicio());
                    dto.setEnd(reserva.getFechaFinal());

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    String horaInicio = reserva.getFechaInicio().format(formatter);
                    String horaFin = reserva.getFechaFinal().format(formatter);

                    if (reserva.getCliente().getId().equals(clienteId)) {
                        dto.setTitle("Sala " + reserva.getSala().getNumero() + ": Tuya");
                    } else {
                        dto.setTitle("Sala " + reserva.getSala().getNumero() + ": Ocupado");
                    }
                    return dto;
                })

                .collect(Collectors.toList());
    }

    @Data
    public static class EventoCalendarioDto {
        private Long id;
        private LocalDateTime start;
        private LocalDateTime end;
        private String title;
        private String description;

    }
}
