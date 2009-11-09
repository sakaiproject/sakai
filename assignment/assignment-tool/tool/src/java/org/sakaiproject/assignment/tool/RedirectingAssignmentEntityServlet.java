package org.sakaiproject.assignment.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.assignment.api.AssignmentEntityProvider;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.access.HttpServletAccessProvider;
import org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * Does a redirect to allow basic DirectServlet access to old assignment Entities
 * 
 * @author Joshua Ryan  josh@asu.edu  alt^I
 *
 */

public class RedirectingAssignmentEntityServlet extends HttpServlet
  implements HttpServletAccessProvider {

  private static final long serialVersionUID = 0L;
  private EntityBroker entityBroker;
  private HttpServletAccessProviderManager accessProviderManager;
  
  /**
   * Initialize the servlet.
   * 
   * @param config
   *        The servlet config.
   * @throws ServletException
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    entityBroker = (EntityBroker) ComponentManager
        .get("org.sakaiproject.entitybroker.EntityBroker");
    accessProviderManager = (HttpServletAccessProviderManager) ComponentManager
        .get("org.sakaiproject.entitybroker.access.HttpServletAccessProviderManager");
    if (accessProviderManager != null)
      accessProviderManager.registerProvider(AssignmentEntityProvider.ENTITY_PREFIX, this);
  }
  
  public void handleAccess(HttpServletRequest req, HttpServletResponse res, EntityReference ref) {    
	  Map<String, String> props = entityBroker.getProperties(req.getPathInfo());
	  String target = props.get("url");
	  String user = props.get("security.user");
	  String site_function = props.get("security.site.function");
	  String site_secref = props.get("security.site.ref");
	  String assignment_function = props.get("security.assignment.function");
	  String assignment_secref = props.get("security.assignment.ref");
	  String submissionAttachmentRefStrs = props.get("submissionAttachmentRefs");
	  
	  Session session = SessionManager.getCurrentSession();
	  clearSessionAttributes(session);
	  
	  if (submissionAttachmentRefStrs != null) {
		  String[] submissionAttachmentRefs = submissionAttachmentRefStrs.split(":::");
	  	session.setAttribute("assignment.content.security.advisor", new MySecurityAdvisor(user, "content.read", 
			  Arrays.asList(submissionAttachmentRefs)));
	  	session.setAttribute("assignment.content.decoration.wrapper.refs", submissionAttachmentRefs);
	  }
	  String decoratedContentWrapper = props.get("assignment.content.decoration.wrapper");
	  
	  Object sessionAdvisors = session.getAttribute("sitevisit.security.advisor");
	  Set siteAdvisors = new HashSet();
	  if (sessionAdvisors != null) {
		  siteAdvisors = (Set)sessionAdvisors;  
	  }
	  
	  siteAdvisors.add(new MySecurityAdvisor(user, site_function, site_secref));	   
	  
	  //dump a couple of advisors into session so we can get at them outside of this threadlocal
	  session.setAttribute("sitevisit.security.advisor", siteAdvisors);
	  
	  session.setAttribute("assignment.security.advisor", 
			  new MySecurityAdvisor(user, assignment_function, assignment_secref));
	  
	  session.setAttribute("assignment.content.decoration.wrapper", decoratedContentWrapper);
	  
	  try {
		  res.sendRedirect(target);		  
	  }
	  catch (IOException e) {
		  e.printStackTrace();
	  }
	  return;
  }
  
  private void clearSessionAttributes(Session session) {
	  session.removeAttribute("assignment.content.security.advisor");
	  session.removeAttribute("assignment.content.decoration.wrapper.refs");
	  
  }
  
  
  /**
   * A simple SecurityAdviser that can be used to override permissions on one reference string for one user for one function.
   */
  private class MySecurityAdvisor implements SecurityAdvisor
  {
	  protected String m_userId;

	  protected String m_function;

	  protected List<String> m_references = new ArrayList<String>();

	  public MySecurityAdvisor(String userId, String function, String reference)
	  {
		  m_userId = userId;
		  m_function = function;
		  m_references.add(reference);
	  }
	  
	  public MySecurityAdvisor(String userId, String function, List<String> references)
	  {
		  m_userId = userId;
		  m_function = function;
		  m_references = references;
	  }

	  public SecurityAdvice isAllowed(String userId, String function, String reference)
	  {
		  SecurityAdvice rv = SecurityAdvice.PASS;
		  if (m_userId.equals(userId) && m_function.equals(function) && m_references.contains(reference))
		  {
			  rv = SecurityAdvice.ALLOWED;
		  }
		  return rv;
	  }

	  public boolean equals(Object obj) {
		  MySecurityAdvisor mine = (MySecurityAdvisor)obj;
		  if (mine == null) return false;
		  if (mine.m_userId == null && m_userId != null) return false;
		  if (mine.m_function == null && m_function != null) return false;
		  if (mine.m_references == null && m_references != null) return false;
		  if (mine.m_references.isEmpty() && !m_references.isEmpty()) return false;
		  if (mine.m_userId != null && m_userId == null) return false;
		  if (mine.m_function != null && m_function == null) return false;
		  if (mine.m_references != null && m_references == null) return false;
		  if (!mine.m_references.isEmpty() && m_references.isEmpty()) return false;

		  Set<String> mineSet = new HashSet<String>(mine.m_references);
		  Set<String> thisSet = new HashSet<String>(m_references);
		  if (mineSet.hashCode() != thisSet.hashCode()) return false;
		  
		  if (m_userId.equals(mine.m_userId) && m_function.equals(mine.m_function) && thisSet.equals(mineSet))
			  return true;

		  return false;
	  }

	  public int hashCode() {
		  int result;
	      result = m_userId.hashCode();
	      result = 29 * result + (m_function != null ? m_function.hashCode() : 0);
	      result = 29 * result + (m_references != null ? m_references.hashCode() : 0);
	      return result;
	  }

  }
}
