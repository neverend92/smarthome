package org.eclipse.smarthome.core.internal.auth;

import org.eclipse.smarthome.core.auth.Permission;

public class PermissionImpl implements Permission {

    /**
     * requested url
     */
    private String reqUrl;

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
        if (attribute.equals("reqUrl")) {
            return this.getReqUrl();
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
        if (attribute.equals("roles")) {
            return this.getRoles();
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
        if (attribute.equals("reqUrl")) {
            return "Requested URL";
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
        return this.reqUrl;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Permission#getReqUrl()
     */
    @Override
    public String getReqUrl() {
        return this.reqUrl;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Permission#getRoles()
     */
    @Override
    public String[] getRoles() {
        return this.roles;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.DTO#set(java.lang.String, java.lang.String)
     */
    @Override
    public void set(String attribute, String value) {
        if (attribute.equals("reqUrl")) {
            this.setReqUrl(value);
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
        if (attribute.equals("roles")) {
            this.setRoles(value);
            return;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Permission#setReqUrl(java.lang.String)
     */
    @Override
    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Permission#setRoles(java.lang.String[])
     */
    @Override
    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // Format <reqUrl>,<role1>,<role2>,...
        StringBuilder sb = new StringBuilder();
        sb.append(Utils.escape(this.getReqUrl()));
        for (String role : this.getRoles()) {
            sb.append(",");
            sb.append(Utils.escape(role));
        }

        return sb.toString();
    }

}
