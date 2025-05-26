package com.utn.gestion_de_turnos.API_Calendar.Factory;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;

public class GoogleCalendarClientFactory {

    public static Calendar createCalendarClient(String accessToken) throws Exception {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        HttpRequestInitializer requestInitializer = request -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setAuthorization("Bearer " + accessToken);
            request.setHeaders(headers);
        };

        return new Calendar.Builder(httpTransport, jsonFactory, requestInitializer)
                .setApplicationName("Gestión de Turnos")
                .build();
    }
}
