/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.search.api;

import java.io.Reader;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.search.model.SearchBuilderItem;

public class DefaultEntityContentProducer implements EntityContentProducer {

    public boolean isContentFromReader(String reference) {
        return false;
    }

    public Reader getContentReader(String reference) {
        return null;
    }

    public String getContent(String reference) {
        return "";
    }

    public String getTitle(String reference) {
        return "";
    }

    public String getUrl(String reference) {
        return "";
    }

    public String getUrl(String reference, Entity.UrlType urlType) {
        return this.getUrl(reference);
    }

    public boolean matches(String reference) {
        return false;
    }

    public Integer getAction(Event event) {
        return SearchBuilderItem.ACTION_UNKNOWN;
    }

    public boolean matches(Event event) {
        return false;
    }

    public String getTool() {
        return "";
    }
    
    public String getSiteId(String reference) {
        return "";
    }

    public Iterator<String> getSiteContentIterator(String context) {
        return null;
    }

    public boolean isForIndex(String reference) {
        return false;
    }

    public boolean canRead(String reference) {
        return false;
    }

    public Map<String, ?> getCustomProperties(String ref) {
        return Collections.emptyMap();
    }

    public String getCustomRDF(String ref) {
        return "";
    }

    public String getId(String ref) {
        return "";
    }

    public String getType(String ref) {
        return "";
    }

    public String getSubType(String ref) {
        return "";
    }

    public String getContainer(String ref) {
        return "";
    }
}
