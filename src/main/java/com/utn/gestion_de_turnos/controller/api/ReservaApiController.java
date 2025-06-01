package com.utn.gestion_de_turnos.controller.api;

import com.utn.gestion_de_turnos.exception.AccesoProhibidoException;
import com.utn.gestion_de_turnos.API_Calendar.Service.GoogleCalendarService;
import com.utn.gestion_de_turnos.exception.ReservaNotFoundException;
import com.utn.gestion_de_turnos.exception.SalaNotFoundException;
import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.repository.SalaRepository;
import com.utn.gestion_de_turnos.repository.UsuarioRepository;
import com.utn.gestion_de_turnos.security.CustomUserDetails;
import com.utn.gestion_de_turnos.service.ReservaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// Eso es el controller de la API REST para la reserva

@RestController
@RequestMapping("/api/reserva")
public class ReservaApiController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private GoogleCalendarService calendarService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SalaRepository salaRepository;

    @PostMapping("/crear")
    public ResponseEntity<?> crearReserva(
            @Valid @RequestBody ReservaApiController.ReservaRequestByCliente request,
            Authentication authentication) {

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Long clienteId = user.getId();

        Reserva reserva = reservaService.saveReserva(
                clienteId,
                request.getSalaId(),
                request.getFechaInicio(),
                request.getFechaFinal(),
                request.getTipoPago()
        );

        return ResponseEntity.ok(reserva);
    }


    @PostMapping("crear/by-empleado")
    public ResponseEntity<?> saveReservaByEmpleado(@RequestBody ReservaRequestByEmpleado request) {
        Long clienteId = request.getClienteId();

        Reserva reserva = reservaService.saveReserva(
                clienteId,
                request.getSalaId(),
                request.getFechaInicio(),
                request.getFechaFinal(),
                request.getTipoPago()
        );

        return ResponseEntity.ok(reserva);
    }


    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long clienteId = userDetails.getId();

        reservaService.cancelarReservaPorCliente(id, clienteId);
        return ResponseEntity.ok(Map.of("message", "Reserva cancelada correctamente"));
    }

    @PutMapping("/{id}/cancelar/by-empleado")
    public ResponseEntity<?> cancelarReservaPorEmpleado(@PathVariable Long id) {
        reservaService.cancelarReservaPorEmpleado(id);
        return ResponseEntity.ok(Map.of("message", "Reserva cancelada correctamente por el empleado"));
    }


    @DeleteMapping("/eliminar/{id}")
    public String eliminarReservaYEvento(@PathVariable Long id) {
        try {
            reservaService.eliminar(id);
            return "Reserva y evento eliminados.";
        } catch (Exception e) {
            return "Error al eliminar: " + e.getMessage();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateReserva(@Valid @RequestBody ReservaUpdateRequest request, Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Long clienteId = user.getId();

        Reserva reservaExistente = reservaService.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // update reserva by cliente
        if (!reservaExistente.getCliente().getId().equals(clienteId)) {
            throw new AccesoProhibidoException("No tienes permiso para modificar esta reserva");
        }

        if (reservaExistente.getEstado() != Reserva.Estado.ACTIVO) {
            throw new RuntimeException("Solo se pueden modificar reservas activas");
        }

        Sala sala = salaRepository.findById(request.getSalaId())
                .orElseThrow(() -> new RuntimeException("Sala no encontrada"));

        reservaExistente.setSala(sala);
        reservaExistente.setFechaInicio(request.getFechaInicio());
        reservaExistente.setFechaFinal(request.getFechaFinal());
        reservaExistente.setTipoPago(request.getTipoPago());

        reservaService.modificar(reservaExistente);

        return ResponseEntity.ok(Map.of("message", "Reserva actualizada correctamente"));
    }


    @PutMapping("/update/by-empleado")
    public ResponseEntity<?> updateReservaEmpleado(@RequestBody ReservaUpdateRequest request, Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        if (user == null || user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("EMPLEADO"))) {
            throw new AccesoProhibidoException("No tienes permiso para realizar esta acción");
        }

        Reserva reserva = reservaService.findById(request.getId())
                .orElseThrow(() -> new ReservaNotFoundException("Reserva no encontrada"));

        Sala sala = salaRepository.findById(request.getSalaId())
                .orElseThrow(() -> new SalaNotFoundException("Sala no encontrada"));

        if (!request.getFechaInicio().isBefore(request.getFechaFinal())) {
            throw new IllegalArgumentException("La hora de inicio debe ser menor que la hora de fin.");
        }

        reserva.setSala(sala);
        reserva.setFechaInicio(request.getFechaInicio());
        reserva.setFechaFinal(request.getFechaFinal());
        reserva.setTipoPago(request.getTipoPago());
        reservaService.modificar(reserva);
        return ResponseEntity.ok(Map.of("message", "Reserva actualizada correctamente"));
    }


    @GetMapping("/all/activas")
    public ResponseEntity<List<ReservaResponse>> getAllReservasActivas(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long clienteId = userDetails.getId();
        List<ReservaResponse> reservasActivas = reservaService.findAllActivas()
                .stream()
                .map(reserva -> new ReservaResponse(
                        reserva.getId(),
                        reserva.getSala().getNumero(),
                        reserva.getSala().getCantPersonas(),
                        reserva.getFechaInicio(),
                        reserva.getFechaFinal(),
                        reserva.getTipoPago(),
                        reserva.getEstado().name(),
                        reserva.getCliente().getEmail()))
                .toList();

        return ResponseEntity.ok(reservasActivas);
    }

    // dto's
    @Data
    public static class ReservaRequestByEmpleado {

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

    @Data
    public static class ReservaRequestByCliente {

        @NotNull(message = "salaId es obligatorio")
        private Long salaId;

        @NotNull(message = "fechaInicio es obligatorio")
        private LocalDateTime fechaInicio;

        @NotNull(message = "fechaFinal es obligatorio")
        private LocalDateTime fechaFinal;

        @NotNull(message = "tipoPago es obligatorio")
        private Reserva.TipoPago tipoPago;
    }

    @Data
    @AllArgsConstructor
    public static class ReservaResponse {
        private Long id;
        private Integer salaNumero;
        private Integer salaCapacidad;
        private LocalDateTime fechaInicio;
        private LocalDateTime fechaFinal;
        private Reserva.TipoPago tipoPago;
        private String estado;
        private String clienteEmail;
    }

    @Data
    public static class ReservaUpdateRequest {
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


}
