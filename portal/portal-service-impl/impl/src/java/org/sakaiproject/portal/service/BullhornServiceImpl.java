package org.sakaiproject.portal.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Observer;
import java.util.Observable;
import java.util.HashSet;
import java.util.Set;

import org.sakaiproject.announcement.api.AnnouncementMessage;
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
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
//import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
/*import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.shared.api.assessment.AssessmentServiceAPI;
import org.sakaiproject.tool.assessment.api.spring.SamigoApi;*/

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

    private final static String BULLHORN_INSERT_SQL
        = "INSERT INTO BULLHORN_ALERTS (ALERT_TYPE, FROM_USER,TO_USER,EVENT,REF,TITLE,SITE_ID,EVENT_DATE,URL) VALUES(?,?,?,?,?,?,?,?,?)";

    private static final List<String> HANDLED_EVENTS = new ArrayList();

    private final static String ASSESSMENT_PUBLISH = "sam.assessment.publish";
    private final static String COMMONS_COMMENT_CREATED = "commons.comment.created";

    private AnnouncementService announcementService;
    //private SamigoApi samigoApi;
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
    private Object commonsManager = null;
    private Method commonsManagerGetPostMethod = null;
    private Method commonsPostGetCreatorIdMethod = null;
    private Method commonsPostGetSiteIdMethod = null;
    private Method commonsPostGetCommentsMethod = null;
    private Method commonsCommentGetCreatorIdMethod = null;

    private boolean commonsInstalled = false;

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

        if (serverConfigurationService.getBoolean("useBullhornAlerts", true)) {
            HANDLED_EVENTS.add(ProfileConstants.EVENT_WALL_ITEM_NEW);
            HANDLED_EVENTS.add(ProfileConstants.EVENT_WALL_ITEM_COMMENT_NEW);
            HANDLED_EVENTS.add(ProfileConstants.EVENT_STATUS_UPDATE);
            HANDLED_EVENTS.add(ProfileConstants.EVENT_FRIEND_REQUEST);
            HANDLED_EVENTS.add(ProfileConstants.EVENT_FRIEND_CONFIRM);
            HANDLED_EVENTS.add(ProfileConstants.EVENT_FRIEND_IGNORE);
            HANDLED_EVENTS.add(ProfileConstants.EVENT_MESSAGE_SENT);
            HANDLED_EVENTS.add(AnnouncementService.SECURE_ANNC_ADD);
            HANDLED_EVENTS.add(AssignmentConstants.EVENT_ADD_ASSIGNMENT);
            HANDLED_EVENTS.add(AssignmentConstants.EVENT_GRADE_ASSIGNMENT_SUBMISSION);
            HANDLED_EVENTS.add(ASSESSMENT_PUBLISH);
            HANDLED_EVENTS.add(COMMONS_COMMENT_CREATED);
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
                                String siteId = "~" + to;
                                Site site = siteService.getSite(siteId);
                                String toolId = site.getToolForCommonId("sakai.profile2").getId();
                                String url = serverConfigurationService.getPortalUrl() + "/site/" + siteId
                                                                        + "/tool/" + toolId + "/profile/wall";
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
                            String oldUserId = switchUser(to);
                            String url = profileLinkLogic.getInternalDirectUrlToUserConnections();
                            switchUser(oldUserId);
                            doSocialInsert(from, to, event, ref, e.getEventTime(), url);
                            countCache.remove(from);
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
                            if(post != null) {
                                Set<String> tos = new HashSet<String>();
                                String siteId = (String) commonsPostGetSiteIdMethod.invoke(post, new Object[] {});
                                String to = (String) commonsPostGetCreatorIdMethod.invoke(post, new Object[] {});
                                tos.add(to);
                                List<Object> comments = (List <Object>) commonsPostGetCommentsMethod.invoke(post, new Object[] {});
                                for(Object comment : comments) {
                                    to = (String) commonsCommentGetCreatorIdMethod.invoke(comment, new Object[] {});
                                    tos.add(to);
                                }
                                sendCommentAlerts(from, event, ref, e, siteId, postId, countCache, tos);
                            }
                        } else if (AnnouncementService.SECURE_ANNC_ADD.equals(event)) {
                            String siteId = pathParts[3];
                            String announcementId = pathParts[pathParts.length - 1];

                            SecurityAdvisor sa = unlock();
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
                        } /*else if (ASSESSMENT_PUBLISH.equals(event)) {
                            String siteId = e.getContext();
                            String assessmentId = ref.split(",")[1].split("=")[1];
                            System.out.println("Asessment Id: " + assessmentId);
                            System.out.println("site Id: " + siteId);
                            try {
                                Site site = siteService.getSite(siteId);
                                String toolId = site.getToolForCommonId("sakai.samigo").getId();
                                String url = serverConfigurationService.getPortalUrl() + "/directtool/" + toolId;
                                //AssessmentServiceAPI assessmentService = samigoApi.getAssessmentServiceAPI();
                                //AssessmentIfc assessment = assessmentService.getAssessment(assessmentId);
                                //String title = assessment.getTitle();
                                String title = "Unknown";
                                for (String  to : site.getUsersIsAllowed("assessment.takeAssessment")) {
                                    doAcademicInsert(from, to, event, ref, title, siteId, e.getEventTime(), url);
                                }
                            } catch (IdUnusedException idue) {
                                log.error("Failed to find the site", idue);
                            }
                        }*/
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
            , BULLHORN_INSERT_SQL
            , new Object[] {PortalService.ACADEMIC, from, to, event, ref, title, siteId, eventTime, url}
            , "ID");
    }

    private void doSocialInsert(String from, String to, String event, String ref, Date eventTime ,String url) {

        sqlService.dbInsert(null
            , BULLHORN_INSERT_SQL
            , new Object[] {PortalService.SOCIAL, from, to, event, ref, "", "", eventTime, url}
            , "ID");
    }

    private void sendCommentAlerts(String from, String event, String ref, Event e, String siteId, String postId, Cache countCache, Set<String> tos) {
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
}
