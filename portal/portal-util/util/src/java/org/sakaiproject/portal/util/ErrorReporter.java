/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.email.cover.EmailService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * ErrorReporter helps with end-user formatted error reporting, user feedback
 * collection, logging and emailing for uncaught throwable based errors.
 * This is a util class as it's used by both the CharronPortal and the SkinnableCharronPortal.
 * </p>
 */
@SuppressWarnings("deprecation")
@Slf4j
public class ErrorReporter
{
	/** messages. */
	private static ResourceLoader rb = new ResourceLoader("portal-util");
	private static final ResourceBundle rbDefault = ResourceBundle.getBundle("portal-util", Locale.getDefault());

	private Map<String, String> censoredHeaders = new HashMap<String, String>();

	private Map<String, String> censoredParameters = new HashMap<String, String>();

	private Map<String, String> censoredAttributes = new HashMap<String, String>();

	public ErrorReporter()
	{
		censoredParameters.put("pw", "pw");
		censoredParameters.put("eid", "eid");
		censoredParameters.put("javax.faces.ViewState", "javax.faces.ViewState");
		censoredHeaders.put("cookie","cookie");
		censoredHeaders.put("authorization","authorization");
	}

	/** Following two methods borrowed from RWikiObjectImpl.java * */

	private static MessageDigest shatemplate = null;

	public static String computeSha1(String content)
	{
		String digest = "";
		try
		{
			if (shatemplate == null)
			{
				shatemplate = MessageDigest.getInstance("SHA");
			}

			MessageDigest shadigest = (MessageDigest) shatemplate.clone();
			byte[] bytedigest = shadigest.digest(content.getBytes("UTF8"));
			digest = byteArrayToHexStr(bytedigest);
		}
		catch (Exception ex)
		{
			log.error("Unable to create SHA hash of content");
		}
		return digest;
	}

	private static String byteArrayToHexStr(byte[] data)
	{
		StringBuffer output = new StringBuffer();
		String tempStr = "";
		int tempInt = 0;
		for (int cnt = 0; cnt < data.length; cnt++)
		{
			// Deposit a byte into the 8 lsb of an int.
			tempInt = data[cnt] & 0xFF;
			// Get hex representation of the int as a
			// string.
			tempStr = Integer.toHexString(tempInt);
			// Append a leading 0 if necessary so that
			// each hex string will contain two
			// characters.
			if (tempStr.length() == 1) tempStr = "0" + tempStr;
			// Concatenate the two characters to the
			// output string.
			output.append(tempStr);
		}// end for loop
		return output.toString().toUpperCase();
	}// end byteArrayToHexStr

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
		StringBuilder buf = new StringBuilder();
		if (st != null)
		{
			for (int i = 0; i < st.length; i++)
			{
				buf.append("\n    at "
						+ st[i].getClassName()
						+ "."
						+ st[i].getMethodName()
						+ "("
						+ ((st[i].isNativeMethod()) ? "Native Method" : (st[i]
								.getFileName()
								+ ":" + st[i].getLineNumber())) + ")");
			}
			buf.append("\n");
		}

		return buf.toString();
	}

	/**
	 * Format a one-level stack trace, just showing the place where the
	 * exception occurred (the first entry in the stack trace).
	 * 
	 * @param t
	 *        The throwable.
	 * @return A display of the first stack trace entry for the throwable.
	 */
	protected String getOneTrace(Throwable t)
	{
		StackTraceElement[] st = t.getStackTrace();
		StringBuilder buf = new StringBuilder();
		if (st != null && st.length > 0)
		{
			buf.append("\n    at "
					+ st[1].getClassName()
					+ "."
					+ st[1].getMethodName()
					+ "("
					+ ((st[1].isNativeMethod()) ? "Native Method" : (st[1].getFileName()
							+ ":" + st[1].getLineNumber())) + ")\n");
		}

		return buf.toString();
	}

	/**
	 * Compute the cause of a throwable, checking for the special
	 * ServletException case, and the points-to-self case.
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
	 * Format a throwable for display: list the various throwables drilling down
	 * the cause, and full stacktrack for the final cause.
	 * 
	 * @param t
	 *        The throwable.
	 * @return The string display of the throwable.
	 */
	protected String throwableDisplay(Throwable t)
	{
		StringBuilder buf = new StringBuilder();
		buf.append(t.toString()
				+ ((getCause(t) == null) ? (getStackTrace(t)) : getOneTrace(t)));
		while (getCause(t) != null)
		{
			t = getCause(t);
			buf.append("caused by: ");
			buf.append(t.toString()
					+ ((getCause(t) == null) ? (getStackTrace(t)) : getOneTrace(t)));
		}

		return buf.toString();
	}

	/**
	 * Logger and email the error report details.
	 * 
	 * @param usageSessionId
	 *        The end-user's usage session id.
	 * @param userId
	 *        The end-user's user id.
	 * @param time
	 *        The time of the error.
	 * @param problem
	 *        The stacktrace of the error.
	 * @param problemdigest
	 *        The sha1 digest of the stacktrace.
	 * @param requestURI
	 *        The request URI.
	 * @param userReport
	 *        The end user comments.
	 * @param object
	 * @param placementDisplay
	 */
	protected void logAndMail(String bugId, String usageSessionId, String userId, String time,
			String problem, String problemdigest, String requestURI, String userReport)
	{
		logAndMail(bugId, usageSessionId, userId, time, problem, problemdigest, requestURI, "",
				"", userReport);
	}

	protected void logAndMail(String bugId, String usageSessionId, String userId, String time,
			String problem, String problemdigest, String requestURI,
			String requestDisplay, String placementDisplay, String userReport)
	{
		// logging and emailing should use the system default locale instead of the user's locale
		ResourceBundle rb = rbDefault;
		
		// log
		log.warn(rb.getString("bugreport.bugreport") + " "
				+ rb.getString("bugreport.bugid") + ": " + bugId + " "
				+ rb.getString("bugreport.user") + ": " + userId + " "
				+ rb.getString("bugreport.usagesession") + ": " + usageSessionId + " "
				+ rb.getString("bugreport.time") + ": " + time + " "
				+ rb.getString("bugreport.usercomment") + ": " + userReport + " "
				+ rb.getString("bugreport.stacktrace") + "\n" + problem + "\n"
				+ placementDisplay + "\n" + requestDisplay);

		// mail
		String emailAddr = ServerConfigurationService.getString("portal.error.email");

		if (emailAddr != null)
		{
			String uSessionInfo = "";
			UsageSession usageSession = UsageSessionService.getSession();

			if (usageSession != null)
			{
				uSessionInfo = rb.getString("bugreport.useragent") + ": "
						+ usageSession.getUserAgent() + "\n"
						+ rb.getString("bugreport.browserid") + ": "
						+ usageSession.getBrowserId() + "\n"
						+ rb.getString("bugreport.ip") + ": "
						+ usageSession.getIpAddress() + "\n";
			}

			String pathInfo = "";
			if (requestURI != null)
			{
				pathInfo = rb.getString("bugreport.path") + ": " + requestURI + "\n";
			}

			User user = null;
			String userName = null;
			String userMail = null;
			String userEid = null;
			if (userId != null)
			{
				try
				{
					user = UserDirectoryService.getUser(userId);
					userName = user.getDisplayName();
					userMail = user.getEmail();
					userEid = user.getEid();
				} catch (UserNotDefinedException e) {
					log.warn("logAndMail: could not find userid: " + userId);
				}
			}

			String subject = rb.getString("bugreport.bugreport") + ": " + problemdigest
					+ " / " + usageSessionId;

			String userComment = "";

			if (userReport != null)
			{
				userComment = rb.getString("bugreport.usercomment") + ":\n\n"
						+ userReport + "\n\n\n";
				subject = subject + " " + rb.getString("bugreport.commentflag");
			}

			String from = "\""
					+ ServerConfigurationService.getString("ui.service", "Sakai")
					+ "\" <"+ServerConfigurationService.getString("setup.request","no-reply@" + ServerConfigurationService.getServerName()) + ">";

			String problemDisplay = "";
			
			if (problem != null) {
				problemDisplay = rb.getString("bugreport.stacktrace") + ":\n\n"
				+ problem + "\n\n";
			}
			
			String body = rb.getString("bugreport.bugid") + ": " + bugId + "\n"
					+ rb.getString("bugreport.user") + ": " + userEid + " ("
					+ userName + ")\n" + rb.getString("bugreport.email") + ": "
					+ userMail + "\n" + rb.getString("bugreport.usagesession") + ": "
					+ usageSessionId + "\n" + rb.getString("bugreport.digest") + ": "
					+ problemdigest + "\n" + rb.getString("bugreport.version-sakai")
					+ ": " + ServerConfigurationService.getString("version.sakai") + "\n"
					+ rb.getString("bugreport.version-service") + ": "
					+ ServerConfigurationService.getString("version.service") + "\n"
					+ rb.getString("bugreport.appserver") + ": "
					+ ServerConfigurationService.getServerId() + "\n" + uSessionInfo
					+ pathInfo + rb.getString("bugreport.time") + ": " + time + "\n\n\n"
					+ userComment + problemDisplay + placementDisplay + "\n\n" + requestDisplay;

			EmailService.send(from, emailAddr, subject, body, emailAddr, null, null);
		}
	}

	/**
	 * Handle the inital report of an error, from an uncaught throwable, with a
	 * user display - but returning a string that contins a fragment.
	 * 
	 * @param req
	 *        The request.
	 * @param res
	 *        The response.
	 * @param t
	 *        The uncaught throwable.
	 */
	public String reportFragment(HttpServletRequest req, HttpServletResponse res, Throwable t)
	{
        	BufferedServletResponse bufferedResponse = new BufferedServletResponse(res);
		report(req,bufferedResponse,t,false);
		String fragment = bufferedResponse.getInternalBuffer().getBuffer().toString();
		return fragment;
        }

	/**
	 * Handle the inital report of an error, from an uncaught throwable, with a
	 * user display.
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
		report(req,res,t,true);
	}

	public void report(HttpServletRequest req, HttpServletResponse res, 
		Throwable t, boolean fullPage)
	{
		boolean showStackTrace = SecurityService.isSuperUser() || 
			ServerConfigurationService.getBoolean("portal.error.showdetail", false);
		
		String bugId = IdManager.createUuid(); 
				 
		String headInclude = (String) req.getAttribute("sakai.html.head");
		String bodyOnload = (String) req.getAttribute("sakai.html.body.onload");
		Time reportTime = TimeService.newTime();
		String time = reportTime.toStringLocalDate() + " "
				+ reportTime.toStringLocalTime24();
		String usageSessionId = UsageSessionService.getSessionId();
		String userId = SessionManager.getCurrentSessionUserId();
		String requestDisplay = requestDisplay(req);
		String placementDisplay = placementDisplay();
		String problem = throwableDisplay(t);
		String problemdigest = computeSha1(problem);
		String postAddr = ServerConfigurationService.getPortalUrl() + "/error-report";
		String requestURI = req.getRequestURI();

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
			res.setStatus(javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			res.setContentType("text/html; charset=UTF-8");
			res.addDateHeader("Expires", System.currentTimeMillis()
					- (1000L * 60L * 60L * 24L * 365L));
			res.addDateHeader("Last-Modified", System.currentTimeMillis());
			res
					.addHeader("Cache-Control",
							"no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
			res.addHeader("Pragma", "no-cache");

			PrintWriter out = null;
			try {
				out = res.getWriter();
			} catch (Exception ex ) {
				out = new PrintWriter(res.getOutputStream());
			}

			if ( fullPage ) 
			{
				out
					.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
				out
					.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
				if (headInclude != null)
				{
					out.println("<head>");
					out.println(headInclude);
					out.println("</head>");
				}
				out.println("<body" + bodyOnload + ">");
				out.println("<div class=\"portletBody\">");
			}
			out.println("<h3>" + rb.getString("bugreport.error") + "</h3>");
			out.println("<p>" + rb.getString("bugreport.statement") + "<br /><br /></p>");

			out.println("<h4>" + rb.getString("bugreport.sendtitle") + "</h4>");
			out.println("<p>" + rb.getString("bugreport.sendinstructions") + "</p>");

			out.println("<form action=\"" + postAddr + "\" method=\"POST\">");
			
			if (showStackTrace) {
				out.println("<input type=\"hidden\" name=\"problem\" value=\"");
				out.println(FormattedText.escapeHtml(problem, false));
				out.println("\">");
			}
			
			out.println("<input type=\"hidden\" name=\"problemRequest\" value=\"");
			out.println(FormattedText.escapeHtml(requestDisplay, false));
			out.println("\">");
			out.println("<input type=\"hidden\" name=\"problemPlacement\" value=\"");
			out.println(FormattedText.escapeHtml(placementDisplay, false));
			out.println("\">");
			out.println("<input type=\"hidden\" name=\"problemdigest\" value=\""
					+ FormattedText.escapeHtml(problemdigest, false) + "\">");
			out.println("<input type=\"hidden\" name=\"session\" value=\""
					+ FormattedText.escapeHtml(usageSessionId, false) + "\">");
			out.println("<input type=\"hidden\" name=\"bugid\" value=\""
					+ FormattedText.escapeHtml(bugId, false) + "\">");
			out.println("<input type=\"hidden\" name=\"user\" value=\""
					+ FormattedText.escapeHtml(userId, false) + "\">");
			out.println("<input type=\"hidden\" name=\"time\" value=\""
					+ FormattedText.escapeHtml(time, false) + "\">");

			out
					.println("<table class=\"itemSummary\" cellspacing=\"5\" cellpadding=\"5\">");
			out.println("<tbody>");
			out.println("<tr>");
			out
					.println("<td><textarea rows=\"10\" cols=\"60\" name=\"comment\"></textarea></td>");
			out.println("</tr>");
			out.println("</tbody>");
			out.println("</table>");
			out.println("<div class=\"act\">");
			out.println("<input type=\"submit\" value=\""
					+ rb.getString("bugreport.sendsubmit") + "\">");
			out.println("</div>");
			out.println("</form><br />");

			out.println("<h4>" + rb.getString("bugreport.recoverytitle") + "</h4>");
			out.println("<p>" + rb.getString("bugreport.recoveryinstructions") + "");
			out.println("<ul><li>" + rb.getString("bugreport.recoveryinstructions1")
					+ "</li>");
			out.println("<li>" + rb.getString("bugreport.recoveryinstructions2")
					+ "</li>");
			out.println("<li>" + rb.getString("bugreport.recoveryinstructions3")
					+ "</li></ul><br /><br /></p>");

			if (showStackTrace) {
				out.println("<h4>" + rb.getString("bugreport.detailstitle") + "</h4>");
				out.println("<p>" + rb.getString("bugreport.detailsnote") + "</p>");
				
				out.println("<p><pre>");
				out.println(FormattedText.escapeHtml(problem, false));
				out.println();
				out.println(rb.getString("bugreport.user") + ": "
						+ FormattedText.escapeHtml(userId, false) + "\n");
				out.println(rb.getString("bugreport.usagesession") + ": "
						+ FormattedText.escapeHtml(usageSessionId, false) + "\n");
				out.println(rb.getString("bugreport.time") + ": "
						+ FormattedText.escapeHtml(time, false) + "\n");
				out.println("</pre></p>");
			}
			
			if ( fullPage ) 
			{
				out.println("</body>");
				out.println("</html>");
			}
			
			if (out != null)
			{
				out.close();
			}

			// log and send the preliminary email
			logAndMail(bugId, usageSessionId, userId, time, problem, problemdigest, requestURI,
					requestDisplay, placementDisplay, null);
		}
		catch (Throwable any)
		{
			log.warn(rbDefault.getString("bugreport.troublereporting"), any);
		}
	}

	private String placementDisplay()
	{
		ResourceBundle rb = rbDefault;
		StringBuilder sb = new StringBuilder();
		try
		{
			Placement p = ToolManager.getCurrentPlacement();
			if (p != null)
			{
				sb.append(rb.getString("bugreport.placement")).append("\n");
				sb.append(rb.getString("bugreport.placement.id")).append(p.getToolId())
						.append("\n");
				sb.append(rb.getString("bugreport.placement.context")).append(
						p.getContext()).append("\n");
				sb.append(rb.getString("bugreport.placement.title")).append(p.getTitle())
						.append("\n");
			}
			else
			{
				sb.append(rb.getString("bugreport.placement")).append("\n");
				sb.append(rb.getString("bugreport.placement.none")).append("\n");
			}
		}
		catch (Exception ex)
		{
			log.error("Failed to generate placement display", ex);
			sb.append("Error " + ex.getMessage());
		}

		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	private String requestDisplay(HttpServletRequest request)
	{
		ResourceBundle rb = rbDefault;
		StringBuilder sb = new StringBuilder();
		try
		{
			sb.append(rb.getString("bugreport.request")).append("\n");
			sb.append(rb.getString("bugreport.request.authtype")).append(
					request.getAuthType()).append("\n");
			sb.append(rb.getString("bugreport.request.charencoding")).append(
					request.getCharacterEncoding()).append("\n");
			sb.append(rb.getString("bugreport.request.contentlength")).append(
					request.getContentLength()).append("\n");
			sb.append(rb.getString("bugreport.request.contenttype")).append(
					request.getContentType()).append("\n");
			sb.append(rb.getString("bugreport.request.contextpath")).append(
					request.getContextPath()).append("\n");
			sb.append(rb.getString("bugreport.request.localaddr")).append(
					request.getLocalAddr()).append("\n");
			sb.append(rb.getString("bugreport.request.localname")).append(
					request.getLocalName()).append("\n");
			sb.append(rb.getString("bugreport.request.localport")).append(
					request.getLocalPort()).append("\n");
			sb.append(rb.getString("bugreport.request.method")).append(
					request.getMethod()).append("\n");
			sb.append(rb.getString("bugreport.request.pathinfo")).append(
					request.getPathInfo()).append("\n");
			sb.append(rb.getString("bugreport.request.protocol")).append(
					request.getProtocol()).append("\n");
			sb.append(rb.getString("bugreport.request.querystring")).append(
					request.getQueryString()).append("\n");
			sb.append(rb.getString("bugreport.request.remoteaddr")).append(
					request.getRemoteAddr()).append("\n");
			sb.append(rb.getString("bugreport.request.remotehost")).append(
					request.getRemoteHost()).append("\n");
			sb.append(rb.getString("bugreport.request.remoteport")).append(
					request.getRemotePort()).append("\n");
			sb.append(rb.getString("bugreport.request.requesturl")).append(
					request.getRequestURL()).append("\n");
			sb.append(rb.getString("bugreport.request.scheme")).append(
					request.getScheme()).append("\n");
			sb.append(rb.getString("bugreport.request.servername")).append(
					request.getServerName()).append("\n");
			sb.append(rb.getString("bugreport.request.headers")).append("\n");
			for (Enumeration e = request.getHeaderNames(); e.hasMoreElements();)
			{
				String headerName = (String) e.nextElement();
				boolean censor =  ( censoredHeaders.get(headerName) != null );
				for (Enumeration he = request.getHeaders(headerName); he
						.hasMoreElements();)
				{
					String headerValue = (String) he.nextElement();
					sb.append(rb.getString("bugreport.request.header"))
							.append(headerName).append(":").append(censor?"---censored---":headerValue).append(
									"\n");
				}
			}
			sb.append(rb.getString("bugreport.request.parameters")).append("\n");
			for (Enumeration e = request.getParameterNames(); e.hasMoreElements();)
			{
				
				String parameterName = (String) e.nextElement();
				boolean censor =  ( censoredParameters.get(parameterName) != null );
				String[] paramvalues = request.getParameterValues(parameterName);
				for (int i = 0; i < paramvalues.length; i++)
				{
					sb.append(rb.getString("bugreport.request.parameter")).append(
							parameterName).append(":").append(i).append(":").append(
							censor?"----censored----":paramvalues[i]).append("\n");
				}
			}
			sb.append(rb.getString("bugreport.request.attributes")).append("\n");
			for (Enumeration e = request.getAttributeNames(); e.hasMoreElements();)
			{
				String attributeName = (String) e.nextElement();
				Object attribute = request.getAttribute(attributeName);
				boolean censor =  ( censoredAttributes.get(attributeName) != null );
				sb.append(rb.getString("bugreport.request.attribute")).append(
						attributeName).append(":").append(censor?"----censored----":attribute).append("\n");
			}
			HttpSession session = request.getSession(false);
			if (session != null)
			{
				DateFormat serverLocaleDateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault()); 
				sb.append(rb.getString("bugreport.session")).append("\n");
				sb.append(rb.getString("bugreport.session.creation")).append(
						session.getCreationTime()).append("\n");
				sb.append(rb.getString("bugreport.session.lastaccess")).append(
						session.getLastAccessedTime()).append("\n");
				sb.append(rb.getString("bugreport.session.creationdatetime")).append(
						serverLocaleDateFormat.format(session.getCreationTime())).append("\n");
				sb.append(rb.getString("bugreport.session.lastaccessdatetime")).append(
						serverLocaleDateFormat.format(session.getLastAccessedTime())).append("\n");
				sb.append(rb.getString("bugreport.session.maxinactive")).append(
						session.getMaxInactiveInterval()).append("\n");
				sb.append(rb.getString("bugreport.session.attributes")).append("\n");
				for (Enumeration e = session.getAttributeNames(); e.hasMoreElements();)
				{
					String attributeName = (String) e.nextElement();
					Object attribute = session.getAttribute(attributeName);
					boolean censor =  ( censoredAttributes.get(attributeName) != null );
					sb.append(rb.getString("bugreport.session.attribute")).append(
							attributeName).append(":").append(censor?"----censored----":attribute).append("\n");
				}

			}
		}
		catch (Exception ex)
		{
			log.error("Failed to generate request display", ex);
			sb.append("Error " + ex.getMessage());
		}

		return sb.toString();
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
		String bugId = req.getParameter("bugid");
		String session = req.getParameter("session");
		String user = req.getParameter("user");
		String time = req.getParameter("time");
		String comment = req.getParameter("comment");
		String problem = req.getParameter("problem");
		String problemdigest = req.getParameter("problemdigest");
		String problemRequest = req.getParameter("problemRequest");
		String problemPlacement = req.getParameter("problemPlacement");

		// log and send the followup email
		logAndMail(bugId, session, user, time, problem, problemdigest, null, problemRequest,
				problemPlacement, comment);

		// always redirect from a post
		try
		{
			res.sendRedirect(res.encodeRedirectURL(ServerConfigurationService
					.getPortalUrl()
					+ "/error-reported"));
		}
		catch (IOException e)
		{
			log.warn(rbDefault.getString("bugreport.troubleredirecting"), e);
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
			res.addDateHeader("Expires", System.currentTimeMillis()
					- (1000L * 60L * 60L * 24L * 365L));
			res.addDateHeader("Last-Modified", System.currentTimeMillis());
			res
					.addHeader("Cache-Control",
							"no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
			res.addHeader("Pragma", "no-cache");

			PrintWriter out = res.getWriter();
			out
					.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			out
					.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
			if (headInclude != null)
			{
				out.println("<head>");
				out.println(headInclude);
				out.println("</head>");
			}
			out.println("<body" + bodyOnload + ">");
			out.println("<div class=\"portletBody\">");

			out.println("<h4>" + rb.getString("bugreport.senttitle") + "</h4>");
			out.println("<p>" + rb.getString("bugreport.sentnote") + "<br /><br /></p>");

			out.println("<h4>" + rb.getString("bugreport.recoverytitle") + "</h4>");
			out.println("<p>" + rb.getString("bugreport.recoveryinstructions.reported") + "");
			out.println("<ul><li>" + rb.getString("bugreport.recoveryinstructions1")
					+ "</li>");
			out.println("<li>" + rb.getString("bugreport.recoveryinstructions2")
					+ "</li>");
			out.println("<li>" + rb.getString("bugreport.recoveryinstructions3")
					+ "</li></ul><br /><br /></p>");

			out.println("</body>");
			out.println("</html>");
		}
		catch (Throwable any)
		{
			log.warn(rbDefault.getString("bugreport.troublethanks"), any);
		}
	}
}
