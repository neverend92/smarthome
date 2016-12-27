package org.eclipse.smarthome.ui.internal.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.eclipse.smarthome.core.auth.AuthenticatedHttpContext;
import org.eclipse.smarthome.core.auth.AuthenticatedSession;
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

        AuthenticatedSession authSession = AuthenticatedSession.getInstance();
        String authSessionId = AuthenticatedHttpContext.getAuthSessionId(req, res);

        // check if logout action was triggered.
        String action = req.getParameter("action");
        if (action != null && action.equals("logout")) {
            authSession.remove(authSessionId);
            logger.debug("Logout User");
            res.sendRedirect("/");
            return;
        }

        // check for authenticated user.
        Authentication auth = authSession.get(authSessionId);

        HttpSession session = req.getSession();

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

    public static String getLastUri(HttpServletRequest req, HttpServletResponse res) {
        String lastUri = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("last_uri")) {
                    lastUri = cookie.getValue();
                    break;
                }
            }
        }

        return lastUri;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        logger.debug("Received incoming auth (POST) request {}", req);

        HttpSession session = req.getSession();

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        AuthenticatedSession authSession = AuthenticatedSession.getInstance();
        String authSessionId = AuthenticatedHttpContext.getAuthSessionId(req, res);

        // do auth check...
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
        Authentication auth = AuthenticationProviderImpl.getInstace().authenticate(creds);

        if (auth == null) {
            session.setAttribute("errors", "Wrong credentials passed. Try again.");
            session.setAttribute("username", username);
            res.sendRedirect("/auth/login");
            return;
        } else {
            authSession.put(authSessionId, auth);

            String last_uri = getLastUri(req, res);

            // if page was set, redirect to this page.
            // else redirect to default root page.
            if (last_uri != null) {
                // session.removeAttribute("last_uri");
                res.sendRedirect(last_uri);
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
