package org.eclipse.smarthome.core.internal.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.auth.Permission;
import org.eclipse.smarthome.core.auth.PermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PermissionRepositoryImpl implements PermissionRepository {

    private ArrayList<Permission> permissions;

    private final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    public PermissionRepositoryImpl() {
        this.permissions = new ArrayList<Permission>();
        this.readConfigs();
    }

    private void readConfigs() {
        File dir = new File(RepositoryProvider.getSourcePath());
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                try {
                    this.processConfigFile(file);
                } catch (IOException e) {
                    logger.warn("Could not process users file '{}': {}", file.getName(), e);
                }
            }
        } else {
            logger.debug("User folder '{}' does not exist.", dir.toString());
        }
    }

    private void processConfigFile(File configFile) throws FileNotFoundException, IOException {
        if (configFile.isDirectory() || !configFile.getName().equals("permissions.cfg")) {
            return;
        }
        logger.debug("Processing permissions file '{}'", configFile.getName());

        List<String> lines = IOUtils.readLines(new FileInputStream(configFile));

        for (String line : lines) {
            Permission permission = parseLine(line);
            if (permission != null) {
                this.permissions.add(permission);
            }
        }
    }

    private Permission parseLine(final String line) {
        String trimmedLine = line.trim();
        if (trimmedLine.startsWith("#") || trimmedLine.isEmpty()) {
            return null;
        }

        String[] content = StringUtils.split(trimmedLine, ',');
        if (content.length < 1) {
            return null;
        }

        Permission permission = new PermissionImpl();
        permission.setReqUrl(content[0]);

        if (content.length > 1) {
            String[] roles = new String[content.length - 1];
            for (int i = 0; i < roles.length; i++) {
                roles[i] = content[i + 1].trim();
            }
            permission.setRoles(roles);
        } else {
            permission.setRoles(new String[0]);
        }

        return permission;
    }

    @Override
    public Permission get(String name) {
        // cut of parameter.
        int idx = name.indexOf('?');
        if (idx != -1) {
            name = name.substring(0, idx);
        }
        idx = name.indexOf('#');
        if (idx != -1) {
            name = name.substring(0, idx);
        }

        for (Permission permission : this.permissions) {
            if (permission.getReqUrl().equals(name) || permission.getReqUrl().equals(name.substring(2))) {
                return permission;
            }
        }
        logger.debug("Permission with name '{}' not found", name);
        return null;
    }

}
