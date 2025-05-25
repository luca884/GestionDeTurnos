package com.utn.gestion_de_turnos.controller.api;

import com.utn.gestion_de_turnos.model.Cliente;
import com.utn.gestion_de_turnos.model.Usuario;
import com.utn.gestion_de_turnos.repository.UsuarioRepository;
import com.utn.gestion_de_turnos.security.CustomUserDetails;
import com.utn.gestion_de_turnos.security.JwtTokenProvider;
import com.utn.gestion_de_turnos.service.ClienteService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
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

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
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

        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return ResponseEntity.ok(new JwtAuthenticationResponse(null, usuario.getRol().name()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerCliente(@Valid @RequestBody Cliente cliente) {
        if (usuarioRepository.findByEmail(cliente.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email ya registrado");
        }
        cliente.setRol(Usuario.Rol.CLIENTE);
        clienteService.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body("Cliente registrado con éxito");
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class JwtAuthenticationResponse {
        private String token;
        private String rol;

        public JwtAuthenticationResponse(String token, String rol) {
            this.token = token;
            this.rol = rol;
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        UserInfoResponse response = new UserInfoResponse(
                userDetails.getUsername(), // email
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse("UNKNOWN")
        );

        return ResponseEntity.ok(response);
    }

    @Data
    @AllArgsConstructor
    public static class UserInfoResponse {
        private String email;
        private String role;
    }

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
