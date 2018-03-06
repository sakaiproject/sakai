/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.component.app.messageforums.entity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.entity.ForumEntityProvider;
import org.sakaiproject.api.app.messageforums.entity.ForumTopicEntityProvider;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;

@Slf4j
public class ForumEntityProviderImpl implements ForumEntityProvider, AutoRegisterEntityProvider,
    PropertyProvideable {

  private DiscussionForumManager forumManager;

  public String getEntityPrefix() {
    return ENTITY_PREFIX;
  }

  public boolean entityExists(String id) {
    DiscussionForum forum = null;
    try {
      forum = forumManager.getForumById(Long.valueOf(id));
    }
    catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return (forum != null);
  }

  public List<String> findEntityRefs(String[] prefixes, String[] name, String[] searchValue,
      boolean exactMatch) {
    List<String> rv = new ArrayList<String>();

    String userId = null;
    String siteId = null;

    if (ENTITY_PREFIX.equals(prefixes[0])) {

      for (int i = 0; i < name.length; i++) {
        if ("context".equalsIgnoreCase(name[i]) || "site".equalsIgnoreCase(name[i]))
          siteId = searchValue[i];
        else if ("user".equalsIgnoreCase(name[i]) || "userId".equalsIgnoreCase(name[i]))
          userId = searchValue[i];
      }
      
      String siteRef = siteId;
      if(siteRef != null && !siteRef.startsWith("/site/")){
    	  siteRef = "/site/" + siteRef;
      }

      if (siteId != null && userId != null) {
        List<DiscussionForum> forums = forumManager.getDiscussionForumsByContextId(siteId);
        for (int i = 0; i < forums.size(); i++) {
          // TODO: authz is way too basic, someone more hip to message center please improve...
          //This should also allow people with read access to an item to link to it
          if (forumManager.isInstructor(userId, siteRef)
              || userId.equals(forums.get(i).getCreatedBy())) {
            rv.add("/" + ENTITY_PREFIX + "/" + forums.get(i).getId().toString());
          }
        }
      }
    }

    return rv;
  }

  public Map<String, String> getProperties(String reference) {
    Map<String, String> props = new HashMap<String, String>();
    DiscussionForum forum =
      forumManager.getForumById(Long.valueOf(reference.substring(reference.lastIndexOf("/") + 1)));

    props.put("title", forum.getTitle());
    props.put("author", forum.getCreatedBy());
    if (forum.getCreated() != null)
      props.put("date", DateFormat.getInstance().format(forum.getCreated()));
    if (forum.getModified() != null) {
      props.put("modified_date", DateFormat.getInstance().format(forum.getModified()));
      props.put("modified_by", forum.getModifiedBy());
    }
    props.put("short_description", forum.getShortDescription());
    props.put("description", forum.getExtendedDescription());
    if (forum.getDraft() != null)
      props.put("draft", forum.getDraft().toString());
    if (forum.getModerated() != null)
      props.put("moderated", forum.getModerated().toString());
    props.put("child_provider", ForumTopicEntityProvider.ENTITY_PREFIX);
    props.put("assignment_name", forum.getDefaultAssignName());
    return props;
  }

  public String getPropertyValue(String reference, String name) {
    // TODO: don't be so lazy, just get what we need...
    Map<String, String> props = getProperties(reference);
    return props.get(name);
  }

  public void setPropertyValue(String reference, String name, String value) {
    // This does nothing for now... we could all the setting of many published assessment properties
    // here though... if you're feeling jumpy feel free.
  }

  public void setForumManager(DiscussionForumManager forumManager) {
    this.forumManager = forumManager;
  }
}
