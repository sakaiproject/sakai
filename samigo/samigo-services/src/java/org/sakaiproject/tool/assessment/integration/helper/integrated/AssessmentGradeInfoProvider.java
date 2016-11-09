/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.integration.helper.integrated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.service.gradebook.shared.ExternalAssignmentProvider;
import org.sakaiproject.service.gradebook.shared.ExternalAssignmentProviderCompat;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.user.api.UserDirectoryService;


/**
 * Provides info to the gradebook about which assessments are visible
 */
public class AssessmentGradeInfoProvider implements ExternalAssignmentProvider, ExternalAssignmentProviderCompat {

    private Logger log = LoggerFactory.getLogger(AssessmentGradeInfoProvider.class);
    private GradebookExternalAssessmentService geaService;
    private UserDirectoryService userDirectoryService;
    private SiteService siteService;
    private MemoryService memoryService;

    private Cache groupedCache;
    private Cache pubAssessmentCache;
    
    public void init() {
        log.info("INIT and Register Samigo AssessmentGradeInfoProvider");
        geaService.registerExternalAssignmentProvider(this);
        groupedCache = memoryService.newCache("org.sakaiproject.tool.assessment.integration.helper.integrated.AssessmentGradeInfoProvider.groupedCache");
        pubAssessmentCache = memoryService.newCache("org.sakaiproject.tool.assessment.integration.helper.integrated.AssessmentGradeInfoProvider.pubAssessmentCache");
    }

    public void destroy() {
        log.info("DESTROY and unregister Samigo AssessmentGradeInfoProvider");
        geaService.unregisterExternalAssignmentProvider(getAppKey());
    }

    public String getAppKey() {
        return "samigo";
    }

    
    private PublishedAssessmentIfc getPublishedAssessment(String id) {
        // SAM-3068 avoid looking up another tool's id
        if (!StringUtils.isNumeric(id)) {
            return null;
        }

        PublishedAssessmentIfc a = (PublishedAssessmentIfc) pubAssessmentCache.get(id);
        if (a != null) {
            log.debug("Returning assessment {} from cache", id);
            return a;
        }

        /* Below we may fail to re-establish the value */
        pubAssessmentCache.remove(id);

        PublishedAssessmentService pas = new PublishedAssessmentService();
        try {
            a = pas.getPublishedAssessment(id);
            pubAssessmentCache.put(id, a);
        } catch (Exception e) {
            // NumberFormatException is thrown on non-numeric IDs
            log.debug("Assessment lookup failed for ID: {} -- {}", id, e.getMessage());
            a = null;
            /* The present cache cannot cache nulls, so nothing to do here. */
            /* If this ever changes, caching this reply might be a good idea */
        }
        return a;
    }

    public boolean isAssignmentDefined(String id) {
        if (log.isDebugEnabled()) {
            log.debug("Samigo provider isAssignmentDefined: " + id);
        }
        return getPublishedAssessment(id) != null;
    }

    
    public boolean isAssignmentGrouped(String id) {
        if (log.isDebugEnabled()) {
            log.debug("Samigo provider isAssignmentGrouped: " + id);
        }
        
        Boolean g = null;
        if (groupedCache.containsKey(id)) {
            g = (Boolean)groupedCache.get(id);
            if(g != null) {
                if (log.isDebugEnabled()) {
                    log.debug("returning grouped value from cache: " + id);
                }
                return (boolean) g;
            }
        }
        
        PublishedAssessmentService pas = new PublishedAssessmentService();
        boolean grouped = false;
        try {
            grouped = pas.isReleasedToGroups(id);
        } catch (Exception e) {
            //isReleasedToGroups does not error check
            if (log.isDebugEnabled()) {
                log.debug("Assignment lookup failed for ID: " + id + " -- " + e.getMessage());
            }
        }
        groupedCache.put(id, grouped);
        return grouped;
    }

    //FIXME: Visibility logic is ripped from LoginServlet, modified some for params we have here
    //TODO: Refactor so that permissions logic is exposed in a service method somewhere
    public boolean isAssignmentVisible(String id, String userId) {
        if (log.isDebugEnabled()) {
            log.debug("Samigo provider isAssignmentVisible: " + id + ", " + userId);
        }

        PublishedAssessmentIfc pub = getPublishedAssessment(id);
        if (pub == null) {
            return false;
        }

        boolean isAuthorized = false;
        boolean isAuthenticated = false;

        String releaseTo = pub.getAssessmentAccessControl().getReleaseTo();
        if (releaseTo != null && releaseTo.indexOf("Anonymous Users")> -1){
            isAuthenticated = true;
            isAuthorized = true;
        }
        else { // check membership
            isAuthenticated = ( userId != null && !("").equals(userId));
            if (isAuthenticated){
                if (releaseTo.indexOf(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)>-1) {
                    isAuthorized = checkMembershipForGroupRelease(pub, userId);
                }
                else {
                    isAuthorized = checkMembership(pub, userId);
                }
            }
        }
        return isAuthorized;
    }

    public List<String> getExternalAssignmentsForCurrentUser(String gradebookUid) {
        List all = PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
            getBasicInfoOfAllPublishedAssessments("title", true, gradebookUid);

        ArrayList<String> externalIds = new ArrayList<String>();
        for (PublishedAssessmentIfc pub : (List<PublishedAssessmentIfc>) all) {
            externalIds.add(pub.getPublishedAssessmentId().toString());
        }
        return externalIds;
    }

    public List<String> getAllExternalAssignments(String gradebookUid) {
        List allPublished = PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
            getBasicInfoOfAllPublishedAssessments2("title", true, gradebookUid);

        List<String> allExternals = new ArrayList<String>();
        for (PublishedAssessmentFacade pub : (List<PublishedAssessmentFacade>) allPublished) {
            String assessmentId = pub.getPublishedAssessmentId().toString();
            allExternals.add(assessmentId);
        }
        return allExternals;
    }

    public Map<String, List<String>> getAllExternalAssignments(String gradebookUid, Collection<String> studentIds) {
        List allPublished = PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
            getBasicInfoOfAllPublishedAssessments2("title", true, gradebookUid);

        //TODO: Update PublishedAssessmentFacadeQueriesAPI to return a list of group IDs
        //      or member lists instead of only group titles.
        //      This borrows some releasedTo logic to keep a narrower patch for now.
        Map<String, Set<String>> allExternals = new HashMap<String, Set<String>>();
        for (String studentId : studentIds) {
            allExternals.put(studentId, new HashSet<String>());
        }

        Set<String> siteUserIds = getSiteUserIds(gradebookUid);
        Map<String, Set<String>> userIdGroupIds = getUserGroups(gradebookUid, studentIds);
        Map<String, Set<String>> groupIdUserIds = invertMapSet(userIdGroupIds);

        // Get all groups for site, and all members therein
        // Get access control for each assessment
        //   1: anonymous - all site users
        //   2: no specific groups - all site users
        //   3: specific groups - all users in all specified groups
        for (PublishedAssessmentFacade pub : (List<PublishedAssessmentFacade>) allPublished) {
            String assessmentId = pub.getPublishedAssessmentId().toString();
            String releaseTo = pub.getReleaseTo();
            if (releaseTo != null && assessmentId != null) {
                if (releaseTo.indexOf("Anonymous Users")> -1) {
                    for (String studentId : studentIds) {
                        allExternals.get(studentId).add(assessmentId);
                    }
                } else if (AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS.equals(releaseTo)) {
                    //TODO: Add a signature to AuthzQueriesFacadeAPI to get authorized groups for
                    //      a set of assessments, rather than just one.
                    Set<String> authorizedGroups = getAuthorizedGroups(assessmentId);
                    for (String groupId : authorizedGroups) {
                        if (groupIdUserIds.containsKey(groupId)) {
                            for (String userId : groupIdUserIds.get(groupId)) {
                                if (allExternals.containsKey(userId)) {
                                    allExternals.get(userId).add(assessmentId);
                                }
                            }
                        }
                    }
                } else {
                    for (String studentId : studentIds) {
                        if (siteUserIds.contains(studentId)) {
                            allExternals.get(studentId).add(assessmentId);
                        }
                    }
                }
            }
        }

        Map<String, List<String>> allExternalsList = new HashMap<String, List<String>>();
        for (String studentId : allExternals.keySet()) {
            allExternalsList.put(studentId, new ArrayList<String>(allExternals.get(studentId)));
        }
        return allExternalsList;
    }

    private Set<String> getAuthorizedGroups(String assessmentId) {
        List authorizations = PersistenceService.getInstance().getAuthzQueriesFacade()
            .getAuthorizationByFunctionAndQualifier("TAKE_PUBLISHED_ASSESSMENT", assessmentId);
        Set<String> authorizedGroups = new HashSet<String>();
        if (authorizations != null && authorizations.size()>0) {
            Iterator authsIter = authorizations.iterator();
            while (authsIter.hasNext()) {
                AuthorizationData ad = (AuthorizationData) authsIter.next();
                authorizedGroups.add(ad.getAgentIdString());
            }
        }
        return authorizedGroups;
    }

    private Set<String> getSiteUserIds(String siteId) {
        Set<String> userIds = new HashSet<String>();
        try {
            Site site = siteService.getSite(siteId);
            for (Member m : site.getMembers()) {
                userIds.add(m.getUserId());
            }
        } catch (IdUnusedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Site not found when attempting to retrieve its users: " + siteId);
            }
        }
        return userIds;
    }

    // Retrieve a map of student ID -> group IDs for a list of users in a site
    private Map<String, Set<String>> getUserGroups(String siteId, Collection<String> studentIds) {
        Map<String, Set<String>> userIdGroupIds = new HashMap<String, Set<String>>();
        for (String studentId : studentIds) {
            userIdGroupIds.put(studentId, new HashSet<String>());
        }
        try {
            Site site = siteService.getSite(siteId);
            for (Group g : site.getGroups()) {
                for (Member m : g.getMembers()) {
                    String userId = m.getUserId();
                    if (userIdGroupIds.containsKey(userId)) {
                        userIdGroupIds.get(userId).add(g.getId());
                    }
                }
            }
        } catch (IdUnusedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Site not found when attempting to retrieve user groups: " + siteId);
            }
        }
        return userIdGroupIds;
    }

    private Map<String, Set<String>> invertMapSet(Map<String, Set<String>> mapSet) {
        Map<String, Set<String>> inverted = new HashMap<String, Set<String>>();
        for (String key : mapSet.keySet()) {
            Set<String> values = mapSet.get(key);
            for (String value : values) {
                if (!inverted.containsKey(value)) {
                    inverted.put(value, new HashSet<String>());
                }
                inverted.get(value).add(key);
            }
        }
        return inverted;
    }

    private boolean checkMembership(PublishedAssessmentIfc pub, String userId){
        boolean isMember=false;
        // get list of site that this published assessment has been released to
        List l = PersistenceService.getInstance().getAuthzQueriesFacade().
        getAuthorizationByFunctionAndQualifier("VIEW_PUBLISHED_ASSESSMENT",
                pub.getPublishedAssessmentId().toString());
        for (int i=0;i<l.size();i++){
            String siteId = ((AuthorizationData)l.get(i)).getAgentIdString();
            try {
                isMember = (siteService.getSite(siteId).getUserRole(userId) != null);
            } catch (IdUnusedException e) {
                log.info("Site with ID: " + siteId + " does not exists but is "
                        + "authorized for assessment id: " + pub.getPublishedAssessmentId());
            }
            if (isMember) {
                break;
            }
        }
        return isMember;
    }

    private boolean checkMembershipForGroupRelease(PublishedAssessmentIfc pub, String userId){
        boolean isMember=false;
        // get the site that owns the published assessment
        List l =PersistenceService.getInstance().getAuthzQueriesFacade().
        getAuthorizationByFunctionAndQualifier("OWN_PUBLISHED_ASSESSMENT",
                pub.getPublishedAssessmentId().toString());
        if (l == null || l.isEmpty()) {
            return false;
        }
        String siteId = ((AuthorizationData)l.get(0)).getAgentIdString();
        Collection siteGroupsContainingUser = null;
        try {
            siteGroupsContainingUser = siteService.getSite(siteId).getGroupsWithMember(userId);
        }
        catch (IdUnusedException ex) {
            // no site found
        }

        // get list of groups that this published assessment has been released to
        l =PersistenceService.getInstance().getAuthzQueriesFacade().
        getAuthorizationByFunctionAndQualifier("TAKE_PUBLISHED_ASSESSMENT",
                pub.getPublishedAssessmentId().toString());
        for (int i=0;i<l.size();i++){
            String groupId = ((AuthorizationData)l.get(i)).getAgentIdString();
            isMember = isUserInAuthorizedGroup(groupId, siteGroupsContainingUser);
            if (isMember) {
                break;
            }
        }
        return isMember;
    }

    private boolean isUserInAuthorizedGroup(String authorizedGroupId, Collection userGroups) {
        if (userGroups==null || userGroups.isEmpty()
                || authorizedGroupId==null || authorizedGroupId.equals("")) {
            return false;
        }
        Iterator userGroupsIter = userGroups.iterator();
        while (userGroupsIter.hasNext()) {
            Group group = (Group) userGroupsIter.next();
            if (group.getId().equals(authorizedGroupId)) {
                return true;
            }
        }
        return false;
    }

    public GradebookExternalAssessmentService getGradebookExternalAssessmentService() {
        return geaService;
    }

    public void setGradebookExternalAssessmentService( GradebookExternalAssessmentService geaService) {
        this.geaService = geaService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public UserDirectoryService getUserDirectoryService() {
        return userDirectoryService;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
    
    public void setMemoryService(MemoryService memoryService) {
		this.memoryService = memoryService;
	}
}

