package com.utn.gestion_de_turnos.service;

import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.repository.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class SalaService {
    @Autowired
    private SalaRepository salaRepository;


    public Sala save (Sala sala){
        return salaRepository.save(sala);
    }

    public Optional<Sala> findById(Long id){
        return salaRepository.findById(id);
    }

    public List<Sala> findAll(){
        return salaRepository.findAll();
    }

    public List<Sala> findSalasDisponibles(){
        return salaRepository.findByDisponibilidadTrue();
    }

    public List<Sala> findSalasNoDisponibles(){
        return  salaRepository.findByDisponibilidadFalse();
    }

    public void deleteByID(Long id){
        salaRepository.deleteById(id);
    }

}
