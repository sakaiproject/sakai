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

import java.util.Map;
import java.util.HashMap;

import org.sakaiproject.lti.api.LTIService;

/**
 * Transfer object for LTI Memberships Jobs.
 * Based on the MEMBERSHIPS_JOBS_MODEL from LTIService.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class LtiMembershipsJobBean extends LTIBaseBean {

    public String siteId;
    public String membershipsId;
    public String membershipsUrl;
    public String consumerkey;
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
