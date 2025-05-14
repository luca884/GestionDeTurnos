package com.utn.gestion_de_turnos.repository;

import com.utn.gestion_de_turnos.model.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaRepository extends JpaRepository<Sala,Long> {

    List<Sala> findByDisponibilidadTrue(); // Buscar salas disponibles

    List<Sala> findByDisponibilidadFalse(); // (opcional) Salas ocupadas




}
