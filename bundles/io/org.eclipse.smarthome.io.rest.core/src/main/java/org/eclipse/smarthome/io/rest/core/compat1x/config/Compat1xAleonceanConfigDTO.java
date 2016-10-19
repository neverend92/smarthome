package org.eclipse.smarthome.io.rest.core.compat1x.config;

import java.util.List;

public class Compat1xAleonceanConfigDTO {

    private String port;

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("port=" + this.getPort());
        return sb.toString();
    }

    public boolean parseObject(List<String> contentLines) {
        if (contentLines == null) {
            return false;
        }

        boolean foundPort = false;

        for (String content : contentLines) {
            if (content.startsWith("#")) {
                continue;
            }

            if (content.indexOf("port") != -1) {
                // set port.
                String[] aContent = content.split("=");
                if (aContent.length != 2) {
                    return false;
                }
                this.setPort(aContent[1].trim());
                foundPort = true;

                continue;
            }
        }

        // check if all fields where passed.
        return foundPort;
    }

}
