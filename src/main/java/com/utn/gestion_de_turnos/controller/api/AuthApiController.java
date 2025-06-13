package com.utn.gestion_de_turnos.controller.api;

import com.utn.gestion_de_turnos.dto.JwtAuthenticationResponseDTO;
import com.utn.gestion_de_turnos.dto.LoginRequestDTO;
import com.utn.gestion_de_turnos.dto.UserInfoResponseDTO;
import com.utn.gestion_de_turnos.model.Cliente;
import com.utn.gestion_de_turnos.model.Usuario;
import com.utn.gestion_de_turnos.repository.UsuarioRepository;
import com.utn.gestion_de_turnos.security.CustomUserDetails;
import com.utn.gestion_de_turnos.security.JwtTokenProvider;
import com.utn.gestion_de_turnos.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

// Eso es un controlador REST para manejar la autenticación y el registro de usuarios

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para login, logout y registro")
public class AuthApiController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve un token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa",
                    content = @Content(schema = @Schema(implementation = JwtAuthenticationResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        // crea HttpOnly cookie с JWT
        Cookie cookie = new Cookie("JWT_TOKEN", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);

        Usuario usuario = usuarioRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return ResponseEntity.ok(new JwtAuthenticationResponseDTO(null, usuario.getRol().name()));
    }


    @Operation(summary = "Registrar nuevo cliente", description = "Crea un nuevo cliente en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente registrado con éxito"),
            @ApiResponse(responseCode = "400", description = "Email ya registrado")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerCliente(@Valid @RequestBody Cliente cliente) {
        if (usuarioRepository.findByEmail(cliente.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email ya registrado");
        }
        cliente.setRol(Usuario.Rol.CLIENTE);
        clienteService.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body("Cliente registrado con éxito");
    }

    @Operation(summary = "Obtener usuario actual", description = "Devuelve información del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datos del usuario",
                    content = @Content(schema = @Schema(implementation = UserInfoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponseDTO> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        UserInfoResponseDTO response = new UserInfoResponseDTO(
                userDetails.getUsername(), // email
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse("UNKNOWN")
        );

        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Cerrar sesión", description = "Elimina la cookie JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout exitoso")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Crea cookie con el mismo nombre pero con un valor vacío y un tiempo de vida de 0 para eliminarla
        Cookie cookie = new Cookie("JWT_TOKEN", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok("Logout exitoso");
    }


}
