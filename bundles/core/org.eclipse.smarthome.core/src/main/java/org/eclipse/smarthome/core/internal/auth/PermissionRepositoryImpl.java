package org.eclipse.smarthome.core.internal.auth;

import java.util.ArrayList;

import org.eclipse.smarthome.core.auth.Permission;
import org.eclipse.smarthome.core.auth.Repository;

public class PermissionRepositoryImpl extends RepositoryImpl<Permission> {

    private static Repository<Permission> repository = null;

    public static Repository<Permission> getInstance() {
        if (repository == null) {
            repository = new PermissionRepositoryImpl();
        }

        return repository;
    }

    public PermissionRepositoryImpl() {
        this.objects = new ArrayList<Permission>();
        this.configFile = "permissions.cfg";
        this.handleConfigs(false);
    }

    @Override
    public Permission get(String name) {
        this.handleConfigs(false);
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
            if (name.startsWith(permission.getReqUrl().replaceAll("index.html", ""))) {
                return permission;
            }
        }
        return null;
    }

    @Override
    protected Permission handleContent(String content) {
        Permission permission = new PermissionImpl();

        permission.setReqUrl(content);

        return permission;
    }

}
