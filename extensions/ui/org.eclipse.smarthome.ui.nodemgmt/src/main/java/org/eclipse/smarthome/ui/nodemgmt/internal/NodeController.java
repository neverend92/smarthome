package org.eclipse.smarthome.ui.nodemgmt.internal;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.eclipse.smarthome.io.rest.core.compat1x.config.Compat1xAleonceanConfigDTO;
import org.eclipse.smarthome.io.rest.core.compat1x.config.Compat1xMqttConfigDTO;
import org.eclipse.smarthome.io.rest.core.compat1x.config.Compat1xMqttEventbusConfigDTO;
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
    private String broker = "";

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
        this.getAttributes().add("name");
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
    private String doAuthCheck(Node node) {
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
    private String doCheckExtension(Node node, String extensionId) {
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
        if (extensions == null) {
            return;
        }
        Gson gson = new Gson();

        for (String extensionId : extensions) {
            // doCheckExtension
            String ret = "";
            ret = this.doCheckExtension(node, extensionId);
            if (ret == null) {
                this.nodeStatus += this.getConsoleStatusLine("error", "could not check extension " + extensionId);
                this.nodeExtensions += this.getExtensionInstallForm(false, extensionId, "no", "", node);
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
                        extensionApiResponse.getVersion(), node);
            } else {
                this.nodeStatus += this.getConsoleStatusLine("warn", "extension " + extensionId + " is NOT installed.");
                this.nodeExtensions += this.getExtensionInstallForm(false, extensionId, "no",
                        extensionApiResponse.getVersion(), node);
            }

        }
    }

    private String doCreateAleonceanItem(Node node, String urlParams, String apiKey) {
        // build url.
        String url = node.getIP() + "/rest/compat1x_item/aleoncean?api_key=" + apiKey;

        return doRequest(url, "POST", urlParams);
    }

    private String doCreateShadowItem(String urlParams, String apiKey) {
        // build url.
        String url = "https://localhost:8443/rest/compat1x_item/mqtt?api_key=" + apiKey;

        return doRequest(url, "POST", urlParams);
    }

    private String doDiscoverItem(Node node, String bindingId, String apiKey) {
        if (bindingId == null || bindingId.equals("")) {
            return null;
        }
        // build url.
        String url = node.getIP() + "/rest/discovery/bindings/" + bindingId + "/scan?api_key=" + apiKey;

        return doRequest(url, "POST", null);
    }

    private String doGetConfig(Node node, String configName) {
        // build url.
        String url = node.getIP() + "/rest/compat1x_config/" + configName + "?api_key=" + this.apiKey;

        return doRequest(url, "GET", null);
    }

    /**
     * Retrieves the items from the node.
     *
     * @param node
     * @param apiKey
     * @return String statuslines.
     * @throws IOException
     */
    private String doGetItems(Node node) {
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
    private String doInstallExtension(Node node, String extensionId, String installType, String apiKey) {
        // build url.
        if (!installType.equals("install") && !installType.equals("uninstall")) {
            return null;
        }

        String url = node.getIP() + "/rest/extensions/" + extensionId + "/" + installType + "?api_key=" + apiKey;

        logger.debug("### INSTALL_URL: {}", url);
        return doRequest(url, "POST", null);
    }

    private String doRequest(String url, String method, String urlParams) {
        if (url.contains("http://")) {
            try {
                return doRequestHttp(url, method, urlParams);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } else if (url.contains("https://")) {
            try {
                return doRequestHttps(url, method, urlParams);
            } catch (IOException | KeyManagementException | NoSuchAlgorithmException e) {
                logger.error(e.getMessage());
            }
        }
        logger.debug("### No http or https passed.");
        return "";
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
    private String doRequestHttp(String url, String method, String urlParams) throws IOException {
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
            wr.writeBytes(this.encodeUrlParams(urlParams));
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

    private String doRequestHttps(String url, String method, String urlParams)
            throws IOException, NoSuchAlgorithmException, KeyManagementException {
        // we assume you know to which host you will connect,
        // so allow all certs and make no cert check
        // taken from
        // @url(http://stackoverflow.com/questions/19540289/how-to-fix-the-java-security-cert-certificateexception-no-subject-alternative)
        SSLContext sc = SSLContext.getInstance("SSL");
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        } };

        sc.init(null, trustAllCerts, new SecureRandom());

        // set socket factory
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // set hostname verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();

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
            wr.writeBytes(this.encodeUrlParams(urlParams));
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

    private String encodeUrlParams(String data) {
        logger.error("### Data 1: {}", data);
        data = data.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
        data = data.replaceAll("\\+", "%2B");
        logger.error("### Data 2: {}", data);
        return data;
    }

    private String doUpdateConfig(Node node, String configName, String urlParams, String apiKey) {
        // build url.
        String url = node.getIP() + "/rest/compat1x_config/" + configName + "?api_key=" + apiKey;

        return doRequest(url, "POST", urlParams);
    }

    /**
     * Performs the version check of OH/ESH
     *
     * @param node
     * @param apiKey
     * @return String statuslines.
     * @throws IOException
     */
    private String doVersionCheck(Node node) {
        // build url.
        String url = node.getIP() + "/rest?api_key=" + this.apiKey;

        return doRequest(url, "GET", null);
    }

    private String formatAttribute(String value) {
        if (value != null) {
            return value;
        }
        return "";
    }

    private String getBindingConfigOptions(Node node, String name) {
        StringBuilder sb = new StringBuilder();
        Gson gson = new Gson();
        if (name.equals("binding-mqtt")) {
            // mqtt.cfg
            String ret = this.doGetConfig(node, "mqtt");
            Compat1xMqttConfigDTO mqttConfigResponse = gson.fromJson(ret, Compat1xMqttConfigDTO.class);
            if (mqttConfigResponse == null) {
                this.nodeStatus += this.getConsoleStatusLine("error", "Could not load config for mqtt");
                // return null;
                mqttConfigResponse = new Compat1xMqttConfigDTO();
            } else {
                this.nodeStatus += this.getConsoleStatusLine("ok", "Loaded config for mqtt");
            }
            if (!node.getName().equals(mqttConfigResponse.getClientId())) {
                this.nodeStatus += this.getConsoleStatusLine("error", "Node Name and Client ID don't match. BAD ERROR");
            }

            // set broker globally.
            this.broker = mqttConfigResponse.getBroker();

            sb.append("<form method=\"POST\" action=\"" + this.getServlet().getBaseUrl() + "?controller="
                    + this.getPlural(this.getEntityName()) + "&action=updateConfig&id=" + node.getId()
                    + "\" id=\"form-config-mqtt\" class=\"form-horizontal\">");
            sb.append("<input type=\"hidden\" name=\"configName\" value=\"mqtt\">");
            sb.append("<input type=\"hidden\" name=\"apiKey\" value=\"" + this.apiKey + "\">");
            sb.append("<input type=\"hidden\" name=\"" + this.getFieldName() + "\" value=\"" + this.getUrlId() + "\">");
            // Broker
            sb.append("<div class=\"form-group\">");
            sb.append("<label for=\"mqtt-broker\" class=\"col-sm-2 control-label\">Broker</label>");
            sb.append("<div class=\"col-sm-10\">");
            sb.append("<input type=\"text\" class=\"form-control\" name=\"mqtt-broker\" value=\""
                    + this.formatAttribute(mqttConfigResponse.getBroker()) + "\">");
            sb.append("</div>");
            sb.append("</div>");
            // Host
            sb.append("<div class=\"form-group\">");
            sb.append("<label for=\"mqtt-host\" class=\"col-sm-2 control-label\">Host</label>");
            sb.append("<div class=\"col-sm-10\">");
            sb.append("<input type=\"text\" class=\"form-control\" name=\"mqtt-host\" value=\""
                    + this.formatAttribute(mqttConfigResponse.getHost()) + "\">");
            sb.append("</div>");
            sb.append("</div>");
            // ClientID
            sb.append("<div class=\"form-group\">");
            sb.append("<label for=\"mqtt-clientId\" class=\"col-sm-2 control-label\">Client-ID</label>");
            sb.append("<div class=\"col-sm-10\">");
            sb.append("<input readonly type=\"text\" class=\"form-control\" name=\"mqtt-clientId\" value=\""
                    + this.formatAttribute(node.getName()) + "\">");
            sb.append("</div>");
            sb.append("</div>");
            // Username
            sb.append("<div class=\"form-group\">");
            sb.append("<label for=\"mqtt-username\" class=\"col-sm-2 control-label\">Username</label>");
            sb.append("<div class=\"col-sm-10\">");
            sb.append("<input type=\"text\" class=\"form-control\" name=\"mqtt-username\" value=\""
                    + this.formatAttribute(mqttConfigResponse.getUsername()) + "\">");
            sb.append("</div>");
            sb.append("</div>");
            // Password
            sb.append("<div class=\"form-group\">");
            sb.append("<label for=\"mqtt-password\" class=\"col-sm-2 control-label\">Password</label>");
            sb.append("<div class=\"col-sm-10\">");
            sb.append("<input type=\"text\" class=\"form-control\" name=\"mqtt-password\" value=\""
                    + this.formatAttribute(mqttConfigResponse.getPassword()) + "\">");
            sb.append("</div>");
            sb.append("</div>");
            // Submit button
            sb.append("<div class=\"form-group\">");
            sb.append("<div class=\"col-sm-offset-2 col-sm-10\">");
            sb.append("<input type=\"submit\" class=\"btn btn-success\" value=\"Save MQTT Config\">");
            sb.append("</div>");
            sb.append("</div>");
            sb.append("</form>");

            // mqtt-eventbus.cfg
            ret = this.doGetConfig(node, "mqtt-eventbus");
            Compat1xMqttEventbusConfigDTO mqttEventbusConfigResponse = gson.fromJson(ret,
                    Compat1xMqttEventbusConfigDTO.class);
            if (mqttEventbusConfigResponse == null) {
                this.nodeStatus += this.getConsoleStatusLine("error", "Could not load config for mqtt-eventbus");
                mqttEventbusConfigResponse = new Compat1xMqttEventbusConfigDTO();
                // return null;
            } else {
                this.nodeStatus += this.getConsoleStatusLine("ok", "Loaded config for mqtt-eventbus");
            }

            if (!mqttEventbusConfigResponse.getBroker().equals(mqttConfigResponse.getBroker())) {
                this.nodeStatus += this.getConsoleStatusLine("error", "Node Name and Client ID don't match. BAD ERROR");
            }

            sb.append("<hr>");
            sb.append("<form method=\"POST\" action=\"" + this.getServlet().getBaseUrl() + "?controller="
                    + this.getPlural(this.getEntityName()) + "&action=updateConfig&id=" + node.getId()
                    + "\" id=\"form-config-mqtt-eventbus\" class=\"form-horizontal\">");
            sb.append("<input type=\"hidden\" name=\"configName\" value=\"mqtt-eventbus\">");
            sb.append("<input type=\"hidden\" name=\"apiKey\" value=\"" + this.apiKey + "\">");
            sb.append("<input type=\"hidden\" name=\"" + this.getFieldName() + "\" value=\"" + this.getUrlId() + "\">");
            // Broker
            sb.append("<div class=\"form-group\">");
            sb.append("<label for=\"mqtt-eventbus-broker\" class=\"col-sm-2 control-label\">Broker</label>");
            sb.append("<div class=\"col-sm-10\">");
            sb.append("<input type=\"text\" class=\"form-control\" name=\"mqtt-eventbus-broker\" value=\""
                    + this.formatAttribute(mqttEventbusConfigResponse.getBroker()) + "\">");
            sb.append("</div>");
            sb.append("</div>");
            // StatePublishTopic
            sb.append("<div class=\"form-group\">");
            sb.append(
                    "<label for=\"mqtt-eventbus-statePublishTopic\" class=\"col-sm-2 control-label\">StatePublishTopic</label>");
            sb.append("<div class=\"col-sm-10\">");
            String statePublishTopic = "/" + node.getName() + "/out/${item}/state";
            sb.append(
                    "<input readonly type=\"text\" class=\"form-control\" name=\"mqtt-eventbus-statePublishTopic\" value=\""
                            + statePublishTopic + "\">");
            sb.append("</div>");
            sb.append("</div>");
            // CommandSubscribeTopic
            sb.append("<div class=\"form-group\">");
            sb.append(
                    "<label for=\"mqtt-eventbus-commandSubscribeTopic\" class=\"col-sm-2 control-label\">CommandSubscribeTopic</label>");
            sb.append("<div class=\"col-sm-10\">");
            String commandSubscribeTopic = "/" + node.getName() + "/in/${item}/command";
            sb.append(
                    "<input readonly type=\"text\" class=\"form-control\" name=\"mqtt-eventbus-commandSubscribeTopic\" value=\""
                            + commandSubscribeTopic + "\">");
            sb.append("</div>");
            sb.append("</div>");
            // Submit button
            sb.append("<div class=\"form-group\">");
            sb.append("<div class=\"col-sm-offset-2 col-sm-10\">");
            sb.append("<input type=\"submit\" class=\"btn btn-success\" value=\"Save MQTT-Eventbus Config\">");
            sb.append("</div>");
            sb.append("</div>");
            sb.append("</form>");
        } else if (name.equals("binding-aleoncean")) {
            // aleoncean.cfg
            String ret = this.doGetConfig(node, "aleoncean");
            Compat1xAleonceanConfigDTO aleonceanConfigResponse = gson.fromJson(ret, Compat1xAleonceanConfigDTO.class);
            if (aleonceanConfigResponse == null) {
                this.nodeStatus += this.getConsoleStatusLine("error", "Could not load config for mqtt");
                // return null;
                aleonceanConfigResponse = new Compat1xAleonceanConfigDTO();
            } else {
                this.nodeStatus += this.getConsoleStatusLine("ok", "Loaded config for aleoncean");
            }
            sb.append("<form method=\"POST\" action=\"" + this.getServlet().getBaseUrl() + "?controller="
                    + this.getPlural(this.getEntityName()) + "&action=updateConfig&id=" + node.getId()
                    + "\" id=\"form-config-mqtt\" class=\"form-horizontal\">");
            sb.append("<input type=\"hidden\" name=\"configName\" value=\"aleoncean\">");
            sb.append("<input type=\"hidden\" name=\"apiKey\" value=\"" + this.apiKey + "\">");
            sb.append("<input type=\"hidden\" name=\"" + this.getFieldName() + "\" value=\"" + this.getUrlId() + "\">");
            // Port
            sb.append("<div class=\"form-group\">");
            sb.append("<label for=\"aleoncean-port\" class=\"col-sm-2 control-label\">Port</label>");
            sb.append("<div class=\"col-sm-10\">");
            sb.append("<input type=\"text\" class=\"form-control\" name=\"aleoncean-port\" value=\""
                    + this.formatAttribute(aleonceanConfigResponse.getPort()) + "\">");
            sb.append("</div>");
            sb.append("</div>");
            // Submit button
            sb.append("<div class=\"form-group\">");
            sb.append("<div class=\"col-sm-offset-2 col-sm-10\">");
            sb.append("<input type=\"submit\" class=\"btn btn-success\" value=\"Save MQTT Config\">");
            sb.append("</div>");
            sb.append("</div>");
            sb.append("</form>");
        }
        return sb.toString();
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
        this.nodeExtensions += this.getExtensionInstallForm(true, "", "", "", node);
        template = template.replace("###NODE_EXTENSIONS###", this.nodeExtensions);

        String selectOptions = String.join("</option><option>", node.getExtensions());
        selectOptions = "<option>" + selectOptions + "</option>";
        template = template.replace("###NODE_BINDINGS###", selectOptions);

        template = template.replaceAll("###HIDDEN_INPUT_URL_ID###",
                "<input type=\"hidden\" name=\"" + this.getFieldName() + "\" value=\"" + this.getUrlId() + "\">"
                        + "<input type=\"hidden\" name=\"apiKey\" value=\"" + this.apiKey + "\">"
                        + "<input type=\"hidden\" name=\"broker\" value=\"" + this.broker + "\">");
        template = template.replace("###FORM_ACTION_NEW_NODE_ITEM###", this.getServlet().getBaseUrl() + "?controller="
                + this.getPlural(this.getEntityName()) + "&action=createNodeItem&id=" + node.getId());
        template = template.replace("###FORM_ACTION_DISCOVER_ITEM###", this.getServlet().getBaseUrl() + "?controller="
                + this.getPlural(this.getEntityName()) + "&action=discoverItem&id=" + node.getId());

        content += template;
        return content;
    }

    private String getExtensionInstallForm(boolean flagInput, String name, String status, String version, Node node) {
        StringBuilder sb = new StringBuilder();

        String formId = "extension-new";
        sb.append("<tr>");

        if (!flagInput) {
            formId = "extension-" + name;
            sb.append("<form id=\"" + formId + "\" method=\"POST\" action=\"" + this.getServlet().getBaseUrl()
                    + "?controller=" + this.getPlural(this.getEntityName()) + "&action=installExtension&id="
                    + node.getId() + "\">");
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
                    + "?controller=" + this.getPlural(this.getEntityName()) + "&action=installExtension&id="
                    + node.getId() + "\">");
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
                    + "&action=deleteExtension&id=" + node.getId() + "\">");
            sb.append("<input type=\"hidden\" name=\"" + this.getFieldName() + "\" value=\"" + this.getUrlId() + "\">");
            sb.append("<input type=\"hidden\" name=\"extension-name\" value=\"" + name + "\">");
            sb.append("<a class=\"btn btn-danger\" href=\"#\" onclick=\"$('#" + formId
                    + "-delete').submit();return false;\">Delete</a>");
            sb.append("</form>");
        }
        sb.append("</td>");

        if (!flagInput) {
            // if (!version.equals("")) {
            sb.append("<td><a class=\"btn btn-warning\" href=\"#\" onclick=\"$('#" + formId
                    + "-details').toggleClass('hidden');return false;\">+</a></td>");
            // } else {
            // sb.append("<td>&nbsp;</td>");
            // }
        } else {
            sb.append("<td>&nbsp;</td>");
        }

        sb.append("</tr>");

        // check version of binding, if lower than 2.0.0 configuration via rest api is not possible.
        // if (!version.equals("")) {
        sb.append("<tr id=\"" + formId + "-details\" class=\"hidden\">");
        sb.append("<td colspan=\"4\">");
        if (!version.equals("2.0.0.SNAPSHOT") && !version.equals("0.9.0.SNAPSHOT")) {
            String bindingConfigOptions = this.getBindingConfigOptions(node, name);
            if (bindingConfigOptions != null && !bindingConfigOptions.equals("")) {
                sb.append(bindingConfigOptions);
            } else {
                sb.append("Config: Manual config for extension " + name + " needed.");
            }
        } else {
            sb.append("Config: Automatic Config not implemented yet.");
        }
        sb.append("</td>");
        sb.append("</tr>");
        // }

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
        if (tmpExtensions == null) {
            tmpExtensions = new String[0];
        }
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

        ret = this.doAuthCheck(node);

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

        ret = this.doVersionCheck(node);
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

        doCheckExtensions(node, node.getExtensions());

        ret = this.doGetItems(node);
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
            StringBuilder sb = new StringBuilder();
            sb.append("<tr>");
            sb.append("<td><a href=\"" + item.getLink() + "?api_key=" + this.apiKey + "\" target=\"_blank\">"
                    + item.getName() + "</a></td>");
            sb.append("<td>" + this.formatAttribute(item.getLabel()) + "</td>");
            sb.append("<td>" + this.formatAttribute(item.getCategory()) + "</td>");
            sb.append("<td>" + this.formatAttribute(item.getType()) + "</td>");
            sb.append("<td>");
            sb.append("<form method=\"POST\" action=\"" + this.getServlet().getBaseUrl() + "?controller="
                    + this.getPlural(this.getEntityName()) + "&action=createShadowItem&id=" + node.getId() + "\">");
            sb.append("<input type=\"hidden\" name=\"apiKey\" value=\"" + this.apiKey + "\">");
            sb.append("<input type=\"hidden\" name=\"" + this.getFieldName() + "\" value=\"" + this.getUrlId() + "\">");
            sb.append("<input type=\"hidden\" name=\"node-item-type\" value=\"" + item.getType() + "\">");
            sb.append("<input type=\"hidden\" name=\"node-item-name\" value=\"" + item.getName() + "\">");
            sb.append("<input type=\"hidden\" name=\"node-item-desc\" value=\"" + item.getLabel() + "\">");
            sb.append("<input type=\"hidden\" name=\"node-item-icon\" value=\"" + null + "\">"); // icon???
            sb.append("<input type=\"hidden\" name=\"node-name\" value=\"" + node.getName() + "\">");
            sb.append("<input type=\"hidden\" name=\"broker\" value=\"" + this.broker + "\">");

            sb.append("<input type=\"submit\" class=\"btn btn-success\" value=\"Add Shadow Item\">");
            sb.append("</form>");
            sb.append("</td>");
            sb.append("</tr>");
            this.nodeItems += sb.toString();
        }
        this.nodeStatus += this.getConsoleStatusLine("ok", "recieved items");
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
        } else if (this.getUrlAction().equals("updateConfig")) {
            return this.postUpdateConfig(req);
        } else if (this.getUrlAction().equals("discoverItem")) {
            return this.postDiscoverItem(req);
        } else if (this.getUrlAction().equals("createNodeItem")) {
            return this.postCreateNodeItem(req);
        } else if (this.getUrlAction().equals("createShadowItem")) {
            return this.postCreateShadowItem(req);
        }

        return false;
    }

    private boolean postCreateNodeItem(HttpServletRequest req) {
        // get node to obtain ip of node.
        Node node = this.getRepository().get(this.getUrlId());
        if (node == null) {
            this.getSession().setAttribute("errors", "Could not create aleoncean item.");
            return false;
        }

        String apiKey = req.getParameter("apiKey");

        String itemName = req.getParameter("aleoncean-item-name");
        String itemType = req.getParameter("aleoncean-item-type");
        String itemDesc = req.getParameter("aleoncean-item-desc");
        String itemIcon = req.getParameter("aleoncean-item-icon");
        String deviceRemoteId = req.getParameter("aleoncean-device-remoteid");
        String deviceType = req.getParameter("aleoncean-device-type");
        String deviceParameter = req.getParameter("aleoncean-device-parameter");

        String urlParams = "itemName=" + itemName + "&itemType=" + itemType + "&itemDesc=" + itemDesc + "&itemIcon="
                + itemIcon + "&deviceRemoteId=" + deviceRemoteId + "&deviceType=" + deviceType + "&deviceParameter="
                + deviceParameter;

        String ret = this.doCreateAleonceanItem(node, urlParams, apiKey);
        if (ret == null || !this.cleanOutputRet(ret).equals("ok")) {
            this.getSession().setAttribute("errors", "Could not create aleoncean node item. (" + ret + ")");
            return false;
        }
        // now directly create shadow Item.
        String mqttBroker = req.getParameter("broker");
        String mqttNodeName = node.getName();
        String nodeItemName = itemName;
        itemName = "Master_" + nodeItemName;
        boolean successShadow = this.postCreateShadowItemHelper(itemName, itemType, itemDesc, itemIcon, mqttBroker,
                mqttNodeName, nodeItemName, apiKey);
        if (successShadow) {
            String tmpSuccess = (String) this.getSession().getAttribute("success");
            this.getSession().setAttribute("success", "Successfully created aleoncean node item.<br>" + tmpSuccess);
        }
        return successShadow;
    }

    private boolean postCreateShadowItem(HttpServletRequest req) {
        // Shadow items are created on the master (no node needed)

        String itemType = req.getParameter("node-item-type");
        String itemDesc = req.getParameter("node-item-desc");
        String itemIcon = req.getParameter("node-item-icon");
        String mqttBroker = req.getParameter("broker");
        String mqttNodeName = req.getParameter("node-name");
        String nodeItemName = req.getParameter("node-item-name");
        String itemName = "Master_" + nodeItemName;
        String apiKey = req.getParameter("apiKey");

        return this.postCreateShadowItemHelper(itemName, itemType, itemDesc, itemIcon, mqttBroker, mqttNodeName,
                nodeItemName, apiKey);
    }

    private boolean postCreateShadowItemHelper(String itemName, String itemType, String itemDesc, String itemIcon,
            String mqttBroker, String mqttNodeName, String nodeItemName, String apiKey) {
        String urlParams = "itemName=" + itemName + "&itemType=" + itemType + "&itemDesc=" + itemDesc + "&itemIcon="
                + itemIcon + "&mqttBroker=" + mqttBroker + "&mqttNodeName=" + mqttNodeName + "&nodeItemName="
                + nodeItemName;
        String ret = this.doCreateShadowItem(urlParams, apiKey);
        if (ret == null || !this.cleanOutputRet(ret).equals("ok")) {
            this.getSession().setAttribute("errors", "Could not create mqtt shadow item.");
            return false;
        }

        this.getSession().setAttribute("success", "Successfully created mqtt shadow item.");
        return true;
    }

    public boolean postDeleteExtension(HttpServletRequest req) {
        return this.handleExtensionChange(req, "delete");
    }

    private boolean postDiscoverItem(HttpServletRequest req) {
        // get node to obtain ip of node.
        Node node = this.getRepository().get(this.getUrlId());
        if (node == null) {
            this.getSession().setAttribute("errors", "Could not scan for item.");
            return false;
        }

        String bindingId = req.getParameter("bindingId");
        String apiKey = req.getParameter("apiKey");

        String ret = this.doDiscoverItem(node, bindingId, apiKey);
        if (ret == null) {
            this.getSession().setAttribute("errors", "Could not scan for node item. (" + ret + ")");
            return false;
        }

        this.getSession().setAttribute("warn",
                "Started scan for node item. Refresh page and check for new node items.");
        return false;
    }

    public boolean postInstallExtension(HttpServletRequest req) {
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

        String ret = this.doInstallExtension(node, extensionId, installType, apiKey);

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
        if (installType.equals("install")) {
            installType = "add";
        }
        this.getSession().setAttribute("success", "Successfully " + installType + "ed extension " + extensionId);
        return true;
    }

    public boolean postUpdateConfig(HttpServletRequest req) {
        String configName = req.getParameter("configName");
        String apiKey = req.getParameter("apiKey");
        Node node = this.getRepository().get(this.getUrlId());
        if (node == null) {
            this.getSession().setAttribute("errors", "Could not update config for " + configName + ".");
            return false;
        }

        String urlParams = "";
        if (configName.equals("mqtt")) {
            String broker = req.getParameter("mqtt-broker");
            String host = req.getParameter("mqtt-host");
            String clientId = req.getParameter("mqtt-clientId");
            String username = req.getParameter("mqtt-username");
            String password = req.getParameter("mqtt-password");
            urlParams = "broker=" + broker + "&host=" + host + "&clientId=" + clientId + "&username=" + username
                    + "&password=" + password;
        } else if (configName.equals("mqtt-eventbus")) {
            String broker = req.getParameter("mqtt-eventbus-broker");
            String statePublishTopic = req.getParameter("mqtt-eventbus-statePublishTopic");
            String commandSubscribeTopic = req.getParameter("mqtt-eventbus-commandSubscribeTopic");
            urlParams = "broker=" + broker + "&statePublishTopic=" + statePublishTopic + "&commandSubscribeTopic="
                    + commandSubscribeTopic;
        } else if (configName.equals("aleoncean")) {
            String port = req.getParameter("aleoncean-port");
            urlParams = "port=" + port;
        } else {
            return false;
        }

        String ret = this.doUpdateConfig(node, configName, urlParams, apiKey);

        if (ret == null || !this.cleanOutputRet(ret).equals("ok")) {
            this.getSession().setAttribute("errors", "Could not update config for " + configName);
            return false;
        }

        this.getSession().setAttribute("success", "Successfully updated config for " + configName);
        return true;
    }

    private String cleanOutputRet(String ret) {
        ret = ret.replaceAll("\"", "");
        ret = ret.replaceAll("'", "");
        return ret;
    }

}
