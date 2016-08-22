package org.eclipse.smarthome.ui.nodemgmt.internal;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;

public class NodeMgmtServlet extends HttpServlet {

    private static final long serialVersionUID = 3201527895131390811L;

    // default (fallback) controller.
    private static final String DEFAULT_CONTROLLER = "nodes";
    // default (fallback) action.
    private static final String DEFAULT_ACTION = "index";
    // array of valid controllers.
    private static final String[] VALID_CONTROLLERS = { "nodes" };

    // array of valid get actions.
    private static final String[] VALID_ACTIONS_GET = { "index", "edit", "add", "search" };

    // array of valid post actions.
    private static final String[] VALID_ACTIONS_POST = { "add", "edit", "delete", "search" };

    // flag for get request.
    private static final int FLAG_GET = 1;
    // flag for post request.
    private static final int FLAG_POST = 2;

    // passed controller.
    private String urlController;
    // passed action.
    private String urlAction;
    // passed id.
    private String urlId;

    // bundle to access resources (template files).
    private Bundle bundle;

    public NodeMgmtServlet(Bundle bundle) {
        this.bundle = bundle;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // check url params.
        if (!this.getURLParams(req, FLAG_GET)) {
            // wrong params found, return 404.
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // get session.
        HttpSession session = req.getSession();

        // build content.
        String fullContent = "";

        // switch by controller.
        if (this.urlController.equals("nodes")) {
            // load user controller
            NodeController controller = new NodeController(this.urlAction, this.urlId, this);

            fullContent = this.getFullContent(controller.getContent(), session);

        }

        if (fullContent == null || fullContent.isEmpty()) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // set content type.
        res.setContentType("text/html;charset=UTF-8");
        // write output.
        res.getWriter().append(fullContent);
        // close writer.
        res.getWriter().close();

        session.removeAttribute("errors");
        session.removeAttribute("success");
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // check url params.
        if (!this.getURLParams(req, FLAG_POST)) {
            // wrong params found, return 404.
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // switch by controller.
        if (this.urlController.equals("nodes")) {
            // load user controller
            NodeController controller = new NodeController(this.urlAction, this.urlId, this);
            // handle post request.
            if (controller.postContent(req)) {
                this.handleSuccess(req, res);
            } else {
                this.handleError(req, res);
            }
        }
    }

    /**
     * Gets the full content for a specific action/page
     *
     * @param innerContent
     * @param session
     * @return
     */
    private String getFullContent(String innerContent, HttpSession session) {

        if (innerContent == null || innerContent.isEmpty()) {
            return null;
        }

        // get html layout for servlet.
        String fullContent = this.getTemplateFile("layout");

        String innerContentNode = "";
        String tabClassNode = "";

        if (this.urlController.equals("nodes")) {
            innerContentNode = innerContent;
            tabClassNode = "active";
        }

        // replace inner content for users.
        fullContent = fullContent.replace("<!--INNERCONTENT_NODES-->", innerContentNode);
        // set user tab pane active
        fullContent = fullContent.replace("###TAB_CONTENT_NODES###", tabClassNode);
        // set user tab nav element active.
        fullContent = fullContent.replace("###TAB_NODES###", tabClassNode);
        // set base url for template.
        fullContent = fullContent.replaceAll("###BASE_URL###",
                NodeMgmtService.MGMT_ALIAS + "/" + NodeMgmtService.SERVLET_NAME);

        // replace error placeholder.
        fullContent = fullContent.replace("<!--ERRORS-->", this.getSessionMessages(session, "errors"));

        // replace success placeholder.
        fullContent = fullContent.replace("<!--SUCCESS-->", this.getSessionMessages(session, "success"));

        return fullContent;
    }

    /**
     * get id parameter for current controller.
     *
     * @return
     */
    private String getParamByController() {
        if (this.urlController.equals("nodes")) {
            return "ip";
        }

        return null;
    }

    /**
     * returns error/success messages
     *
     * @param session
     * @param type
     * @return
     */
    private String getSessionMessages(HttpSession session, String type) {
        // build success string (empty if not successful)
        StringBuilder sb = new StringBuilder();
        // get success msg from session.
        Object tmp = session.getAttribute(type);
        if (tmp != null) {
            // surround with bootstrap alert.
            String bootstrapClass = "danger";
            if (type == "success") {
                bootstrapClass = "success";
            }
            sb.append("<div class=\"alert alert-" + bootstrapClass + "\" role=\"alert\">");
            sb.append(tmp);
            sb.append("</div>");
        }

        return sb.toString();
    }

    /**
     * get template file from filename
     *
     * @param name
     * @return String
     */
    protected String getTemplateFile(String name) {
        String template;
        URL url = this.bundle.getEntry("templates/" + name + ".html");
        if (url != null) {
            try {
                template = IOUtils.toString(url.openStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Cannot find " + name + " - failed to initialize user management servlet");
        }

        return template;
    }

    /**
     * get the url params (get params)
     *
     * @param req
     * @param flagMethod
     * @return boolean
     */
    private boolean getURLParams(HttpServletRequest req, int flagMethod) {
        // get passed controller.
        this.urlController = req.getParameter("controller");
        // check if controller is empty.
        if (this.urlController == null || this.urlController.isEmpty()) {
            // if controller is empty set to default controller.
            urlController = DEFAULT_CONTROLLER;
        }
        // check if controller is allowed.
        if (!Arrays.asList(VALID_CONTROLLERS).contains(this.urlController)) {
            // wrong controller found, quit.
            return false;
        }

        // get passed action.
        this.urlAction = req.getParameter("action");
        // check if action is empty.
        if (this.urlAction == null || this.urlAction.isEmpty()) {
            // if action is empty set to default action.
            this.urlAction = DEFAULT_ACTION;
        }
        // switch on request method (get/post)
        if (flagMethod == FLAG_GET) {
            // check if action is allowed.
            if (!Arrays.asList(VALID_ACTIONS_GET).contains(this.urlAction)) {
                // wrong action found, quit.
                return false;
            }
        } else if (flagMethod == FLAG_POST) {
            // check if action is allowed.
            if (!Arrays.asList(VALID_ACTIONS_POST).contains(this.urlAction)) {
                // wrong action found, quit.
                return false;
            }
        } else {
            // wrong request method found, quit.
            return false;
        }

        // get passed id.
        this.urlId = req.getParameter("id");

        // no errors found, so continue
        return true;
    }

    /**
     * handle failed request, redirect to corresponding url.
     *
     * @param req
     * @param res
     * @throws IOException
     */
    private void handleError(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String param = this.getParamByController();
        if (param == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (this.urlAction.equals("addRole") || this.urlAction.equals("deleteRole")) {
            // error redirect to edit page
            res.sendRedirect(NodeMgmtService.MGMT_ALIAS + "/" + NodeMgmtService.SERVLET_NAME + "?controller="
                    + this.urlController + "&action=edit&id=" + req.getParameter(param));
        } else {
            // build id for next redirect (perhaps emtpy)
            String tmpId = (this.urlId != null && !this.urlId.isEmpty()) ? "&id=" + this.urlId : "";

            // error redirect to same page.
            res.sendRedirect(NodeMgmtService.MGMT_ALIAS + "/" + NodeMgmtService.SERVLET_NAME + "?controller="
                    + this.urlController + "&action=" + this.urlAction + tmpId);
        }
    }

    /**
     * handle successful request, redirect to corresponding url.
     *
     * @param req
     * @param res
     * @throws IOException
     */
    private void handleSuccess(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String param = this.getParamByController();
        if (param == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (this.urlAction.equals("add")) {
            // success redirect to edit page.
            res.sendRedirect(NodeMgmtService.MGMT_ALIAS + "/" + NodeMgmtService.SERVLET_NAME + "?controller="
                    + this.urlController + "&action=edit&id=" + req.getParameter(param));
        } else if (this.urlAction.equals("addRole") || this.urlAction.equals("deleteRole")) {
            // success redirect to edit page.
            res.sendRedirect(NodeMgmtService.MGMT_ALIAS + "/" + NodeMgmtService.SERVLET_NAME + "?controller="
                    + this.urlController + "&action=edit&id=" + req.getParameter(param));
        } else {
            // successful redirect to overview
            res.sendRedirect(NodeMgmtService.MGMT_ALIAS + "/" + NodeMgmtService.SERVLET_NAME + "?controller="
                    + this.urlController);
        }
    }

}
