package org.eclipse.smarthome.core.internal.auth;

import java.io.File;
import java.io.FilenameFilter;

public class ConfigFilenameFilter implements FilenameFilter {

    /**
     * filename to match
     */
    private String filename;

    /**
     * get the filename to match.
     *
     * @return
     */
    public String getFilename() {
        return filename;
    }

    /**
     * set the filename to match.
     *
     * @param filename
     */
    public void setFilename(String filename) {
        this.setFilename(filename);
    }

    /**
     * constructor
     *
     * @param filename
     */
    public ConfigFilenameFilter(String filename) {
        this.filename = filename;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    @Override
    public boolean accept(File dir, String name) {
        // check if filenames match.
        return name.equals(this.getFilename());
    }

}
