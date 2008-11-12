package org.sakaiproject.sitestats.api.report;

import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.sitestats.api.PrefsData;

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


	/** Produce a report based on the parameters passed. */
	public Report getReport(String siteId, PrefsData prefsdata, ReportParams params);
	
	/** Produce a report based on the parameters passed (page results). */
	public Report getReport(String siteId, PrefsData prefsdata, ReportParams params, PagingPosition page, String groupBy, String sortBy, boolean sortAscending);
	
	public int getReportRowCount(String siteId, PrefsData prefsdata, ReportParams params, PagingPosition page, String groupBy, String sortBy, boolean sortAscending);
	
	/** Return utility class to retrieve formatted report parameters. */
	public ReportFormattedParams getReportFormattedParams();
	
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
