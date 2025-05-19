package com.utn.gestion_de_turnos.service;

import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    public Reserva save(Reserva reserva) {
        return reservaRepository.save(reserva);
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