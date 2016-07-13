package org.eclipse.smarthome.ui.usermgmt.internal;

import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.smarthome.core.auth.DTO;
import org.eclipse.smarthome.core.auth.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Controller<E extends DTO> {

    protected final Logger logger = LoggerFactory.getLogger(UserMgmtServlet.class);

    protected UserMgmtServlet servlet;

    protected HttpSession session;

    protected Repository<E> repository;
    protected String entityName;
    protected String fieldName;
    protected ArrayList<String> attributes;

    protected String urlAction;
    protected String urlId;

    public Controller(String urlAction, String urlId, UserMgmtServlet servlet) {
        this.urlAction = urlAction;
        this.urlId = urlId;
        this.servlet = servlet;
        this.attributes = new ArrayList<String>();
    }

    protected String getAdd() {
        String template = this.servlet.getTemplateFile(this.getPlural(this.entityName) + "/form");

        String content = template.replace("###TITLE_NAME###", "Add " + this.getUcFirst(this.entityName));
        content = content.replace("###SUBMIT_NAME###", "Create " + this.getUcFirst(this.entityName));

        content = content.replace("<!--SECTION_ROLES-->", "");

        for (String attribute : this.attributes) {
            content = content.replace(this.getPlaceholderForAttribute(attribute), "");
        }

        return content;
    }

    public String getContent() {
        if (this.urlAction.equals("index")) {
            return this.getIndex();
        } else if (this.urlAction.equals("add")) {
            return this.getAdd();
        } else if (this.urlAction.equals("edit")) {
            return this.getEdit();
        }

        return null;
    }

    protected String getDeleteForm(String id) {
        String content = "";

        content += "<form action=\"\" method=\"POST\" class=\"form-inline\">";
        content += "<input type=\"hidden\" name=\"controller\" value=\"" + this.getPlural(this.entityName) + "\">";
        content += "<input type=\"hidden\" name=\"action\" value=\"delete\">";
        content += "<input type=\"hidden\" name=\"id\" value=\"" + id + "\">";
        content += "<input type=\"submit\" onclick=\"return confirm('Do you really want to delete th " + this.entityName
                + " '" + id + "'?');\" class=\"btn btn-danger\" value=\"Delete " + this.getUcFirst(this.entityName)
                + "\">";
        content += "</form>";

        return content;
    }

    protected String getEdit() {
        String template = this.servlet.getTemplateFile(this.getPlural(this.entityName) + "/form");

        E object = this.repository.get(this.urlId);

        if (object == null) {
            return null;
        }

        String content = template.replace("###TITLE_NAME###", "Edit " + this.getUcFirst(this.entityName));
        content = content.replace("###SUBMIT_NAME###", "Save " + this.getUcFirst(this.entityName));

        content = content.replace("<!--SECTION_ROLES-->", this.getRoleForms(object.getRoles()));

        for (String attribute : this.attributes) {
            content = content.replace(this.getPlaceholderForAttribute(attribute), object.get(attribute));
        }

        return content;
    }

    protected String getIndex() {
        String template = this.servlet.getTemplateFile(this.getPlural(this.entityName) + "/list");
        String content = "";

        ArrayList<E> objects = this.repository.getAll();
        for (E object : objects) {
            content += "<tr>";

            for (String attribute : this.attributes) {
                content += "<td>" + object.get(attribute) + "</td>";
            }

            // roles
            content += "<td>";
            for (int i = 0; i < object.getRoles().length; i++) {
                if (i > 0) {
                    content += ", ";
                }
                content += object.getRoles()[i];
            }
            content += "</td>";

            // action buttons.
            content += "<td>";
            content += "<a class=\"btn btn-warning\" href=\"/usermgmt/app?controller=" + this.getPlural(this.entityName)
                    + "&action=edit&id=" + object.getId() + "\">Edit " + this.getUcFirst(this.entityName) + "</a>";
            content += this.getDeleteForm(object.getId());
            content += "</td>";

            content += "</tr>";
        }

        return template.replace("<!--INNERTABLE-->", content);
    }

    protected E getModel() {
        // needs override
        return null;
    }

    protected String getPlaceholderForAttribute(String attribute) {
        return "###" + attribute.toUpperCase() + "###";
    }

    protected String getPlural(String s) {
        return s + "s";
    }

    protected String getRoleForms(String[] roles) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"line\"><div class=\"decorator\"></div><hr></div>");
        sb.append("<label>Edit Roles of " + this.getUcFirst(this.entityName) + "</label>");
        sb.append("<table class=\"table table-bordered table-striped table-hover\">");
        for (String role : roles) {
            sb.append("<tr>");
            sb.append("<td>" + role + "</td>");
            sb.append("<td>");
            sb.append("<form method=\"POST\" action=\"/usermgmt/app?controller=" + this.getPlural(this.entityName)
                    + "&action=deleteRole\">");
            sb.append("<input type=\"hidden\" name=\"" + this.fieldName + "\" value=\"" + this.urlId + "\">");
            sb.append("<input type=\"hidden\" name=\"role\" value=\"" + role + "\">");
            sb.append("<input type=\"submit\" onclick=\"return confirm('Do you really want to delete the role '" + role
                    + "'?');\" class=\"btn btn-danger\" value=\"Delete Role\">");
            sb.append("</form>");
            sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("<tr><td>");
        sb.append("<form method=\"POST\" action=\"/usermgmt/app?controller=" + this.getPlural(this.entityName)
                + "&action=addRole\">");
        sb.append("<input type=\"hidden\" name=\"" + this.fieldName + "\" value=\"" + this.urlId + "\">");
        sb.append("<input type=\"text\" class=\"form-control\" name=\"role\" placeholder=\"New Role\">");
        sb.append("</td><td>");
        sb.append("<input type=\"submit\" class=\"btn btn-success\" value=\"Add Role\">");
        sb.append("</form>");
        sb.append("</td></tr>");
        sb.append("</table>");

        return sb.toString();
    }

    protected String getUcFirst(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    protected boolean handleRoleChange(HttpServletRequest req, String type) {
        // get role to add from request.
        String role = req.getParameter("role");
        if (role == null || role.isEmpty()) {
            this.session.setAttribute("errors", "Role should not be empty.");
            return false;
        }
        // get username to whom role should be added.
        String paramId = req.getParameter(this.fieldName);
        if (paramId == null || paramId.isEmpty()) {
            this.session.setAttribute("errors", "Could not " + type + " role.");
            return false;
        }
        E object = this.repository.get(paramId);
        if (object == null) {
            this.session.setAttribute("errors", "Could not " + type + " role.");
            return false;
        }

        String[] tmpRoles = object.getRoles();
        boolean condition = false;
        if (type.equals("add")) {
            condition = !Arrays.asList(tmpRoles).contains(role);
        } else if (type.equals("delete")) {
            condition = Arrays.asList(tmpRoles).contains(role);
        }

        if (condition) {
            String[] newRoles;
            if (type.equals("add")) {
                // add role to list.
                newRoles = new String[tmpRoles.length + 1];
                for (int i = 0; i < newRoles.length; i++) {
                    if (i < tmpRoles.length) {
                        newRoles[i] = tmpRoles[i];
                    } else {
                        newRoles[i] = role;
                    }
                }
            } else if (type.equals("delete")) {
                // delete role from list.
                newRoles = new String[tmpRoles.length - 1];
                for (int i = 0, j = 0; j < tmpRoles.length; i++, j++) {
                    if (!tmpRoles[j].equals(role)) {
                        newRoles[i] = tmpRoles[j];
                    } else {
                        i--;
                    }
                }
            } else {
                this.session.setAttribute("errors", "Could not " + type + " role.");
                return false;
            }

            object.setRoles(newRoles);
        } else {
            this.session.setAttribute("errors", "Could not " + type + " role.");
            return false;
        }

        if (!this.repository.update(object.getId(), object)) {
            this.session.setAttribute("errors", "Could not " + type + " role.");
            return false;
        } else {
            this.session.setAttribute("success", "Successfully " + type + "ed role.");
            return true;
        }
    }

    protected E paramsToObject(HttpServletRequest req) {
        E object = this.getModel();

        for (String attribute : this.attributes) {
            String value = req.getParameter(attribute);
            if (value == null || value.isEmpty()) {
                this.session.setAttribute("errors", this.getUcFirst(attribute) + " should not be empty.");
                return null;
            }

            object.set(attribute, value);
        }

        object.setRoles(new String[0]);

        return object;
    }

    protected boolean postAdd(E object) {
        if (object == null) {
            return false;
        }

        if (this.repository.get(object) != null) {
            this.session.setAttribute("errors", this.getUcFirst(this.entityName) + " already exists.");
            return false;
        }

        if (!this.repository.create(object)) {
            this.session.setAttribute("errors", "Could not add " + this.entityName + ".");
            return false;
        }

        this.session.setAttribute("success", "Successfully added " + this.entityName + ".");
        return true;
    }

    protected boolean postAddRole(HttpServletRequest req) {
        return this.handleRoleChange(req, "add");
    }

    public boolean postContent(HttpServletRequest req) {
        this.session = req.getSession();

        if (this.urlAction.equals("add")) {
            return this.postAdd(paramsToObject(req));
        } else if (this.urlAction.equals("edit")) {
            return this.postEdit(this.urlId, paramsToObject(req));
        } else if (this.urlAction.equals("delete")) {
            return this.postDelete(req, this.urlId);
        } else if (this.urlAction.equals("addRole")) {
            return this.postAddRole(req);
        } else if (this.urlAction.equals("deleteRole")) {
            return this.postDeleteRole(req);
        }

        return false;
    }

    protected boolean postDelete(HttpServletRequest req, String urlId) {
        if (urlId == null || urlId.isEmpty()) {
            return false;
        }

        if (!this.repository.delete(urlId)) {
            this.session.setAttribute("errors", "Could not delete " + this.entityName + ".");
            return false;
        }

        this.session.setAttribute("success", "Successfully deleted " + this.entityName + ".");
        return true;
    }

    protected boolean postDeleteRole(HttpServletRequest req) {
        return this.handleRoleChange(req, "delete");
    }

    protected boolean postEdit(String urlId, E object) {
        if (object == null) {
            return false;
        }

        if (!this.repository.update(urlId, object)) {
            this.session.setAttribute("errors", "Could not updated " + this.entityName + ".");
            return false;
        }

        this.session.setAttribute("success", "Successfully updated " + this.entityName + ".");
        return true;
    }

}
