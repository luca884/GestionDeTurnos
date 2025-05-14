package com.utn.gestion_de_turnos.controller;

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


// frontend controller para manejar las vistas de autenticación
@Controller
public class AuthController {


    @Autowired
    private ClienteService clienteService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
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

        cliente.setContrasena(new BCryptPasswordEncoder().encode(cliente.getContrasena()));
        cliente.setRol(Usuario.Rol.CLIENTE);
        clienteService.save(cliente);

        return "redirect:/register?success";
    }


}

