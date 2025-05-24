package com.utn.gestion_de_turnos.controller.api;

import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.security.CustomUserDetails;
import com.utn.gestion_de_turnos.service.ReservaService;
import com.utn.gestion_de_turnos.service.SalaService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// Eso es controlador REST para manejar las operaciones CRUD de la entidad Sala

@RestController
@RequestMapping("/api/salas")
public class SalaApiController {

    @Autowired
    private SalaService salaService;

    @Autowired
    private ReservaService reservaService;

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

    @GetMapping("/cliente/activas")
    public ResponseEntity<List<ReservaResponse>> getReservasActivasCliente(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long clienteId = userDetails.getId();

        List<Reserva> reservas = reservaService.findActiveByClienteId(clienteId);

        List<ReservaResponse> response = reservas.stream()
                .map(reserva -> new ReservaResponse(
                        reserva.getId(),
                        reserva.getSala().getNumero(),
                        reserva.getSala().getCantPersonas(),
                        reserva.getFechaInicio(),
                        reserva.getFechaFinal(),
                        reserva.getTipoPago(),
                        reserva.getEstado().name()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // DTO
    @Data
    @AllArgsConstructor
    public static class ReservaResponse {
        private Long id;
        private Integer salaNumero;
        private Integer salaCapacidad;
        private LocalDateTime fechaInicio;
        private LocalDateTime fechaFinal;
        private Reserva.TipoPago tipoPago;
        private String estado;
    }


}

