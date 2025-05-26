package com.utn.gestion_de_turnos.API_Calendar.Service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.repository.ReservaRepository;
import org.hibernate.validator.constraints.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.time.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GoogleCalendarService {

    private final Calendar calendar;

    @Autowired
    public GoogleCalendarService(Calendar calendar) {
        this.calendar = calendar;
    }

    private ReservaRepository reservaRepository;


    public Event crearEventoSimple(String resumen, String descripcion, LocalDateTime fechaInicio, LocalDateTime fechaFin)
            throws IOException {

        // Convertir LocalDateTime a java.util.Date
        Date inicioDate =  Date.from(fechaInicio.atZone(ZoneId.of("America/Argentina/Buenos_Aires")).toInstant());
        Date finDate =  Date.from(fechaFin.atZone(ZoneId.of("America/Argentina/Buenos_Aires")).toInstant());

        // Convertir java.util.Date a DateTime (de la API de Google)
        DateTime fechaInicioGoogle = new DateTime(inicioDate);
        DateTime fechaFinGoogle = new DateTime(finDate);

        Event event = new Event()
                .setSummary(resumen)
                .setDescription(descripcion);

        EventDateTime start = new EventDateTime()
                .setDateTime(fechaInicioGoogle)
                .setTimeZone("America/Argentina/Buenos_Aires");
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(fechaFinGoogle)
                .setTimeZone("America/Argentina/Buenos_Aires");
        event.setEnd(end);

        return calendar.events().insert("primary", event).execute();
    }


    public List<Reserva> buscarReservasConFiltros(
            Optional<Long> salaId,
            Optional<Long> clienteId,
            Optional<LocalDate> fecha,
            Optional<LocalDate> fechaInicio,
            Optional<LocalDate> fechaFin,
            Optional<DayOfWeek> diaSemana,
            Optional<LocalTime> horaInicio,
            Optional<LocalTime> horaFin
    ) {
        Specification<Reserva> spec = Specification.where(ReservaSpecification.hasSalaId(salaId))
                .and(ReservaSpecification.hasClienteId(clienteId))
                // solo usar fecha si no se usan fechas de rango
                .and(fechaInicio.isEmpty() && fechaFin.isEmpty()
                        ? ReservaSpecification.hasFecha(fecha)
                        : ReservaSpecification.entreFechas(fechaInicio, fechaFin))
                .and(ReservaSpecification.hasDiaSemana(diaSemana))
                .and(ReservaSpecification.entreHoras(horaInicio, horaFin));

        return reservaRepository.findAll(spec);
    }



    public List<Event> listarTodosLosEventos() throws IOException {
        Events events = calendar.events().list("primary")
                .setMaxResults(100)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        return events.getItems(); // Devuelve lista completa para empleados
    }

    public List<Event> listarEventosParaCliente(String emailCliente) throws IOException {
        List<Event> todosLosEventos = listarTodosLosEventos();
        List<Event> eventosFiltrados = new ArrayList<>();

        for (Event event : todosLosEventos) {
            String descripcion = event.getDescription() != null ? event.getDescription() : "";

            if (descripcion.contains("EmailCliente: " + emailCliente)) {
                // Mostrar evento completo si es del cliente
                eventosFiltrados.add(event);
            } else {
                // Mostrar como ocupado si no es del cliente
                Event eventoOculto = new Event()
                        .setSummary("No disponible")
                        .setStart(event.getStart())
                        .setEnd(event.getEnd());
                eventosFiltrados.add(eventoOculto);
            }
        }

        return eventosFiltrados;
    }








/*    public Events listarProximosEventos() throws IOException {
        Events events = calendar.events().list("primary")
                .setMaxResults(10)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("📭 No hay eventos próximos.");
        } else {
            for (Event event : items) {
                String resumen = event.getSummary();
                String inicio = event.getStart().getDateTime() != null ?
                        event.getStart().getDateTime().toString() :
                        event.getStart().getDate().toString();
                System.out.printf("📌 %s (Inicio: %s) | ID: %s\n", resumen, inicio, event.getId());
            }
        }
        return events;
        }
 */

    public void borrarEventoPorId(String eventId) throws IOException {
        calendar.events().delete("primary", eventId).execute();
        System.out.println("🗑️ Evento eliminado con ID: " + eventId);
    }

    public Event obtenerEventoPorId(String eventId) throws IOException {
        return calendar.events().get("primary", eventId).execute();
    }

    public void listarCalendarios() throws IOException {
        CalendarList calendarList = calendar.calendarList().list().execute();
        for (CalendarListEntry entry : calendarList.getItems()) {
            System.out.printf("📖 Nombre: %s | ID: %s\n", entry.getSummary(), entry.getId());
        }
    }
}
