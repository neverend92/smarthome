package org.eclipse.smarthome.ui.nodemgmt.internal;

import java.util.HashMap;

import org.eclipse.smarthome.ui.mgmt.internal.MgmtController;
import org.eclipse.smarthome.ui.mgmt.internal.MgmtServlet;
import org.osgi.framework.Bundle;

public class NodeMgmtServlet extends MgmtServlet {

    private static final long serialVersionUID = 3201527895131390811L;

    public NodeMgmtServlet(String baseUrl, Bundle bundle) {
        this.setBaseUrl(baseUrl);

        this.setDefaultController("nodes");
        this.setDefaultAction("index");

        String[] validControllers = { "nodes" };
        this.setValidControllers(validControllers);

        String[] validActionGet = { "index", "edit", "add" };
        this.setValidActionGet(validActionGet);

        String[] validActionPost = { "add", "edit", "delete" };
        this.setValidActionPost(validActionPost);

        HashMap<String, String> customRedirects = new HashMap<String, String>();
        customRedirects.put("_defaultError",
                baseUrl + "?controller=###URL_CONTROLLER###&action=###URL_ACTION###&id=###URL_ID###");
        customRedirects.put("addSuccess", baseUrl + "?controller=###URL_CONTROLLER###&action=edit&id=###URL_ID###");
        customRedirects.put("editSuccess", baseUrl + "?controller=###URL_CONTROLLER###&action=edit&id=###URL_ID###");
        customRedirects.put("_defaultSuccess", baseUrl + "?controller=###URL_CONTROLLER###");
        this.setCustomRedirects(customRedirects);

        this.setBundle(bundle);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.ui.mgmt.internal.MgmtServlet#getController()
     */
    @Override
    public MgmtController<?> getController() {
        if (this.getUrlController().equals("nodes")) {
            return new NodeController(this.getUrlAction(), this.getUrlId(), this);
        }

        return null;
    }

}
