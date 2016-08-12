package org.eclipse.smarthome.core.internal.auth;

import org.eclipse.smarthome.core.auth.User;

public class UserImpl implements User {

    /**
     * username
     */
    private String username;

    /**
     * password
     */
    private String password;

    /**
     * roles list
     */
    private String[] roles;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.DTO#get(java.lang.String)
     */
    @Override
    public String get(String attribute) {
        if (attribute.equals("username")) {
            return this.getUsername();
        }
        if (attribute.equals("password")) {
            return this.getPassword();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.DTO#getAttributeName(java.lang.String)
     */
    @Override
    public String getAttributeName(String attribute) {
        if (attribute.equals("username")) {
            return "Username";
        }
        if (attribute.equals("password")) {
            return "Password";
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.DTO#getId()
     */
    @Override
    public String getId() {
        return this.username;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.User#getPassword()
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.DTO#getRoles()
     */
    @Override
    public String[] getRoles() {
        return this.roles;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.User#getUsername()
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.DTO#set(java.lang.String, java.lang.String)
     */
    @Override
    public void set(String attribute, String value) {
        if (attribute.equals("username")) {
            this.setUsername(value);
            return;
        }
        if (attribute.equals("password")) {
            this.setPassword(value);
            return;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.User#setPassword(java.lang.String)
     */
    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.DTO#setRoles(java.lang.String[])
     */
    @Override
    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.User#setUsername(java.lang.String)
     */
    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // Format <username>:<password>,<role1>,<role2>,...
        StringBuilder sb = new StringBuilder();
        sb.append(this.getUsername());
        sb.append(":");
        sb.append(this.getPassword());
        for (String role : this.getRoles()) {
            sb.append(",");
            sb.append(role);
        }

        return sb.toString();
    }

}
