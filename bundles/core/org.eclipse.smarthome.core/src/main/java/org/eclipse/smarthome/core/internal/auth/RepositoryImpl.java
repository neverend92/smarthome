package org.eclipse.smarthome.core.internal.auth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.auth.DTO;
import org.eclipse.smarthome.core.auth.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

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

    protected Type gsonType;

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
        this.readConfigFile();
        this.getObjects().add(object);
        this.saveConfigFile();
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Repository#delete(java.lang.String)
     */
    @Override
    public boolean delete(String name) {
        this.readConfigFile();
        E object = this.get(name);
        if (object == null) {
            return false;
        }
        this.getObjects().remove(object);
        this.saveConfigFile();
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Repository#get(org.eclipse.smarthome.core.auth.DTO)
     */
    @Override
    public E get(E object) {
        this.readConfigFile();
        return this.get(object.getId());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Repository#get(java.lang.String)
     */
    @Override
    public E get(String name) {
        this.readConfigFile();
        for (E object : this.getObjects()) {
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
        this.readConfigFile();
        return this.getObjects();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.Repository#getBy(java.lang.String, java.lang.String)
     */
    @Override
    public E getBy(String attribute, String name) {
        this.readConfigFile();
        for (E object : this.getObjects()) {
            if (object.get(attribute).equals(name)) {
                return object;
            }
        }
        return null;
    }

    public Type getGsonType() {
        return gsonType;
    }

    /**
     * Gets the config file
     *
     * @return
     */
    public String getConfigFile() {
        return configFile;
    }

    /**
     * Gets the config file.
     * if it doesn't exists yet, file is created.
     *
     * @return
     * @throws IOException
     */
    protected File getConfigFileObject() throws IOException {
        // enter config dir.
        File dir = new File(this.getSourcePath());
        // check if config dir exists.
        if (!dir.exists()) {
            throw new FileNotFoundException("Source directory doesn't exist.");
        }

        // get config file from dir by using a filename filter.
        File[] files = dir.listFiles(new ConfigFilenameFilter(this.getConfigFile()));
        File file = null;
        // check if config file exists.
        if (files.length > 0) {
            return files[0];
        }

        // if file doesn't exists, create it!
        file = new File(this.getSourcePath() + "/" + this.getConfigFile());
        try {
            file.createNewFile();
            return file;
        } catch (IOException e) {
            throw new IOException("Could not create config file: " + this.getConfigFile());
        }
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
     * Gets the objects list.
     *
     * @return
     */
    public ArrayList<E> getObjects() {
        return objects;
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
     * Reads the config file.
     */
    protected void readConfigFile() {
        // get current time.
        int now = this.getCurrentTimestamp();
        // check if config is still valid (not older than 1min.)
        if (now - RepositoryImpl.CONFIG_LIFETIME < this.getTimestampLastRead()) {
            // if valid, dont read again.
            return;
        }

        // read config again, and set timestamp to now.
        this.setTimestampLastRead(now);
        // init gson
        Gson gson = new Gson();
        ArrayList<E> jsonObjects = new ArrayList<E>();
        try {
            // get objects from json.
            jsonObjects = gson.fromJson(new FileReader(this.getConfigFileObject()), this.getGsonType());
        } catch (JsonSyntaxException | JsonIOException | IOException e) {
            logger.warn("Could not read config file {}.", this.getConfigFile());
            return;
        }

        // set new objects.
        this.setObjects(jsonObjects);
    }

    /**
     * Writes the config file.
     */
    protected void saveConfigFile() {
        Gson gson = new Gson();
        String json = gson.toJson(this.getObjects(), this.getGsonType());
        try {
            IOUtils.write(json, new FileOutputStream(this.getConfigFileObject()));
        } catch (IOException e) {
            logger.warn("Could not write config file {}.", this.getConfigFile());
            return;
        }
    }

    /**
     * Sets the class for E[]
     *
     * @param classE
     */
    public void setGsonType(Type gsonType) {
        this.gsonType = gsonType;
    }

    /**
     * Sets the config file.
     *
     * @param configFile
     */
    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    /**
     * Sets the objects list.
     *
     * @param objects
     */
    public void setObjects(ArrayList<E> objects) {
        this.objects = objects;
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
    public boolean update(String name, E object, boolean changeRoles) {
        this.readConfigFile();
        E tmp = this.get(name);
        if (tmp == null) {
            return false;
        }

        if (!changeRoles) {
            object.set("roles", tmp.getArray("roles"));
        }

        int tmpId = this.objects.indexOf(tmp);
        this.objects.set(tmpId, object);
        this.saveConfigFile();
        return true;
    }

}
