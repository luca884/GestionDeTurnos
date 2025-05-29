// GoogleCalendarClientFactory.java
package com.utn.gestion_de_turnos.API_Calendar.Factory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoogleCalendarClientFactory {

    @Autowired
    private GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow;

    public Calendar getCalendarService() throws Exception {
        Credential credential = googleAuthorizationCodeFlow.loadCredential("user");
        if (credential == null) {
            throw new IllegalStateException("No hay credenciales almacenadas. Primero debes autenticarte.");
        }
        return new Calendar.Builder(
                com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport(),
                com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("GestionDeTurnos").build();
    }
}

