package com.utn.gestion_de_turnos.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.utn.gestion_de_turnos.API_Calendar.Service.GoogleCalendarService;
import com.utn.gestion_de_turnos.API_Calendar.Service.ReservaSpecification;
import com.utn.gestion_de_turnos.exception.AccesoProhibidoException;
import com.utn.gestion_de_turnos.exception.ReservaNoCancelableException;
import com.utn.gestion_de_turnos.exception.ReservaNotFoundException;
import com.utn.gestion_de_turnos.exception.TiempoDeReservaOcupadoException;
import com.utn.gestion_de_turnos.model.Cliente;
import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.repository.ClienteRepository;
import com.utn.gestion_de_turnos.repository.ReservaRepository;
import com.utn.gestion_de_turnos.repository.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import com.google.api.client.util.DateTime;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Transactional
    public Reserva crearReserva(Reserva reserva) throws IOException {
        // Guardás primero en la BDD sin el Google Event ID
        Reserva reservaGuardada = reservaRepository.save(reserva);

        // Crear evento en Google Calendar
        String resumen = "Reserva de " + reserva.getCliente().getNombre();
        String descripcion = "Sala: " + reserva.getSala().getNumero() + "\nEmailCliente: " + reserva.getCliente().getEmail();


        Event evento = googleCalendarService.crearEventoSimple(resumen, descripcion, reserva.getFechaInicio(),reserva.getFechaFinal());

        // Guardar el ID del evento en la reserva
        reserva.setGoogleEventId(evento.getId());

        // Guardar de nuevo en la base con el ID de Google
        return reservaRepository.save(reserva);
    }


    public List<Reserva> findActiveByClienteId(Long clienteId) {
        return reservaRepository.findByClienteIdAndEstado(clienteId, Reserva.Estado.ACTIVO);
    }

    public Optional<Reserva> findById(Long id) {
        return reservaRepository.findById(id);
    }

    public List<Reserva> findAll() {
        return reservaRepository.findAll();
    }

    public List<Reserva> findByEstado(Reserva.Estado estado) {
        return reservaRepository.findByEstado(estado);
    }


    public void cancelarReservaById(Long reservaId, Long clienteId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException("Reserva no encontrada"));

        if (!reserva.getCliente().getId().equals(clienteId)) {
            throw new AccesoProhibidoException("No tienes permiso para cancelar esta reserva");
        }

        if (reserva.getEstado() != Reserva.Estado.ACTIVO) {
            throw new ReservaNoCancelableException("Solo se pueden cancelar reservas activas");
        }

        if (reserva.getFechaInicio().isBefore(LocalDateTime.now())) {
            throw new ReservaNoCancelableException("No se puede cancelar una reserva que ya ha comenzado");
        }

        reserva.setEstado(Reserva.Estado.CANCELADO);
        reservaRepository.save(reserva);
    }

    public boolean existsActiveReservaForSala(Long salaId) {
        List<Reserva> reservas = reservaRepository.findBySalaIdAndEstado(salaId, Reserva.Estado.ACTIVO);
        return !reservas.isEmpty();
    }

    public void updateReserva(Reserva reserva) {
        reservaRepository.save(reserva);
    }




}