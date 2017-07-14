/*************************************************************************************
 * Copyright 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.

 *************************************************************************************/
package org.sakaiproject.commons.api;

import java.util.Map;
import java.util.Observer;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;

/**
 * @author Adrian Fish (adrian.r.fish@gmail.com)
 */
public interface SakaiProxy {

    public String getCurrentSiteId();

    public Site getSiteOrNull(String siteId);

    /**
     * Returns the locale_string property of the current site.
     *
     * @return the locale_string property of the current site.
     */
    public String getCurrentSiteLocale();

    public Tool getCurrentTool();

    public String getCurrentToolId();

    public Session getCurrentSession();

    public String getCurrentUserId();

    public ToolSession getCurrentToolSession();

    public void setCurrentToolSession(ToolSession toolSession);

    public String getDisplayNameForTheUser(String userId);

    public boolean isCurrentUserAdmin();

    public String getPortalUrl();

    public void registerEntityProducer(EntityProducer entityProducer);

    public void registerFunction(String function);

    public boolean isAllowedFunction(String function, String siteId);

    public boolean isAllowedFunction(String function, Role role);

    public void postEvent(String event, String entityId, String siteId);

    public Set<String> getSiteUsers(String siteId);

    public String getCommonsToolId(String siteId);

    public Set<String> getSitePermissionsForCurrentUser(String siteId, String embedder);

    public Map<String, Set<String>> getSitePermissions(String siteId);

    public boolean setPermissionsForSite(String siteId, Map<String, Object> params);

    public Cache getCache(String cache);

    public boolean isUserSite(String siteId);

    public void addObserver(Observer observer);

    public String storeFile(FileItem fileItem, String siteId);
}
