package com.utn.gestion_de_turnos.service;

import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.repository.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SalaService {

    @Autowired
    private SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    public Sala save(Sala sala) {
        return salaRepository.save(sala);
    }

    public Optional<Sala> findById(Long id) {
        return salaRepository.findById(id);
    }

    public List<Sala> findAll() {
        return salaRepository.findAll();
    }

    public void deleteById(Long id) {
        salaRepository.deleteById(id);
    }

    public List<Sala> encontrarSalasDisponibles(LocalDateTime fechaInicio, LocalDateTime fechaFinal) {
        return salaRepository.encontrarSalasDisponibles(fechaInicio, fechaFinal);
    }


}
