package org.eclipse.smarthome.ui.nodemgmt.internal;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

    public NodeController(String urlAction, String urlId, MgmtServlet servlet) {
        super(urlAction, urlId, servlet);

        this.setRepository(NodeRepositoryImpl.getInstance());
        this.setEntityName("node");
        this.setFieldName("ip");

        this.getAttributes().add("ip");
        this.getAttributes().add("description");
        this.getAttributes().add("credentials");
    }

    @Override
    public Node getModel() {
        return new NodeImpl();
    }

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

        content += template;
        return content;

    }

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
        String apiKey = authApiResponse.getToken();
        this.nodeStatus += this.getConsoleStatusLine("ok", "credentials are valid");

        // doVersionCheck
        try {
            ret = this.doVersionCheck(node, apiKey);
        } catch (IOException e) {
            this.nodeStatus += this.getConsoleStatusLine("error", "could not obtain version");
            return;
        }
        if (ret == null) {
            this.nodeStatus += this.getConsoleStatusLine("error", "could not obtain version");
            return;
        }

        VersionApiResponse versionApiResponse = gson.fromJson(ret, VersionApiResponse.class);
        String version = versionApiResponse.getVersion();
        this.nodeStatus += this.getConsoleStatusLine("ok", "node uses Eclipse Smarthome " + version);

        // doGetItems
        try {
            ret = this.doGetItems(node, apiKey);
        } catch (IOException e) {
            this.nodeStatus += this.getConsoleStatusLine("error", "could not obtain items");
            return;
        }
        if (ret == null) {
            this.nodeStatus += this.getConsoleStatusLine("error", "could not obtain items");
            return;
        }

        ItemsApiResponse[] itemsApiResponses = gson.fromJson(ret, ItemsApiResponse[].class);
        for (ItemsApiResponse item : itemsApiResponses) {
            String tmp = "<tr>";
            tmp += "<td><a href=\"" + item.getLink() + "?api_key=" + apiKey + "\" target=\"_blank\">" + item.getName()
                    + "</a></td>";
            tmp += "<td>" + item.getLabel() + "</td>";
            tmp += "<td>" + item.getCategory() + "</td>";
            tmp += "<td>" + item.getType() + "</td>";
            tmp += "<td><a class=\"btn btn-success\" href=\"#\">Add Item</a></td>";
            tmp += "</tr>";
            this.nodeItems += tmp;
        }
        this.nodeStatus += this.getConsoleStatusLine("ok", "recieved items");

        String[] extensions = { "binding-mqtt", "binding-enocean", "binding0" };
        doCheckExtensions(node, apiKey, extensions);
    }

    private String doAuthCheck(Node node) throws IOException {
        // build url.
        String url = node.getIP() + "/rest/auth";

        // load credentials.
        UsernamePasswordCredentials credentials = this.parseCredentials(node.getCredentials());
        String urlParams = "username=" + credentials.getUserName() + "&password=" + credentials.getPassword();

        return doRequest(url, "POST", urlParams);
    }

    private String doVersionCheck(Node node, String apiKey) throws IOException {
        // build url.
        String url = node.getIP() + "/rest?api_key=" + apiKey;

        return doRequest(url, "GET", null);
    }

    private String doGetItems(Node node, String apiKey) throws IOException {
        // build url.
        String url = node.getIP() + "/rest/items?recursive=false&api_key=" + apiKey;

        return doRequest(url, "GET", null);
    }

    private void doCheckExtensions(Node node, String apiKey, String[] extensions) {
        Gson gson = new Gson();

        for (String extensionId : extensions) {
            // doCheckExtension
            String ret = "";
            try {
                ret = this.doCheckExtension(node, apiKey, extensionId);
            } catch (IOException e) {
                this.nodeStatus += this.getConsoleStatusLine("error", "could not check extension " + extensionId);
                continue;
            }
            if (ret == null) {
                this.nodeStatus += this.getConsoleStatusLine("error", "could not check extension " + extensionId);
                continue;
            }

            ExtensionApiResponse extensionApiResponse = gson.fromJson(ret, ExtensionApiResponse.class);
            if (extensionApiResponse.isInstalled()) {
                this.nodeStatus += this.getConsoleStatusLine("ok", "extension " + extensionId + " is installed.");
            } else {
                this.nodeStatus += this.getConsoleStatusLine("warn", "extension " + extensionId + " is NOT installed.");
                this.nodeStatus += this.getConsoleStatusLine("info", "installing extension " + extensionId + " now.");
                // do install.
                try {
                    this.doInstallExtension(node, apiKey, extensionId);
                } catch (IOException e) {
                    this.nodeStatus += this.getConsoleStatusLine("error", "could not install extension " + extensionId);
                    continue;
                }
            }

            if (extensionApiResponse.getVersion().equals("1.0")) {
                this.nodeStatus += this.getConsoleStatusLine("warn",
                        "manual config for extension " + extensionId + " needed.");
            }

        }
    }

    private String doCheckExtension(Node node, String apiKey, String extensionId) throws IOException {
        // build url.
        String url = node.getIP() + "/rest/extensions/" + extensionId + "?api_key=" + apiKey;

        return doRequest(url, "GET", null);
    }

    private String doInstallExtension(Node node, String apiKey, String extensionId) throws IOException {
        // build url.
        String url = node.getIP() + "/rest/extensions/" + extensionId + "/install?api_key=" + apiKey;

        return doRequest(url, "POST", null);
    }

    private String doRequest(String url, String method, String urlParams) throws IOException {
        // init connection object.
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        // set timeout to 0.5s
        con.setConnectTimeout(500);
        // set method to post.
        con.setRequestMethod(method);
        // enable output.
        con.setDoOutput(true);

        if (method.equals("POST")) {
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

    private UsernamePasswordCredentials parseCredentials(String credentials) {
        String[] a_credentials = new String[2];
        a_credentials = credentials.split(":");
        return new UsernamePasswordCredentials(a_credentials[0], a_credentials[1]);
    }

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

}
