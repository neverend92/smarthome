package org.eclipse.smarthome.core.auth;

public interface User extends DTO {

    public String getUsername();

    public void setUsername(String username);

    public String getPassword();

    public void setPassword(String password);

}
