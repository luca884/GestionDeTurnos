package com.utn.gestion_de_turnos.controller.web;

import com.utn.gestion_de_turnos.model.Empleado;
import com.utn.gestion_de_turnos.model.Sala;
import com.utn.gestion_de_turnos.service.EmpleadoService;
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
@RequestMapping("/admin")
public class AdminViewController {

    @Autowired
    private SalaService salaService;

    @Autowired
    private EmpleadoService empleadoService;

    @GetMapping("/empleados")
    public String adminHome(Model model, Authentication authentication) {
        model.addAttribute("email", authentication.getName());
        model.addAttribute("role", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        return "admin";
    }


    @GetMapping("/empleados/nuevo")
    public String adminCrearEmpleado(Model model, Authentication authentication) {
        model.addAttribute("email", authentication.getName());
        model.addAttribute("role", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        return "nuevo-empleado";
    }


    @GetMapping("/salas/nueva")
    public String adminCrearSala(Model model, Authentication authentication) {
        model.addAttribute("email", authentication.getName());
        model.addAttribute("role", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
        return "nueva-sala";
    }

    @GetMapping("/salas")
    public String adminSalas(Model model, Authentication authentication) {
        model.addAttribute("email", authentication.getName());
        model.addAttribute("role", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));

        List<Sala> salas = salaService.findAll();
        model.addAttribute("salas", salas);

        return "admin-salas";
    }

    @PostMapping("/salas/nueva")
    public String createSalaFromForm(
            @RequestParam int numero,
            @RequestParam int cantPersonas,
            @RequestParam String descripcion,
            RedirectAttributes redirectAttributes) {

        Sala sala = new Sala();
        sala.setNumero(numero);
        sala.setCantPersonas(cantPersonas);
        sala.setDescripcion(descripcion);
        salaService.save(sala);
        redirectAttributes.addFlashAttribute("success", "Sala creada correctamente");
        return "redirect:/admin/salas";
    }


    @GetMapping("/salas/update")
    public String showUpdateFormSala(@RequestParam("id") Long id, Model model) {
        Optional<Sala> sala = salaService.findById(id);
        if (sala.isPresent()) {
            model.addAttribute("sala", sala.get());
            return "admin-salas-update";
        } else {
            model.addAttribute("error", "Sala no encontrada");
            return "error";
        }
    }

    @PostMapping("/salas/update")
    public String updateSalaDescriptionFromForm(
            @RequestParam Long id,
            @RequestParam String descripcion,
            RedirectAttributes redirectAttributes) {

        Optional<Sala> salaOpt = salaService.findById(id);
        if (salaOpt.isPresent()) {
            Sala sala = salaOpt.get();
            sala.setDescripcion(descripcion);
            salaService.save(sala);
            redirectAttributes.addFlashAttribute("success", "Sala actualizada correctamente");
            return "redirect:/admin/salas";
        } else {
            redirectAttributes.addFlashAttribute("error", "Sala no encontrada");
            return "redirect:/admin/salas/update?id=" + id;
        }
    }

    @GetMapping("/empleados/update")
    public String showUpdateFormEmpleado(@RequestParam("id") Long id, Model model) {
        Optional<Empleado> empleado = empleadoService.findById(id);
        if (empleado.isPresent()) {
            model.addAttribute("empleado", empleado.get());
            return "admin-empleados-update";
        } else {
            model.addAttribute("error", "Empleado no encontrado");
            return "error";
        }
    }

    @PostMapping("/empleados/update")
    public String updateEmpleadoFromForm(
            @RequestParam Long id,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String email,
            @RequestParam String dni,
            @RequestParam String telefono,
            RedirectAttributes redirectAttributes) {

        Optional<Empleado> empleadoOpt = empleadoService.findById(id);
        if (empleadoOpt.isPresent()) {
            Empleado empleado = empleadoOpt.get();
            empleado.setNombre(nombre);
            empleado.setApellido(apellido);
            empleado.setEmail(email);
            empleado.setDni(dni);
            empleado.setTelefono(telefono);
            empleadoService.save(empleado);
            redirectAttributes.addFlashAttribute("success", "Empleado actualizado correctamente");
            return "redirect:/admin/empleados";
        } else {
            redirectAttributes.addFlashAttribute("error", "Empleado no encontrado");
            return "redirect:/admin/empleados/update?id=" + id;
        }
    }


}
