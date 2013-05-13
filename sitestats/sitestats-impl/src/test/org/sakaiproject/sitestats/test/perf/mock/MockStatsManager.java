package org.sakaiproject.sitestats.test.perf.mock;

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
import org.sakaiproject.user.api.User;

public class MockStatsManager implements StatsManager {

	@Override
	public boolean isEnableSiteVisits() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnableSiteActivity() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVisitsInfoAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnableResourceStats() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnableSitePresences() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getChartBackgroundColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isChartIn3D() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getChartTransparency() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isItemLabelsVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLastJobRunDateVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isServerWideStatsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isShowAnonymousAccessEvents() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnableReportExport() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSortUsersByDisplayName() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PrefsData getPreferences(String siteId, boolean includeUnselected) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setPreferences(String siteId, PrefsData prefsdata) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getResourceName(String ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResourceName(String ref, boolean includeLocationPrefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResourceImage(String ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResourceImageLibraryRelativePath(String ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResourceURL(String ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTotalResources(String siteId, boolean excludeFolders) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SummaryVisitsTotals getSummaryVisitsTotals(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SummaryActivityTotals getSummaryActivityTotals(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SummaryActivityTotals getSummaryActivityTotals(String siteId,
			PrefsData prefsdata) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SummaryVisitsChartData getSummaryVisitsChartData(String siteId,
			String viewType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SummaryActivityChartData getSummaryActivityChartData(String siteId,
			String viewType, String chartType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Stat> getEventStats(String siteId, List<String> events) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public List<EventStat> getEventStats(String siteId, List<String> events,
			String searchKey, Date iDate, Date fDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public List<CommonStatGrpByDate> getEventStatsGrpByDate(String siteId,
			List<String> events, Date iDate, Date fDate, List<String> userIds,
			boolean inverseUserSelection, PagingPosition page) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Stat> getEventStats(String siteId, List<String> events,
			Date iDate, Date fDate, List<String> userIds,
			boolean inverseUserSelection, PagingPosition page,
			List<String> totalsBy, String sortBy, boolean sortAscending,
			int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getEventStatsRowCount(String siteId, List<String> events,
			Date iDate, Date fDate, List<String> userIds,
			boolean inverseUserSelection, List<String> totalsBy) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Stat> getPresenceStats(String siteId, Date iDate, Date fDate,
			List<String> userIds, boolean inverseUserSelection,
			PagingPosition page, List<String> totalsBy, String sortBy,
			boolean sortAscending, int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPresenceStatsRowCount(String siteId, Date iDate, Date fDate,
			List<String> userIds, boolean inverseUserSelection,
			List<String> totalsBy) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Stat> getResourceStats(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public List<ResourceStat> getResourceStats(String siteId, String searchKey,
			Date iDate, Date fDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public List<CommonStatGrpByDate> getResourceStatsGrpByDateAndAction(
			String siteId, String resourceAction, List<String> resourceIds,
			Date iDate, Date fDate, List<String> userIds,
			boolean inverseUserSelection, PagingPosition page) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Stat> getResourceStats(String siteId, String resourceAction,
			List<String> resourceIds, Date iDate, Date fDate,
			List<String> userIds, boolean inverseUserSelection,
			PagingPosition page, List<String> totalsBy, String sortBy,
			boolean sortAscending, int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getResourceStatsRowCount(String siteId, String resourceAction,
			List<String> resourceIds, Date iDate, Date fDate,
			List<String> userIds, boolean inverseUserSelection,
			List<String> totalsBy) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<SiteVisits> getSiteVisits(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SiteVisits> getSiteVisits(String siteId, Date iDate, Date fDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SiteVisits> getSiteVisitsByMonth(String siteId, Date iDate,
			Date fDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTotalSiteVisits(String siteId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTotalSiteVisits(String siteId, Date iDate, Date fDate) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTotalSiteUniqueVisits(String siteId, Date iDate, Date fDate) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTotalSiteUniqueVisits(String siteId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTotalSiteUsers(String siteId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Set<String> getSiteUsers(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUserNameForDisplay(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUserNameForDisplay(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getUsersWithVisits(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Stat> getVisitsTotalsStats(String siteId, Date iDate,
			Date fDate, PagingPosition page, List<String> totalsBy,
			String sortBy, boolean sortAscending, int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SiteActivity> getSiteActivity(String siteId, List<String> events) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SiteActivity> getSiteActivity(String siteId,
			List<String> events, Date iDate, Date fDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SiteActivity> getSiteActivityByDay(String siteId,
			List<String> events, Date iDate, Date fDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SiteActivity> getSiteActivityByMonth(String siteId,
			List<String> events, Date iDate, Date fDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SiteActivityByTool> getSiteActivityByTool(String siteId,
			List<String> events, Date iDate, Date fDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SiteActivity> getSiteActivityGrpByDate(String siteId,
			List<String> events, Date iDate, Date fDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTotalSiteActivity(String siteId, List<String> events,
			Date iDate, Date fDate) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTotalSiteActivity(String siteId, List<String> events) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Stat> getActivityTotalsStats(String siteId,
			List<String> events, Date iDate, Date fDate, PagingPosition page,
			List<String> totalsBy, String sortBy, boolean sortAscending,
			int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getInitialActivityDate(String siteId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEventContextSupported() {
		// We got getContext baby.
		return true;
	}

	@Override
	public void logEvent(Object object, String logAction) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logEvent(Object object, String logAction, String siteId,
			boolean oncePerSession) {
		// TODO Auto-generated method stub

	}

}
