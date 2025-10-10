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

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import org.sakaiproject.lti.api.LTIService;

/**
 * Transfer object for LTI Tool Sites.
 * Based on the TOOL_SITE_MODEL from LTIService.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class LtiToolSiteBean extends LTIBaseBean {

    // Fields from TOOL_SITE_MODEL
    public Long id;                    // TOOL_SITE_MODEL: "id:key"
    public Long toolId;                // TOOL_SITE_MODEL: "tool_id:integer:hidden=true"
    public String siteId;              // TOOL_SITE_MODEL: "SITE_ID:text:label=bl_tool_site_SITE_ID:required=true:maxlength=99:role=admin"
    public String notes;               // TOOL_SITE_MODEL: "notes:text:label=bl_tool_site_notes:maxlength=1024"
    public Date createdAt;             // TOOL_SITE_MODEL: "created_at:autodate"
    public Date updatedAt;             // TOOL_SITE_MODEL: "updated_at:autodate"

    /**
     * Creates an LtiToolSiteBean instance from a Map<String, Object>.
     * 
     * @param map The map containing LTI tool site data
     * @return LtiToolSiteBean instance populated from the map
     */
    public static LtiToolSiteBean of(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        
        LtiToolSiteBean toolSite = new LtiToolSiteBean();
        
        toolSite.setId(getLongValue(map, LTIService.LTI_ID));
        toolSite.setToolId(getLongValue(map, LTIService.LTI_TOOL_ID));
        toolSite.setSiteId(getStringValue(map, LTIService.LTI_SITE_ID));
        toolSite.setNotes(getStringValue(map, "notes"));
        toolSite.setCreatedAt(getDateValue(map, LTIService.LTI_CREATED_AT));
        toolSite.setUpdatedAt(getDateValue(map, LTIService.LTI_UPDATED_AT));
        
        return toolSite;
    }

    /**
     * Converts this LtiToolSiteBean instance to a Map<String, Object>.
     * 
     * @return Map representation of this LtiToolSiteBean
     */
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();
        
        putIfNotNull(map, LTIService.LTI_ID, id);
        putIfNotNull(map, LTIService.LTI_TOOL_ID, toolId);
        putIfNotNull(map, LTIService.LTI_SITE_ID, siteId);
        putIfNotNull(map, "notes", notes);
        putIfNotNull(map, LTIService.LTI_CREATED_AT, createdAt);
        putIfNotNull(map, LTIService.LTI_UPDATED_AT, updatedAt);
        
        return map;
    }

}
