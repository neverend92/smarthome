package org.eclipse.smarthome.core.auth;

import org.apache.commons.httpclient.Credentials;

public interface AuthenticationProvider {

    public Authentication authenticate(Credentials credentials);

    public boolean isAllowed(Authentication auth, String reqUrl);

}
