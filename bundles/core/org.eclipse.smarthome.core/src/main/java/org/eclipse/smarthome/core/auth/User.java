package org.eclipse.smarthome.core.auth;

public interface User extends DTO {

    /**
     * Gets the user's password
     *
     * @return
     */
    public String getPassword();

    /**
     * Gets the roles.
     *
     * @return
     */
    public String[] getRoles();

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
     * Sets the roles.
     *
     * @param roles
     */
    public void setRoles(String[] roles);

    /**
     * Sets username
     *
     * @param username
     */
    public void setUsername(String username);

}
