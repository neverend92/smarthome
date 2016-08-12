package org.eclipse.smarthome.core.auth;

public interface Permission extends DTO {

    /**
     * Gets the request url.
     * 
     * @return
     */
    public String getReqUrl();

    /**
     * Sets the request url.
     * 
     * @param reqUrl
     */
    public void setReqUrl(String reqUrl);

}
