package org.eclipse.smarthome.core.auth;

public interface DTO {

    /**
     * General method to get attribute.
     *
     * @param attribute
     * @return
     */
    public String get(String attribute);

    /**
     * Gets the attributes name (user readable)
     *
     * @param attribute
     * @return
     */
    public String getAttributeName(String attribute);

    /**
     * Gets the object's idetifier
     *
     * @return
     */
    public String getId();

    /**
     * Gets the roles list.
     *
     * @return
     */
    public String[] getRoles();

    /**
     * Sets {@code attribute} to {@code value}
     *
     * @param attribute
     * @param value
     */
    public void set(String attribute, String value);

    /**
     * Sets roles list.
     *
     * @param roles
     */
    public void setRoles(String[] roles);

}
