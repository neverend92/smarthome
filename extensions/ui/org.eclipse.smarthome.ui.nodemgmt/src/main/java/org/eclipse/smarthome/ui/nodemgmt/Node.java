package org.eclipse.smarthome.ui.nodemgmt;

import org.eclipse.smarthome.core.auth.DTO;

public interface Node extends DTO {

    public String getIP();

    public void setIP(String ip);

    public String getDescription();

    public void setDescription(String description);

    public String getCredentials();

    public void setCredentials(String credentials);

}
