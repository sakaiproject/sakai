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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.LinearAccessDeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class LoginServlet
    extends HttpServlet
{
  /**
	 * 
	 */
	private static final long serialVersionUID = -5495078878170443939L;

	private SiteService siteService;

  public LoginServlet()
  {
  }

  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
  {
    doPost(req,res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
  {
	String alias = req.getParameter("id");
	if ((alias==null) ||("").equals(alias)){
		log.warn("The published URL you have entered is incorrect. id is missing. Please check in Published Settings.");
		return;
	}

    HttpSession httpSession = req.getSession(true);
    httpSession.setMaxInactiveInterval(3600); // one hour
    PersonBean person = (PersonBean) ContextUtil.lookupBeanFromExternalServlet(
                        "person", req, res);
    // we are going to use the delivery bean to flag that this access is via url
    // this is the flag that we will use in deliverAssessment.jsp to decide what
    // button to display - daisyf
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBeanFromExternalServlet(
       "delivery", req, res);
    // For SAK-7132. 
    // As this class is only used for taking assessment via URL, 
    // there should not be any assessment grading data at this point
    delivery.setAssessmentGrading(null);
    delivery.setActionString("takeAssessmentViaUrl");

    // reset timer in case this is a timed assessment
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
    // both pages will set agentId and then direct user to BeginAssessment
    PublishedAssessmentService service = new PublishedAssessmentService();
    PublishedAssessmentFacade pub = service.getPublishedAssessmentIdByAlias(alias);

    if (pub==null){
		log.warn("The published URL you have entered is incorrect. Please check in Published Settings.");
    	return;
    }
    delivery.setAssessmentId(pub.getPublishedAssessmentId().toString());
    delivery.setAssessmentTitle(pub.getTitle());
    Boolean honorPledge = BooleanUtils.toBoolean(pub.getAssessmentAccessControl().getHonorPledge());
    delivery.setHonorPledge(honorPledge);
    delivery.setPublishedAssessment(pub);

    BeginDeliveryActionListener listener = new BeginDeliveryActionListener();
    listener.populateBeanFromPub(delivery, pub);

    RequestDispatcher dispatcher = null;
    String path = "/jsf/delivery/invalidAssessment.faces";
    boolean relativePath = true;

    String agentIdString = "";
    boolean isAuthorized = false;
    boolean isAuthenticated = false;

      // Determine if assessment accept Anonymous Users. If so, starting in version 2.0.1
      // all users will be authenticated as anonymous for the assessment in this case.
      //boolean anonymousAllowed = false;
      String releaseTo = pub.getAssessmentAccessControl().getReleaseTo();
      if (releaseTo != null && releaseTo.indexOf("Anonymous Users")> -1){
        //anonymousAllowed = true;
        agentIdString = AgentFacade.createAnonymous();
        isAuthenticated = true;
        isAuthorized = true;
        delivery.setAnonymousLogin(true);
        person.setAnonymousId(agentIdString);
      }
      else { // check membership
    	agentIdString = req.getRemoteUser();
        isAuthenticated = ( agentIdString!= null && !("").equals(agentIdString));
        if (isAuthenticated){
          if (releaseTo.indexOf(AssessmentAccessControl.RELEASE_TO_SELECTED_GROUPS)>-1) {
        	  isAuthorized = checkMembershipForGroupRelease(pub, req, res);
          }
          else {
        	  isAuthorized = checkMembership(pub, req, res);
          }
          // in 2.2, agentId is differnt from req.getRemoteUser()
          agentIdString = AgentFacade.getAgentString();
        }
        delivery.setAnonymousLogin(false);
        person.setAnonymousId(null);
      }

      log.debug("*** agentIdString: "+agentIdString);
       
      String nextAction = delivery.checkFromViaUrlLogin();
      log.debug("nextAction="+nextAction);
      if (isAuthorized){
        // Assessment has been permanently removed
        if ("isRemoved".equals(nextAction)){
          path = "/jsf/delivery/isRemoved.faces";
        }
        // Assessment is available for taking
        else if ("safeToProceed".equals(nextAction)){
          // if assessment is available, set it in delivery bean for display in deliverAssessment.jsp
          listener.processAction(null);
          path = "/jsf/delivery/beginTakingAssessment_viaurl.faces";
        }
        // Assessment is currently not available (eg., retracted for edit, due date has passed, submission limit has been reached, etc)
        else if ("assessmentNotAvailable".equals(nextAction)){
        	path = "/jsf/delivery/assessmentNotAvailable.faces";
        }
        else if ("isRetracted".equals(nextAction)){
        	path = "/jsf/delivery/isRetracted.faces";
        }
        else if ("isRetractedForEdit".equals(nextAction)){
        	path = "/jsf/delivery/isRetractedForEdit.faces";
        }
        else if ("discrepancyInData".equals(nextAction)){
        	path = "/jsf/delivery/discrepancyInData.faces";
        }
        else if ("assessmentHasBeenSubmitted".equals(nextAction)){
        	path = "/jsf/delivery/assessmentHasBeenSubmitted.faces";
        }
        else if ("noSubmissionLeft".equals(nextAction)){
        	path = "/jsf/delivery/noSubmissionLeft.faces";
        }
        else if ("noLateSubmission".equals(nextAction)){
        	path = "/jsf/delivery/noLateSubmission.faces";
        }
        else if ("timeExpired".equals(nextAction)){
        	path = "/jsf/delivery/timeExpired.faces";
        }
        else {
        	path = "/jsf/delivery/assessmentNotAvailable.faces";
        }
      }
      else{ // notAuthorized
    	  if (!isAuthenticated){
    	      relativePath = false;
    	      delivery.setActionString(null);
    	      path = "/authn/login?url=" + URLEncoder.encode(req.getRequestURL().toString()+"?id="+alias, "UTF-8");
    	  }
    	  else { //isAuthenticated but not authorized
    		  path = "/jsf/delivery/accessDenied.faces";
    	  }
      }
      if ("true".equals(req.getParameter("fromDirect"))) {
        // send the user directly into taking the assessment... they already clicked start from the direct servlet
        if (delivery.getNavigation().trim() != null && "1".equals(delivery.getNavigation().trim())) {
          LinearAccessDeliveryActionListener linearDeliveryListener = new LinearAccessDeliveryActionListener();
          linearDeliveryListener.processAction(null);
        }
        else {
          DeliveryActionListener deliveryListener = new DeliveryActionListener();
          //This has to be setup if it's coming from direct otherwise it doesn't start right
          UIComponent uic = new UICommand();
          uic.setId("beginAssessment");
          ActionEvent ae = new ActionEvent(uic);
          deliveryListener.processAction(ae);
        }
        
        //TODO: Should be something a bit more robust as validate() can retun a lot of things...
        if ("takeAssessment".equals(delivery.validate())) {
          path = "/jsf/delivery/deliverAssessment.faces";
        }
        
      }
    log.debug("***path"+path);
    if (relativePath){
      dispatcher = req.getRequestDispatcher(path);
      dispatcher.forward(req, res);
    }
    else{
      log.info("** servlet path="+req.getRequestURL().toString());
      String url = req.getRequestURL().toString();
      String context = req.getContextPath();
      String finalUrl = url.substring(0,url.lastIndexOf(context))+path;
      log.info("**** finalUrl = "+finalUrl);
      res.sendRedirect(finalUrl);
    }
  }

  private boolean checkMembership(PublishedAssessmentFacade pub,
       HttpServletRequest req, HttpServletResponse res){
    boolean isMember=false;
    // get list of site that this published assessment has been released to
    List l =PersistenceService.getInstance().getAuthzQueriesFacade().
        getAuthorizationByFunctionAndQualifier("VIEW_PUBLISHED_ASSESSMENT",
        pub.getPublishedAssessmentId().toString());
    for (int i=0;i<l.size();i++){
      String siteId = ((AuthorizationData)l.get(i)).getAgentIdString();
      isMember = PersistenceService.getInstance().getAuthzQueriesFacade().
          checkMembership(siteId);
      if (isMember)
        break;
    }
    return isMember;
  }
  
  private boolean checkMembershipForGroupRelease(PublishedAssessmentFacade pub,
	       HttpServletRequest req, HttpServletResponse res){
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
		String currentUserId = UserDirectoryService.getCurrentUser().getId();
		try {
			siteGroupsContainingUser = siteService.getSite(siteId).getGroupsWithMember(currentUserId);
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
	      if (isMember)
	        break;
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
}
