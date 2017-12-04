
/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.jsf.util;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.Web;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextAttachmentIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.bean.author.AnswerBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.SectionBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.QuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.util.EmailBean;

/**
 * <p>
 * Customized JsfTool for Samigo - just to workaround the fact that Samigo
 * has the JSF URL mapping "*.faces" hard-coded in several places.  If
 * all instances of "*.faces" were changed to "*.jsf", this class could be removed.
 * </p>
 * 
 */
  @Slf4j
  public class SamigoJsfTool extends JsfTool {
    private static final String HELPER_EXT = ".helper";
    private static final String HELPER_SESSION_PREFIX = "session.";
    private static final String HELPER_RETURN_NOTIFICATION = "/returnToCaller";
    private static final String RESET_ASSESSMENT_BEAN = "/resetAssessmentBean";

    /**
         * Recognize a path that is a resource request. It must have an "extension", i.e. a dot followed by characters that do not include a slash.
	 * 
	 * @param path
	 *        The path to check
	 * @return true if the path is a resource request, false if not.
	 */

	protected boolean isResourceRequest(String path)
	{
	    log.debug("****0. inside isResourceRequest, path="+path);
		// we need some path
		if ((path == null) || (path.length() == 0)) return false;

		// we need a last dot
		int pos = path.lastIndexOf(".");
		if (pos == -1) return false;

		// we need that last dot to be the end of the path, not burried in the path somewhere (i.e. no more slashes after the last dot)
		String ext = path.substring(pos);
	    log.debug("****1. inside isResourceRequest, ext="+ext);
		if (ext.indexOf("/") != -1) return false;

		// these are JSF pages, not resources		
		// THESE LINES OF CODE IS THE ONLY REASON THIS CLASS EXISTS!
		if (ext.equals(".jsf")) return false;
		if (ext.equals(".faces")) return false;
		if (path.startsWith("/faces/")) return false;
                if (path.indexOf(".helper") > -1) return false;
		
		// ok, it's a resource request
		return true;
	}

    protected void dispatch(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
      // NOTE: this is a simple path dispatching, taking the path as the view id = jsp file name for the view,
      //       with default used if no path and a path prefix as configured.

      // build up the target that will be dispatched to
      String target = req.getPathInfo();
      log.debug("***0. dispatch, target ="+target);

      // see if we need to reset the assessmentBean, such as when returning
		// from a helper
		// TODO: there MUST be a cleaner way to do this!! These dependencies
		// shouldn't be here!!
		if (target != null && target.startsWith(RESET_ASSESSMENT_BEAN)) {
			AssessmentBean assessmentBean = (AssessmentBean) ContextUtil
					.lookupBeanFromExternalServlet("assessmentBean", req, res);
			if (assessmentBean != null && assessmentBean.getAssessmentId() != null) {
				AssessmentIfc assessment;
				AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
				AssessmentService assessmentService;
				if (author.getIsEditPendingAssessmentFlow()) {
					assessmentService = new AssessmentService();
	        	}
	        	else {
	        		assessmentService = new PublishedAssessmentService();
	        	}
        		assessment = assessmentService.getAssessment(Long.valueOf(assessmentBean.getAssessmentId()));
				assessmentBean.setAssessment(assessment);
			}
			target = target.replaceFirst(RESET_ASSESSMENT_BEAN, "");
		}

		// see if this is a helper trying to return to caller
		if (HELPER_RETURN_NOTIFICATION.equals(target)) {
			ToolSession session = SessionManager.getCurrentToolSession();
			target = (String) session.getAttribute(ToolManager.getCurrentTool()
					.getId()
					+ Tool.HELPER_DONE_URL);
			if (target != null) {
				session.removeAttribute(ToolManager.getCurrentTool().getId()
						+ Tool.HELPER_DONE_URL);
				res.sendRedirect(target);
				return;
			}
		}
      

      boolean sendToHelper = sendToHelper(req, res);
      boolean isResourceRequest = isResourceRequest(target);
      log.debug("***1. dispatch, send to helper ="+sendToHelper);
      log.debug("***2. dispatch, isResourceRequest ="+ isResourceRequest);

      // see if we have a helper request
      if (sendToHelper) {
        return;
      }

      if (isResourceRequest) {
        // get a dispatcher to the path
        RequestDispatcher resourceDispatcher = getServletContext().getRequestDispatcher(target);
        if (resourceDispatcher != null)  {
          resourceDispatcher.forward(req, res);
          return;
        }
      }

      if (target == null || "/".equals(target)) {
        target = computeDefaultTarget();

        // make sure it's a valid path
        if (!target.startsWith("/")){
          target = "/" + target;
        }

        // now that we've messed with the URL, send a redirect to make it official
        res.sendRedirect(Web.returnUrl(req, target));
        return;
      }

      // see if we want to change the specifically requested view
      String newTarget = redirectRequestedTarget(target);

      // make sure it's a valid path
      if (!newTarget.startsWith("/")){
        newTarget = "/" + newTarget;
      }

      if (!newTarget.equals(target)){
        // now that we've messed with the URL, send a redirect to make it official
        res.sendRedirect(Web.returnUrl(req, newTarget));
        return;
      }
      target = newTarget;

      // store this
      ToolSession toolSession = SessionManager.getCurrentToolSession();
      if (toolSession!=null){
        toolSession.setAttribute(LAST_VIEW_VISITED, target);
      }
        log.debug("3a. dispatch: toolSession="+toolSession);
        log.debug("3b. dispatch: target="+target);
        log.debug("3c. dispatch: lastview?"+m_defaultToLastView);


      // add the configured folder root and extension (if missing)
      target = m_path + target;

      // add the default JSF extension (if we have no extension)
      int lastSlash = target.lastIndexOf("/");
      int lastDot = target.lastIndexOf(".");
      if (lastDot < 0 || lastDot < lastSlash){
        target += JSF_EXT;
      }
     
      // set the information that can be removed from return URLs
      req.setAttribute(URL_PATH, m_path);
      req.setAttribute(URL_EXT, ".jsp");

      // set the sakai request object wrappers to provide the native, not Sakai set up, URL information
      // - this assures that the FacesServlet can dispatch to the proper view based on the path info
      req.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

      // TODO: Should setting the HTTP headers be moved up to the portal level as well?
      res.setContentType("text/html; charset=UTF-8");
      res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
      res.addDateHeader("Last-Modified", System.currentTimeMillis());
      res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
      res.addHeader("Pragma", "no-cache");

      // dispatch to the target
      log.debug("***4. dispatch, dispatching path: " + req.getPathInfo() + " to: " + target + " context: "
	+ getServletContext().getServletContextName());
      // if this is a return from the file picker and going back to 
      // case 1: item mofification, then set 
      //         itemAuthorbean.attachmentlist = filepicker list
      if (target.indexOf("/jsf/author/item/") > -1 
	  && ("true").equals(toolSession.getAttribute("SENT_TO_FILEPICKER_HELPER"))){
	 ItemAuthorBean bean = (ItemAuthorBean) ContextUtil.lookupBeanFromExternalServlet(
                               "itemauthor", req, res);

	 	 // For EMI Item Attachments	
	     AnswerBean emiQAComboItem = bean.getCurrentAnswer();
	     if (emiQAComboItem == null) {
	    	 bean.setItemAttachment();
	     }
	     else {
	    	 emiQAComboItem.setItemTextAttachment();
	     }
         toolSession.removeAttribute("SENT_TO_FILEPICKER_HELPER");
      }

      // case 2: part mofification, then set 
      //         sectionBean.attachmentList = filepicker list
      else if (target.indexOf("/jsf/author/editPart") > -1 
	  && ("true").equals(toolSession.getAttribute("SENT_TO_FILEPICKER_HELPER"))){
	 SectionBean bean = (SectionBean) ContextUtil.lookupBeanFromExternalServlet(
                               "sectionBean", req, res);
         bean.setPartAttachment();
         toolSession.removeAttribute("SENT_TO_FILEPICKER_HELPER");
      }

      // case 3.1: assessment settings mofification, then set assessmentSettingsBean.attachmentList = filepicker list
      else if (target.indexOf("/jsf/author/authorSettings") > -1 
	  && ("true").equals(toolSession.getAttribute("SENT_TO_FILEPICKER_HELPER"))){
	 AssessmentSettingsBean bean = (AssessmentSettingsBean) ContextUtil.lookupBeanFromExternalServlet(
                               "assessmentSettings", req, res);
         bean.setAssessmentAttachment();
         toolSession.removeAttribute("SENT_TO_FILEPICKER_HELPER");
      }
      // case 3.2: published assessment settings mofification, then set assessmentSettingsBean.attachmentList = filepicker list
      else if (target.indexOf("/jsf/author/publishedSettings") > -1 
    		  && ("true").equals(toolSession.getAttribute("SENT_TO_FILEPICKER_HELPER"))){
    	  PublishedAssessmentSettingsBean bean = (PublishedAssessmentSettingsBean) ContextUtil.lookupBeanFromExternalServlet(
    	                               "publishedSettings", req, res);
    	         bean.setAssessmentAttachment();
    	         toolSession.removeAttribute("SENT_TO_FILEPICKER_HELPER");
      }

      // case 4: create new mail, then set
		// emailBean.attachmentList = filepicker list
      else if (target.indexOf("/jsf/evaluation/createNewEmail") > -1
				&& ("true").equals(toolSession.getAttribute("SENT_TO_FILEPICKER_HELPER"))) {
			EmailBean bean = (EmailBean) ContextUtil.lookupBeanFromExternalServlet("email", req, res);
			bean.prepareAttachment();
			toolSession.removeAttribute("SENT_TO_FILEPICKER_HELPER");
		}
      
      else if (target.indexOf("/jsf/evaluation/questionScore") > -1
				&& ("true").equals(toolSession.getAttribute("SENT_TO_FILEPICKER_HELPER"))) {
			QuestionScoresBean bean = (QuestionScoresBean) ContextUtil.lookupBeanFromExternalServlet("questionScores", req, res);
			bean.setAttachment((Long) toolSession.getAttribute("itemGradingId"));
			toolSession.removeAttribute("SENT_TO_FILEPICKER_HELPER");
		}
      
      else if (target.indexOf("/jsf/evaluation/gradeStudentResult") > -1
				&& ("true").equals(toolSession.getAttribute("SENT_TO_FILEPICKER_HELPER"))) {
    	  ItemContentsBean bean = (ItemContentsBean) ContextUtil.lookupBeanFromExternalServlet("itemContents", req, res);
			bean.setAttachment((Long) toolSession.getAttribute("itemGradingId"));
			toolSession.removeAttribute("SENT_TO_FILEPICKER_HELPER");
		}
      
      else if (target.indexOf("/jsf/evaluation/totalScores") > -1
				&& ("true").equals(toolSession.getAttribute("SENT_TO_FILEPICKER_HELPER"))) {
    	  TotalScoresBean bean = (TotalScoresBean) ContextUtil.lookupBeanFromExternalServlet("totalScores", req, res);
			bean.setAttachment((Long) toolSession.getAttribute("assessmentGradingId"));
			toolSession.removeAttribute("SENT_TO_FILEPICKER_HELPER");
		}
    
      RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(target);
      dispatcher.forward(req, res);

      // restore the request object
      req.removeAttribute(Tool.NATIVE_URL);
      req.removeAttribute(URL_PATH);
      req.removeAttribute(URL_EXT);
      
    }


    protected boolean sendToHelper(HttpServletRequest req, HttpServletResponse res)
                      throws ToolException {
      String path = req.getPathInfo();
      if (path == null) path = "/";

      // 0 parts means the path was just "/", otherwise parts[0] = "", parts[1] = item id, parts[2] 
      // if present is "edit"...
      String[] parts = path.split("/");

      log.debug("***a. sendToHelper.partLength="+parts.length);
      String helperPath =null;
      String toolPath=null;

      // e.g. helper url in Samigo can be /jsf/author/item/sakai.filepicker.helper/tool
      //      or /sakai.filepicker.helper 
      if (parts.length > 2){
        log.debug("***b. sendToHelper.partLength="+parts.length);
        helperPath = parts[parts.length - 2];
        toolPath = parts[parts.length - 1];
      }
      else if (parts.length == 2){
        log.debug("***c. sendToHelper.partLength="+parts.length);
        helperPath = parts[1];
      }
      else return false;

      if (!helperPath.endsWith(HELPER_EXT)) return false;
      log.debug("****d. sendToHelper, part #1="+helperPath);
      log.debug("****e. sendToHelper, part #2="+toolPath);

      ToolSession toolSession = SessionManager.getCurrentToolSession();
      toolSession.setAttribute("SENT_TO_FILEPICKER_HELPER", "true");

      Enumeration params = req.getParameterNames();
      while (params.hasMoreElements()) {
        String paramName = (String)params.nextElement();
        if (paramName.startsWith(HELPER_SESSION_PREFIX)) {
	  String attributeName = paramName.substring(HELPER_SESSION_PREFIX.length());
	  toolSession.setAttribute(attributeName, req.getParameter(paramName));
        }
      }

      // calc helper id
      int posEnd = helperPath.lastIndexOf(".");
      String helperId = helperPath.substring(0, posEnd);
      log.debug("****f. sendToHelper, helperId="+helperId);
      ActiveTool helperTool = ActiveToolManager.getActiveTool(helperId);

      String url = req.getContextPath() + req.getServletPath();
      if (toolSession.getAttribute(helperTool.getId() + Tool.HELPER_DONE_URL) == null) {
			toolSession.setAttribute(helperTool.getId() + Tool.HELPER_DONE_URL,
					url + RESET_ASSESSMENT_BEAN + computeDefaultTarget(true));
		}

      log.debug("****g. sendToHelper, url="+url);
      String context = url + "/"+ helperPath;
      log.debug("****h. sendToHelper, context="+context);
      if (toolPath != null) 
        helperTool.help(req, res, context, "/"+toolPath);
      else
        helperTool.help(req, res, context, "");

      return true; // was handled as helper call
    }

    protected String computeDefaultTarget(boolean lastVisited){
      // setup for the default view as configured
      ToolSession session = SessionManager.getCurrentToolSession();
      String target = "/" + m_default;

      // if we are doing lastVisit and there's a last-visited view, for this tool placement / user, use that
      if (lastVisited)	{
        String last = (String) session.getAttribute(LAST_VIEW_VISITED);
        if (last != null) {
          target = last;
	}
      }
      session.removeAttribute(LAST_VIEW_VISITED);
      log.debug("***3. computeDefaultTarget()="+target);
      return target;
    }
  }
