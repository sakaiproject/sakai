/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.profile2.impl;

import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.profile2.api.ProfileConstants;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.EntityContentProducerEvents;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter @Slf4j
public class ProfileContentProducer implements EntityContentProducer, EntityContentProducerEvents {

    private static final String REFERENCE_ROOT = Entity.SEPARATOR + "profile";
    private static final String PROFILE = "profile";
    private static final String TYPE = "type";
    private static final String ID = "id";
    private static final String SPACE = " ";

    @Autowired private SakaiPersonManager sakaiPersonManager;
    @Autowired private ServerConfigurationService serverConfigurationService;

    @Autowired
    @Qualifier("org.sakaiproject.search.elasticsearch.ElasticSearchIndexBuilder")
    private SearchIndexBuilder searchIndexBuilder;

    @Autowired private UserDirectoryService userDirectoryService;

    // Map of events to their corresponding search index actions
    private static final Map<String, Integer> EVENT_ACTIONS = Map.of(
            SakaiPersonManager.PROFILE_UPDATE, SearchBuilderItem.ACTION_ADD,
            ProfileConstants.EVENT_PROFILE_NEW, SearchBuilderItem.ACTION_ADD,
            SakaiPersonManager.PROFILE_DELETE, SearchBuilderItem.ACTION_DELETE
    );

    public void init() {
        log.debug("init");

        searchIndexBuilder.registerEntityContentProducer(this);
    }

    @Override
    public Set<String> getTriggerFunctions() {
        return EVENT_ACTIONS.keySet();
    }

    public boolean canRead(String ref) {

        log.debug("canRead: {}", ref);
        return splitAndValidate(ref) != null;
    }

    public Integer getAction(Event e) {

        log.debug("getAction: {}", e.getEvent());
        return EVENT_ACTIONS.getOrDefault(e.getEvent(), SearchBuilderItem.ACTION_UNKNOWN);
    }

    public String getContainer(String ref) {
        log.debug("getContainer: {}", ref);
        return null;
    }

    public String getContent(String ref) {
        log.debug("getContent: {}", ref);

        String[] parts = splitAndValidate(ref);
        if (parts == null) {
            return null;
        }

        String id = parts[3];
        String userId = parts[5];


        List<SakaiPerson> sps = sakaiPersonManager.findSakaiPersonByUid(userId);
        if (sps != null && sps.size() > 0) {
            SakaiPerson sp = sps.get(0);
            StringBuffer sb = new StringBuffer();

            User user = null;

            try {
                user = userDirectoryService.getUser(userId);
                sb.append(user.getEid()).append(SPACE).append(user.getFirstName())
                    .append(SPACE).append(user.getLastName()).append(SPACE).append(user.getEmail());
            } catch (UserNotDefinedException unde) {
                log.error("No user for user id: " + userId, unde);
            }

            sb.append(sp.getNickname());
            sb.append(sp.getNotes());
            return sb.toString();
        } else {
            log.error("No SakaiPerson for uid: {}", userId);
            return null;
        }
    }

    public Reader getContentReader(String ref) {
        log.debug("getContentReader: {}", ref);
        return null;
    }

    public Map<String, ?> getCustomProperties(String ref) {
        log.debug("getCustomProperties: {}", ref);
        return null;
    }

    public String getCustomRDF(String ref) {
        log.debug("getCustomRDF: {}", ref);
        return null;
    }

    public String getId(String ref) {
        log.debug("getId: {}", ref);

        // Return the user id
        return ref.split(Entity.SEPARATOR)[5];
    }

    public Iterator<String> getSiteContentIterator(String context) {
        log.debug("getSiteContentIterator: {}", context);
        return null;
    }

    public String getSiteId(String ref) {
        log.debug("getSiteId: {}", ref);

        // Return the user's workspace id
        return "~" + ref.split(Entity.SEPARATOR)[5];
    }

    public String getSubType(String ref) {
        log.debug("getSubType: {}", ref);
        return null;
    }

    public String getTitle(String ref) {
        log.debug("getTitle: {}", ref);
        return "Profile";
    }

    public String getTool() {
        log.debug("getTool");
        return PROFILE;
    }

    public String getType(String ref) {
        log.debug("getType: {}", ref);
        return PROFILE;
    }

    public String getUrl(String ref) {
        log.debug("getUrl: {}", ref);
        return null;
    }

    public boolean isContentFromReader(String ref) {
        log.debug("isContentFromReader: {}", ref);
        return false;
    }

    public boolean isForIndex(String ref) {
        log.debug("isForIndex: {}", ref);
        return splitAndValidate(ref) != null;
    }

    public boolean matches(Event e) {
        log.debug("matches: {}", e.getEvent());
        return EVENT_ACTIONS.containsKey(e.getEvent());
    }

    public boolean matches(String ref) {
        log.debug("matches: {}", ref);
        return splitAndValidate(ref) != null;
    }

    private String[] splitAndValidate(String ref) {

		if (!ref.startsWith(REFERENCE_ROOT)) {
			return null;
		}

        String[] parts = ref.split(Entity.SEPARATOR);
        if (parts != null && parts.length == 6 && TYPE.equals(parts[2]) && ID.equals(parts[4])) {
            return parts;
        } else {
            log.error("A profile update ref should have 5 components. Ref: {}", ref);
            return null;
        }
    }
}
