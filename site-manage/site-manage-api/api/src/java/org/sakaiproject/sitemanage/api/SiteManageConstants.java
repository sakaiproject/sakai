/**
 * Copyright (c) 2003-2020 The Apereo Foundation
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
package org.sakaiproject.sitemanage.api;

public class SiteManageConstants {

    public static final String SAK_PROP_IMPORT_NOTIFICATION = "site.setup.import.notification";
    public static final String SITE_INFO_TOOL_ID = "sakai.iframe.site";
    public static final String RESOURCES_TOOL_ID = "sakai.resources";
    public static final String GRADEBOOK_TOOL_ID = "sakai.gradebookng";
    public static final String CALENDAR_TOOL_ID = "sakai.calendar";
    public static final String PROP_SITEINFO_GROUP_FILTER_ENABLED = "siteinfo.group.filter.enabled";

    private SiteManageConstants() {
        throw new RuntimeException(this.getClass().getCanonicalName() + " is not to be instantiated");
    }
}
