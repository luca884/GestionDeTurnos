// GoogleCalendarController.java
package com.utn.gestion_de_turnos.API_Calendar.Controller;


import com.utn.gestion_de_turnos.API_Calendar.Service.GoogleCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class GoogleCalendarController {

    @Autowired
    private GoogleCalendarService calendarService;

    @PostMapping("/crear")
    public String crearEvento(@RequestParam String titulo,
                              @RequestParam String descripcion,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        try {
            Event evento = calendarService.crearEventoConReserva(titulo, descripcion, inicio, fin);
            return "Evento creado con ID: " + evento.getId();
        } catch (IOException e) {
            return "Error al crear evento: " + e.getMessage();
        }
    }

    @GetMapping("/listar")
    public List<Event> listarEventos() throws Exception {
        return calendarService.listarEventos();
    }

    @GetMapping("/buscar")
    public Event obtenerEventoPorId(@RequestParam String id) throws IOException {
        return calendarService.obtenerEventoPorId(id);
    }

    @DeleteMapping("/eliminar")
    public String eliminarEvento(@RequestParam String id) {
        try {
            calendarService.eliminarEvento(id);
            return "Evento eliminado correctamente.";
        } catch (Exception e) {
            return "Error al eliminar evento: " + e.getMessage();
        }
    }

    @PutMapping("/modificar")
    public String modificarEvento(@RequestParam String id,
                                  @RequestParam String titulo,
                                  @RequestParam String descripcion) {
        try {
            calendarService.modificarEvento(id, titulo, descripcion);
            return "Evento modificado correctamente.";
        } catch (Exception e) {
            return "Error al modificar evento: " + e.getMessage();
        }
    }

    @GetMapping("/filtrar")
    public List<Event> filtrarEventos(@RequestParam(required = false) Long idSala,
                                      @RequestParam(required = false) Long idCliente,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) throws IOException {
        return calendarService.filtrarEventos(idSala, idCliente, fecha);
    }
}
