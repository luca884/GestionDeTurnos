package com.utn.gestion_de_turnos.controller.api;

import com.utn.gestion_de_turnos.dto.ReservaResponseEnSalaDTO;
import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.security.CustomUserDetails;
import com.utn.gestion_de_turnos.service.ReservaService;
import com.utn.gestion_de_turnos.service.SalaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Eso es controlador REST para manejar las operaciones CRUD de la entidad Sala

@Tag(name = "Sala", description = "Operaciones relacionadas con salas")
@RestController
@RequestMapping("/api/salas")
public class SalaApiController {

    @Autowired
    private SalaService salaService;

    @Autowired
    private ReservaService reservaService;


    @Operation(summary = "Crear una nueva sala")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sala creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud")
    })
    @PostMapping
    public Sala createSala(@RequestBody Sala sala) {
        return salaService.save(sala);
    }

    @Operation(summary = "Obtener sala por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sala encontrada y devuelta"),
            @ApiResponse(responseCode = "404", description = "Sala no encontrada con el ID proporcionado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Sala> getSalaById(@PathVariable Long id) {
        return salaService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @Operation(summary = "Obtener todas las salas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de salas devuelta correctamente"),
            @ApiResponse(responseCode = "404", description = "No se encontraron salas")
    })
    @GetMapping
    public List<Sala> getAllSalas() {
        return salaService.findAll();
    }

    @Operation(summary = "Eliminar sala por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sala eliminada correctamente"),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar la sala porque tiene reservas activas"),
            @ApiResponse(responseCode = "500", description = "Error inesperado en el servidor")
    })
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


    @Operation(summary = "Verifica si una sala puede ser eliminada (sin reservas activas)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devuelve true si la sala puede eliminarse, false si no")
    })
    @GetMapping("/{id}/can-delete")
    public ResponseEntity<Boolean> canDeleteSala(@PathVariable Long id) {
        boolean canDelete = salaService.canDeleteSala(id);
        return ResponseEntity.ok(canDelete);
    }


    @Operation(summary = "Obtiene las reservas activas del cliente autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de reservas activas del cliente")
    })
    @GetMapping("/cliente/activas")
    public ResponseEntity<List<ReservaResponseEnSalaDTO>> getReservasActivasCliente(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long clienteId = userDetails.getId();

        List<Reserva> reservas = reservaService.findActiveByClienteId(clienteId);

        List<ReservaResponseEnSalaDTO> response = reservas.stream()
                .map(reserva -> new ReservaResponseEnSalaDTO(
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


    @Operation(summary = "Actualiza la descripción de una sala")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirige después de actualizar o si la sala no fue encontrada")
    })
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

}

