/**
 * $Id$
 * $URL$
 * DeveloperHelperServiceMock.java - entity-broker - Jan 12, 2009 3:04:20 PM - azeckoski
 **********************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 **********************************************************************************/

package org.sakaiproject.entitybroker.util.devhelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.azeckoski.reflectutils.ReflectUtils;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.util.SakaiToolData;

/**
 * This is the Mock for the developer helper service,
 * allows the service to always be available even when someone has not implemented it for the
 * system that is using EB
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class DeveloperHelperServiceMock extends AbstractDeveloperHelperService {

    private Map<String, Object> settings = new HashMap<String, Object>();
    @SuppressWarnings("unchecked")
    public <T> T getConfigurationSetting(String settingName, T defaultValue) {
        T value = null;
        Object o = settings.get(settingName);
        if (defaultValue == null) {
            value = (T) o;
        } else {
            ReflectUtils.getInstance().convert( settings.get(settingName), defaultValue.getClass());
        }
        return value;
    }

    public static String defaultLocationId = "home";
    public static String defaultLocationRef = GROUP_BASE + defaultLocationId;
    public String getCurrentLocationId() {
        return defaultLocationId;
    }

    public String getCurrentLocationReference() {
        return defaultLocationRef;
    }

    public static String defaultToolId = "myTool";
    public static String defaultToolRef = "/tool/" + defaultToolId;
    public String getCurrentToolReference() {
        return defaultToolRef;
    }

    public static String defaultUserId = "az11111";
    public static String defaultUserEid = "aaronz";
    public static String defaultUserRef = USER_BASE + defaultUserId;
    public static String currentUserRef = defaultUserRef;
    public String getCurrentUserId() {
        if (currentUserRef != null) {
            return EntityReference.getIdFromRef(currentUserRef);
        }
        return null;
    }

    public static String lastCurrentUser = null;
    public String restoreCurrentUser() {
        return lastCurrentUser;
    }

    public String setCurrentUser(String userReference) {
        String lastCurrentUser = currentUserRef;
        currentUserRef = userReference;
        return lastCurrentUser;
    }

    public String getCurrentUserReference() {
        return currentUserRef;
    }

    public static String defaultServerURL = "http://localhost:8080";
    public static String defaultPortalURL = defaultServerURL + "/portal";
    public String getPortalURL() {
        return defaultPortalURL;
    }

    public String getServerURL() {
        return defaultServerURL;
    }

    public SakaiToolData getToolData(String toolRegistrationId, String locationReference) {
        SakaiToolData std = new SakaiToolData();
        std.setDescription("Fake tool description");
        std.setLocationReference(locationReference);
        std.setPlacementId("fakePlacement");
        std.setRegistrationId(toolRegistrationId);
        std.setTitle("Fake tool title");
        std.setToolURL(defaultPortalURL + locationReference + "/page/pageId");
        return std;
    }

    public String getToolViewURL(String toolRegistrationId, String localView,
            Map<String, String> parameters, String locationReference) {
        return defaultPortalURL + locationReference + "/page/pageId?toolstate-"+toolRegistrationId+"="+localView+"?thing=value";
    }

    public String getUserRefFromUserEid(String userEid) {
        if (defaultUserEid.equals(userEid)) {
            return getUserRefFromUserId(defaultUserId);
        }
        // just pretend the eid and id are the same for testing (this is not going to be the case in production)
        return getUserRefFromUserId(userEid);
    }

    // PERMS

    public static String defaultEntityRef = "/thing/123";
    public Set<String> getEntityReferencesForUserAndPermission(String userReference,
            String permission) {
        HashSet<String> s = new HashSet<String>();
        if (defaultUserRef.equals(userReference) && defaultPermAllowed.equals(permission)) {
            s.add(defaultEntityRef);
        }
        return s;
    }

    public static String defaultPermAllowed = "allow1";
    public Set<String> getUserReferencesForEntityReference(String reference, String permission) {
        HashSet<String> s = new HashSet<String>();
        if (defaultPermAllowed.equals(permission)) {
            s.add(defaultUserRef);
        }
        return s;
    }

    public boolean isUserAdmin(String userReference) {
        if ("admin".equals(userReference)) {
            return true;
        }
        return false;
    }

    public boolean isUserAllowedInEntityReference(String userReference, String permission,
            String reference) {
        if (defaultUserRef.equals(userReference)) {
            if (defaultPermAllowed.equals(permission)) {
                if (defaultEntityRef.equals(reference)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static HashSet<String> registeredPerms = new HashSet<String>();
    public void registerPermission(String permission) {
        registeredPerms.add(permission);
    }

}
