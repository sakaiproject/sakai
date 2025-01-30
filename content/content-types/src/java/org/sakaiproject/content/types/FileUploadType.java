/**********************************************************************************
 * $URL:$
 * $Id:$
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentPrintService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseResourceType;
import org.sakaiproject.content.util.BaseServiceLevelAction;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.ZipFileNumberException;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.util.Resource;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUploadType extends BaseResourceType {

    @Setter private ContentHostingService contentHostingService;
    @Setter private ContentTypeImageService contentTypeImageService;
    @Setter private ResourceTypeRegistry resourceTypeRegistry;
    @Setter private ServerConfigurationService serverConfigurationService;
	@Setter private SessionManager sessionManager;

    protected final String typeId = ResourceType.TYPE_UPLOAD;

    public void init() {
        String resourceClassName = serverConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
        String resourceBundleName = serverConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
        setMessageSource(Resource.getResourceLoader(resourceClassName, resourceBundleName));

        String helperId = "sakai.resource.type.helper";

        BaseInteractionAction createAction = new BaseInteractionAction(CREATE, ActionType.NEW_UPLOAD, typeId, helperId, localizer("create.uploads"));
        createAction.setRequiredPropertyKeys(Collections.singletonList(ResourceProperties.PROP_CONTENT_ENCODING));

        actions.put(ACCESS_PROPERTIES, new BaseServiceLevelAction(ACCESS_PROPERTIES, ActionType.VIEW_METADATA, typeId, false, localizer("action.access")));
        actions.put(COPY, new BaseServiceLevelAction(COPY, ActionType.COPY, typeId, true, localizer("action.copy")));
        actions.put(CREATE, createAction);
        actions.put(DELETE, new BaseServiceLevelAction(DELETE, ActionType.DELETE, typeId, true, localizer("action.delete")));
        actions.put(DUPLICATE, new BaseServiceLevelAction(DUPLICATE, ActionType.DUPLICATE, typeId, false, localizer("action.duplicate")));
        actions.put(EXPAND_ZIP_ARCHIVE, new FileUploadExpandAction(EXPAND_ZIP_ARCHIVE, ActionType.EXPAND_ZIP_ARCHIVE, typeId, false, localizer("action.expandziparchive")));
        actions.put(MAKE_SITE_PAGE, new MakeSitePageAction(MAKE_SITE_PAGE, ActionType.MAKE_SITE_PAGE, typeId));
        actions.put(MOVE, new BaseServiceLevelAction(MOVE, ActionType.MOVE, typeId, true, localizer("action.move")));
        actions.put(REPLACE_CONTENT, new BaseInteractionAction(REPLACE_CONTENT, ActionType.REPLACE_CONTENT, typeId, helperId, localizer("action.replace")));
        actions.put(REVISE_CONTENT, new FileUploadReviseAction(REVISE_CONTENT, ActionType.REVISE_CONTENT, typeId, helperId, localizer("action.revise")));
        actions.put(REVISE_METADATA, new BaseServiceLevelAction(REVISE_METADATA, ActionType.REVISE_METADATA, typeId, false, localizer("action.props")));

        if (serverConfigurationService.getString(ContentPrintService.CONTENT_PRINT_SERVICE_URL, null) != null) {
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
    public String getIconLocation(ContentEntity entity) {
        String iconLocation = null;
        if (entity instanceof ContentResource) {
            String mimetype = ((ContentResource) entity).getContentType();
            if (mimetype != null && !"".equals(mimetype.trim())) {
                if(mimetype.startsWith(ResourceType.MIME_TYPE_MICROSOFT)) {
                    mimetype = mimetype.replaceFirst(ResourceType.MIME_TYPE_MICROSOFT, "");
                }
                iconLocation = contentTypeImageService.getContentTypeImage(mimetype);
            }
        }
        return iconLocation;
    }

    @Override
    public String getIconClass(ContentEntity entity) {
        String iconClass = null;
        if (entity instanceof ContentResource) {
            String mimetype = ((ContentResource) entity).getContentType();
            if (mimetype != null && !"".equals(mimetype.trim())) {
                if(mimetype.startsWith(ResourceType.MIME_TYPE_MICROSOFT)) {
                    mimetype = mimetype.replaceFirst(ResourceType.MIME_TYPE_MICROSOFT, "");
                }
                iconClass = contentTypeImageService.getContentTypeImageClass(mimetype);
            }
        }
        return iconClass;
    }

    @Override
    public String getId() {
        return typeId;
    }

    @Override
    public String getLabel() {
        return localizer("type.upload").getI18nString();
    }

    @Override
    public String getLocalizedHoverText(ContentEntity entity) {
        String hoverText = localizer("type.upload").getI18nString();
        if (entity instanceof ContentResource) {
            String mimetype = ((ContentResource) entity).getContentType();
            if (mimetype != null && !"".equals(mimetype.trim())) {
                if(mimetype.startsWith(ResourceType.MIME_TYPE_MICROSOFT)) {
                    mimetype = mimetype.replaceFirst(ResourceType.MIME_TYPE_MICROSOFT, "");
                }
                hoverText = contentTypeImageService.getContentTypeDisplayName(mimetype);
            }
        }
        return hoverText;
    }

    /**
     * Cless for handling the Revising of HTML and TXT files through the web interface.
     */
    private class FileUploadReviseAction extends BaseInteractionAction {

        public FileUploadReviseAction(String id, ActionType actionType, String typeId, String helperId, Localizer localizer) {
            super(id, actionType, typeId, helperId, localizer);
        }

        @Override
        public List<String> getRequiredPropertyKeys() {
            List<String> rv = new ArrayList<>();
            rv.add(ResourceProperties.PROP_CONTENT_ENCODING);
            return rv;
        }

        @Override
        public boolean available(ContentEntity entity) {
            boolean available = false;
            if (entity instanceof ContentResource) {
                String mimetype = ((ContentResource) entity).getContentType();
                if (mimetype == null) {
                    mimetype = entity.getProperties().getProperty(ResourceProperties.PROP_CONTENT_TYPE);
                }
                available = (ResourceType.MIME_TYPE_HTML.equals(mimetype) || ResourceType.MIME_TYPE_TEXT.equals(mimetype));
            }
            return available;
        }
    }


    /**
     * Class for handling the Expanding of ZIP items.
     */
    private class FileUploadExpandAction extends BaseServiceLevelAction {

        // [WARN] Archive file handling compress/decompress feature contains bugs; exclude action item.
        // Disable property setting masking problematic code per will of the Community.
        // See Jira KNL-155/SAK-800 for more details.
        // also https://jira.sakaiproject.org/browse/KNL-273

        public FileUploadExpandAction(String id, ActionType actionType, String typeId, boolean multipleItemAction, Localizer localizer) {
            super(id, actionType, typeId, multipleItemAction, localizer);
        }

        @Override
        public void initializeAction(Reference reference) {
            try {
                contentHostingService.expandZippedResource(reference.getId());
            } catch (ZipFileNumberException e) {
                log.warn("The ZIP file cannot be expanded because contains more files than the maximum allowed.", e);
                ToolSession toolSession = sessionManager.getCurrentToolSession();
                Collection<String> errorMessages = (Collection<String>) toolSession.getAttribute("resources.request.message_list");
                if (errorMessages == null) {
                    errorMessages = new TreeSet<>();
                    toolSession.setAttribute("resources.request.message_list", errorMessages);
                }
                errorMessages.add(localizer("alert.zip.filenumber").getI18nString());
            } catch (Exception e) {
                log.error("Exception extracting zip content", e);
            }
        }

        @Override
        public boolean available(ContentEntity entity) {
            boolean enabled = false;
            if (entity instanceof ContentResource) {
                enabled = serverConfigurationService.getBoolean(ContentHostingService.RESOURCES_ZIP_ENABLE, true)
                        || serverConfigurationService.getBoolean(ContentHostingService.RESOURCES_ZIP_ENABLE_EXPAND, true);
                enabled = enabled && entity.getId().toLowerCase().endsWith(".zip");
            }
            return enabled;
        }
    }

}
