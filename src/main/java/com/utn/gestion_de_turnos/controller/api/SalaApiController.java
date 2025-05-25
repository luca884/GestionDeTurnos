package com.utn.gestion_de_turnos.controller.api;

import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.security.CustomUserDetails;
import com.utn.gestion_de_turnos.service.ReservaService;
import com.utn.gestion_de_turnos.service.SalaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    public ResponseEntity<?> deleteSalaById(@PathVariable Long id) {
        try {
            salaService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar la sala porque tiene reservas activas.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado al eliminar la sala.");
        }
    }

    @GetMapping("/{id}/can-delete")
    public ResponseEntity<Boolean> canDeleteSala(@PathVariable Long id) {
        boolean canDelete = salaService.canDeleteSala(id);
        return ResponseEntity.ok(canDelete);
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


    @PostMapping("/update")
    public String updateSalaDescription(
            @RequestParam Long id,
            @RequestParam String descripcion,
            RedirectAttributes redirectAttributes) {

        Optional<Sala> salaOpt = salaService.findById(id);
        if (salaOpt.isPresent()) {
            Sala existingSala = salaOpt.get();
            existingSala.setDescripcion(descripcion);
            salaService.save(existingSala);
            redirectAttributes.addFlashAttribute("success", "Sala actualizada correctamente");
            return "redirect:/admin/salas";
        } else {
            redirectAttributes.addFlashAttribute("error", "Sala no encontrada");
            return "redirect:/admin/salas/update?id=" + id;
        }
    }


    // DTOs

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

