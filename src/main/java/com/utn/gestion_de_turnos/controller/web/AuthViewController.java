package com.utn.gestion_de_turnos.controller.web;

import com.utn.gestion_de_turnos.model.Cliente;
import com.utn.gestion_de_turnos.model.Usuario;
import com.utn.gestion_de_turnos.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Eso es un controlador de Spring MVC para manejar las vistas de autenticación y registro de usuarios (Frontend)

@Controller
public class AuthViewController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "success", required = false) String success,
                            @RequestParam(value = "email", required = false) String email,
                            Model model) {
        if ("registro".equals(success)) {
            model.addAttribute("loginSuccess", true);
        }
        if (email != null) {
            model.addAttribute("prefillEmail", email);
        }
        return "login";
    }


    @GetMapping("/register")
    public String register(@RequestParam(value = "success", required = false) String success, Model model) {
        model.addAttribute("cliente", new Cliente());
        if (success != null) {
            model.addAttribute("registrationSuccess", true);
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerClient(@Valid @ModelAttribute("cliente") Cliente cliente,
                                 BindingResult bindingResult,
                                 Model model) {
        if (clienteService.findByEmail(cliente.getEmail()).isPresent()) {
            model.addAttribute("emailError", "Un usuario con este email ya está registrado. ¿Quieres iniciar sesión?");
            return "register";
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        cliente.setRol(Usuario.Rol.CLIENTE);
        clienteService.save(cliente);
        return "redirect:/login?success=registro&email=" + cliente.getEmail();
    }

}
