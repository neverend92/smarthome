package org.eclipse.smarthome.core.auth;

import java.util.ArrayList;

public interface Repository<E extends DTO> {

    public boolean create(E object);

    public boolean delete(String name);

    public E get(E object);

    public E get(String name);

    public ArrayList<E> getAll();

    public boolean update(String name, E object);

}
