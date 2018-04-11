/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/MessageForumsForumManagerImpl.java $
 * $Id: MessageForumsForumManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.component.app.messageforums;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.hibernate.collection.internal.PersistentSet;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import org.sakaiproject.api.app.messageforums.ActorPermissions;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.MessageForumsUser;
import org.sakaiproject.api.app.messageforums.OpenForum;
import org.sakaiproject.api.app.messageforums.OpenTopic;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateTopic;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.cover.ForumScheduleNotificationCover;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.messageforums.dao.hibernate.ActorPermissionsImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DiscussionForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DiscussionTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageForumsUserImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.OpenTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.Util;
import org.sakaiproject.component.app.messageforums.dao.hibernate.util.comparator.ForumBySortIndexAscAndCreatedDateDesc;
import org.sakaiproject.component.app.messageforums.dao.hibernate.util.comparator.TopicBySortIndexAscAndCreatedDateDesc;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;

/**
 * The forums are sorted by this java class.  The topics are sorted by the order-by in the hbm file.
 *
 */
@Slf4j
public class MessageForumsForumManagerImpl extends HibernateDaoSupport implements MessageForumsForumManager {

    private static final String QUERY_FOR_PRIVATE_TOPICS = "findPrivateTopicsByForumId";

    private static final String QUERY_RECEIVED_UUID_BY_CONTEXT_ID = "findReceivedUuidByContextId";
    
    private static final String QUERY_BY_FORUM_OWNER = "findPrivateForumByOwner";
    
    private static final String QUERY_BY_FORUM_OWNER_AREA = "findPrivateForumByOwnerArea";

    private static final String QUERY_BY_FORUM_OWNER_AREA_NULL = "findPrivateForumByOwnerAreaNull";

    private static final String QUERY_BY_FORUM_ID = "findForumById";
    
    private static final String QUERY_BY_FORUM_ID_WITH_ATTACHMENTS = "findForumByIdWithAttachments";    

    private static final String QUERY_BY_FORUM_UUID = "findForumByUuid";
    
    private static final String QUERY_BY_TYPE_AND_CONTEXT = "findForumByTypeAndContext";
    private static final String QUERY_BY_FORUM_ID_AND_TOPICS = "findForumByIdWithTopics";
    private static final String QUERY_BY_TYPE_AND_CONTEXT_WITH_ALL_INFO = "findForumByTypeAndContextWithAllInfo";
    private static final String QUERY_BY_TYPE_AND_CONTEXT_WITH_ALL_TOPICS_MEMBERSHIP = "findForumByTypeAndContextWithTopicsMemberhips";

           
    private static final String QUERY_TOPIC_WITH_MESSAGES_AND_ATTACHMENTS = "findTopicByIdWithMessagesAndAttachments";        
    private static final String QUERY_TOPIC_WITH_MESSAGES = "findTopicByIdWithMessages";  
    private static final String QUERY_TOPIC_WITH_ATTACHMENTS = "findTopicWithAttachmentsById"; 
        
    private static final String QUERY_TOPICS_WITH_MESSAGES_FOR_FORUM = "findTopicsWithMessagesForForum";
    private static final String QUERY_TOPICS_WITH_MESSAGES_AND_ATTACHMENTS_FOR_FORUM = "findTopicsWithMessagesAndAttachmentsForForum";
    private static final String QUERY_TOPICS_WITH_MSGS_AND_ATTACHMENTS_AND_MEMBERSHIPS_FOR_FORUM = "findTopicsWithMessagesMembershipAndAttachmentsForForum";

    private static final String QUERY_FORUMS_FOR_MAIN_PAGE = "findForumsForMainPage";
            
    private static final String QUERY_BY_TOPIC_ID = "findTopicById";
    private static final String QUERY_OPEN_BY_TOPIC_AND_PARENT = "findOpenTopicAndParentById";
    private static final String QUERY_PRIVATE_BY_TOPIC_AND_PARENT = "findPrivateTopicAndParentById";    

    private static final String QUERY_BY_TOPIC_UUID = "findTopicByUuid";

    private static final String QUERY_OF_SUR_KEY_BY_TOPIC = "findOFTopicSurKeyByTopicId";
    private static final String QUERY_PF_SUR_KEY_BY_TOPIC = "findPFTopicSurKeyByTopicId";

    private static final String QUERY_BY_TOPIC_ID_MESSAGES_ATTACHMENTS = "findTopicByIdWithAttachments";
    
    private static final String QUERY_GET_ALL_MOD_TOPICS_IN_SITE = "findAllModeratedTopicsForSite";
    private static final String QUERY_GET_NUM_MOD_TOPICS_WITH_MOD_PERM_BY_PERM_LEVEL = "findNumModeratedTopicsForSiteByUserByMembershipWithPermissionLevelId";
    private static final String QUERY_GET_NUM_MOD_TOPICS_WITH_MOD_PERM_BY_PERM_LEVEL_NAME = "findNumModeratedTopicsForSiteByUserByMembershipWithPermissionLevelName";
    
    private static final String QUERY_GET_FORUM_BY_ID_WITH_TOPICS_AND_ATT_AND_MSGS = "findForumByIdWithTopicsAndAttachmentsAndMessages";
    
    //public static Comparator FORUM_CREATED_DATE_COMPARATOR;
    
    /** Sorts the forums by the sort index and if the same index then order by the creation date */
    public static final Comparator FORUM_SORT_INDEX_CREATED_DATE_COMPARATOR_DESC = new ForumBySortIndexAscAndCreatedDateDesc();

    private IdManager idManager;

    private SessionManager sessionManager;

    private ServerConfigurationService serverConfigurationService;
    private Boolean DEFAULT_AUTO_MARK_READ = false; 

    private MessageForumsTypeManager typeManager;

    public MessageForumsForumManagerImpl() {}

    private EventTrackingService eventTrackingService;
    
    private SiteService siteService;
    private ToolManager toolManager;
    
    public void init() {
       log.info("init()");
       DEFAULT_AUTO_MARK_READ = serverConfigurationService.getBoolean("msgcntr.forums.default.auto.mark.threads.read", false);
    }

    public EventTrackingService getEventTrackingService() {
        return eventTrackingService;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public MessageForumsTypeManager getTypeManager() {
        return typeManager;
    }

    public void setTypeManager(MessageForumsTypeManager typeManager) {
        this.typeManager = typeManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setIdManager(IdManager idManager) {
        this.idManager = idManager;
    }

    public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public IdManager getIdManager() {
        return idManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
    
    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void initializeTopicsForForum(BaseForum forum){
      
      getHibernateTemplate().initialize(forum);
      getHibernateTemplate().initialize(forum.getTopicsSet());
    }
    
    public List getTopicsByIdWithMessages(final Long forumId){
      if (forumId == null) {
        throw new IllegalArgumentException("Null Argument");
      }   
      
      HibernateCallback<List> hcb = session -> {
          Query q = session.getNamedQuery(QUERY_TOPICS_WITH_MESSAGES_FOR_FORUM);
          q.setLong("id", forumId);
          return q.list();
      };

    Topic tempTopic = null;
    Set resultSet = new HashSet();      
    List temp = (ArrayList) getHibernateTemplate().execute(hcb);
    for (Iterator i = temp.iterator(); i.hasNext();)
    {
      Object[] results = (Object[]) i.next();        
          
      if (results != null) {
        if (results[0] instanceof Topic) {
          tempTopic = (Topic)results[0];
          tempTopic.setBaseForum((BaseForum)results[1]);            
        } else {
          tempTopic = (Topic)results[1];
          tempTopic.setBaseForum((BaseForum)results[0]);
        }
        resultSet.add(tempTopic);
      }
    }
    return Util.setToList(resultSet);
  }
    
    public List getTopicsByIdWithMessagesAndAttachments(final Long forumId){
      if (forumId == null) {
        throw new IllegalArgumentException("Null Argument");
      }   
      
      HibernateCallback<List> hcb = session -> {
          Query q = session.getNamedQuery(QUERY_TOPICS_WITH_MESSAGES_AND_ATTACHMENTS_FOR_FORUM);
          q.setLong("id", forumId);
          return q.list();
      };

    Topic tempTopic = null;
    Set resultSet = new HashSet();      
    List temp = (ArrayList) getHibernateTemplate().execute(hcb);
    for (Iterator i = temp.iterator(); i.hasNext();)
    {
      Object[] results = (Object[]) i.next();        
          
      if (results != null) {
        if (results[0] instanceof Topic) {
          tempTopic = (Topic)results[0];
          tempTopic.setBaseForum((BaseForum)results[1]);            
        } else {
          tempTopic = (Topic)results[1];
          tempTopic.setBaseForum((BaseForum)results[0]);
        }
        resultSet.add(tempTopic);
      }
    }
    return Util.setToList(resultSet);    
  }
  
  public List getTopicsByIdWithMessagesMembershipAndAttachments(final Long forumId) {
      if (forumId == null) {
        throw new IllegalArgumentException("Null Argument");
      }   
      
      HibernateCallback<List> hcb = session -> {
          Query q = session.getNamedQuery(QUERY_TOPICS_WITH_MSGS_AND_ATTACHMENTS_AND_MEMBERSHIPS_FOR_FORUM);
          q.setLong("id", forumId);
          return q.list();
      };

    Topic tempTopic = null;
    SortedSet resultSet = new TreeSet(new TopicBySortIndexAscAndCreatedDateDesc());
    List temp = (ArrayList) getHibernateTemplate().execute(hcb);
    for (Iterator i = temp.iterator(); i.hasNext();)
    {
      Object[] results = (Object[]) i.next();        
          
      if (results != null) {
        if (results[0] instanceof Topic) {
          tempTopic = (Topic)results[0];
          tempTopic.setBaseForum((BaseForum)results[1]);            
        } else {
          tempTopic = (Topic)results[1];
          tempTopic.setBaseForum((BaseForum)results[0]);
        }
        resultSet.add(tempTopic);
      }
    }
    return Util.setToList(resultSet);    
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.MessageForumsForumManager#getForumsForMainPage()
   */
  public List<DiscussionForum> getForumsForMainPage() {
    HibernateCallback<List> hcb = session -> {
        Query q = session.getNamedQuery(QUERY_FORUMS_FOR_MAIN_PAGE);
        q.setString("typeUuid", typeManager.getDiscussionForumType());
        q.setString("contextId", getContextId());
        return q.list();
    };
    List returnList = new ArrayList();
    returnList.addAll(new HashSet(getHibernateTemplate().execute(hcb)));
    return returnList;
  }
      
  public List getReceivedUuidByContextId(final List siteList) {
      if (siteList == null) {
          throw new IllegalArgumentException("Null Argument");
      }      

     HibernateCallback<List> hcb = session -> {
         Query q = session.getNamedQuery(QUERY_RECEIVED_UUID_BY_CONTEXT_ID);
         q.setParameterList("siteList", siteList);
         q.setString("userId", getCurrentUser());
         return q.list();
     };

      return getHibernateTemplate().execute(hcb);
	  
  }

    public Topic getTopicByIdWithMessagesAndAttachments(final Long topicId) {

      if (topicId == null) {
          throw new IllegalArgumentException("Null Argument");
      }      

     HibernateCallback<Topic> hcb = session -> {
         Query q = session.getNamedQuery(QUERY_TOPIC_WITH_MESSAGES_AND_ATTACHMENTS);
         q.setLong("id", topicId);
         return (Topic) q.uniqueResult();
     };

      return getHibernateTemplate().execute(hcb);
    }
    
    public Topic getTopicByIdWithMessages(final Long topicId) {

      if (topicId == null) {
          throw new IllegalArgumentException("Null Argument");
      }      

     HibernateCallback<Topic> hcb = session -> {
         Query q = session.getNamedQuery(QUERY_TOPIC_WITH_MESSAGES);
         q.setLong("id", topicId);
         return (Topic) q.uniqueResult();
     };
      
      return getHibernateTemplate().execute(hcb);
    }
    
    public Topic getTopicWithAttachmentsById(final Long topicId) {

        if (topicId == null) {
            throw new IllegalArgumentException("Null Argument");
        }      

       HibernateCallback<Topic> hcb = session -> {
           Query q = session.getNamedQuery(QUERY_TOPIC_WITH_ATTACHMENTS);
           q.setLong("id", topicId);
           return (Topic) q.uniqueResult();
       };
        
        return getHibernateTemplate().execute(hcb);
      }

    
    public List<Attachment> getTopicAttachments(final Long topicId) {
    	if (topicId == null) {
    		throw new IllegalArgumentException("Null Argument topicId");
    	}
    	HibernateCallback<List<Attachment>> hcb = session -> {
            Query q = session.getNamedQuery("findTopicAttachments");
            q.setCacheable(true);
            q.setLong("topic", topicId);
            return q.list();
        };
    	return getHibernateTemplate().execute(hcb);
    } 

    
    public BaseForum getForumByIdWithTopics(final Long forumId) {

      if (forumId == null) {
          throw new IllegalArgumentException("Null Argument");
      }      

     HibernateCallback<BaseForum> hcb = session -> {
         Query q = session.getNamedQuery(QUERY_BY_FORUM_ID_AND_TOPICS);
         q.setLong("id", forumId);
         return (BaseForum) q.uniqueResult();
     };
      
      BaseForum bForum = getHibernateTemplate().execute(hcb);
      
      if (bForum != null){
        getHibernateTemplate().initialize(bForum.getAttachmentsSet());
      }
      
      return bForum;      
    }
    
    public List getForumByTypeAndContext(final String typeUuid) {

      if (typeUuid == null) {
          throw new IllegalArgumentException("Null Argument");
      }      

     HibernateCallback<List> hcb = session -> {
         Query q = session.getNamedQuery(QUERY_BY_TYPE_AND_CONTEXT);
         q.setString("typeUuid", typeUuid);
         q.setString("contextId", getContextId());
         return q.list();
     };

      BaseForum tempForum = null;
      Set resultSet = new HashSet();
      List temp = getHibernateTemplate().execute(hcb);
            
      for (Iterator i = temp.iterator(); i.hasNext();)
      {
        Object[] results = (Object[]) i.next();        
            
        if (results != null) {
          if (results[0] instanceof BaseForum) {
            tempForum = (BaseForum)results[0];
            tempForum.setArea((Area)results[1]);            
          } else {
            tempForum = (BaseForum)results[1];
            tempForum.setArea((Area)results[0]);
          }
          resultSet.add(tempForum);
        }
      }
      
      List resultList = Util.setToList(resultSet);
      Collections.sort(resultList, FORUM_SORT_INDEX_CREATED_DATE_COMPARATOR_DESC);
      
      // Now that the list is sorted, lets index the forums
      int sort_index = 1;
      for(Iterator i = resultList.iterator(); i.hasNext(); ) {
         tempForum = (BaseForum)i.next();
         
         tempForum.setSortIndex(Integer.valueOf(sort_index++));
      }
      
      return resultList;      
    }
    
    public List getForumByTypeAndContext(final String typeUuid, final String contextId) {

        if (typeUuid == null || contextId == null) {
            throw new IllegalArgumentException("Null Argument");
        }      

       HibernateCallback<List> hcb = session -> {
           Query q = session.getNamedQuery(QUERY_BY_TYPE_AND_CONTEXT);
           q.setString("typeUuid", typeUuid);
           q.setString("contextId", contextId);
           return q.list();
       };

        BaseForum tempForum = null;
        Set resultSet = new HashSet();
        List temp = getHibernateTemplate().execute(hcb);
              
        for (Iterator i = temp.iterator(); i.hasNext();)
        {
          Object[] results = (Object[]) i.next();        
              
          if (results != null) {
            if (results[0] instanceof BaseForum) {
              tempForum = (BaseForum)results[0];
              tempForum.setArea((Area)results[1]);            
            } else {
              tempForum = (BaseForum)results[1];
              tempForum.setArea((Area)results[0]);
            }
            resultSet.add(tempForum);
          }
        }
        
        List resultList = Util.setToList(resultSet);
        Collections.sort(resultList, FORUM_SORT_INDEX_CREATED_DATE_COMPARATOR_DESC);

        // Now that the list is sorted, lets index the forums
        int sort_index = 1;
        for(Iterator i = resultList.iterator(); i.hasNext(); ) {
           tempForum = (BaseForum)i.next();
           
           tempForum.setSortIndex(Integer.valueOf(sort_index++));
        }
        
        return resultList;      
      }

    public Topic getTopicByIdWithAttachments(final Long topicId) {

        if (topicId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getTopicByIdWithMessagesAndAttachments executing with topicId: " + topicId);

        HibernateCallback<Topic> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_BY_TOPIC_ID_MESSAGES_ATTACHMENTS);
            q.setLong("id", topicId);
            return (Topic) q.uniqueResult();
        };

        return getHibernateTemplate().execute(hcb);

    }

    public PrivateForum getPrivateForumByOwner(final String owner) {

        if (owner == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getForumByOwner executing with owner: " + owner);

        HibernateCallback<PrivateForum> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_BY_FORUM_OWNER);
            q.setString("owner", owner);
            return (PrivateForum) q.uniqueResult();
        };

        return getHibernateTemplate().execute(hcb);
    }

    
    public PrivateForum getPrivateForumByOwnerArea(final String owner, final Area area) {

      if (owner == null || area == null) {
          throw new IllegalArgumentException("Null Argument");
      }

      log.debug("getForumByOwnerArea executing with owner: " + owner + " and area:" + area);

      HibernateCallback<PrivateForum> hcb = session -> {
          Query q = session.getNamedQuery(QUERY_BY_FORUM_OWNER_AREA);
          q.setString("owner", owner);
          q.setParameter("area", area);
          return (PrivateForum) q.uniqueResult();
      };

      return getHibernateTemplate().execute(hcb);
    }

    public PrivateForum getPrivateForumByOwnerAreaNull(final String owner) {

      if (owner == null) {
          throw new IllegalArgumentException("Null Argument");
      }

      log.debug("getForumByOwnerAreaNull executing with owner: " + owner);

      HibernateCallback<PrivateForum> hcb = session -> {
          Query q = session.getNamedQuery(QUERY_BY_FORUM_OWNER_AREA_NULL);
          q.setString("owner", owner);
          return (PrivateForum) q.uniqueResult();
      };

      return getHibernateTemplate().execute(hcb);
    }
    
    public BaseForum getForumByIdWithAttachments(final Long forumId) {
      
      if (forumId == null) {
          throw new IllegalArgumentException("Null Argument");
      }

      log.debug("getForumByIdWithAttachments executing with forumId: " + forumId);
                  
      HibernateCallback<BaseForum> hcb = session -> {
          Query q = session.getNamedQuery(QUERY_BY_FORUM_ID_WITH_ATTACHMENTS);
          q.setLong("id", forumId);
          return (BaseForum) q.uniqueResult();
      };

      return getHibernateTemplate().execute(hcb);

    }


    public BaseForum getForumById(boolean open, final Long forumId) {
        if (forumId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getForumById executing with forumId: " + forumId);

        if (open) {
            // open works for both open and discussion forums
            return getForumByIdWithAttachments(forumId);
        } else {
            return (BaseForum) getHibernateTemplate().get(PrivateForumImpl.class, forumId);
        }
    }

    public BaseForum getForumByUuid(final String forumId) {
        if (forumId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getForumByUuid executing with forumId: " + forumId);

        HibernateCallback<BaseForum> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_BY_FORUM_UUID);
            q.setString("uuid", forumId);
            return (BaseForum) q.uniqueResult();
        };

        return getHibernateTemplate().execute(hcb);
    }

    public Topic getTopicById(final boolean open, final Long topicId) {
        if (topicId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getTopicById executing with topicId: " + topicId);

        HibernateCallback<List> hcb = session -> {
            String query;
            if (open) {
                query = QUERY_OPEN_BY_TOPIC_AND_PARENT;
            } else {
                query = QUERY_PRIVATE_BY_TOPIC_AND_PARENT;
            }
            Query q = session.getNamedQuery(QUERY_OPEN_BY_TOPIC_AND_PARENT);
            q.setLong("id", topicId);
            return q.list();
        };

        Topic res = null;
		try {
			List temp = getHibernateTemplate().execute(hcb);
			if (temp != null && temp.size() > 0) {

				Object[] results = (Object[]) temp.get(0);
				if (results != null && results.length > 1) {
					if (results[0] instanceof Topic) {
						res = (Topic) results[0];
						res.setBaseForum((BaseForum) results[1]);
					} else {
						res = (Topic) results[1];
						res.setBaseForum((BaseForum) results[0]);
					}
				}
			}
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
	
        return res;
    }

    public Topic getTopicByUuid(final String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getTopicByUuid executing with topicId: " + uuid);
        HibernateCallback<Topic> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_BY_TOPIC_UUID);
            q.setString("uuid", uuid);
            return (Topic) q.uniqueResult();
        };

        return getHibernateTemplate().execute(hcb);
    }
    
    public List getModeratedTopicsInSite(final String contextId) {

        if (contextId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getModeratedTopicsInSite executing with contextId: " + contextId);

        HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_GET_ALL_MOD_TOPICS_IN_SITE);
            q.setString("contextId", contextId);
            return q.list();
        };
        
        Topic tempTopic = null;
        Set resultSet = new HashSet();      
        List temp = getHibernateTemplate().execute(hcb);
        for (Iterator i = temp.iterator(); i.hasNext();)
        {
          Object[] results = (Object[]) i.next();        
              
          if (results != null) {
            if (results[0] instanceof Topic) {
              tempTopic = (Topic)results[0];
              tempTopic.setBaseForum((BaseForum)results[1]);            
            } else {
              tempTopic = (Topic)results[1];
              tempTopic.setBaseForum((BaseForum)results[0]);
            }
            resultSet.add(tempTopic);
          }
        }
        return Util.setToList(resultSet);
    }

    public DiscussionForum createDiscussionForum() {
        DiscussionForum forum = new DiscussionForumImpl();
        forum.setUuid(getNextUuid());
        forum.setCreated(new Date());
        if(getCurrentUser()!=null){
        forum.setCreatedBy(getCurrentUser());
        }
        forum.setLocked(Boolean.FALSE);
        forum.setDraft(Boolean.FALSE);
        forum.setTypeUuid(typeManager.getDiscussionForumType());                  
        forum.setActorPermissions(createDefaultActorPermissions());
        forum.setModerated(Boolean.FALSE);
        forum.setPostFirst(Boolean.FALSE);
        forum.setAutoMarkThreadsRead(DEFAULT_AUTO_MARK_READ);
        log.debug("createDiscussionForum executed");
        return forum;
    }

    public ActorPermissions createDefaultActorPermissions()
    {
      ActorPermissions actorPermissions = new ActorPermissionsImpl();      
      MessageForumsUser nonSpecifiedUser = new MessageForumsUserImpl();
      nonSpecifiedUser.setUserId(typeManager.getNotSpecifiedType());
      nonSpecifiedUser.setUuid(typeManager.getNotSpecifiedType());
      nonSpecifiedUser.setTypeUuid(typeManager.getNotSpecifiedType());
                  
      actorPermissions.addAccesssor(nonSpecifiedUser);      
      actorPermissions.addContributor(nonSpecifiedUser);
      actorPermissions.addModerator(nonSpecifiedUser);
       return actorPermissions;
    }
    
    /**
     * @see org.sakaiproject.api.app.messageforums.MessageForumsForumManager#createPrivateForum(java.lang.String)
     */
    public PrivateForum createPrivateForum(String title) {
    	return createPrivateForum(title, getCurrentUser());
    }
    
    public PrivateForum createPrivateForum(String title, String userId) {
        /** set all non-null properties in case hibernate flushes session before explicit save */
        PrivateForum forum = new PrivateForumImpl();        
        forum.setTitle(title);
        forum.setUuid(idManager.createUuid());
        forum.setAutoForwardEmail("");
        forum.setOwner(userId);        
        forum.setUuid(getNextUuid());
        forum.setCreated(new Date());
        forum.setCreatedBy(userId);
        forum.setSortIndex(Integer.valueOf(0));
        forum.setShortDescription("short-desc");
        forum.setExtendedDescription("ext desc");
        forum.setAutoForward(Boolean.FALSE);
        forum.setAutoForwardEmail("");
        forum.setPreviewPaneEnabled(Boolean.FALSE);
        forum.setModified(new Date());
        if(userId !=null){
        	forum.setModifiedBy(userId);
        }
        forum.setTypeUuid(typeManager.getPrivateMessageAreaType());
        forum.setModerated(Boolean.FALSE);
        forum.setPostFirst(Boolean.FALSE);
        log.debug("createPrivateForum executed");
        return forum;
    }

    /**
     * @see org.sakaiproject.api.app.messageforums.MessageForumsForumManager#savePrivateForum(org.sakaiproject.api.app.messageforums.PrivateForum)
     */
    public void savePrivateForum(PrivateForum forum) {
    	savePrivateForum(forum, getCurrentUser());
    }
    
    public void savePrivateForum(PrivateForum forum, String userId) {
        boolean isNew = forum.getId() == null;

        if (forum.getSortIndex() == null) {
            forum.setSortIndex(Integer.valueOf(0));
        }

        forum.setModified(new Date());
        if(userId!=null){
        forum.setModifiedBy(userId);
        }
        forum.setOwner(userId);
        getHibernateTemplate().saveOrUpdate(forum);

        log.debug("savePrivateForum executed with forumId: " + forum.getId());
    }

    /**
     * Save a discussion forum
     */
    public void saveDiscussionForum(DiscussionForum forum) {
        saveDiscussionForum(forum, false);
    }

    public void saveDiscussionForum(DiscussionForum forum, boolean draft) {
    	saveDiscussionForum(forum, draft, false);
    }
    
    public void saveDiscussionForum(DiscussionForum forum, boolean draft, boolean logEvent) {
    	String currentUser = getCurrentUser();
    	saveDiscussionForum(forum, draft, logEvent, currentUser);
    }
    
    public void saveDiscussionForum(DiscussionForum forum, boolean draft, boolean logEvent, String currentUser) {
    
        boolean isNew = forum.getId() == null;

        if (forum.getSortIndex() == null) {
            forum.setSortIndex(Integer.valueOf(0));
        }
        if (forum.getLocked() == null) {
            forum.setLocked(Boolean.FALSE);
        }
        if (forum.getModerated() == null) {
        	forum.setModerated(Boolean.FALSE);
        }
        if (forum.getPostFirst() == null) {
        	forum.setPostFirst(Boolean.FALSE);
        }
        forum.setDraft(Boolean.valueOf(draft));
        forum.setModified(new Date());
        if(currentUser!=null){
        forum.setModifiedBy(currentUser);
        }
        else if(currentUser==null){
        	 forum.setModifiedBy(forum.getCreatedBy());
        }
        
        // If the topics were not loaded then there is no need to redo the sort index
        //     thus if it's a hibernate persistentset and initialized
        if( forum.getTopicsSet() != null &&
              ((forum.getTopicsSet() instanceof PersistentSet &&
              ((PersistentSet)forum.getTopicsSet()).wasInitialized()) || !(forum.getTopicsSet() instanceof PersistentSet) )) {
           List topics = forum.getTopics();
           boolean someTopicHasZeroSortIndex = false;
           
           for(Iterator i = topics.iterator(); i.hasNext(); ) {
              DiscussionTopic topic = (DiscussionTopic)i.next();
              if(topic.getSortIndex().intValue() == 0) {
                 someTopicHasZeroSortIndex = true;
                 break;
              }
           }
           if(someTopicHasZeroSortIndex) {
              for(Iterator i = topics.iterator(); i.hasNext(); ) {
                 DiscussionTopic topic = (DiscussionTopic)i.next();
                 topic.setSortIndex(Integer.valueOf(topic.getSortIndex().intValue() + 1));
              }
           }
        }
        //make sure availability flag is set properly
        forum.setAvailability(ForumScheduleNotificationCover.makeAvailableHelper(forum.getAvailabilityRestricted(), forum.getOpenDate(), forum.getCloseDate()));
        
        getHibernateTemplate().saveOrUpdate(forum);
        
        //make sure that any open and close dates are scheduled:
        ForumScheduleNotificationCover.scheduleAvailability(forum);
        
        if (logEvent) {
        	if (isNew) {
        		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_FORUM_ADD, getEventMessage(forum), false));
        	} else {
        		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_FORUM_REVISE, getEventMessage(forum), false));
        	}
        }

        log.debug("saveDiscussionForum executed with forumId: " + forum.getId() + ":: draft: " + draft);
    }
    
    public DiscussionTopic createDiscussionForumTopic(DiscussionForum forum) {
        DiscussionTopic topic = new DiscussionTopicImpl();
        topic.setUuid(getNextUuid());
        topic.setTypeUuid(typeManager.getDiscussionForumType());
        topic.setCreated(new Date());
        if(getCurrentUser()!=null){
        topic.setCreatedBy(getCurrentUser());
        }
        topic.setBaseForum(forum);
        topic.setLocked(Boolean.FALSE);
        topic.setDraft(forum.getDraft());
        topic.setModerated(Boolean.FALSE);
        topic.setPostFirst(Boolean.FALSE);
        topic.setPostAnonymous(Boolean.FALSE);
        topic.setRevealIDsToRoles(Boolean.FALSE);
        topic.setAutoMarkThreadsRead(forum.getAutoMarkThreadsRead());
        log.debug("createDiscussionForumTopic executed");
        return topic;
    }
    
    
    public void saveDiscussionForumTopic(DiscussionTopic topic) {	
    	saveDiscussionForumTopic(topic, false);
    }

    /**
     * Save a discussion forum topic
     */
    public void saveDiscussionForumTopic(DiscussionTopic topic, boolean parentForumDraftStatus) {
    	saveDiscussionForumTopic(topic, parentForumDraftStatus, getCurrentUser(), true);
    }
    
    public void saveDiscussionForumTopic(DiscussionTopic topic, boolean parentForumDraftStatus, String currentUser, boolean logEvent) {
        boolean isNew = topic.getId() == null;

        if (topic.getMutable() == null) {
            topic.setMutable(Boolean.FALSE);
        }
        if (topic.getSortIndex() == null) {
            topic.setSortIndex(Integer.valueOf(0));
        }
        topic.setModified(new Date());
        if(currentUser!=null){
        topic.setModifiedBy(currentUser);
        }
        
        if (topic.getModerated() == null) {
        	topic.setModerated(Boolean.FALSE);
        }
        
        if (topic.getPostFirst() == null) {
        	topic.setPostFirst(Boolean.FALSE);
        }

        if (topic.getPostAnonymous() == null) {
        	topic.setPostAnonymous(Boolean.FALSE);
        }

        if (topic.getRevealIDsToRoles() == null) {
        	topic.setRevealIDsToRoles(Boolean.FALSE);
        }

        //make sure availability is set properly
        topic.setAvailability(ForumScheduleNotificationCover.makeAvailableHelper(topic.getAvailabilityRestricted(), topic.getOpenDate(), topic.getCloseDate()));
        
        if (topic.getId() == null) {
            
          DiscussionForum discussionForum = 
            (DiscussionForum) getForumByIdWithTopics(topic.getBaseForum().getId());
          discussionForum.addTopic(topic);

          saveDiscussionForum(discussionForum, parentForumDraftStatus, logEvent, currentUser);
          //sak-5146 saveDiscussionForum(discussionForum, parentForumDraftStatus);
            
        } else {
            getHibernateTemplate().saveOrUpdate(topic);
        }
        //now schedule any jobs that are needed for the open/close dates
        //this will require having the ID of the topic (if its a new one)
        if(topic.getId() == null){
        	Topic topicTmp = getTopicByUuid(topic.getUuid());
        	if(topicTmp != null){
        		//set the ID so that the forum scheduler can schedule any needed jobs
        		topic.setId(topicTmp.getId());
        	}
        }
        if(topic.getId() != null){
        	ForumScheduleNotificationCover.scheduleAvailability(topic);
        }

        log.debug("saveDiscussionForumTopic executed with topicId: " + topic.getId());
    }

    public OpenTopic createOpenForumTopic(OpenForum forum) {
        OpenTopic topic = new OpenTopicImpl();
        topic.setUuid(getNextUuid());
        topic.setTypeUuid(typeManager.getOpenDiscussionForumType());
        topic.setCreated(new Date());
        if(getCurrentUser()!=null){
        topic.setCreatedBy(getCurrentUser());
        }
        topic.setLocked(Boolean.FALSE);
        topic.setModerated(Boolean.FALSE);
        topic.setPostFirst(Boolean.FALSE);
        topic.setPostAnonymous(Boolean.FALSE);
        topic.setRevealIDsToRoles(Boolean.FALSE);
        topic.setDraft(forum.getDraft());
        log.debug("createOpenForumTopic executed");
        return topic;
    }

    public PrivateTopic createPrivateForumTopic(String title, boolean forumIsParent, boolean topicIsMutable, String userId, Long parentId) {
        /** set all non-null properties in case hibernate flushes session before explicit save */
        PrivateTopic topic = new PrivateTopicImpl();
        topic.setTitle(title);
        topic.setUuid(getNextUuid());        
        topic.setCreated(new Date());
        if(userId!=null){
        topic.setCreatedBy(userId);
        }
        topic.setUserId(userId);
        topic.setShortDescription("short-desc");
        topic.setExtendedDescription("ext-desc");
        topic.setMutable(Boolean.valueOf(topicIsMutable));
        topic.setSortIndex(Integer.valueOf(0));
        topic.setModified(new Date());
        if(userId!=null){
        topic.setModifiedBy(userId);
        }
        topic.setTypeUuid(typeManager.getPrivateMessageAreaType());
        topic.setModerated(Boolean.FALSE);
        topic.setPostFirst(Boolean.FALSE);
        topic.setPostAnonymous(Boolean.FALSE);
        topic.setRevealIDsToRoles(Boolean.FALSE);
        topic.setAutoMarkThreadsRead(DEFAULT_AUTO_MARK_READ);
        log.debug("createPrivateForumTopic executed");
        return topic;
    }

    /**
     * Save a private forum topic
     */

    public void savePrivateForumTopic(PrivateTopic topic){
    	savePrivateForumTopic(topic, getCurrentUser());
    }

    public void savePrivateForumTopic(PrivateTopic topic, String userId) {
    	savePrivateForumTopic(topic, userId, getContextId());
    }

    public void savePrivateForumTopic(PrivateTopic topic, String userId, String siteId) {
    	boolean isNew = topic.getId() == null;

    	topic.setModified(new Date());
    	if(userId != null){
    		topic.setModifiedBy(userId);
    	}
    	getHibernateTemplate().saveOrUpdate(topic);

    	if (isNew) {
    		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FOLDER_ADD, getEventMessage(topic, siteId), false));
    	} else {
    		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FOLDER_REVISE, getEventMessage(topic, siteId), false));
    	}

    	log.debug("savePrivateForumTopic executed with forumId: " + topic.getId());
    }

    /**
     * Save an open forum topic
     */
    public void saveOpenForumTopic(OpenTopic topic) {
        boolean isNew = topic.getId() == null;

        topic.setModified(new Date());
        if(getCurrentUser()!=null){
        topic.setModifiedBy(getCurrentUser());
        }
        getHibernateTemplate().saveOrUpdate(topic);

        if (isNew) {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_ADD, getEventMessage(topic), false));
        } else {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_REVISE, getEventMessage(topic), false));
        }

        log.debug("saveOpenForumTopic executed with forumId: " + topic.getId());
    }

    /**
     * Delete a discussion forum and all topics/messages
     */
    public void deleteDiscussionForum(DiscussionForum forum) {
        long id = forum.getId().longValue();
        eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_FORUM_REMOVE, getEventMessage(forum), false));
        try {
            getSessionFactory().getCurrentSession().evict(forum);
        } catch (Exception e) {
            log.error("could not evict forum: " + forum.getId(), e);
        }
        
        // re-retrieve the forum with the area populated so we don't have to
        // rely on "current context"
        forum = (DiscussionForum)getForumById(true, id);
        List<Topic> topics = getTopicsByIdWithMessages(id);
        for (Topic topic : topics) {
            forum.removeTopic(topic);
        }
        
        //Area area = getAreaByContextIdAndTypeId(typeManager.getDiscussionForumType());
        Area area = forum.getArea();
        area.removeDiscussionForum(forum);
        getHibernateTemplate().saveOrUpdate(area);
        
       
        //getHibernateTemplate().delete(forum);
        log.debug("deleteDiscussionForum executed with forumId: " + id);
    }

    
    public Area getAreaByContextIdAndTypeId(final String typeId) {
        log.debug("getAreaByContextIdAndTypeId executing for current user: " + getCurrentUser());
        HibernateCallback<Area> hcb = session -> {
            Query q = session.getNamedQuery("findAreaByContextIdAndTypeId");
            q.setString("contextId", getContextId());
            q.setString("typeId", typeId);
            return (Area) q.uniqueResult();
        };

        return getHibernateTemplate().execute(hcb);
    }
    
    /**
     * Delete a discussion forum topic
     */
    public void deleteDiscussionForumTopic(DiscussionTopic topic) {
        long id = topic.getId().longValue();
        eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_REMOVE, getEventMessage(topic), false));
        try {
            getSessionFactory().getCurrentSession().evict(topic);
        } catch (Exception e) {
            log.error("could not evict topic: " + topic.getId(), e);
        }
        
        Topic finder = getTopicById(true, topic.getId());
        BaseForum forum = finder.getBaseForum();
        forum.removeTopic(topic);
        getHibernateTemplate().saveOrUpdate(forum);
        
        //getHibernateTemplate().delete(topic);
        log.debug("deleteDiscussionForumTopic executed with topicId: " + id);
    }

    /**
     * Delete an open forum topic
     */
    public void deleteOpenForumTopic(OpenTopic topic) {
        eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_FORUM_REMOVE, getEventMessage(topic), false));
        getHibernateTemplate().delete(topic);
        
        log.debug("deleteOpenForumTopic executed with forumId: " + topic.getId());
    }

    /**
     * Delete a private forum topic
     */
    public void deletePrivateForumTopic(PrivateTopic topic) {
        eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FOLDER_REMOVE, getEventMessage(topic), false));
        getHibernateTemplate().delete(topic);
        
        log.debug("deletePrivateForumTopic executed with forumId: " + topic.getId());
    }

    /**
     * Returns a given number of messages if available in the time provided
     * 
     * @param numberMessages
     *            the number of messages to retrieve
     * @param numberDaysInPast
     *            the number days to look back
     */
    public List getRecentPrivateMessages(int numberMessages, int numberDaysInPast) {
        // TODO: Implement Me!
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a given number of discussion forum messages if available in the
     * time provided
     * 
     * @param numberMessages
     *            the number of forum messages to retrieve
     * @param numberDaysInPast
     *            the number days to look back
     */
    public List getRecentDiscussionForumMessages(int numberMessages, int numberDaysInPast) {
        // TODO: Implement Me!
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a given number of open forum messages if available in the time
     * provided
     * 
     * @param numberMessages
     *            the number of forum messages to retrieve
     * @param numberDaysInPast
     *            the number days to look back
     */
    public List getRecentOpenForumMessages(int numberMessages, int numberDaysInPast) {
        // TODO: Implement Me!
        throw new UnsupportedOperationException();
    }

    private boolean isForumLocked(final Long id) {
        if (id == null) {
            log.error("isForumLocked failed with id: null");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("isForumLocked executing with id: " + id);

        HibernateCallback<Boolean> hcb = session -> {
            Query q = session.getNamedQuery("findForumLockedAttribute");
            q.setLong("id", id);
            return (Boolean) q.uniqueResult();
        };

        return getHibernateTemplate().execute(hcb);
    }
    
    
    public List searchTopicMessages(final Long topicId, final String searchText) {
        if (topicId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("searchTopicMessages executing with topicId: " + topicId);

        HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery("findMessagesBySearchText");
            q.setLong("id", topicId);
            q.setString("searchByText", "%" + searchText + "%");
            return q.list();
        };

        return getHibernateTemplate().execute(hcb);
    }

    
    // helpers
    
    /**
     * ContextId is present site id for now.
     */
    private String getContextId() {
        if (TestUtil.isRunningTests()) {
            return "test-context";
        }
        String presentSiteId = null;
        Placement placement = toolManager.getCurrentPlacement();
        if(placement == null){
        	//current placement is null.. let's try another approach to getting the site id
        	if(sessionManager.getCurrentToolSession() != null){
        		ToolConfiguration toolConfig = siteService.findTool(sessionManager.getCurrentToolSession().getId());
        		if(toolConfig != null){
        			presentSiteId = toolConfig.getSiteId();
        		}
        	}
        }else{
        	presentSiteId = placement.getContext();
        }
        log.debug("site: " + presentSiteId);
        return presentSiteId;
    }

    private String getCurrentUser() {
        if (TestUtil.isRunningTests()) {
            return "test-user";
        }
        return sessionManager.getCurrentSessionUserId();
    }

    private String getNextUuid() {
        return idManager.createUuid();
    }

	private boolean isToolInSite(Site thisSite, String toolId) {
		final Collection toolsInSite = thisSite.getTools(toolId);

		return ! toolsInSite.isEmpty();		
	}

   private String getEventMessage(Object object) {
	   return getEventMessage(object, getContextId());
    }
    
    private String getEventMessage(Object object, String context) {
    	String eventMessagePrefix = "";
    	
    	try {
    		// TODO: How to determine what prefix to put on event message
    		if (isToolInSite(siteService.getSite(context), DiscussionForumService.MESSAGE_CENTER_ID))
    			eventMessagePrefix = "/messages&forums/site/";
    		else if (isToolInSite(siteService.getSite(context), DiscussionForumService.MESSAGES_TOOL_ID))
    			eventMessagePrefix = "/messages/site/";
    		else
    			eventMessagePrefix = "/forums/site/";
    	}
    	catch (IdUnusedException e) {
    		log.debug("IdUnusedException attempting to get site with id: " + context);
    		
    		eventMessagePrefix = "/messages&forums/";
    	}
    	
    	return eventMessagePrefix + context + "/" + object.toString() + "/" + getCurrentUser(); 
    }
    
    public List getForumByTypeAndContextWithTopicsAllAttachments(final String typeUuid)
    {
        return getForumByTypeAndContextWithTopicsAllAttachments(typeUuid, getContextId());
    }

		public List getForumByTypeAndContextWithTopicsAllAttachments(final String typeUuid, final String contextId)
		{
			if (typeUuid == null || contextId == null) {
				throw new IllegalArgumentException("Null typeUuid or contextId passed " +
						"to getForumByTypeAndContextWithTopicsAllAttachments. typeUuid:" + 
						typeUuid + " contextId:" + contextId);
			}      

			HibernateCallback<List> hcb = session -> {
                Query q = session.getNamedQuery(QUERY_BY_TYPE_AND_CONTEXT_WITH_ALL_INFO);
                q.setString("typeUuid", typeUuid);
                q.setString("contextId", contextId);
                return q.list();
            };

			BaseForum tempForum = null;
			Set resultSet = new HashSet();
			List temp = getHibernateTemplate().execute(hcb);

			for (Iterator i = temp.iterator(); i.hasNext();)
			{
				BaseForum results = (DiscussionForumImpl) i.next();  
				resultSet.add(results);
			}

			List resultList = Util.setToList(resultSet);
			Collections.sort(resultList, FORUM_SORT_INDEX_CREATED_DATE_COMPARATOR_DESC);

			// Now that the list is sorted, lets index the forums
			int sort_index = 1;
			for(Iterator i = resultList.iterator(); i.hasNext(); ) {
				tempForum = (BaseForum)i.next();

				tempForum.setSortIndex(Integer.valueOf(sort_index++));
			}

			return resultList;      
		}
		
		public List getForumByTypeAndContextWithTopicsMembership(final String typeUuid, final String contextId)
		{
			if (typeUuid == null || contextId == null) {
				throw new IllegalArgumentException("Null Argument");
			}      

			HibernateCallback<List> hcb = session -> {
                Query q = session.getNamedQuery(QUERY_BY_TYPE_AND_CONTEXT_WITH_ALL_TOPICS_MEMBERSHIP);
                q.setString("typeUuid", typeUuid);
                q.setString("contextId", contextId);
                return q.list();
            };

			BaseForum tempForum = null;
			Set resultSet = new HashSet();
			List temp = getHibernateTemplate().execute(hcb);

			for (Iterator i = temp.iterator(); i.hasNext();)
			{
				Object[] results = (Object[]) i.next();        

				if (results != null) {
					if (results[0] instanceof BaseForum) {
						tempForum = (BaseForum)results[0];
						tempForum.setArea((Area)results[1]);            
					} else {
						tempForum = (BaseForum)results[1];
						tempForum.setArea((Area)results[0]);
					}
					resultSet.add(tempForum);
				}
			}

			List resultList = Util.setToList(resultSet);
			Collections.sort(resultList, FORUM_SORT_INDEX_CREATED_DATE_COMPARATOR_DESC);

			// Now that the list is sorted, lets index the forums
			int sort_index = 1;
			for(Iterator i = resultList.iterator(); i.hasNext(); ) {
				tempForum = (BaseForum)i.next();

				tempForum.setSortIndex(Integer.valueOf(sort_index++));
			}

			return resultList;      
		}
	
		public int getNumModTopicCurrentUserHasModPermForWithPermissionLevel(final List membershipList)
		{
			if (membershipList == null) {
	            log.error("getNumModTopicCurrentUserHasModPermForWithPermissionLevel failed with membershipList: null");
	            throw new IllegalArgumentException("Null Argument");
	        }

	        log.debug("getNumModTopicCurrentUserHasModPermForWithPermissionLevel executing with membershipItems: " + membershipList);

	        // hibernate will not like an empty list so return 0
	        if (membershipList.isEmpty()) return 0;

	        HibernateCallback<Number> hcb = session -> {
                Query q = session.getNamedQuery(QUERY_GET_NUM_MOD_TOPICS_WITH_MOD_PERM_BY_PERM_LEVEL);
                q.setParameterList("membershipList", membershipList);
                q.setString("contextId", getContextId());
                return (Number) q.uniqueResult();
            };

	        return getHibernateTemplate().execute(hcb).intValue();
		}
		
		public int getNumModTopicCurrentUserHasModPermForWithPermissionLevelName(final List membershipList)
		{
			if (membershipList == null) {
	            log.error("getNumModTopicCurrentUserHasModPermForWithPermissionLevelName failed with membershipList: null");
	            throw new IllegalArgumentException("Null Argument");
	        }

	        log.debug("getNumModTopicCurrentUserHasModPermForWithPermissionLevelName executing with membershipItems: " + membershipList);

	        // hibernate will not like an empty list so return 0
	        if (membershipList.isEmpty()) return 0;

	        HibernateCallback<Number> hcb = session -> {
                Query q = null;
                if ("mysql".equals(serverConfigurationService.getString("vendor@org.sakaiproject.db.api.SqlService"))) {
                    q = session.createSQLQuery("select straight_join count(*) as NBR " +
                            "from MFR_AREA_T area " +
                            "inner join MFR_OPEN_FORUM_T openforum on openforum.surrogateKey=area.ID inner " +
                            "join MFR_TOPIC_T topic on topic.of_surrogateKey=openforum.ID " +
                            "inner join MFR_MEMBERSHIP_ITEM_T membership on topic.ID=membership.t_surrogateKey, " +
                            "MFR_PERMISSION_LEVEL_T permission " +
                            "where area.CONTEXT_ID = :contextId " +
                            "and topic.MODERATED = true " +
                            "and (membership.NAME in ( :membershipList ) " +
                            "and permission.MODERATE_POSTINGS = true " +
                            "and permission.TYPE_UUID <> :customTypeUuid " +
                            "and permission.NAME=membership.PERMISSION_LEVEL_NAME)");
                } else {
                    q = session.getNamedQuery(QUERY_GET_NUM_MOD_TOPICS_WITH_MOD_PERM_BY_PERM_LEVEL_NAME);
                }
                q.setParameterList("membershipList", membershipList);
                q.setString("contextId", getContextId());
                q.setString("customTypeUuid", typeManager.getCustomLevelType());
                return (Number) q.uniqueResult();
            };

	        return getHibernateTemplate().execute(hcb).intValue();
		}
		
		public BaseForum getForumByIdWithTopicsAttachmentsAndMessages(final Long forumId)
		{
			if (forumId == null) {
				throw new IllegalArgumentException("Null Argument");
			}      

			HibernateCallback<BaseForum> hcb = session -> {
                Query q = session.getNamedQuery(QUERY_GET_FORUM_BY_ID_WITH_TOPICS_AND_ATT_AND_MSGS);
                q.setLong("id", forumId);
                return (BaseForum) q.uniqueResult();
              };
		      
		      BaseForum bForum = getHibernateTemplate().execute(hcb);
		      
		      if (bForum != null){
		        getHibernateTemplate().initialize(bForum.getAttachmentsSet());
		      }
		      
		      return bForum;      
		}
		
		public Topic getTopicByIdWithMemberships(final Long topicId) {

			if (topicId == null) {
				throw new IllegalArgumentException("Null Argument");
			}      

			HibernateCallback<Topic> hcb = session -> {
                Query q = session.getNamedQuery("findTopicByIdWithMemberships");
                q.setLong("id", topicId);
                return (Topic) q.uniqueResult();
            };

			return getHibernateTemplate().execute(hcb);
		}

		public List<Topic> getTopicsInSite(final String contextId)
		{
			return getTopicsInSite(contextId, false);
		}

		public List<Topic> getTopicsInSite(final String contextId, boolean anonymousOnly)
		{
			if (contextId == null)
			{
				throw new IllegalArgumentException("Null Argument");
			}

			final String query = anonymousOnly ? "findAnonymousTopicsInSite" : "findTopicsInSite";

			HibernateCallback<List<Topic>> hcb = session -> {
                Query q = session.getNamedQuery(query);
                q.setString("contextId", contextId);
                return q.list();
            };

			List<Topic> topicList = new ArrayList<>();
			List resultSet = (List) getHibernateTemplate().execute(hcb);
			for (Object objResultArray : resultSet)
			{
				Object[] resultArray = (Object[]) objResultArray;
				for (Object result : resultArray)
				{
					if (result instanceof Topic)
					{
						topicList.add((Topic) result);
						break;
					}
				}
			}
			return topicList;
		}

		public List<Topic> getAnonymousTopicsInSite(final String contextId)
		{
			return getTopicsInSite(contextId, true);
		}

		public boolean isSiteHasAnonymousTopics(String contextId)
		{
			return !getAnonymousTopicsInSite(contextId).isEmpty();
		}
		
		public boolean doesRoleHavePermissionInTopic(final Long topicId, final String roleName, final String permissionName) {

			if (topicId == null) {
				throw new IllegalArgumentException("Null Argument");
			}      

			HibernateCallback<Number> hcb = session -> (Number) session
				.getNamedQuery("findNumRoleWithPermissionInTopic")
				.setLong("id", topicId)
				.setString("roleName", roleName)
				.setString("permissionLevelName", permissionName)
				.uniqueResult();

			Number countRows = getHibernateTemplate().execute(hcb);
			return countRows.intValue() > 0;
		}
}
