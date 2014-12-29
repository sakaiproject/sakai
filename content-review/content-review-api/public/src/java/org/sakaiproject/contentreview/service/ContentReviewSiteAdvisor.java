package org.sakaiproject.contentreview.service;

import org.sakaiproject.site.api.Site;

public interface ContentReviewSiteAdvisor {

	
	public boolean siteCanUseReviewService(Site site);
}
