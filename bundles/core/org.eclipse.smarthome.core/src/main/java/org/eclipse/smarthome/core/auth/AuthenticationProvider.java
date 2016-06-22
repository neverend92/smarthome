package org.eclipse.smarthome.core.auth;

import org.apache.commons.httpclient.Credentials;

public interface AuthenticationProvider {

    Authentication authenticate(Credentials credentials);

}
