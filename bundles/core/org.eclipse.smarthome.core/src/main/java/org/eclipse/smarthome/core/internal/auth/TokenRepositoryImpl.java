package org.eclipse.smarthome.core.internal.auth;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.auth.Repository;
import org.eclipse.smarthome.core.auth.Token;

public class TokenRepositoryImpl extends RepositoryImpl<Token> {

    /**
     * {@code Repository<Token>} instance
     */
    private static Repository<Token> repository = null;

    /**
     * Gets an instance of the class, if already available, otherwise creates new object.
     *
     * @return
     */
    public static Repository<Token> getInstance() {
        if (repository == null) {
            repository = new TokenRepositoryImpl();
        }

        return repository;
    }

    /**
     * Creates new {@code Repository<Token>} object
     */
    public TokenRepositoryImpl() {
        this.objects = new ArrayList<Token>();
        this.configFile = "tokens.cfg";
        this.handleConfigs(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.smarthome.core.internal.auth.RepositoryImpl#handleContent(java.lang.String)
     */
    @Override
    protected Token handleContent(String trimmedLine) {
        Token token = new TokenImpl();

        String[] content = new String[0];
        if (trimmedLine.indexOf(":") != -1) {
            content = StringUtils.split(trimmedLine, ':');
            if (content.length < 1) {
                return null;
            }
        }

        if (content.length != 3) {
            return null;
        }

        token.setToken(content[0]);
        token.setUsername(content[1]);
        token.setExpiresTimestamp(Integer.valueOf(content[2]));

        return token;
    }

}
