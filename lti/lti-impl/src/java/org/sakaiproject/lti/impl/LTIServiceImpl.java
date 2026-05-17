/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *		 http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lti.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import org.json.simple.JSONObject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lti.api.LtiBearerSessionConstants;
import org.sakaiproject.lti.api.LtiBearerSessions;
import org.sakaiproject.lti.api.LTIExportService.ExportType;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.api.LTISubstitutionsFilter;
import org.sakaiproject.lti.api.LTIToolPermissionService;
import org.sakaiproject.lti.api.model.LtiContent;
import org.sakaiproject.lti.api.model.LtiMembershipsJob;
import org.sakaiproject.lti.api.model.LtiTool;
import org.sakaiproject.lti.api.model.LtiToolSite;
import org.sakaiproject.lti.api.repository.LtiContentRepository;
import org.sakaiproject.lti.api.repository.LtiMembershipsJobRepository;
import org.sakaiproject.lti.api.repository.LtiToolRepository;
import org.sakaiproject.lti.api.repository.LtiToolSiteRepository;
import org.sakaiproject.lti.beans.LtiContentBean;
import org.sakaiproject.lti.beans.LtiMembershipsJobBean;
import org.sakaiproject.lti.beans.LtiToolBean;
import org.sakaiproject.lti.beans.LtiToolSiteBean;
import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.foorm.Foorm;
import org.sakaiproject.lti.util.SakaiLTIUtil;
import org.sakaiproject.util.foorm.Foorm;
import org.tsugi.lti.LTIUtil;
import org.sakaiproject.util.MergeConfig;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LTIServiceImpl implements LTIService {

	@Autowired
	@Qualifier("org.sakaiproject.util.ResourceLoader.ltiservice")
	private ResourceLoader rb;

	@Autowired
	private LtiContentRepository contentRepository;

	@Autowired
	private LtiMembershipsJobRepository membershipsJobRepository;

	@Autowired
	private LtiToolRepository toolRepository;

	@Autowired
	private LtiToolSiteRepository toolSiteRepository;

	@Autowired
	private LTIToolPermissionService toolPermissionService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private SessionManager sessionManager;

	@Autowired
	private ServerConfigurationService serverConfigurationService;

	@Autowired
	private SiteService siteService;

	private Foorm foorm = new Foorm();

	private SecurityAdvisor satSecurityAdvisor;

	// The filters that are applied to custom properties.
	private List<LTISubstitutionsFilter> filters = new CopyOnWriteArrayList<>();

	public void init() {

		satSecurityAdvisor = new SecurityAdvisor() {

			public SecurityAdvice isAllowed(String userId, String permission, String reference) {

				if (StringUtils.startsWith(userId, LtiBearerSessionConstants.LTI_TOOL_USER_ID_PREFIX)) {
					// An LTI tool principal only carries permissions while the validated Bearer token session
					// that minted it is in effect. The user id prefix alone proves nothing, so take the tool
					// id from the access token on the session rather than from the id we were handed.
					SakaiAccessToken sat = LtiBearerSessions.getSakaiAccessToken(sessionManager.getCurrentSession());
					if (sat == null || sat.tool_id == null
							|| !userId.equals(LtiBearerSessionConstants.LTI_TOOL_USER_ID_PREFIX + sat.tool_id)) {
						log.warn("{} is an lti tool user id with no matching validated access token on the session", userId);
						return SecurityAdvice.NOT_ALLOWED;
					}

					// The access token has to carry this permission as a granted scope. The token endpoint
					// intersects the requested scopes with the tool's permissions when it mints a token, so
					// without this a narrowly scoped token would still get the tool's whole permission set.
					if (!sat.hasScope(SakaiAccessToken.permissionToLtiApiScope(permission))) {
						log.debug("Access token for tool {} carries no scope for permission {}", sat.tool_id, permission);
						return SecurityAdvice.NOT_ALLOWED;
					}

					// Re-checked here as well as at token issue time because permissions can be revoked
					// while a token is still valid.
					if (toolPermissionService.getToolPermissions(sat.tool_id).contains(permission)) {
						return SecurityAdvice.ALLOWED;
					}

					return SecurityAdvice.NOT_ALLOWED;
				} else {
					return SecurityAdvice.PASS;
				}
			}
		};

		securityService.registerAdvisor(satSecurityAdvisor);
	}

	public void destroy() {

		if (satSecurityAdvisor != null) {
			securityService.unregisterAdvisor(satSecurityAdvisor);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * LTIService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/* Tool Model */
	@Override
	public String[] getToolModel(String siteId) {
		if (isAdmin(siteId)) return TOOL_MODEL;
		return foorm.filterForm(null, TOOL_MODEL, null, ".*:role=admin.*");
	}

	@Override
	public String[] getToolSiteModel(String siteId) {
		if (isAdmin(siteId)) {
			return TOOL_SITE_MODEL;
		}
		return null;
	}

	@Override
	public String[] getContentModel(Long tool_id, String siteId) {
		Map<String, Object> tool = getTool(tool_id, siteId, isAdmin(siteId));
		return getContentModelDao(tool, isAdmin(siteId));
	}

	@Override
	public String[] getContentModelIfConfigurable(Long tool_id, String siteId) {
		Map<String, Object> tool = getTool(tool_id, siteId, isAdmin(siteId));
		if (tool == null) {
			return null;
		}

		boolean phase1 = foorm.formHasConfiguration(tool, CONTENT_MODEL, null, null);
		String[] retval = foorm.filterForm(tool, CONTENT_MODEL);
		if (!isAdmin(siteId)) {
			boolean phase2 = foorm.formHasConfiguration(null, retval, null, ".*:role=admin.*");
			if (!phase1 && !phase2) {
				return null;
			}

			retval = foorm.filterForm(null, retval, null, ".*:role=admin.*");
		}

		return retval;

	}

	@Override
	public String[] getContentModel(Map<String, Object> tool, String siteId) {
		return getContentModelDao(tool, isAdmin(siteId));
	}

	// Note that there is no
	//	 public String[] getContentModelDao(Long tool_id, String siteId)
	// on purpose - if code is doing Dao style it can retrieve its own tool

	private String[] getContentModelDao(Map<String, Object> tool, boolean isAdminRole) {
		if ( tool == null ) return null;
		String[] retval = foorm.filterForm(tool, CONTENT_MODEL);
		if (!isAdminRole) retval = foorm.filterForm(null, retval, null, ".*:role=admin.*");
		return retval;
	}

	@Override
	public String getContentLaunch(Map<String, Object> content) {
		return SakaiLTIUtil.getContentLaunch(content);
	}

	@Override
	public Long getContentKeyFromLaunch(String launch) {
		return SakaiLTIUtil.getContentKeyFromLaunch(launch);
	}

	@Override
	public String getToolLaunch(Map<String, Object> tool, String siteId) {
		return SakaiLTIUtil.getToolLaunch(tool, siteId);
	}

	@Override
	public String getExportUrl(String siteId, String filterId, ExportType exportType) {
		return SakaiLTIUtil.getExportUrl(siteId, filterId, exportType);
	}

	@Override
	public String formOutput(Object row, String fieldInfo) {
		return foorm.formOutput(row, fieldInfo, rb);
	}

	@Override
	public String formOutput(Object row, String[] fieldInfo) {
		return foorm.formOutput(row, fieldInfo, rb);
	}

	@Override
	public String formInput(Object row, String fieldInfo) {
		return foorm.formInput(row, fieldInfo, rb);
	}

	@Override
	public String formInput(Object row, String[] fieldInfo) {
		return foorm.formInput(row, fieldInfo, rb);
	}

	@Override
	public boolean isAdmin(String siteId) {
		if ( siteId == null ) {
			log.warn("isAdmin() requires non-null siteId. Returning false ...");
			return false;
		}
		if (!ADMIN_SITE.equals(siteId) ) return false;
		return isMaintain(siteId);
	}

	@Override
	public boolean isWebApiEnabled() {
		return serverConfigurationService.getBoolean(PROPERTY_WEBAPI_ENABLED, PROPERTY_WEBAPI_ENABLED_DEFAULT);
	}

	@Override
	public boolean isDirectApiEnabled() {
		return serverConfigurationService.getBoolean(PROPERTY_DIRECTAPI_ENABLED, PROPERTY_DIRECTAPI_ENABLED_DEFAULT);
	}

	@Override
	public boolean isApiEnabled() {
		return isWebApiEnabled() || isDirectApiEnabled();
	}

	@Override
	public boolean isMaintain(String siteId) {
		return siteService.allowUpdateSite(siteId);
	}

	/**
	 * Simple API signature for the update series of methods
	 */
	@Override
	@Transactional
	public Object updateTool(Long key, Map<String, Object> newProps, String siteId) {
		return updateTool(key, (Object) newProps, siteId);
	}

	@Override
	@Transactional
	public Object updateTool(Long key, Properties newProps, String siteId) {
		return updateTool(key, (Object) newProps, siteId);
	}

	private Object updateTool(Long key, Object newProps, String siteId) {
		return updateTool(key, newProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	private Object updateTool(Long key, Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole) {
		Long stealth = Long.valueOf(LTIService.LTI_VISIBLE_STEALTH);
		Map<String, Object> oldTool = getTool(key, siteId, true);
		Long oldVisible = oldTool == null ? null : LTIUtil.toLongNull(oldTool.get(LTI_VISIBLE));
		Object result = updateThingDao("lti_tools", LTIService.TOOL_MODEL, null, key, (Object) newProps, siteId, isAdminRole, isMaintainRole);
		if (Boolean.TRUE.equals(result)) {
			Map<String, Object> updatedTool = getTool(key, siteId, true);
			Long updatedVisible = updatedTool == null ? null : LTIUtil.toLongNull(updatedTool.get(LTI_VISIBLE));
			if (!stealth.equals(oldVisible) && stealth.equals(updatedVisible)) {
				deployToolToContentSites(key, isAdminRole, isMaintainRole);
			}
		}
		return result;
	}

	private void deployToolToContentSites(Long toolKey, boolean isAdminRole, boolean isMaintainRole) {
		if (toolKey == null || !isAdminRole || !isMaintainRole) {
			return;
		}

		for (String siteId : contentRepository.findSitesNeedingDeployment(toolKey)) {
			if (StringUtils.isBlank(siteId)) {
				continue;
			}
			// Match the original INNER JOIN SAKAI_SITE - only deploy to sites that actually exist
			siteService.getOptionalSite(siteId).ifPresentOrElse(site -> {

				Properties props = new Properties();
				props.setProperty(LTIService.LTI_TOOL_ID, toolKey.toString());
				props.setProperty(LTIService.LTI_SITE_ID, siteId);
				props.setProperty("notes", rb.getString("tool.added.by.insert.content"));
				Object insertResult = insertToolSiteDao(props, siteId, isAdminRole, isMaintainRole);
				if (insertResult instanceof String) {
					log.warn("Unable to deploy stealthed tool {} to site {}: {}", toolKey, site, insertResult);
				}
			}, () -> log.warn("No content site for id {}", siteId));
		}
	}

	@Override
	public Object updateContent(Long key, Properties newProps, String siteId)
	{
		return updateContentDao(key, newProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	@Override
	public Object updateContent(Long key, Map<String, Object> map, String siteId)
	{
		return updateContentDao(key, map, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	@Override
	public Object updateContent(Long key, Map<String, Object> newProps)
	{
		// siteId can be null if isAdmin is false, the item is just patched in place
		String siteId = null;
		boolean isAdmin = true;
		boolean isMaintain = true;
		return updateContentDao(key, newProps, siteId, isAdmin, isMaintain);
	}

	private Object updateContentDao(Long key, Map<String, Object> newProps, String siteId)
	{
		return updateContentDao(key, (Object) newProps, siteId, true, true);
	}

	private Object updateContentDao(Long key, Object newProps, String siteId,
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
		Map<String,Object> content = getContent(key, siteId, isAdminRole);
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
		Map<String, Object> tool = getTool(newToolKey, siteId, isAdminRole);
		if ( tool == null ) {
			return rb.getString("error.invalid.toolid");
		}

		// If the user is not an admin, they cannot switch to
		// a tool that is stealthed
		Long visible = LTIUtil.toLongNull(tool.get(LTI_VISIBLE));
		if ( visible == null ) visible = new Long(0);
		if ( ( !isAdminRole ) && ( oldToolKey == null || ! oldToolKey.equals(newToolKey) )  ) {
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

	@Override
	public boolean deleteContent(Long key, String siteId) {

		boolean isAdminRole = isAdmin(siteId);
		boolean isMaintainRole = isMaintain(siteId);
		deleteContentLinkDao(key, siteId, isAdminRole, isMaintainRole);

		return contentRepository.findById(key).map(content -> {
				// Non-admins can only delete content in their own site
				if (!isAdminRole && !StringUtils.equals(siteId, content.getSiteId())) {
					return false;
				}
				contentRepository.delete(content);
				return true;
			}).orElse(false);
	}

	private static int getInt(Object o) {
		if (o instanceof String) {
			try {
				return new Integer((String) o);
			} catch (Exception e) {
				return -1;
			}
		}
		if (o instanceof Number)
			return ((Number) o).intValue();
		return -1;
	}

	/**
	 * Adjust the content object based on the settings in the tool object
	 */
	@Override
	public void filterContent(Map<String, Object> content, Map<String, Object> tool) {
		if (content == null || tool == null)
			return;
		int toolHeight = getInt(tool.get(LTIService.LTI_FRAMEHEIGHT));
		int contentHeight = getInt(content.get(LTIService.LTI_FRAMEHEIGHT));
		int frameHeight = 1200;
		if (toolHeight > 0)
			frameHeight = toolHeight;
		if (contentHeight > 0)
			frameHeight = contentHeight;
		content.put(LTIService.LTI_FRAMEHEIGHT, new Integer(frameHeight));

		int debug = getInt(tool.get(LTIService.LTI_DEBUG));
		if ( debug == 2 ) debug = getInt(content.get(LTIService.LTI_DEBUG));
		content.put(LTIService.LTI_DEBUG, debug+"");

		int newpage = getInt(tool.get(LTIService.LTI_NEWPAGE));
		if ( newpage == 2 ) newpage = getInt(content.get(LTIService.LTI_NEWPAGE));
		content.put(LTIService.LTI_NEWPAGE, newpage+"");
	}

	@Override
	public Object insertMembershipsJob(String siteId, String membershipsId, String membershipsUrl, String consumerKey, String ltiVersion) {
		log.debug("insertMembershipsJobDao({},{},{},{},{})", siteId, membershipsId, membershipsUrl, consumerKey, ltiVersion);

		// First, check if there is already a job for this site.
		if (membershipsJobRepository.findById(siteId).isEmpty()) {
			LtiMembershipsJob job = new LtiMembershipsJob();
			job.setSiteId(siteId);
			job.setMembershipsId(membershipsId);
			job.setMembershipsUrl(membershipsUrl);
			job.setConsumerkey(consumerKey);
			job.setLtiVersion(ltiVersion);
			return membershipsJobRepository.save(job);
		} else {
			return "SITE_ALREADY_JOBBED";
		}
	}

	@Override
	public Map<String, Object> getMembershipsJob(String siteId) {
		log.debug("getMembershipsJobDao({})", siteId);
		return membershipsJobRepository.findById(siteId).map(this::ltiMembershipsJobToMap).orElse(null);
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

	private List<Map<String, Object>> getMembershipsJobsDao() {

		log.debug("getMembershipsJobsDao()");
		return membershipsJobRepository.findAll()
			.stream()
			.collect(Collectors.mapping(this::ltiMembershipsJobToMap, Collectors.toList()));
	}

	public List<Map<String, Object>> getMembershipsJobs() {
		return getMembershipsJobsDao();
	}

	@Override
	public String validateTool(Properties newProps) {
		return validateTool((Map) newProps);
	}

	@Override
	public boolean isDraft(Map<String, Object> tool) {
		boolean retval = true;
		if ( tool == null ) return retval;
		if ( StringUtils.isEmpty((String) tool.get(LTI_LAUNCH)) ) return true;
		if ( SakaiLTIUtil.isLTI11(tool) ) {
			String consumerKey = (String) tool.get(LTI_CONSUMERKEY);
			String consumerSecret = (String) tool.get(LTI_SECRET);
			if ( StringUtils.isNotEmpty(consumerSecret) && StringUtils.isNotEmpty(consumerSecret)
				&& (! LTI_SECRET_INCOMPLETE.equals(consumerSecret))
				&& (! LTI_SECRET_INCOMPLETE.equals(consumerKey)) ) retval = false;
		}

		if ( SakaiLTIUtil.isLTI13(tool)
			&& StringUtils.isNotEmpty((String) tool.get(LTI13_CLIENT_ID))
			&& StringUtils.isNotEmpty((String) tool.get(LTI13_TOOL_KEYSET))
			&& StringUtils.isNotEmpty((String) tool.get(LTI13_TOOL_ENDPOINT))
			&& StringUtils.isNotEmpty((String) tool.get(LTI13_TOOL_REDIRECT))) retval = false;

		return retval;
	}

	@Override
	public String validateTool(Map<String, Object> newProps) {
		StringBuffer sb = new StringBuffer();
		if ( StringUtils.isEmpty((String) newProps.get(LTIService.LTI_TITLE)) ) {
			sb.append(" ");
			sb.append(rb.getString("export.title"));
		}
		if ( StringUtils.isEmpty((String) newProps.get(LTIService.LTI_LAUNCH)) ) {
			sb.append(" ");
			sb.append(rb.getString("export.url"));
		}
		if ( sb.length() > 0 ) return sb.toString();
		return null;
	}

	@Override
	public Object insertTool(Properties newProps, String siteId) {
		return insertTool(newProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	@Override
	public Object insertTool(Map<String, Object> newProps, String siteId) {
		return insertTool(newProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	public Object insertToolDao(Properties newProps, String siteId) {
		return insertTool(newProps, siteId, true, true);
	}

	@Override
	public Object insertTool(Object newPropsObject, String siteId, boolean isAdminRole, boolean isMaintainRole) {

		Map<String, Object> newProps = new HashMap<>();
		String errors = foorm.formExtract(newPropsObject, LTIService.TOOL_MODEL, rb, true, newProps, null);
		if (errors != null) {
			return errors;
		}

		LtiTool ltiTool = new LtiTool();
		if (!ADMIN_SITE.equals(siteId)) ltiTool.setSiteId(siteId);
		ltiTool.setTitle((String) newProps.get(LTIService.LTI_TITLE));
		ltiTool.setDescription((String) newProps.get(LTIService.LTI_DESCRIPTION));
		ltiTool.setStatus(LTIUtil.toInteger(newProps.get(LTIService.LTI_STATUS), -1));
		ltiTool.setVisible(LTIUtil.toInteger(newProps.get(LTIService.LTI_VISIBLE), -1));
		ltiTool.setDeploymentId((String) newProps.get(LTIService.LTI_DEPLOYMENT_ID));
		ltiTool.setLaunch((String) newProps.get(LTIService.LTI_LAUNCH));
		ltiTool.setNewPage(LTIUtil.toInteger(newProps.get(LTIService.LTI_NEWPAGE), -1));
		ltiTool.setFrameHeight(LTIUtil.toInteger(newProps.get(LTIService.LTI_FRAMEHEIGHT), -1));
		ltiTool.setFaIcon((String) newProps.get(LTIService.LTI_FA_ICON));
		ltiTool.setPlLaunch(LTIUtil.toInteger(newProps.get(LTIService.LTI_MT_LAUNCH), -1));
		ltiTool.setPlLinkSelection(LTIUtil.toInteger(newProps.get(LTIService.LTI_MT_LINKSELECTION), -1));
		ltiTool.setPlContextlaunch(LTIUtil.toInteger(newProps.get(LTIService.LTI_MT_CONTEXTLAUNCH), -1));
		ltiTool.setPlLessonsSelection(LTIUtil.toInteger(newProps.get(LTIService.LTI_PL_LESSONSSELECTION), -1));
		ltiTool.setPlContentEditor(LTIUtil.toInteger(newProps.get(LTIService.LTI_PL_CONTENTEDITOR), -1));
		ltiTool.setPlAssessmentSelection(LTIUtil.toInteger(newProps.get(LTIService.LTI_PL_ASSESSMENTSELECTION), -1));
		ltiTool.setPlCourseNav(LTIUtil.toInteger(newProps.get(LTIService.LTI_PL_COURSENAV), -1));
		ltiTool.setPlImportItem(LTIUtil.toInteger(newProps.get(LTIService.LTI_PL_IMPORTITEM), -1));
		ltiTool.setPlFileItem(LTIUtil.toInteger(newProps.get(LTIService.LTI_PL_FILEITEM), -1));
		ltiTool.setSendName(LTIUtil.toInteger(newProps.get(LTIService.LTI_SENDNAME), -1));
		ltiTool.setSendEmailAddr(LTIUtil.toInteger(newProps.get(LTIService.LTI_SENDEMAILADDR), -1));
		ltiTool.setPlPrivacy(LTIUtil.toInteger(newProps.get(LTIService.LTI_MT_PRIVACY), -1));
		ltiTool.setAllowOutcomes(LTIUtil.toInteger(newProps.get(LTIService.LTI_ALLOWOUTCOMES), -1));
		ltiTool.setAllowLineItems(LTIUtil.toInteger(newProps.get(LTIService.LTI_ALLOWLINEITEMS), -1));
		ltiTool.setAllowGradebookReadOnly(LTIUtil.toInteger(newProps.get(LTIService.LTI_ALLOWGRADEBOOKREADONLY), -1));
		ltiTool.setAllowRoster(LTIUtil.toInteger(newProps.get(LTIService.LTI_ALLOWROSTER), -1));
		ltiTool.setDebug(LTIUtil.toInteger(newProps.get(LTIService.LTI_DEBUG), -1));
		ltiTool.setSiteinfoConfig(LTIUtil.toInteger(newProps.get(LTIService.LTI_SITEINFOCONFIG), -1));
		ltiTool.setSplash((String) newProps.get(LTIService.LTI_SPLASH));
		ltiTool.setCustom((String) newProps.get(LTIService.LTI_CUSTOM));
		ltiTool.setRolemap((String) newProps.get(LTIService.LTI_ROLEMAP));
		ltiTool.setLti13(LTIUtil.toInteger(newProps.get(LTIService.LTI13), -1));
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

		return toolRepository.save(ltiTool).getId();
	}

	/**
	 * Delete a tool and all of its dependencies. Hibernate deals with the dependencies because
	 * we have one to many relationships set up between them. If any of those cascaded deletions
	 * fail, the transaction will be rolled back.
	 *
	 * This is called by a maintain user in a regular site and deletes a tool, its content
	 * item, and pages with it on the page.
	 *
	 * For the admin user in the !admin site - it deletes a tool and then removes the
	 * placements + pages from all the sites that have the tool - might take a second or two.
	 */
	@Override
	public void deleteTool(Long key, String siteId) throws Exception {

		boolean isAdminRole = isAdmin(siteId);

		LtiTool tool = toolRepository.findById(key).orElseThrow(() -> new Exception("No tool for id " + key));

		// Non-admins can only delete tools in their own site
		if (!isAdminRole && !StringUtils.equals(siteId, tool.getSiteId())) {
			log.warn("Non admins can only delete tools in sites they maintain. Refusing to delete tool {} from site {}", key, siteId);
			throw new Exception("Unauthorized");
		}

		tool.getContents().forEach(content -> {

			// Admin edits all sites with the content item
			String contentSiteId = siteId;
			if (isAdminRole) {
				contentSiteId = content.getSiteId();
			}

			// Is there is a tool placement in the left Nav (i.e. not Lessons)
			// remove the tool content link page from the site
			String pstr = content.getPlacement();
			if (StringUtils.isNotBlank(pstr)) {
				String errstr = deleteContentLink(content.getId(), contentSiteId);
				if (errstr != null) {
					log.warn("Failed to delete content link. Reason: {}", errstr);
				}
			}
		});
		toolRepository.delete(tool);
	}

	@Override
	public Map<String, Object> getTool(Long key, String siteId) {
		return getTool(key, siteId, isAdmin(siteId));
	}

	@Override
	public Map<String, Object> getTool(Long key, String siteId, boolean isAdminRole)
	{
		return toolRepository.findVisibleTool(key, siteId, isAdminRole)
			.map(this::ltiToolToMap)
			.orElse(null);
	}

	@Override
	public List<Map<String, Object>> getTools(String search, String order, int first, int last, String siteId) {
		return getTools(search, order, first, last, siteId, false);
	}

	@Override
	public List<Map<String, Object>> getTools(String search, String order, int first, int last, String siteId, boolean includeStealthed) {
		return getToolsDao(search, order, first, last, siteId, isAdmin(siteId), includeStealthed);
	}

	@Override
	public List<Map<String, Object>> getTools(String search, String order, int first, int last, String siteId, boolean includeStealthed, boolean includeLaunchable) {
		return getToolsDao(search, order, first, last, siteId, isAdmin(siteId), includeStealthed, includeLaunchable);
	}

	private List<Map<String, Object>> getToolsDao(String search, String order, int first, int last, String siteId, boolean isAdminRole) {
		return getToolsDao(search, order, first, last, siteId, isAdminRole, false);
	}

	private List<Map<String, Object>> getToolsDao(String search, String order, int first, int last, String siteId, boolean isAdminRole, boolean isStealthed) {
		return getToolsDao(search, order, first, last, siteId, isAdminRole, isStealthed, true);
	}

	private List<Map<String, Object>> getToolsDao(String search, String order, int first, int last, String siteId, boolean isAdminRole, boolean isStealthed, boolean includeLaunchable) {

		// The default (unordered) view carries the per-tool content and site counts
		boolean attachCounts = StringUtils.isBlank(order);

		List<Map<String, Object>> tools = new ArrayList<>();
		for (LtiTool tool : toolRepository.findVisibleTools(siteId, isAdminRole, isStealthed, includeLaunchable)) {
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
		for (Object[] row : contentRepository.countContentsByTool()) {
			Long toolId = ((Number) row[0]).longValue();
			counts.put(toolId, new long[] { ((Number) row[1]).longValue(), ((Number) row[2]).longValue() });
		}
		return counts;
	}

	@Override
	public List<Map<String, Object>> getToolsLaunch(String siteId) {
		return getToolsLaunch(siteId, false);
	}

	@Override
	public List<Map<String, Object>> getToolsLaunchCourseNav(String siteId, boolean includeStealthed) {
		String query = "( lti_tools."+LTIService.LTI_MT_LAUNCH+" = 1 AND " +
			"lti_tools."+LTIService.LTI_PL_COURSENAV+" = 1 )";
		return getTools(query, LTIService.LTI_TITLE, 0, 0, siteId, includeStealthed, true);
	}

	@Override
	public List<Map<String, Object>> getToolsLaunch(String siteId, boolean includeStealthed) {
		return getTools( "lti_tools."+LTIService.LTI_MT_LAUNCH+" = 1", LTIService.LTI_TITLE, 0, 0, siteId, includeStealthed, true);
	}

	@Override
	public List<Map<String, Object>> getToolsLtiLink(String siteId) {
		return getTools("lti_tools."+LTIService.LTI_MT_LINKSELECTION+" = 1", LTIService.LTI_TITLE, 0, 0, siteId, false, true);
	}

	@Override
	public List<Map<String, Object>> getToolsFileItem(String siteId) {
		return getTools("lti_tools."+LTIService.LTI_PL_FILEITEM+" = 1", LTIService.LTI_TITLE,0,0, siteId, false, true);
	}

	@Override
	public List<Map<String, Object>> getToolsImportItem(String siteId) {
		return getTools("lti_tools."+LTIService.LTI_PL_IMPORTITEM+" = 1", LTIService.LTI_TITLE, 0 ,0, siteId, false, true);
	}


	@Override
	public List<Map<String, Object>> getToolsContentEditor(String siteId) {
		return getTools("lti_tools."+LTIService.LTI_PL_CONTENTEDITOR+" = 1", LTIService.LTI_TITLE, 0, 0, siteId, false, true);
	}

	@Override
	public List<Map<String, Object>> getToolsAssessmentSelection(String siteId) {
		return getTools("lti_tools."+LTIService.LTI_PL_ASSESSMENTSELECTION+" = 1", LTIService.LTI_TITLE, 0, 0, siteId, false, true);
	}

	@Override
	public List<Map<String, Object>> getToolsLessonsSelection(String siteId) {
		return getTools("lti_tools."+LTIService.LTI_PL_LESSONSSELECTION+" = 1", LTIService.LTI_TITLE, 0, 0, siteId, false, true);
	}

	public List<Map<String, Object>> getToolsDao(String search, String order, int first, int last, String siteId) {
		return getToolsDao(search, order, first, last, siteId, true);
	}

	@Override
	public String validateContent(Properties newProps) {
		return validateContent((Map) newProps);
	}

	@Override
	public String validateContent(Map<String, Object> newProps) {
		StringBuffer sb = new StringBuffer();
		if ( StringUtils.isEmpty((String) newProps.get(LTIService.LTI_TITLE)) ) {
			sb.append(" ");
			sb.append(rb.getString("export.title"));
		}
		if ( StringUtils.isEmpty((String) newProps.get(LTIService.LTI_LAUNCH)) ) {
			sb.append(" ");
			sb.append(rb.getString("export.url"));
		}
		if ( sb.length() > 0 ) return sb.toString();
		return null;
	}

	@Override
	public Map<String,Object> createStubLTI11Tool(String toolBaseUrl, String title) {
		Map<String, Object> retval = new HashMap ();
		retval.put(LTIService.LTI_LAUNCH,toolBaseUrl);
		retval.put(LTIService.LTI_TITLE, title);
		retval.put(LTIService.LTI_CONSUMERKEY, LTIService.LTI_SECRET_INCOMPLETE);
		retval.put(LTIService.LTI_SECRET, LTIService.LTI_SECRET_INCOMPLETE);
		retval.put(LTIService.LTI_ALLOWOUTCOMES, "1");
		retval.put(LTIService.LTI_SENDNAME, "1");
		retval.put(LTIService.LTI_SENDEMAILADDR, "1");
		retval.put(LTIService.LTI_NEWPAGE, "2");
		return retval;
	}

	@Override
	public Properties convertToProperties(Map<String, Object> map) {
		return Foorm.convertToProperties(map);
	}

	@Override
	public Object insertContent(Properties newProps, String siteId) {
		if ( newProps.getProperty(LTIService.LTI_PLACEMENTSECRET) == null ) {
			newProps.setProperty(LTIService.LTI_PLACEMENTSECRET, UUID.randomUUID().toString());
		}
		return insertContent(newProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	@Override
	public Object insertContent(Map<String, Object> newMap, String siteId) {
		return insertContent(convertToProperties(newMap), siteId);
	}

	public Object insertContentDao(Properties newProps, String siteId) {
		if ( newProps.getProperty(LTIService.LTI_PLACEMENTSECRET) == null ) {
			newProps.setProperty(LTIService.LTI_PLACEMENTSECRET, UUID.randomUUID().toString());
		}
		return insertContent(newProps, siteId, true, true);
	}

	/**
	 * @return Returns String (falure) or Long (key on success)
	 */
	@Override
	public Object insertContent(Properties newProps, String siteId, boolean isAdminRole, boolean isMaintainRole) {
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
		tool = getTool(toolKey, siteId, isAdminRole);

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
		ltiContent.setTool(toolRepository.findById(toolKey.longValue()).orElse(null));
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

		return contentRepository.save(ltiContent).getId();
	}

	@Override
	public Map<String, Object> getContent(Long key, String siteId) {
		return getContent(key, siteId, isAdmin(siteId));
	}

	// This is with absolutely no site checking...
	@Override
	public Map<String, Object> getContent(Long key) {
		return getContent(key, null, true);
	}

	private Map<String, Object> getContentDao(Long key, String siteId) {
		return getContent(key, siteId, true);
	}

	@Override
	public Map<String, Object> getContent(Long key, String siteId, boolean isAdminRole) {

		return contentRepository.findVisibleContent(key, siteId, isAdminRole).map(content -> {

				Map<String, Object> map = ltiContentToMap(content);
				map.put("launch_url", SakaiLTIUtil.getContentLaunch(map));
				return map;
			}).orElse(null);
	}

	@Override
	public List<Map<String, Object>> getContents(String search, String order, int first, int last, String siteId, boolean isAdminRole) {
		return getContentsDao(search, order, first, last, siteId, isAdminRole);
	}

	@Override
	public List<Map<String, Object>> getContents(String search, String order, int first, int last, String siteId)
	{
		return getContentsDao(search, order, first, last, siteId, isAdmin(siteId));
	}

	private List<Map<String, Object>> getContentsDao(String search, String order, int first, int last, String siteId) {
		return getContentsDao(search, order, first, last, siteId, true);
	}

	/**
	 * Get the contents for a search, adding some data from the site (title and contact properties)
	 * and the launch from the associated tool. Content items are read through the JPA repository and
	 * the site-derived fields are resolved via the SiteService; searching, ordering and pagination
	 * are then applied in memory so that the site-derived columns remain fully searchable and sortable.
	 */
	private List<Map<String, Object>> getContentsDao(String search, String order, int first,
			int last, String siteId, boolean isAdminRole) {

		List<Map<String, Object>> contents = buildContentMaps(search, siteId, isAdminRole);
		orderMaps(contents, order, CONTENT_FIELD_NAMES);
		return paginate(contents, first, last);
	}

	@Override
	public int countContents(final String search, String siteId) {
		return countContentsDao(search, siteId, isAdmin(siteId));
	}

	private int countContentsDao(String search, String siteId, boolean isAdminRole) {
		return buildContentMaps(search, siteId, isAdminRole).size();
	}

	/**
	 * Loads all visible content items, enriches each with the tool launch and site-derived fields, and
	 * filters the result against the (foorm token) search. The returned maps are neither ordered nor paged.
	 */
	private List<Map<String, Object>> buildContentMaps(String search, String siteId, boolean isAdminRole) {

		String propertyKey = serverConfigurationService.getString(LTI_SITE_ATTRIBUTION_PROPERTY_KEY, LTI_SITE_ATTRIBUTION_PROPERTY_KEY_DEFAULT);
		List<Map<String, Object>> results = new ArrayList<>();

		for (LtiContent content : contentRepository.findVisibleContents(siteId, isAdminRole)) {
			Map<String, Object> map = ltiContentToMap(content);

			// Tool-derived fields (LEFT JOIN semantics - null when there is no tool)
			LtiTool tool = content.getTool();
			String toolLaunch = (tool == null) ? null : tool.getLaunch();
			map.put("URL", toolLaunch);
			map.put("searchURL", StringUtils.defaultString(content.getLaunch()) + StringUtils.defaultString(toolLaunch));

			// Site-derived fields (LEFT JOIN semantics - null when the site is missing)
			siteService.getOptionalSite(content.getSiteId()).ifPresent(site -> {

				ResourceProperties props = site.getProperties();
				map.put("SITE_TITLE", site.getTitle());
				map.put("SITE_CONTACT_NAME", (props == null) ? null : props.getProperty("contact-name"));
				map.put("SITE_CONTACT_EMAIL", (props == null) ? null : props.getProperty("contact-email"));
				if (StringUtils.isNotEmpty(propertyKey)) {
					map.put("ATTRIBUTION", (props == null) ? null : props.getProperty(propertyKey));
				}

			});

			map.put("launch_url", SakaiLTIUtil.getContentLaunch(map));

			if (matchesContentSearch(map, search)) {
				results.add(map);
			}
		}
		return results;
	}

	@Override
	@Transactional
	public Object insertToolContent(String id, String toolId, Properties reqProps, String siteId)
	{
		return insertToolContentDao(id, toolId, reqProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	private Object insertToolContentDao(String id, String toolId, Properties reqProps, String siteId, boolean isAdminRole, boolean isMaintainRole)
	{
		log.debug("insertToolContentDao id={} toolId={} siteId={} isAdminRole={} isMaintainRole={}", id, toolId, siteId, isAdminRole, isMaintainRole);
		Object retval = null;
		if ( ! isMaintainRole ) {
			retval = rb.getString("error.maintain.edit");
			return retval;
		}
		if ( toolId == null ) {
			retval = rb.getString("error.id.not.found");
			return retval;
		}

		// Check to see if we have to fix the tool...
		String returnUrl = reqProps.getProperty("returnUrl");

		Long contentKey = null;
		Long toolKey = new Long(toolId);
		Map<String,Object> tool = getTool(toolKey, siteId, isAdminRole);
		if ( tool == null ) {
			retval = rb.getString("error.tool.not.found");
			return retval;
		}

		// Check if the tool is stealth and not yet deployed to the site
		// If the tool is not deployed and the current user is an administrator, add the site to the list of deployed sites.
		// If the tool is not deployed and the current user is not an administrator, return an error message.
		Long visible = LTIUtil.toLong(tool.get(LTIService.LTI_VISIBLE));
		String contentSite = (String) reqProps.get(LTIService.LTI_SITE_ID);
		log.debug("checking if tool {} is stealth and about to deploy to site {}, visible={}", toolKey, contentSite, visible);
		if ( contentSite != null && visible != null && visible == LTIService.LTI_VISIBLE_STEALTH ) {
			boolean isDeployed = toolDeployed(toolKey, contentSite);
			if ( isDeployed ) {
				// The tool is already deployed to the site, our work is done
			} else if ( isAdminRole ) {
				log.debug("tool {} is not deployed, adding site {} to list of deployed sites", toolKey, contentSite);
				Properties props = new Properties();
				props.setProperty(LTIService.LTI_TOOL_ID, toolKey.toString());
				props.setProperty(LTIService.LTI_SITE_ID, contentSite);
				props.setProperty("notes", rb.getString("tool.added.by.insert.content"));
				Object insertResult = insertToolSiteDao(props, contentSite, isAdminRole, isMaintainRole);
				if (insertResult instanceof String) {
					// insertToolSiteDao returned an error message
					retval = insertResult;
					return retval;
				}
			} else {
				// The tool is NOT deployed and the current user is not an administrator
				retval = rb.getString("error.tool.not.available");
				return retval;
			}
		}

		// Make sure any missing required bits are inherited from the tool.
		if ( ! reqProps.containsKey(LTIService.LTI_TOOL_ID) ) {
			reqProps.setProperty(LTIService.LTI_TOOL_ID,toolId);
		}

		if ( ! reqProps.containsKey(LTIService.LTI_TITLE) ) {
			reqProps.setProperty(LTIService.LTI_TITLE,(String) tool.get(LTIService.LTI_TITLE));
		}

		if ( id == null )
		{
			reqProps.setProperty(LTIService.LTI_PLACEMENTSECRET, UUID.randomUUID().toString());
			// insertContentDao checks to make sure that the TOOL_ID in reqProps is suitable
			retval = insertContent(reqProps, siteId, isAdminRole, isMaintainRole);
		} else {
			contentKey = new Long(id);
			if ( returnUrl != null ) {
				if ( LTI_SECRET_INCOMPLETE.equals((String) tool.get(LTI_SECRET)) &&
						LTI_SECRET_INCOMPLETE.equals((String) tool.get(LTI_CONSUMERKEY)) ) {
					String reqSecret = reqProps.getProperty(LTIService.LTI_SECRET);
					String reqKey = reqProps.getProperty(LTIService.LTI_CONSUMERKEY);
					if ( reqSecret == null || reqKey == null || reqKey.trim().length() < 1 || reqSecret.trim().length() < 1 ) {
						retval = "0" + rb.getString("error.need.key.secret");
					}
					Properties toolProps = new Properties();
					toolProps.setProperty(LTI_SECRET, reqSecret);
					toolProps.setProperty(LTI_CONSUMERKEY, reqKey);
					updateTool(toolKey, toolProps, siteId, isAdminRole, isMaintainRole);
				}
			}
			if ( reqProps.get(LTIService.LTI_PLACEMENTSECRET) == null ) {
				reqProps.setProperty(LTIService.LTI_PLACEMENTSECRET, UUID.randomUUID().toString());
			}
			retval = updateContentDao(contentKey, reqProps, siteId, isAdminRole, isMaintainRole);
		}
		return retval;
	}

	@Override
	public Object insertToolSiteLink(String id, String button_text, String siteId)
	{
		return insertToolSiteLinkDao(id, button_text, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	private Object insertToolSiteLinkDao(String id, String button_text, String siteId, boolean isAdminRole, boolean isMaintainRole)
	{
		Object retval = null;

		if ( ! isMaintainRole ) {
			retval = rb.getString("error.maintain.link");
			return retval;
		}

		if ( id == null ) {
			retval = new String("1" + rb.getString("error.id.not.found"));
			return retval;
		}

		Long key = new Long(id);
		Map<String,Object> content = getContent(key, siteId, isAdminRole);
		if ( content == null ) {
			retval = new String("1" + rb.getString("error.content.not.found"));
			return retval;
		}

		Map<String, Object> ltiTool = null;
		Long toolId = LTIUtil.toLongNull(content.get(LTI_TOOL_ID));

		if (toolId != null) {
			ltiTool = getTool(toolId, siteId, true);
		}

		if ( ltiTool == null ) {
			retval = "1" + rb.getString("error.tool.not.found");
			return retval;
		}

		String contentSite = (String) content.get(LTI_SITE_ID);
		try
		{
			Site site = siteService.getSite(contentSite);

			try
			{
				SitePage sitePage = site.addPage();

				ToolConfiguration tool = sitePage.addTool(WEB_PORTLET);

				String title = (String)content.get(LTI_TITLE);
				if (StringUtils.isBlank(title) && ltiTool != null ) {
					title = (String)ltiTool.get(LTI_TITLE);
				}
				tool.setTitle(title);

				String fa_icon = null;

				if (ltiTool != null ) {
					fa_icon = (String)ltiTool.get(LTI_FA_ICON);
				}

				if ( !StringUtils.isBlank(fa_icon) && !"none".equals(fa_icon) ) {
					tool.getPlacementConfig().setProperty("imsti.fa_icon",fa_icon);
				}

				tool.getPlacementConfig().setProperty("source",(String)content.get("launch_url"));

				sitePage.setTitle(title);
				sitePage.setTitleCustom(true);
				siteService.save(site);

				// Record the new placement in the content item
				Properties newProps = new Properties();
				newProps.setProperty(LTI_PLACEMENT, tool.getId());
				retval = updateContentDao(key, newProps, siteId, isAdminRole, isMaintainRole);
			}
			catch (PermissionException ee)
			{
				retval = new String("0" + rb.getFormattedMessage("error.link.placement.update", new Object[]{id}));
				log.warn("Cannot add page and LTI tool to site {}", siteId);
			}
		}
		catch (IdUnusedException e)
		{
			// cannot find site
			retval = new String("0" + rb.getFormattedMessage("error.link.placement.update", new Object[]{id}));
			log.warn("Cannot find site {}", contentSite);
		}

		return retval;
	}

	// Transfer content links from one tool to another
	@Override
	public Object transferToolContentLinks(Long currentTool, Long newTool, String siteId)
	{
		if ( ! isMaintain(siteId) ) {
			log.error("Must be maintain to transferToolContentLinks {}", siteId);
			return new Long(0);
		}

		// Make sure the current user can retrieve both the source and destination URLs.
		Map<String, Object> tool = getTool(currentTool, siteId);
		Map<String, Object> new_tool = getTool(newTool, siteId);
		if ( tool == null || new_tool == null) {
			return rb.getString("error.transfer.bad.tools");
		}

		return transferToolContentLinksDao(currentTool, newTool, siteId, isAdmin(siteId));
	}

	private Object transferToolContentLinksDao(Long currentTool, Long newTool)
	{
		boolean isAdminRole = true;
		String siteId = null;
		return transferToolContentLinksDao(currentTool, newTool, siteId, isAdminRole);
	}

	@Override
	public String deleteContentLink(Long key, String siteId)
	{
		return deleteContentLinkDao(key, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	private String deleteContentLinkDao(Long key, String siteId, boolean isAdminRole, boolean isMaintainRole)
	{
		if ( ! isMaintainRole ) {
			return rb.getString("error.maintain.link");
		}
		if ( key == null ) {
			return rb.getString("error.id.not.found");
		}

		Optional<LtiContent> optContent = contentRepository.findById(key);

		//Map<String,Object> content = getContentDao(key, siteId, isAdminRole);
		if ( optContent.isEmpty() ) {
			return rb.getString("error.content.not.found");
		}

		LtiContent content = optContent.get();

		String pstr = content.getPlacement();
		if ( pstr == null || pstr.length() < 1 ) {
			return rb.getString("error.placement.not.found");
		}

		ToolConfiguration tool = siteService.findTool(pstr);
		if ( tool == null ) {
			return rb.getString("error.placement.not.found");
		}

		String siteStr = (String) content.getSiteId();
		// only admin can remove content from other site
		if ( !StringUtils.equals(siteId, siteStr) && !isAdminRole ) {
			return rb.getString("error.placement.not.found");
		}

		try
		{
			Site site = siteService.getSite(siteStr);
			String sitePageId = tool.getPageId();
			SitePage page = site.getPage(sitePageId);

			if ( page != null ) {
				site.removePage(page);
				try {
					siteService.save(site);
				} catch (Exception e) {
					return rb.getString("error.placement.not.removed");
				}
			} else {
				log.warn("LTI content={} placement={} could not find page in site={}", key, tool.getId(), siteStr);
			}

			// Remove the placement from the content item
			// Our caller can remove the contentitem if they like
			Properties newProps = new Properties();
			newProps.setProperty(LTIService.LTI_PLACEMENT, "");
			content.setPlacement("");
			content = contentRepository.save(content);

			Object retval = updateContentDao(key, newProps, siteId, isAdminRole, isMaintainRole);
			if ( retval instanceof String ) {
				// Lets make this non-fatal
				return rb.getFormattedMessage("error.link.placement.update", new Object[]{retval});
			}

			// success
			return null;
		}
		catch (IdUnusedException ee)
		{
			log.warn("LTI content={} placement={} could not remove page from site={}", key, tool.getId(), siteStr);
			return new String(rb.getFormattedMessage("error.link.placement.update", new Object[]{key.toString()}));
		}
	}

	@Override
	public void registerPropertiesFilter(LTISubstitutionsFilter filter) {
		filters.add(filter);
	}

	@Override
	public void removePropertiesFilter(LTISubstitutionsFilter filter) {
		filters.remove(filter);
	}

	@Override
	public void filterCustomSubstitutions(Properties properties, Map<String, Object> tool, Site site) {
		filters.forEach(filter -> filter.filterCustomSubstitutions(properties, tool, site));
	}

	@Override
	public List<Map<String, Object>> getToolSitesByToolId(String toolId, String siteId) {
		String search = " lti_tool_site.tool_id = " + toolId;
		return getToolSites(search, null, 0, 0, siteId, isAdmin(siteId));
	}

	@Override
	public List<Map<String, Object>> getToolSites(String search, String order, int first, int last, String siteId, boolean isAdminRole) {

		List<Map<String, Object>> toolSites = new ArrayList<>();
		for (LtiToolSite toolSite : toolSiteRepository.findVisibleToolSites(siteId, isAdminRole)) {
			Map<String, Object> map = ltiToolSiteToMap(toolSite);
			if (matchesSearch(map, search, TOOL_SITE_FIELD_NAMES)) {
				toolSites.add(map);
			}
		}
		orderMaps(toolSites, order, TOOL_SITE_FIELD_NAMES);
		return paginate(toolSites, first, last);
	}

	@Override
	public Map<String, Object> getToolSiteById(Long key, String siteId) {

		return toolSiteRepository.findVisibleToolSite(key, siteId, isAdmin(siteId))
				.map(this::ltiToolSiteToMap)
				.orElse(null);
	}

	@Override
	public Object insertToolSite(Properties properties, String siteId) {
		return insertToolSiteDao(properties, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	@Override
	public Object updateToolSite(Long key, Properties newProps, String siteId) {
		return updateThingDao("lti_tool_site", LTIService.TOOL_SITE_MODEL, null, key, newProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	@Override
	public boolean deleteToolSite(Long key, String siteId) {
		return deleteToolSiteDao(key, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	private boolean deleteToolSiteDao(Long key, String siteId, boolean isAdminRole, boolean isMaintainRole) {

		return toolSiteRepository.findById(key).map(toolSite -> {
				// Non-admins can only delete deployments in their own site
				if (!isAdminRole && !siteId.equals(toolSite.getSiteId())) {
					return false;
				}
				toolSiteRepository.delete(toolSite);
				return true;
		}).orElse(false);
	}

	@Override
	public boolean toolDeployed(Long toolKey, String siteId) {
		return getToolSitesByToolId(String.valueOf(toolKey), siteId)
				.stream()
				.anyMatch(toolSite -> siteId.equals(toolSite.get(LTIService.LTI_SITE_ID)));
	}

	@Override
	public Set<String> getToolPermissions(Long toolId, String siteId) {
		if (!isAdmin(siteId) || !isApiEnabled()) {
			return Collections.emptySet();
		}
		return getToolPermissions(toolId);
	}

	@Override
	public Set<String> getToolPermissions(Long toolId) {
		return toolPermissionService.getToolPermissions(toolId);
	}

	@Override
	public void setToolPermissions(Long toolId, Set<String> permissions, String siteId) throws Exception {

		if (!isAdmin(siteId)) {
			throw new Exception("Not authorized");
		}
		if (getTool(toolId, siteId) == null) {
			throw new Exception("Tool not found");
		}

		toolPermissionService.setToolPermissions(toolId, permissions, siteId);
	}

	@Override
	public void deleteToolPermissions(Long toolId) {
		toolPermissionService.deleteToolPermissions(toolId);
	}

	@Override
	public Element archiveContentByKey(Document doc, Long contentKey, String siteId) {
		if ( contentKey == null ) return null;

		Map<String, Object> content = this.getContent(contentKey.longValue(), siteId);
		if ( content == null ) return null;

		Long toolKey = LTIUtil.toLong(content.get(LTIService.LTI_TOOL_ID));
		if (toolKey == null) return null;

		Map<String, Object> tool = this.getTool(toolKey, siteId);
		if (tool == null) return null;

		Element retval = SakaiLTIUtil.archiveContent(doc, content, tool);

		return retval;
	}

	@Override
	public void mergeContent(Element element, Map<String, Object> content, Map<String, Object> tool) {
		SakaiLTIUtil.mergeContent(element, content, tool);
	}

	@Override
	public Long mergeContentFromImport(Element element, String siteId) {

		NodeList nl = element.getElementsByTagName(LTIService.ARCHIVE_LTI_CONTENT_TAG);
		if ( nl.getLength() < 1 ) return null;

		Node toolNode = nl.item(0);
		if ( toolNode.getNodeType() != Node.ELEMENT_NODE ) return null;

		Element toolElement = (Element) toolNode;
		Map<String, Object> content = new HashMap();
		Map<String, Object> tool = new HashMap();
		this.mergeContent(toolElement, content, tool);
		String contentErrors = this.validateContent(content);
		if ( contentErrors != null ) {
			log.warn("import found invalid content tag {}", contentErrors);
			return null;
		}

		String toolErrors = this.validateTool(tool);
		if ( toolErrors != null ) {
			log.warn("import found invalid tool tag {}", toolErrors);
			return null;
		}

		// Lets find the right tool to associate with
		// See also lessonbuilder/tool/src/java/org/sakaiproject/lessonbuildertool/service/BltiEntity.java
		String launchUrl = (String) content.get(LTIService.LTI_LAUNCH);
		if ( launchUrl == null ) {
			log.warn("lti content import could not find launch url");
			return null;
		}

		log.debug("LTI Import launchUrl {}",launchUrl);
		String toolCheckSum = (String) tool.get(LTIService.SAKAI_TOOL_CHECKSUM);
		List<Map<String,Object>> tools = this.getTools(null,null,0,0, siteId);
		Map<String, Object> theTool = SakaiLTIUtil.findBestToolMatch(launchUrl, toolCheckSum, tools);
		if ( theTool == null ) {
				Object result = this.insertTool(tool, siteId);
				if ( ! (result instanceof Long) ) {
					log.info("Could not insert tool {}", result);
					return null;
				}
				theTool = this.getTool((Long) result, siteId);
		}

		Map<String, Object> theContent = null;
		if ( theTool == null ) {
			log.info("No tool to associate to content item {}", launchUrl);
			return null;
		} else {
			Long toolId = LTIUtil.toLongNull(theTool.get(LTIService.LTI_ID));
			log.debug("Matched toolId={} for launchUrl={}", toolId, launchUrl);
			content.put(LTIService.LTI_TOOL_ID, toolId.intValue());
			Object result = this.insertContent(convertToProperties(content), siteId);
			if ( ! (result instanceof Long) ) {
				log.info("Could not insert content {}", result);
				return null;
			}

			theContent = getContent((Long) result, siteId);
			if ( theContent == null) {
				log.warn("Could not re-retrieve inserted content item {}", launchUrl);
				return null;
			} else {
				Long contentKey = LTIUtil.toLongNull(theContent.get(LTIService.LTI_ID));
				log.debug("Created contentKey={} for launchUrl={}", contentKey, launchUrl);
				return contentKey;
			}
		}
	}

	@Override
	public Object copyLTIContent(Long contentKey, String siteId, String oldSiteId)
	{
		Map<String, Object> ltiContent = getContent(contentKey, oldSiteId, true);
		return copyLTIContent(ltiContent, siteId, oldSiteId);
	}

	@Override
	public Object copyLTIContent(Map<String, Object> ltiContent, String siteId, String oldSiteId)
	{
		// The ultimate tool id for the about to be created content item
		Long newToolId = null;

		// Check the tool_id - if the tool_id is global we are cool
		Long ltiToolId = LTIUtil.toLong(ltiContent.get(LTIService.LTI_TOOL_ID));

		// Get the tool bypassing security
		Map<String, Object> ltiTool = getTool(ltiToolId, siteId, true);
		if ( ltiTool == null ) {
			return null;
		}

		// Lets either verifiy we have a good tool or make a copy if needed
		String toolSiteId = (String) ltiTool.get(LTIService.LTI_SITE_ID);
		String toolLaunch = (String) ltiTool.get(LTIService.LTI_LAUNCH);
		// Global tools have no site id - the simplest case
		if ( toolSiteId == null ) {
			newToolId = ltiToolId;
		} else {
			// Check if we have a suitable tool already in the site
			List<Map<String,Object>> tools = this.getTools(null,null,0,0,siteId);
			for ( Map<String,Object> tool : tools ) {
				String oldLaunch = (String) tool.get(LTIService.LTI_LAUNCH);
				if ( oldLaunch == null ) continue;
				if ( oldLaunch.equals(toolLaunch) ) {
					newToolId = LTIUtil.toLong(tool.get(LTIService.LTI_ID));
					break;
				}
			}

			// If we don't have the tool in the new site, check the tools from the old site
			if ( newToolId == null ) {
				tools = this.getToolsDao(null,null,0,0,oldSiteId, true);
				for ( Map<String,Object> tool : tools ) {
					String oldLaunch = (String) tool.get(LTIService.LTI_LAUNCH);
					if ( oldLaunch == null ) continue;
					if ( oldLaunch.equals(toolLaunch) ) {
						// Remove stuff that will be regenerated
						tool.remove(LTIService.LTI_SITE_ID);
						tool.remove(LTIService.LTI_CREATED_AT);
						tool.remove(LTIService.LTI_UPDATED_AT);
						Object newToolInserted = this.insertTool(tool, siteId);
						if ( newToolInserted instanceof Long ) {
							newToolId = (Long) newToolInserted;
							log.debug("Copied tool={} from site={} tosite={} tool={}",ltiToolId,oldSiteId,siteId,newToolInserted);
							break;
						} else {
							log.warn("Could not insert tool - {}",newToolInserted);
							return null;
						}
					}
				}
			}

			if ( newToolId == null ) {
				log.warn("Could not copy tool, launch={}",toolLaunch);
				return null;
			}
		}

		// Finally insert the content item...
		Properties contentProps = convertToProperties(ltiContent);

		// Point at the correct (possibly the same) tool id
		contentProps.put(LTIService.LTI_TOOL_ID, newToolId.toString());

		// Track the resource_link_history
		Map<String, Object> updates = new HashMap<String, Object> ();
		String id_history = SakaiLTIUtil.trackResourceLinkID(ltiContent);
		if ( StringUtils.isNotBlank(id_history) ) {
			String new_settings = (String) contentProps.get(LTIService.LTI_SETTINGS);
			JSONObject new_json = LTIUtil.parseJSONObject(new_settings);
			new_json.put(LTIService.LTI_ID_HISTORY, id_history);
			contentProps.put(LTIService.LTI_SETTINGS, new_json.toString());
		}

		// Remove stuff that will be regenerated
		contentProps.remove(LTIService.LTI_SITE_ID);
		contentProps.remove(LTIService.LTI_CREATED_AT);
		contentProps.remove(LTIService.LTI_UPDATED_AT);

		// Most secrets are in the tool, it is rare to override in the content
		contentProps.remove(LTIService.LTI_SECRET);
		contentProps.remove("launch_url"); // Derived on retrieval

		Object result = this.insertContent(contentProps, siteId);
		return result;
	}

	@Override
	public Long getId(Map<String, Object> thing) {
		Long contentKey = LTIUtil.toLongKey(thing.get(LTIService.LTI_ID));
		return contentKey;
	}

	@Override
	public String fixLtiLaunchUrls(String text, String toContext, MergeConfig mcx) {
		String fromContext = null;
		Map<String, String> transversalMap = null;
		return fixLtiLaunchUrls(text, fromContext, toContext, mcx, transversalMap);
	}

	@Override
	public String fixLtiLaunchUrls(String text, String fromContext, String toContext, Map<String, String> transversalMap) {
		MergeConfig mcx = null;
		return fixLtiLaunchUrls(text, fromContext, toContext, mcx, transversalMap);
	}

	// http://localhost:8080/access/lti/site/7d529bf7-b856-4400-9da1-ba8670ed1489/content:1
	// http://localhost:8080/access/lti/site/7d529bf7-b856-4400-9da1-ba8670ed1489/content:42
	private String fixLtiLaunchUrls(String text, String fromContext, String toContext, MergeConfig mcx, Map<String, String> transversalMap) {
		if (StringUtils.isBlank(text)) return text;
		List<String> urls = SakaiLTIUtil.extractLtiLaunchUrls(text);
		for (String url : urls) {
			String[] pieces = SakaiLTIUtil.getContentKeyAndSiteId(url);
			if (pieces != null) {
				String linkSiteId = pieces[0];
				String linkContentId = pieces[1];

				if ( transversalMap != null && transversalMap.containsKey(url) ) {
					log.debug("Found transversal map entry for {} -> {}", url, transversalMap.get(url));
					text = text.replace(url, transversalMap.get(url));
					continue;
				}

				// Check if we can load up the content item and tool from the old context
				Long toolKey = null;
				Map<String, Object> tool = null;
				Long contentKey = Long.parseLong(linkContentId);
				Map<String, Object> content = this.getContent(contentKey, linkSiteId);
				if ( content != null ) {
					toolKey = LTIUtil.toLongNull(content.get(LTIService.LTI_TOOL_ID));
					// Make sure we can retrieve the tool in this site
					if ( toolKey != null ) tool = this.getTool(toolKey, toContext);
					if ( tool != null ) {
						log.debug("Found tool {} for content item {}",toolKey, contentKey);
					} else {
						log.debug("Found content item {} could not load associated tool {}", contentKey, toolKey);
						content = null;
						toolKey = null;
					}
				}

				// If we cannot find the content item and tool on in this server, get skeleton data
				// from the basiclti.xml import
				if ( content == null && mcx != null && mcx.ltiContentItems != null ) {
					log.debug("Could not find content item {} / {} in site {}, checking ltiContentItems", linkContentId, contentKey, linkSiteId);
					content = mcx.ltiContentItems.get(contentKey);
					tool = null;  // force creation of a new tool in findOrCreateToolForContentItem
				}

				if (content == null) {
					log.error("Could not find content item {} / {} in site {} or imported content items",linkContentId, contentKey,linkSiteId);
					continue;
				}

				if ( toolKey == null ) {
					toolKey = findOrCreateToolForContentItem(content, tool, toContext, fromContext, mcx);
					if (toolKey == null) {
						log.error("Could not associate new content item {} with a tool in site {}", contentKey, toContext);
						continue;
					}
				}

				content.put(LTIService.LTI_SITE_ID, toContext);
				content.put(LTIService.LTI_TOOL_ID, toolKey.toString());
				Object result = this.insertContent(content, toContext);
				if (result instanceof Long) {
					Long newContentId = (Long) result;
					String newUrl = serverConfigurationService.getServerUrl() + LTIService.LAUNCH_PREFIX + toContext + "/content:" + newContentId;
					text = text.replace(url, newUrl);
					if ( transversalMap != null ) transversalMap.put(url, newUrl);
					log.debug("Inserted content item {} in site {} newUrl {}", newContentId, toContext, newUrl);
				} else {
					log.error("Could not insert content item {} in site {}",contentKey,toContext);
					continue;
				}
			}
		}
		log.debug("text {}", text);
		return text;
	}

	@Override
	public LtiToolBean getToolAsBean(Long key, String siteId) {
		Map<String, Object> toolMap = getTool(key, siteId);
		return toolMap != null ? LtiToolBean.of(toolMap) : null;
	}

	@Override
	public LtiToolBean getToolBean(Long key, String siteId) {
		Map<String, Object> toolMap = getTool(key, siteId);
		return toolMap != null ? LtiToolBean.of(toolMap) : null;
	}

	@Override
	public LtiToolBean getToolDaoAsBean(Long key, String siteId, boolean isAdminRole) {
		Map<String, Object> toolMap = getTool(key, siteId, isAdminRole);
		return toolMap != null ? LtiToolBean.of(toolMap) : null;
	}

	@Override
	public List<LtiToolBean> getToolsAsBeans(String search, String order, int first, int last, String siteId) {
		List<Map<String, Object>> toolMaps = getTools(search, order, first, last, siteId);
		return toolMaps.stream()
				.map(LtiToolBean::of)
				.collect(Collectors.toList());
	}

	@Override
	public List<LtiToolBean> getToolBeans(String search, String order, int first, int last, String siteId) {
		List<Map<String, Object>> toolMaps = getTools(search, order, first, last, siteId);
		List<LtiToolBean> toolBeans = new ArrayList<>();
		for (Map<String, Object> toolMap : toolMaps) {
			toolBeans.add(LtiToolBean.of(toolMap));
		}
		return toolBeans;
	}

	@Override
	public List<LtiToolBean> getToolsAsBeans(String search, String order, int first, int last, String siteId, boolean includeStealthed) {
		List<Map<String, Object>> toolMaps = getTools(search, order, first, last, siteId, includeStealthed);
		return toolMaps.stream()
				.map(LtiToolBean::of)
				.collect(Collectors.toList());
	}

	@Override
	public List<LtiToolBean> getToolBeans(String search, String order, int first, int last, String siteId, boolean includeStealthed) {
		List<Map<String, Object>> toolMaps = getTools(search, order, first, last, siteId, includeStealthed);
		List<LtiToolBean> toolBeans = new ArrayList<>();
		for (Map<String, Object> toolMap : toolMaps) {
			toolBeans.add(LtiToolBean.of(toolMap));
		}
		return toolBeans;
	}

	@Override
	public List<LtiToolBean> getToolBeans(String search, String order, int first, int last, String siteId, boolean includeStealthed, boolean includeLaunchable) {
		List<Map<String, Object>> toolMaps = getTools(search, order, first, last, siteId, includeStealthed, includeLaunchable);
		List<LtiToolBean> toolBeans = new ArrayList<>();
		for (Map<String, Object> toolMap : toolMaps) {
			toolBeans.add(LtiToolBean.of(toolMap));
		}
		return toolBeans;
	}

	@Override
	public List<LtiToolBean> getToolsLaunchAsBeans(String siteId) {
		List<Map<String, Object>> toolMaps = getToolsLaunch(siteId);
		return toolMaps.stream()
				.map(LtiToolBean::of)
				.collect(Collectors.toList());
	}

	@Override
	public List<LtiToolBean> getToolsImportItemBeans(String siteId) {
		List<Map<String, Object>> toolMaps = getToolsImportItem(siteId);
		List<LtiToolBean> toolBeans = new ArrayList<>();
		for (Map<String, Object> toolMap : toolMaps) {
			toolBeans.add(LtiToolBean.of(toolMap));
		}
		return toolBeans;
	}

	@Override
	public LtiContentBean getContentBean(Long key, String siteId) {
		Map<String, Object> contentMap = getContent(key, siteId);
		return contentMap != null ? LtiContentBean.of(contentMap) : null;
	}

	@Override
	public List<LtiContentBean> getContentsAsBeans(String search, String order, int first, int last, String siteId) {
		List<Map<String, Object>> contentMaps = getContents(search, order, first, last, siteId);
		return contentMaps.stream()
				.map(LtiContentBean::of)
				.collect(Collectors.toList());
	}

	@Override
	public List<LtiContentBean> getContentsForToolAndSite(Long toolId, String siteId) {

		return toolRepository.findById(toolId).map(tool -> {

			return tool.getContents().stream()
				.filter(c -> StringUtils.equals(siteId, c.getSiteId()))
				.map(LtiContentBean::of)
				.map(bean -> this.decorateContentBean(tool, bean))
				.collect(Collectors.toList());
		}).orElse(Collections.emptyList());
	}

	@Override
	public List<LtiContentBean> getContentBeans(String search, String order, int first, int last, String siteId) {
		List<Map<String, Object>> contentMaps = getContents(search, order, first, last, siteId);
		List<LtiContentBean> contentBeans = new ArrayList<>();
		for (Map<String, Object> contentMap : contentMaps) {
			contentBeans.add(LtiContentBean.of(contentMap));
		}
		return contentBeans;
	}

	/**
	 * Adds the site properties to the content bean and returns the updated bean.
	 */
	private LtiContentBean decorateContentBean(LtiTool tool, LtiContentBean contentBean) {

		String propertyKey = serverConfigurationService.getString(LTI_SITE_ATTRIBUTION_PROPERTY_KEY, LTI_SITE_ATTRIBUTION_PROPERTY_KEY_DEFAULT);

		siteService.getOptionalSite(contentBean.getSiteId()).ifPresent(site -> {

			ResourceProperties props = site.getProperties();
			contentBean.setSiteTitle(site.getTitle());
			contentBean.setSiteContactName((props == null) ? null : props.getProperty("contact-name"));
			contentBean.setSiteContactEmail((props == null) ? null : props.getProperty("contact-email"));
			if (StringUtils.isNotEmpty(propertyKey)) {
				contentBean.setAttribution((props == null) ? null : props.getProperty(propertyKey));
			}

			contentBean.setUrl(tool.getLaunch());
			contentBean.setSearchUrl(StringUtils.defaultString(contentBean.getLaunch()) + StringUtils.defaultString(tool.getLaunch()));
		});

		return contentBean;
	}

	@Override
	public LtiToolSiteBean getToolSiteAsBean(Long key, String siteId) {
		Map<String, Object> toolSiteMap = getToolSiteById(key, siteId);
		return toolSiteMap != null ? LtiToolSiteBean.of(toolSiteMap) : null;
	}

	@Override
	public List<LtiToolSiteBean> getToolSitesByToolIdAsBeans(String toolId, String siteId) {
		List<Map<String, Object>> toolSiteMaps = getToolSitesByToolId(toolId, siteId);
		return toolSiteMaps.stream()
				.map(LtiToolSiteBean::of)
				.collect(Collectors.toList());
	}

	@Override
	public String getDeploymentGroupForLaunch(Long toolKey, String launchSiteId) {
		if (toolKey == null || launchSiteId == null) {
			return null;
		}
		String trimmedSite = launchSiteId.trim();
		if (trimmedSite.isEmpty()) {
			return null;
		}
		List<Map<String, Object>> rows = getToolSitesByToolId(String.valueOf(toolKey), trimmedSite);
		if (rows == null) {
			return null;
		}
		Map<String, Object> bestRow = null;
		Date bestUpdated = null;
		for (Map<String, Object> row : rows) {
			Object siteObj = row.get(LTI_SITE_ID);
			if (siteObj == null) {
				continue;
			}
			if (!trimmedSite.equals(siteObj.toString().trim())) {
				continue;
			}
			Date updated = toolSiteRowUpdatedAt(row.get(LTI_UPDATED_AT));
			if (bestRow == null || isNewerToolSiteRow(updated, bestUpdated)) {
				bestRow = row;
				bestUpdated = updated;
			}
		}
		if (bestRow == null) {
			return null;
		}
		Object dg = bestRow.get(LTI_DEPLOYMENT_GROUP);
		if (dg == null) {
			return null;
		}
		String s = dg.toString().trim();
		return s.isEmpty() ? null : s;
	}

	@Override
	public LtiMembershipsJobBean getMembershipsJobAsBean(String siteId) {
		Map<String, Object> jobMap = getMembershipsJob(siteId);
		return jobMap != null ? LtiMembershipsJobBean.of(jobMap) : null;
	}

	@Override
	public List<LtiMembershipsJobBean> getMembershipsJobsAsBeans() {
		List<Map<String, Object>> jobMaps = getMembershipsJobs();
		return jobMaps.stream()
				.map(LtiMembershipsJobBean::of)
				.collect(Collectors.toList());
	}

	@Override
	public Object insertTool(LtiToolBean toolBean, String siteId) {
		return insertTool(toolBean != null ? toolBean.asMap() : null, siteId);
	}

	@Override
	public Object insertContent(LtiContentBean contentBean, String siteId) {
		return insertContent(contentBean != null ? contentBean.asMap() : null, siteId);
	}

	@Override
	@Transactional
	public Object updateToolAsAdmin(Long key, LtiToolBean toolBean, String siteId) {
		return updateTool(key, toolBean != null ? toolBean.asMap() : null, siteId, true, true);
	}

	/**
	 * Helper method to find or create a tool for a content item
	 * @param content Content item which we are about to insert, at minimum need LTI_LAUNCH and LTI_TITLE
	 * @param tool Tool may be null, may or may not be persisted - if this exists, we will reload to verify it is accessible to the user and site
	 * @param toSiteId Target site ID
	 * @param fromSiteId Source site ID
	 * @param mcx The MergeConfig for this import
	 * @return New tool ID or null if tool cannot be found/created
	 */
	private Long findOrCreateToolForContentItem(Map<String, Object> content, Map<String, Object> tool, String toSiteId, String fromSiteId, MergeConfig mcx) {
		if ( StringUtils.isBlank(toSiteId) ) return null;

		// Get launch URL from content
		String launchUrl = (String) content.get(LTIService.LTI_LAUNCH);
		Long contentKey = this.getId(content);	// May be empty null or not yet persisted or be an id from some other system
		Long contentToolId = LTIUtil.toLongNull(content.get(LTIService.LTI_TOOL_ID));
		Map<String, Object> contentTool = null;

		if (StringUtils.isBlank(launchUrl)) {
			log.error("Could not find launch url for content item {} in site {}", launchUrl, toSiteId);
			return null;
		}

		// Check if this tool has already been created in the target site
		if (StringUtils.isNotBlank(toSiteId) && contentToolId != null) {
			contentTool = this.getTool(contentToolId, toSiteId);
			if (contentTool != null) {
				log.debug("Found tool {} for content item {} in site {}", contentToolId, launchUrl, toSiteId);
				return this.getId(contentTool);
			}
		}

		// Check if this tool can be retrieved the source site
		if (StringUtils.isNotBlank(fromSiteId) && contentToolId != null) {
			contentTool = this.getTool(contentToolId, fromSiteId);
			if (contentTool != null) {
				log.debug("Found tool {} for content item {} in site {}", contentToolId, launchUrl, fromSiteId);
				return this.getId(contentTool);
			}
		}

		// Use fuzzy launchUrl Matching to find a tool we can use - less than ideal but better than nothing
		String toolBaseUrl = SakaiLTIUtil.stripOffQuery(launchUrl);
		List<Map<String,Object>> tools = this.getTools(null, null, 0, 0, toSiteId);
		contentTool = SakaiLTIUtil.findBestToolMatch(toolBaseUrl, null, tools);
		if (contentTool != null) {
			log.debug("Found tool {} for content item {} in site {}", this.getId(contentTool), launchUrl, toSiteId);
			return this.getId(contentTool);
		}

		// Now we need to create a new tool - first check if the tool data is valid and sufficient
		log.debug("Inserting new tool for content item {} / {} in site {}", launchUrl, toolBaseUrl, toSiteId);
		if ( tool != null ) {
			String toolErrors = this.validateTool(tool);
			if ( toolErrors != null ) {
				log.debug("Could not validate tool template for content item {} in site {} {}", launchUrl, toSiteId, toolErrors);
				tool = null;
			}
		}

		// If the tool is null or invalid, check if the tool data is available in the imported content items
		if ( tool == null && mcx.ltiContentItems != null ) {
			Map<String, Object> importedContent = mcx.ltiContentItems.get(contentKey);
			if ( importedContent != null ) {
				try {
					// In order to pass only one Map through the entirety of the merge() process,
					// we store the tool in a Map<String, Object> inside of a Map<String, Object>
					Object toolObj = importedContent.get(LTIService.TOOL_IMPORT_MAP);
					if (toolObj instanceof Map) {
						@SuppressWarnings("unchecked")
						Map<String, Object> toolMap = (Map<String, Object>) toolObj;
						tool = toolMap;
						String toolErrors = this.validateTool(tool);
						if ( toolErrors != null ) {
							log.debug("Could not validate imported tool for content item map {} in site {} {}", launchUrl, toSiteId, toolErrors);
							tool = null;
						}
						log.debug("Found tool for content item in item map {} in site {} {}", launchUrl, toSiteId, toolErrors);
					}
				} catch (ClassCastException e) {
					tool = null;
				}
			}
		}

		// Fall through and create a stub tool
		if ( tool == null ) {
			String contentTitle = (String) content.get(LTIService.LTI_TITLE);
			if (StringUtils.isBlank(contentTitle)) contentTitle = toolBaseUrl;
			log.debug("Creating stub tool for content item {} / {} in site {}", launchUrl, toolBaseUrl, toSiteId);
			tool = createStubLTI11Tool(toolBaseUrl, contentTitle);
		}

		// At this point we definately have a tool
		Object toolResult = this.insertTool(tool, toSiteId);
		if (toolResult instanceof Long) {
			log.debug("Inserted stub tool {} for content item {} in site {}", toolResult, launchUrl, toSiteId);
			return (Long) toolResult;
		}

		log.warn("Could not insert stub tool for content item {} in site {}", launchUrl, toSiteId);
		return null;
	}

	@Override
	public LtiContentBean getContentAsBean(Long key, String siteId) {
		Map<String, Object> contentMap = getContent(key, siteId);
		return contentMap != null ? LtiContentBean.of(contentMap) : null;
	}

	@Override
	public Object updateContent(Long key, LtiContentBean contentBean, String siteId) {
		return updateContent(key, contentBean != null ? contentBean.asMap() : null, siteId);
	}

	@Override
	public String getContentLaunch(LtiContentBean contentBean) {
		return getContentLaunch(contentBean != null ? contentBean.asMap() : null);
	}

	@Override
	public String formOutput(LtiToolBean toolBean, String fieldinfo) {
		Map<String, Object> toolMap = (toolBean != null) ? toolBean.asMap() : null;
		return formOutput(toolMap, fieldinfo);
	}

	@Override
	public String formOutput(LtiToolBean toolBean, String[] formDefinition) {
		Map<String, Object> toolMap = (toolBean != null) ? toolBean.asMap() : null;
		return formOutput(toolMap, formDefinition);
	}

	@Override
	public String formOutput(LtiContentBean contentBean, String fieldinfo) {
		Map<String, Object> contentMap = (contentBean != null) ? contentBean.asMap() : null;
		return formOutput(contentMap, fieldinfo);
	}

	@Override
	public String formOutput(LtiContentBean contentBean, String[] formDefinition) {
		Map<String, Object> contentMap = (contentBean != null) ? contentBean.asMap() : null;
		return formOutput(contentMap, formDefinition);
	}

	@Override
	public String formOutput(LtiToolSiteBean toolSiteBean, String fieldinfo) {
		Map<String, Object> toolSiteMap = (toolSiteBean != null) ? toolSiteBean.asMap() : null;
		return formOutput(toolSiteMap, fieldinfo);
	}

	@Override
	public String formOutput(LtiToolSiteBean toolSiteBean, String[] formDefinition) {
		Map<String, Object> toolSiteMap = (toolSiteBean != null) ? toolSiteBean.asMap() : null;
		return formOutput(toolSiteMap, formDefinition);
	}

	@Override
	public String formInput(LtiToolBean toolBean, String fieldinfo) {
		Map<String, Object> toolMap = (toolBean != null) ? toolBean.asMap() : null;
		return formInput(toolMap, fieldinfo);
	}

	@Override
	public String formInput(LtiToolBean toolBean, String[] formDefinition) {
		Map<String, Object> toolMap = (toolBean != null) ? toolBean.asMap() : null;
		return formInput(toolMap, formDefinition);
	}

	@Override
	public String formInput(LtiContentBean contentBean, String fieldinfo) {
		Map<String, Object> contentMap = (contentBean != null) ? contentBean.asMap() : null;
		return formInput(contentMap, fieldinfo);
	}

	@Override
	public String formInput(LtiContentBean contentBean, String[] formDefinition) {
		Map<String, Object> contentMap = (contentBean != null) ? contentBean.asMap() : null;
		return formInput(contentMap, formDefinition);
	}

	@Override
	public String formInput(LtiToolSiteBean toolSiteBean, String fieldinfo) {
		Map<String, Object> toolSiteMap = (toolSiteBean != null) ? toolSiteBean.asMap() : null;
		return formInput(toolSiteMap, fieldinfo);
	}

	@Override
	public String formInput(LtiToolSiteBean toolSiteBean, String[] formDefinition) {
		Map<String, Object> toolSiteMap = (toolSiteBean != null) ? toolSiteBean.asMap() : null;
		return formInput(toolSiteMap, formDefinition);
	}

	/**
	 * Normalizes {@link LTIService#LTI_UPDATED_AT} values from JDBC/Foorm maps for duplicate-row resolution.
	 */
	private Date toolSiteRowUpdatedAt(Object updatedAt) {
		if (updatedAt == null) {
			return null;
		}
		if (updatedAt instanceof Instant) {
			return Date.from((Instant) updatedAt);
		}
		if (updatedAt instanceof Date) {
			return (Date) updatedAt;
		}
		if (updatedAt instanceof Number) {
			return new Date(((Number) updatedAt).longValue());
		}
		return null;
	}

	/**
	 * @return true if {@code candidate} should replace {@code best} as the newer tool-site row
	 */
	private boolean isNewerToolSiteRow(Date candidateTs, Date bestTs) {
		if (candidateTs != null) {
			return bestTs == null || candidateTs.after(bestTs);
		}
		return false;
	}

	// Valid search/order field names for each model (used for app-side filtering and ordering)
	private static final Set<String> CONTENT_FIELD_NAMES = searchFieldNames((String[]) ArrayUtils.addAll(LTIService.CONTENT_MODEL, LTIService.CONTENT_EXTRA_FIELDS));
	private static final Set<String> TOOL_FIELD_NAMES = searchFieldNames(LTIService.TOOL_MODEL);
	private static final Set<String> TOOL_SITE_FIELD_NAMES = searchFieldNames(LTIService.TOOL_SITE_MODEL);

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
	private Object transferToolContentLinksDao(Long currentTool, Long newTool, String siteId, boolean isAdminRole)
	{
		if (siteId == null && !isAdminRole ) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}

		// Admins reassign links in every site; non-admins only within their own site
		int count = contentRepository.reassignTool(currentTool.longValue(), newTool.longValue(), isAdminRole ? null : siteId);
		log.debug("Count={} reassigned content links from tool {} to tool {}", count, currentTool, newTool);
		return Long.valueOf(count);
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
	private Object updateThingDao(String table, String[] formModel, String[] fullModel, Long key, Object newProps, String siteId) {
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
	private Object updateThingDao(String table, String[] formModel, String[] fullModel,
			Long key, Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole)
	{
		if (table == null || formModel == null || key == null || newProps == null) {
			throw new IllegalArgumentException(
					"table, model, key, and newProps must all be non-null");
		}
		if (siteId == null && !isAdminRole ) {
			throw new IllegalArgumentException("siteId must be non-null for non-admins");
		}

		if ( ! (newProps instanceof Properties || newProps instanceof Map)	) {
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
			LtiTool tool = toolRepository.findById(key.longValue()).orElse(null);
			if (tool == null) {
				return false;
			}
			// Non-admins can only update tools in their own site
			if (!isAdminRole && !siteId.equals(tool.getSiteId())) {
				return false;
			}
			applyLtiToolUpdates(tool, newMapping);
			tool.setUpdatedAt(Instant.now());
			toolRepository.save(tool);
			return true;
		}

		// Content items are now updated through the JPA repository, applying only the supplied fields
		if ("lti_content".equals(table)) {
			LtiContent content = contentRepository.findById(key.longValue()).orElse(null);
			if (content == null) {
				return false;
			}
			// Non-admins can only update content in their own site
			if (!isAdminRole && !siteId.equals(content.getSiteId())) {
				return false;
			}
			applyLtiContentUpdates(content, newMapping);
			content.setUpdatedAt(Instant.now());
			contentRepository.save(content);
			return true;
		}

		// Tool/site deployments are now updated through the JPA repository, applying only the supplied fields
		if ("lti_tool_site".equals(table)) {
			LtiToolSite toolSite = toolSiteRepository.findById(key.longValue()).orElse(null);
			if (toolSite == null) {
				return false;
			}
			// Non-admins can only update deployments in their own site
			if (!isAdminRole && !siteId.equals(toolSite.getSiteId())) {
				return false;
			}
			applyLtiToolSiteUpdates(toolSite, newMapping);
			toolSite.setUpdatedAt(Instant.now());
			toolSiteRepository.save(toolSite);
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
		if (m.containsKey(LTIService.LTI_DEPLOYMENT_ID)) tool.setDeploymentId((String) m.get(LTIService.LTI_DEPLOYMENT_ID));
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
			content.setTool(toolId == null ? null : toolRepository.findById(toolId).orElse(null));
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
			toolSite.setTool(toolId == null ? null : toolRepository.findById(toolId).orElse(null));
		}
		if (m.containsKey(LTI_SITE_ID)) toolSite.setSiteId((String) m.get(LTI_SITE_ID));
		if (m.containsKey("notes")) toolSite.setNotes((String) m.get("notes"));
		if (m.containsKey(LTIService.LTI_DEPLOYMENT_GROUP)) toolSite.setDeploymentGroup((String) m.get(LTIService.LTI_DEPLOYMENT_GROUP));
	}

	/*-- Straight-up API methods ------------------------*/

	private Object insertToolSiteDao(Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole) {
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
			ltiToolSite.setTool(toolRepository.findById(toolId).orElse(null));
		}
		ltiToolSite.setNotes((String) newMapping.get("notes"));
		ltiToolSite.setDeploymentGroup((String) newMapping.get(LTIService.LTI_DEPLOYMENT_GROUP));

		return toolSiteRepository.save(ltiToolSite).getId();
	}
}
