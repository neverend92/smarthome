package org.eclipse.smarthome.core.auth;

public interface Authentication {

    public String getUsername();

    public void setUsername(String username);

    public String[] getRoles();

    public void setRoles(String[] roles);

    public boolean hasRole(String role);

    public String getToken();

    public void setToken(String token);

    public int getExpiresTimestamp();

    public void setExpiresTimestamp(int expiresTimestamp);
}
