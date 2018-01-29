package org.sakaiproject.sitemanage.api;

public class SiteManageConstants {

    public static final String SAK_PROP_IMPORT_NOTIFICATION = "site.setup.import.notification";
    public static final String SITE_INFO_TOOL_ID = "sakai.iframe.site";

    private SiteManageConstants() {
        throw new RuntimeException(this.getClass().getCanonicalName() + " is not to be instantiated");
    }
}
