package org.eclipse.smarthome.io.rest.core.compat1x.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.io.rest.JSONResponse;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path(Compat1xConfigResource.PATH_COMPAT1X_CONFIG)
@Api(value = Compat1xConfigResource.PATH_COMPAT1X_CONFIG)
public class Compat1xConfigResource implements RESTResource {

    /** The URI path to this resource */
    public static final String PATH_COMPAT1X_CONFIG = "compat1x_config";

    /** The default services configuration directory name */
    final static protected String SERVICES_FOLDER = "services";

    /** The program argument name for setting the main config directory path */
    final static protected String CONFIG_DIR_PROG_ARGUMENT = "smarthome.configdir";

    /** The default main configuration directory name */
    final static protected String DEFAULT_CONFIG_FOLDER = "conf";

    protected final Logger logger = LoggerFactory.getLogger(Compat1xConfigResource.class);

    @Context
    UriInfo uriInfo;

    @GET
    @Path("mqtt")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Get current mqtt config.", response = Compat1xMqttConfigDTO.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getMQTTConfig() {
        Compat1xMqttConfigDTO config = new Compat1xMqttConfigDTO();
        List<String> contentLines = this.getFileContent("mqtt.cfg");
        if (!config.parseObject(contentLines)) {
            return JSONResponse.createResponse(Status.BAD_REQUEST, null, "Could not find config.");
        }

        return JSONResponse.createResponse(Status.OK, config, null);
    }

    @GET
    @Path("mqtt-eventbus")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Get current mqtt eventbus config.", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getMQTTEventbusConfig() {
        Compat1xMqttEventbusConfigDTO config = new Compat1xMqttEventbusConfigDTO();
        List<String> contentLines = this.getFileContent("mqtt-eventbus.cfg");
        if (!config.parseObject(contentLines)) {
            return JSONResponse.createResponse(Status.BAD_REQUEST, null, "Could not find config.");
        }

        return JSONResponse.createResponse(Status.OK, config, null);
    }

    @GET
    @Path("aleoncean")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Get current aleoncean config.", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getAleonceanConfig() {
        Compat1xAleonceanConfigDTO config = new Compat1xAleonceanConfigDTO();
        List<String> contentLines = this.getFileContent("aleoncean.cfg");
        if (!config.parseObject(contentLines)) {
            return JSONResponse.createResponse(Status.BAD_REQUEST, null, "Could not find config.");
        }

        return JSONResponse.createResponse(Status.OK, config, null);
    }

    @POST
    @Path("mqtt")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Set current mqtt config.", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response setMQTTConfig(@FormParam("broker") @ApiParam(value = "Broker", required = true) String broker,
            @FormParam("host") @ApiParam(value = "Broker Host", required = true) String host,
            @FormParam("clientId") @ApiParam(value = "Client ID", required = true) String clientId,
            @FormParam("username") @ApiParam(value = "Username", required = true) String username,
            @FormParam("password") @ApiParam(value = "Password", required = true) String password) {
        Compat1xMqttConfigDTO config = new Compat1xMqttConfigDTO();
        config.setBroker(broker);
        config.setClientId(clientId);
        config.setHost(host);
        config.setPassword(password);
        config.setUsername(username);

        if (this.setFileContent("mqtt.cfg", config.toString())) {
            return JSONResponse.createResponse(Status.OK, "ok", null);
        }

        return JSONResponse.createResponse(Status.OK, "failed", null);
    }

    @POST
    @Path("mqtt-eventbus")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Set current mqtt eventbus config.", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response setMQTTEventbusConfig(
            @FormParam("broker") @ApiParam(value = "Broker", required = true) String broker,
            @FormParam("statePublishTopic") @ApiParam(value = "statePublishTopic", required = true) String statePublishTopic,
            @FormParam("commandSubscribeTopic") @ApiParam(value = "commandSubscribeTopic", required = true) String commandSubscribeTopic) {
        Compat1xMqttEventbusConfigDTO config = new Compat1xMqttEventbusConfigDTO();
        config.setBroker(broker);
        config.setStatePublishTopic(statePublishTopic);
        config.setCommandSubscribeTopic(commandSubscribeTopic);

        if (this.setFileContent("mqtt-eventbus.cfg", config.toString())) {
            return JSONResponse.createResponse(Status.OK, "ok", null);
        }

        return JSONResponse.createResponse(Status.OK, "failed", null);
    }

    @POST
    @Path("aleoncean")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Set current aleoncean config.", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response setAleonceanConfig(@FormParam("port") @ApiParam(value = "Port", required = true) String port) {
        Compat1xAleonceanConfigDTO config = new Compat1xAleonceanConfigDTO();
        config.setPort(port);

        if (this.setFileContent("aleoncean.cfg", config.toString())) {
            return JSONResponse.createResponse(Status.OK, "ok", null);
        }

        return JSONResponse.createResponse(Status.OK, "failed", null);
    }

    private List<String> getFileContent(String filename) {
        File configFile = this.getConfigFile(filename);
        if (configFile == null) {
            return null;
        }

        try {
            List<String> lines = IOUtils.readLines(new FileInputStream(configFile));
            return lines;
        } catch (IOException e) {
            e.printStackTrace();
            logger.debug(e.getMessage());
        }

        return null;
    }

    private boolean setFileContent(String filename, String content) {
        File configFile = this.getConfigFile(filename);
        if (configFile == null) {
            return false;
        }
        try {
            IOUtils.write(content, new FileOutputStream(configFile));
        } catch (IOException e) {
            e.printStackTrace();
            logger.debug(e.getMessage());
        }
        return true;
    }

    /**
     * Gets the config path.
     *
     * @return
     */
    private String getSourcePath() {
        String progArg = System.getProperty(CONFIG_DIR_PROG_ARGUMENT);
        String path;
        if (progArg != null) {
            path = progArg;
        } else {
            path = DEFAULT_CONFIG_FOLDER;
        }

        return path + File.separator + SERVICES_FOLDER;
    }

    /**
     * Gets the config file from filename.
     *
     * @param filename
     * @return
     */
    private File getConfigFile(String filename) {
        File dir = new File(this.getSourcePath());
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (!file.isDirectory() && file.getName().equals(filename)) {
                    return file;
                }
            }
        } else {
            logger.debug("Config folder '{}' does not exist.", dir.toString());
        }

        // file doesnt exist. create it!
        String path = this.getSourcePath() + File.separator + filename;
        File newFile = new File(path);
        try {
            IOUtils.write("", new FileOutputStream(newFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile;
    }

}
