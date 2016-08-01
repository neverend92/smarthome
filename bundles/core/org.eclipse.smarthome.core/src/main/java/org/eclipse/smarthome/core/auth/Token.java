package org.eclipse.smarthome.core.auth;

public interface Token extends DTO {

    public String getToken();

    public void setToken(String token);

    public int getExpiresTimestamp();

    public void setExpiresTimestamp(int expiresTimestamp);

    public String getUsername();

    public void setUsername(String username);

}
