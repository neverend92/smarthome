package org.eclipse.smarthome.ui.internal.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.eclipse.smarthome.core.auth.Authentication;
import org.eclipse.smarthome.core.internal.auth.AuthenticationProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthServlet extends HttpServlet {

    private static final long serialVersionUID = 7700873790924746422L;

    private final Logger logger = LoggerFactory.getLogger(AuthServlet.class);

    private String indexTemplate;
    private String formTemplate;
    private String logoutTemplate;

    public AuthServlet(String indexTemplate, String formTemplate, String logoutTemplate) {
        this.indexTemplate = indexTemplate;
        this.formTemplate = formTemplate;
        this.logoutTemplate = logoutTemplate;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        logger.debug("Received incoming auth (GET) request {}", req);

        String innerContent = "";

        HttpSession session = req.getSession();
        // set session timeout to 30min.
        if (session.isNew()) {
            session.setMaxInactiveInterval(30 * 60);
        }

        // check if logout action was triggered.
        String action = req.getParameter("action");
        if (action != null && action.equals("logout")) {
            session.removeAttribute("auth");
            logger.debug("Logout User");
            res.sendRedirect("/");
            return;
        }

        // check for authenticated user.
        Authentication auth = (Authentication) session.getAttribute("auth");

        if (auth == null) {
            // show login form.
            innerContent = getFormTemplate(session);
        } else {
            // show logout button.
            innerContent = logoutTemplate.replace("###username###", auth.getUsername());

            String roles = "";
            boolean isFirst = true;
            for (String role : auth.getRoles()) {
                if (!isFirst) {
                    roles += ", ";
                } else {
                    isFirst = false;
                }
                roles += role;
            }
            innerContent = innerContent.replace("###roles###", roles);

            innerContent = innerContent.replace("###token###", auth.getToken());
        }

        res.setContentType("text/html;charset=UTF-8");
        res.getWriter().append(indexTemplate.replace("<!--INNERCONTENT-->", innerContent));
        res.getWriter().close();

        session.removeAttribute("errors");
        session.removeAttribute("username");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        logger.debug("Received incoming auth (POST) request {}", req);

        HttpSession session = req.getSession();
        // set session timeout to 30min.
        if (session.isNew()) {
            session.setMaxInactiveInterval(30 * 60);
        }

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        logger.debug("### " + username + ":" + password);

        // do auth check...
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
        Authentication auth = AuthenticationProviderImpl.getInstace().authenticate(creds);

        if (auth == null) {
            session.setAttribute("errors", "Wrong credentials passed. Try again.");
            session.setAttribute("username", username);
            res.sendRedirect("/auth/login");
            return;
        } else {
            session.setAttribute("auth", auth);

            // check for last requested page.
            Object last_uri = session.getAttribute("last_uri");

            // if page was set, redirect to this page.
            // else redirect to default root page.
            if (last_uri != null) {
                session.removeAttribute("last_uri");
                res.sendRedirect(last_uri.toString());
            } else {
                res.sendRedirect("/");
            }
            return;
        }
    }

    private String getFormTemplate(HttpSession session) {
        StringBuilder errors = new StringBuilder();
        Object tmpError = session.getAttribute("errors");
        if (tmpError != null) {
            errors.append("<div class=\"alert alert-danger\" role=\"alert\">");
            errors.append(tmpError);
            errors.append("</div>");
        }

        String innerContent = formTemplate.replace("<!--ERRORS-->", errors);

        String username = "";
        Object tmpUsername = session.getAttribute("username");
        if (tmpUsername != null) {
            username = tmpUsername.toString();
        }

        innerContent = innerContent.replace("###username###", username);

        return innerContent;
    }
}
