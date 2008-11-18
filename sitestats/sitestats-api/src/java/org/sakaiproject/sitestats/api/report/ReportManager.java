package org.sakaiproject.sitestats.api.report;

import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;

public interface ReportManager {

	public static final String	WHO_CUSTOM					= "who-custom";
	public static final String	WHO_ROLE					= "who-role";
	public static final String	WHO_GROUPS					= "who-groups";
	public static final String	WHO_ALL						= "who-all";
	public static final String	WHO_NONE					= "who-none";
	public static final String	WHEN_CUSTOM					= "when-custom";
	public static final String	WHEN_LAST30DAYS				= "when-last30days";
	public static final String	WHEN_LAST7DAYS				= "when-last7days";
	public static final String	WHEN_ALL					= "when-all";
	public static final String	WHAT_RESOURCES				= "what-resources";
	public static final String	WHAT_RESOURCES_ACTION_NEW	= "new";
	public static final String	WHAT_RESOURCES_ACTION_READ	= "read";
	public static final String	WHAT_RESOURCES_ACTION_REVS	= "revise";
	public static final String	WHAT_RESOURCES_ACTION_DEL	= "delete";
	public static final String	WHAT_EVENTS_BYEVENTS		= "what-events-byevent";
	public static final String	WHAT_EVENTS_BYTOOL			= "what-events-bytool";
	public static final String	WHAT_EVENTS					= "what-events";
	public static final String	WHAT_VISITS					= "what-visits";
	public static final String	HOW_TOTALSBY				= "how-totalsby";


	/** Produce a report based on supplied parameters. */
	public Report getReport(String siteId, boolean restrictToToolsInSite, ReportParams params);
	
	/**
	 * Produce a report based on supplied parameters (paged results).
	 * @param siteId The site ID
	 * @param restrictToToolsInSite Whether to limit report to events from tools available in site
	 * @param params Object containing specific report parameters (see {@link ReportParams})
	 * @param page Paging information (see {@link PagingPosition})
	 * @param sortBy Columns to sort by
	 * @param sortAscending Sort ascending?
	 * @return The report (see {@link Report})
	 */
	public Report getReport(String siteId, boolean restrictToToolsInSite, ReportParams params, PagingPosition page, String sortBy, boolean sortAscending);
	
	/**
	 * Get row count for a report based on supplied parameters (paged results).
	 * @param siteId The site ID
	 * @param restrictToToolsInSite Whether to limit report to events from tools available in site
	 * @param params Object containing specific report parameters (see {@link ReportParams})
	 * @return The report row count
	 */
	public int getReportRowCount(String siteId, boolean restrictToToolsInSite, ReportParams params);
	
	/** Return utility class to retrieve formatted report parameters. */
	public ReportFormattedParams getReportFormattedParams();
	
	/**
	 * Check if a given column is displayable (has data) for the specified report parameters.
	 * @param params Object containing specific report parameters (see {@link ReportParams})
	 * @param column Column name (see {@link StatsManager#T_SITE}, {@link StatsManager#T_USER}, {@link StatsManager#T_EVENT}, {@link StatsManager#T_RESOURCE}, {@link StatsManager#T_RESOURCE_ACTION}, {@link StatsManager#T_DATE}, {@link StatsManager#T_LASTDATE}, {@link StatsManager#T_TOTAL}) 
	 * @return True if column has data and can be displayed; false otherwise
	 */
	public boolean isReportColumnAvailable(ReportParams params, String column);
	
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
