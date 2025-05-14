package com.utn.gestion_de_turnos.controller;


import com.utn.gestion_de_turnos.model.Cliente;
import com.utn.gestion_de_turnos.model.Usuario;
import com.utn.gestion_de_turnos.repository.UsuarioRepository;
import com.utn.gestion_de_turnos.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ClientAuthController {

    @Autowired
    private ClienteService clienteService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> registerClient(@RequestBody Map<String, String> requestBody) {
        try {
            String nombre = requestBody.get("nombre");
            String email = requestBody.get("email");
            String password = requestBody.get("contrasena");

            if (usuarioRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body("Usuario con este email ya existe");
            }
            Cliente cliente = new Cliente();
            cliente.setNombre(nombre);
            cliente.setEmail(email);
            cliente.setContrasena(bCryptPasswordEncoder.encode(password));
            cliente.setRol(Usuario.Rol.CLIENTE);
            clienteService.save(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body("Cliente registrado con éxito");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar el cliente: " + e.getMessage());
        }
    }

}
