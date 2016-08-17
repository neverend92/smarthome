package org.eclipse.smarthome.io.rest.internal.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.eclipse.smarthome.core.auth.Authentication;
import org.eclipse.smarthome.core.auth.AuthenticationProvider;
import org.eclipse.smarthome.core.internal.auth.AuthenticationProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@PreMatching
public class AuthFilter implements ContainerRequestFilter {

    private final transient Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    private final List<String> pathUnauthorized;

    public AuthFilter() {
        pathUnauthorized = new ArrayList<String>();
        // pathUnauthorized.add("swagger.json");
        pathUnauthorized.add("auth");
        logger.debug("### AuthFilter init...");
    }

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        UriInfo uriInfo = ctx.getUriInfo();

        // check if request should be protected.
        String path = uriInfo.getPath();
        if (pathUnauthorized.contains(path)) {
            // allow access.
            return;
        }

        // get url parameters.
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        if (queryParameters != null && queryParameters.containsKey("api_key")) {
            // check api_key.
            String apiKey = queryParameters.getFirst("api_key");
            AuthenticationProvider authProvider = AuthenticationProviderImpl.getInstace();
            Authentication auth = authProvider.authenticateToken(apiKey);
            if (authProvider.isAllowed(auth, "/rest/")) {
                return;
            }
            ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":{\"code\":403,\"message\":\"User is not allowed to access resource.\"}}")
                    .build());
        } else {
            ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":{\"code\":401,\"message\":\"Authentication needed to access resource.\"}}.")
                    .build());
        }
    }

}
