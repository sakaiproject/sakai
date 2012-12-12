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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.gradebook.shared.ExternalAssignmentProvider;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * Provides info to the gradebook about which assessments are visible
 */
public class AssessmentGradeInfoProvider implements ExternalAssignmentProvider {

    private Log log = LogFactory.getLog(AssessmentGradeInfoProvider.class);
    private GradebookExternalAssessmentService geaService;
    private UserDirectoryService userDirectoryService;
    private SiteService siteService;

    public void init() {
        log.info("INIT and Register Samigo AssessmentGradeInfoProvider");
        geaService.registerExternalAssignmentProvider(this);
    }

    public void destroy() {
        log.info("DESTROY and unregister Samigo AssessmentGradeInfoProvider");
        geaService.unregisterExternalAssignmentProvider(getAppKey());
    }

    public String getAppKey() {
        return "samigo";
    }

    private PublishedAssessmentIfc getPublishedAssessment(String id) {
        PublishedAssessmentService pas = new PublishedAssessmentService();
        PublishedAssessmentIfc a;
        try {
            a = pas.getPublishedAssessment(id);
        } catch (Exception e) {
            // NumberFormatException is thrown on non-numeric IDs
            if (log.isDebugEnabled()) {
                log.debug("Assessment lookup failed for ID: " + id + " -- " + e.getMessage());
            }
            a = null;
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
}

