package com.utn.gestion_de_turnos.service;

import com.utn.gestion_de_turnos.model.Empleado;
import com.utn.gestion_de_turnos.repository.EmpleadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoService {
    @Autowired
    private EmpleadoRepository empleadoRepository;

    public Empleado save(Empleado empleado) {
        return empleadoRepository.save(empleado);
    }

    public Optional<Empleado> findById(Long id) {
        return empleadoRepository.findById(id);
    }

    public List<Empleado> findAll() {
        return empleadoRepository.findAll();
    }

    public void deleteById(Long id) {
        empleadoRepository.deleteById(id);
    }
}
