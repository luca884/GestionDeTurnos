package com.utn.gestion_de_turnos.service;

import com.utn.gestion_de_turnos.model.Admin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final Map<String, Admin> sessions = new ConcurrentHashMap<>();

    public static void storeSession(String sessionId, Admin admin) {
        sessions.put(sessionId, admin);
    }

    public static Admin getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public static void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    public static boolean isSessionValid(String sessionId) {
        return sessions.containsKey(sessionId);
    }
}
