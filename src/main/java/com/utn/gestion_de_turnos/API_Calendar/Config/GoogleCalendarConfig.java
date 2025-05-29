// GoogleCalendarConfig.java
package com.utn.gestion_de_turnos.API_Calendar.Config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleCalendarConfig {

    @Bean
    public JsonFactory jsonFactory() {
        return JacksonFactory.getDefaultInstance();
    }

    @Bean
    public java.security.GeneralSecurityException googleSecurityException() {
        return new java.security.GeneralSecurityException();
    }

    @Bean
    public com.google.api.client.http.HttpTransport httpTransport() throws Exception {
        return GoogleNetHttpTransport.newTrustedTransport();
    }
}
