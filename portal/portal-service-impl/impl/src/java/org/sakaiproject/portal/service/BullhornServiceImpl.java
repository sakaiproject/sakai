package org.sakaiproject.portal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Observer;
import java.util.Observable;

import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfileLinkLogic;
import org.sakaiproject.profile2.logic.ProfileStatusLogic;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfileStatus;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.model.WallItemComment;
import org.sakaiproject.profile2.util.ProfileConstants;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter @Slf4j
public class BullhornServiceImpl implements Observer {

    private final static String SOCIAL_INSERT_SQL
        = "INSERT INTO SOCIAL_ALERTS (FROM_USER,TO_USER,EVENT,REF,EVENT_DATE,URL) VALUES(?,?,?,?,?,?)";

    private final static String ACADEMIC_INSERT_SQL
        = "INSERT INTO ACADEMIC_ALERTS (FROM_USER,TO_USER,EVENT,REF,TITLE,SITE_ID,EVENT_DATE,URL) VALUES(?,?,?,?,?,?,?,?)";

    private static final List<String> HANDLED_EVENTS = new ArrayList();

    private AnnouncementService announcementService;
    private AssignmentService assignmentService;
    private EntityManager entityManager;
    private EventTrackingService eventTrackingService;
    private MemoryService memoryService;
    private ProfileConnectionsLogic profileConnectionsLogic;
    private ProfileLinkLogic profileLinkLogic;
    private ProfileWallLogic profileWallLogic;
    private ProfileStatusLogic profileStatusLogic;
    private UserDirectoryService userDirectoryService;
    private SecurityService securityService;
    private SessionManager sessionManager;
    private ServerConfigurationService serverConfigurationService;
    private SiteService siteService;
    private SqlService sqlService;

    public void init() {

        if (serverConfigurationService.getBoolean("useBullhornAlerts", true)) {
            HANDLED_EVENTS.add(ProfileConstants.EVENT_WALL_ITEM_NEW);
            HANDLED_EVENTS.add(ProfileConstants.EVENT_WALL_ITEM_COMMENT_NEW);
            HANDLED_EVENTS.add(ProfileConstants.EVENT_STATUS_UPDATE);
            HANDLED_EVENTS.add(ProfileConstants.EVENT_FRIEND_REQUEST);
            HANDLED_EVENTS.add(ProfileConstants.EVENT_FRIEND_CONFIRM);
            HANDLED_EVENTS.add(ProfileConstants.EVENT_FRIEND_IGNORE);
            HANDLED_EVENTS.add(ProfileConstants.EVENT_MESSAGE_SENT);
            HANDLED_EVENTS.add(ContentHostingService.EVENT_RESOURCE_ADD);
            HANDLED_EVENTS.add(AnnouncementService.SECURE_ANNC_ADD);
            HANDLED_EVENTS.add(AssignmentConstants.EVENT_ADD_ASSIGNMENT);
            HANDLED_EVENTS.add(AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION);
            eventTrackingService.addObserver(this);
        }

        if (serverConfigurationService.getBoolean("auto.ddl", true)) {
            sqlService.ddl(this.getClass().getClassLoader(), "bullhorn_tables");
        }
    }

    public void update(Observable o, final Object arg) {

        if (arg instanceof Event) {
            new Thread(() -> {
                Event e = (Event) arg;
                String event = e.getEvent();
                String ref = e.getResource();
                String[] pathParts = ref.split("/");
                String from = e.getUserId();
                long at = e.getEventTime().getTime();
                try {
                    if (HANDLED_EVENTS.contains(event)) {
                        Cache countCache = memoryService.newCache("bullhorn_alert_count_cache");

                        if (ProfileConstants.EVENT_WALL_ITEM_NEW.equals(event)) {
                            String to = pathParts[2];
                            if (!to.equals(from)) {
                                String wallItemId = pathParts[5];
                                WallItem wallItem
                                    = profileWallLogic.getWallItem(Long.parseLong(wallItemId));
                                String oldUserId = switchUser(to);
                                String url = profileLinkLogic.getInternalDirectUrlToUserWall(to, wallItemId);
                                switchUser(oldUserId);
                                doSocialInsert(from, to, event, ref, e.getEventTime(), url);
                                countCache.remove(to);
                            }
                        } else if (ProfileConstants.EVENT_WALL_ITEM_COMMENT_NEW.equals(event)) {
                            String commentId = pathParts[5];
                            try {
                                WallItemComment wallItemComment
                                    = profileWallLogic.getWallItemComment(Long.parseLong(commentId));
                                WallItem wallItem = wallItemComment.getWallItem();
                                String to = wallItem.getCreatorUuid();
                                if (!to.equals(from)) {
                                    String oldUserId = switchUser(to);
                                    String url = profileLinkLogic.getInternalDirectUrlToUserWall(wallItem.getUserUuid()
                                                                                    , Long.toString(wallItem.getId()));
                                    switchUser(oldUserId);
                                    doSocialInsert(from, to, event, ref, e.getEventTime(), url);
                                    countCache.remove(to);
                                }
                            } catch (Exception exp) {
                                log.error("Failed to store wall item alert.", exp);
                            }
                        } else if (ProfileConstants.EVENT_STATUS_UPDATE.equals(event)) {
                            // Get all the posters friends
                            String oldUserId = switchUser(from);
                            String url = profileLinkLogic.getInternalDirectUrlToUserProfile(from);
                            List<Person> connections = profileConnectionsLogic.getConnectionsForUser(from);
                            for (Person connection : connections) {
                                String to = connection.getProfile().getUserUuid();
                                doSocialInsert(from, to, event, ref, e.getEventTime(), url);
                                countCache.remove(to);
                            }
                            switchUser(oldUserId);
                        } else if (ProfileConstants.EVENT_FRIEND_REQUEST.equals(event)) {
                            String to = pathParts[2];
                            String oldUserId = switchUser(to);
                            String url = profileLinkLogic.getInternalDirectUrlToUserConnections();
                            switchUser(oldUserId);
                            doSocialInsert(from, to, event, ref, e.getEventTime(), url);
                            countCache.remove(to);
                        } else if (ProfileConstants.EVENT_FRIEND_CONFIRM.equals(event)
                                    || ProfileConstants.EVENT_FRIEND_IGNORE.equals(event)) {
                            String to = pathParts[2];
                            sqlService.dbWrite("DELETE FROM SOCIAL_ALERTS WHERE EVENT = ? AND FROM_USER = ?"
                                    , new Object[] {ProfileConstants.EVENT_FRIEND_REQUEST, to});
                            String oldUserId = switchUser(to);
                            String url = profileLinkLogic.getInternalDirectUrlToUserConnections();
                            switchUser(oldUserId);
                            doSocialInsert(from, to, event, ref, e.getEventTime(), url);
                            countCache.remove(from);
                        } else if (ProfileConstants.EVENT_MESSAGE_SENT.equals(event)) {
                            String to = pathParts[2];
                            String oldUserId = switchUser(to);
                            String url = profileLinkLogic.getInternalDirectUrlToUserProfile(to);
                            switchUser(oldUserId);
                            doSocialInsert(from, to, event, ref, e.getEventTime(), url);
                            countCache.remove(to);
                        } else if (ContentHostingService.EVENT_RESOURCE_ADD.equals(event)) {
                            String siteId = pathParts[3];
                            // In this case title = filename
                            String title = pathParts[pathParts.length - 1];

                            try {
                                Site site = siteService.getSite(siteId);
                                String toolId = site.getToolForCommonId("sakai.resources").getId();
                                String url = serverConfigurationService.getPortalUrl() + "/site/"
                                                                            + siteId + "/tool/" + toolId;
                                // Get all the members of the site
                                for (Member member : site.getMembers()) {
                                    String to = member.getUserId();
                                    if (!from.equals(to) && !securityService.isSuperUser(to)) {
                                        // If this member can read resources in the site, store an alert
                                        if (securityService.unlock(to
                                                , ContentHostingService.AUTH_RESOURCE_READ, ref)) {
                                            doAcademicInsert(from, to, event, ref, title, siteId, e.getEventTime(), url);
                                            countCache.remove(to);
                                        }
                                    }
                                }
                            } catch (IdUnusedException idue) {
                                log.error("No site with id '" + siteId + "'", idue);
                            }
                        } else if (AnnouncementService.SECURE_ANNC_ADD.equals(event)) {
                            String siteId = pathParts[3];
                            String announcementId = pathParts[pathParts.length - 1];

                            try {
                                Site site = siteService.getSite(siteId);
                                String toolId = site.getToolForCommonId("sakai.announcements").getId();
                                String url = serverConfigurationService.getPortalUrl() + "/site/"
                                                                            + siteId + "/tool/" + toolId;

                                SecurityAdvisor sa = unlock();

                                // In this case title = announcement subject
                                String title
                                    = ((AnnouncementMessageHeader) announcementService.getMessage(entityManager.newReference(ref)).getHeader()).getSubject();

                                lock(sa);

                                // Get all the members of the site with read ability
                                for (String  to : site.getUsersIsAllowed(AnnouncementService.SECURE_ANNC_READ)) {
                                    if (!from.equals(to) && !securityService.isSuperUser(to)) {
                                        doAcademicInsert(from, to, event, ref, title, siteId, e.getEventTime(), url);
                                        countCache.remove(to);
                                    }
                                }
                            } catch (IdUnusedException idue) {
                                log.error("No site with id '" + siteId + "'", idue);
                            }
                        } else if (AssignmentConstants.EVENT_ADD_ASSIGNMENT.equals(event)) {
                            String siteId = pathParts[3];
                            String assignmentId = pathParts[pathParts.length - 1];
                            try {
                                Assignment assignment = assignmentService.getAssignment(assignmentId);
                                Time openTime = assignment.getOpenTime();
                                if (openTime == null || openTime.getTime() < (new Date().getTime())) {
                                    Site site = siteService.getSite(siteId);
                                    String title = assignment.getTitle();
                                    // Get all the members of the site with read ability
                                    for (String  to : site.getUsersIsAllowed(AssignmentService.SECURE_ACCESS_ASSIGNMENT)) {
                                        if (!from.equals(to) && !securityService.isSuperUser(to)) {
                                            switchUser(to);
                                            String url = assignmentService.getDeepLink(siteId, assignmentId);
                                            doAcademicInsert(from, to, event, ref, title, siteId, e.getEventTime(), url);
                                            countCache.remove(to);
                                        }
                                    }
                                }
                            } catch (IdUnusedException idue) {
                                log.error("Failed to find either the assignment or the site", idue);
                            }
                        } else if (AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION.equals(event)) {
                            String siteId = pathParts[3];
                            String submissionId = pathParts[pathParts.length - 1];
                            try {
                                SecurityAdvisor sa = unlock();
                                AssignmentSubmission submission = assignmentService.getSubmission(submissionId);
                                lock(sa);
                                if (submission.getGradeReleased()) {
                                    Site site = siteService.getSite(siteId);
                                    Assignment assignment = submission.getAssignment();
                                    String title = assignment.getTitle();
                                    for (String to : ((List<String>) submission.getSubmitterIds())) {
                                        switchUser(to);
                                        String url = assignmentService.getDeepLink(siteId, assignment.getId());
                                        doAcademicInsert(from, to, event, ref, title, siteId, e.getEventTime(), url);
                                        countCache.remove(to);
                                    }
                                }
                            } catch (IdUnusedException idue) {
                                log.error("Failed to find either the submission or the site", idue);
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.error("Caught exception whilst handling events", ex);
                }
            }).start();
        }
    }

    private SecurityAdvisor unlock() {

        SecurityAdvisor securityAdvisor = new SecurityAdvisor() {
                public SecurityAdvice isAllowed(String userId, String function, String reference) {
                    return SecurityAdvice.ALLOWED;
                }
            };
        securityService.pushAdvisor(securityAdvisor);
        return securityAdvisor;
    }

    private void lock(SecurityAdvisor securityAdvisor) {
        securityService.popAdvisor(securityAdvisor);
    }

    private String switchUser(String to) {

        Session session = sessionManager.getCurrentSession();
        String from = session.getUserId();
        session.setUserId(to);
        return from;
    }

    private void doAcademicInsert(String from, String to, String event, String ref
                                            , String title, String siteId, Date eventTime ,String url) {

        sqlService.dbInsert(null
            , ACADEMIC_INSERT_SQL
            , new Object[] {from, to, event, ref, title, siteId, eventTime, url}
            , "ID");
    }

    private void doSocialInsert(String from, String to, String event, String ref, Date eventTime ,String url) {

        sqlService.dbInsert(null
            , SOCIAL_INSERT_SQL
            , new Object[] {from, to, event, ref, eventTime, url}
            , "ID");
    }
}
