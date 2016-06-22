package org.eclipse.smarthome.core.internal.auth;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.eclipse.smarthome.core.auth.Authentication;
import org.eclipse.smarthome.core.auth.AuthenticationProvider;

public class AuthenticationProviderImpl implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Credentials credentials) {
        UsernamePasswordCredentials testCreds = new UsernamePasswordCredentials("test", "test");

        if (testCreds.equals(credentials)) {
            return new AuthenticationImpl(((UsernamePasswordCredentials) credentials).getUserName());
        }

        return null;
    }
}
