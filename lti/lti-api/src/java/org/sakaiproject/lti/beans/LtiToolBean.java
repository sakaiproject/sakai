/*
 *
 * $URL$
 * $Id$
 *
 * Copyright (c) 2025 Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.lti.beans;

import org.sakaiproject.lti.foorm.FoormBaseBean;
import org.sakaiproject.lti.foorm.FoormField;
import org.sakaiproject.lti.foorm.FoormType;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.lti.api.LTIService;

/**
 * Transfer object for LTI Tools.
 * Based on the TOOL_MODEL from LTIService.
 * <p>
 * Includes <em>non-persisted</em> fields that round-trip between bean and map in
 * {@link #of(java.util.Map)} and {@link #asMap()} but are never stored: launch-flow fields
 * ({@link #toolState}, {@link #platformState}, {@link #relaunchUrl}, {@link #origSiteIdNull}) and
 * archive-only {@link #sakaiToolChecksum}.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@ToString(exclude = {"secret", "lti13AutoToken", "toolState", "platformState", "relaunchUrl", "origSiteIdNull", "sakaiToolChecksum"})
public class LtiToolBean extends FoormBaseBean {

    @Override
    protected Set<String> getExcludedArchiveFieldNames() {
        return Set.of(LTIService.SAKAI_TOOL_CHECKSUM);
    }

    @FoormField(value = "id", type = FoormType.KEY, archive = true)
    public Long id;
    @FoormField(value = "SITE_ID", type = FoormType.TEXT, maxlength = 99, role = "admin")
    public String siteId;
    @FoormField(value = "title", type = FoormType.TEXT, label = "bl_title", required = true, maxlength = 1024, archive = true)
    public String title;
    @FoormField(value = "description", type = FoormType.TEXTAREA, label = "bl_description", maxlength = 4096, archive = true)
    public String description;
    @FoormField(value = "status", type = FoormType.RADIO, label = "bl_status", choices = {"enable", "disable"})
    public String status;
    @FoormField(value = "visible", type = FoormType.RADIO, label = "bl_visible", choices = {"visible", "stealth"}, role = "admin")
    public String visible;
    @FoormField(value = "deployment_id", type = FoormType.INTEGER, hidden = true, archive = true)
    public Long deploymentId;
    @FoormField(value = "launch", type = FoormType.URL, label = "bl_launch", maxlength = 1024, required = true, archive = true)
    public String launch;
    @FoormField(value = "newpage", type = FoormType.RADIO, label = "bl_newpage", choices = {"off", "on", "content"}, archive = true)
    public Integer newpage;
    @FoormField(value = "frameheight", type = FoormType.INTEGER, label = "bl_frameheight", archive = true)
    public Integer frameheight;
    @FoormField(value = "fa_icon", type = FoormType.TEXT, label = "bl_fa_icon", maxlength = 1024, archive = true)
    public String faIcon;
    
    @FoormField(value = "pl_launch", type = FoormType.CHECKBOX, label = "bl_pl_launch", archive = true)
    public Boolean plLaunch;
    @FoormField(value = "pl_linkselection", type = FoormType.CHECKBOX, label = "bl_pl_linkselection", archive = true)
    public Boolean plLinkselection;
    @FoormField(value = "pl_contextlaunch", type = FoormType.CHECKBOX, label = "bl_pl_contextlaunch", hidden = true)
    public Boolean plContextlaunch;

    @FoormField(value = "pl_lessonsselection", type = FoormType.CHECKBOX, label = "bl_pl_lessonsselection", archive = true)
    public Boolean plLessonsselection;
    @FoormField(value = "pl_contenteditor", type = FoormType.CHECKBOX, label = "bl_pl_contenteditor", archive = true)
    public Boolean plContenteditor;
    @FoormField(value = "pl_assessmentselection", type = FoormType.CHECKBOX, label = "bl_pl_assessmentselection", archive = true)
    public Boolean plAssessmentselection;
    @FoormField(value = "pl_coursenav", type = FoormType.CHECKBOX, label = "bl_pl_coursenav", archive = true)
    public Boolean plCoursenav;
    @FoormField(value = "pl_importitem", type = FoormType.CHECKBOX, label = "bl_pl_importitem", role = "admin", archive = true)
    public Boolean plImportitem;
    @FoormField(value = "pl_fileitem", type = FoormType.CHECKBOX, label = "bl_pl_fileitem", role = "admin", hidden = true, archive = true)
    public Boolean plFileitem;
    
    @FoormField(value = "sendname", type = FoormType.CHECKBOX, label = "bl_sendname", archive = true)
    public Boolean sendname;
    @FoormField(value = "sendemailaddr", type = FoormType.CHECKBOX, label = "bl_sendemailaddr", archive = true)
    public Boolean sendemailaddr;
    @FoormField(value = "pl_privacy", type = FoormType.CHECKBOX, label = "bl_pl_privacy", role = "admin")
    public Boolean plPrivacy;

    @FoormField(value = "allowoutcomes", type = FoormType.CHECKBOX, label = "bl_allowoutcomes", archive = true)
    public Boolean allowoutcomes;
    @FoormField(value = "allowlineitems", type = FoormType.CHECKBOX, label = "bl_allowlineitems", archive = true)
    public Boolean allowlineitems;
    @FoormField(value = "allowroster", type = FoormType.CHECKBOX, label = "bl_allowroster", archive = true)
    public Boolean allowroster;
    
    @FoormField(value = "debug", type = FoormType.RADIO, label = "bl_debug", choices = {"off", "on", "content"})
    public Integer debug;
    @FoormField(value = "siteinfoconfig", type = FoormType.RADIO, label = "bl_siteinfoconfig", advanced = true, choices = {"bypass", "config"})
    public String siteinfoconfig;
    @FoormField(value = "splash", type = FoormType.TEXTAREA, label = "bl_splash", rows = 5, cols = 25, maxlength = 16384)
    public String splash;
    @FoormField(value = "custom", type = FoormType.TEXTAREA, label = "bl_custom", rows = 5, cols = 25, maxlength = 16384, archive = true)
    public String custom;
    @FoormField(value = "rolemap", type = FoormType.TEXTAREA, label = "bl_rolemap", rows = 5, cols = 25, maxlength = 16384, role = "admin", archive = true)
    public String rolemap;
    @FoormField(value = "lti13", type = FoormType.RADIO, label = "bl_lti13", choices = {"off", "on", "both"}, role = "admin", archive = true)
    public Integer lti13;
    
    @FoormField(value = "lti13_tool_keyset", type = FoormType.TEXT, label = "bl_lti13_tool_keyset", maxlength = 1024, role = "admin")
    public String lti13ToolKeyset;
    @FoormField(value = "lti13_oidc_endpoint", type = FoormType.TEXT, label = "bl_lti13_oidc_endpoint", maxlength = 1024, role = "admin")
    public String lti13OidcEndpoint;
    @FoormField(value = "lti13_oidc_redirect", type = FoormType.TEXT, label = "bl_lti13_oidc_redirect", maxlength = 1024, role = "admin")
    public String lti13OidcRedirect;

    @FoormField(value = "lti13_lms_issuer", type = FoormType.TEXT, label = "bl_lti13_lms_issuer", readonly = true, persist = false, maxlength = 1024, role = "admin")
    public String lti13LmsIssuer;
    @FoormField(value = "lti13_client_id", type = FoormType.TEXT, label = "bl_lti13_client_id", readonly = true, maxlength = 1024, role = "admin")
    public String lti13ClientId;
    @FoormField(value = "lti13_lms_deployment_id", type = FoormType.TEXT, label = "bl_lti13_lms_deployment_id", readonly = true, maxlength = 1024, role = "admin")
    public String lti13LmsDeploymentId;
    @FoormField(value = "lti13_lms_keyset", type = FoormType.TEXT, label = "bl_lti13_lms_keyset", readonly = true, persist = false, maxlength = 1024, role = "admin")
    public String lti13LmsKeyset;
    @FoormField(value = "lti13_lms_endpoint", type = FoormType.TEXT, label = "bl_lti13_lms_endpoint", readonly = true, persist = false, maxlength = 1024, role = "admin")
    public String lti13LmsEndpoint;
    @FoormField(value = "lti13_lms_token", type = FoormType.TEXT, label = "bl_lti13_lms_token", readonly = true, persist = false, maxlength = 1024, role = "admin")
    public String lti13LmsToken;
    
    @FoormField(value = "consumerkey", type = FoormType.TEXT, label = "bl_consumerkey", maxlength = 1024)
    public String consumerkey;
    @FoormField(value = "secret", type = FoormType.TEXT, label = "bl_secret", maxlength = 1024)
    public String secret;
    @FoormField(value = "xmlimport", type = FoormType.TEXTAREA, hidden = true)
    public String xmlimport;
    @FoormField(value = "lti13_auto_token", type = FoormType.TEXT, hidden = true, maxlength = 1024)
    public String lti13AutoToken;
    @FoormField(value = "lti13_auto_state", type = FoormType.INTEGER, hidden = true)
    public Integer lti13AutoState;
    @FoormField(value = "lti13_auto_registration", type = FoormType.TEXTAREA, hidden = true)
    public String lti13AutoRegistration;

    @FoormField(value = "created_at", type = FoormType.AUTODATE)
    public Date createdAt;
    @FoormField(value = "updated_at", type = FoormType.AUTODATE)
    public Date updatedAt;

    @FoormField(value = "lti_content_count", type = FoormType.INTEGER)
    public Long ltiContentCount;
    @FoormField(value = "lti_site_count", type = FoormType.INTEGER)
    public Long ltiSiteCount;

    /**
     * Launch-flow only fields.
     * <p>
     * These fields are <strong>not persisted</strong> to the tool record. They round-trip between
     * bean and map in {@link #of(java.util.Map)} and {@link #asMap()}, but are never stored in the
     * database. They are used only during the launch request/response flow. The platform may
     * receive them as request parameters (e.g. when building a Content Item or LTI 1.1 launch URL)
     * and should pass them through so they can be sent back to the tool in the launch payload,
     * allowing the tool to restore context or continue a flow.
     * </p>
     * <ul>
     *   <li><strong>toolState</strong> – Opaque value set by the tool (e.g. in a Content Item
     *       request) and returned in the launch so the tool can resume state.</li>
     *   <li><strong>platformState</strong> – Opaque value set by the platform and returned in the
     *       launch for platform-specific state.</li>
     *   <li><strong>relaunchUrl</strong> – URL the platform may use for a "relaunch" or "open in new
     *       window" action; returned in the launch so the tool can offer a consistent relaunch
     *       link.</li>
     *   <li><strong>origSiteIdNull</strong> – Internal: set to {@code "true"} when the tool’s
     *       stored site id was null before the launch context was applied; used by the LTI 1.3
     *       OIDC redirect flow.</li>
     *   <li><strong>sakaiToolChecksum</strong> – Computed checksum for tool import/export archives;
     *       round-trips in {@link #asMap()} and {@link #of(java.util.Map)} but is never stored.</li>
     * </ul>
     */
    @FoormField(value = "tool_state", type = FoormType.TEXT)
    public String toolState;
    @FoormField(value = "platform_state", type = FoormType.TEXT)
    public String platformState;
    @FoormField(value = "relaunch_url", type = FoormType.TEXT)
    public String relaunchUrl;
    @FoormField(value = "orig_site_id_null", type = FoormType.TEXT)
    public String origSiteIdNull;
    @FoormField(value = "sakai_tool_checksum", type = FoormType.TEXT, maxlength = 99, hidden = true, persist = false, archive = true)
    public String sakaiToolChecksum;

    /**
     * Creates an LtiToolBean instance from a Map<String, Object>.
     * 
     * @param map The map containing LTI tool data
     * @return LtiToolBean instance populated from the map
     */
    public static LtiToolBean of(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        
        LtiToolBean tool = new LtiToolBean();
        
        // Core fields
        tool.setId(getLongValue(map, LTIService.LTI_ID));
        tool.setSiteId(getStringValue(map, LTIService.LTI_SITE_ID));
        tool.setTitle(getStringValue(map, LTIService.LTI_TITLE));
        tool.setDescription(getStringValue(map, LTIService.LTI_DESCRIPTION));
        tool.setStatus(getStringValue(map, LTIService.LTI_STATUS));
        tool.setVisible(getStringValue(map, LTIService.LTI_VISIBLE));
        tool.setDeploymentId(getLongValue(map, "deployment_id"));
        tool.setLaunch(getStringValue(map, LTIService.LTI_LAUNCH));
        tool.setNewpage(getThreeStateValue(map, "newpage", "newpage"));
        tool.setFrameheight(getIntegerValue(map, LTIService.LTI_FRAMEHEIGHT));
        tool.setFaIcon(getStringValue(map, LTIService.LTI_FA_ICON));
        
        // Message Types (pl_ prefix for backwards compatibility)
        tool.setPlLaunch(getBooleanValue(map, "pl_launch"));
        tool.setPlLinkselection(getBooleanValue(map, "pl_linkselection"));
        tool.setPlContextlaunch(getBooleanValue(map, "pl_contextlaunch"));
        
        // Placements
        tool.setPlLessonsselection(getBooleanValue(map, "pl_lessonsselection"));
        tool.setPlContenteditor(getBooleanValue(map, "pl_contenteditor"));
        tool.setPlAssessmentselection(getBooleanValue(map, "pl_assessmentselection"));
        tool.setPlCoursenav(getBooleanValue(map, "pl_coursenav"));
        tool.setPlImportitem(getBooleanValue(map, "pl_importitem"));
        tool.setPlFileitem(getBooleanValue(map, "pl_fileitem"));
        
        // Privacy
        tool.setSendname(getBooleanValue(map, LTIService.LTI_SENDNAME));
        tool.setSendemailaddr(getBooleanValue(map, LTIService.LTI_SENDEMAILADDR));
        tool.setPlPrivacy(getBooleanValue(map, "pl_privacy"));
        
        // Services
        tool.setAllowoutcomes(getBooleanValue(map, LTIService.LTI_ALLOWOUTCOMES));
        tool.setAllowlineitems(getBooleanValue(map, LTIService.LTI_ALLOWLINEITEMS));
        tool.setAllowroster(getBooleanValue(map, LTIService.LTI_ALLOWROSTER));
        
        // Configuration
        tool.setDebug(getThreeStateValue(map, LTIService.LTI_DEBUG, "debug"));
        tool.setSiteinfoconfig(getStringValue(map, "siteinfoconfig"));
        tool.setSplash(getStringValue(map, LTIService.LTI_SPLASH));
        tool.setCustom(getStringValue(map, LTIService.LTI_CUSTOM));
        tool.setRolemap(getStringValue(map, LTIService.LTI_ROLEMAP));
        tool.setLti13(getIntegerValue(map, LTIService.LTI13));
        
        // LTI 1.3 security values from the tool
        tool.setLti13ToolKeyset(getStringValue(map, "lti13_tool_keyset"));
        tool.setLti13OidcEndpoint(getStringValue(map, "lti13_oidc_endpoint"));
        tool.setLti13OidcRedirect(getStringValue(map, "lti13_oidc_redirect"));
        
        // LTI 1.3 security values from the LMS
        tool.setLti13LmsIssuer(getStringValue(map, "lti13_lms_issuer"));
        tool.setLti13ClientId(getStringValue(map, "lti13_client_id"));
        tool.setLti13LmsDeploymentId(getStringValue(map, "lti13_lms_deployment_id"));
        tool.setLti13LmsKeyset(getStringValue(map, "lti13_lms_keyset"));
        tool.setLti13LmsEndpoint(getStringValue(map, "lti13_lms_endpoint"));
        tool.setLti13LmsToken(getStringValue(map, "lti13_lms_token"));
        
        // LTI 1.1 security arrangement
        tool.setConsumerkey(getStringValue(map, LTIService.LTI_CONSUMERKEY));
        tool.setSecret(getStringValue(map, LTIService.LTI_SECRET));
        tool.setXmlimport(getStringValue(map, LTIService.LTI_XMLIMPORT));
        tool.setLti13AutoToken(getStringValue(map, "lti13_auto_token"));
        tool.setLti13AutoState(getIntegerValue(map, "lti13_auto_state"));
        tool.setLti13AutoRegistration(getStringValue(map, "lti13_auto_registration"));
        tool.setSakaiToolChecksum(getStringValue(map, "sakai_tool_checksum"));
        
        // Timestamps
        tool.setCreatedAt(getDateValue(map, LTIService.LTI_CREATED_AT));
        tool.setUpdatedAt(getDateValue(map, LTIService.LTI_UPDATED_AT));
        
        // Live attributes - computed fields that may be present in Map data
        tool.setLtiContentCount(getLongValue(map, "lti_content_count"));
        tool.setLtiSiteCount(getLongValue(map, "lti_site_count"));
        
        // Launch-flow only (not persisted)
        tool.setToolState(getStringValue(map, "tool_state"));
        tool.setPlatformState(getStringValue(map, "platform_state"));
        tool.setRelaunchUrl(getStringValue(map, "relaunch_url"));
        tool.setOrigSiteIdNull(getStringValue(map, "orig_site_id_null"));
        
        return tool;
    }

    /**
     * Converts this LtiToolBean instance to a Map<String, Object>.
     * 
     * @return Map representation of this LtiToolBean
     */
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();
        
        // Core fields
        putIfNotNull(map, LTIService.LTI_ID, id);
        putIfNotNull(map, LTIService.LTI_SITE_ID, siteId);
        putIfNotNull(map, LTIService.LTI_TITLE, title);
        putIfNotNull(map, LTIService.LTI_DESCRIPTION, description);
        putIfNotNull(map, LTIService.LTI_STATUS, status);
        putIfNotNull(map, LTIService.LTI_VISIBLE, visible);
        putIfNotNull(map, "deployment_id", deploymentId);
        putIfNotNull(map, LTIService.LTI_LAUNCH, launch);
        putThreeStateIfNotNull(map, "newpage", newpage, "newpage");
        putIfNotNull(map, LTIService.LTI_FRAMEHEIGHT, frameheight);
        putIfNotNull(map, LTIService.LTI_FA_ICON, faIcon);
        
        // Message Types
        putBooleanAsInteger(map, "pl_launch", plLaunch);
        putBooleanAsInteger(map, "pl_linkselection", plLinkselection);
        putBooleanAsInteger(map, "pl_contextlaunch", plContextlaunch);
        
        // Placements
        putBooleanAsInteger(map, "pl_lessonsselection", plLessonsselection);
        putBooleanAsInteger(map, "pl_contenteditor", plContenteditor);
        putBooleanAsInteger(map, "pl_assessmentselection", plAssessmentselection);
        putBooleanAsInteger(map, "pl_coursenav", plCoursenav);
        putBooleanAsInteger(map, "pl_importitem", plImportitem);
        putBooleanAsInteger(map, "pl_fileitem", plFileitem);
        
        // Privacy
        putBooleanAsInteger(map, LTIService.LTI_SENDNAME, sendname);
        putBooleanAsInteger(map, LTIService.LTI_SENDEMAILADDR, sendemailaddr);
        putBooleanAsInteger(map, "pl_privacy", plPrivacy);
        
        // Services
        putBooleanAsInteger(map, LTIService.LTI_ALLOWOUTCOMES, allowoutcomes);
        putBooleanAsInteger(map, LTIService.LTI_ALLOWLINEITEMS, allowlineitems);
        putBooleanAsInteger(map, LTIService.LTI_ALLOWROSTER, allowroster);
        
        // Configuration
        putThreeStateIfNotNull(map, LTIService.LTI_DEBUG, debug, "debug");
        putIfNotNull(map, "siteinfoconfig", siteinfoconfig);
        putIfNotNull(map, LTIService.LTI_SPLASH, splash);
        putIfNotNull(map, LTIService.LTI_CUSTOM, custom);
        putIfNotNull(map, LTIService.LTI_ROLEMAP, rolemap);
        putIfNotNull(map, LTIService.LTI13, lti13);
        
        // LTI 1.3 security values from the tool
        putIfNotNull(map, "lti13_tool_keyset", lti13ToolKeyset);
        putIfNotNull(map, "lti13_oidc_endpoint", lti13OidcEndpoint);
        putIfNotNull(map, "lti13_oidc_redirect", lti13OidcRedirect);
        
        // LTI 1.3 security values from the LMS
        putIfNotNull(map, "lti13_lms_issuer", lti13LmsIssuer);
        putIfNotNull(map, "lti13_client_id", lti13ClientId);
        putIfNotNull(map, "lti13_lms_deployment_id", lti13LmsDeploymentId);
        putIfNotNull(map, "lti13_lms_keyset", lti13LmsKeyset);
        putIfNotNull(map, "lti13_lms_endpoint", lti13LmsEndpoint);
        putIfNotNull(map, "lti13_lms_token", lti13LmsToken);
        
        // LTI 1.1 security arrangement
        putIfNotNull(map, LTIService.LTI_CONSUMERKEY, consumerkey);
        putIfNotNull(map, LTIService.LTI_SECRET, secret);
        putIfNotNull(map, LTIService.LTI_XMLIMPORT, xmlimport);
        putIfNotNull(map, "lti13_auto_token", lti13AutoToken);
        putIfNotNull(map, "lti13_auto_state", lti13AutoState);
        putIfNotNull(map, "lti13_auto_registration", lti13AutoRegistration);
        
        // Timestamps
        putIfNotNull(map, LTIService.LTI_CREATED_AT, createdAt);
        putIfNotNull(map, LTIService.LTI_UPDATED_AT, updatedAt);
        
        // Live attributes - computed fields that may be present in Map data
        putIfNotNull(map, "lti_content_count", ltiContentCount);
        putIfNotNull(map, "lti_site_count", ltiSiteCount);
        
        // Non-persisted
        putIfNotNull(map, "tool_state", toolState);
        putIfNotNull(map, "platform_state", platformState);
        putIfNotNull(map, "relaunch_url", relaunchUrl);
        putIfNotNull(map, "orig_site_id_null", origSiteIdNull);
        putIfNotNull(map, "sakai_tool_checksum", sakaiToolChecksum);
        
        return map;
    }

    /**
     * Creates an LtiToolBean from an archive XML element.
     * Uses the same format as {@link #toXml} (child elements per archivable field).
     * For use in archive/import flows.
     *
     * @param element the sakai-lti-tool element (or equivalent)
     * @return new bean populated from the element, or null if element is null
     */
    public static LtiToolBean fromXml(Element element) {
        if (element == null) {
            return null;
        }
        LtiToolBean bean = new LtiToolBean();
        bean.populateFromArchiveElement(element);
        return bean;
    }

    /**
     * Serializes this bean to an XML element in archive format.
     * Uses the same structure as the existing archive process (child elements per archivable field).
     * For use in archive/export flows. The checksum is not included; add it separately if needed
     * (e.g. via SakaiLTIUtil.archiveToolBean).
     *
     * @param doc   the document to create elements in
     * @param stack parent stack; if null or empty, the element is appended to doc; otherwise to stack.peek()
     * @return the created element, or null if doc is null
     */
    public Element toXml(Document doc, Stack<Element> stack) {
        if (doc == null) {
            return null;
        }
        Element el = toArchiveElement(doc, LTIService.ARCHIVE_LTI_TOOL_TAG);
        if (el == null) {
            return null;
        }
        if (stack == null || stack.isEmpty()) {
            doc.appendChild(el);
        } else {
            stack.peek().appendChild(el);
        }
        if (stack != null) {
            stack.push(el);
            stack.pop();
        }
        return el;
    }

}
