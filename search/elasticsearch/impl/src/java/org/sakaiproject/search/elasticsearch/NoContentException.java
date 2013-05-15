package org.sakaiproject.search.elasticsearch;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 5/13/13
 * Time: 9:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class NoContentException extends Exception {
    private String id;
    private String reference;
    private String siteId;
    public NoContentException(String id, String reference, String siteId) {
        this.id = id;
        this.reference = reference;
        this.siteId = siteId;
    }

    public String getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
}
