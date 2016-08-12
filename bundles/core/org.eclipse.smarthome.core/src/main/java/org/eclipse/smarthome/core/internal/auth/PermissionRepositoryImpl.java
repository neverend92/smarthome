package org.eclipse.smarthome.core.internal.auth;

import java.util.ArrayList;

import org.eclipse.smarthome.core.auth.Permission;
import org.eclipse.smarthome.core.auth.Repository;

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
        this.objects = new ArrayList<Permission>();
        // set config file.
        this.configFile = "permissions.cfg";
        // read configs.
        this.readConfigs();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.internal.auth.RepositoryImpl#get(java.lang.String)
     */
    @Override
    public Permission get(String name) {
        this.readConfigs();
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

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.internal.auth.RepositoryImpl#handleContent(java.lang.String)
     */
    @Override
    protected Permission handleContent(String content) {
        Permission permission = new PermissionImpl();

        permission.setReqUrl(content);

        return permission;
    }

}
