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

import java.util.UUID;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.foorm.SakaiFoorm;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

/**
 * <p>
 * Implements the LTIService, all but a Storage model.
 * </p>
 */
public abstract class BaseLTIService implements LTIService {
	/** Our log (commons). */
	private static Logger M_log = LoggerFactory.getLogger(BaseLTIService.class);

	/** Constants */
	private final String ADMIN_SITE = "!admin";
	public final String LAUNCH_PREFIX = "/access/basiclti/site/";

	/** Resource bundle using current language locale */
	protected static ResourceLoader rb = new ResourceLoader("ltiservice");
	/**
	 * 
	 */
	protected static SakaiFoorm foorm = new SakaiFoorm();

	/** Dependency: SessionManager */
	protected SessionManager m_sessionManager = null;

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

	/**
	 * 
	 */
	protected SecurityService securityService = null;
	/**
	 * 
	 */
	protected SiteService siteService = null;
	/**
	 * 
	 */
	protected ToolManager toolManager = null;

	/**
	 * Pull in any necessary services using factory pattern
	 */
	protected void getServices() {
		if (securityService == null)
			securityService = (SecurityService) ComponentManager
				.get("org.sakaiproject.authz.api.SecurityService");
		if (siteService == null)
			siteService = (SiteService) ComponentManager
				.get("org.sakaiproject.site.api.SiteService");
		if (toolManager == null)
			toolManager = (ToolManager) ComponentManager
				.get("org.sakaiproject.tool.api.ToolManager");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init() {
		try {
			M_log.info("init()");

		} catch (Exception t) {
			M_log.warn("init(): ", t);
		}

		getServices();

		// Check to see if all out properties are defined
		ArrayList<String> strings = foorm.checkI18NStrings(LTIService.TOOL_MODEL, rb);
		for (String str : strings) {
			M_log.warn(str + "=Missing LTIService Translation");
		}

		strings = foorm.checkI18NStrings(LTIService.CONTENT_MODEL, rb);
		for (String str : strings) {
			M_log.warn(str + "=Missing LTIService Translation");
		}

		strings = foorm.checkI18NStrings(LTIService.DEPLOY_MODEL, rb);
		for (String str : strings) {
			M_log.warn(str + "=Missing LTIService Translation");
		}
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy() {
		M_log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * LTIService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/* Tool Model */
	public String[] getToolModel() {
		return getToolModelDao(isAdmin());
	}

	public String[] getToolModelDao() {
		return getToolModelDao(true);
	}

	public String[] getToolModelDao(boolean isAdminRole) {
		if (isAdminRole) return TOOL_MODEL;
		return foorm.filterForm(null, TOOL_MODEL, null, ".*:role=admin.*");
	}


	/* Content Model */
	public String[] getContentModel(Long tool_id) {
		Map<String, Object> tool = getToolDao(tool_id, getContext(), isAdmin());
		return getContentModelDao(tool, isAdmin());
	}

	public String[] getContentModel(Map<String, Object> tool) {
		return getContentModelDao(tool, isAdmin());
	}

	// Note that there is no
	//   public String[] getContentModelDao(Long tool_id, String siteId) 
	// on purpose - if code is doing Dao style it can retrieve its own tool

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#getContentModelDao(Map<String, Object>, java.lang.String, boolean)
	 */
	protected String[] getContentModelDao(Map<String, Object> tool, boolean isAdminRole) {
		if ( tool == null ) return null;
		String[] retval = foorm.filterForm(tool, CONTENT_MODEL);
		if (!isAdminRole) retval = foorm.filterForm(null, retval, null, ".*:role=admin.*");
		return retval;
	}

	/**
	 * 
	 * @return
	 */
	protected String getContext() {
		String retval = toolManager.getCurrentPlacement().getContext();
		// Don't continue - this will be a security hole in isAdmin()
		if ( retval == null ) {
			throw new java.lang.RuntimeException("getContext() required non-null context");
		}
		return retval;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#getContentLaunch(java.util.Map)
	 */
	public String getContentLaunch(Map<String, Object> content) {
		if ( content == null ) return null;
		int key = getInt(content.get(LTIService.LTI_ID));
		String siteId = (String) content.get(LTIService.LTI_SITE_ID);
		if (key < 0 || siteId == null)
			return null;
		return LAUNCH_PREFIX + siteId + "/content:" + key;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#getToolLaunch(java.util.Map)
	 */
	public String getToolLaunch(Map<String, Object> tool, String siteId) {
		if ( tool == null ) return null;
		int key = getInt(tool.get(LTIService.LTI_ID));
		if (key < 0 || siteId == null)
			return null;
		return LAUNCH_PREFIX + siteId + "/tool:" + key;
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#formOutput(java.lang.Object,
	 *      java.lang.String)
	 */
	public String formOutput(Object row, String fieldInfo) {
		return foorm.formOutput(row, fieldInfo, rb);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#formOutput(java.lang.Object,
	 *      java.lang.String[])
	 */
	public String formOutput(Object row, String[] fieldInfo) {
		return foorm.formOutput(row, fieldInfo, rb);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#formInput(java.lang.Object,
	 *      java.lang.String)
	 */
	public String formInput(Object row, String fieldInfo) {
		return foorm.formInput(row, fieldInfo, rb);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#formInput(java.lang.Object,
	 *      java.lang.String[])
	 */
	public String formInput(Object row, String[] fieldInfo) {
		return foorm.formInput(row, fieldInfo, rb);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#isAdmin()
	 */
	public boolean isAdmin() {
		return isAdmin(getContext());
	}

	protected boolean isAdmin(String siteId) {
		if ( siteId == null ) {
			throw new java.lang.RuntimeException("isAdmin() requires non-null siteId");
		}
		if (!ADMIN_SITE.equals(siteId) ) return false;
		return isMaintain(siteId);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#isMaintain()
	 */
	public boolean isMaintain() {
		return isMaintain(getContext());
	}

	protected boolean isMaintain(String siteId) {
		return siteService.allowUpdateSite(siteId);
	}

	/**
	 * Simple API signature for the update series of methods
	 */
	public Object updateTool(Long key, Map<String, Object> newProps) {
		return updateTool(key, (Object) newProps);
	}

	/**
	 * Simple API signature for the update series of methods
	 */
	public Object updateTool(Long key, Properties newProps) {
		return updateTool(key, (Object) newProps);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.impl.BaseLTIService#updateTool(java.lang.Long,
	 *	  java.lang.Object)
	 */
	protected Object updateTool(Long key, Object newProps) {
		return updateToolDao(key, newProps, getContext(), isAdmin(), isMaintain());
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.impl.BaseLTIService#updateToolDao(java.lang.Long,
	 *	  java.lang.Object, java.lang.String)
	 */
	public Object updateToolDao(Long key, Map<String, Object> newProps, String siteId) {
		return updateToolDao(key, (Object) newProps, siteId, true, true);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#updateContent(java.lang.Long, java.util.Map)
	 */
	public Object updateContent(Long key, Map<String, Object> newProps) {
		return updateContent(key, (Object) newProps);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#updateContent(java.lang.Long,
	 *      java.util.Properties)
	 */
	public Object updateContent(Long key, Properties newProps) {
		return updateContent(key, (Object) newProps);
	}
	
	/**
	 * 
	 * @param key
	 * @param newProps
	 * @param siteId
	 * @return
	 */
	public Object updateContent(Long key, Properties newProps, String siteId)
	{
		return updateContentDao(key, newProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	/**
	 * 
	 * @param key
	 * @param newProps
	 * @return
	 */
	public Object updateContent(Long key, Object newProps)
	{
		return updateContentDao(key, newProps, getContext(), isAdmin(), isMaintain());
	}

	/**
	 * 
	 * @param key
	 * @param newProps
	 * @param siteId
	 * @return
	 */
	public Object updateContentDao(Long key, Map<String, Object> newProps, String siteId)
	{
		return updateContentDao(key, (Object) newProps, siteId, true, true);
	}

	public boolean deleteContent(Long key) {
		return deleteContentDao(key, getContext(), isAdmin(), isMaintain());
	}

	/**
	 * 
	 * @param o
	 * @return
	 */
	public static int getInt(Object o) {
		if (o instanceof String) {
			try {
				return (new Integer((String) o)).intValue();
			} catch (Exception e) {
				return -1;
			}
		}
		if (o instanceof Number)
			return ((Number) o).intValue();
		return -1;
	}

	// Adjust the content object based on the settings in the tool object
	/**
	 * 
	 */
	public void filterContent(Map<String, Object> content, Map<String, Object> tool) {
		if (content == null || tool == null)
			return;
		int heightOverride = getInt(tool.get(LTIService.LTI_ALLOWFRAMEHEIGHT));
		int toolHeight = getInt(tool.get(LTIService.LTI_FRAMEHEIGHT));
		int contentHeight = getInt(content.get(LTIService.LTI_FRAMEHEIGHT));
		int frameHeight = 1200;
		if (toolHeight > 0)
			frameHeight = toolHeight;
		if (heightOverride == 1 && contentHeight > 0)
			frameHeight = contentHeight;
		content.put(LTIService.LTI_FRAMEHEIGHT, new Integer(frameHeight));

		int debug = getInt(tool.get(LTIService.LTI_DEBUG));
		if ( debug == 2 ) debug = getInt(content.get(LTIService.LTI_DEBUG));
		content.put(LTIService.LTI_DEBUG, debug+"");

		int newpage = getInt(tool.get(LTIService.LTI_NEWPAGE));
		if ( newpage == 2 ) newpage = getInt(content.get(LTIService.LTI_NEWPAGE));
		content.put(LTIService.LTI_NEWPAGE, newpage+"");
	}

	/**
	 * 
	 * @param propName
	 * @param content
	 * @param tool
	 * @return
	 */
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

	public Object insertTool(Properties newProps) {
		return insertToolDao(newProps, getContext(), isAdmin(), isMaintain());
	}

	public Object insertTool(Map<String,Object> newProps) {
		return insertToolDao(newProps, getContext(), isAdmin(), isMaintain());
	}

	public Object insertToolDao(Properties newProps, String siteId) {
		return insertToolDao(newProps, siteId, true, true);
	}

	public boolean deleteTool(Long key) {
		return deleteToolDao(key, getContext(), isAdmin(), isMaintain());
	}

	public boolean deleteToolDao(Long key, String siteId) {
		return deleteToolDao(key, siteId, true, true);
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.lti.api.LTIService#getTool(java.lang.String)
	 */
	public Map<String, Object> getTool(String url) {
		throw new java.lang.RuntimeException("getTool(String url) not implemented");
	}

	/**
	 * 
	 * @param urlorkey
	 * @param retval
	 * @return
	 */
	private boolean getTool(Object urlorkey, Map<String, Object> retval) {
		throw new java.lang.RuntimeException("getTool(String url) not implemented");
	}

	public Map<String, Object> getTool(Long key, String siteId) {
		boolean isAdmin = false;
		if (siteId != null) {
			isAdmin = isAdmin(siteId);
		} else {
			isAdmin = isAdmin();
		}
		return getToolDao(key, getContext(), isAdmin);
	}

	public Map<String, Object> getToolDao(Long key, String siteId)
	{
		return getToolDao(key, siteId, true);
	}

	public List<Map<String, Object>> getTools(String search, String order, int first, int last) {
		return getToolsDao(search, order, first, last, getContext(), isAdmin());
	}

        public List<Map<String, Object>> getToolsLaunch() {
		return getTools( "lti_tools."+LTIService.LTI_PL_LAUNCH+" = 1 OR ( " +
			"( lti_tools."+LTIService.LTI_PL_LINKSELECTION+" IS NULL OR lti_tools."+LTIService.LTI_PL_LINKSELECTION+" = 0 ) and " + 
			"( lti_tools."+LTIService.LTI_PL_FILEITEM+" IS NULL OR lti_tools."+LTIService.LTI_PL_FILEITEM+" = 0 ) and " + 
			"( lti_tools."+LTIService.LTI_PL_CONTENTEDITOR+" IS NULL OR lti_tools."+LTIService.LTI_PL_CONTENTEDITOR+" = 0 ) and " + 
			"( lti_tools."+LTIService.LTI_PL_ASSESSMENTSELECTION+" IS NULL OR lti_tools."+LTIService.LTI_PL_ASSESSMENTSELECTION+" = 0 ) " +
			" ) ", null, 0, 0);
	}

        public List<Map<String, Object>> getToolsLtiLink() {
		return getTools("lti_tools."+LTIService.LTI_PL_LINKSELECTION+" = 1",null,0,0);
	}

        public List<Map<String, Object>> getToolsFileItem() {
		return getTools("lti_tools."+LTIService.LTI_PL_FILEITEM+" = 1",null,0,0);
	}

        public List<Map<String, Object>> getToolsContentEditor() {
		return getTools("lti_tools."+LTIService.LTI_PL_CONTENTEDITOR+" = 1",null,0,0);
	}

        public List<Map<String, Object>> getToolsAssessmentSelection() {
		return getTools("lti_tools."+LTIService.LTI_PL_ASSESSMENTSELECTION+" = 1",null,0,0);
	}

	public List<Map<String, Object>> getToolsDao(String search, String order, int first, int last, String siteId) {
		return getToolsDao(search, order, first, last, siteId, true);
	}

	public Object insertContent(Properties newProps, String siteId) {
		boolean isAdmin = false;
		if (siteId != null) {
			isAdmin = isAdmin(siteId);
		} else {
			isAdmin = isAdmin();
		}
		return insertContentDao(newProps, getContext(), isAdmin, isMaintain());
	}

	public Object insertContentDao(Properties newProps, String siteId)
	{
		return insertContentDao(newProps, siteId, true, true);
	}

	public Map<String, Object> getContent(Long key) {
		return getContentDao(key, getContext(), isAdmin());
	}

	public Map<String, Object> getContent(Long key, String siteId) {
		return getContentDao(key, siteId, isAdmin());
	}
	
	// This is with absolutely no site checking...
	public Map<String, Object> getContentDao(Long key) {
		return getContentDao(key, null, true);
	}

	public Map<String, Object> getContentDao(Long key, String siteId) {
		return getContentDao(key, siteId, true);
	}

	public List<Map<String, Object>> getContents(String search, String order, int first, int last) 
	{
		return getContentsDao(search, order, first, last, getContext(), isAdmin());
	}

	public List<Map<String, Object>> getContentsDao(String search, String order, int first, int last, String siteId) {
		return getContentsDao(search, order, first, last, siteId, true);
	}

	public Object insertToolContent(String id, String toolId, Properties reqProps)
	{
		return insertToolContentDao(id, toolId, reqProps, getContext(), isAdmin(), isMaintain());
	}
	
	// TODO: Review the site-manage use cases for this method signature
	public Object insertToolContent(String id, String toolId, Properties reqProps, String siteId)
	{
		if ( isAdmin() || siteId.equals(getContext()) ) {
			// Is OK
		} else {
			M_log.warn("insertToolContent siteId="+siteId+" context="+getContext()+" isAdmin="+isAdmin());
		}
		return insertToolContentDao(id, toolId, reqProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}
	
	protected Object insertToolContentDao(String id, String toolId, Properties reqProps, String siteId, boolean isAdminRole, boolean isMaintainRole)
	{
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
		if ( id == null ) 
		{
			reqProps.setProperty(LTIService.LTI_PLACEMENTSECRET, UUID.randomUUID().toString());
			// insertContentDao checks to make sure that the TOOL_ID in reqProps is suitable
			retval = insertContentDao(reqProps, siteId, isAdminRole, isMaintainRole);
		} else {
			contentKey = new Long(id);
			Long toolKey = new Long(toolId);
			Map<String,Object> tool = getToolDao(toolKey, siteId, isAdminRole);
			if ( tool == null ) {
				retval = rb.getString("error.tool.not.found");
			}
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
			if ( tool.get(LTIService.LTI_PLACEMENTSECRET) == null ) {
				reqProps.setProperty(LTIService.LTI_PLACEMENTSECRET, UUID.randomUUID().toString());
			}
			retval = updateContentDao(contentKey, reqProps, siteId, isAdminRole, isMaintainRole);
		}
		return retval;
	}
	
	public Object insertToolSiteLink(String id, String button_text)
	{
		return insertToolSiteLink(id, button_text, getContext());
	}
	
	public Object insertToolSiteLink(String id, String button_text, String siteId)
	{
		return insertToolSiteLinkDao(id, button_text, siteId, isAdmin(siteId), isMaintain(siteId));
	}
/*
	public Object insertToolSiteLinkDao(String id, String button_text, String siteId)
	{
		return insertToolSiteLinkDao(id, button_text, siteId, true, true);
	}
*/
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
		if (  content == null ) {
			retval = new String("1" + rb.getString("error.content.not.found"));
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
				String fa_icon = (String)content.get(LTI_FA_ICON);
				if ( fa_icon != null && fa_icon.length() > 0 ) {
					tool.getPlacementConfig().setProperty("imsti.fa_icon",fa_icon);
				}
				tool.getPlacementConfig().setProperty("source",(String)content.get("launch_url"));
				tool.setTitle((String) content.get(LTI_TITLE));
				
				sitePage.setTitle(button_text);
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
				M_log.warn(this + " cannot add page and basic lti tool to site " + siteId);
			}
		}
		catch (IdUnusedException e)
		{
			// cannot find site
			retval = new String("0" + rb.getFormattedMessage("error.link.placement.update", new Object[]{id}));
			M_log.warn(this + " cannot find site " + contentSite);
		}
				
		return retval;
	}
	
	public String deleteContentLink(Long key)
	{
		return deleteContentLinkDao(key, getContext(), isAdmin(), isMaintain() );
	}

	public String deleteContentLinkDao(Long key, String siteId)
	{
		return deleteContentLinkDao(key, siteId, true, true);
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
		// only admin can remve content from other site
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
				M_log.warn(this + " LTI content="+key+" placement="+tool.getId()+" could not find page in site=" + siteStr);
			}
	
			// Remove the placement from the content item
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
			M_log.warn(this + " LTI content="+key+" placement="+tool.getId()+" could not remove page from site=" + siteStr);
			return new String(rb.getFormattedMessage("error.link.placement.update", new Object[]{key.toString()}));
		}
	}

	// The methods for deployment objects
	public String[] getDeployModel() {
		return DEPLOY_MODEL;
	}

	public Object insertDeployDao(Properties newProps) {
		return insertDeployDao(newProps, null, true, true);
	}

	public Object updateDeployDao(Long key, Object newProps) {
		return updateDeployDao(key, newProps, null, true, true);
	}

	public boolean deleteDeployDao(Long key) {
		return deleteDeployDao(key, null, true, true);
	}

	public Map<String, Object> getDeployDao(Long key) {
		return getDeployDao(key, null, true);
	}

	public List<Map<String, Object>> getDeploysDao(String search, String order, int first, int last) {
		return getDeploysDao(search, order, first, last, null, true);
	}

	public abstract Object insertProxyBindingDao(Properties newProps);
	public abstract Object updateProxyBindingDao(Long key, Object newProps);
	public abstract boolean deleteProxyBindingDao(Long key);
	public abstract Map<String, Object> getProxyBindingDao(Long key);
	public abstract Map<String, Object> getProxyBindingDao(Long tool_id, String siteId);

}
