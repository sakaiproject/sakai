package org.sakaiproject.lti.impl.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.imsglobal.basiclti.BasicLTIUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lti.api.LTIException;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.api.SiteMembershipsSynchroniser;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

public class SiteMembershipsSyncJob implements StatefulJob {
	
	private static Log M_log = LogFactory.getLog(SiteMembershipsSyncJob.class);
	
    private LTIService ltiService = null;
    public void setLtiService(LTIService ltiService) {
        this.ltiService = ltiService;
    }

    private SiteMembershipsSynchroniser siteMembershipsSynchroniser = null;
    public void setSiteMembershipsSynchroniser(SiteMembershipsSynchroniser siteMembershipsSynchroniser) {
        this.siteMembershipsSynchroniser = siteMembershipsSynchroniser;
    }

	public void execute(JobExecutionContext context) throws JobExecutionException {

        M_log.info("SiteMembershipsSyncJob.execute");
        List<String> emailTrustedConsumersList =null;
        final String emailTrustedConsumersConfig = ServerConfigurationService
                .getString("basiclti.provider.email.trusted.consumers", null);
        if(BasicLTIUtil.isNotBlank(emailTrustedConsumersConfig)) {
            String[] emailTrustedConsumers = emailTrustedConsumersConfig.split(":");
            emailTrustedConsumersList = Arrays.asList(emailTrustedConsumers);

        }

        // Get the current list of jobs
        List<Map<String, Object>> jobs = ltiService.getMembershipsJobs();

        for (Map<String, Object> job : jobs) {
        	boolean isEmailTrustedConsumer=false;
            String siteId = (String) job.get("SITE_ID");
            String membershipsId = (String) job.get("memberships_id");
            String membershipsUrl = (String) job.get("memberships_url");
            String consumerKey = (String) job.get("consumerkey");
            String ltiVersion = (String) job.get("lti_version");
            if (emailTrustedConsumersList!=null&&emailTrustedConsumersList.contains(consumerKey)) {
            	isEmailTrustedConsumer = true;
            }

            siteMembershipsSynchroniser.synchroniseSiteMemberships(siteId, membershipsId, membershipsUrl, consumerKey, isEmailTrustedConsumer,ltiVersion);
        }
	}
}
