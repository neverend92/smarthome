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

    private final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    public UserRepositoryImpl() {
        this.users = new ArrayList<User>();
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
        if (configFile.isDirectory() || !configFile.getName().equals("users.cfg")) {
            return;
        }
        logger.debug("Processing users file '{}'", configFile.getName());

        List<String> lines = IOUtils.readLines(new FileInputStream(configFile));

        for (String line : lines) {
            User user = parseLine(line);
            if (user != null) {
                this.users.add(user);
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
            user.setRoles(roles);
        } else {
            user.setRoles(new String[0]);
        }

        return user;
    }

    @Override
    public User get(String name) {
        for (User user : this.users) {
            if (user.getUsername().equals(name)) {
                return user;
            }
        }
        logger.debug("User with name '{}' not found", name);
        return null;
    }

}
