package org.sakaiproject.sitestats.test.perf.mock;

import org.sakaiproject.site.api.Site;

public abstract class MockSite implements Site {

    private String siteId;

    public MockSite(String siteId) {
        this.siteId = siteId;
    }

    public String getId() {
        return siteId;
    }

}
