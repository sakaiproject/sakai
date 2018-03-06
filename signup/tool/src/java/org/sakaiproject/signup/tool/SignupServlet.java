/**
 * Copyright (c) 2007-2016 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.tool;

import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.jsf.util.HelperAwareJsfTool;
import org.sakaiproject.jsf.util.JsfTool;
import org.sakaiproject.signup.tool.jsf.attachment.AttachmentHandler;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.Web;

/**
 * <p>
 * This Servlet class will enable the path to javaScript and image files in
 * Signup tool and it will also enable the ajax call later in the future. For
 * Sakai 2.5, it will not pick up the CSS file via the 'sakai.html.head'. and we
 * take this out.
 * </P>
 */
@SuppressWarnings("serial")
@Slf4j
public class SignupServlet extends JsfTool {

	private String headerPreContent;

	private ServletContext servletContext = null;

	private static final String HELPER_EXT = ".helper";

	private static final String HELPER_SESSION_PREFIX = "session.";

	/** the alternate next view */
	public static final String ALTERNATE_DONE_URL = "altDoneURL";

	/** the set of alternate views */
	public static final String ALTERNATE_DONE_URL_MAP = "altDoneURLSet";

	/**
	 * Initialize the Servlet class.
	 */
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		// headerPreContent =
		// servletConfig.getInitParameter("headerPreContent");
		this.servletContext = servletConfig.getServletContext();
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// addPrecontent(request);

		// if(ajax request)
		// else call the super doPost
		super.doPost(request, response);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// addPrecontent(request);
		// if(ajax request)
		// else call the super doGet
		super.doGet(request, response);
	}

	protected void dispatch(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		ToolSession session = null;
		String target = req.getPathInfo();

		// see if we have a helper request
		if (sendToHelper(req, res, target)) {
			return;
		}

		// see if we have a resource request - i.e. a path with an extension,
		// and one that is not the JSF_EXT
		if (isResourceRequest(target)) {
			// get a dispatcher to the path
			RequestDispatcher resourceDispatcher = getServletContext().getRequestDispatcher(target);
			if (resourceDispatcher != null) {
				resourceDispatcher.forward(req, res);
				return;
			}
		}

		if ("Title".equals(req.getParameter("panel"))) {
			// This allows only one Title JSF for each tool
			target = "/title.jsf";
		}

		else {
			session = SessionManager.getCurrentToolSession();

			if (target == null || "/".equals(target) || target.length() == 0) {
				if (!m_defaultToLastView) {
					// make sure tool session is clean
					session.clearAttributes();
				}

				target = computeDefaultTarget();

				// make sure it's a valid path
				if (!target.startsWith("/")) {
					target = "/" + target;
				}

				// now that we've messed with the URL, send a redirect to make
				// it official
				res.sendRedirect(Web.returnUrl(req, target));
				return;
			}

			// see if we want to change the specifically requested view
			String newTarget = redirectRequestedTarget(target);

			// make sure it's a valid path
			if (!newTarget.startsWith("/")) {
				newTarget = "/" + newTarget;
			}

			if (!newTarget.equals(target)) {
				// now that we've messed with the URL, send a redirect to make
				// it official
				res.sendRedirect(Web.returnUrl(req, newTarget));
				return;
			}
			target = newTarget;

			// store this
			session.setAttribute(LAST_VIEW_VISITED, target);
		}

		// add the configured folder root and extension (if missing)
		target = m_path + target;

		// add the default JSF extension (if we have no extension)
		int lastSlash = target.lastIndexOf("/");
		int lastDot = target.lastIndexOf(".");
		if (lastDot < 0 || lastDot < lastSlash) {
			target += JSF_EXT;
		}

		// set the information that can be removed from return URLs
		req.setAttribute(URL_PATH, m_path);
		req.setAttribute(URL_EXT, ".jsp");

		// set the sakai request object wrappers to provide the native, not
		// Sakai set up, URL information
		// - this assures that the FacesServlet can dispatch to the proper view
		// based on the path info
		req.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

		// TODO: Should setting the HTTP headers be moved up to the portal level
		// as well?
		res.setContentType("text/html; charset=UTF-8");
		res.addDateHeader("Expires", System.currentTimeMillis()
				- (1000L * 60L * 60L * 24L * 365L));
		res.addDateHeader("Last-Modified", System.currentTimeMillis());
		res.addHeader("Cache-Control",
						"no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		res.addHeader("Pragma", "no-cache");

		if (session != null && ("true").equals(session.getAttribute("SENT_TO_FILEPICKER_HELPER"))) {
			AttachmentHandler bean = (AttachmentHandler) lookupBeanFromExternalServlet("AttachmentHandler", req, res);
			bean.setAttachmentItems();
			session.removeAttribute("SENT_TO_FILEPICKER_HELPER");
		}

		// dispatch to the target
		log.debug("dispatching path: " + req.getPathInfo() + " to: " + target
				+ " context: " + getServletContext().getServletContextName());
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(target);
		dispatcher.forward(req, res);

		// restore the request object
		req.removeAttribute(Tool.NATIVE_URL);
		req.removeAttribute(URL_PATH);
		req.removeAttribute(URL_EXT);

		// see if we have a helper request
		if (!sendToHelper(req, res, "")) {
			ToolSession toolSession = SessionManager.getCurrentToolSession();
			if (("true").equals(toolSession.getAttribute("SENT_TO_FILEPICKER_HELPER"))) {
				AttachmentHandler bean = (AttachmentHandler) lookupBeanFromExternalServlet("AttachmentHandler", req, res);
				bean.setAttachmentItems();
				toolSession.removeAttribute("SENT_TO_FILEPICKER_HELPER");
			}
		}
	}

	protected boolean sendToHelper(HttpServletRequest req, HttpServletResponse res, String target) {
		String path = req.getPathInfo();
		if (path == null)
			path = "/";

		// 0 parts means the path was just "/", otherwise parts[0] = "",
		// parts[1] = item id, parts[2] if present is "edit"...
		String[] parts = path.split("/");

		if (parts.length < 2) {
			return false;
		}

		String helperPath = null;
		String toolPath = "";
		/*
		 * e.g. helper url in Signup can be
		 * /jsf/signup/newMeeting/sakai.filepicker.helper/tool or
		 * /sakai.filepicker.helper
		 */
		if (parts.length > 2) {
			helperPath = parts[parts.length - 2];
			toolPath = parts[parts.length - 1];
		} else if (parts.length == 2) {
			helperPath = parts[1];
		} else
			return false;

		if (!helperPath.endsWith(HELPER_EXT))
			return false;

		ToolSession toolSession = SessionManager.getCurrentToolSession();

		Enumeration params = req.getParameterNames();
		while (params.hasMoreElements()) {
			String paramName = (String) params.nextElement();
			if (paramName.startsWith(HELPER_SESSION_PREFIX)) {
				String attributeName = paramName.substring(HELPER_SESSION_PREFIX.length());
				toolSession.setAttribute(attributeName, req.getParameter(paramName));
			}
		}

		// calc helper id
		int posEnd = helperPath.lastIndexOf(".");

		String helperId = helperPath.substring(0, posEnd);
		ActiveTool helperTool = ActiveToolManager.getActiveTool(helperId);

		// get the current location (if one doesn't exist) and save it for when
		// we return from the helper
		if (toolSession.getAttribute(helperTool.getId() + Tool.HELPER_DONE_URL) == null) {
			toolSession.setAttribute(helperTool.getId() + Tool.HELPER_DONE_URL,
					req.getContextPath() + req.getServletPath() + computeDefaultTarget(true));
		}
		toolSession.setAttribute(helperTool.getId() + "thetoolPath", req.getContextPath()
				+ req.getServletPath());

		if (helperPath.endsWith("sakai.filepicker.helper"))
			toolSession.setAttribute("SENT_TO_FILEPICKER_HELPER", "true");// flag
																			// for
																			// filePicker

		// saves the alternate done url map into a tool specific attribute
		if (toolSession.getAttribute(helperTool.getId() + ALTERNATE_DONE_URL) == null) {
			toolSession.setAttribute(helperTool.getId() + ALTERNATE_DONE_URL,
					toolSession.getAttribute(ALTERNATE_DONE_URL));
			toolSession.setAttribute(helperTool.getId()
					+ ALTERNATE_DONE_URL_MAP, toolSession.getAttribute(ALTERNATE_DONE_URL_MAP));
			toolSession.removeAttribute(ALTERNATE_DONE_URL);
			toolSession.removeAttribute(ALTERNATE_DONE_URL_MAP);
		}

		String context = req.getContextPath() + req.getServletPath() + "/"+ helperPath;
		try {
			helperTool.help(req, res, context, "/" + toolPath);
		} catch (ToolException e) {
			throw new RuntimeException(e);
		}

		return true; // was handled as helper call
	}

	/**
	 * Helper method to look up backing bean, when OUTSIDE faces in a servlet.
	 * Don't forget to cast! e.g. (TemplateBean)
	 * ContextUtil.lookupBean("template")
	 * 
	 * @param beanName
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @return the backing bean
	 */
	public Serializable lookupBeanFromExternalServlet(String beanName,
			HttpServletRequest request, HttpServletResponse response) {
		// prepare lifecycle
		LifecycleFactory lFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
		Lifecycle lifecycle = lFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

		FacesContextFactory fcFactory = (FacesContextFactory) FactoryFinder
				.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);

		// in the integrated environment, we can't get the ServletContext from
		// the
		// HttpSession of the request - because the HttpSession is
		// webcontainer-wide,
		// its not tied to a particular servlet.

		if (this.servletContext == null) {
			servletContext = request.getSession().getServletContext();
		}

		FacesContext facesContext = fcFactory.getFacesContext(servletContext,
				request, response, lifecycle);

		ApplicationFactory factory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
		Application application = factory.getApplication();
		Serializable bean = (Serializable) application.getVariableResolver().resolveVariable(facesContext, beanName);
		return bean;
	}

	protected String computeDefaultTarget(boolean lastVisited) {
		// setup for the default view as configured
		String target = "/" + m_default;

		// if we are doing lastVisit and there's a last-visited view, for this
		// tool placement / user, use that
		if (lastVisited) {
			ToolSession session = SessionManager.getCurrentToolSession();
			String last = (String) session.getAttribute(LAST_VIEW_VISITED);
			if (last != null) {
				target = last;
			}
		}

		return target;
	}

	/*
	 * private void addPrecontent(HttpServletRequest request) { String
	 * sakaiHeader = (String) request.getAttribute("sakai.html.head");
	 * request.setAttribute("sakai.html.head", sakaiHeader + headerPreContent); }
	 */
}
