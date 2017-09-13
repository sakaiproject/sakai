/**
 * Copyright (c) 2006-2015 The Apereo Foundation
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
package org.sakaiproject.sitestats.tool.entityproviders;

import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportParams;

import java.util.List;

import lombok.Getter;

/**
 * Wraps a ReportDef for the purpose of providing a less verbose JSON feed. Key
 * fields from the ReportParams member are provided at the top level, again to
 * make JSON parsing less complex.
 *
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
@Getter
public class StrippedReportDef {

	private long id;
	private String siteId;
	private String title;
	private String description;
    private long from = 0L;
    private long to = 0L;
    private List<String> toolIds;
    private List<String> eventIds;
    private List<String> resourceIds;
    private String resourceAction;
    private String who;
    private List<String> userIds;

    public StrippedReportDef() {
        super();
    }

    public StrippedReportDef(ReportDef reportDef) {

        super();

        this.id = reportDef.getId();
        this.siteId = reportDef.getSiteId();
        this.title = reportDef.getTitle();
        this.description = reportDef.getDescription();
        ReportParams params = reportDef.getReportParams();
        // The report parameters might not all be defined and so there may not be a
        // from / to value.
        if (params.getWhenFrom() != null) {
            this.from = params.getWhenFrom().getTime();
        }
        if (params.getWhenTo() != null) {
            this.to = params.getWhenTo().getTime();
        }
        this.toolIds = params.getWhatToolIds();
        this.eventIds = params.getWhatEventIds();
        this.resourceIds = params.getWhatResourceIds();
        this.resourceAction = params.getWhatResourceAction();
        this.who = params.getWho();
        this.userIds = params.getWhoUserIds();
    }
}
