package org.eclipse.smarthome.core.auth;

public interface DTO {

    public String get(String attribute);

    public String getAttributeName(String attribute);

    public String getId();

    public String[] getRoles();

    public void set(String attribute, String value);

    public void setRoles(String[] roles);

}
