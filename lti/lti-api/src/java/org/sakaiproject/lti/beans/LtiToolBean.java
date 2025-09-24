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
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@ToString(exclude = {"secret", "lti13AutoToken"})
public class LtiToolBean extends LTIBaseBean {

    public Long id;
    public String siteId;
    public String title;
    public String description;
    public String status;
    public String visible;
    public Long deploymentId;
    public String launch;
    public String newpage;
    public Integer frameheight;
    public String faIcon;
    
    // Message Types (pl_ prefix for backwards compatibility)
    public Boolean plLaunch;
    public Boolean plLinkselection;
    public Boolean plContextlaunch;
    
    // Placements
    public Boolean plLessonsselection;
    public Boolean plContenteditor;
    public Boolean plAssessmentselection;
    public Boolean plCoursenav;
    public Boolean plImportitem;
    public Boolean plFileitem;
    
    // Privacy
    public Boolean sendname;
    public Boolean sendemailaddr;
    public Boolean plPrivacy;
    
    // Services
    public Boolean allowoutcomes;
    public Boolean allowlineitems;
    public Boolean allowroster;
    
    public String debug;
    public String siteinfoconfig;
    public String splash;
    public String custom;
    public String rolemap;
    public String lti13;
    
    // LTI 1.3 security values from the tool
    public String lti13ToolKeyset;
    public String lti13OidcEndpoint;
    public String lti13OidcRedirect;
    
    // LTI 1.3 security values from the LMS
    public String lti13LmsIssuer;
    public String lti13ClientId;
    public String lti13LmsDeploymentId;
    public String lti13LmsKeyset;
    public String lti13LmsEndpoint;
    public String lti13LmsToken;
    
    // LTI 1.1 security arrangement
    public String consumerkey;
    public String secret;
    public String lti13Settings;
    public String xmlimport;
    public String lti13AutoToken;
    public Integer lti13AutoState;
    public String lti13AutoRegistration;
    public String sakaiToolChecksum;
    
    public Date createdAt;
    public Date updatedAt;

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
        tool.setNewpage(getStringValue(map, "newpage"));
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
        tool.setDebug(getStringValue(map, LTIService.LTI_DEBUG));
        tool.setSiteinfoconfig(getStringValue(map, "siteinfoconfig"));
        tool.setSplash(getStringValue(map, LTIService.LTI_SPLASH));
        tool.setCustom(getStringValue(map, LTIService.LTI_CUSTOM));
        tool.setRolemap(getStringValue(map, LTIService.LTI_ROLEMAP));
        tool.setLti13(getStringValue(map, LTIService.LTI13));
        
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
        tool.setLti13Settings(getStringValue(map, "lti13_settings"));
        tool.setXmlimport(getStringValue(map, LTIService.LTI_XMLIMPORT));
        tool.setLti13AutoToken(getStringValue(map, "lti13_auto_token"));
        tool.setLti13AutoState(getIntegerValue(map, "lti13_auto_state"));
        tool.setLti13AutoRegistration(getStringValue(map, "lti13_auto_registration"));
        tool.setSakaiToolChecksum(getStringValue(map, "sakai_tool_checksum"));
        
        // Timestamps
        tool.setCreatedAt(getDateValue(map, LTIService.LTI_CREATED_AT));
        tool.setUpdatedAt(getDateValue(map, LTIService.LTI_UPDATED_AT));
        
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
        putIfNotNull(map, "newpage", newpage);
        putIfNotNull(map, LTIService.LTI_FRAMEHEIGHT, frameheight);
        putIfNotNull(map, LTIService.LTI_FA_ICON, faIcon);
        
        // Message Types
        putIfNotNull(map, "pl_launch", plLaunch);
        putIfNotNull(map, "pl_linkselection", plLinkselection);
        putIfNotNull(map, "pl_contextlaunch", plContextlaunch);
        
        // Placements
        putIfNotNull(map, "pl_lessonsselection", plLessonsselection);
        putIfNotNull(map, "pl_contenteditor", plContenteditor);
        putIfNotNull(map, "pl_assessmentselection", plAssessmentselection);
        putIfNotNull(map, "pl_coursenav", plCoursenav);
        putIfNotNull(map, "pl_importitem", plImportitem);
        putIfNotNull(map, "pl_fileitem", plFileitem);
        
        // Privacy
        putIfNotNull(map, LTIService.LTI_SENDNAME, sendname);
        putIfNotNull(map, LTIService.LTI_SENDEMAILADDR, sendemailaddr);
        putIfNotNull(map, "pl_privacy", plPrivacy);
        
        // Services
        putIfNotNull(map, LTIService.LTI_ALLOWOUTCOMES, allowoutcomes);
        putIfNotNull(map, LTIService.LTI_ALLOWLINEITEMS, allowlineitems);
        putIfNotNull(map, LTIService.LTI_ALLOWROSTER, allowroster);
        
        // Configuration
        putIfNotNull(map, LTIService.LTI_DEBUG, debug);
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
        putIfNotNull(map, "lti13_settings", lti13Settings);
        putIfNotNull(map, LTIService.LTI_XMLIMPORT, xmlimport);
        putIfNotNull(map, "lti13_auto_token", lti13AutoToken);
        putIfNotNull(map, "lti13_auto_state", lti13AutoState);
        putIfNotNull(map, "lti13_auto_registration", lti13AutoRegistration);
        putIfNotNull(map, "sakai_tool_checksum", sakaiToolChecksum);
        
        // Timestamps
        putIfNotNull(map, LTIService.LTI_CREATED_AT, createdAt);
        putIfNotNull(map, LTIService.LTI_UPDATED_AT, updatedAt);
        
        return map;
    }

}
