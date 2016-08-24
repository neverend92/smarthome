package org.eclipse.smarthome.ui.mgmt.internal;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MgmtServlet extends HttpServlet {

    private static final long serialVersionUID = 1105083395865000820L;

    private static final Logger logger = LoggerFactory.getLogger(MgmtServlet.class);

    // flag for get request.
    protected static final int FLAG_GET = 1;
    // flag for post request.
    protected static final int FLAG_POST = 2;

    // base url of the servlet.
    protected String baseUrl;

    // default (fallback) controller.
    protected String defaultController;
    // default (fallback) action.
    protected String defaultAction;

    // array of valid controllers.
    protected String[] validControllers;

    // array of valid get actions.
    protected String[] validActionGet;

    // array of valid post actions.
    protected String[] validActionPost;

    // passed controller.
    protected String urlController;

    // passed action.
    protected String urlAction;

    // passed id.
    protected String urlId;

    // custom redirects for actions in case of success and error.
    protected HashMap<String, String> customRedirects;

    // bundle to access resources (template files).
    protected Bundle bundle;

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

        if (this.getController() == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        fullContent = this.getFullContent(this.getController().getContent(), session);

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

        if (this.getController() == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // handle post request.
        if (this.getController().postContent(req)) {
            this.handleSuccess(req, res);
        } else {
            this.handleError(req, res);
        }
    }

    /**
     * Gets the base url.
     *
     * @return
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Gets the current bundle.
     *
     * @return
     */
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Gets the current controller.
     *
     * @return
     */
    protected MgmtController<?> getController() {
        // needs override.
        return null;
    }

    /**
     * Gets HashMap of custom redirects.
     *
     * @return
     */
    public HashMap<String, String> getCustomRedirects() {
        return customRedirects;
    }

    /**
     * Gets the default action.
     *
     * @return
     */
    public String getDefaultAction() {
        return defaultAction;
    }

    /**
     * Gets the default controller.
     *
     * @return
     */
    public String getDefaultController() {
        return defaultController;
    }

    /**
     * Gets the full content for a specific action/page.
     *
     * @param innerContent
     * @param session
     * @return
     */
    protected String getFullContent(String innerContent, HttpSession session) {
        if (innerContent == null || innerContent.isEmpty()) {
            return null;
        }

        // get html layout for servlet.
        String fullContent = this.getTemplateFile("layout");

        for (String entity : this.getValidControllers()) {
            String tmpInnerContent = "";
            String tmpTabClass = "";

            if (this.getUrlController().equals(entity)) {
                tmpInnerContent = innerContent;
                tmpTabClass = "active";
            }

            // replace inner content.
            fullContent = fullContent.replace("<!--INNERCONTENT_" + entity.toUpperCase() + "-->", tmpInnerContent);
            // set user tab pane class.
            fullContent = fullContent.replaceAll("###TAB_" + entity.toUpperCase() + "###", tmpTabClass);
        }

        // set base url for template.
        fullContent = fullContent.replaceAll("###BASE_URL###", this.getBaseUrl());

        // replace error placeholder.
        fullContent = fullContent.replace("<!--ERRORS-->", this.getSessionMessages(session, "errors"));

        // replace success placeholder.
        fullContent = fullContent.replace("<!--SUCCESS-->", this.getSessionMessages(session, "success"));

        return fullContent;
    }

    /**
     * returns error/success messages
     *
     * @param session
     * @param type
     * @return
     */
    protected String getSessionMessages(HttpSession session, String type) {
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
     * Get template file from filename.
     *
     * @param string
     * @return
     */
    public String getTemplateFile(String name) {
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
     * Gets the url action.
     *
     * @return
     */
    public String getUrlAction() {
        return urlAction;
    }

    /**
     * Gets the url controller.
     *
     * @return
     */
    public String getUrlController() {
        return urlController;
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
     * Get the url params (get params)
     *
     * @param req
     * @param flagGet
     * @return
     */
    protected boolean getURLParams(HttpServletRequest req, int flagMethod) {
        // get passed controller.
        this.setUrlController(req.getParameter("controller"));
        // check if controller is empty.
        if (this.getUrlController() == null || this.getUrlController().isEmpty()) {
            // if controller is empty set to default controller.
            this.setUrlController(this.getDefaultController());
        }
        // check if controller is allowed.
        if (!Arrays.asList(this.getValidControllers()).contains(this.getUrlController())) {
            // wrong controller found, quit.
            return false;
        }

        // get passed action.
        this.setUrlAction(req.getParameter("action"));
        // check if action is empty.
        if (this.getUrlAction() == null || this.getUrlAction().isEmpty()) {
            // if action is empty set to default action.
            this.setUrlAction(this.getDefaultAction());
        }
        // switch on request method (get/post)
        if (flagMethod == FLAG_GET) {
            // check if action is allowed.
            if (!Arrays.asList(this.getValidActionGet()).contains(this.getUrlAction())) {
                // wrong action found, quit.
                return false;
            }
        } else if (flagMethod == FLAG_POST) {
            // check if action is allowed.
            if (!Arrays.asList(this.getValidActionPost()).contains(this.getUrlAction())) {
                // wrong action found, quit.
                return false;
            }
        } else {
            // wrong request method found, quit.
            return false;
        }

        // get passed id.
        this.setUrlId(req.getParameter("id"));

        // no errors found, so continue
        return true;
    }

    /**
     * Gets the valid actions for method GET.
     *
     * @return
     */
    public String[] getValidActionGet() {
        return validActionGet;
    }

    /**
     * Gets the valid actions for method POST.
     *
     * @return
     */
    public String[] getValidActionPost() {
        return validActionPost;
    }

    /**
     * Gets all valid controllers.
     *
     * @return
     */
    public String[] getValidControllers() {
        return validControllers;
    }

    private void handleError(HttpServletRequest req, HttpServletResponse res) throws IOException {
        this.handleRedirect(req, res, "Error");
    }

    private void handleRedirect(HttpServletRequest req, HttpServletResponse res, String key) throws IOException {
        String param = this.getController().getFieldName();
        if (param == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String hashKey = this.getUrlAction() + key;
        String redirect = "";
        if (this.getCustomRedirects().containsKey(hashKey)) {
            // custom redirect found.
            redirect = this.getCustomRedirects().get(hashKey);
        } else {
            // default redirect.
            // build id for next redirect (perhaps emtpy)
            String tmpId = (this.getUrlId() != null && !this.getUrlId().isEmpty()) ? this.getUrlId() : "";

            // error redirect to same page.
            redirect = this.getCustomRedirects().get("_default" + key);
            redirect = redirect.replace("###URL_ID###", tmpId);
        }

        redirect = redirect.replace("###URL_CONTROLLER###", this.getUrlController());
        redirect = redirect.replace("###URL_ACTION###", this.getUrlAction());
        if (req.getParameter(param) != null) {
            redirect = redirect.replace("###URL_ID###", req.getParameter(param));
        }

        res.sendRedirect(redirect);
    }

    private void handleSuccess(HttpServletRequest req, HttpServletResponse res) throws IOException {
        this.handleRedirect(req, res, "Success");
    }

    /**
     * Sets the base url.
     *
     * @param baseUrl
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Sets the current bundle.
     *
     * @param bundle
     */
    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Sets the custom redirect HashMap.
     *
     * @param customRedirects
     */
    public void setCustomRedirects(HashMap<String, String> customRedirects) {
        this.customRedirects = customRedirects;
    }

    /**
     * Sets the default action.
     *
     * @param defaultAction
     */
    public void setDefaultAction(String defaultAction) {
        this.defaultAction = defaultAction;
    }

    /**
     * Sets the default controller.
     *
     * @param defaultController
     */
    public void setDefaultController(String defaultController) {
        this.defaultController = defaultController;
    }

    /**
     * Sets the url action.
     *
     * @param urlAction
     */
    public void setUrlAction(String urlAction) {
        this.urlAction = urlAction;
    }

    /**
     * Sets the url controller.
     *
     * @param urlController
     */
    public void setUrlController(String urlController) {
        this.urlController = urlController;
    }

    /**
     * Sets the url id.
     *
     * @param urlId
     */
    public void setUrlId(String urlId) {
        this.urlId = urlId;
    }

    /**
     * Sets valid actions for method get.
     *
     * @param validActionGet
     */
    public void setValidActionGet(String[] validActionGet) {
        this.validActionGet = validActionGet;
    }

    /**
     * Sets valid actions for method post.
     *
     * @param validActionPost
     */
    public void setValidActionPost(String[] validActionPost) {
        this.validActionPost = validActionPost;
    }

    /**
     * Sets valid controllers.
     *
     * @param validControllers
     */
    public void setValidControllers(String[] validControllers) {
        this.validControllers = validControllers;
    }

}
