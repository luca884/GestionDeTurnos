package com.utn.gestion_de_turnos.API_Calendar.Service;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.List;

@Service
public class GoogleCalendarService {

    private final Calendar calendar;

    @Autowired
    public GoogleCalendarService(Calendar calendar) {
        this.calendar = calendar;
    }


    public Event crearEventoSimple(String resumen, String descripcion, String fechaInicio, String fechaFin)
            throws IOException {

        Event event = new Event()
                .setSummary(resumen)
                .setDescription(descripcion);

        EventDateTime start = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(fechaInicio))
                .setTimeZone("America/Argentina/Buenos_Aires");
        event.setStart(start);

        EventDateTime end = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(fechaFin))
                .setTimeZone("America/Argentina/Buenos_Aires");
        event.setEnd(end);

        return calendar.events().insert("primary", event).execute();
    }


    public Events listarProximosEventos() throws IOException {
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
