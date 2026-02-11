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

    // Core fields from CONTENT_MODEL
    public Long id;                    // CONTENT_MODEL: "id:key:archive=true"
    public Long toolId;                // CONTENT_MODEL: "tool_id:integer:hidden=true"
    public String siteId;              // CONTENT_MODEL: "SITE_ID:text:label=bl_content_site_id:required=true:maxlength=99:role=admin"
    public String title;               // CONTENT_MODEL: "title:text:label=bl_title:required=true:maxlength=1024:archive=true"
    public String description;         // CONTENT_MODEL: "description:textarea:label=bl_description:maxlength=4096:archive=true"
    public Integer frameheight;        // CONTENT_MODEL: "frameheight:integer:label=bl_frameheight:archive=true"
    public Boolean newpage;            // CONTENT_MODEL: "newpage:checkbox:label=bl_newpage:archive=true"
    public Boolean protect;            // CONTENT_MODEL: "protect:checkbox:label=bl_protect:role=admin"
    public Boolean debug;              // CONTENT_MODEL: "debug:checkbox:label=bl_debug"
    // LTI fields from CONTENT_MODEL
    public String custom;              // CONTENT_MODEL: "custom:textarea:label=bl_custom:rows=5:cols=25:maxlength=16384:archive=true"
    public String launch;              // CONTENT_MODEL: "launch:url:label=bl_launch:hidden=true:maxlength=1024:archive=true"
    public String xmlimport;           // CONTENT_MODEL: "xmlimport:text:hidden=true:maxlength=1M"
    public String settings;            // CONTENT_MODEL: "settings:text:hidden=true:maxlength=1M"
    public String contentitem;         // CONTENT_MODEL: "contentitem:text:label=bl_contentitem:rows=5:cols=25:maxlength=1M:hidden=true:archive=true"
    public String placement;           // CONTENT_MODEL: "placement:text:hidden=true:maxlength=256"
    public String placementsecret;     // CONTENT_MODEL: "placementsecret:text:hidden=true:maxlength=512"
    public String oldplacementsecret;  // CONTENT_MODEL: "oldplacementsecret:text:hidden=true:maxlength=512"
    // Timestamps from CONTENT_MODEL
    public Date createdAt;             // CONTENT_MODEL: "created_at:autodate"
    public Date updatedAt;             // CONTENT_MODEL: "updated_at:autodate"

    // Extra fields that can be populated from joins (CONTENT_EXTRA_FIELDS)
    public String siteTitle;           // CONTENT_EXTRA_FIELDS: "SITE_TITLE:text:table=SAKAI_SITE:realname=TITLE"
    public String siteContactName;     // CONTENT_EXTRA_FIELDS: "SITE_CONTACT_NAME:text:table=ssp1:realname=VALUE"
    public String siteContactEmail;    // CONTENT_EXTRA_FIELDS: "SITE_CONTACT_EMAIL:text:table=ssp2:realname=VALUE"
    public String attribution;         // CONTENT_EXTRA_FIELDS: "ATTRIBUTION:text:table=ssp3:realname=VALUE"
    public String url;                 // CONTENT_EXTRA_FIELDS: "URL:text:table=ssp4:realname=VALUE"
    public String searchUrl;           // CONTENT_EXTRA_FIELDS: "searchURL:text:table=ssp5:realname=VALUE"

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
