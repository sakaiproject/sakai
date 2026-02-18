/*
 * Copyright (c) 2003-2023 The Apereo Foundation
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
package org.sakaiproject.tool.assessment;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.messaging.api.AbstractUserNotificationHandler;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;


import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.facade.ExtendedTimeFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

import static org.sakaiproject.samigo.util.SamigoConstants.*;



/*
 *   Note: Restoring of soft deleted Assessments does not recreate bullhorn alerts!
 */

@Slf4j
@Component
public class TestsAndQuizzesUserNotificationHandler extends AbstractUserNotificationHandler {

    public static Pattern idPattern = Pattern.compile("siteId=(\\S*),\\s*\\S*\\s*publishedAssessmentId=(\\S*)", Pattern.CASE_INSENSITIVE);
    private PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();

    @Resource
    private SiteService siteService;

    @Resource
    private AuthzGroupService authzGroupService;

    @Resource
    private SessionManager sessionManager;

    @Resource
    private UserDirectoryService userDirectoryService;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    private PlatformTransactionManager transactionManager;
    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    private SessionFactory sessionFactory;
    private EventTrackingService eventTrackingService;


    public TestsAndQuizzesUserNotificationHandler(){
        super();
        eventTrackingService = ComponentManager.get(EventTrackingService.class);
    }

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(EVENT_ASSESSMENT_AVAILABLE, EVENT_PUBLISHED_ASSESSMENT_RETRACTED, EVENT_ASSESSMENT_UPDATE_AVAILABLE,EVENT_ASSESSMENT_DELETE, EVENT_PUBLISHED_ASSESSMENT_REMOVE);
    }


    @Override
    public Optional<List<UserNotificationData>> handleEvent(Event e) {

        String from = e.getUserId();
        String ref = e.getResource();


        List<String> refParts = regexHelper(ref);
        String siteId = refParts.get(0);
        String publishedAssessmentId = refParts.get(1);

        PublishedAssessmentFacade pub = null;
        try {
            pub = publishedAssessmentService.getPublishedAssessment(publishedAssessmentId, true);
        }catch(Exception e1){
            log.error(e1.getMessage());
        }
        String releaseTo = pub.getAssessmentAccessControl().getReleaseTo();

        ExtendedTimeFacade extendedTimeFacade = PersistenceService.getInstance().getExtendedTimeFacade();
        List<ExtendedTime> extendedTimes = extendedTimeFacade.getEntriesForPub(pub.getData());

        Map<String, String> selectedGroups = pub.getReleaseToGroups();


        if (!releaseTo.equals("Anonymous Users")) {
            try {
                switch (e.getEvent()) {
                    case EVENT_ASSESSMENT_AVAILABLE:
                        checkForDelays(pub, extendedTimes,siteId, e.getUserId());
                        return Optional.of(handleAdd(from,ref, siteId, pub, extendedTimes, selectedGroups, releaseTo));
                    case EVENT_ASSESSMENT_UPDATE_AVAILABLE:
                        checkForDelays(pub, extendedTimes,siteId,e.getUserId());
                        return Optional.of(handleUpdate(from, ref, siteId,pub, extendedTimes, selectedGroups, releaseTo));
                    case EVENT_PUBLISHED_ASSESSMENT_RETRACTED:
                    case EVENT_ASSESSMENT_DELETE:
                    case EVENT_PUBLISHED_ASSESSMENT_REMOVE:
                        return Optional.of(deleteAlerts(siteId, pub));
                    default:
                        return Optional.empty();
                }
            } catch (Exception ex) {
                log.error("Failed to handleEvent for Test&Quizzes userNotification alert", ex);
            }
        }
        return Optional.empty();
    }


    private List<UserNotificationData> handleAdd(String from, String ref ,String siteId, PublishedAssessmentFacade assignment, List<ExtendedTime> extendedTimes,  Map<String, String> selectedGroups, String releaseTo)
            throws Exception {
        List<UserNotificationData> bhEvents = new ArrayList<>();

        Instant startDateInstant = assignment.getStartDate().toInstant();
        Site site = siteService.getSite(siteId);
        String title = assignment.getTitle();

        Collection<String> groupsUsers = null;

        if (releaseTo.equals(AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS)) {
            groupsUsers =  getUsersInSelectedGroups(siteId,  selectedGroups);
        }

        Set<String> userUids = site.getUsersIsAllowed(AUTHZ_TAKE_ASSESSMENT);
        for (User u : userDirectoryService.getUsers(userUids)) {
            String to = u.getId();
            //  If this is a grouped assignment, is 'to' in one of the groups?
            if ((releaseTo.equals(site.getTitle()) || (groupsUsers != null && groupsUsers.contains(to))) && (!from.equals(to) && !securityService.isSuperUser(to)) && checkTime(startDateInstant,extendedTimes, to, siteId) && !bhAlreadyExistsForUser(ref, to)) {
                //link to tool
                String url = site.getUrl() + "/tool/" + site.getToolForCommonId(TOOL_ID).getId();
                bhEvents.add(new UserNotificationData(from, to, siteId, title, url, TOOL_ID, false, null));
            }
        }
        return bhEvents;
    }

    private List<UserNotificationData> handleUpdate(String from, String ref, String siteId, PublishedAssessmentFacade assignment, List<ExtendedTime> extendedTimes, Map<String, String> selectedGroups, String releaseTo)
            throws Exception {
        Site site = siteService.getSite(siteId);
        Set<String> siteUsers = site.getUsersIsAllowed(AUTHZ_TAKE_ASSESSMENT);

        Collection<String> groupsUsers = null;

        if (releaseTo.equals(AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS)) {
            groupsUsers =  getUsersInSelectedGroups(siteId,  selectedGroups);
        }

        if(!siteUsers.isEmpty()){
            try{
                TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                transactionTemplate.execute(status -> {
                    sessionFactory.getCurrentSession().createQuery("delete UserNotification where EVENT in :events and REF = :ref and TO_USER in :toUsers")
                            .setParameterList("events", new String[]{EVENT_ASSESSMENT_AVAILABLE, EVENT_ASSESSMENT_UPDATE_AVAILABLE})
                            .setString("ref", ref)
                            .setParameterList("toUsers", siteUsers).executeUpdate();
                    return null;
                });
            }catch (Exception e2){
                log.error("failed to delete TestAndQuizzes userNotification alerts: " + e2.getMessage());
            }
        }

        Instant startDateInstant = assignment.getStartDate().toInstant();
        String title = assignment.getTitle();

        List<UserNotificationData> bhEvents = new ArrayList<>();
        if (!releaseTo.equals("Anonymous Users")) {
            for (String to : siteUsers) {
                //  If this is a grouped assignment, is 'to' in one of the groups?
                if ((releaseTo.equals(siteId) || (groupsUsers != null && groupsUsers.contains(to))) && (!from.equals(to) && !securityService.isSuperUser(to)) && checkTime(startDateInstant, extendedTimes, to, siteId) && !bhAlreadyExistsForUser(ref, to)) {
                    //link to tool
                    String url = site.getUrl() + "/tool/" + site.getToolForCommonId(TOOL_ID).getId();
                    bhEvents.add(new UserNotificationData(from, to, siteId, title, url, TOOL_ID, false, null));
                }
            }
            return bhEvents;
        }
        return bhEvents;
    }

    private List<UserNotificationData> deleteAlerts(String siteId, PublishedAssessmentFacade assignment)
            throws IdUnusedException {
        Site site = siteService.getSite(siteId);
        Set<String> users = site.getUsersIsAllowed(AUTHZ_TAKE_ASSESSMENT);
        List<UserNotificationData> bhEvents = new ArrayList<>();
        // Clean out all the alerts for the site  users.

        String ref = "siteId="+siteId+", assessmentId=" +assignment.getAssessmentId()+", publishedAssessmentId="+ assignment.getPublishedAssessmentId();
        if(!users.isEmpty()){
            try{
                TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
                transactionTemplate.execute(status -> {
                    sessionFactory.getCurrentSession().createQuery("delete UserNotification where EVENT in :events and REF = :ref and TO_USER in :toUsers")
                            .setParameterList("events", new String[]{EVENT_ASSESSMENT_AVAILABLE, EVENT_ASSESSMENT_UPDATE_AVAILABLE})
                            .setString("ref", ref)
                            .setParameterList("toUsers", users).executeUpdate();
                    return null;
                });
            }catch (Exception e3){
                log.error("failed to delete TestAndQuizzes UserNotificationData data " + e3.getMessage());
            }
        }


        eventTrackingService.cancelDelays(ref, EVENT_ASSESSMENT_AVAILABLE);
        return bhEvents;
    }

    /*
     *   user overrides group exception
     *   select first extended time of list for user exceptions; for multiple group exception the last is picked --> related to issues SAK-45277, SAK-44729
     */

    private boolean checkTime(Instant startDateInstant , List <ExtendedTime> extendedTimes, String to, String siteId){
        ListIterator<ExtendedTime> times = extendedTimes.listIterator();
        boolean first = true;
        boolean result = true;
        boolean exTimeIsSet =false;

        while(times.hasNext()) {
            ExtendedTime exTime = (ExtendedTime) times.next();
            String user = exTime.getUser();

            Set<String> set = new HashSet<String>();
            set.add(getGroupRef(siteId, exTime.getGroup()));
            Collection<String> groupUsers = authzGroupService.getAuthzUsersInGroups(set);


            Instant startInstant = exTime.getStartDate().toInstant();

            if (startInstant.isBefore(Instant.now()) && (( user != null && user.equals(to) && first) || (first && groupUsers != null && groupUsers.contains(to)))) {
                result = true;
                exTimeIsSet = true;
                if(StringUtils.isNotEmpty(user)){
                    first = false;
                }
            } else if (startInstant.isAfter(Instant.now()) && ((user != null && user.equals(to) && first) || (first && groupUsers != null && groupUsers.contains(to)))) {
                result = false;
                exTimeIsSet = true;
                if(StringUtils.isNotEmpty(user)){
                    first = false;
                }
            }
        }
        if(!exTimeIsSet && startDateInstant.isAfter(Instant.now())){
            result = false;
        }
        return result;
    }

    /*
     *  sakai allows only one active delay for a certain reference
     *  after first event is fired after publishing --> check if a new delay should be created
     */

    private void checkForDelays(PublishedAssessmentFacade  assignment,List<ExtendedTime> extendedTimes,String siteId, String userId){
        Instant earliestDelayInstant = null;
        if(assignment.getStartDate().toInstant().isAfter(Instant.now())){
            earliestDelayInstant =  assignment.getStartDate().toInstant();
        }
        if (!extendedTimes.isEmpty()){
            for (ExtendedTime exTime : extendedTimes) {
                Instant exStartInstant = exTime.getStartDate().toInstant();
                if (exStartInstant.isAfter(Instant.now()) && (earliestDelayInstant != null && exStartInstant.isBefore(earliestDelayInstant))) {
                    earliestDelayInstant = exStartInstant;

                } else if (earliestDelayInstant != null && exStartInstant.isAfter(earliestDelayInstant)) {
                    //leave empty
                } else if (exStartInstant.isAfter(Instant.now())) {
                    earliestDelayInstant = exStartInstant;
                }
            }
        }
        if(earliestDelayInstant != null){
            // creating a new delay after an already delayed event, the sessionUserId is different from the original author of the assessment
            Session session = sessionManager.getCurrentSession();
            boolean flag = false;
            String tmpSessionUserId = null;
            if(!Objects.equals(session.getUserId(), userId)){
                tmpSessionUserId = session.getUserId();
                session.setUserId(userId);
                flag = true;
            }
            Event event = eventTrackingService.newEvent(EVENT_ASSESSMENT_AVAILABLE, "siteId=" + siteId + ", assessmentId=" + assignment.getAssessmentId() + ", publishedAssessmentId=" + assignment.getPublishedAssessmentId(), true);
            eventTrackingService.delay(event, earliestDelayInstant);
            if(flag){
                session.setUserId(tmpSessionUserId);
            }
        }
    }

    private boolean bhAlreadyExistsForUser(String ref, String toUser){
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return transactionTemplate.execute(status -> {
            Long bhWithRef = (Long) sessionFactory.getCurrentSession()
                    .createQuery("select count(*) from UserNotification where ref = :ref and event = :event and toUser = :toUser")
                    .setString("ref", ref).setString("event", EVENT_ASSESSMENT_AVAILABLE).setString("toUser", toUser).uniqueResult();
            return bhWithRef > 0;
        });
    }

    private <T> Collection<T> removeDuplicates(Collection<T> list){
        ArrayList<T> newList = new ArrayList<T>();
        for (T element : list) {
            if (!newList.contains(element)) {
                newList.add(element);
            }
        }
        return newList;
    }

    private String getGroupRef(String siteId, String groupId){
        return  "/site/" + siteId + "/group/" +  groupId;
    }

    private Collection<String> getUsersInSelectedGroups(String siteId, Map<String, String> selectedGroups){
        String[] groups = null;
        Set<String> groupIds = new HashSet<String>();
        Collection<String> groupsUsers = null;

        for(Map.Entry<String, String> entry: selectedGroups.entrySet()){
            String id = getGroupRef(siteId, entry.getKey());
            groupIds.add(id);
        }
        groupsUsers = authzGroupService.getAuthzUsersInGroups(groupIds);
        groupsUsers = removeDuplicates(groupsUsers);
        return groupsUsers;

    }

    public static List<String> regexHelper(String ref){
        Matcher matcher = idPattern.matcher(ref);
        String  siteId = null;
        String publishedAssessmentId = null;
        List<String> response = new ArrayList<String>();

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                siteId = matcher.group(1);
                response.add(siteId);
            }
            if (matcher.group(2) != null) {
                publishedAssessmentId = matcher.group(2);
                response.add(publishedAssessmentId);
            }
        }
        return response;

    }



}
