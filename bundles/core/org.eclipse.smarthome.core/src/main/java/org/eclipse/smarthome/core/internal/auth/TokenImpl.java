package org.eclipse.smarthome.core.internal.auth;

import org.eclipse.smarthome.core.auth.Token;

public class TokenImpl implements Token {

    /**
     * username
     */
    private String username;

    /**
     * token
     */
    private String token;

    /**
     * timestamp, when token expires.
     */
    private int expiresTimstamp;

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
        if (attribute.equals("token")) {
            return this.getToken();
        }
        if (attribute.equals("expiresTimestamp")) {
            return String.valueOf(this.getExpiresTimestamp());
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.DTO#getArray(java.lang.String)
     */
    @Override
    public String[] getArray(String attribute) {
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
        if (attribute.equals("token")) {
            return "API Token";
        }
        if (attribute.equals("expiresTimestamp")) {
            return "Expires Timestamp";
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Token#getExpiresTimestamp()
     */
    @Override
    public int getExpiresTimestamp() {
        return this.expiresTimstamp;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.DTO#getId()
     */
    @Override
    public String getId() {
        return this.getToken();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Token#getToken()
     */
    @Override
    public String getToken() {
        return this.token;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Token#getUsername()
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
        if (attribute.equals("token")) {
            this.setToken(value);
            return;
        }
        if (attribute.equals("expiresTimestamp")) {
            this.setExpiresTimestamp(Integer.valueOf(value));
            return;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.DTO#set(java.lang.String, java.lang.String[])
     */
    @Override
    public void set(String attribute, String[] value) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Token#setExpiresTimestamp(int)
     */
    @Override
    public void setExpiresTimestamp(int expiresTimestamp) {
        this.expiresTimstamp = expiresTimestamp;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Token#setToken(java.lang.String)
     */
    @Override
    public void setToken(String token) {
        this.token = token;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Token#setUsername(java.lang.String)
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
        // Format <token>:<username>:<timestamp>
        StringBuilder sb = new StringBuilder();
        sb.append(Utils.escape(this.getToken()));
        sb.append(":");
        sb.append(Utils.escape(this.getUsername()));
        sb.append(":");
        sb.append(this.getExpiresTimestamp());

        return sb.toString();
    }

}
