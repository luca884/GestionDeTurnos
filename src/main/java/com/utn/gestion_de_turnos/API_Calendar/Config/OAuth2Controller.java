// OAuth2Controller.java
package com.utn.gestion_de_turnos.API_Calendar.Config;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
public class OAuth2Controller {

    @Autowired
    private GoogleAuthorizationCodeFlow authorizationCodeFlow;

    @GetMapping("/oauth2callback")
    public String oauth2Callback(@RequestParam("code") String code) throws IOException {
        GoogleTokenResponse response = authorizationCodeFlow.newTokenRequest(code)
                .setRedirectUri("http://localhost:8080/oauth2callback")
                .execute();

        Credential credential = authorizationCodeFlow.createAndStoreCredential(response, "user");
        return "Autenticación completada. Token guardado correctamente.";
    }
}
