package com.example.demo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extremely simple in-memory session manager used only for the demo.
 * NOTE: this is insecure (no expiration, in-memory map) and should NOT be used in production.
 */
public class SessionManager {
    private static final Map<String, String> sessions = new ConcurrentHashMap<>();

    public static String createSession(String username) {
        String id = java.util.UUID.randomUUID().toString();
        sessions.put(id, username);
        return id;
    }

    public static String getUsername(String sessionId) {
        return sessions.get(sessionId);
    }
}
