/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.email.cover.EmailService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.webapp.cover.SessionManager;

/**
 * <p>
 * ErrorReporter helps with end-user formatted error reporting, user feedback collection, logging and emailing for uncaught throwable based errors.
 * </p>
 */
public class ErrorReporter
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(ErrorReporter.class);

	/** resource loader to use */
	protected ResourceLoader rb = null;

	public ErrorReporter(ResourceLoader rb)
	{
		this.rb = rb;
	}

	/**
	 * Format the full stack trace.
	 * 
	 * @param t
	 *        The throwable.
	 * @return A display of the full stack trace for the throwable.
	 */
	protected String getStackTrace(Throwable t)
	{
		StackTraceElement[] st = t.getStackTrace();
		StringBuffer buf = new StringBuffer();
		if (st != null)
		{
			for (int i = 0; i < st.length; i++)
			{
				buf.append("\n    at " + st[i].getClassName() + "." + st[i].getMethodName() + "("
						+ ((st[i].isNativeMethod()) ? "Native Method" : (st[i].getFileName() + ":" + st[i].getLineNumber())) + ")");
			}
			buf.append("\n");
		}

		return buf.toString();
	}

	/**
	 * Format a one-level stack trace, just showing the place where the exception occurred (the first entry in the stack trace).
	 * 
	 * @param t
	 *        The throwable.
	 * @return A display of the first stack trace entry for the throwable.
	 */
	protected String getOneTrace(Throwable t)
	{
		StackTraceElement[] st = t.getStackTrace();
		StringBuffer buf = new StringBuffer();
		if (st != null && st.length > 0)
		{
			buf.append("\n    at " + st[1].getClassName() + "." + st[1].getMethodName() + "("
					+ ((st[1].isNativeMethod()) ? "Native Method" : (st[1].getFileName() + ":" + st[1].getLineNumber())) + ")\n");
		}

		return buf.toString();
	}

	/**
	 * Compute the cause of a throwable, checking for the special ServletException case, and the points-to-self case.
	 * 
	 * @param t
	 *        The throwable.
	 * @return The cause of the throwable, or null if there is none.
	 */
	protected Throwable getCause(Throwable t)
	{
		Throwable rv = null;

		// ServletException is non-standard
		if (t instanceof ServletException)
		{
			rv = ((ServletException) t).getRootCause();
		}

		// try for the standard cause
		if (rv == null) rv = t.getCause();

		// clear it if the cause is pointing at the throwable
		if ((rv != null) && (rv == t)) rv = null;

		return rv;
	}

	/**
	 * Format a throwable for display: list the various throwables drilling down the cause, and full stacktrack for the final cause.
	 * 
	 * @param t
	 *        The throwable.
	 * @return The string display of the throwable.
	 */
	protected String throwableDisplay(Throwable t)
	{
		StringBuffer buf = new StringBuffer();
		buf.append(t.toString() + ((getCause(t) == null) ? (getStackTrace(t)) : getOneTrace(t)));
		while (getCause(t) != null)
		{
			t = getCause(t);
			buf.append("caused by: ");
			buf.append(t.toString() + ((getCause(t) == null) ? (getStackTrace(t)) : getOneTrace(t)));
		}

		return buf.toString();
	}

	/**
	 * Log and email the error report details.
	 * 
	 * @param usageSessionId
	 *        The end-user's usage session id.
	 * @param userId
	 *        The end-user's user id.
	 * @param time
	 *        The time of the error.
	 * @param problem
	 *        The stacktrace of the error.
	 * @param userReport
	 *        The end user comments.
	 */
	protected void logAndMail(String usageSessionId, String userId, String time, String problem, String userReport)
	{
		// log
		M_log.warn(rb.getString("bugreport.bugreport") + " " + rb.getString("bugreport.user") + ": " + userId + " "
				+ rb.getString("bugreport.usagesession") + ": " + usageSessionId + " " + rb.getString("bugreport.time")
				+ ": " + time + " " + rb.getString("bugreport.usercomment") + ": " + userReport + " " + rb.getString("bugreport.stacktrace") + "\n" + problem);

		// mail
		String emailAddr = ServerConfigurationService.getString("portal.error.email");
		if (emailAddr != null)
		{
			String from = "\"" + ServerConfigurationService.getString("ui.service", "Sakai") + "\"<no-reply@"
					+ ServerConfigurationService.getServerName() + ">";
			String subject = rb.getString("bugreport.bugreport") + ": " + usageSessionId;
			String body = rb.getString("bugreport.user") + ": " + userId + "\n"
					+ rb.getString("bugreport.usagesession") + ": " + usageSessionId + "\n"
					+ rb.getString("bugreport.time") + ": " + time + "\n\n\n" + rb.getString("bugreport.usercomment")
					+ ":\n\n" + userReport + "\n\n\n" + rb.getString("bugreport.stacktrace") + ":\n\n" + problem;

			EmailService.send(from, emailAddr, subject, body, null, null, null);
		}
	}

	/**
	 * Handle the inital report of an error, from an uncaught throwable, with a user display.
	 * 
	 * @param req
	 *        The request.
	 * @param res
	 *        The response.
	 * @param t
	 *        The uncaught throwable.
	 */
	public void report(HttpServletRequest req, HttpServletResponse res, Throwable t)
	{
		String headInclude = (String) req.getAttribute("sakai.html.head");
		String bodyOnload = (String) req.getAttribute("sakai.html.body.onload");
		String time = TimeService.newTime().toStringLocalFullZ();
		String usageSessionId = UsageSessionService.getSessionId();
		String userId = SessionManager.getCurrentSessionUserId();
		String problem = throwableDisplay(t);
		String postAddr = ServerConfigurationService.getPortalUrl() + "/error-report";

		if (bodyOnload == null)
		{
			bodyOnload = "";
		}
		else
		{
			bodyOnload = " onload=\"" + bodyOnload + "\"";
		}
		try
		{
			// headers
			res.setContentType("text/html; charset=UTF-8");
			res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
			res.addDateHeader("Last-Modified", System.currentTimeMillis());
			res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
			res.addHeader("Pragma", "no-cache");

			PrintWriter out = res.getWriter();
			out
					.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
			if (headInclude != null)
			{
				out.println("<head>");
				out.println(headInclude);
				out.println("</head>");
			}
			out.println("<body" + bodyOnload + ">");
			out.println("<div class=\"portletBody\">");
			out.println("<h3>" + rb.getString("bugreport.error") + "</h3>");
			out.println("<p>" + rb.getString("bugreport.statement") + "<br /><br /></p>");

			out.println("<h4>" + rb.getString("bugreport.sendtitle") + "</h4>");
			out
					.println("<p>" + rb.getString("bugreport.sendinstructions") + "</p>");

			out.println("<form action=\"" + postAddr + "\" method=\"POST\">");
			out.println("<input type=\"hidden\" name=\"problem\" value=\"");
			out.println(problem);
			out.println("\">");
			out.println("<input type=\"hidden\" name=\"session\" value=\"" + usageSessionId + "\">");
			out.println("<input type=\"hidden\" name=\"user\" value=\"" + userId + "\">");
			out.println("<input type=\"hidden\" name=\"time\" value=\"" + time + "\">");

			out.println("<table class=\"itemSummary\" cellspacing=\"5\" cellpadding=\"5\">");
			out.println("<tbody>");
			out.println("<tr>");
			out.println("<td><textarea rows=\"10\" cols=\"60\" name=\"comment\"></textarea></td>");
			out.println("</tr>");
			out.println("</tbody>");
			out.println("</table>");
			out.println("<div class=\"act\">");
			out.println("<input type=\"submit\" value=\"" + rb.getString("bugreport.sendsubmit") + "\">");
			out.println("</div>");
			out.println("</form><br />");

			out.println("<h4>" + rb.getString("bugreport.recoverytitle") + "</h4>");
			out.println("<p>" + rb.getString("bugreport.recoveryinstructions") + "");
			out.println("<ul><li>" + rb.getString("bugreport.recoveryinstructions1") + "</li>");
			out.println("<li>" + rb.getString("bugreport.recoveryinstructions2") + "</li>");
			out.println("<li>" + rb.getString("bugreport.recoveryinstructions3") + "</li></ul><br /><br /></p>");

			out.println("<h4>" + rb.getString("bugreport.detailstitle") + "</h4>");
			out.println("<p>" + rb.getString("bugreport.detailsnote") + "</p>");
			out.println("<p><pre>");
			out.println(problem);
			out.println();
			out.println(rb.getString("bugreport.user") + ": " + userId + "\n");
			out.println(rb.getString("bugreport.usagesession") + ": " + usageSessionId + "\n");
			out.println(rb.getString("bugreport.time") + ": " + time + "\n");
			out.println("</pre></p>");

			out.println("</body>");
			out.println("</html>");

			// log and send the preliminary email
			logAndMail(usageSessionId, userId, time, problem, null);
		}
		catch (Throwable any)
		{
			M_log.warn(rb.getString("bugreport.troublereporting"), any);
		}
	}

	/**
	 * Accept the user feedback post.
	 * 
	 * @param req
	 *        The request.
	 * @param res
	 *        The response.
	 */
	public void postResponse(HttpServletRequest req, HttpServletResponse res)
	{
		String session = req.getParameter("session");
		String user = req.getParameter("user");
		String time = req.getParameter("time");
		String comment = req.getParameter("comment");
		String problem = req.getParameter("problem");

		// log and send the preliminary email
		logAndMail(session, user, time, problem, comment);

		// always redirect from a post
		try
		{
			res.sendRedirect(res.encodeRedirectURL(ServerConfigurationService.getPortalUrl() + "/error-reported"));
		}
		catch (IOException e)
		{
			M_log.warn(rb.getString("bugreport.troubleredirecting"), e);
		}
	}

	/**
	 * Accept the user feedback post.
	 * 
	 * @param req
	 *        The request.
	 * @param res
	 *        The response.
	 */
	public void thanksResponse(HttpServletRequest req, HttpServletResponse res)
	{
		String headInclude = (String) req.getAttribute("sakai.html.head");
		String bodyOnload = (String) req.getAttribute("sakai.html.body.onload");

		if (bodyOnload == null)
		{
			bodyOnload = "";
		}
		else
		{
			bodyOnload = " onload=\"" + bodyOnload + "\"";
		}
		try
		{
			// headers
			res.setContentType("text/html; charset=UTF-8");
			res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
			res.addDateHeader("Last-Modified", System.currentTimeMillis());
			res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
			res.addHeader("Pragma", "no-cache");

			PrintWriter out = res.getWriter();
			out
					.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
			if (headInclude != null)
			{
				out.println("<head>");
				out.println(headInclude);
				out.println("</head>");
			}
			out.println("<body" + bodyOnload + ">");
			out.println("<div class=\"portletBody\">");
			out.println("<h3>" + rb.getString("bugreport.error") + "</h3>");
			out.println("<p>" + rb.getString("bugreport.statement") + "<br /><br /></p>");

			out.println("<h4>" + rb.getString("bugreport.senttitle") + "</h4>");
			out.println("<p>" + rb.getString("bugreport.sentnote") + "<br /><br /></p>");

			out.println("<h4>" + rb.getString("bugreport.recoverytitle") + "</h4>");
			out.println("<p>" + rb.getString("bugreport.recoveryinstructions") + "");
			out.println("<ul><li>" + rb.getString("bugreport.recoveryinstructions1") + "</li>");
			out.println("<li>" + rb.getString("bugreport.recoveryinstructions2") + "</li>");
			out.println("<li>" + rb.getString("bugreport.recoveryinstructions3") + "</li></ul><br /><br /></p>");

			out.println("</body>");
			out.println("</html>");
		}
		catch (Throwable any)
		{
			M_log.warn(rb.getString("bugreport.troublethanks"), any);
		}
	}
}
