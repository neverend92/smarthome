package org.eclipse.smarthome.core.internal.auth;

import org.eclipse.smarthome.core.auth.Authentication;

public class AuthenticationImpl implements Authentication {

    private String username;
    private String[] roles;

    public AuthenticationImpl(String username) {
        this.setUsername(username);
        this.setRoles(new String[0]);
    }

    public AuthenticationImpl(String username, String[] roles) {
        this.setUsername(username);
        this.setRoles(roles);
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String[] getRoles() {
        return this.roles;
    }

    @Override
    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    @Override
    public boolean hasRole(String role) {
        for (String tmpRole : this.roles) {
            if (tmpRole.equals(role)) {
                return true;
            }
        }
        return false;
    }

}
