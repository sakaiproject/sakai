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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lti.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import java.lang.StringBuffer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import org.json.simple.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lti.api.LTIExportService.ExportType;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.api.LTISubstitutionsFilter;
import org.sakaiproject.lti.beans.LtiContentBean;
import org.sakaiproject.lti.beans.LtiToolBean;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.foorm.SakaiFoorm;
import org.sakaiproject.lti.util.SakaiLTIUtil;
import org.sakaiproject.util.foorm.Foorm;
import org.tsugi.lti.LTIUtil;
import org.sakaiproject.util.MergeConfig;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
/**
 * <p>
 * Implements the LTIService, all but a Storage model.
 * </p>
 */
@Slf4j
public abstract class BaseLTIService implements LTIService {

	/** Resource bundle using current language locale */
	protected static ResourceLoader rb = new ResourceLoader("ltiservice");
	/**
	 * 
	 */
	protected static SakaiFoorm foorm = new SakaiFoorm();

	/** Dependency: SessionManager */
	protected SessionManager m_sessionManager = null;

	// The filters that are applied to custom properties.
	protected List<LTISubstitutionsFilter> filters = new CopyOnWriteArrayList<>();

	/**
	 * Dependency: SessionManager.
	 * 
	 * @param service
	 *          The SessionManager.
	 */
	public void setSessionManager(SessionManager service) {
		m_sessionManager = service;
	}

	/** Dependency: UsageSessionService */
	protected UsageSessionService m_usageSessionService = null;

	/**
	 * Dependency: UsageSessionService.
	 * 
	 * @param service
	 *          The UsageSessionService.
	 */
	public void setUsageSessionService(UsageSessionService service) {
		m_usageSessionService = service;
	}

	/** Dependency: UserDirectoryService */
	protected UserDirectoryService m_userDirectoryService = null;

	/**
	 * Dependency: UserDirectoryService.
	 * 
	 * @param service
	 *          The UserDirectoryService.
	 */
	public void setUserDirectoryService(UserDirectoryService service) {
		m_userDirectoryService = service;
	}

	/** Dependency: EventTrackingService */
	protected EventTrackingService m_eventTrackingService = null;

	/**
	 * Dependency: EventTrackingService.
	 * 
	 * @param service
	 *          The EventTrackingService.
	 */
	public void setEventTrackingService(EventTrackingService service) {
		m_eventTrackingService = service;
	}

	@Setter protected SecurityService securityService = null;

	@Setter protected SiteService siteService = null;

	@Setter protected ServerConfigurationService serverConfigurationService;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init() {
		try {
			log.info("init()");

		} catch (Exception t) {
			log.warn("init(): ", t);
		}

		// Check to see if all out properties are defined
		ArrayList<String> strings = foorm.checkI18NStrings(LTIService.TOOL_MODEL, rb);
		for (String str : strings) {
			log.warn("{}=Missing LTIService Translation", str);
		}

		strings = foorm.checkI18NStrings(LTIService.CONTENT_MODEL, rb);
		for (String str : strings) {
			log.warn("{}=Missing LTIService Translation", str);
		}

	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy() {
		log.info("destroy()");
	}

	protected String[] getContentModelDaoIfConfigurable(Map<String, Object> tool, boolean isAdminRole) {
		if (tool == null) {
			return null;
		}

		boolean phase1 = foorm.formHasConfiguration(tool, CONTENT_MODEL, null, null);
		String[] retval = foorm.filterForm(tool, CONTENT_MODEL);
		if (!isAdminRole) {
			boolean phase2 = foorm.formHasConfiguration(null, retval, null, ".*:role=admin.*");
			if (!phase1 && !phase2) {
				return null;
			}

			retval = foorm.filterForm(null, retval, null, ".*:role=admin.*");
		}

		return retval;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * LTIService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/* Tool Model */
	@Override
	public String[] getToolModel(String siteId) {
		return getToolModelDao(isAdmin(siteId));
	}

	public String[] getToolModelDao() {
		return getToolModelDao(true);
	}

	public String[] getToolModelDao(boolean isAdminRole) {
		if (isAdminRole) return TOOL_MODEL;
		return foorm.filterForm(null, TOOL_MODEL, null, ".*:role=admin.*");
	}

	@Override
	public String[] getToolSiteModel(String siteId) {
		return getToolSiteModelDao(isAdmin(siteId));
	}

	public String[] getToolSiteModelDao(boolean isAdminRole) {
		if (isAdminRole) {
			return TOOL_SITE_MODEL;
		}
		return null;
	}

	/* Content Model */
	public String[] getContentModel(Long tool_id, String siteId) {
		Map<String, Object> tool = getToolDao(tool_id, siteId, isAdmin(siteId));
		return getContentModelDao(tool, isAdmin(siteId));
	}

	@Override
	public String[] getContentModelIfConfigurable(Long tool_id, String siteId) {
		Map<String, Object> tool = getToolDao(tool_id, siteId, isAdmin(siteId));
		return getContentModelDaoIfConfigurable(tool, isAdmin(siteId));
	}

	@Override
	public String[] getContentModel(Map<String, Object> tool, String siteId) {
		return getContentModelDao(tool, isAdmin(siteId));
	}

	// Note that there is no
	//   public String[] getContentModelDao(Long tool_id, String siteId) 
	// on purpose - if code is doing Dao style it can retrieve its own tool

	protected String[] getContentModelDao(Map<String, Object> tool, boolean isAdminRole) {
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
			throw new java.lang.RuntimeException("isAdmin() requires non-null siteId");
		}
		if (!ADMIN_SITE.equals(siteId) ) return false;
		return isMaintain(siteId);
	}

	@Override
	public boolean isMaintain(String siteId) {
		return siteService.allowUpdateSite(siteId);
	}

	/**
	 * Simple API signature for the update series of methods
	 */
	@Override
	public Object updateTool(Long key, Map<String, Object> newProps, String siteId) {
		return updateTool(key, (Object) newProps, siteId);
	}

	/**
	 * Simple API signature for the update series of methods
	 */
	@Override
	public Object updateTool(Long key, Properties newProps, String siteId) {
		return updateTool(key, (Object) newProps, siteId);
	}

	@Override
	public Object updateToolDao(Long key, Map<String, Object> newProps, String siteId) {
		return updateToolDao(key, newProps, siteId, true, true);
	}


	private Object updateTool(Long key, Object newProps, String siteId) {
		return updateToolDao(key, newProps, siteId, isAdmin(siteId), isMaintain(siteId));
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
	public Object updateContentDao(Long key, Map<String, Object> newProps)
	{
		// siteId can be null if isAdmin is false, the item is just patched in place
		String siteId = null;
		boolean isAdmin = true;
		boolean isMaintain = true;
		return updateContentDao(key, newProps, siteId, isAdmin, isMaintain);
	}

	@Override
	public Object updateContentDao(Long key, Map<String, Object> newProps, String siteId)
	{
		return updateContentDao(key, (Object) newProps, siteId, true, true);
	}

	@Override
	public boolean deleteContent(Long key, String siteId) {
		return deleteContentDao(key, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	public static int getInt(Object o) {
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
	public void filterContent(LtiContentBean content, LtiToolBean tool) {
		if (content == null) {
			return;
		}
		Map<String, Object> contentMap = content.asMap();
		Map<String, Object> toolMap = (tool != null) ? tool.asMap() : null;
		filterContent(contentMap, toolMap);
		content.applyFromMap(contentMap);
	}

	public static Integer getCorrectProperty(String propName, Map<String, Object> content,
			Map<String, Object> tool) {
		int toolProp = getInt(tool.get(propName));
		int contentProp = getInt(content.get(propName));
		if (toolProp == -1 || contentProp == -1)
			return null;

		int allowProp = getInt(tool.get("allow" + propName));
		int allowCode = -1;
		if (allowProp >= 0) {
			allowCode = allowProp;
		} else if (toolProp >= 0) {
			allowCode = toolProp;
		}

		// There is no control row assertion
		if (allowCode == -1)
			return null;

		// If the control property wants to override
		if (allowCode == 0 && toolProp != 0)
			return new Integer(0);
		if (allowCode == 1 && toolProp != 1)
			return new Integer(1);
		return null;
	}

	protected abstract Object insertMembershipsJobDao(String siteId, String membershipsId, String membershipsUrl, String consumerKey, String ltiVersion);

	public Object insertMembershipsJob(String siteId, String membershipsId, String membershipsUrl, String consumerKey, String ltiVersion) {
		return insertMembershipsJobDao(siteId, membershipsId, membershipsUrl, consumerKey, ltiVersion);
	}

	protected abstract Map<String, Object> getMembershipsJobDao(String siteId);

	public Map<String, Object> getMembershipsJob(String siteId) {
		return getMembershipsJobDao(siteId);
	}

	protected abstract List<Map<String, Object>> getMembershipsJobsDao();

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
		return null;
	}

	@Override
	public Object insertTool(Properties newProps, String siteId) {
		return insertToolDao(newProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	@Override
	public Object insertTool(Map<String, Object> newProps, String siteId) {
		return insertToolDao(newProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	@Override
	public Object insertToolDao(Properties newProps, String siteId) {
		return insertToolDao(newProps, siteId, true, true);
	}

	@Override
	public boolean deleteTool(Long key, String siteId) {
		return deleteToolDao(key, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	/** Delete a tool and delete the content items and site links assocated with the tool
	 *
	 * This is called by a maintain user in a regualr site and deletes a tool, its content
	 * item, and pages with it on the page.
	 *
	 * For the admin user in the !admin site - it deletes a tool and then removes the
	 * placements + pages from all the sites that have the tool - might take a second or two.
	 *
	 * @return A list of strings that are error messages
	 */
	@Override
	public List<String> deleteToolAndDependencies(Long key, String siteId) {

		List<String> retval = new ArrayList<String> ();
		String errstr;

		List<Map<String,Object>> contents;
		if ( isAdmin(siteId) ) {
			contents = this.getContentsDao("lti_content.tool_id = "+key,null,0,5000, null, isAdmin(siteId));
		} else {
			contents = this.getContents("lti_content.tool_id = "+key, null,0,5000, siteId);
		}

		for ( Map<String,Object> content : contents ) {
			// the content with same tool id remove the content link first
			Long content_key = LTIUtil.toLongNull(content.get(LTIService.LTI_ID));
			if ( content_key == null ) continue;

			// Check the tool_id - just double checking in case the WHER clause fails
			Long tool_id = LTIUtil.toLongNull(content.get(LTIService.LTI_TOOL_ID));
			if ( ! key.equals(tool_id) ) continue;

			// Admin edits all sites with the content item
			String contentSite = siteId;
			if ( isAdmin(siteId) ) {
				contentSite = content.get(LTIService.LTI_SITE_ID).toString();
			}

			// Is there is a tool placement in the left Nav (i.e. not Lessons)
			// remove the tool content link page from the site
			String pstr = (String) content.get(LTIService.LTI_PLACEMENT);
			if ( pstr != null && pstr.length() > 1 ) {
				errstr = this.deleteContentLink(content_key, contentSite);
				if ( errstr != null ) {
					log.debug(errstr);
					retval.add(errstr);
				}
			}

			// remove the content item that depends on the tool
			if ( ! this.deleteContent(content_key, contentSite) ) {
				errstr = "Unable to delete content itemkey="+key+" site="+siteId;
				log.debug(errstr);
				retval.add(errstr);
			}
		}

		int countDelete = deleteToolSitesForToolIdDao(String.valueOf(key));
		log.debug("Delete toolSites, toolId={}, countDelete={}", key, countDelete);

		// We are going to delete the tool even if there were problems along the way
		// Since that is the one thing we are supposed to do in this method
		if ( ! this.deleteTool(key, siteId) ) {
			errstr = "Unable to delete tool key="+key+" site="+siteId;
			log.debug(errstr);
			retval.add(errstr);
		}
		return retval;
	}

	@Override
	public Map<String, Object> getTool(Long key, String siteId) {
		return getToolDao(key, siteId, isAdmin(siteId));
	}


	@Override
	public Map<String, Object> getToolDao(Long key, String siteId)
	{
		return getToolDao(key, siteId, true);
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

	@Override
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
		return insertContentDao(newProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	@Override
	public Object insertContent(Map<String, Object> newMap, String siteId) {
		return insertContent(convertToProperties(newMap), siteId);
	}

	@Override
	public Object insertContentDao(Properties newProps, String siteId) {
		if ( newProps.getProperty(LTIService.LTI_PLACEMENTSECRET) == null ) {
			newProps.setProperty(LTIService.LTI_PLACEMENTSECRET, UUID.randomUUID().toString());
		}
		return insertContentDao(newProps, siteId, true, true);
	}

	@Override
	public Map<String, Object> getContent(Long key, String siteId) {
		return getContentDao(key, siteId, isAdmin(siteId));
	}


	// This is with absolutely no site checking...
	@Override
	public Map<String, Object> getContentDao(Long key) {
		return getContentDao(key, null, true);
	}

	@Override
	public Map<String, Object> getContentDao(Long key, String siteId) {
		return getContentDao(key, siteId, true);
	}

	@Override
	public List<Map<String, Object>> getContents(String search, String order, int first, int last, String siteId)
	{
		return getContentsDao(search, order, first, last, siteId, isAdmin(siteId));
	}


	@Override
	public List<Map<String, Object>> getContentsDao(String search, String order, int first, int last, String siteId) {
		return getContentsDao(search, order, first, last, siteId, true);
	}

	@Override
	public int countContents(final String search, String siteId) {
		return countContentsDao(search, siteId, isAdmin(siteId));
	}

	@Override
	public Object insertToolContent(String id, String toolId, Properties reqProps, String siteId)
	{
		return insertToolContentDao(id, toolId, reqProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	protected Object insertToolContentDao(String id, String toolId, Properties reqProps, String siteId, boolean isAdminRole, boolean isMaintainRole)
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
		Map<String,Object> tool = getToolDao(toolKey, siteId, isAdminRole);
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
		if ( contentSite != null && visible != null && visible.equals(LTIService.LTI_VISIBLE_STEALTH) ) {
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
			retval = insertContentDao(reqProps, siteId, isAdminRole, isMaintainRole);
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
					updateToolDao(toolKey, toolProps, siteId, isAdminRole, isMaintainRole);
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

	protected Object insertToolSiteLinkDao(String id, String button_text, String siteId, boolean isAdminRole, boolean isMaintainRole)
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
		Map<String,Object> content = getContentDao(key, siteId, isAdminRole);
		if ( content == null ) {
			retval = new String("1" + rb.getString("error.content.not.found"));
			return retval;
		}

		Map<String, Object> ltiTool = null;
		Long toolId = LTIUtil.toLongNull(content.get(LTI_TOOL_ID));

		if (toolId != null) {
			ltiTool = getToolDao(toolId, siteId);
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

	public Object transferToolContentLinksDao(Long currentTool, Long newTool)
	{
		boolean isAdminRole = true;
		String siteId = null;
		return transferToolContentLinksDao(currentTool, newTool, siteId, isAdminRole);
	}

	protected abstract Object transferToolContentLinksDao(Long currentTool, Long newTool, String siteId, boolean isAdminRole);

	@Override
	public String deleteContentLink(Long key, String siteId)
	{
		return deleteContentLinkDao(key, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	protected String deleteContentLinkDao(Long key, String siteId, boolean isAdminRole, boolean isMaintainRole)
	{
		if ( ! isMaintainRole ) {
			return rb.getString("error.maintain.link");
		}
		if ( key == null ) {
			return rb.getString("error.id.not.found");
		}

		Map<String,Object> content = getContentDao(key, siteId, isAdminRole);
		if (  content == null ) {
			return rb.getString("error.content.not.found");
		}

		String pstr = (String) content.get(LTIService.LTI_PLACEMENT);
		if ( pstr == null || pstr.length() < 1 ) {
			return rb.getString("error.placement.not.found");
		}

		ToolConfiguration tool = siteService.findTool(pstr);
		if ( tool == null ) {
			return rb.getString("error.placement.not.found");
		}

		String siteStr = (String) content.get(LTI_SITE_ID);
		// only admin can remove content from other site
		if ( ! siteId.equals(siteStr) && !isAdminRole ) {
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
	public void filterCustomSubstitutions(Properties properties, LtiToolBean tool, Site site) {
		filterCustomSubstitutions(properties, tool != null ? tool.asMap() : null, site);
	}

	@Override
	public List<Map<String, Object>> getToolSitesByToolId(String toolId, String siteId) {
		String search = " lti_tool_site.tool_id = " + toolId;
		return getToolSitesDao(search, null, 0, 0, siteId, isAdmin(siteId));
	}

	@Override
	public Map<String, Object> getToolSiteById(Long key, String siteId) {
		return getToolSiteDao(key, siteId);
	}

	@Override
	public Object insertToolSite(Properties properties, String siteId) {
		return insertToolSiteDao(properties, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	@Override
	public Object updateToolSite(Long key, Properties newProps, String siteId) {
		return updateToolSiteDao(key, newProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	@Override
	public boolean deleteToolSite(Long key, String siteId) {
		return deleteToolSiteDao(key, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	@Override
	public boolean toolDeployed(Long toolKey, String siteId) {
		return getToolSitesByToolId(String.valueOf(toolKey), siteId)
				.stream()
				.anyMatch(toolSite -> siteId.equals(toolSite.get(LTIService.LTI_SITE_ID)));
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

		log.debug("LTI Import launchUrl {}", launchUrl);
		String toolCheckSum = (String) tool.get(LTIService.SAKAI_TOOL_CHECKSUM);
		List<Map<String, Object>> tools = this.getTools(null, null, 0, 0, siteId);
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

			theContent = this.getContent((Long) result, siteId);
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
		Map<String, Object> ltiContent = this.getContentDao(contentKey, oldSiteId, true);
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
		Map<String, Object> ltiTool = this.getToolDao(ltiToolId, siteId, true);
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
	protected String fixLtiLaunchUrls(String text, String fromContext, String toContext, MergeConfig mcx, Map<String, String> transversalMap) {
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

	/**
	 * Helper method to find or create a tool for a content item
	 * @param content Content item which we are about to insert, at minimum need LTI_LAUNCH and LTI_TITLE
	 * @param tool Tool may be null, may or may not be persisted - if this exists, we will reload to verify it is accessible to the user and site
	 * @param toSiteId Target site ID
	 * @param fromSiteId Source site ID
	 * @param mcx The MergeConfig for this import
	 * @return New tool ID or null if tool cannot be found/created
	 */
	protected Long findOrCreateToolForContentItem(Map<String, Object> content, Map<String, Object> tool, String toSiteId, String fromSiteId, MergeConfig mcx) {
		if ( StringUtils.isBlank(toSiteId) ) return null;

		// Get launch URL from content
		String launchUrl = (String) content.get(LTIService.LTI_LAUNCH);
		Long contentKey = this.getId(content);  // May be empty null or not yet persisted or be an id from some other system
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



}
