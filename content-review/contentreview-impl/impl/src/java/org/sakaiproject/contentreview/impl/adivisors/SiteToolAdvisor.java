package org.sakaiproject.contentreview.impl.adivisors;

import org.sakaiproject.contentreview.service.ContentReviewSiteAdvisor;
import org.sakaiproject.site.api.Site;

import java.util.Collection;
import java.util.Collections;

/**
 * This {@link ContentReviewSiteAdvisor} checks for specific tools in the site to allow the site to use the
 * ContentReviewService
 *
 * In most scenarii, it will check for "sakai.assignment2" and "sakai.assignment.grades"
 * @author Colin Hebert
 */
public class SiteToolAdvisor implements ContentReviewSiteAdvisor {
    /**
     * Collection of tool common IDs which rely on content review
     */
    private Collection<String> contentReviewTools = Collections.emptyList();

    public boolean siteCanUseReviewService(Site site) {
        for(String toolId : contentReviewTools)
            if(site.getToolForCommonId(toolId) != null)
                return true;
        return false;
    }

    public Collection<String> getContentReviewTools() {
        return contentReviewTools;
    }

    public void setContentReviewTools(Collection<String> contentReviewTools) {
        this.contentReviewTools = contentReviewTools;
    }
}
