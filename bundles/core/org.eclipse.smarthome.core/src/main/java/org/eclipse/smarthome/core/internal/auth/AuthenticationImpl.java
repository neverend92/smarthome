package org.eclipse.smarthome.core.internal.auth;

import org.eclipse.smarthome.core.auth.Authentication;

public class AuthenticationImpl implements Authentication {

    private String username;
    private String[] roles;
    private String token;
    private int expiresTimestamp;

    /**
     * Create new {@code Authentication} object
     * with empty role list.
     *
     * @param String username
     * @param String token
     * @param int expiresTimestamp
     */
    public AuthenticationImpl(String username, String token, int expiresTimestamp) {
        this(username, new String[0], token, expiresTimestamp);
    }

    /**
     * Create new {@code Authentication} object.
     *
     * @param String username
     * @param String[]m roles
     * @param String token
     * @param int expiresTimestamp
     */
    public AuthenticationImpl(String username, String[] roles, String token, int expiresTimestamp) {
        this.setUsername(username);
        this.setRoles(roles);
        this.setToken(token);
        this.setExpiresTimestamp(expiresTimestamp);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Authentication#getUsername()
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Authentication#setUsername(java.lang.String)
     */
    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Authentication#getRoles()
     */
    @Override
    public String[] getRoles() {
        return this.roles;
    }

    @Override
    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Authentication#hasRole(java.lang.String)
     */
    @Override
    public boolean hasRole(String role) {
        for (String tmpRole : this.roles) {
            if (tmpRole.equals(role)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Authentication#getToken()
     */
    @Override
    public String getToken() {
        return this.token;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Authentication#setToken(java.lang.String)
     */
    @Override
    public void setToken(String token) {
        this.token = token;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Authentication#getExpiresTimestamp()
     */
    @Override
    public int getExpiresTimestamp() {
        return this.expiresTimestamp;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Authentication#setExpiresTimestamp(int)
     */
    @Override
    public void setExpiresTimestamp(int expiresTimestamp) {
        this.expiresTimestamp = expiresTimestamp;
    }

}
