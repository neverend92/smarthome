package org.eclipse.smarthome.ui.usermgmt.internal;

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

public class UserMgmtServlet extends HttpServlet {

    // private final Logger logger = LoggerFactory.getLogger(UserMgmtServlet.class);

    private static final long serialVersionUID = 2627006202641947948L;

    // passed controller.
    private String urlController;
    // passed action.
    private String urlAction;
    // passed id.
    private String urlId;

    // bundle to access resources (template files).
    private Bundle bundle;

    // default (fallback) controller.
    private static final String DEFAULT_CONTROLLER = "users";
    // default (fallback) action.
    private static final String DEFAULT_ACTION = "index";

    // array of valid controllers.
    private static final String[] VALID_CONTROLLERS = { "users", "permissions" };
    // array of valid get actions.
    private static final String[] VALID_ACTIONS_GET = { "index", "edit", "add" };
    // array of valid post actions.
    private static final String[] VALID_ACTIONS_POST = { "add", "edit", "delete", "addRole", "deleteRole" };

    // flag for get request.
    private static final int FLAG_GET = 1;
    // flag for post request.
    private static final int FLAG_POST = 2;

    public UserMgmtServlet(Bundle bundle) {
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

        // get html layout for servlet.
        String layoutTemplate = this.getTemplateFile("layout");

        // build content.
        String fullContent = "";
        // switch by controller.
        if (this.urlController.equals("users")) {
            // load user controller
            UserController controller = new UserController(this.urlAction, this.urlId, this);

            // replace inner content for users.
            fullContent = layoutTemplate.replace("<!--INNERCONTENT_USERS-->", controller.getContent());
            fullContent = fullContent.replace("<!--INNERCONTENT_ROLES-->", "");
            // set user tab pane active
            fullContent = fullContent.replace("###TAB_CONTENT_USERS###", "active");
            fullContent = fullContent.replace("###TAB_CONTENT_ROLES###", "");
            // set user tab nav element active.
            fullContent = fullContent.replace("###TAB_USERS###", "active");
            fullContent = fullContent.replace("###TAB_ROLES###", "");

        } else if (this.urlController.equals("permissions")) {
            // load permission controller
            PermissionController controller = new PermissionController(this.urlAction, this.urlId, this);

            // replace inner content for permissions.
            fullContent = layoutTemplate.replace("<!--INNERCONTENT_USERS-->", "");
            fullContent = fullContent.replace("<!--INNERCONTENT_PERMISSIONS-->", controller.getContent());
            // set permission tab pane active.
            fullContent = fullContent.replace("###TAB_CONTENT_USERS###", "");
            fullContent = fullContent.replace("###TAB_CONTENT_PERMISSIONS###", "active");
            // set permission tab nav element active.
            fullContent = fullContent.replace("###TAB_USERS###", "");
            fullContent = fullContent.replace("###TAB_PERMISSIONS###", "active");

        }

        // build error string (empty if no error)
        StringBuilder errors = new StringBuilder();
        // get errors from session.
        Object tmpError = session.getAttribute("errors");
        if (tmpError != null) {
            // surround with bootstrap alert.
            errors.append("<div class=\"alert alert-danger\" role=\"alert\">");
            errors.append(tmpError);
            errors.append("</div>");
        }
        // replace error placeholder.
        fullContent = fullContent.replace("<!--ERRORS-->", errors);

        // build success string (empty if not successful)
        StringBuilder success = new StringBuilder();
        // get success msg from session.
        Object tmpSuccess = session.getAttribute("success");
        if (tmpSuccess != null) {
            // surround with bootstrap alert.
            success.append("<div class=\"alert alert-success\" role=\"alert\">");
            success.append(tmpSuccess);
            success.append("</div>");
        }
        // replace success placeholder.
        fullContent = fullContent.replace("<!--SUCCESS-->", success);

        // set session id.
        fullContent = fullContent.replaceAll("###sessionId###", session.getId());

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

        // build id for next redirect (perhaps emtpy)
        String tmpId = (this.urlId != null && !this.urlAction.isEmpty()) ? "&id=" + this.urlId : "";

        // switch by controller.
        if (this.urlController.equals("users")) {
            // load user controller
            UserController controller = new UserController(this.urlAction, this.urlId, this);
            // handle post request.
            if (controller.postContent(req)) {
                if (this.urlAction.equals("add")) {
                    // success redirect to edit page.
                    res.sendRedirect("/usermgmt/app?controller=users&action=edit&id=" + req.getParameter("username"));
                } else if (this.urlAction.equals("addRole") || this.urlAction.equals("deleteRole")) {
                    // success redirect to edit page.
                    res.sendRedirect("/usermgmt/app?controller=users&action=edit&id=" + req.getParameter("user"));
                } else {
                    // successful redirect to overview.
                    res.sendRedirect("/usermgmt/app");
                }
            } else {
                if (this.urlAction.equals("addRole") || this.urlAction.equals("deleteRole")) {
                    // error redirect to edit page
                    res.sendRedirect("/usermgmt/app?controller=users&action=edit&id=" + req.getParameter("user"));
                } else {
                    // error redirect to same page.
                    res.sendRedirect("/usermgmt/app?controller=users&action=" + this.urlAction + tmpId);
                }
            }
        } else if (this.urlController.equals("permissions")) {
            // load permission controller
            PermissionController controller = new PermissionController(this.urlAction, this.urlId, this);
            // handle post request.
            if (controller.postContent(req)) {

                if (this.urlAction.equals("add")) {
                    // success redirect to edit page.
                    res.sendRedirect(
                            "/usermgmt/app?controller=permissions&action=edit&id=" + req.getParameter("username"));
                } else if (this.urlAction.equals("addRole") || this.urlAction.equals("deleteRole")) {
                    // success redirect to edit page.
                    res.sendRedirect("/usermgmt/app?controller=permissions&action=edit&id=" + req.getParameter("user"));
                } else {
                    // successful redirect to overview
                    res.sendRedirect("/usermgmt/app?controller=permissions");
                }

            } else {
                if (this.urlAction.equals("addRole") || this.urlAction.equals("deleteRole")) {
                    // error redirect to edit page
                    res.sendRedirect("/usermgmt/app?controller=permissions&action=edit&id=" + req.getParameter("user"));
                } else {
                    // error redirect to same page.
                    res.sendRedirect("/usermgmt/app?controller=permissions&action=" + this.urlAction + tmpId);
                }

            }
        }
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

}
