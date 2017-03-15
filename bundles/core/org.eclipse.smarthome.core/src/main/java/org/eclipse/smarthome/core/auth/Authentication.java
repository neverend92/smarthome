package org.eclipse.smarthome.core.auth;

public interface Authentication {

    /**
     * Gets timestamp when token expires.
     *
     * @return timestamp
     */
    public int getExpiresTimestamp();

    /**
     * Gets user roles for authentication
     *
     * @return roles
     */
    public String[] getRoles();

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
     * Checks whether role in roles list.
     *
     * @param role
     * @return
     */
    public boolean hasRole(String role);

    /**
     * Sets timestamp when token expires.
     *
     * @param int expiresTimestamp
     */
    public void setExpiresTimestamp(int expiresTimestamp);

    /**
     * Sets roles list
     *
     * @param roles
     */
    public void setRoles(String[] roles);

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
