package org.eclipse.smarthome.core.internal.auth;

import org.eclipse.smarthome.core.auth.User;

public class UserImpl implements User {

    private String username;
    private String password;
    private String[] roles;

    @Override
    public String get(String attribute) {
        if (attribute.equals("username")) {
            return this.getUsername();
        }
        if (attribute.equals("password")) {
            return this.getPassword();
        }
        return null;
    }

    @Override
    public String getAttributeName(String attribute) {
        if (attribute.equals("username")) {
            return "Username";
        }
        if (attribute.equals("password")) {
            return "Password";
        }
        return null;
    }

    @Override
    public String getId() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String[] getRoles() {
        return this.roles;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void set(String attribute, String value) {
        if (attribute.equals("username")) {
            this.setUsername(value);
        }
        if (attribute.equals("password")) {
            this.setPassword(value);
        }
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getUsername());
        sb.append(":");
        sb.append(this.getPassword());
        for (String role : this.getRoles()) {
            sb.append(",");
            sb.append(role);
        }

        return sb.toString();
    }

}
