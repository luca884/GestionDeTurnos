package com.utn.gestion_de_turnos.controller.api;

import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.service.SalaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Eso es controlador REST para manejar las operaciones CRUD de la entidad Sala

@RestController
@RequestMapping("/api/salas")
public class SalaApiController {

    @Autowired
    private SalaService salaService;

    @PostMapping
    public Sala createSala(@RequestBody Sala sala) {
        return salaService.save(sala);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sala> getSalaById(@PathVariable Long id) {
        return salaService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }



    @GetMapping
    public List<Sala> getAllSalas() {
        return salaService.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteSalaById(@PathVariable Long id) {
        salaService.deleteById(id);
    }

    @PutMapping("/{id}/disponibilidad")
    public ResponseEntity<?> updateDisponibilidad(@PathVariable Long id,
                                                  @RequestBody SalaDisponibilidadUpdateRequest request) {
        return salaService.findById(id).map(sala -> {
            sala.setDisponibilidad(request.isDisponibilidad());
            salaService.save(sala);
            return ResponseEntity.noContent().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    public static class SalaDisponibilidadUpdateRequest {
        private boolean disponibilidad;

        public boolean isDisponibilidad() {
            return disponibilidad;
        }

        public void setDisponibilidad(boolean disponibilidad) {
            this.disponibilidad = disponibilidad;
        }
    }


}

