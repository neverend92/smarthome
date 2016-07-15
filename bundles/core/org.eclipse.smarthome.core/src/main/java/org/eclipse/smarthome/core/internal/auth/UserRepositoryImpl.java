package org.eclipse.smarthome.core.internal.auth;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.auth.Repository;
import org.eclipse.smarthome.core.auth.User;

public class UserRepositoryImpl extends RepositoryImpl<User> {

    private static Repository<User> repository = null;

    public static Repository<User> getInstance() {
        if (repository == null) {
            repository = new UserRepositoryImpl();
        }

        return repository;
    }

    public UserRepositoryImpl() {
        this.objects = new ArrayList<User>();
        this.configFile = "users.cfg";
        this.handleConfigs(false);
    }

    @Override
    protected User handleContent(String content) {
        User user = new UserImpl();

        if (content.indexOf(":") != -1) {
            user.setUsername(StringUtils.substringBefore(content, ":").trim());
            user.setPassword(StringUtils.substringAfter(content, ":").trim());
            return user;
        }

        return null;
    }

}
