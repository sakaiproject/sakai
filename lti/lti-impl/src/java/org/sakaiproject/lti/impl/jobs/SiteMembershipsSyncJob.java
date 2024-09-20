/**
 * Copyright (c) 2009-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lti.impl.jobs;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import org.sakaiproject.basiclti.util.SakaiLTIProviderUtil;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.api.SiteMembershipsSynchroniser;

@Slf4j
public class SiteMembershipsSyncJob implements StatefulJob {
	
    private LTIService ltiService = null;
    public void setLtiService(LTIService ltiService) {
        this.ltiService = ltiService;
    }

    private SiteMembershipsSynchroniser siteMembershipsSynchroniser = null;
    public void setSiteMembershipsSynchroniser(SiteMembershipsSynchroniser siteMembershipsSynchroniser) {
        this.siteMembershipsSynchroniser = siteMembershipsSynchroniser;
    }

	public void execute(JobExecutionContext context) throws JobExecutionException {
		
        log.info("SiteMembershipsSyncJob.execute");

        // Get the current list of jobs
        List<Map<String, Object>> jobs = ltiService.getMembershipsJobs();

        for (Map<String, Object> job : jobs) {
            String siteId = (String) job.get("SITE_ID");
            String membershipsId = (String) job.get("memberships_id");
            String membershipsUrl = (String) job.get("memberships_url");
            String consumerKey = (String) job.get("consumerkey");
            String ltiVersion = (String) job.get("lti_version");
            boolean isEmailTrustedConsumer= SakaiLTIProviderUtil.isEmailTrustedConsumer(consumerKey);

            siteMembershipsSynchroniser.synchroniseSiteMemberships(siteId, membershipsId, membershipsUrl, consumerKey, isEmailTrustedConsumer,ltiVersion);
        }
	}
}
