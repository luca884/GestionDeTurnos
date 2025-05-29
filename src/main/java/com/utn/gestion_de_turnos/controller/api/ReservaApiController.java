package com.utn.gestion_de_turnos.controller.api;

import com.utn.gestion_de_turnos.exception.TiempoDeReservaOcupadoException;
import com.google.api.services.calendar.model.Event;
import com.utn.gestion_de_turnos.API_Calendar.Service.GoogleCalendarService;
import com.utn.gestion_de_turnos.model.Cliente;
import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.model.Usuario;
import com.utn.gestion_de_turnos.repository.SalaRepository;
import com.utn.gestion_de_turnos.repository.UsuarioRepository;
import com.utn.gestion_de_turnos.security.CustomUserDetails;
import com.utn.gestion_de_turnos.service.ReservaService;
import jakarta.validation.Valid;
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

    @Autowired
    private GoogleCalendarService calendarService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SalaRepository salaRepository;

    @PostMapping("/crear")
    public String crearReservaConDTO(@Valid @RequestBody ReservaRequest dto) {
        try {
            Cliente cliente = (Cliente) usuarioRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Sala sala = salaRepository.findById(dto.getSalaId())
                    .orElseThrow(() -> new RuntimeException("Sala no encontrada"));

            reservaService.crearReserva(
                    dto.clienteId,
                    dto.salaId,
                    dto.fechaInicio,
                    dto.fechaFinal,
                    dto.getTipoPago()
            );


        } catch (Exception e) {
            return "Error al crear reserva y evento: " + e.getMessage();
        }
        return "Se ha realizado la Reserva correctamente";
    }

    @GetMapping("/listar")
    public List<Reserva> listarTodas() {
        return reservaService.listarTodas();
    }

    @GetMapping("/cliente")
    public List<Reserva> listarReservasDelCliente() {
        return reservaService.listarPorUsuarioActual();
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

@PutMapping("/modificar/{id}")
public String modificarReservaYEvento(@PathVariable Long id) {
    try {
        Reserva reserva = reservaService.buscarPorId(id);
        reservaService.modificar(reserva);
        return "Reserva y evento modificados.";
    } catch (Exception e) {
        return "Error al modificar: " + e.getMessage();
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
