package com.utn.gestion_de_turnos.API_Calendar.Config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.utn.gestion_de_turnos.API_Calendar.Factory.GoogleCalendarClientFactory;
import com.utn.gestion_de_turnos.API_Calendar.Service.GoogleCalendarService;
import com.utn.gestion_de_turnos.model.Cliente;
import com.utn.gestion_de_turnos.model.Usuario;
import com.utn.gestion_de_turnos.repository.ClienteRepository;
import com.utn.gestion_de_turnos.repository.UsuarioRepository;
import com.utn.gestion_de_turnos.security.CustomUserDetails;
import com.utn.gestion_de_turnos.security.JwtTokenProvider;
import com.utn.gestion_de_turnos.service.ClienteService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.Cookie;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class OAuth2Controller {

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;



    private final String CLIENT_ID = "";
    private final String CLIENT_SECRET = "Y";
    private final String REDIRECT_URI = "http://localhost:8080/oauth2/callback";

    @GetMapping("/oauth2/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code, HttpServletResponse servletResponse) {
        try {
            // 1. Intercambiar el código por un access token
            String accessToken = intercambiarCodigoPorTokens(code).toString();

            // 2. Obtener información del usuario desde la API de Google
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (!userInfoResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No se pudo obtener información del usuario.");
            }

            Map<String, Object> userInfo = userInfoResponse.getBody();
            String email = (String) userInfo.get("email");
            String nombre = (String) userInfo.get("name");

            // 3. Buscar o registrar el usuario en tu base de datos
            Usuario usuario = usuarioRepository.findByEmail(email).orElseGet(() -> {
                Cliente nuevo = new Cliente();
                nuevo.setEmail(email);
                nuevo.setNombre(nombre);
                nuevo.setRol(Usuario.Rol.CLIENTE);
                return clienteRepository.save(nuevo);
            });

            // 4. Crear el objeto de autenticación
            CustomUserDetails userDetails = new CustomUserDetails(usuario);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 5. Generar JWT y colocarlo en una cookie
            String jwt = tokenProvider.generateToken(authentication);

            Cookie cookie = new Cookie("JWT_TOKEN", jwt);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // poner en true en producción
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 1 día
            servletResponse.addCookie(cookie);

            // 6. Devolver respuesta JSON con información del usuario
            return ResponseEntity.ok(Map.of(
                    "usuario", usuario,
                    "rol", usuario.getRol().name(),
                    "jwt", jwt
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("❌ Error: " + e.getMessage());
        }
    }


    private Map<String, Object> intercambiarCodigoPorTokens(String code) {
        String tokenEndpoint = "https://oauth2.googleapis.com/token";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", CLIENT_ID);
        formData.add("client_secret", CLIENT_SECRET);
        formData.add("redirect_uri", REDIRECT_URI);
        formData.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenEndpoint, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("❌ No se pudo obtener el access_token");
        }
    }

    private GoogleIdToken.Payload verificarIdToken(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            return idToken != null ? idToken.getPayload() : null;
        } catch (Exception e) {
            throw new RuntimeException("Error al verificar ID Token: " + e.getMessage());
        }
    }
}
