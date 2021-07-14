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
package org.sakaiproject.messaging.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.ignite.IgniteMessaging;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.ignite.EagerIgniteSpringBean;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.messaging.api.BullhornAlert;
import org.sakaiproject.messaging.api.BullhornData;
import org.sakaiproject.messaging.api.BullhornHandler;
import org.sakaiproject.messaging.api.MessageListener;
import org.sakaiproject.messaging.api.MessagingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessagingServiceImpl implements MessagingService, Observer {

    private static final List<String> HANDLED_EVENTS = new ArrayList<>();

    @Resource
    private EagerIgniteSpringBean ignite;

    @Resource
    private EventTrackingService eventTrackingService;
    @Resource
    private MemoryService memoryService;
    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    private PlatformTransactionManager transactionManager;
    @Resource
    private UserDirectoryService userDirectoryService;
    @Resource
    private SecurityService securityService;
    @Resource
    private ServerConfigurationService serverConfigurationService;
    @Resource
    private SiteService siteService;
    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    private SessionFactory sessionFactory;
    @Resource(name = "org.sakaiproject.time.api.UserTimeService")
    UserTimeService userTimeService;

    private IgniteMessaging messaging;

    @Autowired
    private List<BullhornHandler> handlers;

    private Map<String, BullhornHandler> handlerMap = new HashMap<>();

    public void init() {

        if (serverConfigurationService.getBoolean("portal.bullhorns.enabled", true)) {
            HANDLED_EVENTS.add(SiteService.EVENT_SITE_PUBLISH);

            handlers.forEach(h -> {
                h.getHandledEvents().forEach(he -> {

                    HANDLED_EVENTS.add(he);
                    handlerMap.put(he, h);
                });
            });

            if (log.isDebugEnabled()) {
                HANDLED_EVENTS.forEach(e -> log.debug("BH EVENT: {}", e));
            }

            eventTrackingService.addLocalObserver(this);
        }

        messaging = ignite.message(ignite.cluster().forLocal());
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
                        Optional<List<BullhornData>> result = handler.handleEvent(e);
                        if (result.isPresent()) {
                            result.get().forEach(bd -> {
                                doInsert(from, bd.getTo(), event, ref, bd.getTitle(),
                                                bd.getSiteId(), e.getEventTime(), bd.getUrl());
                            });
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
                send("USER#" + to, ba);
            }
        });
    }

    @Transactional  
    public boolean clearAlert(String userId, long alertId) {

        sessionFactory.getCurrentSession().createQuery("delete BullhornAlert where id = :id and toUser = :toUser")
                    .setParameter("id", alertId).setParameter("toUser", userId)
                    .executeUpdate();
        return true;
    }

    @Transactional
    public List<BullhornAlert> getAlerts(String userId) {

        List<BullhornAlert> alerts = sessionFactory.getCurrentSession().createCriteria(BullhornAlert.class)
                .add(Restrictions.eq("deferred", false))
                .add(Restrictions.eq("toUser", userId)).list();

        return alerts.stream().map(this::decorateAlert).collect(Collectors.toList());
    }

    @Transactional
    public boolean clearAllAlerts(String userId) {

        sessionFactory.getCurrentSession().createQuery(
                "delete BullhornAlert where toUser = :toUser and deferred = :deferred")
                .setParameter("toUser", userId).setParameter("deferred", false)
                .executeUpdate();
        return true;
    }

    private BullhornAlert decorateAlert(BullhornAlert alert) {

        try {
            User fromUser = userDirectoryService.getUser(alert.getFromUser());
            alert.setFromDisplayName(fromUser.getDisplayName());
            alert.setFormattedEventDate(userTimeService.dateTimeFormat(alert.getEventDate(), null, null));
            if (StringUtils.isNotBlank(alert.getSiteId())) {
                alert.setSiteTitle(siteService.getSite(alert.getSiteId()).getTitle());
            }
        } catch (UserNotDefinedException unde) {
            alert.setFromDisplayName(alert.getFromUser());
        } catch (IdUnusedException iue) {
            alert.setSiteTitle(alert.getSiteId());
        }

        return alert;
    }

    public void listen(String topic, MessageListener listener) {

        messaging.localListen(topic, (nodeId, message) -> {

            listener.read(decorateAlert((BullhornAlert) message));
            return true;
        });
    }

    public void send(String topic, BullhornAlert ba) {
        messaging.send(topic, ba);
    }

}
