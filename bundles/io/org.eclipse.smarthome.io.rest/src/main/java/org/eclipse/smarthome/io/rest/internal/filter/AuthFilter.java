package org.eclipse.smarthome.io.rest.internal.filter;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@PreMatching
public class AuthFilter implements ContainerRequestFilter {

    private final transient Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    public AuthFilter() {
        logger.debug("### AuthFilter init...");
    }

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        logger.debug("### doing the AUTH filter...");
    }

}
