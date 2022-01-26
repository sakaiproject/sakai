/**
 * Copyright (c) 2003-2021 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
