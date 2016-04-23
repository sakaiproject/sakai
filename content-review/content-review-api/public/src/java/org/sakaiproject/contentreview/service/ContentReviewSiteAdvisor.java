package org.sakaiproject.contentreview.service;

import org.sakaiproject.site.api.Site;

public interface ContentReviewSiteAdvisor {

	
	public boolean siteCanUseReviewService(Site site);

	public boolean siteCanUseLTIReviewService(Site site);

	public boolean siteCanUseLTIDirectSubmission(Site site);
}
