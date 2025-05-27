package com.utn.gestion_de_turnos.repository;

import com.utn.gestion_de_turnos.model.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalaRepository extends JpaRepository<Sala,Long> {


    @Query("SELECT s FROM Sala s WHERE s.id NOT IN (" +
            "SELECT r.sala.id FROM Reserva r WHERE " +
            "r.fechaInicio < :fechaFinal AND r.fechaFinal > :fechaInicio)")
    List<Sala> encontrarSalasDisponibles(@Param("fechaInicio") LocalDateTime fechaInicio,
                                  @Param("fechaFinal") LocalDateTime fechaFinal);

}