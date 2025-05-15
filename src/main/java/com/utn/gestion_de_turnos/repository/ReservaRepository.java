package com.utn.gestion_de_turnos.repository;

import com.utn.gestion_de_turnos.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByEstado(Reserva.Estado estado);
    List<Reserva> findByClienteId(Long clienteId);
    List<Reserva> findBySalaId(Long salaId);
}