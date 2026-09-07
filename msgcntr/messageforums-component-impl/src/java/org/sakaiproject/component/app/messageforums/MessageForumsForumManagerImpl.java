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
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.collection.spi.PersistentSet;
import org.hibernate.query.Query;
import org.sakaiproject.api.app.messageforums.ActorPermissions;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.MessageForumsUser;
import org.sakaiproject.api.app.messageforums.MessageMoveHistory;
import org.sakaiproject.api.app.messageforums.OpenForum;
import org.sakaiproject.api.app.messageforums.OpenTopic;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateTopic;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.UnreadStatus;
import org.sakaiproject.api.app.messageforums.cover.ForumScheduleNotificationCover;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.messageforums.dao.hibernate.ActorPermissionsImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AreaImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AttachmentImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.BaseForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DBMembershipItemImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DiscussionForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DiscussionTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageForumsUserImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.OpenForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.OpenTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PermissionLevelImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.TopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.Util;
import org.sakaiproject.component.app.messageforums.dao.hibernate.util.comparator.ForumBySortIndexAscAndCreatedDateDesc;
import org.sakaiproject.component.app.messageforums.dao.hibernate.util.comparator.TopicBySortIndexAscAndCreatedDateDesc;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * The forums are sorted by this java class.  The topics are sorted by the order-by in the hbm file.
 *
 */
@Slf4j
@Transactional
public class MessageForumsForumManagerImpl implements MessageForumsForumManager {

    @Getter @Setter
    private ServerConfigurationService serverConfigurationService;
    @Getter @Setter
    private IdManager idManager;
    @Getter @Setter
    private SessionManager sessionManager;
    @Getter @Setter
    private EventTrackingService eventTrackingService;
    @Getter @Setter
    private SiteService siteService;
    @Getter @Setter
    private ToolManager toolManager;
    @Getter @Setter
    private MessageForumsTypeManager typeManager;
    @Setter
    private ContentHostingService contentHostingService;
    @Getter
    @Setter
    private SessionFactory sessionFactory;

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
    private static final String QUERY_FAQ_FORUMS = "findFaqForums";
            
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
    private static final String QUERY_UNREAD_STATUSES_FOR_TOPIC = "findUnreadStatusesForTopic";
    private static final String QUERY_GET_UNREAD_STATUSES_FOR_TOPIC = "findHistoryForMessage";


    private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";

    /** Sorts the forums by the sort index and if the same index then order by the creation date */
    public static final Comparator FORUM_SORT_INDEX_CREATED_DATE_COMPARATOR_DESC = new ForumBySortIndexAscAndCreatedDateDesc();

    private Boolean DEFAULT_AUTO_MARK_READ = false; 

    public MessageForumsForumManagerImpl() {}
    
    public void init() {
       log.info("init()");
       DEFAULT_AUTO_MARK_READ = serverConfigurationService.getBoolean("msgcntr.forums.default.auto.mark.threads.read", false);
    }

    public void initializeTopicsForForum(BaseForum forum){      
      Hibernate.initialize(forum);
      Hibernate.initialize(forum.getTopicsSet());
    }
    
    public List getTopicsByIdWithMessages(final Long forumId){
      if (forumId == null) {
        throw new IllegalArgumentException("Null Argument");
      }   

      Session session = sessionFactory.getCurrentSession();
      CriteriaBuilder cb = session.getCriteriaBuilder();

      CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

      Root<TopicImpl> topic = cq.from(TopicImpl.class);

      Join<TopicImpl, OpenForum> forum =
        topic.join("openForum", JoinType.INNER);

      topic.fetch("messagesSet", JoinType.LEFT);

      cq.select(cb.array(topic, forum))
        .where(cb.equal(forum.get("id"), forumId));

      List<Object[]> temp = session.createQuery(cq).getResultList();

    Topic tempTopic = null;
    Set resultSet = new HashSet();      

    for (Iterator i = temp.iterator(); i.hasNext();)
    {
      Object[] results = (Object[]) i.next();        
          
      if (results != null) {
        if (results[0] instanceof Topic) {
          tempTopic = (Topic) Hibernate.unproxy(results[0]);
          tempTopic.setBaseForum((BaseForum) Hibernate.unproxy(results[1]));
        } else {
          tempTopic = (Topic) Hibernate.unproxy(results[1]);
          tempTopic.setBaseForum((BaseForum) Hibernate.unproxy(results[0]));
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

      Session session = sessionFactory.getCurrentSession();
      CriteriaBuilder cb = session.getCriteriaBuilder();

      CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

      Root<TopicImpl> topic = cq.from(TopicImpl.class);

      Join<TopicImpl, OpenForum> forum =
        topic.join("openForum", JoinType.INNER);

      topic.fetch("messagesSet", JoinType.LEFT);

      cq.select(cb.array(topic, forum))
        .where(cb.equal(forum.get("id"), forumId));

      List<Object[]> temp = session.createQuery(cq).getResultList();

    Topic tempTopic = null;
    Set resultSet = new HashSet();      

    for (Iterator i = temp.iterator(); i.hasNext();)
    {
      Object[] results = (Object[]) i.next();        
          
      if (results != null) {
        if (results[0] instanceof Topic) {
          tempTopic = (Topic) Hibernate.unproxy(results[0]);
          tempTopic.setBaseForum((BaseForum) Hibernate.unproxy(results[1]));
        } else {
          tempTopic = (Topic) Hibernate.unproxy(results[1]);
          tempTopic.setBaseForum((BaseForum) Hibernate.unproxy(results[0]));
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

      Session session = sessionFactory.getCurrentSession();
      CriteriaBuilder cb = session.getCriteriaBuilder();

      CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

      Root<TopicImpl> topic = cq.from(TopicImpl.class);

      Join<TopicImpl, OpenForum> forum =
              topic.join("openForum", JoinType.INNER);

      Fetch<TopicImpl, ?> message =
              topic.fetch("messagesSet", JoinType.LEFT);

      message.fetch("attachmentsSet", JoinType.LEFT);

      topic.fetch("membershipItemSet", JoinType.LEFT);

      cq.select(cb.array(topic, forum))
        .where(cb.equal(forum.get("id"), forumId));

      List<Object[]> temp = session.createQuery(cq).getResultList();

    Topic tempTopic = null;
    SortedSet resultSet = new TreeSet(new TopicBySortIndexAscAndCreatedDateDesc());

    for (Iterator i = temp.iterator(); i.hasNext();)
    {
      Object[] results = (Object[]) i.next();        
          
      if (results != null) {
        if (results[0] instanceof Topic) {
          tempTopic = (Topic) Hibernate.unproxy(results[0]);
          tempTopic.setBaseForum((BaseForum) Hibernate.unproxy(results[1]));
        } else {
          tempTopic = (Topic) Hibernate.unproxy(results[1]);
          tempTopic.setBaseForum((BaseForum) Hibernate.unproxy(results[0]));
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
    return getForumsForSite(getContextId());
  }

  public List<DiscussionForum> getForumsForSite(String siteId) {

      Session session = sessionFactory.getCurrentSession();
      CriteriaBuilder cb = session.getCriteriaBuilder();

      CriteriaQuery<OpenForumImpl> cq = cb.createQuery(OpenForumImpl.class);
      Root<OpenForumImpl> forum = cq.from(OpenForumImpl.class);

      Fetch<OpenForumImpl, ?> topic = forum.fetch("topicsSet", JoinType.LEFT);
      topic.fetch("attachmentsSet", JoinType.LEFT);
      forum.fetch("attachmentsSet", JoinType.LEFT);

      Join<OpenForumImpl, ?> area = forum.join("area", JoinType.INNER);

      cq.select(forum)
        .distinct(true)
        .where(
            cb.equal(forum.get("typeUuid"), typeManager.getDiscussionForumType()),
            cb.equal(area.get("contextId"), siteId)
        );

      List<OpenForumImpl> temp = session.createQuery(cq).getResultList();

      List<DiscussionForum> returnList = temp.stream()
        .distinct()
        .map(f -> (DiscussionForum) f)
        .toList();
      return returnList;
  }

      
  public List getReceivedUuidByContextId(final List siteList) {
      if (siteList == null) {
          throw new IllegalArgumentException("Null Argument");
      }      

      Session session = sessionFactory.getCurrentSession();
      CriteriaBuilder cb = session.getCriteriaBuilder();

      CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
      Root<PrivateTopicImpl> topic = cq.from(PrivateTopicImpl.class);

      Join<PrivateTopicImpl, ?> forum = topic.join("privateForum", JoinType.LEFT);
      Join<?, ?> area = forum.join("area", JoinType.LEFT);

      cq.select(cb.array(area.get("contextId"), topic.get("uuid")))
        .where(
            area.get("contextId").in(siteList),
            cb.equal(topic.get("title"), "pvt_received"),
            cb.equal(topic.get("owner"), getCurrentUser())
        );

      return session.createQuery(cq).getResultList();
  }

    public Topic getTopicByIdWithMessagesAndAttachments(final Long topicId) {

      if (topicId == null) {
          throw new IllegalArgumentException("Null Argument");
      }      

      Session session = sessionFactory.getCurrentSession();
      CriteriaBuilder cb = session.getCriteriaBuilder();

      CriteriaQuery<TopicImpl> cq = cb.createQuery(TopicImpl.class);
      Root<TopicImpl> topic = cq.from(TopicImpl.class);

      Fetch<TopicImpl, ?> message = topic.fetch("messagesSet", JoinType.LEFT);
      message.fetch("attachmentsSet", JoinType.LEFT);

      cq.select(topic)
        .where(cb.equal(topic.get("id"), topicId));

      return session.createQuery(cq)
              .uniqueResultOptional()
              .orElse(null);
    }
    
    public Topic getTopicByIdWithMessages(final Long topicId) {

      if (topicId == null) {
          throw new IllegalArgumentException("Null Argument");
      }      

      Session session = sessionFactory.getCurrentSession();
      CriteriaBuilder cb = session.getCriteriaBuilder();

      CriteriaQuery<TopicImpl> cq = cb.createQuery(TopicImpl.class);
      Root<TopicImpl> topic = cq.from(TopicImpl.class);

      topic.fetch("messagesSet", JoinType.LEFT);

      cq.select(topic)
        .where(cb.equal(topic.get("id"), topicId));

      return session.createQuery(cq)
              .uniqueResultOptional()
              .orElse(null);
    }
    
    public Topic getTopicWithAttachmentsById(final Long topicId) {

        if (topicId == null) {
            throw new IllegalArgumentException("Null Argument");
        }      

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<TopicImpl> cq = cb.createQuery(TopicImpl.class);
        Root<TopicImpl> topic = cq.from(TopicImpl.class);

        topic.fetch("messagesSet", JoinType.LEFT);
        topic.fetch("attachmentsSet", JoinType.LEFT);

        cq.select(topic)
          .where(cb.equal(topic.get("id"), topicId));

        return session.createQuery(cq)
                .uniqueResultOptional()
                .orElse(null);
      }

    
    public List<Attachment> getTopicAttachments(final Long topicId) {
    	if (topicId == null) {
    		throw new IllegalArgumentException("Null Argument topicId");
    	}
    	Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<AttachmentImpl> cq = cb.createQuery(AttachmentImpl.class);
        Root<AttachmentImpl> attachment = cq.from(AttachmentImpl.class);

        cq.select(attachment)
          .where(cb.equal(attachment.get("topic").get("id"), topicId));

        return (List<Attachment>) (List<?>) session.createQuery(cq)
                      .setCacheable(true)
                      .getResultList();
    } 

    
    public BaseForum getForumByIdWithTopics(final Long forumId) {

      if (forumId == null) {
          throw new IllegalArgumentException("Null Argument");
      }      

      Session session = sessionFactory.getCurrentSession();
      CriteriaBuilder cb = session.getCriteriaBuilder();

      CriteriaQuery<OpenForumImpl> cq = cb.createQuery(OpenForumImpl.class);
      Root<OpenForumImpl> forum = cq.from(OpenForumImpl.class);

      forum.fetch("topicsSet", JoinType.LEFT);

      cq.select(forum)
        .where(cb.equal(forum.get("id"), forumId));

      List<OpenForumImpl> results = session.createQuery(cq).getResultList();

      BaseForum bForum = results.isEmpty() ? null : results.get(0);

      if (bForum != null) {
          Hibernate.initialize(bForum.getAttachmentsSet());
      }
      
      return bForum;      
    }
    
    public List getForumByTypeAndContext(final String typeUuid) {

      if (typeUuid == null) {
          throw new IllegalArgumentException("Null Argument");
      }      

      Session session = sessionFactory.getCurrentSession();
      CriteriaBuilder cb = session.getCriteriaBuilder();

      CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
      Root<BaseForumImpl> forum = cq.from(BaseForumImpl.class);
      Join<BaseForumImpl, AreaImpl> area = forum.join("area");

      forum.fetch("topicsSet", JoinType.LEFT);

      cq.select(cb.array(forum, area))
        .where(cb.equal(forum.get("typeUuid"), typeUuid),
               cb.equal(area.get("contextId"), getContextId()));

      List<Object[]> temp = session.createQuery(cq).getResultList();

      BaseForum tempForum = null;
      Set resultSet = new HashSet();
            
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

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<BaseForumImpl> forum = cq.from(BaseForumImpl.class);
        Join<BaseForumImpl, AreaImpl> area = forum.join("area");

        forum.fetch("topicsSet", JoinType.LEFT);

        cq.select(cb.array(forum, area))
          .where(cb.equal(forum.get("typeUuid"), typeUuid),
                 cb.equal(area.get("contextId"), contextId));

        List<Object[]> temp = session.createQuery(cq).getResultList();

        BaseForum tempForum = null;
        Set resultSet = new HashSet();
              
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

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<TopicImpl> cq = cb.createQuery(TopicImpl.class);
        Root<TopicImpl> topic = cq.from(TopicImpl.class);

        topic.fetch("attachmentsSet", JoinType.LEFT);

        cq.select(topic)
          .where(cb.equal(topic.get("id"), topicId));

        Topic result = session.createQuery(cq).uniqueResult();

        // unproxy to avoid ClassCastException in certain scenarios
        return (Topic) Hibernate.unproxy(result);

    }

    public PrivateForum getPrivateForumByOwner(final String owner) {

        if (owner == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getForumByOwner executing with owner: " + owner);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<PrivateForumImpl> cq = cb.createQuery(PrivateForumImpl.class);
        Root<PrivateForumImpl> pf = cq.from(PrivateForumImpl.class);

        cq.select(pf)
          .where(cb.equal(pf.get("owner"), owner));

        return session.createQuery(cq).uniqueResult();
    }

    
    public PrivateForum getPrivateForumByOwnerArea(final String owner, final Area area) {

      if (owner == null || area == null) {
          throw new IllegalArgumentException("Null Argument");
      }

      log.debug("getForumByOwnerArea executing with owner: " + owner + " and area:" + area);

      Session session = sessionFactory.getCurrentSession();
      CriteriaBuilder cb = session.getCriteriaBuilder();

      CriteriaQuery<PrivateForumImpl> cq = cb.createQuery(PrivateForumImpl.class);
      Root<PrivateForumImpl> pf = cq.from(PrivateForumImpl.class);

      cq.select(pf)
        .where(cb.equal(pf.get("owner"), owner),
               cb.equal(pf.get("area"), area));

      return session.createQuery(cq).uniqueResult();
    }

    public PrivateForum getPrivateForumByOwnerAreaNull(final String owner) {

      if (owner == null) {
          throw new IllegalArgumentException("Null Argument");
      }

      log.debug("getForumByOwnerAreaNull executing with owner: " + owner);

      Session session = sessionFactory.getCurrentSession();
      CriteriaBuilder cb = session.getCriteriaBuilder();

      CriteriaQuery<PrivateForumImpl> cq = cb.createQuery(PrivateForumImpl.class);
      Root<PrivateForumImpl> pf = cq.from(PrivateForumImpl.class);

      cq.select(pf)
        .where(cb.equal(pf.get("owner"), owner),
               cb.isNull(pf.get("area")));

      return session.createQuery(cq).uniqueResult();
    }
    
    public BaseForum getForumByIdWithAttachments(final Long forumId) {
      
      if (forumId == null) {
          throw new IllegalArgumentException("Null Argument");
      }

      log.debug("getForumByIdWithAttachments executing with forumId: " + forumId);
                  
      Session session = sessionFactory.getCurrentSession();
      CriteriaBuilder cb = session.getCriteriaBuilder();

      CriteriaQuery<OpenForumImpl> cq = cb.createQuery(OpenForumImpl.class);
      Root<OpenForumImpl> f = cq.from(OpenForumImpl.class);

      f.fetch("attachmentsSet", JoinType.LEFT);

      cq.select(f)
        .where(cb.equal(f.get("id"), forumId));

      OpenForumImpl result = session.createQuery(cq).uniqueResult();

      // unproxy the result to avoid ClassCastException in certain scenarios
      return (BaseForum) Hibernate.unproxy(result);

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
            Session session = sessionFactory.getCurrentSession();
            CriteriaBuilder cb = session.getCriteriaBuilder();

            CriteriaQuery<PrivateForumImpl> cq = cb.createQuery(PrivateForumImpl.class);
            Root<PrivateForumImpl> forum = cq.from(PrivateForumImpl.class);

            cq.select(forum)
              .where(cb.equal(forum.get("id"), forumId));

            return session.createQuery(cq).uniqueResultOptional().orElse(null);
        }
    }

    public BaseForum getForumByUuid(final String forumId) {
        if (forumId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getForumByUuid executing with forumId: " + forumId);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<BaseForumImpl> cq = cb.createQuery(BaseForumImpl.class);
        Root<BaseForumImpl> forum = cq.from(BaseForumImpl.class);

        cq.select(forum).where(cb.equal(forum.get("uuid"), forumId));

        return session.createQuery(cq).uniqueResult();
    }

    public Topic getTopicById(final boolean open, final Long topicId) {
        if (topicId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getTopicById executing with topicId: " + topicId);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<TopicImpl> cq = cb.createQuery(TopicImpl.class);
        Root<TopicImpl> topicRoot = cq.from(TopicImpl.class);

        cq.select(topicRoot).where(cb.equal(topicRoot.get("id"), topicId));

        // Use uniqueResult() so missing topics return null instead of throwing NoResultException.
        Topic topic = (Topic) Hibernate.unproxy(session.createQuery(cq).uniqueResultOptional().orElse(null));

        if (topic != null) {
            BaseForum parentForum = open ? (BaseForum) Hibernate.unproxy(topic.getOpenForum()) : (BaseForum) Hibernate.unproxy(topic.getPrivateForum());
            topic.setBaseForum(parentForum);
        }

        return topic;
    }

    public Topic getTopicByUuid(final String uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getTopicByUuid executing with topicId: " + uuid);
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<TopicImpl> cq = cb.createQuery(TopicImpl.class);
        Root<TopicImpl> topic = cq.from(TopicImpl.class);

        cq.select(topic).where(cb.equal(topic.get("uuid"), uuid));

        return session.createQuery(cq).uniqueResult();
    }
    
    public List<Topic> getModeratedTopicsInSite(final String contextId) {

        if (contextId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getModeratedTopicsInSite executing with contextId: " + contextId);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<TopicImpl> topic = cq.from(TopicImpl.class);
        Join<TopicImpl, ?> forum = topic.join("openForum", JoinType.INNER);
        Join<?, ?> area = forum.join("area", JoinType.INNER);

        cq.select(cb.array(topic, forum))
          .where(
              cb.equal(area.get("contextId"), contextId),
              cb.isTrue(topic.get("moderated"))
          );

        List<Object[]> temp = session.createQuery(cq).getResultList();
        
        Topic tempTopic = null;
        Set<Topic> resultSet = new HashSet<>();
        for (Iterator i = temp.iterator(); i.hasNext();)
        {
          Object[] results = (Object[]) i.next();        
              
          if (results != null) {
            if (results[0] instanceof Topic) {
              tempTopic = (Topic) Hibernate.unproxy(results[0]);
              tempTopic.setBaseForum((BaseForum) Hibernate.unproxy(results[1]));
            } else {
              tempTopic = (Topic) Hibernate.unproxy(results[1]);
              tempTopic.setBaseForum((BaseForum) Hibernate.unproxy(results[0]));
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
        forum.setRestrictPermissionsForGroups(Boolean.FALSE);
        forum.setFaqForum(Boolean.FALSE);
        log.debug("createDiscussionForum executed");
        return forum;
    }

    public boolean isPublishToFaqEnabled() {
        return serverConfigurationService.getBoolean(PUBLISH_TO_FAQ_ENABLED_PROPERTY, true);
    }

    public DiscussionForum getFaqForumForArea(Area area) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<OpenForumImpl> cq = cb.createQuery(OpenForumImpl.class);
        Root<OpenForumImpl> forum = cq.from(OpenForumImpl.class);

        forum.fetch("topicsSet", JoinType.LEFT).fetch("attachmentsSet", JoinType.LEFT);
        forum.fetch("attachmentsSet", JoinType.LEFT);

        cq.select(forum)
          .distinct(true)
          .where(
              cb.equal(forum.get("typeUuid"), area.getTypeUuid()),
              cb.equal(forum.get("area").get("contextId"), area.getContextId()),
              cb.isTrue(forum.get("faqForum"))
         );

        DiscussionForum existingFaqForum = session.createQuery(cq)
                .getResultList()
                .stream()
                .findAny()
                .map(f -> (DiscussionForum) f)
                .orElse(null);
        return existingFaqForum != null ? existingFaqForum : createFaqForum(area);
    }

    public DiscussionTopic getFaqTopicForForum(DiscussionForum faqForum) {
        if (faqForum == null) {
            return null;
        }

        Set<DiscussionTopic> faqForumTopics = faqForum.getTopicsSet();
        if (faqForumTopics == null) {
            return null;
        }

        return faqForumTopics.stream()
                .filter(topic -> Boolean.TRUE.equals(topic.getFaqTopic()))
                .findAny().orElse(null);
    }

    public DiscussionForum getOrCreateFaqForumForArea(Area area) {
        DiscussionForum existingFaqForum = getFaqForumForArea(area);
        return existingFaqForum != null
                ? existingFaqForum
                : createFaqForum(area);
    }

    public DiscussionTopic getOrCreateFaqTopicForForum(DiscussionForum discussionForum) {
        DiscussionTopic existingFaqTopic = getFaqTopicForForum(discussionForum);
        return existingFaqTopic != null
                ? existingFaqTopic
                : createFaqTopic(discussionForum);
    }

    public ActorPermissions createDefaultActorPermissions()
    {
      ActorPermissions actorPermissions = new ActorPermissionsImpl();      
      MessageForumsUser nonSpecifiedUser = new MessageForumsUserImpl();
      nonSpecifiedUser.setUserId(typeManager.getNotSpecifiedType());
      nonSpecifiedUser.setUuid(typeManager.getNotSpecifiedType());
      nonSpecifiedUser.setTypeUuid(typeManager.getNotSpecifiedType());
                  
      actorPermissions.addAccessor(nonSpecifiedUser);
      actorPermissions.addContributor(nonSpecifiedUser);
      actorPermissions.addModerator(nonSpecifiedUser);
       return actorPermissions;
    }
    
    // Create a new FAQ forum based on defaults
    public DiscussionForum createFaqForum(Area discussionArea) {
        log.debug("Creating a new FAQ Forum");

        // Get site locale first, use server locale as fallback
        Locale locale = siteService.getSiteLocale(discussionArea.getContextId()).orElse(Locale.getDefault());
        ResourceBundle resourceBundle = ResourceBundle.getBundle(MESSAGECENTER_BUNDLE, locale);

        DiscussionForum createdForum = createDiscussionForum();
        createdForum.setArea(discussionArea);
        createdForum.setCreatedBy(UserDirectoryService.ADMIN_ID);
        createdForum.setTitle(resourceBundle.getString("cdfm_faq_forum_title"));
        createdForum.setShortDescription(resourceBundle.getString("cdfm_faq_forum_description"));
        createdForum.setModerated(discussionArea.getModerated());
        createdForum.setPostFirst(discussionArea.getPostFirst());
        createdForum.setFaqForum(Boolean.TRUE);

        DiscussionForum savedForum = saveDiscussionForum(createdForum);

        DiscussionTopic discussionTopic = createFaqTopic(savedForum);

        savedForum.addTopic(discussionTopic);

        return savedForum;
    }

    // Create a new FAQ topic based on defaults
    public DiscussionTopic createFaqTopic(DiscussionForum discussionForum) {
        log.debug("Creating a new FAQ Topic");

        // Get site locale first, use server locale as fallback
        Locale locale = siteService.getSiteLocale(discussionForum.getArea().getContextId()).orElse(Locale.getDefault());
        ResourceBundle resourceBundle = ResourceBundle.getBundle(MESSAGECENTER_BUNDLE, locale);

        DiscussionTopic createdTopic = createDiscussionForumTopic(discussionForum);
        createdTopic.setTitle(resourceBundle.getString("cdfm_faq_topic_title"));
        createdTopic.setShortDescription(resourceBundle.getString("cdfm_faq_topic_description"));
        createdTopic.setCreatedBy(UserDirectoryService.ADMIN_ID);
        createdTopic.setFaqTopic(Boolean.TRUE);
        createdTopic.setBaseForum(discussionForum);

        DiscussionTopic savedTopic = saveDiscussionForumTopic(createdTopic, false);

        return savedTopic;
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
        forum.setAutoForward(PrivateForumImpl.AUTO_FOWARD_DEFAULT);
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
    public PrivateForum savePrivateForum(PrivateForum forum) {
    	return savePrivateForum(forum, getCurrentUser());
    }
    
    public PrivateForum savePrivateForum(PrivateForum forum, String userId) {
        boolean isNew = forum.getId() == null;

        if (forum.getSortIndex() == null) {
            forum.setSortIndex(Integer.valueOf(0));
        }

        forum.setModified(new Date());
        if(userId!=null){
        forum.setModifiedBy(userId);
        }
        forum.setOwner(userId);
        Session session = sessionFactory.getCurrentSession();
        PrivateForum mergedForum = session.merge(forum);

        log.debug("savePrivateForum executed with forumId: " + mergedForum.getId());

        return mergedForum;
    }

    /**
     * Save a discussion forum
     */
    public DiscussionForum saveDiscussionForum(DiscussionForum forum) {
        return saveDiscussionForum(forum, false);
    }

    public DiscussionForum saveDiscussionForum(DiscussionForum forum, boolean draft) {
        return saveDiscussionForum(forum, draft, false);
    }
    
    public DiscussionForum saveDiscussionForum(DiscussionForum forum, boolean draft, boolean logEvent) {
    	String currentUser = getCurrentUser();
        return saveDiscussionForum(forum, draft, logEvent, currentUser);
    }
    
    public DiscussionForum saveDiscussionForum(DiscussionForum forum, boolean draft, boolean logEvent, String currentUser) {
    
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
        forum.setAvailability(ForumScheduleNotificationCover.makeAvailableHelper(forum.getAvailabilityRestricted(), forum.getOpenDate(), forum.getCloseDate(), forum.getLockedAfterClosed()));

        Session session = sessionFactory.getCurrentSession();
        DiscussionForum mergedForum = session.merge(forum);

        //make sure that any open and close dates are scheduled:
        ForumScheduleNotificationCover.scheduleAvailability(mergedForum);
        
        if (logEvent) {
        	if (isNew) {
        		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_FORUM_ADD, getEventMessage(mergedForum), false));
        	} else {
        		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_FORUM_REVISE, getEventMessage(mergedForum), false));
        	}
        }

        log.debug("saveDiscussionForum executed with forumId: {} :: draft: {}", mergedForum.getId(), draft);

        return mergedForum;
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
        topic.setRestrictPermissionsForGroups(Boolean.FALSE);
        topic.setFaqTopic(Boolean.FALSE);
        log.debug("createDiscussionForumTopic executed");
        return topic;
    }
    
    
    public DiscussionTopic saveDiscussionForumTopic(DiscussionTopic topic) {
    	return saveDiscussionForumTopic(topic, false);
    }

    /**
     * Save a discussion forum topic
     */
    public DiscussionTopic saveDiscussionForumTopic(DiscussionTopic topic, boolean parentForumDraftStatus) {
    	return saveDiscussionForumTopic(topic, parentForumDraftStatus, getCurrentUser(), true);
    }
    
    public DiscussionTopic saveDiscussionForumTopic(DiscussionTopic topic, boolean parentForumDraftStatus, String currentUser, boolean logEvent) {
        boolean isNew = topic.getId() == null;

        topic.setModified(new Date());

        transformNullsToFalse(topic, currentUser);

        //make sure availability is set properly
        topic.setAvailability(ForumScheduleNotificationCover.makeAvailableHelper(topic.getAvailabilityRestricted(), topic.getOpenDate(), topic.getCloseDate(), topic.getLockedAfterClosed()));

        DiscussionTopic topicReturn = topic;
        if (isNew) {
            DiscussionForum discussionForum = (DiscussionForum) getForumByIdWithTopics(topic.getBaseForum().getId());
            discussionForum.addTopic(topic);
        } else {
            topicReturn = (DiscussionTopic) getSessionFactory().getCurrentSession().merge(topic);
            topicReturn.setBaseForum(topic.getBaseForum());
        }

        //now schedule any jobs that are needed for the open/close dates
        //this will require having the ID of the topic (if its a new one)
        if(topicReturn.getId() == null){
        	Topic topicTmp = getTopicByUuid(topicReturn.getUuid());
        	if(topicTmp != null){
        		//set the ID so that the forum scheduler can schedule any needed jobs
        		topicReturn.setId(topicTmp.getId());
        	}
        }
        
        if(topicReturn.getId() != null){
        	ForumScheduleNotificationCover.scheduleAvailability(topicReturn);
        }

        log.debug("saveDiscussionForumTopic executed with topicId: " + topicReturn.getId());
        return topicReturn;
    }

    private void transformNullsToFalse(DiscussionTopic topic, String currentUser) {
        if (topic.getMutable() == null) {
            topic.setMutable(Boolean.FALSE);
        }
        if (topic.getSortIndex() == null) {
            topic.setSortIndex(Integer.valueOf(0));
        }
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
    }

    public Message createMessage(final DiscussionTopic topic) {
        final Message message = new MessageImpl();
        message.setUuid(getNextUuid());
        message.setTypeUuid(typeManager.getDiscussionForumType());
        message.setCreated(new Date());
        if (getCurrentUser() != null) {
            topic.setCreatedBy(getCurrentUser());
        }
        message.setDraft(topic.getDraft());
        log.debug("createDiscussionForumTopic executed");
        return message;
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

    public PrivateTopic savePrivateForumTopic(PrivateTopic topic){
    	return savePrivateForumTopic(topic, getCurrentUser());
    }

    public PrivateTopic savePrivateForumTopic(PrivateTopic topic, String userId) {
    	return savePrivateForumTopic(topic, userId, getContextId());
    }

    public PrivateTopic savePrivateForumTopic(PrivateTopic topic, String userId, String siteId) {
    	boolean isNew = topic.getId() == null;

    	topic.setModified(new Date());
    	if(userId != null){
    		topic.setModifiedBy(userId);
    	}
    	Session session = sessionFactory.getCurrentSession();
    	PrivateTopic mergedTopic = session.merge(topic);

    	if (isNew) {
    		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FOLDER_ADD, getEventMessage(mergedTopic, siteId), false));
    	} else {
    		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FOLDER_REVISE, getEventMessage(mergedTopic, siteId), false));
    	}

    	log.debug("savePrivateForumTopic executed with forumId: " + mergedTopic.getId());

    	return mergedTopic;
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
        Session session = sessionFactory.getCurrentSession();
        topic = session.merge(topic);

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
        forum = (DiscussionForum) getForumById(true, id);
        eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_FORUM_REMOVE,
                getEventMessage(forum, getForumContext(forum)), false));

        Session session = sessionFactory.getCurrentSession();

        List<Topic> topics = getTopicsByIdWithMessages(id);
        for (Topic topic : topics) {
            List<Message> messages = topic.getMessages();
            for(Message message:messages){
                List<MessageMoveHistory> moveHistory = getMoveHistoryForMessageId(message.getId());
                for (MessageMoveHistory messageHistory: moveHistory){
                    session.remove(messageHistory);
                }
                List attachmentsMessage =  (List<Attachment>) message.getAttachments();
                if (!attachmentsMessage.isEmpty()) {
                    deleteAttachments(attachmentsMessage);
                }
            }
            List attachmentsTopic =  topic.getAttachments();
            if (!attachmentsTopic.isEmpty()) {
                deleteAttachments(attachmentsTopic);
            }

            //unread status
            List<UnreadStatus> statuses = getUnreadStatusesForTopic(topic.getId());
            statuses.forEach(session::remove);

            forum.removeTopic(topic);
            Topic topicTmp = session.merge(topic);
            session.remove(topicTmp);
        }

        List attachmentsForum =  forum.getAttachments();
        if (!attachmentsForum.isEmpty()) {
            deleteAttachments(attachmentsForum);
        }
        
        Area area = forum.getArea();
        area.removeDiscussionForum(forum);
        DiscussionForum forumTmp = session.merge(forum);
        session.merge(area);
        session.remove(forumTmp);

        log.debug("deleteDiscussionForum executed with forumId: " + id);
    }

    private void deleteAttachments(List attachments) {
        for(Attachment attachment: (List<Attachment>) attachments) {
            try {
                contentHostingService.removeResource( attachment.getAttachmentId());
            } catch  (PermissionException  | InUseException | TypeException | IdUnusedException e) {
                log.warn("Could not delete attachment with id {} due to {}", attachment.getAttachmentId(), e.toString());
            }
        }
    }

    private List<MessageMoveHistory> getMoveHistoryForMessageId(Long messageId) {
         Session session = sessionFactory.getCurrentSession();
         CriteriaBuilder cb = session.getCriteriaBuilder();

         CriteriaQuery<MessageMoveHistory> cq = cb.createQuery(MessageMoveHistory.class);
         Root<MessageMoveHistory> history = cq.from(MessageMoveHistory.class);

         cq.select(history)
           .where(cb.equal(history.get("messageId"), messageId));

         return session.createQuery(cq).getResultList();
    }

    private List<UnreadStatus> getUnreadStatusesForTopic(Long topicId) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<UnreadStatus> cq = cb.createQuery(UnreadStatus.class);
        Root<UnreadStatus> status = cq.from(UnreadStatus.class);

        cq.select(status)
          .where(cb.equal(status.get("topicId"), topicId));

        return session.createQuery(cq).getResultList();
    }

    
    public Area getAreaByContextIdAndTypeId(final String typeId) {
        log.debug("getAreaByContextIdAndTypeId executing for current user: " + getCurrentUser());
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Area> cq = cb.createQuery(Area.class);
        Root<Area> area = cq.from(Area.class);
        area.fetch("membershipItemSet", JoinType.LEFT);
        area.fetch("hiddenGroups", JoinType.LEFT);

        cq.select(area)
          .where(cb.equal(area.get("contextId"), getContextId()),
                 cb.equal(area.get("typeUuid"), typeId));

        return session.createQuery(cq).uniqueResult();
    }
    
    /**
     * Delete a discussion forum topic
     */
    public void deleteDiscussionForumTopic(DiscussionTopic topic) {
        long id = topic.getId().longValue();
        eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_REMOVE, getEventMessage(topic), false));
        Session session = sessionFactory.getCurrentSession();
        try {
            session.evict(topic);
        } catch (Exception e) {
            log.error("could not evict topic: " + topic.getId(), e);
        }

        List<Message> messages = topic.getMessages();
        for(Message message:messages){
            List<MessageMoveHistory> moveHistory = getMoveHistoryForMessageId(message.getId());
            for (MessageMoveHistory messageHistory: moveHistory){
                session.remove(messageHistory);
            }
            List attachmentsMessage =  (List<Attachment>) message.getAttachments();
            if (!attachmentsMessage.isEmpty()) {
                deleteAttachments(attachmentsMessage);
            }
        }

        List attachmentsTopic =  topic.getAttachments();
        if (!attachmentsTopic.isEmpty()) {
            deleteAttachments(attachmentsTopic);
        }

        //unread status
        List<UnreadStatus> statuses = getUnreadStatusesForTopic(topic.getId());
        statuses.forEach(session::remove);
        
        Topic finder = getTopicById(true, topic.getId());
        BaseForum forum = finder.getBaseForum();
        forum.removeTopic(topic);
        session.merge(forum);
        
        Topic topicTmp = session.merge(topic);
        session.remove(topicTmp);
        log.debug("deleteDiscussionForumTopic executed with topicId: " + id);
    }

    /**
     * Delete an open forum topic
     */
    public void deleteOpenForumTopic(OpenTopic topic) {
        eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_FORUM_REMOVE, getEventMessage(topic), false));
        Session session = sessionFactory.getCurrentSession();
        session.remove(topic);
        
        log.debug("deleteOpenForumTopic executed with forumId: " + topic.getId());
    }

    /**
     * Delete a private forum topic
     */
    public void deletePrivateForumTopic(PrivateTopic topic) {
        eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_FOLDER_REMOVE, getEventMessage(topic), false));
        Session session = sessionFactory.getCurrentSession();
        session.remove(topic);
        
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

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Boolean> cq = cb.createQuery(Boolean.class);
        Root<OpenForumImpl> forum = cq.from(OpenForumImpl.class);

        cq.select(forum.get("locked"))
          .where(cb.equal(forum.get("id"), id));

        Boolean result = session.createQuery(cq).uniqueResult();
        return result != null && result.booleanValue();
    }
    
    
    public List searchTopicMessages(final Long topicId, final String searchText) {
        if (topicId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("searchTopicMessages executing with topicId: " + topicId);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Message> cq = cb.createQuery(Message.class);
        Root<MessageImpl> message = cq.from(MessageImpl.class);

        String pattern = "%" + searchText + "%";

        cq.select(message)
          .where(cb.and(
                  cb.equal(message.get("topic").get("id"), topicId),
                  cb.equal(message.get("draft"), false),
                  cb.or(
                          cb.like(message.get("author"), pattern),
                          cb.like(message.get("title"), pattern),
                          cb.like(message.get("body"), pattern)
                  )
          ));

        return session.createQuery(cq).getResultList();
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

    private String getForumContext(DiscussionForum forum) {
        return Optional.ofNullable(forum)
                .map(DiscussionForum::getArea)
                .map(Area::getContextId)
                .orElseGet(this::getContextId);
    }

    private String getEventMessage(Object object) {
        return getEventMessage(object, getContextId());
    }

    private String getEventMessage(Object object, String context) {
        String eventMessagePrefix = "";

        try {
            // TODO: How to determine what prefix to put on event message
            Site site = siteService.getSite(context);
            if (site != null && isToolInSite(site, DiscussionForumService.MESSAGE_CENTER_ID))
                eventMessagePrefix = "/messages&forums/site/";
            else if (site != null && isToolInSite(site, DiscussionForumService.MESSAGES_TOOL_ID))
                eventMessagePrefix = "/messages/site/";
            else
                eventMessagePrefix = "/forums/site/";
        }
        catch (IdUnusedException e) {
            log.debug("IdUnusedException attempting to get site with id: " + context);

            eventMessagePrefix = "/messages&forums/";
        }
        catch (RuntimeException e) {
            log.warn("Could not determine forum event prefix for context {}", context, e);
            eventMessagePrefix = "/forums/site/";
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

			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<OpenForumImpl> cq = cb.createQuery(OpenForumImpl.class);
			Root<OpenForumImpl> forum = cq.from(OpenForumImpl.class);

			Join<Object, Object> area = forum.join("area");
			forum.fetch("attachmentsSet", JoinType.LEFT);
			Fetch<Object, Object> topicFetch = forum.fetch("topicsSet", JoinType.LEFT);
			topicFetch.fetch("messagesSet", JoinType.LEFT);
			topicFetch.fetch("attachmentsSet", JoinType.LEFT);

			cq.select(forum).distinct(true);
			cq.where(
				cb.equal(forum.get("typeUuid"), cb.parameter(String.class, "typeUuid")),
				cb.equal(area.get("contextId"), cb.parameter(String.class, "contextId"))
					);

			session.createQuery(cq)
				.setParameter("typeUuid", typeUuid)
				.setParameter("contextId", contextId)
				.list();
			
			List temp = session.createQuery(cq).getResultList();

			BaseForum tempForum = null;
			Set resultSet = new HashSet();

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

	public List getForumByTypeAndContextWithTopicsMembership(final String typeUuid, final String contextId) {
		if (typeUuid == null || contextId == null) {
			throw new IllegalArgumentException("Null Argument");
		}
		
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OpenForumImpl> cq = cb.createQuery(OpenForumImpl.class);

		Root<OpenForumImpl> forum = cq.from(OpenForumImpl.class);
		Join<OpenForumImpl, AreaImpl> area = forum.join("area");

		cq.select(forum)
			.where(cb.and(
				cb.equal(forum.get("typeUuid"), typeUuid),
				cb.equal(area.get("contextId"), contextId)
			));

		final List<BaseForum> resultList = new ArrayList<>(session.createQuery(cq).getResultList());

		Collections.sort(resultList, FORUM_SORT_INDEX_CREATED_DATE_COMPARATOR_DESC);

		// Now that the list is sorted, lets index the forums
		for (int sortIndex = 0; sortIndex < resultList.size(); sortIndex++) {
			resultList.get(sortIndex).setSortIndex(Integer.valueOf(sortIndex + 1));
		}
		return resultList;
	}
	
		public int getNumModTopicCurrentUserHasModPermForWithPermissionLevel(final List<String> membershipList, final List<Topic> moderatedTopics)
		{
			if (membershipList == null) {
	            log.error("getNumModTopicCurrentUserHasModPermForWithPermissionLevel failed with membershipList: null");
	            throw new IllegalArgumentException("Null Argument");
	        }

	        log.debug("getNumModTopicCurrentUserHasModPermForWithPermissionLevel executing with membershipItems: {}", membershipList);

	        // hibernate will not like an empty list so return 0
	        if (membershipList.isEmpty()) return 0;

	        Session session = sessionFactory.getCurrentSession();
	        CriteriaBuilder cb = session.getCriteriaBuilder();
	        CriteriaQuery<Long> cq = cb.createQuery(Long.class);

	        Root<TopicImpl> topic = cq.from(TopicImpl.class);
	        Root<PermissionLevelImpl> pl = cq.from(PermissionLevelImpl.class);

	        Join<TopicImpl, DBMembershipItemImpl> membershipItem = topic.join("membershipItemSet");

	        Predicate topicInList = topic.in(moderatedTopics);
	        Predicate membershipInList = membershipItem.get("name").in(membershipList);
	        Predicate moderatePostings = cb.isTrue(pl.get("moderatePostings"));
	        Predicate permissionLevelMatches = cb.equal(membershipItem.get("permissionLevel").get("id"), pl.get("id"));

	        cq.select(cb.count(topic))
	          .where(cb.and(topicInList, membershipInList, moderatePostings, permissionLevelMatches));

	        Long result = session.createQuery(cq).uniqueResult();

	        return result != null ? result.intValue() : 0;
		}
		
		public int getNumModTopicCurrentUserHasModPermForWithPermissionLevelName(final List<String> membershipList, final List<Topic> moderatedTopics)
		{
			if (membershipList == null) {
	            log.error("getNumModTopicCurrentUserHasModPermForWithPermissionLevelName failed with membershipList: null");
	            throw new IllegalArgumentException("Null Argument");
	        }

	        log.debug("getNumModTopicCurrentUserHasModPermForWithPermissionLevelName executing with membershipItems: " + membershipList);

	        // hibernate will not like an empty list so return 0
	        if (membershipList.isEmpty()) return 0;

	        Session session = sessionFactory.getCurrentSession();
	        CriteriaBuilder cb = session.getCriteriaBuilder();
	        CriteriaQuery<Long> cq = cb.createQuery(Long.class);

	        Root<TopicImpl> topic = cq.from(TopicImpl.class);
	        Root<PermissionLevelImpl> pl = cq.from(PermissionLevelImpl.class);

	        Join<TopicImpl, DBMembershipItemImpl> membershipItem = topic.join("membershipItemSet");

	        Predicate topicInList = topic.in(moderatedTopics);
	        Predicate membershipInList = membershipItem.get("name").in(membershipList);
	        Predicate moderatePostings = cb.isTrue(pl.get("moderatePostings"));
	        Predicate notCustomType = cb.notEqual(pl.get("typeUuid"), cb.parameter(String.class, "customTypeUuid"));
	        Predicate nameMatches = cb.equal(pl.get("name"), membershipItem.get("permissionLevelName"));

	        cq.select(cb.count(topic))
	          .where(cb.and(topicInList, membershipInList, moderatePostings, notCustomType, nameMatches));

	        Long result =  session.createQuery(cq)
	                .setParameter("customTypeUuid", typeManager.getCustomLevelType())
	                .uniqueResult();

	        return result != null ? result.intValue() : 0;
		}
		
		public BaseForum getForumByIdWithTopicsAttachmentsAndMessages(final Long forumId)
		{
			if (forumId == null) {
				throw new IllegalArgumentException("Null Argument");
			}      

			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<OpenForumImpl> cq = cb.createQuery(OpenForumImpl.class);
			Root<OpenForumImpl> forum = cq.from(OpenForumImpl.class);

			Fetch<OpenForumImpl, Object> topic = forum.fetch("topicsSet", JoinType.LEFT);
			topic.fetch("messagesSet", JoinType.LEFT);
			topic.fetch("attachmentsSet", JoinType.LEFT);

			cq.select(forum)
				.where(cb.equal(forum.get("id"), cb.parameter(Long.class, "id")));

			BaseForum bForum = session.createQuery(cq)
					.setParameter("id", forumId)
					.uniqueResult();

		      
		      if (bForum != null){
		        Hibernate.initialize(bForum.getAttachmentsSet());
		      }
		      
		      return bForum;      
		}
		
		public Topic getTopicByIdWithMemberships(final Long topicId) {

			if (topicId == null) {
				throw new IllegalArgumentException("Null Argument");
			}

			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<TopicImpl> cq = cb.createQuery(TopicImpl.class);
			Root<TopicImpl> topic = cq.from(TopicImpl.class);

			Fetch<TopicImpl, Object> forum = topic.fetch("openForum", JoinType.LEFT);
			forum.fetch("area", JoinType.LEFT);
			topic.fetch("membershipItemSet", JoinType.LEFT);

			cq.select(topic)
				.where(cb.equal(topic.get("id"), cb.parameter(Long.class, "id")));

			return session.createQuery(cq)
				.setParameter("id", topicId)
				.uniqueResult();
		}

		public BaseForum getForumByIdWithMemberships(final Long forumId) {

			if (forumId == null) {
				throw new IllegalArgumentException("Null Argument");
			}

			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<OpenForumImpl> cq = cb.createQuery(OpenForumImpl.class);
			Root<OpenForumImpl> forum = cq.from(OpenForumImpl.class);

			forum.fetch("area", JoinType.LEFT);
			forum.fetch("membershipItemSet", JoinType.LEFT);

			cq.select(forum)
				.where(cb.equal(forum.get("id"), cb.parameter(Long.class, "id")));

			return session.createQuery(cq)
				.setParameter("id", forumId)
				.uniqueResult();
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

			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<TopicImpl> cq = cb.createQuery(TopicImpl.class);
			Root<TopicImpl> topic = cq.from(TopicImpl.class);
			Join<Object, Object> forum = topic.join("openForum");
			Join<Object, Object> area = forum.join("area");

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(area.get("contextId"), cb.parameter(String.class, "contextId")));
			if (anonymousOnly) {
				predicates.add(cb.isTrue(topic.get("postAnonymous")));
			}

			cq.select(topic).where(predicates.toArray(new Predicate[0]));

			List resultSet = session.createQuery(cq)
				.setParameter("contextId", contextId)
				.list();

			List<Topic> topicList = new ArrayList<>();
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

			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<Long> cq = cb.createQuery(Long.class);
			Root<DBMembershipItemImpl> membership = cq.from(DBMembershipItemImpl.class);

			cq.select(cb.count(membership));
				cq.where(
					cb.equal(membership.get("topic").get("id"), cb.parameter(Long.class, "id")),
					cb.equal(membership.get("name"), cb.parameter(String.class, "roleName")),
					cb.equal(membership.get("permissionLevelName"), cb.parameter(String.class, "permissionLevelName"))
				);

			Long count = session.createQuery(cq)
				.setParameter("id", topicId)
				.setParameter("roleName", roleName)
				.setParameter("permissionLevelName", permissionName)
				.uniqueResult();

			return count != null && count > 0;
		}
		
		public List<String> getAllowedGroupForRestrictedTopic(final Long topicId, final String permissionName) {
			if (topicId == null) {
				throw new IllegalArgumentException("Null Argument");
			}

			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<String> cq = cb.createQuery(String.class);
			Root<DBMembershipItemImpl> membership = cq.from(DBMembershipItemImpl.class);

			cq.select(membership.get("name"));
			cq.where(
				cb.equal(membership.get("topic").get("id"), cb.parameter(Long.class, "id")),
				cb.equal(membership.get("type"), 3),
				cb.equal(membership.get("permissionLevelName"), cb.parameter(String.class, "permissionLevelName"))
			);

			return session.createQuery(cq)
					.setParameter("id", topicId)
					.setParameter("permissionLevelName", permissionName)
					.setCacheable(true)
					.list();
		}

		public List<String> getAllowedGroupForRestrictedForum(final Long forumId, final String permissionName) {
			if (forumId == null) {
				throw new IllegalArgumentException("Null Argument");
			}

			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<String> cq = cb.createQuery(String.class);
			Root<DBMembershipItemImpl> membership = cq.from(DBMembershipItemImpl.class);

			cq.select(membership.get("name"));
			cq.where(
				cb.equal(membership.get("forum").get("id"), cb.parameter(Long.class, "id")),
				cb.equal(membership.get("type"), 3),
				cb.equal(membership.get("permissionLevelName"), cb.parameter(String.class, "permissionLevelName"))
			);

			return session.createQuery(cq)
				.setParameter("id", forumId)
				.setParameter("permissionLevelName", permissionName)
				.setCacheable(true)
				.list();
		}
}
