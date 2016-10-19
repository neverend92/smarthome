package org.eclipse.smarthome.io.rest.core.compat1x.config;

import java.util.List;

public class Compat1xMqttConfigDTO {

    private String broker;

    private String host;

    private String clientId;

    private String username;

    private String password;

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getBroker() + ".url=tcp://" + this.getHost());
        sb.append(System.getProperty("line.separator"));
        sb.append(this.getBroker() + ".clientId=" + this.getClientId());
        sb.append(System.getProperty("line.separator"));
        sb.append(this.getBroker() + ".user=" + this.getUsername());
        sb.append(System.getProperty("line.separator"));
        sb.append(this.getBroker() + ".pwd=" + this.getPassword());
        return sb.toString();
    }

    public boolean parseObject(List<String> contentLines) {
        if (contentLines == null) {
            return false;
        }

        boolean foundBroker = false;
        boolean foundHost = false;
        boolean foundClientId = false;
        boolean foundUsername = false;
        boolean foundPassword = false;

        for (String content : contentLines) {
            if (content.startsWith("#")) {
                continue;
            }

            if (content.indexOf(".url") != -1) {
                // set broker.
                int idx = content.indexOf(".url");
                this.setBroker(content.substring(0, idx));
                foundBroker = true;

                // set host.
                String[] aContent = content.split("=");
                if (aContent.length != 2) {
                    return false;
                }
                String host = aContent[1].trim();
                this.setHost(host.replaceAll("tcp://", ""));
                foundHost = true;

                continue;
            }

            if (content.indexOf(".clientId") != -1) {
                // set clientId.
                String[] aContent = content.split("=");
                if (aContent.length != 2) {
                    return false;
                }
                this.setClientId(aContent[1].trim());
                foundClientId = true;
                continue;
            }

            if (content.indexOf(".user") != -1) {
                // set username.
                String[] aContent = content.split("=");
                if (aContent.length != 2) {
                    return false;
                }
                this.setUsername(aContent[1].trim());
                foundUsername = true;
                continue;
            }

            if (content.indexOf(".pwd") != -1) {
                // set username.
                String[] aContent = content.split("=");
                if (aContent.length != 2) {
                    return false;
                }
                this.setPassword(aContent[1].trim());
                foundPassword = true;
                continue;
            }
        }

        // check if all fields where passed.
        return (foundBroker && foundHost && foundClientId && foundUsername && foundPassword);
    }

}
