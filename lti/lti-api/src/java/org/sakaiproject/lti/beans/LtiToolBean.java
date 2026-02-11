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

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import org.sakaiproject.lti.api.LTIService;

/**
 * Transfer object for LTI Tools.
 * Based on the TOOL_MODEL from LTIService.
 * <p>
 * Includes a few <em>launch-flow only</em> fields ({@link #toolState}, {@link #platformState},
 * {@link #relaunchUrl}, {@link #origSiteIdNull}) that are not persisted. They round-trip between
 * bean and map in {@link #of(java.util.Map)} and {@link #asMap()} but are never stored; they are
 * set from the request during launch and passed through so they can be included in the outbound
 * launch.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@ToString(exclude = {"secret", "lti13AutoToken"})
public class LtiToolBean extends LTIBaseBean {

    // Core fields from TOOL_MODEL
    public Long id;                    // TOOL_MODEL: "id:key:archive=true"
    public String siteId;              // TOOL_MODEL: "SITE_ID:text:maxlength=99:role=admin"
    public String title;               // TOOL_MODEL: "title:text:label=bl_title:required=true:maxlength=1024:archive=true"
    public String description;         // TOOL_MODEL: "description:textarea:label=bl_description:maxlength=4096:archive=true"
    public String status;              // TOOL_MODEL: "status:radio:label=bl_status:choices=enable,disable"
    public String visible;             // TOOL_MODEL: "visible:radio:label=bl_visible:choices=visible,stealth:role=admin"
    public Long deploymentId;          // TOOL_MODEL: "deployment_id:integer:hidden=true:archive=true"
    public String launch;              // TOOL_MODEL: "launch:url:label=bl_launch:maxlength=1024:required=true:archive=true"
    public Integer newpage;            // TOOL_MODEL: "newpage:radio:label=bl_newpage:choices=off,on,content:archive=true"
    public Integer frameheight;        // TOOL_MODEL: "frameheight:integer:label=bl_frameheight:archive=true"
    public String faIcon;              // TOOL_MODEL: "fa_icon:text:label=bl_fa_icon:maxlength=1024:archive=true"
    
    // Message Types (pl_ prefix for backwards compatibility) from TOOL_MODEL
    public Boolean plLaunch;           // TOOL_MODEL: "pl_launch:checkbox:label=bl_pl_launch:archive=true"
    public Boolean plLinkselection;    // TOOL_MODEL: "pl_linkselection:checkbox:label=bl_pl_linkselection:archive=true"
    public Boolean plContextlaunch;    // TOOL_MODEL: "pl_contextlaunch:checkbox:label=bl_pl_contextlaunch:hidden=true"
    
    // Placements from TOOL_MODEL
    public Boolean plLessonsselection;     // TOOL_MODEL: "pl_lessonsselection:checkbox:label=bl_pl_lessonsselection:archive=true"
    public Boolean plContenteditor;        // TOOL_MODEL: "pl_contenteditor:checkbox:label=bl_pl_contenteditor:archive=true"
    public Boolean plAssessmentselection;  // TOOL_MODEL: "pl_assessmentselection:checkbox:label=bl_pl_assessmentselection:archive=true"
    public Boolean plCoursenav;            // TOOL_MODEL: "pl_coursenav:checkbox:label=bl_pl_coursenav:archive=true"
    public Boolean plImportitem;           // TOOL_MODEL: "pl_importitem:checkbox:label=bl_pl_importitem:role=admin:archive=true"
    public Boolean plFileitem;             // TOOL_MODEL: "pl_fileitem:checkbox:label=bl_pl_fileitem:role=admin:hidden=true:archive=true"
    
    // Privacy from TOOL_MODEL
    public Boolean sendname;           // TOOL_MODEL: "sendname:checkbox:label=bl_sendname:archive=true"
    public Boolean sendemailaddr;      // TOOL_MODEL: "sendemailaddr:checkbox:label=bl_sendemailaddr:archive=true"
    public Boolean plPrivacy;          // TOOL_MODEL: "pl_privacy:checkbox:label=bl_pl_privacy:role=admin"
    
    // Services from TOOL_MODEL
    public Boolean allowoutcomes;      // TOOL_MODEL: "allowoutcomes:checkbox:label=bl_allowoutcomes:archive=true"
    public Boolean allowlineitems;     // TOOL_MODEL: "allowlineitems:checkbox:label=bl_allowlineitems:archive=true"
    public Boolean allowroster;        // TOOL_MODEL: "allowroster:checkbox:label=bl_allowroster:archive=true"
    
    // Configuration fields from TOOL_MODEL
    public Integer debug;              // TOOL_MODEL: "debug:radio:label=bl_debug:choices=off,on,content"
    public String siteinfoconfig;      // TOOL_MODEL: "siteinfoconfig:radio:label=bl_siteinfoconfig:advanced:choices=bypass,config"
    public String splash;              // TOOL_MODEL: "splash:textarea:label=bl_splash:rows=5:cols=25:maxlength=16384"
    public String custom;              // TOOL_MODEL: "custom:textarea:label=bl_custom:rows=5:cols=25:maxlength=16384:archive=true"
    public String rolemap;             // TOOL_MODEL: "rolemap:textarea:label=bl_rolemap:rows=5:cols=25:maxlength=16384:role=admin:archive=true"
    public Integer lti13;              // TOOL_MODEL: "lti13:radio:label=bl_lti13:choices=off,on,both:role=admin:archive=true"
    
    // LTI 1.3 security values from the tool from TOOL_MODEL
    public String lti13ToolKeyset;     // TOOL_MODEL: "lti13_tool_keyset:text:label=bl_lti13_tool_keyset:maxlength=1024:role=admin"
    public String lti13OidcEndpoint;   // TOOL_MODEL: "lti13_oidc_endpoint:text:label=bl_lti13_oidc_endpoint:maxlength=1024:role=admin"
    public String lti13OidcRedirect;   // TOOL_MODEL: "lti13_oidc_redirect:text:label=bl_lti13_oidc_redirect:maxlength=1024:role=admin"
    
    // LTI 1.3 security values from the LMS from TOOL_MODEL
    public String lti13LmsIssuer;      // TOOL_MODEL: "lti13_lms_issuer:text:label=bl_lti13_lms_issuer:readonly=true:persist=false:maxlength=1024:role=admin"
    public String lti13ClientId;       // TOOL_MODEL: "lti13_client_id:text:label=bl_lti13_client_id:readonly=true:maxlength=1024:role=admin"
    public String lti13LmsDeploymentId; // TOOL_MODEL: "lti13_lms_deployment_id:text:label=bl_lti13_lms_deployment_id:readonly=true:maxlength=1024:role=admin"
    public String lti13LmsKeyset;      // TOOL_MODEL: "lti13_lms_keyset:text:label=bl_lti13_lms_keyset:readonly=true:persist=false:maxlength=1024:role=admin"
    public String lti13LmsEndpoint;    // TOOL_MODEL: "lti13_lms_endpoint:text:label=bl_lti13_lms_endpoint:readonly=true:persist=false:maxlength=1024:role=admin"
    public String lti13LmsToken;       // TOOL_MODEL: "lti13_lms_token:text:label=bl_lti13_lms_token:readonly=true:persist=false:maxlength=1024:role=admin"
    
    // LTI 1.1 security arrangement from TOOL_MODEL
    public String consumerkey;         // TOOL_MODEL: "consumerkey:text:label=bl_consumerkey:maxlength=1024"
    public String secret;              // TOOL_MODEL: "secret:text:label=bl_secret:maxlength=1024"
    public String xmlimport;           // TOOL_MODEL: "xmlimport:textarea:hidden=true:maxlength=1M"
    public String lti13AutoToken;      // TOOL_MODEL: "lti13_auto_token:text:hidden=true:maxlength=1024"
    public Integer lti13AutoState;     // TOOL_MODEL: "lti13_auto_state:integer:hidden=true"
    public String lti13AutoRegistration; // TOOL_MODEL: "lti13_auto_registration:textarea:hidden=true:maxlength=1M"
    public String sakaiToolChecksum;   // TOOL_MODEL: "sakai_tool_checksum:text:maxlength=99:hidden=true:persist=false:archive=true"
    
    // Timestamps from TOOL_MODEL
    public Date createdAt;             // TOOL_MODEL: "created_at:autodate"
    public Date updatedAt;             // TOOL_MODEL: "updated_at:autodate"
    
    // Live attributes - computed fields that may be present in Map data
    public Long ltiContentCount;       // Live attribute: "lti_content_count" from database joins
    public Long ltiSiteCount;          // Live attribute: "lti_site_count" from database joins

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
     * </ul>
     */
    public String toolState;          // "tool_state" in launch
    public String platformState;      // "platform_state" in launch
    public String relaunchUrl;        // "relaunch_url" in launch
    public String origSiteIdNull;     // "orig_site_id_null" (internal, LTI 1.3 redirect)

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
        putIfNotNull(map, "sakai_tool_checksum", sakaiToolChecksum);
        
        // Timestamps
        putIfNotNull(map, LTIService.LTI_CREATED_AT, createdAt);
        putIfNotNull(map, LTIService.LTI_UPDATED_AT, updatedAt);
        
        // Live attributes - computed fields that may be present in Map data
        putIfNotNull(map, "lti_content_count", ltiContentCount);
        putIfNotNull(map, "lti_site_count", ltiSiteCount);
        
        // Launch-flow only (not persisted)
        putIfNotNull(map, "tool_state", toolState);
        putIfNotNull(map, "platform_state", platformState);
        putIfNotNull(map, "relaunch_url", relaunchUrl);
        putIfNotNull(map, "orig_site_id_null", origSiteIdNull);
        
        return map;
    }

}
