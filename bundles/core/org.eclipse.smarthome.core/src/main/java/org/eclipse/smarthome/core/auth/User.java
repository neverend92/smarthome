package org.eclipse.smarthome.core.auth;

public interface User {

    public String getUsername();

    public void setUsername(String username);

    public String getPassword();

    public void setPassword(String password);

    public String[] getRoles();

    public void setRoles(String[] roles);

}
