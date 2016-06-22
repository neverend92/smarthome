package org.eclipse.smarthome.ui.internal.auth;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthService {

    public static final String AUTH_ALIAS = "/auth";
    public static final String SERVLET_NAME = "login";

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    protected HttpService httpService;

    private BundleContext bundleContext;

    protected void activate(ComponentContext componentContext) {
        try {
            // get bundle context to access data in bundle.
            bundleContext = componentContext.getBundleContext();

            // empty properties hashtable
            Hashtable<String, String> props = new Hashtable<String, String>();

            // create new httpservlet for auth at "/auth/login"
            // use default HttpContext NOT AuthenticatedHttpContext
            httpService.registerServlet(AUTH_ALIAS + "/" + SERVLET_NAME, createServlet(), props,
                    httpService.createDefaultHttpContext());

            // register resources in web root (like css, js, etc.)
            httpService.registerResources(AUTH_ALIAS, "web", null);
            logger.info("Started auth at " + AUTH_ALIAS);
        } catch (NamespaceException | ServletException e) {
            logger.error("Error during auth startup: {}", e.getMessage());
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        httpService.unregister(AUTH_ALIAS);
        logger.info("Stopped auth");
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    protected HttpServlet createServlet() {
        return new AuthServlet(getTemplateFile("index.html"), getTemplateFile("form.html"),
                getTemplateFile("logout.html"));
    }

    private String getTemplateFile(String name) {
        String template;
        URL url = bundleContext.getBundle().getEntry("templates/" + name);
        if (url != null) {
            try {
                template = IOUtils.toString(url.openStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Cannot find " + name + " - failed to initialize auth servlet");
        }

        return template;
    }

}
