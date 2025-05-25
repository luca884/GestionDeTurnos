package com.utn.gestion_de_turnos.controller.api;

import com.utn.gestion_de_turnos.exception.TiempoDeReservaOcupadoException;
import com.utn.gestion_de_turnos.model.Cliente;
import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.security.CustomUserDetails;
import com.utn.gestion_de_turnos.service.ClienteService;
import com.utn.gestion_de_turnos.service.ReservaService;
import com.utn.gestion_de_turnos.service.SalaService;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reserva")
public class ReservaApiController {

    @Autowired
    private ReservaService reservaService;
    private ClienteService clienteService;
    private SalaService salaService;

    @PostMapping
    public ResponseEntity<?> saveReserva(@RequestBody ReservaRequest request, Authentication authentication) {
        try {
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
            Long clienteId = user.getId();
            Reserva reserva = reservaService.crearReserva(
                    clienteId,
                    request.getSalaId(),
                    request.getFechaInicio(),
                    request.getFechaFinal(),
                    request.getTipoPago());
            return ResponseEntity.ok(reserva);
        } catch (TiempoDeReservaOcupadoException e) {
            Map<String, String> error = Map.of("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long clienteId = userDetails.getId();

        try {
            reservaService.cancelarReservaById(id, clienteId);
            return ResponseEntity.ok(Map.of("message", "Reserva cancelada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
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
    public List<Reserva> findByEstado(@RequestParam Reserva.Estado estado){
        return reservaService.findByEstado(estado);
    }

    // DTO interno
    @Data
    public static class ReservaRequest {

        @NotNull(message = "clienteId es obligatorio")
        private Long clienteId;

        @NotNull(message = "salaId es obligatorio")
        private Long salaId;

        @NotNull(message = "fechaInicio es obligatorio")
        private LocalDateTime fechaInicio;

        @NotNull(message = "fechaFinal es obligatorio")
        private LocalDateTime fechaFinal;

        @NotNull(message = "tipoPago es obligatorio")
        private Reserva.TipoPago tipoPago;


    }
}
