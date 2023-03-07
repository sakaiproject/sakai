/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.types;

import static org.sakaiproject.content.api.ResourceToolAction.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseResourceType;
import org.sakaiproject.content.util.BaseServiceLevelAction;
import org.sakaiproject.util.Resource;

import lombok.Setter;

public class UrlResourceType extends BaseResourceType {

    @Setter private ResourceTypeRegistry resourceTypeRegistry;
    @Setter private ServerConfigurationService serverConfigurationService;

    protected String typeId = ResourceType.TYPE_URL;

    public void init() {
        String resourceClassName = serverConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
        String resourceBundleName = serverConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
        setMessageSource(Resource.getResourceLoader(resourceClassName, resourceBundleName));

        String helperId = "sakai.resource.type.helper";

        actions.put(CREATE, new BaseInteractionAction(CREATE, ActionType.NEW_URLS, typeId, helperId, localizer("create.urls")));
        actions.put(REVISE_CONTENT, new BaseInteractionAction(REVISE_CONTENT, ActionType.REVISE_CONTENT, typeId, helperId, localizer("action.revise")));
        actions.put(ACCESS_PROPERTIES, new BaseServiceLevelAction(ACCESS_PROPERTIES, ActionType.VIEW_METADATA, typeId, false, localizer("action.access")));
        actions.put(REVISE_METADATA, new BaseServiceLevelAction(REVISE_METADATA, ActionType.REVISE_METADATA, typeId, false, localizer("action.props")));
        actions.put(DUPLICATE, new BaseServiceLevelAction(DUPLICATE, ActionType.DUPLICATE, typeId, false, localizer("action.duplicate")));
        actions.put(COPY, new BaseServiceLevelAction(COPY, ActionType.COPY, typeId, true, localizer("action.copy")));
        actions.put(MOVE, new BaseServiceLevelAction(MOVE, ActionType.MOVE, typeId, true, localizer("action.move")));
        actions.put(DELETE, new BaseServiceLevelAction(DELETE, ActionType.DELETE, typeId, true, localizer("action.delete")));
        actions.put(MAKE_SITE_PAGE, new MakeSitePageAction(MAKE_SITE_PAGE, ActionType.MAKE_SITE_PAGE, typeId));

        // initialize actionMap with an empty List for each ActionType
        for (ActionType type : ActionType.values()) {
            actionMap.put(type, new ArrayList<>());
        }

        // for each action in actions, add a link in actionMap
        for (String id : actions.keySet()) {
            ResourceToolAction action = actions.get(id);
            List<ResourceToolAction> list = actionMap.computeIfAbsent(action.getActionType(), k -> new ArrayList<>());
            list.add(action);
        }

        resourceTypeRegistry.register(this);
    }

    @Override
    public String getId() {
        return typeId;
    }

    @Override
    public String getLabel() {
        return localizer("type.url").getI18nString();
    }

    @Override
    public String getLocalizedHoverText(ContentEntity member) {
        return localizer("type.url").getI18nString();
    }

    @Override
    public boolean hasRightsDialog() {
        return serverConfigurationService.getBoolean("content.url.rightsdialog", true);
    }

}
