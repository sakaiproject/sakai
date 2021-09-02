package org.sakaiproject.commons.api.datamodel;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.util.BaseResourceProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.Timestamp;
import java.util.Stack;

@Getter @Setter
public class PostLike implements Entity {

    private String id = "";
    private String postId;
    private String userId;
    private boolean liked;
    private Timestamp modified;
    private String url;

    public ResourceProperties getProperties() {
        ResourceProperties rp = new BaseResourceProperties();
        rp.addProperty("id", getId());
        return rp;
    }

    public String getReference() {
        return null;
    }

    public String getUrl(String arg0) {
        return getUrl();
    }

    public String getReference(String base) {
        return getReference();
    }

    public Element toXml(Document arg0, Stack arg1) {
        return null;
    }
}
