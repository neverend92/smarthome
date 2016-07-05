package org.eclipse.smarthome.core.internal.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.auth.User;
import org.eclipse.smarthome.core.auth.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserRepositoryImpl implements UserRepository {

    private ArrayList<User> users;

    /** The default users configuration directory name */
    final static public String USERS_FOLDER = "users";

    /** The program argument name for setting the main config directory path */
    final static public String CONFIG_DIR_PROG_ARGUMENT = "smarthome.configdir";

    /** The default main configuration directory name */
    final static public String DEFAULT_CONFIG_FOLDER = "conf";

    private final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    public UserRepositoryImpl() {
        this.users = new ArrayList<User>();
        this.readConfigs();
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

    private void readConfigs() {
        File dir = new File(this.getSourcePath());
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
        if (configFile.isDirectory() || !configFile.getName().endsWith(".users")) {
            logger.debug("Ignoring users file '{}'", configFile.getName());
            return;
        }
        logger.debug("Processing users file '{}'", configFile.getName());

        List<String> lines = IOUtils.readLines(new FileInputStream(configFile));

        for (String line : lines) {
            User user = parseLine(line);
            if (user != null) {
                this.users.add(user);
                logger.debug("### adding user: {}, {}, {}", user.getUsername(), user.getPassword(),
                        user.getRoles().length);
            }
        }
    }

    private User parseLine(final String line) {
        String trimmedLine = line.trim();
        if (trimmedLine.startsWith("#") || trimmedLine.isEmpty()) {
            return null;
        }

        String[] content = StringUtils.split(trimmedLine, ',');
        if (content.length < 1) {
            return null;
        }

        User user = new UserImpl();
        String credentials = content[0];
        if (credentials.indexOf(":") != -1) {
            user.setUsername(StringUtils.substringBefore(credentials, ":").trim());
            user.setPassword(StringUtils.substringAfter(credentials, ":").trim());
        } else {
            return null;
        }

        if (content.length > 1) {
            String[] roles = new String[content.length - 1];
            for (int i = 0; i < roles.length; i++) {
                roles[i] = content[i + 1].trim();
            }
        } else {
            user.setRoles(new String[0]);
        }

        return user;
    }

    @Override
    public User getUser(String name) {
        for (User user : this.users) {
            if (user.getUsername().equals(name)) {
                return user;
            }
        }
        logger.debug("User with name '{}' not found", name);
        return null;
    }

    @Override
    public ArrayList<User> getAll() {
        return this.users;
    }

}
