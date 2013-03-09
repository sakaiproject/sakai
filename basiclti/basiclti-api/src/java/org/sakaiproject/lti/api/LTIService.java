/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
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

package org.sakaiproject.lti.api;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;

/**
 * <p>
 * A LTIService does things for LTI
 * </p>
 * <p>
 * Location is a combination of site id, (optional) page id and (optional) tool id
 * </p>
 */
public interface LTIService {
	/** This string starts the references to resources in this service. */
	static final String REFERENCE_ROOT = "/lti";

	static String WEB_PORTLET = "sakai.web.168";
	
	/**
	 * 
	 * @return
	 */
	public boolean isAdmin();

	/**
	 * 
	 * @return
	 */
	public boolean isMaintain();

	/**
	 * 
	 * @return
	 */
	public String[] getMappingModel();

	/**
	 * 
	 * @param newProps
	 * @return
	 */
	public Object insertMapping(Properties newProps);

	/**
	 * 
	 * @param key
	 * @return
	 */
	public Map<String, Object> getMapping(Long key);

	/**
	 * 
	 * @param key
	 * @return
	 */
	public boolean deleteMapping(Long key);

	/**
	 * 
	 * @param key
	 * @param newProps
	 * @return
	 */
	public Object updateMapping(Long key, Map<String, Object> newProps);

	/**
	 * 
	 * @param key
	 * @param newProps
	 * @return
	 */
	public Object updateMapping(Long key, Properties newProps);

	/**
	 * 
	 * @param search
	 * @param order
	 * @param first
	 * @param last
	 * @return
	 */
	public List<Map<String, Object>> getMappings(String search, String order, int first,
			int last);

	/**
	 * 
	 * @return
	 */
	public String[] getToolModel();

	/**
	 * 
	 * @param newProps
	 * @return
	 */
	public Object insertTool(Properties newProps);

	/**
	 * 
	 * @param newProps
	 * @param siteId
	 * @return
	 */
	public Object insertToolDao(Properties newProps, String siteId);
		
	/**
	 * insert lti tool content
	 * @param id
	 * @param toolId
	 * @param reqProps
	 * @return
	 */
	public Object insertToolContent(String id, String toolId, Properties reqProps);
	
	/**
	 * insert lti tool content
	 * @param id
	 * @param toolId
	 * @param reqProps
	 * @param siteId
	 * @return
	 */
	public Object insertToolContent(String id, String toolId, Properties reqProps, String siteId);

	/**
	 * create an instance of lti tool within site
	 * @param id
	 * @param title
	 * @return
	 */
	public Object insertToolSiteLink(String id, String title);
	
	/**
	 * create an instance of lti tool within site
	 * @param id
	 * @param title
	 * @param siteId
	 * @return
	 */
	public Object insertToolSiteLink(String id, String title, String siteId);
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public Map<String, Object> getTool(Long key);

	/**
	 * 
	 * @param key
	 * @param siteId
	 * @return
	 */
	public Map<String, Object> getToolDao(Long key, String siteId);

	/**
	 * 
	 * @param url
	 * @return
	 */
	public Map<String, Object> getTool(String url);

	/**
	 * 
	 * @param key
	 * @return
	 */
	public boolean deleteTool(Long key);

	/**
	 * 
	 * @param key
	 * @param newProps
	 * @return
	 */
	public Object updateTool(Long key, Properties newProps);

	/**
	 * 
	 * @param key
	 * @param newProps
	 * @return
	 */
	public Object updateTool(Long key, Map<String, Object> newProps);

	/**
	 * 
	 * @param search
	 * @param order
	 * @param first
	 * @param last
	 * @return
	 */
	public List<Map<String, Object>> getTools(String search, String order, int first, int last);

	/**
	 * 
	 * @param search
	 * @param order
	 * @param first
	 * @param last
	 * @param siteId
	 * @return
	 */
	public List<Map<String, Object>> getToolsDao(String search, String order, int first, int last, String siteId);

	/**
	 * 
	 * @param url
	 * @return
	 */
	public String checkMapping(String url);

	/**
	 * 
	 * @param tool_id
	 * @return
	 */
	public String[] getContentModel(Long tool_id);

	/**
	 * 
	 * @param tool
	 * @return
	 */
	public String[] getContentModel(Map<String,Object> tool);

	/**
	 * 
	 * @param newProps
	 * @return
	 */
	public Object insertContent(Properties newProps);

	/**
	 * 
	 * @param newProps
	 * @param siteId
	 * @return
	 */
	public Object insertContentDao(Properties newProps, String siteId);

	/**
	 * 
	 * @param key
	 * @return
	 */
	public Map<String, Object> getContent(Long key);
	
	/**
	 * 
	 * @param key
	 * @param siteId
	 * @return
	 */
	public Map<String, Object> getContent(Long key, String siteId);

	/**
	 * Absolutely no checking at all.
	 * 
	 * @param key
	 * @return
	 */
	public Map<String, Object> getContentDao(Long key);

	/**
	 * 
	 * @param key
	 * @param siteId
	 * @return
	 */
	public Map<String, Object> getContentDao(Long key, String siteId);

	/**
	 * 
	 * @param key
	 * @return
	 */
	public boolean deleteContent(Long key);
	
	/**
	 * remove the tool content site link
	 * @param key
	 * @return
	 */
	public String deleteContentLink(Long key);

	/**
	 * 
	 * @param key
	 * @param newProps
	 * @return
	 */
	public Object updateContent(Long key, Map<String, Object> newProps);

	/**
	 * 
	 * @param key
	 * @param newProps
	 * @return
	 */
	public Object updateContent(Long key, Properties newProps);
	
	/**
	 * 
	 * @param key
	 * @param newProps
	 * @param siteId
	 * @return
	 */
	public Object updateContent(Long key, Properties newProps, String siteId);

	/**
	 * 
	 * @param key
	 * @param newProps
	 * @param siteId
	 * @return
	 */
	public Object updateContentDao(Long key, Map<String, Object> newProps, String siteId);

	/**
	 * 
	 * @param search
	 * @param order
	 * @param first
	 * @param last
	 * @return
	 */
	public List<Map<String, Object>> getContents(String search, String order, int first, int last);

	/**
	 * 
	 * @param content
	 * @return
	 */
	public String getContentLaunch(Map<String, Object> content);

	/**
	 * 
	 * @param content
	 * @param tool
	 */
	public void filterContent(Map<String, Object> content, Map<String, Object> tool);

	/**
	 * 
	 * @param row
	 * @param fieldInfo
	 * @return
	 */
	public String formOutput(Object row, String fieldInfo);

	/**
	 * 
	 * @param row
	 * @param fieldInfo
	 * @return
	 */
	public String formOutput(Object row, String[] fieldInfo);

	/**
	 * 
	 * @param row
	 * @param fieldInfo
	 * @return
	 */
	public String formInput(Object row, String fieldInfo);

	/**
	 * 
	 * @param row
	 * @param fieldInfo
	 * @return
	 */
	public String formInput(Object row, String[] fieldInfo);

	// For Instructors, this model is filtered down dynamically based on
	// Tool settings
	/**
	 * Model Descriptions for Foorm You should probably retrieve these through getters in
	 * case there is some filtering in the service based on role/permission
	 */
	static String[] CONTENT_MODEL = { 
		"id:key", 
		"tool_id:integer:hidden=true",
		"SITE_ID:text:label=bl_content_site_id:required=true:maxlength=99:role=admin",
		"title:text:label=bl_title:required=true:allowed=true:maxlength=255",
		"pagetitle:text:label=bl_pagetitle:required=true:allowed=true:maxlength=255",
		"frameheight:integer:label=bl_frameheight",
		"newpage:checkbox:label=bl_newpage",
		"debug:checkbox:label=bl_debug",
		"custom:textarea:label=bl_custom:rows=5:cols=25:maxlength=1024",
		"launch:url:allowed=true:maxlength=1024",
		"consumerkey:text:allowed=true:maxlength=255",
		"secret:text:allowed=true:maxlength=255",
		"xmlimport:text:hidden=true:maxlength=16384",
		"settings:text:hidden=true:maxlength=8096",
		"placement:text:hidden=true:maxlength=256", 
		"placementsecret:text:hidden=true:maxlength=512",
		"oldplacementsecret:text:hidden=true:maxlength=512",
		"created_at:autodate",
		"updated_at:autodate" };

	/**
	 * 
	 */
	static String[] TOOL_MODEL = { 
		"id:key",
		"SITE_ID:text:maxlength=99:role=admin",
		"title:text:label=bl_title:required=true:maxlength=255",
		"allowtitle:radio:label=bl_allowtitle:choices=disallow,allow",
		"pagetitle:text:label=bl_pagetitle:required=true:maxlength=255",
		"allowpagetitle:radio:label=bl_allowpagetitle:choices=disallow,allow",
		"description:textarea:label=bl_description:maxlength=4096:",
		"status:radio:label=bl_status:choices=enable,disable",
		"visible:radio:label=bl_visible:choices=visible,stealth:role=admin",
		"launch:url:label=bl_launch:maxlength=1024",
		"allowlaunch:radio:label=bl_allowlaunch:choices=disallow,allow",
		"domain:text:label=bl_domain:hidden=true:maxlength=255",
		"consumerkey:text:label=bl_consumerkey:maxlength=255",
		"allowconsumerkey:radio:label=bl_allowconsumerkey:choices=disallow,allow",
		"secret:text:label=bl_secret:maxlength=255",
		"allowsecret:radio:label=bl_allowsecret:choices=disallow,allow",
		"frameheight:integer:label=bl_frameheight",
		"allowframeheight:radio:label=bl_allowframeheight:choices=disallow,allow",
		"privacy:header:fields=sendname,sendemailaddr",
		"sendname:checkbox:label=bl_sendname",
		"sendemailaddr:checkbox:label=bl_sendemailaddr",
		"allowoutcomes:checkbox:label=bl_allowoutcomes",
		"allowroster:checkbox:label=bl_allowroster",
		"allowsettings:checkbox:label=bl_allowsettings",
		"allowlori:checkbox:label=bl_allowlori",
		"newpage:radio:label=bl_newpage:choices=off,on,content",
		"debug:radio:label=bl_debug:choices=off,on,content",
		"custom:textarea:label=bl_custom:rows=5:cols=25:maxlength=1024",
		"allowcustom:checkbox:label=bl_allowcustom",
		"xmlimport:text:hidden=true:maxlength=16384",
		"splash:textarea:label=bl_splash:rows=5:cols=25:maxlength=4096",
		"created_at:autodate", 
		"updated_at:autodate" };

	/**
	 * 
	 */
	static String[] MAPPING_MODEL = { "id:key",
		"matchpattern:url:label=bl_matchpattern:required=true:maxlength=255",
		"launch:url:label=bl_launchurl:required=true:maxlength=255",
		"note:text:label=bl_note:maxlength=255", 
		"created_at:autodate",
		"updated_at:autodate" };

	/** Static constants for data fields */

	static final String LTI_ID =    	"id";
	static final String LTI_SITE_ID =     "SITE_ID";
	static final String LTI_TOOL_ID =     "tool_id";
	static final String LTI_TITLE =    	"title";
	static final String LTI_ALLOWTITLE =	"allowtitle";
	static final String LTI_PAGETITLE =    	"pagetitle";
	static final String LTI_ALLOWPAGETITLE =	"allowpagetitle";
	static final String LTI_PLACEMENT =    "placement";
	static final String LTI_DESCRIPTION = "description";
	static final String LTI_STATUS = 	"status";
	static final String LTI_VISIBLE = 	"visible";
	static final String LTI_LAUNCH = 	"launch";
	static final String LTI_ALLOWLAUNCH = 	"allowlaunch";
	static final String LTI_CONSUMERKEY= 	"consumerkey";
	static final String LTI_ALLOWCONSUMERKEY= 	"allowconsumerkey";
	static final String LTI_SECRET =   	"secret";
	static final String LTI_ALLOWSECRET =   	"allowsecret";
	static final String LTI_SECRET_INCOMPLETE = "-----";
	static final String LTI_FRAMEHEIGHT = "frameheight";
	static final String LTI_ALLOWFRAMEHEIGHT = "allowframeheight";
	static final String LTI_SENDNAME =	"sendname";
	static final String LTI_SENDEMAILADDR = "sendemailaddr";
	static final String LTI_ALLOWOUTCOMES = "allowoutcomes";
	static final String LTI_ALLOWROSTER = "allowroster";
	static final String LTI_ALLOWSETTINGS = "allowsettings";
	static final String LTI_ALLOWLORI = "allowlori";
	static final String LTI_SETTINGS = "settings";
	static final String LTI_NEWPAGE =	"newpage";
	static final String LTI_DEBUG =	"debug";
	static final String LTI_CUSTOM = 	"custom";
	static final String LTI_SPLASH = 	"splash";
	static final String LTI_ALLOWCUSTOM = "allowcustom";
	static final String LTI_XMLIMPORT = 	"xmlimport";
	static final String LTI_CREATED_AT =  "created_at"; 
	static final String LTI_UPATED_AT = 	"updated_at";
	static final String LTI_MATCHPATTERN = "matchpattern";
	static final String LTI_NOTE = 	"note";
	static final String LTI_PLACEMENTSECRET = 	"placementsecret";
	static final String LTI_OLDPLACEMENTSECRET = 	"oldplacementsecret";

}
