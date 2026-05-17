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
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.lti.api.LTIExportService.ExportType;
import org.sakaiproject.lti.beans.LtiContentBean;
import org.sakaiproject.lti.beans.LtiMembershipsJobBean;
import org.sakaiproject.lti.beans.LtiToolBean;
import org.sakaiproject.lti.beans.LtiToolSiteBean;
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
    final String ADMIN_SITE = "!admin";
    final String LAUNCH_PREFIX = "/access/lti/site/";
    final String LAUNCH_PREFIX_LEGACY = "/access/basiclti/site/";

    // /access/lti/site/22153323-3037-480f-b979-c630e3e2b3cf/content:1
    final String LAUNCH_CONTENT_REGEX = "^/access/.*lti/site/.*/content:(\\d+)";

    /**
     * This string starts the references to resources in this service.
     */
    final String REFERENCE_ROOT = "/lti";

    /**
     * Our indication that a secret is hidden
     */
    final String SECRET_HIDDEN = "***************";

    final String WEB_PORTLET = "sakai.web.168";

    /** The custom property to key the webapi endpoint on */
    final String PROPERTY_CUSTOM_WEBAPI_ENDPOINT = "sakai_webapi_endpoint";

    /** The custom property to key the direct endpoint on */
    final String PROPERTY_CUSTOM_DIRECTAPI_ENDPOINT = "sakai_directapi_endpoint";

    /** When false, LTI Bearer tokens are rejected on webapi ({@code /api}) without validation. */
    final String PROPERTY_WEBAPI_ENABLED = "lti.webapi.enabled";
    final Boolean PROPERTY_WEBAPI_ENABLED_DEFAULT = true;

    /** When false, LTI Bearer tokens are rejected on Entity Broker ({@code /direct}) without validation. */
    final String PROPERTY_DIRECTAPI_ENABLED = "lti.directapi.enabled";
    final Boolean PROPERTY_DIRECTAPI_ENABLED_DEFAULT = true;

    /**
     * Model Descriptions for Foorm You should probably retrieve these through getters in
     * case there is some filtering in the service based on role/permission
     */
    final String[] CONTENT_MODEL = {
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
    final String[] CONTENT_EXTRA_FIELDS = {
            "SITE_TITLE:text:table=SAKAI_SITE:realname=TITLE",
            "SITE_CONTACT_NAME:text:table=ssp1:realname=VALUE",
            "SITE_CONTACT_EMAIL:text:table=ssp2:realname=VALUE",
            "ATTRIBUTION:text:table=ssp3:realname=VALUE",
            "URL:text:table=lti_tools:realname=launch",
            "searchURL:text:table=NULL" //no realname and table is NULL for this, it just exists in the select
    };

    final String[] TOOL_MODEL = {
            "id:key:archive=true",
            "SITE_ID:text:maxlength=99:role=admin",
            "title:text:label=bl_title:required=true:maxlength=1024:archive=true",
            "description:textarea:label=bl_description:maxlength=4096:archive=true",
            "status:radio:label=bl_status:choices=enable,disable",
            "visible:radio:label=bl_visible:choices=visible,stealth:role=admin",
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
            "services:header:fields=allowoutcomes,allowlineitems,allowgradebookreadonly,allowroster",
            "allowoutcomes:checkbox:label=bl_allowoutcomes:archive=true",
            "allowlineitems:checkbox:label=bl_allowlineitems:archive=true",
            "allowgradebookreadonly:checkbox:label=bl_allowgradebookreadonly:archive=true",
            "allowroster:checkbox:label=bl_allowroster:archive=true",

            "debug:radio:label=bl_debug:choices=off,on,content",
            "siteinfoconfig:radio:label=bl_siteinfoconfig:advanced:choices=bypass,config",
            "splash:textarea:label=bl_splash:rows=5:cols=25:maxlength=16384",

            // LTI 1.x user-entered custom
            "custom:textarea:label=bl_custom:rows=5:cols=25:maxlength=16384:archive=true",
            "rolemap:textarea:label=bl_rolemap:rows=5:cols=25:maxlength=16384:role=admin:archive=true",
            "lti13:radio:label=bl_lti13:choices=off,on,both:role=admin:archive=true",

            // LTI 1.3 security values from the tool
            "lti13_tool_security:header:fields=lti13_tool_keyset,deployment_id,lti13_oidc_endpoint,lti13_oidc_redirect",
            "lti13_tool_keyset:text:label=bl_lti13_tool_keyset:maxlength=1024:role=admin",  // From the tool - keep legacy field name
            "deployment_id:text:label=bl_deployment_id:maxlength=255:role=admin:archive=true",
            "lti13_oidc_endpoint:text:label=bl_lti13_oidc_endpoint:maxlength=1024:role=admin",  // From the tool - keep legacy field name
            "lti13_oidc_redirect:text:label=bl_lti13_oidc_redirect:maxlength=1024:role=admin",  // From the tool - keep legacy field name

            // LTI 1.3 security values from the LMS
            "lti13_lms_security:header:fields=lti13_lms_issuer,lti13_client_id,lti13_lms_keyset,lti13_lms_endpoint,lti13_lms_token",
            "lti13_lms_issuer:text:label=bl_lti13_lms_issuer:readonly=true:persist=false:maxlength=1024:role=admin",
            "lti13_client_id:text:label=bl_lti13_client_id:readonly=true:maxlength=1024:role=admin",
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

    final String[] TOOL_SITE_MODEL = {
            "id:key",
            "tool_id:integer:hidden=true",
            "SITE_ID:text:label=bl_tool_site_SITE_ID:required=true:maxlength=99:role=admin",
            "notes:text:label=bl_tool_site_notes:maxlength=1024",
            "deployment_group:text:label=bl_deployment_group:maxlength=128:alphanumeric=true:truncate=false",
            "created_at:autodate",
            "updated_at:autodate",
    };

    final String[] MEMBERSHIPS_JOBS_MODEL = {
            "SITE_ID:text:maxlength=99:required=true",
            "memberships_id:text:maxlength=256:required=true",
            "memberships_url:text:maxlength=4000:required=true",
            "consumerkey:text:label=bl_consumerkey:maxlength=1024",
            "lti_version:text:maxlength=32:required=true"};
    /**
     * Static constants for data fields
     */

    final String LTI_ID = "id";
    final String LTI_SITE_ID = "SITE_ID";
    final String LTI_TOOL_ID = "tool_id";
    final String LTI_TITLE = "title";
    final String LTI_FA_ICON = "fa_icon";
    final String LTI_PLACEMENT = "placement";
    final String LTI_DESCRIPTION = "description";
    final String LTI_ID_HISTORY = "id_history";
    final String LTI_STATUS = "status";
    final String LTI_VISIBLE = "visible";
    // This feels a little backwards - so we use constants
    final int LTI_VISIBLE_GLOBAL = 0;
    final int LTI_VISIBLE_STEALTH = 1;
    final String LTI_LAUNCH = "launch";
    final String LTI_CONSUMERKEY = "consumerkey";
    final String LTI_SECRET = "secret";
    final String LTI_NEW_SECRET = "new_secret";
    final String LTI_SECRET_INCOMPLETE = "-----";
    final String LTI_FRAMEHEIGHT = "frameheight";
    final String LTI_SENDNAME = "sendname";
    final String LTI_SENDEMAILADDR = "sendemailaddr";
    final String LTI_ALLOWOUTCOMES = "allowoutcomes";
    final String LTI_ALLOWLINEITEMS = "allowlineitems";
    final String LTI_ALLOWGRADEBOOKREADONLY = "allowgradebookreadonly";
    final String LTI_ALLOWROSTER = "allowroster";
    final String LTI_SETTINGS = "settings";
    // This field is mis-named - so we make an alias :(
    final String LTI_CONTENTITEM = "contentitem";
    final String LTI_LINEITEM = "contentitem";
    final String LTI_NEWPAGE = "newpage";
    // choices=off,on,content
    final int LTI_TOOL_NEWPAGE_OFF = 0;
    final int LTI_TOOL_NEWPAGE_ON = 1;
    final int LTI_TOOL_NEWPAGE_CONTENT = 2;
    final String LTI_PROTECT = "protect";
    final String LTI_DEBUG = "debug";
    // choices=off,on,content
    final int LTI_TOOL_DEBUG_OFF = 0;
    final int LTI_TOOL_DEBUG_ON = 1;
    final int LTI_TOOL_DEBUG_CONTENT = 2;
    final String LTI_CUSTOM = "custom";
    final String LTI_ROLEMAP = "rolemap";
    final String LTI_SPLASH = "splash";
    final String LTI13_AUTO_TOKEN = "lti13_auto_token";
    final String LTI13_AUTO_STATE = "lti13_auto_state";
    final String LTI13_AUTO_REGISTRATION = "lti13_auto_registration";
    final String LTI_XMLIMPORT = "xmlimport";
    final String LTI_CREATED_AT = "created_at";
    final String LTI_UPDATED_AT = "updated_at";
    final String LTI_MATCHPATTERN = "matchpattern";
    final String LTI_NOTE = "note";
    final String LTI_PLACEMENTSECRET = "placementsecret";
    final String LTI_OLDPLACEMENTSECRET = "oldplacementsecret";

    // SAK-49540 - Message Types (keep columns named pl_ for upwards compatibility)
    final String LTI_MT_LAUNCH = "pl_launch";
    final String LTI_MT_LINKSELECTION = "pl_linkselection";
    final String LTI_MT_CONTEXTLAUNCH = "pl_contextlaunch";
    final String LTI_MT_PRIVACY = "pl_privacy";

    // SAK-49540 - Placements
    final String LTI_PL_FILEITEM = "pl_fileitem";
    final String LTI_PL_IMPORTITEM = "pl_importitem";
    final String LTI_PL_CONTENTEDITOR = "pl_contenteditor";
    final String LTI_PL_ASSESSMENTSELECTION = "pl_assessmentselection";
    final String LTI_PL_LESSONSSELECTION = "pl_lessonsselection";
    final String LTI_PL_COURSENAV = "pl_coursenav";

    final String LTI_SITEINFOCONFIG = "siteinfoconfig";
    final String LTI_SEARCH_TOKEN_SEPARATOR_AND = "#&#";
    final String LTI_SEARCH_TOKEN_SEPARATOR_OR = "#|#";
    final String ESCAPED_LTI_SEARCH_TOKEN_SEPARATOR_AND = "\\#\\&\\#";
    final String ESCAPED_LTI_SEARCH_TOKEN_SEPARATOR_OR = "\\#\\|\\#";
    final String LTI_SEARCH_TOKEN_NULL = "#null#";
    final String LTI_SEARCH_TOKEN_DATE = "#date#";
    final String LTI_SEARCH_TOKEN_EXACT = "#exact#";
    final String LTI_SEARCH_INTERNAL_DATE_FORMAT = "dd/MM/yyyy H:mm:ss";
    final String LTI_SITE_ATTRIBUTION_PROPERTY_KEY = "basiclti.tool.site.attribution.key";
    final String LTI_SITE_ATTRIBUTION_PROPERTY_KEY_DEFAULT = "Department";
    final String LTI_SITE_ATTRIBUTION_PROPERTY_NAME = "basiclti.tool.site.attribution.name";
    final String LTI_SITE_ATTRIBUTION_PROPERTY_NAME_DEFAULT = "content.attribution";

    // LTI 1.3
    final String LTI13 = "lti13";
    final Long LTI13_LTI11 = 0L;
    final Long LTI13_LTI13 = 1L;
    final Long LTI13_BOTH = 2L;
    final String LTI_DEPLOYMENT_ID = "deployment_id";
    final String LTI13_CLIENT_ID = "lti13_client_id";
    final String LTI13_LMS_DEPLOYMENT_ID = "lti13_lms_deployment_id";

    final String LTI13_TOOL_KEYSET = "lti13_tool_keyset";
    final String LTI13_TOOL_ENDPOINT = "lti13_oidc_endpoint";
    final String LTI13_TOOL_REDIRECT = "lti13_oidc_redirect";

    // Not persisted - generated dynamically
    final String LTI13_LMS_ISSUER = "lti13_lms_issuer";
    final String LTI13_LMS_KEYSET = "lti13_lms_keyset";
    final String LTI13_LMS_TOKEN = "lti13_lms_token";
    final String LTI13_LMS_ENDPOINT = "lti13_lms_endpoint";

    /**
     * Optional per-site deployment identifier for LTI 1.3 (lti_tool_site.deployment_group).
     * Used in {@link org.sakaiproject.lti.util.SakaiLTIUtil#resolveLaunchDeploymentId} at precedence
     * step 2 (after explicit site {@code lti13.deployment_id}, before mapped site properties).
     */
    final String LTI_DEPLOYMENT_GROUP = "deployment_group";

    /**
     * @deprecated No longer read; use {@link org.sakaiproject.lti.util.SakaiLTIUtil#resolveLaunchDeploymentId} instead.
     */
    final String LTI_JWT_DEPLOYMENT_ID_OVERRIDE_PROP = "lti_jwt_deployment_id_override";

    // Checksum for import and export
    final String SAKAI_TOOL_CHECKSUM = "sakai_tool_checksum";
    final String ARCHIVE_LTI_CONTENT_TAG = "sakai-lti-content";
    final String ARCHIVE_LTI_TOOL_TAG = "sakai-lti-tool";
    final String TOOL_IMPORT_MAP = "TOOL_IMPORT";

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

    Object insertTool(Object newPropsObject, String siteId, boolean isAdminRole, boolean isMaintainRole);

    void deleteTool(Long key, String siteId) throws Exception;

    Map<String, Object> getTool(Long key, String siteId);

    Map<String, Object> getTool(Long key, String siteId, boolean isAdminRole);

    Object updateTool(Long key, Properties newProps, String siteId);

    Object updateTool(Long key, Map<String, Object> newProps, String siteId);

    // -- Tool Content
    Object insertToolContent(String id, String toolId, Properties reqProps, String siteId);

    Object insertToolSiteLink(String id, String title, String siteId);

    String getToolLaunch(Map<String, Object> tool, String siteId);

    String getExportUrl(String siteId, String filterId, ExportType exportType);

    // Transferring content links from one tool to another
    Object transferToolContentLinks(Long currentTool, Long newTool, String siteId);

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

    // --- Content

    String validateContent(Properties newProps);

    String validateContent(Map<String, Object> newProps);

    Object insertContent(Properties newProps, String siteId);

    Object insertContent(Map<String, Object> newProps, String siteId);

    Object insertContent(Properties newProps, String siteId, boolean isAdminRole, boolean isMaintainRole);

    Map<String, Object> getContent(Long key);

    Map<String, Object> getContent(Long key, String siteId);

    Map<String, Object> getContent(Long key, String siteId, boolean isAdminRole);

    boolean deleteContent(Long key, String siteId);

    Object updateContent(Long key, Map<String, Object> newProps);

    Object updateContent(Long key, Map<String, Object> newProps, String siteId);

    Object updateContent(Long key, Properties newProps, String siteId);

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
    List<Map<String, Object>> getContents(String search, String order, int first, int last, String siteId);

    List<Map<String, Object>> getContents(String search, String order, int first, int last, String siteId, boolean isAdminRole);

    int countContents(String search, String siteId);

    String deleteContentLink(Long key, String siteId);

    String getContentLaunch(Map<String, Object> content);

    Long getContentKeyFromLaunch(String launch);

    void filterContent(Map<String, Object> content, Map<String, Object> tool);

    // These can be static and moved to the tool, or at least split off into a Foorm UI

    String formOutput(Object row, String fieldInfo);

    String formOutput(Object row, String[] fieldInfo);

    String formInput(Object row, String fieldInfo);

    String formInput(Object row, String[] fieldInfo);

    boolean isAdmin(String siteId);

    /**
     * Is lti.webapi.enabled on
     */
    boolean isWebApiEnabled();

    /**
     * Is lti.directapi.enabled on
     */
    boolean isDirectApiEnabled();

    /**
     * Are either of the lti.webapi.enabled or lti.directapi.enabled properties switched on?
     */
    boolean isApiEnabled();

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

    List<Map<String, Object>> getToolSitesByToolId(String toolId, String siteId);

    Map<String, Object> getToolSiteById(Long key, String siteId);

    List<Map<String, Object>> getToolSites(String search, String order, int first, int last, String siteId, boolean isAdminRole);

    Object insertToolSite(Properties properties, String siteId);

    Object updateToolSite(Long key, Properties newProps, String siteId);

    boolean deleteToolSite(Long key, String siteId);

    Set<String> getToolPermissions(Long toolId);

    Set<String> getToolPermissions(Long toolId, String siteId);

    void setToolPermissions(Long toolId, Set<String> permissions, String siteId) throws Exception;

    void deleteToolPermissions(Long toolId);

    boolean toolDeployed(Long toolKey, String siteId);

    /**
     * Include an LTI content item and its tool in a Sakai Archive
     * @param siteId
     */
    Element archiveContentByKey(Document doc, Long contentKey, String siteId);

    /**
     * Extract a tool and content from an LTI content element in XML
     *
     * @param  element  The sakai-lti-content tag
     * @param  content  An empty map to return the content item
     * @param  tool  An empty map to return the tool associated with content item
     */
    void mergeContent(Element element, Map<String, Object> content, Map<String, Object> tool);

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
    LtiToolBean getToolAsBean(Long key, String siteId);

    /**
     * Get a single LTI tool as a Bean (alias for getToolAsBean)
     * @param key The tool ID
     * @param siteId The site ID
     * @return LtiToolBean or null if not found
     */
    LtiToolBean getToolBean(Long key, String siteId);

    /**
     * Get a tool as a Bean, bypassing security checks (DAO method)
     * @param key The tool key
     * @param siteId The site ID
     * @param isAdminRole Whether to bypass security checks
     * @return LtiToolBean instance or null if not found
     */
    LtiToolBean getToolDaoAsBean(Long key, String siteId, boolean isAdminRole);

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
    List<LtiToolBean> getToolsAsBeans(String search, String order, int first, int last, String siteId);

    /**
     * Get a list of LTI tools as Beans (alias for getToolsAsBeans)
     * @param search Search criteria
     * @param order Sort order
     * @param first First result index
     * @param last Last result index
     * @param siteId The site ID
     * @return List of LtiToolBean objects
     */
    List<LtiToolBean> getToolBeans(String search, String order, int first, int last, String siteId);

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
    List<LtiToolBean> getToolsAsBeans(String search, String order, int first, int last, String siteId, boolean includeStealthed);

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
    List<LtiToolBean> getToolBeans(String search, String order, int first, int last, String siteId, boolean includeStealthed);

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
    List<LtiToolBean> getToolBeans(String search, String order, int first, int last, String siteId, boolean includeStealthed, boolean includeLaunchable);

    /**
     * Get a list of launchable LTI tools as Beans
     * @param siteId The site ID
     * @return List of LtiToolBean objects
     */
    List<LtiToolBean> getToolsLaunchAsBeans(String siteId);

    /**
     * Get a list of tools that can return an imported Common Cartridge as Beans
     * @param siteId The site ID
     * @return List of LtiToolBean objects
     */
    List<LtiToolBean> getToolsImportItemBeans(String siteId);

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
    LtiContentBean getContentAsBean(Long key, String siteId);

    /**
     * Get a single LTI content item as a Bean (alias for getContentAsBean)
     * @param key The content ID
     * @param siteId The site ID
     * @return LtiContentBean or null if not found
     */
    LtiContentBean getContentBean(Long key, String siteId);

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
    List<LtiContentBean> getContentsAsBeans(String search, String order, int first, int last, String siteId);

    List<LtiContentBean> getContentsForToolAndSite(Long toolId, String siteId);

    /**
     * Get a list of LTI content items as Beans (alias for getContentsAsBeans)
     * @param search Search criteria
     * @param order Sort order
     * @param first First result index
     * @param last Last result index
     * @param siteId The site ID
     * @return List of LtiContentBean objects
     */
    List<LtiContentBean> getContentBeans(String search, String order, int first, int last, String siteId);

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
    LtiToolSiteBean getToolSiteAsBean(Long key, String siteId);

    /**
     * Get a list of LTI tool sites as Beans
     * @param toolId The tool ID
     * @param siteId The site ID
     * @return List of LtiToolSiteBean objects
     */
    List<LtiToolSiteBean> getToolSitesByToolIdAsBeans(String toolId, String siteId);

    /**
     * Returns the optional {@link #LTI_DEPLOYMENT_GROUP} for a tool deployed to the given site,
     * or null when unset or when there is no matching tool-site row.
     * <p>
     * If more than one {@code lti_tool_site} row matches the tool and site (no composite unique
     * is enforced at the schema level), the row with the greatest {@link #LTI_UPDATED_AT} wins;
     * null timestamps are treated as older than any real timestamp, and ties among nulls keep
     * the first matching row.
     *
     * @param toolKey primary key of the LTI tool
     * @param launchSiteId site id where the launch occurs (must match the tool-site row {@link #LTI_SITE_ID})
     */
    String getDeploymentGroupForLaunch(Long toolKey, String launchSiteId);

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
    LtiMembershipsJobBean getMembershipsJobAsBean(String siteId);

    /**
     * Get all LTI memberships jobs as Beans
     * @return List of LtiMembershipsJobBean objects
     */
    List<LtiMembershipsJobBean> getMembershipsJobsAsBeans();

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
    Object insertTool(LtiToolBean toolBean, String siteId);

    /**
     * Insert a new LTI content using Bean
     * @param contentBean The content data as Bean
     * @param siteId The site ID
     * @return The ID of the inserted content
     */
    Object insertContent(LtiContentBean contentBean, String siteId);

    /**
     * Update an existing LTI tool using Bean
     * @param key The tool ID
     * @param toolBean The tool data as Bean
     * @param siteId The site ID
     * @return The result of the update operation
     */
    Object updateToolAsAdmin(Long key, LtiToolBean toolBean, String siteId);

    /**
     * Update an existing LTI content using Bean
     * @param key The content ID
     * @param contentBean The content data as Bean
     * @param siteId The site ID
     * @return The result of the update operation
     */
    Object updateContent(Long key, LtiContentBean contentBean, String siteId);

    // ------------------------------------------------------------------------------------
    // BEAN UTILITY METHODS
    // ------------------------------------------------------------------------------------
    // These methods provide utility functions that work with Bean objects.

    /**
     * Get the launch URL for a content item using Bean
     * @param contentBean The content data as Bean
     * @return The launch URL
     */
    String getContentLaunch(LtiContentBean contentBean);

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
    String formOutput(LtiToolBean toolBean, String fieldinfo);

    /**
     * Generate form output for a tool Bean
     * @param toolBean The tool data as Bean
     * @param formDefinition The form definition array
     * @return The formatted output
     */
    String formOutput(LtiToolBean toolBean, String[] formDefinition);

    /**
     * Generate form output for a content Bean
     * @param contentBean The content data as Bean
     * @param fieldinfo The field information
     * @return The formatted output
     */
    String formOutput(LtiContentBean contentBean, String fieldinfo);

    /**
     * Generate form output for a content Bean
     * @param contentBean The content data as Bean
     * @param formDefinition The form definition array
     * @return The formatted output
     */
    String formOutput(LtiContentBean contentBean, String[] formDefinition);

    /**
     * Generate form output for a tool site Bean
     * @param toolSiteBean The tool site data as Bean
     * @param fieldinfo The field information
     * @return The formatted output
     */
    String formOutput(LtiToolSiteBean toolSiteBean, String fieldinfo);

    /**
     * Generate form output for a tool site Bean
     * @param toolSiteBean The tool site data as Bean
     * @param formDefinition The form definition array
     * @return The formatted output
     */
    String formOutput(LtiToolSiteBean toolSiteBean, String[] formDefinition);

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
    String formInput(LtiToolBean toolBean, String fieldinfo);

    /**
     * Generate form input for a tool Bean
     * @param toolBean The tool data as Bean
     * @param formDefinition The form definition array
     * @return The formatted input
     */
    String formInput(LtiToolBean toolBean, String[] formDefinition);

    /**
     * Generate form input for a content Bean
     * @param contentBean The content data as Bean
     * @param fieldinfo The field information
     * @return The formatted input
     */
    String formInput(LtiContentBean contentBean, String fieldinfo);

    /**
     * Generate form input for a content Bean
     * @param contentBean The content data as Bean
     * @param formDefinition The form definition array
     * @return The formatted input
     */
    String formInput(LtiContentBean contentBean, String[] formDefinition);

    /**
     * Generate form input for a tool site Bean
     * @param toolSiteBean The tool site data as Bean
     * @param fieldinfo The field information
     * @return The formatted input
     */
    String formInput(LtiToolSiteBean toolSiteBean, String fieldinfo);

    /**
     * Generate form input for a tool site Bean
     * @param toolSiteBean The tool site data as Bean
     * @param formDefinition The form definition array
     * @return The formatted input
     */
    String formInput(LtiToolSiteBean toolSiteBean, String[] formDefinition);
}
