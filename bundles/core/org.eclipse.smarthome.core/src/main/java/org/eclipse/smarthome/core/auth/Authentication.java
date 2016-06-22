package org.eclipse.smarthome.core.auth;

public interface Authentication {

    public String getUsername();

    public void setUsername(String username);

    public String[] getRoles();

    public void setRoles(String[] roles);
}
