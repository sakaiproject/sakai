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
import org.sakaiproject.lti.api.LTIExportService.ExportType;
import org.sakaiproject.site.api.Site;

/**
 * <p>
 * A LTIService does things for LTI
 * </p>
 * <p>
 * Location is a combination of site id, (optional) page id and (optional) tool id
 * </p>
 */
public interface LTIService extends LTISubstitutionsFilter {
    /**
     * This string starts the references to resources in this service.
     */
    String REFERENCE_ROOT = "/lti";

    /**
     * Our indication that a secret is hidden
     */
    String SECRET_HIDDEN = "***************";

    String WEB_PORTLET = "sakai.web.168";
    /**
     * Model Descriptions for Foorm You should probably retrieve these through getters in
     * case there is some filtering in the service based on role/permission
     */
    String[] CONTENT_MODEL = {
            "id:key",
            "tool_id:integer:hidden=true",
            "SITE_ID:text:label=bl_content_site_id:required=true:maxlength=99:role=admin",
            "title:text:label=bl_title:required=true:allowed=true:maxlength=1024",
            "pagetitle:text:label=bl_pagetitle:required=true:allowed=true:maxlength=1024",
            "fa_icon:text:label=bl_fa_icon:allowed=true:maxlength=1024",
            "frameheight:integer:label=bl_frameheight:allowed=true",
            "toolorder:integer:label=bl_toolorder:maxlength=2",
            "newpage:checkbox:label=bl_newpage",
            "debug:checkbox:label=bl_debug",
            "custom:textarea:label=bl_custom:rows=5:cols=25:allowed=true:maxlength=16384",
            "launch:url:label=bl_launch:maxlength=1024:allowed=true",
            "consumerkey:text:label=bl_consumerkey:allowed=true:maxlength=1024",
            "secret:text:label=bl_secret:allowed=true:maxlength=1024",
            "resource_handler:text:label=bl_resource_handler:maxlength=1024:role=admin:only=lti2",
            "xmlimport:text:hidden=true:maxlength=1M",
            // LTI 2.x settings
            "settings:text:hidden=true:maxlength=1M",
            // Sakai LTI 1.x extension settings (see SAK-25621)
            "settings_ext:text:hidden=true:maxlength=1M",
            // LTI Content-Item (see SAK-29328)
            "contentitem:text:label=bl_contentitem:rows=5:cols=25:maxlength=1M:hidden=true",
            "placement:text:hidden=true:maxlength=256",
            "placementsecret:text:hidden=true:maxlength=512",
            "oldplacementsecret:text:hidden=true:maxlength=512",
            // SHA256 Support (See SAK-33898)
            "sha256:radio:label=bl_sha256:choices=off,on",
            // LTI 1.3 expansion space (See SAK-33772)
            "lti13:radio:label=bl_lti13:role=admin",
            "lti13_settings:text:maxlength=1M:role=admin",
            "created_at:autodate",
            "updated_at:autodate"};
    String[] CONTENT_EXTRA_FIELDS = {
            "SITE_TITLE:text:table=SAKAI_SITE:realname=TITLE",
            "SITE_CONTACT_NAME:text:table=ssp1:realname=VALUE",
            "SITE_CONTACT_EMAIL:text:table=ssp2:realname=VALUE",
            "ATTRIBUTION:text:table=ssp3:realname=VALUE",
            "URL:text:table=lti_tools:realname=launch",
            "searchURL:text:table=NULL" //no realname and table is NULL for this, it just exists in the select
    };
    /**
     *
     */
    String[] TOOL_MODEL = {
            "id:key",
            "version:radio:label=bl_version:choices=lti1,lti2:hidden=true",
            "SITE_ID:text:maxlength=99:role=admin",
            "title:text:label=bl_title:required=true:maxlength=1024",
            "allowtitle:radio:label=bl_allowtitle:choices=disallow,allow",
            "fa_icon:text:label=bl_fa_icon:allowed=true:maxlength=1024",
            "pagetitle:text:label=bl_pagetitle:required=true:maxlength=1024",
            "allowpagetitle:radio:label=bl_allowpagetitle:choices=disallow,allow",
            "description:textarea:label=bl_description:maxlength=4096",
            "status:radio:label=bl_status:choices=enable,disable",
            "visible:radio:label=bl_visible:choices=visible,stealth:role=admin",
            "resource_handler:text:label=bl_resource_handler:maxlength=1024:role=admin:only=lti2",
            "deployment_id:integer:hidden=true",
            "lti2_launch:header:fields=launch,consumerkey,secret:only=lti2",
            "launch:url:label=bl_launch:maxlength=1024:required=true",
            "allowlaunch:radio:label=bl_allowlaunch:choices=disallow,allow:only=lti1",
            "consumerkey:text:label=bl_consumerkey:maxlength=1024",
            "allowconsumerkey:radio:label=bl_allowconsumerkey:choices=disallow,allow:only=lti1",
            "secret:text:label=bl_secret:maxlength=1024",
            "allowsecret:radio:label=bl_allowsecret:choices=disallow,allow:only=lti1",
            "frameheight:integer:label=bl_frameheight",
            "toolorder:integer:label=bl_toolorder:maxlength=2",
            "allowframeheight:radio:label=bl_allowframeheight:choices=disallow,allow",
            "siteinfoconfig:radio:label=bl_siteinfoconfig:choices=bypass,config",
            "privacy:header:fields=sendname,sendemailaddr",
            "sendname:checkbox:label=bl_sendname",
            "sendemailaddr:checkbox:label=bl_sendemailaddr",
            "services:header:fields=allowoutcomes,allowroster,allowsettings",
            "allowoutcomes:checkbox:label=bl_allowoutcomes",
            "allowroster:checkbox:label=bl_allowroster",
            "allowsettings:checkbox:label=bl_allowsettings",
            // Hide these from end users until they are working in the various Sakai tools
            "pl_header:header:fields=pl_launch,pl_linkselection,pl_importitem,pl_fileitem,pl_contenteditor,pl_assessmentselection",
            "pl_launch:checkbox:label=bl_pl_launch",
            "pl_linkselection:checkbox:label=bl_pl_linkselection",
            "pl_contenteditor:checkbox:label=bl_pl_contenteditor",
            "pl_importitem:checkbox:label=bl_pl_importitem:role=admin",
            "pl_fileitem:checkbox:label=bl_pl_fileitem:role=admin",
            "pl_assessmentselection:checkbox:label=bl_pl_assessmentselection:role=admin",
            "newpage:radio:label=bl_newpage:choices=off,on,content",
            "debug:radio:label=bl_debug:choices=off,on,content",
            // LTI 1.x user-entered custom
            "custom:textarea:label=bl_custom:rows=5:cols=25:maxlength=16384",
            // LTI 2.x settings from web services
            "settings:text:hidden=true:maxlength=1M",
            // LTI 2.x tool-registration time parameters
            "parameter:textarea:label=bl_parameter:rows=5:cols=25:maxlength=16384:only=lti2",
            "tool_proxy_binding:textarea:label=bl_tool_proxy_binding:maxlength=2M:only=lti2:hide=insert:role=admin",
            "allowcustom:checkbox:label=bl_allowcustom",
            // SHA256 Support (See SAK-33898)
            "sha256:radio:label=bl_sha256:choices=off,on,content",
            // LTI 1.3 expansion space (See SAK-33772)
            "lti13:radio:label=bl_lti13:choices=off,on,content:role=admin",
            "lti13_settings:text:maxlength=1M:role=admin",
            "xmlimport:textarea:hidden=true:maxlength=1M",
            "splash:textarea:label=bl_splash:rows=5:cols=25:maxlength=16384",
            "created_at:autodate",
            "updated_at:autodate"};
    /**
     *
     */
    String[] DEPLOY_MODEL = {
            "id:key",
            "reg_state:radio:label=bl_reg_state:choices=lti2_ready,lti2_received,lti2_complete:hidden=true",
            "title:text:label=bl_title:required=true:maxlength=1024",
            "pagetitle:text:label=bl_pagetitle:required=true:maxlength=1024",
            "description:textarea:label=bl_description:maxlength=4096",
            "lti2_status:header:fields=status,visible",
            "status:radio:label=bl_status:choices=enable,disable",
            "visible:radio:label=bl_visible:choices=visible,stealth:role=admin",
            "privacy:header:fields=sendname,sendemailaddr",
            "sendname:checkbox:label=bl_sendname",
            "sendemailaddr:checkbox:label=bl_sendemailaddr",
            "services:header:fields=allowoutcomes,allowroster,allowsettings",
            "allowoutcomes:checkbox:label=bl_allowoutcomes",
            "allowroster:checkbox:label=bl_allowroster",
            "allowsettings:checkbox:label=bl_allowsettings",
            "allowcontentitem:checkbox:label=bl_allowcontentitem",
            "lti2_internal:header:fields=reg_launch,reg_key,reg_secret,reg_password,consumerkey,secret,reg_profile:hide=insert",
            "reg_launch:url:label=bl_reg_launch:maxlength=1024:role=admin",
            "reg_key:text:label=bl_reg_key:maxlength=1024:hide=insert:role=admin",
            "reg_password:text:label=bl_reg_password:maxlength=1024:hide=insert:role=admin",
            "reg_ack:text:label=bl_reg_ack:maxlength=4096:hide=insert:role=admin",
            "consumerkey:text:label=bl_consumerkey:maxlength=1024:hide=insert",
            "secret:text:label=bl_secret:maxlength=1024:hide=insert",
            "new_secret:text:label=bl_secret:maxlength=1024:hide=insert",
            "reg_profile:textarea:label=bl_reg_profile:maxlength=2M:hide=insert:role=admin",
            "settings:text:hidden=true:maxlength=1M",   // This is "custom" in the JSON
            "created_at:autodate",
            "updated_at:autodate"};
    // The model for the ToolProxy Binding (LTI 2.0)
    String[] BINDING_MODEL = {
            "id:key",
            "tool_id:integer:hidden=true",
            "SITE_ID:text:maxlength=99:role=admin",
            "settings:text:hidden=true:maxlength=1M",
            "created_at:autodate",
            "updated_at:autodate"};
    String[] MEMBERSHIPS_JOBS_MODEL = {
            "SITE_ID:text:maxlength=99:required=true",
            "memberships_id:text:maxlength=256:required=true",
            "memberships_url:text:maxlength=4000:required=true",
            "consumerkey:text:label=bl_consumerkey:allowed=true:maxlength=1024",
            "lti_version:text:maxlength=32:required=true"};
    /**
     * Static constants for data fields
     */

    String LTI_ID = "id";
    String LTI_SITE_ID = "SITE_ID";
    String LTI_TOOL_ID = "tool_id";
    String LTI_TITLE = "title";
    String LTI_ALLOWTITLE = "allowtitle";
    String LTI_PAGETITLE = "pagetitle";
    String LTI_ALLOWPAGETITLE = "allowpagetitle";
    String LTI_FA_ICON = "fa_icon";
    String LTI_PLACEMENT = "placement";
    String LTI_DESCRIPTION = "description";
    String LTI_STATUS = "status";
    String LTI_VISIBLE = "visible";
    String LTI_LAUNCH = "launch";
    String LTI_ALLOWLAUNCH = "allowlaunch";
    String LTI_CONSUMERKEY = "consumerkey";
    String LTI_ALLOWCONSUMERKEY = "allowconsumerkey";
    String LTI_SECRET = "secret";
    String LTI_NEW_SECRET = "new_secret";
    String LTI_ALLOWSECRET = "allowsecret";
    String LTI_SECRET_INCOMPLETE = "-----";
    String LTI_FRAMEHEIGHT = "frameheight";
    String LTI_ALLOWFRAMEHEIGHT = "allowframeheight";
    String LTI_TOOLORDER = "toolorder";
    String LTI_SENDNAME = "sendname";
    String LTI_SENDEMAILADDR = "sendemailaddr";
    String LTI_ALLOWOUTCOMES = "allowoutcomes";
    String LTI_ALLOWROSTER = "allowroster";
    String LTI_ALLOWSETTINGS = "allowsettings";
    String LTI_ALLOWCONTENTITEM = "allowcontentitem";
    String LTI_SETTINGS = "settings";
    String LTI_SETTINGS_EXT = "settings_ext";
    String LTI_CONTENTITEM = "contentitem";
    String LTI_NEWPAGE = "newpage";
    String LTI_DEBUG = "debug";
    String LTI_CUSTOM = "custom";
    String LTI_SPLASH = "splash";
    String LTI_ALLOWCUSTOM = "allowcustom";
    String LTI_XMLIMPORT = "xmlimport";
    String LTI_CREATED_AT = "created_at";
    String LTI_UPDATED_AT = "updated_at";
    String LTI_MATCHPATTERN = "matchpattern";
    String LTI_NOTE = "note";
    String LTI_PLACEMENTSECRET = "placementsecret";
    String LTI_OLDPLACEMENTSECRET = "oldplacementsecret";
    String LTI_DEPLOYMENT_ID = "deployment_id";
    // SHA256 Support (See SAK-33898)
    String LTI_SHA256 = "sha256";
    // BLTI-230 - LTI 2.0
    String LTI_VERSION = "version";
    Long LTI_VERSION_1 = 0L;
    Long LTI_VERSION_2 = new Long(1);
    String LTI_RESOURCE_HANDLER = "resource_handler";
    String LTI_REG_STATE = "reg_state";
    String LTI_REG_STATE_REGISTERED = "1";
    String LTI_REG_LAUNCH = "reg_launch";
    String LTI_REG_KEY = "reg_key";
    String LTI_REG_ACK = "reg_ack";
    String LTI_REG_PASSWORD = "reg_password";
    String LTI_PARAMETER = "parameter";
    String LTI_REG_PROFILE = "reg_profile"; // A.k.a tool_proxy
    // A subset of a tool_proxy with only a single resource_handler
    String LTI_TOOL_PROXY_BINDING = "tool_proxy_binding";
    // End of BLTI-230 - LTI 2.0
    String LTI_PL_LAUNCH = "pl_launch";
    String LTI_SITEINFOCONFIG = "siteinfoconfig";
    String LTI_PL_LINKSELECTION = "pl_linkselection";
    String LTI_PL_FILEITEM = "pl_fileitem";
    String LTI_PL_IMPORTITEM = "pl_importitem";
    String LTI_PL_CONTENTEDITOR = "pl_contenteditor";
    String LTI_PL_ASSESSMENTSELECTION = "pl_assessmentselection";
    String LTI_SEARCH_TOKEN_SEPARATOR_AND = "#&#";
    String LTI_SEARCH_TOKEN_SEPARATOR_OR = "#|#";
    String ESCAPED_LTI_SEARCH_TOKEN_SEPARATOR_AND = "\\#\\&\\#";
    String ESCAPED_LTI_SEARCH_TOKEN_SEPARATOR_OR = "\\#\\|\\#";
    String LTI_SEARCH_TOKEN_NULL = "#null#";
    String LTI_SEARCH_TOKEN_DATE = "#date#";
    String LTI_SEARCH_TOKEN_EXACT = "#exact#";
    String LTI_SEARCH_INTERNAL_DATE_FORMAT = "dd/MM/yyyy H:mm:ss";
    String LTI_SITE_ATTRIBUTION_PROPERTY_KEY = "basiclti.tool.site.attribution.key";
    String LTI_SITE_ATTRIBUTION_PROPERTY_KEY_DEFAULT = "Department";
    String LTI_SITE_ATTRIBUTION_PROPERTY_NAME = "basiclti.tool.site.attribution.name";
    String LTI_SITE_ATTRIBUTION_PROPERTY_NAME_DEFAULT = "content.attribution";

    // For Instructors, this model is filtered down dynamically based on
    // Tool settings

    boolean isMaintain(String siteId);

    /**
     * Adds a memberships job. Quartz uses these to sync memberships for LTI
     * sites
     */
    Object insertMembershipsJob(String siteId, String membershipsId, String membershipsUrl, String consumerKey, String ltiVersion);

    /**
     * Gets the memberships job for a site.
     *
     * @return A single row mapping, or null if none exists yet.
     */
    Map<String, Object> getMembershipsJob(String siteId);

    /**
     * Gets all the memberships jobs. Quartz uses these to sync memberships for LTI
     * sites
     *
     * @return A list of row mappings
     */
    List<Map<String, Object>> getMembershipsJobs();


    // -- Models

    String[] getToolModel(String siteId);

    String[] getContentModel(Long tool_id, String siteId);

    String[] getContentModel(Map<String, Object> tool, String siteId);

    String[] getDeployModel();


    // ---Tool

    Object insertTool(Properties newProps, String siteId);

    Object insertTool(Map<String, Object> newProps, String siteId);

    Object insertToolDao(Properties newProps, String siteId);

    Object insertToolDao(Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole);

    boolean deleteTool(Long key, String siteId);

    public List<String>  deleteToolAndContents(Long key, String siteId);

    boolean deleteToolDao(Long key, String siteId, boolean isAdminRole, boolean isMaintainRole);

    Map<String, Object> getTool(Long key, String siteId);

    Map<String, Object> getToolDao(Long key, String siteId);

    Map<String, Object> getToolDao(Long key, String siteId, boolean isAdminRole);


    Object updateTool(Long key, Properties newProps, String siteId);

    Object updateTool(Long key, Map<String, Object> newProps, String siteId);

    Object updateToolDao(Long key, Map<String, Object> newProps, String siteId);

    Object updateToolDao(Long key, Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole);




    // -- Tool Content
    Object insertToolContent(String id, String toolId, Properties reqProps, String siteId);

    Object insertToolSiteLink(String id, String title, String siteId);



    Map<String, Object> getToolForResourceHandlerDao(String resourceType);



    String getToolLaunch(Map<String, Object> tool, String siteId);

    String getExportUrl(String siteId, String filterId, ExportType exportType);


    List<Map<String, Object>> getTools(String search, String order, int first, int last, String siteId);

    /**
     * Gets a list of the launchable tools in the site
     * @param siteId
     */
    List<Map<String, Object>> getToolsLaunch(String siteId);

    /**
     * Gets a list of tools that can configure themselves in the site
     * @param siteId
     */
    List<Map<String, Object>> getToolsLtiLink(String siteId);

    /**
     * Get a list of tools that can return a FileItem
     * @param siteId
     */
    List<Map<String, Object>> getToolsFileItem(String siteId);

    /**
     * Get a list of tools that can return an imported Common Cartridge
     * @param siteId
     */
    List<Map<String, Object>> getToolsImportItem(String siteId);

    /**
     * Get a list of tools that can return content for the editor
     * @param siteId
     */
    List<Map<String, Object>> getToolsContentEditor(String siteId);

    /**
     * Get a list of tools that can function as Assessments
     * @param siteId
     */
    List<Map<String, Object>> getToolsAssessmentSelection(String siteId);

    List<Map<String, Object>> getToolsDao(String search, String order, int first, int last, String siteId);

    List<Map<String, Object>> getToolsDao(String search, String order, int first, int last, String siteId, boolean isAdmin);


    // --- Content

    Object insertContent(Properties newProps, String siteId);

    Object insertContentDao(Properties newProps, String siteId);

    Object insertContentDao(Properties newProps, String siteId, boolean isAdminRole, boolean isMaintainRole);

    Map<String, Object> getContent(Long key, String siteId);

    Map<String, Object> getContentDao(Long key);

    Map<String, Object> getContentDao(Long key, String siteId);

    Map<String, Object> getContentDao(Long key, String siteId, boolean isAdminRole);

    boolean deleteContent(Long key, String siteId);

    boolean deleteContentDao(Long key, String siteId, boolean isAdminRole, boolean isMaintainRole);



    Object updateContent(Long key, Map<String, Object> newProps, String siteId);

    Object updateContent(Long key, Properties newProps, String siteId);

    Object updateContentDao(Long key, Map<String, Object> newProps, String siteId);

    Object updateContentDao(Long key, Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole);

    List<Map<String, Object>> getContents(String search, String order, int first, int last, String siteId);

    /**
     * This finds a set of LTI Contents objects.
     *
     * @param search The SQL search string to limit the results
     * @param order  The SQL order by string.
     * @param first  The first item that should be returned.
     * @param last   The last item that should be returned.
     * @param siteId The site ID or null to search as admin.
     * @return A List of LTI Contents objects.
     */
    List<Map<String, Object>> getContentsDao(String search, String order, int first, int last, String siteId);

    List<Map<String, Object>> getContentsDao(String search, String order, int first, int last, String siteId, boolean isAdminRole);

    int countContents(String search, String siteId);

    int countContentsDao(String search, String siteId, boolean isAdminRole);

    String deleteContentLink(Long key, String siteId);

    String getContentLaunch(Map<String, Object> content);

    void filterContent(Map<String, Object> content, Map<String, Object> tool);


    // --- Deploy
    Object insertDeployDao(Properties newProps);

    Object insertDeployDao(Properties newProps, String siteId, boolean isAdminRole, boolean isMaintainRole);

    Object updateDeployDao(Long key, Object newProps);

    Object updateDeployDao(Long key, Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole);

    boolean deleteDeployDao(Long key);

    boolean deleteDeployDao(Long key, String siteId, boolean isAdminRole, boolean isMaintainRole);

    Map<String, Object> getDeployDao(Long key);

    Map<String, Object> getDeployDao(Long key, String siteId, boolean isAdminRole);

    Map<String, Object> getDeployForConsumerKeyDao(String consumerKey);

    List<Map<String, Object>> getDeploysDao(String search, String order, int first, int last);

    List<Map<String, Object>> getDeploysDao(String search, String order, int first, int last, String siteId, boolean isAdminRole);

    // -- Proxy binding
    boolean deleteProxyBindingDao(Long key);

    Object insertProxyBindingDao(Properties newProps);

    Object updateProxyBindingDao(Long key, Object newProps);

    Map<String, Object> getProxyBindingDao(Long tool_id, String siteId);


    // These can be static and moved to the tool, or at least split off into a Foorm UI

    String formOutput(Object row, String fieldInfo);

    String formOutput(Object row, String[] fieldInfo);

    String formInput(Object row, String fieldInfo);

    String formInput(Object row, String[] fieldInfo);

    boolean isAdmin(String siteId);

    /**
     * This adds a filter for the custom properties.
     * @param filter The filter to add.
     */
    void registerPropertiesFilter(LTISubstitutionsFilter filter);

    /**
     * This removes a filter for custom properties.
     * @param filter The filter to remove.
     */
    void removePropertiesFilter(LTISubstitutionsFilter filter);
}
