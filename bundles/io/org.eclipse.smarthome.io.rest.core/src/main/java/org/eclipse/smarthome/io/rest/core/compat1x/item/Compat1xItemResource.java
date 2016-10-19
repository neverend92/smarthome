package org.eclipse.smarthome.io.rest.core.compat1x.item;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.ws.rs.FormParam;
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

@Path(Compat1xItemResource.PATH_COMPAT1X_CONFIG)
@Api(value = Compat1xItemResource.PATH_COMPAT1X_CONFIG)
public class Compat1xItemResource implements RESTResource {

    /** The URI path to this resource */
    public static final String PATH_COMPAT1X_CONFIG = "compat1x_item";

    /** The default services configuration directory name */
    final static protected String SERVICES_FOLDER = "items";

    /** The program argument name for setting the main config directory path */
    final static protected String CONFIG_DIR_PROG_ARGUMENT = "smarthome.configdir";

    /** The default main configuration directory name */
    final static protected String DEFAULT_CONFIG_FOLDER = "conf";

    protected final Logger logger = LoggerFactory.getLogger(Compat1xItemResource.class);

    @Context
    UriInfo uriInfo;

    @POST
    @Path("aleoncean")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Creates an aleoncean item.", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response createAleonceanItem(
            @FormParam("itemName") @ApiParam(value = "Item Name", required = true) String itemName,
            @FormParam("itemType") @ApiParam(value = "Item Type", required = true) String itemType,
            @FormParam("itemDesc") @ApiParam(value = "Item Description", required = true) String itemDesc,
            @FormParam("itemIcon") @ApiParam(value = "Item Icon", required = false) String itemIcon,
            @FormParam("deviceRemoteId") @ApiParam(value = "Device Remote ID", required = true) String deviceRemoteId,
            @FormParam("deviceType") @ApiParam(value = "Device Type", required = true) String deviceType,
            @FormParam("deviceParameter") @ApiParam(value = "Device Parameter", required = true) String deviceParamter) {
        // create item config string.
        if (itemIcon != null && !itemIcon.equals("")) {
            itemIcon = "<" + itemIcon + ">";
        } else {
            itemIcon = "";
        }
        String itemConfig = itemType + " " + itemName + " \"" + itemDesc + "\" " + itemIcon + " {aleoncean=\"REMOTEID="
                + deviceRemoteId + ",TYPE=" + deviceType + ",PARAMETER=" + deviceParamter + "\"}";

        String filename = "aleoncean.items";
        List<String> content = this.getFileContent(filename);
        content.add(itemConfig);

        if (this.setFileContent(filename, content)) {
            return JSONResponse.createResponse(Status.OK, "ok", null);
        }

        return JSONResponse.createResponse(Status.OK, "failed", null);
    }

    @POST
    @Path("mqtt")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Creates an mqtt item.", response = String.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response createMqttItem(
            @FormParam("itemName") @ApiParam(value = "Item Name", required = true) String itemName,
            @FormParam("itemType") @ApiParam(value = "Item Type", required = true) String itemType,
            @FormParam("itemDesc") @ApiParam(value = "Item Description", required = true) String itemDesc,
            @FormParam("itemIcon") @ApiParam(value = "Item Icon", required = false) String itemIcon,
            @FormParam("mqttBroker") @ApiParam(value = "MQTT Broker", required = true) String mqttBroker,
            @FormParam("mqttNodeName") @ApiParam(value = "MQTT Node Name", required = true) String mqttNodeName,
            @FormParam("nodeItemName") @ApiParam(value = "Node Item Name", required = true) String nodeItemName) {
        // create item config string.
        if (itemIcon != null && !itemIcon.equals("")) {
            itemIcon = "<" + itemIcon + ">";
        } else {
            itemIcon = "";
        }
        String mqttStateTopic = mqttBroker + ":/" + mqttNodeName + "/out/" + nodeItemName + "/state:state:default";
        String mqttCommandTopic = mqttBroker + ":/" + mqttNodeName + "/in/" + nodeItemName
                + "/command:command:*:default";

        String itemConfig = itemType + " " + itemName + " \"" + itemDesc + "\" " + itemIcon + " { mqtt=\"<["
                + mqttStateTopic + "], >[" + mqttCommandTopic + "]\" }";

        String filename = "mqtt.items";
        List<String> content = this.getFileContent(filename);
        content.add(itemConfig);

        if (this.setFileContent(filename, content)) {
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

    private boolean setFileContent(String filename, List<String> content) {
        File configFile = this.getConfigFile(filename);
        if (configFile == null) {
            return false;
        }
        try {
            IOUtils.writeLines(content, null, new FileOutputStream(configFile));
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
