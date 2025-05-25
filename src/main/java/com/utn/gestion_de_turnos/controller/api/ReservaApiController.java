package com.utn.gestion_de_turnos.controller.api;

import com.utn.gestion_de_turnos.exception.TiempoDeReservaOcupadoException;
import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.security.CustomUserDetails;
import com.utn.gestion_de_turnos.service.ReservaService;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<?> saveReserva(@RequestBody ReservaRequest request, Authentication authentication) {
        try {
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
            Long clienteId = user.getId();
            Reserva reserva = reservaService.crearReserva(
                    clienteId,
                    request.getSalaId(),
                    request.getFechaInicio(),
                    request.getFechaFinal(),
                    request.getTipoPago());
            return ResponseEntity.ok(reserva);
        } catch (TiempoDeReservaOcupadoException e) {
            Map<String, String> error = Map.of("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }


    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long clienteId = userDetails.getId();

        try {
            reservaService.cancelarReservaPorCliente(id, clienteId);
            return ResponseEntity.ok(Map.of("message", "Reserva cancelada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancelar/by-empleado")
    public ResponseEntity<?> cancelarReservaPorEmpleado(@PathVariable Long id) {
        try {
            reservaService.cancelarReservaPorEmpleado(id);
            return ResponseEntity.ok(Map.of("message", "Reserva cancelada correctamente por el empleado"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }


    @GetMapping("/all/activas")
    public ResponseEntity<List<ReservaResponse>> getAllReservasActivas(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long clienteId = userDetails.getId();
        List<ReservaResponse> reservasActivas = reservaService.findByAllActivas()
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

    @PostMapping("/by-empleado")
    public ResponseEntity<?> saveReservaByEmpleado(@RequestBody ReservaRequest request) {
        try {
            Long clienteId = request.getClienteId(); // <-- теперь из запроса
            Reserva reserva = reservaService.crearReserva(
                    clienteId,
                    request.getSalaId(),
                    request.getFechaInicio(),
                    request.getFechaFinal(),
                    request.getTipoPago());
            return ResponseEntity.ok(reserva);
        } catch (TiempoDeReservaOcupadoException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }


    // dto
    @Data
    public static class ReservaRequest {

        @NotNull(message = "clienteId es obligatorio")
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
}
