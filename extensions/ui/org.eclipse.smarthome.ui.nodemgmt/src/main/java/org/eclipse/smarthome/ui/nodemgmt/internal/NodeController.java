package org.eclipse.smarthome.ui.nodemgmt.internal;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.smarthome.ui.nodemgmt.NodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeController {

    protected final Logger logger = LoggerFactory.getLogger(NodeController.class);

    protected NodeMgmtServlet servlet;

    protected HttpSession session;

    protected NodeRepository repository;
    protected String entityName;
    protected String fieldName;
    protected ArrayList<String> attributes;

    protected String urlAction;
    protected String urlId;

    public NodeController(String urlAction, String urlId, NodeMgmtServlet servlet) {
        this.urlAction = urlAction;
        this.urlId = urlId;
        this.servlet = servlet;
        this.attributes = new ArrayList<String>();
    }

    public boolean postContent(HttpServletRequest req) {
        // TODO Auto-generated method stub
        return false;
    }

    public String getContent() {
        // TODO Auto-generated method stub
        return null;
    }

}
