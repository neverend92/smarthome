package org.eclipse.smarthome.core.auth;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.smarthome.core.internal.auth.AuthenticationProviderImpl;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticatedHttpContext implements HttpContext {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticatedHttpContext.class);

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
        return bundle.getResource(name);
    }

    public static String getAuthSessionId(HttpServletRequest req, HttpServletResponse res) {
        String authSessionId = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("AUTHSESSIONID")) {
                    authSessionId = cookie.getValue();
                    break;
                }
            }
        }
        if (authSessionId == null) {
            authSessionId = UUID.randomUUID().toString();
            Cookie cookie = new Cookie("AUTHSESSIONID", authSessionId);
            cookie.setPath("/");
            res.addCookie(cookie);
        }

        logger.debug("##### authSessionId: {} ({})", authSessionId, req.getRequestURL());
        return authSessionId;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osgi.service.http.HttpContext#handleSecurity(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public boolean handleSecurity(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String authSessionId = AuthenticatedHttpContext.getAuthSessionId(req, res);

        AuthenticatedSession authSession = AuthenticatedSession.getInstance();

        HttpSession session = req.getSession();

        // request url
        String reqUrl = req.getRequestURI();
        // request params
        String reqQuery = req.getQueryString();

        Authentication auth = authSession.get(authSessionId);

        // check for valid authentication
        if (auth == null) {
            // no valid authentication
            // redirect to login page.
            String lastUri = reqUrl;
            if (reqQuery != null) {
                lastUri += "?" + reqQuery;
            }
            session.setAttribute("last_uri", lastUri);
            // res.sendRedirect("/auth/login");
            res.setHeader("Location", "/auth/login");
            res.setStatus(HttpServletResponse.SC_FOUND);
            return false;
        }

        // there is a valid authentication
        // but check if user is allowed to see specific content.
        if (!AuthenticationProviderImpl.getInstace().isAllowed(auth, reqUrl)) {
            // res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            // res.sendRedirect("/");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // check if user accesses the rest docs.
        // if so, append the apikey (token).
        if (reqUrl.startsWith("/doc/index.html") && (reqQuery == null || reqQuery.indexOf(auth.getToken()) == -1)) {
            // res.sendRedirect("/doc/index.html?api_key=" + auth.getToken());
            res.setHeader("Location", "/doc/index.html?api_key=" + auth.getToken());
            res.setStatus(HttpServletResponse.SC_FOUND);
            return true;
        }

        // check if user accesses the paper ui.
        // if so, append the apikey (token).
        if (reqUrl.startsWith("/ui/index.html") && (reqQuery == null || reqQuery.indexOf(auth.getToken()) == -1)) {
            // res.sendRedirect("/ui/index.html?api_key=" + auth.getToken());
            res.setHeader("Location", "/ui/index.html?api_key=" + auth.getToken());
            res.setStatus(HttpServletResponse.SC_FOUND);
            return true;
        }

        Cookie tokenCookie = new Cookie("api_key", auth.getToken());
        tokenCookie.setPath("/");
        res.addCookie(tokenCookie);

        // set headers, so that browsers don't try to cache pages.
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        res.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        res.setHeader("Expires", "0"); // Proxies.

        return true;
    }

}
