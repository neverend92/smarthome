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
    protected static final String USERS_FOLDER = "users";

    /** The program argument name for setting the main config directory path */
    protected static final String CONFIG_DIR_PROG_ARGUMENT = "smarthome.configdir";

    /** The default main configuration directory name */
    protected static final String DEFAULT_CONFIG_FOLDER = "conf";

    /**
     * repeat of reading config file. (current value: 1min)
     */
    protected static final int CONFIG_LIFETIME = 1 * 60;

    protected ArrayList<E> objects;

    protected String configFile;

    protected final Logger logger = LoggerFactory.getLogger(RepositoryImpl.class);

    /**
     * timestamp, when config file was read.
     */
    protected int timestampLastRead = 0;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Repository#create(org.eclipse.smarthome.core.auth.DTO)
     */
    @Override
    public boolean create(E object) {
        this.readConfigs();
        this.objects.add(object);
        this.saveConfigs();
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Repository#delete(java.lang.String)
     */
    @Override
    public boolean delete(String name) {
        this.readConfigs();
        E object = this.get(name);
        if (object == null) {
            return false;
        }
        this.objects.remove(object);
        this.saveConfigs();
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Repository#get(org.eclipse.smarthome.core.auth.DTO)
     */
    @Override
    public E get(E object) {
        this.readConfigs();
        return this.get(object.getId());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Repository#get(java.lang.String)
     */
    @Override
    public E get(String name) {
        this.readConfigs();
        for (E object : this.objects) {
            if (object.getId().equals(name)) {
                return object;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Repository#getAll()
     */
    @Override
    public ArrayList<E> getAll() {
        this.readConfigs();
        return this.objects;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Repository#getBy(java.lang.String, java.lang.String)
     */
    @Override
    public E getBy(String attribute, String name) {
        this.readConfigs();
        for (E object : this.objects) {
            if (object.get(attribute).equals(name)) {
                return object;
            }
        }
        return null;
    }

    /**
     * Gets the current UNIX timestamp.
     *
     * @return
     */
    private int getCurrentTimestamp() {
        return (int) (System.currentTimeMillis() / 1000L);
    }

    /**
     * Get the path to config folder.
     *
     * @return String path to config folder.
     */
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

    /**
     * Gets the timestamp when config file was read.
     *
     * @return
     */
    protected int getTimestampLastRead() {
        return timestampLastRead;
    }

    /**
     * Reads or writes the config files, depending on parameter {@code saveFile}
     *
     * @param boolean saveFile
     */
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

    /**
     * Handles the content of the config file, needs to be overridden in subclasses.
     *
     * @param String content
     * @return E Object builded from {@code content}
     */
    protected E handleContent(String content) {
        return null;
    }

    /**
     * Parses one line of a config file.
     *
     * @param String line
     * @return E Object builded from {@code line}
     */
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

    /**
     * Processed a config file and starts parsing each line.
     *
     * @param String configFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected void processConfigFile(File configFile) throws FileNotFoundException, IOException {
        logger.debug("Processing users file '{}'", configFile.getName());

        List<String> lines = IOUtils.readLines(new FileInputStream(configFile));

        this.objects = new ArrayList<E>();
        for (String line : lines) {
            E object = parseLine(line);
            if (object != null) {
                this.objects.add(object);
            }

        }
    }

    /**
     * reads a config file.
     *
     * @see handleConfigs(false)
     */
    protected void readConfigs() {
        // check if file is up to date or not!
        // prevents multiple load of file.
        int now = this.getCurrentTimestamp();
        if (now - RepositoryImpl.CONFIG_LIFETIME > this.getTimestampLastRead()) {
            this.handleConfigs(false);
            this.setTimestampLastRead(now);
        }
    }

    /**
     * saves a config file, by writing new content to the file.
     *
     * @param String configFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected void saveConfigFile(File configFile) throws FileNotFoundException, IOException {
        IOUtils.writeLines(this.objects, null, new FileOutputStream(configFile));
    }

    /**
     * saves a config file.
     *
     * @see handleConfigs(true)
     */
    protected void saveConfigs() {
        this.handleConfigs(true);
    }

    /**
     * Sets the timestamp when config file was read.
     *
     * @param timestampLastRead
     */
    protected void setTimestampLastRead(int timestampLastRead) {
        this.timestampLastRead = timestampLastRead;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Repository#update(java.lang.String, org.eclipse.smarthome.core.auth.DTO)
     */
    @Override
    public boolean update(String name, E object) {
        this.handleConfigs(false);
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
