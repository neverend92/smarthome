package org.eclipse.smarthome.ui.usermgmt.internal;

import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.smarthome.core.auth.User;
import org.eclipse.smarthome.core.auth.UserRepository;
import org.eclipse.smarthome.core.internal.auth.UserImpl;
import org.eclipse.smarthome.core.internal.auth.UserRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserController {

    private String urlAction;
    private String urlId;

    private UserMgmtServlet servlet;

    private UserRepository repository;

    private HttpSession session;

    private final Logger logger = LoggerFactory.getLogger(UserMgmtServlet.class);

    public UserController(String urlAction, String urlId, UserMgmtServlet servlet) {
        this.urlAction = urlAction;
        this.urlId = urlId;
        this.servlet = servlet;
        this.repository = new UserRepositoryImpl();
    }

    public String getContent() {
        if (this.urlAction.equals("index")) {
            return this.getIndex();
        } else if (this.urlAction.equals("add")) {
            return this.getAdd();
        } else if (this.urlAction.equals("edit")) {
            return this.getEdit();
        }

        return "";
    }

    public boolean postContent(HttpServletRequest req) {
        this.session = req.getSession();

        if (this.urlAction.equals("add")) {
            return this.postAdd(req, paramsToUser(req));
        } else if (this.urlAction.equals("edit")) {
            return this.postEdit(req, this.urlId, paramsToUser(req));
        } else if (this.urlAction.equals("delete")) {
            return this.postDelete(req, this.urlId);
        } else if (this.urlAction.equals("addRole")) {
            return this.postAddRole(req);
        } else if (this.urlAction.equals("deleteRole")) {
            return this.postDeleteRole(req);
        }

        return false;
    }

    private User paramsToUser(HttpServletRequest req) {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username == null || username.isEmpty()) {
            this.session.setAttribute("errors", "Username should not be empty.");
            return null;
        }

        if (password == null || password.isEmpty()) {
            this.session.setAttribute("errors", "Password should not be empty.");
            return null;
        }

        User user = new UserImpl();
        user.setUsername(username);
        user.setPassword(password);
        user.setRoles(new String[0]);

        return user;
    }

    private boolean postAdd(HttpServletRequest req, User user) {
        if (user == null) {
            return false;
        }
        if (!this.repository.create(user)) {
            this.session.setAttribute("errors", "Could not add user.");
            return false;
        } else {
            this.session.setAttribute("success", "Successfully added user.");
            return true;
        }
    }

    private boolean postEdit(HttpServletRequest req, String name, User user) {
        if (user == null) {
            return false;
        }
        if (!this.repository.update(name, user)) {
            this.session.setAttribute("errors", "Could not updated user.");
            return false;
        } else {
            this.session.setAttribute("success", "Successfully updated user.");
            return true;
        }
    }

    private boolean postDelete(HttpServletRequest req, String id) {
        if (id == null || id.isEmpty()) {
            this.session.setAttribute("errors", "No ID passed.");
            return false;
        }
        if (!this.repository.delete(id)) {
            this.session.setAttribute("errors", "Could not delete user.");
            return false;
        } else {
            this.session.setAttribute("success", "Successfully deleted user.");
            return true;
        }
    }

    private boolean postAddRole(HttpServletRequest req) {
        // get role to add from request.
        String role = req.getParameter("role");
        if (role == null || role.isEmpty()) {
            this.session.setAttribute("errors", "Role should not be empty.");
            return false;
        }
        // get username to whom role should be added.
        String username = req.getParameter("user");
        if (username == null || username.isEmpty()) {
            this.session.setAttribute("errors", "Could not add role.");
            return false;
        }
        User user = this.repository.get(username);
        if (user == null) {
            this.session.setAttribute("errors", "Could not add role.");
            return false;
        }

        String[] tmpRoles = user.getRoles();
        if (!Arrays.asList(tmpRoles).contains(role)) {
            // add role to list.
            String[] newRoles = new String[tmpRoles.length + 1];
            for (int i = 0; i < newRoles.length; i++) {
                if (i < tmpRoles.length) {
                    newRoles[i] = tmpRoles[i];
                } else {
                    newRoles[i] = role;
                }
            }
            user.setRoles(newRoles);
        } else {
            this.session.setAttribute("errors", "Role already exists.");
            return false;
        }

        if (!this.repository.update(user.getUsername(), user)) {
            this.session.setAttribute("errors", "Could not add role.");
            return false;
        } else {
            this.session.setAttribute("success", "Successfully added role.");
            return true;
        }
    }

    private boolean postDeleteRole(HttpServletRequest req) {
        // get role to add from request.
        String role = req.getParameter("role");
        if (role == null || role.isEmpty()) {
            this.session.setAttribute("errors", "Role should not be empty.");
            return false;
        }
        // get username to whom role should be added.
        String username = req.getParameter("user");
        if (username == null || username.isEmpty()) {
            this.session.setAttribute("errors", "Could not add role.");
            return false;
        }
        User user = this.repository.get(username);
        if (user == null) {
            this.session.setAttribute("errors", "Could not delete role.");
            return false;
        }

        String[] tmpRoles = user.getRoles();
        if (Arrays.asList(tmpRoles).contains(role)) {
            // add role to list.
            String[] newRoles = new String[tmpRoles.length - 1];
            for (int i = 0, j = 0; j < tmpRoles.length; i++, j++) {
                if (!tmpRoles[j].equals(role)) {
                    logger.debug("### role: {}", tmpRoles[j]);
                    newRoles[i] = tmpRoles[j];
                } else {
                    i--;
                }
            }
            user.setRoles(newRoles);
        } else {
            this.session.setAttribute("errors", "Could not delete role.");
            return false;
        }

        if (!this.repository.update(user.getUsername(), user)) {
            this.session.setAttribute("errors", "Could not delete role.");
            return false;
        } else {
            this.session.setAttribute("success", "Successfully deleted role.");
            return true;
        }
    }

    private String getIndex() {
        String template = this.servlet.getTemplateFile("users/list");
        String content = "";

        ArrayList<User> users = this.repository.getAll();
        for (User user : users) {
            content += "<tr>";

            // username
            content += "<td>" + user.getUsername() + "</td>";

            // password
            content += "<td>" + user.getPassword() + "</td>";

            // roles
            content += "<td>";
            for (int i = 0; i < user.getRoles().length; i++) {
                if (i > 0) {
                    content += ", ";
                }
                content += user.getRoles()[i];
            }
            content += "</td>";

            // action buttons.
            content += "<td>";
            content += "<a class=\"btn btn-warning\" href=\"/usermgmt/app?controller=users&action=edit&id="
                    + user.getUsername() + "\">Edit User</a>";
            content += this.getDeleteForm(user.getUsername());
            content += "</td>";

            content += "</tr>";
        }

        return template.replace("<!--INNERTABLE-->", content);
    }

    private String getDeleteForm(String id) {
        String content = "";

        content += "<form action=\"\" method=\"POST\" class=\"form-inline\">";
        content += "<input type=\"hidden\" name=\"controller\" value=\"users\">";
        content += "<input type=\"hidden\" name=\"action\" value=\"delete\">";
        content += "<input type=\"hidden\" name=\"id\" value=\"" + id + "\">";
        content += "<input type=\"submit\" class=\"btn btn-danger\" value=\"Delete User\">";
        content += "</form>";

        return content;
    }

    private String getAdd() {
        String template = this.servlet.getTemplateFile("users/form");

        String content = template.replace("###USERNAME###", "");
        content = content.replace("###PASSWORD###", "");

        content = content.replace("###TITLE_NAME###", "Add User");
        content = content.replace("###SUBMIT_NAME###", "Create User");

        content = content.replace("<!--SECTION_ROLES-->", "");

        return content;
    }

    private String getEdit() {
        String template = this.servlet.getTemplateFile("users/form");

        User user = this.repository.get(this.urlId);

        String content = template.replace("###USERNAME###", user.getUsername());
        content = content.replace("###PASSWORD###", user.getPassword());

        content = content.replace("###TITLE_NAME###", "Edit User");
        content = content.replace("###SUBMIT_NAME###", "Save User");

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"line\"><div class=\"decorator\"></div><hr></div>");
        sb.append("<label>Edit Roles of User</label>");
        sb.append("<table class=\"table table-bordered table-striped table-hover\">");
        for (String role : user.getRoles()) {
            sb.append("<tr>");
            sb.append("<td>" + role + "</td>");
            sb.append("<td>");
            sb.append("<form method=\"POST\" action=\"/usermgmt/app?controller=users&action=deleteRole\">");
            sb.append("<input type=\"hidden\" name=\"user\" value=\"" + this.urlId + "\">");
            sb.append("<input type=\"hidden\" name=\"role\" value=\"" + role + "\">");
            sb.append("<input type=\"submit\" class=\"btn btn-danger\" value=\"Delete Role\">");
            sb.append("</form>");
            sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("<tr><td>");
        sb.append("<form method=\"POST\" action=\"/usermgmt/app?controller=users&action=addRole\">");
        sb.append("<input type=\"hidden\" name=\"user\" value=\"" + this.urlId + "\">");
        sb.append("<input type=\"text\" class=\"form-control\" name=\"role\" placeholder=\"New Role\">");
        sb.append("</td><td>");
        sb.append("<input type=\"submit\" class=\"btn btn-success\" value=\"Add Role\">");
        sb.append("</form>");
        sb.append("</td></tr>");
        sb.append("</table>");
        content = content.replace("<!--SECTION_ROLES-->", sb);

        return content;
    }

}
