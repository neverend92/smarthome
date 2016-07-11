package org.eclipse.smarthome.ui.usermgmt.internal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserMgmtServlet extends HttpServlet {

    private static final long serialVersionUID = 7700873790924746422L;

    private final Logger logger = LoggerFactory.getLogger(UserMgmtServlet.class);

    private String urlController;
    private String urlAction;
    private int urlId;

    private String layoutTemplate;
    private String usersListTemplate;
    private String usersEditTemplate;
    private String usersAddTemplate;
    private String rolesListTemplate;
    private String rolesEditTemplate;
    private String rolesAddTemplate;

    private static final String DEFAULT_CONTROLLER = "users";
    private static final String DEFAULT_ACTION = "index";

    public UserMgmtServlet(String layoutTemplate, String usersListTemplate, String usersEditTemplate,
            String usersAddTemplate, String rolesListTemplate, String rolesEditTemplate, String rolesAddTemplate) {
        this.layoutTemplate = layoutTemplate;
        this.usersListTemplate = usersListTemplate;
        this.usersEditTemplate = usersEditTemplate;
        this.usersAddTemplate = usersAddTemplate;
        this.rolesListTemplate = rolesListTemplate;
        this.rolesEditTemplate = rolesEditTemplate;
        this.rolesAddTemplate = rolesAddTemplate;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        this.getURLParams(req);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        this.getURLParams(req);

    }

    private boolean getURLParams(HttpServletRequest req) {
        // get request params.
        this.urlController = req.getParameter("controller");
        if (urlController.isEmpty()) {
            urlController = DEFAULT_CONTROLLER;
        }
        this.urlAction = req.getParameter("action");
        if (urlAction.isEmpty()) {
            urlAction = DEFAULT_ACTION;
        }
        this.urlId = Integer.parseInt(req.getParameter("id"));

        return true;
    }

}
