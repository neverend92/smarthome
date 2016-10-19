package org.eclipse.smarthome.io.rest.core.compat1x.config;

import java.util.List;

public class Compat1xMqttEventbusConfigDTO {

    private String broker;

    private String statePublishTopic;

    private String commandSubscribeTopic;

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getStatePublishTopic() {
        return statePublishTopic;
    }

    public void setStatePublishTopic(String statePublishTopic) {
        this.statePublishTopic = statePublishTopic;
    }

    public String getCommandSubscribeTopic() {
        return commandSubscribeTopic;
    }

    public void setCommandSubscribeTopic(String commandSubscribeTopic) {
        this.commandSubscribeTopic = commandSubscribeTopic;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("broker=" + this.getBroker());
        sb.append(System.getProperty("line.separator"));
        sb.append("statePublishTopic=" + this.getStatePublishTopic());
        sb.append(System.getProperty("line.separator"));
        sb.append("commandSubscribeTopic=" + this.getCommandSubscribeTopic());
        return sb.toString();
    }

    public boolean parseObject(List<String> contentLines) {
        if (contentLines == null) {
            return false;
        }

        boolean foundBroker = false;
        boolean foundStatePublishTopic = false;
        boolean foundCommandSubscribeTopic = false;

        for (String content : contentLines) {
            if (content.startsWith("#")) {
                continue;
            }

            if (content.indexOf("broker") != -1) {
                // set broker.
                String[] aContent = content.split("=");
                if (aContent.length != 2) {
                    return false;
                }
                this.setBroker(aContent[1].trim());
                foundBroker = true;
                continue;
            }

            if (content.indexOf("statePublishTopic") != -1) {
                // set statePublishTopic.
                String[] aContent = content.split("=");
                if (aContent.length != 2) {
                    return false;
                }
                this.setStatePublishTopic(aContent[1].trim());
                foundStatePublishTopic = true;
                continue;
            }

            if (content.indexOf("commandSubscribeTopic") != -1) {
                // set commandSubscribeTopic.
                String[] aContent = content.split("=");
                if (aContent.length != 2) {
                    return false;
                }
                this.setCommandSubscribeTopic(aContent[1].trim());
                foundCommandSubscribeTopic = true;
                continue;
            }
        }

        // check if all fields where passed.
        return (foundBroker && foundStatePublishTopic && foundCommandSubscribeTopic);
    }

}
