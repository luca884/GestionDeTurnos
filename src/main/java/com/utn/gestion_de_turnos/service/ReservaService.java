package com.utn.gestion_de_turnos.service;

import com.utn.gestion_de_turnos.exception.TiempoDeReservaOcupadoException;
import com.utn.gestion_de_turnos.model.Cliente;
import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.repository.ClienteRepository;
import com.utn.gestion_de_turnos.repository.SalaRepository;
import com.utn.gestion_de_turnos.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private SalaRepository salaRepository;


    public Reserva crearReserva(Long clienteId, Long salaId, LocalDateTime fechaInicio, LocalDateTime fechaFinal, Reserva.TipoPago tipoPago) {
        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow(()->
                new RuntimeException("Cliente no encontrado"));
        Sala sala = salaRepository.findById(salaId).orElseThrow(()->
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

    public void deleteById(Long id) {
        reservaRepository.deleteById(id);
    }

    public List<Reserva> findByEstado(Reserva.Estado estado) {
        return reservaRepository.findByEstado(estado);
    }

    public List<Reserva> findByClienteId(Long clienteId) {
        return reservaRepository.findByClienteId(clienteId);
    }

    public List<Reserva> findBySalaId(Long salaId) {
        return reservaRepository.findBySalaId(salaId);
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
}