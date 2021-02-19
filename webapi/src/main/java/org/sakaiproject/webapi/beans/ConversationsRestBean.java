/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.beans;

import java.util.List;

import org.springframework.hateoas.EntityModel;

import org.sakaiproject.conversations.api.model.Settings;
import org.sakaiproject.conversations.api.model.Tag;

public class ConversationsRestBean {

    public String userId;
    public String siteId;
    public List<SimpleGroup> groups;
    public List<EntityModel> topics;
    public boolean canCreateTopic;
    public boolean canUpdatePermissions;
    public boolean canEditTags;
    public boolean canViewSiteStatistics;
    public boolean canPin;
    public boolean isInstructor;
    public boolean canViewAnonymous;
    public Settings settings;
    public boolean showGuidelines;
    public List<Tag> tags;
}
