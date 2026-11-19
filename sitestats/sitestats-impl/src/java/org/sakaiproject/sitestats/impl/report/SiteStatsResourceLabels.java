/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.sitestats.impl.report;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;

/** Root labels shared by report summaries and the resource selector. */
public final class SiteStatsResourceLabels {

    private SiteStatsResourceLabels() { }

    public static String rootLabel(String root, ToolManager toolManager, ResourceLoader messages) {
        if (StatsManager.RESOURCES_DIR.equals(root)) {
            return toolManager.getTool(StatsManager.RESOURCES_TOOLID).getTitle();
        }
        if (StatsManager.DROPBOX_DIR.equals(root)) {
            return toolManager.getTool(StatsManager.DROPBOX_TOOLID).getTitle();
        }
        if (StatsManager.ATTACHMENTS_DIR.equals(root)) {
            return messages.getString("report_content_attachments");
        }
        return "all".equals(root) ? messages.getString("report_what_all") : null;
    }
}
