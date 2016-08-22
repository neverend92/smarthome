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
     * General method to get array attribute.
     *
     * @param attribute
     * @return
     */
    public String[] getArray(String attribute);

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
     * Sets {@code attribute} to {@code value}
     *
     * @param attribute
     * @param value
     */
    public void set(String attribute, String value);

    /**
     * Sets {@code attribute} to {@code value} (array)
     * 
     * @param attribute
     * @param value
     */
    public void set(String attribute, String[] value);

}
