/**
 * Copyright (c) 2005-2013 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.entity.impl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.BrowseSearchable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectDefinable;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.TemplateMap;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.assessment.entity.api.PublishedAssessmentEntityProvider;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PublishedAssessmentEntityProviderImpl implements
        PublishedAssessmentEntityProvider,
        CoreEntityProvider,
        AutoRegisterEntityProvider,
        PropertyProvideable,
        BrowseSearchable,
        RESTful,
        Outputable,
        RedirectDefinable {

    @Setter private DeveloperHelperService developerHelperService;
    @Setter private PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries;
    @Setter private SecurityService securityService;
    @Setter private SiteService siteService;

    public String getEntityPrefix() {
        return ENTITY_PREFIX;
    }

    public boolean entityExists(String id) {
        try {
            PublishedAssessmentService service = new PublishedAssessmentService();
            PublishedAssessmentFacade pub = service.getPublishedAssessment(id);
            if (pub != null) return true;
        } catch (Exception e) {
            log.debug("could not find published assessment with id [{}]", id, e);
        }
        return false;
    }

    public TemplateMap[] defineURLMappings() {
        return new TemplateMap[]{
                new TemplateMap("/{prefix}/context/{siteId}", "{prefix}{dot-extension}")
        };
    }


    /**
     * Browse for the list of published assessments for a given site for a user
     *
     * @param siteId
     * @param userId
     *
     * @return list of published assessments that a user has access to see in a site
     */
    private List<String> browseEntities(String siteId, String userId) {
        List<String> references = new ArrayList<>();
        List<PublishedAssessmentFacade> assessments = new ArrayList<>();
        boolean canPublish = false;
        String siteReference = "/site/" + siteId;
        String orderBy = "title";
        Date currentDate = new Date();

        // Check what the user can do
        if (securityService.unlock(SamigoConstants.CAN_PUBLISH, siteReference)) {
            assessments.addAll(publishedAssessmentFacadeQueries.getBasicInfoOfAllPublishedAssessments(orderBy, true, siteId));
            canPublish = true;
        } else if (securityService.unlock(SamigoConstants.CAN_TAKE, siteReference)) {
            assessments.addAll(publishedAssessmentFacadeQueries.getBasicInfoOfAllActivePublishedAssessments(orderBy, siteId, true));
        }

        if (!assessments.isEmpty()) {
            for (PublishedAssessmentFacade assessment : assessments) {
                if (canPublish || assessment.getStartDate() == null || currentDate.after(assessment.getStartDate()))
                    references.add("/" + ENTITY_PREFIX + "/" + assessment.getPublishedAssessmentId());
            }
        }
        return references;
    }

    public List<PublishedAssessmentFacade> getEntities(EntityReference ref, Search search) {
        List<PublishedAssessmentFacade> assessments = new ArrayList<>();
        Restriction[] restrictions = search.getRestrictions();
        String orderBy = "title";
        String siteId = null;
        String userId = null;

        for (Restriction restriction : restrictions) {
            if (StringUtils.equalsAnyIgnoreCase(restriction.property, "siteId", "context")) {
                siteId = (String) restriction.value;
            }
            if (restriction.property.equalsIgnoreCase("userId")) {
                userId = (String) restriction.value;
            }
        }

        if (userId == null) userId = developerHelperService.getCurrentUserId();
        if (userId == null) throw new SecurityException("No user is currently logged in so no data can be retrieved");
        if (siteId == null) return assessments;

        // Check what the user can do
        if (securityService.unlock(SamigoConstants.CAN_PUBLISH, "/site/" + siteId)) {
            assessments.addAll(publishedAssessmentFacadeQueries.getBasicInfoOfAllPublishedAssessments(orderBy, true, siteId));
        } else if (securityService.unlock(SamigoConstants.CAN_TAKE, "/site/" + siteId)) {
            assessments.addAll(publishedAssessmentFacadeQueries.getBasicInfoOfAllActivePublishedAssessments(orderBy, siteId, true));
        }

        return assessments;
    }

    public Object getEntity(EntityReference ref) {
        return new EntityData(new EntityReference("dummy reference"), "dummy");
    }

    public List<EntityData> browseEntities(Search search,
                                           String userReference,
                                           String associatedReference,
                                           Map<String, Object> params) {
        List<EntityData> results = new ArrayList<>();
        List<PublishedAssessmentFacade> assessments = new ArrayList<>();
        Date currentDate = new Date();
        Restriction[] restrictions = search.getRestrictions();
        boolean canPublish = false;
        String orderBy = "title";
        String siteId = (String) params.get("context");
        String siteReference = "/site/" + siteId;
        String userId = null;

        for (Restriction restriction : restrictions) {
            if (restriction.property.equalsIgnoreCase("userId")) {
                userId = (String) restriction.value;
            }
            if (restriction.property.equalsIgnoreCase("context")) {
                siteId = (String) restriction.value;
            }
        }
        if (userId == null) return results;
        if (siteId == null) return results;

        // Check what the user can do
        if (securityService.unlock(SamigoConstants.CAN_PUBLISH, siteReference)) {
            assessments.addAll(publishedAssessmentFacadeQueries.getBasicInfoOfAllPublishedAssessments(orderBy,true, siteId));
            canPublish = true;
        } else if (securityService.unlock(SamigoConstants.CAN_TAKE, siteReference)) {
            assessments.addAll(publishedAssessmentFacadeQueries.getBasicInfoOfAllActivePublishedAssessments(orderBy, siteId, true));
        }
        for (PublishedAssessmentFacade assessment : assessments) {
            if (canPublish || assessment.getStartDate() == null || currentDate.after(assessment.getStartDate())) {
                String thisEntityReference = "/" + ENTITY_PREFIX + "/" + assessment.getPublishedAssessmentId();
                String thisEntityTitle = assessment.getTitle();
                results.add(new EntityData(new EntityReference(thisEntityReference), thisEntityTitle));
            }
        }
        return results;
    }

    public List<String> findEntityRefs(String[] prefixes,
                                       String[] name,
                                       String[] searchValue,
                                       boolean exactMatch) {
        if (ENTITY_PREFIX.equals(prefixes[0])) {
            String siteId = null;
            String userId = null;
            for (int i = 0; i < name.length; i++) {
                if ("context".equalsIgnoreCase(name[i]) || "site".equalsIgnoreCase(name[i])) {
                    siteId = searchValue[i];
                } else if ("user".equalsIgnoreCase(name[i]) || "userId".equalsIgnoreCase(name[i])) {
                    userId = searchValue[i];
                }
            }
            if (siteId != null && userId != null) {
                return browseEntities(siteId, userId);
            }
        }
        return Collections.emptyList();
    }

    public Map<String, String> getProperties(String reference) {
        Map<String, String> props = new HashMap<>();
        PublishedAssessmentService service = new PublishedAssessmentService();
        PublishedAssessmentFacade pub = service.getPublishedAssessment(reference.substring(ENTITY_PREFIX.length() + 2));
        if (pub != null) {
            props.put("title", pub.getTitle());
            props.put("description", pub.getDescription());
            props.put("author", pub.getCreatedBy());
            props.put("comments", pub.getComments());
            props.put("siteId", pub.getOwnerSiteId());
            props.put("modified_by", pub.getLastModifiedBy());
            if (pub.getCreatedDate() != null) props.put("created_date", DateFormat.getInstance().format(pub.getCreatedDate()));
            if (pub.getLastModifiedDate() != null) props.put("modified_date", DateFormat.getInstance().format(pub.getLastModifiedDate()));
            if (pub.getTotalScore() != null) props.put("totalScore", pub.getTotalScore().toString());
            if (pub.getStartDate() != null) {
                props.put("start_date", DateFormat.getInstance().format(pub.getStartDate()));
            } else if (pub.getAssessmentAccessControl().getStartDate() != null) {
                props.put("start_date", DateFormat.getInstance().format(pub.getAssessmentAccessControl().getStartDate()));
            }
            if (pub.getDueDate() != null) {
                props.put("due_date", DateFormat.getInstance().format(pub.getDueDate()));
            } else if (pub.getAssessmentAccessControl().getDueDate() != null) {
                props.put("due_date", DateFormat.getInstance().format(pub.getAssessmentAccessControl().getDueDate()));
            }
            if (pub.getRetractDate() != null) {
                props.put("retract_date", DateFormat.getInstance().format(pub.getRetractDate()));
            } else if (pub.getAssessmentAccessControl().getRetractDate() != null) {
                props.put("retract_date", DateFormat.getInstance().format(pub.getAssessmentAccessControl().getRetractDate()));
            }
        }
        return props;
    }


    public String[] getHandledOutputFormats() {
        return new String[]{Formats.XML, Formats.JSON};
    }

    public String[] getHandledInputFormats() {
        return new String[]{Formats.XML, Formats.JSON, Formats.HTML};
    }

    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        return null;
    }

    public Object getSampleEntity() {
        return null;
    }

    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
    }

    public void deleteEntity(EntityReference ref, Map<String, Object> params) {
    }


    public String getPropertyValue(String reference, String name) {
        Map<String, String> props = getProperties(reference);
        return props.get(name);
    }

    public void setPropertyValue(String reference, String name, String value) {
    }

    @EntityCustomAction(action = "deepLink", viewKey = EntityView.VIEW_LIST)
    public Map<String, String> getAssessmentDeepLink(EntityView view, Map<String, Object> params) {
        String publishedId = view.getPathSegment(1);

        if (publishedId == null) {
            // format of the view should be in a standard assessment reference
            throw new IllegalArgumentException(
                    "Must include context and publishedId in the path ("
                            + view
                            + "): e.g. /direct/sam_pub/deepLink/{publishedId}");
        }

        Map<String, String> assessData = new HashMap<>();

        try {
            PublishedAssessmentService service = new PublishedAssessmentService();
            PublishedAssessmentFacade pub = service.getPublishedAssessment(publishedId);
            Site site = siteService.getSite(pub.getOwnerSiteId());
            ToolConfiguration toolConfig = site.getToolForCommonId(SamigoConstants.TOOL_ID);
            String siteReference = siteService.siteReference(site.getId());
            String url = "";
            if (securityService.unlock(SamigoConstants.CAN_PUBLISH, siteReference)) {
                // Construct the deep link URL for instructor to go to settings
                url = "/portal/site/" + site.getId()
                        + "/tool-reset/" + toolConfig.getId();
                        // + "/jsf/author/authorSettings?publishedId=" + publishedId
                        // + "&action=settings_published";
            } else if (securityService.unlock(SamigoConstants.CAN_TAKE, siteReference)) {
                url = "/portal/site/" + site.getId()
                        + "/tool-reset/" + toolConfig.getId();
            }
            assessData.put("assessmentUrl", url);
            assessData.put("assessmentTitle", pub.getTitle());
        } catch (Exception e) {
            throw new EntityException(e.toString(), publishedId);
        }
        return assessData;
    }
}
