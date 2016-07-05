package org.eclipse.smarthome.core.auth;

import java.util.ArrayList;

public interface UserRepository {

    public User getUser(String name);

    public ArrayList<User> getAll();

}
