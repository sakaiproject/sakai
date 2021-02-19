/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.entity.api;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DefaultEntityProducer implements EntityProducer {

    public String getLabel() {
        return "";
    }

    public boolean willArchiveMerge() {
        return false;
    }

    public String archive(String siteId, Document doc, Stack<Element> stack, String archivePath, List<Reference> attachments) {
        return "";
    }

    public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map<String, String> attachmentNames, Map<String, String> userIdTrans,
            Set<String> userListAllowImport) {
        return "";
    }

    public boolean parseEntityReference(String reference, Reference ref) {
        return false;
    }

    public String getEntityDescription(Reference ref) {
        return "";
    }

    public ResourceProperties getEntityResourceProperties(Reference ref) {
        return null;
    }

    public Entity getEntity(Reference ref) {
        return null;
    }

    public String getEntityUrl(Reference ref) {
        return "";
    }

    public Optional<String> getEntityUrl(Reference ref, Entity.UrlType urlType) {
        return Optional.empty();
    }

    public Collection<String> getEntityAuthzGroups(Reference ref, String userId) {
        return Collections.<String>emptyList();
    }

    public HttpAccess getHttpAccess() {
        return null;
    }
}
