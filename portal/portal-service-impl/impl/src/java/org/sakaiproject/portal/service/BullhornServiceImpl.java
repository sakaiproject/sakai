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

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;
import java.sql.ResultSet;

import org.apache.commons.lang.StringUtils;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentServiceConstants;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderEvents;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.portal.api.BullhornService;
import org.sakaiproject.portal.beans.BullhornAlert;
import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfileLinkLogic;
import org.sakaiproject.profile2.logic.ProfileStatusLogic;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BullhornServiceImpl implements BullhornService, Observer {

    private final static String BULLHORN_INSERT_SQL =
                "INSERT INTO BULLHORN_ALERTS (ALERT_TYPE, FROM_USER,TO_USER,EVENT,REF,TITLE,SITE_ID,EVENT_DATE,URL) VALUES(?,?,?,?,?,?,?,?,?)";

    private static final String ALERTS_SELECT_SQL =
                "SELECT * FROM BULLHORN_ALERTS WHERE ALERT_TYPE = ? AND TO_USER = ? ORDER BY EVENT_DATE DESC";

    private static final String ALERTS_COUNT_SQL =
                "SELECT COUNT(*) FROM BULLHORN_ALERTS WHERE ALERT_TYPE = ? AND TO_USER = ?";

    private static final String ALERT_DELETE_SQL =
                "DELETE FROM BULLHORN_ALERTS WHERE ID = ? AND TO_USER = ?";

    private static final String ALERTS_DELETE_SQL =
                "DELETE FROM BULLHORN_ALERTS WHERE ALERT_TYPE = ? AND TO_USER = ?";

    private static final List<String> HANDLED_EVENTS = new ArrayList<>();

    private final static String COMMONS_COMMENT_CREATED = "commons.comment.created";

    @Setter
    private AnnouncementService announcementService;
    @Setter
    private AssignmentService assignmentService;
    @Setter
    private EntityManager entityManager;
    @Setter
    private EventTrackingService eventTrackingService;
    @Setter
    private MemoryService memoryService;
    @Setter
    private ProfileConnectionsLogic profileConnectionsLogic;
    @Setter
    private ProfileLinkLogic profileLinkLogic;
    @Setter
    private ProfileStatusLogic profileStatusLogic;
    @Setter
    private UserDirectoryService userDirectoryService;
    @Setter
    private SecurityService securityService;
    @Setter
    private ServerConfigurationService serverConfigurationService;
    @Setter
    private SessionManager sessionManager;
    @Setter
    private SimplePageToolDao simplePageToolDao;
    @Setter
    private SiteService siteService;
    @Setter
    private SqlService sqlService;
    @Setter
    private SessionFactory sessionFactory;

    private Object commonsManager = null;
    private Method commonsManagerGetPostMethod = null;
    private Method commonsPostGetCreatorIdMethod = null;
    private Method commonsPostGetSiteIdMethod = null;
    private Method commonsPostGetCommentsMethod = null;
    private Method commonsCommentGetCreatorIdMethod = null;

    private boolean commonsInstalled = false;

    private Cache<String, Map> countCache = null;

    public void init() {

        try {
            Class postClass = Class.forName("org.sakaiproject.commons.api.datamodel.Post");
            Class commentClass = Class.forName("org.sakaiproject.commons.api.datamodel.Comment");
            if (postClass != null && commentClass != null) {
                log.debug("Found commons Post and Comment classes. Commons IS installed.");
                commonsPostGetCreatorIdMethod = postClass.getMethod("getCreatorId", new Class[] {});
                commonsPostGetSiteIdMethod = postClass.getMethod("getSiteId", new Class[] {});
                commonsPostGetCommentsMethod = postClass.getMethod("getComments", new Class[] {});
                commonsCommentGetCreatorIdMethod = commentClass.getMethod("getCreatorId", new Class[] {});
                ComponentManager componentManager = org.sakaiproject.component.cover.ComponentManager.getInstance();
                commonsManager = componentManager.get("org.sakaiproject.commons.api.CommonsManager");
                if (commonsManager != null) {
                    commonsManagerGetPostMethod
                        = commonsManager.getClass().getMethod("getPost", new Class[] { String.class, boolean.class });
                }
                if (commonsManager != null &&
                    commonsManagerGetPostMethod != null && commonsPostGetCommentsMethod != null &&
                    commonsPostGetCreatorIdMethod != null && commonsPostGetSiteIdMethod != null && 
                    commonsCommentGetCreatorIdMethod != null) {
                    log.debug("All good, got everything");
                    commonsInstalled = true;
                } else {
                    log.error("Commons is installed, but we're unable to get one of the methods");
                }
            } else {
                log.debug("Commons IS NOT installed.");
            }
        } catch (Exception e) {
            log.debug("Failed to setup stubs for commons tool", e);
        }

        if (serverConfigurationService.getBoolean("portal.bullhorns.enabled", false)) {
            HANDLED_EVENTS.add(ProfileConstants.EVENT_STATUS_UPDATE);
            HANDLED_EVENTS.add(ProfileConstants.EVENT_FRIEND_REQUEST);
            HANDLED_EVENTS.add(ProfileConstants.EVENT_FRIEND_CONFIRM);
            HANDLED_EVENTS.add(ProfileConstants.EVENT_FRIEND_IGNORE);
            HANDLED_EVENTS.add(ProfileConstants.EVENT_MESSAGE_SENT);
            HANDLED_EVENTS.add(AnnouncementService.SECURE_ANNC_ADD);
            HANDLED_EVENTS.add(AssignmentConstants.EVENT_ADD_ASSIGNMENT);
            HANDLED_EVENTS.add(AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION);
            HANDLED_EVENTS.add(COMMONS_COMMENT_CREATED);
            HANDLED_EVENTS.add(LessonBuilderEvents.COMMENT_CREATE);
            eventTrackingService.addLocalObserver(this);
        }

        if (serverConfigurationService.getBoolean("auto.ddl", true)) {
            sqlService.ddl(this.getClass().getClassLoader(), "bullhorn_tables");
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
                // About to start a new thread that expects the changes in this hibernate session
                // to have been persisted, so we flush.
                try {
                    sessionFactory.getCurrentSession().flush();
                } catch (HibernateException he) {
                    // This will be thrown if there is no current Hibernate session. Nothing to do.
                }

                new Thread(() -> {
                    String ref = e.getResource();
                    String context = e.getContext();
                    String[] pathParts = ref.split("/");
                    String from = e.getUserId();
                    long at = e.getEventTime().getTime();
                    try {
                        if (ProfileConstants.EVENT_STATUS_UPDATE.equals(event)) {
                            // Get all the posters friends
                            List<User> connections = profileConnectionsLogic.getConnectedUsersForUserInsecurely(from);
                            for (User connection : connections) {
                                String to = connection.getId();
                                String url = profileLinkLogic.getInternalDirectUrlToUserProfile(to, from);
                                doSocialInsert(from, to, event, ref, e.getEventTime(), url);
                                countCache.remove(to);
                            }
                        } else if (ProfileConstants.EVENT_FRIEND_REQUEST.equals(event)) {
                            String to = pathParts[2];
                            String siteId = "~" + to;
                            Site site = siteService.getSite(siteId);
                            String toolId = site.getToolForCommonId("sakai.profile2").getId();
                            String url = serverConfigurationService.getPortalUrl() + "/site/" + siteId
                                                                        + "/tool/" + toolId + "/connections";
                            doSocialInsert(from, to, event, ref, e.getEventTime(), url);
                            countCache.remove(to);
                        } else if (ProfileConstants.EVENT_FRIEND_CONFIRM.equals(event)
                                    || ProfileConstants.EVENT_FRIEND_IGNORE.equals(event)) {
                            String to = pathParts[2];
                            sqlService.dbWrite("DELETE FROM BULLHORN_ALERTS WHERE EVENT = ? AND FROM_USER = ?"
                                    , new Object[] {ProfileConstants.EVENT_FRIEND_REQUEST, to});
                            String url = profileLinkLogic.getInternalDirectUrlToUserConnections(to);
                            doSocialInsert(from, to, event, ref, e.getEventTime(), url);
                            countCache.remove(from);
                            countCache.remove(to);
                        } else if (ProfileConstants.EVENT_MESSAGE_SENT.equals(event)) {
                            String to = pathParts[2];
                            String siteId = "~" + to;
                            Site site = siteService.getSite(siteId);
                            String toolId = site.getToolForCommonId("sakai.profile2").getId();
                            String url = serverConfigurationService.getPortalUrl() + "/site/" + siteId
                                                                        + "/tool/" + toolId + "/messages";
                            doSocialInsert(from, to, event, ref, e.getEventTime(), url);
                            countCache.remove(to);
                        } else if (commonsInstalled && COMMONS_COMMENT_CREATED.equals(event)) {
                            String type = pathParts[2];
                            String postId = pathParts[4];
                            // To is always going to be the author of the original post
                            Object post = commonsManagerGetPostMethod.invoke(commonsManager, new Object[] { postId, true });
                            if (post != null) {
                                Set<String> tos = new HashSet<>();
                                String siteId = (String) commonsPostGetSiteIdMethod.invoke(post, new Object[] {});
                                String to = (String) commonsPostGetCreatorIdMethod.invoke(post, new Object[] {});
                                tos.add(to);
                                List<Object> comments = (List <Object>) commonsPostGetCommentsMethod.invoke(post, new Object[] {});
                                for (Object comment : comments) {
                                    to = (String) commonsCommentGetCreatorIdMethod.invoke(comment, new Object[] {});
                                    tos.add(to);
                                }
                                sendCommentAlerts(from, event, ref, e, siteId, postId, tos);
                            }
                        } else if (AnnouncementService.SECURE_ANNC_ADD.equals(event)) {
                            String siteId = pathParts[3];
                            String announcementId = pathParts[pathParts.length - 1];

                            SecurityAdvisor sa = unlock(new String[] {AnnouncementService.SECURE_ANNC_READ});
                            try {
                                AnnouncementMessage message
                                    = (AnnouncementMessage) announcementService.getMessage(
                                                                    entityManager.newReference(ref));

                                if (announcementService.isMessageViewable(message)) {
                                    Site site = siteService.getSite(siteId);
                                    String toolId = site.getToolForCommonId("sakai.announcements").getId();
                                    String url = serverConfigurationService.getPortalUrl() + "/directtool/" + toolId
                                                        + "?itemReference=" + ref + "&sakai_action=doShowmetadata";

                                    // In this case title = announcement subject
                                    String title
                                        = ((AnnouncementMessageHeader) message.getHeader()).getSubject();

                                    // Get all the members of the site with read ability
                                    for (String  to : site.getUsersIsAllowed(AnnouncementService.SECURE_ANNC_READ)) {
                                        if (!from.equals(to) && !securityService.isSuperUser(to)) {
                                            doAcademicInsert(from, to, event, ref, title, siteId, e.getEventTime(), url);
                                            countCache.remove(to);
                                        }
                                    }
                                }
                            } catch (IdUnusedException idue) {
                                log.error("No site with id '" + siteId + "'", idue);
                            } finally {
                                lock(sa);
                            }
                        } else if (AssignmentConstants.EVENT_ADD_ASSIGNMENT.equals(event)) {
                            String siteId = pathParts[3];
                            String assignmentId = pathParts[pathParts.length - 1];
                            SecurityAdvisor sa = unlock(new String[] {AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT, AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION});
                            switchToAdmin();
                            try {
                                Assignment assignment = assignmentService.getAssignment(assignmentId);
                                switchToNull();
                                Instant openTime = assignment.getOpenDate();
                                if (openTime == null || openTime.isBefore(Instant.now())) {
                                    Site site = siteService.getSite(siteId);
                                    String title = assignment.getTitle();
                                    String url = assignmentService.getDeepLink(siteId, assignmentId);
                                    // Get all the members of the site with read ability
                                    for (String  to : site.getUsersIsAllowed(AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT)) {
                                        if (!from.equals(to) && !securityService.isSuperUser(to)) {
                                            doAcademicInsert(from, to, event, ref, title, siteId, e.getEventTime(), url);
                                            countCache.remove(to);
                                        }
                                    }
                                }
                            } catch (IdUnusedException idue) {
                                log.error("Failed to find either the assignment or the site", idue);
                            } finally {
                                switchToNull();
                                lock(sa);
                            }
                        } else if (AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION.equals(event)) {
                            String siteId = pathParts[3];
                            String submissionId = pathParts[pathParts.length - 1];
                            SecurityAdvisor sa = unlock(new String[] {AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT_SUBMISSION
                                                            , AssignmentServiceConstants.SECURE_ACCESS_ASSIGNMENT
                                                            , AssignmentServiceConstants.SECURE_ADD_ASSIGNMENT_SUBMISSION});

                            // Without hacking assignment's permissions model, this is only way to
                            // get a submission, other than switching to the submitting user.
                            switchToAdmin();
                            try {
                                AssignmentSubmission submission = assignmentService.getSubmission(submissionId);
                                switchToNull();
                                if (submission.getGradeReleased()) {
                                    Site site = siteService.getSite(siteId);
                                    Assignment assignment = submission.getAssignment();
                                    String title = assignment.getTitle();
                                    String url = assignmentService.getDeepLink(siteId, assignment.getId());
                                    submission.getSubmitters().forEach(to -> {
                                        doAcademicInsert(from, to.getSubmitter(), event, ref, title, siteId, e.getEventTime(), url);
                                        countCache.remove(to.getSubmitter());
                                    });
                                }
                            } catch (IdUnusedException idue) {
                                log.error("Failed to find either the submission or the site", idue);
                            } finally {
                                switchToNull();
                                lock(sa);
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
                                            doAcademicInsert(from, to, event, ref, "title", context, e.getEventTime(), url);
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
                                                doAcademicInsert(from, to, event, ref, "title", context, e.getEventTime(), url);
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
                        }
                    } catch (Exception ex) {
                        log.error("Caught exception whilst handling events", ex);
                    }
                }).start();
            }
        }
    }

    /**
     * Supply null to this and everything will be allowed. Supply
     * a list of functions and only they will be allowed.
     */
    private SecurityAdvisor unlock(final String[] functions) {

        SecurityAdvisor securityAdvisor = new SecurityAdvisor() {
                public SecurityAdvice isAllowed(String userId, String function, String reference) {

                    if (functions != null) {
                        if (Arrays.asList(functions).contains(function)) {
                            return SecurityAdvice.ALLOWED;
                        } else {
                            return SecurityAdvice.NOT_ALLOWED;
                        }
                    } else {
                        return SecurityAdvice.ALLOWED;
                    }
                }
            };
        securityService.pushAdvisor(securityAdvisor);
        return securityAdvisor;
    }

    private void lock(SecurityAdvisor securityAdvisor) {
        securityService.popAdvisor(securityAdvisor);
    }

    private void doAcademicInsert(String from, String to, String event, String ref
                                            , String title, String siteId, Date eventTime ,String url) {

        sqlService.dbInsert(null
            , BULLHORN_INSERT_SQL
            , new Object[] {ACADEMIC, from, to, event, ref, title, siteId, eventTime, url}
            , "ID");
    }

    private void doSocialInsert(String from, String to, String event, String ref, Date eventTime ,String url) {

        sqlService.dbInsert(null
            , BULLHORN_INSERT_SQL
            , new Object[] {SOCIAL, from, to, event, ref, "", "", eventTime, url}
            , "ID");
    }

    private void sendCommentAlerts(String from, String event, String ref, Event e, String siteId, String postId, Set<String> tos) {

        log.debug("sending comment alerts: from is {}, tos is {}, siteId is {}", from, tos, siteId);
        boolean isSocial = siteId.equals("SOCIAL");
        for (String to : tos) {
            // If we're commenting on our own post, no alert needed
            if (!from.equals(to)) {
                String mySiteId = siteId;
                if (isSocial) {
                    mySiteId = "~" + to;
                }
                Site site = null;
                try {
                    site = siteService.getSite(mySiteId);
                } catch (IdUnusedException ex) {
                    log.error("Couldn't find site " + mySiteId, ex);
                }
                if (site != null) {
                    String toolId = site.getToolForCommonId("sakai.commons").getId();
                    String url = serverConfigurationService.getPortalUrl() + "/directtool/"
                                 + toolId + "/posts/" + postId;
                    if (isSocial) {
                        doSocialInsert(from, to, event, ref, e.getEventTime(), url);
                    } else {
                        String title = "";
                        doAcademicInsert(from, to, event, ref, title, siteId, e.getEventTime(), url);
                    }
                    countCache.remove(to);
                }
            }
        }
    }

    public List<BullhornAlert> getSocialAlerts(String userId) {

        List<BullhornAlert> alerts = sqlService.dbRead(ALERTS_SELECT_SQL
                , new Object[] { SOCIAL, userId }
                , new SqlReader() {
                        public Object readSqlResultRecord(ResultSet rs) {
                            return new BullhornAlert(rs);
                        }
                    }
                );

        for (BullhornAlert alert : alerts) {
            try {
                User fromUser = userDirectoryService.getUser(alert.from);
                alert.fromDisplayName = fromUser.getDisplayName();
            } catch (UserNotDefinedException unde) {
                alert.fromDisplayName = alert.from;
            }
        }

        return alerts;
    }

    public int getSocialAlertCount(String userId) {

        Map<String, Integer> cachedCounts = (Map<String, Integer>) countCache.get(userId);

        if (cachedCounts == null) { cachedCounts = new HashMap<>(); }

        Integer count = cachedCounts.get("social");

        if (count != null) {
            log.debug("bullhorn_alert_count_cache hit");
            return count;
        } else {
            log.debug("bullhorn_alert_count_cache miss");

            List<Integer> counts = sqlService.dbRead(ALERTS_COUNT_SQL
            , new Object[] { SOCIAL, userId }
            , new SqlReader() {
                    public Object readSqlResultRecord(ResultSet rs) {

                        try {
                            return rs.getInt(1);
                        } catch (Exception e) {
                            log.error("Failed to get social alert count. Returning 0 ..." , e);
                            return 0;
                        }
                    }
                }
            );
            count = counts.get(0);
            cachedCounts.put("social", count);
            countCache.put(userId, cachedCounts);
            return count;
        }
    }

    public boolean clearBullhornAlert(String userId, long alertId) {

        sqlService.dbWrite(ALERT_DELETE_SQL
                        , new Object[] {alertId, userId});

        countCache.remove(userId);

        return true;
    }

    public boolean clearAllSocialAlerts(String userId) {

        sqlService.dbWrite(ALERTS_DELETE_SQL
                        , new Object[] {SOCIAL, userId});

        countCache.remove(userId);

        return true;
    }

    public List<BullhornAlert> getAcademicAlerts(String userId) {

        List<BullhornAlert> alerts = sqlService.dbRead(ALERTS_SELECT_SQL
                , new Object[] { ACADEMIC, userId }
                , new SqlReader() {
                        public Object readSqlResultRecord(ResultSet rs) {
                            return new BullhornAlert(rs);
                        }
                    }
                );

        for (BullhornAlert alert : alerts) {
            try {
                User fromUser = userDirectoryService.getUser(alert.from);
                alert.fromDisplayName = fromUser.getDisplayName();
                if (StringUtils.isNotBlank(alert.siteId)) {
                    alert.siteTitle = siteService.getSite(alert.siteId).getTitle();
                }
            } catch (UserNotDefinedException unde) {
                alert.fromDisplayName = alert.from;
            } catch (IdUnusedException iue) {
                alert.siteTitle = alert.siteId;
            }
        }

        return alerts;
    }

    public int getAcademicAlertCount(String userId) {

        Map<String, Integer> cachedCounts = (Map<String, Integer>) countCache.get(userId);

        if (cachedCounts == null) { cachedCounts = new HashMap(); }

        Integer count = cachedCounts.get("academic");

        if (count != null) {
            log.debug("bullhorn_alert_count_cache hit");
            return count;
        } else {
            log.debug("bullhorn_alert_count_cache miss");
            List<Integer> counts = sqlService.dbRead(ALERTS_COUNT_SQL
                , new Object[] { ACADEMIC, userId }
                , new SqlReader() {
                        public Object readSqlResultRecord(ResultSet rs) {

                            try {
                                return rs.getInt(1);
                            } catch (Exception e) {
                                log.error("Failed to get social alert count. Returning 0 ..." , e);
                                return 0;
                            }
                        }
                    }
                );
            count = counts.get(0);
            cachedCounts.put("academic", count);
            countCache.put(userId, cachedCounts);
            return count;
        }
    }

    public boolean clearAllAcademicAlerts(String userId) {

        sqlService.dbWrite(ALERTS_DELETE_SQL
                        , new Object[] {ACADEMIC, userId});

        countCache.remove(userId);

        return true;
    }

    private void switchToAdmin() {

        Session session = sessionManager.getCurrentSession();
        session.setUserId(UserDirectoryService.ADMIN_ID);
    }

    private void switchToNull() {

        Session session = sessionManager.getCurrentSession();
        session.setUserId(null);
    }
}
