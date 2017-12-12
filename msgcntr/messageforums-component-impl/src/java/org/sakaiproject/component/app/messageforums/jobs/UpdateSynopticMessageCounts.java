/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.component.app.messageforums.jobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.cover.SynopticMsgcntrManagerCover;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.site.api.SiteService;

@Slf4j
public class UpdateSynopticMessageCounts implements Job{

	
	private PrivateMessageManager pvtMessageManager;
	private MessageForumsTypeManager typeManager;
	private DiscussionForumManager forumManager;
	private UIPermissionsManager uiPermissionsManager;
	private MessageForumsMessageManager messageManager;
	private SiteService siteService;
	private SecurityService securityService;
	private SqlService sqlService;

	private static final boolean runOracleSQL = false;
	//this SQL is more generic but also slower
	private static final String FIND_ALL_SYNOPTIC_SITES_QUERY_GENERIC = "select SITE_ID, TITLE from SAKAI_SITE where IS_USER = 0 and PUBLISHED = 1 and IS_SPECIAL = 0";
	private static final String FIND_ALL_SYNOPTIC_SITES_BY_SITE_QUERY_GENERIC = FIND_ALL_SYNOPTIC_SITES_QUERY_GENERIC + " and SITE_ID like ?";
	//this SQL works for Oracle and runs faster than the Generic Query
	private static final String FIND_ALL_SYNOPTIC_SITES_QUERY_ORACLE_PART1 = "select q.SITE_ID, q.TITLE, sum(q.Decoded) as BINARY_FLAGS from (" +
																	"select ss.SITE_ID, ss.TITLE, " +
																	"decode (sst.REGISTRATION,'sakai.messagecenter',100,'sakai.messages',10,'sakai.forums',1,0) as Decoded " +
																	"from SAKAI_SITE ss, SAKAI_SITE_TOOL sst " +
																	"where ss.IS_USER = 0 " +
																	"and ss.PUBLISHED = 1 " +
																	"and ss.IS_SPECIAL = 0 " +
																	"and ss.SITE_ID = sst.SITE_ID ";
	private static final String FIND_ALL_SYNOPTIC_SITES_QUERY_ORACLE_PART2 = "and sst.REGISTRATION in ('sakai.messagecenter','sakai.messages','sakai.forums')) q " +
																	"Group By q.SITE_ID, q.TITLE";
	private static final String FIND_ALL_SYNOPTIC_SITES_QUERY_ORACLE = FIND_ALL_SYNOPTIC_SITES_QUERY_ORACLE_PART1 + FIND_ALL_SYNOPTIC_SITES_QUERY_ORACLE_PART2;
	private static final String FIND_ALL_SYNOPTIC_SITES_BY_SITE_QUERY_ORACLE = FIND_ALL_SYNOPTIC_SITES_QUERY_ORACLE_PART1 +
																	"and ss.SITE_ID like ? " +
																	FIND_ALL_SYNOPTIC_SITES_QUERY_ORACLE_PART2;
	
	private static final String UNREAD_MESSAGES_QUERY = "SELECT message.USER_ID, message.CONTEXT_ID, count(*) unread_messages " +
															"FROM MFR_PVT_MSG_USR_T message " +										
															"where READ_STATUS = 0 " +
															"group by message.USER_ID, message.CONTEXT_ID";
	private static final String UNREAD_MESSAGES_BY_SITE_QUERY = "SELECT message.USER_ID, message.CONTEXT_ID, count(*) unread_messages " +
															"FROM MFR_PVT_MSG_USR_T message " +										
															"where READ_STATUS = 0 " +
															"and message.CONTEXT_ID like ?" +
															"group by message.USER_ID, message.CONTEXT_ID";
	
	private static final String TOPICS_AND_FORUMS_QUERY = "select area.CONTEXT_ID, forum.ID as FORUM_ID, topic.ID as TOPIC_ID, forum.DRAFT as isForumDraft, topic.DRAFT as isTopicDraft, topic.MODERATED as isTopicModerated, forum.LOCKED as isForumLocked, topic.LOCKED as isTopicLocked, forum.CREATED_BY as forumCreatedBy, topic.CREATED_BY as topicCreatedBy " +
															"from MFR_AREA_T area, MFR_OPEN_FORUM_T forum, MFR_TOPIC_T topic " +
															"Where area.ID = forum.surrogateKey and forum.ID = topic.of_surrogateKey";
	
	private boolean updateNewMembersOnly = ServerConfigurationService.getBoolean("msgcntr.synoptic.updateMessageCounts.updateNewMembersOnly", false);
	//by default, this job only updates/adds the counts when the counts for forums or messages isn't 0, this overrides that and forces updates for all items no matter what
	private boolean addItemsWhenNoUnreadCounts = ServerConfigurationService.getBoolean("msgcntr.synoptic.updateMessageCounts.addItemsWhenNoUnreadCounts", false);
	
	public void init() {
		
	}
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{

		Connection clConnection = null;  	
		Statement statement = null;
		ResultSet unreadMessageCountRS = null;
		ResultSet allTopicsAndForumsRS = null;
		ResultSet synotpicSitesRS = null;
		PreparedStatement unreadMessagesbySitePS = null;
		PreparedStatement findSitesbySitePS = null;		
		String siteFilter = ServerConfigurationService.getString("msgcntr.synoptic.updateMessageCountsSiteFilter");
		boolean filterSites = siteFilter != null && !"".equals(siteFilter);
		
		
		
		//loop through all sites and call updateSynopticToolInfoForAllUsers
		int count = 0;

		log.info("UpdateSynopticMessageCounts job launched: " + new Date());

		try {
			clConnection = sqlService.borrowConnection();
			statement = clConnection.createStatement();	
			
			//CREATE HASHMAP OF UNREAD MESSAGES COUNT
			if(filterSites){
				unreadMessagesbySitePS = clConnection.prepareStatement(UNREAD_MESSAGES_BY_SITE_QUERY);
				unreadMessagesbySitePS.setString(1, siteFilter);
				unreadMessageCountRS = unreadMessagesbySitePS.executeQuery();
			}else{
				unreadMessageCountRS = statement.executeQuery(UNREAD_MESSAGES_QUERY);
			}
			HashMap<String, HashMap<String, Integer>> siteAndUserMessageCountHM = getSiteAndUserMessageCountHM(unreadMessageCountRS);
			
			
			//CREATE HASHMAP OF ALL TOPICS AND FORUMS
			allTopicsAndForumsRS = statement.executeQuery(TOPICS_AND_FORUMS_QUERY);
			HashMap<String, HashMap<Long, DecoratedForumInfo>> allTopicsAndForumsHM = getAllTopicsAndForumsHM(allTopicsAndForumsRS);
			//LOOP TRHOUGH ALL SITES AND UPDATE INFO
			String siteId = "";
			String siteTitle = "";
			int BINARY_FLAGS;
			
			if(runOracleSQL){
				if(filterSites){
					findSitesbySitePS = clConnection.prepareStatement(FIND_ALL_SYNOPTIC_SITES_BY_SITE_QUERY_ORACLE);
					findSitesbySitePS.setString(1, siteFilter);
					synotpicSitesRS = findSitesbySitePS.executeQuery();
				}else{
					synotpicSitesRS = statement.executeQuery(FIND_ALL_SYNOPTIC_SITES_QUERY_ORACLE);
				}
			}else{
				if(filterSites){
					findSitesbySitePS = clConnection.prepareStatement(FIND_ALL_SYNOPTIC_SITES_BY_SITE_QUERY_GENERIC);
					findSitesbySitePS.setString(1, siteFilter);
					synotpicSitesRS = findSitesbySitePS.executeQuery();
				}else{
					synotpicSitesRS = statement.executeQuery(FIND_ALL_SYNOPTIC_SITES_QUERY_GENERIC);
				}
			}
			
			while (synotpicSitesRS.next()) {
				boolean isMessageForumsPageInSite = false;
				boolean isMessagesPageInSite = false;
				boolean isForumsPageInSite = false;
				
				siteId = synotpicSitesRS.getString("SITE_ID");
				siteTitle = synotpicSitesRS.getString("TITLE");	
			
				if(runOracleSQL){
					BINARY_FLAGS = synotpicSitesRS.getInt("BINARY_FLAGS");

					//BINARY_FLAGS returns a sumation of the values:
					//has messagecenter= 100
					//has messages     = 10
					//has forums       = 1

					//so if a tool has both messages and forums, the value would be 11
					//this if it as binary true false where first bit is messagecenter, 
					//second is messages, and 3rd is forums

					if(BINARY_FLAGS % 10 >= 1)
						isForumsPageInSite = true;

					if(BINARY_FLAGS % 100 >= 10)
						isMessagesPageInSite = true;

					if(BINARY_FLAGS % 1000 >= 100)
						isMessageForumsPageInSite = true;
				}
				
				updateSynopticToolInfoForAllUsers(siteId, siteTitle, clConnection, isMessageForumsPageInSite, isMessagesPageInSite, isForumsPageInSite, siteAndUserMessageCountHM, allTopicsAndForumsHM);
				
				count++;
				if(count % 1000 == 0){
					log.info("UpdateSynopticMessageCounts Progress: " + count + " Sites updated");
				}
			}
						
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
		} finally {
			try {
				if(unreadMessageCountRS != null)
					unreadMessageCountRS.close();
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
			try {
				if(allTopicsAndForumsRS != null)
					allTopicsAndForumsRS.close();
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
			try {
				if(synotpicSitesRS != null)
					synotpicSitesRS.close();
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
			try {
				if(statement != null)
					statement.close();
			} catch (Exception e) {
				log.warn(e.getMessage());
			}	
			try{
				if(unreadMessagesbySitePS != null)
					unreadMessagesbySitePS.close();				
			}catch(Exception e){
				log.warn(e.getMessage());
			}
			try{
				if(findSitesbySitePS != null)
					findSitesbySitePS.close();				
			}catch(Exception e){
				log.warn(e.getMessage());
			}
			sqlService.returnConnection(clConnection);
		}
		
		log.info("UpdateSynopticMessageCounts job finished: " + new Date());
	}
	
	
	private void updateSynopticToolInfoForAllUsers(String siteId, String siteTitle, Connection clConnection, boolean isMessageForumsPageInSite, boolean isMessagesPageInSite, boolean isForumsPageInSite, HashMap<String, HashMap<String, Integer>> siteAndUserMessageCountHM, HashMap<String, HashMap<Long, DecoratedForumInfo>> allTopicsAndForumsHM) throws Exception
	{		
		PreparedStatement isMessageForumsPageInSiteQuery = null;
		PreparedStatement isMessagesPageInSiteQuery = null;
		PreparedStatement isForumsPageInSiteQuery = null;
		PreparedStatement getAllUsersInSiteQuery = null;
		if(!runOracleSQL){		
			ResultSet rsMessagesForums = null;
			ResultSet rsMessages = null;
			ResultSet rsForusm = null;
			try{
				isMessageForumsPageInSiteQuery = clConnection.prepareStatement("select * from SAKAI_SITE_TOOL where SITE_ID = ? and REGISTRATION = 'sakai.messagecenter'");
				isMessageForumsPageInSiteQuery.setString(1, siteId);
				isMessagesPageInSiteQuery = clConnection.prepareStatement("select * from SAKAI_SITE_TOOL where SITE_ID = ? and REGISTRATION = 'sakai.messages'");
				isMessagesPageInSiteQuery.setString(1, siteId);
				isForumsPageInSiteQuery = clConnection.prepareStatement("select * from SAKAI_SITE_TOOL where SITE_ID = ? and REGISTRATION = 'sakai.forums'");
				isForumsPageInSiteQuery.setString(1, siteId);


				rsMessagesForums = isMessageForumsPageInSiteQuery.executeQuery();
				while(rsMessagesForums.next()){
					isMessageForumsPageInSite = true;
				}

				rsMessages = isMessagesPageInSiteQuery.executeQuery();
				while(rsMessages.next()){
					isMessagesPageInSite = true;
				}

				rsForusm = isForumsPageInSiteQuery.executeQuery();
				while(rsForusm.next()){
					isForumsPageInSite = true;
				}
			}catch (Exception e){
				log.warn(e.getMessage(), e);
			}finally{
				try{
					if(rsMessagesForums != null)
						rsMessagesForums.close();
				}catch (Exception e){
					log.warn(e.getMessage());
				}
				try{
					if(rsMessages != null)
						rsMessages.close();
				}catch (Exception e){
					log.warn(e.getMessage());
				}
				try{
					if(rsForusm != null)
						rsForusm.close();
				}catch (Exception e){
					log.warn(e.getMessage());
				}
			}
		}
		
		ResultSet usersMap = null;

		try{
			String allUsersQuery = "select USER_ID from SAKAI_SITE_USER where SITE_ID = ?";
			if(updateNewMembersOnly){
				allUsersQuery += " and USER_ID not in (select USER_ID from MFR_SYNOPTIC_ITEM where SITE_ID = ?)";
			}
			getAllUsersInSiteQuery = clConnection.prepareStatement(allUsersQuery);
			
			getAllUsersInSiteQuery.setString(1, siteId);
			if(updateNewMembersOnly){
				getAllUsersInSiteQuery.setString(2, siteId);
			}
			
			usersMap = getAllUsersInSiteQuery.executeQuery();

			//loop through all users in site and update their information:
			HashMap<String, Integer> userHM = null;
			Integer count = null;
			Map<String, Integer[]> unreadCountMap = new HashMap<String, Integer[]>();
			List<String> userIds = new ArrayList<String>();
			while(usersMap.next()){
				int unreadPrivate = 0;
				int unreadForum = 0;

				String userId = usersMap.getString("USER_ID");
				userIds.add(userId);

				//message count:			
				if (isMessageForumsPageInSite || isMessagesPageInSite) 
				{
					userHM = siteAndUserMessageCountHM.get(siteId);
					if(userHM != null){
						count = userHM.get(userId);
						if(count != null){
							unreadPrivate = count.intValue();
						}					
					}
				}

				boolean isSuperUser = securityService.isSuperUser(userId); 

				//forums count:
				HashMap<Long, DecoratedForumInfo> dfHM = null;
				if (isMessageForumsPageInSite || isForumsPageInSite){			
					dfHM = allTopicsAndForumsHM.get(siteId);
					if(dfHM != null){
						//site has forums added to the tool
						Set<Entry<Long, DecoratedForumInfo>> dfEntrySet = dfHM.entrySet();

						for (Iterator<Entry<Long, DecoratedForumInfo>> iterator = dfEntrySet.iterator(); iterator.hasNext();) {
							Entry<Long, DecoratedForumInfo> entry = iterator.next(); 
							Long dfId = entry.getKey();

							DecoratedForumInfo dForum = entry.getValue();
							boolean isInstructor = getForumManager().isInstructor(userId, "/site/" + siteId);

							// Only count unread messages for forums the user can view:
							if (dForum.getIsDraft().equals(Boolean.FALSE)
									||isInstructor
									|| isSuperUser
									||forumManager.isForumOwner(dfId, dForum.getCreator(), userId, "/site/" + siteId))
							{ 


								final Iterator<DecoratedTopicsInfo> topicIter = dForum.getTopics().iterator();

								while (topicIter.hasNext()) 
								{
									DecoratedTopicsInfo topic = (DecoratedTopicsInfo) topicIter.next();

									Long topicId = topic.getTopicId();
									Boolean isTopicDraft = topic.getIsDraft();
									Boolean isTopicModerated = topic.getIsModerated();
									Boolean isTopicLocked = topic.getIsLocked();
									String topicOwner = topic.getCreator();

									//Only count unread messages for topics the user can view:
									if (isTopicDraft.equals(Boolean.FALSE)
											|| isInstructor
											|| isSuperUser
											||forumManager.isTopicOwner(topicId, topicOwner, userId, "/site/" + siteId)){ 

										if (getUiPermissionsManager().isRead(topicId, isTopicDraft, dForum.getIsDraft(), userId, siteId))
										{
											if (!isTopicModerated.booleanValue() || (isTopicModerated.booleanValue() && 
													getUiPermissionsManager().isModeratePostings(topicId, dForum.getIsLocked(), dForum.getIsDraft(), isTopicLocked, isTopicDraft, userId, siteId)))
											{
												unreadForum += getMessageManager().findUnreadMessageCountByTopicIdByUserId(topicId, userId);
											}
											else
											{	
												// b/c topic is moderated and user does not have mod perm, user may only
												// see approved msgs or pending/denied msgs authored by user
												unreadForum += getMessageManager().findUnreadViewableMessageCountByTopicIdByUserId(topicId, userId);

											}
										}
									}
								}
							}
						}
					}		
				}

				//update synoptic tool info:
				if(unreadPrivate != 0 || unreadForum != 0 || updateNewMembersOnly || addItemsWhenNoUnreadCounts){
					unreadCountMap.put(userId, new Integer[]{unreadPrivate, unreadForum});
				}
			}
			SynopticMsgcntrManagerCover.createOrUpdateSynopticToolInfo(userIds, siteId, siteTitle, unreadCountMap);
		}catch (Exception e){
			log.warn(e.getMessage(), e);
		}finally{

			try{
				if(usersMap != null)
					usersMap.close();
			}catch(Exception e){
				log.warn(e.getMessage(), e);
			}
			try{
				if(getAllUsersInSiteQuery != null)
					getAllUsersInSiteQuery.close();
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
			try {
				if (isForumsPageInSiteQuery != null)
					isForumsPageInSiteQuery.close();
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
			try {
				if (isMessagesPageInSiteQuery != null)
					isMessagesPageInSiteQuery.close();
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
			try {
				if (isMessageForumsPageInSiteQuery != null)
					isMessageForumsPageInSiteQuery.close();
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}
	}
	
	
	
	public HashMap<String, HashMap<Long, DecoratedForumInfo>> getAllTopicsAndForumsHM(ResultSet rs){
		HashMap<String, HashMap<Long, DecoratedForumInfo>> returnHM = new HashMap<String, HashMap<Long, DecoratedForumInfo>>();

		if(rs != null){
			String CONTEXT_ID, FORUM_CREATED_BY, TOPIC_CREATED_BY;
			Long FORUM_ID, TOPIC_ID;
			Boolean IS_TOPIC_DRAFT, IS_FORUM_DRAFT, IS_TOPIC_MODERATED, IS_FORUM_LOCKED, IS_TOPIC_LOCKED;
			try{
				while(rs.next()){
					CONTEXT_ID = rs.getString("CONTEXT_ID");
					FORUM_ID = rs.getLong("FORUM_ID");
					TOPIC_ID = rs.getLong("TOPIC_ID");
					IS_TOPIC_DRAFT = rs.getBoolean("isForumDraft");
					IS_FORUM_DRAFT = rs.getBoolean("isTopicDraft");
					IS_TOPIC_MODERATED = rs.getBoolean("isTopicModerated");
					IS_FORUM_LOCKED = rs.getBoolean("isForumLocked");
					IS_TOPIC_LOCKED = rs.getBoolean("isTopicLocked");
					FORUM_CREATED_BY = rs.getString("forumCreatedBy");
					TOPIC_CREATED_BY = rs.getString("topicCreatedBy");
					
					if(returnHM.containsKey(CONTEXT_ID)){
						//hashmap already has this site id, now look for forum id:
						if(returnHM.get(CONTEXT_ID).containsKey(FORUM_ID)){
							
							DecoratedTopicsInfo dTopic = new DecoratedTopicsInfo(TOPIC_ID, IS_TOPIC_LOCKED, IS_TOPIC_DRAFT, IS_TOPIC_MODERATED, TOPIC_CREATED_BY);
							returnHM.get(CONTEXT_ID).get(FORUM_ID).addTopic(dTopic);
						}else{
							//this is a new forum, so add it to the list
							
							DecoratedTopicsInfo dTopic = new DecoratedTopicsInfo(TOPIC_ID, IS_TOPIC_LOCKED, IS_TOPIC_DRAFT, IS_TOPIC_MODERATED, TOPIC_CREATED_BY);
							DecoratedForumInfo dForum = new DecoratedForumInfo(FORUM_ID, IS_FORUM_LOCKED, IS_FORUM_DRAFT, FORUM_CREATED_BY);
							dForum.addTopic(dTopic);
														
							returnHM.get(CONTEXT_ID).put(FORUM_ID, dForum);
						}												
					}else{
						DecoratedTopicsInfo dTopic = new DecoratedTopicsInfo(TOPIC_ID, IS_TOPIC_LOCKED, IS_TOPIC_DRAFT, IS_TOPIC_MODERATED, TOPIC_CREATED_BY);
						
						DecoratedForumInfo dForum = new DecoratedForumInfo(FORUM_ID, IS_FORUM_LOCKED, IS_FORUM_DRAFT, FORUM_CREATED_BY);
						
						dForum.addTopic(dTopic);
						
						
						HashMap<Long, DecoratedForumInfo> forumHM = new HashMap<Long, DecoratedForumInfo>();
						
						forumHM.put(FORUM_ID, dForum);
						
						returnHM.put(CONTEXT_ID, forumHM);
					}
				}
			}catch(Exception e){
				log.error(e.getMessage(), e);
			}
		}
		
		return returnHM;
		
	}
	
	public HashMap<String, HashMap<String, Integer>> getSiteAndUserMessageCountHM(ResultSet rs){
		HashMap<String, HashMap<String, Integer>> returnHM = new HashMap<String, HashMap<String, Integer>>();
		
		if(rs != null){
			try{
				String siteId, userId;
				Integer messageCount;
			while(rs.next()){
				
				siteId = rs.getString("CONTEXT_ID");
				userId = rs.getString("USER_ID");
				messageCount = Integer.valueOf(rs.getInt("unread_messages"));
				
				if(returnHM.containsKey(siteId)){
					returnHM.get(siteId).put(userId, messageCount);
				}else{
					HashMap<String, Integer> newHashMap = new HashMap<String, Integer>();
					newHashMap.put(userId, messageCount);
					returnHM.put(siteId, newHashMap);
				}				
			}
			}catch(Exception e){
				log.error(e.getMessage(), e);
			}
		}
		
		return returnHM;
	}

	
	public PrivateMessageManager getPvtMessageManager() {
		return pvtMessageManager;
	}


	public void setPvtMessageManager(PrivateMessageManager pvtMessageManager) {
		this.pvtMessageManager = pvtMessageManager;
	}


	public MessageForumsTypeManager getTypeManager() {
		return typeManager;
	}


	public void setTypeManager(MessageForumsTypeManager typeManager) {
		this.typeManager = typeManager;
	}


	public DiscussionForumManager getForumManager() {
		return forumManager;
	}


	public void setForumManager(DiscussionForumManager forumManager) {
		this.forumManager = forumManager;
	}


	public UIPermissionsManager getUiPermissionsManager() {
		return uiPermissionsManager;
	}


	public void setUiPermissionsManager(UIPermissionsManager uiPermissionsManager) {
		this.uiPermissionsManager = uiPermissionsManager;
	}


	public MessageForumsMessageManager getMessageManager() {
		return messageManager;
	}


	public void setMessageManager(MessageForumsMessageManager messageManager) {
		this.messageManager = messageManager;
	}

	public SiteService getSiteService() {
		return siteService;
	}


	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}

	public class DecoratedForumInfo{
		
		private Long forumId;
		private Boolean isLocked, isDraft;
		private String creator;
		private ArrayList<DecoratedTopicsInfo> topics = new ArrayList<DecoratedTopicsInfo>();
		
		public DecoratedForumInfo(Long forumId, Boolean isLocked, Boolean isDraft, String creator){
			this.forumId = forumId;
			this.isLocked = isLocked;
			this.isDraft = isDraft;
			this.creator = creator;
		}

		public Long getForumId() {
			return forumId;
		}

		public void setForumId(Long forumId) {
			this.forumId = forumId;
		}

		public Boolean getIsLocked() {
			return isLocked;
		}

		public void setIsLocked(Boolean isLocked) {
			this.isLocked = isLocked;
		}

		public Boolean getIsDraft() {
			return isDraft;
		}

		public void setIsDraft(Boolean isDraft) {
			this.isDraft = isDraft;
		}

		public String getCreator() {
			return creator;
		}

		public void setCreator(String creator) {
			this.creator = creator;
		}

		public void addTopic(DecoratedTopicsInfo dTopics){
			topics.add(dTopics);
		}
		
		public ArrayList<DecoratedTopicsInfo> getTopics(){
			return topics;
		}
	}

	public class DecoratedTopicsInfo{
		
		private Long topicId;
		private Boolean isLocked, isDraft, isModerated;
		private String creator;
		
		public DecoratedTopicsInfo(Long topicId, Boolean isLocked, Boolean isDraft, Boolean isModerated, String creator){
			this.topicId = topicId;
			this.isLocked = isLocked;
			this.isDraft = isDraft;
			this.isModerated = isModerated;
			this.creator = creator;
		}

		public Long getTopicId() {
			return topicId;
		}

		public void setTopicId(Long topicId) {
			this.topicId = topicId;
		}

		public Boolean getIsLocked() {
			return isLocked;
		}

		public void setIsLocked(Boolean isLocked) {
			this.isLocked = isLocked;
		}

		public Boolean getIsDraft() {
			return isDraft;
		}

		public void setIsDraft(Boolean isDraft) {
			this.isDraft = isDraft;
		}

		public Boolean getIsModerated() {
			return isModerated;
		}

		public void setIsModerated(Boolean isModerated) {
			this.isModerated = isModerated;
		}

		public String getCreator() {
			return creator;
		}

		public void setCreator(String creator) {
			this.creator = creator;
		}
	}
}



