package API_Calendar.Controller;


import API_Calendar.Service.CalendarService;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    @Autowired
    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @PostMapping("/events")
    public String crearEvento(
            @RequestParam String resumen,
            @RequestParam String descripcion,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin
    ) throws Exception {
        calendarService.crearEventoSimple(resumen, descripcion, fechaInicio, fechaFin);
        return "✅ Evento creado correctamente.";
    }

    @GetMapping("/events")
    public Events listarEventos() throws Exception {
        return calendarService.listarProximosEventos();
    }

    @DeleteMapping("/events/{eventId}")
    public String borrarEvento(@PathVariable String eventId) throws Exception {
        calendarService.borrarEventoPorId(eventId);
        return "🗑️ Evento eliminado con ID: " + eventId;
    }

    @GetMapping("/events/{eventId}")
    public Event obtenerEventoPorId(@PathVariable String eventId) throws Exception {
        return calendarService.obtenerEventoPorId(eventId);
    }

    @GetMapping("/calendars")
    public String listarCalendarios() throws Exception {
        calendarService.listarCalendarios();
        return "📚 Lista de calendarios mostrada por consola.";
    }
}
