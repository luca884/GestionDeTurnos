package com.utn.gestion_de_turnos.controller.api;

import com.utn.gestion_de_turnos.model.Cliente;
import com.utn.gestion_de_turnos.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

// Eso es un controlador REST para manejar las operaciones CRUD de la entidad Cliente.

@RestController
@RequestMapping("/api/cliente")
@Tag(name = "Cliente", description = "Operaciones relacionadas con los clientes")
public class ClienteApiController {
    @Autowired
    private ClienteService clienteService;

    @PostMapping
    @Operation(
            summary = "Crear un nuevo cliente",
            description = "Crea un cliente nuevo en el sistema"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente creado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Cliente.class),
                            examples = @ExampleObject(value = "{ \"nombre\": \"Juan\", \"apellido\": \"Pérez\", \"email\": \"juan@example.com\", \"dni\": \"12345678\", \"telefono\": \"1122334455\" }")
                    ))
    })
    public Cliente createCliente(@RequestBody Cliente cliente) {
        return clienteService.save(cliente);
    }

    @GetMapping("/{id}")

    @Operation(
            summary = "Obtener cliente por ID",
            description = "Devuelve los datos del cliente según su ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = Cliente.class))),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Cliente> getClienteById(@PathVariable Long id) {
        return clienteService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Listar todos los clientes",
            description = "Devuelve una lista de todos los clientes"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de clientes",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Cliente.class)
                    ))
    })
    @GetMapping("/all")
    public List<Cliente> getAllClientes() {
        return clienteService.findAll();
    }


    @Operation(
            summary = "Eliminar cliente por ID",
            description = "Elimina un cliente del sistema por su ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente eliminado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @DeleteMapping("/{id}")
    public void deleteCliente(@PathVariable Long id) {
        clienteService.deleteById(id);
    }
}
