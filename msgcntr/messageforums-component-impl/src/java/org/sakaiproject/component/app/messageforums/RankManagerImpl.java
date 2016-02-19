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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.api.app.messageforums.Rank;
import org.sakaiproject.api.app.messageforums.RankImage;
import org.sakaiproject.api.app.messageforums.RankManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.messageforums.dao.hibernate.RankImageImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.RankImpl;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.Validator;

public class RankManagerImpl extends HibernateDaoSupport implements RankManager {
    private static final Log LOG = LogFactory.getLog(RankManagerImpl.class);
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

    public void init() {
        LOG.info("init()");
    }

    public EventTrackingService getEventTrackingService() {
        return eventTrackingService;
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
        Placement placement = ToolManager.getCurrentPlacement();
        String presentSiteId = placement.getContext();
        return presentSiteId;
    }

    public void saveRank(Rank rank) {
        if (!isRanksEnabled())
        {
            LOG.warn("saveRank invoked, but ranks are disabled");
            return;
        }
        rank.setUuid(getNextUuid());
        rank.setCreated(new Date());
        rank.setCreatedBy(getCurrentUser());
        rank.setModified(new Date());
        rank.setModifiedBy(getCurrentUser());
        rank.setContextId(getContextId());
        getHibernateTemplate().saveOrUpdate(rank);
        if (LOG.isDebugEnabled()) LOG.debug("saveRank executed for rank = " + rank.getTitle() + " contextid = " + rank.getContextId());
    }

    public List getRankList(final String contextId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRank(contextId: " + contextId + ")");
        }
        if (contextId == null) {
            throw new IllegalArgumentException("Null Argument");
        }
        if (!isRanksEnabled())
        {
            return new ArrayList();
        }
        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_CONTEXT_ID);
                q.setParameter("contextId", contextId, Hibernate.STRING);
                return q.list();
            }
        };

        List ranklist = (List) getHibernateTemplate().execute(hcb);
        return ranklist;
    }

    public List findRanksByContextIdOrderByMinPostDesc(final String contextId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRank(contextId: " + contextId + ")");
        }

        if (contextId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        if (!isRanksEnabled())
        {
            return new ArrayList();
        }

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_CONTEXT_ID_ORDER_BY_MIN_POST_DESC);
                q.setParameter("contextId", contextId, Hibernate.STRING);
                return q.list();
            }
        };

        List ranklist = (List) getHibernateTemplate().execute(hcb);
        return ranklist;
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

        LOG.info("createRankImage:  Rank Image  " + image.getUuid() + " created successfully");
        return image;
    }

    public void removeRank(Rank rank) {
        LOG.info("removeRank(Rank rank)");
        if (!isRanksEnabled())
        {
            LOG.warn("removeRank invoked, but ranks are disabled");
            return;
        }
        if (rank.getRankImage() != null) {
            removeImageAttachmentObject(rank.getRankImage());
        }
        getHibernateTemplate().delete(rank);
    }

    public void removeImageAttachmentObject(RankImage o) {
        LOG.info("removeImageAttachmentObject(RankImage o)");
        if (!isRanksEnabled())
        {
            LOG.warn("removeImageAttachmentObject invoked, but ranks are disabled");
            return;
        }
        getHibernateTemplate().delete(o);
    }

    public void removeImageAttachToRank(final Rank rank, final RankImage imageAttach) {
        LOG.info("removeImageAttachToRank(final Rank rank, final RankImage imageAttach)");
        if (rank == null || imageAttach == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        if (!isRanksEnabled())
        {
            LOG.warn("removeImageAttachToRank invoked, but ranks are disabled");
            return;
        }

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Rank returnedData = (Rank) session.get(RankImpl.class, rank.getId());
                RankImage returnedAttach = (RankImage) session.get(RankImageImpl.class, Long.valueOf(imageAttach.getId()));
                if (returnedData != null) {
                    returnedData.setRankImage(null);
                    session.saveOrUpdate(returnedData);

                    if (returnedAttach.getAttachmentId().toLowerCase().startsWith("/attachment"))
                        try {
                            contentHostingService.removeResource(returnedAttach.getAttachmentId());
                            // returnedAttach.setSyllabus(null);
                            // session.saveOrUpdate(returnedAttach);
                            session.delete(returnedAttach);
                        } catch (PermissionException e) {
                            e.printStackTrace();
                        } catch (IdUnusedException e) {
                            e.printStackTrace();
                        } catch (TypeException e) {
                            e.printStackTrace();
                        } catch (InUseException e) {
                            e.printStackTrace();
                        }
                }
                return null;
            }
        };
        getHibernateTemplate().execute(hcb);
    }

    public Rank getRankById(final Long rankId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRankById: " + rankId + ")");
        }

        if (rankId == null) {
            throw new IllegalArgumentException("getRankById(): rankId is null");
        }

        if (!isRanksEnabled())
        {
            // This is 'warn' because it implies some code is aware of a rank, but ranks are disabled
            LOG.warn("getRankById invoked, but ranks are disabled");
            return null;
        }

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_RANK_ID);
                q.setParameter("rankId", rankId, Hibernate.LONG);
                return q.uniqueResult();
            }
        };

        Rank rank = (Rank) getHibernateTemplate().execute(hcb);
        return rank;
    }

    public RankImage createRankImageAttachmentObject(String attachId, String name) {
        if (!isRanksEnabled())
        {
            LOG.warn("createRankImageAttachmentObject invoked, but ranks are disabled");
            return null;
        }
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

            getHibernateTemplate().saveOrUpdate(attach);

            return attach;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addImageAttachToRank(final Rank rank, final RankImage imageAttach) {

        if (rank == null || imageAttach == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        if (!isRanksEnabled())
        {
            LOG.warn("addImageAttachToRank invoked, but ranks are disabled");
            return;
        }

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Rank returnedData = (Rank) session.get(RankImpl.class, rank.getId());
                if (returnedData != null) {
                    imageAttach.setRank(rank);
                    returnedData.setRankImage(imageAttach);
                    session.save(returnedData);
                }
                return null;
            }
        };
        getHibernateTemplate().execute(hcb);
    }

    /**
     * Apparently, the ContentResource object gives a url, but it doesn't escape any special characters. So, need to do some
     * escaping just for the name portion of the url. So, find the string "attachment" and escape anything after it.
     */
    private String resourceUrlEscaping(String url) {
        int attIndex = url.indexOf("attachment");
        String leftOfAttachment = url.substring(0, attIndex);
        String rightOfAttachment = url.substring(attIndex);

        String finalUrl = leftOfAttachment.concat(Validator.escapeUrl(rightOfAttachment));
        return finalUrl;
    }

    public List findRanksByContextIdUserId(final String contextId, final String userid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findRanksByContextIdBasedOnRoles(contextId: " + contextId + ")");
        }

        if (contextId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        if (!isRanksEnabled())
        {
            return new ArrayList();
        }

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_CONTEXT_ID_USERID);
                q.setParameter("contextId", contextId, Hibernate.STRING);
                q.setParameter("userId", userid, Hibernate.STRING);
                return q.list();
            }
        };

        List ranklist = (List) getHibernateTemplate().execute(hcb);
        return ranklist;
    }

    public List findRanksByContextIdBasedOnNumPost(final String contextId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findRanksByContextIdBasedOnNumPost(contextId: " + contextId + ")");
        }

        if (contextId == null) {
            throw new IllegalArgumentException("Null Argument");
        }

        if (!isRanksEnabled())
        {
            return new ArrayList();
        }

        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_BY_CONTEXT_ID_NUM_POSTS_BASED);
                q.setParameter("contextId", contextId, Hibernate.STRING);
                return q.list();
            }
        };

        List ranklist = (List) getHibernateTemplate().execute(hcb);
        return ranklist;
    }
}
