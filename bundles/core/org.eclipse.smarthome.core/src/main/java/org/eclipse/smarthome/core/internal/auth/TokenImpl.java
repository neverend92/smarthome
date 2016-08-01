package org.eclipse.smarthome.core.internal.auth;

import org.eclipse.smarthome.core.auth.Token;

public class TokenImpl implements Token {

    private String username;
    private String[] roles;
    private String token;
    private int expiresTimstamp;

    @Override
    public String get(String attribute) {
        if (attribute.equals("username")) {
            return this.getUsername();
        }
        if (attribute.equals("token")) {
            return this.getToken();
        }
        if (attribute.equals("expiresTimestamp")) {
            return String.valueOf(this.getExpiresTimestamp());
        }
        return null;
    }

    @Override
    public String getAttributeName(String attribute) {
        if (attribute.equals("username")) {
            return "Username";
        }
        if (attribute.equals("token")) {
            return "API Token";
        }
        if (attribute.equals("expiresTimestamp")) {
            return "Expires Timestamp";
        }
        return null;
    }

    @Override
    public int getExpiresTimestamp() {
        return this.expiresTimstamp;
    }

    @Override
    public String getId() {
        return this.getToken();
    }

    @Override
    public String[] getRoles() {
        return this.roles;
    }

    @Override
    public String getToken() {
        return this.token;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void set(String attribute, String value) {
        if (attribute.equals("username")) {
            this.setUsername(value);
            return;
        }
        if (attribute.equals("token")) {
            this.setToken(value);
            return;
        }
        if (attribute.equals("expiresTimestamp")) {
            this.setExpiresTimestamp(Integer.valueOf(value));
            return;
        }
    }

    @Override
    public void setExpiresTimestamp(int expiresTimestamp) {
        this.expiresTimstamp = expiresTimestamp;
    }

    @Override
    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getToken());
        sb.append(":");
        sb.append(this.getUsername());
        sb.append(":");
        sb.append(this.getExpiresTimestamp());

        return sb.toString();
    }

}
