/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.sakaiproject.api.app.messageforums.Rank;
import org.sakaiproject.api.app.messageforums.RankImage;
import org.sakaiproject.api.app.messageforums.RankManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.messageforums.dao.hibernate.RankImageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.RankImpl;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class RankManagerImpl implements RankManager {
    private static final String QUERY_BY_CONTEXT_ID_USERID = "findRanksByContextIdUserID";
    private static final String QUERY_BY_CONTEXT_ID_NUM_POSTS_BASED = "findRanksByContextIdBasedOnNumPost";
    private static final String QUERY_BY_CONTEXT_ID = "findRanksByContextId";
    private static final String QUERY_BY_CONTEXT_ID_ORDER_BY_MIN_POST_DESC = "findRanksByContextIdOrderByMinPostDesc";
    private static final String QUERY_BY_RANK_ID = "findRankByRankId";

    private IdManager idManager;

    private SessionManager sessionManager;

    protected UserDirectoryService userDirectoryService;

    private EventTrackingService eventTrackingService;

    private ContentHostingService contentHostingService;

    private ServerConfigurationService serverConfigurationService;
    
    private ToolManager toolManager;
    @Setter private FormattedText formattedText;
    @Setter private SessionFactory sessionFactory;
    
    public void init() {
        log.info("init()");
    }

    public EventTrackingService getEventTrackingService() {
        return eventTrackingService;
    }

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public boolean isRanksEnabled()
    {
        return serverConfigurationService.getBoolean("msgcntr.forums.ranks.enable", false);
    }

    private String getContextId() {
        if (TestUtil.isRunningTests()) {
            return "test-context";
        }
        Placement placement = toolManager.getCurrentPlacement();
        String presentSiteId = placement.getContext();
        return presentSiteId;
    }

    public void saveRank(Rank rank) {
        if (!isRanksEnabled())
        {
            log.warn("saveRank invoked, but ranks are disabled");
            return;
        }
        Session session = sessionFactory.getCurrentSession();
        rank.setUuid(getNextUuid());
        rank.setCreated(new Date());
        rank.setCreatedBy(getCurrentUser());
        rank.setModified(new Date());
        rank.setModifiedBy(getCurrentUser());
        rank.setContextId(getContextId());
        rank = session.merge(rank);
        if (log.isDebugEnabled()) log.debug("saveRank executed for rank = " + rank.getTitle() + " contextid = " + rank.getContextId());
    }

    public List getRankList(final String contextId) {
        if (log.isDebugEnabled()) {
            log.debug("getRank(contextId: " + contextId + ")");
        }
        if (contextId == null) {
            throw new IllegalArgumentException("Null Argument");
        }
        if (!isRanksEnabled())
        {
            return new ArrayList();
        }
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<RankImpl> cq = cb.createQuery(RankImpl.class);
        Root<RankImpl> root = cq.from(RankImpl.class);

        cq.select(root)
          .where(cb.equal(root.get("contextId"), contextId))
          .orderBy(cb.asc(root.get("title")));

        return session.createQuery(cq).getResultList();
    }

    public List findRanksByContextIdOrderByMinPostDesc(final String contextId) {
        if (log.isDebugEnabled()) {
            log.debug("getRank(contextId: " + contextId + ")");
        }

        if (contextId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        if (!isRanksEnabled())
        {
            return new ArrayList();
        }

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<RankImpl> cq = cb.createQuery(RankImpl.class);
        Root<RankImpl> root = cq.from(RankImpl.class);

        cq.select(root)
          .where(
              cb.and(
                  cb.equal(root.get("contextId"), contextId),
                  cb.equal(root.get("type"), "2")
              )
          )
          .orderBy(cb.desc(root.get("minPosts")));

        return session.createQuery(cq).getResultList();
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

    public RankImage createRankImage() {
        RankImage image = new RankImageImpl();
        image.setUuid(getNextUuid());
        image.setCreated(new Date());
        image.setCreatedBy(getCurrentUser());
        image.setModified(new Date());
        image.setModifiedBy(getCurrentUser());

        log.info("createRankImage:  Rank Image  " + image.getUuid() + " created successfully");
        return image;
    }

    public void removeRank(Rank rank) {
        log.info("removeRank(Rank rank)");
        Session session = sessionFactory.getCurrentSession();
        if (!isRanksEnabled())
        {
            log.warn("removeRank invoked, but ranks are disabled");
            return;
        }
        if (rank.getRankImage() != null) {
            removeImageAttachmentObject(rank.getRankImage());
        }
        session.remove(rank);
    }

    public void removeImageAttachmentObject(RankImage o) {
        log.info("removeImageAttachmentObject(RankImage o)");
        Session session = sessionFactory.getCurrentSession();
        if (!isRanksEnabled())
        {
            log.warn("removeImageAttachmentObject invoked, but ranks are disabled");
            return;
        }
        session.remove(o);
    }

    public void removeImageAttachToRank(final Rank rank, final RankImage imageAttach) {
        log.info("removeImageAttachToRank(final Rank rank, final RankImage imageAttach)");
        if (rank == null || imageAttach == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        if (!isRanksEnabled())
        {
            log.warn("removeImageAttachToRank invoked, but ranks are disabled");
            return;
        }

        Session session = sessionFactory.getCurrentSession();
        Rank returnedData = (Rank) session.get(RankImpl.class, rank.getId());
        RankImage returnedAttach = (RankImage) session.get(RankImageImpl.class, Long.valueOf(imageAttach.getId()));
        if (returnedData != null) {
            returnedData.setRankImage(null);
            session.merge(returnedData);

            if (returnedAttach.getAttachmentId().toLowerCase().startsWith("/attachment"))
                try {
                    contentHostingService.removeResource(returnedAttach.getAttachmentId());
                    session.remove(returnedAttach);
                } catch (PermissionException | IdUnusedException | TypeException | InUseException e) {
                    log.error(e.getMessage(), e);
                }
        }
    }

    public Rank getRankById(final Long rankId) {
        if (log.isDebugEnabled()) {
            log.debug("getRankById: " + rankId + ")");
        }

        if (rankId == null) {
            throw new IllegalArgumentException("getRankById(): rankId is null");
        }

        if (!isRanksEnabled())
        {
            // This is 'warn' because it implies some code is aware of a rank, but ranks are disabled
            log.warn("getRankById invoked, but ranks are disabled");
            return null;
        }

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<RankImpl> cq = cb.createQuery(RankImpl.class);
        Root<RankImpl> root = cq.from(RankImpl.class);

        cq.select(root)
          .where(cb.equal(root.get("id"), rankId));

        return session.createQuery(cq).uniqueResult();
    }

    public RankImage createRankImageAttachmentObject(String attachId, String name) {
        if (!isRanksEnabled())
        {
            log.warn("createRankImageAttachmentObject invoked, but ranks are disabled");
            return null;
        }
        Session session = sessionFactory.getCurrentSession();
        try {
            RankImage attach = new RankImageImpl();
            attach.setCreated(new Date());
            attach.setModified(new Date());

            ContentResource cr = contentHostingService.getResource(attachId);

            User creator = userDirectoryService.getUser(cr.getProperties().getProperty(cr.getProperties().getNamePropCreator()));
            attach.setCreatedBy(creator.getDisplayName());
            User modifier = userDirectoryService.getUser(cr.getProperties().getProperty(cr.getProperties().getNamePropModifiedBy()));
            attach.setModifiedBy(modifier.getDisplayName());

            attach.setAttachmentSize((Long.valueOf(cr.getContentLength())).toString());
            attach.setAttachmentId(attachId);
            attach.setAttachmentName(name);
            attach.setAttachmentType(cr.getContentType());
            String tempString = cr.getUrl();
            attach.setAttachmentUrl(resourceUrlEscaping(tempString));

            session.merge(attach);

            return attach;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public void addImageAttachToRank(final Rank rank, final RankImage imageAttach) {

        if (rank == null || imageAttach == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        if (!isRanksEnabled())
        {
            log.warn("addImageAttachToRank invoked, but ranks are disabled");
            return;
        }

        Session session = sessionFactory.getCurrentSession();
        Rank returnedData = (Rank) session.get(RankImpl.class, rank.getId());
        if (returnedData != null) {
            imageAttach.setRank(returnedData);
            returnedData.setRankImage(imageAttach);
            session.merge(returnedData);
        }
    }

    /**
     * Apparently, the ContentResource object gives a url, but it doesn't escape any special characters. So, need to do some
     * escaping just for the name portion of the url. So, find the string "attachment" and escape anything after it.
     */
    private String resourceUrlEscaping(String url) {
        int attIndex = url.indexOf("attachment");
        String leftOfAttachment = url.substring(0, attIndex);
        String rightOfAttachment = url.substring(attIndex);
        String finalUrl = leftOfAttachment.concat(URLEncoder.encode(rightOfAttachment));
        return finalUrl;
    }

    public List findRanksByContextIdUserId(final String contextId, final String userid) {
        if (log.isDebugEnabled()) {
            log.debug("findRanksByContextIdBasedOnRoles(contextId: " + contextId + ")");
        }

        if (contextId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        if (!isRanksEnabled())
        {
            return new ArrayList();
        }

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<RankImpl> cq = cb.createQuery(RankImpl.class);
        Root<RankImpl> root = cq.from(RankImpl.class);

        Join<RankImpl, String> assignJoin = root.join("assignToIds", JoinType.INNER);

        cq.select(root)
          .where(
              cb.and(
                  cb.equal(root.get("contextId"), contextId),
                  cb.equal(assignJoin, userid)
              )
          );

        return session.createQuery(cq).getResultList();
    }

    public List findRanksByContextIdBasedOnNumPost(final String contextId) {
        if (log.isDebugEnabled()) {
            log.debug("findRanksByContextIdBasedOnNumPost(contextId: " + contextId + ")");
        }

        if (contextId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        if (!isRanksEnabled())
        {
            return new ArrayList();
        }

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<RankImpl> cq = cb.createQuery(RankImpl.class);
        Root<RankImpl> root = cq.from(RankImpl.class);

        cq.select(root)
          .where(
              cb.and(
                  cb.equal(root.get("contextId"), contextId),
                  cb.equal(root.get("type"), "2")
              )
          )
          .orderBy(cb.asc(root.get("title")));

        return session.createQuery(cq).getResultList();
    }
}
