package org.eclipse.smarthome.ui.nodemgmt.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.auth.Repository;
import org.eclipse.smarthome.core.internal.auth.RepositoryImpl;
import org.eclipse.smarthome.ui.nodemgmt.Node;

import com.google.gson.reflect.TypeToken;

public class NodeRepositoryImpl extends RepositoryImpl<Node> {

    /**
     * {@code Repository<Node>} instance
     */
    private static Repository<Node> repository = null;

    /**
     * Gets an instance of the class, if already available, otherwise creates new object.
     *
     * @return
     */
    public static Repository<Node> getInstance() {
        if (repository == null) {
            repository = new NodeRepositoryImpl();
        }

        return repository;
    }

    /**
     * Creates new {@code Repository<Node>} object
     */
    public NodeRepositoryImpl() {
        // create empty objects list.
        this.setObjects(new ArrayList<Node>());
        // set config file.
        this.setConfigFile("nodes.json");
        // set class
        this.setGsonType(new TypeToken<List<NodeImpl>>() {
        }.getType());
        // read configs.
        this.readConfigFile();
    }

}
