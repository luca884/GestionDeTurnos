// ReservaApiController.java adaptado con DTO de creación
package com.utn.gestion_de_turnos.controller.api;

import com.google.api.services.calendar.model.Event;
import com.utn.gestion_de_turnos.API_Calendar.Service.GoogleCalendarService;
import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.model.Usuario;
import com.utn.gestion_de_turnos.repository.SalaRepository;
import com.utn.gestion_de_turnos.repository.UsuarioRepository;
import com.utn.gestion_de_turnos.service.ReservaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
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
            Usuario usuario = usuarioRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Sala sala = salaRepository.findById(dto.getSalaId())
                    .orElseThrow(() -> new RuntimeException("Sala no encontrada"));

            // Crear evento en Google Calendar
            Event evento = calendarService.crearEventoConReserva("Reserva de Sala", "Reserva vía API", dto.getFechaInicio(), dto.getFechaFinal());

            Reserva reserva = new Reserva();
            reserva.setUsuario(usuario);
            reserva.setSala(sala);
            reserva.setInicio(dto.getFechaInicio());
            reserva.setFin(dto.getFechaFinal());
            reserva.setTipoPago(dto.getTipoPago());
            reserva.setGoogleEventId(evento.getId());
            reserva.setTitulo("Reserva de Sala");
            reserva.setDescripcion("Reserva sincronizada con Google Calendar");

            reservaService.modificar(reserva);

            return "Reserva y evento creados correctamente con ID: " + evento.getId();
        } catch (Exception e) {
            return "Error al crear reserva y evento: " + e.getMessage();
        }
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
            Reserva reserva = reservaService.buscarPorId(id);
            calendarService.eliminarEvento(reserva.getGoogleEventId());
            reservaService.eliminar(id);
            return "Reserva y evento eliminados.";
        } catch (Exception e) {
            return "Error al eliminar: " + e.getMessage();
        }
    }

    @PutMapping("/modificar/{id}")
    public String modificarReservaYEvento(@PathVariable Long id,
                                          @RequestParam String titulo,
                                          @RequestParam String descripcion) {
        try {
            Reserva reserva = reservaService.buscarPorId(id);
            calendarService.modificarEvento(reserva.getGoogleEventId(), titulo, descripcion);
            reserva.setTitulo(titulo);
            reserva.setDescripcion(descripcion);
            reservaService.modificar(reserva);
            return "Reserva y evento modificados.";
        } catch (Exception e) {
            return "Error al modificar: " + e.getMessage();
        }
    }

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
}
