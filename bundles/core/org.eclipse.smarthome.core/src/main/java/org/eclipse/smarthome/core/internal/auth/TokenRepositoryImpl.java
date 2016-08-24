package org.eclipse.smarthome.core.internal.auth;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.auth.Repository;
import org.eclipse.smarthome.core.auth.Token;

import com.google.gson.reflect.TypeToken;

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
        // create empty objects list.
        this.setObjects(new ArrayList<Token>());
        // set config file.
        this.setConfigFile("tokens.json");
        // set class
        this.setGsonType(new TypeToken<List<TokenImpl>>() {
        }.getType());
        // read configs.
        this.readConfigFile();

    }

}
