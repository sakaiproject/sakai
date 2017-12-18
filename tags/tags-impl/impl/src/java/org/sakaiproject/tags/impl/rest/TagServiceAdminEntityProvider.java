/**********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 *
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tags.impl.rest;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.tags.api.Errors;
import org.sakaiproject.tags.api.TagCollection;
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tags.api.TagServiceException;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Web services for managing tags.  Intended for administrator use.
 */
@Slf4j
public class TagServiceAdminEntityProvider implements EntityProvider, AutoRegisterEntityProvider, ActionsExecutable, Outputable, Describeable {

    private static final String ADMIN_SITE_REALM = "/site/!admin";
    private static final String SAKAI_SESSION_TOKEN_PROPERTY = "sakai.tagservice-admin.token";
    private static final String REQUEST_SESSION_PARAMETER = "session";

    protected DeveloperHelperService developerHelperService;
    private EntityProviderManager entityProviderManager;
    private  SessionManager sessionManager = (SessionManager) ComponentManager.get("org.sakaiproject.tool.api.SessionManager");
    private  SecurityService securityService = (SecurityService) ComponentManager.get("org.sakaiproject.authz.api.SecurityService");

    @Override
    public String[] getHandledOutputFormats() {
        return new String[] { Formats.JSON,Formats.XML };
    }

    @Override
    public String getEntityPrefix() {
        return "tagservice-admin";
    }

    /**
     * Return a Tags Service service token to be passed with subsequent requests.
     */
    @EntityCustomAction(action = "startSession", viewKey = EntityView.VIEW_NEW)
    public String startSession(EntityView view, Map<String, Object> params) {
        try {
            assertPermission();

            JSONObject result = new JSONObject();
            String newSessionId = mintSessionId();
            result.put(REQUEST_SESSION_PARAMETER, newSessionId);

            sessionManager.getCurrentSession().setAttribute(SAKAI_SESSION_TOKEN_PROPERTY, newSessionId);

            return result.toJSONString();
        } catch (Exception e) {
            return respondWithError(e);
        }
    }


    @EntityCustomAction(action = "createTag", viewKey = EntityView.VIEW_NEW)
    public String createTag(EntityView view, Map<String, Object> params) {
        try {
            assertSession(params);

            WrappedParams wp = new WrappedParams(params);

            //Mandatory fields
            String tagId = UUID.randomUUID().toString();
            String tagCollectionId = wp.getString("tagcollectionid");
            String tagLabel = wp.getString("taglabel");


            //Optional fields (default values)
            String description = null;
            String externalId = null;
            String alternativeLabels = null;
            Boolean externalCreation = Boolean.TRUE;
            long  externalCreationDate = 0L;
            Boolean externalUpdate= Boolean.TRUE;
            long  lastUpdateDateInExternalSystem = 0L;
            String parentId = null;
            String externalHierarchyCode = null;
            String externalType = null;
            String data = null;


            if (wp.containsKey("description")){
                description = wp.getString("description");
            }
            if (wp.containsKey("externalid")){
                externalId = wp.getString("externalid");
            }
            if (wp.containsKey("alternativelabels")){
                alternativeLabels = wp.getString("alternativelabels");
            }
            if (wp.containsKey("externalcreation")){
                externalCreation = wp.getBoolean("externalcreation");
            }
            if (wp.containsKey("externalcreationdate")){
                externalCreationDate = wp.getEpochMS("externalcreationdate");
            }
            if (wp.containsKey("externalupdate")){
                externalUpdate= wp.getBoolean("externalupdate");
            }
            if (wp.containsKey("lastupdatedateinexternalsystem")){
                lastUpdateDateInExternalSystem = wp.getEpochMS("lastupdatedateinexternalsystem");
            }
            if (wp.containsKey("parentid")){
                parentId = wp.getString("parentid");
            }
            if (wp.containsKey("externalhierarchycode")){
                externalHierarchyCode = wp.getString("externalhierarchycode");
            }
            if (wp.containsKey("externaltype")){
                externalType = wp.getString("externaltype");
            }
            if (wp.containsKey("data")) {
                data = wp.getString("data");
            }



            Tag tag = new Tag(tagId,
                    tagCollectionId,
                    tagLabel,
                    description,
                    null,
                    0L,
                    null,
                    0L,
                    externalId,
                    alternativeLabels,
                    externalCreation,
                    externalCreationDate,
                    externalUpdate,
                    lastUpdateDateInExternalSystem,
                    parentId,
                    externalHierarchyCode,
                    externalType,
                    data,
                    null);


            Errors errors = tag.validate();

            if (errors.hasErrors()) {
                return respondWithError(errors);
            }

            String uuid = tagService().getTags().createTag(tag);

            JSONObject result = new JSONObject();
            result.put("status", "OK");
            result.put("tagid", uuid);
            return result.toJSONString();
        } catch (Exception e) {
            return respondWithError(e);
        }
    }

    @EntityCustomAction(action = "createTagCollection", viewKey = EntityView.VIEW_NEW)
    public String createTagCollection(EntityView view, Map<String, Object> params) {
        try {
            assertSession(params);

            WrappedParams wp = new WrappedParams(params);


            //Mandatory fields
            String tagCollectionId = UUID.randomUUID().toString();
            String  name = wp.getString("name");

            //Optional fields (default values)
            String description = null;
            String externalsourcename = null;
            String externalsourcedescription = null;

            Boolean externalupdate = Boolean.TRUE;
            Boolean externalcreation = Boolean.TRUE;
            Long lastsynchronizationdate = 0L;
            Long lastupdatedateinexternalsystem = 0L;


            if (wp.containsKey("description")){
                description = wp.getString("description");
            }
            if (wp.containsKey("externalsourcename")){
                externalsourcename = wp.getString("externalsourcename");
            }
            if (wp.containsKey("externalsourcedescription")){
                externalsourcedescription = wp.getString("externalsourcedescription");
            }
            if (wp.containsKey("externalcreation")){
                externalcreation = wp.getBoolean("externalcreation");
            }
            if (wp.containsKey("externalupdate")){
                externalupdate = wp.getBoolean("externalupdate");
            }
            if (wp.containsKey("lastsynchronizationdate")){
                lastsynchronizationdate = wp.getEpochMS("lastsynchronizationdate");
            }
            if (wp.containsKey("lastupdatedateinexternalsystem")) {
                lastupdatedateinexternalsystem = wp.getEpochMS("lastupdatedateinexternalsystem");
            }



            TagCollection tagCollection = new TagCollection(tagCollectionId,
                    name,
                    description,
                    null,
                    0L,
                    externalsourcename,
                    externalsourcedescription,
                    null,
                    0L,
                    externalupdate,
                    externalcreation,
                    lastsynchronizationdate,
                    lastupdatedateinexternalsystem);


            Errors errors = tagCollection.validate();

            if (errors.hasErrors()) {
                return respondWithError(errors);
            }

            String uuid = tagService().getTagCollections().createTagCollection(tagCollection);

            JSONObject result = new JSONObject();
            result.put("status", "OK");
            result.put("tagcollectionid", uuid);
            return result.toJSONString();
        } catch (Exception e) {
            return respondWithError(e);
        }
    }


    @EntityCustomAction(action = "deleteTag", viewKey = EntityView.VIEW_NEW)
    public String deleteTag(EntityView view, Map<String, Object> params) {
        try {
            assertSession(params);

            WrappedParams wp = new WrappedParams(params);

            String uuid = wp.getString("id");
            tagService().getTags().deleteTag(uuid);

            JSONObject result = new JSONObject();
            result.put("status", "OK");
            result.put("tagid", uuid);
            return result.toJSONString();
        } catch (Exception e) {
            return respondWithError(e);
        }
    }

    @EntityCustomAction(action = "deleteTagCollection", viewKey = EntityView.VIEW_NEW)
    public String deleteTagCollection(EntityView view, Map<String, Object> params) {
        try {
            assertSession(params);

            WrappedParams wp = new WrappedParams(params);

            String uuid = wp.getString("id");
            tagService().getTagCollections().deleteTagCollection(uuid);

            JSONObject result = new JSONObject();
            result.put("status", "OK");
            result.put("tagcollectionid", uuid);
            return result.toJSONString();
        } catch (Exception e) {
            return respondWithError(e);
        }
    }



    @EntityCustomAction(action = "updateTag", viewKey = EntityView.VIEW_EDIT)
    public String updateTag(EntityView view, Map<String, Object> params) {
        try {
            assertSession(params);

            WrappedParams wp = new WrappedParams(params);

            String tagid= wp.getString("tagid");

            Tag tag = tagService().getTags().getForId(tagid).get();

            if (wp.containsKey("tagcollectionid")) {
                tag.setTagCollectionId(wp.getString("tagcollectionid"));
            }
            if (wp.containsKey("taglabel")){
                tag.setTagLabel(wp.getString("taglabel"));
            }
            if (wp.containsKey("description")){
                tag.setDescription(wp.getString("description"));
            }
            if (wp.containsKey("externalid")){
                tag.setExternalId(wp.getString("externalid"));
            }
            if (wp.containsKey("alternativelabels")){
                tag.setAlternativeLabels(wp.getString("alternativelabels"));
            }
            if (wp.containsKey("externalcreation")){
                tag.setExternalCreation(wp.getBoolean("externalcreation"));
            }
            if (wp.containsKey("externalcreationdate")){
                tag.setExternalCreationDate(wp.getEpochMS("externalcreationdate"));
            }
            if (wp.containsKey("externalupdate")){
                tag.setExternalUpdate(wp.getBoolean("externalupdate"));
            }
            if (wp.containsKey("lastupdatedateinexternalsystem")){
                tag.setLastUpdateDateInExternalSystem(wp.getEpochMS("lastupdatedateinexternalsystem"));
            }
            if (wp.containsKey("parentid")){
                tag.setParentId(wp.getString("parentid"));
            }
            if (wp.containsKey("externalhierarchycode")){
                tag.setExternalHierarchyCode(wp.getString("externalhierarchycode"));
            }
            if (wp.containsKey("externaltype")){
                tag.setExternalType(wp.getString("externaltype"));
            }
            if (wp.containsKey("data")) {
                tag.setData(wp.getString("data"));
            }


            Errors errors = tag.validate();

            if (errors.hasErrors()) {
                return respondWithError(errors);
            }

            tagService().getTags().updateTag(tag);

            JSONObject result = new JSONObject();
            result.put("status", "OK");
            result.put("tagid", tagid);
            return result.toJSONString();
        } catch (Exception e) {
            return respondWithError(e);
        }
    }

    @EntityCustomAction(action = "updateTagCollection", viewKey = EntityView.VIEW_EDIT)
    public String updateTagCollection(EntityView view, Map<String, Object> params) {
        try {
            assertSession(params);

            WrappedParams wp = new WrappedParams(params);

            String tagcollectionid= wp.getString("tagcollectionid");

            TagCollection tagCollection = tagService().getTagCollections().getForId(tagcollectionid).get();

            //We don't need to change the creation date or user

            if (wp.containsKey("name")){
                tagCollection.setName(wp.getString("name"));
            }
            if (wp.containsKey("description")){
            tagCollection.setDescription(wp.getString("description"));
            }
            if (wp.containsKey("externalsourcename")){
                tagCollection.setExternalSourceName(wp.getString("externalsourcename"));
            }
            if (wp.containsKey("externalsourcedescription")){
                tagCollection.setExternalSourceDescription(wp.getString("externalsourcedescription"));
            }
            if (wp.containsKey("externalupdate")){
                tagCollection.setExternalUpdate(wp.getBoolean("externalupdate"));
            }
            if (wp.containsKey("externalcreation")){
                tagCollection.setExternalCreation(wp.getBoolean("externalcreation"));
            }
            if (wp.containsKey("lastsynchronizationdate")){
                tagCollection.setLastSynchronizationDate(wp.getEpochMS("lastsynchronizationdate"));
            }
            if (wp.containsKey("lastupdatedateinexternalsystem")) {
                tagCollection.setLastUpdateDateInExternalSystem(wp.getEpochMS("lastupdatedateinexternalsystem"));
            }
            Errors errors = tagCollection.validate();

            if (errors.hasErrors()) {
                return respondWithError(errors);
            }

            tagService().getTagCollections().updateTagCollection(tagCollection);

            JSONObject result = new JSONObject();
            result.put("status", "OK");
            result.put("tagcollectionid", tagcollectionid);
            return result.toJSONString();
        } catch (Exception e) {
            return respondWithError(e);
        }
    }

    @EntityCustomAction(action = "downloadCollection", viewKey = EntityView.VIEW_LIST)
    public List<Tag> downloadCollection(EntityView view, Map<String, Object> params) {
        try {
            assertSession(params);

            WrappedParams wp = new WrappedParams(params);

            String tagcollectionid= wp.getString("tagcollectionid");

            List<Tag> tags = tagService().getTags().getAllInCollection(tagcollectionid);

            return tags;
        } catch (Exception e) {
            log.error("Error calling getTagsInCollection:",e);
            return null;
        }
    }

    private String respondWithError(Exception e) {
        JSONObject result = new JSONObject();
        result.put("status", "ERROR");
        result.put("message", e.getMessage());

        log.error("Caught an error while handling a request", e);

        return result.toJSONString();
    }

    private String respondWithError(Errors e) {
        JSONObject result = new JSONObject();
        result.put("status", "ERROR");
        result.put("message", e.toMap());

        return result.toJSONString();
    }

    private void assertSession(Map<String, Object> params) {
        assertPermission();

        String tokenFromUser = (String)params.get(REQUEST_SESSION_PARAMETER);
        String tokenFromSession = (String)sessionManager.getCurrentSession().getAttribute(SAKAI_SESSION_TOKEN_PROPERTY);

        if (tokenFromSession == null || tokenFromUser == null || !tokenFromSession.equals(tokenFromUser)) {
            log.error("assertSession failed for user " + sessionManager.getCurrentSessionUserId());
            throw new TagServiceException("Access denied");
        }
    }

    private void assertPermission() {
        if (!securityService.unlock("tagservice.manage", ADMIN_SITE_REALM)) {
            log.error("assertPermission denied access to user " + sessionManager.getCurrentSessionUserId());
            throw new TagServiceException("Access denied");
        }
    }

    private TagService tagService() {
        return (TagService) ComponentManager.get(TagService.class);
    }

    private String mintSessionId() {
        byte[] b = new byte[32];

        try {
            SecureRandom.getInstance("SHA1PRNG").nextBytes(b);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Couldn't generate a session Id", e);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            sb.append(String.format("%02x", b[i]));
        }

        return sb.toString();
    }

    public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
        this.entityProviderManager = entityProviderManager;
    }

    public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
        this.developerHelperService = developerHelperService;
    }

    private class WrappedParams {

        private final Map<String, Object> params;

        public WrappedParams(Map<String, Object> params) {
            this.params = params;
        }

        public String getString(String name) {
            String result = (String)params.get(name);

            if (result == null) {
                throw new IllegalArgumentException("Parameter " + name + " cannot be null.");
            }

            return result;
        }

        public long getEpochMS(String name) {
            return Long.valueOf(getString(name));
        }

        public boolean getBoolean(String name) {
            return Boolean.valueOf(getString(name));
        }

        public boolean containsKey(String name) {
            return this.params.containsKey(name);
        }
    }
}
