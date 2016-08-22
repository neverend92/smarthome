package org.eclipse.smarthome.core.auth;

import java.util.ArrayList;

public interface Repository<E extends DTO> {

    /**
     * Creates new object.
     *
     * @param object
     * @return
     */
    public boolean create(E object);

    /**
     * Deletes object.
     *
     * @param name
     * @return
     */
    public boolean delete(String name);

    /**
     * Gets object by object.
     *
     * @param object
     * @return
     */
    public E get(E object);

    /**
     * Gets object by name.
     *
     * @param name
     * @return
     */
    public E get(String name);

    /**
     * Gets all objects.
     *
     * @return
     */
    public ArrayList<E> getAll();

    /**
     * Gets object by attribute.
     *
     * @param attribute
     * @param name
     * @return
     */
    public E getBy(String attribute, String name);

    /**
     * Updates object.
     *
     * @param name
     * @param object
     * @param changeRoles
     * @return
     */
    public boolean update(String name, E object, boolean changeRoles);

}
