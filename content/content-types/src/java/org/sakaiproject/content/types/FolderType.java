/**********************************************************************************
 * $URL$
 * $Id$
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
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ExpandableResourceType;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.util.BaseInteractionAction;
import org.sakaiproject.content.util.BaseResourceType;
import org.sakaiproject.content.util.BaseServiceLevelAction;
import org.sakaiproject.content.util.ZipContentUtil;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.util.Resource;

@Slf4j
public class FolderType extends BaseResourceType implements ExpandableResourceType {

    @Setter private ContentHostingService contentHostingService;
    @Setter private EntityManager entityManager;
    @Setter private ResourceTypeRegistry resourceTypeRegistry;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SessionManager sessionManager;
    @Setter private SiteService siteService;

    protected String typeId = ResourceType.TYPE_FOLDER;

    public void init() {
        String resourceClassName = serverConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
        String resourceBundleName = serverConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
        setMessageSource(Resource.getResourceLoader(resourceClassName, resourceBundleName));

        String helperId = "sakai.resource.type.helper";

        actions.put(CREATE, new BaseInteractionAction(CREATE, ActionType.NEW_FOLDER, typeId, helperId, localizer("create.folder")));
        actions.put(ACCESS_PROPERTIES, new BaseServiceLevelAction(ACCESS_PROPERTIES, ActionType.VIEW_METADATA, typeId, false, localizer("action.access")));
        actions.put(REVISE_METADATA, new BaseServiceLevelAction(REVISE_METADATA, ActionType.REVISE_METADATA, typeId, false, localizer("action.props")));
        actions.put(PASTE_COPIED, new BaseServiceLevelAction(PASTE_COPIED, ActionType.PASTE_COPIED, typeId, false, localizer("action.pastecopy")));
        actions.put(PASTE_MOVED, new BaseServiceLevelAction(PASTE_MOVED, ActionType.PASTE_MOVED, typeId, false, localizer("action.pastemove")));
        actions.put(COPY, new FolderCopyAction(COPY, ActionType.COPY, typeId, true, localizer("action.copy")));
        actions.put(MOVE, new FolderMoveAction(MOVE, ActionType.MOVE, typeId, true, localizer("action.move")));
        actions.put(DELETE, new FolderDeleteAction(DELETE, ActionType.DELETE, typeId, true, localizer("action.delete")));
        actions.put(REORDER, new FolderReorderAction(REORDER, ActionType.REVISE_ORDER, typeId, false, localizer("action.reorder")));
        actions.put(PERMISSIONS, new FolderPermissionsAction(PERMISSIONS, ActionType.REVISE_PERMISSIONS, typeId, "sakai.permissions.helper", localizer("action.permissions")));
        actions.put(EXPAND, new BaseServiceLevelAction(EXPAND, ActionType.EXPAND_FOLDER, typeId, false, localizer("expand.item")));
        actions.put(COLLAPSE, new BaseServiceLevelAction(COLLAPSE, ActionType.COLLAPSE_FOLDER, typeId, false, localizer("collapse.item")));
        actions.put(COMPRESS_ZIP_FOLDER, new FolderCompressAction(COMPRESS_ZIP_FOLDER, ActionType.COMPRESS_ZIP_FOLDER, typeId, false, localizer("action.compresszipfolder")));
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
    public String getIconLocation(ContentEntity entity, boolean expanded) {
        String iconLocation = "sakai/dir_openroot.gif";
        if (entity.isCollection()) {
            ContentCollection collection = (ContentCollection) entity;
            int memberCount = collection.getMemberCount();
            if (memberCount == 0) {
                iconLocation = "sakai/dir_closed.gif";
            } else if (memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT) {
                iconLocation = "sakai/dir_unexpand.gif";
            } else if (expanded) {
                iconLocation = "sakai/dir_openminus.gif";
            } else {
                iconLocation = "sakai/dir_closedplus.gif";
            }
        }
        return iconLocation;
    }

    @Override
    public String getIconLocation(ContentEntity entity) {
        String iconLocation = "sakai/dir_openroot.gif";
        if (entity != null && entity.isCollection()) {
            ContentCollection collection = (ContentCollection) entity;
            int memberCount = collection.getMemberCount();
            if (memberCount == 0) {
                iconLocation = "sakai/dir_closed.gif";
            } else if (memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT) {
                iconLocation = "sakai/dir_unexpand.gif";
            }
        }
        return iconLocation;
    }

    @Override
    public String getIconClass(ContentEntity entity, boolean expanded) {
        String iconClass = "fa fa-folder-open-o";
        if (entity.isCollection()) {
            ContentCollection collection = (ContentCollection) entity;
            int memberCount = collection.getMemberCount();
            if (memberCount == 0) {
                iconClass = "fa fa-folder-o";
            } else if (memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT) {
                iconClass = "fa fa-folder";
            } else if (expanded) {
                iconClass = "fa fa-folder-open";
            } else {
                iconClass = "fa fa-folder";
            }
        }
        return iconClass;
    }

    @Override
    public String getIconClass(ContentEntity entity) {
        String iconClass = "fa fa-folder-open-o";
        if (entity != null && entity.isCollection()) {
            ContentCollection collection = (ContentCollection) entity;
            int memberCount = collection.getMemberCount();
            if (memberCount == 0) {
                iconClass = "fa fa-folder-o";
            } else if (memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT) {
                iconClass = "fa fa-folder";
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
        return localizer("type.folder").getI18nString();
    }

    @Override
    public String getLocalizedHoverText(ContentEntity entity, boolean expanded) {
        String hoverText = localizer("type.folder").getI18nString();
        if (entity.isCollection()) {
            ContentCollection collection = (ContentCollection) entity;
            int memberCount = collection.getMemberCount();
            if (memberCount == 0) {
                hoverText = localizer("type.folder").getI18nString();
            } else if (memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT) {
                hoverText = localizer("list.toobig").getI18nString();
            } else if (expanded) {
                hoverText = localizer("sh.close").getI18nString();
            } else {
                hoverText = localizer("sh.open").getI18nString();
            }
        }
        return hoverText;
    }

    @Override
    public String getLocalizedHoverText(ContentEntity member) {
        return localizer("type.folder").getI18nString();
    }

    @Override
    public boolean hasRightsDialog() {
        return false;
    }

    @Override
    public ServiceLevelAction getCollapseAction() {
        return (ServiceLevelAction) this.actions.get(COLLAPSE);
    }

    @Override
    public ServiceLevelAction getExpandAction() {
        return (ServiceLevelAction) this.actions.get(EXPAND);
    }

    @Override
    public boolean allowAddAction(ResourceToolAction action, ContentEntity entity) {
        // allow all add actions in regular folders
        return true;
    }

    public class FolderPermissionsAction extends BaseInteractionAction {

        public FolderPermissionsAction(String id, ActionType actionType,
                                       String typeId, String helperId, Localizer localizer) {
            super(id, actionType, typeId, helperId, localizer);
        }

        public String initializeAction(Reference reference) {
            ToolSession toolSession = sessionManager.getCurrentToolSession();

            toolSession.setAttribute(PermissionsHelper.TARGET_REF, reference.getReference());

            // use the folder's context (as a site and as a resource) for roles
            Collection<String> rolesRefs = new ArrayList<>();
            rolesRefs.add(siteService.siteReference(reference.getContext()));
            rolesRefs.add(reference.getReference());
            toolSession.setAttribute(PermissionsHelper.ROLES_REF, rolesRefs);

            // ... with this description
            String title = reference.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
            String[] args = {title};

            toolSession.setAttribute(PermissionsHelper.DESCRIPTION, formattedLocalizer("title.permissions", args).getI18nString());

            // ... showing only locks that are prpefixed with this
            toolSession.setAttribute(PermissionsHelper.PREFIX, "content.");

            return BaseInteractionAction.getInitializationId(reference.getReference(), this.getTypeId(), this.getId());
        }

        public boolean available(ContentEntity entity) {
            boolean ok = true;
            if (entity == null || ContentHostingService.ROOT_COLLECTIONS.contains(entity.getId())) {
                ok = false;
            } else if (entity.getId().startsWith(ContentHostingService.COLLECTION_DROPBOX)) {
                ok = false;
            } else {
                ContentCollection parent = entity.getContainingCollection();
                if (parent == null) {
                    ok = false;
                }
            }
            return ok;
        }

    }

    public class FolderReorderAction extends BaseServiceLevelAction {

        public FolderReorderAction(String id, ActionType actionType, String typeId, boolean multipleItemAction, Localizer localizer) {
            super(id, actionType, typeId, multipleItemAction, localizer);
        }

        @Override
        public boolean available(ContentEntity entity) {
            boolean isAvailable = true;

            if (entity instanceof ContentCollection) {
                isAvailable = ((ContentCollection) entity).getMemberCount() > 1;
            }
            return isAvailable;
        }
    }


    public class FolderCopyAction extends BaseServiceLevelAction {
        public FolderCopyAction(String id, ActionType actionType, String typeId, boolean multipleItemAction, Localizer localizer) {
            super(id, actionType, typeId, multipleItemAction, localizer);
        }

        @Override
        public boolean available(ContentEntity entity) {
            boolean ok = true;
            if (entity == null || ContentHostingService.ROOT_COLLECTIONS.contains(entity.getId())) {
                ok = false;
            } else {
                ContentCollection parent = entity.getContainingCollection();
                if (parent == null) {
                    ok = false;
                }
            }
            return ok;
        }

    }


    public class FolderDeleteAction extends BaseServiceLevelAction {
        public FolderDeleteAction(String id, ActionType actionType, String typeId, boolean multipleItemAction, Localizer localizer) {
            super(id, actionType, typeId, multipleItemAction, localizer);
        }

        public boolean available(ContentEntity entity) {
            boolean ok = true;
            if (entity == null || ContentHostingService.ROOT_COLLECTIONS.contains(entity.getId())) {
                ok = false;
            } else {
                ContentCollection parent = entity.getContainingCollection();
                if (parent == null) {
                    ok = false;
                } else {
                    ContentCollection grandparent = parent.getContainingCollection();
                    if (grandparent != null && ContentHostingService.COLLECTION_DROPBOX.equals(grandparent.getId())) {
                        Reference ref = entityManager.newReference(entity.getReference());
                        if (ref != null) {
                            String siteId = ref.getContext();
                            if (siteId != null) {
                                String dropboxId = contentHostingService.getDropboxCollection(siteId);
                                if (entity.getId().equals(dropboxId)) {
                                    ok = false;
                                }
                            }
                        }
                    }
                }
            }
            return ok;
        }

    }

    public class FolderMoveAction extends BaseServiceLevelAction {

        public FolderMoveAction(String id, ActionType actionType, String typeId, boolean multipleItemAction, Localizer localizer) {
            super(id, actionType, typeId, multipleItemAction, localizer);
        }

        @Override
        public boolean available(ContentEntity entity) {
            boolean ok = true;
            if (entity == null || ContentHostingService.ROOT_COLLECTIONS.contains(entity.getId())) {
                ok = false;
            } else if (entity.getId().startsWith(ContentHostingService.COLLECTION_DROPBOX)) {
                ok = false;
            } else {
                ContentCollection parent = entity.getContainingCollection();
                if (parent == null) {
                    ok = false;
                }
            }
            return ok;
        }
    }

    public class FolderCompressAction extends BaseServiceLevelAction {

        public FolderCompressAction(String id, ActionType actionType, String typeId, boolean multipleItemAction, Localizer localizer) {
            super(id, actionType, typeId, multipleItemAction, localizer);
        }


        public void initializeAction(Reference reference) {
            ZipContentUtil zipUtil = new ZipContentUtil();
            try {
                zipUtil.compressFolder(reference);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }

        public boolean available(ContentEntity entity) {
            return serverConfigurationService.getBoolean(ContentHostingService.RESOURCES_ZIP_ENABLE, true)
                    || serverConfigurationService.getBoolean(ContentHostingService.RESOURCES_ZIP_ENABLE_COMPRESS, true);
        }
    }
}
