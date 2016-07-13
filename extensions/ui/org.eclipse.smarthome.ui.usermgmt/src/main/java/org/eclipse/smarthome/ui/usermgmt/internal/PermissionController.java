package org.eclipse.smarthome.ui.usermgmt.internal;

import org.eclipse.smarthome.core.auth.Permission;
import org.eclipse.smarthome.core.internal.auth.PermissionImpl;
import org.eclipse.smarthome.core.internal.auth.PermissionRepositoryImpl;

public class PermissionController extends Controller<Permission> {

    public PermissionController(String urlAction, String urlId, UserMgmtServlet servlet) {
        super(urlAction, urlId, servlet);

        this.repository = new PermissionRepositoryImpl();
        this.entityName = "permission";
        this.fieldName = "reqUrl";

        this.attributes.add("reqUrl");
    }

    @Override
    protected Permission getModel() {
        return new PermissionImpl();
    }

}
