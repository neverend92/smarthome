package org.eclipse.smarthome.ui.nodemgmt;

import org.eclipse.smarthome.core.auth.DTO;

public interface Node extends DTO {

    /**
     * Gets the saved credentials for the node.
     *
     * @return
     */
    public String getCredentials();

    /**
     * Gets the node description.
     *
     * @return
     */
    public String getDescription();

    /**
     * Gets the handled extensions.
     *
     * @return
     */
    public String[] getExtensions();

    /**
     * Return the IP of the node.
     *
     * @return
     */
    public String getIP();

    /**
     * Gets the node name.
     * 
     * @return
     */
    public String getName();

    /**
     * Sets new credentials for the node.
     *
     * @param credentials
     */
    public void setCredentials(String credentials);

    /**
     * Sets the node description
     *
     * @param description
     */
    public void setDescription(String description);

    /**
     * Sets the handled extensions for the node.
     *
     * @param extensions
     */
    public void setExtensions(String[] extensions);

    /**
     * Sets the IP of the node.
     *
     * @param ip
     */
    public void setIP(String ip);

    /**
     * Sets the node name.
     * 
     * @param name
     */
    public void setName(String name);

}
