package org.eclipse.smarthome.ui.nodemgmt;

import org.apache.commons.httpclient.Credentials;

public interface Node {

    public String getIP();

    public void setIP(String ip);

    public String getDesc();

    public void setDesc(String desc);

    public Credentials getCredentials();

    public void setCredentials(Credentials creds);

}
