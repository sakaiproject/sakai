package org.sakaiproject.sitemanage.api;

public class SiteManageConstants {

    public static final String SAK_PROP_IMPORT_NOTIFICATION = "site.setup.import.notification";
    public static final String SITE_INFO_TOOL_ID = "sakai.iframe.site";
    public static final String RESOURCES_TOOL_ID = "sakai.resources";
    public static final String GRADEBOOK_CLASSIC_TOOL_ID = "sakai.gradebook.tool";
    public static final String GRADEBOOKNG_TOOL_ID = "sakai.gradebookng";

    private SiteManageConstants() {
        throw new RuntimeException(this.getClass().getCanonicalName() + " is not to be instantiated");
    }
}
