/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.portal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Function;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderEvents;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.portal.api.BullhornData;
import org.sakaiproject.portal.api.BullhornHandler;
import org.sakaiproject.portal.api.BullhornService;
import org.sakaiproject.portal.beans.BullhornAlert;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BullhornServiceImpl implements BullhornService, Observer {

    private static final List<String> HANDLED_EVENTS = new ArrayList<>();

    @Inject
    private EventTrackingService eventTrackingService;
    @Inject
    private MemoryService memoryService;
    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    private PlatformTransactionManager transactionManager;
    @Inject
    private UserDirectoryService userDirectoryService;
    @Inject
    private SecurityService securityService;
    @Inject
    private ServerConfigurationService serverConfigurationService;
    @Resource(name = "org.sakaiproject.lessonbuildertool.model.SimplePageToolDao")
    private SimplePageToolDao simplePageToolDao;
    @Inject
    private SiteService siteService;
    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    private SessionFactory sessionFactory;

    private Cache<String, Long> countCache = null;

    @Autowired
    private List<BullhornHandler> handlers;

    private Map<String, BullhornHandler> handlerMap = new HashMap<>();

    public void init() {

        if (serverConfigurationService.getBoolean("portal.bullhorns.enabled", true)) {
            HANDLED_EVENTS.add(LessonBuilderEvents.COMMENT_CREATE);
            HANDLED_EVENTS.add(SiteService.EVENT_SITE_PUBLISH);

            handlers.forEach(h -> {
                h.getHandledEvents().forEach(he -> {
                    HANDLED_EVENTS.add(he);
                    handlerMap.put(he, h);
                });
            });

            eventTrackingService.addLocalObserver(this);
        }

        countCache = memoryService.getCache("bullhorn_alert_count_cache");
    }

    public void update(Observable o, final Object arg) {

        if (arg instanceof Event) {
            Event e = (Event) arg;
            String event = e.getEvent();
            // We add this comparation with UNKNOWN_USER because implementation of BaseEventTrackingService
            // UNKNOWN_USER is an user in a server without session. 
            if (HANDLED_EVENTS.contains(event) && !EventTrackingService.UNKNOWN_USER.equals(e.getUserId()) ) {
                String ref = e.getResource();
                String context = e.getContext();
                String[] pathParts = ref.split("/");
                String from = e.getUserId();
                long at = e.getEventTime().getTime();
                try {
                    BullhornHandler handler = handlerMap.get(event);
                    if (handler != null) {
                        Optional<List<BullhornData>> result = handler.handleEvent(e, countCache);
                        if (result.isPresent()) {
                            result.get().forEach(bd -> {

                                doInsert(from, bd.getTo(), event, ref, bd.getTitle(),
                                                bd.getSiteId(), e.getEventTime(), bd.getUrl());
                            });
                        }
                    } else if (LessonBuilderEvents.COMMENT_CREATE.equals(event)) {
                        try {
                            long commentId = Long.parseLong(pathParts[pathParts.length - 1]);
                            SimplePageComment comment = simplePageToolDao.findCommentById(commentId);

                            String url = simplePageToolDao.getPageUrl(comment.getPageId());

                            if (url != null) {
                                List<String> done = new ArrayList<>();
                                // Alert tutor types.
                                List<User> receivers = securityService.unlockUsers(
                                    SimplePage.PERMISSION_LESSONBUILDER_UPDATE, "/site/" + context);
                                for (User receiver : receivers) {
                                    String to = receiver.getId();
                                    if (!to.equals(from)) {
                                        doInsert(from, to, event, ref, "title", context, e.getEventTime(), url);
                                        done.add(to);
                                        countCache.remove(to);
                                    }
                                }

                                // Get all the comments in the same item
                                List<SimplePageComment> comments
                                    = simplePageToolDao.findCommentsOnItems(
                                        Arrays.asList(new Long[] {comment.getItemId()}));

                                if (comments.size() > 1) {
                                    // Not the first, alert all the other commenters unless they already have been
                                    for (SimplePageComment c : comments) {
                                        String to = c.getAuthor();
                                        if (!to.equals(from) && !done.contains(to)) {
                                            doInsert(from, to, event, ref, "title", context, e.getEventTime(), url);
                                            done.add(to);
                                            countCache.remove(to);
                                        }
                                    }
                                }
                            } else {
                                log.error("null url for page {}", comment.getPageId());
                            }
                        } catch (NumberFormatException nfe) {
                            log.error("Caught number format exception whilst handling events", nfe);
                        }
                    } else if (SiteService.EVENT_SITE_PUBLISH.equals(event)) {
                        final String siteId = pathParts[2];

                        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

                        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

                            protected void doInTransactionWithoutResult(TransactionStatus status) {

                                final List<BullhornAlert> deferredAlerts
                                    = sessionFactory.getCurrentSession().createCriteria(BullhornAlert.class)
                                        .add(Restrictions.eq("deferred", true))
                                        .add(Restrictions.eq("siteId", siteId)).list();

                                for (BullhornAlert da : deferredAlerts) {
                                    da.setDeferred(false);
                                    sessionFactory.getCurrentSession().update(da);
                                    countCache.remove(da.getToUser());
                                }
                            }
                        });
                    }
                } catch (Exception ex) {
                    log.error("Caught exception whilst handling events", ex);
                }
            }
        }
    }

    private void doInsert(String from, String to, String event, String ref
                                            , String title, String siteId, Date eventDate, String url) {

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            protected void doInTransactionWithoutResult(TransactionStatus status) {

                BullhornAlert ba = new BullhornAlert();
                ba.setFromUser(from);
                ba.setToUser(to);
                ba.setEvent(event);
                ba.setRef(ref);
                ba.setTitle(title);
                ba.setSiteId(siteId);
                ba.setEventDate(eventDate.toInstant());
                ba.setUrl(url);
                try {
                    ba.setDeferred(!siteService.getSite(siteId).isPublished());
                } catch (IdUnusedException iue) {
                    log.warn("Failed to find site with id {} while setting deferred to published", siteId);
                }

                sessionFactory.getCurrentSession().persist(ba);
            }
        });
    }

    @Transactional  
    public boolean clearAlert(String userId, long alertId) {

        sessionFactory.getCurrentSession().createQuery("delete BullhornAlert where id = :id and toUser = :toUser")
                    .setLong("id", alertId).setString("toUser", userId)
                    .executeUpdate();
        countCache.remove(userId);
        return true;
    }

    @Transactional
    public List<BullhornAlert> getAlerts(String userId) {

        List<BullhornAlert> alerts = sessionFactory.getCurrentSession().createCriteria(BullhornAlert.class)
                .add(Restrictions.eq("deferred", false))
                .add(Restrictions.eq("toUser", userId)).list();

        for (BullhornAlert alert : alerts) {
            try {
                User fromUser = userDirectoryService.getUser(alert.getFromUser());
                alert.setFromDisplayName(fromUser.getDisplayName());
                if (StringUtils.isNotBlank(alert.getSiteId())) {
                    alert.setSiteTitle(siteService.getSite(alert.getSiteId()).getTitle());
                }
            } catch (UserNotDefinedException unde) {
                alert.setFromDisplayName(alert.getFromUser());
            } catch (IdUnusedException iue) {
                alert.setSiteTitle(alert.getSiteId());
            }
        }

        return alerts;
    }

    @Transactional
    public long getAlertCount(String userId) {

        Long count = (Long) countCache.get(userId);

        if (count != null) {
            log.debug("bullhorn_alert_count_cache hit");
            return count;
        } else {
            log.debug("bullhorn_alert_count_cache miss");

            count = (Long) sessionFactory.getCurrentSession().createCriteria(BullhornAlert.class)
                .add(Restrictions.eq("toUser", userId))
                .add(Restrictions.eq("deferred", false))
                .setProjection(Projections.rowCount()).uniqueResult();
            countCache.put(userId, count);
            return count;
        }
    }

    @Transactional
    public boolean clearAllAlerts(String userId) {

        sessionFactory.getCurrentSession().createQuery(
                "delete BullhornAlert where toUser = :toUser and deferred = :deferred")
                .setString("toUser", userId).setBoolean("deferred", false)
                .executeUpdate();
        countCache.remove(userId);
        return true;
    }
}
