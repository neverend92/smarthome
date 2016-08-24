package org.eclipse.smarthome.ui.nodemgmt.internal;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.smarthome.ui.mgmt.internal.MgmtController;
import org.eclipse.smarthome.ui.mgmt.internal.MgmtServlet;
import org.eclipse.smarthome.ui.nodemgmt.Node;

public class NodeController extends MgmtController<Node> {

    public NodeController(String urlAction, String urlId, MgmtServlet servlet) {
        super(urlAction, urlId, servlet);

        this.setRepository(NodeRepositoryImpl.getInstance());
        this.setEntityName("node");
        this.setFieldName("ip");

        this.getAttributes().add("ip");
        this.getAttributes().add("description");
        this.getAttributes().add("credentials");
    }

    @Override
    public Node getModel() {
        return new NodeImpl();
    }

    @Override
    public String getEdit() {
        String content = super.getEdit();

        Node node = this.getRepository().get(this.getUrlId());

        if (node == null) {
            return null;
        }

        String template = this.getServlet().getTemplateFile(this.getPlural(this.getEntityName()) + "/edit-extra");

        boolean isReachable = false;
        try {
            URL url = new URL(node.getIP() + "/rest");
            isReachable = true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String reachable = "<span class=\"label label-danger\">offline</span>";
        if (isReachable) {
            reachable = "<span class=\"label label-success\">online</span>";
        }
        template = template.replace("###REACHABLE###", reachable);

        boolean hasValidCreds = false;
        // TODO
        String validCreds = "<span class=\"label label-danger\">credentials wrong</span>";
        if (hasValidCreds) {
            validCreds = "<span class=\"label label-success\">credentials correct</span>";
        }
        template = template.replace("###VALIDCREDS###", validCreds);

        boolean hasValidConfig = false;
        // TODO
        String validConfig = "<span class=\"label label-danger\">config invalid/incomplete</span>";
        if (hasValidConfig) {
            validConfig = "<span class=\"label label-success\">config valid</span>";
        }
        template = template.replace("###VALIDCONFIG###", validConfig);

        content += template;
        return content;

    }

}
