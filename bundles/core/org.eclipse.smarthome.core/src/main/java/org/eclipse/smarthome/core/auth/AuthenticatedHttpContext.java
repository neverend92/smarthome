package org.eclipse.smarthome.core.auth;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.smarthome.core.internal.auth.PermissionRepositoryImpl;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticatedHttpContext implements HttpContext {

    private final Logger logger = LoggerFactory.getLogger(AuthenticatedHttpContext.class);

    private Bundle bundle;

    public AuthenticatedHttpContext(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public boolean handleSecurity(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession();
        // set session timeout to 30min.
        if (session.isNew()) {
            session.setMaxInactiveInterval(30 * 60);
        }

        String reqUrl = req.getRequestURI();

        logger.debug("### requested uri: {}", reqUrl);

        Authentication auth = (Authentication) session.getAttribute("auth");

        // check for valid authentication
        if (auth == null) {
            // no valid authentication
            // redirect to login page.
            session.setAttribute("last_uri", reqUrl);
            res.sendRedirect("/auth/login");
            return false;
        }

        // there is a valid authentication
        // but check if user is allowed to see specific content.
        PermissionRepository repo = new PermissionRepositoryImpl();
        Permission permission = repo.get(reqUrl);

        if (permission == null) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        if (permission.getRoles().length > 0) {
            if (!AuthUtils.hasRoleMatch(permission.getRoles(), auth.getRoles())) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
        }

        return true;
    }

    @Override
    public URL getResource(String name) {
        URL url = this.bundle.getResource(name);
        logger.debug("### Requested Resource: {}, Resource URL: {}", name, url);
        return url;
    }

    @Override
    public String getMimeType(String name) {
        logger.debug("### Requested Resource: {}, Mimetype: {}", name, null);
        return null;
    }

}
