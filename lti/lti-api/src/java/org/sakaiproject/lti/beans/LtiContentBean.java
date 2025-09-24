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
 * Transfer object for LTI Content items.
 * Based on the CONTENT_MODEL from LTIService.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@ToString(exclude = {"placementsecret", "oldplacementsecret", "lti13Settings"})
public class LtiContentBean extends LTIBaseBean {

    public Long id;
    public Long toolId;
    public String siteId;
    public String title;
    public String description;
    public Integer frameheight;
    public Boolean newpage;
    public Boolean protect;
    public Boolean debug;
    public String custom;
    public String launch;
    public String xmlimport;
    public String settings;
    public String contentitem;
    public String placement;
    public String placementsecret;
    public String oldplacementsecret;
    public Long lti13;
    public String lti13Settings;
    public Date createdAt;
    public Date updatedAt;

    // Extra fields that can be populated from joins
    public String siteTitle;
    public String siteContactName;
    public String siteContactEmail;
    public String attribution;
    public String url;
    public String searchUrl;

    /**
     * Creates an LtiContentBean instance from a Map<String, Object>.
     * 
     * @param map The map containing LTI content data
     * @return LtiContentBean instance populated from the map
     */
    public static LtiContentBean of(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        
        LtiContentBean content = new LtiContentBean();
        
        // Core fields
        content.setId(getLongValue(map, LTIService.LTI_ID));
        content.setToolId(getLongValue(map, LTIService.LTI_TOOL_ID));
        content.setSiteId(getStringValue(map, LTIService.LTI_SITE_ID));
        content.setTitle(getStringValue(map, LTIService.LTI_TITLE));
        content.setDescription(getStringValue(map, LTIService.LTI_DESCRIPTION));
        content.setFrameheight(getIntegerValue(map, LTIService.LTI_FRAMEHEIGHT));
        content.setNewpage(getBooleanValue(map, LTIService.LTI_NEWPAGE));
        content.setProtect(getBooleanValue(map, LTIService.LTI_PROTECT));
        content.setDebug(getBooleanValue(map, LTIService.LTI_DEBUG));
        
        // LTI fields
        content.setCustom(getStringValue(map, LTIService.LTI_CUSTOM));
        content.setLaunch(getStringValue(map, LTIService.LTI_LAUNCH));
        content.setXmlimport(getStringValue(map, LTIService.LTI_XMLIMPORT));
        content.setSettings(getStringValue(map, LTIService.LTI_SETTINGS));
        content.setContentitem(getStringValue(map, "contentitem"));
        content.setPlacement(getStringValue(map, "placement"));
        content.setPlacementsecret(getStringValue(map, LTIService.LTI_PLACEMENTSECRET));
        content.setOldplacementsecret(getStringValue(map, LTIService.LTI_OLDPLACEMENTSECRET));
        
        // LTI 1.3 fields
        content.setLti13(getLongValue(map, LTIService.LTI13));
        content.setLti13Settings(getStringValue(map, "lti13_settings"));
        
        // Timestamps
        content.setCreatedAt(getDateValue(map, LTIService.LTI_CREATED_AT));
        content.setUpdatedAt(getDateValue(map, LTIService.LTI_UPDATED_AT));
        
        // Extra fields from joins
        content.setSiteTitle(getStringValue(map, "SITE_TITLE"));
        content.setSiteContactName(getStringValue(map, "SITE_CONTACT_NAME"));
        content.setSiteContactEmail(getStringValue(map, "SITE_CONTACT_EMAIL"));
        content.setAttribution(getStringValue(map, "ATTRIBUTION"));
        content.setUrl(getStringValue(map, "URL"));
        content.setSearchUrl(getStringValue(map, "searchURL"));
        
        return content;
    }

    /**
     * Converts this LtiContentBean instance to a Map<String, Object>.
     * 
     * @return Map representation of this LtiContentBean
     */
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();
        
        // Core fields
        putIfNotNull(map, LTIService.LTI_ID, id);
        putIfNotNull(map, LTIService.LTI_TOOL_ID, toolId);
        putIfNotNull(map, LTIService.LTI_SITE_ID, siteId);
        putIfNotNull(map, LTIService.LTI_TITLE, title);
        putIfNotNull(map, LTIService.LTI_DESCRIPTION, description);
        putIfNotNull(map, LTIService.LTI_FRAMEHEIGHT, frameheight);
        putIfNotNull(map, LTIService.LTI_NEWPAGE, newpage);
        putIfNotNull(map, LTIService.LTI_PROTECT, protect);
        putIfNotNull(map, LTIService.LTI_DEBUG, debug);
        
        // LTI fields
        putIfNotNull(map, LTIService.LTI_CUSTOM, custom);
        putIfNotNull(map, LTIService.LTI_LAUNCH, launch);
        putIfNotNull(map, LTIService.LTI_XMLIMPORT, xmlimport);
        putIfNotNull(map, LTIService.LTI_SETTINGS, settings);
        putIfNotNull(map, "contentitem", contentitem);
        putIfNotNull(map, "placement", placement);
        putIfNotNull(map, LTIService.LTI_PLACEMENTSECRET, placementsecret);
        putIfNotNull(map, LTIService.LTI_OLDPLACEMENTSECRET, oldplacementsecret);
        
        // LTI 1.3 fields
        putIfNotNull(map, LTIService.LTI13, lti13);
        putIfNotNull(map, "lti13_settings", lti13Settings);
        
        // Timestamps
        putIfNotNull(map, LTIService.LTI_CREATED_AT, createdAt);
        putIfNotNull(map, LTIService.LTI_UPDATED_AT, updatedAt);
        
        // Extra fields from joins
        putIfNotNull(map, "SITE_TITLE", siteTitle);
        putIfNotNull(map, "SITE_CONTACT_NAME", siteContactName);
        putIfNotNull(map, "SITE_CONTACT_EMAIL", siteContactEmail);
        putIfNotNull(map, "ATTRIBUTION", attribution);
        putIfNotNull(map, "URL", url);
        putIfNotNull(map, "searchURL", searchUrl);
        
        return map;
    }

}
