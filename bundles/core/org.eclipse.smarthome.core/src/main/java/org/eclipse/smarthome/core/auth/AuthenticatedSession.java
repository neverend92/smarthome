package org.eclipse.smarthome.core.auth;

import java.util.HashMap;

public class AuthenticatedSession {

    private static HashMap<String, Authentication> entries;
    private static HashMap<String, Integer> validity;

    private static AuthenticatedSession session;

    public static AuthenticatedSession getInstance() {
        if (session == null) {
            session = new AuthenticatedSession();
            entries = new HashMap<String, Authentication>();
            validity = new HashMap<String, Integer>();
        }
        return session;
    }

    public Authentication get(String sessionId) {

        Object timestamp = validity.get(sessionId);
        if (timestamp == null) {
            return null;
        }
        if ((int) timestamp < this.getCurrentTimestamp()) {
            return null;
        }
        return entries.get(sessionId);
    }

    public void put(String sessionId, Authentication auth) {
        entries.put(sessionId, auth);
        validity.put(sessionId, this.getCurrentTimestamp() + 60 * 60);
    }

    public void remove(String sessionId) {
        entries.remove(sessionId);
        validity.remove(sessionId);
    }

    /**
     * Gets the current UNIX timestamp.
     *
     * @return
     */
    private int getCurrentTimestamp() {
        return (int) (System.currentTimeMillis() / 1000L);
    }

}
