/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.servlet.delivery;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.select.SelectAssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.LinearAccessDeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import lombok.extern.slf4j.Slf4j;

/**
 * Servlet for handling assessment delivery via published URL.
 *
 * <p>This servlet processes requests to access published assessments through direct URLs,
 * handling both anonymous and authenticated access. It manages authentication, authorization,
 * group membership validation, and routes users to the appropriate assessment delivery interface
 * or review pages.</p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Validates assessment alias from URL parameters</li>
 *   <li>Authenticates users (including anonymous access when configured)</li>
 *   <li>Authorizes access based on site membership and group releases</li>
 *   <li>Routes to assessment taking or review interfaces</li>
 *   <li>Handles preview mode for instructors</li>
 *   <li>Manages assessment delivery state and session configuration</li>
 * </ul>
 */
@Slf4j
public class LoginServlet extends HttpServlet {

    private static final String PARAM_ID = "id";
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_FROM_DIRECT = "fromDirect";
    private static final String ACTION_REVIEW = "review";

    private static final String PATH_ASSESSMENT_NOT_AVAILABLE = "/jsf/delivery/assessmentNotAvailable.faces";
    private static final String PATH_BEGIN_TAKING_ASSESSMENT = "/jsf/delivery/beginTakingAssessment.faces";
    private static final String PATH_REVIEW_INDEX = "/jsf/review/reviewIndex.faces";
    private static final String PATH_IS_REMOVED = "/jsf/delivery/isRemoved.faces";
    private static final String PATH_IS_RETRACTED = "/jsf/delivery/isRetracted.faces";
    private static final String PATH_IS_RETRACTED_FOR_EDIT = "/jsf/delivery/isRetractedForEdit.faces";
    private static final String PATH_DISCREPANCY_IN_DATA = "/jsf/delivery/discrepancyInData.faces";
    private static final String PATH_ASSESSMENT_SUBMITTED = "/jsf/delivery/assessmentHasBeenSubmitted.faces";
    private static final String PATH_NO_SUBMISSION_LEFT = "/jsf/delivery/noSubmissionLeft.faces";
    private static final String PATH_NO_LATE_SUBMISSION = "/jsf/delivery/noLateSubmission.faces";
    private static final String PATH_TIME_EXPIRED = "/jsf/delivery/timeExpired.faces";
    private static final String PATH_ACCESS_DENIED = "/jsf/delivery/accessDenied.faces";
    private static final String PATH_SECURE_DELIVERY_ERROR = "/jsf/delivery/secureDeliveryError.faces";
    private static final String PATH_DELIVER_ASSESSMENT = "/jsf/delivery/deliverAssessment.faces";
    private static final String PATH_AUTHN_LOGIN = "/authn/login";

    @Autowired private SiteService siteService;
    @Autowired private UserDirectoryService userDirectoryService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doPost(req, res);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        String alias = req.getParameter(PARAM_ID);
        if (StringUtils.isEmpty(alias)) {
            log.warn("Missing required id parameter for published URL");
            forwardTo(req, res, PATH_ASSESSMENT_NOT_AVAILABLE);
            return;
        }

        String action = req.getParameter(PARAM_ACTION);
        if (ACTION_REVIEW.equals(action)) {
            doReviewAssessment(req, res, alias);
        } else {
            doTakeAssessment(req, res, alias);
        }
    }

    public void doReviewAssessment(HttpServletRequest req, HttpServletResponse res, String alias)
            throws ServletException, IOException {
        PublishedAssessmentService service = new PublishedAssessmentService();
        PublishedAssessmentFacade publishedAssessment = service.getPublishedAssessmentIdByAlias(alias);

        if (publishedAssessment == null) {
            log.warn("Published assessment not found for alias: {}", alias);
            forwardTo(req, res, PATH_ASSESSMENT_NOT_AVAILABLE);
            return;
        }

        String siteId = publishedAssessment.getOwnerSiteId();
        setSkinFolder(req, siteId);
        boolean isInstructor = PersistenceService.getInstance()
                .getAuthzQueriesFacade()
                .hasPrivilege(SamigoConstants.AUTHZ_EDIT_ASSESSMENT_ANY, siteId);

        // If this is called by an instructor or user is not authenticated, handle redirect in doTakeAssignment
        if (isInstructor || StringUtils.isEmpty(req.getRemoteUser())) {
            doTakeAssessment(req, res, alias);
            return;
        }

        DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBeanFromExternalServlet("delivery", req, res);
        delivery.setSiteId(siteId);

        delivery.setAccessByUrlAndAuthorized(true);

        String assessmentId = publishedAssessment.getPublishedAssessmentId().toString();

        SelectAssessmentBean select = (SelectAssessmentBean) ContextUtil.lookupBeanFromExternalServlet("select", req, res);
        // Set to 3 to initiate the right view
        select.setDisplayAllAssessments("3");
        select.setReviewAssessmentId(assessmentId);

        SelectActionListener listener = new SelectActionListener();
        listener.processAction(null);

        // Redirect to review-view
        forwardTo(req, res, PATH_REVIEW_INDEX);
    }

    public void doTakeAssessment(HttpServletRequest req, HttpServletResponse res, String alias)
            throws ServletException, IOException {

        HttpSession httpSession = req.getSession(true);
        httpSession.setMaxInactiveInterval(3600); // one hour
        PersonBean person = (PersonBean) ContextUtil.lookupBeanFromExternalServlet("person", req, res);
        // we are going to use the delivery bean to flag that this access is via url
        // this is the flag that we will use in deliverAssessment.jsp to decide what
        // button to display - daisyf
        DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBeanFromExternalServlet("delivery", req, res);
        // For SAK-7132. 
        // As this class is only used for taking assessment via URL, 
        // there should not be any assessment grading data at this point
        delivery.setAssessmentGrading(null);

        PublishedAssessmentService service = new PublishedAssessmentService();
        PublishedAssessmentFacade pub = service.getPublishedAssessmentIdByAlias(alias);
        if (pub == null) {
            log.warn("No published assessment found with id [{}]", alias);
            forwardTo(req, res, PATH_ASSESSMENT_NOT_AVAILABLE);
            return;
        }

        String siteId = pub.getOwnerSiteId();
        setSkinFolder(req, siteId);

        boolean isInstructor = PersistenceService.getInstance()
                .getAuthzQueriesFacade()
                .hasPrivilege(SamigoConstants.AUTHZ_EDIT_ASSESSMENT_ANY, siteId);

        if (isInstructor) {
            delivery.setActionString("previewAssessment");
        } else {
            delivery.setSiteId(siteId);
            delivery.setActionString("takeAssessmentViaUrl");
        }

        // reset the timer in case this is a timed assessment
        delivery.setTimeElapse("0");
        delivery.setLastTimer(0);
        delivery.setTimeLimit("0");
        delivery.setBeginAssessment(true);

        delivery.setNumberRetake(-1);
        delivery.setActualNumberRetake(-1);

        // set path
        delivery.setContextPath(req.getContextPath());

        // 1. get publishedAssessment and check if anonymous is allowed
        // 2. If so, goto welcome.faces
        // 3. If not, goto login.faces
        // both pages will set agentId and then direct the user to BeginAssessment

        delivery.setAssessmentId(pub.getPublishedAssessmentId().toString());
        delivery.setAssessmentTitle(pub.getTitle());
        boolean honorPledge = BooleanUtils.toBoolean(pub.getAssessmentAccessControl().getHonorPledge());
        delivery.setHonorPledge(honorPledge);
        delivery.setPublishedAssessment(pub);

        BeginDeliveryActionListener listener = new BeginDeliveryActionListener();
        listener.populateBeanFromPub(delivery, pub);

        if (!isInstructor) {
            listener.processAction(null);
        }

        String path;
        String agentIdString;
        boolean relativePath = true;
        boolean isAuthorized = false;
        boolean isAuthenticated = false;

        // Determine if the assessment accepts Anonymous Users. If so, starting in version 2.0.1,
        // all users will be authenticated as anonymous for the assessment in this case.
        // boolean anonymousAllowed = false;
        String releaseTo = pub.getAssessmentAccessControl().getReleaseTo();
        if (releaseTo != null && releaseTo.contains("Anonymous Users")) {
            // anonymousAllowed = true;
            agentIdString = AgentFacade.createAnonymous();
            isAuthenticated = true;
            isAuthorized = true;
            delivery.setAnonymousLogin(true);
            person.setAnonymousId(agentIdString);
        } else { // check membership
            agentIdString = req.getRemoteUser();
            isAuthenticated = (agentIdString != null && !agentIdString.isEmpty());
            if (isAuthenticated) {
                if (releaseTo != null && releaseTo.contains(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
                    isAuthorized = checkMembershipForGroupRelease(pub);
                } else {
                    isAuthorized = checkMembership(pub);
                }
                // in 2.2, agentId is different from req.getRemoteUser()
                agentIdString = AgentFacade.getAgentString();
            }
            delivery.setAnonymousLogin(false);
            delivery.setAccessByUrlAndAuthorized(isAuthorized);
            person.setAnonymousId(null);
        }

        log.debug("Resolved agent id: {}", agentIdString);

        String nextAction = delivery.checkFromViaUrlLogin();
        log.debug("Resolved delivery nextAction: {}", nextAction);
        if (isAuthorized) {
            path = resolveAuthorizedPath(nextAction);
        } else { // notAuthorized
            if (!isAuthenticated) {
                relativePath = false;
                delivery.setActionString(null);
                path = buildLoginRedirectPath(req, alias);
            } else { // isAuthenticated but not authorized
                path = PATH_ACCESS_DENIED;
                if (releaseTo == null || !releaseTo.contains(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)) {
                    // log access denied because they are not in a valid group for the quiz
                    delivery.updatEventLog("error_access_denied");
                }
            }
        }
        if ("true".equals(req.getParameter(PARAM_FROM_DIRECT))) {
            String deliveryValidate = delivery.validate();
            // This has to be set up if it's coming from direct otherwise it doesn't start right
            UIComponent uic = new UICommand();
            uic.setId("beginAssessment");
            ActionEvent ae = new ActionEvent(uic);

            // send the user directly into taking the assessment... they already clicked start from the direct servlet
            if ("1".equals(StringUtils.trim(delivery.getNavigation()))) {
                LinearAccessDeliveryActionListener linearDeliveryListener = new LinearAccessDeliveryActionListener();
                linearDeliveryListener.processAction(ae);
            } else {
                DeliveryActionListener deliveryListener = new DeliveryActionListener();
                deliveryListener.processAction(ae);
            }

            if ("takeAssessment".equals(deliveryValidate)) {
                path = PATH_DELIVER_ASSESSMENT;
            }
        }
        log.debug("Resolved view path: {}", path);
        if (relativePath) {
            forwardTo(req, res, path);
        } else {
            log.info("Servlet request URL: {}", req.getRequestURL().toString());
            String url = req.getRequestURL().toString();
            String context = req.getContextPath();
            String finalUrl = url.substring(0, url.lastIndexOf(context)) + path;
            log.info("Redirecting to final URL: {}", finalUrl);
            res.sendRedirect(finalUrl);
        }
    }

    private boolean checkMembership(PublishedAssessmentFacade pub) {
        boolean isMember = false;
        // get list of site that this published assessment has been released to
        List<AuthorizationData> l = PersistenceService.getInstance().getAuthzQueriesFacade().
                getAuthorizationByFunctionAndQualifier("VIEW_PUBLISHED_ASSESSMENT",
                        pub.getPublishedAssessmentId().toString());
        for (AuthorizationData authorizationData : l) {
            String siteId = authorizationData.getAgentIdString();
            isMember = PersistenceService.getInstance().getAuthzQueriesFacade().checkMembership(siteId);
            if (isMember) break;
        }
        return isMember;
    }

    private boolean checkMembershipForGroupRelease(PublishedAssessmentFacade pub) {
        boolean isMember = false;
        // get the site that owns the published assessment
        List<AuthorizationData> aData = PersistenceService.getInstance().getAuthzQueriesFacade().
                getAuthorizationByFunctionAndQualifier("OWN_PUBLISHED_ASSESSMENT",
                        pub.getPublishedAssessmentId().toString());
        if (aData == null || aData.isEmpty()) return false;

        String siteId = aData.get(0).getAgentIdString();
        String currentUserId = userDirectoryService.getCurrentUser().getId();

        Collection<Group> siteGroupsContainingUser = siteService.getOptionalSite(siteId).map(s -> s.getGroupsWithMember(currentUserId)).orElse(Collections.emptyList());

        // get a list of groups that this published assessment has been released to
        aData = PersistenceService.getInstance().getAuthzQueriesFacade().
                getAuthorizationByFunctionAndQualifier("TAKE_PUBLISHED_ASSESSMENT", pub.getPublishedAssessmentId().toString());
        for (AuthorizationData aDatum : aData) {
            String groupId = aDatum.getAgentIdString();
            isMember = isUserInAuthorizedGroup(groupId, siteGroupsContainingUser);
            if (isMember) break;
        }
        return isMember;
    }

    private boolean isUserInAuthorizedGroup(String authorizedGroupId, Collection<Group> userGroups) {
        if (userGroups == null || StringUtils.isBlank(authorizedGroupId)) return false;
        return userGroups.stream().anyMatch(group -> group.getId().equals(authorizedGroupId));
    }

    private String resolveAuthorizedPath(String nextAction) {
        if ("safeToProceed".equals(nextAction)) {
            return PATH_BEGIN_TAKING_ASSESSMENT;
        }
        if ("assessmentNotAvailable".equals(nextAction)) {
            return PATH_ASSESSMENT_NOT_AVAILABLE;
        }
        if ("isRemoved".equals(nextAction)) {
            return PATH_IS_REMOVED;
        }
        if ("isRetracted".equals(nextAction)) {
            return PATH_IS_RETRACTED;
        }
        if ("isRetractedForEdit".equals(nextAction)) {
            return PATH_IS_RETRACTED_FOR_EDIT;
        }
        if ("discrepancyInData".equals(nextAction)) {
            return PATH_DISCREPANCY_IN_DATA;
        }
        if ("assessmentHasBeenSubmitted".equals(nextAction)) {
            return PATH_ASSESSMENT_SUBMITTED;
        }
        if ("noSubmissionLeft".equals(nextAction)) {
            return PATH_NO_SUBMISSION_LEFT;
        }
        if ("noLateSubmission".equals(nextAction)) {
            return PATH_NO_LATE_SUBMISSION;
        }
        if ("timeExpired".equals(nextAction)) {
            return PATH_TIME_EXPIRED;
        }
        if ("accessDenied".equals(nextAction)) {
            return PATH_ACCESS_DENIED;
        }
        if ("secureDeliveryError".equals(nextAction)) {
            return PATH_SECURE_DELIVERY_ERROR;
        }
        return PATH_ASSESSMENT_NOT_AVAILABLE;
    }

    private String buildLoginRedirectPath(HttpServletRequest req, String alias) {
        String originalUrl = req.getRequestURL().toString();
        String query = req.getQueryString();
        if (StringUtils.isNotBlank(query)) {
            originalUrl += "?" + query;
        } else {
            originalUrl += "?" + PARAM_ID + "=" + alias;
        }
        return PATH_AUTHN_LOGIN + "?url=" + URLEncoder.encode(originalUrl, StandardCharsets.UTF_8);
    }

    private void forwardTo(HttpServletRequest req, HttpServletResponse res, String path) throws ServletException, IOException {
        RequestDispatcher dispatcher = req.getRequestDispatcher(path);
        dispatcher.forward(req, res);
    }

    private void setSkinFolder(HttpServletRequest req, String siteId) {
        siteService.getOptionalSite(siteId).ifPresent(site -> {             
            String skinFolder = site.getSkin();
            if (StringUtils.isNotBlank(skinFolder)) {
                req.setAttribute("sakai.skin.folder", skinFolder);
            }
        });
    }
}
