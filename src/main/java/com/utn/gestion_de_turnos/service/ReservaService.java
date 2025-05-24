package com.utn.gestion_de_turnos.service;


import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.utn.gestion_de_turnos.API_Calendar.Service.GoogleCalendarService;
import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.repository.ClienteRepository;
import com.utn.gestion_de_turnos.repository.ReservaRepository;
import io.jsonwebtoken.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired
    private GoogleCalendarService googleCalendarService;
    private ClienteRepository clienteRepository;
    @Autowired


    @Transactional
    public Reserva crearReserva(Reserva reserva) throws IOException, java.io.IOException {
        // Guardas primero en la BDD sin el Google Event ID
        Reserva reservaGuardada = reservaRepository.save(reserva);

        // Crear evento en Google Calendar
        String resumen = "Reserva de " + reserva.getCliente().getNombre();
        String descripcion = "Sala: " + reserva.getSala().getNumero() + "\nEmailCliente: " + reserva.getCliente().getEmail();

        // Convertir Date a formato String ISO para la API
        String fechaInicio = new DateTime(String.valueOf(reserva.getFechaInicio())).toString();
        String fechaFin = String.valueOf(new DateTime(reserva.getFechaFinal().toString()));

        Event evento = googleCalendarService.crearEventoSimple(resumen, descripcion, fechaInicio, fechaFin);

        // Guardar el ID del evento en la reserva
        reservaGuardada.setGoogleEventId(evento.getId());

        // Guardar de nuevo en la base con el ID de Google
        return reservaRepository.save(reservaGuardada);
    }

    @GetMapping("/{id}")
    public Optional<Reserva> findById(Long id) {
        return reservaRepository.findById(id);
    }

    public List<Reserva> findAll() {
        return reservaRepository.findAll();
    }
    @Transactional
    public void eliminarReserva(Long id) throws IOException, java.io.IOException {
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            // Borrar evento de Google Calendar
            if (reserva.getGoogleEventId() != null) {
                googleCalendarService.borrarEventoPorId(reserva.getGoogleEventId());
            }
            // Borrar reserva de la base
            reservaRepository.deleteById(id);
        } else {
            throw new RuntimeException("Reserva no encontrada");
        }
    }


    public List<Reserva> findByEstado(Reserva.Estado estado) {
        return reservaRepository.findByEstado(estado);
    }

    public Reserva cancelarReserva(Long id) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(id);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            reserva.setEstado(Reserva.Estado.CANCELADO);
            return reservaRepository.save(reserva);
        }
        return null;
    }

    public List<Reserva> findActiveByClienteId(Long clienteId) {
        return reservaRepository.findActiveByClienteId(clienteId);
    }
}