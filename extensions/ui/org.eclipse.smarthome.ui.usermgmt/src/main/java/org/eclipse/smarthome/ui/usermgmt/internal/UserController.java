package org.eclipse.smarthome.ui.usermgmt.internal;

import org.eclipse.smarthome.core.auth.User;
import org.eclipse.smarthome.core.internal.auth.UserImpl;
import org.eclipse.smarthome.core.internal.auth.UserRepositoryImpl;

public class UserController extends Controller<User> {

    public UserController(String urlAction, String urlId, UserMgmtServlet servlet) {
        super(urlAction, urlId, servlet);

        this.repository = UserRepositoryImpl.getInstance();
        this.entityName = "user";
        this.fieldName = "username";

        this.attributes.add("username");
        this.attributes.add("password");
    }

    @Override
    protected User getModel() {
        return new UserImpl();
    }

}
