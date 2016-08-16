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

public class AuthenticationProviderImpl implements AuthenticationProvider {

    /**
     * lifetime of token in seconds (current value: 1h)
     */
    public static final int TOKEN_LIFETIME = 60 * 60;

    /**
     * {@code AuthenticationProvider} instance
     */
    private static AuthenticationProvider authProvider = null;

    /**
     * Gets an instance of the class, if already available, otherwise creates new object.
     *
     * @return
     */
    public static AuthenticationProvider getInstace() {
        if (authProvider == null) {
            authProvider = new AuthenticationProviderImpl();
        }

        return authProvider;
    }

    /**
     * user repository.
     */
    private Repository<User> userRepository;

    /**
     * token repository.
     */
    private Repository<Token> tokenRepository;

    /**
     * Creates new {@code AuthenticationProvider} object
     */
    public AuthenticationProviderImpl() {
        // init repositories.
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

    @Override
    public Authentication authenticateToken(String passedToken) {
        Token token = this.tokenRepository.get(passedToken);

        if (token == null) {
            return null;
        }

        User user = this.userRepository.get(token.getUsername());

        if (user == null) {
            return null;
        }

        return new AuthenticationImpl(user.getUsername(), user.getRoles(), token.getToken(),
                token.getExpiresTimestamp());
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

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.AuthenticationProvider#generateToken()
     */
    @Override
    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Gets the current UNIX timestamp.
     *
     * @return
     */
    private int getCurrentTimestamp() {
        return (int) (System.currentTimeMillis() / 1000L);
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
        if (auth == null) {
            return false;
        }

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
