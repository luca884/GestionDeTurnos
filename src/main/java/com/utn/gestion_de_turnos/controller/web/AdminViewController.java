package com.utn.gestion_de_turnos.controller.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin")
public class AdminViewController {

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
}
