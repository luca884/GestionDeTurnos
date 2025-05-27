package com.utn.gestion_de_turnos.repository;

import com.utn.gestion_de_turnos.model.Reserva;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByEstado(Reserva.Estado estado);

    List<Reserva> findByClienteId(Long clienteId);

    List<Reserva> findByClienteIdAndEstado(Long clienteId, Reserva.Estado estado);

    List<Reserva> findBySalaId(Long salaId);

    Optional<Reserva> findByGoogleEventId(String eventId);


    @Query("SELECT t FROM Reserva t WHERE t.sala.id = :salaId AND t.estado = 'ACTIVO' AND " +
            "(:fechaInicio < t.fechaFinal AND :fechaFinal > t.fechaInicio)")
    List<Reserva> findConflictingReservas(@Param("salaId") Long salaId,
                                          @Param("fechaInicio") LocalDateTime fechaInicio,
                                          @Param("fechaFinal") LocalDateTime fechaFinal);


    @Query("SELECT r FROM Reserva r WHERE r.sala.id = :salaId AND r.estado = :estado")
    List<Reserva> findBySalaIdAndEstado(@Param("salaId") Long salaId,
                                        @Param("estado") Reserva.Estado estado);

    List<Reserva> findAll(Specification<Reserva> spec);

    List<Reserva> findByUsuarioId(Long id);
}