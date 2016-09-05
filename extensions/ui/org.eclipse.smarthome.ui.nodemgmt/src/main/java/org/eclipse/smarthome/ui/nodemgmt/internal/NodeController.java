package org.eclipse.smarthome.ui.nodemgmt.internal;

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

        content += template;
        return content;

    }

}
