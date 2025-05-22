package com.utn.gestion_de_turnos.API_Calendar.Controller;


import com.utn.gestion_de_turnos.API_Calendar.Service.GoogleCalendarService;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
public class GoogleCalendarController {

    private final GoogleCalendarService googleCalendarService;

    @Autowired
    public GoogleCalendarController(GoogleCalendarService googleCalendarService) {
        this.googleCalendarService = googleCalendarService;
    }

    @PostMapping("/events")
    public String crearEvento(
            @RequestParam String resumen,
            @RequestParam String descripcion,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin
    ) throws Exception {
        googleCalendarService.crearEventoSimple(resumen, descripcion, fechaInicio, fechaFin);
        return "✅ Evento creado correctamente.";
    }

    @GetMapping("/events")
    public Events listarEventos() throws Exception {
        return googleCalendarService.listarProximosEventos();
    }

    @DeleteMapping("/events/{eventId}")
    public String borrarEvento(@PathVariable String eventId) throws Exception {
        googleCalendarService.borrarEventoPorId(eventId);
        return "🗑️ Evento eliminado con ID: " + eventId;
    }

    @GetMapping("/events/{eventId}")
    public Event obtenerEventoPorId(@PathVariable String eventId) throws Exception {
        return googleCalendarService.obtenerEventoPorId(eventId);
    }

    @GetMapping("/calendars")
    public String listarCalendarios() throws Exception {
        googleCalendarService.listarCalendarios();
        return "📚 Lista de calendarios mostrada por consola.";
    }
}
