package org.eclipse.smarthome.core.internal.auth;

import java.util.HashMap;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.eclipse.smarthome.core.auth.Authentication;
import org.eclipse.smarthome.core.auth.AuthenticationProvider;
import org.eclipse.smarthome.core.auth.UserRepository;
import org.eclipse.smarthome.core.auth.User;

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
        User user = (User) this.userRepository.get(username);

        if (user == null) {
            return null;
        }

        UsernamePasswordCredentials testCreds = new UsernamePasswordCredentials(user.getUsername(), user.getPassword());

        if (testCreds.equals(credentials)) {
            return new AuthenticationImpl(user.getUsername(), user.getRoles());
        }

        return null;
    }

    @Override
    public boolean isAllowed(Authentication auth, String reqUrl) {
        HashMap<String, String> acls = new HashMap<String, String>();
        // Basic UI
        acls.put("/basicui/app", ""); // no role needed.

        // Classic UI
        acls.put("/classicui/app", ""); // no role needed.

        // Paper UI
        acls.put("/ui/index.html", ""); // no role needed.

        // REST Doc
        acls.put("/doc/index.html", ""); // no role needed.

        return false;
    }
}
