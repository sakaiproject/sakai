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

import java.util.Map;
import java.util.HashMap;

import org.sakaiproject.lti.api.LTIService;

/**
 * Transfer object for LTI Memberships Jobs.
 * Based on the MEMBERSHIPS_JOBS_MODEL from LTIService.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@ToString
public class LtiMembershipsJobBean extends FoormBaseBean {

    @FoormField(value = "SITE_ID", type = FoormType.TEXT, maxlength = 99, required = true)
    public String siteId;
    @FoormField(value = "memberships_id", type = FoormType.TEXT, maxlength = 256, required = true)
    public String membershipsId;
    @FoormField(value = "memberships_url", type = FoormType.TEXT, maxlength = 4000, required = true)
    public String membershipsUrl;
    @FoormField(value = "consumerkey", type = FoormType.TEXT, label = "bl_consumerkey", maxlength = 1024)
    public String consumerkey;
    @FoormField(value = "lti_version", type = FoormType.TEXT, maxlength = 32, required = true)
    public String ltiVersion;

    /**
     * Creates an LtiMembershipsJobBean instance from a Map<String, Object>.
     * 
     * @param map The map containing LTI memberships job data
     * @return LtiMembershipsJobBean instance populated from the map
     */
    public static LtiMembershipsJobBean of(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        
        LtiMembershipsJobBean job = new LtiMembershipsJobBean();
        
        job.setSiteId(getStringValue(map, LTIService.LTI_SITE_ID));
        job.setMembershipsId(getStringValue(map, "memberships_id"));
        job.setMembershipsUrl(getStringValue(map, "memberships_url"));
        job.setConsumerkey(getStringValue(map, LTIService.LTI_CONSUMERKEY));
        job.setLtiVersion(getStringValue(map, "lti_version"));
        
        return job;
    }

    /**
     * Converts this LtiMembershipsJobBean instance to a Map<String, Object>.
     * 
     * @return Map representation of this LtiMembershipsJobBean
     */
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();
        
        putIfNotNull(map, LTIService.LTI_SITE_ID, siteId);
        putIfNotNull(map, "memberships_id", membershipsId);
        putIfNotNull(map, "memberships_url", membershipsUrl);
        putIfNotNull(map, LTIService.LTI_CONSUMERKEY, consumerkey);
        putIfNotNull(map, "lti_version", ltiVersion);
        
        return map;
    }

}
