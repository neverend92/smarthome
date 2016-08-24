package org.eclipse.smarthome.core.internal.auth;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.auth.Repository;
import org.eclipse.smarthome.core.auth.User;

import com.google.gson.reflect.TypeToken;

public class UserRepositoryImpl extends RepositoryImpl<User> {

    /**
     * {@code Repository<User>} instance
     */
    private static Repository<User> repository = null;

    /**
     * Gets an instance of the class, if already available, otherwise creates new object.
     *
     * @return
     */
    public static Repository<User> getInstance() {
        if (repository == null) {
            repository = new UserRepositoryImpl();
        }

        return repository;
    }

    /**
     * Creates new {@code Repository<User>} object
     */
    public UserRepositoryImpl() {
        // create empty objects list.
        this.setObjects(new ArrayList<User>());
        // set config file
        this.setConfigFile("users.json");
        // set class
        this.setGsonType(new TypeToken<List<UserImpl>>() {
        }.getType());
        // read configs.
        this.readConfigFile();

    }

}
