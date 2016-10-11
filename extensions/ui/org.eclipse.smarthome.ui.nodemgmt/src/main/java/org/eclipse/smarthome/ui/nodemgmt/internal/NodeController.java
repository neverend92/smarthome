package org.eclipse.smarthome.ui.nodemgmt.internal;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.eclipse.smarthome.ui.mgmt.internal.MgmtController;
import org.eclipse.smarthome.ui.mgmt.internal.MgmtServlet;
import org.eclipse.smarthome.ui.nodemgmt.Node;
import org.eclipse.smarthome.ui.nodemgmt.api.AuthApiResponse;
import org.eclipse.smarthome.ui.nodemgmt.api.ExtensionApiResponse;
import org.eclipse.smarthome.ui.nodemgmt.api.ItemsApiResponse;
import org.eclipse.smarthome.ui.nodemgmt.api.VersionApiResponse;

import com.google.gson.Gson;

public class NodeController extends MgmtController<Node> {

    private String nodeStatus = "";
    private String nodeItems = "";
    private String nodeExtensions = "";
    private String apiKey = "";

    /**
     * NodeController
     *
     * @param urlAction
     * @param urlId
     * @param servlet
     */
    public NodeController(String urlAction, String urlId, MgmtServlet servlet) {
        super(urlAction, urlId, servlet);

        this.setRepository(NodeRepositoryImpl.getInstance());
        this.setEntityName("node");
        this.setFieldName("ip");

        this.getAttributes().add("ip");
        this.getAttributes().add("description");
        this.getAttributes().add("credentials");
    }

    /**
     * Performs the auth check
     * Checks for availability of node and for correct credentials.
     *
     * @param node
     * @return String statuslines.
     * @throws IOException
     */
    private String doAuthCheck(Node node) throws IOException {
        // build url.
        String url = node.getIP() + "/rest/auth";

        // load credentials.
        UsernamePasswordCredentials credentials = this.parseCredentials(node.getCredentials());
        String urlParams = "username=" + credentials.getUserName() + "&password=" + credentials.getPassword();

        return doRequest(url, "POST", urlParams);
    }

    /**
     * Checks a single extension, if it is installed.
     *
     * @param node
     * @param apiKey
     * @param extensionId
     * @return String statuslines.
     * @throws IOException
     */
    private String doCheckExtension(Node node, String extensionId) throws IOException {
        // build url.
        String url = node.getIP() + "/rest/extensions/" + extensionId + "?api_key=" + this.apiKey;

        return doRequest(url, "GET", null);
    }

    /**
     * Checks several extension, if they are installed.
     * If not installation is performed.
     *
     * @param node
     * @param apiKey
     * @param extensions
     */
    private void doCheckExtensions(Node node, String[] extensions) {
        Gson gson = new Gson();

        for (String extensionId : extensions) {
            // doCheckExtension
            String ret = "";
            try {
                ret = this.doCheckExtension(node, extensionId);
            } catch (IOException e) {
                this.nodeStatus += this.getConsoleStatusLine("error", "could not check extension " + extensionId);
                continue;
            }
            if (ret == null) {
                this.nodeStatus += this.getConsoleStatusLine("error", "could not check extension " + extensionId);
                this.nodeExtensions += this.getExtensionInstallForm(false, extensionId, "no", "", node.getId());
                continue;
            }
            ExtensionApiResponse extensionApiResponse = gson.fromJson(ret, ExtensionApiResponse.class);
            if (extensionApiResponse == null) {
                this.nodeStatus += this.getConsoleStatusLine("error", "could not check extension " + extensionId);
                continue;
            }
            if (extensionApiResponse.isInstalled()) {
                this.nodeStatus += this.getConsoleStatusLine("ok", "extension " + extensionId + " is installed.");
                this.nodeExtensions += this.getExtensionInstallForm(false, extensionId, "yes",
                        extensionApiResponse.getVersion(), node.getId());
            } else {
                this.nodeStatus += this.getConsoleStatusLine("warn", "extension " + extensionId + " is NOT installed.");
                this.nodeExtensions += this.getExtensionInstallForm(false, extensionId, "no",
                        extensionApiResponse.getVersion(), node.getId());
            }

        }
    }

    /**
     * Retrieves the items from the node.
     *
     * @param node
     * @param apiKey
     * @return String statuslines.
     * @throws IOException
     */
    private String doGetItems(Node node) throws IOException {
        // build url.
        String url = node.getIP() + "/rest/items?recursive=false&api_key=" + this.apiKey;

        return doRequest(url, "GET", null);
    }

    /**
     * Installes a single extension.
     *
     * @param node
     * @param apiKey
     * @param extensionId
     * @return String statuslines.
     * @throws IOException
     */
    private String doInstallExtension(Node node, String extensionId, String installType, String apiKey)
            throws IOException {
        // build url.
        if (!installType.equals("install") && !installType.equals("uninstall")) {
            return null;
        }

        String url = node.getIP() + "/rest/extensions/" + extensionId + "/" + installType + "?api_key=" + apiKey;

        logger.debug("### INSTALL_URL: {}", url);
        return doRequest(url, "POST", null);
    }

    /**
     * Performes a request to the REST-API.
     *
     * @param url
     * @param method
     * @param urlParams
     * @return String response
     * @throws IOException
     */
    private String doRequest(String url, String method, String urlParams) throws IOException {
        // init connection object.
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        // set timeout to 0.5s
        con.setConnectTimeout(500);
        // set method to post.
        con.setRequestMethod(method);
        // enable output.
        con.setDoOutput(true);

        if (method.equals("POST") && urlParams != null) {
            // create stream
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            // add post params.
            wr.writeBytes(urlParams);
            // clear stream
            wr.flush();
            // close stream.
            wr.close();
        }

        int responseCode = con.getResponseCode();
        // check response code.
        if (responseCode != 200) {
            return null;
        }

        // read request repsonse.
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = br.readLine()) != null) {
            response.append(inputLine);
        }
        br.close();
        con.disconnect();

        return response.toString();
    }

    /**
     * Performs the version check of OH/ESH
     *
     * @param node
     * @param apiKey
     * @return String statuslines.
     * @throws IOException
     */
    private String doVersionCheck(Node node) throws IOException {
        // build url.
        String url = node.getIP() + "/rest?api_key=" + this.apiKey;

        return doRequest(url, "GET", null);
    }

    /**
     * Creates a statusline for node status outputs.
     *
     * @param type
     * @param msg
     * @return String statusline
     */
    private String getConsoleStatusLine(String type, String msg) {
        String ret = "<span class=\"text-";

        switch (type) {
            case "ok":
                ret += "success";
                break;
            case "error":
                ret += "danger";
                break;
            case "warn":
                ret += "warning";
                break;
            case "info":
            default:
                ret += "info";
                break;
        }

        ret += "\">[";
        ret += type.toUpperCase();
        ret += "]&nbsp;";

        for (int i = type.length(); i < 5; i++) {
            ret += "&nbsp;";
        }

        ret += msg;
        ret += "</span><br>";

        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.ui.mgmt.internal.MgmtController#getEdit()
     */
    @Override
    public String getEdit() {
        String content = super.getEdit();

        Node node = this.getRepository().get(this.getUrlId());
        if (node == null) {
            return null;
        }

        String template = this.getServlet().getTemplateFile(this.getPlural(this.getEntityName()) + "/edit-extra");

        this.loadNodeStatus(node);
        template = template.replace("###NODE_STATUS###", this.nodeStatus);
        template = template.replace("###NODE_ITEMS###", this.nodeItems);
        this.nodeExtensions += this.getExtensionInstallForm(true, "", "", "", node.getId());
        template = template.replace("###NODE_EXTENSIONS###", this.nodeExtensions);

        content += template;
        return content;
    }

    private String getExtensionInstallForm(boolean flagInput, String name, String status, String version,
            String nodeId) {
        StringBuilder sb = new StringBuilder();

        String formId = "extension-new";
        sb.append("<tr>");

        if (!flagInput) {
            formId = "extension-" + name;
            sb.append("<form id=\"" + formId + "\"method=\"POST\" action=\"" + this.getServlet().getBaseUrl()
                    + "?controller=" + this.getPlural(this.getEntityName()) + "&action=installExtension&id=" + nodeId
                    + "\">");
            sb.append("<input type=\"hidden\" name=\"" + this.getFieldName() + "\" value=\"" + this.getUrlId() + "\">");
            sb.append("<input type=\"hidden\" name=\"extension-name\" value=\"" + name + "\">");
            sb.append("<input type=\"hidden\" name=\"apiKey\" value=\"" + this.apiKey + "\">");

            String installType = "uninstall";
            if (status.equals("no")) {
                installType = "install";
            }
            sb.append("<input type=\"hidden\" name=\"install-type\" value=\"" + installType + "\">");
            sb.append("</form></td>");
            sb.append("<td>" + name + "</td>");
            sb.append("<td>" + status + "</td>");
        } else {
            sb.append("<td><form id=\"" + formId + "\"method=\"POST\" action=\"" + this.getServlet().getBaseUrl()
                    + "?controller=" + this.getPlural(this.getEntityName()) + "&action=installExtension&id=" + nodeId
                    + "\">");
            sb.append(
                    "<input type=\"text\" class=\"form-control\" name=\"extension-name\" placeholder=\"binding-xyz\">");
            sb.append("<input type=\"hidden\" name=\"install-type\" value=\"install\">");
            sb.append("<input type=\"hidden\" name=\"" + this.getFieldName() + "\" value=\"" + this.getUrlId() + "\">");
            sb.append("<input type=\"hidden\" name=\"apiKey\" value=\"" + this.apiKey + "\">");
            sb.append("<input type=\"hidden\" name=\"type\" value=\"add\">");
            sb.append("</form></td>");
            sb.append("<td>&nbsp;</td>");
        }

        sb.append("<td>");
        String buttonClass = "btn-danger";
        String buttonText = "Uninstall";
        if (flagInput || status.equals("no")) {
            buttonClass = "btn-success";
            buttonText = "Install";
        }
        sb.append("<a class=\"btn " + buttonClass + "\" href=\"#\" onclick=\"$('#" + formId
                + "').submit();return false;\">" + buttonText + "</a>");

        if (!flagInput && status.equals("no")) {
            sb.append("<form class=\"form-inline\" method=\"POST\" id=\"" + formId + "-delete\" action=\""
                    + this.getServlet().getBaseUrl() + "?controller=" + this.getPlural(this.getEntityName())
                    + "&action=deleteExtension&id=" + nodeId + "\">");
            sb.append("<input type=\"hidden\" name=\"" + this.getFieldName() + "\" value=\"" + this.getUrlId() + "\">");
            sb.append("<input type=\"hidden\" name=\"extension-name\" value=\"" + name + "\">");
            sb.append("<a class=\"btn btn-danger\" href=\"#\" onclick=\"$('#" + formId
                    + "-delete').submit();return false;\">Delete</a>");
            sb.append("</form>");
        }
        sb.append("</td>");

        if (!flagInput) {
            if (!version.equals("")) {
                sb.append("<td><a class=\"btn btn-warning\" href=\"#\" onclick=\"$('#" + formId
                        + "-details').toggleClass('hidden');return false;\">+</a></td>");
            } else {
                sb.append("<td>&nbsp;</td>");
            }
        } else {
            sb.append("<td>&nbsp;</td>");
        }

        sb.append("</tr>");

        // check version of binding, if lower than 2.0.0 configuration via rest api is not possible.
        if (!version.equals("")) {
            sb.append("<tr id=\"" + formId + "-details\" class=\"hidden\">");
            sb.append("<td colspan=\"4\">");
            if (!version.equals("2.0.0.SNAPSHOT") && !version.equals("0.9.0.SNAPSHOT")) {
                sb.append("Config: Manual config for extension " + name + " needed.");
            } else {
                sb.append("Config: Automatic Config not implemented yet.");
            }
            sb.append("</td>");
            sb.append("</tr>");
        }

        return sb.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.ui.mgmt.internal.MgmtController#getModel()
     */
    @Override
    public Node getModel() {
        return new NodeImpl();
    }

    private boolean handleExtensionChange(HttpServletRequest req, String type) {
        String extensionId = req.getParameter("extension-name");
        if (extensionId == null || extensionId.isEmpty()) {
            this.getSession().setAttribute("errors", "Extension should not be empty.");
            return false;
        }

        Node node = this.getRepository().get(this.getUrlId());
        if (node == null) {
            this.getSession().setAttribute("errors", "Could not " + type + " extension.");
            return false;
        }

        String[] tmpExtensions = node.getExtensions();
        // build condition:
        // add: role should NOT exists yet.
        // delete: role must NOT exists yet.
        boolean condition = false;
        if (type.equals("add")) {
            condition = !Arrays.asList(tmpExtensions).contains(extensionId);
        } else if (type.equals("delete")) {
            condition = Arrays.asList(tmpExtensions).contains(extensionId);
        }

        // check condition.
        if (condition) {
            String[] newExtensions;
            if (type.equals("add")) {
                // add role to list.
                // add one spot in the array.
                newExtensions = new String[tmpExtensions.length + 1];
                // copy old roles into new array.
                for (int i = 0; i < newExtensions.length; i++) {
                    if (i < tmpExtensions.length) {
                        // copy old role.
                        newExtensions[i] = tmpExtensions[i];
                    } else {
                        // add new role
                        newExtensions[i] = extensionId;
                    }
                }
            } else if (type.equals("delete")) {
                // delete role from list.
                // make array smaller.
                newExtensions = new String[tmpExtensions.length - 1];
                // copy old roles into new array.
                for (int i = 0, j = 0; j < tmpExtensions.length; i++, j++) {
                    if (!tmpExtensions[j].equals(extensionId)) {
                        // copy old role.
                        newExtensions[i] = tmpExtensions[j];
                    } else {
                        // delete role by skipping it.
                        i--;
                    }
                }
            } else {
                // throw error.
                this.getSession().setAttribute("errors", "Could not " + type + " extension.");
                return false;
            }

            // set new roles array to object.
            node.setExtensions(newExtensions);
        } else {
            // throw error.
            this.getSession().setAttribute("errors", "Could not " + type + " extension.");
            return false;
        }

        // update in repository.
        if (!this.getRepository().update(node.getId(), node, true)) {
            this.getSession().setAttribute("errors", "Could not " + type + " extension.");
            return false;
        } else {
            this.getSession().setAttribute("success", "Successfully " + type + "ed extension.");
            return true;
        }
    }

    /**
     * Loads the Node Status
     *
     * @param node
     */
    private void loadNodeStatus(Node node) {
        String ret = null;
        Gson gson = new Gson();

        // doAuthCheck
        try {
            ret = this.doAuthCheck(node);
        } catch (IOException e) {
            this.nodeStatus += this.getConsoleStatusLine("error", "node is offline");
            return;
        }

        this.nodeStatus += this.getConsoleStatusLine("ok", "node is online");

        if (ret == null) {
            this.nodeStatus += this.getConsoleStatusLine("error", "credentials are invalid");
            return;
        }

        AuthApiResponse authApiResponse = gson.fromJson(ret, AuthApiResponse.class);
        if (authApiResponse == null) {
            this.nodeStatus += this.getConsoleStatusLine("error", "credentials are invalid");
            return;
        }
        this.apiKey = authApiResponse.getToken();
        this.nodeStatus += this.getConsoleStatusLine("ok", "credentials are valid");

        // doVersionCheck
        try {
            ret = this.doVersionCheck(node);
        } catch (IOException e) {
            this.nodeStatus += this.getConsoleStatusLine("error", "could not obtain version");
            return;
        }
        if (ret == null) {
            this.nodeStatus += this.getConsoleStatusLine("error", "could not obtain version");
            return;
        }

        VersionApiResponse versionApiResponse = gson.fromJson(ret, VersionApiResponse.class);
        if (versionApiResponse == null) {
            this.nodeStatus += this.getConsoleStatusLine("error", "could not obtain version");
            return;
        }
        String version = versionApiResponse.getVersion();
        this.nodeStatus += this.getConsoleStatusLine("ok", "node uses Eclipse Smarthome " + version);

        // doGetItems
        try {
            ret = this.doGetItems(node);
        } catch (IOException e) {
            this.nodeStatus += this.getConsoleStatusLine("error", "could not obtain items");
            return;
        }
        if (ret == null) {
            this.nodeStatus += this.getConsoleStatusLine("error", "could not obtain items");
            return;
        }

        ItemsApiResponse[] itemsApiResponses = gson.fromJson(ret, ItemsApiResponse[].class);
        if (itemsApiResponses == null) {
            this.nodeStatus += this.getConsoleStatusLine("error", "could not obtain items");
            return;
        }
        for (ItemsApiResponse item : itemsApiResponses) {
            String tmp = "<tr>";
            tmp += "<td><a href=\"" + item.getLink() + "?api_key=" + apiKey + "\" target=\"_blank\">" + item.getName()
                    + "</a></td>";
            tmp += "<td>" + this.formatAttribute(item.getLabel()) + "</td>";
            tmp += "<td>" + this.formatAttribute(item.getCategory()) + "</td>";
            tmp += "<td>" + this.formatAttribute(item.getType()) + "</td>";
            tmp += "<td><a class=\"btn btn-success\" href=\"#\">Add Item</a></td>";
            tmp += "</tr>";
            this.nodeItems += tmp;
        }
        this.nodeStatus += this.getConsoleStatusLine("ok", "recieved items");

        // String[] extensions = { "binding-mqtt", "binding-enocean", "binding0" };

        doCheckExtensions(node, node.getExtensions());
    }

    private String formatAttribute(String value) {
        if (value != null) {
            return value;
        }
        return "";
    }

    /**
     * Parses String to UsernamePasswordCredentials.
     *
     * @param credentials
     * @return UsernamePasswordCredentials
     */
    private UsernamePasswordCredentials parseCredentials(String credentials) {
        String[] a_credentials = new String[2];
        a_credentials = credentials.split(":");
        return new UsernamePasswordCredentials(a_credentials[0], a_credentials[1]);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.ui.mgmt.internal.MgmtController#postContent(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public boolean postContent(HttpServletRequest req) {
        boolean ret = super.postContent(req);
        if (ret != false) {
            return ret;
        }

        if (this.getUrlAction().equals("installExtension")) {
            return this.postInstallExtension(req);
        } else if (this.getUrlAction().equals("deleteExtension")) {
            return this.postDeleteExtension(req);
        }

        return false;
    }

    public boolean postDeleteExtension(HttpServletRequest req) {
        return this.handleExtensionChange(req, "delete");
    }

    public boolean postInstallExtension(HttpServletRequest req) {
        // get role to add from request.
        String extensionId = req.getParameter("extension-name");
        String installType = req.getParameter("install-type");
        String type = req.getParameter("type");
        String apiKey = req.getParameter("apiKey");

        Node node = this.getRepository().get(this.getUrlId());
        if (node == null) {
            this.getSession().setAttribute("errors", "Could not " + type + " extension.");
            return false;
        }

        // add extension to node.
        if ("add".equals(type)) {
            if (!this.handleExtensionChange(req, "add")) {
                return false;
            }
        }

        String ret = null;
        // do install.
        try {
            ret = this.doInstallExtension(node, extensionId, installType, apiKey);
        } catch (IOException e) {
            this.getSession().setAttribute("errors", "Could not " + installType + " extension " + extensionId);
            return false;
        }

        if (ret == null) {
            this.getSession().setAttribute("errors", "Could not " + installType + " extension " + extensionId);
            return false;
        }

        // sleep to ensure that extension is installed at next reload.
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            // do nothing if sleep fails.
        }

        this.getSession().setAttribute("success", "Successfully " + installType + "ed extension " + extensionId);
        return true;
    }

}
