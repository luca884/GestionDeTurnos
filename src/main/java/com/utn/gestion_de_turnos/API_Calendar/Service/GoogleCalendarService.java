// GoogleCalendarService.java
package com.utn.gestion_de_turnos.API_Calendar.Service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.model.Usuario;
import com.utn.gestion_de_turnos.repository.ReservaRepository;
import com.utn.gestion_de_turnos.repository.UsuarioRepository;
import com.utn.gestion_de_turnos.API_Calendar.Factory.GoogleCalendarClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GoogleCalendarService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GoogleCalendarClientFactory calendarClientFactory;

    private Calendar getCalendar() throws Exception {
        return calendarClientFactory.getCalendarService();
    }

    public Event crearEventoConReserva(String resumen, String descripcion, LocalDateTime inicio, LocalDateTime fin) throws IOException {
        Calendar calendar = getCalendar();

        Event event = new Event()
                .setSummary(resumen)
                .setDescription(descripcion)
                .setStart(new EventDateTime()
                        .setDateTime(new DateTime(Date.from(inicio.atZone(ZoneId.systemDefault()).toInstant())))
                        .setTimeZone("America/Argentina/Buenos_Aires"))
                .setEnd(new EventDateTime()
                        .setDateTime(new DateTime(Date.from(fin.atZone(ZoneId.systemDefault()).toInstant())))
                        .setTimeZone("America/Argentina/Buenos_Aires"));

        Event createdEvent = calendar.events().insert("primary", event).execute();

        Reserva reserva = new Reserva();
        reserva.setTitulo(resumen);
        reserva.setDescripcion(descripcion);
        reserva.setInicio(inicio);
        reserva.setFin(fin);
        reserva.setGoogleEventId(createdEvent.getId());
        reserva.setUsuario(getUsuarioActual());
        reservaRepository.save(reserva);

        return createdEvent;
    }

    public List<Event> listarEventos() throws Exception {
        Calendar calendar = getCalendar();
        Events events = calendar.events().list("primary").execute();
        List<Event> items = events.getItems();
        Usuario actual = getUsuarioActual();

        if (actual.getRol().equals("CLIENTE")) {
            return items.stream()
                    .filter(event -> reservaRepository.findByGoogleEventId(event.getId())
                            .map(r -> r.getUsuario().getId().equals(actual.getId()))
                            .orElse(false))
                    .collect(Collectors.toList());
        }
        return items;
    }

    public List<Event> filtrarEventos(Long idSala, Long idCliente, LocalDate fecha) throws Exception {
        Calendar calendar = getCalendar();
        List<Reserva> reservasFiltradas = reservaRepository.findAll().stream()
                .filter(reserva -> (idSala == null || (reserva.getSala() != null && reserva.getSala().getId().equals(idSala)))
                        && (idCliente == null || reserva.getUsuario().getId().equals(idCliente))
                        && (fecha == null || reserva.getInicio().toLocalDate().equals(fecha)))
                .collect(Collectors.toList());

        List<String> idsEventos = reservasFiltradas.stream()
                .map(Reserva::getGoogleEventId)
                .collect(Collectors.toList());

        Events allEvents = calendar.events().list("primary").execute();

        return allEvents.getItems().stream()
                .filter(event -> idsEventos.contains(event.getId()))
                .collect(Collectors.toList());
    }

    public void eliminarEvento(String idEvento) throws Exception {
        Calendar calendar = getCalendar();
        Optional<Reserva> reservaOpt = reservaRepository.findByGoogleEventId(idEvento);

        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            Usuario actual = getUsuarioActual();
            if (actual.getRol().equals("CLIENTE") && !reserva.getUsuario().getId().equals(actual.getId())) {
                throw new SecurityException("No puedes eliminar reservas de otros usuarios");
            }
            reservaRepository.delete(reserva);
        }
        calendar.events().delete("primary", idEvento).execute();
    }

    public void modificarEvento(String idEvento, String nuevoTitulo, String nuevaDescripcion) throws Exception {
        Calendar calendar = getCalendar();
        Event event = calendar.events().get("primary", idEvento).execute();
        event.setSummary(nuevoTitulo);
        event.setDescription(nuevaDescripcion);

        Optional<Reserva> reservaOpt = reservaRepository.findByGoogleEventId(idEvento);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            Usuario actual = getUsuarioActual();
            if (actual.getRol().equals("CLIENTE") && !reserva.getUsuario().getId().equals(actual.getId())) {
                throw new SecurityException("No puedes modificar reservas de otros usuarios");
            }
            reserva.setTitulo(nuevoTitulo);
            reserva.setDescripcion(nuevaDescripcion);
            reservaRepository.save(reserva);
        }
        calendar.events().update("primary", idEvento, event).execute();
    }

    public Event obtenerEventoPorId(String idEvento) throws IOException {
        Calendar calendar = getCalendar();
        Event event = calendar.events().get("primary", idEvento).execute();

        Usuario actual = getUsuarioActual();
        Optional<Reserva> reservaOpt = reservaRepository.findByGoogleEventId(idEvento);

        if (actual.getRol().equals("CLIENTE") && reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            if (!reserva.getUsuario().getId().equals(actual.getId())) {
                throw new SecurityException("No puedes acceder a eventos de otros usuarios");
            }
        }
        return event;
    }

    private Usuario getUsuarioActual() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}
