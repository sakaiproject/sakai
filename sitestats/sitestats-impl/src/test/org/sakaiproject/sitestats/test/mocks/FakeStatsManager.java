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
package org.sakaiproject.sitestats.test.mocks;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SiteActivity;
import org.sakaiproject.sitestats.api.SiteActivityByTool;
import org.sakaiproject.sitestats.api.SitePresenceTotal;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.SummaryActivityChartData;
import org.sakaiproject.sitestats.api.SummaryActivityTotals;
import org.sakaiproject.sitestats.api.SummaryVisitsChartData;
import org.sakaiproject.sitestats.api.SummaryVisitsTotals;
import org.sakaiproject.user.api.User;

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

	public int getResourceStatsRowCount(String siteId, String resourceAction, List<String> resourceIds, Date date, Date date2, List<String> userIds, boolean inverseUserSelection, List<String> totalsBy) {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<Stat> getLessonBuilderStats(final String siteId,
			final String resourceAction,
			final List<String> resourceIds,
			final Date iDate,
			final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page, 
			final List<String> totalsBy,
			final String sortBy, 
			final boolean sortAscending,
			final int maxResults) {
		return null;
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

	public boolean isEnableSitePresences() {
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

	public boolean isEnableReportExport() {
		// TODO Auto-generated method stub
		return true;
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

	public String getLessonPageTitle(long pageId) {
		return null;
	}

	@Override
	public int getTotalLessonPages(String siteId) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getTotalReadLessonPages(String siteId) {
		return 0;
	}

	public String getMostReadLessonPage(final String siteId) {
		return null;
	}

	public String getMostActiveLessonPageReader(final String siteId) {
		return null;
	}

	public List<Stat> getPresenceStats(String siteId, Date iDate, Date fDate, List<String> userIds, boolean inverseUserSelection, PagingPosition page, List<String> totalsBy, String sortBy,
			boolean sortAscending, int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPresenceStatsRowCount(String siteId, Date iDate, Date fDate, List<String> userIds, boolean inverseUserSelection, List<String> totalsBy) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Map<String, SitePresenceTotal> getPresenceTotalsForSite(final String siteId) {

		final Map<String, SitePresenceTotal> totals = new HashMap<String, SitePresenceTotal>();
		return totals;
	}

	public String getUserNameForDisplay(String userId) {
		return userId;
	}
	
	public String getUserNameForDisplay(User user) {
		if(isSortUsersByDisplayName()) {
			return user.getDisplayName();
		}else{
			return user.getSortName();
		}
	}

	public boolean isSortUsersByDisplayName() {
		return false;
	}

}
