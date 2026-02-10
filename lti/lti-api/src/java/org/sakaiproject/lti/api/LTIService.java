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

import org.sakaiproject.site.api.Site;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.lti.api.LTIExportService.ExportType;
import org.sakaiproject.util.MergeConfig;

/**
 * <p>
 * A LTIService does things for LTI
 * </p>
 * <p>
 * Location is a combination of site id, (optional) page id and (optional) tool id
 * </p>
 */
public interface LTIService extends LTISubstitutionsFilter {

    /** Constants */
    String ADMIN_SITE = "!admin";
    String LAUNCH_PREFIX = "/access/lti/site/";
    String LAUNCH_PREFIX_LEGACY = "/access/basiclti/site/";

    // /access/lti/site/22153323-3037-480f-b979-c630e3e2b3cf/content:1
    String LAUNCH_CONTENT_REGEX = "^/access/.*lti/site/.*/content:(\\d+)";

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
            "id:key:archive=true",
            "tool_id:integer:hidden=true",
            "SITE_ID:text:label=bl_content_site_id:required=true:maxlength=99:role=admin",
            "title:text:label=bl_title:required=true:maxlength=1024:archive=true",
            "description:textarea:label=bl_description:maxlength=4096:archive=true",
            "frameheight:integer:label=bl_frameheight:archive=true",
            "newpage:checkbox:label=bl_newpage:archive=true",
            "protect:checkbox:label=bl_protect:role=admin",
            "debug:checkbox:label=bl_debug",
            "custom:textarea:label=bl_custom:rows=5:cols=25:maxlength=16384:archive=true",
            "launch:url:label=bl_launch:hidden=true:maxlength=1024:archive=true",
            "xmlimport:text:hidden=true:maxlength=1M",
            // LTI 2.x settings
            "settings:text:hidden=true:maxlength=1M",
            // This actually ends up storing the lineitem within the contentitem (not the whole contentitem)
            "contentitem:text:label=bl_contentitem:rows=5:cols=25:maxlength=1M:hidden=true:archive=true",
            "placement:text:hidden=true:maxlength=256",
            "placementsecret:text:hidden=true:maxlength=512",
            "oldplacementsecret:text:hidden=true:maxlength=512",
            // LTI 1.3 support removed from content model
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
            "id:key:archive=true",
            "SITE_ID:text:maxlength=99:role=admin",
            "title:text:label=bl_title:required=true:maxlength=1024:archive=true",
            "description:textarea:label=bl_description:maxlength=4096:archive=true",
            "status:radio:label=bl_status:choices=enable,disable",
            "visible:radio:label=bl_visible:choices=visible,stealth:role=admin",
            "deployment_id:integer:hidden=true:archive=true",
            "launch:url:label=bl_launch:maxlength=1024:required=true:archive=true",
            "newpage:radio:label=bl_newpage:choices=off,on,content:archive=true",
            "frameheight:integer:label=bl_frameheight:archive=true",
            "fa_icon:text:label=bl_fa_icon:maxlength=1024:archive=true",
            // SAK-49540 - Message Types (keep columns named pl_ for upwards compatibility)
            "pl_header:header:fields=pl_launch,pl_linkselection",
            "pl_launch:checkbox:label=bl_pl_launch:archive=true",
            "pl_linkselection:checkbox:label=bl_pl_linkselection:archive=true",
            "pl_contextlaunch:checkbox:label=bl_pl_contextlaunch:hidden=true",
            // SAK-49540 - Placements
            "pl_placement:header:fields=pl_lessonsselection,pl_contenteditor,pl_assessmentselection,pl_coursenav,pl_importitem",
            "pl_lessonsselection:checkbox:label=bl_pl_lessonsselection:archive=true",
            "pl_contenteditor:checkbox:label=bl_pl_contenteditor:archive=true",
            "pl_assessmentselection:checkbox:label=bl_pl_assessmentselection:archive=true",
            "pl_coursenav:checkbox:label=bl_pl_coursenav:archive=true",
            "pl_importitem:checkbox:label=bl_pl_importitem:role=admin:archive=true",
            "pl_fileitem:checkbox:label=bl_pl_fileitem:role=admin:hidden=true:archive=true",
            "privacy:header:fields=sendname,sendemailaddr,pl_privacy",
            "sendname:checkbox:label=bl_sendname:archive=true",
            "sendemailaddr:checkbox:label=bl_sendemailaddr:archive=true",
            "pl_privacy:checkbox:label=bl_pl_privacy:role=admin",
            "services:header:fields=allowoutcomes,allowlineitems,allowroster",
            "allowoutcomes:checkbox:label=bl_allowoutcomes:archive=true",
            "allowlineitems:checkbox:label=bl_allowlineitems:archive=true",
            "allowroster:checkbox:label=bl_allowroster:archive=true",

            "debug:radio:label=bl_debug:choices=off,on,content",
            "siteinfoconfig:radio:label=bl_siteinfoconfig:advanced:choices=bypass,config",
            "splash:textarea:label=bl_splash:rows=5:cols=25:maxlength=16384",

            // LTI 1.x user-entered custom
            "custom:textarea:label=bl_custom:rows=5:cols=25:maxlength=16384:archive=true",
            "rolemap:textarea:label=bl_rolemap:rows=5:cols=25:maxlength=16384:role=admin:archive=true",
            "lti13:radio:label=bl_lti13:choices=off,on,both:role=admin:archive=true",

            // LTI 1.3 security values from the tool
            "lti13_tool_security:header:fields=lti13_tool_keyset,lti13_oidc_endpoint,lti13_oidc_redirect",
            "lti13_tool_keyset:text:label=bl_lti13_tool_keyset:maxlength=1024:role=admin",  // From the tool - keep legacy field name
            "lti13_oidc_endpoint:text:label=bl_lti13_oidc_endpoint:maxlength=1024:role=admin",  // From the tool - keep legacy field name
            "lti13_oidc_redirect:text:label=bl_lti13_oidc_redirect:maxlength=1024:role=admin",  // From the tool - keep legacy field name

            // LTI 1.3 security values from the LMS
            "lti13_lms_security:header:fields=lti13_lms_issuer,lti13_client_id,lti13_lms_keyset,lti13_lms_endpoint,lti13_lms_token",
            "lti13_lms_issuer:text:label=bl_lti13_lms_issuer:readonly=true:persist=false:maxlength=1024:role=admin",
            "lti13_client_id:text:label=bl_lti13_client_id:readonly=true:maxlength=1024:role=admin",
            "lti13_lms_deployment_id:text:label=bl_lti13_lms_deployment_id:readonly=true:maxlength=1024:role=admin",
            "lti13_lms_keyset:text:label=bl_lti13_lms_keyset:readonly=true:persist=false:maxlength=1024:role=admin",
            "lti13_lms_endpoint:text:label=bl_lti13_lms_endpoint:readonly=true:persist=false:maxlength=1024:role=admin",
            "lti13_lms_token:text:label=bl_lti13_lms_token:readonly=true:persist=false:maxlength=1024:role=admin",

            // LTI 1.1 security arrangement
            "lti11_security:header:fields=consumerkey,allowconsumerkey,secret,allowsecret",
            "consumerkey:text:label=bl_consumerkey:maxlength=1024",

            "secret:text:label=bl_secret:maxlength=1024",

            "xmlimport:textarea:hidden=true:maxlength=1M",
            "lti13_auto_token:text:hidden=true:maxlength=1024",
            "lti13_auto_state:integer:hidden=true",
            "lti13_auto_registration:textarea:hidden=true:maxlength=1M",
            "sakai_tool_checksum:text:maxlength=99:hidden=true:persist=false:archive=true",
            "created_at:autodate",
            "updated_at:autodate"};

    String[] TOOL_SITE_MODEL = {
            "id:key",
            "tool_id:integer:hidden=true",
            "SITE_ID:text:label=bl_tool_site_SITE_ID:required=true:maxlength=99:role=admin",
            "notes:text:label=bl_tool_site_notes:maxlength=1024",
            "created_at:autodate",
            "updated_at:autodate",
    };

    String[] MEMBERSHIPS_JOBS_MODEL = {
            "SITE_ID:text:maxlength=99:required=true",
            "memberships_id:text:maxlength=256:required=true",
            "memberships_url:text:maxlength=4000:required=true",
            "consumerkey:text:label=bl_consumerkey:maxlength=1024",
            "lti_version:text:maxlength=32:required=true"};
    /**
     * Static constants for data fields
     */

    String LTI_ID = "id";
    String LTI_SITE_ID = "SITE_ID";
    String LTI_TOOL_ID = "tool_id";
    String LTI_TITLE = "title";
    String LTI_FA_ICON = "fa_icon";
    String LTI_PLACEMENT = "placement";
    String LTI_DESCRIPTION = "description";
    String LTI_ID_HISTORY = "id_history";
    String LTI_STATUS = "status";
    String LTI_VISIBLE = "visible";
    // This feels a little backwards - so we use constants
    int LTI_VISIBLE_GLOBAL = 0;
    int LTI_VISIBLE_STEALTH = 1;
    String LTI_LAUNCH = "launch";
    String LTI_CONSUMERKEY = "consumerkey";
    String LTI_SECRET = "secret";
    String LTI_NEW_SECRET = "new_secret";
    String LTI_SECRET_INCOMPLETE = "-----";
    String LTI_FRAMEHEIGHT = "frameheight";
    String LTI_SENDNAME = "sendname";
    String LTI_SENDEMAILADDR = "sendemailaddr";
    String LTI_ALLOWOUTCOMES = "allowoutcomes";
    String LTI_ALLOWLINEITEMS = "allowlineitems";
    String LTI_ALLOWROSTER = "allowroster";
    String LTI_SETTINGS = "settings";
    // This field is mis-named - so we make an alias :(
    String LTI_CONTENTITEM = "contentitem";
    String LTI_LINEITEM = "contentitem";
    String LTI_NEWPAGE = "newpage";
    // choices=off,on,content
    int LTI_TOOL_NEWPAGE_OFF = 0;
    int LTI_TOOL_NEWPAGE_ON = 1;
    int LTI_TOOL_NEWPAGE_CONTENT = 2;
    String LTI_PROTECT = "protect";
    String LTI_DEBUG = "debug";
    // choices=off,on,content
    int LTI_TOOL_DEBUG_OFF = 0;
    int LTI_TOOL_DEBUG_ON = 1;
    int LTI_TOOL_DEBUG_CONTENT = 2;
    String LTI_CUSTOM = "custom";
    String LTI_ROLEMAP = "rolemap";
    String LTI_SPLASH = "splash";
    String LTI13_AUTO_TOKEN = "lti13_auto_token";
    String LTI13_AUTO_STATE = "lti13_auto_state";
    String LTI13_AUTO_REGISTRATION = "lti13_auto_registration";
    String LTI_XMLIMPORT = "xmlimport";
    String LTI_CREATED_AT = "created_at";
    String LTI_UPDATED_AT = "updated_at";
    String LTI_MATCHPATTERN = "matchpattern";
    String LTI_NOTE = "note";
    String LTI_PLACEMENTSECRET = "placementsecret";
    String LTI_OLDPLACEMENTSECRET = "oldplacementsecret";

    // SAK-49540 - Message Types (keep columns named pl_ for upwards compatibility)
    String LTI_MT_LAUNCH = "pl_launch";
    String LTI_MT_LINKSELECTION = "pl_linkselection";
    String LTI_MT_CONTEXTLAUNCH = "pl_contextlaunch";
    String LTI_MT_PRIVACY = "pl_privacy";

	// SAK-49540 - Placements
    String LTI_PL_FILEITEM = "pl_fileitem";
    String LTI_PL_IMPORTITEM = "pl_importitem";
    String LTI_PL_CONTENTEDITOR = "pl_contenteditor";
    String LTI_PL_ASSESSMENTSELECTION = "pl_assessmentselection";
    String LTI_PL_LESSONSSELECTION = "pl_lessonsselection";
    String LTI_PL_COURSENAV = "pl_coursenav";

    String LTI_SITEINFOCONFIG = "siteinfoconfig";
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

    // LTI 1.3
    String LTI13 = "lti13";
    Long LTI13_LTI11 = 0L;
    Long LTI13_LTI13 = 1L;
    Long LTI13_BOTH = 2L;
    String LTI13_CLIENT_ID = "lti13_client_id";

    String LTI13_TOOL_KEYSET = "lti13_tool_keyset";
    String LTI13_TOOL_ENDPOINT = "lti13_oidc_endpoint";
    String LTI13_TOOL_REDIRECT = "lti13_oidc_redirect";

    // Not persisted - generated dynamically
    String LTI13_LMS_ISSUER = "lti13_lms_issuer";
    String LTI13_LMS_DEPLOYMENT_ID = "lti13_lms_deployment_id";
    String LTI13_LMS_KEYSET = "lti13_lms_keyset";
    String LTI13_LMS_TOKEN = "lti13_lms_token";
    String LTI13_LMS_ENDPOINT = "lti13_lms_endpoint";

    // Checksum for import and export
    String SAKAI_TOOL_CHECKSUM = "sakai_tool_checksum";
    String ARCHIVE_LTI_CONTENT_TAG = "sakai-lti-content";
    String ARCHIVE_LTI_TOOL_TAG = "sakai-lti-tool";
    String TOOL_IMPORT_MAP = "TOOL_IMPORT";

    /**
     * Indicate if the current logged in user has the maintain role in a site
     */
    boolean isMaintain(String siteId);

    /**
     * getId from an LTI map
     */
    Long getId(Map<String, Object> thing);

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

    String[] getToolSiteModel(String siteId);

    String[] getContentModel(Long tool_id, String siteId);

    /**
     * @param tool_id
     * @param siteId
     * @return If the form does not contain configuration, returns null; otherwise returns an array containing the result of getContentModel(tool_id, siteId)
     */
    public String[] getContentModelIfConfigurable(Long tool_id, String siteId);

    String[] getContentModel(Map<String, Object> tool, String siteId);

    // ---Tool

    String validateTool(Properties newProps);

    String validateTool(Map<String, Object> newProps);

    // Returns whether or not a tool needs further configuration
    boolean isDraft(Map<String, Object> tool);

    Map<String, Object> createStubLTI11Tool(String toolBaseUrl, String title);

    Properties convertToProperties(Map<String, Object> map);

    Object insertTool(Properties newProps, String siteId);

    Object insertTool(Map<String, Object> newProps, String siteId);

    Object insertToolDao(Properties newProps, String siteId);

    Object insertToolDao(Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole);

    boolean deleteTool(Long key, String siteId);

    public List<String>  deleteToolAndDependencies(Long key, String siteId);

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

    String getToolLaunch(Map<String, Object> tool, String siteId);

    String getExportUrl(String siteId, String filterId, ExportType exportType);

    // Transferring content links from one tool to another
    Object transferToolContentLinks(Long currentTool, Long newTool, String siteId);

    Object transferToolContentLinksDao(Long currentTool, Long newTool);

    // Tool Retrieval
    List<Map<String, Object>> getTools(String search, String order, int first, int last, String siteId);


    List<Map<String, Object>> getTools(String search, String order, int first, int last, String siteId, boolean includeStealthed);


    List<Map<String, Object>> getTools(String search, String order, int first, int last, String siteId, boolean includeStealthed, boolean includeLaunchable);


    /**
     * Gets a list of the launchable tools in the site
     * @param siteId
     */
    List<Map<String, Object>> getToolsLaunch(String siteId);

    /**
     * Gets a list of the launchable tools in the site, optionally including stealthed LTI tools
     * @param siteId
     * @param includeStealthed
     */
    List<Map<String, Object>> getToolsLaunch(String siteId, boolean includeStealthed);

    /**
     * Gets a list of the launchable tools from the site navigation (i.e. left nav)
     * @param includeStealthed
     * @param siteId
     */
    List<Map<String, Object>> getToolsLaunchCourseNav(String siteId, boolean includeStealthed);

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

    /**
     * Get a list of tools that can be used for Lessons
     * @param siteId
     */
    List<Map<String, Object>> getToolsLessonsSelection(String siteId);

    List<Map<String, Object>> getToolsDao(String search, String order, int first, int last, String siteId);

    List<Map<String, Object>> getToolsDao(String search, String order, int first, int last, String siteId, boolean isAdmin);

    List<Map<String, Object>> getToolsDao(String search, String order, int first, int last, String siteId, boolean isAdmin, boolean includeStealthed);

    List<Map<String, Object>> getToolsDao(String search, String order, int first, int last, String siteId, boolean isAdmin, boolean includeStealthed, boolean includeLaunchable);

    // --- Content

    String validateContent(Properties newProps);

    String validateContent(Map<String, Object> newProps);

    Object insertContent(Properties newProps, String siteId);

    Object insertContent(Map<String, Object> newProps, String siteId);

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

    Object updateContentDao(Long key, Map<String, Object> newProps);

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

    Long getContentKeyFromLaunch(String launch);

    void filterContent(Map<String, Object> content, Map<String, Object> tool);

    /**
     * Bean overload: filter content using tool/content beans (delegates to Map version).
     * Obtains content map, filters in place, then persists filtered values back to the bean.
     */
    default void filterContent(org.sakaiproject.lti.beans.LtiContentBean content, org.sakaiproject.lti.beans.LtiToolBean tool) {
        if (content == null) {
            return;
        }
        Map<String, Object> contentMap = content.asMap();
        Map<String, Object> toolMap = (tool != null) ? tool.asMap() : null;
        filterContent(contentMap, toolMap);
        content.applyFromMap(contentMap);
    }

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

    /**
     * Bean overload: filter custom substitutions using tool bean (delegates to Map version).
     */
    default void filterCustomSubstitutions(Properties properties, org.sakaiproject.lti.beans.LtiToolBean tool, Site site) {
        filterCustomSubstitutions(properties, tool != null ? tool.asMap() : null, site);
    }

    List<Map<String, Object>> getToolSitesByToolId(String toolId, String siteId);

    Map<String, Object> getToolSiteById(Long key, String siteId);

    Map<String, Object> getToolSiteDao(Long key, String siteId);

    List<Map<String, Object>> getToolSitesDao(String search, String order, int first, int last, String siteId, boolean isAdminRole);

    Object insertToolSite(Properties properties, String siteId);

    Object insertToolSiteDao(Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole);

    Object updateToolSite(Long key, Properties newProps, String siteId);

    Object updateToolSiteDao(Long key, Object newProps, String siteId, boolean isAdminRole, boolean isMaintainRole);

    boolean deleteToolSite(Long key, String siteId);

    boolean deleteToolSiteDao(Long key, String siteId, boolean isAdminRole, boolean isMaintainRole);

    int deleteToolSitesForToolIdDao(String toolId);

    boolean toolDeployed(Long toolKey, String siteId);

    /**
     * Include an LTI content item and its tool in a Sakai Archive
     * @param siteId
     */
    Element archiveContentByKey(Document doc, Long contentKey, String siteId);

    /**
     * Extract a tool and content from an LTI content element in XML.
     * Primary overload; populates the beans in place.
     *
     * @param  element  The sakai-lti-content tag
     * @param  content  Bean to populate with content (may be null to skip)
     * @param  tool  Bean to populate with nested tool (may be null to skip)
     */
    void mergeContent(Element element, org.sakaiproject.lti.beans.LtiContentBean content, org.sakaiproject.lti.beans.LtiToolBean tool);

    /**
     * Extract a tool and content from an LTI content element in XML.
     * Shims to bean overload; populates the maps.
     */
    default void mergeContent(Element element, Map<String, Object> content, Map<String, Object> tool) {
        org.sakaiproject.lti.beans.LtiContentBean contentBean = new org.sakaiproject.lti.beans.LtiContentBean();
        org.sakaiproject.lti.beans.LtiToolBean toolBean = new org.sakaiproject.lti.beans.LtiToolBean();
        mergeContent(element, contentBean, toolBean);
        if (content != null) {
            content.putAll(contentBean.asMap());
        }
        if (tool != null) {
            tool.putAll(toolBean.asMap());
        }
    }

    /**
     * Import a content item and link it to an existing or new tool
     * @param siteId
     */
    Long mergeContentFromImport(Element element, String siteId);

    /**
     * Copy an LTI Content Item from an old site into a new site
     *
     * This copies an LTI Content Item from one site to another site.
     * The content item is linked to an appropriate tool entry - either in
     * the new site or globally avalable.  If no suitable tool can be found,
     * it is created.
     *
     * This routine uses Dao access and assumes the calling code has insured
     * that the logged in user has appropriate permissions in both sites
     * before calling this routine.
     *
     * @param  contentKey  The old content item key from the old site
     * @param  siteId  The site id that the item is being copied from
     * @param  oldSiteId  The site id that the item is being copied from
     */
    Object copyLTIContent(Long contentKey, String siteId, String oldSiteId);

    /**
     * Copy an LTI Content Item from an old site into a new site
     *
     * This copies an LTI Content Item from one site to another site.
     * The content item is linked to an appropriate tool entry - either in
     * the new site or globally avalable.  If no suitable tool can be found,
     * it is created.
     *
     * This routine uses Dao access and assumes the calling code has insured
     * that the logged in user has appropriate permissions in both sites
     * before calling this routine.
     *
     * @param  ltiContent  The old content item from the old site
     * @param  siteId  The site id that the item is being copied from
     * @param  oldSiteId  The site id that the item is being copied from
     */
    Object copyLTIContent(Map<String, Object> ltiContent, String siteId, String oldSiteId);

    /**
     * Fix LTI launch URLs when copying content between contexts
     * @param text The text containing LTI launch URLs
     * @param fromContext The source context
     * @param toContext The destination context
     * @return The text with updated LTI launch URLs
     */
    String fixLtiLaunchUrls(String text, String fromContext, String toContext, Map<String, String> transversalMap);

    /**
     * Fix LTI launch URLs when copying content between contexts
     * @param text The text containing LTI launch URLs
     * @param toContext The destination context
     * @param mcx A map of import content items and their tools
     * @return The text with updated LTI launch URLs
     */
    String fixLtiLaunchUrls(String text, String toContext, MergeConfig mcx);

    // ====================================================================================
    // BEAN OVERLOAD METHODS - STRONGLY TYPED ALTERNATIVES TO Map<String, Object> METHODS
    // ====================================================================================
    // These methods provide type-safe alternatives to the traditional Map-based API.
    // They return strongly typed Bean objects instead of Map<String, Object>, providing
    // compile-time type checking, better IDE support, and eliminating the need for
    // manual casting and null checking in calling code.
    //
    // The Bean objects provide:
    // - Direct property access (e.g., tool.title instead of (String) toolMap.get("title"))
    // - Type safety (e.g., tool.id is Long, not Object)
    // - Security (sensitive fields excluded from toString() for logging safety)
    // - Convenience methods (asMap() for conversion back to Map when needed)
    // ====================================================================================

    // ------------------------------------------------------------------------------------
    // TOOL BEAN METHODS - Single Tool Retrieval
    // ------------------------------------------------------------------------------------
    // These methods retrieve individual LTI tools as strongly typed LtiToolBean objects.
    // They provide alternatives to the Map-based getTool() methods.

    /**
     * Get a single LTI tool as a Bean
     * @param key The tool ID
     * @param siteId The site ID
     * @return LtiToolBean or null if not found
     */
    default org.sakaiproject.lti.beans.LtiToolBean getToolAsBean(Long key, String siteId) {
        Map<String, Object> toolMap = getTool(key, siteId);
        return toolMap != null ? org.sakaiproject.lti.beans.LtiToolBean.of(toolMap) : null;
    }

    /**
     * Get a single LTI tool as a Bean (alias for getToolAsBean)
     * @param key The tool ID
     * @param siteId The site ID
     * @return LtiToolBean or null if not found
     */
    default org.sakaiproject.lti.beans.LtiToolBean getToolBean(Long key, String siteId) {
        Map<String, Object> toolMap = getTool(key, siteId);
        return toolMap != null ? org.sakaiproject.lti.beans.LtiToolBean.of(toolMap) : null;
    }

    /**
     * Get a tool as a Bean, bypassing security checks (DAO method)
     * @param key The tool key
     * @param siteId The site ID
     * @param isAdminRole Whether to bypass security checks
     * @return LtiToolBean instance or null if not found
     */
    default org.sakaiproject.lti.beans.LtiToolBean getToolDaoAsBean(Long key, String siteId, boolean isAdminRole) {
        Map<String, Object> toolMap = getToolDao(key, siteId, isAdminRole);
        return toolMap != null ? org.sakaiproject.lti.beans.LtiToolBean.of(toolMap) : null;
    }

    // ------------------------------------------------------------------------------------
    // TOOL BEAN METHODS - Multiple Tool Retrieval
    // ------------------------------------------------------------------------------------
    // These methods retrieve lists of LTI tools as strongly typed LtiToolBean objects.
    // They provide alternatives to the Map-based getTools() methods with various filtering options.

    /**
     * Get a list of LTI tools as Beans
     * @param search Search criteria
     * @param order Sort order
     * @param first First result index
     * @param last Last result index
     * @param siteId The site ID
     * @return List of LtiToolBean objects
     */
    default List<org.sakaiproject.lti.beans.LtiToolBean> getToolsAsBeans(String search, String order, int first, int last, String siteId) {
        List<Map<String, Object>> toolMaps = getTools(search, order, first, last, siteId);
        return toolMaps.stream()
                .map(org.sakaiproject.lti.beans.LtiToolBean::of)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get a list of LTI tools as Beans (alias for getToolsAsBeans)
     * @param search Search criteria
     * @param order Sort order
     * @param first First result index
     * @param last Last result index
     * @param siteId The site ID
     * @return List of LtiToolBean objects
     */
    default List<org.sakaiproject.lti.beans.LtiToolBean> getToolBeans(String search, String order, int first, int last, String siteId) {
        List<Map<String, Object>> toolMaps = getTools(search, order, first, last, siteId);
        List<org.sakaiproject.lti.beans.LtiToolBean> toolBeans = new java.util.ArrayList<>();
        for (Map<String, Object> toolMap : toolMaps) {
            toolBeans.add(org.sakaiproject.lti.beans.LtiToolBean.of(toolMap));
        }
        return toolBeans;
    }

    /**
     * Get a list of LTI tools as Beans with stealthed option
     * @param search Search criteria
     * @param order Sort order
     * @param first First result index
     * @param last Last result index
     * @param siteId The site ID
     * @param includeStealthed Whether to include stealthed tools
     * @return List of LtiToolBean objects
     */
    default List<org.sakaiproject.lti.beans.LtiToolBean> getToolsAsBeans(String search, String order, int first, int last, String siteId, boolean includeStealthed) {
        List<Map<String, Object>> toolMaps = getTools(search, order, first, last, siteId, includeStealthed);
        return toolMaps.stream()
                .map(org.sakaiproject.lti.beans.LtiToolBean::of)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get a list of LTI tools as Beans with stealthed option (alias for getToolsAsBeans)
     * @param search Search criteria
     * @param order Sort order
     * @param first First result index
     * @param last Last result index
     * @param siteId The site ID
     * @param includeStealthed Whether to include stealthed tools
     * @return List of LtiToolBean objects
     */
    default List<org.sakaiproject.lti.beans.LtiToolBean> getToolBeans(String search, String order, int first, int last, String siteId, boolean includeStealthed) {
        List<Map<String, Object>> toolMaps = getTools(search, order, first, last, siteId, includeStealthed);
        List<org.sakaiproject.lti.beans.LtiToolBean> toolBeans = new java.util.ArrayList<>();
        for (Map<String, Object> toolMap : toolMaps) {
            toolBeans.add(org.sakaiproject.lti.beans.LtiToolBean.of(toolMap));
        }
        return toolBeans;
    }

    /**
     * Get a list of LTI tools as Beans with stealthed and launchable options
     * @param search Search criteria
     * @param order Sort order
     * @param first First result index
     * @param last Last result index
     * @param siteId The site ID
     * @param includeStealthed Whether to include stealthed tools
     * @param includeLaunchable Whether to include only launchable tools
     * @return List of LtiToolBean objects
     */
    default List<org.sakaiproject.lti.beans.LtiToolBean> getToolBeans(String search, String order, int first, int last, String siteId, boolean includeStealthed, boolean includeLaunchable) {
        List<Map<String, Object>> toolMaps = getTools(search, order, first, last, siteId, includeStealthed, includeLaunchable);
        List<org.sakaiproject.lti.beans.LtiToolBean> toolBeans = new java.util.ArrayList<>();
        for (Map<String, Object> toolMap : toolMaps) {
            toolBeans.add(org.sakaiproject.lti.beans.LtiToolBean.of(toolMap));
        }
        return toolBeans;
    }

    /**
     * Get a list of launchable LTI tools as Beans
     * @param siteId The site ID
     * @return List of LtiToolBean objects
     */
    default List<org.sakaiproject.lti.beans.LtiToolBean> getToolsLaunchAsBeans(String siteId) {
        List<Map<String, Object>> toolMaps = getToolsLaunch(siteId);
        return toolMaps.stream()
                .map(org.sakaiproject.lti.beans.LtiToolBean::of)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get a list of tools that can return an imported Common Cartridge as Beans
     * @param siteId The site ID
     * @return List of LtiToolBean objects
     */
    default List<org.sakaiproject.lti.beans.LtiToolBean> getToolsImportItemBeans(String siteId) {
        List<Map<String, Object>> toolMaps = getToolsImportItem(siteId);
        List<org.sakaiproject.lti.beans.LtiToolBean> toolBeans = new java.util.ArrayList<>();
        for (Map<String, Object> toolMap : toolMaps) {
            toolBeans.add(org.sakaiproject.lti.beans.LtiToolBean.of(toolMap));
        }
        return toolBeans;
    }

    // ------------------------------------------------------------------------------------
    // CONTENT BEAN METHODS - Single Content Retrieval
    // ------------------------------------------------------------------------------------
    // These methods retrieve individual LTI content items as strongly typed LtiContentBean objects.
    // They provide alternatives to the Map-based getContent() methods.

    /**
     * Get a single LTI content item as a Bean
     * @param key The content ID
     * @param siteId The site ID
     * @return LtiContentBean or null if not found
     */
    default org.sakaiproject.lti.beans.LtiContentBean getContentAsBean(Long key, String siteId) {
        Map<String, Object> contentMap = getContent(key, siteId);
        return contentMap != null ? org.sakaiproject.lti.beans.LtiContentBean.of(contentMap) : null;
    }

    /**
     * Get a single LTI content item as a Bean (alias for getContentAsBean)
     * @param key The content ID
     * @param siteId The site ID
     * @return LtiContentBean or null if not found
     */
    default org.sakaiproject.lti.beans.LtiContentBean getContentBean(Long key, String siteId) {
        Map<String, Object> contentMap = getContent(key, siteId);
        return contentMap != null ? org.sakaiproject.lti.beans.LtiContentBean.of(contentMap) : null;
    }

    // ------------------------------------------------------------------------------------
    // CONTENT BEAN METHODS - Multiple Content Retrieval
    // ------------------------------------------------------------------------------------
    // These methods retrieve lists of LTI content items as strongly typed LtiContentBean objects.
    // They provide alternatives to the Map-based getContents() methods.

    /**
     * Get a list of LTI content items as Beans
     * @param search Search criteria
     * @param order Sort order
     * @param first First result index
     * @param last Last result index
     * @param siteId The site ID
     * @return List of LtiContentBean objects
     */
    default List<org.sakaiproject.lti.beans.LtiContentBean> getContentsAsBeans(String search, String order, int first, int last, String siteId) {
        List<Map<String, Object>> contentMaps = getContents(search, order, first, last, siteId);
        return contentMaps.stream()
                .map(org.sakaiproject.lti.beans.LtiContentBean::of)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get a list of LTI content items as Beans (alias for getContentsAsBeans)
     * @param search Search criteria
     * @param order Sort order
     * @param first First result index
     * @param last Last result index
     * @param siteId The site ID
     * @return List of LtiContentBean objects
     */
    default List<org.sakaiproject.lti.beans.LtiContentBean> getContentBeans(String search, String order, int first, int last, String siteId) {
        List<Map<String, Object>> contentMaps = getContents(search, order, first, last, siteId);
        List<org.sakaiproject.lti.beans.LtiContentBean> contentBeans = new java.util.ArrayList<>();
        for (Map<String, Object> contentMap : contentMaps) {
            contentBeans.add(org.sakaiproject.lti.beans.LtiContentBean.of(contentMap));
        }
        return contentBeans;
    }

    /**
     * Get a list of LTI content items as Beans (DAO access, bypasses security).
     * @param search Search criteria
     * @param order Sort order
     * @param first First result index
     * @param last Last result index
     * @param siteId The site ID
     * @return List of LtiContentBean objects
     */
    default List<org.sakaiproject.lti.beans.LtiContentBean> getContentsDaoAsBeans(String search, String order, int first, int last, String siteId) {
        List<Map<String, Object>> contentMaps = getContentsDao(search, order, first, last, siteId);
        List<org.sakaiproject.lti.beans.LtiContentBean> contentBeans = new java.util.ArrayList<>();
        for (Map<String, Object> contentMap : contentMaps) {
            contentBeans.add(org.sakaiproject.lti.beans.LtiContentBean.of(contentMap));
        }
        return contentBeans;
    }

    /**
     * Get a list of LTI content items as Beans (DAO access, bypasses security).
     * @param search Search criteria
     * @param order Sort order
     * @param first First result index
     * @param last Last result index
     * @param siteId The site ID
     * @param isAdminRole Whether to bypass security checks
     * @return List of LtiContentBean objects
     */
    default List<org.sakaiproject.lti.beans.LtiContentBean> getContentsDaoAsBeans(String search, String order, int first, int last, String siteId, boolean isAdminRole) {
        List<Map<String, Object>> contentMaps = getContentsDao(search, order, first, last, siteId, isAdminRole);
        List<org.sakaiproject.lti.beans.LtiContentBean> contentBeans = new java.util.ArrayList<>();
        for (Map<String, Object> contentMap : contentMaps) {
            contentBeans.add(org.sakaiproject.lti.beans.LtiContentBean.of(contentMap));
        }
        return contentBeans;
    }

    // ------------------------------------------------------------------------------------
    // TOOL SITE BEAN METHODS
    // ------------------------------------------------------------------------------------
    // These methods retrieve LTI tool site relationships as strongly typed LtiToolSiteBean objects.
    // They provide alternatives to the Map-based getToolSite*() methods.

    /**
     * Get a single LTI tool site as a Bean
     * @param key The tool site ID
     * @param siteId The site ID
     * @return LtiToolSiteBean or null if not found
     */
    default org.sakaiproject.lti.beans.LtiToolSiteBean getToolSiteAsBean(Long key, String siteId) {
        Map<String, Object> toolSiteMap = getToolSiteById(key, siteId);
        return toolSiteMap != null ? org.sakaiproject.lti.beans.LtiToolSiteBean.of(toolSiteMap) : null;
    }

    /**
     * Get a list of LTI tool sites as Beans
     * @param toolId The tool ID
     * @param siteId The site ID
     * @return List of LtiToolSiteBean objects
     */
    default List<org.sakaiproject.lti.beans.LtiToolSiteBean> getToolSitesByToolIdAsBeans(String toolId, String siteId) {
        List<Map<String, Object>> toolSiteMaps = getToolSitesByToolId(toolId, siteId);
        return toolSiteMaps.stream()
                .map(org.sakaiproject.lti.beans.LtiToolSiteBean::of)
                .collect(java.util.stream.Collectors.toList());
    }

    // ------------------------------------------------------------------------------------
    // MEMBERSHIPS JOB BEAN METHODS
    // ------------------------------------------------------------------------------------
    // These methods retrieve LTI memberships job data as strongly typed LtiMembershipsJobBean objects.
    // They provide alternatives to the Map-based getMembershipsJob*() methods.

    /**
     * Get a single LTI memberships job as a Bean
     * @param siteId The site ID
     * @return LtiMembershipsJobBean or null if not found
     */
    default org.sakaiproject.lti.beans.LtiMembershipsJobBean getMembershipsJobAsBean(String siteId) {
        Map<String, Object> jobMap = getMembershipsJob(siteId);
        return jobMap != null ? org.sakaiproject.lti.beans.LtiMembershipsJobBean.of(jobMap) : null;
    }

    /**
     * Get all LTI memberships jobs as Beans
     * @return List of LtiMembershipsJobBean objects
     */
    default List<org.sakaiproject.lti.beans.LtiMembershipsJobBean> getMembershipsJobsAsBeans() {
        List<Map<String, Object>> jobMaps = getMembershipsJobs();
        return jobMaps.stream()
                .map(org.sakaiproject.lti.beans.LtiMembershipsJobBean::of)
                .collect(java.util.stream.Collectors.toList());
    }

    // ------------------------------------------------------------------------------------
    // BEAN INSERT/UPDATE METHODS
    // ------------------------------------------------------------------------------------
    // These methods allow inserting and updating LTI entities using Bean objects instead of Maps.
    // They automatically convert Bean objects to Maps using asMap() before calling the underlying methods.

    /**
     * Insert a new LTI tool using Bean
     * @param toolBean The tool data as Bean
     * @param siteId The site ID
     * @return The ID of the inserted tool
     */
    default Object insertTool(org.sakaiproject.lti.beans.LtiToolBean toolBean, String siteId) {
        return insertTool(toolBean != null ? toolBean.asMap() : null, siteId);
    }

    /**
     * Insert a new LTI content using Bean
     * @param contentBean The content data as Bean
     * @param siteId The site ID
     * @return The ID of the inserted content
     */
    default Object insertContent(org.sakaiproject.lti.beans.LtiContentBean contentBean, String siteId) {
        return insertContent(contentBean != null ? contentBean.asMap() : null, siteId);
    }

    /**
     * Update an existing LTI tool using Bean
     * @param key The tool ID
     * @param toolBean The tool data as Bean
     * @param siteId The site ID
     * @return The result of the update operation
     */
    default Object updateTool(Long key, org.sakaiproject.lti.beans.LtiToolBean toolBean, String siteId) {
        return updateTool(key, toolBean != null ? toolBean.asMap() : null, siteId);
    }

    /**
     * Update a tool with a tool bean (DAO method)
     * @param key the tool key
     * @param tool the tool bean
     * @param siteId the site id
     * @return the result
     */
    default Object updateToolDao(Long key, org.sakaiproject.lti.beans.LtiToolBean tool, String siteId) {
        return updateToolDao(key, tool != null ? tool.asMap() : null, siteId);
    }

    /**
     * Update an existing LTI content using Bean
     * @param key The content ID
     * @param contentBean The content data as Bean
     * @param siteId The site ID
     * @return The result of the update operation
     */
    default Object updateContent(Long key, org.sakaiproject.lti.beans.LtiContentBean contentBean, String siteId) {
        return updateContent(key, contentBean != null ? contentBean.asMap() : null, siteId);
    }

    // ------------------------------------------------------------------------------------
    // BEAN UTILITY METHODS
    // ------------------------------------------------------------------------------------
    // These methods provide utility functions that work with Bean objects.

    /**
     * Get the launch URL for a content item using Bean
     * @param contentBean The content data as Bean
     * @return The launch URL
     */
    default String getContentLaunch(org.sakaiproject.lti.beans.LtiContentBean contentBean) {
        return getContentLaunch(contentBean != null ? contentBean.asMap() : null);
    }

    // ------------------------------------------------------------------------------------
    // BEAN FORM OUTPUT METHODS
    // ------------------------------------------------------------------------------------
    // These methods provide Bean-aware alternatives to the formOutput methods.
    // They take Bean objects as parameters and automatically convert them to Maps
    // using asMap() before calling the underlying formOutput methods.

    /**
     * Generate form output for a tool Bean
     * @param toolBean The tool data as Bean
     * @param fieldinfo The field information
     * @return The formatted output
     */
    default String formOutput(org.sakaiproject.lti.beans.LtiToolBean toolBean, String fieldinfo) {
        Map<String, Object> toolMap = (toolBean != null) ? toolBean.asMap() : null;
        return formOutput(toolMap, fieldinfo);
    }

    /**
     * Generate form output for a tool Bean
     * @param toolBean The tool data as Bean
     * @param formDefinition The form definition array
     * @return The formatted output
     */
    default String formOutput(org.sakaiproject.lti.beans.LtiToolBean toolBean, String[] formDefinition) {
        Map<String, Object> toolMap = (toolBean != null) ? toolBean.asMap() : null;
        return formOutput(toolMap, formDefinition);
    }

    /**
     * Generate form output for a content Bean
     * @param contentBean The content data as Bean
     * @param fieldinfo The field information
     * @return The formatted output
     */
    default String formOutput(org.sakaiproject.lti.beans.LtiContentBean contentBean, String fieldinfo) {
        Map<String, Object> contentMap = (contentBean != null) ? contentBean.asMap() : null;
        return formOutput(contentMap, fieldinfo);
    }

    /**
     * Generate form output for a content Bean
     * @param contentBean The content data as Bean
     * @param formDefinition The form definition array
     * @return The formatted output
     */
    default String formOutput(org.sakaiproject.lti.beans.LtiContentBean contentBean, String[] formDefinition) {
        Map<String, Object> contentMap = (contentBean != null) ? contentBean.asMap() : null;
        return formOutput(contentMap, formDefinition);
    }

    /**
     * Generate form output for a tool site Bean
     * @param toolSiteBean The tool site data as Bean
     * @param fieldinfo The field information
     * @return The formatted output
     */
    default String formOutput(org.sakaiproject.lti.beans.LtiToolSiteBean toolSiteBean, String fieldinfo) {
        Map<String, Object> toolSiteMap = (toolSiteBean != null) ? toolSiteBean.asMap() : null;
        return formOutput(toolSiteMap, fieldinfo);
    }

    /**
     * Generate form output for a tool site Bean
     * @param toolSiteBean The tool site data as Bean
     * @param formDefinition The form definition array
     * @return The formatted output
     */
    default String formOutput(org.sakaiproject.lti.beans.LtiToolSiteBean toolSiteBean, String[] formDefinition) {
        Map<String, Object> toolSiteMap = (toolSiteBean != null) ? toolSiteBean.asMap() : null;
        return formOutput(toolSiteMap, formDefinition);
    }

    // ------------------------------------------------------------------------------------
    // BEAN FORM INPUT METHODS
    // ------------------------------------------------------------------------------------
    // These methods provide Bean-aware alternatives to the formInput methods.
    // They take Bean objects as parameters and automatically convert them to Maps
    // using asMap() before calling the underlying formInput methods.

    /**
     * Generate form input for a tool Bean
     * @param toolBean The tool data as Bean
     * @param fieldinfo The field information
     * @return The formatted input
     */
    default String formInput(org.sakaiproject.lti.beans.LtiToolBean toolBean, String fieldinfo) {
        Map<String, Object> toolMap = (toolBean != null) ? toolBean.asMap() : null;
        return formInput(toolMap, fieldinfo);
    }

    /**
     * Generate form input for a tool Bean
     * @param toolBean The tool data as Bean
     * @param formDefinition The form definition array
     * @return The formatted input
     */
    default String formInput(org.sakaiproject.lti.beans.LtiToolBean toolBean, String[] formDefinition) {
        Map<String, Object> toolMap = (toolBean != null) ? toolBean.asMap() : null;
        return formInput(toolMap, formDefinition);
    }

    /**
     * Generate form input for a content Bean
     * @param contentBean The content data as Bean
     * @param fieldinfo The field information
     * @return The formatted input
     */
    default String formInput(org.sakaiproject.lti.beans.LtiContentBean contentBean, String fieldinfo) {
        Map<String, Object> contentMap = (contentBean != null) ? contentBean.asMap() : null;
        return formInput(contentMap, fieldinfo);
    }

    /**
     * Generate form input for a content Bean
     * @param contentBean The content data as Bean
     * @param formDefinition The form definition array
     * @return The formatted input
     */
    default String formInput(org.sakaiproject.lti.beans.LtiContentBean contentBean, String[] formDefinition) {
        Map<String, Object> contentMap = (contentBean != null) ? contentBean.asMap() : null;
        return formInput(contentMap, formDefinition);
    }

    /**
     * Generate form input for a tool site Bean
     * @param toolSiteBean The tool site data as Bean
     * @param fieldinfo The field information
     * @return The formatted input
     */
    default String formInput(org.sakaiproject.lti.beans.LtiToolSiteBean toolSiteBean, String fieldinfo) {
        Map<String, Object> toolSiteMap = (toolSiteBean != null) ? toolSiteBean.asMap() : null;
        return formInput(toolSiteMap, fieldinfo);
    }

    /**
     * Generate form input for a tool site Bean
     * @param toolSiteBean The tool site data as Bean
     * @param formDefinition The form definition array
     * @return The formatted input
     */
    default String formInput(org.sakaiproject.lti.beans.LtiToolSiteBean toolSiteBean, String[] formDefinition) {
        Map<String, Object> toolSiteMap = (toolSiteBean != null) ? toolSiteBean.asMap() : null;
        return formInput(toolSiteMap, formDefinition);
    }
}
