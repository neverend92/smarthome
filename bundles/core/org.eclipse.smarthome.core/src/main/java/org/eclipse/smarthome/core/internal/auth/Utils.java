package org.eclipse.smarthome.core.internal.auth;

public class Utils {

    /**
     * Escapes all {@code chars} to make config file work.
     *
     * @param str
     * @param toReplace
     * @return
     */
    public static String escape(String str, String[] toReplace) {
        for (String s : toReplace) {
            str = str.replaceAll(s, "");
        }
        return str;
    }

    public static String escape(String str) {
        return Utils.escape(str, new String[] { ",", ":" });
    }

}
