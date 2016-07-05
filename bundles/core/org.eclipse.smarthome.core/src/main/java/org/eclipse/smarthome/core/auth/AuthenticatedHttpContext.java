package org.eclipse.smarthome.core.auth;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticatedHttpContext implements HttpContext {

    private final Logger logger = LoggerFactory.getLogger(AuthenticatedHttpContext.class);

    public AuthenticatedHttpContext() {
    }

    @Override
    public boolean handleSecurity(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession();
        // set session timeout to 30min.
        if (session.isNew()) {
            session.setMaxInactiveInterval(30 * 60);
        }

        Authentication auth = (Authentication) session.getAttribute("auth");

        if (auth == null) {
            // redirect to login page.
            session.setAttribute("last_uri", req.getRequestURI());
            res.sendRedirect("/auth/login");
            return false;
        }

        return true;
    }

    @Override
    public URL getResource(String name) {
        return null;
    }

    @Override
    public String getMimeType(String name) {
        return null;
    }

}
