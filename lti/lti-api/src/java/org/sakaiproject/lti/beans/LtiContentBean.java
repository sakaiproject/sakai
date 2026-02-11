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
@ToString(exclude = {"placementsecret", "oldplacementsecret"})
public class LtiContentBean extends FoormBaseBean {

    @FoormField(value = "id", type = FoormType.KEY, archive = true)
    public Long id;
    @FoormField(value = "tool_id", type = FoormType.INTEGER, hidden = true)
    public Long toolId;
    @FoormField(value = "SITE_ID", type = FoormType.TEXT, label = "bl_content_site_id", required = true, maxlength = 99, role = "admin")
    public String siteId;
    @FoormField(value = "title", type = FoormType.TEXT, label = "bl_title", required = true, maxlength = 1024, archive = true)
    public String title;
    @FoormField(value = "description", type = FoormType.TEXTAREA, label = "bl_description", maxlength = 4096, archive = true)
    public String description;
    @FoormField(value = "frameheight", type = FoormType.INTEGER, label = "bl_frameheight", archive = true)
    public Integer frameheight;
    @FoormField(value = "newpage", type = FoormType.CHECKBOX, label = "bl_newpage", archive = true)
    public Boolean newpage;
    @FoormField(value = "protect", type = FoormType.CHECKBOX, label = "bl_protect", role = "admin")
    public Boolean protect;
    @FoormField(value = "debug", type = FoormType.CHECKBOX, label = "bl_debug")
    public Boolean debug;
    @FoormField(value = "custom", type = FoormType.TEXTAREA, label = "bl_custom", rows = 5, cols = 25, maxlength = 16384, archive = true)
    public String custom;
    @FoormField(value = "launch", type = FoormType.URL, label = "bl_launch", hidden = true, maxlength = 1024, archive = true)
    public String launch;
    @FoormField(value = "xmlimport", type = FoormType.TEXT, hidden = true)
    public String xmlimport;
    @FoormField(value = "settings", type = FoormType.TEXT, hidden = true)
    public String settings;
    @FoormField(value = "contentitem", type = FoormType.TEXTAREA, label = "bl_contentitem", rows = 5, cols = 25, hidden = true, archive = true)
    public String contentitem;
    @FoormField(value = "placement", type = FoormType.TEXT, hidden = true, maxlength = 256)
    public String placement;
    @FoormField(value = "placementsecret", type = FoormType.TEXT, hidden = true, maxlength = 512)
    public String placementsecret;
    @FoormField(value = "oldplacementsecret", type = FoormType.TEXT, hidden = true, maxlength = 512)
    public String oldplacementsecret;
    @FoormField(value = "created_at", type = FoormType.AUTODATE)
    public Date createdAt;
    @FoormField(value = "updated_at", type = FoormType.AUTODATE)
    public Date updatedAt;
    @FoormField(value = "SITE_TITLE", type = FoormType.TEXT)
    public String siteTitle;
    @FoormField(value = "SITE_CONTACT_NAME", type = FoormType.TEXT)
    public String siteContactName;
    @FoormField(value = "SITE_CONTACT_EMAIL", type = FoormType.TEXT)
    public String siteContactEmail;
    @FoormField(value = "ATTRIBUTION", type = FoormType.TEXT)
    public String attribution;
    @FoormField(value = "URL", type = FoormType.TEXT)
    public String url;
    @FoormField(value = "searchURL", type = FoormType.TEXT)
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
        putBooleanAsInteger(map, LTIService.LTI_NEWPAGE, newpage);
        putBooleanAsInteger(map, LTIService.LTI_PROTECT, protect);
        putBooleanAsInteger(map, LTIService.LTI_DEBUG, debug);
        
        // LTI fields
        putIfNotNull(map, LTIService.LTI_CUSTOM, custom);
        putIfNotNull(map, LTIService.LTI_LAUNCH, launch);
        putIfNotNull(map, LTIService.LTI_XMLIMPORT, xmlimport);
        putIfNotNull(map, LTIService.LTI_SETTINGS, settings);
        putIfNotNull(map, "contentitem", contentitem);
        putIfNotNull(map, "placement", placement);
        putIfNotNull(map, LTIService.LTI_PLACEMENTSECRET, placementsecret);
        putIfNotNull(map, LTIService.LTI_OLDPLACEMENTSECRET, oldplacementsecret);
        
        
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
