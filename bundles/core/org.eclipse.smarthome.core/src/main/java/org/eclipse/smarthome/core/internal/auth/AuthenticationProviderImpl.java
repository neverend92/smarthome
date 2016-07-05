package org.eclipse.smarthome.core.internal.auth;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.eclipse.smarthome.core.auth.Authentication;
import org.eclipse.smarthome.core.auth.AuthenticationProvider;
import org.eclipse.smarthome.core.auth.User;
import org.eclipse.smarthome.core.auth.UserRepository;

public class AuthenticationProviderImpl implements AuthenticationProvider {

    private UserRepository userRepository;

    public AuthenticationProviderImpl() {
        // init provider.
        this.userRepository = new UserRepositoryImpl();
    }

    @Override
    public Authentication authenticate(Credentials credentials) {
        // obtain all possible credentials.
        String username = ((UsernamePasswordCredentials) credentials).getUserName();
        User user = this.userRepository.getUser(username);

        if (user == null) {
            return null;
        }

        UsernamePasswordCredentials testCreds = new UsernamePasswordCredentials(user.getUsername(), user.getPassword());

        if (testCreds.equals(credentials)) {
            return new AuthenticationImpl(user.getUsername(), user.getRoles());
        }

        return null;
    }
}
