package org.eclipse.smarthome.core.internal.auth;

import java.util.UUID;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.eclipse.smarthome.core.auth.Authentication;
import org.eclipse.smarthome.core.auth.AuthenticationProvider;
import org.eclipse.smarthome.core.auth.Permission;
import org.eclipse.smarthome.core.auth.Repository;
import org.eclipse.smarthome.core.auth.User;

public class AuthenticationProviderImpl implements AuthenticationProvider {

    private static AuthenticationProvider authProvider = null;

    public static AuthenticationProvider getInstace() {
        if (authProvider == null) {
            authProvider = new AuthenticationProviderImpl("t");
        }

        return authProvider;
    }

    private Repository<User> userRepository;

    public AuthenticationProviderImpl(String t) {
        // init provider.
        this.userRepository = UserRepositoryImpl.getInstance();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.auth.AuthenticationProvider#authenticate(org.apache.commons.httpclient.Credentials)
     */
    @Override
    public Authentication authenticate(Credentials credentials) {
        // obtain all possible credentials.
        String username = ((UsernamePasswordCredentials) credentials).getUserName();
        User user = this.userRepository.get(username);

        if (user == null) {
            return null;
        }

        UsernamePasswordCredentials testCreds = new UsernamePasswordCredentials(user.getUsername(), user.getPassword());

        if (testCreds.equals(credentials)) {
            return new AuthenticationImpl(user.getUsername(), user.getRoles());
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.AuthenticationProvider#generateToken()
     */
    @Override
    public String generateToken(Authentication auth) {
        if (auth == null) {
            return null;
        }
        return UUID.randomUUID().toString();
    }

    /**
     * checks if there is a match in the two string arrays.
     *
     * @param roles1
     * @param roles2
     * @return
     */
    private boolean hasRoleMatch(String[] roles1, String[] roles2) {
        for (String role1 : roles1) {
            for (String role2 : roles2) {
                if (role1.equals(role2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.auth.AuthenticationProvider#isAllowed(org.eclipse.smarthome.core.auth.Authentication,
     * java.lang.String)
     */
    @Override
    public boolean isAllowed(Authentication auth, String reqUrl) {
        Repository<Permission> repo = PermissionRepositoryImpl.getInstance();
        Permission permission = repo.get(reqUrl);

        if (permission == null) {
            return false;
        }

        if (permission.getRoles().length > 0) {
            if (!this.hasRoleMatch(permission.getRoles(), auth.getRoles())) {
                return false;
            }
        }

        return true;
    }
}
