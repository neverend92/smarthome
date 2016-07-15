package org.eclipse.smarthome.core.internal.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.auth.DTO;
import org.eclipse.smarthome.core.auth.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryImpl<E extends DTO> implements Repository<E> {

    /** The default users configuration directory name */
    final static protected String USERS_FOLDER = "users";

    /** The program argument name for setting the main config directory path */
    final static protected String CONFIG_DIR_PROG_ARGUMENT = "smarthome.configdir";

    /** The default main configuration directory name */
    final static protected String DEFAULT_CONFIG_FOLDER = "conf";

    protected ArrayList<E> objects;

    protected String configFile;

    protected final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    @Override
    public boolean create(E object) {
        this.objects.add(object);
        this.handleConfigs(true);
        return true;
    }

    @Override
    public boolean delete(String name) {
        E object = this.get(name);
        if (object == null) {
            return false;
        }
        this.objects.remove(object);
        this.handleConfigs(true);
        return true;
    }

    @Override
    public E get(E object) {
        return this.get(object.getId());
    }

    @Override
    public E get(String name) {
        for (E object : this.objects) {
            if (object.getId().equals(name)) {
                return object;
            }
        }
        return null;
    }

    @Override
    public ArrayList<E> getAll() {
        return this.objects;
    }

    protected String getSourcePath() {
        String progArg = System.getProperty(CONFIG_DIR_PROG_ARGUMENT);
        String path;
        if (progArg != null) {
            path = progArg;
        } else {
            path = DEFAULT_CONFIG_FOLDER;
        }

        return path + File.separator + USERS_FOLDER;
    }

    protected void handleConfigs(boolean saveFile) {
        File dir = new File(this.getSourcePath());
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                try {
                    if (!file.isDirectory() && file.getName().equals(this.configFile)) {
                        if (saveFile) {
                            this.saveConfigFile(file);
                        } else {
                            this.processConfigFile(file);
                        }
                    }
                } catch (IOException e) {
                    logger.warn("Could not process users file '{}': {}", file.getName(), e);
                }
            }
        } else {
            logger.debug("User folder '{}' does not exist.", dir.toString());
        }
    }

    protected E handleContent(String content) {
        // needs override.
        return null;
    }

    protected E parseLine(String line) {
        String trimmedLine = line.trim();
        if (trimmedLine.startsWith("#") || trimmedLine.isEmpty()) {
            return null;
        }

        String[] content = StringUtils.split(trimmedLine, ',');
        if (content.length < 1) {
            return null;
        }

        E object = this.handleContent(content[0]);

        if (content.length > 1) {
            String[] roles = new String[content.length - 1];
            for (int i = 0; i < roles.length; i++) {
                roles[i] = content[i + 1].trim();
            }
            object.setRoles(roles);
        } else {
            object.setRoles(new String[0]);
        }

        return object;
    }

    protected void processConfigFile(File configFile) throws FileNotFoundException, IOException {
        logger.debug("Processing users file '{}'", configFile.getName());

        List<String> lines = IOUtils.readLines(new FileInputStream(configFile));

        for (String line : lines) {
            E object = parseLine(line);
            if (object != null) {
                this.objects.add(object);
            }

        }
    }

    protected void saveConfigFile(File configFile) throws FileNotFoundException, IOException {
        IOUtils.writeLines(this.objects, null, new FileOutputStream(configFile));
    }

    @Override
    public boolean update(String name, E object) {
        E tmp = this.get(name);
        if (tmp == null) {
            return false;
        }

        object.setRoles(tmp.getRoles());
        int tmpId = this.objects.indexOf(tmp);
        this.objects.set(tmpId, object);
        this.handleConfigs(true);
        return true;
    }

}
