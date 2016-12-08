package org.eclipse.smarthome.core.internal.auth;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.auth.Permission;
import org.eclipse.smarthome.core.auth.Repository;

import com.google.gson.reflect.TypeToken;

public class PermissionRepositoryImpl extends RepositoryImpl<Permission> {

    /**
     * {@code Repository<Permission>} instance
     */
    private static Repository<Permission> repository = null;

    /**
     * Gets an instance of the class, if already available, otherwise creates new object.
     *
     * @return
     */
    public static Repository<Permission> getInstance() {
        if (repository == null) {
            repository = new PermissionRepositoryImpl();
        }

        return repository;
    }

    /**
     * Creates new {@code Repository<Permission>} object
     */
    public PermissionRepositoryImpl() {
        // create empty objects list.
        this.setObjects(new ArrayList<Permission>());
        // set config file.
        this.setConfigFile("permissions.json");
        // set class
        this.setGsonType(new TypeToken<List<PermissionImpl>>() {
        }.getType());
        // read configs.
        this.readConfigFile();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.internal.auth.RepositoryImpl#get(java.lang.String)
     */
    @Override
    public Permission get(String name) {
        this.readConfigFile();
        // cut of parameter.
        int idx = name.indexOf('?');
        if (idx != -1) {
            name = name.substring(0, idx);
        }
        idx = name.indexOf('#');
        if (idx != -1) {
            name = name.substring(0, idx);
        }

        for (Permission permission : this.objects) {
            // direct access
            if (permission.getReqUrl().equals(name) || permission.getReqUrl().equals(name.substring(2))) {
                return permission;
            }

            // access for subfolders
            String compare = permission.getReqUrl();
            compare = compare.replaceAll("ui/index.html", "ui/");
            compare = compare.replaceAll("doc/index.html", "doc/");
            compare = compare.replaceAll("usermgmt/app", "usermgmt/");
            compare = compare.replaceAll("nodemgmt/app", "nodemgmt/");
            compare = compare.replaceAll("classic/app", "classicui/");
            compare = compare.replaceAll("basicui/app", "basicui/");
            compare = compare.replaceAll("start/index", "start/");
            if (name.startsWith(compare)) {
                return permission;
            }
        }
        return null;
    }

}
