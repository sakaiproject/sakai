/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.login;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.common.authentication.Authentication;
import org.sakaiproject.api.common.authentication.AuthenticationException;
import org.sakaiproject.api.common.authentication.Evidence;
import org.sakaiproject.api.common.authentication.cover.AuthenticationManager;
import org.sakaiproject.api.kernel.session.Session;
import org.sakaiproject.api.kernel.session.cover.SessionManager;
import org.sakaiproject.api.kernel.tool.Tool;
import org.sakaiproject.service.framework.config.cover.ServerConfigurationService;
import org.sakaiproject.util.IdPwEvidence;
import org.sakaiproject.util.LoginUtil;
import org.sakaiproject.util.web.Web;

/**
 * <p>
 * Login tool for Sakai. Works with the ContainerLoginTool servlet to offer container or internal login.
 * </p>
 * <p>
 * This "tool", being login, is not placed, instead each user can interact with only one login at a time. The Sakai Session is used for attributes.
 * </p>
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Revision$
 */
public class LoginTool extends HttpServlet
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(LoginTool.class);

	/** Session attribute used to store a message between steps. */
	protected static final String ATTR_MSG = "sakai.login.message";

	/** Session attribute set and shared with ContainerLoginTool: URL for redirecting back here. */
	public static final String ATTR_RETURN_URL = "sakai.login.return.url";

	/** Session attribute set and shared with ContainerLoginTool: if set we have failed container and need to check internal. */
	public static final String ATTR_CONTAINER_CHECKED = "sakai.login.container.checked";

	private static ResourceBundle rb = ResourceBundle.getBundle("auth");
   
	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "Sakai Login";
	}

	/**
	 * Initialize the servlet.
	 * 
	 * @param config
	 *        The servlet config.
	 * @throws ServletException
	 */
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		M_log.info("init()");
	}

	/**
	 * Shutdown the servlet.
	 */
	public void destroy()
	{
		M_log.info("destroy()");

		super.destroy();
	}

	/**
	 * Respond to requests.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @throws ServletException.
	 * @throws IOException.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// get the session
		Session session = SessionManager.getCurrentSession();

		// get my tool registration
		Tool tool = (Tool) req.getAttribute(Tool.TOOL);

		// recognize what to do from the path
		String option = req.getPathInfo();

		// maybe we don't want to do the container this time
		boolean skipContainer = false;

		// if missing, set it to "/login"
		if ((option == null) || ("/".equals(option)))
		{
			option = "/login";
		}
		
		// look for the extreme login (i.e. to skip container checks)
		else if ("/xlogin".equals(option))
		{
			option = "/login";
			skipContainer = true;
		}

		// get the parts (the first will be "", second will be "login" or "logout")
		String[] parts = option.split("/");

		if (parts[1].equals("logout"))
		{
			// get the session info complete needs, since the logout will invalidate and clear the session
			String returnUrl = (String) session.getAttribute(Tool.HELPER_DONE_URL);

			// logout the user
			LoginUtil.logout();

			complete(returnUrl, null, tool, res);
			return;
		}
		else
		{
			// see if we need to check container
			boolean checkContainer = ServerConfigurationService.getBoolean("container.login", false);
			if (checkContainer && !skipContainer)
			{
				// if we have not checked the container yet, check it now
				if (session.getAttribute(ATTR_CONTAINER_CHECKED) == null)
				{
					// save our return path
					session.setAttribute(ATTR_RETURN_URL, Web.returnUrl(req, null));

					String containerCheckPath = this.getServletConfig().getInitParameter("container");
					String containerCheckUrl = Web.serverUrl(req) + containerCheckPath;

					// support query parms in url for container auth
					String queryString = req.getQueryString();
					if (queryString != null)
						 containerCheckUrl = containerCheckUrl + "?" + queryString;
						
					res.sendRedirect(res.encodeRedirectURL(containerCheckUrl));
					return;
				}
			}

			// send the form
			sendForm(req, res);
		}
	}

	/**
	 * Send the login form
	 * 
	 * @param req
	 *        Servlet request.
	 * @param res
	 *        Servlet response.
	 * @throws IOException
	 */
	protected void sendForm(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		final String headHtml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
				+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">"
				+ "  <head>"
				+ "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"
				+ "    <link href=\"SKIN_ROOT/tool_base.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />"
				+ "    <link href=\"SKIN_ROOT/DEFAULT_SKIN/tool.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />"
				+ "    <meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />"
				+ "    <title>Sakai</title>"
				+ "    <script type=\"text/javascript\" language=\"JavaScript\" src=\"/library/js/headscripts.js\"></script>"
				+ "  </head>"
				+ "  <body onload=\" setFocus(focus_path);parent.updCourier(doubleDeep, ignoreCourier);\">"
				+ "<script type=\"text/javascript\" language=\"JavaScript\">" + "  focus_path = [\"eid\"];" + "</script>";

		final String tailHtml = "</body></html>";

		final String loginHtml = "<table class=\"login\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">"
				+ "		<tr>"
				+ "			<th colspan=\"2\">"
				+ "				Login Required"
				+ "			</th>"
				+ "		</tr>"
				+ "		<tr>"
				+ "			<td class=\"logo\">"
				+ "			</td>"
				+ "			<td class=\"form\">"
				+ "				<form method=\"post\" action=\"ACTION\" enctype=\"application/x-www-form-urlencoded\">"
				+ "                                        MSG"
				+ "							<table border=\"0\" class=\"loginform\">"
				+ "								<tr>"
				+ "									<td>"
				+ "										<label for=\"eid\">EID</label>"
				+ "									</td>"
				+ "									<td>"
				+ "										<input name=\"eid\" id=\"eid\"  type=\"text\"/>"
				+ "									</td>"
				+ "								</tr>"
				+ "								<tr>"
				+ "									<td>"
				+ "										<label for=\"pw\">PW</label>"
				+ "									</td>"
				+ "									<td>"
				+ "										<input name=\"pw\" id=\"pw\"  type=\"password\"/>"
				+ "									</td>"
				+ "								</tr>"
				+ "								<tr>"
				+ "									<td colspan=\"2\">"
				+ "										<input name=\"submit\" type=\"submit\" id=\"submit\" value=\"LoginSubmit\"/>"
				+ "									</td>"
				+ "								</tr>"
				+ "							</table>"
				+ "						</form>"
				+ "					</td>"
				+ "				</tr>"
				+ "			</table>";

		// get the Sakai session
		Session session = SessionManager.getCurrentSession();

		// get my tool registration
		Tool tool = (Tool) req.getAttribute(Tool.TOOL);

		// fragment or not?
		boolean fragment = Boolean.TRUE.toString().equals(req.getAttribute(Tool.FRAGMENT));

		String eidWording = rb.getString("userid");
		String pwWording = rb.getString("log.pass");
		String loginRequired = rb.getString("log.logreq");
		String loginWording = rb.getString("log.login");

		if (!fragment)
		{
			// set our response type
			res.setContentType("text/html; charset=UTF-8");
		}

		String defaultSkin = ServerConfigurationService.getString("skin.default");
		String skinRoot = ServerConfigurationService.getString("skin.repo");

		// get our response writer
		PrintWriter out = res.getWriter();

		if (!fragment)
		{
			// start our complete document
			String head = headHtml.replaceAll("DEFAULT_SKIN", defaultSkin);
			head = head.replaceAll("SKIN_ROOT", skinRoot);
			out.println(head);
		}

		// if we are in helper mode, there might be a helper message
		if (session.getAttribute(Tool.HELPER_MESSAGE) != null)
		{
			out.println("<p>" + session.getAttribute(Tool.HELPER_MESSAGE) + "</p>");
		}

		// add our return URL
		String returnUrl = res.encodeURL(Web.returnUrl(req, null));
		String html = loginHtml.replaceAll("ACTION", res.encodeURL(returnUrl));

		// add our wording
		html = html.replaceAll("EID", eidWording);
		html = html.replaceAll("PW", pwWording);
		html = html.replaceAll("Login Required", loginRequired);
		html = html.replaceAll("LoginSubmit", loginWording);

		// add the default skin
		html = html.replaceAll("DEFAULT_SKIN", defaultSkin);
		html = html.replaceAll("SKIN_ROOT", skinRoot);

		// write a message if present
		String msg = (String) session.getAttribute(ATTR_MSG);
		if (msg != null)
		{
			html = html.replaceAll("MSG", "<div class=\"alertMessage\">" + rb.getString("gen.alert") + " " + msg + "</div>");
			session.removeAttribute(ATTR_MSG);
		}
		else
		{
			html = html.replaceAll("MSG", "");
		}

		// write the login screen
		out.println(html);

		if (!fragment)
		{
			// close the complete document
			out.println(tailHtml);
		}
	}

	/**
	 * Respond to data posting requests.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @throws ServletException.
	 * @throws IOException.
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		// get the Sakai session
		Session session = SessionManager.getCurrentSession();

		// get my tool registration
		Tool tool = (Tool) req.getAttribute(Tool.TOOL);

		// here comes the data back from the form... these fields will be present, blank if not filled in
		String eid = req.getParameter("eid").trim();
		String pw = req.getParameter("pw").trim();

		// one of these will be there, one null, depending on how the submit was done
		String submit = req.getParameter("submit");
		String cancel = req.getParameter("cancel");

		// cancel
		if (cancel != null)
		{
			session.setAttribute(ATTR_MSG, rb.getString("log.canceled"));

			// get the session info complete needs, since the logout will invalidate and clear the session
			String returnUrl = (String) session.getAttribute(Tool.HELPER_DONE_URL);

			// TODO: send to the cancel URL, cleanup session
			complete(returnUrl, session, tool, res);
		}

		// submit
		else
		{
			Evidence e = new IdPwEvidence(eid, pw);

			// authenticate
			try
			{
				if ((eid.length() == 0) || (pw.length() == 0))
				{
					throw new AuthenticationException("missing required fields");
				}

				Authentication a = AuthenticationManager.authenticate(e);

				// login the user
				if (LoginUtil.login(a, req))
				{
					// get the session info complete needs, since the logout will invalidate and clear the session
					String returnUrl = (String) session.getAttribute(Tool.HELPER_DONE_URL);
		
					complete(returnUrl, session, tool, res);
				}
				else
				{
					session.setAttribute(ATTR_MSG, rb.getString("log.tryagain"));
					res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, null)));					
				}
			}
			catch (AuthenticationException ex)
			{
				session.setAttribute(ATTR_MSG, rb.getString("log.invalid"));

				// respond with a redirect back here
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, null)));
			}
		}
	}

	/**
	 * Cleanup and redirect when we have a successful login / logout
	 * 
	 * @param session
	 * @param tool
	 * @param res
	 * @throws IOException
	 */
	protected void complete(String returnUrl, Session session, Tool tool, HttpServletResponse res) throws IOException
	{
		// cleanup session
		if (session != null)
		{
			session.removeAttribute(Tool.HELPER_MESSAGE);
			session.removeAttribute(Tool.HELPER_DONE_URL);
			session.removeAttribute(ATTR_MSG);
			session.removeAttribute(ATTR_RETURN_URL);
			session.removeAttribute(ATTR_CONTAINER_CHECKED);
		}

		// if we end up with nowhere to go, go to the portal
		if (returnUrl == null)
		{
			returnUrl = ServerConfigurationService.getPortalUrl();
			M_log.info("complete: nowhere set to go, going to portal");
		}

		// redirect to the done URL
		res.sendRedirect(res.encodeRedirectURL(returnUrl));
	}
}
