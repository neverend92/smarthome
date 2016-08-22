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

    /**
     * the osgi bundle to access resources.
     */
    private Bundle bundle;

    /**
     * Creates a new {@code AuthenticatedHttpContext}
     *
     * @param bundle
     */
    public AuthenticatedHttpContext(Bundle bundle) {
        this.bundle = bundle;
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

        // request url
        String reqUrl = req.getRequestURI();
        // request params
        String reqQuery = req.getQueryString();

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
            return false;
        }

        // check if user accesses the rest docs.
        // if so, append the apikey (token).
        if (reqUrl.startsWith("/doc/index.html") && (reqQuery == null || reqQuery.indexOf(auth.getToken()) == -1)) {
            res.sendRedirect("/doc/index.html?api_key=" + auth.getToken());
            return true;
        }

        // check if user accesses the paper ui.
        // if so, append the apikey (token).
        if (reqUrl.startsWith("/ui/index.html") && (reqQuery == null || reqQuery.indexOf(auth.getToken()) == -1)) {
            res.sendRedirect("/ui/index.html?api_key=" + auth.getToken());
            return true;
        }

        // set headers, so that browsers don't try to cache pages.
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        res.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        res.setHeader("Expires", "0"); // Proxies.

        return true;
    }

}
