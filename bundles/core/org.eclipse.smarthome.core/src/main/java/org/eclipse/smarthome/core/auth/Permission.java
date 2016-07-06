package org.eclipse.smarthome.core.auth;

public interface Permission {

    public String getReqUrl();

    public void setReqUrl(String reqUrl);

    public String[] getRoles();

    public void setRoles(String[] roles);

}
