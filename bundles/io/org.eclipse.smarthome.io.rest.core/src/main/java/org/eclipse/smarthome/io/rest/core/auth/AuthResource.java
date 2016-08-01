package org.eclipse.smarthome.io.rest.core.auth;

import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.eclipse.smarthome.core.auth.Authentication;
import org.eclipse.smarthome.core.internal.auth.AuthenticationProviderImpl;
import org.eclipse.smarthome.io.rest.JSONResponse;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path(AuthResource.PATH_AUTH)
@Api(value = AuthResource.PATH_AUTH)
public class AuthResource implements RESTResource {

    /** The URI path to this resource */
    public static final String PATH_AUTH = "auth";

    private final Logger logger = LoggerFactory.getLogger(AuthResource.class);

    @Context
    UriInfo uriInfo;

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Login to get access token.", response = Authentication.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Wrong credentials") })
    public Response login(@HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) @ApiParam(value = "language") String language,
            @FormParam("username") @ApiParam(value = "Username", required = true) String username,
            @FormParam("password") @ApiParam(value = "Password", required = true) String password) {
        // do auth check...
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
        Authentication auth = AuthenticationProviderImpl.getInstace().authenticate(creds);
        if (auth == null) {
            return JSONResponse.createResponse(Status.FORBIDDEN, null, "Wrong credentials");
        }

        return JSONResponse.createResponse(Status.OK, auth, null);
    }

}
