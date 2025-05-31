package com.utn.gestion_de_turnos.config;

import com.utn.gestion_de_turnos.model.Admin;
import com.utn.gestion_de_turnos.model.Usuario;
import com.utn.gestion_de_turnos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        boolean adminExiste = usuarioRepository.findByEmail("ADMIN@gmail.com").isPresent();

        if (!adminExiste) {
            Admin admin = new Admin();
            admin.setEmail("ADMIN@gmail.com");
            admin.setContrasena(passwordEncoder.encode("ADMIN123"));
            admin.setRol(Usuario.Rol.ADMIN); // Asegurate de usar tu enum correctamente
            usuarioRepository.save(admin);
            System.out.println("✔ Usuario ADMIN creado con éxito.");
        } else {
            System.out.println("✔ Usuario ADMIN ya existe.");
        }
    }
}
