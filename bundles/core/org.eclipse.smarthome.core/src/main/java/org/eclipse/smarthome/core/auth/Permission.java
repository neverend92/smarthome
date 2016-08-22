package org.eclipse.smarthome.core.auth;

public interface Permission extends DTO {

    /**
     * Gets the request url.
     *
     * @return
     */
    public String getReqUrl();

    /**
     * Gets the roles.
     *
     * @return
     */
    public String[] getRoles();

    /**
     * Sets the request url.
     *
     * @param reqUrl
     */
    public void setReqUrl(String reqUrl);

    /**
     * Sets the roles.
     * 
     * @param roles
     */
    public void setRoles(String[] roles);

}
