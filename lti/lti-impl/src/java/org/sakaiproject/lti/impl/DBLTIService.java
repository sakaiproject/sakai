/**
 * Copyright (c) 2009-2017 The Apereo Foundation
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


package org.sakaiproject.lti.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.api.model.LtiContent;
import org.sakaiproject.lti.api.model.LtiMembershipsJob;
import org.sakaiproject.lti.api.model.LtiTool;
import org.sakaiproject.lti.api.model.LtiToolFunction;
import org.sakaiproject.lti.api.model.LtiToolSite;
import org.sakaiproject.lti.api.repository.LtiContentRepository;
import org.sakaiproject.lti.api.repository.LtiMembershipsJobRepository;
import org.sakaiproject.lti.api.repository.LtiToolFunctionRepository;
import org.sakaiproject.lti.api.repository.LtiToolRepository;
import org.sakaiproject.lti.api.repository.LtiToolSiteRepository;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

import org.tsugi.lti.LTIUtil;

@Slf4j
public class DBLTIService extends BaseLTIService implements LTIService {

    @Autowired
    private LtiContentRepository ltiContentRepository;

    @Autowired
    private LtiMembershipsJobRepository ltiMembershipsJobRepository;

    @Autowired
    private LtiToolFunctionRepository ltiToolFunctionRepository;

    @Autowired
    private LtiToolRepository ltiToolRepository;

    @Autowired
    private LtiToolSiteRepository ltiToolSiteRepository;

    public Object insertMembershipsJobDao(String siteId, String membershipsId, String membershipsUrl, String consumerKey, String ltiVersion) {

		log.debug("insertMembershipsJobDao({},{},{},{},{})", siteId, membershipsId, membershipsUrl, consumerKey, ltiVersion);

		// First, check if there is already a job for this site.
        if (ltiMembershipsJobRepository.findById(siteId).isEmpty()) {
            LtiMembershipsJob job = new LtiMembershipsJob();
            job.setSiteId(siteId);
            job.setMembershipsId(membershipsId);
            job.setMembershipsUrl(membershipsUrl);
            job.setConsumerkey(consumerKey);
            job.setLtiVersion(ltiVersion);
            return ltiMembershipsJobRepository.save(job);
		} else {
			return "SITE_ALREADY_JOBBED";
		}
    }

	public List<Map<String, Object>> getMembershipsJobsDao() {

		log.debug("getMembershipsJobsDao()");
		return ltiMembershipsJobRepository.findAll()
			.stream()
			.collect(Collectors.mapping(this::ltiMembershipsJobToMap, Collectors.toList()));
    }

	public Map<String, Object> getMembershipsJobDao(String siteId) {

		log.debug("getMembershipsJobDao({})", siteId);
		return ltiMembershipsJobRepository.findById(siteId).map(this::ltiMembershipsJobToMap).orElse(null);
    }

	/**
	 * Converts an {@link LtiMembershipsJob} entity into the foorm-style property map that the rest of
	 * the LTI code expects. The map keys mirror the persisted columns of {@link LTIService#MEMBERSHIPS_JOBS_MODEL}.
	 */
	private Map<String, Object> ltiMembershipsJobToMap(LtiMembershipsJob job) {
		Map<String, Object> map = new HashMap<>();
		map.put(LTI_SITE_ID, job.getSiteId());
		map.put("memberships_id", job.getMembershipsId());
		map.put("memberships_url", job.getMembershipsUrl());
		map.put(LTIService.LTI_CONSUMERKEY, job.getConsumerkey());
		map.put("lti_version", job.getLtiVersion());
		return map;
    }

	public Object insertToolDao(Object newPropsObject, String siteId, boolean isAdminRole, boolean isMaintainRole) {

		@SuppressWarnings("unchecked")
		Map<Object, Object> newProps = (Map<Object, Object>) newPropsObject;

		LtiTool ltiTool = new LtiTool();
		if (!ADMIN_SITE.equals(siteId)) ltiTool.setSiteId(siteId);
		ltiTool.setTitle((String) newProps.get(LTIService.LTI_TITLE));
		ltiTool.setDescription((String) newProps.get(LTIService.LTI_DESCRIPTION));
		if (newProps.get(LTIService.LTI_STATUS) != null) ltiTool.setStatus((Integer) newProps.get(LTIService.LTI_STATUS));
		if (newProps.get(LTIService.LTI_VISIBLE) != null) ltiTool.setVisible((Integer) newProps.get(LTIService.LTI_VISIBLE));
		ltiTool.setDeploymentId((Integer) newProps.get(LTIService.LTI_DEPLOYMENT_ID));
		ltiTool.setLaunch((String) newProps.get(LTIService.LTI_LAUNCH));
		if (newProps.get(LTIService.LTI_NEWPAGE) != null) ltiTool.setNewPage((Integer) newProps.get(LTIService.LTI_NEWPAGE));
		ltiTool.setFrameHeight((Integer) newProps.get(LTIService.LTI_FRAMEHEIGHT));
		ltiTool.setFaIcon((String) newProps.get(LTIService.LTI_FA_ICON));
		if (newProps.get(LTIService.LTI_MT_LAUNCH) != null) ltiTool.setPlLaunch((Integer) newProps.get(LTIService.LTI_MT_LAUNCH));
		if (newProps.get(LTIService.LTI_MT_LINKSELECTION) != null) ltiTool.setPlLinkSelection((Integer) newProps.get(LTIService.LTI_MT_LINKSELECTION));
		if (newProps.get(LTIService.LTI_MT_CONTEXTLAUNCH) != null) ltiTool.setPlContextlaunch((Integer) newProps.get(LTIService.LTI_MT_CONTEXTLAUNCH));
		if (newProps.get(LTIService.LTI_PL_LESSONSSELECTION) != null) ltiTool.setPlLessonsSelection((Integer) newProps.get(LTIService.LTI_PL_LESSONSSELECTION));
		if (newProps.get(LTIService.LTI_PL_CONTENTEDITOR) != null) ltiTool.setPlContentEditor((Integer) newProps.get(LTIService.LTI_PL_CONTENTEDITOR));
		if (newProps.get(LTIService.LTI_PL_ASSESSMENTSELECTION) != null) ltiTool.setPlAssessmentSelection((Integer) newProps.get(LTIService.LTI_PL_ASSESSMENTSELECTION));
		if (newProps.get(LTIService.LTI_PL_COURSENAV) != null) ltiTool.setPlCourseNav((Integer) newProps.get(LTIService.LTI_PL_COURSENAV));
		if (newProps.get(LTIService.LTI_PL_IMPORTITEM) != null) ltiTool.setPlImportItem((Integer) newProps.get(LTIService.LTI_PL_IMPORTITEM));
		if (newProps.get(LTIService.LTI_PL_FILEITEM) != null) ltiTool.setPlFileItem((Integer) newProps.get(LTIService.LTI_PL_FILEITEM));
		if (newProps.get(LTIService.LTI_SENDNAME) != null) ltiTool.setSendName((Integer) newProps.get(LTIService.LTI_SENDNAME));
		if (newProps.get(LTIService.LTI_SENDEMAILADDR) != null) ltiTool.setSendEmailAddr((Integer) newProps.get(LTIService.LTI_SENDEMAILADDR));
		if (newProps.get(LTIService.LTI_MT_PRIVACY) != null) ltiTool.setPlPrivacy((Integer) newProps.get(LTIService.LTI_MT_PRIVACY));
		if (newProps.get(LTIService.LTI_ALLOWOUTCOMES) != null) ltiTool.setAllowOutcomes((Integer) newProps.get(LTIService.LTI_ALLOWOUTCOMES));
		if (newProps.get(LTIService.LTI_ALLOWLINEITEMS) != null) ltiTool.setAllowLineItems((Integer) newProps.get(LTIService.LTI_ALLOWLINEITEMS));
		if (newProps.get(LTIService.LTI_ALLOWGRADEBOOKREADONLY) != null) ltiTool.setAllowGradebookReadOnly((Integer) newProps.get(LTIService.LTI_ALLOWGRADEBOOKREADONLY));
		if (newProps.get(LTIService.LTI_ALLOWROSTER) != null) ltiTool.setAllowRoster((Integer) newProps.get(LTIService.LTI_ALLOWROSTER));
		if (newProps.get(LTIService.LTI_DEBUG) != null) ltiTool.setDebug((Integer) newProps.get(LTIService.LTI_DEBUG));
		if (newProps.get(LTIService.LTI_SITEINFOCONFIG) != null) ltiTool.setSiteinfoConfig((Integer) newProps.get(LTIService.LTI_SITEINFOCONFIG));
		ltiTool.setSplash((String) newProps.get(LTIService.LTI_SPLASH));
		ltiTool.setCustom((String) newProps.get(LTIService.LTI_CUSTOM));
		ltiTool.setRolemap((String) newProps.get(LTIService.LTI_ROLEMAP));
		if (newProps.get(LTIService.LTI13) != null) ltiTool.setLti13((Integer) newProps.get(LTIService.LTI13));
		ltiTool.setLti13ToolKeyset((String) newProps.get(LTIService.LTI13_TOOL_KEYSET));
		ltiTool.setLti13OidcEndpoint((String) newProps.get(LTIService.LTI13_TOOL_ENDPOINT));
		ltiTool.setLti13OidcRedirect((String) newProps.get(LTIService.LTI13_TOOL_REDIRECT));
		ltiTool.setLti13ClientId((String) newProps.get(LTIService.LTI13_CLIENT_ID));
		ltiTool.setLti13LmsDeploymentId((String) newProps.get(LTIService.LTI13_LMS_DEPLOYMENT_ID));
		ltiTool.setConsumerKey((String) newProps.get(LTIService.LTI_CONSUMERKEY));
		ltiTool.setSecret((String) newProps.get(LTIService.LTI_SECRET));
		ltiTool.setXmlImport((String) newProps.get(LTIService.LTI_XMLIMPORT));
		ltiTool.setLti13AutoToken((String) newProps.get(LTIService.LTI13_AUTO_TOKEN));
		ltiTool.setLti13AutoState((Integer) newProps.get(LTIService.LTI13_AUTO_STATE));
		ltiTool.setLti13AutoRegistration((String) newProps.get(LTIService.LTI13_AUTO_REGISTRATION));

		Instant now = Instant.now();
		ltiTool.setCreatedAt(now);
		ltiTool.setUpdatedAt(now);

		return ltiToolRepository.save(ltiTool);
	}

	private void deployToolToContentSites(Long toolKey, boolean isAdminRole, boolean isMaintainRole) {
		if (toolKey == null || !isAdminRole || !isMaintainRole) {
			return;
		}

		for (String site : ltiContentRepository.findSitesNeedingDeployment(toolKey.longValue())) {
			String contentSite = StringUtils.trimToNull(site);
			if (contentSite == null) {
				continue;
			}
			// Match the original INNER JOIN SAKAI_SITE - only deploy to sites that actually exist
			try {
				siteService.getSite(contentSite);
			} catch (IdUnusedException e) {
				continue;
			}

			Properties props = new Properties();
			props.setProperty(LTIService.LTI_TOOL_ID, toolKey.toString());
			props.setProperty(LTIService.LTI_SITE_ID, contentSite);
			props.setProperty("notes", rb.getString("tool.added.by.insert.content"));
			Object insertResult = insertToolSiteDao(props, contentSite, isAdminRole, isMaintainRole);
			if (insertResult instanceof String) {
				log.warn("Unable to deploy stealthed tool {} to site {}: {}", toolKey, contentSite, insertResult);
			}
		}
	}

	public Map<String, Object> getToolDao(Long key, String siteId, boolean isAdminRole)
	{
		return getThingDao("lti_tools", LTIService.TOOL_MODEL, key, siteId, isAdminRole);
	}

	/**
	 * Converts an {@link LtiTool} entity into the foorm-style property map that the rest of
	 * the LTI code expects. The map keys mirror the persisted columns of {@link LTIService#TOOL_MODEL}
	 * and this method is the inverse of the field mapping performed in {@link #insertToolDao}.
	 */
	private Map<String, Object> ltiToolToMap(LtiTool tool) {
		Map<String, Object> map = new HashMap<>();
		map.put(LTIService.LTI_ID, tool.getId());
		map.put(LTI_SITE_ID, tool.getSiteId());
		map.put(LTIService.LTI_TITLE, tool.getTitle());
		map.put(LTIService.LTI_DESCRIPTION, tool.getDescription());
		map.put(LTIService.LTI_STATUS, tool.getStatus());
		map.put(LTI_VISIBLE, tool.getVisible());
		map.put(LTIService.LTI_DEPLOYMENT_ID, tool.getDeploymentId());
		map.put(LTIService.LTI_LAUNCH, tool.getLaunch());
		map.put(LTIService.LTI_NEWPAGE, tool.getNewPage());
		map.put(LTIService.LTI_FRAMEHEIGHT, tool.getFrameHeight());
		map.put(LTIService.LTI_FA_ICON, tool.getFaIcon());
		map.put(LTIService.LTI_MT_LAUNCH, tool.getPlLaunch());
		map.put(LTIService.LTI_MT_LINKSELECTION, tool.getPlLinkSelection());
		map.put(LTIService.LTI_MT_CONTEXTLAUNCH, tool.getPlContextlaunch());
		map.put(LTIService.LTI_PL_LESSONSSELECTION, tool.getPlLessonsSelection());
		map.put(LTIService.LTI_PL_CONTENTEDITOR, tool.getPlContentEditor());
		map.put(LTIService.LTI_PL_ASSESSMENTSELECTION, tool.getPlAssessmentSelection());
		map.put(LTIService.LTI_PL_COURSENAV, tool.getPlCourseNav());
		map.put(LTIService.LTI_PL_IMPORTITEM, tool.getPlImportItem());
		map.put(LTIService.LTI_PL_FILEITEM, tool.getPlFileItem());
		map.put(LTIService.LTI_SENDNAME, tool.getSendName());
		map.put(LTIService.LTI_SENDEMAILADDR, tool.getSendEmailAddr());
		map.put(LTIService.LTI_MT_PRIVACY, tool.getPlPrivacy());
		map.put(LTIService.LTI_ALLOWOUTCOMES, tool.getAllowOutcomes());
		map.put(LTIService.LTI_ALLOWLINEITEMS, tool.getAllowLineItems());
		map.put(LTIService.LTI_ALLOWGRADEBOOKREADONLY, tool.getAllowGradebookReadOnly());
		map.put(LTIService.LTI_ALLOWROSTER, tool.getAllowRoster());
		map.put(LTIService.LTI_DEBUG, tool.getDebug());
		map.put(LTIService.LTI_SITEINFOCONFIG, tool.getSiteinfoConfig());
		map.put(LTIService.LTI_SPLASH, tool.getSplash());
		map.put(LTIService.LTI_CUSTOM, tool.getCustom());
		map.put(LTIService.LTI_ROLEMAP, tool.getRolemap());
		map.put(LTIService.LTI13, tool.getLti13());
		map.put(LTIService.LTI13_TOOL_KEYSET, tool.getLti13ToolKeyset());
		map.put(LTIService.LTI13_TOOL_ENDPOINT, tool.getLti13OidcEndpoint());
		map.put(LTIService.LTI13_TOOL_REDIRECT, tool.getLti13OidcRedirect());
		map.put(LTIService.LTI13_CLIENT_ID, tool.getLti13ClientId());
		map.put(LTIService.LTI13_LMS_DEPLOYMENT_ID, tool.getLti13LmsDeploymentId());
		map.put(LTIService.LTI_CONSUMERKEY, tool.getConsumerKey());
		map.put(LTIService.LTI_SECRET, tool.getSecret());
		map.put(LTIService.LTI_XMLIMPORT, tool.getXmlImport());
		map.put(LTIService.LTI13_AUTO_TOKEN, tool.getLti13AutoToken());
		map.put(LTIService.LTI13_AUTO_STATE, tool.getLti13AutoState());
		map.put(LTIService.LTI13_AUTO_REGISTRATION, tool.getLti13AutoRegistration());
		map.put(LTIService.LTI_CREATED_AT, tool.getCreatedAt());
		map.put(LTIService.LTI_UPDATED_AT, tool.getUpdatedAt());
		return map;
	}

	/**
	 * Converts an {@link LtiContent} entity into the foorm-style property map that the rest of
	 * the LTI code expects. The map keys mirror the persisted columns of {@link LTIService#CONTENT_MODEL}
	 * and this method is the inverse of the field mapping performed in {@link #insertContentDao}.
	 */
	private Map<String, Object> ltiContentToMap(LtiContent content) {
		Map<String, Object> map = new HashMap<>();
		map.put(LTIService.LTI_ID, content.getId());
		map.put(LTI_TOOL_ID, content.getTool() == null ? null : content.getTool().getId());
		map.put(LTI_SITE_ID, content.getSiteId());
		map.put(LTIService.LTI_TITLE, content.getTitle());
		map.put(LTIService.LTI_DESCRIPTION, content.getDescription());
		map.put(LTIService.LTI_FRAMEHEIGHT, content.getFrameheight());
		map.put(LTIService.LTI_NEWPAGE, content.getNewpage());
		map.put(LTIService.LTI_PROTECT, content.getProtect());
		map.put(LTIService.LTI_DEBUG, content.getDebug());
		map.put(LTIService.LTI_CUSTOM, content.getCustom());
		map.put(LTIService.LTI_LAUNCH, content.getLaunch());
		map.put(LTIService.LTI_XMLIMPORT, content.getXmlimport());
		map.put(LTIService.LTI_SETTINGS, content.getSettings());
		map.put(LTIService.LTI_CONTENTITEM, content.getContentitem());
		map.put(LTIService.LTI_PLACEMENT, content.getPlacement());
		map.put(LTIService.LTI_PLACEMENTSECRET, content.getPlacementsecret());
		map.put(LTIService.LTI_OLDPLACEMENTSECRET, content.getOldplacementsecret());
		map.put(LTIService.LTI_CREATED_AT, content.getCreatedAt());
		map.put(LTIService.LTI_UPDATED_AT, content.getUpdatedAt());
		return map;
	}

	/**
	 * Converts an {@link LtiToolSite} entity into the foorm-style property map that the rest of
	 * the LTI code expects. The map keys mirror the persisted columns of {@link LTIService#TOOL_SITE_MODEL}
	 * and this method is the inverse of the field mapping performed in {@link #insertToolSiteDao}.
	 */
	private Map<String, Object> ltiToolSiteToMap(LtiToolSite toolSite) {
		Map<String, Object> map = new HashMap<>();
		map.put(LTIService.LTI_ID, toolSite.getId());
		map.put(LTI_TOOL_ID, toolSite.getTool() == null ? null : toolSite.getTool().getId());
		map.put(LTI_SITE_ID, toolSite.getSiteId());
		map.put("notes", toolSite.getNotes());
		map.put(LTIService.LTI_DEPLOYMENT_GROUP, toolSite.getDeploymentGroup());
		map.put(LTIService.LTI_CREATED_AT, toolSite.getCreatedAt());
		map.put(LTIService.LTI_UPDATED_AT, toolSite.getUpdatedAt());
		return map;
	}

	/**
	 * Converts an {@link LtiToolFunction} entity into the foorm-style property map that the rest of
	 * the LTI code expects. The map keys mirror the persisted columns of {@link LTIService#TOOL_FUNCTION_MODEL}.
	 */
	private Map<String, Object> ltiToolFunctionToMap(LtiToolFunction toolFunction) {
		Map<String, Object> map = new HashMap<>();
		map.put(LTIService.LTI_ID, toolFunction.getId());
		map.put(LTI_TOOL_ID, toolFunction.getTool() == null ? null : toolFunction.getTool().getId());
		map.put(LTIService.LTI_FUNCTION_NAME, toolFunction.getFunctionName());
		map.put(LTIService.LTI_CREATED_AT, toolFunction.getCreatedAt());
		map.put(LTIService.LTI_UPDATED_AT, toolFunction.getUpdatedAt());
		return map;
	}

	public boolean deleteToolDao(Long key, String siteId, boolean isAdminRole, boolean isMaintainRole) {
		return deleteThingDao("lti_tools", LTIService.TOOL_MODEL, key, siteId, isAdminRole, isMaintainRole);
	}

	public Object updateToolDao(Long key, Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole) {
		Long stealth = Long.valueOf(LTIService.LTI_VISIBLE_STEALTH);
		Map<String, Object> oldTool = getToolDao(key, siteId, true);
		Long oldVisible = oldTool == null ? null : LTIUtil.toLongNull(oldTool.get(LTI_VISIBLE));
		Object result = updateThingDao("lti_tools", LTIService.TOOL_MODEL, null, key, (Object) newProps, siteId, isAdminRole, isMaintainRole);
		if (Boolean.TRUE.equals(result)) {
			Map<String, Object> updatedTool = getToolDao(key, siteId, true);
			Long updatedVisible = updatedTool == null ? null : LTIUtil.toLongNull(updatedTool.get(LTI_VISIBLE));
			if (!stealth.equals(oldVisible) && stealth.equals(updatedVisible)) {
				deployToolToContentSites(key, isAdminRole, isMaintainRole);
			}
		}
		return result;
	}

	public List<Map<String, Object>> getToolsDao(String search, String order, int first, int last, String siteId, boolean isAdminRole) {
		return getToolsDao(search, order, first, last, siteId, isAdminRole, false);
	}

	public List<Map<String, Object>> getToolsDao(String search, String order, int first, int last, String siteId, boolean isAdminRole, boolean isStealthed) {
		return getToolsDao(search, order, first, last, siteId, isAdminRole, isStealthed, true);
	}

	public List<Map<String, Object>> getToolsDao(String search, String order, int first, int last, String siteId, boolean isAdminRole, boolean isStealthed, boolean includeLaunchable) {

		// The default (unordered) view carries the per-tool content and site counts
		boolean attachCounts = StringUtils.isBlank(order);

		List<Map<String, Object>> tools = new ArrayList<>();
		for (LtiTool tool : ltiToolRepository.findVisibleTools(siteId, isAdminRole, isStealthed, includeLaunchable)) {
			Map<String, Object> map = ltiToolToMap(tool);
			if (matchesToolSearch(map, search)) {
				tools.add(map);
			}
		}

		if (attachCounts) {
			Map<Long, long[]> counts = toolContentCounts();
			for (Map<String, Object> map : tools) {
				Object id = map.get(LTIService.LTI_ID);
				long[] toolCounts = (id instanceof Long) ? counts.get(id) : null;
				map.put("lti_content_count", (toolCounts == null) ? 0L : toolCounts[0]);
				map.put("lti_site_count", (toolCounts == null) ? 0L : toolCounts[1]);
			}
		}

		orderMaps(tools, order, TOOL_FIELD_NAMES);
		return paginate(tools, first, last);
	}

	// Per-tool content/site counts keyed by tool id: value is {contentCount, distinctSiteCount}
	private Map<Long, long[]> toolContentCounts() {
		Map<Long, long[]> counts = new HashMap<>();
		for (Object[] row : ltiContentRepository.countContentsByTool()) {
			Long toolId = ((Number) row[0]).longValue();
            counts.put(toolId, new long[] { ((Number) row[1]).longValue(), ((Number) row[2]).longValue() });
		}
		return counts;
	}

	/**
	 * @return Returns String (falure) or Long (key on success)
	 */
	public Object insertContentDao(Properties newProps, String siteId, boolean isAdminRole, boolean isMaintainRole) {
		if ( newProps == null ) {
			throw new IllegalArgumentException(
					"newProps must be non-null");
		}
		if (siteId == null && !isAdminRole ) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}

		if (!isMaintainRole) return null;

		String toolId = newProps.getProperty(LTI_TOOL_ID);
		if (toolId == null)
			return rb.getString("error.missing.toolid");
		Long toolKey = null;
		try {
			toolKey = new Long(toolId);
		} catch (Exception e) {
			return rb.getString("error.invalid.toolid");
		}

		// Load the tool we are aiming for Using DAO
		Map<String, Object> tool = null;
		tool = getToolDao(toolKey, siteId, isAdminRole);
			
		if ( tool == null ) {
			return rb.getString("error.invalid.toolid");
		}

		Long visible = LTIUtil.toLongNull(tool.get(LTI_VISIBLE));
		if ( visible == null ) visible = new Long(0);
		if ( ! isAdminRole ) {
			// Tool is stealthed and not deployed to the site.
			if ( visible == 1 && !toolDeployed(toolKey, siteId)) {
				return rb.getString("error.invalid.toolid");
			}
		}

		String[] contentModel = getContentModelDao(tool, isAdminRole);

		if (contentModel == null)
			return rb.getString("error.invalid.toolid");

		// Non-admins can only insert content into their own site
		if (!isAdminRole) {
			newProps.setProperty(LTI_SITE_ID, siteId);
		}

		// Check that all required fields are present and in the proper format before persisting
		Map<String, Object> newMapping = new HashMap<>();
		String errors = foorm.formExtract(newProps, contentModel, rb, true, newMapping, null);
		if (errors != null) {
			return errors;
		}

		LtiContent ltiContent = new LtiContent();
		ltiContent.setSiteId(isAdminRole ? (String) newMapping.get(LTI_SITE_ID) : siteId);
		ltiContent.setTool(ltiToolRepository.findById(toolKey.longValue()).orElse(null));
		ltiContent.setTitle((String) newMapping.get(LTIService.LTI_TITLE));
		ltiContent.setDescription((String) newMapping.get(LTIService.LTI_DESCRIPTION));
		ltiContent.setFrameheight(LTIUtil.toInteger(newMapping.get(LTIService.LTI_FRAMEHEIGHT), null));
		Integer newpage = LTIUtil.toInteger(newMapping.get(LTIService.LTI_NEWPAGE), null);
		if (newpage != null) ltiContent.setNewpage(newpage);
		Integer protect = LTIUtil.toInteger(newMapping.get(LTIService.LTI_PROTECT), null);
		if (protect != null) ltiContent.setProtect(protect);
		Integer debug = LTIUtil.toInteger(newMapping.get(LTIService.LTI_DEBUG), null);
		if (debug != null) ltiContent.setDebug(debug);
		ltiContent.setCustom((String) newMapping.get(LTIService.LTI_CUSTOM));
		ltiContent.setLaunch((String) newMapping.get(LTIService.LTI_LAUNCH));
		ltiContent.setXmlimport((String) newMapping.get(LTIService.LTI_XMLIMPORT));
		ltiContent.setSettings((String) newMapping.get(LTIService.LTI_SETTINGS));
		ltiContent.setContentitem((String) newMapping.get(LTIService.LTI_CONTENTITEM));
		ltiContent.setPlacement((String) newMapping.get(LTIService.LTI_PLACEMENT));
		ltiContent.setPlacementsecret((String) newMapping.get(LTIService.LTI_PLACEMENTSECRET));
		ltiContent.setOldplacementsecret((String) newMapping.get(LTIService.LTI_OLDPLACEMENTSECRET));

		Instant now = Instant.now();
		ltiContent.setCreatedAt(now);
		ltiContent.setUpdatedAt(now);

		return ltiContentRepository.save(ltiContent).getId();
	}

	@Override
	public Map<String, Object> getContentDao(Long key, String siteId, boolean isAdminRole) {
		Map<String, Object> retval = getThingDao("lti_content", LTIService.CONTENT_MODEL, key, siteId, isAdminRole);
		if (retval == null) return retval;
		retval.put("launch_url", getContentLaunch(retval));
		return retval;
	}

	public boolean deleteContentDao(Long key, String siteId, boolean isAdminRole, boolean isMaintainRole) {
		deleteContentLinkDao(key, siteId, isAdminRole, isMaintainRole);
		return deleteThingDao("lti_content", LTIService.CONTENT_MODEL, key, siteId, isAdminRole, isMaintainRole);
	}

	public Object updateContentDao(Long key, Object newProps, String siteId,
		boolean isAdminRole, boolean isMaintainRole)
	{
		if ( key == null || newProps == null ) {
			throw new IllegalArgumentException(
					"both key and newProps must be non-null");
		}
		if (siteId == null && !isAdminRole ) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}

		// Load the content item
		Map<String,Object> content = getContentDao(key, siteId, isAdminRole);
		if (  content == null ) {
			return rb.getString("error.content.not.found");
		}
		Long oldToolKey = LTIUtil.toLongNull(content.get(LTI_TOOL_ID));

		Object oToolId = (Object) foorm.getField(newProps, LTI_TOOL_ID);
		Long newToolKey = null;
		if ( oToolId != null && oToolId instanceof Number ) {
			newToolKey = new Long( ((Number) oToolId).longValue());
		} else if ( oToolId != null ) {
			try {
				newToolKey = new Long((String) oToolId);
			} catch (Exception e) {
				return rb.getString("error.invalid.toolid");
			}
		}
		if ( newToolKey == null || newToolKey < 0 ) newToolKey = oldToolKey;

		// Load the tool we are aiming for
		Map<String, Object> tool = getToolDao(newToolKey, siteId, isAdminRole);
		if ( tool == null ) {
			return rb.getString("error.invalid.toolid");
		}

		// If the user is not an admin, they cannot switch to 
		// a tool that is stealthed
		Long visible = LTIUtil.toLongNull(tool.get(LTI_VISIBLE));
		if ( visible == null ) visible = new Long(0);
		if ( ( !isAdminRole ) && ( ! oldToolKey.equals(newToolKey) )  ) {
			// Tool is stealthed and not deployed to the site.
			if ( visible == 1 && !toolDeployed(newToolKey, siteId)) {
				return rb.getString("error.invalid.toolid");
			}
		}

		String[] contentModel = getContentModelDao(tool, isAdminRole);
		if (contentModel == null)
			return rb.getString("error.invalid.toolid");

		return updateThingDao("lti_content", contentModel, LTIService.CONTENT_MODEL, 
			key, newProps, siteId, isAdminRole, isMaintainRole);
	}

	// Valid search/order field names for each model (used for app-side filtering and ordering)
	private static final Set<String> CONTENT_FIELD_NAMES = searchFieldNames((String[]) ArrayUtils.addAll(LTIService.CONTENT_MODEL, LTIService.CONTENT_EXTRA_FIELDS));
	private static final Set<String> TOOL_FIELD_NAMES = searchFieldNames(LTIService.TOOL_MODEL);
	private static final Set<String> TOOL_SITE_FIELD_NAMES = searchFieldNames(LTIService.TOOL_SITE_MODEL);
	private static final Set<String> TOOL_FUNCTION_FIELD_NAMES = searchFieldNames(LTIService.TOOL_FUNCTION_MODEL);

	private static Set<String> searchFieldNames(String[] model) {
		Set<String> names = new HashSet<>();
		for (String line : model) {
			String[] parts = line.split(":");
			if (parts.length == 0) {
				continue;
			}
			String type = (parts.length > 1) ? parts[1] : "";
			if ("header".equals(type) || line.contains("persist=false")) {
				continue; // section headers and non-persisted fields are not searchable/orderable
			}
			names.add(parts[0]);
		}
		return names;
	}

	/**
	 * Get the contents for a search, adding some data from the site (title and contact properties)
	 * and the launch from the associated tool. Content items are read through the JPA repository and
	 * the site-derived fields are resolved via the SiteService; searching, ordering and pagination
	 * are then applied in memory so that the site-derived columns remain fully searchable and sortable.
	 */
	public List<Map<String, Object>> getContentsDao(String search, String order, int first,
			int last, String siteId, boolean isAdminRole) {

		List<Map<String, Object>> contents = buildContentMaps(search, siteId, isAdminRole);
		orderMaps(contents, order, CONTENT_FIELD_NAMES);
		return paginate(contents, first, last);
	}

	/**
	 *
	 * {@inheritDoc}
	 *
	 * @see org.sakaiproject.lti.api.LTIService#countContentsDao(java.lang.String,
	 *      java.lang.String, boolean)
	 */
	public int countContentsDao(String search, String siteId, boolean isAdminRole) {
		return buildContentMaps(search, siteId, isAdminRole).size();
	}

	/**
	 * Loads all visible content items, enriches each with the tool launch and site-derived fields, and
	 * filters the result against the (foorm token) search. The returned maps are neither ordered nor paged.
	 */
	private List<Map<String, Object>> buildContentMaps(String search, String siteId, boolean isAdminRole) {

		String propertyKey = serverConfigurationService.getString(LTI_SITE_ATTRIBUTION_PROPERTY_KEY, LTI_SITE_ATTRIBUTION_PROPERTY_KEY_DEFAULT);
		Map<String, Site> siteCache = new HashMap<>();
		List<Map<String, Object>> results = new ArrayList<>();

		for (LtiContent content : ltiContentRepository.findVisibleContents(siteId, isAdminRole)) {
			Map<String, Object> map = ltiContentToMap(content);

			// Tool-derived fields (LEFT JOIN semantics - null when there is no tool)
			LtiTool tool = content.getTool();
			String toolLaunch = (tool == null) ? null : tool.getLaunch();
			map.put("URL", toolLaunch);
			map.put("searchURL", StringUtils.defaultString(content.getLaunch()) + StringUtils.defaultString(toolLaunch));

			// Site-derived fields (LEFT JOIN semantics - null when the site is missing)
			Site site = resolveSite(content.getSiteId(), siteCache);
			ResourceProperties props = (site == null) ? null : site.getProperties();
			map.put("SITE_TITLE", (site == null) ? null : site.getTitle());
			map.put("SITE_CONTACT_NAME", (props == null) ? null : props.getProperty("contact-name"));
			map.put("SITE_CONTACT_EMAIL", (props == null) ? null : props.getProperty("contact-email"));
			if (StringUtils.isNotEmpty(propertyKey)) {
				map.put("ATTRIBUTION", (props == null) ? null : props.getProperty(propertyKey));
			}

			map.put("launch_url", getContentLaunch(map));

			if (matchesContentSearch(map, search)) {
				results.add(map);
			}
		}
		return results;
	}

	private Site resolveSite(String siteId, Map<String, Site> cache) {
		if (siteId == null) {
			return null;
		}
		if (cache.containsKey(siteId)) {
			return cache.get(siteId);
		}
		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			// No such site - leave the site-derived fields null, as the LEFT JOIN would
		}
		cache.put(siteId, site);
		return site;
	}

	/**
	 * Evaluates a content search against an enriched map. The content UI only ever sends foorm token
	 * searches; a (theoretical) raw SQL clause cannot be evaluated in memory and is ignored.
	 */
	private boolean matchesContentSearch(Map<String, Object> map, String search) {
		if (StringUtils.isBlank(search)) {
			return true;
		}
		if (foorm.isSearchRaw(search)) {
			log.warn("Ignoring unsupported raw search for content list: {}", search);
			return true;
		}
		return matchesTokenSearch(map, search, CONTENT_FIELD_NAMES);
	}

	/**
	 * Evaluates a tool search against a tool map. Tool searches are either foorm token searches or raw
	 * SQL capability clauses (e.g. {@code lti_tools.pl_coursenav = 1}); both are supported in memory.
	 */
	private boolean matchesToolSearch(Map<String, Object> map, String search) {
		return matchesSearch(map, search, TOOL_FIELD_NAMES);
	}

	/**
	 * Evaluates a search against a map, supporting both foorm token searches and raw SQL clauses.
	 */
	private boolean matchesSearch(Map<String, Object> map, String search, Set<String> fieldNames) {
		if (StringUtils.isBlank(search)) {
			return true;
		}
		if (foorm.isSearchRaw(search)) {
			return matchesRawSearch(map, search);
		}
		return matchesTokenSearch(map, search, fieldNames);
	}

	/**
	 * Evaluates a foorm token search (SEARCH_FIELD:SEARCH_VALUE joined by #&#/#|#) against an enriched
	 * map. AND binds tighter than OR, mirroring the SQL that foorm.secureSearch would generate. Tokens
	 * for unknown fields or with no value are dropped (no constraint), as secureSearch would drop them.
	 */
	private boolean matchesTokenSearch(Map<String, Object> map, String search, Set<String> fieldNames) {
		List<String> tokens = foorm.getSearchTokens(search);
		List<String> separators = foorm.getSearchSeparators(search);

		boolean orAccum = false;
		Boolean currentAnd = null;
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			String field = foorm.getSearchField(token);
			if (StringUtils.isEmpty(field) || StringUtils.isEmpty(foorm.getSearchValue(token)) || !fieldNames.contains(field)) {
				continue;
			}
			boolean tokenResult = matchesSearchToken(map, token);
			if (currentAnd == null) {
				currentAnd = tokenResult;
			} else {
				String separator = (i - 1 < separators.size()) ? separators.get(i - 1) : LTIService.LTI_SEARCH_TOKEN_SEPARATOR_AND;
				if (LTIService.LTI_SEARCH_TOKEN_SEPARATOR_OR.equals(separator)) {
					orAccum = orAccum || currentAnd;
					currentAnd = tokenResult;
				} else {
					currentAnd = currentAnd && tokenResult;
				}
			}
		}
		return (currentAnd == null) ? true : (orAccum || currentAnd);
	}

	private boolean matchesSearchToken(Map<String, Object> map, String token) {
		String field = foorm.getSearchField(token);
		String value = foorm.getSearchValue(token);
		Object cell = map.get(field);

		if (LTIService.LTI_SEARCH_TOKEN_NULL.equals(value)) {
			return cell == null;
		}
		if (value.startsWith(LTIService.LTI_SEARCH_TOKEN_DATE)) {
			return matchesSearchDate(cell, value.substring(LTIService.LTI_SEARCH_TOKEN_DATE.length()));
		}
		if (value.startsWith(LTIService.LTI_SEARCH_TOKEN_EXACT)) {
			String exact = value.substring(LTIService.LTI_SEARCH_TOKEN_EXACT.length());
			return cell != null && String.valueOf(cell).equals(exact);
		}
		// Default: case-insensitive LIKE %value%
		String like = value
				.replace(LTIService.ESCAPED_LTI_SEARCH_TOKEN_SEPARATOR_AND, LTIService.LTI_SEARCH_TOKEN_SEPARATOR_AND)
				.replace(LTIService.ESCAPED_LTI_SEARCH_TOKEN_SEPARATOR_OR, LTIService.LTI_SEARCH_TOKEN_SEPARATOR_OR);
		return cell != null && String.valueOf(cell).toLowerCase().contains(like.toLowerCase());
	}

	private boolean matchesSearchDate(Object cell, String spec) {
		if (!(cell instanceof Instant) || StringUtils.isEmpty(spec)) {
			return false;
		}
		String operator = "=";
		if (spec.startsWith("<")) {
			operator = "<";
			spec = spec.substring(1);
		} else if (spec.startsWith(">")) {
			operator = ">";
			spec = spec.substring(1);
		}
		Instant target;
		try {
			Date parsed = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, rb.getLocale()).parse(spec);
			target = parsed.toInstant();
		} catch (ParseException e) {
			return false;
		}
		int cmp = ((Instant) cell).compareTo(target);
		if ("<".equals(operator)) {
			return cmp < 0;
		}
		if (">".equals(operator)) {
			return cmp > 0;
		}
		return ((Instant) cell).getEpochSecond() == target.getEpochSecond();
	}

	/**
	 * Evaluates a raw SQL capability clause (parentheses, AND/OR, {@code field OP value} and
	 * {@code field IS [NOT] NULL}) against a tool map. Used by the placement/message-type filters.
	 */
	private boolean matchesRawSearch(Map<String, Object> map, String search) {
		try {
			return new RawSearchEvaluator(search, map).evaluate();
		} catch (RuntimeException e) {
			log.warn("Could not evaluate raw search '{}': {}", search, e.toString());
			return false;
		}
	}

	private void orderMaps(List<Map<String, Object>> rows, String order, Set<String> fieldNames) {
		if (StringUtils.isBlank(order)) {
			return;
		}
		String[] parts = order.trim().split("\\s+");
		boolean ascending = true;
		if (parts.length == 2) {
			if ("desc".equalsIgnoreCase(parts[1])) {
				ascending = false;
			} else if (!"asc".equalsIgnoreCase(parts[1])) {
				throw new IllegalArgumentException("order must be [table.]field [asc|desc]");
			}
		} else if (parts.length != 1) {
			throw new IllegalArgumentException("order must be [table.]field [asc|desc]");
		}
		int dot = parts[0].indexOf('.');
		String field = (dot >= 0) ? parts[0].substring(dot + 1) : parts[0];
		if (!fieldNames.contains(field)) {
			throw new IllegalArgumentException("order must be [table.]field [asc|desc]");
		}
		Comparator<Map<String, Object>> comparator = (a, b) -> compareSearchCells(a.get(field), b.get(field));
		rows.sort(ascending ? comparator : comparator.reversed());
	}

	private int compareSearchCells(Object a, Object b) {
		if (a == null && b == null) {
			return 0;
		}
		if (a == null) {
			return 1; // nulls last
		}
		if (b == null) {
			return -1;
		}
		if (a instanceof Instant && b instanceof Instant) {
			return ((Instant) a).compareTo((Instant) b);
		}
		if (a instanceof Number && b instanceof Number) {
			return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
		}
		return String.valueOf(a).compareToIgnoreCase(String.valueOf(b));
	}

	private List<Map<String, Object>> paginate(List<Map<String, Object>> list, int first, int last) {
		// last == 0 means "no paging"; first > last matches foorm.getPagedSelect returning the statement unchanged
		if (last == 0 || first > last) {
			return list;
		}
		int from = Math.max(0, first);
		if (from >= list.size()) {
			return new ArrayList<>();
		}
		int to = Math.min(list.size(), last + 1);
		return new ArrayList<>(list.subList(from, to));
	}

	/**
	 * A small recursive-descent evaluator for the raw SQL capability clauses that the tool filters use.
	 * Supports parentheses, AND/OR (case-insensitive), comparisons ({@code = != <> < > <= >=}) and
	 * {@code IS [NOT] NULL} against the fields of a single tool map. Identifiers may be qualified with a
	 * table prefix (e.g. {@code lti_tools.pl_launch}), which is stripped before the map lookup.
	 *
	 * CLAUDE Opus wrote this. I'm pretty sure we can remove the sql search terms but this works.
	 */
	private static final class RawSearchEvaluator {

		private final List<String> tokens;
		private final Map<String, Object> row;
		private int pos;

		RawSearchEvaluator(String search, Map<String, Object> row) {
			this.tokens = tokenize(search);
			this.row = row;
		}

		boolean evaluate() {
			pos = 0;
			boolean value = parseOr();
			if (pos != tokens.size()) {
				throw new IllegalArgumentException("Unexpected token: " + tokens.get(pos));
			}
			return value;
		}

		private boolean parseOr() {
			boolean value = parseAnd();
			while (peekKeyword("OR")) {
				pos++;
				value = parseAnd() | value;
			}
			return value;
		}

		private boolean parseAnd() {
			boolean value = parseTerm();
			while (peekKeyword("AND")) {
				pos++;
				value = parseTerm() & value;
			}
			return value;
		}

		private boolean parseTerm() {
			if (peek("(")) {
				pos++;
				boolean value = parseOr();
				expect(")");
				return value;
			}
			return parseComparison();
		}

		private boolean parseComparison() {
			String field = stripTable(next());
			Object cell = row.get(field);
			String op = next();
			if ("IS".equalsIgnoreCase(op)) {
				boolean negate = peekKeyword("NOT");
				if (negate) {
					pos++;
				}
				expectKeyword("NULL");
				return negate ? cell != null : cell == null;
			}
			return compare(cell, op, unquote(next()));
		}

		private boolean compare(Object cell, String op, String value) {
			if (cell == null) {
				return false; // SQL comparisons against NULL are never true
			}
			Double cellNum = toDouble(cell);
			Double valNum = toDouble(value);
			int cmp = (cellNum != null && valNum != null)
					? Double.compare(cellNum, valNum)
					: String.valueOf(cell).compareTo(value);
			switch (op) {
				case "=":
				case "==":
					return cmp == 0;
				case "!=":
				case "<>":
					return cmp != 0;
				case "<":
					return cmp < 0;
				case ">":
					return cmp > 0;
				case "<=":
					return cmp <= 0;
				case ">=":
					return cmp >= 0;
				default:
					throw new IllegalArgumentException("Unsupported operator: " + op);
			}
		}

		private String next() {
			if (pos >= tokens.size()) {
				throw new IllegalArgumentException("Unexpected end of search");
			}
			return tokens.get(pos++);
		}

		private boolean peek(String token) {
			return pos < tokens.size() && tokens.get(pos).equals(token);
		}

		private boolean peekKeyword(String keyword) {
			return pos < tokens.size() && tokens.get(pos).equalsIgnoreCase(keyword);
		}

		private void expect(String token) {
			if (!peek(token)) {
				throw new IllegalArgumentException("Expected '" + token + "'");
			}
			pos++;
		}

		private void expectKeyword(String keyword) {
			if (!peekKeyword(keyword)) {
				throw new IllegalArgumentException("Expected '" + keyword + "'");
			}
			pos++;
		}

		private static String stripTable(String identifier) {
			int dot = identifier.lastIndexOf('.');
			return (dot >= 0) ? identifier.substring(dot + 1) : identifier;
		}

		private static String unquote(String value) {
			if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
				return value.substring(1, value.length() - 1);
			}
			return value;
		}

		private static Double toDouble(Object o) {
			if (o instanceof Number) {
				return ((Number) o).doubleValue();
			}
			try {
				return Double.valueOf(String.valueOf(o));
			} catch (NumberFormatException e) {
				return null;
			}
		}

		private static List<String> tokenize(String s) {
			List<String> out = new ArrayList<>();
			int i = 0;
			int n = s.length();
			while (i < n) {
				char c = s.charAt(i);
				if (Character.isWhitespace(c)) {
					i++;
				} else if (c == '(' || c == ')') {
					out.add(String.valueOf(c));
					i++;
				} else if (c == '\'') {
					int j = i + 1;
					while (j < n && s.charAt(j) != '\'') {
						j++;
					}
					if (j < n) {
						j++; // include closing quote
					}
					out.add(s.substring(i, j));
					i = j;
				} else if (c == '=' || c == '<' || c == '>' || c == '!') {
					if (i + 1 < n && (s.charAt(i + 1) == '=' || (c == '<' && s.charAt(i + 1) == '>'))) {
						out.add(s.substring(i, i + 2));
						i += 2;
					} else {
						out.add(String.valueOf(c));
						i++;
					}
				} else if (Character.isLetterOrDigit(c) || c == '_' || c == '.') {
					int j = i;
					while (j < n) {
						char d = s.charAt(j);
						if (Character.isLetterOrDigit(d) || d == '_' || d == '.') {
							j++;
						} else {
							break;
						}
					}
					out.add(s.substring(i, j));
					i = j;
				} else {
					throw new IllegalArgumentException("Unexpected character '" + c + "' in search");
				}
			}
			return out;
		}
	}

	/**
	 * Transfer all of the content links associated with one tool to another tool
	 *
	 * @param currentTool
	 *		The tool that we are transferring the links from
	 * @param newTool
	 *		The tool that we are transferring the links to
	 * @param siteId
	 *		The siteId in use if we are not isAdminRole
	 * @param isAdminRole
	 *		This is true if we are doing this as an administrator (i.e. we can bypass
	 *		rules about SITE_ID being null in the inserted object.
	 * @return Returns String (failure) or Long (count of items changed) on success
	 */
	protected Object transferToolContentLinksDao(Long currentTool, Long newTool, String siteId, boolean isAdminRole)
	{
		if (siteId == null && !isAdminRole ) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}

		// Admins reassign links in every site; non-admins only within their own site
		int count = ltiContentRepository.reassignTool(currentTool.longValue(), newTool.longValue(), isAdminRole ? null : siteId);
		log.debug("Count={} reassigned content links from tool {} to tool {}", count, currentTool, newTool);
		return Long.valueOf(count);
	}

	private Map<String, Object> getThingDao(String table, String[] model, Long key,
			String siteId, boolean isAdminRole)
	{
		if (table == null || model == null || key == null) {
			throw new IllegalArgumentException("table, model, and key must all be non-null");
		}
		if (siteId == null && !isAdminRole ) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}

		// Tools are now read through the JPA repository, applying the visibility rules there
		if ("lti_tools".equals(table)) {
			return ltiToolRepository.findVisibleTool(key.longValue(), siteId, isAdminRole)
					.map(this::ltiToolToMap)
					.orElse(null);
		}

		// Content items are now read through the JPA repository, applying the visibility rules there
		if ("lti_content".equals(table)) {
			return ltiContentRepository.findVisibleContent(key.longValue(), siteId, isAdminRole)
					.map(this::ltiContentToMap)
					.orElse(null);
		}

		// Tool/site deployments are now read through the JPA repository, applying the visibility rules there
		if ("lti_tool_site".equals(table)) {
			return ltiToolSiteRepository.findVisibleToolSite(key.longValue(), siteId, isAdminRole)
					.map(this::ltiToolSiteToMap)
					.orElse(null);
		}

		throw new IllegalArgumentException("getThingDao does not support table: " + table);
	}

	/**
	 * 
	 * @param table
	 * @param model
	 * @param key
	 * @param siteId
	 * @param isMaintainRole
	 * @return
	 */
	public boolean deleteThingDao(String table, String[] model, Long key, String siteId, 
		boolean isAdminRole, boolean isMaintainRole) 
	{
		if (table == null || model == null || key == null) {
			throw new IllegalArgumentException("table, model, and key must all be non-null");
		}
		if (siteId == null && !isAdminRole ) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}

		if (!isMaintainRole) return false;

		// Content items are now deleted through the JPA repository, applying the site rules there
		if ("lti_content".equals(table)) {
			LtiContent content = ltiContentRepository.findById(key.longValue()).orElse(null);
			if (content == null) {
				return false;
			}
			// Non-admins can only delete content in their own site
			if (!isAdminRole && !siteId.equals(content.getSiteId())) {
				return false;
			}
			ltiContentRepository.delete(content);
			return true;
		}

		// Tools are now deleted through the JPA repository, applying the site rules there
		if ("lti_tools".equals(table)) {
			LtiTool tool = ltiToolRepository.findById(key.longValue()).orElse(null);
			if (tool == null) {
				return false;
			}
			// Non-admins can only delete tools in their own site
			if (!isAdminRole && !siteId.equals(tool.getSiteId())) {
				return false;
			}
			ltiToolRepository.delete(tool);
			return true;
		}

		// Tool/site deployments are now deleted through the JPA repository, applying the site rules there
		if ("lti_tool_site".equals(table)) {
			LtiToolSite toolSite = ltiToolSiteRepository.findById(key.longValue()).orElse(null);
			if (toolSite == null) {
				return false;
			}
			// Non-admins can only delete deployments in their own site
			if (!isAdminRole && !siteId.equals(toolSite.getSiteId())) {
				return false;
			}
			ltiToolSiteRepository.delete(toolSite);
			return true;
		}

		throw new IllegalArgumentException("deleteThingDao does not support table: " + table);
	}

	/**
	 * 
	 * @param table
	 * @param formModel
	 * @param fullModel
	 * @param key
	 * @param newProps
	 * @param siteId
	 * @return
	 */
	public Object updateThingDao(String table, String[] formModel, String[] fullModel, Long key, Object newProps, String siteId) {
		return updateThingDao(table, formModel, fullModel, key, newProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	/**
	 * 
	 * @param table
	 * @param formModel
	 * @param fullModel
	 * @param key
	 * @param newProps
	 * @param siteId
	 * @param isMaintainRole
	 * @return
	 */
	public Object updateThingDao(String table, String[] formModel, String[] fullModel,
			Long key, Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole) 
	{
		if (table == null || formModel == null || key == null || newProps == null) {
			throw new IllegalArgumentException(
					"table, model, key, and newProps must all be non-null");
		}
		if (siteId == null && !isAdminRole ) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}
	
		if ( ! (newProps instanceof Properties || newProps instanceof Map)  ) {
			throw new IllegalArgumentException("newProps must Properties or Map<String, Object>");
		}

		if (!isMaintainRole) return false;

		HashMap<String, Object> newMapping = new HashMap<String, Object>();

		String errors = foorm.formExtract(newProps, formModel, rb, false, newMapping, null);
		if (errors != null)
			return errors;

		final String[] columns = (fullModel == null) ? foorm.getPersistedFields(formModel) : foorm.getPersistedFields(fullModel);

		// Only admins can update *into* a site
		if ( !isAdminRole && (Arrays.asList(columns).indexOf(LTI_SITE_ID) >= 0)) {
			newMapping.put(LTI_SITE_ID, siteId);
		}

		// Remove non-persisted fields
		newMapping.keySet().removeIf(k -> Arrays.asList(columns).indexOf(k) < 0);

		// Tools are now updated through the JPA repository, applying only the supplied fields
		if ("lti_tools".equals(table)) {
			LtiTool tool = ltiToolRepository.findById(key.longValue()).orElse(null);
			if (tool == null) {
				return false;
			}
			// Non-admins can only update tools in their own site
			if (!isAdminRole && !siteId.equals(tool.getSiteId())) {
				return false;
			}
			applyLtiToolUpdates(tool, newMapping);
			tool.setUpdatedAt(Instant.now());
			ltiToolRepository.save(tool);
			return true;
		}

		// Content items are now updated through the JPA repository, applying only the supplied fields
		if ("lti_content".equals(table)) {
			LtiContent content = ltiContentRepository.findById(key.longValue()).orElse(null);
			if (content == null) {
				return false;
			}
			// Non-admins can only update content in their own site
			if (!isAdminRole && !siteId.equals(content.getSiteId())) {
				return false;
			}
			applyLtiContentUpdates(content, newMapping);
			content.setUpdatedAt(Instant.now());
			ltiContentRepository.save(content);
			return true;
		}

		// Tool/site deployments are now updated through the JPA repository, applying only the supplied fields
		if ("lti_tool_site".equals(table)) {
			LtiToolSite toolSite = ltiToolSiteRepository.findById(key.longValue()).orElse(null);
			if (toolSite == null) {
				return false;
			}
			// Non-admins can only update deployments in their own site
			if (!isAdminRole && !siteId.equals(toolSite.getSiteId())) {
				return false;
			}
			applyLtiToolSiteUpdates(toolSite, newMapping);
			toolSite.setUpdatedAt(Instant.now());
			ltiToolSiteRepository.save(toolSite);
			return true;
		}

		throw new IllegalArgumentException("updateThingDao does not support table: " + table);
	}

	/**
	 * Applies a partial update to an {@link LtiTool} entity, setting only the fields that are
	 * present in the supplied mapping (the validated, persisted columns from {@link LTIService#TOOL_MODEL}).
	 * A field that is absent is left untouched; a field that is present but null is cleared.
	 */
	private void applyLtiToolUpdates(LtiTool tool, Map<String, Object> m) {
		if (m.containsKey(LTI_SITE_ID)) tool.setSiteId((String) m.get(LTI_SITE_ID));
		if (m.containsKey(LTIService.LTI_TITLE)) tool.setTitle((String) m.get(LTIService.LTI_TITLE));
		if (m.containsKey(LTIService.LTI_DESCRIPTION)) tool.setDescription((String) m.get(LTIService.LTI_DESCRIPTION));
		if (m.containsKey(LTIService.LTI_STATUS)) tool.setStatus(LTIUtil.toInteger(m.get(LTIService.LTI_STATUS), null));
		if (m.containsKey(LTI_VISIBLE)) tool.setVisible(LTIUtil.toInteger(m.get(LTI_VISIBLE), null));
		if (m.containsKey(LTIService.LTI_DEPLOYMENT_ID)) tool.setDeploymentId(LTIUtil.toInteger(m.get(LTIService.LTI_DEPLOYMENT_ID), null));
		if (m.containsKey(LTIService.LTI_LAUNCH)) tool.setLaunch((String) m.get(LTIService.LTI_LAUNCH));
		if (m.containsKey(LTIService.LTI_NEWPAGE)) tool.setNewPage(LTIUtil.toInteger(m.get(LTIService.LTI_NEWPAGE), null));
		if (m.containsKey(LTIService.LTI_FRAMEHEIGHT)) tool.setFrameHeight(LTIUtil.toInteger(m.get(LTIService.LTI_FRAMEHEIGHT), null));
		if (m.containsKey(LTIService.LTI_FA_ICON)) tool.setFaIcon((String) m.get(LTIService.LTI_FA_ICON));
		if (m.containsKey(LTIService.LTI_MT_LAUNCH)) tool.setPlLaunch(LTIUtil.toInteger(m.get(LTIService.LTI_MT_LAUNCH), null));
		if (m.containsKey(LTIService.LTI_MT_LINKSELECTION)) tool.setPlLinkSelection(LTIUtil.toInteger(m.get(LTIService.LTI_MT_LINKSELECTION), null));
		if (m.containsKey(LTIService.LTI_MT_CONTEXTLAUNCH)) tool.setPlContextlaunch(LTIUtil.toInteger(m.get(LTIService.LTI_MT_CONTEXTLAUNCH), null));
		if (m.containsKey(LTIService.LTI_PL_LESSONSSELECTION)) tool.setPlLessonsSelection(LTIUtil.toInteger(m.get(LTIService.LTI_PL_LESSONSSELECTION), null));
		if (m.containsKey(LTIService.LTI_PL_CONTENTEDITOR)) tool.setPlContentEditor(LTIUtil.toInteger(m.get(LTIService.LTI_PL_CONTENTEDITOR), null));
		if (m.containsKey(LTIService.LTI_PL_ASSESSMENTSELECTION)) tool.setPlAssessmentSelection(LTIUtil.toInteger(m.get(LTIService.LTI_PL_ASSESSMENTSELECTION), null));
		if (m.containsKey(LTIService.LTI_PL_COURSENAV)) tool.setPlCourseNav(LTIUtil.toInteger(m.get(LTIService.LTI_PL_COURSENAV), null));
		if (m.containsKey(LTIService.LTI_PL_IMPORTITEM)) tool.setPlImportItem(LTIUtil.toInteger(m.get(LTIService.LTI_PL_IMPORTITEM), null));
		if (m.containsKey(LTIService.LTI_PL_FILEITEM)) tool.setPlFileItem(LTIUtil.toInteger(m.get(LTIService.LTI_PL_FILEITEM), null));
		if (m.containsKey(LTIService.LTI_SENDNAME)) tool.setSendName(LTIUtil.toInteger(m.get(LTIService.LTI_SENDNAME), null));
		if (m.containsKey(LTIService.LTI_SENDEMAILADDR)) tool.setSendEmailAddr(LTIUtil.toInteger(m.get(LTIService.LTI_SENDEMAILADDR), null));
		if (m.containsKey(LTIService.LTI_MT_PRIVACY)) tool.setPlPrivacy(LTIUtil.toInteger(m.get(LTIService.LTI_MT_PRIVACY), null));
		if (m.containsKey(LTIService.LTI_ALLOWOUTCOMES)) tool.setAllowOutcomes(LTIUtil.toInteger(m.get(LTIService.LTI_ALLOWOUTCOMES), null));
		if (m.containsKey(LTIService.LTI_ALLOWLINEITEMS)) tool.setAllowLineItems(LTIUtil.toInteger(m.get(LTIService.LTI_ALLOWLINEITEMS), null));
		if (m.containsKey(LTIService.LTI_ALLOWGRADEBOOKREADONLY)) tool.setAllowGradebookReadOnly(LTIUtil.toInteger(m.get(LTIService.LTI_ALLOWGRADEBOOKREADONLY), null));
		if (m.containsKey(LTIService.LTI_ALLOWROSTER)) tool.setAllowRoster(LTIUtil.toInteger(m.get(LTIService.LTI_ALLOWROSTER), null));
		if (m.containsKey(LTIService.LTI_DEBUG)) tool.setDebug(LTIUtil.toInteger(m.get(LTIService.LTI_DEBUG), null));
		if (m.containsKey(LTIService.LTI_SITEINFOCONFIG)) tool.setSiteinfoConfig(LTIUtil.toInteger(m.get(LTIService.LTI_SITEINFOCONFIG), null));
		if (m.containsKey(LTIService.LTI_SPLASH)) tool.setSplash((String) m.get(LTIService.LTI_SPLASH));
		if (m.containsKey(LTIService.LTI_CUSTOM)) tool.setCustom((String) m.get(LTIService.LTI_CUSTOM));
		if (m.containsKey(LTIService.LTI_ROLEMAP)) tool.setRolemap((String) m.get(LTIService.LTI_ROLEMAP));
		if (m.containsKey(LTIService.LTI13)) tool.setLti13(LTIUtil.toInteger(m.get(LTIService.LTI13), null));
		if (m.containsKey(LTIService.LTI13_TOOL_KEYSET)) tool.setLti13ToolKeyset((String) m.get(LTIService.LTI13_TOOL_KEYSET));
		if (m.containsKey(LTIService.LTI13_TOOL_ENDPOINT)) tool.setLti13OidcEndpoint((String) m.get(LTIService.LTI13_TOOL_ENDPOINT));
		if (m.containsKey(LTIService.LTI13_TOOL_REDIRECT)) tool.setLti13OidcRedirect((String) m.get(LTIService.LTI13_TOOL_REDIRECT));
		if (m.containsKey(LTIService.LTI13_CLIENT_ID)) tool.setLti13ClientId((String) m.get(LTIService.LTI13_CLIENT_ID));
		if (m.containsKey(LTIService.LTI13_LMS_DEPLOYMENT_ID)) tool.setLti13LmsDeploymentId((String) m.get(LTIService.LTI13_LMS_DEPLOYMENT_ID));
		if (m.containsKey(LTIService.LTI_CONSUMERKEY)) tool.setConsumerKey((String) m.get(LTIService.LTI_CONSUMERKEY));
		if (m.containsKey(LTIService.LTI_SECRET)) tool.setSecret((String) m.get(LTIService.LTI_SECRET));
		if (m.containsKey(LTIService.LTI_XMLIMPORT)) tool.setXmlImport((String) m.get(LTIService.LTI_XMLIMPORT));
		if (m.containsKey(LTIService.LTI13_AUTO_TOKEN)) tool.setLti13AutoToken((String) m.get(LTIService.LTI13_AUTO_TOKEN));
		if (m.containsKey(LTIService.LTI13_AUTO_STATE)) tool.setLti13AutoState(LTIUtil.toInteger(m.get(LTIService.LTI13_AUTO_STATE), null));
		if (m.containsKey(LTIService.LTI13_AUTO_REGISTRATION)) tool.setLti13AutoRegistration((String) m.get(LTIService.LTI13_AUTO_REGISTRATION));
	}

	/**
	 * Applies a partial update to an {@link LtiContent} entity, setting only the fields that are
	 * present in the supplied mapping (the validated, persisted columns from {@link LTIService#CONTENT_MODEL}).
	 * A field that is absent is left untouched; a field that is present but null is cleared.
	 */
	private void applyLtiContentUpdates(LtiContent content, Map<String, Object> m) {
		if (m.containsKey(LTI_TOOL_ID)) {
			Long toolId = LTIUtil.toLong(m.get(LTI_TOOL_ID), null);
			content.setTool(toolId == null ? null : ltiToolRepository.findById(toolId).orElse(null));
		}
		if (m.containsKey(LTI_SITE_ID)) content.setSiteId((String) m.get(LTI_SITE_ID));
		if (m.containsKey(LTIService.LTI_TITLE)) content.setTitle((String) m.get(LTIService.LTI_TITLE));
		if (m.containsKey(LTIService.LTI_DESCRIPTION)) content.setDescription((String) m.get(LTIService.LTI_DESCRIPTION));
		if (m.containsKey(LTIService.LTI_FRAMEHEIGHT)) content.setFrameheight(LTIUtil.toInteger(m.get(LTIService.LTI_FRAMEHEIGHT), null));
		if (m.containsKey(LTIService.LTI_NEWPAGE)) content.setNewpage(LTIUtil.toInteger(m.get(LTIService.LTI_NEWPAGE), null));
		if (m.containsKey(LTIService.LTI_PROTECT)) content.setProtect(LTIUtil.toInteger(m.get(LTIService.LTI_PROTECT), null));
		if (m.containsKey(LTIService.LTI_DEBUG)) content.setDebug(LTIUtil.toInteger(m.get(LTIService.LTI_DEBUG), null));
		if (m.containsKey(LTIService.LTI_CUSTOM)) content.setCustom((String) m.get(LTIService.LTI_CUSTOM));
		if (m.containsKey(LTIService.LTI_LAUNCH)) content.setLaunch((String) m.get(LTIService.LTI_LAUNCH));
		if (m.containsKey(LTIService.LTI_XMLIMPORT)) content.setXmlimport((String) m.get(LTIService.LTI_XMLIMPORT));
		if (m.containsKey(LTIService.LTI_SETTINGS)) content.setSettings((String) m.get(LTIService.LTI_SETTINGS));
		if (m.containsKey(LTIService.LTI_CONTENTITEM)) content.setContentitem((String) m.get(LTIService.LTI_CONTENTITEM));
		if (m.containsKey(LTIService.LTI_PLACEMENT)) content.setPlacement((String) m.get(LTIService.LTI_PLACEMENT));
		if (m.containsKey(LTIService.LTI_PLACEMENTSECRET)) content.setPlacementsecret((String) m.get(LTIService.LTI_PLACEMENTSECRET));
		if (m.containsKey(LTIService.LTI_OLDPLACEMENTSECRET)) content.setOldplacementsecret((String) m.get(LTIService.LTI_OLDPLACEMENTSECRET));
	}

	/**
	 * Applies a partial update to an {@link LtiToolSite} entity, setting only the fields that are
	 * present in the supplied mapping (the validated, persisted columns from {@link LTIService#TOOL_SITE_MODEL}).
	 * A field that is absent is left untouched; a field that is present but null is cleared.
	 */
	private void applyLtiToolSiteUpdates(LtiToolSite toolSite, Map<String, Object> m) {
		if (m.containsKey(LTI_TOOL_ID)) {
			Long toolId = LTIUtil.toLong(m.get(LTI_TOOL_ID), null);
			toolSite.setTool(toolId == null ? null : ltiToolRepository.findById(toolId).orElse(null));
		}
		if (m.containsKey(LTI_SITE_ID)) toolSite.setSiteId((String) m.get(LTI_SITE_ID));
		if (m.containsKey("notes")) toolSite.setNotes((String) m.get("notes"));
		if (m.containsKey(LTIService.LTI_DEPLOYMENT_GROUP)) toolSite.setDeploymentGroup((String) m.get(LTIService.LTI_DEPLOYMENT_GROUP));
	}

	/*-- Straight-up API methods ------------------------*/

	@Override
	public List<Map<String, Object>> getToolSitesDao(String search, String order, int first, int last, String siteId, boolean isAdminRole) {

		List<Map<String, Object>> toolSites = new ArrayList<>();
		for (LtiToolSite toolSite : ltiToolSiteRepository.findVisibleToolSites(siteId, isAdminRole)) {
			Map<String, Object> map = ltiToolSiteToMap(toolSite);
			if (matchesSearch(map, search, TOOL_SITE_FIELD_NAMES)) {
				toolSites.add(map);
			}
		}
		orderMaps(toolSites, order, TOOL_SITE_FIELD_NAMES);
		return paginate(toolSites, first, last);
	}

	@Override
	public Map<String, Object> getToolSiteDao(Long key, String siteId) {
		return getThingDao("lti_tool_site", LTIService.TOOL_SITE_MODEL, key, siteId, isAdmin(siteId));
	}

	@Override
	public Object insertToolSiteDao(Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole) {
		if (newProps == null) {
			throw new IllegalArgumentException("newProps must be non-null");
		}
		if (siteId == null && !isAdminRole) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}
		if (!(newProps instanceof Properties || newProps instanceof Map)) {
			throw new IllegalArgumentException("newProps must be Properties or Map<String, Object>");
		}

		if (!isMaintainRole) return null;

		// Non-admins can only deploy tools to their own site
		if (!isAdminRole) {
			((Map) newProps).put(LTI_SITE_ID, siteId);
		}

		// Check that all required fields are present and in the proper format before persisting
		Map<String, Object> newMapping = new HashMap<>();
		String errors = foorm.formExtract(newProps, LTIService.TOOL_SITE_MODEL, rb, true, newMapping, null);
		if (errors != null) {
			return errors;
		}

		LtiToolSite ltiToolSite = new LtiToolSite();
		ltiToolSite.setSiteId(isAdminRole ? (String) newMapping.get(LTI_SITE_ID) : siteId);
		Long toolId = LTIUtil.toLong(newMapping.get(LTI_TOOL_ID), null);
		if (toolId != null) {
			ltiToolSite.setTool(ltiToolRepository.findById(toolId).orElse(null));
		}
		ltiToolSite.setNotes((String) newMapping.get("notes"));
		ltiToolSite.setDeploymentGroup((String) newMapping.get(LTIService.LTI_DEPLOYMENT_GROUP));

		Instant now = Instant.now();
		ltiToolSite.setCreatedAt(now);
		ltiToolSite.setUpdatedAt(now);

		return ltiToolSiteRepository.save(ltiToolSite).getId();
	}

	@Override
	public Object updateToolSiteDao(Long key, Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole) {
		return updateThingDao("lti_tool_site", LTIService.TOOL_SITE_MODEL, null, key, newProps, siteId, isAdminRole, isMaintainRole);
	}

	@Override
	public boolean deleteToolSiteDao(Long key, String siteId, boolean isAdminRole, boolean isMaintainRole) {
		return deleteThingDao("lti_tool_site", LTIService.TOOL_SITE_MODEL, key, siteId, isAdminRole, isMaintainRole);
	}

	@Override
	public int deleteToolSitesForToolIdDao(String toolId) {

		Long toolKey = LTIUtil.toLong(toolId, null);
		if (toolKey == null) {
			return 0;
		}
		int count = ltiToolSiteRepository.deleteByTool_Id(toolKey);
		log.debug("Count={} deleted tool sites for tool_id={}", count, toolKey);
		return count;
	}

	@Override
	public List<Map<String, Object>> getToolFunctionsDao(String search, String order, int first, int last, String siteId, boolean isAdminRole) {

		// Tool functions have no site column, so every function is visible to everyone
		List<Map<String, Object>> toolFunctions = new ArrayList<>();
		for (LtiToolFunction toolFunction : ltiToolFunctionRepository.findAll()) {
			Map<String, Object> map = ltiToolFunctionToMap(toolFunction);
			if (matchesSearch(map, search, TOOL_FUNCTION_FIELD_NAMES)) {
				toolFunctions.add(map);
			}
		}
		orderMaps(toolFunctions, order, TOOL_FUNCTION_FIELD_NAMES);
		return paginate(toolFunctions, first, last);
	}

	@Override
	public Object insertToolFunctionDao(Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole) {
		if (newProps == null) {
			throw new IllegalArgumentException("newProps must be non-null");
		}
		if (siteId == null && !isAdminRole) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}
		if (!(newProps instanceof Properties || newProps instanceof Map)) {
			throw new IllegalArgumentException("newProps must be Properties or Map<String, Object>");
		}

		if (!isMaintainRole) return null;

		// Check that all required fields are present and in the proper format before persisting
		Map<String, Object> newMapping = new HashMap<>();
		String errors = foorm.formExtract(newProps, LTIService.TOOL_FUNCTION_MODEL, rb, true, newMapping, null);
		if (errors != null) {
			return errors;
		}

		LtiToolFunction ltiToolFunction = new LtiToolFunction();
		Long toolId = LTIUtil.toLong(newMapping.get(LTI_TOOL_ID), null);
		if (toolId != null) {
			ltiToolFunction.setTool(ltiToolRepository.findById(toolId).orElse(null));
		}
		ltiToolFunction.setFunctionName((String) newMapping.get(LTIService.LTI_FUNCTION_NAME));

		Instant now = Instant.now();
		ltiToolFunction.setCreatedAt(now);
		ltiToolFunction.setUpdatedAt(now);

		return ltiToolFunctionRepository.save(ltiToolFunction).getId();
	}

	@Override
	public int deleteToolFunctionsForToolIdDao(String toolId) {

		Long toolKey = LTIUtil.toLong(toolId, null);
		if (toolKey == null) {
			return 0;
		}
		int count = ltiToolFunctionRepository.deleteByTool_Id(toolKey);
		log.debug("Count={} deleted tool functions for tool_id={}", count, toolKey);
		return count;
	}


}
