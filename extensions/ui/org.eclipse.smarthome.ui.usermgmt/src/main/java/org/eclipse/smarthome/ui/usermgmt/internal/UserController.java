package org.eclipse.smarthome.ui.usermgmt.internal;

import org.eclipse.smarthome.core.auth.User;
import org.eclipse.smarthome.core.internal.auth.UserImpl;
import org.eclipse.smarthome.core.internal.auth.UserRepositoryImpl;
import org.eclipse.smarthome.ui.mgmt.internal.MgmtController;
import org.eclipse.smarthome.ui.mgmt.internal.MgmtServlet;

public class UserController extends MgmtController<User> {

    public UserController(String urlAction, String urlId, MgmtServlet servlet) {
        super(urlAction, urlId, servlet);

        this.setRepository(UserRepositoryImpl.getInstance());
        this.setEntityName("user");
        this.setFieldName("username");

        this.getAttributes().add("username");
        this.getAttributes().add("password");
    }

    @Override
    public User getModel() {
        return new UserImpl();
    }

}
