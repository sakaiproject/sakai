/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.content.entityproviders;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.googledrive.model.GoogleDriveItem;
import org.sakaiproject.googledrive.service.GoogleDriveService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;

public class AjaxContentEntityProvider extends AbstractEntityProvider implements EntityProvider, AutoRegisterEntityProvider, ActionsExecutable, Outputable, Describeable {

    public final static String ENTITY_PREFIX = "contentajax";

    @Getter @Setter private ServerConfigurationService serverConfigurationService;
    @Getter @Setter private GoogleDriveService googleDriveService;
    @Getter @Setter private UserDirectoryService userDirectoryService;
    @Getter @Setter private SessionManager sessionManager;
    @Getter @Setter private ContentTypeImageService contentTypeImageService;

    @Override
    public String getEntityPrefix() {
        return ENTITY_PREFIX;
    }

    @Override
    public String[] getHandledOutputFormats() {
        return new String[] { Formats.JSON };
    }

    @EntityCustomAction(action = "doNavigateGoogleDrive", viewKey = EntityView.VIEW_LIST)
    public JSONArray handleGoogleDriveData(EntityReference view, Map<String,Object> params) {
        boolean googledriveOn = serverConfigurationService.getBoolean(GoogleDriveService.GOOGLEDRIVE_ENABLED, Boolean.FALSE);
        String googledriveCollectionId = (String) params.get("googledriveCollectionId");
        JSONArray json = new JSONArray();

        if (googledriveOn){
            List<GoogleDriveItem> children;

            if (googledriveCollectionId == null) {
                children = googleDriveService.getDriveRootItems(userDirectoryService.getCurrentUser().getId());

            } else {
                int depth = 0;
                if (params.get("googledriveCollectionDepth") != null) depth = Integer.parseInt(params.get("googledriveCollectionDepth").toString());
                children = googleDriveService.getDriveChildrenItems(userDirectoryService.getCurrentUser().getId(), googledriveCollectionId, depth);
            }

            for (GoogleDriveItem child : children) {
                JSONObject jsonObj = new JSONObject();
                
                jsonObj.put("view", child.getViewUrl());
                
                jsonObj.put("id", child.getGoogleDriveItemId());
                jsonObj.put("url", child.getDownloadUrl());
                jsonObj.put("text", child.getName());
                JSONObject jsonState = new JSONObject();
                jsonState.put("opened", child.isExpanded());
                jsonState.put("selected", child.isExpanded());
                jsonObj.put("state", jsonState);   
                jsonObj.put("depth", child.getDepth());
                
                String jstreeClass = "";
                if(child.getDownloadUrl() == null){
                    jstreeClass += "no-attach ";
                }

                if (child.getIcon() != null) jsonObj.put("icon", child.getIcon()); 
                else jsonObj.put("icon", contentTypeImageService.getContentTypeImageClass(child.getMimeType()));

                if (child.isFolder()) {
                    jstreeClass += "is-folder ";
                    jsonObj.put("children", true);
                }

                JSONObject liAttrs = new JSONObject();
                liAttrs.put("class", jstreeClass);
                liAttrs.put("data-view", child.getViewUrl());
                liAttrs.put("data-url", child.getDownloadUrl());
                jsonObj.put("li_attr", liAttrs);
                
                json.add(jsonObj);
            }
        }

        JSONArray finalJson = new JSONArray();
        finalJson.add(json);
        return finalJson;
    }
}
