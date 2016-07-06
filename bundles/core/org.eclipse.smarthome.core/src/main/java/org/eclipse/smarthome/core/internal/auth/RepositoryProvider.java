package org.eclipse.smarthome.core.internal.auth;

import java.io.File;

public class RepositoryProvider {

    /** The default users configuration directory name */
    final static public String USERS_FOLDER = "users";

    /** The program argument name for setting the main config directory path */
    final static public String CONFIG_DIR_PROG_ARGUMENT = "smarthome.configdir";

    /** The default main configuration directory name */
    final static public String DEFAULT_CONFIG_FOLDER = "conf";

    protected static String getSourcePath() {
        String progArg = System.getProperty(CONFIG_DIR_PROG_ARGUMENT);
        String path;
        if (progArg != null) {
            path = progArg;
        } else {
            path = DEFAULT_CONFIG_FOLDER;
        }

        return path + File.separator + USERS_FOLDER;
    }

}
