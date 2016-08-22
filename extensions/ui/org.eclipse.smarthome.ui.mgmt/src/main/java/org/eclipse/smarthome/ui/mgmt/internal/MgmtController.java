package org.eclipse.smarthome.ui.mgmt.internal;

import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.smarthome.core.auth.DTO;
import org.eclipse.smarthome.core.auth.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MgmtController<E extends DTO> {

    protected final Logger logger = LoggerFactory.getLogger(MgmtController.class);

    protected MgmtServlet servlet;

    protected HttpSession session;

    protected Repository<E> repository;
    protected String entityName;
    protected String fieldName;
    protected ArrayList<String> attributes;

    protected String urlAction;
    protected String urlId;

    public MgmtController(String urlAction, String urlId, MgmtServlet servlet) {
        this.setUrlAction(urlAction);
        this.setUrlId(urlId);
        this.setServlet(servlet);
        this.setAttributes(new ArrayList<String>());
    }

    /**
     * Gets the content for add view.
     *
     * @return
     */
    protected String getAdd() {
        String template = this.getServlet().getTemplateFile(this.getPlural(this.getEntityName()) + "/form");

        String content = template.replace("###TITLE_NAME###", "Add " + this.getUcFirst(this.getEntityName()));
        content = content.replace("###SUBMIT_NAME###", "Create " + this.getUcFirst(this.getEntityName()));

        content = content.replace("<!--SECTION_ROLES-->", "");

        for (String attribute : this.getAttributes()) {
            content = content.replace(this.getPlaceholderForAttribute(attribute), "");
        }

        return content;
    }

    /**
     * Gets the list of object attributes.
     *
     * @return
     */
    public ArrayList<String> getAttributes() {
        return attributes;
    }

    /**
     * Gets the content for a get request.
     *
     * @return
     */
    public String getContent() {
        if (this.getUrlAction().equals("index")) {
            return this.getIndex();
        } else if (this.getUrlAction().equals("add")) {
            return this.getAdd();
        } else if (this.getUrlAction().equals("edit")) {
            return this.getEdit();
        }

        return null;
    }

    /**
     * Gets the delete form for a specific element.
     *
     * @param id
     * @return
     */
    protected String getDeleteForm(String id) {
        String content = "";

        content += "<form action=\"\" method=\"POST\" class=\"form-inline\">";
        content += "<input type=\"hidden\" name=\"controller\" value=\"" + this.getPlural(this.getEntityName()) + "\">";
        content += "<input type=\"hidden\" name=\"action\" value=\"delete\">";
        content += "<input type=\"hidden\" name=\"id\" value=\"" + id + "\">";
        content += "<input type=\"submit\" onclick=\"return confirm('Do you really want to delete the "
                + this.getUcFirst(this.getEntityName()) + " " + id + "?');\" class=\"btn btn-danger\" value=\"Delete "
                + this.getUcFirst(this.getEntityName()) + "\">";
        content += "</form>";

        return content;
    }

    /**
     * Gets the content for edit view.
     *
     * @return
     */
    protected String getEdit() {
        String template = this.getServlet().getTemplateFile(this.getPlural(this.getEntityName()) + "/form");

        E object = this.getRepository().get(this.getUrlId());

        if (object == null) {
            return null;
        }

        String content = template.replace("###TITLE_NAME###", "Edit " + this.getUcFirst(this.getEntityName()));
        content = content.replace("###SUBMIT_NAME###", "Save " + this.getUcFirst(this.getEntityName()));

        content = content.replace("<!--SECTION_ROLES-->", this.getRoleForms(object.getArray("roles")));

        for (String attribute : this.getAttributes()) {
            content = content.replace(this.getPlaceholderForAttribute(attribute), object.get(attribute));
        }

        return content;
    }

    /**
     * Gets the entity name.
     *
     * @return
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Gets the field name.
     *
     * @return
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Gets the content for index view.
     *
     * @return
     */
    protected String getIndex() {
        String template = this.getServlet().getTemplateFile(this.getPlural(this.getEntityName()) + "/list");
        String content = "";

        ArrayList<E> objects = this.getRepository().getAll();
        for (E object : objects) {
            content += "<tr>";

            for (String attribute : this.getAttributes()) {
                content += "<td>" + object.get(attribute) + "</td>";
            }

            // roles
            if (object.getArray("roles") != null) {
                content += "<td>";
                for (int i = 0; i < object.getArray("roles").length; i++) {
                    if (i > 0) {
                        content += ", ";
                    }
                    content += object.getArray("roles")[i];
                }
                content += "</td>";
            }

            // action buttons.
            content += "<td>";
            content += "<a class=\"btn btn-warning\" href=\"/usermgmt/app?controller="
                    + this.getPlural(this.getEntityName()) + "&action=edit&id=" + object.getId() + "\">Edit "
                    + this.getUcFirst(this.getEntityName()) + "</a>";
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

    /**
     * Gets the placeholder in view for attribute.
     *
     * @param attribute
     * @return
     */
    protected String getPlaceholderForAttribute(String attribute) {
        return "###" + attribute.toUpperCase() + "###";
    }

    /**
     * Gets the plural string
     *
     * @param s
     * @return
     */
    protected String getPlural(String s) {
        return s + "s";
    }

    /**
     * Gets the current repository.
     *
     * @return
     */
    public Repository<E> getRepository() {
        return repository;
    }

    /**
     * Gets the role forms.
     *
     * @param roles
     * @return
     */
    protected String getRoleForms(String[] roles) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"line\"><div class=\"decorator\"></div><hr></div>");
        sb.append("<label>Edit Roles of " + this.getUcFirst(this.getEntityName()) + "</label>");
        sb.append("<table class=\"table table-bordered table-striped table-hover\">");
        for (String role : roles) {
            sb.append("<tr>");
            sb.append("<td>" + role + "</td>");
            sb.append("<td>");
            sb.append("<form method=\"POST\" action=\"/usermgmt/app?controller=" + this.getPlural(this.getEntityName())
                    + "&action=deleteRole\">");
            sb.append("<input type=\"hidden\" name=\"" + this.getFieldName() + "\" value=\"" + this.getUrlId() + "\">");
            sb.append("<input type=\"hidden\" name=\"role\" value=\"" + role + "\">");
            sb.append("<input type=\"submit\" onclick=\"return confirm('Do you really want to delete the role " + role
                    + "?');\" class=\"btn btn-danger\" value=\"Delete Role\">");
            sb.append("</form>");
            sb.append("</td>");
            sb.append("</tr>");
        }
        sb.append("<tr><td>");
        sb.append("<form method=\"POST\" action=\"/usermgmt/app?controller=" + this.getPlural(this.getEntityName())
                + "&action=addRole\">");
        sb.append("<input type=\"hidden\" name=\"" + this.getFieldName() + "\" value=\"" + this.getUrlId() + "\">");
        sb.append("<input type=\"text\" class=\"form-control\" name=\"role\" placeholder=\"New Role\">");
        sb.append("</td><td>");
        sb.append("<input type=\"submit\" class=\"btn btn-success\" value=\"Add Role\">");
        sb.append("</form>");
        sb.append("</td></tr>");
        sb.append("</table>");

        return sb.toString();
    }

    /**
     * Gets the servlet
     *
     * @return
     */
    public MgmtServlet getServlet() {
        return servlet;
    }

    /**
     * Gets the http session.
     *
     * @return
     */
    public HttpSession getSession() {
        return session;
    }

    /**
     * Return the string with first letter uppercase.
     *
     * @param s
     * @return
     */
    protected String getUcFirst(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Gets the url action.
     *
     * @return
     */
    public String getUrlAction() {
        return urlAction;
    }

    /**
     * Gets the url id.
     *
     * @return
     */
    public String getUrlId() {
        return urlId;
    }

    /**
     * Handles post request to add or delete a role.
     *
     * @param req
     * @param type
     * @return
     */
    private boolean handleRoleChange(HttpServletRequest req, String type) {
        // get role to add from request.
        String role = req.getParameter("role");
        // check if a valid role was passed.
        if (role == null || role.isEmpty()) {
            this.getSession().setAttribute("errors", "Role should not be empty.");
            return false;
        }
        // get username to whom role should be added.
        String paramId = req.getParameter(this.getFieldName());
        // check if a valid object id was passed.
        if (paramId == null || paramId.isEmpty()) {
            this.getSession().setAttribute("errors", "Could not " + type + " role.");
            return false;
        }
        // get the object by id.
        E object = this.getRepository().get(paramId);
        // check if object exists.
        if (object == null) {
            this.getSession().setAttribute("errors", "Could not " + type + " role.");
            return false;
        }

        // save old roles.
        String[] tmpRoles = object.getArray("roles");
        // build condition:
        // add: role should NOT exists yet.
        // delete: role must NOT exists yet.
        boolean condition = false;
        if (type.equals("add")) {
            condition = !Arrays.asList(tmpRoles).contains(role);
        } else if (type.equals("delete")) {
            condition = Arrays.asList(tmpRoles).contains(role);
        }

        // check condition.
        if (condition) {
            String[] newRoles;
            if (type.equals("add")) {
                // add role to list.
                // add one spot in the array.
                newRoles = new String[tmpRoles.length + 1];
                // copy old roles into new array.
                for (int i = 0; i < newRoles.length; i++) {
                    if (i < tmpRoles.length) {
                        // copy old role.
                        newRoles[i] = tmpRoles[i];
                    } else {
                        // add new role
                        newRoles[i] = role;
                    }
                }
            } else if (type.equals("delete")) {
                // delete role from list.
                // make array smaller.
                newRoles = new String[tmpRoles.length - 1];
                // copy old roles into new array.
                for (int i = 0, j = 0; j < tmpRoles.length; i++, j++) {
                    if (!tmpRoles[j].equals(role)) {
                        // copy old role.
                        newRoles[i] = tmpRoles[j];
                    } else {
                        // delete role by skipping it.
                        i--;
                    }
                }
            } else {
                // throw error.
                this.getSession().setAttribute("errors", "Could not " + type + " role.");
                return false;
            }

            // set new roles array to object.
            object.set("roles", newRoles);
        } else {
            // throw error.
            this.getSession().setAttribute("errors", "Could not " + type + " role.");
            return false;
        }

        // update in repository.
        if (!this.getRepository().update(object.getId(), object, true)) {
            this.getSession().setAttribute("errors", "Could not " + type + " role.");
            return false;
        } else {
            this.getSession().setAttribute("success", "Successfully " + type + "ed role.");
            return true;
        }
    }

    /**
     * Creates an object from request params.
     *
     * @param req
     * @return
     */
    private E paramsToObject(HttpServletRequest req) {
        E object = this.getModel();

        for (String attribute : this.getAttributes()) {
            String value = req.getParameter(attribute);
            if (value == null || value.isEmpty()) {
                this.getSession().setAttribute("errors", this.getUcFirst(attribute) + " should not be empty.");
                return null;
            }

            object.set(attribute, value);
        }

        // TODO don't do that for Token and Node!!!
        object.set("roles", new String[0]);

        return object;
    }

    /**
     * Handles post request to add object
     *
     * @param object
     * @return
     */
    private boolean postAdd(E object) {
        if (object == null) {
            return false;
        }

        if (this.getRepository().get(object) != null) {
            this.getSession().setAttribute("errors", this.getUcFirst(this.getEntityName()) + " already exists.");
            return false;
        }

        if (!this.getRepository().create(object)) {
            this.getSession().setAttribute("errors", "Could not add " + this.getUcFirst(this.getEntityName()) + ".");
            return false;
        }

        this.getSession().setAttribute("success", "Successfully added " + this.getUcFirst(this.getEntityName()) + ".");
        return true;
    }

    /**
     * Handles post request to add role.
     *
     * @param req
     * @return
     */
    private boolean postAddRole(HttpServletRequest req) {
        return this.handleRoleChange(req, "add");
    }

    /**
     * Handles a post request.
     *
     * @param req
     * @return
     */
    public boolean postContent(HttpServletRequest req) {
        this.setSession(req.getSession());

        if (this.getUrlAction().equals("add")) {
            return this.postAdd(this.paramsToObject(req));
        } else if (this.getUrlAction().equals("edit")) {
            return this.postEdit(this.urlId, this.paramsToObject(req));
        } else if (this.getUrlAction().equals("delete")) {
            return this.postDelete(req, this.urlId);
        } else if (this.getUrlAction().equals("addRole")) {
            return this.postAddRole(req);
        } else if (this.getUrlAction().equals("deleteRole")) {
            return this.postDeleteRole(req);
        }

        return false;
    }

    /**
     * Handles post request to delete object.
     *
     * @param req
     * @param urlId
     * @return
     */
    private boolean postDelete(HttpServletRequest req, String urlId) {
        if (urlId == null || urlId.isEmpty()) {
            return false;
        }

        if (!this.getRepository().delete(urlId)) {
            this.getSession().setAttribute("errors", "Could not delete " + this.getUcFirst(this.getEntityName()) + ".");
            return false;
        }

        this.getSession().setAttribute("success",
                "Successfully deleted " + this.getUcFirst(this.getEntityName()) + ".");
        return true;
    }

    /**
     * Handles post request to delete role.
     *
     * @param req
     * @return
     */
    private boolean postDeleteRole(HttpServletRequest req) {
        return this.handleRoleChange(req, "delete");
    }

    /**
     * Handles post request to update object.
     *
     * @param urlId
     * @param object
     * @return
     */
    private boolean postEdit(String urlId, E object) {
        if (object == null) {
            return false;
        }

        if (!this.getRepository().update(urlId, object, false)) {
            this.getSession().setAttribute("errors",
                    "Could not updated " + this.getUcFirst(this.getEntityName()) + ".");
            return false;
        }

        this.getSession().setAttribute("success",
                "Successfully updated " + this.getUcFirst(this.getEntityName()) + ".");
        return true;
    }

    /**
     * Sets the attributes list.
     *
     * @param attributes
     */
    public void setAttributes(ArrayList<String> attributes) {
        this.attributes = attributes;
    }

    /**
     * Sets the entity name.
     *
     * @param entityName
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * Sets the field name.
     *
     * @param fieldName
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Sets the repository
     *
     * @param repository
     */
    public void setRepository(Repository<E> repository) {
        this.repository = repository;
    }

    /**
     * Sets the servlet
     *
     * @param servlet
     */
    public void setServlet(MgmtServlet servlet) {
        this.servlet = servlet;
    }

    /**
     * Sets the http session
     *
     * @param session
     */
    public void setSession(HttpSession session) {
        this.session = session;
    }

    /**
     * Sets url action
     *
     * @param urlAction
     */
    public void setUrlAction(String urlAction) {
        this.urlAction = urlAction;
    }

    /**
     * Sets url id.
     *
     * @param urlId
     */
    public void setUrlId(String urlId) {
        this.urlId = urlId;
    }

}
