package com.utn.gestion_de_turnos.controller.api;
import com.utn.gestion_de_turnos.model.Cliente;
import com.utn.gestion_de_turnos.model.Empleado;
import com.utn.gestion_de_turnos.service.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

// Eso es un controlador REST para manejar las operaciones CRUD (para el Admin) de la entidad Empleado

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoApiController {

    @Autowired
    private EmpleadoService empleadoService;

    @PostMapping
    public Empleado createEmpleado(@RequestBody Empleado empleado) {
        return empleadoService.save(empleado);
    }

    @GetMapping("/{id}")
    public Optional<Empleado> getEmpleadoById(@PathVariable Long id) {
        return empleadoService.findById(id);
    }

    @GetMapping
    public List<Empleado> getAllEmpleados() {
        return empleadoService.findAll();
    }

    public List<Cliente> getAllClientes(){return  empleadoService.findAllClientes();}

    @DeleteMapping("/{id}")
    public void deleteEmpleado(@PathVariable Long id) {
        empleadoService.deleteById(id);
    }

}
