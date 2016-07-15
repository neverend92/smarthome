package org.eclipse.smarthome.core.auth;

import org.apache.commons.httpclient.Credentials;

public interface AuthenticationProvider {

    /**
     * Checks if the passed credentials are valid.
     *
     * @param credentials
     * @return
     */
    public Authentication authenticate(Credentials credentials);

    /**
     * generates a strong pseudo random token for api access.
     *
     * @param auth
     * @return
     */
    public String generateToken(Authentication auth);

    /**
     * Checks if the requested URL is allowed for the passed authentication
     *
     * @param auth
     * @param reqUrl
     * @return
     */
    public boolean isAllowed(Authentication auth, String reqUrl);

}
