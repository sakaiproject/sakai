package org.sakaiproject.commons.api;

import org.sakaiproject.entity.api.Entity;

public class PostReferenceFactory {

    public static String getReference(String commonsId, String postId) {
        return CommonsManager.REFERENCE_ROOT + Entity.SEPARATOR + commonsId + Entity.SEPARATOR + "posts" + Entity.SEPARATOR + postId;
    }
}
