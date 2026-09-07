/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/MessageForumsMessageManagerImpl.java $
 * $Id: MessageForumsMessageManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DraftRecipient;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.MessageMoveHistory;
import org.sakaiproject.api.app.messageforums.OpenForum;
import org.sakaiproject.api.app.messageforums.OpenTopic;
import org.sakaiproject.api.app.messageforums.PermissionLevel;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.UnreadStatus;
import org.sakaiproject.api.app.messageforums.UserStatistics;
import org.sakaiproject.api.app.messageforums.cover.SynopticMsgcntrManagerCover;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AreaImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AttachmentImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DBMembershipItemImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageMoveHistoryImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.OpenForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.OpenTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PermissionLevelImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateMessageRecipientImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.TopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.UnreadStatusImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.Util;
import org.sakaiproject.component.app.messageforums.exception.LockedException;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class MessageForumsMessageManagerImpl implements MessageForumsMessageManager {

    //private static final String QUERY_BY_MESSAGE_ID = "findMessageById";
    //private static final String QUERY_ATTACHMENT_BY_ID = "findAttachmentById";
    private static final String QUERY_BY_MESSAGE_ID_WITH_ATTACHMENTS = "findMessageByIdWithAttachments";
    private static final String QUERY_COUNT_BY_READ = "findReadMessageCountByTopicId";
    private static final String QUERY_COUNT_BY_AUTHORED = "findAuhtoredMessageCountByTopicId";
    private static final String QUERY_MESSAGE_COUNTS_FOR_MAIN_PAGE = "findMessageCountsForMainPage";
    private static final String QUERY_READ_MESSAGE_COUNTS_FOR_MAIN_PAGE = "findReadMessageCountsForMainPage";
    private static final String QUERY_BY_TOPIC_ID = "findMessagesByTopicId";
    private static final String QUERY_COUNT_VIEWABLE_BY_TOPIC_ID = "findViewableMessageCountByTopicIdByUserId";
    private static final String QUERY_COUNT_VIEWABLE_BY_TOPIC_ID_BY_USERS = "findViewableMessageCountByTopicIdByUserIds";
    private static final String QUERY_COUNT_READ_VIEWABLE_BY_TOPIC_ID = "findReadViewableMessageCountByTopicIdByUserId";
    private static final String QUERY_UNREAD_STATUS = "findUnreadStatusForMessage";
    private static final String QUERY_CHILD_MESSAGES = "finalAllChildMessages";
    private static final String QUERY_READ_STATUS_WITH_MSGS_USER = "findReadStatusByMsgIds";
    private static final String QUERY_FIND_PENDING_MSGS_BY_CONTEXT_AND_USER_AND_PERMISSION_LEVEL = "findAllPendingMsgsByContextByMembershipByPermissionLevel";
    private static final String QUERY_FIND_PENDING_MSGS_BY_CONTEXT_AND_USER_AND_PERMISSION_LEVEL_NAME = "findAllPendingMsgsByContextByMembershipByPermissionLevelName";
    private static final String QUERY_FIND_PENDING_MSGS_BY_TOPICID = "findPendingMsgsByTopicId";
    private static final String QUERY_UNDELETED_MSG_BY_TOPIC_ID = "findUndeletedMessagesByTopicId";
    private static final String QUERY_MOVED_MESSAGES_BY_TOPICID = "findMovedMessagesByTopicId";
    private static final String QUERY_MOVED_HISTORY_BY_MESSAGEID = "findMovedHistoryByMessageId";
    //private static final String ID = "id";

    // Oracle's 1000 'in' clause limit
    private static final int MAX_IN_CLAUSE_SIZE = 1000;

    private static final String MESSAGECENTER_HELPER_TOOL_ID = "sakai.messageforums.helper";

    private IdManager idManager;                      

    private MessageForumsTypeManager typeManager;

    private PermissionLevelManager permissionLevelManager;

    private SessionManager sessionManager;

    private EventTrackingService eventTrackingService;
    
    private ContentHostingService contentHostingService;

    private SiteService siteService;
    
    private ToolManager toolManager;
    
    @Getter
    @Setter
    private SessionFactory sessionFactory;
    
    public void init() {
       log.info("init()");
        ;
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

    public void setPermissionLevelManager(PermissionLevelManager permissionLevelManager) { this.permissionLevelManager = permissionLevelManager; }
    
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setIdManager(IdManager idManager) {
        this.idManager = idManager;
    }

    public IdManager getIdManager() {
        return idManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
    
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }
 
    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of discussion forum messages grouped by site for sites with
     * 		Forum topics that don't have membership items in the db
     */
    public List<Object []> findDiscussionForumMessageCountsForTopicsWithMissingPermsForAllSites(final List<String> siteList) {
    	if (siteList == null) {
            log.error("findDiscussionForumMessageCountsForTopicsWithMissingPermsForAllSites failed with null site list.");
            throw new IllegalArgumentException("Null Argument");
    	}	
        
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<MessageImpl> messages = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = messages.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");

        Subquery<Long> subquery = cq.subquery(Long.class);
        Root<DBMembershipItemImpl> dbmi = subquery.from(DBMembershipItemImpl.class);
        Join<DBMembershipItemImpl, TopicImpl> dbmiTopic = dbmi.join("topic");
        subquery.select(dbmiTopic.get("id")).distinct(true)
                .where(cb.equal(dbmiTopic, topic));

        Predicate approvedOrOwner = cb.or(
            cb.isTrue(messages.get("approved")),
            cb.equal(messages.get("createdBy"), getCurrentUser())
        );

        Predicate topicNotInSubquery = cb.not(topic.get("id").in(subquery));

        cq.select(cb.array(area.get("contextId"), cb.count(messages)))
          .where(
              area.get("contextId").in(siteList),
              cb.isFalse(forum.get("draft")),
              cb.isFalse(topic.get("draft")),
              cb.isFalse(messages.get("draft")),
              cb.isFalse(messages.get("deleted")),
              approvedOrOwner,
              topicNotInSubquery
           )
           .groupBy(area.get("contextId"));

        return session.createQuery(cq).list();
    }
    
    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of discussion forum messages grouped by site for sites with
     * 		Forum topics that don't have membership items in the db
     */
    public List<Object []> findDiscussionForumReadMessageCountsForTopicsWithMissingPermsForAllSites(final List<String> siteList) {
    	if (siteList == null) {
            log.error("findDiscussionForumReadMessageCountsForTopicsWithMissingPermsForAllSites failed with null site list.");
            throw new IllegalArgumentException("Null Argument");
    	}	

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<MessageImpl> messages = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = messages.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");
        Root<UnreadStatusImpl> us = cq.from(UnreadStatusImpl.class);

        Subquery<Long> subquery = cq.subquery(Long.class);
        Root<DBMembershipItemImpl> dbmi = subquery.from(DBMembershipItemImpl.class);
        Join<DBMembershipItemImpl, TopicImpl> dbmiTopic = dbmi.join("topic");
        subquery.select(dbmiTopic.get("id")).distinct(true)
                .where(cb.equal(dbmiTopic, topic));

        Predicate approvedOrOwner = cb.or(
                cb.isTrue(messages.get("approved")),
                cb.equal(messages.get("createdBy"), getCurrentUser())
        );

        Predicate topicNotInSubquery = cb.not(topic.get("id").in(subquery));

        cq.select(cb.array(area.get("contextId"), cb.count(messages)))
          .where(
              area.get("contextId").in(siteList),
              cb.isFalse(forum.get("draft")),
              cb.isFalse(topic.get("draft")),
              cb.isFalse(messages.get("draft")),
              cb.isFalse(messages.get("deleted")),
              cb.equal(us.get("userId"), getCurrentUser()),
              cb.isTrue(us.get("read")),
              cb.equal(messages.get("id"), us.get("messageId")),
              approvedOrOwner,
              topicNotInSubquery
          )
          .groupBy(area.get("contextId"));

        return session.createQuery(cq).list();
    }
    
    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of discussion forum messages grouped by site
     */
    public List findDiscussionForumMessageCountsForAllSitesByPermissionLevelId(final List siteList, final List roleList) {
    	if (siteList == null) {
            log.error("findDiscussionForumMessageCountsForAllSitesByPermissionLevelId failed with null site list.");
            throw new IllegalArgumentException("Null Argument");
    	}	

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<MessageImpl> messages = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = messages.join("topic");
        Join<TopicImpl, DBMembershipItemImpl> membershipItem = topic.join("membershipItemSet");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");
        Root<PermissionLevelImpl> pl = cq.from(PermissionLevelImpl.class);

        Predicate approvedOrOwner = cb.or(
                cb.isTrue(messages.get("approved")),
                cb.equal(messages.get("createdBy"), getCurrentUser())
        );

        Predicate readAndAllowed = cb.and(
                cb.isTrue(pl.get("read")),
                cb.or(cb.isTrue(pl.get("moderatePostings")), approvedOrOwner)
        );

        cq.select(cb.array(area.get("contextId"), membershipItem.get("name"), cb.count(messages)))
          .where(
              area.get("contextId").in(siteList),
              cb.isFalse(forum.get("draft")),
              cb.isFalse(topic.get("draft")),
              cb.isFalse(messages.get("draft")),
              cb.isFalse(messages.get("deleted")),
              membershipItem.get("name").in(roleList),
              cb.equal(membershipItem.get("permissionLevel").get("id"), pl.get("id")),
              readAndAllowed
          )
          .groupBy(area.get("contextId"), membershipItem.get("name"));

        return session.createQuery(cq).list();
    }

    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of discussion forum messages grouped by site
     */
    public List findDiscussionForumMessageCountsForAllSitesByPermissionLevelName(final List siteList, final List roleList) {
    	if (siteList == null) {
            log.error("findDiscussionForumMessageCountsForAllSitesByPermissionLevelName failed with null site list.");
            throw new IllegalArgumentException("Null Argument");
    	}	
        
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, DBMembershipItemImpl> membershipItem = topic.join("membershipItemSet");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");

        Root<PermissionLevelImpl> pl = cq.from(PermissionLevelImpl.class);

        String userId = getCurrentUser();
        String customTypeUuid = typeManager.getCustomLevelType();

        cq.select(cb.array(area.get("contextId"), membershipItem.get("name"), cb.count(message)))
          .where(
              area.get("contextId").in(siteList),
              cb.isFalse(forum.get("draft")),
              cb.isFalse(topic.get("draft")),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              membershipItem.get("name").in(roleList),
              cb.notEqual(pl.get("typeUuid"), customTypeUuid),
              cb.equal(pl.get("name"), membershipItem.get("permissionLevelName")),
              cb.isTrue(pl.get("read")),
              cb.or(
                  cb.isTrue(pl.get("moderatePostings")),
                  cb.isTrue(message.get("approved")),
                  cb.equal(message.get("createdBy"), userId)
              )
          )
          .groupBy(area.get("contextId"), membershipItem.get("name"));

        return session.createQuery(cq).getResultList();
    }

    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of read discussion forum messages grouped by site
     */
    public List findDiscussionForumReadMessageCountsForAllSitesByPermissionLevelId(final List siteList, final List roleList) {
        
        /*HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery("findDiscussionForumReadMessageCountsForAllSitesByPermissionLevelId");
            q.setParameterList("siteList", siteList);
            q.setParameterList("roleList", roleList);
            q.setParameter("userId", getCurrentUser());
            return q.list();
        };
        
        return getHibernateTemplate().execute(hcb);*/

        return null;
    }

    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of read discussion forum messages grouped by site
     */
    public List findDiscussionForumReadMessageCountsForAllSitesByPermissionLevelName(final List siteList, final List roleList) {

        /*HibernateCallback<List> hcb = session -> {
            Query q = session.getNamedQuery("findDiscussionForumReadMessageCountsForAllSitesByPermissionLevelName");
            q.setParameterList("siteList", siteList);
            q.setParameterList("roleList", roleList);
            q.setParameter("userId", getCurrentUser());
            q.setParameter("customTypeUuid", typeManager.getCustomLevelType());
            return q.list();
        };
        
        return getHibernateTemplate().execute(hcb);*/

        return null;
    }
    
    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of discussion forum messages grouped by topics within a site
     * 		Used by sites that are grouped
     */
    public List findDiscussionForumMessageCountsForGroupedSitesByTopic(final List siteList, final List roleList) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, DBMembershipItemImpl> membershipItem = topic.join("membershipItemSet");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");

        Root<PermissionLevelImpl> pl = cq.from(PermissionLevelImpl.class);

        String userId = getCurrentUser();
        String customTypeUuid = typeManager.getCustomLevelType();

        cq.select(cb.array(area.get("contextId"), topic.get("id"), membershipItem.get("name"), cb.count(message)))
          .where(
              area.get("contextId").in(siteList),
              membershipItem.get("name").in(roleList),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              cb.isFalse(forum.get("draft")),
              cb.isFalse(topic.get("draft")),
              cb.or(
                  cb.and(
                      cb.notEqual(pl.get("typeUuid"), customTypeUuid),
                      cb.equal(pl.get("name"), membershipItem.get("permissionLevelName"))
                  ),
                  cb.equal(membershipItem.get("permissionLevel").get("id"), pl.get("id"))
              ),
              cb.isTrue(pl.get("read")),
              cb.or(
                  cb.isTrue(pl.get("moderatePostings")),
                  cb.isTrue(message.get("approved")),
                  cb.equal(message.get("createdBy"), userId)
              )
          )
          .groupBy(area.get("contextId"), topic.get("id"), membershipItem.get("name"));

        return session.createQuery(cq).getResultList();
    }

    /**
     * FOR SYNOPTIC TOOL:
     * 		Returns the count of discussion forum messages grouped by topics within a site
     * 		Used by sites that are grouped
     */
    public List findDiscussionForumReadMessageCountsForGroupedSitesByTopic(final List siteList, final List roleList) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, DBMembershipItemImpl> membershipItem = topic.join("membershipItemSet");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");

        Root<UnreadStatusImpl> us = cq.from(UnreadStatusImpl.class);
        Root<PermissionLevelImpl> pl = cq.from(PermissionLevelImpl.class);

        String userId = getCurrentUser();
        String customTypeUuid = typeManager.getCustomLevelType();

        cq.select(cb.array(area.get("contextId"), topic.get("id"), membershipItem.get("name"), cb.count(message)))
          .where(
              area.get("contextId").in(siteList),
              membershipItem.get("name").in(roleList),
              cb.equal(us.get("userId"), userId),
              cb.isTrue(us.get("read")),
              cb.equal(message.get("id"), us.get("messageId")),
              cb.isFalse(message.get("deleted")),
              cb.isFalse(forum.get("draft")),
              cb.isFalse(topic.get("draft")),
              cb.or(
                  cb.and(
                      cb.notEqual(pl.get("typeUuid"), customTypeUuid),
                      cb.equal(pl.get("name"), membershipItem.get("permissionLevelName"))
                  ),
                  cb.equal(membershipItem.get("permissionLevel").get("id"), pl.get("id"))
              ),
              cb.isTrue(pl.get("read")),
              cb.or(
                  cb.isTrue(pl.get("moderatePostings")),
                  cb.isTrue(message.get("approved")),
                  cb.equal(message.get("createdBy"), userId)
              )
          )
          .groupBy(area.get("contextId"), topic.get("id"), membershipItem.get("name"));

        return session.createQuery(cq).getResultList();
    }

    /**
     * FOR STATISTICS TOOL:
     * 		Returns the number of read messages by topic for specified user
     */
    
    public int findAuhtoredMessageCountByTopicIdByUserId(final Long topicId, final String userId){
    	if (topicId == null || userId == null) {
            log.error("findAuthoredMessageCountByTopicIdByUserId failed with topicId: " + topicId + 
            			" and userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findAuthoredMessageCountByTopicIdByUserId executing with topicId: " + topicId + 
        				" and userId: " + userId);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);

        cq.select(cb.count(message))
          .where(
              cb.equal(message.get("topic").get("id"), topicId),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              cb.equal(message.get("createdBy"), userId)
          );

        return session.createQuery(cq).uniqueResult().intValue();
    }
    
    public int findAuthoredMessageCountForStudent(final String userId) {
    	if (userId == null) {
    		log.error("findAuthoredMessageCountForStudentInSite failed with a null userId");
    		throw new IllegalArgumentException("userId cannot be null");
    	}
    	
    	if (log.isDebugEnabled()) log.debug("findAuthoredMessageCountForStudentInSite executing with userId: " + userId);

    	Session session = sessionFactory.getCurrentSession();
    	CriteriaBuilder cb = session.getCriteriaBuilder();
    	CriteriaQuery<Long> cq = cb.createQuery(Long.class);

    	Root<MessageImpl> message = cq.from(MessageImpl.class);
    	Join<MessageImpl, TopicImpl> topic = message.join("topic");
    	Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
    	Join<OpenForumImpl, AreaImpl> area = forum.join("area");

    	cq.select(cb.count(message))
    	  .where(
    	      cb.equal(area.get("contextId"), getContextId()),
    	      cb.isFalse(message.get("draft")),
    	      cb.isFalse(message.get("deleted")),
    	      cb.equal(message.get("createdBy"), userId)
    	  );

    	return session.createQuery(cq).uniqueResult().intValue();
    }

    public List<Object[]> findAuthoredNewMessageCountForAllStudents() {
        if (log.isDebugEnabled()) log.debug("findAuthoredNewMessageCountForAllStudents executing");

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");

        cq.select(cb.array(message.get("createdBy"), cb.count(message)))
          .where(
              cb.equal(area.get("contextId"), getContextId()),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              cb.isNull(message.get("inReplyTo"))
          )
          .groupBy(message.get("createdBy"));

        return session.createQuery(cq).getResultList();
    }

    public List<Object[]> findAuthoredRepliesMessageCountForAllStudents() {
        if (log.isDebugEnabled()) log.debug("findAuthoredRepliesMessageCountForAllStudents executing");

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");

        cq.select(cb.array(message.get("createdBy"), cb.count(message)))
          .where(
              cb.equal(area.get("contextId"), getContextId()),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              cb.isNotNull(message.get("inReplyTo"))
          )
          .groupBy(message.get("createdBy"));

        return session.createQuery(cq).getResultList();
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.api.app.messageforums.MessageForumsMessageManager#findAuthoredMessagesForStudent(java.lang.String)
     */
    public List<Message> findAuthoredMessagesForStudent(final String studentId) {
      if (log.isDebugEnabled()) log.debug("findReadMessagesForCurrentStudent()");

      Session session = sessionFactory.getCurrentSession();
      CriteriaBuilder cb = session.getCriteriaBuilder();
      CriteriaQuery<MessageImpl> cq = cb.createQuery(MessageImpl.class);

      Root<MessageImpl> message = cq.from(MessageImpl.class);
      Fetch<MessageImpl, TopicImpl> topicFetch = message.fetch("topic");
      Fetch<TopicImpl, OpenForumImpl> openForumFetch = topicFetch.fetch("openForum");

      Join<MessageImpl, TopicImpl> topic = (Join<MessageImpl, TopicImpl>) topicFetch;
      Join<TopicImpl, OpenForumImpl> openForum = (Join<TopicImpl, OpenForumImpl>) openForumFetch;
      Join<OpenForumImpl, AreaImpl> area = openForum.join("area");

      cq.select(message)
        .where(
            cb.equal(area.get("contextId"), getContextId()),
            cb.equal(message.get("createdBy"), studentId),
            cb.isFalse(message.get("draft")),
            cb.isFalse(message.get("deleted"))
        )
        .orderBy(cb.desc(message.get("created")));

      return (List<Message>) (List<?>) session.createQuery(cq).getResultList();
    }
    
    public List<UserStatistics> findAuthoredStatsForStudent(final String studentId) {
        if (log.isDebugEnabled()) log.debug("findAuthoredStatsForStudent()");
        
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");

        cq.select(cb.array(
                forum.get("title"),
                topic.get("title"),
                message.get("created"),
                message.get("title"),
                message.get("id"),
                topic.get("id"),
                forum.get("id")))
          .where(
              cb.equal(area.get("contextId"), getContextId()),
              cb.equal(message.get("createdBy"), studentId),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted"))
          );

        List<Object[]> results = session.createQuery(cq).getResultList();

        List<UserStatistics> returnList = new ArrayList<UserStatistics>();
        for(Object[] result : results){
      	  UserStatistics stat = new UserStatistics((String) result[0], (String) result[1], (Date) result[2], (String) result[3], 
      			  ((Integer) result[4]).toString(), ((Integer) result[5]).toString(), ((Integer) result[6]).toString(), studentId);
      	  returnList.add(stat);
        }
        return returnList;
      }
    
    public List<Message> findAuthoredMessagesForStudentByTopicId(final String studentId, final Long topicId) {
    	if (log.isDebugEnabled()) log.debug("findReadMessagesForCurrentStudentByTopicId()");

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MessageImpl> cq = cb.createQuery(MessageImpl.class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Fetch<MessageImpl, TopicImpl> topicFetch = message.fetch("topic");
        Fetch<TopicImpl, OpenForumImpl> openForumFetch = topicFetch.fetch("openForum");

        Join<MessageImpl, TopicImpl> topic = (Join<MessageImpl, TopicImpl>) topicFetch;
        Join<TopicImpl, OpenForumImpl> openForum = (Join<TopicImpl, OpenForumImpl>) openForumFetch;
        Join<OpenForumImpl, AreaImpl> area = openForum.join("area");

        cq.select(message)
          .where(
              cb.equal(area.get("contextId"), getContextId()),
              cb.equal(message.get("createdBy"), studentId),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              cb.equal(topic.get("id"), topicId)
          )
          .orderBy(cb.desc(message.get("created")));

        return (List<Message>) (List<?>) session.createQuery(cq).getResultList();
    }

    public List<UserStatistics> findAuthoredStatsForStudentByTopicId(final String studentId, final Long topicId) {
    	if (log.isDebugEnabled()) log.debug("findAuthoredStatsForStudentByTopicId()");

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");

        cq.select(cb.array(
                forum.get("title"),
                topic.get("title"),
                message.get("created"),
                message.get("title"),
                message.get("id"),
                topic.get("id"),
                forum.get("id")))
          .where(
              cb.equal(topic.get("id"), topicId),
              cb.equal(message.get("createdBy"), studentId),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted"))
          );

        List<Object[]> results = session.createQuery(cq).getResultList();

    	List<UserStatistics> returnList = new ArrayList<UserStatistics>();
    	for(Object[] result : results){
    		UserStatistics stat = new UserStatistics((String) result[0], (String) result[1], (Date) result[2], (String) result[3], 
    				((Integer) result[4]).toString(), ((Integer) result[5]).toString(), ((Integer) result[6]).toString(), studentId);
    		returnList.add(stat);
    	}
    	return returnList;
    }

    public List<Message> findAuthoredMessagesForStudentByForumId(final String studentId, final Long forumId) {
    	if (log.isDebugEnabled()) log.debug("findAuthoredMessagesForStudentByForumId()");

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MessageImpl> cq = cb.createQuery(MessageImpl.class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Fetch<MessageImpl, TopicImpl> topicFetch = message.fetch("topic");
        Fetch<TopicImpl, OpenForumImpl> openForumFetch = topicFetch.fetch("openForum");

        Join<MessageImpl, TopicImpl> topic = (Join<MessageImpl, TopicImpl>) topicFetch;
        Join<TopicImpl, OpenForumImpl> openForum = (Join<TopicImpl, OpenForumImpl>) openForumFetch;
        Join<OpenForumImpl, AreaImpl> area = openForum.join("area");

        cq.select(message)
          .where(
              cb.equal(area.get("contextId"), getContextId()),
              cb.equal(message.get("createdBy"), studentId),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              cb.equal(openForum.get("id"), forumId)
          )
          .orderBy(cb.desc(message.get("created")));

        return (List<Message>) (List<?>) session.createQuery(cq).getResultList();
    }
    
    public List<UserStatistics> findAuthoredStatsForStudentByForumId(final String studentId, final Long topicId) {
    	if (log.isDebugEnabled()) log.debug("findAuthoredStatsForStudentByForumId()");

    	Session session = sessionFactory.getCurrentSession();
    	CriteriaBuilder cb = session.getCriteriaBuilder();
    	CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

    	Root<MessageImpl> message = cq.from(MessageImpl.class);
    	Join<MessageImpl, TopicImpl> topic = message.join("topic");
    	Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");

    	cq.select(cb.array(
    	        forum.get("title"),
    	        topic.get("title"),
    	        message.get("created"),
    	        message.get("title"),
    	        message.get("id"),
    	        topic.get("id"),
    	        forum.get("id")))
    	  .where(
    	      cb.equal(forum.get("id"), topicId),
    	      cb.equal(message.get("createdBy"), studentId),
    	      cb.isFalse(message.get("draft")),
    	      cb.isFalse(message.get("deleted"))
    	  );

    	List<Object[]> results = session.createQuery(cq).getResultList();

    	List<UserStatistics> returnList = new ArrayList<UserStatistics>();
    	for(Object[] result : results){
    		UserStatistics stat = new UserStatistics((String) result[0], (String) result[1], (Date) result[2], (String) result[3], 
    				((Integer) result[4]).toString(), ((Integer) result[5]).toString(), ((Integer) result[6]).toString(), studentId);
    		returnList.add(stat);
    	}
    	return returnList;
    }
    
    public List<Object[]> findAuthoredMessageCountForAllStudents() {
    	if (log.isDebugEnabled()) log.debug("findAuthoredMessageCountForAllStudents executing");

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");

        cq.select(cb.array(message.get("createdBy"), cb.count(message)))
          .where(
              cb.equal(area.get("contextId"), getContextId()),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted"))
          )
          .groupBy(message.get("createdBy"));

        return session.createQuery(cq).getResultList();
    }
    
    public List<Object[]> findAuthoredMessageCountForAllStudentsByTopicId(final Long topicId) {
    	if (log.isDebugEnabled()) log.debug("findAuthoredMessageCountForAllStudentsByTopicId executing");

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");

        cq.select(cb.array(message.get("createdBy"), cb.count(message)))
          .where(
              cb.equal(area.get("contextId"), getContextId()),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              cb.equal(topic.get("id"), topicId)
          )
          .groupBy(message.get("createdBy"));

        return session.createQuery(cq).getResultList();
    }
    
    public List<Object[]> findAuthoredMessageCountForAllStudentsByForumId(final Long forumId) {
    	if (log.isDebugEnabled()) log.debug("findAuthoredMessageCountForAllStudentsByForumId executing");

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");

        cq.select(cb.array(message.get("createdBy"), cb.count(message)))
          .where(
              cb.equal(area.get("contextId"), getContextId()),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              cb.equal(forum.get("id"), forumId)
           )
          .groupBy(message.get("createdBy"));

        return session.createQuery(cq).getResultList();

    }

    public List<Object[]> findAuthoredNewMessageCountForAllStudentsByTopicId(final Long topicId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");

        cq.select(cb.array(message.get("createdBy"), cb.count(message)))
          .where(
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              cb.equal(topic.get("id"), topicId),
              cb.isNull(message.get("inReplyTo"))
           )
          .groupBy(message.get("createdBy"));

        return session.createQuery(cq).getResultList();
    }

    public List<Object[]> findAuthoredNewMessageCountForAllStudentsByForumId(final Long forumId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");

        cq.select(cb.array(message.get("createdBy"), cb.count(message)))
          .where(
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              cb.equal(forum.get("id"), forumId),
              cb.isNull(message.get("inReplyTo"))
          )
          .groupBy(message.get("createdBy"));

        return session.createQuery(cq).getResultList();
    }

    public List<Object[]> findAuthoredRepliesMessageCountForAllStudentsByTopicId(final Long topicId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");

        cq.select(cb.array(message.get("createdBy"), cb.count(message)))
          .where(
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              cb.equal(topic.get("id"), topicId),
              cb.isNotNull(message.get("inReplyTo"))
           )
          .groupBy(message.get("createdBy"));

        return session.createQuery(cq).getResultList();
    }

    public List<Object[]> findAuthoredRepliesMessageCountForAllStudentsByForumId(final Long forumId) {

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");

        cq.select(cb.array(message.get("createdBy"), cb.count(message)))
          .where(
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              cb.equal(forum.get("id"), forumId),
              cb.isNotNull(message.get("inReplyTo"))
          )
          .groupBy(message.get("createdBy"));

        return session.createQuery(cq).getResultList();
    }
    
    public int findReadMessageCountByTopicIdByUserId(final Long topicId, final String userId) {
        if (topicId == null || userId == null) {
            log.error("findReadMessageCountByTopicIdByUserId failed with topicId: " + topicId + 
            			" and userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findReadMessageCountByTopicIdByUserId executing with topicId: " + topicId + 
        				" and userId: " + userId);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Root<UnreadStatusImpl> us = cq.from(UnreadStatusImpl.class);

        cq.select(cb.count(message))
          .where(
              cb.equal(message.get("topic").get("id"), topicId),
              cb.equal(us.get("userId"), userId),
              cb.equal(message.get("id"), us.get("messageId")),
              cb.isTrue(us.get("read")),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted"))
          );

        return session.createQuery(cq).uniqueResult().intValue();
    }
    
    public int findReadMessageCountForStudent(final String userId) {
    	if (userId == null) {
    		log.error("findReadMessageCountForStudent failed with null userId");
    		throw new IllegalArgumentException("userId cannot be null");
    	}
    	
    	if (log.isDebugEnabled()) log.debug("findReadMessageCountForStudent executing with userId: " + userId);
    	
    	Session session = sessionFactory.getCurrentSession();
    	CriteriaBuilder cb = session.getCriteriaBuilder();
    	CriteriaQuery<Long> cq = cb.createQuery(Long.class);

    	Root<MessageImpl> message = cq.from(MessageImpl.class);
    	Root<UnreadStatusImpl> us = cq.from(UnreadStatusImpl.class);
    	Join<MessageImpl, TopicImpl> topic = message.join("topic");
    	Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
    	Join<OpenForumImpl, AreaImpl> area = forum.join("area");

    	cq.select(cb.count(message))
    	  .where(
    	      cb.equal(message.get("id"), us.get("messageId")),
    	      cb.equal(area.get("contextId"), getContextId()),
    	      cb.equal(us.get("userId"), userId),
    	      cb.isTrue(us.get("read")),
    	      cb.isFalse(message.get("draft")),
    	      cb.isFalse(message.get("deleted"))
    	  );

    	return session.createQuery(cq).uniqueResult().intValue();
    }
    
    /*
     * (non-Javadoc)
     * @see org.sakaiproject.api.app.messageforums.MessageForumsMessageManager#findReadMessagesForStudent()
     */
    public List<UserStatistics> findReadStatsForStudent(final String studentId) {
      if (log.isDebugEnabled()) log.debug("findReadStatsForStudent()");

      Session session = sessionFactory.getCurrentSession();
      CriteriaBuilder cb = session.getCriteriaBuilder();
      CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

      Root<MessageImpl> message = cq.from(MessageImpl.class);
      Root<UnreadStatusImpl> us = cq.from(UnreadStatusImpl.class);
      Join<MessageImpl, TopicImpl> topic = message.join("topic");
      Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
      Join<OpenForumImpl, AreaImpl> area = forum.join("area");

      cq.select(cb.array(
              forum.get("title"),
              topic.get("title"),
              message.get("created"),
              message.get("title"),
              message.get("id"),
              topic.get("id"),
              forum.get("id")
          ))
          .where(
              cb.equal(area.get("contextId"), getContextId()),
              cb.equal(us.get("userId"), studentId),
              cb.equal(message.get("id"), us.get("messageId")),
              cb.isTrue(us.get("read")),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted"))
          );

      List<Object[]> results = session.createQuery(cq).getResultList();

      List<UserStatistics> returnList = new ArrayList<UserStatistics>();
      for(Object[] result : results){
    	  UserStatistics stat = new UserStatistics((String) result[0], (String) result[1], (Date) result[2], (String) result[3], 
    			  ((Integer) result[4]).toString(), ((Integer) result[5]).toString(), ((Integer) result[6]).toString(), studentId);
    	  returnList.add(stat);
      }
      return returnList;
    }
    
    public List<UserStatistics> findReadStatsForStudentByTopicId(final String studentId, final Long topicId) {
    	if (log.isDebugEnabled()) log.debug("findReadStatsForStudentByTopicId()");

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Root<UnreadStatusImpl> us = cq.from(UnreadStatusImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");

        cq.select(cb.array(
                forum.get("title"),
                topic.get("title"),
                message.get("created"),
                message.get("title"),
                message.get("id"),
                topic.get("id"),
                forum.get("id")
            ))
            .where(
                cb.equal(topic.get("id"), topicId),
                cb.equal(us.get("userId"), studentId),
                cb.equal(message.get("id"), us.get("messageId")),
                cb.isTrue(us.get("read")),
                cb.isFalse(message.get("draft")),
                cb.isFalse(message.get("deleted"))
            );

        List<Object[]> results = session.createQuery(cq).getResultList();

        List<UserStatistics> returnList = new ArrayList<UserStatistics>();
        for(Object[] result : results){
      	  UserStatistics stat = new UserStatistics((String) result[0], (String) result[1], (Date) result[2], (String) result[3], 
      			  ((Integer) result[4]).toString(), ((Integer) result[5]).toString(), ((Integer) result[6]).toString(), studentId);
      	  returnList.add(stat);
        }
        return returnList;
    }
    
    public List<UserStatistics> findReadStatsForStudentByForumId(final String studentId, final Long forumId) {
    	if (log.isDebugEnabled()) log.debug("findReadStatsForStudentByForumId()");

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Root<UnreadStatusImpl> us = cq.from(UnreadStatusImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");

        cq.select(cb.array(
                forum.get("title"),
                topic.get("title"),
                message.get("created"),
                message.get("title"),
                message.get("id"),
                topic.get("id"),
                forum.get("id")
            ))
            .where(
                cb.equal(forum.get("id"), forumId),
                cb.equal(us.get("userId"), studentId),
                cb.equal(message.get("id"), us.get("messageId")),
                cb.isTrue(us.get("read")),
                cb.isFalse(message.get("draft")),
                cb.isFalse(message.get("deleted"))
            );

        List<Object[]> results = session.createQuery(cq).getResultList();
        List<UserStatistics> returnList = new ArrayList<UserStatistics>();
        for(Object[] result : results){
      	  UserStatistics stat = new UserStatistics((String) result[0], (String) result[1], (Date) result[2], (String) result[3], 
      			  ((Integer) result[4]).toString(), ((Integer) result[5]).toString(), ((Integer) result[6]).toString(), studentId);
      	  returnList.add(stat);
        }
        return returnList;
    }
    
    public List<Object[]> findReadMessageCountForAllStudents() {
        if (log.isDebugEnabled()) log.debug("findReadMessageCountForAllStudentsInSite executing");

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Root<UnreadStatusImpl> us = cq.from(UnreadStatusImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");

        cq.select(cb.array(us.get("userId"), cb.count(message)))
          .where(
              cb.equal(message.get("id"), us.get("messageId")),
              cb.equal(area.get("contextId"), getContextId()),
              cb.isTrue(us.get("read")),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted"))
           )
          .groupBy(us.get("userId"));

        return session.createQuery(cq).getResultList();
    }
    
    public List<Object[]> findReadMessageCountForAllStudentsByTopicId(final Long topicId) {
        if (log.isDebugEnabled()) log.debug("findReadMessageCountForAllStudentsByTopicId executing");

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Root<UnreadStatusImpl> us = cq.from(UnreadStatusImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");

        cq.select(cb.array(us.get("userId"), cb.count(message)))
          .where(
              cb.equal(message.get("id"), us.get("messageId")),
              cb.equal(area.get("contextId"), getContextId()),
              cb.isTrue(us.get("read")),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              cb.equal(topic.get("id"), topicId)
           )
          .groupBy(us.get("userId"));

        return session.createQuery(cq).getResultList();
    }
    
    public List<Object[]> findReadMessageCountForAllStudentsByForumId(final Long forumId) {
        if (log.isDebugEnabled()) log.debug("findReadMessageCountForAllStudentsByForumId executing");

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Root<UnreadStatusImpl> us = cq.from(UnreadStatusImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");

        cq.select(cb.array(us.get("userId"), cb.count(message)))
          .where(
               cb.equal(message.get("id"), us.get("messageId")),
               cb.equal(area.get("contextId"), getContextId()),
               cb.isTrue(us.get("read")),
               cb.isFalse(message.get("draft")),
               cb.isFalse(message.get("deleted")),
               cb.equal(forum.get("id"), forumId)
               )
          .groupBy(us.get("userId"));

        return session.createQuery(cq).getResultList();
    }
    
    /**
     * Returns count of all messages in a topic that have been approved or were authored by given user
     */
    public int findViewableMessageCountByTopicIdByUserId(final Long topicId, final String userId) {
        if (topicId == null || userId == null) {
            log.error("findViewableMessageCountByTopicIdByUserId failed with topicId: {}, userId: {}", topicId, userId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findViewableMessageCountByTopicIdByUserId with topicId: {}, userId: {}", topicId, userId);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<MessageImpl> message = cq.from(MessageImpl.class);

        cq.select(cb.count(message))
          .where(
              cb.equal(message.get("topic").get("id"), topicId),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              cb.or(
                  cb.isTrue(message.get("approved")),
                  cb.equal(message.get("createdBy"), userId)
              )
          );

        return session.createQuery(cq).uniqueResult().intValue();
    }

    /**
     * Returns count of all messages in a topic that have been approved or were authored by given user
     * This is meant to be a more efficient version of findViewableMessageCountByTopicIdByUserId
     * that is capable of fetching all users in a site at once instead of via separate queries
     */
    public Map<String, Integer> findViewableMessageCountByTopicIdByUserIds(final Long topicId, final Set<String> userIds) {
        if (topicId == null || userIds == null) {
            log.error("findViewableMessageCountByTopicIdByUserIds failed with topicId: {}, userIds: {}", topicId, userIds);
            throw new IllegalArgumentException("Null Argument");
        }

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<MessageImpl> message = cq.from(MessageImpl.class);

        cq.select(cb.array(message.get("createdBy"), message.get("approved"), cb.count(message)))
          .where(
              cb.equal(message.get("topic").get("id"), topicId),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              message.get("createdBy").in(userIds)
          )
          .groupBy(message.get("createdBy"), message.get("approved"));

        List<Object[]> results = session.createQuery(cq).getResultList();

        Map<String, Integer> userViewableMap = new HashMap<>();
        int totalApproved = 0;

        for (Object[] result : results) {
            final String userId = (String) result[0];
            final Boolean approved = (Boolean) result[1];
            final Long authored = (Long) result[2];

            if (approved != null && approved) {
                totalApproved += authored;
            } else if (authored != null) {
                userViewableMap.put(userId, authored.intValue());
            }
        }

        if (totalApproved > 0) {
            for (String userId : userIds) {
                userViewableMap.merge(userId, totalApproved, Integer::sum);
            }
        }

        return userViewableMap;
    }
    
    /**
     * Returns count of all msgs in a topic that have been approved or were authored by curr user
     */
    public int findViewableMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            log.error("findViewableMessageCountByTopicId failed with topicId: null");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findViewableMessageCountByTopicId executing with topicId: {}", topicId);

        if (getCurrentUser() != null) {
            return findViewableMessageCountByTopicIdByUserId(topicId, getCurrentUser());
        }
        return 0;
    }

   public int findUnreadMessageCountByTopicIdByUserId(final Long topicId, final String userId){
	   if (topicId == null || userId == null) {
           log.error("findUnreadMessageCountByTopicIdByUserId failed with topicId: {}, userId: {}", topicId, userId);
 
           throw new IllegalArgumentException("Null Argument");
       }

       log.debug("findUnreadMessageCountByTopicIdByUserId executing with topicId: {}", topicId);

       return findMessageCountByTopicId(topicId) - findReadMessageCountByTopicIdByUserId(topicId, userId);
   }
    
   public int findUnreadMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            log.error("findUnreadMessageCountByTopicId failed with topicId: null");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findUnreadMessageCountByTopicId executing with topicId: {}", topicId);

        return findMessageCountByTopicId(topicId) - findReadMessageCountByTopicId(topicId);
    }
   
   /**
    * Returns count of all unread msgs for given user that have been approved or
    * were authored by user
    */
   public int findUnreadViewableMessageCountByTopicIdByUserId(final Long topicId, final String userId) {
       if (topicId == null) {
           log.error("findUnreadViewableMessageCountByTopicIdByUserId failed with topicId: null and userid: " + userId);
           throw new IllegalArgumentException("Null Argument");
       }

       log.debug("findUnreadViewableMessageCountByTopicIdByUserId executing with topicId: {}. userId: {}", topicId, userId);

       return findViewableMessageCountByTopicIdByUserId(topicId, userId) - findReadViewableMessageCountByTopicIdByUserId(topicId, userId);
   }
   
   /**
    * Returns count of all unread msgs for current user that have been approved or
    * were authored by current user
    */
   public int findUnreadViewableMessageCountByTopicId(final Long topicId) {
       if (topicId == null) {
           log.error("findUnreadViewableMessageCountByTopicId failed with topicId: null");
           throw new IllegalArgumentException("Null Argument");
       }

       log.debug("findUnreadViewableMessageCountByTopicId executing with topicId: " + topicId);

       if(getCurrentUser()!=null){
       return findUnreadViewableMessageCountByTopicIdByUserId(topicId, getCurrentUser());
       }
       else return 0;
   }
    
    public int findReadMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            log.error("findReadMessageCountByTopicId failed with topicId: Null");
            throw new IllegalArgumentException("Null Argument");
        }

        if(getCurrentUser()!=null){
        return findReadMessageCountByTopicIdByUserId(topicId, getCurrentUser());
        }else return 0;
    }
    
    /**
     * Returns count of all read msgs for given user that have been approved or
     * were authored by user
     * @param topicId
     * @param userId
     * @return
     */
    public int findReadViewableMessageCountByTopicIdByUserId(final Long topicId, final String userId) {
    	if (topicId == null || userId == null) {
            log.error("findReadViewableMessageCountByTopicIdByUserId failed with topicId: " + topicId + 
            			" and userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findReadViewableMessageCountByTopicIdByUserId executing with topicId: " + topicId + 
        				" and userId: " + userId);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Root<UnreadStatusImpl> us = cq.from(UnreadStatusImpl.class);

        cq.select(cb.count(message))
          .where(
              cb.equal(message.get("topic").get("id"), topicId),
              cb.equal(us.get("userId"), userId),
              cb.equal(message.get("id"), us.get("messageId")),
              cb.isTrue(us.get("read")),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted")),
              cb.or(
                  cb.isTrue(message.get("approved")),
                  cb.equal(message.get("createdBy"), userId)
              )
          );

        return session.createQuery(cq).uniqueResult().intValue();
    }
    
    /**
     * Returns count of all read msgs for current user that have been approved or
     * were authored by user
     * @param topicId
     * @return
     */
    public int findReadViewableMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            log.error("findReadViewableMessageCountByTopicId failed with topicId: null");
            throw new IllegalArgumentException("Null Argument");
        }

        if(getCurrentUser()!=null){
        return findReadViewableMessageCountByTopicIdByUserId(topicId, getCurrentUser());
        }
        else return 0;
    }
    
    public List findMessagesByTopicId(final Long topicId) {
        if (topicId == null) {
            log.error("findMessagesByTopicId failed with topicId: null");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findMessagesByTopicId executing with topicId: " + topicId);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MessageImpl> cq = cb.createQuery(MessageImpl.class);
        Root<MessageImpl> message = cq.from(MessageImpl.class);

        cq.select(message)
          .where(cb.equal(message.get("topic").get("id"), topicId));

        return session.createQuery(cq).getResultList();
    }
    
    public List findUndeletedMessagesByTopicId(final Long topicId) {
        if (topicId == null) {
            log.error("findUndeletedMessagesByTopicId failed with topicId: null");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findUndeletedMessagesByTopicId executing with topicId: " + topicId);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MessageImpl> cq = cb.createQuery(MessageImpl.class);
        Root<MessageImpl> message = cq.from(MessageImpl.class);

        cq.select(message)
          .where(
              cb.equal(message.get("topic").get("id"), topicId),
              cb.isFalse(message.get("deleted"))
          );

        return session.createQuery(cq).getResultList();
    }
    
    public int findMessageCountByTopicId(final Long topicId) {
        if (topicId == null) {
            log.error("findMessageCountByTopicId failed with topicId: null");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findMessageCountByTopicId executing with topicId: " + topicId);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<MessageImpl> message = cq.from(MessageImpl.class);

        cq.select(cb.count(message))
          .where(
              cb.equal(message.get("topic").get("id"), topicId),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted"))
          );

        return session.createQuery(cq).uniqueResult().intValue();
    }
    
    public List<Object[]> findMessageCountByForumId(final Long forumId) {
        if (forumId == null) {
            log.error("findMessageCountByForumId failed with forumId: " + forumId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findMessageCountByForumId executing with forumId: " + forumId);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");

        cq.select(cb.array(topic.get("id"), cb.count(message)))
          .where(
              cb.equal(forum.get("id"), forumId),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted"))
          )
          .groupBy(topic.get("id"));

        return session.createQuery(cq).getResultList();
    }
    
    /*
    +     * (non-Javadoc)
    +     * @see org.sakaiproject.api.app.messageforums.MessageForumsForumManager#findMessageCountsForMainPage(java.util.List)
    +     */
    public List<Object[]> findMessageCountsForMainPage(final Collection<Long> topicIds) {
    	if (topicIds.isEmpty()) return new ArrayList<Object[]>();

    	Session session = sessionFactory.getCurrentSession();

    	Iterator<Long> itTopicIds = topicIds.iterator();
    	int numTopics = topicIds.size();
    	List<Object[]> retrievedCounts = new ArrayList<>(numTopics);

    	List<Long> queryTopics = new ArrayList<>(Math.min(numTopics, MAX_IN_CLAUSE_SIZE));
    	int querySize = 0;

    	while (itTopicIds.hasNext()) {
    		while (itTopicIds.hasNext() && querySize < MAX_IN_CLAUSE_SIZE) {
    			queryTopics.add(itTopicIds.next());
    			querySize++;
    		}

    		CriteriaBuilder cb = session.getCriteriaBuilder();
    		CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

    		Root<TopicImpl> topic = cq.from(TopicImpl.class);
    		Join<TopicImpl, MessageImpl> msg = topic.join("messagesSet", JoinType.LEFT);
    		msg.on(cb.and(
    			cb.isFalse(msg.get("draft")),
    			cb.isFalse(msg.get("deleted"))
    		));

    		cq.select(cb.array(topic.get("id"), cb.count(msg)))
    			.where(topic.get("id").in(queryTopics))
    			.groupBy(topic.get("id"));

    		retrievedCounts.addAll(session.createQuery(cq).getResultList());

    		queryTopics.clear();
    		querySize = 0;
    	}

    	return retrievedCounts;
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.api.app.messageforums.MessageForumsMessageManager#findReadMessageCountsForMainPage(java.util.Collection)
     */
    public List<Object[]> findReadMessageCountsForMainPage(final Collection<Long> topicIds) {
    	if (topicIds.isEmpty()) return new ArrayList<>();

    	Session session = sessionFactory.getCurrentSession();
    	String userId = getCurrentUser();

    	Iterator<Long> itTopicIds = topicIds.iterator();
    	int numTopics = topicIds.size();
    	List<Object[]> retrievedCounts = new ArrayList<>(numTopics);

    	List<Long> queryTopics = new ArrayList<>(Math.min(numTopics, MAX_IN_CLAUSE_SIZE));
    	int querySize = 0;

    	while (itTopicIds.hasNext()) {
    		while (itTopicIds.hasNext() && querySize < MAX_IN_CLAUSE_SIZE) {
    			queryTopics.add(itTopicIds.next());
    			querySize++;
    		}

    		CriteriaBuilder cb = session.getCriteriaBuilder();
    		CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

    		Root<TopicImpl> topic = cq.from(TopicImpl.class);
    		Join<TopicImpl, MessageImpl> msg = topic.join("messagesSet", JoinType.LEFT);
    		msg.on(cb.and(
    			cb.isFalse(msg.get("draft")),
    			cb.isFalse(msg.get("deleted"))
    		));

    		Root<UnreadStatusImpl> readMsg = cq.from(UnreadStatusImpl.class);

    		cq.select(cb.array(topic.get("id"), cb.count(readMsg)))
    		.where(
    			topic.get("id").in(queryTopics),
    				cb.equal(msg.get("id"), readMsg.get("messageId")),
    				cb.equal(readMsg.get("userId"), userId),
    				cb.isTrue(readMsg.get("read"))
    			)
    		.groupBy(topic.get("id"));

    		retrievedCounts.addAll(session.createQuery(cq).getResultList());

    		queryTopics.clear();
    		querySize = 0;
    	}

    	return retrievedCounts;
    }


    /**
     * Returns a topic id and count for a given site
     * @return
     */
    public List<Object[]> findMessageCountTotal() {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");

        cq.select(cb.array(topic.get("id"), cb.count(message)))
          .where(
              cb.equal(area.get("contextId"), getContextId()),
              cb.isFalse(message.get("draft")),
              cb.isFalse(message.get("deleted"))
          )
          .groupBy(topic.get("id"));

        return session.createQuery(cq).getResultList();
    }
    
    public UnreadStatus findUnreadStatusByUserId(final Long topicId, final Long messageId, final String userId){
    	if (messageId == null || topicId == null || userId == null) {
            log.error("findUnreadStatusByUserId failed with topicId: " + topicId + ", messageId: " + messageId
            		+ ", userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("findUnreadStatus executing with topicId: " + topicId + ", messageId: " + messageId);

        Session session = getSessionFactory().getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<UnreadStatusImpl> cq = cb.createQuery(UnreadStatusImpl.class);
        Root<UnreadStatusImpl> root = cq.from(UnreadStatusImpl.class);

        cq.select(root).where(
            cb.equal(root.get("messageId"), messageId),
            cb.equal(root.get("topicId"), topicId),
            cb.equal(root.get("userId"), userId)
        );

        return session.createQuery(cq).uniqueResult();
    }
    
    public UnreadStatus findUnreadStatus(final Long topicId, final Long messageId) {
        if (messageId == null || topicId == null) {
            log.error("findUnreadStatus failed with topicId: " + topicId + ", messageId: " + messageId);
            throw new IllegalArgumentException("Null Argument");
        }

        if(getCurrentUser()!=null){
        return findUnreadStatusByUserId(topicId, messageId, getCurrentUser()); 
        }else return null;
    }

    public void deleteUnreadStatus(Long topicId, Long messageId) {
        if (messageId == null || topicId == null) {
            log.error("deleteUnreadStatus failed with topicId: " + topicId + ", messageId: " + messageId);
            throw new IllegalArgumentException("Null Argument");
        }

        Session session = getSessionFactory().getCurrentSession();

        log.debug("deleteUnreadStatus executing with topicId: " + topicId + ", messageId: " + messageId);

        UnreadStatus status = findUnreadStatus(topicId, messageId);
        if (status != null) {
            session.remove(status);
        }
    }

    private boolean isMessageFromForums(Message message) {
    	return message.getTopic() != null;
    }
    
    public void markMessageNotReadForUser(Long topicId, Long messageId, boolean read) {
        if (messageId == null || topicId == null) {
            log.error("markMessageReadNotForUser failed with topicId: " + topicId + ", messageId: " + messageId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("markMessageNotReadForUser executing with topicId: " + topicId + ", messageId: " + messageId);

        if(getCurrentUser()!=null){
        markMessageNotReadForUser(topicId, messageId, read, getCurrentUser());
        }
        else return;
    }
    
    public void markMessageNotReadForUser(Long topicId, Long messageId, boolean read, String userId)
    {
    	markMessageNotReadForUser(topicId, messageId, read, userId, toolManager.getCurrentPlacement().getContext(), toolManager.getCurrentTool().getId());
    }
    
    public void markMessageNotReadForUser(Long topicId, Long messageId, boolean read, String userId, String context, String toolId)
    {
    	// to only add to event log if not read
    	boolean trulyUnread;
    	boolean originalReadStatus;
    	
    	if (messageId == null || topicId == null || userId == null) {
            log.error("markMessageNotReadForUser failed with topicId: " + topicId + ", messageId: " + messageId + ", userId: " + userId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("markMessageNotReadForUser executing with topicId: " + topicId + ", messageId: " + messageId);

        Session session = getSessionFactory().getCurrentSession();

        UnreadStatus status = findUnreadStatusByUserId(topicId, messageId, userId);
        if (status == null) {
            status = new UnreadStatusImpl();
            trulyUnread = true;
            originalReadStatus = false;
        }
        else {
        	trulyUnread = status.getRead().booleanValue();
        	trulyUnread = !trulyUnread;
        	originalReadStatus = status.getRead().booleanValue();
        }
        
        status.setTopicId(topicId);
        status.setMessageId(messageId);
        status.setUserId(userId);
        status.setRead(!Boolean.valueOf(read));

        Message message = (Message) getMessageById(messageId);
        boolean isMessageFromForums = isMessageFromForums(message);
        Integer nr = message.getNumReaders();
        if (trulyUnread && !read) {
        	//increment the message count
            	if (nr == null)
                    nr = Integer.valueOf(0);
            	nr = Integer.valueOf(nr.intValue() + 1);
            	message.setNumReaders(nr);
            	log.debug("set Message readers count to: " + nr);
            	//baseForum is probably null
            	if (message.getTopic().getBaseForum()==null && message.getTopic().getOpenForum() != null)
                    message.getTopic().setBaseForum((BaseForum) message.getTopic().getOpenForum());
	 
            	message = this.saveOrUpdateMessage(message, false, toolId, userId, context, true);

        	if (isMessageFromForums)
        		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_READ, getEventMessage(message, toolId, userId, context), false));
        	else
        		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_READ, getEventMessage(message, toolId, userId, context), false));
        } else if (!trulyUnread && read) {
            //decrement the message count
            if (nr == null)
                nr = Integer.valueOf(0);
            if (nr > 0 ) {
                nr = Integer.valueOf(nr.intValue() - 1);
            }
            message.setNumReaders(nr);
            log.debug("set message readers count to [{}]", nr);
            message = this.saveOrUpdateMessage(message, false, toolId, userId, context, true);
        }

        session.merge(status);
       
        
        	
        	if (isMessageFromForums){
        		if(!originalReadStatus && read){
        			//status is changing from Unread to Read, so decrement unread number for Synoptic Messages
        			decrementForumSynopticToolInfo(userId, context, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
        		}else if(originalReadStatus && !read){
        			//status is changing from Read to Unread, so increment unread number for Synoptic Messages
        			incrementForumSynopticToolInfo(userId, context, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
        		}
        	}else{
        		if(!originalReadStatus && read){
        			//status is changing from Unread to Read, so decrement unread number for Synoptic Messages
        			decrementMessagesSynopticToolInfo(userId, context, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
        		}else if(originalReadStatus && !read){
        			//status is changing from Read to Unread, so increment unread number for Synoptic Messages
        			incrementMessagesSynopticToolInfo(userId, context, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
        		}
        	}
        
    }


    public void decrementForumSynopticToolInfo(String userId, String siteId, int numOfAttempts) {
    	try {
    		SynopticMsgcntrManagerCover.decrementForumSynopticToolInfo(Arrays.asList(userId), siteId);
    	} catch (HibernateOptimisticLockingFailureException holfe) {

    		// failed, so wait and try again
    		try {
    			Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
    		} catch (InterruptedException e) {
    			log.error(e.getMessage(), e);
    		}

    		numOfAttempts--;

    		if (numOfAttempts <= 0) {
    			log.info("MessageForumsMessageManagerImpl: decrementForumSynopticToolInfo: HibernateOptimisticLockingFailureException no more retries left");
    			log.error(holfe.getMessage(), holfe);
    		} else {
    			log.info("MessageForumsMessageManagerImpl: decrementForumSynopticToolInfo: HibernateOptimisticLockingFailureException: attempts left: "
    					+ numOfAttempts);
    			decrementForumSynopticToolInfo(userId, siteId, numOfAttempts);
    		}
    	}

    }

    public void incrementForumSynopticToolInfo(String userId, String siteId, int numOfAttempts) {
    	try {
    		SynopticMsgcntrManagerCover.incrementForumSynopticToolInfo(Arrays.asList(userId), siteId);
    	} catch (HibernateOptimisticLockingFailureException holfe) {

    		// failed, so wait and try again
    		try {
    			Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
    		} catch (InterruptedException e) {
    			log.error(e.getMessage(), e);
    		}

    		numOfAttempts--;

    		if (numOfAttempts <= 0) {
    			log.info("MessageForumsMessageManagerImpl: incrementForumSynopticToolInfo: HibernateOptimisticLockingFailureException no more retries left");
    			log.error(holfe.getMessage(), holfe);
    		} else {
    			log.info("MessageForumsMessageManagerImpl: incrementForumSynopticToolInfo: HibernateOptimisticLockingFailureException: attempts left: "
    					+ numOfAttempts);
    			incrementForumSynopticToolInfo(userId, siteId, numOfAttempts);
    		}
    	}

    }

    public void decrementMessagesSynopticToolInfo(String userId, String siteId, int numOfAttempts) {
    	try {
    		SynopticMsgcntrManagerCover.decrementMessagesSynopticToolInfo(Arrays.asList(userId), siteId);
    	} catch (HibernateOptimisticLockingFailureException holfe) {

    		// failed, so wait and try again
    		try {
    			Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
    		} catch (InterruptedException e) {
    			log.error(e.getMessage(), e);
    		}

    		numOfAttempts--;

    		if (numOfAttempts <= 0) {
    			log.info("MessageForumsMessageManagerImpl: decrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException no more retries left");
    			log.error(holfe.getMessage(), holfe);
    		} else {
    			log.info("MessageForumsMessageManagerImpl: decrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException: attempts left: "
    					+ numOfAttempts);
    			decrementMessagesSynopticToolInfo(userId, siteId, numOfAttempts);
    		}
    	}

    }

    public void incrementMessagesSynopticToolInfo(String userId, String siteId, int numOfAttempts) {
    	try {
    		SynopticMsgcntrManagerCover.incrementMessagesSynopticToolInfo(Arrays.asList(userId), siteId);
    	} catch (HibernateOptimisticLockingFailureException holfe) {

    		// failed, so wait and try again
    		try {
    			Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
    		} catch (InterruptedException e) {
    			log.error(e.getMessage(), e);
    		}

    		numOfAttempts--;

    		if (numOfAttempts <= 0) {
    			log.info("MessageForumsMessageManagerImpl: incrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException no more retries left");
    			log.error(holfe.getMessage(), holfe);
    		} else {
    			log.info("MessageForumsMessageManagerImpl: incrementMessagesSynopticToolInfo: HibernateOptimisticLockingFailureException: attempts left: "
    					+ numOfAttempts);
    			incrementMessagesSynopticToolInfo(userId, siteId, numOfAttempts);
    		}
    	}

    }
    
    public boolean isMessageReadForUser(final Long topicId, final Long messageId) {
        if (messageId == null || topicId == null) {
            log.error("getMessageById failed with topicId: " + topicId + ", messageId: " + messageId);
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getMessageById executing with topicId: " + topicId + ", messageId: " + messageId);

        UnreadStatus status = findUnreadStatus(topicId, messageId);
        if (status == null) {
            return false; // not been saved yet, so it is unread
        }
        return status.getRead().booleanValue();        
    }

    public PrivateMessage createPrivateMessage() {
        PrivateMessage message = new PrivateMessageImpl();
        message.setUuid(getNextUuid());
        message.setTypeUuid(typeManager.getPrivateMessageAreaType());
        message.setCreated(new Date());
        message.setCreatedBy(getCurrentUser());
        message.setDraft(Boolean.FALSE);
        message.setHasAttachments(Boolean.FALSE);
        
        log.debug("message " + message.getUuid() + " created successfully");
        return message;        
    }

    public Message createDiscussionMessage() {
        return createMessage(typeManager.getDiscussionForumType());
    }

    public Message createOpenMessage() {
        return createMessage(typeManager.getOpenDiscussionForumType());
    }

    public Message createMessage(String typeId) {
        Message message = new MessageImpl();
        message.setUuid(getNextUuid());
        message.setTypeUuid(typeId);
        message.setCreated(new Date());
        message.setCreatedBy(getCurrentUser());
        message.setDraft(Boolean.FALSE);
        message.setHasAttachments(Boolean.FALSE);

        log.debug("message " + message.getUuid() + " created successfully");
        return message;        
    }

    public Attachment createAttachment() {
        Attachment attachment = new AttachmentImpl();
        attachment.setUuid(getNextUuid());
        attachment.setCreated(new Date());
        attachment.setCreatedBy(getCurrentUser());
        attachment.setModified(new Date());
        attachment.setModifiedBy(getCurrentUser());

        log.debug("attachment " + attachment.getUuid() + " created successfully");
        return attachment;        
    }

    public Message saveOrUpdateMessage(Message message) {
        return saveOrUpdateMessage(message, true);
    }

    public Message saveOrUpdateMessage(Message message, boolean logEvent) {
        return saveOrUpdateMessage(message, logEvent, toolManager.getCurrentTool().getId(), getCurrentUser(), getContextId());
    }

    public Message saveOrUpdateMessage(Message message, boolean logEvent, boolean ignoreLockedTopicForum) {
        return saveOrUpdateMessage(message, logEvent, toolManager.getCurrentTool().getId(), getCurrentUser(), getContextId(), ignoreLockedTopicForum);
    }

    public Message saveOrUpdateMessage(Message message, boolean logEvent, String toolId, String userId, String contextId){
        return saveOrUpdateMessage(message, logEvent, toolId, userId, contextId, false);
    }

    public Message saveOrUpdateMessage(Message message, boolean logEvent, String toolId, String userId, String contextId, boolean ignoreLockedTopicForum){
        boolean isNew = message.getId() == null;

        if (!ignoreLockedTopicForum && !(message instanceof PrivateMessage) && isForumOrTopicLocked(message.getTopic().getBaseForum().getId(), message.getTopic().getId())) {
            log.warn("Forum or Topic is locked for [messageId: {}] not saving", (isNew ? "new" : message.getId().toString()));
            throw new LockedException("Message could not be saved [messageId: " + (isNew ? "new" : message.getId().toString()) + "]");
        }

        message.setModified(new Date());
        if(getCurrentUser()!=null){
            message.setModifiedBy(getCurrentUser());
        }
        if(message.getUuid() == null || message.getCreated() == null
        	|| message.getCreatedBy() == null || message.getModified() == null
        	|| message.getModifiedBy() == null || message.getTitle() == null 
        	|| message.getAuthor() == null || message.getHasAttachments() == null
        	|| message.getTypeUuid() == null 
        	|| message.getDraft() == null)
        {
        	log.error("null attribute(s) for saving message in MessageForumsMessageManagerImpl.saveMessage");
        }

        if (message.getNumReaders() == null)
        	message.setNumReaders(0);

        manageThreadId(message, logEvent, isNew);

        final Message persistedMessage = (Message) getSessionFactory().getCurrentSession().merge(message);

        handleEvent(message, logEvent, toolId, userId, contextId, isNew, persistedMessage);

        log.debug("message " + persistedMessage.getId() + " saved successfully");
        return persistedMessage;
    }

    private void handleEvent(Message message, boolean logEvent, String toolId, String userId, String contextId,
                             boolean isNew, Message persistedMessage) {
        if (logEvent && !isMessageFromForums(persistedMessage)) { // Forums handles events itself
        	if (isNew) {
        		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_ADD, getEventMessage(persistedMessage, toolId, userId, contextId), false));
        	} else {
        		eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_RESPONSE, getEventMessage(persistedMessage, toolId, userId, contextId), false));
        	}
        }
    }

    private void manageThreadId(Message message, boolean logEvent, boolean isNew) {
        //MSGCNTR-448 if this is a top new top level message make sure the thread date is set
        if (logEvent && isNew && message.getDateThreadlastUpdated() == null) {
        	//we don't need to do this on non log events
        	message.setDateThreadlastUpdated(new Date());
        	if (message.getInReplyTo() != null) {
        		if (message.getInReplyTo().getThreadId() != null) {
        			message.setThreadId(message.getInReplyTo().getThreadId());
        		} else {
        			message.setThreadId(message.getInReplyTo().getId());
        		}
        	}
        }
    }

    @Override
    public String saveMessage(Message message) {
        return saveMessage(message, true);
    }

    @Override
    public String saveMessage(Message message, boolean logEvent) {
        return saveMessage(message, logEvent, toolManager.getCurrentTool().getId(), getCurrentUser(), getContextId());
    }

    @Override
    public String saveMessage(Message message, boolean logEvent, String toolId, String userId, String contextId) {
        return saveMessage(message, logEvent, toolId, userId, contextId, false);
    }

    @Override
    public String saveMessage(Message message, boolean logEvent, boolean ignoreLockedTopicForum) {
        return saveMessage(message, logEvent, toolManager.getCurrentTool().getId(), getCurrentUser(), getContextId(), ignoreLockedTopicForum);
    }

    @Override
    public String saveMessage(Message message, boolean logEvent, String toolId, String userId, String contextId,
                              boolean ignoreLockedTopicForum) {

        if (!ignoreLockedTopicForum && !(message instanceof PrivateMessage)
                && isForumOrTopicLocked(message.getTopic().getBaseForum().getId(), message.getTopic().getId())) {
            log.warn("saveMessage executed [messageId: new] but forum is locked -- save aborted");
            throw new LockedException("Message could not be saved [messageId: new]");
        }

        if (message.getModified() == null) {
            message.setModified(new Date());
        }
        if (message.getModifiedBy() == null && getCurrentUser() != null) {
            message.setModifiedBy(getCurrentUser());
        }
        if (message.getUuid() == null || message.getCreated() == null || message.getCreatedBy() == null
                || message.getModified() == null || message.getModifiedBy() == null || message.getTitle() == null
                || message.getAuthor() == null || message.getHasAttachments() == null || message.getTypeUuid() == null
                || message.getDraft() == null) {
            log.error("null attribute(s) for saving message in MessageForumsMessageManagerImpl.saveMessage");
        }

        if (message.getNumReaders() == null) {
            message.setNumReaders(0);
        }
        manageThreadId(message, logEvent);

        final Message messageReturn = (Message) getSessionFactory().getCurrentSession().merge(message);

        handleEvent(messageReturn, logEvent, toolId, userId, contextId);

        log.debug("new message with id " + messageReturn.getId().toString() + " saved successfully");
        return messageReturn.getId().toString();
    }

    private void handleEvent(Message message, boolean logEvent, String toolId, String userId, String contextId) {
        if (logEvent) {
            if (isMessageFromForums(message)) {
                eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_ADD,
                        getEventMessage(message, toolId, userId, contextId), false));
            } else {
                eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_ADD,
                        getEventMessage(message, toolId, userId, contextId), false));
            }
        }
    }

    private void manageThreadId(Message message, boolean logEvent) {
        // MSGCNTR-448 if this is a top new top level message make sure the thread date
        // is set
        if (logEvent && message.getDateThreadlastUpdated() == null) {
            // we don't need to do this on non log events
            message.setDateThreadlastUpdated(new Date());
            if (message.getInReplyTo() != null) {
                if (message.getInReplyTo().getThreadId() != null) {
                    message.setThreadId(message.getInReplyTo().getThreadId());
                } else {
                    message.setThreadId(message.getInReplyTo().getId());
                }
            }
        }
    }

    public void deleteMessage(Message message) {
        long id = message.getId().longValue();
        message.setInReplyTo(null);
        
        Session session = sessionFactory.getCurrentSession();
        session.merge(message);
        
        try {
            session.flush();
        } 
        catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
        
        if (isMessageFromForums(message))
        	eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_REMOVE, getEventMessage(message), false));
        else
        	eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_MESSAGES_REMOVE, getEventMessage(message), false));

        try {
            getSessionFactory().getCurrentSession().evict(message);
        } catch (Exception e) {
            log.error("could not evict message: " + message.getId(), e);
        }
        
        Topic topic = message.getTopic();        
        topic.removeMessage(message);
        session.merge(topic);
		//getHibernateTemplate().delete(message);

        try {
            getSessionFactory().getCurrentSession().flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.debug("message " + id + " deleted successfully");
    }
    
    public Message getMessageById(final Long messageId) {        
        if (messageId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getMessageById executing with messageId: " + messageId);
        
        Session session = sessionFactory.getCurrentSession();
        return session.get(MessageImpl.class, messageId);
    }   
    
    /**
     * @see org.sakaiproject.api.app.messageforums.MessageForumsMessageManager#getMessageByIdWithAttachments(java.lang.Long)
     */
    public Message getMessageByIdWithAttachments(final Long messageId){
      
      if (messageId == null) {
        throw new IllegalArgumentException("Null Argument");
       }

       log.debug("getMessageByIdWithAttachments executing with messageId: " + messageId);

       Session session = sessionFactory.getCurrentSession();
       CriteriaBuilder cb = session.getCriteriaBuilder();
       CriteriaQuery<MessageImpl> cq = cb.createQuery(MessageImpl.class);
       Root<MessageImpl> message = cq.from(MessageImpl.class);
       message.fetch("attachmentsSet", JoinType.LEFT);

       cq.select(message)
         .where(cb.equal(message.get("id"), messageId));

       Message msg = session.createQuery(cq).uniqueResult();
       if (msg != null) msg.setTopic((Topic) Hibernate.unproxy(msg.getTopic()));
       return msg;
    }
    
    public Attachment getAttachmentById(final Long attachmentId) {        
        if (attachmentId == null) {
            throw new IllegalArgumentException("Null Argument");
        }
        
        log.debug("getAttachmentById executing with attachmentId: " + attachmentId);
        
        Session session = sessionFactory.getCurrentSession();
        return session.get(AttachmentImpl.class, attachmentId);
    }
    
    public void getChildMsgs(final Long messageId, List returnList)
    {
    	List tempList;

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MessageImpl> cq = cb.createQuery(MessageImpl.class);
        Root<MessageImpl> message = cq.from(MessageImpl.class);

        cq.select(message)
          .where(cb.equal(message.get("inReplyTo").get("id"), messageId));

        tempList = (List<Message>) (List<?>) session.createQuery(cq).getResultList();

      if(tempList != null)
      {
      	for(int i=0; i<tempList.size(); i++)
      	{
      		getChildMsgs(((Message)tempList.get(i)).getId(), returnList);
      		returnList.add((Message) tempList.get(i));
      	}
      }
    }
    
    /**
     * Will set the approved status on the given message
     */
    public void markMessageApproval(Long messageId, boolean approved)
    {
    	if (messageId == null) {
            log.error("markMessageApproval failed with messageId: null");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("markMessageApproval executing with messageId: " + messageId);

        Session session = sessionFactory.getCurrentSession();
        
        Message message = (Message) getMessageById(messageId);
        message.setApproved(Boolean.valueOf(approved));
        
        session.merge(message);
    }



    public void deleteMsgWithChild(final Long messageId)
    {
    	List thisList = new ArrayList();
    	getChildMsgs(messageId, thisList);
    	
    	for(int i=0; i<thisList.size(); i++)
    	{
    		Message delMessage = getMessageById(((Message)thisList.get(i)).getId());
    		deleteMessage(delMessage);
    	}

  		deleteMessage(getMessageById(messageId));
    }
    
    public List getFirstLevelChildMsgs(final Long messageId)
    {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MessageImpl> cq = cb.createQuery(MessageImpl.class);
        Root<MessageImpl> message = cq.from(MessageImpl.class);

        cq.select(message)
          .where(cb.equal(message.get("inReplyTo").get("id"), messageId));

        return (List<Message>) (List<?>) session.createQuery(cq).getResultList();
    }

    public List sortMessageBySubject(Topic topic, boolean asc) {
        List list = topic.getMessages();
        if (asc) {
            Collections.sort(list, MessageImpl.SUBJECT_COMPARATOR);
        } else {
            Collections.sort(list, MessageImpl.SUBJECT_COMPARATOR_DESC);
        }
        topic.setMessages(list);
        return list;
    }

    public List sortMessageByAuthor(Topic topic, boolean asc) {
        List list = topic.getMessages();
        if (asc) {
            Collections.sort(list, MessageImpl.AUTHORED_BY_COMPARATOR);
        } else {
            Collections.sort(list, MessageImpl.AUTHORED_BY_COMPARATOR_DESC);
        }
        topic.setMessages(list);
        return list;
    }

    public List sortMessageByDate(Topic topic, boolean asc) {
        List list = topic.getMessages();
        if (asc) {
            Collections.sort(list, MessageImpl.DATE_COMPARATOR);
        } else {
            Collections.sort(list, MessageImpl.DATE_COMPARATOR_DESC);
        }
        topic.setMessages(list);
        return list;
    }
    
    public List sortMessageByDate(List list, boolean asc) {
        if (list == null || list.isEmpty())
        	return null;
        
        if (asc) {
            Collections.sort(list, MessageImpl.DATE_COMPARATOR);
        } else {
            Collections.sort(list, MessageImpl.DATE_COMPARATOR_DESC);
        }

        return list;
    }
    

    private boolean isForumOrTopicLocked(final Long forumId, final Long topicId) {
        if (forumId == null || topicId == null) {
            log.error("isForumLocked called with null arguments");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("isForumLocked executing with forumId: " + forumId + ":: topicId: " + topicId);

        final Date now = new Date();

        Session session = sessionFactory.getCurrentSession();
        OpenForumImpl forum = session.get(OpenForumImpl.class, forumId);
        OpenTopicImpl topic = session.get(OpenTopicImpl.class, topicId);

        return isLocked(forum, now) || isLocked(topic, now);
    }

    private boolean isLocked(OpenForum forum, Date now) {
        if (forum == null) return true;
        if (Boolean.TRUE.equals(forum.getLocked())) return true;
        if (!Boolean.TRUE.equals(forum.getAvailabilityRestricted())) return false;
        if (!Boolean.TRUE.equals(forum.getLockedAfterClosed())) return false;
        Date closeDate = forum.getCloseDate();
        return closeDate != null && closeDate.before(now);
    }

    private boolean isLocked(OpenTopic topic, Date now) {
        if (topic == null) return true;
        if (Boolean.TRUE.equals(topic.getLocked())) return true;
        if (!Boolean.TRUE.equals(topic.getAvailabilityRestricted())) return false;
        if (!Boolean.TRUE.equals(topic.getLockedAfterClosed())) return false;
        Date closeDate = topic.getCloseDate();
        return closeDate != null && closeDate.before(now);
    }
    
    // helpers
    
    private String getCurrentUser() {        
        if (TestUtil.isRunningTests()) {
            return "test-user";
        }
        return sessionManager.getCurrentSessionUserId();
    }
    
    private String getNextUuid() {        
        return idManager.createUuid();
    }

    private String getEventMessage(Object object) {
    	return getEventMessage(object, toolManager.getCurrentTool().getId(), getCurrentUser(), getContextId());
    }
    
    private String getEventMessage(Object object, String toolId, String userId, String contextId) {
    	String eventMessagePrefix = "";
    	
    		if (toolId.equals(DiscussionForumService.MESSAGE_CENTER_ID))
    			eventMessagePrefix = "/messagesAndForums/site/";
    		else if (toolId.equals(DiscussionForumService.MESSAGES_TOOL_ID))
    			eventMessagePrefix = "/messages/site/";
    		else
    			eventMessagePrefix = "/forums/site/";
    	
    	return eventMessagePrefix + contextId + "/" + object.toString() + "/" + userId;
    }

        
    public List getAllRelatedMsgs(final Long messageId)
    {
    	Message rootMsg = getMessageById(messageId); 
    	while(rootMsg.getInReplyTo() != null)
    	{
    		rootMsg = rootMsg.getInReplyTo();
    	}
    	List childList = new ArrayList();
    	getChildMsgs(rootMsg.getId(), childList);
    	List returnList = new ArrayList();
    	returnList.add(rootMsg);
    	for(int i=0; i<childList.size(); i++)
    	{
    		returnList.add((Message)childList.get(i));
    	}

    	return returnList;
    }
    
    /**
     * 
     * @param topicId
     * @param searchText
     * @return
     */
    
    public List findPvtMsgsBySearchText(final String typeUuid, final String searchText, 
          final Date searchFromDate, final Date searchToDate, final String selectedLabel, final boolean searchByText,
          final boolean searchByAuthor, final boolean searchByBody, final boolean searchByLabel, final boolean searchByDate) {

      log.debug("findPvtMsgsBySearchText executing with searchText: " + searchText);

      final String likeText = "%" + searchText + "%";
      final Date fromDate = (searchFromDate == null) ? new Date(0) : searchFromDate;
      final Date toDate = (searchToDate == null) ? new Date(System.currentTimeMillis()) : searchToDate;
      final String userId = getCurrentUser();
      final String contextId = toolManager.getCurrentPlacement().getContext();

      Session session = sessionFactory.getCurrentSession();
      CriteriaBuilder cb = session.getCriteriaBuilder();
      CriteriaQuery<PrivateMessageImpl> cq = cb.createQuery(PrivateMessageImpl.class);
      Root<PrivateMessageImpl> message = cq.from(PrivateMessageImpl.class);
      message.fetch("recipients", JoinType.LEFT);
      Join<PrivateMessageImpl, PrivateMessageRecipientImpl> recipient =
              (Join<PrivateMessageImpl, PrivateMessageRecipientImpl>) message.getFetches().iterator().next();

      Predicate searchPredicate;
      if (searchByAuthor) {
          searchPredicate = cb.like(message.get("author"), likeText);
      } else if (searchByText) {
          searchPredicate = cb.like(message.get("title"), likeText);
      } else if (searchByBody) {
          searchPredicate = cb.like(message.get("body"), likeText);
      } else {
          searchPredicate = cb.conjunction();
      }

      Predicate datePredicate = searchByDate
              ? cb.between(message.get("created"), fromDate, toDate)
              : cb.conjunction();

      Predicate labelPredicate = searchByLabel
              ? cb.equal(message.get("label"), selectedLabel)
              : cb.conjunction();

      cq.select(message)
        .where(
            searchPredicate,
            datePredicate,
            labelPredicate,
            cb.equal(recipient.get("typeUuid"), typeUuid),
            cb.equal(recipient.get("userId"), userId),
            cb.equal(recipient.get("contextId"), contextId)
        );

      return (List<PrivateMessage>) (List<?>) session.createQuery(cq).getResultList();
  }
    
    private Integer convertBooleanToInteger(boolean value) {
       Integer retVal = (Boolean.TRUE.equals(value)) ? 1 : 0;
       return Integer.valueOf(retVal);
    }
    
    private String getContextId() {
      if (TestUtil.isRunningTests()) {
          return "test-context";
      }
      Placement placement = toolManager.getCurrentPlacement();
      String presentSiteId = placement.getContext();
      return presentSiteId;
  }
    
  public String getAttachmentUrl(String id)
  {
  	try
  	{
		return contentHostingService.getResource(id).getUrl(false);
  	}
  	catch(Exception e)
  	{
  		log.error("MessageForumsMessageManagerImpl.getAttachmentUrl" + e, e);
  	}
  	return null;
  }
  
  public String getAttachmentRelativeUrl(String id) {
      try
      {
          return contentHostingService.getResource(id).getUrl(true);
      }
      catch(Exception e)
      {
          log.error("MessageForumsMessageManagerImpl.getAttachmentUrl" + e, e);
      }
      return null;

  }

	/**
	 * Returns true if the tool with the id passed in exists in the
	 * current site.
	 * 
	 * @param toolId
	 * 			The tool id to search for.
	 * 
	 * @return
	 * 			TRUE if tool exists, FALSE otherwise.
	 */
	public boolean currentToolMatch(String toolId) {
		String curToolId = toolManager.getCurrentTool().getId();
		
		if (curToolId.equals(MESSAGECENTER_HELPER_TOOL_ID)) {
			curToolId = toolManager.getCurrentPlacement().getTool().getId();
		}

		if (toolId.equals(curToolId)) {
			return true;
		}
		
		return false;
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
	public boolean isToolInSite(String siteId, String toolId) {
		Site thisSite;
		try {
			thisSite = siteService.getSite(siteId);
			
			Collection toolsInSite = thisSite.getTools(toolId);

			return ! toolsInSite.isEmpty();
		} 
		catch (IdUnusedException e) {
			// Weirdness - should not happen
			log.error("IdUnusedException attempting to get site for id " + siteId + " to check if tool " 
							+ "with id " + toolId + " is in it.", e);
		}
		
		return false;
	}

	@Override
	public Map<Long, Boolean> getReadStatusForMessagesWithId(final List<Long> msgIds, final String userId) {
		Map<Long, Boolean> statusMap = new HashMap<>();
		if( msgIds != null && msgIds.size() > 0)
		{
			Session session = sessionFactory.getCurrentSession();
			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<UnreadStatusImpl> cq = cb.createQuery(UnreadStatusImpl.class);
			Root<UnreadStatusImpl> us = cq.from(UnreadStatusImpl.class);

			cq.select(us)
				.where(
					cb.equal(us.get("userId"), userId),
					us.get("messageId").in(msgIds)
				);

			List statusList = session.createQuery(cq).getResultList();

			msgIds.forEach(i -> statusMap.put(i, Boolean.FALSE));

			if(statusList != null)
			{
				for(int i=0; i<statusList.size(); i++)
				{
					UnreadStatus status = (UnreadStatus) statusList.get(i);
					if(status != null)
					{
						statusMap.put(status.getMessageId(), status.getRead());
					}
				}
			}
		}
		return statusMap;
	}

    public List<Message> getPendingMsgsInSiteByMembership(final List<String> membershipList, final List<Topic> moderatedTopics)
    {
        if (membershipList == null || membershipList.isEmpty() || moderatedTopics == null || moderatedTopics.isEmpty()) {
            log.debug("membershipList is null or empty | moderatedTopics is null or empty");
            return Collections.emptyList();
        }

        Set<Message> resultSet = new HashSet<>();

        // First, check by permissionLevel (custom permissions)
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq1 = cb.createQuery(Object[].class);
        Root<MessageImpl> message1 = cq1.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic1 = message1.join("topic");
        Join<TopicImpl, OpenForumImpl> forum1 = topic1.join("openForum");
        Join<TopicImpl, DBMembershipItemImpl> membershipItem1 = topic1.join("membershipItemSet");
        Join<DBMembershipItemImpl, PermissionLevelImpl> permissionLevel1 = membershipItem1.join("permissionLevel");

        cq1.select(cb.array(message1, topic1, forum1))
           .where(
               topic1.in(moderatedTopics),
               membershipItem1.get("name").in(membershipList),
               cb.isFalse(message1.get("deleted")),
               cb.isNull(message1.get("approved")),
               cb.isTrue(permissionLevel1.get("moderatePostings"))
           );

        List<Object[]> temp1 = session.createQuery(cq1).getResultList();

        Message tempMsg = null;
        Set<Message> resultSet2 = new HashSet<>();
        for (Iterator i = temp1.iterator(); i.hasNext();)
        {
          Object[] results = (Object[]) i.next();        
              
          if (results != null && results[0] instanceof Message)
          {
              tempMsg = (Message)results[0];
              tempMsg.setTopic((Topic)results[1]);
              tempMsg.getTopic().setBaseForum((BaseForum)results[2]);
              resultSet2.add(tempMsg);
          }
        }

        // Second, check by PermissionLevelName (non-custom permissions)
        CriteriaQuery<Object[]> cq2 = cb.createQuery(Object[].class);
        Root<MessageImpl> message2 = cq2.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic2 = message2.join("topic");
        Join<TopicImpl, OpenForumImpl> forum2 = topic2.join("openForum");
        Join<TopicImpl, DBMembershipItemImpl> membershipItem2 = topic2.join("membershipItemSet");

        cq2.select(cb.array(message2, topic2, forum2, membershipItem2.get("permissionLevelName")))
           .where(
               topic2.in(moderatedTopics),
               membershipItem2.get("name").in(membershipList),
               cb.isNull(membershipItem2.get("permissionLevel")),
               cb.isFalse(message2.get("deleted")),
               cb.isNull(message2.get("approved"))
           );
        
        List<Object[]> temp2 = session.createQuery(cq2).getResultList();

        for (Iterator i = temp2.iterator(); i.hasNext();)
        {
          Object[] results = (Object[]) i.next();        
              
          if (results != null && results[0] instanceof Message)
          {
              tempMsg = (Message)results[0];
              tempMsg.setTopic((Topic)results[1]); 
              tempMsg.getTopic().setBaseForum((BaseForum)results[2]);

              // See if the permission level has ability to moderate
              PermissionLevel permLevel = permissionLevelManager.getPermissionLevelByName((String)results[3]);
              if (permLevel.getModeratePostings()) {
                  resultSet.add(tempMsg);
              }
          }
        }
        
        return Util.setToList(resultSet); 
	}
	
	public List getPendingMsgsInTopic(final Long topicId)
	{
		if (topicId == null) {
            log.error("getPendingMsgsInTopic failed with topicId: null");
            throw new IllegalArgumentException("Null Argument");
        }

        log.debug("getPendingMsgsInTopic executing with topicId: " + topicId);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");

        cq.select(cb.array(message, topic, forum))
          .where(
              cb.equal(topic.get("id"), topicId),
              cb.isFalse(topic.get("draft")),
              cb.isFalse(forum.get("draft")),
              cb.isFalse(message.get("deleted")),
              cb.isNull(message.get("approved"))
          );

        List temp =  session.createQuery(cq).getResultList();

        Message tempMsg = null;
        Set resultSet = new HashSet();      
        for (Iterator i = temp.iterator(); i.hasNext();)
        {
          Object[] results = (Object[]) i.next();        
              
          if (results != null) {
            if (results[0] instanceof Message) {
              tempMsg = (Message)results[0];
              tempMsg.setTopic((Topic)results[1]); 
              tempMsg.getTopic().setBaseForum((BaseForum)results[2]);
            }
            resultSet.add(tempMsg);
          }
        }
        return Util.setToList(resultSet); 
	}
	
	public List<Message> getAllMessagesInSite(final String siteId) {
        log.debug("getAllMessagesInSite executing with siteId: " + siteId);

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<MessageImpl> message = cq.from(MessageImpl.class);
        Join<MessageImpl, TopicImpl> topic = message.join("topic");
        Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");
        Join<OpenForumImpl, AreaImpl> area = forum.join("area");

        cq.select(cb.array(message, topic, forum))
          .where(
              cb.equal(area.get("contextId"), siteId),
              cb.isFalse(message.get("deleted"))
          );

        List temp = session.createQuery(cq).getResultList();

        Message tempMsg = null;
        Set resultSet = new HashSet();      
        log.debug("got an initial list of " + temp.size());
        for (Iterator i = temp.iterator(); i.hasNext();)
        {
          Object[] results = (Object[]) i.next();        
              
          if (results != null) {
            if (results[0] instanceof Message) {
              tempMsg = (Message)results[0];
              tempMsg.setTopic((Topic)results[1]); 
              tempMsg.getTopic().setBaseForum((BaseForum)results[2]);
            }
            resultSet.add(tempMsg);
          }
        }
        
        log.debug("about to return");
        return Util.setToList(resultSet); 
	}
	

	public void saveMessageMoveHistory(Long messageId, Long desttopicId, Long sourceTopicId, boolean checkReminder){
		if (messageId == null || desttopicId == null || sourceTopicId == null) {
			log.error("saveMessageMoveHistory failed with desttopicId: " + desttopicId + ", messageId: " + messageId + ", sourceTopicId: " + sourceTopicId);
			throw new IllegalArgumentException("Null Argument");
}

		if (log.isDebugEnabled()) log.debug("saveMessageMoveHistory executing with desttopicId: " + desttopicId + ", messageId: " + messageId + ", sourceTopicId: " + sourceTopicId);


		List moved_history = null;

		Session session = sessionFactory.getCurrentSession();

		moved_history = this.findMovedHistoryByMessageId(messageId);

		// if moving back to the original topic,  set reminder to false, otherwise the original topic will show a Move reminder.
			if (log.isDebugEnabled()) log.debug("saveMessageMoveHistory (moved_messages size  " + moved_history.size()  );
			for (Iterator histIter = moved_history.iterator(); histIter.hasNext();) {
				MessageMoveHistory hist = (MessageMoveHistory) histIter.next();
				if (log.isDebugEnabled()) log.debug("moved message ids = " +  hist.getId()  + "  from : " + hist.getFromTopicId()  + "   topic : " +  hist.getToTopicId() );
				if (hist.getFromTopicId().equals(desttopicId)){
					hist.setReminder(false);
					hist.setModified(new Date());
					hist.setModifiedBy(getCurrentUser());
 					session.merge(hist);
				}
			}

		MessageMoveHistory mhist = new MessageMoveHistoryImpl ();

		mhist.setToTopicId(desttopicId);
		mhist.setMessageId(messageId);
		mhist.setFromTopicId(sourceTopicId);
		mhist.setReminder(checkReminder);
		mhist.setUuid(getNextUuid());
		mhist.setCreated(new Date());
		mhist.setCreatedBy(getCurrentUser());
		mhist.setModified(new Date());
		mhist.setModifiedBy(getCurrentUser());

		session.merge(mhist);


	}

	public List findMovedMessagesByTopicId(final Long topicId) {
		if (topicId == null) {
			log.error("findMovedMessagesByTopicId failed with topicId: " + topicId);
			throw new IllegalArgumentException("Null Argument");
		}

		if (log.isDebugEnabled()) log.debug("findMovedMessagesByTopicId executing with topicId: " + topicId);

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<MessageImpl> cq = cb.createQuery(MessageImpl.class);
		Root<MessageImpl> message = cq.from(MessageImpl.class);
		Root<MessageMoveHistoryImpl> mhist = cq.from(MessageMoveHistoryImpl.class);

		cq.select(message)
			.where(
				cb.equal(mhist.get("fromTopicId"), topicId),
				cb.equal(message.get("id"), mhist.get("messageId")),
				cb.isTrue(mhist.get("reminder"))
			);

		return (List<Message>) (List<?>) session.createQuery(cq).getResultList();
	}

	public List getRecentDiscussionForumThreadsByTopicIds(final List<Long> topicIds, final int numberOfMessages) {
		if (topicIds.isEmpty())
		{
			return new ArrayList<Object[]>();
		}
		if (log.isDebugEnabled())
		{
			log.debug("getRecentDiscussionForumThreadsByTopicIds executing for list of size: " + topicIds.size());
		}
		
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
		Root<MessageImpl> message = cq.from(MessageImpl.class);
		Join<MessageImpl, TopicImpl> topic = message.join("topic");
		Join<TopicImpl, OpenForumImpl> forum = topic.join("openForum");

		cq.select(cb.array(message, topic, forum))
			.where(
				topic.get("id").in(topicIds),
				cb.isNull(message.get("inReplyTo")),
				cb.isFalse(message.get("draft")),
				cb.isFalse(message.get("deleted"))
			)
			.orderBy(cb.desc(message.get("modified")));

		List temp = session.createQuery(cq)
				.setMaxResults(numberOfMessages)
				.getResultList();

		Message tempMsg = null;
		Set resultSet = new HashSet();
		log.debug("got an initial list of " + temp.size());
		for (Iterator i = temp.iterator(); i.hasNext();)
		{
			Object[] results = (Object[]) i.next();

			if (results != null) {
				if (results[0] instanceof Message) {
					tempMsg = (Message)results[0];
					tempMsg.setTopic((Topic)results[1]);
					tempMsg.getTopic().setBaseForum((BaseForum)results[2]);
					Hibernate.initialize(tempMsg.getAttachments());
				}
				resultSet.add(tempMsg);
			}
		}

		log.debug("about to return");
		return Util.setToList(resultSet);
	}

	public List findMovedHistoryByMessageId(final Long messageid){
		if (messageid == null) {
			log.error("findMovedHistoryByMessageId failed with messageid: " + messageid);
			throw new IllegalArgumentException("Null Argument");
		}

		if (log.isDebugEnabled()) log.debug("findMovedHistoryByMessageId executing with messageid: " + messageid);

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<MessageMoveHistoryImpl> cq = cb.createQuery(MessageMoveHistoryImpl.class);
		Root<MessageMoveHistoryImpl> root = cq.from(MessageMoveHistoryImpl.class);

		cq.select(root)
			.where(cb.equal(root.get("messageId"), messageid));

		return (List<MessageMoveHistory>) (List<?>) session.createQuery(cq).getResultList();

	}

	@Override
	public void saveDraftRecipients(long msgId, List<DraftRecipient> recipients) {
		Session session = sessionFactory.getCurrentSession();
		for (DraftRecipient dr : recipients) {
			session.merge(dr);
		}
	}

	@Override
	public List<DraftRecipient> findDraftRecipientsByMessageId(long msgId) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<DraftRecipient> cq = cb.createQuery(DraftRecipient.class);
        Root<DraftRecipient> root = cq.from(DraftRecipient.class);

        cq.select(root).where(cb.equal(root.get("draftId"), msgId));

        return session.createQuery(cq).getResultList();
	}

	@Override
	public void deleteDraftRecipientsByMessageId(long msgId) {
		Session session = sessionFactory.getCurrentSession();
		for (DraftRecipient dr : findDraftRecipientsByMessageId(msgId)) {
			session.remove(dr);
		}
	}
}
