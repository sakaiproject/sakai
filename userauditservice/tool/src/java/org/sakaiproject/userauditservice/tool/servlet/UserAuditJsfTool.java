/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation
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

package org.sakaiproject.userauditservice.tool.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.jsf.util.JsfTool;
import org.sakaiproject.util.Web;

@Slf4j
public class UserAuditJsfTool extends JsfTool {

	private static final long serialVersionUID = -976719461956856669L;

	private SessionManager sessionManager;

	private ActiveToolManager activeToolManager;

	private ToolManager toolManager;

	private static final String HELPER_EXT = ".helper";

	private static final String PANEL = "panel";

	private static final String HELPER_SESSION_PREFIX = "session.";

	// FIXME: http://bugs.sakaiproject.org/jira/browse/GM-88
	private static final String MYFACES_VIEW_COLLECTION = "org.apache.myfaces.application.jsp.JspStateManagerImpl.SERIALIZED_VIEW";

	private static final String STORED_MYFACES_VIEW_COLLECTION = "STORED_"
			+ MYFACES_VIEW_COLLECTION;

	// If this URL is requested, the helper is done and ready to return
	public static final String HELPER_RETURN_NOTIFICATION = "/returnToSender";

	// Override init to inject necessary components via ComponentManager cover
	@Override
	public void init(ServletConfig config) throws ServletException {

		super.init(config);

		sessionManager = (SessionManager) ComponentManager
				.get("org.sakaiproject.tool.api.SessionManager");
		activeToolManager = (ActiveToolManager) ComponentManager
				.get("org.sakaiproject.tool.api.ActiveToolManager");
		toolManager = (ToolManager) ComponentManager
				.get("org.sakaiproject.tool.api.ToolManager");
	}

	@Override
	protected void dispatch(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		log.debug("dispatch()");

		String target = req.getPathInfo();

		ToolSession session = sessionManager.getCurrentToolSession();

		if (log.isDebugEnabled()) {
			Map<String, String[]> reqParms = req.getParameterMap();
			for(Map.Entry<String, String[]> entry : reqParms.entrySet())
			{
				String reqParmKey = entry.getKey();
				StringBuffer sb = new StringBuffer();
				sb.append("REQ_PARM: ");
				sb.append(reqParmKey);
				sb.append(" = ");
				sb.append('[');
				String[] reqParm = reqParms.get(reqParmKey);
				for (int i = 0; i < reqParm.length;) {
					sb.append(reqParm[i]);
					if (++i < reqParm.length) {
						sb.append(", ");
					}
				}
				sb.append(']');
				log.debug(sb.toString());
			}
			Enumeration<String> sessionParmNames = session.getAttributeNames();
			while (sessionParmNames.hasMoreElements()) {
				String sessionParmName = sessionParmNames.nextElement();
				log.debug("SESS_PARM: " + sessionParmName + " = "
						+ session.getAttribute(sessionParmName));
			}
		}

		// see if this is the helper trying to return to caller
		if (HELPER_RETURN_NOTIFICATION.equals(target)) {
			target = (String) session.getAttribute(toolManager.getCurrentTool()
					.getId()
					+ Tool.HELPER_DONE_URL);
			if (target != null) {

				// FIXME: Workaround for
				// http://bugs.sakaiproject.org/jira/browse/GM-88
				Object viewCollection = session
						.getAttribute(STORED_MYFACES_VIEW_COLLECTION);
				if (viewCollection != null) {
					session.removeAttribute(STORED_MYFACES_VIEW_COLLECTION);
					session.setAttribute(MYFACES_VIEW_COLLECTION,
							viewCollection);
				}

				session.removeAttribute(toolManager.getCurrentTool().getId()
						+ Tool.HELPER_DONE_URL);
				res.sendRedirect(target);
				return;
			}
		}

		// Need this here until ToolServlet is updated to support this in
		// sendToHelper method
		// http://bugs.sakaiproject.org/jira/browse/SAK-9043
		// http://bugs.sakaiproject.org/jira/browse/GM-69
		Enumeration<String> params = req.getParameterNames();
		while (params.hasMoreElements()) {
			String paramName = params.nextElement();
			if (paramName.startsWith(HELPER_SESSION_PREFIX)) {
				String attributeName = paramName
						.substring(HELPER_SESSION_PREFIX.length());
				session
						.setAttribute(attributeName, req
								.getParameter(paramName));
			}
		}

		if (sendToHelper(req, res, target)) {
			return;
		}

		// see if we have a resource request - i.e. a path with an extension,
		// and one that is not the JSF_EXT
		if (isResourceRequest(target)) {
			// get a dispatcher to the path
			RequestDispatcher resourceDispatcher = getServletContext()
					.getRequestDispatcher(target);
			if (resourceDispatcher != null) {
				resourceDispatcher.forward(req, res);
				return;
			}
		}

		if ("Title".equals(req.getParameter(PANEL))) {
			// This allows only one Title JSF for each tool
			target = "/title.jsf";

		} else {

			if ((target == null) || "/".equals(target)) {
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
			if (m_defaultToLastView) {
				session.setAttribute(LAST_VIEW_VISITED, target);
			}
		}

		// add the configured folder root and extension (if missing)
		target = m_path + target;

		// add the default JSF extension (if we have no extension)
		int lastSlash = target.lastIndexOf('/');
		int lastDot = target.lastIndexOf('.');
		if ((lastDot < 0) || (lastDot < lastSlash)) {
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
		res
				.addHeader("Cache-Control",
						"no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		res.addHeader("Pragma", "no-cache");

		// dispatch to the target
		/*
		 * M_log.debug("dispatching path: " + req.getPathInfo() + " to: " +
		 * target + " context: " + getServletContext().getServletContextName());
		 */

		RequestDispatcher dispatcher = getServletContext()
				.getRequestDispatcher(target);
		dispatcher.forward(req, res);

		// restore the request object
		req.removeAttribute(Tool.NATIVE_URL);
		req.removeAttribute(URL_PATH);
		req.removeAttribute(URL_EXT);
	}

	protected boolean sendToHelper(HttpServletRequest req,
			HttpServletResponse res, String target) throws ToolException {
		if (target == null) {
			target = "/";
		}

		// 0 parts means the target was just "/", otherwise parts[0] = "",
		// parts[1] = item id, parts[2] if present is "edit"...
		String[] parts = target.split("/");

		if (parts.length < 2) {
			return false;
		}

		if (!parts[1].endsWith(HELPER_EXT)) {
			return false;
		}

		// calc helper id
		int posEnd = parts[1].lastIndexOf('.');

		String helperId = target.substring(1, posEnd + 1);
		ActiveTool helperTool = activeToolManager.getActiveTool(helperId);

		ToolSession toolSession = sessionManager.getCurrentToolSession();

		if (toolSession.getAttribute(helperTool.getId() + Tool.HELPER_DONE_URL) == null) {
			toolSession.setAttribute(helperTool.getId() + Tool.HELPER_DONE_URL,
					req.getContextPath() + req.getServletPath()
							+ computeDefaultTarget());
		}

		String context = req.getContextPath() + req.getServletPath()
				+ Web.makePath(parts, 1, 2);
		String toolPath = Web.makePath(parts, 2, parts.length);

		// FIXME: Workaround for http://bugs.sakaiproject.org/jira/browse/GM-88
		// Don't overwrite if already stored
		if (toolSession.getAttribute(STORED_MYFACES_VIEW_COLLECTION) == null) {
			Object viewCollection = toolSession
					.getAttribute(MYFACES_VIEW_COLLECTION);
			if (viewCollection != null) {
				toolSession.removeAttribute(MYFACES_VIEW_COLLECTION);
				toolSession.setAttribute(STORED_MYFACES_VIEW_COLLECTION,
						viewCollection);
			}
		}

		helperTool.help(req, res, context, toolPath);

		return true; // was handled as helper call
	}
}
