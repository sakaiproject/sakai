/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.rsf.producers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.rsf.params.BeginAssessmentViewParameters;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.jsfnav.DynamicNavigationCaseReporter;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Producer to show basic Assessment info and allow the user (student) to begin the assessment if
 * they want. Also ensures that the user is logged in and has the proper authorization as well as
 * that the assessment is currently available.
 * 
 * This code contains some parts I'd rather not have to do in order to pass control off from RSF to
 * JSF... please don't judge, but feel free to propose better solutions or rewrite delivery in RSF
 * completely ;-)
 * 
 * @author Joshua Ryan  josh@asu.edu   alt^I
 * 
 */
public class BeginAssessmentProducer implements ViewComponentProducer,
    DynamicNavigationCaseReporter, DefaultView, ViewParamsReporter {

  public HttpServletRequest httpServletRequest;
  public HttpServletResponse httpServletResponse;
  public MessageLocator messageLocator;
  
  private static Log log = LogFactory.getLog(BeginAssessmentProducer.class);
  
	public static final String VIEW_ID = "BeginTakingAssessment";
	public String getViewID() {
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
    BeginAssessmentViewParameters params = null;
    String alias = "";
    if (viewparams != null){
      params = (BeginAssessmentViewParameters) viewparams;
      alias = params.pubReference;
    }
    else{
      log.warn("Something bad... we have no viewparams");
    }


    //Begin cut and past (with small deviations) from existing LoginServlet that currently does
    //the job of url aliased assessment delivery in Samigo, some of this could possibly be removed
    //unless this just replaces the LoginServlet someday.
    
    HttpSession httpSession = httpServletRequest.getSession(true);
    httpSession.setMaxInactiveInterval(3600); // one hour
    PersonBean person = (PersonBean) ContextUtil.lookupBeanFromExternalServlet("person", httpServletRequest, httpServletResponse);
    // we are going to use the delivery bean to flag that this access is via url
    // this is the flag that we will use in deliverAssessment.jsp to decide what
    // button to display - daisyf
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBeanFromExternalServlet("delivery", httpServletRequest, httpServletResponse);
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

    // set path
    delivery.setContextPath(httpServletRequest.getContextPath());

    // 1. get publishedAssessment and check if anonymous is allowed
    // 2. If so, goto welcome.faces
    // 3. If not, goto login.faces
    // both pages will set agentId and then direct user to BeginAssessment
    PublishedAssessmentService service = new PublishedAssessmentService();
    PublishedAssessmentFacade pub = service.getPublishedAssessment(alias);

    delivery.setAssessmentId(pub.getPublishedAssessmentId().toString());
    delivery.setAssessmentTitle(pub.getTitle());
    delivery.setPublishedAssessment(pub);

    String path = null;

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
      agentIdString = httpServletRequest.getRemoteUser();
      isAuthenticated = ( agentIdString!= null && !("").equals(agentIdString));
      if (isAuthenticated){
        isAuthorized = checkMembership(pub);
        // in 2.2, agentId is differnt from httpServletRequest.getRemoteUser()
        agentIdString = AgentFacade.getAgentString();
      }
    }

    log.debug("*** agentIdString: "+agentIdString);

    // check if assessment is available
    // We are getting the total no. of submission (for grade) per assessment
    // by the given agent at the same time
    boolean assessmentIsAvailable = assessmentIsAvailable(service, agentIdString, pub,
                                                          delivery);
    if (isAuthorized){
      if (!assessmentIsAvailable) {
        UIBranchContainer.make(tofill, "assessmentNotAvailable:");
        // TODO added info on why
      }
      else {
        // if assessment is available, set it in delivery bean for display in deliverAssessment.jsp
        BeginDeliveryActionListener listener = new BeginDeliveryActionListener();
        listener.processAction(null);

        UIForm form = UIForm.make(tofill, "takeAssessmentForm:");

        String url = "/samigo/servlet/Login?id="
          + pub.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.ALIAS)
          + "&fromDirect=true";

        UICommand.make(form, "takeAssessment", messageLocator.getMessage("begin_assessment_"), null)
          .decorate(new UITooltipDecorator(messageLocator.getMessage("begin_assessment_")))
          .decorate(new UIFreeAttributeDecorator("onclick", "window.location.href='" + url + "'"));
        //UILink.make(form, "takeAssessment", url);
        
        UIOutput.make(form, "assessmentTitle", delivery.getAssessmentTitle());
        UIOutput.make(form, "courseName", delivery.getCourseName());
        UIOutput.make(form, "creatorName", delivery.getCreatorName());
        
        if (delivery.getHasTimeLimit()) {
          UIBranchContainer timeLimit = UIBranchContainer.make(form, "timeLimit:");
          UIOutput.make(timeLimit, "timeLimit", delivery.getTimeLimit());
          UIOutput.make(timeLimit, "timeLimitHour", delivery.getTimeLimit_hour() + "");
          UIOutput.make(timeLimit, "timeLimitMin", delivery.getTimeLimit_minute() + "");
        }
        else {
          UIBranchContainer noTimeLimit = UIBranchContainer.make(form, "noTimeLimit:");
          UIOutput.make(noTimeLimit, "timeLimit", delivery.getTimeLimit());          
        }
        
        if (!delivery.getAnonymousLogin() && !delivery.getSettings().isUnlimitedAttempts()) {
          UIBranchContainer limited = UIBranchContainer.make(tofill, "limited:");
          UIOutput.make(limited, "maxAttempts", delivery.getSettings().getMaxAttempts() + "");
          UIOutput.make(limited, "remaining", delivery.getSubmissionsRemaining() + "");
        }
        else if (!delivery.getAnonymousLogin() && delivery.getSettings().isUnlimitedAttempts()) {
          UIBranchContainer.make(tofill, "unlimited:");
        }
        
        if (delivery.getFeedbackComponent().getShowImmediate()) {
          UIBranchContainer.make(tofill, "immediate:");
        }
        else if (delivery.getFeedbackComponent().getShowNoFeedback()) {
          UIBranchContainer.make(tofill, "nofeedback:");
        }
        else if (delivery.getFeedbackComponent().getShowDateFeedback()) {
          UIBranchContainer feedback = UIBranchContainer.make(tofill, "feedback:");
          UIOutput.make(feedback, "feedBackDate", delivery.getSettings().getFeedbackDate().toString());
        }

        if (delivery.getDueDate() != null) {
          UIBranchContainer duedate = UIBranchContainer.make(tofill, "duedate:");
          UIOutput.make(duedate, "duedate", delivery.getDueDateString());
        }
      }
    }
    else{ // notAuthorized
      if (!isAuthenticated){
        if (AgentFacade.isStandaloneEnvironment()) {
          delivery.setActionString(null);
          path = delivery.getPortal();
        }
        else{
          delivery.setActionString(null);
          try {
            path = "/authn/login?url=" + URLEncoder.encode(httpServletRequest.getRequestURL().toString()+"?pubId="+alias, "UTF-8");          
          }
          catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
        log.info("** servlet path="+httpServletRequest.getRequestURL().toString());
        String url = httpServletRequest.getRequestURL().toString();
        String context = httpServletRequest.getContextPath();
        String finalUrl = url.substring(0,url.lastIndexOf(context))+path;
        log.info("**** finalUrl = "+finalUrl);
        try {
          httpServletResponse.sendRedirect(finalUrl);
        }
        catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      else { //isAuthenticated but not authorized
        UIBranchContainer.make(tofill, "accessDenied:");
        // TODO added info on why
      }
    }
 
    //End cut and paste from LoginServlet (with small deviations)
	}

    private boolean checkMembership(PublishedAssessmentFacade pub){
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

    // check if assessment is available based on criteria like
    // dueDate
    public boolean assessmentIsAvailable(PublishedAssessmentService service,
                                         String agentIdString, PublishedAssessmentFacade pub,
                                         DeliveryBean delivery){
      boolean assessmentIsAvailable = false;
      String nextAction = delivery.checkBeforeProceed();
      log.debug("nextAction="+nextAction);
      if (("safeToProceed").equals(nextAction)){
        assessmentIsAvailable = true;
      }
      return assessmentIsAvailable;
    }

    public ViewParameters getViewParameters() {
      return new BeginAssessmentViewParameters();
    }

    public List reportNavigationCases() {
      List togo = new ArrayList();
      togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));

      return togo;
    }

    public void setMessageLocator(MessageLocator messageLocator) {
      this.messageLocator = messageLocator;
    }
}
