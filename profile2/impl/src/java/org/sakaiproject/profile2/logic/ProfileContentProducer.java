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

package org.sakaiproject.profile2.logic;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.profile2.logic.ProfilePrivacyLogic;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.EntityContentProducerEvents;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Setter @Slf4j
public class ProfileContentProducer implements EntityContentProducer, EntityContentProducerEvents {

    private static final String PROFILE = "profile";
    private static final String TYPE = "type";
    private static final String ID = "id";
    private static final String SPACE = " ";

    private ProfilePrivacyLogic privacyLogic;
    private SakaiPersonManager sakaiPersonManager;
    private ServerConfigurationService serverConfigurationService;
    private SearchIndexBuilder searchIndexBuilder;
    private UserDirectoryService userDirectoryService;

    public void init() {
        log.debug("init");

        if (serverConfigurationService.getBoolean("profile2.indexProfiles", true)) {
            searchIndexBuilder.registerEntityContentProducer(this);
        }
    }

    public Set<String> getTriggerFunctions() {

        Set<String> h = new HashSet<>();
        h.add(ProfileConstants.EVENT_PROFILE_NEW);
        h.add(SakaiPersonManager.PROFILE_UPDATE);
        h.add(SakaiPersonManager.PROFILE_DELETE);
        return h;
    }

    public boolean canRead(String ref) {

        log.debug("canRead: {}", ref);
        return splitAndValidate(ref) != null;
    }

    public Integer getAction(Event e) {

        log.debug("getAction: {}", e.getEvent());
        String eventName = e.getEvent();

        if (SakaiPersonManager.PROFILE_UPDATE.equals(eventName)
                || ProfileConstants.EVENT_PROFILE_NEW.equals(eventName)) {
            return SearchBuilderItem.ACTION_ADD;
        } else if (SakaiPersonManager.PROFILE_DELETE.equals(eventName)) {
            return SearchBuilderItem.ACTION_DELETE;
        } else {
            return SearchBuilderItem.ACTION_UNKNOWN;
        }
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
            ProfilePrivacy privacy = privacyLogic.getPrivacyRecordForUser(userId);
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

            if (privacy.getPersonalInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
                sb.append(sp.getNickname());
                sb.append(sp.getNotes());
            }
            if (privacy.getStaffInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
                sb.append(" ").append(sp.getStaffProfile());
                sb.append(" ").append(sp.getPublications());
            }
            if (privacy.getPersonalInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
                sb.append(" ").append(sp.getFavouriteBooks());
                sb.append(" ").append(sp.getFavouriteTvShows());
                sb.append(" ").append(sp.getFavouriteQuotes());
            }
            if (privacy.getStudentInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
                sb.append(" ").append(sp.getEducationCourse());
                sb.append(" ").append(sp.getEducationSubjects());
                sb.append(" ").append(sp.getCampus());
                sb.append(" ").append(sp.getEducationCourse());
                sb.append(" ").append(sp.getEducationSubjects());
            }
            if (privacy.getBusinessInfo() == ProfileConstants.PRIVACY_OPTION_EVERYONE) {
                sb.append(" ").append(sp.getBusinessBiography());
            }
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
        return SakaiPersonManager.PROFILE_UPDATE.equals(e.getEvent());
    }

    public boolean matches(String ref) {
        log.debug("matches: {}", ref);
        return splitAndValidate(ref) != null;
    }

    private String[] splitAndValidate(String ref) {

        String[] parts = ref.split(Entity.SEPARATOR);
        if (parts != null && parts.length == 6 && PROFILE.equals(parts[1])
                && TYPE.equals(parts[2]) && ID.equals(parts[4])) {
            return parts;
        } else {
            log.error("A profile update ref should have 5 components. Ref: {}", ref);
            return null;
        }
    }
}
