package org.eclipse.smarthome.ui.nodemgmt.internal;

import org.eclipse.smarthome.ui.nodemgmt.Node;

public class NodeImpl implements Node {

    /**
     * ip address
     */
    private String ip;

    /**
     * description of node.
     */
    private String description;

    /**
     * name of the node.
     * used for mqtt.
     */
    private String name;

    /**
     * credentials to authenticate at node.
     * if they are null, no auth is needed.
     */
    private String credentials;

    /**
     * extensions that are handled by this node.
     */
    private String[] extensions;

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.DTO#get(java.lang.String)
     */
    @Override
    public String get(String attribute) {
        if (attribute.equals("ip")) {
            return this.getIP();
        }
        if (attribute.equals("description")) {
            return this.getDescription();
        }
        if (attribute.equals("name")) {
            return this.getName();
        }
        if (attribute.equals("credentials")) {
            return this.getCredentials();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.DTO#getArray(java.lang.String)
     */
    @Override
    public String[] getArray(String attribute) {
        // do nothing, Node has no array attributes so far.
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.DTO#getAttributeName(java.lang.String)
     */
    @Override
    public String getAttributeName(String attribute) {
        if (attribute.equals("ip")) {
            return "IP Address";
        }
        if (attribute.equals("description")) {
            return "Description";
        }
        if (attribute.equals("credentials")) {
            return "Credentials";
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.ui.nodemgmt.Node#getCredentials()
     */
    @Override
    public String getCredentials() {
        return this.credentials;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.ui.nodemgmt.Node#getDescription()
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.ui.nodemgmt.Node#getExtensions()
     */
    @Override
    public String[] getExtensions() {
        if (this.extensions != null) {
            return this.extensions;
        }
        return new String[0];
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.DTO#getId()
     */
    @Override
    public String getId() {
        return this.getIP();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.ui.nodemgmt.Node#getIP()
     */
    @Override
    public String getIP() {
        return this.ip;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.DTO#set(java.lang.String, java.lang.String)
     */
    @Override
    public void set(String attribute, String value) {
        if (attribute.equals("ip")) {
            this.setIP(value);
            return;
        }
        if (attribute.equals("description")) {
            this.setDescription(value);
            return;
        }
        if (attribute.equals("name")) {
            this.setName(value);
            return;
        }
        if (attribute.equals("credentials")) {
            this.setCredentials(value);
            return;
        }
        return;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.auth.DTO#set(java.lang.String, java.lang.String[])
     */
    @Override
    public void set(String attribute, String[] value) {
        // do nothing, Node has no array attributes so far.
        return;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.ui.nodemgmt.Node#setCredentials(org.apache.commons.httpclient.Credentials)
     */
    @Override
    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.ui.nodemgmt.Node#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.ui.nodemgmt.Node#setExtensions(java.lang.String[])
     */
    @Override
    public void setExtensions(String[] extensions) {
        this.extensions = extensions;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.ui.nodemgmt.Node#setIP(java.lang.String)
     */
    @Override
    public void setIP(String ip) {
        this.ip = ip;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

}
