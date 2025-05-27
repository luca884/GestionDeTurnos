package com.utn.gestion_de_turnos.service;

import com.fasterxml.jackson.databind.deser.CreatorProperty;
import com.utn.gestion_de_turnos.model.Cliente;
import com.utn.gestion_de_turnos.model.Empleado;
import com.utn.gestion_de_turnos.repository.ClienteRepository;
import com.utn.gestion_de_turnos.repository.EmpleadoRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class EmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ClienteRepository clienteRepository;

    public Empleado save(Empleado empleado) {
        if (empleado == null) {
            throw new IllegalArgumentException("Empleado no puede ser nulo");
        }
        empleado.setContrasena(passwordEncoder.encode(empleado.getContrasena()));
        return empleadoRepository.save(empleado);
    }

    public Optional<Empleado> findById(Long id) {
        return empleadoRepository.findById(id);
    }

    public List<Empleado> findAll() {
        return empleadoRepository.findAll();
    }

    public void deleteById(Long id) {
        if (!empleadoRepository.existsById(id)) {
            throw new IllegalArgumentException("Empleado con ID " + id + " no existe");
        }
        empleadoRepository.deleteById(id);
    }

    public Empleado findByEmail(String email) {
        Empleado empleado = empleadoRepository.findByEmail(email);

        if (empleado == null) {
            System.out.println("Empleado con email " + email + " no encontrado.");
        } else {
            System.out.println("Empleado encontrado: " + empleado.getEmail());
        }

        return empleado;
    }

    public List<Cliente> findAllClientes(){
        return clienteRepository.findAll();
    }



    public Empleado login(String email, String contrasena) {
        Empleado empleado = empleadoRepository.findByEmail(email);

        if (empleado == null) {
            System.out.println("Email no encontrado: " + email);
            return null;
        }

        if (passwordEncoder.matches(contrasena, empleado.getContrasena())) {
            System.out.println("Login exitoso para: " + empleado.getEmail());
            return empleado;
        }

        System.out.println("Contraseña incorrecta para el email: " + email);
        return null;
    }
}
