package com.utn.gestion_de_turnos.repository;

import com.utn.gestion_de_turnos.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByEstado(Reserva.Estado estado);

    List<Reserva> findByClienteId(Long clienteId);

    List<Reserva> findByClienteIdAndEstado(Long clienteId, Reserva.Estado estado);

    List<Reserva> findBySalaId(Long salaId);


    @Query("SELECT t FROM Reserva t WHERE t.sala.id = :salaId AND t.estado = 'ACTIVO' AND " +
            "(:fechaInicio < t.fechaFinal AND :fechaFinal > t.fechaInicio)")
    List<Reserva> findConflictingReservas(@Param("salaId") Long salaId,
                                          @Param("fechaInicio") LocalDateTime fechaInicio,
                                          @Param("fechaFinal") LocalDateTime fechaFinal);
}
