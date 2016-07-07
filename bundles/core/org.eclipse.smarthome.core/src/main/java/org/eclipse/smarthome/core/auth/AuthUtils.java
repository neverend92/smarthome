package org.eclipse.smarthome.core.auth;

public class AuthUtils {

    public static boolean hasRoleMatch(String[] roles1, String[] roles2) {
        for (String role1 : roles1) {
            for (String role2 : roles2) {
                if (role1.equals(role2)) {
                    return true;
                }
            }
        }

        return false;
    }

}
