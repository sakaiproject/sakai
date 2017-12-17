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

import java.util.Map;
import java.util.Optional;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

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
import org.sakaiproject.tags.api.Tag;
import org.sakaiproject.tags.api.TagCollection;
import org.sakaiproject.tags.api.TagService;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Web services supporting AJAX requests from the Tags System end user display.
 */
@Slf4j
public class TagServiceEntityProvider implements EntityProvider, AutoRegisterEntityProvider, ActionsExecutable, Outputable, Describeable {

    private static final String TAGSERVICE_PREFIX = "tagservice";
    private static final int TAGSERVICE_PAGE_LIMIT_DEFAULT = 30;
    private static final int TAGSERVICE_FIRST_PAGE_DEFAULT = 1;

    protected DeveloperHelperService developerHelperService;
    private EntityProviderManager entityProviderManager;
    private  SessionManager sessionManager = (SessionManager) ComponentManager.get("org.sakaiproject.tool.api.SessionManager");

    @Override
    public String[] getHandledOutputFormats() {
        return new String[] { Formats.JSON };
    }

    @Override
    public String getEntityPrefix() {
        return TAGSERVICE_PREFIX;
    }

    private boolean checkCSRFToken(Map<String, Object> params) {
        Object sessionToken = sessionManager.getCurrentSession().getAttribute("sakai.csrf.token");

        if (sessionToken == null || !sessionToken.equals(params.get("sakai_csrf_token"))) {
            log.warn("CSRF token validation failed");
            return false;
        }

        return true;
    }


    @EntityCustomAction(action = "getTag", viewKey = EntityView.VIEW_LIST)
    public Tag getTag(EntityView view, Map<String, Object> params) {
        try {
            WrappedParams wp = new WrappedParams(params);

            String tagid= wp.getString("tagid");

            Optional<Tag> tag = tagService().getTags().getForId(tagid);

            return tag.get();
        } catch (Exception e) {
            log.error("Error calling getTag:",e);
            return null;
        }
    }


    @EntityCustomAction(action = "getPaginatedTagCollections", viewKey = EntityView.VIEW_LIST)
    public JSONObject getTagCollections(EntityView view, Map<String, Object> params) {
        try {
            WrappedParams wp = new WrappedParams(params);
            int maxPageSize = tagService().getMaxPageSize();
            int page= wp.getInteger("page",TAGSERVICE_FIRST_PAGE_DEFAULT);
            int pageLimit= wp.getInteger("pagelimit",TAGSERVICE_PAGE_LIMIT_DEFAULT);

            if (pageLimit > maxPageSize) {
                pageLimit = maxPageSize;
            }

            List<TagCollection> tagCollections = tagService().getTagCollections().getTagCollectionsPaginated(page,pageLimit);
            int tagCollectionsCount = tagService().getTagCollections().getTotalTagCollections();

            JSONObject responseDetailsJson = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            for(TagCollection p : tagCollections) {
                JSONObject formDetailsJson = new JSONObject();
                formDetailsJson.put("tagCollectionId", p.getTagCollectionId());
                formDetailsJson.put("name", p.getName());
                formDetailsJson.put("description", p.getDescription());
                formDetailsJson.put("externalsourcename", p.getExternalSourceName());
                formDetailsJson.put("externalsourcedescription", p.getExternalSourceDescription());
                jsonArray.add(formDetailsJson);
            }
            responseDetailsJson.put("total",tagCollectionsCount );
            responseDetailsJson.put("tagCollections", jsonArray);//Here you can see the data in json format

            return responseDetailsJson;

        } catch (Exception e) {
            log.error("Error calling getTagCollections:",e);
            return null;
        }
    }



     @EntityCustomAction(action = "getTagsPaginatedInCollection", viewKey = EntityView.VIEW_LIST)
    public JSONObject getTagsPaginatedInCollection(EntityView view, Map<String, Object> params) {
        try {
            WrappedParams wp = new WrappedParams(params);
            int maxPageSize = tagService().getMaxPageSize();
            String tagcollectionid= wp.getString("tagcollectionid");
            int page= wp.getInteger("page",TAGSERVICE_FIRST_PAGE_DEFAULT);
            int pageLimit= wp.getInteger("pagelimit",TAGSERVICE_PAGE_LIMIT_DEFAULT);

            if (pageLimit > maxPageSize) {
                pageLimit = maxPageSize;
            }

            List<Tag> tags = tagService().getTags().getTagsPaginatedInCollection(page, pageLimit, tagcollectionid);
            int tagCount = tagService().getTags().getTotalTagsInCollection(tagcollectionid);

            JSONObject responseDetailsJson = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            for(Tag p : tags) {
                JSONObject formDetailsJson = new JSONObject();
                formDetailsJson.put("tagId", p.getTagId());
                formDetailsJson.put("tagLabel", p.getTagLabel());
                formDetailsJson.put("collectionName", p.getCollectionName());
                formDetailsJson.put("tagCollectionId", p.getTagCollectionId());
                jsonArray.add(formDetailsJson);
            }
            responseDetailsJson.put("total",tagCount );
            responseDetailsJson.put("tags", jsonArray);//Here you can see the data in json format

            return responseDetailsJson;
        } catch (Exception e) {
            log.error("Error calling getTagsPaginatedInCollection:",e);
            return null;
        }
    }



    @EntityCustomAction(action = "getTagsPaginatedByPrefixInLabel", viewKey = EntityView.VIEW_LIST)
    public JSONObject getTagsPaginatedByPrefixInLabel(EntityView view, Map<String, Object> params) {
        try {
            WrappedParams wp = new WrappedParams(params);
            int maxPageSize = tagService().getMaxPageSize();
            String prefix= wp.getString("prefix");
            int page= wp.getInteger("page",TAGSERVICE_FIRST_PAGE_DEFAULT);
            int pageLimit= wp.getInteger("pagelimit",TAGSERVICE_PAGE_LIMIT_DEFAULT);

            if (pageLimit > maxPageSize) pageLimit = maxPageSize;

            List<Tag> tags = tagService().getTags().getTagsPaginatedByPrefixInLabel(page, pageLimit, prefix);
            int tagCount = tagService().getTags().getTotalTagsByPrefixInLabel(prefix);

            JSONObject responseDetailsJson = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            for(Tag p : tags) {
                JSONObject formDetailsJson = new JSONObject();
                formDetailsJson.put("tagId", p.getTagId());
                formDetailsJson.put("tagLabel", p.getTagLabel());
                formDetailsJson.put("collectionName", p.getCollectionName());
                formDetailsJson.put("tagCollectionId", p.getTagCollectionId());
                jsonArray.add(formDetailsJson);
            }
            responseDetailsJson.put("total",tagCount );
            responseDetailsJson.put("tags", jsonArray);//Here you can see the data in json format

            return responseDetailsJson;
        } catch (Exception e) {
            log.error("Error calling getTagsPaginatedByPrefixInLabel:",e);
            return null;
        }
    }



    private TagService tagService() {
        return (TagService) ComponentManager.get(TagService.class);
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

        public int getInteger(String name, int defaultValue) {
            int result=defaultValue;

            try {
                result = Integer.parseInt(params.get(name).toString());
            }catch (Exception e) {
               log.debug("Param " + name + " is not a valid integer, so returning the default value");
            }
            return result;

        }
    }

}
