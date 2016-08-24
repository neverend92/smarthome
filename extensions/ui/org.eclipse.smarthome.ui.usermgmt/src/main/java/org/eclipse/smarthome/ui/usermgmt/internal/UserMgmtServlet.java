package org.eclipse.smarthome.ui.usermgmt.internal;

import java.util.HashMap;

import org.eclipse.smarthome.ui.mgmt.internal.MgmtController;
import org.eclipse.smarthome.ui.mgmt.internal.MgmtServlet;
import org.osgi.framework.Bundle;

public class UserMgmtServlet extends MgmtServlet {

    private static final long serialVersionUID = -2921929026201615993L;

    public UserMgmtServlet(String baseUrl, Bundle bundle) {
        this.setBaseUrl(baseUrl);

        this.setDefaultController("users");
        this.setDefaultAction("index");

        String[] validControllers = { "users", "permissions" };
        this.setValidControllers(validControllers);

        String[] validActionGet = { "index", "edit", "add" };
        this.setValidActionGet(validActionGet);

        String[] validActionPost = { "add", "edit", "delete", "addRole", "deleteRole" };
        this.setValidActionPost(validActionPost);

        HashMap<String, String> customRedirects = new HashMap<String, String>();
        customRedirects.put("addRoleError", baseUrl + "?controller=###URL_CONTROLLER###&action=edit&id=###URL_ID###");
        customRedirects.put("deleteRoleError",
                baseUrl + "?controller=###URL_CONTROLLER###&action=edit&id=###URL_ID###");
        customRedirects.put("_defaultError",
                baseUrl + "?controller=###URL_CONTROLLER###&action=###URL_ACTION###&id=###URL_ID###");
        customRedirects.put("addSuccess", baseUrl + "?controller=###URL_CONTROLLER###&action=edit&id=###URL_ID###");
        customRedirects.put("addRoleSuccess", baseUrl + "?controller=###URL_CONTROLLER###&action=edit&id=###URL_ID###");
        customRedirects.put("deleteRoleSuccess",
                baseUrl + "?controller=###URL_CONTROLLER###&action=edit&id=###URL_ID###");
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
        if (this.getUrlController().equals("users")) {
            return new UserController(this.getUrlAction(), this.getUrlId(), this);
        } else if (this.getUrlController().equals("permissions")) {
            return new PermissionController(this.getUrlAction(), this.getUrlId(), this);
        }

        return null;
    }

}
