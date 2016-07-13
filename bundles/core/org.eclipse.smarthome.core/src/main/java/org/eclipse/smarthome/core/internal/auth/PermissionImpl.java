package org.eclipse.smarthome.core.internal.auth;

import org.eclipse.smarthome.core.auth.Permission;

public class PermissionImpl implements Permission {

    private String reqUrl;
    private String[] roles;

    @Override
    public String get(String attribute) {
        if (attribute.equals("reqUrl")) {
            return this.getReqUrl();
        }
        return null;
    }

    @Override
    public String getAttributeName(String attribute) {
        if (attribute.equals("reqUrl")) {
            return "Requested URL";
        }
        return null;
    }

    @Override
    public String getId() {
        return this.reqUrl;
    }

    @Override
    public String getReqUrl() {
        return this.reqUrl;
    }

    @Override
    public String[] getRoles() {
        return this.roles;
    }

    @Override
    public void set(String attribute, String value) {
        if (attribute.equals("reqUrl")) {
            this.setReqUrl(value);
        }
        return;
    }

    @Override
    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    @Override
    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getReqUrl());
        for (String role : this.getRoles()) {
            sb.append(",");
            sb.append(role);
        }

        return sb.toString();
    }

}
