package com.utn.gestion_de_turnos.service;

import com.utn.gestion_de_turnos.model.Cliente;
import com.utn.gestion_de_turnos.repository.ClienteRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;


@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Cliente save(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no puede ser nulo");
        }
        cliente.setContrasena(passwordEncoder.encode(cliente.getContrasena()));
        return clienteRepository.save(cliente);
    }

    public Optional<Cliente> findByIdRE(Long id) { // para Entity
        return clienteRepository.findById(id);
    }

    public Optional<Cliente> findById(Long id) {
        return clienteRepository.findById(id);
    }

    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    public void deleteById(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new IllegalArgumentException("Cliente con ID " + id + " no existe");
        }
        clienteRepository.deleteById(id);
    }

    public Optional<Cliente> findByEmail(String email) {
        Cliente cliente = clienteRepository.findByEmail(email);
        if (cliente != null) {
            System.out.println("Cliente encontrado: " + cliente.getEmail());
            return Optional.of(cliente);
        } else {
            System.out.println("Cliente con email " + email + " no encontrado.");
            return Optional.empty();
        }
    }

    public Cliente login(String email, String contrasena) {
        Cliente cliente = clienteRepository.findByEmail(email);

        if (cliente == null) {
            System.out.println("Email no encontrado: " + email);
            return null;
        }

        if (passwordEncoder.matches(contrasena, cliente.getContrasena())) {
            System.out.println("Login exitoso para: " + cliente.getEmail());
            return cliente;
        }

        System.out.println("Contraseña incorrecta para el email: " + email);
        return null;
    }
}