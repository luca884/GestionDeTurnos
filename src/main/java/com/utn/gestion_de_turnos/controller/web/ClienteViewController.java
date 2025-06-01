package com.utn.gestion_de_turnos.controller.web;

import com.utn.gestion_de_turnos.model.Cliente;
import com.utn.gestion_de_turnos.model.Reserva;
import com.utn.gestion_de_turnos.security.CustomUserDetails;
import com.utn.gestion_de_turnos.service.ClienteService;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cliente")
public class ClienteViewController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private SalaService salaService;

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/reservas")
    public String clienteHome(Model model, Authentication authentication) {
        model.addAttribute("email", authentication.getName());
        model.addAttribute("role", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        return "cliente";
    }

    @GetMapping("/reservas/nueva")
    public String clienteCrearReserva(Model model, Authentication authentication) {
        model.addAttribute("email", authentication.getName());
        model.addAttribute("role", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        return "cliente-nueva-reserva";
    }

    @GetMapping("/reservas/update")
    public String showUpdateFormReserva(@RequestParam("id") Long id, Model model) {
        Optional<Reserva> reserva = reservaService.findById(id);
        if (reserva.isPresent()) {
            Reserva r = reserva.get();
            model.addAttribute("reserva", r);
            model.addAttribute("salas", salaService.findAll());
            model.addAttribute("horas", List.of("08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00"));
            model.addAttribute("fechaSoloFecha", r.getFechaInicio().toLocalDate());
            model.addAttribute("horaInicio", r.getFechaInicio().toLocalTime().toString().substring(0, 5));
            model.addAttribute("horaFinal", r.getFechaFinal().toLocalTime().toString().substring(0, 5));
            return "cliente-reserva-update";
        } else {
            model.addAttribute("error", "Reserva no encontrada");
            return "error";
        }
    }

    @GetMapping("/calendario")
    public String clienteCalendario(Model model, Authentication authentication) {
        model.addAttribute("email", authentication.getName());
        model.addAttribute("role", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        return "cliente-calendario";
    }

    @GetMapping("/salas")
    public String clienteSalas(Model model, Authentication authentication) {
        model.addAttribute("email", authentication.getName());
        model.addAttribute("role", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        return "cliente-salas";
    }


    @GetMapping("/perfil")
    public String mostrarPerfil(Model model, Authentication authentication) {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Cliente cliente = clienteService.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        model.addAttribute("cliente", cliente);
        model.addAttribute("email", cliente.getEmail());
        model.addAttribute("role", cliente.getRol().name());

        return "cliente-perfil";
    }

    @PostMapping("/perfil/update")
    public String actualizarPerfilCliente(
            @RequestParam String nombre,
            @RequestParam(required = false) String apellido,
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String telefono,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
            Cliente cliente = clienteService.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            cliente.setNombre(nombre);
            cliente.setApellido(apellido);
            cliente.setDni(dni);
            cliente.setTelefono(telefono);
            clienteService.save(cliente);
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil");
        }
        return "redirect:/cliente/perfil";
    }

}
