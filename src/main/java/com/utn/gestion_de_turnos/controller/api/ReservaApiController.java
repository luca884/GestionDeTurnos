package com.utn.gestion_de_turnos.controller.api;

import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.security.CustomUserDetails;
import com.utn.gestion_de_turnos.service.ReservaService;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

// Eso es el controller de la API REST para la reserva
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/reserva")
public class ReservaApiController {

    @Autowired
    private ReservaService reservaService;

    @PostMapping
    public ResponseEntity<?> saveTurno(@RequestBody ReservaRequest request, Authentication authentication) {
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
        }
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






    // dto
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
