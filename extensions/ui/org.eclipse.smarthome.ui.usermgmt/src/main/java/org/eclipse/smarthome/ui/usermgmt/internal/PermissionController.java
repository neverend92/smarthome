package org.eclipse.smarthome.ui.usermgmt.internal;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.smarthome.core.auth.Permission;
import org.eclipse.smarthome.core.auth.PermissionRepository;
import org.eclipse.smarthome.core.internal.auth.PermissionImpl;
import org.eclipse.smarthome.core.internal.auth.PermissionRepositoryImpl;

public class PermissionController {

    private String urlAction;
    private String urlId;

    private UserMgmtServlet servlet;

    private PermissionRepository repository;

    public PermissionController(String urlAction, String urlId, UserMgmtServlet servlet) {
        this.urlAction = urlAction;
        this.urlId = urlId;
        this.servlet = servlet;
        this.repository = new PermissionRepositoryImpl();
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

        if (this.urlAction.equals("add")) {
            return this.postAdd(paramsToPermission(req));
        } else if (this.urlAction.equals("edit")) {
            return this.postEdit(this.urlId, paramsToPermission(req));
        } else if (this.urlAction.equals("delete")) {
            return this.postDelete(req, this.urlId);
        }

        return false;
    }

    private Permission paramsToPermission(HttpServletRequest req) {
        String reqUrl = req.getParameter("reqUrl");

        if (reqUrl == null || reqUrl.isEmpty()) {
            req.getSession().setAttribute("errors", "Requested URL should not be empty.");
            return null;
        }

        Permission permission = new PermissionImpl();
        permission.setReqUrl(reqUrl);
        permission.setRoles(new String[0]);

        return permission;
    }

    private boolean postAdd(Permission permission) {
        if (permission == null) {
            return false;
        }
        return this.repository.create(permission);
    }

    private boolean postEdit(String name, Permission permission) {
        if (permission == null) {
            return false;
        }
        return this.repository.update(name, permission);
    }

    private boolean postDelete(HttpServletRequest req, String id) {
        if (id == null || id.isEmpty()) {
            req.getSession().setAttribute("errors", "No ID passed.");
            return false;
        }
        return this.repository.delete(id);
    }

    private String getIndex() {
        String template = this.servlet.getTemplateFile("permissions/list");
        String content = "";

        ArrayList<Permission> permissions = this.repository.getAll();
        for (Permission permission : permissions) {
            content += "<tr>";

            // requested Url
            content += "<td>" + permission.getReqUrl() + "</td>";

            // roles
            content += "<td>";
            for (int i = 0; i < permission.getRoles().length; i++) {
                if (i > 0) {
                    content += ", ";
                }
                content += permission.getRoles()[i];
            }
            content += "</td>";

            // action buttons.
            content += "<td>";
            content += this.getForm("GET", "edit", permission.getReqUrl(), "edit");
            content += this.getForm("POST", "delete", permission.getReqUrl(), "delete");
            content += "</td>";

            content += "</tr>";
        }

        return template.replace("<!--INNERTABLE-->", content);
    }

    private String getForm(String method, String action, String id, String submitName) {
        String content = "";

        content += "<form action=\"\" method=\"" + method + "\" class=\"form-inline\">";
        content += "<input type=\"hidden\" name=\"controller\" value=\"permissions\">";
        content += "<input type=\"hidden\" name=\"action\" value=\"" + action + "\">";
        content += "<input type=\"hidden\" name=\"id\" value=\"" + id + "\">";
        content += "<input type=\"submit\" class=\"btn btn-default\" value=\"" + submitName + "\">";
        content += "</form>";

        return content;
    }

    private String getAdd() {
        String template = this.servlet.getTemplateFile("permissions/form");

        String content = template.replace("###REQURL###", "");

        content = content.replace("###SUBMIT_NAME###", "Create Permission");

        return content;
    }

    private String getEdit() {
        String template = this.servlet.getTemplateFile("permissions/form");

        Permission permission = this.repository.get(this.urlId);

        String content = template.replace("###REQURL###", permission.getReqUrl());
        // content = content.replace("###PASSWORD###", permission.getRoles());

        content = content.replace("###SUBMIT_NAME###", "Create User");

        return content;
    }

}
