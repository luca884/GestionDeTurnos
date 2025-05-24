package com.utn.gestion_de_turnos.controller.api;

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
    public ResponseEntity<?> saveTurno(@RequestBody ReservaRequest request, Authentication authentication) {
        try {
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
            Long clienteId = user.getId();
            Optional<Cliente> clienteOptional = clienteService.findById(clienteId);
            Optional<Sala> salaOptional = salaService.findById(request.getSalaId());
            if (clienteOptional.isPresent() && salaOptional.isPresent()) {
                Cliente cliente = clienteOptional.get();
                Sala sala = salaOptional.get();

                // Obtener cliente y sala desde sus servicios o el servicio de reserva
                Reserva reserva = new Reserva();
                reserva.setCliente(cliente);
                reserva.setSala(sala);
                reserva.setFechaInicio(request.getFechaInicio());
                reserva.setFechaFinal(request.getFechaFinal());
                reserva.setTipoPago(request.getTipoPago());
                reserva.setEstado(Reserva.Estado.ACTIVO);

                return ResponseEntity.ok(reservaService.crearReserva(reserva));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
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

    @PutMapping("/{id}/cancelar")
    public void cancelarReserva(@PathVariable Long id){
        reservaService.cancelarReserva(id);
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
