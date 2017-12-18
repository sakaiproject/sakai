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
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.lti.api.LTISubstitutionsFilter;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lti.api.LTIExportService.ExportType;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.foorm.SakaiFoorm;

/**
 * <p>
 * Implements the LTIService, all but a Storage model.
 * </p>
 */
@Slf4j
public abstract class BaseLTIService implements LTIService {
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

	/**
	 * 
	 */
	protected ServerConfigurationService serverConfigurationService;

	/**
	 * Pull in any necessary services using factory pattern
	 */
	protected void getServices() {
		if (securityService == null)
			securityService = ComponentManager.get(SecurityService.class);
		if (siteService == null)
			siteService = ComponentManager.get(SiteService.class);
		if (serverConfigurationService == null)
			serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
	}

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

		getServices();

		// Check to see if all out properties are defined
		ArrayList<String> strings = foorm.checkI18NStrings(LTIService.TOOL_MODEL, rb);
		for (String str : strings) {
			log.warn("{}=Missing LTIService Translation", str);
		}

		strings = foorm.checkI18NStrings(LTIService.CONTENT_MODEL, rb);
		for (String str : strings) {
			log.warn("{}=Missing LTIService Translation", str);
		}

		strings = foorm.checkI18NStrings(LTIService.DEPLOY_MODEL, rb);
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


	/* Content Model */
	public String[] getContentModel(Long tool_id, String siteId) {
		Map<String, Object> tool = getToolDao(tool_id, siteId, isAdmin(siteId));
		return getContentModelDao(tool, isAdmin(siteId));
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
		if ( content == null ) return null;
		int key = getInt(content.get(LTIService.LTI_ID));
		String siteId = (String) content.get(LTIService.LTI_SITE_ID);
		if (key < 0 || siteId == null)
			return null;
		return LAUNCH_PREFIX + siteId + "/content:" + key;
	}

	@Override
	public String getToolLaunch(Map<String, Object> tool, String siteId) {
		if ( tool == null ) return null;
		int key = getInt(tool.get(LTIService.LTI_ID));
		if (key < 0 || siteId == null)
			return null;
		return LAUNCH_PREFIX + siteId + "/tool:" + key;
	}

	@Override
	public String getExportUrl(String siteId, String filterId, ExportType exportType) {
		if (siteId == null) {
			return null;
		}
		return "/access/basiclti/site/" + siteId + "/export:" + exportType + ((filterId != null && !"".equals(filterId)) ? (":" + filterId) : "");
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
	public List<String> deleteToolAndContents(Long key, String siteId) {

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
			Long content_key = foorm.getLongNull(content.get(LTIService.LTI_ID));
			if ( content_key == null ) continue;

			// Check the tool_id - just double checking in case the WHER clause fails
			Long tool_id = foorm.getLongNull(content.get(LTIService.LTI_TOOL_ID));
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
		return getToolsDao(search, order, first, last, siteId, isAdmin(siteId));
	}

	@Override
    public List<Map<String, Object>> getToolsLaunch(String siteId) {
		return getTools( "lti_tools."+LTIService.LTI_PL_LAUNCH+" = 1 OR ( " +
			"( lti_tools."+LTIService.LTI_PL_LINKSELECTION+" IS NULL OR lti_tools."+LTIService.LTI_PL_LINKSELECTION+" = 0 ) and " + 
			"( lti_tools."+LTIService.LTI_PL_FILEITEM+" IS NULL OR lti_tools."+LTIService.LTI_PL_FILEITEM+" = 0 ) and " + 
			"( lti_tools."+LTIService.LTI_PL_IMPORTITEM+" IS NULL OR lti_tools."+LTIService.LTI_PL_IMPORTITEM+" = 0 ) and " + 
			"( lti_tools."+LTIService.LTI_PL_CONTENTEDITOR+" IS NULL OR lti_tools."+LTIService.LTI_PL_CONTENTEDITOR+" = 0 ) and " + 
			"( lti_tools."+LTIService.LTI_PL_ASSESSMENTSELECTION+" IS NULL OR lti_tools."+LTIService.LTI_PL_ASSESSMENTSELECTION+" = 0 ) " +
			" ) ", null, 0, 0, siteId);
	}

	@Override
    public List<Map<String, Object>> getToolsLtiLink(String siteId) {
		return getTools("lti_tools."+LTIService.LTI_PL_LINKSELECTION+" = 1",null,0,0, siteId);
	}

	@Override
    public List<Map<String, Object>> getToolsFileItem(String siteId) {
		return getTools("lti_tools."+LTIService.LTI_PL_FILEITEM+" = 1",null,0,0, siteId);
	}

	@Override
    public List<Map<String, Object>> getToolsImportItem(String siteId) {
		return getTools("lti_tools."+LTIService.LTI_PL_IMPORTITEM+" = 1",null,0,0, siteId);
	}

	@Override
    public List<Map<String, Object>> getToolsContentEditor(String siteId) {
		return getTools("lti_tools."+LTIService.LTI_PL_CONTENTEDITOR+" = 1",null,0,0, siteId);
	}

	@Override
    public List<Map<String, Object>> getToolsAssessmentSelection(String siteId) {
		return getTools("lti_tools."+LTIService.LTI_PL_ASSESSMENTSELECTION+" = 1",null,0,0, siteId);
	}

	@Override
	public List<Map<String, Object>> getToolsDao(String search, String order, int first, int last, String siteId) {
		return getToolsDao(search, order, first, last, siteId, true);
	}

	@Override
	public Object insertContent(Properties newProps, String siteId) {
		return insertContentDao(newProps, siteId, isAdmin(siteId), isMaintain(siteId));
	}

	@Override
	public Object insertContentDao(Properties newProps, String siteId) {
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

		// Make sure any missing required bits are inherited from the tool.
		if ( ! reqProps.containsKey(LTIService.LTI_TOOL_ID) ) {
			reqProps.setProperty(LTIService.LTI_TOOL_ID,toolId);
		}

		if ( ! reqProps.containsKey(LTIService.LTI_TITLE) ) {
			reqProps.setProperty(LTIService.LTI_TITLE,(String) tool.get(LTIService.LTI_TITLE));
		}

		if ( ! reqProps.containsKey(LTIService.LTI_PAGETITLE) ) {
			reqProps.setProperty(LTIService.LTI_PAGETITLE,(String) tool.get(LTIService.LTI_PAGETITLE));
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
			if ( tool.get(LTIService.LTI_PLACEMENTSECRET) == null ) {
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
				log.warn("Cannot add page and basic lti tool to site {}", siteId);
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

}
