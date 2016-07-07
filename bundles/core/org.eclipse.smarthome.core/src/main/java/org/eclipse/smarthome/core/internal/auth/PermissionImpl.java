package org.eclipse.smarthome.core.internal.auth;

import org.eclipse.smarthome.core.auth.Permission;

public class PermissionImpl implements Permission {

    private String reqUrl;

    private String[] roles;

    @Override
    public String getReqUrl() {
        return this.reqUrl;
    }

    @Override
    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    @Override
    public String[] getRoles() {
        return this.roles;
    }

    @Override
    public void setRoles(String[] roles) {
        this.roles = roles;
    }

}