package org.eclipse.smarthome.ui.nodemgmt.internal;

import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.smarthome.core.auth.AuthenticatedHttpContext;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeMgmtService {

    public static final String MGMT_ALIAS = "/nodemgmt";
    public static final String SERVLET_NAME = "app";

    private static final Logger logger = LoggerFactory.getLogger(NodeMgmtService.class);

    protected HttpService httpService;

    private BundleContext bundleContext;

    protected void activate(ComponentContext componentContext) {
        try {
            // get bundle context to access data in bundle.
            bundleContext = componentContext.getBundleContext();

            // empty properties hashtable
            Hashtable<String, String> props = new Hashtable<String, String>();

            // create new httpservlet for auth at "/nodegmgt/app"
            // use AuthenticatedHttpContext
            httpService.registerServlet(MGMT_ALIAS + "/" + SERVLET_NAME, createServlet(), props,
                    new AuthenticatedHttpContext(componentContext.getBundleContext().getBundle()));

            // register resources in web root (like css, js, etc.)
            httpService.registerResources(MGMT_ALIAS, "web", null);
            logger.info("Started node management at " + MGMT_ALIAS);
        } catch (NamespaceException | ServletException e) {
            logger.error("Error during node management startup: {}", e.getMessage());
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        httpService.unregister(MGMT_ALIAS);
        logger.info("Stopped node management");
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    protected HttpServlet createServlet() {
        return new NodeMgmtServlet(bundleContext.getBundle());
    }

}
