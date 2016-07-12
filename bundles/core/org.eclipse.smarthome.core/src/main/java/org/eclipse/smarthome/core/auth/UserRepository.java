package org.eclipse.smarthome.core.auth;

import java.util.ArrayList;

public interface UserRepository {

    /**
     *
     * @param name
     * @return
     */
    public User get(String name);

    public ArrayList<User> getAll();

    public boolean create(User user);

    public boolean update(String name, User user);

    public boolean delete(String name);

}
