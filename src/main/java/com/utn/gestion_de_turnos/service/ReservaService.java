package com.utn.gestion_de_turnos.service;

import com.google.api.services.calendar.model.Event;
import com.utn.gestion_de_turnos.API_Calendar.Service.GoogleCalendarService;
import com.utn.gestion_de_turnos.exception.AccesoProhibidoException;
import com.utn.gestion_de_turnos.exception.ReservaNoCancelableException;
import com.utn.gestion_de_turnos.exception.ReservaNotFoundException;
import com.utn.gestion_de_turnos.exception.TiempoDeReservaOcupadoException;
import com.utn.gestion_de_turnos.model.Cliente;
import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.model.Usuario;
import com.utn.gestion_de_turnos.repository.ClienteRepository;
import com.utn.gestion_de_turnos.repository.ReservaRepository;
import com.utn.gestion_de_turnos.repository.SalaRepository;
import com.utn.gestion_de_turnos.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired
    private GoogleCalendarService googleCalendarService;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private SalaRepository salaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public Reserva saveReserva(Long clienteId, Long salaId, LocalDateTime fechaInicio, LocalDateTime fechaFinal, Reserva.TipoPago tipoPago) {

        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow(() ->
                new RuntimeException("Cliente no encontrado"));
        Sala sala = salaRepository.findById(salaId).orElseThrow(() ->
                new RuntimeException("Sala no encontrada"));
        List<Reserva> conflictingReservas = reservaRepository.findConflictingReservas(salaId, fechaInicio, fechaFinal);
        if (!conflictingReservas.isEmpty()) {
            throw new TiempoDeReservaOcupadoException("El turno se superpone con otro existente");
        }
        Reserva reserva = new Reserva();
        reserva.setCliente(cliente);
        reserva.setSala(sala);
        reserva.setFechaInicio(fechaInicio);
        reserva.setFechaFinal(fechaFinal);
        reserva.setTipoPago(tipoPago);
        reserva.setEstado(Reserva.Estado.ACTIVO);

        reservaRepository.save(reserva);

        // Crear evento en Google Calendar
        try {
            String titulo = "Reserva de " + reserva.getCliente().getNombre();
            String descripcion = "Sala: " + reserva.getSala().getNumero() + "\nEmailCliente: " + reserva.getCliente().getEmail();


            Event evento = googleCalendarService.crearEventoConReserva(titulo, descripcion, reserva.getFechaInicio(), reserva.getFechaFinal());

            // Guardar el ID del evento en la reserva
            reserva.setGoogleEventId(evento.getId());

            // Guardar de nuevo en la base con el ID de Google
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Error al crear el evento en Google Calendar: " + e.getMessage(), e);
        }
        return reservaRepository.save(reserva);
    }

    @Transactional
    public void cancelarReservaPorCliente(Long reservaId, Long clienteId) {

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
        googleCalendarService.eliminarEvento(reserva.getGoogleEventId());
        reservaRepository.save(reserva);
    }

    @Transactional
    public void cancelarReservaPorEmpleado(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException("Reserva no encontrada"));

        if (reserva.getEstado() != Reserva.Estado.ACTIVO) {
            throw new ReservaNoCancelableException("Solo se pueden cancelar reservas activas");
        }

        if (reserva.getFechaInicio().isBefore(LocalDateTime.now())) {
            throw new ReservaNoCancelableException("No se puede cancelar una reserva que ya ha comenzado");
        }
        reserva.setEstado(Reserva.Estado.CANCELADO);
        googleCalendarService.eliminarEvento(reserva.getGoogleEventId());
        reservaRepository.save(reserva);
    }


    @Transactional
    public void modificar(Reserva reserva) {
        if (!reservaRepository.existsById(reserva.getId())) {
            throw new EntityNotFoundException("La reserva no existe");
        }
        List<Reserva> conflictingReservas = reservaRepository.findConflictingReservas(
                        reserva.getSala().getId(),
                        reserva.getFechaInicio(),
                        reserva.getFechaFinal()
                ).stream().filter(r -> !r.getId().equals(reserva.getId())) // исключаем текущую
                .collect(Collectors.toList());
        if (!conflictingReservas.isEmpty()) {
            throw new TiempoDeReservaOcupadoException("El turno se superpone con otro existente");
        }

        reservaRepository.save(reserva);

        // Modificar evento en Google Calendar
        String titulo = "Reserva de " + reserva.getCliente().getNombre();
        String descripcion = "Sala: " + reserva.getSala().getNumero() + "\nEmailCliente: " + reserva.getCliente().getEmail();

        googleCalendarService.modificarEvento(
                reserva.getGoogleEventId(),
                titulo,
                descripcion,
                reserva.getFechaInicio(),
                reserva.getFechaFinal()
        );
    }


    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }

    public List<Reserva> listarPorUsuarioActual() {
        Usuario usuario = obtenerUsuarioActual();
        return reservaRepository.findByClienteId(usuario.getId());
    }

    public Reserva buscarPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + id));
    }

    public void eliminar(Long id) throws Exception {

        if (reservaRepository.existsById(id)) {
            Optional<Reserva> reserva = reservaRepository.findById(id);
            googleCalendarService.eliminarEvento(reserva.get().getGoogleEventId());
            reservaRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("La reserva no existe");
        }
    }


    private Usuario obtenerUsuarioActual() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado por email: " + email));
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


    public List<Reserva> findAllActivas() {
        return reservaRepository.findByEstado(Reserva.Estado.ACTIVO);
    }

    public List<Reserva> findByEstado(Reserva.Estado estado) {
        return reservaRepository.findByEstado(estado);
    }


    public boolean existsActiveReservaForSala(Long salaId) {
        List<Reserva> reservas = reservaRepository.findBySalaIdAndEstado(salaId, Reserva.Estado.ACTIVO);
        return !reservas.isEmpty();
    }

    public void updateReserva(Reserva reserva) {
        reservaRepository.save(reserva);
    }

}