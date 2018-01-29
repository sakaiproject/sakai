/**
 * $URL: https://source.sakaiproject.org/svn/sitestats/trunk/sitestats-impl/src/java/org/sakaiproject/sitestats/impl/entity/SiteStatsMetricsEntityProvider.java $
 * $Id: SiteStatsMetricsEntityProvider.java 105078 2012-02-24 23:00:38Z ottenhoff@longsight.com $
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.entityproviders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;

import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.sitestats.api.SitePresenceTotal;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;

/**
 * Provides some JSON targets as custom actions.
 *
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
@Setter
@Slf4j
public class SiteStatsEntityProvider extends AbstractEntityProvider implements AutoRegisterEntityProvider, ActionsExecutable, Outputable, Describeable {

    public static final String PREFIX = "sitestats";

    private ReportManager reportManager;
    private StatsManager statsManager;

    public String getEntityPrefix() {
        return PREFIX;
    }

    public Object getSampleEntity() {
        return PREFIX;
    }

    public String[] getHandledOutputFormats() {
        return new String[] { Formats.JSON };
    }

    /**
     *  Lists the available reports
     */
    @EntityCustomAction(action = "listreports", viewKey = EntityView.VIEW_LIST)
    public List<StrippedReportDef> handleListReports(EntityView view, Map<String, Object> params) {

        String userId = developerHelperService.getCurrentUserId();

        if (userId == null || userId.length() <= 0) {
            throw new EntityException("You must be logged in to list the reports", "", HttpServletResponse.SC_FORBIDDEN);
        }

        String siteId = view.getPathSegment(2);

        if (siteId == null || siteId.length() <= 0) {
            throw new EntityException("The reports request must include the site id", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        if (log.isDebugEnabled()) {
            log.debug("SITE ID:" + siteId);
        }

        if (!developerHelperService.isUserAllowedInEntityReference("/user/" + userId, "sitestats.view", "/site/" + siteId)) {
            throw new EntityException("You don't have access to sitestats in this site", "", HttpServletResponse.SC_FORBIDDEN);
        }

        List<StrippedReportDef> stripped = new ArrayList<StrippedReportDef>();

        for (ReportDef rd : reportManager.getReportDefinitions(siteId, true, false)) {
            stripped.add(new StrippedReportDef(rd));
        }

        return stripped;
    }

    /**
     * Runs a particular report
     */
    @EntityCustomAction(action = "runreport", viewKey = EntityView.VIEW_LIST)
    public List<StrippedStat> handleRunReport(EntityView view, Map<String, Object> params) {

        String userId = developerHelperService.getCurrentUserId();

        if (userId == null || userId.length() <= 0) {
            throw new EntityException("You must be logged in to list the reportes", "", HttpServletResponse.SC_FORBIDDEN);
        }

        String siteId = view.getPathSegment(2);

        if (log.isDebugEnabled()) {
            log.debug("SITE ID:" + siteId);
        }

        if (!developerHelperService.isUserAllowedInEntityReference("/user/" + userId, "sitestats.view", "/site/" + siteId)) {
            throw new EntityException("You don't have access to sitestats in this site", "", HttpServletResponse.SC_FORBIDDEN);
        }

        String reportIdString = (String) params.get("id");

        if (reportIdString == null || reportIdString.length() <= 0) {
            throw new EntityException("You must supply a numeric report id", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        if (log.isDebugEnabled()) {
            log.debug("REPORT ID:" + reportIdString);
        }

        long reportId = -1;
        
        try {
            reportId = Long.parseLong(reportIdString);
        } catch (NumberFormatException nfe) {
            throw new EntityException("You must supply a numeric report id", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        ReportDef reportDef = reportManager.getReportDefinition(reportId);

        if (reportDef == null) {
            throw new EntityException("Report with id '" + reportId + "' doesn't exist.", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        Report report = reportManager.getReport(reportDef, true);

        List<StrippedStat> stripped = new ArrayList<StrippedStat>();

        for (Stat stat : report.getReportData()) {
            stripped.add(new StrippedStat(stat));
        }

        return stripped;
    }

    @EntityCustomAction(action = "presencetotals", viewKey = EntityView.VIEW_LIST)
    public Map<String, SitePresenceTotal> handlePresenceTotals(EntityView view, Map<String, Object> params) {

        String userId = developerHelperService.getCurrentUserId();

        if (userId == null || userId.length() <= 0) {
            throw new EntityException("You must be logged in to list the reports", "", HttpServletResponse.SC_FORBIDDEN);
        }

        String siteId = view.getPathSegment(2);

        if (siteId == null || siteId.length() <= 0) {
            throw new EntityException("The totals request must include the site id", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        if (log.isDebugEnabled()) {
            log.debug("SITE ID:" + siteId);
        }

        if (!developerHelperService.isUserAllowedInEntityReference("/user/" + userId, "sitestats.view", "/site/" + siteId)) {
            throw new EntityException("You don't have access to sitestats in this site", "", HttpServletResponse.SC_FORBIDDEN);
        }

        return statsManager.getPresenceTotalsForSite(siteId);
    }
}
