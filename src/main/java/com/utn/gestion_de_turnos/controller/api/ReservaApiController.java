package com.utn.gestion_de_turnos.controller.api;


import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.service.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/reserva")
public class ReservaApiController {

    @Autowired
    private ReservaService reservaService;

    @PostMapping
    public Reserva createReserva(@RequestBody Reserva reserva) throws IOException {
        return reservaService.crearReserva(reserva);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reserva> getReservaById(@PathVariable Long id){
        return reservaService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public void deleteReservaById(@PathVariable Long id) throws IOException {
        reservaService.eliminarReserva(id);
    }

    @GetMapping
    public List<Reserva> findAll(){
       return reservaService.findAll();
    }

    @GetMapping("/estado")
    public void findByEstado(Reserva.Estado estado){
        reservaService.findByEstado(estado);
    }

    @PutMapping("/{id}/cancelar")
    public void cancelarReserva(@PathVariable Long id){
        reservaService.cancelarReserva(id);
    }











}
