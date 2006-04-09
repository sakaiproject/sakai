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
import org.sakaiproject.util.ExternalTrustedEvidence;
import org.sakaiproject.util.LoginUtil;

/**
 * <p>
 * Login servlet that checks the container remote user - used in conjunction with the LoginTool.
 * </p>
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Revision$
 */
public class ContainerLogin extends HttpServlet
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(ContainerLogin.class);

	/**
	 * Access the Servlet's information display.
	 * 
	 * @return servlet information.
	 */
	public String getServletInfo()
	{
		return "Sakai Container Login";
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

		// check the remote user for authentication
		try
		{
			String remoteUser = req.getRemoteUser();
			Evidence e = new ExternalTrustedEvidence(remoteUser);
			Authentication a = AuthenticationManager.authenticate(e);

			// login the user
			if (LoginUtil.login(a, req))
			{
				// get the return URL
				String url = (String) session.getAttribute(Tool.HELPER_DONE_URL);
	
				// cleanup session
				session.removeAttribute(Tool.HELPER_MESSAGE);
				session.removeAttribute(Tool.HELPER_DONE_URL);
	
				// redirect to the done URL
				res.sendRedirect(res.encodeRedirectURL(url));
				
				return;
			}
		}
		catch (AuthenticationException ex)
		{
		}

		// mark the session and redirect (for login failuer or authentication exception)
		session.setAttribute(LoginTool.ATTR_CONTAINER_CHECKED, LoginTool.ATTR_CONTAINER_CHECKED);
		res.sendRedirect(res.encodeRedirectURL((String) session.getAttribute(LoginTool.ATTR_RETURN_URL)));
	}
}



