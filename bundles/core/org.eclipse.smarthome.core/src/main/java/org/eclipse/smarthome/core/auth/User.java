package org.eclipse.smarthome.core.auth;

public interface User extends DTO {

    /**
     * Gets the user's password
     *
     * @return
     */
    public String getPassword();

    /**
     * Gets username
     *
     * @return username
     */
    public String getUsername();

    /**
     * Sets the user's password.
     *
     * @param password
     */
    public void setPassword(String password);

    /**
     * Sets username
     *
     * @param username
     */
    public void setUsername(String username);

}
