package com.utn.gestion_de_turnos.controller.web;

import com.utn.gestion_de_turnos.exception.AccesoProhibidoException;
import com.utn.gestion_de_turnos.exception.ReservaNotFoundException;
import com.utn.gestion_de_turnos.exception.SalaNotFoundException;
import com.utn.gestion_de_turnos.model.Cliente;
import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.model.Usuario;
import com.utn.gestion_de_turnos.security.CustomUserDetails;
import com.utn.gestion_de_turnos.service.EmpleadoService;
import com.utn.gestion_de_turnos.service.ReservaService;
import com.utn.gestion_de_turnos.service.SalaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.utn.gestion_de_turnos.model.Empleado;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/empleado")
public class EmpleadoViewController {

    @Autowired
    private SalaService salaService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private EmpleadoService empleadoService;

    @GetMapping("/reservas")
    public String empleadoHome(Model model, Authentication authentication) {
        model.addAttribute("email", authentication.getName());
        model.addAttribute("role", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        return "empleado";
    }

    @GetMapping("/salas")
    public String empleadoSalas(Model model, Authentication authentication) {
        model.addAttribute("email", authentication.getName());
        model.addAttribute("role", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));

        List<Sala> salas = salaService.findAll();
        model.addAttribute("salas", salas);

        return "empleado-salas";
    }

    @GetMapping("/calendario")
    public String empleadoCalendario(Model model, Authentication authentication) {
        model.addAttribute("email", authentication.getName());
        model.addAttribute("role", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        return "empleado-api-calendario";
    }

    @GetMapping("/reservas/nueva")
    public String clienteCrearReserva(Model model, Authentication authentication) {
        model.addAttribute("email", authentication.getName());
        model.addAttribute("role", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        return "empleado-nueva-reserva";
    }

    @GetMapping("/reservas/update")
    public String showUpdateFormReserva(@RequestParam("id") Long id, Model model) {
        Optional<Reserva> reserva = reservaService.findById(id);
        if (model.containsAttribute("error")) {
            model.addAttribute("error", model.getAttribute("error"));
        }
        if (reserva.isPresent()) {
            Reserva r = reserva.get();
            model.addAttribute("reserva", r);
            model.addAttribute("salas", salaService.findAll());
            model.addAttribute("horas", List.of("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00"));
            model.addAttribute("fechaSoloFecha", r.getFechaInicio().toLocalDate());
            model.addAttribute("horaInicio", r.getFechaInicio().toLocalTime().toString().substring(0, 5));
            model.addAttribute("horaFinal", r.getFechaFinal().toLocalTime().toString().substring(0, 5));
            return "empleado-reserva-update";
        } else {
            model.addAttribute("error", "Reserva no encontrada");
            return "error";
        }
    }

    @PostMapping("/reservas/update")
    public String updateReservaFromForm(
            @RequestParam Long id,
            @RequestParam Long salaId,
            @RequestParam String fecha,
            @RequestParam String horaInicio,
            @RequestParam String horaFin,
            @RequestParam Reserva.TipoPago tipoPago,
            Authentication auth,
            Model model,
            RedirectAttributes redirectAttributes) {

        Reserva reserva = reservaService.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException("Reserva no encontrada"));

        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        if (!user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("EMPLEADO"))) {
            throw new AccesoProhibidoException("No puedes editar esta reserva");
        }
        Sala sala = salaService.findById(salaId)
                .orElseThrow(() -> new SalaNotFoundException("Sala no encontrada"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime fechaInicio = LocalDateTime.parse(fecha + " " + horaInicio, formatter);
        LocalDateTime fechaFinal = LocalDateTime.parse(fecha + " " + horaFin, formatter);
        if (fechaInicio.isAfter(fechaFinal) || fechaInicio.isEqual(fechaFinal)) {
            model.addAttribute("error", "La fecha y hora de inicio debe ser anterior a la fecha y hora de fin.");
            model.addAttribute("reserva", reserva);
            model.addAttribute("fecha", fecha);
            model.addAttribute("horaInicio", horaInicio);
            model.addAttribute("horaFin", horaFin);
            model.addAttribute("salaId", salaId);
            model.addAttribute("tipoPago", tipoPago);
            redirectAttributes.addFlashAttribute("error", "La fecha y hora de inicio debe ser anterior a la fecha y hora de fin.");
            return "redirect:/empleado/reservas/update?id=" + id;
        }
        reserva.setSala(sala);
        reserva.setFechaInicio(fechaInicio);
        reserva.setFechaFinal(fechaFinal);
        reserva.setTipoPago(tipoPago);
        reservaService.updateReserva(reserva);
        redirectAttributes.addFlashAttribute("success", "Reserva actualizada correctamente");
        return "redirect:/empleado/reservas";
    }


    @GetMapping("/perfil")
    public String mostrarPerfil(Model model, Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Empleado empleado = empleadoService.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        model.addAttribute("empleado", empleado);
        model.addAttribute("email", empleado.getEmail());
        model.addAttribute("role", empleado.getRol().name());
        return "empleado-perfil";
    }
}
