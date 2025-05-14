package com.utn.gestion_de_turnos.controller;

import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.service.SalaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

public class SalaController {

    @Autowired
    private SalaService salaService;

    @PostMapping //Anotacion que sirve para ingresar informaion que ingresa el usuario
    public Sala createSala(@RequestBody Sala sala ){ // (este caso crear una Sala con los datos cargados)
        return salaService.save(sala);
    }

    @GetMapping //Anotacion que sirve para devolver información al usuario
    public Optional<Sala> getSalaById(@PathVariable Long id){ // esta anotacion se refiere a que el dato del parametro sera ingresado por el fronted
        return salaService.findById(id);
    }

    @GetMapping
    public List<Sala> getSalasAll(){
        return salaService.findAll();
    }

    @GetMapping
    public List<Sala> getSalasDisponibles(){
        return salaService.findSalasDisponibles();
    }

    @GetMapping
    public List<Sala> getSalasNoDisponibles(){
        return salaService.findSalasNoDisponibles();
    }

    @DeleteMapping
    public void deleteSalaById(@PathVariable Long id){
        salaService.deleteByID(id);
    }





}
