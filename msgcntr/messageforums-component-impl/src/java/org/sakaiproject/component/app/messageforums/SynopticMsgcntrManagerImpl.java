package org.sakaiproject.component.app.messageforums;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrItem;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.app.messageforums.dao.hibernate.SynopticMsgcntrItemImpl;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class SynopticMsgcntrManagerImpl extends HibernateDaoSupport implements SynopticMsgcntrManager {
	
	private static final Log LOG = LogFactory.getLog(SynopticMsgcntrManagerImpl.class);
	private static final String QUERY_WORKSPACE_SYNOPTIC_ITEMS = "findWorkspaceSynopticMsgcntrItems";
	private static final String QUERY_SITE_SYNOPTIC_ITEMS = "findSiteSynopticMsgcntrItems";
	private static final String QUERY_UPDATE_ALL_SITE_TITLES = "updateSiteTitles";

	private HashMap mfPageInSiteMap, sitesMap;
	// transient variable for when on home page of site
	private transient DecoratedCompiledMessageStats siteContents;
	private MessageForumsMessageManager messageManager;
	private UIPermissionsManager uiPermissionsManager;
	private PrivateMessageManager pvtMessageManager;
	private MessageForumsTypeManager typeManager;
	private DiscussionForumManager forumManager;
	
	private static int ORACLE_IN_CLAUSE_SIZE_LIMIT = 1000;

	
	public SynopticMsgcntrManagerImpl() {}
	
	public void init() {
		LOG.info("init()");
	}

	public List<SynopticMsgcntrItem> getWorkspaceSynopticMsgcntrItems(final String userId) {

		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.getNamedQuery(QUERY_WORKSPACE_SYNOPTIC_ITEMS);
				q.setParameter("userId", userId, Hibernate.STRING);
				return q.list();
			}
		};

		return (List<SynopticMsgcntrItem>) getHibernateTemplate().execute(hcb);	  
	}

	public List<SynopticMsgcntrItem> getSiteSynopticMsgcntrItems(final List<String> userIds, final String siteId) {
		if(userIds == null || userIds.size() == 0){
			return new ArrayList<SynopticMsgcntrItem>();
		}
		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				List rtn = new ArrayList();
				Query q = session.getNamedQuery(QUERY_SITE_SYNOPTIC_ITEMS);
				q.setParameter("siteId", siteId, Hibernate.STRING);
				for (int initIndex = 0; initIndex < userIds.size(); initIndex+=ORACLE_IN_CLAUSE_SIZE_LIMIT) {
					q.setParameterList("userIds", userIds.subList(initIndex, Math.min(initIndex+ORACLE_IN_CLAUSE_SIZE_LIMIT, userIds.size())));
					rtn.addAll(q.list());
				}
				return rtn;
			}
		};

		return (List<SynopticMsgcntrItem>) getHibernateTemplate().execute(hcb);	  
	}

	public SynopticMsgcntrItem createSynopticMsgcntrItem(String userId, String siteId, String siteTitle){
		return new SynopticMsgcntrItemImpl(userId, siteId, siteTitle);
	}

	public void saveSynopticMsgcntrItems(List<SynopticMsgcntrItem> items){
		for (SynopticMsgcntrItem item : items) {
			getHibernateTemplate().saveOrUpdate(item);
		}
	}
	
	public void deleteSynopticMsgcntrItem(SynopticMsgcntrItem item){
		getHibernateTemplate().delete(item);
	}
	
	public void incrementMessagesSynopticToolInfo(List<String> userIds, String siteId){
		incAndDecSynopticToolInfo(userIds, siteId, true, true);
	}
	
	public void incrementForumSynopticToolInfo(List<String> userIds, String siteId){
		incAndDecSynopticToolInfo(userIds, siteId, false, true);
	}
	
	public void decrementMessagesSynopticToolInfo(List<String> userIds, String siteId){
		incAndDecSynopticToolInfo(userIds, siteId, true, false);
	}
	
	public void decrementForumSynopticToolInfo(List<String> userIds, String siteId){
		incAndDecSynopticToolInfo(userIds, siteId, false, false);
	}
	
	private void incAndDecSynopticToolInfo(List<String> userIds, String siteId, boolean messages, boolean increment){
		if(userIds == null || userIds.size() == 0){
			return;
		}
		  List<SynopticMsgcntrItem> items = getSiteSynopticMsgcntrItems(userIds, siteId);
		  //first, find the users who don't have an item yet:
		  List<String> missingUsers = new ArrayList<String>(userIds);
		  List<String> foundUsers = new ArrayList<String>();
		  if(items != null){
			  for(SynopticMsgcntrItem item : items){
				  missingUsers.remove(item.getUserId());
				  foundUsers.add(item.getUserId());
			  }
		  }
		  
		  resetMessagesAndForumSynopticInfo(missingUsers, siteId, items);
		  
		  if(foundUsers != null && foundUsers.size() > 0){
			  //now take the existing list of found items and decrement them
			  String[] userIdsArr = foundUsers.toArray(new String[]{});
			  int subArrayIndex = 0;
				do{
					int subArraySize = ORACLE_IN_CLAUSE_SIZE_LIMIT;
					if(subArrayIndex + subArraySize > userIdsArr.length){
						subArraySize = (userIdsArr.length - subArrayIndex);
					}
					String[] subSiteRefs = Arrays.copyOfRange(userIdsArr, subArrayIndex, subArrayIndex + subArraySize);
					String COL = messages ? "NEW_MESSAGES_COUNT" : "NEW_FORUM_COUNT";
					String symbol = increment ? "+" : "-";
					String decWhere = increment ? "" : " AND " + COL + " > 0";
					String query = "UPDATE MFR_SYNOPTIC_ITEM SET " + COL + " = " + COL + " " + symbol + " 1 WHERE USER_ID in (?) and SITE_ID = '" + siteId + "'" + decWhere;
					String inParams = "(";
					for(int i = 0; i < subSiteRefs.length; i++){
						inParams += "'" + subSiteRefs[i].replace("'", "''") + "'"; //escape apostrophe
						if(i < subSiteRefs.length - 1){
							inParams += ",";
						}
					}
					inParams += ")";
					query = query.replace("(?)", inParams);
					
					Connection clConnection = null;  	
					//Statement statement = null;
					PreparedStatement updateStatement = null;
					try {
						clConnection = SqlService.borrowConnection();
						updateStatement = clConnection.prepareStatement(query);
						updateStatement.execute();
						//in case autosubmit isn't true, commit this right away
						clConnection.commit();
					}catch(Exception e){
						LOG.error(e.getMessage(), e);
					}finally{
						if(updateStatement != null){
							try {
								updateStatement.close();
							} catch (SQLException e) {
								LOG.error(e.getMessage(), e);
							}
						}
						SqlService.returnConnection(clConnection);
					}					
					
					subArrayIndex = subArrayIndex + subArraySize;
				}while(subArrayIndex < userIdsArr.length);
		  }		 
	}
	
	
	public void resetMessagesAndForumSynopticInfo(List<String> userIds, String siteId, List<SynopticMsgcntrItem> items) {
		
		//MSGCNTR-430 we can't do this if we don't know the user -DH
		if (userIds == null || userIds.size() == 0) {
			return;
		}
		//find which user's need a new item:
		List<String> newUsers = new ArrayList<String>(userIds);
		if(items != null){
			for(SynopticMsgcntrItem item : items){
				newUsers.remove(item.getUserId());
			}
		}
		//Get Stats for all users:
		Map<String, DecoratedCompiledMessageStats> dcmStats = this.getSiteInfo(siteId, userIds);
		
		//update existing ones:
		for(SynopticMsgcntrItem item : items){
			DecoratedCompiledMessageStats dcmStat = dcmStats.get(item.getUserId());
			if(dcmStat != null){
				item.setNewMessagesCount(dcmStat.getUnreadPrivateAmt());
				item.setMessagesLastVisitToCurrentDt();

				item.setNewForumCount(dcmStat.getUnreadForumsAmt());
				item.setForumLastVisitToCurrentDt();
			}
		}
		
		//now create the new items:
		for(String userId : newUsers){
			DecoratedCompiledMessageStats dcmStat = dcmStats.get(userId);
			if(dcmStat != null){
				SynopticMsgcntrItem synopticMsgcntrItem = createSynopticMsgcntrItem(userId, siteId, dcmStat.getSiteName());
				synopticMsgcntrItem.setNewMessagesCount(dcmStat.getUnreadPrivateAmt());
				synopticMsgcntrItem.setNewForumCount(dcmStat.getUnreadForumsAmt());
				items.add(synopticMsgcntrItem);
			}
		}

		saveSynopticMsgcntrItems(items);
	}

	public void setMessagesSynopticInfoHelper(String userId, String siteId, int newMessageCount){
		setSynopticInfoHelper(userId, siteId, true, newMessageCount);
	}
	
	public void setForumSynopticInfoHelper(String userId, String siteId, int newMessageCount){
		setSynopticInfoHelper(userId, siteId, false, newMessageCount);
	}
	
	private void setSynopticInfoHelper(String userId, String siteId, boolean messages, int newMessageCount){
		//this could be an anon access
		if (userId == null && forumManager.getAnonRole()) {
			return;
		}
		
		List<SynopticMsgcntrItem> items = getSiteSynopticMsgcntrItems(Arrays.asList(userId), siteId);
		SynopticMsgcntrItem item = null;
		if(items != null && items.size() == 1){
			item = items.get(0);
		}
		if(item == null){
			//item does not exist, call the reset function to set the 
			//actually number of unread messages instead of decrementing
			resetMessagesAndForumSynopticInfo(Arrays.asList(userId), siteId, items);
		}else{
			if(messages){
				item.setNewMessagesCount(newMessageCount);				
				item.setMessagesLastVisitToCurrentDt();
			}else{
				item.setNewForumCount(newMessageCount);
				item.setForumLastVisitToCurrentDt();
			}
			saveSynopticMsgcntrItems(Arrays.asList(item));
		}
	}
	
	/*
	 * Update Difference will get the existing count and add the difference (if negative, then its a subtract mathamatically)
	 */
	
	public void updateDifferenceMessagesSynopticInfoHelper(String userId, String siteId, int differenceCount){
		updateDifferenceSynopticInfoHelper(userId, siteId, true, differenceCount);
	}
	
	public void updateDifferenceForumSynopticInfoHelper(String userId, String siteId, int differenceCount){
		updateDifferenceSynopticInfoHelper(userId, siteId, false, differenceCount);
	}
	
	private void updateDifferenceSynopticInfoHelper(String userId, String siteId, boolean messages, int differenceCount){
		List<SynopticMsgcntrItem> items = getSiteSynopticMsgcntrItems(Arrays.asList(userId), siteId);
		SynopticMsgcntrItem item = null;
		if(items != null && items.size() == 1){
			item = items.get(0);
		}
		if(item == null){
			//item does not exist, call the reset function to set the 
			//actually number of unread messages instead of decrementing
			resetMessagesAndForumSynopticInfo(Arrays.asList(userId), siteId, items);
		}else{
			if(messages){
				int newCount = item.getNewMessagesCount() + differenceCount;
				if(newCount < 0){
					newCount = 0;
				}
				item.setNewMessagesCount(newCount);				
				item.setMessagesLastVisitToCurrentDt();
			}else{
				int newCount = item.getNewForumCount() + differenceCount;
				if(newCount < 0){
					newCount = 0;
				}
				item.setNewForumCount(newCount);
				item.setForumLastVisitToCurrentDt();
			}
			saveSynopticMsgcntrItems(Arrays.asList(item));
		}
	}
	
	
	public void createOrUpdateSynopticToolInfo(List<String> userIds, String siteId, String siteTitle, Map<String, Integer[]> unreadCounts){	
		List<SynopticMsgcntrItem> items = getSiteSynopticMsgcntrItems(userIds, siteId);
		List<String> newUsers = new ArrayList<String>(userIds);
		for(SynopticMsgcntrItem item : items){
			newUsers.remove(item.getUserId());
		}
		boolean updateSiteTitles = false;
		for(SynopticMsgcntrItem item : items){
			if(unreadCounts.containsKey(item.getUserId())){
				int unreadMessageCount = unreadCounts.get(item.getUserId())[0];
				int unreadForumCount = unreadCounts.get(item.getUserId())[1];
				item.setNewMessagesCount(unreadMessageCount);				
				item.setMessagesLastVisitToCurrentDt();

				item.setNewForumCount(unreadForumCount);
				item.setForumLastVisitToCurrentDt();

				if(item.getSiteTitle() == null || (item.getSiteTitle() != null && !item.getSiteTitle().equals(siteTitle))){
					updateSiteTitles = true;
				}
			}
		}
		for(String userId : newUsers){
			if(unreadCounts.containsKey(userId)){
				int unreadMessageCount = unreadCounts.get(userId)[0];
				int unreadForumCount = unreadCounts.get(userId)[1];
				SynopticMsgcntrItem synopticMsgcntrItem = createSynopticMsgcntrItem(userId, siteId, siteTitle);	
				synopticMsgcntrItem.setNewMessagesCount(unreadMessageCount);
				synopticMsgcntrItem.setNewForumCount(unreadForumCount);

				items.add(synopticMsgcntrItem);	
			}
		}
		saveSynopticMsgcntrItems(items);
		if(updateSiteTitles){
			updateAllSiteTitles(siteId, siteTitle);
		}
	}
	
	
	
	/**
	 * @return 
	 * 		DecoratedCompiledMessageStats for a single site
	 */
	public Map<String, DecoratedCompiledMessageStats> getSiteInfo(String siteId, List<String> userIds) {
		return getSiteContents(siteId, userIds);
	}
	
	public int findAllUnreadMessages(List aggregateList){
		int unreadCount = 0;
	    for (Iterator i = aggregateList.iterator(); i.hasNext();){
	      Object[] element = (Object[]) i.next();
	      /** filter on type and read status*/
	      if (Boolean.TRUE.equals(element[0])){
	        continue;
	      }
	      else{        
	        unreadCount += ((Integer) element[2]).intValue();
	      }      
	    }
	            
	    return unreadCount;    
	}
	
	public void resetAllUsersSynopticInfoInSite(String siteId){
		List<String> users = new ArrayList<String>();
		Site site;
		try {
			site = getSite(siteId);

			for (Iterator iterator = site.getMembers().iterator(); iterator.hasNext();) {
				Member member = (Member) iterator.next();
				String userId = member.getUserId();
				users.add(userId);
			}
			
			resetAllUsersSynopticInfoInSite(siteId, users);
			
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void resetAllUsersSynopticInfoInSite(String siteId, List<String> users){
		String NEW_MESSAGE_COUNT_FOR_ALL_USERS_SQL = "SELECT USER_ID, count(*) unread_messages " + 
														"FROM MFR_PVT_MSG_USR_T message " +									
														"where READ_STATUS = 0 and CONTEXT_ID = ? " +
														"Group By USER_ID";
		
		String RETURN_ALL_FORUMS_AND_TOPICS_SQL = "select forum.ID as FORUM_ID, topic.ID as TOPIC_ID, forum.DRAFT as isForumDraft, topic.DRAFT as isTopicDraft, topic.MODERATED as isTopicModerated, forum.LOCKED as isForumLocked, topic.LOCKED as isTopicLocked, forum.CREATED_BY as forumCreatedBy, topic.CREATED_BY as topicCreatedBy, forum.AVAILABILITY as forumAvailability, topic.AVAILABILITY as topicAvailability  " +
														"from MFR_AREA_T area, MFR_OPEN_FORUM_T forum, MFR_TOPIC_T topic " + 
														"Where area.ID = forum.surrogateKey and forum.ID = topic.of_surrogateKey " +
														"and area.CONTEXT_ID = ?";
		Connection clConnection = null;  	
		//Statement statement = null;
		PreparedStatement newMessageCountForAllUsers = null;
		PreparedStatement returnAllForumsAndTopics = null;
		ResultSet forumsAndTopicsRS = null;
		ResultSet newMessagesCountRS = null;
		try {
			Site site = getSite(siteId);
			
			
			clConnection = SqlService.borrowConnection();
						
			//setup prepared statements:
			newMessageCountForAllUsers = clConnection.prepareStatement(NEW_MESSAGE_COUNT_FOR_ALL_USERS_SQL);
			newMessageCountForAllUsers.setString(1, siteId);
			returnAllForumsAndTopics = clConnection.prepareStatement(RETURN_ALL_FORUMS_AND_TOPICS_SQL);
			returnAllForumsAndTopics.setString(1, siteId);
			
			forumsAndTopicsRS = returnAllForumsAndTopics.executeQuery();			
			HashMap<Long, DecoratedForumInfo> dfHM = getDecoratedForumsAndTopics(forumsAndTopicsRS);
			
			newMessagesCountRS = newMessageCountForAllUsers.executeQuery();
			HashMap<String, Integer> unreadMessagesHM = getUnreadMessagesHM(newMessagesCountRS);
						
			Map<String, DecoratedCompiledMessageStats> cdmsMap = getDMessageStats(users, siteId, site, dfHM, unreadMessagesHM);
			//creat a map of the unread counts:
			Map<String, Integer[]> unreadCounts = new HashMap<String, Integer[]>();
			for(Entry<String, DecoratedCompiledMessageStats> entry : cdmsMap.entrySet()){
				unreadCounts.put(entry.getKey(), new Integer[]{entry.getValue().getUnreadPrivateAmt(), entry.getValue().getUnreadForumsAmt()});
			}
			createOrUpdateSynopticToolInfo(users, siteId, site.getTitle(), unreadCounts);		
		} catch (IdUnusedException e) {
			LOG.error(e);
		} catch (SQLException e) {
			LOG.error(e);
		} finally{

			try {
				if(forumsAndTopicsRS != null)
					forumsAndTopicsRS.close();
			} catch (Exception e) {
				LOG.warn(e);
			}

			try {
				if(newMessagesCountRS != null)
					newMessagesCountRS.close();
			} catch (Exception e) {
				LOG.warn(e);
			}
			try {
				if(newMessageCountForAllUsers != null)
					newMessageCountForAllUsers.close();
			} catch (Exception e) {
				LOG.warn(e);
			}
			try {
				if(returnAllForumsAndTopics != null)
					returnAllForumsAndTopics.close();
			} catch (Exception e) {
				LOG.warn(e);
			}
			SqlService.returnConnection(clConnection);
		}
		
	}
	
	
	/**
	 * This method is used to get live information regarding the new message count per user for a forum ID
	 * 
	 * This information can be used to compare against a later current information set after an event takes place
	 * (ie. after saving a forum)  By only looking at the one forum that was updated, we can compare the two results
	 * and update the synoptic data based on the difference while avoiding calling unneeded forums, which can slow
	 * down this process significantly
	 * 
	 * @param siteId
	 * @param forumId
	 * @return
	 */
	public HashMap<String, Integer> getUserToNewMessagesForForumMap(String siteId, Long forumId, Long topicId){
		HashMap<String, Integer> returnHM = new HashMap<String, Integer>();
		String RETURN_ALL_TOPICS_FOR_FORUM_SQL = "select forum.ID as FORUM_ID, topic.ID as TOPIC_ID, forum.DRAFT as isForumDraft, topic.DRAFT as isTopicDraft, topic.MODERATED as isTopicModerated, forum.LOCKED as isForumLocked, topic.LOCKED as isTopicLocked, forum.CREATED_BY as forumCreatedBy, topic.CREATED_BY as topicCreatedBy, forum.AVAILABILITY as forumAvailability, topic.AVAILABILITY as topicAvailability  " +
													"from MFR_AREA_T area, MFR_OPEN_FORUM_T forum, MFR_TOPIC_T topic " + 
													"Where area.ID = forum.surrogateKey and forum.ID = topic.of_surrogateKey " +
													"and area.CONTEXT_ID = ? and forum.ID = ?";
		if(topicId != null){
			RETURN_ALL_TOPICS_FOR_FORUM_SQL = RETURN_ALL_TOPICS_FOR_FORUM_SQL + " and topic.ID = ?"; 
		}
		
		
		
		List<String> users = new ArrayList<String>();
		Site site;
		Connection clConnection = null;  	
		PreparedStatement returnAllTopicsForForum = null;
		ResultSet forumsAndTopicsRS = null;
		try {
			site = getSite(siteId);

			//get the list of users in the site:
			for (Iterator iterator = site.getMembers().iterator(); iterator.hasNext();) {
				Member member = (Member) iterator.next();
				String userId = member.getUserId();
				users.add(userId);
			}
			
			
			
			
			clConnection = SqlService.borrowConnection();
			returnAllTopicsForForum = clConnection.prepareStatement(RETURN_ALL_TOPICS_FOR_FORUM_SQL);
			returnAllTopicsForForum.setString(1, siteId);
			returnAllTopicsForForum.setString(2, "" +forumId);
			if(topicId != null)
				returnAllTopicsForForum.setString(3, "" +topicId);
			
			
			forumsAndTopicsRS = returnAllTopicsForForum.executeQuery();			
			HashMap<Long, DecoratedForumInfo> dfHM = getDecoratedForumsAndTopics(forumsAndTopicsRS);
			
			Map<String, DecoratedCompiledMessageStats> dcmsMap = getDMessageStats(users, siteId, site, dfHM, null);
			for (Iterator iterator = users.iterator(); iterator.hasNext();) {
        		String userId = (String) iterator.next();        		
        		//by passing a null, we can ignore all message tool calls (speeding up the process)
        		if(dcmsMap.containsKey(userId)){
        			returnHM.put(userId, Integer.valueOf(dcmsMap.get(userId).getUnreadForumsAmt()));
        		}
			}
						
		} catch (IdUnusedException e) {
			LOG.error(e);
		} catch (SQLException e) {
			LOG.error(e);
		} finally{
			try {
				if(forumsAndTopicsRS != null)
					forumsAndTopicsRS.close();
			} catch (Exception e) {
				LOG.warn(e);
			}
			try {
				if(returnAllTopicsForForum != null)
					returnAllTopicsForForum.close();
			} catch (Exception e) {
				LOG.warn(e);
			}
			
			SqlService.returnConnection(clConnection);
		}
		
		return returnHM;
	}
	
	public void updateSynopticMessagesForForumComparingOldMessagesCount(String siteId, Long forumId, Long topicId, HashMap<String, Integer> previousCountHM){
		if(previousCountHM != null){

			// get new count Hash Map for comparison
			HashMap<String, Integer> newCountHM = getUserToNewMessagesForForumMap(siteId, forumId, topicId);

			if(newCountHM != null){
				//loop through new count HM and compare:
				Set<String> users = newCountHM.keySet();


				int oldForumCount = 0;
				int newForumCount = 0;
				int forumCountDiff = 0;

				for (Iterator iterator = users.iterator(); iterator.hasNext();) {
					String userId = (String) iterator.next();

					oldForumCount = 0;
					newForumCount = 0;
					forumCountDiff = 0;

					Integer oldForumCountInt = previousCountHM.get(userId);
					if(oldForumCountInt != null){
						oldForumCount = oldForumCountInt.intValue();
					}	

					Integer newForumCountInt = newCountHM.get(userId);
					if(newForumCountInt != null){
						newForumCount = newForumCountInt.intValue();
					}

					forumCountDiff = newForumCount - oldForumCount;

					this.updateDifferenceForumSynopticInfoHelper(userId, siteId, forumCountDiff);
				}
			}
		}
	}
	
	
	private Map<String, DecoratedCompiledMessageStats> getDMessageStats(List<String> userIds, String siteId, Site site, HashMap<Long, DecoratedForumInfo> dfHM, Map<String, Integer> unreadMessagesHM){
		
		final Map<String, DecoratedCompiledMessageStats> dcms = new HashMap<String, DecoratedCompiledMessageStats>();

		// Check if tool within site
		// if so, get stats for just this site

		boolean isMessageForumsPageInSite = isMessageForumsPageInSite(site);
		
		//MSGCNTR-430 if this is the anon user we can't actually do anything more -DH
		if (userIds == null || userIds.size() == 0) {
			return dcms;
		}
		for(String user : userIds){
			DecoratedCompiledMessageStats dcm = new DecoratedCompiledMessageStats();
			dcm.setSiteName(site.getTitle());
			dcm.setSiteId(siteId);
			dcms.put(user, dcm);
		}
				
		if (isMessageForumsPageInSite || isMessagesPageInSite(site)) 
		{
			if(unreadMessagesHM != null){

				// Get private message area so we can get the private message forum so we can get the
				// List of topics so we can get the Received topic to finally determine number of unread messages
				// only check if Messages & Forums in site, just Messages is on by default
				boolean isEnabled;



				if (isMessageForumsPageInSite) {
					final Area area = pvtMessageManager.getPrivateMessageArea(siteId);
					isEnabled = area.getEnabled().booleanValue();
				}
				else {
					isEnabled = true;
				}

				if (isEnabled) {
					for(Entry<String, DecoratedCompiledMessageStats> entry : dcms.entrySet()){
						Integer newMessageCount = unreadMessagesHM.get(entry.getKey());
						if(newMessageCount != null){
							entry.getValue().setUnreadPrivateAmt(newMessageCount.intValue());
						}								
					}
				}
			}
		}

		if (isMessageForumsPageInSite || isForumsPageInSite(site)) 
		{
			Set<Long> dfKeySet = null;
			if(dfHM != null){
				Map<String, Boolean> overridingPermissionMap = new HashMap<String, Boolean>();
				for(String user : userIds){
					boolean hasOverridingPermission = SecurityService.isSuperUser(user) || getForumManager().isInstructor(user, "/site/" + siteId);
					overridingPermissionMap.put(user, hasOverridingPermission);
				}

				//site has forums added to the tool
				dfKeySet = dfHM.keySet();

				//need to check that the area hasn't been disabled:
				Area area = forumManager.getDiscussionForumArea(siteId);
				if(area != null){
					for (Iterator iterator = dfKeySet.iterator(); iterator.hasNext();) {
						Long dfId = (Long) iterator.next();

						DecoratedForumInfo dForum = dfHM.get(dfId);



						// Only count unread messages for forums the user can view:
						final Iterator<DecoratedTopicsInfo> topicIter = dForum.getTopics().iterator();

						while (topicIter.hasNext()) 
						{
							DecoratedTopicsInfo topic = (DecoratedTopicsInfo) topicIter.next();
							for(String userId : userIds){
								if(overridingPermissionMap.get(userId)
										|| (area.getAvailability() && ((dForum.getIsDraft().equals(Boolean.FALSE) && dForum.getAvailability()) ||
												forumManager.isForumOwner(dfId, dForum.getCreator(), userId, "/site/" + siteId)))){
									Long topicId = topic.getTopicId();
									Boolean isTopicDraft = topic.getIsDraft();
									Boolean isTopicModerated = topic.getIsModerated();
									Boolean isTopicLocked = topic.getIsLocked();
									String topicOwner = topic.getCreator();

									//Only count unread messages for topics the user can view:
									if ((isTopicDraft.equals(Boolean.FALSE) && topic.getAvailability())
											|| overridingPermissionMap.get(userId)
											||forumManager.isTopicOwner(topicId, topicOwner, userId, "/site/" + siteId)){ 

										if (getUiPermissionsManager().isRead(topicId, isTopicDraft, dForum.getIsDraft(), userId, siteId))
										{
											if (!isTopicModerated.booleanValue() || (isTopicModerated.booleanValue() && 
													getUiPermissionsManager().isModeratePostings(topicId, dForum.getIsLocked(), dForum.getIsDraft(), isTopicLocked, isTopicDraft, userId, siteId)))
											{
												//TODO: to improve this performance, try using the logic in MessageForumStatisticsBean.getTopicStatistics
												//essentially, get the number of possible messages for that user and subtract the number read.  See commit for SAK-27810 for more details.
												dcms.get(userId).setUnreadForumsAmt(dcms.get(userId).getUnreadForumsAmt() + getMessageManager().findUnreadMessageCountByTopicIdByUserId(topicId, userId));
											}
											else
											{	
												// b/c topic is moderated and user does not have mod perm, user may only
												// see approved msgs or pending/denied msgs authored by user
												dcms.get(userId).setUnreadForumsAmt(dcms.get(userId).getUnreadForumsAmt() + getMessageManager().findUnreadViewableMessageCountByTopicIdByUserId(topicId, userId));
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}


		return dcms;
	}
	
	
	/**
	 * Returns List to populate page if on Home page of a site
	 * 
	 * @return
	 * 		List of DecoratedCompiledMessageStats for a particular site
	 */
	private Map<String, DecoratedCompiledMessageStats> getSiteContents(String siteId, List<String> userIds) 
	{
		String NEW_MESSAGE_COUNT_SQL = "SELECT USER_ID, count(*) unread_messages " +
										"FROM MFR_PVT_MSG_USR_T message " +									
										"where READ_STATUS = 0 and USER_ID in (?) and CONTEXT_ID = ? " +
										"Group By USER_ID";
		
		String RETURN_ALL_FORUMS_AND_TOPICS_SQL = "select forum.ID as FORUM_ID, topic.ID as TOPIC_ID, forum.DRAFT as isForumDraft, topic.DRAFT as isTopicDraft, topic.MODERATED as isTopicModerated, forum.LOCKED as isForumLocked, topic.LOCKED as isTopicLocked, forum.CREATED_BY as forumCreatedBy, topic.CREATED_BY as topicCreatedBy, forum.AVAILABILITY as forumAvailability, topic.AVAILABILITY as topicAvailability  " +
													"from MFR_AREA_T area, MFR_OPEN_FORUM_T forum, MFR_TOPIC_T topic " + 
													"Where area.ID = forum.surrogateKey and forum.ID = topic.of_surrogateKey " +
													"and area.CONTEXT_ID = ?";
		
		Connection clConnection = null;  	
		PreparedStatement returnAllForumsAndTopics = null;
		ResultSet forumsAndTopicsRS = null;
		
		Map<String, DecoratedCompiledMessageStats> stats = new HashMap<String, DecoratedCompiledMessageStats>();
		Map<String, Integer> unreadMessagesHM = new HashMap<String, Integer>();
		try{
			clConnection = SqlService.borrowConnection();
			
			//First create the messages map:
			String[] userIdsArr = userIds.toArray(new String[]{});
			int subArrayIndex = 0;
			do{
				int subArraySize = ORACLE_IN_CLAUSE_SIZE_LIMIT;
				if(subArrayIndex + subArraySize > userIdsArr.length){
					subArraySize = (userIdsArr.length - subArrayIndex);
				}
				String[] subUsers = Arrays.copyOfRange(userIdsArr, subArrayIndex, subArrayIndex + subArraySize);
				String query = NEW_MESSAGE_COUNT_SQL;
				String inParams = "(";
				for(int i = 0; i < subUsers.length; i++){
					inParams += "'" + subUsers[i].replace("'", "''") + "'"; //escape apostrophe
					if(i < subUsers.length - 1){
						inParams += ",";
					}
				}
				inParams += ")";
				query = query.replace("(?)", inParams);
				PreparedStatement newMessageCount = null;
				ResultSet newMessagesCountRS = null;
				try {
					newMessageCount = clConnection.prepareStatement(query);
					newMessageCount.setString(1, siteId);
					newMessagesCountRS = newMessageCount.executeQuery();
					unreadMessagesHM.putAll(getUnreadMessagesHM(newMessagesCountRS));
				}catch(Exception e){
					LOG.error(e.getMessage(), e);
				}finally{
					if(newMessageCount != null){
						try {
							newMessageCount.close();
						} catch (SQLException e) {
							LOG.error(e.getMessage(), e);
						}
					}
					try {
						if(newMessagesCountRS != null)
							newMessagesCountRS.close();
					} catch (Exception e) {
						LOG.warn(e.getMessage(), e);
					}
					
				}		

				subArrayIndex = subArrayIndex + subArraySize;
			}while(subArrayIndex < userIdsArr.length);

			//now get the forum data:
			returnAllForumsAndTopics = clConnection.prepareStatement(RETURN_ALL_FORUMS_AND_TOPICS_SQL);			
			returnAllForumsAndTopics.setString(1, siteId);			
			forumsAndTopicsRS = returnAllForumsAndTopics.executeQuery();			
			HashMap<Long, DecoratedForumInfo> dfHM = getDecoratedForumsAndTopics(forumsAndTopicsRS);
			
			Site site = getSite(siteId);
			
			//now we have the data, update each user:
			stats.putAll(getDMessageStats(userIds, siteId, site, dfHM, unreadMessagesHM));
			
		}catch(IdUnusedException e) {
			LOG.error("IdUnusedException while trying to check if site has MF tool.");
		} catch (SQLException e) {
			LOG.error(e);
		} finally{
			
			try {
				if(forumsAndTopicsRS != null)
					forumsAndTopicsRS.close();
			} catch (Exception e) {
				LOG.warn(e);
			}

			try {
				if(returnAllForumsAndTopics != null)
					returnAllForumsAndTopics.close();
			} catch (Exception e) {
				LOG.warn(e);
			}	
			
			SqlService.returnConnection(clConnection);
		}
		
		return stats;
	}
	
	
	private HashMap<Long, DecoratedForumInfo> getDecoratedForumsAndTopics(ResultSet rs){
		HashMap<Long, DecoratedForumInfo> returnHM = new HashMap<Long, DecoratedForumInfo>();

		try {
			String FORUM_CREATED_BY, TOPIC_CREATED_BY;
			Long FORUM_ID, TOPIC_ID;
			Boolean IS_TOPIC_DRAFT, IS_FORUM_DRAFT, IS_TOPIC_MODERATED, IS_FORUM_LOCKED, IS_TOPIC_LOCKED, FORUM_AVAILABILITY, TOPIC_AVAILABILITY;

			while(rs.next()){
				FORUM_ID = rs.getLong("FORUM_ID");
				TOPIC_ID = rs.getLong("TOPIC_ID");
				IS_TOPIC_DRAFT = rs.getBoolean("isTopicDraft");
				IS_FORUM_DRAFT = rs.getBoolean("isForumDraft");
				IS_TOPIC_MODERATED = rs.getBoolean("isTopicModerated");
				IS_FORUM_LOCKED = rs.getBoolean("isForumLocked");
				IS_TOPIC_LOCKED = rs.getBoolean("isTopicLocked");
				FORUM_CREATED_BY = rs.getString("forumCreatedBy");
				TOPIC_CREATED_BY = rs.getString("topicCreatedBy");
				FORUM_AVAILABILITY = rs.getBoolean("forumAvailability");
				TOPIC_AVAILABILITY = rs.getBoolean("topicAvailability");


				//hashmap already has this site id, now look for forum id:
				if(returnHM.containsKey(FORUM_ID)){						
					DecoratedTopicsInfo dTopic = new DecoratedTopicsInfo(TOPIC_ID, IS_TOPIC_LOCKED, IS_TOPIC_DRAFT, TOPIC_AVAILABILITY, IS_TOPIC_MODERATED, TOPIC_CREATED_BY);
					returnHM.get(FORUM_ID).addTopic(dTopic);
				}else{
					//this is a new forum, so add it to the list						
					DecoratedTopicsInfo dTopic = new DecoratedTopicsInfo(TOPIC_ID, IS_TOPIC_LOCKED, IS_TOPIC_DRAFT, TOPIC_AVAILABILITY, IS_TOPIC_MODERATED, TOPIC_CREATED_BY);
					DecoratedForumInfo dForum = new DecoratedForumInfo(FORUM_ID, IS_FORUM_LOCKED, IS_FORUM_DRAFT, FORUM_AVAILABILITY, FORUM_CREATED_BY);
					dForum.addTopic(dTopic);

					returnHM.put(FORUM_ID, dForum);
				}												
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnHM;
	}
	
	public HashMap<String, Integer> getUnreadMessagesHM(ResultSet rs){
		HashMap<String, Integer> returnHM = new HashMap<String, Integer>();
		
		if(rs != null){
			try{
				String userId;
				Integer messageCount;
			while(rs.next()){
				userId = rs.getString("USER_ID");
				messageCount = Integer.valueOf(rs.getInt("unread_messages"));

				returnHM.put(userId, messageCount);		
			}
			}catch(Exception e){
				LOG.error(e);
			}
		}
		
		return returnHM;
	}

	/**
	 * @return TRUE if Messages & Forums (Message Center) exists in this site,
	 *         FALSE otherwise
	 */
	private boolean isMessageForumsPageInSite(Site thisSite) {
		if (mfPageInSiteMap == null) {
			mfPageInSiteMap = new HashMap();
		}
		
		Boolean isMFPageInSite;
		if ((isMFPageInSite = (Boolean) mfPageInSiteMap.get(thisSite)) == null) {
			isMFPageInSite = isToolInSite(thisSite, DiscussionForumService.MESSAGE_CENTER_ID);
			mfPageInSiteMap.put(thisSite, isMFPageInSite);
		}
		
		return isMFPageInSite;
	}
	
	/**
	 * Return TRUE if tool with id passed in exists in site passed in
	 * FALSE otherwise.
	 * 
	 * @param thisSite
	 * 			Site object to check
	 * @param toolId
	 * 			Tool id to be checked
	 * 
	 * @return
	 */
	private boolean isToolInSite(Site thisSite, String toolId) {
		final Collection toolsInSite = thisSite.getTools(toolId);

		return ! toolsInSite.isEmpty();		
	}

	
	/**
	 * Returns the Site object for this id, if it exists.
	 * If not, returns IdUnusedException
	 * 
	 * @param siteId
	 * 			The site id to check
	 * 
	 * @return
	 * 			Site object for this id
	 */
	private Site getSite(String siteId) 
	throws IdUnusedException {
		if (sitesMap == null) {
			sitesMap = new HashMap();
		}
	
		if (sitesMap.get(siteId) == null) {
			Site site = SiteService.getSite(siteId);
			sitesMap.put(site.getId(), site);
			return site;
		}
		else {
			return (Site) sitesMap.get(siteId);
		}
	}

	
	/**
	 * @return TRUE if Messages tool exists in this site,
	 *         FALSE otherwise
	 */
	private boolean isMessagesPageInSite(Site thisSite) {
		return isToolInSite(thisSite, DiscussionForumService.MESSAGES_TOOL_ID);
	}
	
	/**
	 * @return TRUE if Forums tool exists in this site,
	 *         FALSE otherwise
	 */
	private boolean isForumsPageInSite(Site thisSite) {
		return isToolInSite(thisSite, DiscussionForumService.FORUMS_TOOL_ID);
	}
	
	
	public MessageForumsMessageManager getMessageManager() {
		return messageManager;
	}

	public void setMessageManager(MessageForumsMessageManager messageManager) {
		this.messageManager = messageManager;
	}
	
	public UIPermissionsManager getUiPermissionsManager() {
		return uiPermissionsManager;
	}

	public void setUiPermissionsManager(UIPermissionsManager uiPermissionsManager) {
		this.uiPermissionsManager = uiPermissionsManager;
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
	
	/**
	 * Used to store synoptic information for a users unread messages.
	 * Whether on the Home page of a site or in MyWorkspace determines
	 * what properties are filled.
	 * <p>
	 * If in MyWorkspace, each object contains the number of unread
	 * Private Messages and number of unread Discussion Forum messages.
	 * </p>
	 * <p>
	 * If in the Home page of a site, each object contains either the
	 * number of unread Private Messages or number of unread Discussion 
	 * Forum messages.</p>
	 * 
	 * @author josephrodriguez
	 *
	 */
	public class DecoratedCompiledMessageStats {
		private String siteName;
		private String siteId;
		
		/** MyWorkspace information */
		private int unreadPrivateAmt = 0;
		private int unreadForumsAmt = 0;
		private String mcPageURL;
		private String privateMessagesUrl;
		private boolean messagesandForums;
		private boolean messages;
		private boolean forums;
		
		public String getSiteName() {
			return siteName;
		}

		public void setSiteName(String siteName) {
			this.siteName = siteName;
		}

		public int getUnreadPrivateAmt() {
			return unreadPrivateAmt;
		}

		public void setUnreadPrivateAmt(int unreadPrivateAmt) {
			this.unreadPrivateAmt = unreadPrivateAmt;
		}

		public int getUnreadForumsAmt() {
			return unreadForumsAmt;
		}

		public void setUnreadForumsAmt(int unreadForumsAmt) {
			this.unreadForumsAmt = unreadForumsAmt;
		}

		public String getMcPageURL() {
			return mcPageURL;
		}

		public void setMcPageURL(String mcPageURL) {
			this.mcPageURL = mcPageURL;
		}

		public String getSiteId() {
			return siteId;
		}

		public void setSiteId(String siteId) {
			this.siteId = siteId;
		}

		public String getPrivateMessagesUrl() {
			return privateMessagesUrl;
		}

		public void setPrivateMessagesUrl(String privateMessagesUrl) {
			this.privateMessagesUrl = privateMessagesUrl;
		}

		public boolean isMessagesandForums() {
			return messagesandForums;
		}

		public void setMessagesandForums(boolean messagesandForums) {
			this.messagesandForums = messagesandForums;
		}

		public boolean isMessages() {
			return messages;
		}

		public void setMessages(boolean messages) {
			this.messages = messages;
		}

		public boolean isForums() {
			return forums;
		}

		public void setForums(boolean forums) {
			this.forums = forums;
		}
	}
	
public class DecoratedForumInfo{
		
		private Long forumId;
		private Boolean isLocked, isDraft, availability;
		private String creator;
		private ArrayList<DecoratedTopicsInfo> topics = new ArrayList<DecoratedTopicsInfo>();
		
		public DecoratedForumInfo(Long forumId, Boolean isLocked, Boolean isDraft, Boolean availability, String creator){
			this.forumId = forumId;
			this.isLocked = isLocked;
			this.isDraft = isDraft;
			this.creator = creator;
			this.availability = availability;
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

		public Boolean getAvailability() {
			return availability;
		}

		public void setAvailability(Boolean availability) {
			this.availability = availability;
		}
	}

	public class DecoratedTopicsInfo{
		
		private Long topicId;
		private Boolean isLocked, isDraft, isModerated, availability;
		private String creator;
		
		public DecoratedTopicsInfo(Long topicId, Boolean isLocked, Boolean isDraft, Boolean availability, Boolean isModerated, String creator){
			this.topicId = topicId;
			this.isLocked = isLocked;
			this.isDraft = isDraft;
			this.isModerated = isModerated;
			this.creator = creator;
			this.availability = availability;
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

		public Boolean getAvailability() {
			return availability;
		}

		public void setAvailability(Boolean availability) {
			this.availability = availability;
		}
	}

	public void updateAllSiteTitles(final String siteId, final String siteTitle) {
		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query q = session.getNamedQuery(QUERY_UPDATE_ALL_SITE_TITLES);
				q.setParameter("siteTitle", siteTitle, Hibernate.STRING);
				q.setParameter("siteId", siteId, Hibernate.STRING);
				return q.executeUpdate();
			}
		};

		getHibernateTemplate().execute(hcb);
	}
}
