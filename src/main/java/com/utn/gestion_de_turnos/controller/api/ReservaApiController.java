package com.utn.gestion_de_turnos.controller.api;

import com.utn.gestion_de_turnos.dto.ReservaResponseDTO;
import com.utn.gestion_de_turnos.dto.ReservaUpdateRequestDTO;
import com.utn.gestion_de_turnos.dto.ReservaRequestByClienteDTO;
import com.utn.gestion_de_turnos.dto.ReservaRequestByEmpleadoDTO;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

// Eso es el controller de la API REST para la reserva

@Tag(name = "Reserva", description = "Operaciones relacionadas con reservas")
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

    @Operation(summary = "Crear una reserva", description = "Permite a un cliente autenticado crear una nueva reserva.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva creada correctamente", content = @Content(schema = @Schema(implementation = Reserva.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content)
    })
    @PostMapping("/crear")
    public ResponseEntity<?> crearReserva(
            @Valid @RequestBody ReservaRequestByClienteDTO request,
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



    @Operation(summary = "Crear una reserva por empleado", description = "Permite a un empleado crear una reserva en nombre de un cliente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva creada correctamente", content = @Content(schema = @Schema(implementation = Reserva.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content)
    })
    @PostMapping("crear/by-empleado")
    public ResponseEntity<?> saveReservaByEmpleado(@RequestBody ReservaRequestByEmpleadoDTO request) {
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


    @Operation(summary = "Cancelar una reserva (cliente)", description = "Permite a un cliente cancelar su propia reserva.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva cancelada correctamente", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso prohibido", content = @Content)
    })
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long clienteId = userDetails.getId();

        reservaService.cancelarReservaPorCliente(id, clienteId);
        return ResponseEntity.ok(Map.of("message", "Reserva cancelada correctamente"));
    }


    @Operation(summary = "Cancelar una reserva (empleado)", description = "Permite a un empleado cancelar cualquier reserva.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva cancelada correctamente", content = @Content)
    })
    @PutMapping("/{id}/cancelar/by-empleado")
    public ResponseEntity<?> cancelarReservaPorEmpleado(@PathVariable Long id) {
        reservaService.cancelarReservaPorEmpleado(id);
        return ResponseEntity.ok(Map.of("message", "Reserva cancelada correctamente por el empleado"));
    }


    @Operation(summary = "Eliminar reserva y evento asociado", description = "Elimina una reserva y su evento correspondiente del calendario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva eliminada correctamente", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    @DeleteMapping("/eliminar/{id}")
    public String eliminarReservaYEvento(@PathVariable Long id) {
        try {
            reservaService.eliminar(id);
            return "Reserva y evento eliminados.";
        } catch (Exception e) {
            return "Error al eliminar: " + e.getMessage();
        }
    }

    @Operation(summary = "Actualizar reserva por cliente", description = "Permite al cliente modificar su propia reserva si está activa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva actualizada correctamente", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso prohibido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reserva o sala no encontrada", content = @Content)
    })
    @PutMapping("/update")
    public ResponseEntity<?> updateReserva(@Valid @RequestBody ReservaUpdateRequestDTO request, Authentication authentication) {
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


    @Operation(summary = "Actualizar reserva por empleado", description = "Permite a un empleado modificar cualquier reserva.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva actualizada correctamente", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso prohibido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reserva o sala no encontrada", content = @Content)
    })
    @PutMapping("/update/by-empleado")
    public ResponseEntity<?> updateReservaEmpleado(@RequestBody ReservaUpdateRequestDTO request, Authentication authentication) {
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



    @Operation(summary = "Obtener todas las reservas activas", description = "Devuelve una lista de todas las reservas activas disponibles para el usuario autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reservas activas obtenida correctamente", content = @Content)
    })
    @GetMapping("/all/activas")
    public ResponseEntity<List<ReservaResponseDTO>> getAllReservasActivas(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long clienteId = userDetails.getId();
        List<ReservaResponseDTO> reservasActivas = reservaService.findAllActivas()
                .stream()
                .map(reserva -> new ReservaResponseDTO(
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

}
