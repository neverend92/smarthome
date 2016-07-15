package org.eclipse.smarthome.core.auth;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.smarthome.core.internal.auth.AuthenticationProviderImpl;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

public class AuthenticatedHttpContext implements HttpContext {

    private Bundle bundle;

    public AuthenticatedHttpContext(Bundle bundle) {
        this.bundle = bundle;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osgi.service.http.HttpContext#handleSecurity(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public boolean handleSecurity(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession();
        // set session timeout to 30min.
        if (session.isNew()) {
            session.setMaxInactiveInterval(30 * 60);
        }

        String reqUrl = req.getRequestURI();

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
        if (!AuthenticationProviderImpl.getInstace().isAllowed(auth, reqUrl)) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osgi.service.http.HttpContext#getResource(java.lang.String)
     */
    @Override
    public URL getResource(String name) {
        return this.bundle.getResource(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osgi.service.http.HttpContext#getMimeType(java.lang.String)
     */
    @Override
    public String getMimeType(String name) {
        return null;
    }

}
