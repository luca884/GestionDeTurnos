package com.utn.gestion_de_turnos.API_Calendar.Config;
import com.utn.gestion_de_turnos.API_Calendar.Service.GoogleCalendarService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@RestController
public class OAuth2Controller {

    @Autowired
    private GoogleCalendarService googleCalendarService;

    private final String CLIENT_ID = "TU_CLIENT_ID";
    private final String CLIENT_SECRET = "TU_CLIENT_SECRET";
    private final String REDIRECT_URI = "http://localhost:8080/oauth2/callback";

    @GetMapping("/oauth2/callback")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {
        try {
            String accessToken = intercambiarCodigoPorAccessToken(code);
            var eventos = googleCalendarService.listarProximosEventos();
            return ResponseEntity.ok(eventos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    private String intercambiarCodigoPorAccessToken(String code) {
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
            Map<String, Object> body = response.getBody();
            assert body != null;
            return (String) body.get("access_token");
        } else {
            throw new RuntimeException("No se pudo obtener el access_token");
        }
    }
}
