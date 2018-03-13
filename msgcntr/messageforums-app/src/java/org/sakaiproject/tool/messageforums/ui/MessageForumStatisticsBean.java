/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/ui/MessageForumsSynopticBean.java $
 * $Id: MessageForumsSynopticBean.java $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.messageforums.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.api.app.messageforums.AnonymousManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MembershipManager;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.UserStatistics;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.app.messageforums.MembershipItem;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class MessageForumStatisticsBean {
	
	/**
	 * Used to store Statistic information on message forum per 
	 * per user
	 */
	
	public class DecoratedCompiledMessageStatistics {
		private String siteName;
		private String siteId;
		private String siteUser;
		private String siteUserId;
		private String siteAnonId = null;
		private boolean useAnonId;
		private int authoredForumsAmt;
		private int readForumsAmt;
		private int unreadForumsAmt;
		private Double percentReadForumsAmt;
		private DecoratedGradebookAssignment gradebookAssignment;

		public String getSiteName(){
			return this.siteName;
		}
		
		public void setSiteName(String newValue){
			this.siteName = newValue;
		}
		
		public String getSiteId(){
			return this.siteId;
		}
		
		public void setSiteId(String newValue){
			this.siteId = newValue;
		}
		
		public String getSiteUser(){
			return this.siteUser;
		}
		
		public void setSiteUser(String newValue){
			this.siteUser = newValue;
		}

		public String getSiteUserId(){
			return this.siteUserId;
		}
		
		public void setSiteUserId(String newValue){
			this.siteUserId = newValue;
		}

		public String getSiteAnonId(){
			return this.siteAnonId;
		}

		public void setSiteAnonId(String newValue){
			this.siteAnonId = newValue;
		}

		public boolean getUseAnonId(){
			return this.useAnonId;
		}

		public void setUseAnonId(boolean newValue){
			this.useAnonId = newValue;
		}

		public String getAnonAwareId(){
			return useAnonId ? siteAnonId : siteUser;
		}
		
		public int getAuthoredForumsAmt(){
			return this.authoredForumsAmt;
		}
		
		public void setAuthoredForumsAmt(int newValue){
			this.authoredForumsAmt = newValue;
		}
		
		public int getReadForumsAmt(){
			return this.readForumsAmt;
		}
		
		public void setReadForumsAmt(int newValue){
			this.readForumsAmt = newValue;
		}
		
		public int getUnreadForumsAmt(){
			return this.unreadForumsAmt;
		}
		
		public void setUnreadForumsAmt(int newValue){
			this.unreadForumsAmt = newValue;
		}
		
		public Double getPercentReadForumsAmt(){
			return this.percentReadForumsAmt;
		}
		
		public void setPercentReadFOrumsAmt(Double newValue){
			this.percentReadForumsAmt = newValue;
		}

		public DecoratedGradebookAssignment getGradebookAssignment() {
			return gradebookAssignment;
		}

		public void setGradebookAssignment(
				DecoratedGradebookAssignment gradebookAssignment) {
			this.gradebookAssignment = gradebookAssignment;
		}
	}
	/* === End DecoratedCompiledMessageStatistics === */
	
	public class DecoratedGradebookAssignment{
		private String userUuid;
		private String name;
		private String score;
		private String comment;
		private String pointsPossible;
		private boolean allowedToGrade = false;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getScore() {
			return score;
		}
		public void setScore(String score) {
			this.score = score;
		}
		public String getComment() {
			return comment;
		}
		public void setComment(String comment) {
			this.comment = comment;
		}
		public String getPointsPossible() {
			return pointsPossible;
		}
		public void setPointsPossible(String pointsPossible) {
			this.pointsPossible = pointsPossible;
		}
		public boolean isAllowedToGrade() {
			return allowedToGrade;
		}
		public void setAllowedToGrade(boolean allowedToGrade) {
			this.allowedToGrade = allowedToGrade;
		}
		public String getUserUuid() {
			return userUuid;
		}
		public void setUserUuid(String userUuid) {
			this.userUuid = userUuid;
		}		
	}
	
	public class DecoratedCompiledStatisticsByTopic{
		private Long topicId;
		private Long forumId;
		private String forumTitle;
		private String topicTitle;
		private Date forumDate;
		private Date topicDate;
		private int authoredForumsAmt;
		private int totalTopicMessages;
		private int unreadTopicMessages;
		private Double percentReadForumsAmt;
		private DecoratedGradebookAssignment gradebookAssignment;
		
		public String getForumTitle() {
			return forumTitle;
		}
		public void setForumTitle(String forumTitle) {
			this.forumTitle = forumTitle;
		}
		public String getTopicTitle() {
			return topicTitle;
		}
		public void setTopicTitle(String topicTitle) {
			this.topicTitle = topicTitle;
		}
		public Date getForumDate() {
			return forumDate;
		}
		public void setForumDate(Date forumDate) {
			this.forumDate = forumDate;
		}
		public int getAuthoredForumsAmt() {
			return authoredForumsAmt;
		}
		public void setAuthoredForumsAmt(int authoredForumsAmt) {
			this.authoredForumsAmt = authoredForumsAmt;
		}
		public Double getPercentReadForumsAmt() {
			return percentReadForumsAmt;
		}
		public void setPercentReadForumsAmt(Double percentReadForumsAmt) {
			this.percentReadForumsAmt = percentReadForumsAmt;
		}
		public int getTotalTopicMessages() {
			return totalTopicMessages;
		}
		public void setTotalTopicMessages(int totalTopicMessages) {
			this.totalTopicMessages = totalTopicMessages;
		}
		public int getUnreadTopicMessages() {
			return unreadTopicMessages;
		}
		public void setUnreadTopicMessages(int unreadTopicMessages) {
			this.unreadTopicMessages = unreadTopicMessages;
		}
		public Date getTopicDate() {
			return topicDate;
		}
		public void setTopicDate(Date topicDate) {
			this.topicDate = topicDate;
		}
		public Long getTopicId() {
			return topicId;
		}
		public void setTopicId(Long topicId) {
			this.topicId = topicId;
		}
		public Long getForumId() {
			return forumId;
		}
		public void setForumId(Long forumId) {
			this.forumId = forumId;
		}
		public DecoratedGradebookAssignment getGradebookAssignment() {
			return gradebookAssignment;
		}
		public void setGradebookAssignment(
				DecoratedGradebookAssignment gradebookAssignment) {
			this.gradebookAssignment = gradebookAssignment;
		}	
	}
	
	/** Decorated Bean to store stats for user **/
	public DecoratedCompiledMessageStatistics userInfo = null;
	public UserStatistics userAuthoredInfo = null;
	
	private Map<String, MembershipItem> courseMemberMap;
	protected boolean ascending = true;
	protected boolean ascendingForUser = false;
	protected boolean ascendingForUser2 = false;
	protected boolean ascendingForUser3 = false;
	protected boolean ascendingForAllTopics = false;
	protected String sortBy = NAME_SORT;
	protected String sortByUser = FORUM_DATE_SORT;
	protected String sortByUser2 = FORUM_DATE_SORT2;
	protected String sortByUser3 = FORUM_DATE_SORT3;
	protected String sortByAllTopics = ALL_TOPICS_FORUM_TITLE_SORT;

	private static final String LIST_PAGE = "dfStatisticsList";
	private static final String NAME_SORT = "sort_by_name";
	private static final String AUTHORED_SORT = "sort_by_num_authored";
	private static final String READ_SORT = "sort_by_num_read";
	private static final String UNREAD_SORT = "sort_by_num_unread";
	private static final String PERCENT_READ_SORT = "sort_by_percent_read";
	private static final String GRADE_SORT = "sort_by_grade";
	private static final String SITE_USER_ID = "siteUserId";
	private static final String FORUM_TITLE_SORT = "sort_by_forum_title";
	private static final String TOPIC_TITLE_SORT = "sort_by_topic_title";
	private static final String FORUM_DATE_SORT = "sort_by_forum_date";
	private static final String FORUM_SUBJECT_SORT = "sort_by_forum_subject_2";
	private static final String FORUM_TITLE_SORT2 = "sort_by_forum_title_2";
	private static final String TOPIC_TITLE_SORT2 = "sort_by_topic_title_2";
	private static final String FORUM_DATE_SORT2 = "sort_by_forum_date_2";
	private static final String FORUM_SUBJECT_SORT2 = "sort_by_forum_subject_2";
	private static final String FORUM_DATE_SORT3 = "sort_by_forum_date_3";
	private static final String TOPIC_TITLE_SORT3 = "sort_by_forum_subject_3";
	private static final String ALL_TOPICS_FORUM_TITLE_SORT = "sort_by_all_topics_forum_title";
	private static final String ALL_TOPICS_FORUM_DATE_SORT = "sort_by_all_topics_forum_date";
	private static final String ALL_TOPICS_TOPIC_DATE_SORT = "sort_by_all_topics_topic_date";
	private static final String ALL_TOPICS_TOPIC_TITLE_SORT = "sort_by_all_topics_topic_title";
	private static final String ALL_TOPICS_TOPIC_TOTAL_MESSAGES_SORT = "sort_by_all_topics_topic_total_messages";	
	private static final String TOPIC_ID = "topicId";
	private static final String FORUM_ID = "forumId";
	private static final String TOPIC_TITLE = "topicTitle";
	private static final String FORUM_TITLE = "forumTitle";

	private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";

	private static final String FORUM_STATISTICS = "dfStatisticsList";
	private static final String FORUM_STATISTICS_BY_ALL_TOPICS = "dfStatisticsListByAllTopics";
	private static final String FORUM_STATISTICS_BY_TOPIC = "dfStatisticsListByTopic";
	private static final String FORUM_STATISTICS_USER = "dfStatisticsUser";
	private static final String FORUM_STATISTICS_ALL_AUTHORED_MSG = "dfStatisticsAllAuthoredMessageForOneUser";
	private static final String FORUM_STATISTICS_MSG = "dfStatisticsFullTextForOne";

	public String selectedSiteUserId = null;
	public String selectedSiteUser = null;
	public String selectedMsgId= null;
	public String selectedMsgSubject= null;
	public String selectedForumTitle= null;
	public String selectedTopicTitle= null;
	public String selectedTopicId= null;
	public String selectedAllTopicsTopicId = null;
	public String selectedAllTopicsForumId = null;
	public String selectedAllTopicsTopicTitle = null;
	public String selectedAllTopicsForumTitle = null;

	private String buttonUserName;
	private boolean isFirstParticipant = false;
	private boolean isLastParticipant = false;
	
	//Comparatibles
	public static Comparator nameComparatorAsc;
	public static Comparator authoredComparatorAsc;
	public static Comparator readComparatorAsc;
	public static Comparator unreadComparatorAsc;
	public static Comparator percentReadComparatorAsc;
	public static Comparator GradeComparatorAsc;
	public static Comparator nameComparatorDesc;
	public static Comparator authoredComparatorDesc;
	public static Comparator readComparatorDesc;
	public static Comparator unreadComparatorDesc;
	public static Comparator percentReadComparatorDesc;
	public static Comparator dateComparaterDesc;
	public static Comparator GradeComparatorDesc;
	public static Comparator forumTitleComparatorAsc;
	public static Comparator forumTitleComparatorDesc;
	public static Comparator topicTitleComparatorAsc;
	public static Comparator topicTitleComparatorDesc;
	public static Comparator forumDateComparatorAsc;
	public static Comparator forumDateComparatorDesc;
	public static Comparator forumSubjectComparatorAsc;
	public static Comparator forumSubjectComparatorDesc;
	public static Comparator AllTopicsForumDateComparatorAsc;
 	public static Comparator AllTopicsTopicDateComparatorAsc;
 	public static Comparator AllTopicsTopicTitleComparatorAsc;
 	public static Comparator AllTopicsTopicTotalMessagesComparatorAsc;
 	public static Comparator AllTopicsForumTitleComparatorAsc;
 	public static Comparator AllTopicsForumDateComparatorDesc;
 	public static Comparator AllTopicsTopicDateComparatorDesc;
 	public static Comparator AllTopicsTopicTitleComparatorDesc;

 	public static Comparator AllTopicsTopicTotalMessagesComparatorDesc;
 	public static Comparator AllTopicsForumTitleComparatorDesc;

 	private static final String DEFAULT_GB_ITEM = "Default_0";
 	private static final String DEFAULT_ALL_GROUPS = "all_participants_desc";
 	private static final String SELECT_ASSIGN = "stat_forum_no_gbitem";
 	private boolean gradebookExistChecked = false;
 	private boolean gradebookExist = false;
 	private List<SelectItem> assignments = null;
 	private List<SelectItem> groups = null;
 	private String selectedAssign = DEFAULT_GB_ITEM;
 	private String selectedGroup = DEFAULT_GB_ITEM;
 	private boolean gradebookItemChosen = false;
 	private String selAssignName;
 	private String gbItemPointsPossible;
 	private boolean gradeByPoints;
 	private boolean gradeByPercent;
 	private boolean gradeByLetter;
 	private List<DecoratedCompiledMessageStatistics> gradeStatistics = null;
 	private static final String NO_ASSGN = "cdfm_no_assign_for_grade";
 	private static final String ALERT = "cdfm_alert";
 	private static final String GRADE_SUCCESSFUL = "cdfm_grade_successful";
 	private static final String GRADE_GREATER_ZERO = "cdfm_grade_greater_than_zero";
 	private static final String GRADE_DECIMAL_WARN = "cdfm_grade_decimal_warn";
 	private static final String GRADE_INVALID_GENERIC = "cdfm_grade_invalid_warn";

	private boolean m_displayAnonIds; // this will be true in a pure-anon scenario

	/** Needed if within a site so we only need stats for this site */
	private MessageForumsMessageManager messageManager;
	
	/** Needed to get topics if tool within a site */
	private DiscussionForumManager forumManager;
	
	private MembershipManager membershipManager;

	/** Manages anonymous IDs */
	private AnonymousManager anonymousManager;

	private ToolManager toolManager;
	private UserDirectoryService userDirectoryService;
	private SecurityService securityService;
	private EventTrackingService eventTrackingService;
	private SiteService siteService;
	private SessionManager sessionManager;
	
	/** Needed to determine if user has read permission of topic */
	private UIPermissionsManager uiPermissionsManager;
	
	public void setMessageManager(MessageForumsMessageManager messageManager){
		this.messageManager = messageManager;
	}
	
	public void setForumManager(DiscussionForumManager forumManager){
		this.forumManager = forumManager;
	}

	public void setAnonymousManager(AnonymousManager anonymousManager)
	{
		this.anonymousManager = anonymousManager;
	}

	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}
	
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public String getSelectedSiteUserId(){
		return this.selectedSiteUserId;
	}
	
	public String getSelectedSiteUser(){
		return this.selectedSiteUser;
	}
	
	public void setUiPermissionsManager(UIPermissionsManager uiPermissionsManager){
		this.uiPermissionsManager = uiPermissionsManager;
	}
	
	public void setMembershipManager(MembershipManager membershipManager)
	{
		this.membershipManager = membershipManager;
	}
	
	
	public DecoratedCompiledMessageStatistics getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(DecoratedCompiledMessageStatistics userInfo) {
		this.userInfo = userInfo;
	}
	
	
	public List getAllUserStatistics(){
		selectedAllTopicsTopicId = null;
		selectedAllTopicsForumId = null;
		selectedAllTopicsForumTitle = null;
		selectedAllTopicsTopicTitle = null;
		//clear any gradebook info:
		resetGradebookVariables();

		// Determine whether the site is a pure anonymous scenario (Ie. all topics in the site are anonymous)
		String siteId = toolManager.getCurrentPlacement().getContext();
		List<Topic> allTopics = forumManager.getTopicsInSite(siteId);
		// If it's empty, reveal identities, otherwise assume it's pure anonymous until we find a topic on which we need to reveal IDs
		m_displayAnonIds = !allTopics.isEmpty();
		for (Topic topic : allTopics)
		{
			if (!isUseAnonymousId(topic))
			{
				m_displayAnonIds = false;
				break;
			}
		}
		Map<String, String> userIdAnonIdMap = new HashMap<>();
		if (m_displayAnonIds)
		{
			userIdAnonIdMap.putAll(anonymousManager.getUserIdAnonIdMap(siteId));
		}

		Map<String, Integer> studentTotalCount = getStudentTopicMessagCount();
		
		Map<String, DecoratedCompiledMessageStatistics> tmpStatistics = new TreeMap<String, DecoratedCompiledMessageStatistics>();

		// process the returned read statistics for the students to get them sorted by user id
		List<Object[]> studentReadStats = messageManager.findReadMessageCountForAllStudents();
		for (Object[] readStat: studentReadStats) {
			DecoratedCompiledMessageStatistics userStats = tmpStatistics.get(readStat[0]);
			if (userStats == null) {
				userStats = new DecoratedCompiledMessageStatistics();
				tmpStatistics.put((String)readStat[0], userStats);
			}
			Integer totalForum = 0;
			if(studentTotalCount.containsKey((String) readStat[0])){
				totalForum = studentTotalCount.get((String) readStat[0]);
			}
			if (totalForum > 0) {
				userStats.setReadForumsAmt(((Long)readStat[1]).intValue());
			} else {
				userStats.setReadForumsAmt(0);				
			}
		}
		
		// process the returned authored statistics for the students to get them sorted by user id
		List<Object[]> studentAuthoredStats = messageManager.findAuthoredMessageCountForAllStudents();
		for (Object[] authoredStat: studentAuthoredStats) {
			DecoratedCompiledMessageStatistics userStats = tmpStatistics.get(authoredStat[0]);
			if (userStats == null) {
				userStats = new DecoratedCompiledMessageStatistics();
				tmpStatistics.put((String)authoredStat[0], userStats);
			}
			Integer totalForum = 0;
			if(studentTotalCount.containsKey((String) authoredStat[0])){
				totalForum = studentTotalCount.get((String) authoredStat[0]);
			}
			if (totalForum > 0) {
				userStats.setAuthoredForumsAmt(((Long)authoredStat[1]).intValue());
			} else {
				userStats.setAuthoredForumsAmt(0);
			}
		}

		// now process the users from the list of site members to add display information
		// this will also prune the list of members so only the papropriate ones are displayed
		final List<DecoratedCompiledMessageStatistics> statistics = new ArrayList<DecoratedCompiledMessageStatistics>();

		if(courseMemberMap == null){
			courseMemberMap = membershipManager.getAllCourseUsersAsMap();
		}
		List members = membershipManager.convertMemberMapToList(courseMemberMap);
		
		for (Iterator i = members.iterator(); i.hasNext();) {
			MembershipItem item = (MembershipItem) i.next();
			
			if (null != item.getUser()) {
				userInfo = tmpStatistics.get(item.getUser().getId());
				if (userInfo == null) {
					userInfo = new DecoratedCompiledMessageStatistics();
					userInfo.setReadForumsAmt(0);
					userInfo.setAuthoredForumsAmt(0);
				}
				
				String userId = item.getUser().getId();
				userInfo.setSiteUserId(userId);
				userInfo.setSiteUser(item.getName());
				if (m_displayAnonIds)
				{
					String anonId = userIdAnonIdMap.get(userId);
					if (StringUtils.isEmpty(anonId))
					{
						// AnonymousMapping isn't in the database yet. Create it:
						anonId = anonymousManager.getOrCreateAnonId(siteId, userId);
					}
					userInfo.setSiteAnonId(anonId);
				}
				userInfo.setUseAnonId(m_displayAnonIds);

				Integer totalForum = 0;
				if(studentTotalCount.containsKey(item.getUser().getId())){
					totalForum = studentTotalCount.get(item.getUser().getId());
				}
				if (totalForum > 0) {
					userInfo.setUnreadForumsAmt(totalForum - userInfo.getReadForumsAmt());
					userInfo.setPercentReadFOrumsAmt((double)userInfo.getReadForumsAmt() / (double)totalForum);
				} else {
					userInfo.setUnreadForumsAmt(0);
					userInfo.setPercentReadFOrumsAmt((double)0);
				}

				statistics.add(userInfo);
			}
		}

		sortStatistics(statistics);
		return statistics;
	}

	Map<String, List> userAuthoredStatisticsCache = new HashMap<String, List>();
	public List getUserAuthoredStatistics(){
		if(!userAuthoredStatisticsCache.containsKey(selectedSiteUserId)){
			final List<UserStatistics> statistics = new ArrayList<UserStatistics>();

			if((selectedAllTopicsTopicId == null || "".equals(selectedAllTopicsTopicId))
					&& (selectedAllTopicsForumId != null && !"".equals(selectedAllTopicsForumId))){
				List<UserStatistics> allStatistics = messageManager.findAuthoredStatsForStudentByForumId(selectedSiteUserId, Long.parseLong(selectedAllTopicsForumId));
				statistics.addAll(filterToAnonAwareStatistics(allStatistics));
			}else if(selectedAllTopicsTopicId != null && !"".equals(selectedAllTopicsTopicId)){
				statistics.addAll(messageManager.findAuthoredStatsForStudentByTopicId(selectedSiteUserId, Long.parseLong(selectedAllTopicsTopicId)));
			}else{
				List<UserStatistics> allStatistics = messageManager.findAuthoredStatsForStudent(selectedSiteUserId);
				statistics.addAll(filterToAnonAwareStatistics(allStatistics));
			}
			sortStatisticsByUser(statistics);
			userAuthoredStatisticsCache.put(selectedSiteUserId, statistics);
		} else {
		    // sort the statistics
		    List<UserStatistics> statistics = userAuthoredStatisticsCache.get(selectedSiteUserId);
		    sortStatisticsByUser(statistics);
		    userAuthoredStatisticsCache.put(selectedSiteUserId, statistics);
		}
		
		return userAuthoredStatisticsCache.get(selectedSiteUserId);
	}

	/**
	 * Filters out statistics associcated with anonymous topics for which the user is not permitted to see IDs
	 */
	private List<UserStatistics> filterToAnonAwareStatistics(List<UserStatistics> statistics)
	{
		if (!isAnonymousEnabled() || m_displayAnonIds)
		{
			return statistics;
		}
		List<UserStatistics> filtered = new ArrayList<>();
		for (UserStatistics stats : statistics)
		{
			Topic topic = forumManager.getTopicById(Long.parseLong(stats.getTopicId()));
			if (!isUseAnonymousId(topic))
			{
				filtered.add(stats);
			}
		}
		return filtered;
	}
	
	public List getTopicStatistics(){
		final List<DecoratedCompiledMessageStatistics> statistics = new ArrayList<DecoratedCompiledMessageStatistics>();
		m_displayAnonIds = false;
				
		if((selectedAllTopicsTopicId != null && !"".equals(selectedAllTopicsTopicId)) || selectedAllTopicsForumId != null && !"".equals(selectedAllTopicsForumId)){
			Map<String, Integer> userMessageTotal = new HashMap<String, Integer>();
			DiscussionForum forum = forumManager.getForumByIdWithTopics(Long.parseLong(selectedAllTopicsForumId));
			Map<String, Boolean> overridingPermissionMap = getOverridingPermissionsMap();
			if(selectedAllTopicsTopicId != null && !"".equals(selectedAllTopicsTopicId)){
				DiscussionTopic currTopic = forumManager.getTopicById(Long.parseLong(selectedAllTopicsTopicId));
				m_displayAnonIds = isUseAnonymousId(currTopic);
				int topicMessages = messageManager.findMessageCountByTopicId(Long.parseLong(selectedAllTopicsTopicId));
				userMessageTotal = getStudentTopicMessagCount(forum, currTopic, topicMessages, overridingPermissionMap);
			}else{
				//totalForum = messageManager.findMessageCountByForumId(Long.parseLong(selectedAllTopicsForumId));
				List<Object[]> totalTopcsCountList = messageManager.findMessageCountByForumId(forum.getId());
				Map<Long, Integer> totalTopcsCountMap = new HashMap<Long, Integer>();
				for(Object[] objArr : totalTopcsCountList){
					totalTopcsCountMap.put((Long) objArr[0], ((Long) objArr[1]).intValue());
				}
				// if there are no topics, reveal IDs; otherwise assume we're in a pure anonymous scenario (all topics are anonymous), if we find one that isn't anonymous, we'll flip it to false
				m_displayAnonIds = !forum.getTopicsSet().isEmpty();
				for (Iterator itor = forum.getTopicsSet().iterator(); itor.hasNext(); ) {
					DiscussionTopic currTopic = (DiscussionTopic)itor.next();
					if (m_displayAnonIds && (!isUseAnonymousId(currTopic)))
					{
						m_displayAnonIds = false;
					}

					Integer topicCount = totalTopcsCountMap.get(currTopic.getId());
					if(topicCount == null){
						topicCount = 0;
					}

					Map<String, Integer> totalTopicCountMap = getStudentTopicMessagCount(forum, currTopic, topicCount, overridingPermissionMap);
					for(Entry<String, Integer> entry : totalTopicCountMap.entrySet()){
						Integer studentCount = entry.getValue();
						if(userMessageTotal.containsKey(entry.getKey())){
							studentCount += userMessageTotal.get(entry.getKey());
						}
						userMessageTotal.put(entry.getKey(), studentCount);
					}
				}
			}
			
			
			
			
			Map<String, DecoratedCompiledMessageStatistics> tmpStatistics = new TreeMap<String, DecoratedCompiledMessageStatistics>();

			// process the returned read statistics for the students to get them sorted by user id
			List<Object[]> studentReadStats;
			if(selectedAllTopicsTopicId != null && !"".equals(selectedAllTopicsTopicId)){
				studentReadStats = messageManager.findReadMessageCountForAllStudentsByTopicId(Long.parseLong(selectedAllTopicsTopicId));
			}else{
				studentReadStats = messageManager.findReadMessageCountForAllStudentsByForumId(Long.parseLong(selectedAllTopicsForumId));
			}
			
			for (Object[] readStat: studentReadStats) {
				DecoratedCompiledMessageStatistics userStats = tmpStatistics.get(readStat[0]);
				if (userStats == null) {
					userStats = new DecoratedCompiledMessageStatistics();
					tmpStatistics.put((String)readStat[0], userStats);
				}
				Integer totalForum = 0;
				if(userMessageTotal.containsKey((String) readStat[0])){
					totalForum = userMessageTotal.get((String) readStat[0]);
				}
				if (totalForum > 0) {
					userStats.setReadForumsAmt(((Long)readStat[1]).intValue());
				} else {
					userStats.setReadForumsAmt(0);				
				}
			}

			// process the returned authored statistics for the students to get them sorted by user id
			List<Object[]> studentAuthoredStats;
			if(selectedAllTopicsTopicId != null && !"".equals(selectedAllTopicsTopicId)){
				studentAuthoredStats = messageManager.findAuthoredMessageCountForAllStudentsByTopicId(Long.parseLong(selectedAllTopicsTopicId));
			}else{
				studentAuthoredStats = messageManager.findAuthoredMessageCountForAllStudentsByForumId(Long.parseLong(selectedAllTopicsForumId));
			}
			
			for (Object[] authoredStat: studentAuthoredStats) {
				DecoratedCompiledMessageStatistics userStats = tmpStatistics.get(authoredStat[0]);
				if (userStats == null) {
					userStats = new DecoratedCompiledMessageStatistics();
					tmpStatistics.put((String)authoredStat[0], userStats);
				}
				Integer totalForum = 0;
				if(userMessageTotal.containsKey((String) authoredStat[0])){
					totalForum = userMessageTotal.get((String) authoredStat[0]);
				}
				if (totalForum > 0) {
					userStats.setAuthoredForumsAmt(((Long)authoredStat[1]).intValue());
				} else {
					userStats.setAuthoredForumsAmt(0);
				}
			}

			// now process the users from the list of site members to add display information
			// this will also prune the list of members so only the papropriate ones are displayed
			courseMemberMap = membershipManager.getAllCourseUsersAsMap();
			Map convertedMap = convertMemberMapToUserIdMap(courseMemberMap);
			Map<String, DecoratedGradebookAssignment> studentGradesMap = getGradebookAssignment();
			List<DecoratedUser> dUsers = new ArrayList();
			
			if(!DEFAULT_GB_ITEM.equals(selectedGroup)){
				try{
					Site currentSite = siteService.getSite(toolManager.getCurrentPlacement().getContext());		
					if(currentSite.hasGroups()){
						Group group = currentSite.getGroup(selectedGroup);
						
						for (Iterator i = group.getMembers().iterator(); i.hasNext();) {
							Member item = (Member) i.next();
							if(convertedMap.containsKey(item.getUserId())){
								MembershipItem memItem = (MembershipItem) convertedMap.get(item.getUserId());
								if (null != memItem.getUser()) {
									dUsers.add(new DecoratedUser(memItem.getUser().getId(), memItem.getName()));
								}
							}
						}
					}
				}catch (IdUnusedException e) {
					log.error(e.getMessage());
				}
			}else{
				for (Iterator i = courseMemberMap.entrySet().iterator(); i.hasNext();) {
					Entry entry = (Entry) i.next();
					MembershipItem item = (MembershipItem) entry.getValue();
					if (null != item.getUser()) {
						dUsers.add(new DecoratedUser(item.getUser().getId(), item.getName()));
					}
				}
			}

			Map<String, String> userIdAnonIdMap = null;
			// If appropriate, get the map of userIds -> anonIds in one query up front
			if (m_displayAnonIds)
			{
				String siteId = toolManager.getCurrentPlacement().getContext();
				List<String> userIds = new ArrayList<>();
				for (DecoratedUser dUser : dUsers)
				{
					userIds.add(dUser.getId());
				}
				userIdAnonIdMap = anonymousManager.getOrCreateUserIdAnonIdMap(siteId, userIds);
			}

			for (Iterator i = dUsers.iterator(); i.hasNext();) {
				DecoratedUser item = (DecoratedUser) i.next();
				userInfo = tmpStatistics.get(item.getId());
				if (userInfo == null) {
					userInfo = new DecoratedCompiledMessageStatistics();
					userInfo.setReadForumsAmt(0);
					userInfo.setAuthoredForumsAmt(0);
				}

				userInfo.setSiteUserId(item.getId());
				userInfo.setSiteUser(item.getName());
				// Set up the anonId for this userInfo if appropriate
				userInfo.setUseAnonId(m_displayAnonIds);
				if (m_displayAnonIds)
				{
					userInfo.setSiteAnonId(userIdAnonIdMap.get(item.getId()));
				}
				Integer totalForum = 0;
				if(userMessageTotal.containsKey(item.getId())){
					totalForum = userMessageTotal.get(item.getId());
				}
				if (totalForum > 0) {
					userInfo.setUnreadForumsAmt(totalForum - userInfo.getReadForumsAmt());
					userInfo.setPercentReadFOrumsAmt((double)userInfo.getReadForumsAmt() / (double)totalForum);
				} else {
					userInfo.setUnreadForumsAmt(0);
					userInfo.setPercentReadFOrumsAmt((double)0);
				}

				DecoratedGradebookAssignment decoGradeAssgn = studentGradesMap.get(item.getId());
				if(decoGradeAssgn == null){
					decoGradeAssgn = new DecoratedGradebookAssignment();
					decoGradeAssgn.setAllowedToGrade(false);
					decoGradeAssgn.setUserUuid(item.getId());
				}

				userInfo.setGradebookAssignment(decoGradeAssgn);

				statistics.add(userInfo);
			}

			sortStatistics(statistics);
		}
		
		gradeStatistics = statistics;
		// gradeStatistics is updated; keep track of which gb assignment / group is being represented
		m_gradeStatisticsAssign = selectedAssign;
		m_gradeStatisticsGroup = selectedGroup;
		m_comparator = determineComparator();
		return gradeStatistics;
	}

	// Keep track of the gb assignment / group that's being represented by gradeStatistics
	String m_gradeStatisticsAssign = null;
	String m_gradeStatisticsGroup = null;
	Comparator m_comparator = null;
	/**
	 * Retreives topic statistics while determining whether to retrieve them from the cache or by invoking getTopicStatistics()
	 */
	public List getGradeStatisticsForStatsListByTopic()
	{
		// Determine if something has changed that warrants the cached gradeStatistics to be refreshed
		// Has the selected gradebook assignment or the selected group changed since the gradeStatistics were cached?
		boolean refreshCachedStatistics = !StringUtils.equals(m_gradeStatisticsAssign, selectedAssign) || !StringUtils.equals(m_gradeStatisticsGroup, selectedGroup);
		if (!refreshCachedStatistics)
		{
			// Are we sorting on a different column?
			Comparator comparator = determineComparator();
			if (comparator != null && !comparator.equals(m_comparator))
			{
				refreshCachedStatistics = true;
			}
		}

		if (refreshCachedStatistics)
		{
			// The selected gradebook assignment or group has changed; our gradeStatistics need to be updated
			getTopicStatistics();
		}
		return gradeStatistics;
	}
	
	private Map convertMemberMapToUserIdMap(Map origMap){
		Map returnMap = new HashMap();
		for (Iterator i = origMap.entrySet().iterator(); i.hasNext();) {
			Entry entry = (Entry) i.next();
			MembershipItem item = (MembershipItem) entry.getValue();
			if (null != item.getUser()) {
				returnMap.put(item.getUser().getId(), item);
			}
		}
		return returnMap;
	}
	
	public List getAllTopicStatistics(){
		//clear any gradebook info:
		resetGradebookVariables();
		
		final Map<Long, DecoratedCompiledStatisticsByTopic> statisticsMap = new HashMap<Long, DecoratedCompiledStatisticsByTopic>();
		
		List<DiscussionForum> tempForums = forumManager.getForumsForMainPage();
		Set<Long> topicIdsForCounts = new HashSet<Long>();
		
		DecoratedCompiledStatisticsByTopic dCompiledStatsByTopic = null;
		//grab the info for forums and topics in the site
		for (DiscussionForum forum: tempForums) {
			for (Iterator itor = forum.getTopicsSet().iterator(); itor.hasNext(); ) {
				 DiscussionTopic currTopic = (DiscussionTopic)itor.next();	 
				 
				 dCompiledStatsByTopic = new DecoratedCompiledStatisticsByTopic();
				 dCompiledStatsByTopic.setForumDate(forum.getModified());
				 dCompiledStatsByTopic.setForumId(forum.getId());
				 dCompiledStatsByTopic.setTopicDate(currTopic.getModified());
				 dCompiledStatsByTopic.setForumTitle(forum.getTitle());
				 dCompiledStatsByTopic.setTopicTitle(currTopic.getTitle());
				 dCompiledStatsByTopic.setTopicId(currTopic.getId());
				 
				 statisticsMap.put(currTopic.getId(), dCompiledStatsByTopic);
				 
				 topicIdsForCounts.add(currTopic.getId());				 
			}
		}
		//get counts
		List<Object[]> topicMessageCounts = forumManager.getMessageCountsForMainPage(topicIdsForCounts);
		for (Object[] counts: topicMessageCounts) {
			dCompiledStatsByTopic = statisticsMap.get(counts[0]);
			if(dCompiledStatsByTopic != null){
				dCompiledStatsByTopic.setTotalTopicMessages(((Long) counts[1]).intValue());
			}
		}
		
		final List<DecoratedCompiledStatisticsByTopic> statistics = new ArrayList(statisticsMap.values());
		
		sortStatisticsByAllTopics(statistics);
		return statistics;		
	}
		
	public List getUserAuthoredStatistics2(){
		final List<UserStatistics> statistics = new ArrayList<UserStatistics>();

		List<Message> messages;
		if((selectedAllTopicsTopicId == null || "".equals(selectedAllTopicsTopicId))
				&& (selectedAllTopicsForumId != null && !"".equals(selectedAllTopicsForumId))){
			List<Message> allMessages = messageManager.findAuthoredMessagesForStudentByForumId(selectedSiteUserId, Long.parseLong(selectedAllTopicsForumId));
			messages = filterToAnonAwareMessages(allMessages);
		}else if(selectedAllTopicsTopicId != null && !"".equals(selectedAllTopicsTopicId)){
			messages = messageManager.findAuthoredMessagesForStudentByTopicId(selectedSiteUserId, Long.parseLong(selectedAllTopicsTopicId));
		}else{
			List<Message> allMessages = messageManager.findAuthoredMessagesForStudent(selectedSiteUserId);
			messages = filterToAnonAwareMessages(allMessages);
		}
		 
		if (messages == null) return statistics;

		for (Message msg: messages) {
			Message mesWithAttach = (Message)messageManager.getMessageByIdWithAttachments(msg.getId());
			List decoAttachList = new ArrayList();
			List attachList = mesWithAttach.getAttachments();
			if(attachList != null ) {
				for(int i=0; i<attachList.size(); i++)
				{
					DecoratedAttachment decoAttach = new DecoratedAttachment((Attachment)attachList.get(i));
					decoAttachList.add(decoAttach);
				}
			}
			
			userAuthoredInfo = new UserStatistics();
			userAuthoredInfo.setSiteUserId(selectedSiteUserId);
			userAuthoredInfo.setForumTitle(msg.getTopic().getOpenForum().getTitle());
			userAuthoredInfo.setTopicTitle(msg.getTopic().getTitle());
			userAuthoredInfo.setForumDate(msg.getCreated());
			userAuthoredInfo.setForumSubject(msg.getTitle());
			userAuthoredInfo.setMessage(msg.getBody());
			userAuthoredInfo.setMsgId(Long.toString(msg.getId()));
			userAuthoredInfo.setTopicId(Long.toString(msg.getTopic().getId()));
			userAuthoredInfo.setForumId(Long.toString(msg.getTopic().getOpenForum().getId()));
			userAuthoredInfo.setMsgDeleted(msg.getDeleted());
			userAuthoredInfo.setDecoAttachmentsList(decoAttachList);
			userAuthoredInfo.setMessage(msg.getBody());	
			messageManager.markMessageReadForUser(msg.getTopic().getId(), msg.getId(), true, getCurrentUserId());
			
			statistics.add(userAuthoredInfo);
		}

		sortStatisticsByUser3(statistics);
		return statistics;
	}
	
	/**
	 * Filters out messages associated with anonymous topics for which the user is not permitted to see IDs
	 */
	private List<Message> filterToAnonAwareMessages(List<Message> messages)
	{
		if (!isAnonymousEnabled() || m_displayAnonIds)
		{
			return messages;
		}

		List<Message> filtered = new ArrayList<Message>();
		// Map topics to value of 'isUseAnonymousId(Topic)' to prevent redundant queries
		Map<Topic, Boolean> topicToUseAnon = new HashMap<>();
		for (Message message : messages)
		{
			Topic topic = message.getTopic();
			Boolean useAnon = topicToUseAnon.get(topic);
			if (useAnon == null)
			{
				useAnon = Boolean.valueOf(isUseAnonymousId(topic));
				topicToUseAnon.put(topic, useAnon);
			}
			if (!useAnon)
			{
				// User can see IDs for this topic; add it to the result set
				filtered.add(message);
			}
		}
		return filtered;
	}

	private String getCurrentUserId() {
		String currentUserId = sessionManager.getCurrentSessionUserId();;
		return currentUserId;
	}

	public List getUserSubjectMsgBody(){
		final List statistics = new ArrayList();

		if(selectedMsgId != null){
			try{
				Long msgId = Long.valueOf(selectedMsgId);			

				Message mesWithAttach = (Message)messageManager.getMessageByIdWithAttachments(msgId);
				Topic t = forumManager.getTopicById(mesWithAttach.getTopic().getId());
				DiscussionForum d = forumManager.getForumById(t.getOpenForum().getId());


				List decoAttachList = new ArrayList();
				List attachList = mesWithAttach.getAttachments();
				if(attachList != null ) {
					for(int i=0; i<attachList.size(); i++)
					{
						DecoratedAttachment decoAttach = new DecoratedAttachment((Attachment)attachList.get(i));
						decoAttachList.add(decoAttach);
					}
				}							
				userAuthoredInfo = new UserStatistics();
				userAuthoredInfo.setSiteUserId(selectedSiteUserId);
				userAuthoredInfo.setForumTitle(d.getTitle());
				userAuthoredInfo.setTopicTitle(t.getTitle());
				userAuthoredInfo.setForumDate(mesWithAttach.getCreated());
				userAuthoredInfo.setForumSubject(mesWithAttach.getTitle());
				userAuthoredInfo.setMessage(mesWithAttach.getBody());
				userAuthoredInfo.setMsgId(selectedMsgId);
				userAuthoredInfo.setTopicId(Long.toString(t.getId()));
				userAuthoredInfo.setForumId(Long.toString(d.getId()));
				userAuthoredInfo.setMsgDeleted(mesWithAttach.getDeleted());
				userAuthoredInfo.setDecoAttachmentsList(decoAttachList);

				messageManager.markMessageReadForUser(t.getId(), mesWithAttach.getId(), true, getCurrentUserId());

				statistics.add(userAuthoredInfo);
			}catch(Exception e){
				log.error("MessageForumsStatisticsBean: getUserSubjectMsgBody: selected message Id was not of type Long");
			}
		}
		return statistics;
	}

	Map<String, List> userReadStatisticsCache = new HashMap<String, List>();
	public List getUserReadStatistics(){
		if(!userReadStatisticsCache.containsKey(selectedSiteUserId)){
			final List<UserStatistics> statistics = new ArrayList();

			List<Message> messages;

			if((selectedAllTopicsTopicId == null || "".equals(selectedAllTopicsTopicId))
					&& (selectedAllTopicsForumId != null && !"".equals(selectedAllTopicsForumId))){
				List<UserStatistics> allStatistics = messageManager.findReadStatsForStudentByForumId(selectedSiteUserId, Long.parseLong(selectedAllTopicsForumId));
				statistics.addAll(filterToAnonAwareStatistics(allStatistics));
			}else if(selectedAllTopicsTopicId != null && !"".equals(selectedAllTopicsTopicId)){
				statistics.addAll(messageManager.findReadStatsForStudentByTopicId(selectedSiteUserId, Long.parseLong(selectedAllTopicsTopicId)));
			}else{
				List<UserStatistics> allStatistics = messageManager.findReadStatsForStudent(selectedSiteUserId);
				statistics.addAll(filterToAnonAwareStatistics(allStatistics));
			}

			sortStatisticsByUser2(statistics);	

			userReadStatisticsCache.put(selectedSiteUserId, statistics);
		} else {
		    // sort the statistics
		    List<UserStatistics> statistics = userReadStatisticsCache.get(selectedSiteUserId);
		    sortStatisticsByUser2(statistics);
		    userReadStatisticsCache.put(selectedSiteUserId, statistics);
		}
		return userReadStatisticsCache.get(selectedSiteUserId);
	}
	
	public class dMessageStatusInfo{
		private String forumTitle;
		private String topicTitle;
		private Long messageId;
		private Date messageCreated;
		private String messageTitle;
		
		public dMessageStatusInfo(String forumTitle, String topicTitle, Long messageId, Date messageCreated, String messageTitle){
			this.forumTitle = forumTitle;
			this.topicTitle = topicTitle;
			this.messageId = messageId;
			this.messageCreated = messageCreated;
			this.messageTitle = messageTitle;
		}

		public String getForumTitle() {
			return forumTitle;
		}

		public String getTopicTitle() {
			return topicTitle;
		}

		public Long getMessageId() {
			return messageId;
		}

		public Date getMessageCreated() {
			return messageCreated;
		}

		public String getMessageTitle() {
			return messageTitle;
		}

	}
	
	/**
	 * Sorting Utils
	 */
	
	private List sortStatistics(List statistics){
		Comparator comparator = determineComparator();
		Collections.sort(statistics, comparator);
		return statistics;
	}
	
	private List sortStatisticsByUser(List statistics){
		Comparator comparator = determineComparatorByUser();
		Collections.sort(statistics, comparator);
		return statistics;
	}
	
	private List sortStatisticsByUser2(List statistics){
		Comparator comparator = determineComparatorByUser2();
		Collections.sort(statistics, comparator);
		return statistics;
	}
	
	private List sortStatisticsByUser3(List statistics){
		Comparator comparator = determineComparatorByUser3();
		Collections.sort(statistics, comparator);
		return statistics;
	}
	
	private List sortStatisticsByAllTopics(List statistics){
		Comparator comparator = determineComparatorByAllTopics();
		Collections.sort(statistics, comparator);
		return statistics;
	}
	
	
	public void toggleSort(String sortByType) {
		if (sortBy.equals(sortByType)) {
	       if (ascending) {
	    	   ascending = false;
	       } else {
	    	   ascending = true;
	       }
	    } else {
	    	sortBy = sortByType;
	    	ascending = true;
	    }
	}
	
	public void toggleSortByUser(String sortByType) {
		if (sortByUser.equals(sortByType)) {
	       if (ascendingForUser) {
	    	   ascendingForUser = false;
	       } else {
	    	   ascendingForUser = true;
	       }
	    } else {
	    	sortByUser = sortByType;
	    	ascendingForUser = true;
	    }
	}
	
	public void toggleSortByUser2(String sortByType) {
		if (sortByUser2.equals(sortByType)) {
	       if (ascendingForUser2) {
	    	   ascendingForUser2 = false;
	       } else {
	    	   ascendingForUser2 = true;
	       }
	    } else {
	    	sortByUser2 = sortByType;
	    	ascendingForUser2 = true;
	    }
	}
	
	public void toggleSortByUser3(String sortByType) {
		if (sortByUser3.equals(sortByType)) {
	       if (ascendingForUser3) {
	    	   ascendingForUser3 = false;
	       } else {
	    	   ascendingForUser3 = true;
	       }
	    } else {
	    	sortByUser3 = sortByType;
	    	ascendingForUser3 = true;
	    }
	}
	
	public void toggleSortByAllTopics(String sortByType){
		if(sortByAllTopics.equals(sortByType)){
			ascendingForAllTopics = !ascendingForAllTopics;
		}else{
			sortByAllTopics = sortByType;
			ascendingForAllTopics = true;
		}
	}
	
	
	public String toggleNameSort()	{
		toggleSort(NAME_SORT);
		return LIST_PAGE;
	}
	
	public String toggleAuthoredSort()	{
		toggleSort(AUTHORED_SORT);
		return LIST_PAGE;
	}
	
	public String toggleReadSort()	{    
		toggleSort(READ_SORT);
		return LIST_PAGE;
	}
	    
	public String toggleUnreadSort()	{    
		toggleSort(UNREAD_SORT);
		return LIST_PAGE;
	}
	
	public String togglePercentReadSort()	{    
		toggleSort(PERCENT_READ_SORT);	    
		return LIST_PAGE;
	}
	
	public String toggleGradeSort()	{    
		toggleSort(GRADE_SORT);	    
		return LIST_PAGE;
	}
	
	
	public String toggleForumTitleSort()	{    
		toggleSortByUser(FORUM_TITLE_SORT);	    
		return FORUM_STATISTICS_USER;
	}

	public boolean isForumTitleSort() {
		if (sortByUser.equals(FORUM_TITLE_SORT))
			return true;
		return false;
	}
	
	public String toggleTopicTitleSort()	{    
		toggleSortByUser(TOPIC_TITLE_SORT);	    
		return FORUM_STATISTICS_USER;
	}

	public boolean isTopicTitleSort() {
		if (sortByUser.equals(TOPIC_TITLE_SORT))
			return true;
		return false;
	}	
	
	public String toggleDateSort()	{    
		toggleSortByUser(FORUM_DATE_SORT);	    
		return FORUM_STATISTICS_USER;
	}

	public boolean isForumDateSort() {
		if (sortByUser.equals(FORUM_DATE_SORT))
			return true;
		return false;
	}	
	
	public String toggleSubjectSort()	{    
		toggleSortByUser(FORUM_SUBJECT_SORT);	    
		return FORUM_STATISTICS_USER;
	}

	public boolean isForumSubjectSort() {
		if (sortByUser.equals(FORUM_SUBJECT_SORT))
			return true;
		return false;
	}
	
	public String toggleForumTitleSort2()	{    
		toggleSortByUser2(FORUM_TITLE_SORT2);	    
		return FORUM_STATISTICS_USER;
	}

	public boolean isForumTitleSort2() {
		if (sortByUser2.equals(FORUM_TITLE_SORT2))
			return true;
		return false;
	}	
	
	public String toggleTopicTitleSort2()	{    
		toggleSortByUser2(TOPIC_TITLE_SORT2);	    	
		return FORUM_STATISTICS_USER;
	}

	public boolean isTopicTitleSort2() {
		if (sortByUser2.equals(TOPIC_TITLE_SORT2))
			return true;
		return false;
	}	
	
	public String toggleDateSort2()	{    
		toggleSortByUser2(FORUM_DATE_SORT2);	    
		return FORUM_STATISTICS_USER;
	}

	public boolean isForumDateSort2() {
		if (sortByUser2.equals(FORUM_DATE_SORT2))
			return true;
		return false;
	}
	
	public String toggleSubjectSort2()	{    
		toggleSortByUser2(FORUM_SUBJECT_SORT2);	    
		return FORUM_STATISTICS_USER;
	}

	public boolean isForumSubjectSort2() {
		if (sortByUser2.equals(FORUM_SUBJECT_SORT2))
			return true;
		return false;
	}
	
	public String toggleDateSort3()	{    
		toggleSortByUser3(FORUM_DATE_SORT3);	    
		return FORUM_STATISTICS_ALL_AUTHORED_MSG;
	}

	public boolean isForumDateSort3() {
		if (sortByUser3.equals(FORUM_DATE_SORT3))
			return true;
		return false;
	}	
	
	public String toggleTopicTitleSort3()	{    
		toggleSortByUser3(TOPIC_TITLE_SORT3);	    	
		return FORUM_STATISTICS_ALL_AUTHORED_MSG;
	}

	public boolean isTopicTitleSort3() {
		if (sortByUser3.equals(TOPIC_TITLE_SORT3))
			return true;
		return false;
	}	
	
	public String toggleAllTopicsForumTitleSort(){
		toggleSortByAllTopics(ALL_TOPICS_FORUM_TITLE_SORT);
		return FORUM_STATISTICS_BY_ALL_TOPICS;
	}
	
	public boolean isAllTopicsForumTitleSort(){
		return ALL_TOPICS_FORUM_TITLE_SORT.equals(sortByAllTopics);
	}
	
	public String toggleAllTopicsForumDateSort(){
		toggleSortByAllTopics(ALL_TOPICS_FORUM_DATE_SORT);
		return FORUM_STATISTICS_BY_ALL_TOPICS;
	}
	
	public boolean isAllTopicsForumDateSort(){
		return ALL_TOPICS_FORUM_DATE_SORT.equals(sortByAllTopics);
	}
	
	public String toggleAllTopicsTopicDateSort(){
		toggleSortByAllTopics(ALL_TOPICS_TOPIC_DATE_SORT);
		return FORUM_STATISTICS_BY_ALL_TOPICS;
	}
	
	public boolean isAllTopicsTopicDateSort(){
		return ALL_TOPICS_TOPIC_DATE_SORT.equals(sortByAllTopics);
	}
	
	public String toggleAllTopicsTopicTitleSort(){
		toggleSortByAllTopics(ALL_TOPICS_TOPIC_TITLE_SORT);
		return FORUM_STATISTICS_BY_ALL_TOPICS;
	}
	
	public boolean isAllTopicsTopicTitleSort(){
		return ALL_TOPICS_TOPIC_TITLE_SORT.equals(sortByAllTopics);
	}
	
	public String toggleAllTopicsTopicTotalMessagesSort(){
		toggleSortByAllTopics(ALL_TOPICS_TOPIC_TOTAL_MESSAGES_SORT);
		return FORUM_STATISTICS_BY_ALL_TOPICS;
	}
	
	public boolean isAllTopicsTopicTotalMessagesSort(){
		return ALL_TOPICS_TOPIC_TOTAL_MESSAGES_SORT.equals(sortByAllTopics);
	}	
	
	public String toggleTopicNameSort()	{    
		toggleSort(NAME_SORT);	    
		return FORUM_STATISTICS_BY_TOPIC;
	}
	
	public String toggleTopicAuthoredSort()	{    
		toggleSort(AUTHORED_SORT);	    
		return FORUM_STATISTICS_BY_TOPIC;
	}
	
	public String toggleTopicReadSort()	{    
		toggleSort(READ_SORT);	    
		return FORUM_STATISTICS_BY_TOPIC;
	}
	
	public String toggleTopicUnreadSort()	{    
		toggleSort(UNREAD_SORT);	    
		return FORUM_STATISTICS_BY_TOPIC;
	}
	
	public String toggleTopicPercentReadSort()	{    
		toggleSort(PERCENT_READ_SORT);	    
		return FORUM_STATISTICS_BY_TOPIC;
	}
	
	public String toggleTopicGradeSort()	{    
		toggleSort(GRADE_SORT);	    
		return FORUM_STATISTICS_BY_TOPIC;
	}
	
	
	public boolean isNameSort() {
		if (sortBy.equals(NAME_SORT))
			return true;
		return false;
	}
		
	public boolean isAuthoredSort() {
		if (sortBy.equals(AUTHORED_SORT))
			return true;
		return false;
	}
		
	public boolean isReadSort() {
		if (sortBy.equals(READ_SORT))
			return true;
		return false;
	}
	
	public boolean isUnreadSort() {
		if (sortBy.equals(UNREAD_SORT))
			return true;
		return false;
	}
	
	public boolean isPercentReadSort() {
		if (sortBy.equals(PERCENT_READ_SORT))
			return true;
		return false;
	}
	
	public boolean isGradeSort() {
		if (sortBy.equals(GRADE_SORT))
			return true;
		return false;
	}
	
	public boolean isAscending() {
		return ascending;
	}
	
	public boolean isAscendingForUser() {
		return ascendingForUser;
	}
	
	public boolean isAscendingForUser2() {
		return ascendingForUser2;
	}
	
	public boolean isAscendingForUser3() {
		return ascendingForUser3;
	}	
	
	public boolean isAscendingForAllTopics(){
		return ascendingForAllTopics;
	}
	
	private Comparator determineComparator(){
		if(ascending){
			if (sortBy.equals(NAME_SORT)){
				return nameComparatorAsc;
			}else if (sortBy.equals(AUTHORED_SORT)){
				return authoredComparatorAsc;
			}else if (sortBy.equals(READ_SORT)){
				return readComparatorAsc;
			}else if (sortBy.equals(UNREAD_SORT)){
				return unreadComparatorAsc;
			}else if (sortBy.equals(PERCENT_READ_SORT)){
				return percentReadComparatorAsc;
			}else if(sortBy.equals(GRADE_SORT)){
 				return GradeComparatorAsc;
			}
		}else{
			if (sortBy.equals(NAME_SORT)){
				return nameComparatorDesc;
			}else if (sortBy.equals(AUTHORED_SORT)){
				return authoredComparatorDesc;
			}else if (sortBy.equals(READ_SORT)){
				return readComparatorDesc;
			}else if (sortBy.equals(UNREAD_SORT)){
				return unreadComparatorDesc;
			}else if (sortBy.equals(PERCENT_READ_SORT)){
				return percentReadComparatorDesc;
			}else if(sortBy.equals(GRADE_SORT)){
				return GradeComparatorDesc;
			}
		}
		//default return NameComparatorAsc
		return nameComparatorAsc;
	}
	
	private Comparator determineComparatorByUser(){
		if(ascendingForUser){
			if (sortByUser.equals(FORUM_TITLE_SORT)){
				return forumTitleComparatorAsc;
			}else if (sortByUser.equals(FORUM_DATE_SORT)){
				return forumDateComparatorAsc;
			}else if (sortByUser.equals(FORUM_SUBJECT_SORT)){
				return forumSubjectComparatorAsc;
			}else if (sortByUser.equals(TOPIC_TITLE_SORT)){
				return topicTitleComparatorAsc;
			}
		}else{
			if (sortByUser.equals(FORUM_TITLE_SORT)){
				return forumTitleComparatorDesc;
			}else if (sortByUser.equals(FORUM_DATE_SORT)){
				return forumDateComparatorDesc;
			}else if (sortByUser.equals(FORUM_SUBJECT_SORT)){
				return forumSubjectComparatorDesc;
			}else if (sortByUser.equals(TOPIC_TITLE_SORT)){
				return topicTitleComparatorDesc;
			}
		}
		//default return NameComparatorAsc
		return forumDateComparatorDesc;
	}
	
	private Comparator determineComparatorByUser2(){
		if(ascendingForUser2){
			if (sortByUser2.equals(FORUM_TITLE_SORT2)){
				return forumTitleComparatorAsc;
			}else if (sortByUser2.equals(FORUM_DATE_SORT2)){
				return forumDateComparatorAsc;
			}else if (sortByUser2.equals(FORUM_SUBJECT_SORT2)){
				return forumSubjectComparatorAsc;
			}else if (sortByUser2.equals(TOPIC_TITLE_SORT2)){
				return topicTitleComparatorAsc;
			}
		}else{
			if (sortByUser2.equals(FORUM_TITLE_SORT2)){
				return forumTitleComparatorDesc;
			}else if (sortByUser2.equals(FORUM_DATE_SORT2)){
				return forumDateComparatorDesc;
			}else if (sortByUser2.equals(FORUM_SUBJECT_SORT2)){
				return forumSubjectComparatorDesc;
			}else if (sortByUser2.equals(TOPIC_TITLE_SORT2)){
				return topicTitleComparatorDesc;
			}
		}
		//default return NameComparatorAsc
		return forumDateComparatorDesc;
	}
	
	private Comparator determineComparatorByUser3(){
		if(ascendingForUser3){
			if (sortByUser3.equals(TOPIC_TITLE_SORT3)){
				return topicTitleComparatorAsc;
			}else if (sortByUser3.equals(FORUM_DATE_SORT3)){
				return forumDateComparatorAsc;
			}
		}else{
			if (sortByUser3.equals(TOPIC_TITLE_SORT3)){
				return topicTitleComparatorDesc;
			}else if (sortByUser3.equals(FORUM_DATE_SORT3)){
				return forumDateComparatorDesc;
			}
		}
		//default return NameComparatorAsc
		return forumDateComparatorDesc;
	}
	
	private Comparator determineComparatorByAllTopics(){
		if(ascendingForAllTopics){
			if (sortByAllTopics.equals(ALL_TOPICS_FORUM_DATE_SORT)){
				return AllTopicsForumDateComparatorAsc;
			}else if (sortByAllTopics.equals(ALL_TOPICS_FORUM_TITLE_SORT)){
				return AllTopicsForumTitleComparatorAsc;
			}else if (sortByAllTopics.equals(ALL_TOPICS_TOPIC_TITLE_SORT)){
				return AllTopicsTopicTitleComparatorAsc;
			}else if (sortByAllTopics.equals(ALL_TOPICS_TOPIC_TOTAL_MESSAGES_SORT)){
				return AllTopicsTopicTotalMessagesComparatorAsc;
			}else if(sortByAllTopics.equals(ALL_TOPICS_TOPIC_DATE_SORT)){
				return AllTopicsTopicDateComparatorAsc;
			}
		}else{
			if (sortByAllTopics.equals(ALL_TOPICS_FORUM_DATE_SORT)){
				return AllTopicsForumDateComparatorDesc;
			}else if (sortByAllTopics.equals(ALL_TOPICS_FORUM_TITLE_SORT)){
				return AllTopicsForumTitleComparatorDesc;
			}else if (sortByAllTopics.equals(ALL_TOPICS_TOPIC_TITLE_SORT)){
				return AllTopicsTopicTitleComparatorDesc;
			}else if (sortByAllTopics.equals(ALL_TOPICS_TOPIC_TOTAL_MESSAGES_SORT)){
				return AllTopicsTopicTotalMessagesComparatorDesc;
			}else if(sortByAllTopics.equals(ALL_TOPICS_TOPIC_DATE_SORT)){
				return AllTopicsTopicDateComparatorDesc;
			}
		}
		//default return NameComparatorAsc
		return AllTopicsForumTitleComparatorDesc;
	}

	
	static {
		/**
		 * Comparators for DecoratedCompileMessageStatistics
		 */
		nameComparatorAsc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				String name1 = ((DecoratedCompiledMessageStatistics) item).getAnonAwareId().toUpperCase();
				String name2 = ((DecoratedCompiledMessageStatistics) anotherItem).getAnonAwareId().toUpperCase();
				return name1.compareTo(name2);
			}
		};
		
		authoredComparatorAsc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				int authored1 = ((DecoratedCompiledMessageStatistics) item).getAuthoredForumsAmt();
				int authored2 = ((DecoratedCompiledMessageStatistics) anotherItem).getAuthoredForumsAmt();
				if(authored1 - authored2 == 0){
					//we can't have descrepancies on how the order happens, otherwise jsf will scramble the scores
					//with other scores that are equal to this (jsp submits twice, causing "sort" to happen between when
					//the user enters the data and when it gets submitted in JSP (behind the scenes)
					return nameComparatorAsc.compare(item, anotherItem);
				}else{
					return authored1 - authored2;
				}
			}
		};
		
		readComparatorAsc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				int read1 = ((DecoratedCompiledMessageStatistics) item).getReadForumsAmt();
				int read2 = ((DecoratedCompiledMessageStatistics) anotherItem).getReadForumsAmt();
				if(read1 - read2 == 0){
					//we can't have descrepancies on how the order happens, otherwise jsf will scramble the scores
					//with other scores that are equal to this (jsp submits twice, causing "sort" to happen between when
					//the user enters the data and when it gets submitted in JSP (behind the scenes)
					return nameComparatorAsc.compare(item, anotherItem);
				}else{
					return read1 - read2;
				}
			}
		};
		
		unreadComparatorAsc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				int unread1 = ((DecoratedCompiledMessageStatistics) item).getUnreadForumsAmt();
				int unread2 = ((DecoratedCompiledMessageStatistics) anotherItem).getUnreadForumsAmt();
				if(unread1 - unread2 == 0){
					//we can't have descrepancies on how the order happens, otherwise jsf will scramble the scores
					//with other scores that are equal to this (jsp submits twice, causing "sort" to happen between when
					//the user enters the data and when it gets submitted in JSP (behind the scenes)
					return nameComparatorAsc.compare(item, anotherItem);
				}else{
					return unread1 - unread2;
				}
			}
		};
		
		percentReadComparatorAsc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				double percentRead1 = ((DecoratedCompiledMessageStatistics) item).getPercentReadForumsAmt();
				double percentRead2 = ((DecoratedCompiledMessageStatistics) anotherItem).getPercentReadForumsAmt();
				if(percentRead1 == percentRead2){
					//we can't have descrepancies on how the order happens, otherwise jsf will scramble the scores
					//with other scores that are equal to this (jsp submits twice, causing "sort" to happen between when
					//the user enters the data and when it gets submitted in JSP (behind the scenes)
					return nameComparatorAsc.compare(item, anotherItem);
				}
				else if(percentRead1 < percentRead2){
					return -1;
				}
				else {
					return 1;
				}
			}
		};
		nameComparatorDesc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				String name1 = ((DecoratedCompiledMessageStatistics) item).getAnonAwareId().toUpperCase();
				String name2 = ((DecoratedCompiledMessageStatistics) anotherItem).getAnonAwareId().toUpperCase();
				return name2.compareTo(name1);
			}
		};
		
		forumTitleComparatorAsc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				String title1 = ((UserStatistics) item).getForumTitle().toUpperCase();
				String title2 = ((UserStatistics) anotherItem).getForumTitle().toUpperCase();
				return title1.compareTo(title2);
			}
		};
		
		topicTitleComparatorAsc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				String title1 = ((UserStatistics) item).getTopicTitle().toUpperCase();
				String title2 = ((UserStatistics) anotherItem).getTopicTitle().toUpperCase();
				return title1.compareTo(title2);
			}
		};
		
		forumDateComparatorAsc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				Date date1 = ((UserStatistics) item).getForumDate();
				Date date2 = ((UserStatistics) anotherItem).getForumDate();
				return date1.compareTo(date2);
			}
		};
		
		forumSubjectComparatorAsc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				String subject1 = ((UserStatistics) item).getForumSubject().toUpperCase();
				String subject2 = ((UserStatistics) anotherItem).getForumSubject().toUpperCase();
				return subject1.compareTo(subject2);
			}
		};

		authoredComparatorDesc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				int authored1 = ((DecoratedCompiledMessageStatistics) item).getAuthoredForumsAmt();
				int authored2 = ((DecoratedCompiledMessageStatistics) anotherItem).getAuthoredForumsAmt();
				if(authored1 - authored2 == 0){
					//we can't have descrepancies on how the order happens, otherwise jsf will scramble the scores
					//with other scores that are equal to this (jsp submits twice, causing "sort" to happen between when
					//the user enters the data and when it gets submitted in JSP (behind the scenes)
					return nameComparatorAsc.compare(item, anotherItem);
				}else{
					return authored2 - authored1;
				}
			}
		};
		
		readComparatorDesc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				int read1 = ((DecoratedCompiledMessageStatistics) item).getReadForumsAmt();
				int read2 = ((DecoratedCompiledMessageStatistics) anotherItem).getReadForumsAmt();
				if(read1 - read2 == 0){
					//we can't have descrepancies on how the order happens, otherwise jsf will scramble the scores
					//with other scores that are equal to this (jsp submits twice, causing "sort" to happen between when
					//the user enters the data and when it gets submitted in JSP (behind the scenes)
					return nameComparatorAsc.compare(item, anotherItem);
				}else{
					return read2 - read1;
				}
			}
		};
		
		unreadComparatorDesc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				int unread1 = ((DecoratedCompiledMessageStatistics) item).getUnreadForumsAmt();
				int unread2 = ((DecoratedCompiledMessageStatistics) anotherItem).getUnreadForumsAmt();
				if(unread1 - unread2 == 0){
					//we can't have descrepancies on how the order happens, otherwise jsf will scramble the scores
					//with other scores that are equal to this (jsp submits twice, causing "sort" to happen between when
					//the user enters the data and when it gets submitted in JSP (behind the scenes)
					return nameComparatorAsc.compare(item, anotherItem);
				}else{
					return unread2 - unread1;
				}
			}
		};
		
		percentReadComparatorDesc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				double percentRead1 = ((DecoratedCompiledMessageStatistics) item).getPercentReadForumsAmt();
				double percentRead2 = ((DecoratedCompiledMessageStatistics) anotherItem).getPercentReadForumsAmt();
				if(percentRead1 == percentRead2){
					//we can't have descrepancies on how the order happens, otherwise jsf will scramble the scores
					//with other scores that are equal to this (jsp submits twice, causing "sort" to happen between when
					//the user enters the data and when it gets submitted in JSP (behind the scenes)
					return nameComparatorAsc.compare(item, anotherItem);
				}
				else if(percentRead1 < percentRead2){
					return 1;
				}
				else {
					return -1;
				}
			}
		};
		
		/**
		 * Comparator for DecoratedCompiledUserStatistics
		 */
		
		dateComparaterDesc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				Date date1 = ((UserStatistics) item).getForumDate();
				Date date2 = ((UserStatistics) anotherItem).getForumDate();
				return date2.compareTo(date1);
			}
		};
		
		forumTitleComparatorDesc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				String title1 = ((UserStatistics) item).getForumTitle().toUpperCase();
				String title2 = ((UserStatistics) anotherItem).getForumTitle().toUpperCase();
				return title2.compareTo(title1);
			}
		};
		
		topicTitleComparatorDesc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				String title1 = ((UserStatistics) item).getTopicTitle().toUpperCase();
				String title2 = ((UserStatistics) anotherItem).getTopicTitle().toUpperCase();
				return title2.compareTo(title1);
			}
		};
		
		forumDateComparatorDesc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				Date date1 = ((UserStatistics) item).getForumDate();
				Date date2 = ((UserStatistics) anotherItem).getForumDate();
				return date2.compareTo(date1);
			}
		};
		
		forumSubjectComparatorDesc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				String subject1 = ((UserStatistics) item).getForumSubject().toUpperCase();
				String subject2 = ((UserStatistics) anotherItem).getForumSubject().toUpperCase();
				return subject2.compareTo(subject1);
			}
		};
		
		AllTopicsForumDateComparatorDesc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				Date date1 = ((DecoratedCompiledStatisticsByTopic) item).getForumDate();
				Date date2 = ((DecoratedCompiledStatisticsByTopic) anotherItem).getForumDate();
				return date2.compareTo(date1);				
			}
		};
		
		AllTopicsForumDateComparatorAsc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				Date date1 = ((DecoratedCompiledStatisticsByTopic) item).getForumDate();
				Date date2 = ((DecoratedCompiledStatisticsByTopic) anotherItem).getForumDate();
				return date1.compareTo(date2);				
			}
		};
		
		AllTopicsTopicDateComparatorDesc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				Date date1 = ((DecoratedCompiledStatisticsByTopic) item).getTopicDate();
				Date date2 = ((DecoratedCompiledStatisticsByTopic) anotherItem).getTopicDate();
				return date2.compareTo(date1);				
			}
		};
		
		AllTopicsTopicDateComparatorAsc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				Date date1 = ((DecoratedCompiledStatisticsByTopic) item).getTopicDate();
				Date date2 = ((DecoratedCompiledStatisticsByTopic) anotherItem).getTopicDate();
				return date1.compareTo(date2);				
			}
		};
		
		AllTopicsTopicTitleComparatorAsc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				String title1 = ((DecoratedCompiledStatisticsByTopic) item).getTopicTitle().toUpperCase();
				String title2 = ((DecoratedCompiledStatisticsByTopic) anotherItem).getTopicTitle().toUpperCase();
				return title1.compareTo(title2);
			}
		};
		
		AllTopicsTopicTitleComparatorDesc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				String title1 = ((DecoratedCompiledStatisticsByTopic) item).getTopicTitle().toUpperCase();
				String title2 = ((DecoratedCompiledStatisticsByTopic) anotherItem).getTopicTitle().toUpperCase();
				return title2.compareTo(title1);
			}
		};
		
		AllTopicsForumTitleComparatorAsc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				String title1 = ((DecoratedCompiledStatisticsByTopic) item).getForumTitle().toUpperCase();
				String title2 = ((DecoratedCompiledStatisticsByTopic) anotherItem).getForumTitle().toUpperCase();
				return title1.compareTo(title2);
			}
		};
		
		AllTopicsForumTitleComparatorDesc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				String title1 = ((DecoratedCompiledStatisticsByTopic) item).getForumTitle().toUpperCase();
				String title2 = ((DecoratedCompiledStatisticsByTopic) anotherItem).getForumTitle().toUpperCase();
				return title2.compareTo(title1);
			}
		};
		
		AllTopicsTopicTotalMessagesComparatorAsc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				int read1 = ((DecoratedCompiledStatisticsByTopic) item).getTotalTopicMessages();
				int read2 = ((DecoratedCompiledStatisticsByTopic) anotherItem).getTotalTopicMessages();
				return read1 - read2;
			}
		};
		
		AllTopicsTopicTotalMessagesComparatorDesc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				int read1 = ((DecoratedCompiledStatisticsByTopic) item).getTotalTopicMessages();
				int read2 = ((DecoratedCompiledStatisticsByTopic) anotherItem).getTotalTopicMessages();
				return read2 - read1;
			}
		};
		
		GradeComparatorAsc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				DecoratedCompiledMessageStatistics stat = ((DecoratedCompiledMessageStatistics) item);
				DecoratedCompiledMessageStatistics stat2 = ((DecoratedCompiledMessageStatistics) anotherItem);

				return compareGradesFromStats(stat, stat2);
			}
		};
		
		GradeComparatorDesc = new Comparator(){
			public int compare(Object item, Object anotherItem){
				DecoratedCompiledMessageStatistics stat = ((DecoratedCompiledMessageStatistics) item);
				DecoratedCompiledMessageStatistics stat2 = ((DecoratedCompiledMessageStatistics) anotherItem);

				return 0 - compareGradesFromStats(stat, stat2);
			}
		};
	}

	/**
	 * Compares two statistics by grades.
	 * Higher grades are greater than lower grades.
	 * Stats with equal grades are compared by name
	 * If one has a grade, and the other doesn't, treat having a grade as greater than not having a grade
	 * If neither has a grade, compare by name
	 */
	private static int compareGradesFromStats(DecoratedCompiledMessageStatistics stat1, DecoratedCompiledMessageStatistics stat2)
	{
		Double grd1 = getGradeFromStat(stat1);
		Double grd2 = getGradeFromStat(stat2);
		// If they're both null, or an equal grade, revert to the name comparator
		if ((grd1 == null && grd2 == null) || (grd1 != null && grd1.equals(grd2)))
		{
			return nameComparatorAsc.compare(stat1, stat2);
		}
		boolean exists1 = grd1 != null;
		boolean exists2 = grd2 != null;
		if (exists1 && exists2)
		{
			// Both grades exist, compare them
			return Double.compare(grd1, grd2);
		}
		// One grade exists and the other doesn't, Boolean compare their existence
		return Boolean.compare(exists1, exists2);
	}

	/**
	 * Gets the grade from a statistics if possible, otherwise returns null
	 */
	private static Double getGradeFromStat(DecoratedCompiledMessageStatistics stat)
	{
		if (stat == null)
		{
			return null;
		}
		DecoratedGradebookAssignment asn = stat.getGradebookAssignment();
		if (asn == null || !asn.isAllowedToGrade())
		{
			return null;
		}

		String score = asn.getScore();
		if (StringUtils.isBlank(score) || !isNumber(score))
		{
			return null;
		}

		try
		{
			return Double.valueOf(score);
		}
		catch (NumberFormatException e)
		{
			// Impossible - isNumber would have caught this
		}

		return null;
	}
	
	/**
	 * Actions
	 */
	
	/**
	   * @return
	   */
	public String processActionStatisticsUser()
	{
		log.debug("processActionStatisticsUser");
		
		selectedSiteUserId = getExternalParameterByKey(SITE_USER_ID);
		//reset cache
		userReadStatisticsCache = new HashMap<String, List>();
		userAuthoredStatisticsCache = new HashMap<String, List>();
		
		return processActionStatisticsUserHelper();
	}
	
	public String processActionStatisticsUserHelper(){
		Map<String, String> userIdName = getUserIdName();

		if (!m_displayAnonIds)
		{
			selectedSiteUser = getUserName(selectedSiteUserId);
		}
		else
		{
			// set display name to anonymous id
			selectedSiteUser = StringUtils.trimToEmpty(userIdName.get(selectedSiteUserId));
		}
		
		isLastParticipant = false;
		isFirstParticipant = false;
		
		Set<String> userIdSet = userIdName.keySet();
		String[] userIdArray =(String[]) userIdSet.toArray(new String[userIdSet.size()]);
		
		int len = userIdArray.length;
		if (len > 0)
		{
			isFirstParticipant = userIdArray[0].equals(selectedSiteUserId);
			isLastParticipant = userIdArray[len - 1].equals(selectedSiteUserId);
		}
		
		return FORUM_STATISTICS_USER;
	}


	private String getUserName(String userId) {
		String userName="";
		try
		{
			User user=userDirectoryService.getUser(userId) ;
			if (ServerConfigurationService.getBoolean("msg.displayEid", true)) {
				if(user != null) {
					userName= user.getLastName() + ", " + user.getFirstName() + " (" + user.getDisplayId() + ")" ;
				}
			} else {
				if(user != null) {
					userName = user.getLastName() + ", " + user.getFirstName();
				}
			}
		}
		catch (UserNotDefinedException e) {
			log.error(e.getMessage(), e);
		}
		
		return userName;
	}
	
	public String processActionBackToUser() {
		return FORUM_STATISTICS_USER;
	}
	


	// **************************************** helper methods**********************************

	private String getExternalParameterByKey(String parameterId)
	{    
		ExternalContext context = FacesContext.getCurrentInstance()
		.getExternalContext();
		Map paramMap = context.getRequestParameterMap();

		return (String) paramMap.get(parameterId);    
	}


	public String getButtonUserName() {
		String userName;

		if (m_displayAnonIds)
		{
			userName = StringUtils.trimToEmpty(anonymousManager.getAnonId(toolManager.getCurrentPlacement().getContext(), selectedSiteUserId));
		}
		else
		{
			try
			{
				User u = userDirectoryService.getUser(selectedSiteUserId);
				userName = u.getDisplayName();
			}
			catch (UserNotDefinedException unde)
			{
				userName = selectedSiteUser;
			}
		}

		buttonUserName = getResourceBundleString("return_to_statistics" , new Object[] {userName}) ;
		return buttonUserName;
	}

	public void setButtonUserName(String buttonUserName) {
		this.buttonUserName = buttonUserName;
	}

	public static String getResourceBundleString(String key) 
	{
		final ResourceLoader rb = new ResourceLoader(MESSAGECENTER_BUNDLE);
		return rb.getString(key);
	}

	public static String getResourceBundleString(String key, Object[] args) {
		final ResourceLoader rb = new ResourceLoader(MESSAGECENTER_BUNDLE);
		return rb.getFormattedMessage(key, args);
	}

	public String getSelectedMsgId() {
		return selectedMsgId;
	}

	public void setSelectedMsgId(String selectedMsgId) {
		this.selectedMsgId = selectedMsgId;
	}

	public String getSelectedMsgSubject() {
		return selectedMsgSubject;
	}

	public void setSelectedMsgSubject(String selectedMsgSubject) {
		this.selectedMsgSubject = selectedMsgSubject;
	}
	
	public String processActionDisplayMsgBody() {
		log.debug("processActionDisplayMsgBody");

		selectedMsgId = getExternalParameterByKey("msgId");
		Message message =(Message) messageManager.getMessageById(Long.parseLong(selectedMsgId));
		selectedMsgSubject = message.getTitle();
		
		return FORUM_STATISTICS_MSG;
	}

	public String getSelectedForumTitle() {
		return selectedForumTitle;
	}

	public void setSelectedForumTitle(String selectedForumTitle) {
		this.selectedForumTitle = selectedForumTitle;
	}

	public String getSelectedTopicTitle() {
		return selectedTopicTitle;
	}

	public void setSelectedTopicTitle(String selectedTopicTitle) {
		this.selectedTopicTitle = selectedTopicTitle;
	}
	
	/**
	 * Builds a map of userid (uuid) to name (for display), or to anonymous id, as determined by bean state
	 * @return map of Sakai user id to display name/anonymous id
	 */
	public Map<String, String> getUserIdName() {
		Map<String, String> idNameMap = new LinkedHashMap<String, String>();
		
		Map courseMemberMap = membershipManager.getAllCourseUsersAsMap();
		List<MembershipItem> members;
		if (m_displayAnonIds)
		{
			members = convertNameToAnonIdAndSort(courseMemberMap);
		}
		else
		{
			members = membershipManager.convertMemberMapToList(courseMemberMap);
		}

		for (MembershipItem item : members)
		{
			if (null != item.getUser()) {				
				idNameMap.put(item.getUser().getId(), item.getName());
			}
		}
		return idNameMap;
	}

	/**
	 * Replace the name for each membership item with the anonid, then sort. Sorting by default uses the name attribute
	 * @param memberMap map of id (this is NOT the user uuid or eid) to membershipitem
	 * @return list of memberhip items with names replaced by anonid, sorted by anonid
	 */
	private List<MembershipItem> convertNameToAnonIdAndSort(Map<String, MembershipItem> memberMap)
	{
		Map<String, String> userIdToAnonIdMap = anonymousManager.getUserIdAnonIdMap(toolManager.getCurrentPlacement().getContext());
		List<MembershipItem> list = new ArrayList<>();
		for (MembershipItem item : memberMap.values())
		{
			User u = item.getUser();
			if (u != null)
			{
				String anonId = userIdToAnonIdMap.get(u.getId());
				if (anonId != null)
				{
					item.setName(anonId);
					list.add(item);
				}
			}
		}

		Collections.sort(list);

		return list;
	}

	private int getCurrentPosition(Object[] userIdArray, String userId)  {
		int currentPosition = -1;
		for (int i =0; i < userIdArray.length; i++) {
			if(userIdArray[i].equals(userId)) {
				currentPosition = i;
				break;
			}
		}
		return currentPosition;
	}
	
	public String processDisplayNextParticipant() {
		isLastParticipant = false;
		isFirstParticipant = false;
		
		Map<String, String> userIdName = getUserIdName();
		Set<String> userIdSet = userIdName.keySet();
		String[] userIdArray =(String[]) userIdSet.toArray(new String[userIdSet.size()]);
		int currentPosition = getCurrentPosition(userIdArray, selectedSiteUserId);
		
		if(currentPosition < userIdArray.length-1) {
			selectedSiteUserId = userIdArray[currentPosition+1];
			selectedSiteUser = (String)userIdName.get(selectedSiteUserId);
		}
		
		if(currentPosition == userIdArray.length - 2) {
			isLastParticipant = true;
		}
		return FORUM_STATISTICS_USER;
		
	}
	
	public String processDisplayPreviousParticipant() {		
		isLastParticipant = false;
		isFirstParticipant = false;
		
		Map<String, String> userIdName = getUserIdName();
		Set<String> userIdSet = userIdName.keySet();
		String[] userIdArray =(String[]) userIdSet.toArray(new String[userIdSet.size()]);
		int currentPosition = getCurrentPosition(userIdArray, selectedSiteUserId);
		
		if(currentPosition > 0) {
			selectedSiteUserId = userIdArray[currentPosition -1];
			selectedSiteUser = (String)userIdName.get(selectedSiteUserId);
		}
		
		if(currentPosition == 1) {
			isFirstParticipant = true;
		}
		return FORUM_STATISTICS_USER;
	}
			

	public boolean getIsFirstParticipant() {
		return isFirstParticipant;
	}

	public void setFirstParticipant(boolean isFirstParticipant) {
		this.isFirstParticipant = isFirstParticipant;
	}

	public boolean getIsLastParticipant() {
		return isLastParticipant;
	}

	public void setLastParticipant(boolean isLastParticipant) {
		this.isLastParticipant = isLastParticipant;
	}
				
	public String processActionStatisticsByAllTopics(){
		return FORUM_STATISTICS_BY_ALL_TOPICS;
	}
	
	public String processActionStatisticsByTopic()
	{
		log.debug("processActionStatisticsByTopic");
		
		//to save some speed, only update if the values have changed
		boolean newTopic = !getExternalParameterByKey(TOPIC_ID).equals(selectedAllTopicsTopicId);
		boolean newForum = !getExternalParameterByKey(FORUM_ID).equals(selectedAllTopicsForumId);
		
		selectedAllTopicsTopicId = getExternalParameterByKey(TOPIC_ID);
		selectedAllTopicsForumId = getExternalParameterByKey(FORUM_ID);
		if(newForum){
			if(selectedAllTopicsForumId != null && !"".equals(selectedAllTopicsForumId)){
				try{
					DiscussionForum df = forumManager.getForumById(Long.parseLong(selectedAllTopicsForumId));
					selectedAllTopicsForumTitle = df.getTitle();
				}catch (Exception e) {
					log.warn("MessageForumStatisticsBean.processActionStatisticsByTopic: Wasn't able to find discussion forum for id: " + selectedAllTopicsForumId);
				}
			}
		}
		if(newTopic){
			if(selectedAllTopicsTopicId != null && !"".equals(selectedAllTopicsTopicId)){
				try{
					DiscussionTopic dt = forumManager.getTopicById(Long.parseLong(selectedAllTopicsTopicId));
					selectedAllTopicsTopicTitle = dt.getTitle();
				}catch (Exception e) {
					log.warn("MessageForumStatisticsBean.processActionStatisticsByTopic: Wasn't able to find discussion topic for id: " + selectedAllTopicsForumId);
				}
			}
		}
							
		// Get topic statistics. For performance, we want this information up front and cached before we render the jsp files.
		// The default gradebook assignment must be known before we get the statistics, since grades will be included
		setDefaultSelectedAssign();
		getTopicStatistics();
		return FORUM_STATISTICS_BY_TOPIC;
	}

	public String getSelectedAllTopicsTopicTitle() {
		return selectedAllTopicsTopicTitle;
	}

	public void setSelectedAllTopicsTopicTitle(String selectedAllTopicsTopicTitle) {
		this.selectedAllTopicsTopicTitle = selectedAllTopicsTopicTitle;
	}

	public String getSelectedAllTopicsForumTitle() {
		return selectedAllTopicsForumTitle;
	}

	public void setSelectedAllTopicsForumTitle(String selectedAllTopicsForumTitle) {
		this.selectedAllTopicsForumTitle = selectedAllTopicsForumTitle;
	}

	public String getSelectedAllTopicsForumId() {
		return selectedAllTopicsForumId;
	}

	public void setSelectedAllTopicsForumId(String selectedAllTopicsForumId) {
		this.selectedAllTopicsForumId = selectedAllTopicsForumId;
	}

	public String getSelectedAllTopicsTopicId() {
		return selectedAllTopicsTopicId;
	}

	public void setSelectedAllTopicsTopicId(String selectedAllTopicsTopicId) {
		this.selectedAllTopicsTopicId = selectedAllTopicsTopicId;
	}
	
	public void setUpGradebookAssignments(){
		try {
			assignments = new ArrayList<SelectItem>();
			assignments.add(new SelectItem(DEFAULT_GB_ITEM, getResourceBundleString(SELECT_ASSIGN)));

			//Code to get the gradebook service from ComponentManager

			GradebookService gradebookService = getGradebookService();

			if(getGradebookExist()) {
				List gradeAssignmentsBeforeFilter = gradebookService.getAssignments(toolManager.getCurrentPlacement().getContext());
				for(int i=0; i<gradeAssignmentsBeforeFilter.size(); i++) {
					Assignment thisAssign = (Assignment) gradeAssignmentsBeforeFilter.get(i);
					if(!thisAssign.isExternallyMaintained()) {
						try {
							assignments.add(new SelectItem(Integer.toString(assignments.size()), thisAssign.getName()));
						} catch(Exception e) {
							log.error("DiscussionForumTool - processDfMsgGrd:" + e);
						}
					}
				}
			}
		} catch(SecurityException se) {
			log.debug("SecurityException caught while getting assignments.", se);
		} catch(Exception e1) {
			log.error("DiscussionForumTool&processDfMsgGrad:" + e1);
		}
	}
	
	protected GradebookService getGradebookService() {
		if (isGradebookDefined()) {
			return (GradebookService)  ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
		}
		return null;
	}
	
	protected boolean isGradebookDefined()
	{
		boolean rv = false;
		try
		{
			Object og = ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
			if (!(og instanceof GradebookService)) {
				log.info("Error getting gradebook service from component manager. CM returns:" + og.getClass().getName());
				return false;
			}

			GradebookService g = (GradebookService) og;
			String gradebookUid = toolManager.getCurrentPlacement().getContext();
			if (g.isGradebookDefined(gradebookUid) && (g.currentUserHasEditPerm(gradebookUid) || g.currentUserHasGradingPerm(gradebookUid)))
			{
				rv = true;
			}
		}
		catch (Exception e)
		{
			log.info(this + "isGradebookDefined " + e.getMessage());
		}

		return rv;

	}
	
	public boolean getGradebookExist() 
	{
		if (!gradebookExistChecked)
		{
			try 
			{ 

				GradebookService gradebookService = getGradebookService();
				if (gradebookService == null) return false;
				gradebookExist = gradebookService.isGradebookDefined(toolManager.getCurrentPlacement().getContext());
				gradebookExistChecked = true;
				return gradebookExist;
			}
			catch(Exception e)
			{
				gradebookExist = false;
				gradebookExistChecked = true;
				return gradebookExist;
			}
		}
		else
		{
			return gradebookExist;
		}
	}

	public String processGradeAssignChange(ValueChangeEvent vce) 
	{ 
		String changeAssign = (String) vce.getNewValue(); 
		if (changeAssign == null) 
		{ 
			return null; 
		} 
		else 
		{ 
			gradebookItemChosen = true;
			selectedAssign = changeAssign; 
			if(!DEFAULT_GB_ITEM.equalsIgnoreCase(selectedAssign)) {
				String gradebookUid = toolManager.getCurrentPlacement().getContext();
				selAssignName = ((SelectItem)assignments.get((Integer.valueOf(selectedAssign)).intValue())).getLabel();	
			}

			return null;
		} 
	}
	
	public String processGroupChange(ValueChangeEvent vce) 
	{ 
		String changeAssign = (String) vce.getNewValue(); 
		if (changeAssign == null) 
		{ 
			return null; 
		} 
		else 
		{ 
			selectedGroup = changeAssign; 
			return null;
		} 
	}
	
	public List<SelectItem> getAssignments() 
	{
		if(assignments == null){
			setUpGradebookAssignments();
		}
		return assignments; 
	} 

	public void setAssignments(List assignments) 
	{ 
		this.assignments = assignments; 
	} 
	
	public void setDefaultSelectedAssign(){
		if(!gradebookItemChosen){
			if(selectedAllTopicsTopicId != null && !"".equals(selectedAllTopicsTopicId)){
				String defaultAssignName = forumManager.getTopicById(Long.parseLong(selectedAllTopicsTopicId)).getDefaultAssignName();
				setDefaultSelectedAssign(defaultAssignName);
			}else{
				String defaultAssignName = forumManager.getForumById(Long.parseLong(selectedAllTopicsForumId)).getDefaultAssignName();
				setDefaultSelectedAssign(defaultAssignName);
			}			
		}
		gradebookItemChosen = false;
	}
	
	public String getSelectedAssign() 
	{ 		
		return selectedAssign; 
	} 

	public void setSelectedAssign(String selectedAssign) 
	{ 
		this.selectedAssign = selectedAssign; 
	} 
	
	private void resetGradebookVariables(){
		gradebookExistChecked = false;
		gradebookExist = false;
		assignments = null; 
		selectedAssign = DEFAULT_GB_ITEM;
		selectedGroup = DEFAULT_GB_ITEM;
		gradebookItemChosen = false;
		selAssignName = "";
	}
	
	private void setDefaultSelectedAssign(String assign){
		selectedAssign = DEFAULT_GB_ITEM;
		selAssignName = "";
		if(assign != null){
			for (SelectItem item : getAssignments()) {
				if(assign.equals(item.getLabel())){
					selectedAssign = item.getValue().toString();
					selAssignName = assign;
				}
			}
		}
		
	}
	
	private Map<String, DecoratedGradebookAssignment> getGradebookAssignment(){
		Map<String, DecoratedGradebookAssignment> returnVal = new HashMap<String, DecoratedGradebookAssignment>();

		if(!DEFAULT_GB_ITEM.equalsIgnoreCase(selectedAssign)) {
			String gradebookUid = toolManager.getCurrentPlacement().getContext();
			selAssignName = ((SelectItem)assignments.get((Integer.valueOf(selectedAssign)).intValue())).getLabel();


			GradebookService gradebookService = getGradebookService();
			if (gradebookService == null) return returnVal;
			
			int gradeEntryType = gradebookService.getGradeEntryType(gradebookUid);
			if (gradeEntryType == GradebookService.GRADE_TYPE_LETTER) {
			    gradeByLetter = true;
			    gradeByPoints = false;
			    gradeByPercent = false;
			} else if (gradeEntryType == GradebookService.GRADE_TYPE_PERCENTAGE) {
			    gradeByPercent = true;
			    gradeByPoints = false;
			    gradeByLetter = false;
			} else {
			    gradeByPoints = true;
			    gradeByPercent = false;
			    gradeByLetter = false;
			}        

			Assignment assignment = gradebookService.getAssignmentByNameOrId(gradebookUid, selAssignName);
			if(assignment != null){
				gbItemPointsPossible = assignment.getPoints().toString();			

				//grab all grades for the id's that the user is able to grade:
				Map studentIdFunctionMap = gradebookService.getViewableStudentsForItemForCurrentUser(gradebookUid, assignment.getId());
				List<GradeDefinition> grades = gradebookService.getGradesForStudentsForItem(gradebookUid, assignment.getId(), new ArrayList(studentIdFunctionMap.keySet()));
				//add grade values to return map
				for(GradeDefinition gradeDef : grades){
					String studentUuid = gradeDef.getStudentUid();		  
					DecoratedGradebookAssignment gradeAssignment = new DecoratedGradebookAssignment();
					gradeAssignment.setAllowedToGrade(true);						
					gradeAssignment.setScore(gradeDef.getGrade());
					gradeAssignment.setComment(gradeDef.getGradeComment());
					gradeAssignment.setName(selAssignName);
					gradeAssignment.setPointsPossible(gbItemPointsPossible);						
					gradeAssignment.setUserUuid(studentUuid);
					returnVal.put(studentUuid, gradeAssignment);
				}
				//now populate empty data for users who can be graded but don't have a grade yet:
				for (Iterator iterator = studentIdFunctionMap.entrySet().iterator(); iterator.hasNext();) {
					Entry entry = (Entry) iterator.next();
					if(!returnVal.containsKey(entry.getKey().toString())){
						//this user needs to be added a gradeable:
						DecoratedGradebookAssignment gradeAssignment = new DecoratedGradebookAssignment();
						gradeAssignment.setAllowedToGrade(true);				
						gradeAssignment.setName(selAssignName);
						gradeAssignment.setPointsPossible(gbItemPointsPossible);
						gradeAssignment.setUserUuid(entry.getKey().toString());
						returnVal.put(entry.getKey().toString(), gradeAssignment);
					}
				}
			}			
		}

		return returnVal;
	}

	public String getSelAssignName() {
		return selAssignName;
	}

	public void setSelAssignName(String selAssignName) {
		this.selAssignName = selAssignName;
	}

	public String getGbItemPointsPossible() {
		return gbItemPointsPossible;
	}

	public void setGbItemPointsPossible(String gbItemPointsPossible) {
		this.gbItemPointsPossible = gbItemPointsPossible;
	}
	
	public boolean isGradeByPoints() {
	    return gradeByPoints;
	}
	
	public boolean isGradeByPercent() {
	    return gradeByPercent;
	}
	
	public boolean isGradeByLetter() {
	    return gradeByLetter;
	}
	
	public String proccessActionSubmitGrades(){
		GradebookService gradebookService = getGradebookService();
		if (gradebookService == null) {
			return null;
		}
		
		if(gradeStatistics != null){
	  	
			if(selectedAssign == null || selectedAssign.trim().length()==0 || DEFAULT_GB_ITEM.equalsIgnoreCase(selectedAssign)) 
			{ 
				setErrorMessage(getResourceBundleString(NO_ASSGN)); 
				return null; 
			}     

			if(!validateGradeInput())
				return null;

			try 
			{   
				String selectedAssignName = ((SelectItem)assignments.get((Integer.valueOf(selectedAssign)).intValue())).getLabel();
				String gradebookUuid = toolManager.getCurrentPlacement().getContext();
				
				List<GradeDefinition> gradeInfoToSave = new ArrayList<GradeDefinition>();
				for (DecoratedCompiledMessageStatistics gradeStatistic : gradeStatistics) {
					if(gradeStatistic.getGradebookAssignment() != null && gradeStatistic.getGradebookAssignment().isAllowedToGrade()){
						//ignore empty grades                                                                                  
		                                if(gradeStatistic.getGradebookAssignment().getScore() != null &&
                		                        !"".equals(gradeStatistic.getGradebookAssignment().getScore())){

		                                    GradeDefinition gradeDef = new GradeDefinition();
		                                    gradeDef.setStudentUid(gradeStatistic.getGradebookAssignment().getUserUuid());
		                                    gradeDef.setGrade(gradeStatistic.getGradebookAssignment().getScore());
		                                    gradeDef.setGradeComment(gradeStatistic.getGradebookAssignment().getComment());
		                                    
		                                    gradeInfoToSave.add(gradeDef);

						}
					}
				}
				
				gradebookService.saveGradesAndComments(gradebookUuid, gradebookService.getAssignmentByNameOrId(gradebookUuid, selectedAssignName).getId(), gradeInfoToSave);

				setSuccessMessage(getResourceBundleString(GRADE_SUCCESSFUL));
			} 
			catch(SecurityException se) {
				log.error("MessageForumStatisticsBean Security Exception - proccessActionSubmitGrades:" + se);
				setErrorMessage(getResourceBundleString("cdfm_no_gb_perm"));
			}
			catch(Exception e) 
			{ 
				log.error("MessageForumStatisticsBean - proccessActionSubmitGrades:" + e); 
			} 

			String eventRef = "";
			if(selectedAllTopicsTopicId != null && !"".equals(selectedAllTopicsTopicId)){
				eventRef = getEventReference(selectedAllTopicsTopicId);
			}else{
				eventRef = getEventReference(selectedAllTopicsForumId);
			}
			
			eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_GRADE, eventRef, true));

		}		
		
		//to stop it from going back to the default gradebook item, set gradebookItemChosen flag:
		gradebookItemChosen = true;
		return null;
	}

	private String getEventReference(String ref) 
	{
		String eventMessagePrefix = "";
		final String toolId = toolManager.getCurrentTool().getId();

		if (toolId.equals(DiscussionForumService.MESSAGE_CENTER_ID))
			eventMessagePrefix = "/messagesAndForums";
		else if (toolId.equals(DiscussionForumService.MESSAGES_TOOL_ID))
			eventMessagePrefix = "/messages";
		else
			eventMessagePrefix = "/forums";

		return eventMessagePrefix + getContextSiteId() + "/" + ref + "/" + sessionManager.getCurrentSessionUserId();
	}
	
	private boolean validateGradeInput(){
		boolean validated = true;

		Map<String, String> studentIdToGradeMap = new HashMap<String, String>();
		for (DecoratedCompiledMessageStatistics gradeStatistic : gradeStatistics) {
			if(gradeStatistic.getGradebookAssignment() != null && gradeStatistic.getGradebookAssignment().isAllowedToGrade()){
				//ignore empty grades
                                if(gradeStatistic.getGradebookAssignment().getScore() != null &&
                                        !"".equals(gradeStatistic.getGradebookAssignment().getScore())){
                                    studentIdToGradeMap.put(gradeStatistic.getGradebookAssignment().getUserUuid(), gradeStatistic.getGradebookAssignment().getScore());
					
				}
			}
		}
		
		GradebookService gradebookService = getGradebookService();
		String gradebookUid = toolManager.getCurrentPlacement().getContext();
		List<String> studentsWithInvalidGrades = gradebookService.identifyStudentsWithInvalidGrades(
		        gradebookUid, studentIdToGradeMap);
		
		if (studentsWithInvalidGrades != null && !studentsWithInvalidGrades.isEmpty()) {
		    // let's see if we can give the user additional information. Otherwise,
		    // just use the generic error message
		    if (gradebookService.getGradeEntryType(gradebookUid) == GradebookService.GRADE_TYPE_LETTER) {
		        setErrorMessage(getResourceBundleString(GRADE_INVALID_GENERIC));
		        return false;
		    }
		    
		    for (String studentId : studentsWithInvalidGrades) {
		        String grade = studentIdToGradeMap.get(studentId);
		        
		        if(!isNumber(grade))
	                {
	                        setErrorMessage(getResourceBundleString(GRADE_GREATER_ZERO));
	                        return false;
	                }
	                else if(!isFewerDigit(grade))
	                {
	                        setErrorMessage(getResourceBundleString(GRADE_DECIMAL_WARN));
	                        return false;
	                }
		    }
		    
		    // if we made it this far, just use the generic message
		    setErrorMessage(getResourceBundleString(GRADE_INVALID_GENERIC));
                    return false;
		}
		
		return validated;
	}
	
	private void setErrorMessage(String errorMsg)
	{
		log.debug("setErrorMessage(String " + errorMsg + ")");
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage(FacesMessage.SEVERITY_ERROR, getResourceBundleString(ALERT) + errorMsg, null));
	}
	
	private void setSuccessMessage(String successMsg)
	{
		log.debug("setSuccessMessage(String " + successMsg + ")");
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, successMsg, null));
	}
	
	private String getContextSiteId()
	{
		log.debug("getContextSiteId()");
		return ("/site/" + toolManager.getCurrentPlacement().getContext());
	}
	
	public static boolean isNumber(String validateString) 
	{
		try  
		{
			double d = Double.valueOf(validateString).doubleValue();
			if(d >= 0)
				return true;
			else
				return false;
		}
		catch (NumberFormatException e) 
		{
			log.error(e.getMessage(), e);
			return false;
		}
	}

	public boolean isFewerDigit(String validateString)
	{
		String stringValue = Double.valueOf(validateString).toString();
		if(stringValue.lastIndexOf(".") >= 0)
		{
			String subString = stringValue.substring(stringValue.lastIndexOf("."));
			if(subString != null && subString.length() > 3)
				return false;
		}

		return true;
	}

	public List<SelectItem> getGroups() {
		if(groups == null){
			groups = new ArrayList<SelectItem>();
			try{
				Site currentSite = siteService.getSite(toolManager.getCurrentPlacement().getContext());		
				if(currentSite.hasGroups()){					
					groups.add(new SelectItem(DEFAULT_GB_ITEM, getResourceBundleString(DEFAULT_ALL_GROUPS)));
					Collection siteGroups = currentSite.getGroups();    
					for (Iterator groupIterator = siteGroups.iterator(); groupIterator.hasNext();){
						Group currentGroup = (Group) groupIterator.next();      
						groups.add(new SelectItem(currentGroup.getId(), currentGroup.getTitle()));
					}
				}
			}catch (IdUnusedException e){
				log.error(e.getMessage());
			}
		}
		return groups;
	}

	List<SelectItem> m_groupsForStatisticsByTopic;
	public List<SelectItem> getGroupsForStatisticsByTopic()
	{
		// Set up the topic statistics, cache things like m_displayAnonIds
		if (m_displayAnonIds)
		{
			// Prevent single-user group exploit of anonymity
			m_groupsForStatisticsByTopic = Collections.emptyList();
		}
		else
		{
			m_groupsForStatisticsByTopic = getGroups();
		}
		return m_groupsForStatisticsByTopic;
	}

	public List<SelectItem> getCachedGroupsForStatisticsByTopic()
	{
		return m_groupsForStatisticsByTopic;
	}

	public void setGroups(List<SelectItem> groups) {
		this.groups = groups;
	}

	public String getSelectedGroup() {
		return selectedGroup;
	}

	public void setSelectedGroup(String selectedGroup) {
		if (selectedGroup != null)
		{
			this.selectedGroup = selectedGroup;
		}
	}
	
	private class DecoratedUser{
		private String id;
		private String name;
		
		public DecoratedUser(String id, String name){
			this.id = id;
			this.name = name;
		}
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
	}
	
	public Map<String, Integer> getStudentTopicMessagCount(){
		//Get a map of the user's total messages count. Take into account everything about their access
		Map<String, Integer> studentTotalCount = new HashMap<String, Integer>();
		List<Object[]> totalTopcsCountList = messageManager.findMessageCountTotal();
		Map<Long, Integer> totalTopcsCountMap = new HashMap<Long, Integer>();
		for(Object[] objArr : totalTopcsCountList){
			totalTopcsCountMap.put((Long) objArr[0], ((Long) objArr[1]).intValue());
		}
		Map<String, Boolean> overridingPermissionMap = getOverridingPermissionsMap();

		//loop through the topics and add the counts if user has access:
		List<DiscussionForum> tempForums = forumManager.getForumsForMainPage();
		for (DiscussionForum forum: tempForums) {
			for (Iterator itor = forum.getTopicsSet().iterator(); itor.hasNext(); ) {
				DiscussionTopic currTopic = (DiscussionTopic)itor.next();
				
				Integer topicCount = totalTopcsCountMap.get(currTopic.getId());
				if(topicCount == null){
					topicCount = 0;
				}
				
				Map<String, Integer> totalTopicCountMap = getStudentTopicMessagCount(forum, currTopic, topicCount, overridingPermissionMap);
				for(Entry<String, Integer> entry : totalTopicCountMap.entrySet()){
					Integer studentCount = entry.getValue();
					if(studentTotalCount.containsKey(entry.getKey())){
						studentCount += studentTotalCount.get(entry.getKey());
					}
					studentTotalCount.put(entry.getKey(), studentCount);
				}
			}
		}
		
		return studentTotalCount;
	}
	
	public Map<String, Integer> getStudentTopicMessagCount(DiscussionForum forum, DiscussionTopic currTopic, Integer topicTotalCount, Map<String, Boolean> overridingPermissionMap){
		Map<String, Integer> studentTotalCount = new HashMap<String, Integer>();
		for(Entry<String, Boolean> entry: overridingPermissionMap.entrySet()){

			if ((!forum.getDraft() && currTopic.getDraft().equals(Boolean.FALSE))
					|| entry.getValue())
			{ // this is the start of the big topic if
				// set the message count for moderated topics, otherwise it will be set later
				if(uiPermissionsManager.isRead(currTopic, (DiscussionForum)currTopic.getOpenForum(), entry.getKey())){
					Integer topicCount = topicTotalCount;
					if(topicCount == null){
						topicCount = 0;
					}
					if (currTopic.getModerated() && !uiPermissionsManager.isModeratePostings(currTopic, (DiscussionForum)currTopic.getOpenForum(), entry.getKey())) {
						topicCount = messageManager.findViewableMessageCountByTopicIdByUserId(currTopic.getId(), entry.getKey());
					}
					Integer totalCount = studentTotalCount.get(entry.getKey());
					if(totalCount == null){
						totalCount = 0;
					}
					totalCount += topicCount;
					studentTotalCount.put(entry.getKey(), totalCount);
				}
			}
		}
		return studentTotalCount;
	}
	
	public Map<String, Boolean> getOverridingPermissionsMap(){
		Map<String, Boolean> overridingPermissionMap = new HashMap<String, Boolean>();
		if(courseMemberMap == null){
			courseMemberMap = membershipManager.getAllCourseUsersAsMap();
		}
		List members = membershipManager.convertMemberMapToList(courseMemberMap);
		for (Iterator i = members.iterator(); i.hasNext();) {
			MembershipItem item = (MembershipItem) i.next();
			if (null != item.getUser()) {
				overridingPermissionMap.put(item.getUser().getId(), forumManager.isInstructor(item.getUser()) || securityService.isSuperUser(item.getUser().getId()));
			}
		}
		return overridingPermissionMap;
	}

	private boolean isAnonymousEnabled()
	{
		return anonymousManager.isAnonymousEnabled();
	}

	public boolean isPureAnon()
	{
		if (!isAnonymousEnabled())
		{
			return false;
		}
		return m_displayAnonIds && isAnonymousEnabled();
	}

	private boolean isUseAnonymousId(Topic topic)
	{
		if (!isAnonymousEnabled())
		{
			return false;
		}

		// if topic.postAnonymous
		//   if topic.revealIDsToRoles
		//     return !uiPermissionManager.isIdentifyAnonAuthors
		//   return true
		// return false

		// Condenses to:
		return topic.getPostAnonymous() && (!topic.getRevealIDsToRoles() || !uiPermissionsManager.isIdentifyAnonAuthors(topic));
	}
}
