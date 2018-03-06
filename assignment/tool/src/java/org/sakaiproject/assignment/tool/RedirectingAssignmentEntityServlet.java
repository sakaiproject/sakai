/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.assignment.tool;

import java.io.IOException;
import java.util.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.assignment.entityproviders.AssignmentEntityProvider;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Does a redirect to allow basic DirectServlet access to old assignment
 * Entities
 * 
 * @author Joshua Ryan josh@asu.edu alt^I
 * 
 */
@Slf4j
public class RedirectingAssignmentEntityServlet extends HttpServlet implements
		EntityViewAccessProvider {

	private static final long serialVersionUID = 0L;
	private EntityBroker entityBroker;
	private SessionManager sessionManager;
	private EntityViewAccessProviderManager accessProviderManager;

	/**
	 * Initialize the servlet.
	 * 
	 * @param config
	 *            The servlet config.
	 * @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException {
		log.info("init()");
		super.init(config);
		entityBroker = (EntityBroker) ComponentManager
				.get("org.sakaiproject.entitybroker.EntityBroker");
		sessionManager = (SessionManager) ComponentManager
				.get("org.sakaiproject.tool.api.SessionManager");
		accessProviderManager = (EntityViewAccessProviderManager) ComponentManager
				.get("org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager");
		if (accessProviderManager != null) {
			accessProviderManager.registerProvider(
					AssignmentEntityProvider.ENTITY_PREFIX, this);
		}
	}

	public void handleAccess(EntityView view, HttpServletRequest req,
			HttpServletResponse res) {
		log.debug("handleAccess()");
		Map<String, String> props = entityBroker.getProperties(req
				.getPathInfo());
		String target = props.get("url");
		log.debug("handleAccess() -> {}", target);
		String user = props.get("security.user");
		String site_function = props.get("security.site.function");
		String site_secref = props.get("security.site.ref");
		String assignment_function = props.get("security.assignment.function");
		String assignment_grade_function = props
				.get("security.assignment.grade.function");
		String assignment_secref = props.get("security.assignment.ref");
		String assignment_grade_secref = props
				.get("security.assignment.grade.ref");
		String submissionAttachmentRefStrs = props
				.get("submissionAttachmentRefs");

		Session session = sessionManager.getCurrentSession();
		clearSessionAttributes(session);

		if (submissionAttachmentRefStrs != null) {
			String[] submissionAttachmentRefs = submissionAttachmentRefStrs
					.split(":::");
			session.setAttribute(
					"assignment.content.security.advisor",
					new MySecurityAdvisor(user, "content.read", Arrays
							.asList(submissionAttachmentRefs)));
			session.setAttribute("assignment.content.decoration.wrapper.refs",
					submissionAttachmentRefs);
		}
		String decoratedContentWrapper = props
				.get("assignment.content.decoration.wrapper");

		Object sessionAdvisors = session
				.getAttribute("sitevisit.security.advisor");
		Set siteAdvisors = new HashSet();
		if (sessionAdvisors != null) {
			siteAdvisors = (Set) sessionAdvisors;
		}

		siteAdvisors
				.add(new MySecurityAdvisor(user, site_function, site_secref));

		// dump a couple of advisors into session so we can get at them outside
		// of this threadlocal
		session.setAttribute("sitevisit.security.advisor", siteAdvisors);

		List<String> assignment_functions = new ArrayList<String>();
		assignment_functions.add(assignment_function);
		assignment_functions.add(assignment_grade_function);

		List<String> assignment_secrefs = new ArrayList<String>();
		assignment_secrefs.add(assignment_secref);
		assignment_secrefs.add(assignment_grade_secref);

		SecurityAdvisor secAdv = new MySecurityAdvisor(user,
				assignment_functions, assignment_secrefs);

		session.setAttribute("assignment.security.advisor", secAdv);
		session.setAttribute("assignment.grade.security.advisor", secAdv);

		session.setAttribute("assignment.content.decoration.wrapper",
				decoratedContentWrapper);

		try {
			setNoCacheHeaders(res);
			res.sendRedirect(target);
		} catch (IOException e) {
			log.error("Cannot send redirect target: {}", target, e);
		}
		return;
	}

	// set standard no-cache headers
	protected void setNoCacheHeaders(HttpServletResponse resp) {
		resp.setContentType("text/html; charset=UTF-8");
		// some old date
		resp.addHeader("Expires", "Mon, 01 Jan 2001 00:00:00 GMT");
		// TODO: do we need this? adding a date header is expensive contention
		// for the date formatter, ours or Tomcats.
		// resp.addDateHeader("Last-Modified", System.currentTimeMillis());
		resp.addHeader("Cache-Control",
				"no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		resp.addHeader("Pragma", "no-cache");
	}

	private void clearSessionAttributes(Session session) {
		session.removeAttribute("assignment.content.security.advisor");
		session.removeAttribute("assignment.content.decoration.wrapper.refs");

	}

	/**
	 * A simple SecurityAdviser that can be used to override permissions on one
	 * reference string for one user for one function.
	 */
	private class MySecurityAdvisor implements SecurityAdvisor {
		protected String m_userId;

		protected List<String> m_functions = new ArrayList<String>();

		protected List<String> m_references = new ArrayList<String>();

		public MySecurityAdvisor(String userId, String function,
				String reference) {
			m_userId = userId;
			m_functions.add(function);
			m_references.add(reference);
		}

		public MySecurityAdvisor(String userId, String function,
				List<String> references) {
			m_userId = userId;
			m_functions.add(function);
			m_references = references;
		}

		public MySecurityAdvisor(String userId, List<String> functions,
				List<String> references) {
			m_userId = userId;
			m_functions = functions;
			m_references = references;
		}

		public MySecurityAdvisor(String userId, List<String> functions,
				String reference) {
			m_userId = userId;
			m_functions = functions;
			m_references.add(reference);
		}

		public SecurityAdvice isAllowed(String userId, String function,
				String reference) {
			SecurityAdvice rv = SecurityAdvice.PASS;
			if (m_userId.equals(userId) && m_functions.contains(function)
					&& m_references.contains(reference)) {
				rv = SecurityAdvice.ALLOWED;
			}
			return rv;
		}

		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (obj == this) return true;
			if (obj instanceof MySecurityAdvisor) {
				MySecurityAdvisor mine = (MySecurityAdvisor) obj;
				if (mine.m_userId == null && m_userId != null)
					return false;
				if (mine.m_functions == null && m_functions != null)
					return false;
				if (mine.m_functions != null && mine.m_functions.isEmpty()
						&& m_functions != null && !m_functions.isEmpty())
					return false;
				if (mine.m_references == null && m_references != null)
					return false;
				if (mine.m_references != null && mine.m_references.isEmpty()
						&& m_references != null && !m_references.isEmpty())
					return false;
				if (mine.m_userId != null && m_userId == null)
					return false;
				if (mine.m_functions != null && m_functions == null)
					return false;
				if (mine.m_functions != null && !mine.m_functions.isEmpty()
						&& m_functions != null && m_functions.isEmpty())
					return false;
				if (mine.m_references != null && m_references == null)
					return false;
				if (mine.m_references != null && !mine.m_references.isEmpty()
						&& m_references != null && m_references.isEmpty())
					return false;
				// if both m_references == null, return true?
				if (mine.m_references == null && m_references == null)
					return true;

				Set<String> mineRSet = new HashSet<String>(mine.m_references);
				Set<String> thisRSet = new HashSet<String>(m_references);
				if (mineRSet.hashCode() != thisRSet.hashCode())
					return false;

				Set<String> mineFSet = new HashSet<String>(mine.m_functions);
				Set<String> thisFSet = new HashSet<String>(m_functions);
				if (mineFSet.hashCode() != thisFSet.hashCode())
					return false;

				if (m_userId.equals(mine.m_userId) && thisFSet.equals(mineFSet)
						&& thisRSet.equals(mineRSet))
					return true;
			}
			return false;
		}

		public int hashCode() {
			int result;
			result = m_userId.hashCode();
			result = 29 * result
					+ (m_functions != null ? m_functions.hashCode() : 0);
			result = 29 * result
					+ (m_references != null ? m_references.hashCode() : 0);
			return result;
		}

	}
}
