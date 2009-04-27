package org.sakaiproject.sitestats.test.mocks;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.sitestats.api.CommonStatGrpByDate;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SiteActivity;
import org.sakaiproject.sitestats.api.SiteActivityByTool;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.SummaryActivityChartData;
import org.sakaiproject.sitestats.api.SummaryActivityTotals;
import org.sakaiproject.sitestats.api.SummaryVisitsChartData;
import org.sakaiproject.sitestats.api.SummaryVisitsTotals;

public class FakeStatsManager implements StatsManager {

	public List<Stat> getActivityTotalsStats(String siteId, List<String> events, Date date, Date date2, PagingPosition page, List<String> totalsBy, String sortBy, boolean sortAscending, int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getChartBackgroundColor() {
		// TODO Auto-generated method stub
		return null;
	}

	public float getChartTransparency() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<Stat> getEventStats(String siteId, List<String> events) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<EventStat> getEventStats(String siteId, List<String> events, String searchKey, Date date, Date date2) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Stat> getEventStats(String siteId, List<String> events, Date date, Date date2, List<String> userIds, boolean inverseUserSelection, PagingPosition page, List<String> totalsBy,
			String sortBy, boolean sortAscending, int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<CommonStatGrpByDate> getEventStatsGrpByDate(String siteId, List<String> events, Date date, Date date2, List<String> userIds, boolean inverseUserSelection, PagingPosition page) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getEventStatsRowCount(String siteId, List<String> events, Date date, Date date2, List<String> userIds, boolean inverseUserSelection, List<String> totalsBy) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Date getInitialActivityDate(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	public PrefsData getPreferences(String siteId, boolean includeUnselected) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getResourceImage(String ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getResourceImageLibraryRelativePath(String ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getResourceName(String ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getResourceName(String ref, boolean includeLocationPrefix) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Stat> getResourceStats(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<ResourceStat> getResourceStats(String siteId, String searchKey, Date date, Date date2) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Stat> getResourceStats(String siteId, String resourceAction, List<String> resourceIds, Date date, Date date2, List<String> userIds, boolean inverseUserSelection, PagingPosition page,
			List<String> totalsBy, String sortBy, boolean sortAscending, int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<CommonStatGrpByDate> getResourceStatsGrpByDateAndAction(String siteId, String resourceAction, List<String> resourceIds, Date date, Date date2, List<String> userIds,
			boolean inverseUserSelection, PagingPosition page) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getResourceStatsRowCount(String siteId, String resourceAction, List<String> resourceIds, Date date, Date date2, List<String> userIds, boolean inverseUserSelection, List<String> totalsBy) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getResourceURL(String ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SiteActivity> getSiteActivity(String siteId, List<String> events) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SiteActivity> getSiteActivity(String siteId, List<String> events, Date date, Date date2) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SiteActivity> getSiteActivityByDay(String siteId, List<String> events, Date date, Date date2) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SiteActivity> getSiteActivityByMonth(String siteId, List<String> events, Date date, Date date2) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SiteActivityByTool> getSiteActivityByTool(String siteId, List<String> events, Date date, Date date2) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SiteActivity> getSiteActivityGrpByDate(String siteId, List<String> events, Date date, Date date2) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String> getSiteUsers(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SiteVisits> getSiteVisits(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SiteVisits> getSiteVisits(String siteId, Date date, Date date2) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SiteVisits> getSiteVisitsByMonth(String siteId, Date date, Date date2) {
		// TODO Auto-generated method stub
		return null;
	}

	public SummaryActivityChartData getSummaryActivityChartData(String siteId, String viewType, String chartType) {
		// TODO Auto-generated method stub
		return null;
	}

	public SummaryActivityTotals getSummaryActivityTotals(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	public SummaryActivityTotals getSummaryActivityTotals(String siteId, PrefsData prefsdata) {
		// TODO Auto-generated method stub
		return null;
	}

	public SummaryVisitsChartData getSummaryVisitsChartData(String siteId, String viewType) {
		// TODO Auto-generated method stub
		return null;
	}

	public SummaryVisitsTotals getSummaryVisitsTotals(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getTotalSiteActivity(String siteId, List<String> events, Date date, Date date2) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getTotalSiteActivity(String siteId, List<String> events) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getTotalSiteUniqueVisits(String siteId, Date date, Date date2) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getTotalSiteUniqueVisits(String siteId) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getTotalSiteUsers(String siteId) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getTotalSiteVisits(String siteId) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getTotalSiteVisits(String siteId, Date date, Date date2) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Set<String> getUsersWithVisits(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Stat> getVisitsTotalsStats(String siteId, Date date, Date date2, PagingPosition page, List<String> totalsBy, String sortBy, boolean sortAscending, int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isChartIn3D() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEnableSiteActivity() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEnableSiteVisits() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean isEnableResourceStats() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEventContextSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isItemLabelsVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isLastJobRunDateVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isServerWideStatsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isShowAnonymousAccessEvents() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isVisitsInfoAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	public void logEvent(Object object, String logAction) {
		// TODO Auto-generated method stub

	}

	public void logEvent(Object object, String logAction, String siteId, boolean oncePerSession) {
		// TODO Auto-generated method stub

	}

	public boolean setPreferences(String siteId, PrefsData prefsdata) {
		// TODO Auto-generated method stub
		return false;
	}

	public int getTotalResources(String siteId, boolean excludeFolders) {
		// TODO Auto-generated method stub
		return 0;
	}

}
