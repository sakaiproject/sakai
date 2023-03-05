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

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.api.ContentPrintService;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseResourceType;
import org.sakaiproject.content.util.BaseServiceLevelAction;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.util.Resource;

import lombok.Setter;

public class TextDocumentType extends BaseResourceType {

    @Setter private ContentPrintService contentPrintService;
    @Setter private ResourceTypeRegistry resourceTypeRegistry;
    @Setter private org.sakaiproject.component.api.ServerConfigurationService serverConfigurationService;

    protected String typeId = ResourceType.TYPE_TEXT;

    public void init() {
        String resourceClassName = serverConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
        String resourceBundleName = serverConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
        setMessageSource(Resource.getResourceLoader(resourceClassName, resourceBundleName));

        String helperId = "sakai.resource.type.helper";

        actions.put(CREATE, new TextDocumentCreateAction(CREATE, ActionType.CREATE, typeId, helperId, localizer("create.text")));
        // actions.put(ACCESS_CONTENT, new TextDocumentAccessAction());
        actions.put(REVISE_CONTENT, new TextDocumentReviseAction(REVISE_CONTENT, ActionType.REVISE_CONTENT, typeId, helperId, localizer("action.revise")));
        actions.put(REPLACE_CONTENT, new TextDocumentReplaceAction(REPLACE_CONTENT, ActionType.REPLACE_CONTENT, typeId, helperId, localizer("action.replace")));
        actions.put(ACCESS_PROPERTIES, new BaseServiceLevelAction(ACCESS_PROPERTIES, ActionType.VIEW_METADATA, typeId, false, localizer("action.access")));
        actions.put(REVISE_METADATA, new BaseServiceLevelAction(REVISE_METADATA, ActionType.REVISE_METADATA, typeId, false, localizer("action.props")));
        actions.put(COPY, new BaseServiceLevelAction(COPY, ActionType.COPY, typeId, true, localizer("action.copy")));
        actions.put(DUPLICATE, new BaseServiceLevelAction(DUPLICATE, ActionType.DUPLICATE, typeId, false, localizer("action.duplicate")));
        actions.put(MOVE, new BaseServiceLevelAction(MOVE, ActionType.MOVE, typeId, true, localizer("action.move")));
        actions.put(DELETE, new BaseServiceLevelAction(DELETE, ActionType.DELETE, typeId, true, localizer("action.delete")));
        actions.put(MAKE_SITE_PAGE, new MakeSitePageAction(MAKE_SITE_PAGE, ActionType.MAKE_SITE_PAGE, typeId));
        if (serverConfigurationService.getString(contentPrintService.CONTENT_PRINT_SERVICE_URL, null) != null) {
            // print service url is provided. Add the Print option.
            actions.put(PRINT_FILE, new BaseServiceLevelAction(PRINT_FILE, ActionType.PRINT_FILE, typeId, false, localizer("action.printfile")));
        }

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
        return localizer("type.text").getI18nString();
    }

    @Override
    public String getLocalizedHoverText(ContentEntity member) {
        return localizer("type.text").getI18nString();
    }

    public class TextDocumentReplaceAction extends BaseInteractionAction {

        public TextDocumentReplaceAction(String id, ActionType actionType, String typeId, String helperId, Localizer localizer) {
            super(id, actionType, typeId, helperId, localizer);
        }

        @Override
        public List<String> getRequiredPropertyKeys() {
            List<String> rv = new ArrayList<>();
            rv.add(ResourceProperties.PROP_CONTENT_ENCODING);
            return rv;
        }
    }

    public class TextDocumentCreateAction extends BaseInteractionAction {
        public TextDocumentCreateAction(String id, ActionType actionType, String typeId, String helperId, Localizer localizer) {
            super(id, actionType, typeId, helperId, localizer);
        }

        @Override
        public List<String> getRequiredPropertyKeys() {
            List<String> rv = new ArrayList<>();
            rv.add(ResourceProperties.PROP_CONTENT_ENCODING);
            return rv;
        }
    }

    public class TextDocumentReviseAction extends BaseInteractionAction {
        public TextDocumentReviseAction(String id, ActionType actionType, String typeId, String helperId, Localizer localizer) {
            super(id, actionType, typeId, helperId, localizer);
        }

        @Override
        public List<String> getRequiredPropertyKeys() {
            List<String> rv = new ArrayList<>();
            rv.add(ResourceProperties.PROP_CONTENT_ENCODING);
            return rv;
        }
    }

}
