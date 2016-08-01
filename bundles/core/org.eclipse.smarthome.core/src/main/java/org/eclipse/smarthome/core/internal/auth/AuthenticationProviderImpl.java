package org.eclipse.smarthome.core.internal.auth;

import java.util.UUID;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.eclipse.smarthome.core.auth.Authentication;
import org.eclipse.smarthome.core.auth.AuthenticationProvider;
import org.eclipse.smarthome.core.auth.Permission;
import org.eclipse.smarthome.core.auth.Repository;
import org.eclipse.smarthome.core.auth.Token;
import org.eclipse.smarthome.core.auth.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationProviderImpl implements AuthenticationProvider {

    /** lifetime of token in seconds (current value: 1h) */
    public static final int TOKEN_LIFETIME = 60 * 60;

    private static AuthenticationProvider authProvider = null;

    public static AuthenticationProvider getInstace() {
        if (authProvider == null) {
            authProvider = new AuthenticationProviderImpl();
        }

        return authProvider;
    }

    private Repository<User> userRepository;

    private Repository<Token> tokenRepository;

    private final Logger logger = LoggerFactory.getLogger(AuthenticationProviderImpl.class);

    public AuthenticationProviderImpl() {
        // init provider.
        this.userRepository = UserRepositoryImpl.getInstance();
        this.tokenRepository = TokenRepositoryImpl.getInstance();
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
            Authentication auth = new AuthenticationImpl(user.getUsername(), user.getRoles(), this.generateToken(),
                    this.calcExpiresTimestamp());

            // save token to file.
            Token token = new TokenImpl();
            token.setToken(auth.getToken());
            token.setUsername(auth.getUsername());
            token.setExpiresTimestamp(auth.getExpiresTimestamp());

            Token tmpToken = this.tokenRepository.getBy("username", token.getUsername());
            if (tmpToken != null) {
                token.setToken(tmpToken.getToken());
                if (!this.tokenRepository.update(token.getToken(), token)) {
                    return null;
                }
                auth.setToken(token.getToken());
            } else {
                if (!this.tokenRepository.create(token)) {
                    return null;
                }
            }

            return auth;
        }

        return null;
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

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.AuthenticationProvider#generateToken()
     */
    @Override
    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.AuthenticationProvider#calcExpiresTimestamp()
     */
    @Override
    public int calcExpiresTimestamp() {
        return (this.getCurrentTimestamp() + AuthenticationProviderImpl.TOKEN_LIFETIME);
    }

    /**
     * Gets the current UNIX timestamp.
     *
     * @return
     */
    private int getCurrentTimestamp() {
        return (int) (System.currentTimeMillis() / 1000L);
    }
}
