/**
 * $URL$
 * $Id$
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
package org.sakaiproject.sitestats.api.report;

import java.util.List;

import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.sitestats.api.StatsManager;

public interface ReportManager {

	public static final String	WHO_CUSTOM					= "who-custom";
	public static final String	WHO_ROLE					= "who-role";
	public static final String	WHO_GROUPS					= "who-groups";
	public static final String	WHO_ALL						= "who-all";
	public static final String	WHO_NONE					= "who-none";
	public static final String	WHEN_CUSTOM					= "when-custom";
	public static final String	WHEN_LAST365DAYS			= "when-last365days";
	public static final String	WHEN_LAST30DAYS				= "when-last30days";
	public static final String	WHEN_LAST7DAYS				= "when-last7days";
	public static final String	WHEN_ALL					= "when-all";
	public static final String	WHAT_RESOURCES				= "what-resources";
	public static final String	WHAT_RESOURCES_ACTION_NEW	= "new";
	public static final String	WHAT_RESOURCES_ACTION_READ	= "read";
	public static final String	WHAT_RESOURCES_ACTION_REVS	= "revise";
	public static final String	WHAT_RESOURCES_ACTION_DEL	= "delete";
	public static final String	WHAT_LESSONPAGES			= "what-lessonpages";
	public static final String	WHAT_LESSONS_ACTION_CREATE	= "create";
	public static final String	WHAT_LESSONS_ACTION_READ	= "read";
	public static final String	WHAT_LESSONS_ACTION_UPDATE	= "update";
	public static final String	WHAT_LESSONS_ACTION_DELETE	= "delete";
	public static final String	WHAT_EVENTS_BYEVENTS		= "what-events-byevent";
	public static final String	WHAT_EVENTS_BYTOOL			= "what-events-bytool";
	public static final String	WHAT_EVENTS_ALLEVENTS		= "all";
	public static final String	WHAT_EVENTS_ALLTOOLS		= "all";
	public static final String	WHAT_EVENTS					= "what-events";
	public static final String	WHAT_VISITS					= "what-visits";
	public static final String	WHAT_PRESENCES				= "what-presences";
	public static final String	WHAT_VISITS_TOTALS			= "what-visits-totals";
	public static final String	WHAT_ACTIVITY_TOTALS		= "what-activity-totals";
	public static final String	HOW_TOTALSBY				= "how-totalsby";
	public static final String	HOW_SORT_DEFAULT			= "default";
	public static final String	HOW_PRESENTATION_TABLE		= "how-presentation-table";
	public static final String	HOW_PRESENTATION_CHART		= "how-presentation-chart";
	public static final String	HOW_PRESENTATION_BOTH		= "how-presentation-both";


	/** Produce a report based on supplied parameters. */
	public Report getReport(ReportDef reportDef, boolean restrictToToolsInSite);
	
	/**
	 * Produce a report based on supplied parameters (paged results).
	 * @param params Object containing specific report parameters (see {@link ReportDef})
	 * @param restrictToToolsInSite Whether to limit report to events from tools available in site
	 * @param page Paging information (see {@link PagingPosition})
	 * @param log If true, an event will be logged
	 * @return The report (see {@link Report})
	 */
	public Report getReport(ReportDef reportDef, boolean restrictToToolsInSite, PagingPosition page, boolean log);
	
	/**
	 * Get row count for a report based on supplied parameters (paged results).
	 * @param params Object containing specific report parameters (see {@link ReportDef})
	 * @param restrictToToolsInSite Whether to limit report to events from tools available in site
	 * @return The report row count
	 */
	public int getReportRowCount(ReportDef reportDef, boolean restrictToToolsInSite);
	
	/** Return utility class to retrieve formatted report parameters. */
	public ReportFormattedParams getReportFormattedParams();
	
	/**
	 * Check if a given column is displayable (has data) for the specified report parameters.
	 * @param params Object containing specific report parameters (see {@link ReportParams})
	 * @param column Column name (see {@link StatsManager#T_SITE}, {@link StatsManager#T_USER}, {@link StatsManager#T_EVENT}, {@link StatsManager#T_RESOURCE}, {@link StatsManager#T_RESOURCE_ACTION}, {@link StatsManager#T_DATE}, {@link StatsManager#T_LASTDATE}, {@link StatsManager#T_TOTAL}) 
	 * @return True if column has data and can be displayed; false otherwise
	 */
	public boolean isReportColumnAvailable(ReportParams params, String column);
	
	/** Load a report definition from DB. */
	public ReportDef getReportDefinition(long id);
	
	/** Save (add or update) a report definition to DB. */
	public boolean saveReportDefinition(ReportDef reportDef);
	
	/** Remove a report definition from DB. */
	public boolean removeReportDefinition(ReportDef reportDef);
	
	/**
	 * Load report definitions for a specific site from DB.
	 * @param siteId The site id or null to load default (global) report definitions.
	 * @param includedPredefined If true, predefined report definitions will also be included
	 * @param includeHidden If true, hidden report definitions will also be included
	 * @return A list of report definitions.
	 */
	public List<ReportDef> getReportDefinitions(String siteId, boolean includedPredefined, boolean includeHidden);
	
	/**
	 * Constructs an excel workbook document representing the table.
	 * @param report The Report object to export.
	 * @return The excel workbook
	 */
	public byte[] getReportAsExcel(Report report, String sheetName);
	
	/**
	 * Constructs a CSV string representing the table.
	 * @param report The Report object to export.
	 * @return The csv document
	 */
	public String getReportAsCsv(Report report);
	
	/**
	 * Constructs a PDF representing the table.
	 * @param report The Report object to export.
	 * @return The csv document
	 */
	public byte[] getReportAsPDF(Report report);
}
