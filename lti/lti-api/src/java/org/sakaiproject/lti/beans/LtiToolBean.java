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

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

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
public class LtiToolBean extends LTIBaseBean {

	/** Map from canonical field name to Field for reflective access. */
	private static final Map<String, Field> FIELDS_BY_NAME = buildFieldMap();

	private static Map<String, Field> buildFieldMap() {
		Map<String, Field> map = new ConcurrentHashMap<>();
		for (Field f : LtiToolBean.class.getDeclaredFields()) {
			FoormField ann = f.getAnnotation(FoormField.class);
			if (ann != null) {
				map.put(ann.value(), f);
			}
		}
		return map;
	}

	/**
	 * Returns the value of the property identified by its canonical field name.
	 *
	 * @param fieldName Canonical name from {@link FoormField} (e.g. {@code deployment_id}, {@code SITE_ID})
	 * @return The property value, or null if the field is unknown or its value is null
	 */
	public Object getValueByFieldName(String fieldName) {
		Field f = FIELDS_BY_NAME.get(fieldName);
		if (f == null) {
			return null;
		}
		try {
			return f.get(this);
		} catch (IllegalAccessException e) {
			return null;
		}
	}

    // TOOL_MODEL
    // "id:key:archive=true"
    @FoormField("id") public Long id;
    // "SITE_ID:text:maxlength=99:role=admin"
    @FoormField("SITE_ID") public String siteId;
    // "title:text:label=bl_title:required=true:maxlength=1024:archive=true"
    @FoormField("title") public String title;
    // "description:textarea:label=bl_description:maxlength=4096:archive=true"
    @FoormField("description") public String description;
    // "status:radio:label=bl_status:choices=enable,disable"
    @FoormField("status") public String status;
    // "visible:radio:label=bl_visible:choices=visible,stealth:role=admin"
    @FoormField("visible") public String visible;
    // "deployment_id:integer:hidden=true:archive=true"
    @FoormField("deployment_id") public Long deploymentId;
    // "launch:url:label=bl_launch:maxlength=1024:required=true:archive=true"
    @FoormField("launch") public String launch;
    // "newpage:radio:label=bl_newpage:choices=off,on,content:archive=true"
    @FoormField("newpage") public Integer newpage;
    // "frameheight:integer:label=bl_frameheight:archive=true"
    @FoormField("frameheight") public Integer frameheight;
    // "fa_icon:text:label=bl_fa_icon:maxlength=1024:archive=true"
    @FoormField("fa_icon") public String faIcon;
    
    // "pl_launch:checkbox:label=bl_pl_launch:archive=true"
    @FoormField("pl_launch") public Boolean plLaunch;
    // "pl_linkselection:checkbox:label=bl_pl_linkselection:archive=true"
    @FoormField("pl_linkselection") public Boolean plLinkselection;
    // "pl_contextlaunch:checkbox:label=bl_pl_contextlaunch:hidden=true"
    @FoormField("pl_contextlaunch") public Boolean plContextlaunch;

    // "pl_lessonsselection:checkbox:label=bl_pl_lessonsselection:archive=true"
    @FoormField("pl_lessonsselection") public Boolean plLessonsselection;
    // "pl_contenteditor:checkbox:label=bl_pl_contenteditor:archive=true"
    @FoormField("pl_contenteditor") public Boolean plContenteditor;
    // "pl_assessmentselection:checkbox:label=bl_pl_assessmentselection:archive=true"
    @FoormField("pl_assessmentselection") public Boolean plAssessmentselection;
    // "pl_coursenav:checkbox:label=bl_pl_coursenav:archive=true"
    @FoormField("pl_coursenav") public Boolean plCoursenav;
    // "pl_importitem:checkbox:label=bl_pl_importitem:role=admin:archive=true"
    @FoormField("pl_importitem") public Boolean plImportitem;
    // "pl_fileitem:checkbox:label=bl_pl_fileitem:role=admin:hidden=true:archive=true"
    @FoormField("pl_fileitem") public Boolean plFileitem;
    
    // "sendname:checkbox:label=bl_sendname:archive=true"
    @FoormField("sendname") public Boolean sendname;
    // "sendemailaddr:checkbox:label=bl_sendemailaddr:archive=true"
    @FoormField("sendemailaddr") public Boolean sendemailaddr;
    // "pl_privacy:checkbox:label=bl_pl_privacy:role=admin"
    @FoormField("pl_privacy") public Boolean plPrivacy;

    // "allowoutcomes:checkbox:label=bl_allowoutcomes:archive=true"
    @FoormField("allowoutcomes") public Boolean allowoutcomes;
    // "allowlineitems:checkbox:label=bl_allowlineitems:archive=true"
    @FoormField("allowlineitems") public Boolean allowlineitems;
    // "allowroster:checkbox:label=bl_allowroster:archive=true"
    @FoormField("allowroster") public Boolean allowroster;
    
    // "debug:radio:label=bl_debug:choices=off,on,content"
    @FoormField("debug") public Integer debug;
    // "siteinfoconfig:radio:label=bl_siteinfoconfig:advanced:choices=bypass,config"
    @FoormField("siteinfoconfig") public String siteinfoconfig;
    // "splash:textarea:label=bl_splash:rows=5:cols=25:maxlength=16384"
    @FoormField("splash") public String splash;
    // "custom:textarea:label=bl_custom:rows=5:cols=25:maxlength=16384:archive=true"
    @FoormField("custom") public String custom;
    // "rolemap:textarea:label=bl_rolemap:rows=5:cols=25:maxlength=16384:role=admin:archive=true"
    @FoormField("rolemap") public String rolemap;
    // "lti13:radio:label=bl_lti13:choices=off,on,both:role=admin:archive=true"
    @FoormField("lti13") public Integer lti13;
    
    // "lti13_tool_keyset:text:label=bl_lti13_tool_keyset:maxlength=1024:role=admin"
    @FoormField("lti13_tool_keyset") public String lti13ToolKeyset;
    // "lti13_oidc_endpoint:text:label=bl_lti13_oidc_endpoint:maxlength=1024:role=admin"
    @FoormField("lti13_oidc_endpoint") public String lti13OidcEndpoint;
    // "lti13_oidc_redirect:text:label=bl_lti13_oidc_redirect:maxlength=1024:role=admin"
    @FoormField("lti13_oidc_redirect") public String lti13OidcRedirect;

    // "lti13_lms_issuer:text:label=bl_lti13_lms_issuer:readonly=true:persist=false:maxlength=1024:role=admin"
    @FoormField("lti13_lms_issuer") public String lti13LmsIssuer;
    // "lti13_client_id:text:label=bl_lti13_client_id:readonly=true:maxlength=1024:role=admin"
    @FoormField("lti13_client_id") public String lti13ClientId;
    // "lti13_lms_deployment_id:text:label=bl_lti13_lms_deployment_id:readonly=true:maxlength=1024:role=admin"
    @FoormField("lti13_lms_deployment_id") public String lti13LmsDeploymentId;
    // "lti13_lms_keyset:text:label=bl_lti13_lms_keyset:readonly=true:persist=false:maxlength=1024:role=admin"
    @FoormField("lti13_lms_keyset") public String lti13LmsKeyset;
    // "lti13_lms_endpoint:text:label=bl_lti13_lms_endpoint:readonly=true:persist=false:maxlength=1024:role=admin"
    @FoormField("lti13_lms_endpoint") public String lti13LmsEndpoint;
    // "lti13_lms_token:text:label=bl_lti13_lms_token:readonly=true:persist=false:maxlength=1024:role=admin"
    @FoormField("lti13_lms_token") public String lti13LmsToken;
    
    // "consumerkey:text:label=bl_consumerkey:maxlength=1024"
    @FoormField("consumerkey") public String consumerkey;
    // "secret:text:label=bl_secret:maxlength=1024"
    @FoormField("secret") public String secret;
    // "xmlimport:textarea:hidden=true:maxlength=1M"
    @FoormField("xmlimport") public String xmlimport;
    // "lti13_auto_token:text:hidden=true:maxlength=1024"
    @FoormField("lti13_auto_token") public String lti13AutoToken;
    // "lti13_auto_state:integer:hidden=true"
    @FoormField("lti13_auto_state") public Integer lti13AutoState;
    // "lti13_auto_registration:textarea:hidden=true:maxlength=1M"
    @FoormField("lti13_auto_registration") public String lti13AutoRegistration;

    // "created_at:autodate"
    @FoormField("created_at") public Date createdAt;
    // "updated_at:autodate"
    @FoormField("updated_at") public Date updatedAt;

    // Live attributes (from joins, not in TOOL_MODEL)
    @FoormField("lti_content_count") public Long ltiContentCount;
    @FoormField("lti_site_count") public Long ltiSiteCount;

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
    // Launch-flow only (not in TOOL_MODEL)
    @FoormField("tool_state") public String toolState;
    @FoormField("platform_state") public String platformState;
    @FoormField("relaunch_url") public String relaunchUrl;
    @FoormField("orig_site_id_null") public String origSiteIdNull;
    // "sakai_tool_checksum:text:maxlength=99:hidden=true:persist=false:archive=true"
    @FoormField("sakai_tool_checksum") public String sakaiToolChecksum;

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

}
