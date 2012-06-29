/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.blti;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Set;
import java.util.Iterator;
import java.lang.StringBuffer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.server.OAuthServlet;
import net.oauth.signature.OAuthSignatureMethod;

import org.imsglobal.basiclti.XMLMap;

import org.sakaiproject.component.cover.ComponentManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.imsglobal.basiclti.BasicLTIUtil;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
import org.imsglobal.basiclti.BasicLTIConstants;
import org.sakaiproject.basiclti.util.ShaUtil;
import org.sakaiproject.util.FormattedText;

import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.Assignment;

import org.imsglobal.pox.IMSPOXRequest;

import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.util.foorm.SakaiFoorm;
import org.sakaiproject.util.foorm.FoormUtil;

/**
 * Notes:
 * 
 * This program is directly exposed as a URL to receive IMS Basic LTI messages
 * so it must be carefully reviewed and any changes must be looked at carefully.
 * Here are some issues:
 * 
 * - This uses the RemoteHostFilter so by default it only accepts local IP
 * addresses. This configuration can be changed in web.xml or using the
 * webservices.allow, etc (see RemoteHostFilter)
 * 
 * - This will only function when it is enabled via sakai.properties
 * 
 * - This servlet makes use of security advisors - once an advisor has been
 * added, it must be removed - often in a finally. Also the code below only adds
 * the advisor for very short segments of code to allow for easier review.
 * 
 * Implemented using a SHA-1 hash of the effective context_id and then stores
 * the original context_id in a site.property "lti_context_id" which will be
 * useful for later reference. Since SHA-1 hashes to 40 chars, that would leave
 * us 59 chars (i.e. 58 + ":") to use for LTI key. This also means that the new
 * maximum supported size of an effective context_id is the maximum message size
 * of SHA-1: maximum length of (264 ? 1) bits.
 */

@SuppressWarnings("deprecation")
public class ServiceServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Log M_log = LogFactory.getLog(ServiceServlet.class);
	private static ResourceLoader rb = new ResourceLoader("blis");

    protected static SakaiFoorm foorm = new SakaiFoorm();

    protected static LTIService ltiService = null;

	private final String returnHTML = 
		"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n" + 
		"	\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" + 
		"<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n" + 
		"<body>\n" + 
		"<script language=\"javascript\">\n" + 
		"$message = '<div align=\"center\" style=\"text-align:left;width:80%;margin-top:5px;margin-left:auto;margin-right:auto;border-width:1px 1px 1px 1px;border-style:solid;border-color: gray;padding:.5em;font-family:Verdana,Arial,Helvetica,sans-serif;font-size:.8em\"><p>MESSAGE</p>';\n" +
		"$closeText = '<p><a href=\"javascript: self.close()\">CLOSETEXT</a></p>';\n" +
		"$gotMessage = GOTMESSAGE;\n" +
		"if(self.location==top.location) {\n" + 
		"  if ( $gotMessage ) {\n" +
		"    document.write($message);\n" +
		"    document.write($closeText);\n" +
		"  } else {\n" + 
		"    self.close();\n" +
		"  }\n" + 
		"} else {\n" +
		"  document.write($message);\n" +
		"}\n" +
		"</script>\n" + 
		"</div></body>\n" + 
		"</html>\n";

	/**
	 * Setup a security advisor.
	 */
	public void pushAdvisor() {
		// setup a security advisor
		SecurityService.pushAdvisor(new SecurityAdvisor() {
				public SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				return SecurityAdvice.ALLOWED;
				}
				});
	}

	/**
	 * Remove our security advisor.
	 */
	public void popAdvisor() {
		SecurityService.popAdvisor();
	}

	public void doError(HttpServletRequest request,HttpServletResponse response, 
			Map<String, Object> theMap, String s, String message, Exception e) 
		throws java.io.IOException 
		{
			if (e != null) {
				M_log.error(e.getLocalizedMessage(), e);
			}
			theMap.put("/message_response/statusinfo/codemajor", "Fail");
			theMap.put("/message_response/statusinfo/severity", "Error");
			String msg = rb.getString(s) + ": " + message;
			M_log.info(msg);
			theMap.put("/message_response/statusinfo/description", FormattedText.escapeHtmlFormattedText(msg));
			String theXml = XMLMap.getXML(theMap, true);
			PrintWriter out = response.getWriter();
			out.println(theXml);
		}

	@Override
		public void init(ServletConfig config) throws ServletException {
			super.init(config);
			if ( ltiService == null ) ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
		}

	/* launch_presentation_return_url=http://lmsng.school.edu/portal/123/page/988/

		The TP may add a parameter called lti_errormsg that includes some detail as to 
		the nature of the error.  The lti_errormsg value should make sense if displayed 
		to the user.  If the tool has displayed a message to the end user and only wants 
		to give the TC a message to log, use the parameter lti_errorlog instead of 
		lti_errormsg. If the tool is terminating normally, and wants a message displayed 
		to the user it can include a text message as the lti_msg parameter to the 
		return URL. If the tool is terminating normally and wants to give the TC a 
		message to log, use the parameter lti_log. 

		http://localhost:8080/imsblis/service/return-url/site/12345
		http://localhost:8080/imsblis/service/return-url/pda/12345
	*/
	protected void handleReturnUrl(HttpServletRequest request, HttpServletResponse response)
		throws IOException
	{
		String lti_errorlog = request.getParameter("lti_errorlog");
		if ( lti_errorlog != null ) M_log.error(lti_errorlog);
		String lti_errormsg = request.getParameter("lti_errormsg");
		if ( lti_errormsg != null ) M_log.error(lti_errormsg);
		String lti_log = request.getParameter("lti_log");
		if ( lti_log != null ) M_log.info(lti_log);
		String lti_msg = request.getParameter("lti_msg");
		if ( lti_msg != null ) M_log.info(lti_msg);
		
		String message = rb.getString("outcome.tool.finished");
		String gotMessage = "false";
		if ( lti_msg != null ) {
			message = rb.getString("outcome.tool.lti_msg") + " " + lti_msg;
			gotMessage = "true";
		} else if ( lti_errormsg != null ) {
			message = rb.getString("outcome.tool.lti_errormsg") + " " + lti_errormsg;
			gotMessage = "true";
		}

		String rpi = request.getPathInfo();
		if ( rpi.length() > 11 ) rpi = rpi.substring(11);
		String portalUrl = ServerConfigurationService.getPortalUrl();
		portalUrl = portalUrl + rpi;
		String output = returnHTML.replace("URL",portalUrl);
		output = output.replace("GOTMESSAGE",gotMessage);
		output = output.replace("MESSAGE",message);
		output = output.replace("CLOSETEXT",rb.getString("outcome.tool.close.window"));
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println(output);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String rpi = request.getPathInfo();
		if ( rpi.startsWith("/return-url") ) {
			handleReturnUrl(request, response);
			return;
		} 
		doPost(request, response);
	}

	@SuppressWarnings("unchecked")
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			String contentType = request.getContentType();
			if ( contentType != null && contentType.startsWith("application/xml") ) {
				doPostXml(request, response);
			} else {
				doPostForm(request, response);
			}
		}

	@SuppressWarnings("unchecked")
		protected void doPostForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			String ipAddress = request.getRemoteAddr();

			M_log.debug("Basic LTI Service request from IP=" + ipAddress);

			String allowOutcomes = ServerConfigurationService.getString(
					SakaiBLTIUtil.BASICLTI_OUTCOMES_ENABLED, null);
			if ( ! "true".equals(allowOutcomes) ) allowOutcomes = null;

			String allowSettings = ServerConfigurationService.getString(
					SakaiBLTIUtil.BASICLTI_SETTINGS_ENABLED, null);
			if ( ! "true".equals(allowSettings) ) allowSettings = null;

			String allowRoster = ServerConfigurationService.getString(
					SakaiBLTIUtil.BASICLTI_ROSTER_ENABLED, null);
			if ( ! "true".equals(allowRoster) ) allowRoster = null;

			if (allowOutcomes == null && allowSettings == null && allowRoster == null ) {
				M_log.warn("Basic LTI Services are disabled IP=" + ipAddress);
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			// Lets return an XML Response
			Map<String,Object> theMap = new TreeMap<String,Object>();

			Map<String,String[]> params = (Map<String,String[]>)request.getParameterMap();
			for (Map.Entry<String,String[]> param : params.entrySet()) {
				M_log.debug(param.getKey() + ":" + param.getValue()[0]);
			}

			//check lti_message_type
			String lti_message_type = request.getParameter(BasicLTIConstants.LTI_MESSAGE_TYPE);
			theMap.put("/message_response/lti_message_type", lti_message_type);
			String sourcedid = null;
			String message_type = null;
			if( BasicLTIUtil.equals(lti_message_type, "basic-lis-replaceresult") || 
					BasicLTIUtil.equals(lti_message_type, "basic-lis-createresult") || 
					BasicLTIUtil.equals(lti_message_type, "basic-lis-updateresult") || 
					BasicLTIUtil.equals(lti_message_type, "basic-lis-deleteresult") || 
					BasicLTIUtil.equals(lti_message_type, "basic-lis-readresult") ) {
				sourcedid = request.getParameter("sourcedid");
				if ( allowOutcomes != null ) message_type = "basicoutcome";
			} else if( BasicLTIUtil.equals(lti_message_type, "basic-lti-loadsetting") || 
					BasicLTIUtil.equals(lti_message_type, "basic-lti-savesetting") || 
					BasicLTIUtil.equals(lti_message_type, "basic-lti-deletesetting") ) {
				sourcedid = request.getParameter("id");
				if ( allowSettings != null ) message_type = "toolsetting";
			} else if( BasicLTIUtil.equals(lti_message_type, "basic-lis-readmembershipsforcontext") ) {
				sourcedid = request.getParameter("id");
				if ( allowRoster != null ) message_type = "roster";
			} else {
				doError(request, response, theMap, "outcomes.invalid", "lti_message_type="+lti_message_type, null);
				return;
			}

			// If we have not gotten one of our allowed message types, stop now
			if ( message_type == null ) {
				doError(request, response, theMap, "outcomes.invalid", "lti_message_type="+lti_message_type, null);
				return;
			}

			// No point continuing without a sourcedid
			if(BasicLTIUtil.isBlank(sourcedid)) {
				doError(request, response, theMap, "outcomes.missing", "sourcedid", null);
				return;
			}

			String lti_version = request.getParameter(BasicLTIConstants.LTI_VERSION);
			if(!BasicLTIUtil.equals(lti_version, "LTI-1p0")) {
				doError(request, response, theMap, "outcomes.invalid", "lti_version="+lti_version, null);
				return;
			}

			String oauth_consumer_key = request.getParameter("oauth_consumer_key");
			if(BasicLTIUtil.isBlank(oauth_consumer_key)) {
				doError(request, response, theMap, "outcomes.missing", "oauth_consumer_key", null);
				return;
			}

			// Sadly not supported easily using the Gradebook API - may have to dig
			// deeper later
			if ( BasicLTIUtil.equals(lti_message_type, "basic-lis-deleteresult") ) {
				theMap.put("/message_response/statusinfo/codemajor", "Unsupported");
				theMap.put("/message_response/statusinfo/severity", "Error");
				theMap.put("/message_response/statusinfo/codeminor", "cannotdelete");
				String theXml = XMLMap.getXML(theMap, true);
				PrintWriter out = response.getWriter();
				out.println(theXml);
				return;
			}

			// Truncate this to the maximum length to insure no cruft at the end
			if ( sourcedid.length() > 2048) sourcedid = sourcedid.substring(0,2048);

			// Attempt to parse the sourcedid, any failure is fatal
			String placement_id = null;
			String signature = null;
			String user_id = null;
			try {
				int pos = sourcedid.indexOf(":::");
				if ( pos > 0 ) {
					signature = sourcedid.substring(0, pos);
					String dec2 = sourcedid.substring(pos+3);
					pos = dec2.indexOf(":::");
					user_id = dec2.substring(0,pos);
					placement_id = dec2.substring(pos+3);
				}
			} catch (Exception e) {
				// Log some detail for ourselves
				M_log.warn("Unable to decrypt result_sourcedid IP=" + ipAddress + " Error=" + e.getMessage(),e);
				signature = null;
				placement_id = null;
				user_id = null;
			}

			// Send a more generic message back to the caller
			if ( placement_id == null || user_id == null ) {
				doError(request, response, theMap, "outcomes.sourcedid", "sourcedid", null);
				return;
			}

			M_log.debug("signature="+signature);
			M_log.debug("user_id="+user_id);
			M_log.debug("placement_id="+placement_id);

			Properties pitch = getPropertiesFromPlacement(placement_id);
			if ( pitch == null ) {
				M_log.debug("Error retrieving result_sourcedid information");
				doError(request, response, theMap, "outcomes.sourcedid", "sourcedid", null);
				return;
			}
	
			String siteId = pitch.getProperty(LTIService.LTI_SITE_ID);
			Site site = null;
			try { 
				site = SiteService.getSite(siteId);
			} catch (Exception e) {
				M_log.debug("Error retrieving result_sourcedid site: "+e.getLocalizedMessage(), e);
			}

			// Send a more generic message back to the caller
			if (  site == null ) {
				doError(request, response, theMap, "outcomes.sourcedid", "sourcedid", null);
				return;
			}

			// Check the message signature using OAuth
			String oauth_secret = pitch.getProperty(LTIService.LTI_SECRET);

			OAuthMessage oam = OAuthServlet.getMessage(request, null);
			OAuthValidator oav = new SimpleOAuthValidator();
			OAuthConsumer cons = new OAuthConsumer("about:blank#OAuth+CallBack+NotUsed", oauth_consumer_key,oauth_secret, null);

			OAuthAccessor acc = new OAuthAccessor(cons);

			String base_string = null;
			try {
				base_string = OAuthSignatureMethod.getBaseString(oam);
			} catch (Exception e) {
				M_log.error(e.getLocalizedMessage(), e);
				base_string = null;
			}

			try {
				oav.validateMessage(oam, acc);
			} catch (Exception e) {
				M_log.warn("Provider failed to validate message");
				M_log.warn(e.getLocalizedMessage(), e);
				if (base_string != null) {
					M_log.warn(base_string);
				}
				doError(request, response, theMap, "outcome.no.validate", oauth_consumer_key, null);
				return;
			}

			// Check the signature of the sourcedid to make sure it was not altered
			String placement_secret  = pitch.getProperty(LTIService.LTI_PLACEMENTSECRET);

			// Send a generic message back to the caller
			if ( placement_secret == null ) {
				doError(request, response, theMap, "outcomes.sourcedid", "sourcedid", null);
				return;
			}

			String pre_hash = placement_secret + ":::" + user_id + ":::" + placement_id;
			String received_signature = ShaUtil.sha256Hash(pre_hash);
			M_log.debug("Received signature="+signature+" received="+received_signature);
			boolean matched = signature.equals(received_signature);

			String old_placement_secret  = pitch.getProperty(LTIService.LTI_OLDPLACEMENTSECRET);
			if ( old_placement_secret != null && ! matched ) {
				pre_hash = placement_secret + ":::" + user_id + ":::" + placement_id;
				received_signature = ShaUtil.sha256Hash(pre_hash);
				M_log.debug("Received signature II="+signature+" received="+received_signature);
				matched = signature.equals(received_signature);
			}

			// Send a message back to the caller
			if ( ! matched ) {
				doError(request, response, theMap, "outcomes.sourcedid", "sourcedid", null);
				return;
			}

			// Perform the message-specific handling
			if ( "basicoutcome".equals(message_type) ) processOutcome(request, response, lti_message_type, site, siteId, placement_id, pitch, user_id, theMap);

			if ( "toolsetting".equals(message_type) ) processSetting(request, response, lti_message_type, site, siteId, placement_id, pitch, user_id, theMap);

			if ( "roster".equals(message_type) ) processRoster(request, response, lti_message_type, site, siteId, placement_id, pitch, user_id, theMap);
		}

	protected void processSetting(HttpServletRequest request, HttpServletResponse response, 
			String lti_message_type, 
			Site site, String siteId, String placement_id, Properties pitch,
			String user_id,  Map<String, Object> theMap)
		throws java.io.IOException
		{
			String setting = null;

			// Check for permission in placement
			String allowSetting = pitch.getProperty(LTIService.LTI_ALLOWSETTINGS);
			if ( ! "on".equals(allowSetting) ) {
				doError(request, response, theMap, "outcomes.invalid", "lti_message_type="+lti_message_type, null);
				return;
			}

			pushAdvisor();
			boolean success = false;
			try { 
				if ( "basic-lti-loadsetting".equals(lti_message_type) ) {
					setting = pitch.getProperty(LTIService.LTI_SETTINGS);
					if ( setting != null ) {
						theMap.put("/message_response/setting/value", setting);
					}
					success = true;
				} else {
					if ( isPlacement(placement_id) ) {
						ToolConfiguration placement = SiteService.findTool(placement_id);
						if ( "basic-lti-savesetting".equals(lti_message_type) ) {
							setting = request.getParameter("setting");
							if ( setting == null ) {
								M_log.warn("No setting parameter");
								doError(request, response, theMap, "setting.empty", "", null);
							} else {
								if ( setting.length() > 8096) setting = setting.substring(0,8096);
								placement.getPlacementConfig().setProperty("toolsetting", setting);
							}
						} else if ( "basic-lti-deletesetting".equals(lti_message_type) ) {
							placement.getPlacementConfig().remove("toolsetting");
						}
						try {
							placement.save();
							success = true;
						} catch(Exception e) {
							doError(request, response, theMap, "setting.save.fail", "", e);
						}
					} else {
						Map<String,Object> content = null;
						String contentStr = pitch.getProperty("contentKey");
						Long contentKey = foorm.getLongKey(contentStr);
						if ( contentKey > 0 ) content = ltiService.getContentDao(contentKey, siteId);
						if ( content != null ) {
							if ( "basic-lti-savesetting".equals(lti_message_type) ) {
								setting = request.getParameter("setting");
								if ( setting == null ) {
									M_log.warn("No setting parameter");
									doError(request, response, theMap, "setting.empty", "", null);
								} else {
									if ( setting.length() > 8096) setting = setting.substring(0,8096);
									content.put(LTIService.LTI_SETTINGS,setting);
									success = true;
								}
							} else if ( "basic-lti-deletesetting".equals(lti_message_type) ) {
								content.put(LTIService.LTI_SETTINGS,null);
								success = true;
							}
							if ( success ) {
								Object result = ltiService.updateContentDao(contentKey,content, siteId);
								if ( result instanceof String ) {
									M_log.warn("Setting update failed");
									doError(request, response, theMap, "setting.fail", "", null);
									success = false;
								}
							}
						}
					}
				}
			} catch (Exception e) {
				doError(request, response, theMap, "setting.fail", "", e);
			} finally {
				popAdvisor();
			}

			if ( ! success ) return;

			theMap.put("/message_response/statusinfo/codemajor", "Success");
			theMap.put("/message_response/statusinfo/severity", "Status");
			theMap.put("/message_response/statusinfo/codeminor", "fullsuccess");
			String theXml = XMLMap.getXML(theMap, true);
			PrintWriter out = response.getWriter();
			out.println(theXml);
		}

	protected void processOutcome(HttpServletRequest request, HttpServletResponse response, 
			String lti_message_type, 
			Site site, String siteId, String placement_id, Properties pitch,
			String user_id,  Map<String, Object> theMap)
		throws java.io.IOException
		{
			// Make sure the user exists in the site
			boolean userExistsInSite = false;
			try {
				Member member = site.getMember(user_id);
				if(member != null ) userExistsInSite = true;
			} catch (Exception e) {
				M_log.warn(e.getLocalizedMessage() + " siteId="+siteId, e);
				doError(request, response, theMap, "outcome.site.membership", "", e);
				return;
			}

			// Make sure the placement is configured to receive grades
			String assignment = pitch.getProperty("assignment");
			M_log.debug("ASSN="+assignment);
			if ( assignment == null ) {
				doError(request, response, theMap, "outcome.no.assignment", "", null);
				return;
			}

			// Look up the assignment so we can find the max points
			GradebookService g = (GradebookService)  ComponentManager
				.get("org.sakaiproject.service.gradebook.GradebookService");

			pushAdvisor();
			Assignment assignmentObject = getOrMakeAssignment(assignment, siteId, g);
			popAdvisor();

			if ( assignmentObject == null ) {
				doError(request, response, theMap, "outcome.no.assignment", "", null);
				return;
			}

			// Things look good - time to process the grade
			boolean isRead = BasicLTIUtil.equals(lti_message_type, "basic-lis-readresult");

			String result_resultscore_textstring = request.getParameter("result_resultscore_textstring");

			if(BasicLTIUtil.isBlank(result_resultscore_textstring) && ! isRead ) {
				doError(request, response, theMap, "outcomes.missing", "result_resultscore_textstring", null);
				return;
			}

			// We don't need to retrieve the assignments and check if it 
			// is a valid column because if the column is wrong, we 
			// will get an exception below

			// Lets store or retrieve the grade using the securityadvisor
			Session sess = SessionManager.getCurrentSession();
			String theGrade = null;
			pushAdvisor();
			boolean success = false;

			try {
				// Indicate "who" is setting this grade - needs to be a real user account
				String gb_user_id = ServerConfigurationService.getString(
						"basiclti.outcomes.userid", "admin");
				String gb_user_eid = ServerConfigurationService.getString(
						"basiclti.outcomes.usereid", gb_user_id);
				sess.setUserId(gb_user_id);
				sess.setUserEid(gb_user_eid);
				Double dGrade;
				if ( isRead ) {
					theGrade = g.getAssignmentScoreString(siteId, assignment, user_id);
					dGrade = new Double(theGrade);
					dGrade = dGrade / assignmentObject.getPoints();
					theMap.put("/message_response/result/resultscore/textstring", dGrade.toString());
				} else { 
					dGrade = new Double(result_resultscore_textstring);
					dGrade = dGrade * assignmentObject.getPoints();
					g.setAssignmentScore(siteId, assignment, user_id, dGrade, "External Outcome");
					M_log.info("Stored Score=" + siteId + " assignment="+ assignment + " user_id=" + user_id + 
						" score="+ result_resultscore_textstring);
				}
				success = true;
				theMap.put("/message_response/statusinfo/codemajor", "Success");
				theMap.put("/message_response/statusinfo/severity", "Status");
				theMap.put("/message_response/statusinfo/codeminor", "fullsuccess");
			} catch (Exception e) {
				doError(request, response, theMap, "outcome.grade.fail", "siteId="+siteId, e);
			} finally {
				sess.invalidate(); // Make sure to leave no traces
				popAdvisor();
			}

			if ( ! success ) return;

			String theXml = XMLMap.getXML(theMap, true);
			PrintWriter out = response.getWriter();
			out.println(theXml);
		}

	protected void processRoster(HttpServletRequest request, HttpServletResponse response, 
			String lti_message_type, 
			Site site, String siteId, String placement_id, Properties pitch,
			String user_id,  Map<String, Object> theMap)
		throws java.io.IOException
		{
			// Check for permission in placement
			String allowRoster = pitch.getProperty(LTIService.LTI_ALLOWROSTER);
			if ( ! "on".equals(allowRoster) ) {
				doError(request, response, theMap, "outcomes.invalid", "lti_message_type="+lti_message_type, null);
				return;
			}

			String releaseName = pitch.getProperty(LTIService.LTI_SENDNAME);
			String releaseEmail = pitch.getProperty(LTIService.LTI_SENDEMAILADDR);
			String assignment = pitch.getProperty("assignment");
			String allowOutcomes = ServerConfigurationService.getString(
					SakaiBLTIUtil.BASICLTI_OUTCOMES_ENABLED, null);
			if ( ! "true".equals(allowOutcomes) ) allowOutcomes = null;

			String maintainRole = site.getMaintainRole();

			pushAdvisor();
			boolean success = false;
			try { 
				List<Map<String,String>> lm = new ArrayList<Map<String,String>>();
				Set<Member> members = site.getMembers();
				for (Member member : members ) {
					Map<String,String> mm = new TreeMap<String,String>();
					Role role = member.getRole();
					String ims_user_id = member.getUserId();
					mm.put("/user_id",ims_user_id);
					String ims_role = "Learner";
					if ( maintainRole != null && maintainRole.equals(role.getId())) ims_role = "Instructor";
					mm.put("/role",ims_role);
					User user = null;
					if ( "true".equals(allowOutcomes) && assignment != null ) {
						user = UserDirectoryService.getUser(ims_user_id);
						String placement_secret  = pitch.getProperty(LTIService.LTI_PLACEMENTSECRET);
						String result_sourcedid = SakaiBLTIUtil.getSourceDID(user, placement_id, placement_secret);
						if ( result_sourcedid != null ) mm.put("/lis_result_sourcedid",result_sourcedid);
					}

					if ( "on".equals(releaseName) || "on".equals(releaseEmail) ) {
						if ( user == null ) user = UserDirectoryService.getUser(ims_user_id);
						if ( "on".equals(releaseName) ) {
							mm.put("/person_name_given",user.getFirstName());
							mm.put("/person_name_family",user.getLastName());
							mm.put("/person_name_full",user.getDisplayName());
						}
						if ( "on".equals(releaseEmail) ) {
							mm.put("/person_contact_email_primary",user.getEmail());
							mm.put("/person_sourcedid",user.getEid());
						}
					}
					lm.add(mm);
				}
				theMap.put("/message_response/members/member", lm);
				success = true;
			} catch (Exception e) {
				doError(request, response, theMap, "memberships.fail", "", e);
			} finally {
				popAdvisor();
			}

			if ( ! success ) return;

			theMap.put("/message_response/statusinfo/codemajor", "Success");
			theMap.put("/message_response/statusinfo/severity", "Status");
			theMap.put("/message_response/statusinfo/codeminor", "fullsuccess");
			String theXml = XMLMap.getXML(theMap, true);
			PrintWriter out = response.getWriter();
			out.println(theXml);
		}

	/* IMS POX XML versions of this service */
	public void doErrorXml(HttpServletRequest request,HttpServletResponse response, 
			IMSPOXRequest pox, String s, String message, Exception e) 
		throws java.io.IOException 
		{
			if (e != null) {
				M_log.error(e.getLocalizedMessage(), e);
			}
			String msg = rb.getString(s) + ": " + message;
			M_log.info(msg);
			response.setContentType("application/xml");
			PrintWriter out = response.getWriter();
            String output = null;
            if ( pox == null ) {
                output = IMSPOXRequest.getFatalResponse(msg);
            } else {
                output = pox.getResponseFailure(msg, null);
            }
			out.println(output);
		}

	@SuppressWarnings("unchecked")
		protected void doPostXml(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

			String ipAddress = request.getRemoteAddr();

			M_log.debug("LTI POX Service request from IP=" + ipAddress);

			String allowOutcomes = ServerConfigurationService.getString(
					SakaiBLTIUtil.BASICLTI_OUTCOMES_ENABLED, null);
			if ( ! "true".equals(allowOutcomes) ) allowOutcomes = null;

			if (allowOutcomes == null ) {
				M_log.warn("Basic LTI Services are disabled IP=" + ipAddress);
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			IMSPOXRequest pox = new IMSPOXRequest(request);
			if ( ! pox.valid ) {
				doErrorXml(request, response, pox, "pox.invalid", pox.errorMessage, null);
				return;
			}

			//check lti_message_type
			String lti_message_type = pox.getOperation();

			String sourcedid = null;
			String message_type = null;
			if ( ( "replaceResultRequest".equals(lti_message_type) || "readResultRequest".equals(lti_message_type) ||
                   "deleteResultRequest".equals(lti_message_type) )  && allowOutcomes != null ) {
				Map<String,String> bodyMap = pox.getBodyMap();
				sourcedid = bodyMap.get("/resultRecord/sourcedGUID/sourcedId");
				// System.out.println("sourcedid="+sourcedid);
				message_type = "basicoutcome";
			} else {
				String output = pox.getResponseUnsupported("Not supported "+lti_message_type);
				response.setContentType("application/xml");
				PrintWriter out = response.getWriter();
				out.println(output);
				return;
			}

			// No point continuing without a sourcedid
			if(BasicLTIUtil.isBlank(sourcedid)) {
				doErrorXml(request, response, pox, "outcomes.missing", "sourcedid", null);
				return;
			}

			// Truncate this to the maximum length to insure no cruft at the end
			if ( sourcedid.length() > 2048) sourcedid = sourcedid.substring(0,2048);

			// Attempt to parse the sourcedid, any failure is fatal
			String placement_id = null;
			String signature = null;
			String user_id = null;
			try {
				int pos = sourcedid.indexOf(":::");
				if ( pos > 0 ) {
					signature = sourcedid.substring(0, pos);
					String dec2 = sourcedid.substring(pos+3);
					pos = dec2.indexOf(":::");
					user_id = dec2.substring(0,pos);
					placement_id = dec2.substring(pos+3);
				}
			} catch (Exception e) {
				// Log some detail for ourselves
				M_log.warn("Unable to decrypt result_sourcedid IP=" + ipAddress + " Error=" + e.getMessage(),e);
				signature = null;
				placement_id = null;
				user_id = null;
			}

			// Send a more generic message back to the caller
			if ( placement_id == null || user_id == null ) {
				doErrorXml(request, response, pox, "outcomes.sourcedid", "sourcedid", null);
				return;
			}

			M_log.debug("signature="+signature);
			M_log.debug("user_id="+user_id);
			M_log.debug("placement_id="+placement_id);

			Properties pitch = getPropertiesFromPlacement(placement_id);
			if ( pitch == null ) {
				M_log.debug("Error retrieving result_sourcedid information");
				doError(request, response, null, "outcomes.sourcedid", "sourcedid", null);
				return;
			}
	
			String siteId = pitch.getProperty(LTIService.LTI_SITE_ID);
			Site site = null;
			try { 
				site = SiteService.getSite(siteId);
			} catch (Exception e) {
				M_log.debug("Error retrieving result_sourcedid site: "+e.getLocalizedMessage(), e);
			}

			// Send a more generic message back to the caller
			if (  site == null ) {
				doError(request, response, null, "outcomes.sourcedid", "sourcedid", null);
				return;
			}

			// Check the message signature using OAuth
			String oauth_consumer_key = pox.getOAuthConsumerKey();
			String oauth_secret = pitch.getProperty(LTIService.LTI_SECRET);

			pox.validateRequest(oauth_consumer_key, oauth_secret, request);
			if ( ! pox.valid ) {
				if (pox.base_string != null) {
					M_log.warn(pox.base_string);
				}
				doErrorXml(request, response, pox, "outcome.no.validate", oauth_consumer_key, null);
				return;
			}

			// Check the signature of the sourcedid to make sure it was not altered
			String placement_secret  = pitch.getProperty(LTIService.LTI_PLACEMENTSECRET);

			// Send a generic message back to the caller
			if ( placement_secret ==null ) {
				doErrorXml(request, response, pox, "outcomes.sourcedid", "sourcedid", null);
				return;
			}

			String pre_hash = placement_secret + ":::" + user_id + ":::" + placement_id;
			String received_signature = ShaUtil.sha256Hash(pre_hash);
			M_log.debug("Received signature="+signature+" received="+received_signature);
			boolean matched = signature.equals(received_signature);

			String old_placement_secret  = pitch.getProperty(LTIService.LTI_OLDPLACEMENTSECRET);
			if ( old_placement_secret != null && ! matched ) {
				pre_hash = placement_secret + ":::" + user_id + ":::" + placement_id;
				received_signature = ShaUtil.sha256Hash(pre_hash);
				M_log.debug("Received signature II="+signature+" received="+received_signature);
				matched = signature.equals(received_signature);
			}

			// Send a message back to the caller
			if ( ! matched ) {
				doErrorXml(request, response, pox, "outcomes.sourcedid", "sourcedid", null);
				return;
			}

			if ( "basicoutcome".equals(message_type) ) {
				processOutcomeXml(request, response, lti_message_type, site, siteId, pitch, user_id, pox);
			} else {
				response.setContentType("application/xml");
				PrintWriter writer = response.getWriter();
				String desc = "Message received and validated operation="+pox.getOperation();
				String output = pox.getResponseUnsupported(desc);
				writer.println(output);
			}

		}

	protected void processOutcomeXml(HttpServletRequest request, HttpServletResponse response, 
			String lti_message_type, 
			Site site, String siteId, Properties pitch,
			String user_id, IMSPOXRequest pox)
		throws java.io.IOException
		{
			// Make sure the user exists in the site
			boolean userExistsInSite = false;
			try {
				Member member = site.getMember(user_id);
				if(member != null ) userExistsInSite = true;
			} catch (Exception e) {
				M_log.warn(e.getLocalizedMessage() + " siteId="+siteId, e);
				doErrorXml(request, response, pox, "outcome.site.membership", "", e);
				return;
			}

			// Make sure the placement is configured to receive grades
			String assignment = pitch.getProperty("assignment");
			M_log.debug("ASSN="+assignment);
			if ( assignment == null ) {
				doErrorXml(request, response, pox, "outcome.no.assignment", "", null);
				return;
			}

			// Look up the assignment so we can find the max points
			GradebookService g = (GradebookService)  ComponentManager
				.get("org.sakaiproject.service.gradebook.GradebookService");

			pushAdvisor();
			Assignment assignmentObject = getOrMakeAssignment(assignment, siteId, g);
			popAdvisor();

			if ( assignmentObject == null ) {
				doErrorXml(request, response, pox, "outcome.no.assignment", "", null);
				return;
			}

			// Things look good - time to process the grade
			boolean isRead = BasicLTIUtil.equals(lti_message_type, "readResultRequest");
			boolean isDelete = BasicLTIUtil.equals(lti_message_type, "deleteResultRequest");

			Map<String,String> bodyMap = pox.getBodyMap();
			String result_resultscore_textstring = bodyMap.get("/resultRecord/result/resultScore/textString");
			String sourced_id = bodyMap.get("/resultRecord/result/sourcedId");
			// System.out.println("grade="+result_resultscore_textstring);

			if(BasicLTIUtil.isBlank(result_resultscore_textstring) && ! isRead && ! isDelete ) {
				doErrorXml(request, response, pox, "outcomes.missing", "result_resultscore_textstring", null);
				return;
			}

			// Lets return an XML Response
			Map<String,Object> theMap = new TreeMap<String,Object>();

			// We don't need to retrieve the assignments and check if it 
			// is a valid column because if the column is wrong, we 
			// will get an exception below

			// Lets store or retrieve the grade using the securityadvisor
			Session sess = SessionManager.getCurrentSession();
			String theGrade = null;
			pushAdvisor();
			boolean success = false;
			String message = null;

			try {
				// Indicate "who" is setting this grade - needs to be a real user account
				String gb_user_id = ServerConfigurationService.getString(
						"basiclti.outcomes.userid", "admin");
				String gb_user_eid = ServerConfigurationService.getString(
						"basiclti.outcomes.usereid", gb_user_id);
				sess.setUserId(gb_user_id);
				sess.setUserEid(gb_user_eid);
				Double dGrade;
				if ( isRead ) {
					theGrade = g.getAssignmentScoreString(siteId, assignment, user_id);
					String sGrade = "";
					dGrade = new Double(theGrade);
					dGrade = dGrade / assignmentObject.getPoints();
					if ( dGrade != 0.0 ) sGrade = dGrade.toString();
					theMap.put("/readResultResponse/result/sourcedId", sourced_id);
					theMap.put("/readResultResponse/result/resultScore/textString", sGrade);
					theMap.put("/readResultResponse/result/resultScore/language", "en");
					message = "Result read";
				} else if ( isDelete ) { 
					// It would be nice to empty it out but we can't
					g.setAssignmentScore(siteId, assignment, user_id, new Double(0.0), "External Outcome");
					M_log.info("Delete Score site=" + siteId + " assignment="+ assignment + " user_id=" + user_id);
					theMap.put("/deleteResultResponse", "");
					message = "Result deleted";
				} else { 
					dGrade = new Double(result_resultscore_textstring);
					if ( dGrade < 0.0 || dGrade > 1.0 ) {
						throw new Exception("Grade out of range");
					}
					dGrade = dGrade * assignmentObject.getPoints();
					g.setAssignmentScore(siteId, assignment, user_id, dGrade, "External Outcome");

					M_log.info("Stored Score=" + siteId + " assignment="+ assignment + " user_id=" + user_id + " score="+ result_resultscore_textstring);
					theMap.put("/replaceResultResponse", "");
					message = "Result replaced";
				}
				success = true;
			} catch (Exception e) {
				doErrorXml(request, response, pox, "outcome.grade.fail", e.getMessage()+" siteId="+siteId, e);
			} finally {
				sess.invalidate(); // Make sure to leave no traces
				popAdvisor();
			}

			if ( !success ) return;

			String output = null;
			String theXml = "";
			if ( theMap.size() > 0 ) theXml = XMLMap.getXMLFragment(theMap, true);
			output = pox.getResponseSuccess(message, theXml);

			response.setContentType("application/xml");
			PrintWriter out = response.getWriter();
			out.println(output);
		}

		public Assignment getOrMakeAssignment(String assignment, String siteId, GradebookService g )
		{
			Assignment assignmentObject = null;

			try {
				List gradebookAssignments = g.getAssignments(siteId);
				for (Iterator i=gradebookAssignments.iterator(); i.hasNext();) {
					Assignment gAssignment = (Assignment) i.next();
					if ( gAssignment.isExternallyMaintained() ) continue;
					if ( assignment.equals(gAssignment.getName()) ) { 
						assignmentObject = gAssignment;
						break;
					}
				}
			} catch (Exception e) {
				assignmentObject = null; // Just to make double sure
			}

			// Attempt to add assignment to grade book
			if ( assignmentObject == null && g.isGradebookDefined(siteId) ) {
				try {
					assignmentObject = new Assignment();
					assignmentObject.setPoints(Double.valueOf(100));
					assignmentObject.setExternallyMaintained(false);
					assignmentObject.setName(assignment);
					assignmentObject.setReleased(true);
					assignmentObject.setUngraded(false);
					g.addAssignment(siteId, assignmentObject);
					M_log.info("Added assignment: "+assignment);
				}
				catch (ConflictingAssignmentNameException e) {
					M_log.warn("ConflictingAssignmentNameException while adding assignment" + e.getMessage());
					assignmentObject = null; // Just to make double sure
				}
				catch (Exception e) {
					M_log.warn("GradebookNotFoundException (may be because GradeBook has not yet been added to the Site) " + e.getMessage());
					M_log.warn(this + ":addGradeItem " + e.getMessage());
				}
			}
			return assignmentObject;
		}

	// Extract the necessary properties from a placement
	protected Properties getPropertiesFromPlacement(String placement_id)
	{
		// These are the fields from a placement - they are not an exact match
		// for the fields in tool/content
		String [] fieldList = { "key", LTIService.LTI_SECRET, LTIService.LTI_PLACEMENTSECRET, 
				LTIService.LTI_OLDPLACEMENTSECRET, LTIService.LTI_ALLOWSETTINGS, 
				"assignment", LTIService.LTI_ALLOWROSTER, "releasename", "releaseemail", 
				"toolsetting"};

		Properties retval = new Properties();

		String siteId = null;
		if ( isPlacement(placement_id) ) {
			ToolConfiguration placement = null;
			Properties config = null;
			try {
				placement = SiteService.findTool(placement_id);
				config = placement.getConfig();
				siteId = placement.getSiteId();
			} catch (Exception e) {
				M_log.debug("Error getPropertiesFromPlacement: "+e.getLocalizedMessage(), e);
				return null;
			}
			retval.setProperty("placementId",placement_id);
			retval.setProperty(LTIService.LTI_SITE_ID,siteId);
			for ( String field : fieldList ) {
				String value = SakaiBLTIUtil.toNull(SakaiBLTIUtil.getCorrectProperty(config,field, placement));
				if ( field.equals("toolsetting") ) {
                    value = config.getProperty("toolsetting", null);
					field = LTIService.LTI_SETTINGS;
				}
				if ( value == null ) continue;
				if ( field.equals("releasename") ) field = LTIService.LTI_SENDNAME;
				if ( field.equals("releaseemail") ) field = LTIService.LTI_SENDEMAILADDR;
				if ( field.equals("key") ) field = LTIService.LTI_CONSUMERKEY;
				retval.setProperty(field, value);
			}
		} else { // Get information from content item
			Map<String,Object> content = null;
			Map<String,Object> tool = null;

			String contentStr = placement_id.substring(8);
			Long contentKey = foorm.getLongKey(contentStr);
			if ( contentKey < 0 ) return null;
			content = ltiService.getContentDao(contentKey);
			if ( content == null ) return null;
			siteId = (String) content.get(LTIService.LTI_SITE_ID);
			if ( siteId == null ) return null;

			retval.setProperty("contentKey",contentStr);
			retval.setProperty(LTIService.LTI_SITE_ID,siteId);

			Long toolKey = foorm.getLongKey(content.get(LTIService.LTI_TOOL_ID));
			if ( toolKey < 0 ) return null;
			tool = ltiService.getToolDao(toolKey, siteId);
			if ( tool == null ) return null;

			// Adjust the content items based on the tool items
			if ( tool != null || content != null )
			{
				ltiService.filterContent(content, tool);
			}

			for (String formInput : LTIService.TOOL_MODEL) {
				Properties info = foorm.parseFormString(formInput);
				String field = info.getProperty("field", null);
				String type = info.getProperty("type", null);
				Object o = tool.get(field);
				if ( o instanceof String ) {
					retval.setProperty(field,(String) o);
					continue;
				}
				if ( "checkbox".equals(type) ) {
					int check = getInt(o);
					if ( check == 1 ) {	
						retval.setProperty(field,"on");
					} else {
						retval.setProperty(field,"off");
					}
				}
			}

			for (String formInput : LTIService.CONTENT_MODEL) {
				Properties info = foorm.parseFormString(formInput);
				String field = info.getProperty("field", null);
				String type = info.getProperty("type", null);
				Object o = content.get(field);
				if ( o instanceof String ) {
					retval.setProperty(field,(String) o);
					continue;
				}
				if ( "checkbox".equals(type) ) {
					int check = getInt(o);
					if ( check == 1 ) {	
						retval.setProperty(field,"on");
					} else {
						retval.setProperty(field,"off");
					}
				}
			}
			retval.setProperty("assignment",(String)content.get("title"));
		}
		return retval;
	}

	boolean isPlacement(String placement_id) {
		if ( placement_id == null ) return false;
		return ! (placement_id.startsWith("content:") && placement_id.length() > 8) ;
	}

	// Convienence
    public static int getInt(Object o)
    {
		return FoormUtil.getInt(o);
    }

	public void destroy() {

	}

}
