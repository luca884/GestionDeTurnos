//package com.utn.gestion_de_turnos.controller.api;
//
//
//import com.utn.gestion_de_turnos.dto.EventoCalendarioDTO;
//import com.utn.gestion_de_turnos.model.Reserva;
//import com.utn.gestion_de_turnos.security.CustomUserDetails;
//import com.utn.gestion_de_turnos.service.ReservaService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.security.core.Authentication;
//
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//import java.util.stream.Collectors;
//
//
//@RestController
//@RequestMapping("/api/calendario")
//@Tag(name = "Calendario", description = "Eventos visibles en el calendario del cliente")
//public class CalendarioController {
//
//    @Autowired
//    private final ReservaService reservaService;
//
//    public CalendarioController(ReservaService reservaService) {
//        this.reservaService = reservaService;
//    }
//
//    @GetMapping("/eventos")
//    @Operation(
//            summary = "Obtener eventos del calendario",
//            description = "Devuelve todas las reservas activas como eventos. El cliente autenticado verá cuáles le pertenecen."
//    )
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Lista de eventos obtenida con éxito",
//                    content = @Content(schema = @Schema(implementation = EventoCalendarioDTO.class))),
//            @ApiResponse(responseCode = "401", description = "No autenticado")
//    })
//    public List<EventoCalendarioDTO> obtenerEventos(Authentication authentication) {
//        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
//        Long clienteId = user.getId();
//        List<Reserva> todasReservas = reservaService.findAllActivas();
//        return todasReservas.stream()
//                .map(reserva -> {
//                    EventoCalendarioDTO dto = new EventoCalendarioDTO();
//                    dto.setId(reserva.getId());
//                    dto.setStart(reserva.getFechaInicio());
//                    dto.setEnd(reserva.getFechaFinal());
//
//                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
//                    String horaInicio = reserva.getFechaInicio().format(formatter);
//                    String horaFin = reserva.getFechaFinal().format(formatter);
//
//                    if (reserva.getCliente().getId().equals(clienteId)) {
//                        dto.setTitle("Sala " + reserva.getSala().getNumero() + ": Tuya");
//                    } else {
//                        dto.setTitle("Sala " + reserva.getSala().getNumero() + ": Ocupado");
//                    }
//                    return dto;
//                })
//
//                .collect(Collectors.toList());
//    }
//
//}
package com.utn.gestion_de_turnos.controller.api;
import com.utn.gestion_de_turnos.dto.EventoCalendarioDTO;
import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.security.CustomUserDetails;
import com.utn.gestion_de_turnos.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/calendario")
@Tag(name = "Calendario", description = "Eventos visibles en el calendario del cliente")
public class CalendarioController {

    private final ReservaService reservaService;

    public CalendarioController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping("/eventos")
    @Operation(
            summary = "Obtener eventos del calendario",
            description = "Devuelve todas las reservas activas como eventos. El cliente autenticado verá cuáles le pertenecen."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de eventos obtenida con éxito",
                    content = @Content(schema = @Schema(implementation = EventoCalendarioDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public List<EventoCalendarioDTO> obtenerEventos(Authentication authentication) {

        // puede venir null si no hay login
        Long clienteId = null;
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails cud) {
            clienteId = cud.getId();
        }

        // 👇 esto es lo que pide el error: usar una variable final dentro del stream
        final Long finalClienteId = clienteId;

        List<Reserva> todasReservas = reservaService.findAllActivas();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return todasReservas.stream()
                .map(reserva -> {
                    EventoCalendarioDTO dto = new EventoCalendarioDTO();
                    dto.setId(reserva.getId());
                    dto.setStart(reserva.getFechaInicio());
                    dto.setEnd(reserva.getFechaFinal());

                    String horaInicio = reserva.getFechaInicio().format(formatter);
                    String horaFin = reserva.getFechaFinal().format(formatter);

                    // si hay usuario logueado y la reserva es suya
                    if (finalClienteId != null
                            && reserva.getCliente() != null
                            && reserva.getCliente().getId().equals(finalClienteId)) {
                        dto.setTitle("Sala " + reserva.getSala().getNumero()
                                + " (" + horaInicio + " - " + horaFin + "): Tuya");
                        dto.setDescription("Reserva propia");
                    } else {
                        dto.setTitle("Sala " + reserva.getSala().getNumero()
                                + " (" + horaInicio + " - " + horaFin + "): Ocupado");
                        dto.setDescription("Reserva de otro cliente");
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }
}
