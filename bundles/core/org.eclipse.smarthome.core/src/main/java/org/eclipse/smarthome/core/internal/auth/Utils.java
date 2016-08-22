package org.eclipse.smarthome.core.internal.auth;

public class Utils {

    /**
     * Escapes "," and ":" to make config file work.
     *
     * @param s
     * @return
     */
    public static String escape(String s) {
        return s.replaceAll(",", "").replaceAll(":", "");
    }

}
