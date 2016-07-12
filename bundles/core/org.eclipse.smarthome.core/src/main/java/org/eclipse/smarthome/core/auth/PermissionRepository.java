package org.eclipse.smarthome.core.auth;

import java.util.ArrayList;

public interface PermissionRepository {

    public Permission get(String name);

    public ArrayList<Permission> getAll();

    public boolean create(Permission permission);

    public boolean update(String name, Permission permission);

    public boolean delete(String name);

}
