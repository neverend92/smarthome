/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.paper.internal;

import org.eclipse.smarthome.core.auth.AuthenticatedHttpContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This component registers the Paper UI Webapp.
 *
 * @author Dennis Nobel - Initial contribution
 */
public class PaperUIApp {

    public static final String WEBAPP_ALIAS = "/paperui";
    private final Logger logger = LoggerFactory.getLogger(PaperUIApp.class);

    protected HttpService httpService;

    protected void activate(ComponentContext componentContext) {

        try {
            AuthenticatedHttpContext authHttpContext = new AuthenticatedHttpContext(
                    componentContext.getBundleContext().getBundle());
            httpService.registerResources(WEBAPP_ALIAS, "web", authHttpContext);
            logger.info("Started Paper UI at " + WEBAPP_ALIAS);
        } catch (NamespaceException e) {
            logger.error("Error during servlet startup", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        httpService.unregister(WEBAPP_ALIAS);
        logger.info("Stopped Paper UI");
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

}
