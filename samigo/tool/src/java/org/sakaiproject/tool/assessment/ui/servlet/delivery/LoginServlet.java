/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.assessment.ui.servlet.delivery;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class LoginServlet
    extends HttpServlet
{
  private static Log log = LogFactory.getLog(LoginServlet.class);

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
    HttpSession httpSession = req.getSession(true);
    httpSession.setMaxInactiveInterval(3600); // one hour
    // we are going to use the delivery bean to flag that this access is via url
    // this is the flag that we will use in deliverAssessment.jsp to decide what
    // button to display - daisyf
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBeanFromExternalServlet(
       "delivery", req, res);
    delivery.setActionString("takeAssessmentViaUrl");

    // reset timer in case this is a timed assessment
    delivery.setTimeElapse("0");
    delivery.setLastTimer(0);
    delivery.setTimeLimit("0");
    delivery.setBeginAssessment(true);

    // set path
    delivery.setContextPath(req.getContextPath());

    String alias = req.getParameter("id");
    // 1. get publishedAssessment and check if anonymous is allowed
    // 2. If so, goto welcome.faces
    // 3. If not, goto login.faces
    // both pages will set agentId and then direct user to BeginAssessment
    PublishedAssessmentService service = new PublishedAssessmentService();
    PublishedAssessmentFacade pub = service.getPublishedAssessmentIdByAlias(alias);

    delivery.setAssessmentId(pub.getPublishedAssessmentId().toString());
    delivery.setAssessmentTitle(pub.getTitle());
    delivery.setPublishedAssessment(pub);

    RequestDispatcher dispatcher = null;
    String contextPath = req.getContextPath();
    String path = "/jsf/delivery/invalidAssessment.faces";
    boolean relativePath = true;

    String agentIdString = "";
    boolean isAuthorized = false;
    boolean isAuthenticated = false;

    if (pub != null){
      // Determine if assessment accept Anonymous Users. If so, starting in version 2.0.1
      // all users will be authenticated as anonymous for the assessment in this case.
      boolean anonymousAllowed = false;
      String releaseTo = pub.getAssessmentAccessControl().getReleaseTo();
      if (releaseTo != null && releaseTo.indexOf("Anonymous Users")> -1){
        anonymousAllowed = true;
        agentIdString = AgentFacade.createAnonymous();
        isAuthenticated = true;
        isAuthorized = true;
        delivery.setAnonymousLogin(true);
        PersonBean person = (PersonBean) ContextUtil.lookupBeanFromExternalServlet(
                              "person", req, res);
        person.setAnonymousId(agentIdString);
      }
      else { // check membership
	agentIdString = req.getRemoteUser();
        isAuthenticated = ( agentIdString!= null && !("").equals(agentIdString));
        if (isAuthenticated)
          isAuthorized = checkMembership(pub, req, res);
      }

      // check if assessment is available
      // We are getting the total no. of submission (for grade) per assessment
      // by the given agent at the same time
      boolean assessmentIsAvailable = assessmentIsAvailable(service, agentIdString, pub);
      if (isAuthorized){
        if (!assessmentIsAvailable) {
          path = "/jsf/delivery/assessmentNotAvailable.faces";
        }
        else {
          // if assessment is available, set it in delivery bean for display in deliverAssessment.jsp
          BeginDeliveryActionListener listener = new BeginDeliveryActionListener();
          listener.processAction(null);
          path = "/jsf/delivery/beginTakingAssessment_viaurl.faces";
        }
      }
      else{ // notAuthorized
        if (!isAuthenticated){
          if (AgentFacade.isStandaloneEnvironment())
            path = "/jsf/delivery/login.faces";
          else{
            relativePath = false;
            path = "/authn/login?url=" + URLEncoder.encode(req.getRequestURL().toString()+"?id="+alias, "UTF-8");
	  }
        }
        else { //isAuthenticated but not authorized
          path = "/jsf/delivery/accessDenied.faces";
        }
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

  private boolean isAvailable(PublishedAssessmentFacade f, HashMap h) {
    boolean returnValue = false;
    //1. prepare our significant parameters
    Date currentDate = new Date();
    Date startDate = f.getAssessmentAccessControl().getStartDate();
    Date retractDate = f.getAssessmentAccessControl().getRetractDate();
    Date dueDate = f.getAssessmentAccessControl().getDueDate();
    boolean acceptLateSubmission = AssessmentAccessControlIfc.
        ACCEPT_LATE_SUBMISSION.equals(
        f.getAssessmentAccessControl().getLateHandling());
    int maxSubmissionsAllowed = 9999;
    if ( (Boolean.FALSE).equals(f.getAssessmentAccessControl().getUnlimitedSubmissions()))
      maxSubmissionsAllowed = f.getAssessmentAccessControl().getSubmissionsAllowed().intValue();
    boolean notSubmitted = false;
    int totalSubmitted = 0;
    if (h.get(f.getPublishedAssessmentId()) == null)
      notSubmitted = true;
    else
      totalSubmitted = ( (Integer) h.get(f.getPublishedAssessmentId())).
          intValue();

      //2. time to go through all the criteria
    if (retractDate == null || retractDate.after(currentDate)) {
      if (dueDate == null || dueDate.after(currentDate) ||
          (dueDate.before(currentDate) && notSubmitted &&
           acceptLateSubmission)) {
        if (totalSubmitted < maxSubmissionsAllowed) {
          if (startDate == null ||
              (startDate != null && startDate.before(currentDate))){
            returnValue = true;
          }
        }
      }
    }
    return returnValue;
  }

  // check if assessment is available based on criteria like
  // dueDate
  public boolean assessmentIsAvailable(PublishedAssessmentService service,
      String agentIdString, PublishedAssessmentFacade pub){
    boolean assessmentIsAvailable = false;
    Integer submissions = new Integer(0);
    submissions = service.getTotalSubmission(agentIdString,
    pub.getPublishedAssessmentId().toString());
    HashMap h = new HashMap();
    if (submissions.intValue()>0)
      h.put(pub.getPublishedAssessmentId(), submissions);
    assessmentIsAvailable = isAvailable(pub, h);
    log.debug("**** assessmentIsAvailable="+assessmentIsAvailable);
    log.debug("pub assessmentId="+pub.getPublishedAssessmentId());
    log.debug("pub assessment relaeseTo="+pub.getAssessmentAccessControl().getReleaseTo());
    return assessmentIsAvailable;
  }
}
