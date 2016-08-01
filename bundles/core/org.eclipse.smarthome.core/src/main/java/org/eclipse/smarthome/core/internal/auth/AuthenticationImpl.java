package org.eclipse.smarthome.core.internal.auth;

import org.eclipse.smarthome.core.auth.Authentication;

public class AuthenticationImpl implements Authentication {

    private String username;
    private String[] roles;
    private String token;
    private int expiresTimestamp;

    public AuthenticationImpl(String username, String token, int expiresTimestamp) {
        this(username, new String[0], token, expiresTimestamp);
    }

    public AuthenticationImpl(String username, String[] roles, String token, int expiresTimestamp) {
        this.setUsername(username);
        this.setRoles(roles);
        this.setToken(token);
        this.setExpiresTimestamp(expiresTimestamp);
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

    @Override
    public String getToken() {
        return this.token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public int getExpiresTimestamp() {
        return this.expiresTimestamp;
    }

    @Override
    public void setExpiresTimestamp(int expiresTimestamp) {
        this.expiresTimestamp = expiresTimestamp;
    }

}
