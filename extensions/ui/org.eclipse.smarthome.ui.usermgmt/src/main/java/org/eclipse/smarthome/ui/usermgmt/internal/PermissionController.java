package org.eclipse.smarthome.ui.usermgmt.internal;

import org.eclipse.smarthome.core.auth.Permission;
import org.eclipse.smarthome.core.internal.auth.PermissionImpl;
import org.eclipse.smarthome.core.internal.auth.PermissionRepositoryImpl;
import org.eclipse.smarthome.ui.mgmt.internal.MgmtController;
import org.eclipse.smarthome.ui.mgmt.internal.MgmtServlet;

public class PermissionController extends MgmtController<Permission> {

    public PermissionController(String urlAction, String urlId, MgmtServlet servlet) {
        super(urlAction, urlId, servlet);

        this.repository = PermissionRepositoryImpl.getInstance();
        this.entityName = "permission";
        this.fieldName = "reqUrl";

        this.attributes.add("reqUrl");
    }

    @Override
    protected Permission getModel() {
        return new PermissionImpl();
    }

}
