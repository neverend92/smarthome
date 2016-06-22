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
        logger.debug("Received incoming auth (GET) request ", req);

        HttpSession session = req.getSession();

        String action = req.getParameter("action");
        if (action != null && action.equals("logout")) {
            session.removeAttribute("auth");
            logger.debug("Logout User");
            res.sendRedirect("/");
        }

        Authentication auth = (Authentication) session.getAttribute("auth");

        String innerContent;

        if (auth == null) {
            // show login form.
            StringBuilder errors = new StringBuilder();
            Object tmpError = session.getAttribute("errors");
            if (tmpError != null) {
                errors.append("<div class=\"alert alert-danger\" role=\"alert\">");
                errors.append(tmpError);
                errors.append("</div>");
            }

            innerContent = formTemplate.replace("<!--ERRORS-->", errors);

            String username = "";
            Object tmpUsername = session.getAttribute("username");
            if (tmpUsername != null) {
                username = tmpUsername.toString();
            }

            innerContent = innerContent.replace("###username###", username);

        } else {
            // show logout button.
            String username = auth.getUsername();
            innerContent = logoutTemplate.replace("###username###", username);
        }

        res.setContentType("text/html;charset=UTF-8");
        res.getWriter().append(indexTemplate.replace("<!--INNERCONTENT-->", innerContent));
        res.getWriter().close();

        session.removeAttribute("errors");
        session.removeAttribute("username");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        logger.debug("Received incoming auth (POST) request ", req);

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        logger.debug("### " + username + ":" + password);

        HttpSession session = req.getSession();

        // do auth check...
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
        AuthenticationProviderImpl authProvider = new AuthenticationProviderImpl();
        Authentication auth = authProvider.authenticate(creds);

        if (auth == null) {
            session.setAttribute("errors", "Wrong credentials passed. Try again.");
            session.setAttribute("username", username);
            res.sendRedirect("/auth/login");
        } else {
            session.setAttribute("auth", auth);

            Object last_uri = session.getAttribute("last_uri");

            if (last_uri != null) {
                session.removeAttribute("last_uri");
                res.sendRedirect(last_uri.toString());
            } else {
                res.sendRedirect("/");
            }
            return;
        }
    }
}
