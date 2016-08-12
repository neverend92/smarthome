package org.eclipse.smarthome.core.auth;

public interface Token extends DTO {

    /**
     * Gets timestamp when token expires.
     *
     * @return timestamp
     */
    public int getExpiresTimestamp();

    /**
     * Gets auth token
     *
     * @return token
     */
    public String getToken();

    /**
     * Gets username
     *
     * @return username
     */
    public String getUsername();

    /**
     * Sets timestamp when token expires.
     *
     * @param int expiresTimestamp
     */
    public void setExpiresTimestamp(int expiresTimestamp);

    /**
     * Sets auth token
     *
     * @param token
     */
    public void setToken(String token);

    /**
     * Sets username
     *
     * @param username
     */
    public void setUsername(String username);

}
