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

import java.lang.StringBuffer;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Set;
import java.util.Iterator;
import java.util.UUID;

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

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;

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
import org.sakaiproject.exception.IdUnusedException;
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

import org.sakaiproject.lessonbuildertool.SimplePageItem;

import org.imsglobal.pox.IMSPOXRequest;

import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.util.foorm.SakaiFoorm;
import org.sakaiproject.util.foorm.FoormUtil;

import org.sakaiproject.blti.LessonsFacade;

/**
 * Notes:
 * 
 * This program is directly exposed as a URL to receive IMS Basic LTI messages
 * so it must be carefully reviewed and any changes must be looked at carefully.
 * Here are some issues:
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

	protected static XPath xpath = null;
	protected static XPathExpression LESSONS_RESOURCES_EXPR = null;
	protected static XPathExpression LESSONS_FOLDER_EXPR = null;
	protected static XPathExpression LESSONS_TYPE_EXPR = null;
	protected static XPathExpression LESSONS_TITLE_EXPR = null;
	protected static XPathExpression LESSONS_TEMPID_EXPR = null;
	protected static XPathExpression LESSONS_URL_EXPR = null;
	protected static XPathExpression LESSONS_CUSTOM_EXPR = null;

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
            LessonsFacade.init();
			if ( ltiService == null ) ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
			try {
				xpath = XPathFactory.newInstance().newXPath();
				LESSONS_RESOURCES_EXPR = xpath.compile("params/resources/*");
				LESSONS_FOLDER_EXPR = xpath.compile("resources/*");
				LESSONS_TYPE_EXPR = xpath.compile("type");
				LESSONS_TITLE_EXPR = xpath.compile("title");
				LESSONS_TEMPID_EXPR = xpath.compile("tempId");
				LESSONS_URL_EXPR = xpath.compile("launchUrl");
				LESSONS_CUSTOM_EXPR = xpath.compile("launchParams");
			} catch (Exception e) {
				M_log.error("Error compiling XPath expressions.");
				throw new ServletException();
			}
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
			if ( contentType != null && contentType.startsWith("application/json") ) {
				doPostJSON(request, response);
			} else if ( contentType != null && contentType.startsWith("application/xml") ) {
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
				M_log.warn("LTI Services are disabled IP=" + ipAddress);
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
			M_log.debug("oauth_secret: "+oauth_secret);
			oauth_secret = SakaiBLTIUtil.decryptSecret(oauth_secret);
			M_log.debug("oauth_secret (decrypted): "+oauth_secret);

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
	public void doErrorXML(HttpServletRequest request,HttpServletResponse response, 
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
		String body = null;
		String operation = pox.getOperation();
		if ( operation != null ) {
			body = "<"+operation.replace("Request", "Response")+"/>";
		}
                output = pox.getResponseFailure(msg, null, body);
            }
			out.println(output);
			M_log.debug(output);
		}


	@SuppressWarnings("unchecked")
    protected void doPostJSON(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException 
    {
        String ipAddress = request.getRemoteAddr();

        M_log.warn("LTI JSON Services not implemented IP=" + ipAddress);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return;
    }

	@SuppressWarnings("unchecked")
	protected void doPostXml(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{

			String ipAddress = request.getRemoteAddr();

			M_log.debug("LTI POX Service request from IP=" + ipAddress);

			String allowOutcomes = ServerConfigurationService.getString(
					SakaiBLTIUtil.BASICLTI_OUTCOMES_ENABLED, null);
			if ( ! "true".equals(allowOutcomes) ) allowOutcomes = null;

			String allowLori = ServerConfigurationService.getString(
					SakaiBLTIUtil.BASICLTI_LORI_ENABLED, null);
			if ( ! "true".equals(allowLori) ) allowLori = null;

			if (allowOutcomes == null && allowLori == null ) {
				M_log.warn("LTI Services are disabled IP=" + ipAddress);
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			IMSPOXRequest pox = new IMSPOXRequest(request);
			if ( ! pox.valid ) {
				doErrorXML(request, response, pox, "pox.invalid", pox.errorMessage, null);
				return;
			}

			//check lti_message_type
			String lti_message_type = pox.getOperation();

			String sourcedid = null;
			String message_type = null;
            if ( M_log.isDebugEnabled() ) M_log.debug("POST\n"+XMLMap.prettyPrint(pox.postBody));
			Map<String,String> bodyMap = pox.getBodyMap();
			if ( ( "replaceResultRequest".equals(lti_message_type) || "readResultRequest".equals(lti_message_type) ||
                   "deleteResultRequest".equals(lti_message_type) )  && allowOutcomes != null ) {
				sourcedid = bodyMap.get("/resultRecord/sourcedGUID/sourcedId");
				message_type = "basicoutcome";
			} else if ( "getCourseStructureRequest".equals(lti_message_type) ) {
                sourcedid = bodyMap.get("/params/sourcedGUID/sourcedId");
				message_type = "getstructure";
			} else if ( "addCourseResourcesRequest".equals(lti_message_type) ) {
                sourcedid = bodyMap.get("/params/sourcedGUID/sourcedId");
				message_type = "addstructure";
			} else {
				String output = pox.getResponseUnsupported("Not supported "+lti_message_type);
				response.setContentType("application/xml");
				PrintWriter out = response.getWriter();
				out.println(output);
				return;
			}

			// No point continuing without a sourcedid
			if(BasicLTIUtil.isBlank(sourcedid)) {
				doErrorXML(request, response, pox, "outcomes.missing", "sourcedid", null);
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
				doErrorXML(request, response, pox, "outcomes.sourcedid", "sourcedid", null);
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
			M_log.debug("oauth_secret: "+oauth_secret);
			oauth_secret = SakaiBLTIUtil.decryptSecret(oauth_secret);
			M_log.debug("oauth_secret (decrypted): "+oauth_secret);

			pox.validateRequest(oauth_consumer_key, oauth_secret, request);
			if ( ! pox.valid ) {
				if (pox.base_string != null) {
					M_log.warn(pox.base_string);
				}
				doErrorXML(request, response, pox, "outcome.no.validate", oauth_consumer_key, null);
				return;
			}

			// Check the signature of the sourcedid to make sure it was not altered
			String placement_secret  = pitch.getProperty(LTIService.LTI_PLACEMENTSECRET);

			// Send a generic message back to the caller
			if ( placement_secret ==null ) {
				M_log.debug("placement_secret is null");
				doErrorXML(request, response, pox, "outcomes.sourcedid", "sourcedid", null);
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
				doErrorXML(request, response, pox, "outcomes.sourcedid", "sourcedid", null);
				return;
			}

			String placementLori = pitch.getProperty("allowlori");

			if ( allowOutcomes != null && "basicoutcome".equals(message_type) ) {
				processOutcomeXml(request, response, lti_message_type, site, siteId, pitch, user_id, pox);
			} else if ( allowLori != null && "on".equals(placementLori) && "getstructure".equals(message_type) ) {
				processCourseStructureXml(request, response, lti_message_type, siteId, pox);
			} else if ( allowLori != null && "on".equals(placementLori) && "addstructure".equals(message_type) ) {
				processAddResourceXML(request, response, lti_message_type, siteId, pox);
			} else {
				response.setContentType("application/xml");
				PrintWriter writer = response.getWriter();
				String desc = "Message received and validated operation="+pox.getOperation();
				String output = pox.getResponseUnsupported(desc);
				writer.println(output);
			}
		}

	protected void processCourseStructureXml(HttpServletRequest request, HttpServletResponse response, 
			String lti_message_type, String siteId, IMSPOXRequest pox)
		throws java.io.IOException
	{
            // userId is irrelevant as this is server to server
			Map<String,String> bodyMap = pox.getBodyMap();
			String context_id = bodyMap.get("/params/courseId");
			if ( context_id == null || ! context_id.equals(siteId) ) {
				doErrorXML(request, response, pox, "outcomes.sourcedid", "sourcedid", null);
				M_log.warn("mis-match courseId="+context_id+" siteId="+siteId);
				return;
			}

			// First make sure that we have Lessons in the site
			SitePage lessonsPage = null;
			ToolConfiguration lessonsConfig = null;
			try {
				Site site = SiteService.getSite(siteId);
				for (SitePage page : (List<SitePage>)site.getPages()) {
					for(ToolConfiguration tool : (List<ToolConfiguration>) page.getTools()) {
						String tid = tool.getToolId();
						if ( "sakai.lessonbuildertool".equals(tid) ) {
							lessonsPage = page;
							lessonsConfig = tool;
							break;
						}
					}
				}
			} catch (IdUnusedException ex) {
				doErrorXML(request, response, pox, "outcomes.notools", "sourcedid", null);
				M_log.warn("Could not scan site for Lessons tool.");
				return;
			}

			if ( lessonsConfig == null ) {
				M_log.warn("Could not find sakai.lessonbulder in site="+siteId);
				doErrorXML(request, response, pox, "outcomes.nolessons", "sourcedid", null);
				return;
			}

			// Now lets find the structure within Lessons
			List<Long> structureList = new ArrayList<Long>();

			List<SimplePageItem> sitePages = LessonsFacade.findItemsInSite(context_id);
			List<Map<String,Object>> structureMap = iteratePagesXML(sitePages,structureList,0);

			if ( structureMap.size() < 1 ) {
				Map<String,Object> cMap = new TreeMap<String,Object>();
				cMap.put("/folderId","0");
				cMap.put("/title",lessonsPage.getTitle());
				cMap.put("/description",lessonsPage.getTitle());
				cMap.put("/type","folder");
				structureMap.add(cMap);
			}

			Map<String,Object> theMap = new TreeMap<String,Object>();
			theMap.put("/getCourseStructureResponse/resources/resource",structureMap);
			String theXml = XMLMap.getXMLFragment(theMap, true);
			String output = pox.getResponseSuccess("processCourseStructureXml", theXml);

			PrintWriter out = response.getWriter();
			out.println(output);
			M_log.debug(output);
			return;
	}

	protected void processAddResourceXML(HttpServletRequest request, HttpServletResponse response, 
			String lti_message_type, String siteId, IMSPOXRequest pox)
		throws java.io.IOException
	{
            // userId is irrelevant because this is server to server
			Map<String,String> bodyMap = pox.getBodyMap();
			String context_id = bodyMap.get("/params/courseId");

			if ( context_id == null || ! context_id.equals(siteId) ) {
				doErrorXML(request, response, pox, "outcomes.sourcedid", "sourcedid", null);
				M_log.warn("mis-match courseId="+context_id+" siteId="+siteId);
				return;
			}

			String folder_id = bodyMap.get("/params/folderId");
			Long folderId = null;
			try { folderId = new Long(folder_id); }
			catch (Exception e) { folderId = null; }

			List<Long> structureList = new ArrayList<Long>();
			List<SimplePageItem> sitePages = LessonsFacade.findItemsInSite(context_id);
			SimplePageItem thePage = LessonsFacade.findFolder(sitePages, folderId, structureList, 1);

			// Something wrong, add on the first page
			if ( thePage == null ) {
				M_log.debug("Inserting at top...");
				for (SimplePageItem i : sitePages) {
					if (i.getType() != SimplePageItem.PAGE) continue;
					// System.out.println("item="+i.getName()+"id="+i.getId()+" sakaiId="+i.getSakaiId());
					thePage = i;
					break;
				}
			}

			// No pages in Lessons yet... 
			// If we can find the Lessons tool, lets add its first page. 
			if ( thePage == null ) {
				M_log.debug("Creating top page...");
				SitePage lessonsPage = null;
				ToolConfiguration lessonsConfig = null;
				try {
					Site site = SiteService.getSite(siteId);
					for (SitePage page : (List<SitePage>)site.getPages()) {
						for(ToolConfiguration tool : (List<ToolConfiguration>) page.getTools()) {
							String tid = tool.getToolId();
							if ( "sakai.lessonbuildertool".equals(tid) ) {
								lessonsPage = page;
								lessonsConfig = tool;
								break;
							}
						}
					}
				} catch (IdUnusedException ex) {
					M_log.warn("Could not load site.");
				}
				if ( lessonsConfig == null ) {
					M_log.warn("Could not find sakai.lessonbulder in site="+siteId);
				} else {
					String title = lessonsPage.getTitle();
					String toolId = lessonsConfig.getPageId();
					thePage = LessonsFacade.addFirstPage(siteId, toolId, title);
				}
			}

			if ( thePage == null ) {
				doErrorXML(request, response, pox, "lessons.page.notfound", 
					"Unable to find page in structure at "+folderId, null);
				return;
			}

			Element bodyElement = pox.bodyElement;
			// System.out.println(XMLMap.nodeToString(bodyElement));
			// System.out.println(XMLMap.nodeToString(bodyElement, true));
			NodeList nl = null;
			try {
				Object result = LESSONS_RESOURCES_EXPR.evaluate(bodyElement, XPathConstants.NODESET);
				nl = (NodeList) result;
				// System.out.println("result = "+result+" count="+nl.getLength());
			} catch(Exception e) {
				e.printStackTrace();
				nl = null;
			}

			if ( nl == null || nl.getLength() < 1 ) {
				doErrorXML(request, response, pox, "lessons.page.noresources", 
					"No resources to add", null);
				return;
			}


            Long pageNum = Long.valueOf(thePage.getSakaiId());
            List<SimplePageItem> items = LessonsFacade.findItemsOnPage(pageNum);
			int seq = items.size() + 1;
            List<Map<String,String>> resultList = new ArrayList<Map<String,String>>();

			recursivelyAddResourcesXML(context_id, thePage, nl, seq, resultList);
			// One success means overall status is a success
			boolean success = false;
			for ( Map<String,String> result : resultList ) {
				if ( "success".equals(result.get("/status")) ) success = true;
			}

            Map<String,Object> theMap = new TreeMap<String,Object>();
            theMap.put("/addCourseResourcesResponse/resources/resource",resultList);
            String theXml = XMLMap.getXMLFragment(theMap, true);

			response.setContentType("application/xml");
			String output = null;
			if ( success ) {
				output = pox.getResponseSuccess("Items Added",theXml);
			} else {
				output = pox.getResponseFailure("Items were not added", null);
			}

			PrintWriter out = response.getWriter();
			out.println(output);
			M_log.debug(output);
	}

	protected void recursivelyAddResourcesXML(String siteId, SimplePageItem thePage, NodeList nl, 
        int startPos, List<Map<String,String>> resultList)
	{
		for(int i=0, cnt=nl.getLength(); i<cnt; i++)
		{
			Node node = nl.item(i);
			if ( node.getNodeType() != Node.ELEMENT_NODE ) continue;
			M_log.debug("Node="+node.getNodeName());

			if ( ! "resource".equals(node.getNodeName()) ) {
				continue;
			}

			String typeStr = null;
			try {
				typeStr = (String) LESSONS_TYPE_EXPR.evaluate(node);
			} catch (Exception e) {
				typeStr = null;
			}
			String titleStr = null;
			try {
				titleStr = (String) LESSONS_TITLE_EXPR.evaluate(node);
			} catch (Exception e) {
				titleStr = null;
			}
			String tempId = null;
			try {
				tempId = (String) LESSONS_TEMPID_EXPR.evaluate(node);
			} catch (Exception e) {
				tempId = null;
			}
			
			if ( "folder".equals(typeStr) ) {
				SimplePageItem subPageItem = LessonsFacade.addLessonsFolder(thePage, titleStr, startPos);
                if ( tempId != null ) {
                    Map<String,String> result = new TreeMap<String,String> ();
                    result.put("/tempId",tempId);
                    result.put("/id", subPageItem.getSakaiId());
                    resultList.add(result);
                }
				startPos++;
				NodeList childNodes = null;
				try {
					Object result = LESSONS_FOLDER_EXPR.evaluate(node, XPathConstants.NODESET);
					childNodes = (NodeList) result;
					M_log.debug("children of the folder = "+result+" count="+childNodes.getLength());
				} catch(Exception e) {
					e.printStackTrace();
					nl = null;
				}

				M_log.debug("===== DOWN THE RABIT HOLE ==========");
				recursivelyAddResourcesXML(siteId, subPageItem, childNodes, 1, resultList);
				continue;
			}

			if ( ! "lti".equals(typeStr) ) {
				M_log.warn("No support for type:"+typeStr);
				continue;
			}

			String launchUrl = null;
			try {
				launchUrl = (String) LESSONS_URL_EXPR.evaluate(node);
			} catch (Exception e) {
				launchUrl = null;
			}
			String launchParams = null;
			try {
				launchParams = (String) LESSONS_CUSTOM_EXPR.evaluate(node);
			} catch (Exception e) {
				launchParams = null;
			}

			if ( titleStr == null || launchUrl == null || launchParams == null ) {
				M_log.warn("Missing required value type, name, url, launch, parms");
				continue;
			}

            M_log.debug("type="+typeStr+" name="+titleStr+" launchUrl="+launchUrl+" lanchParams="+launchParams);

            Map<String,String> result = new TreeMap<String,String> ();
            result.put("/tempId",tempId);

			// Time to add the launch tool
			String sakaiId = null;
            try {
			    sakaiId = LessonsFacade.doImportTool(siteId, launchUrl, titleStr, null, launchParams);
                if ( sakaiId == null ) {
                    result.put("/status", "failure");
                    result.put("/description","doImportTool failed");
				    M_log.warn("Unable to add LTI Placement "+titleStr);
                } else {
                    result.put("/status", "success");
                    result.put("/description","doImportTool success");
                    result.put("/id", sakaiId);
                }
            } catch (Exception e) {
                sakaiId = null;
                e.printStackTrace();
                result.put("/status", "failure");
                result.put("/description", e.getMessage());
            }
            resultList.add(result);

			if ( sakaiId == null ) continue;

			LessonsFacade.addLessonsLaunch(thePage, sakaiId, titleStr, startPos);
		}
	}

	protected List<Map<String,Object>> iteratePagesXML(List<SimplePageItem> sitePages, 
		List<Long> structureList, int depth)
	{
		List<Map<String,Object>> structureMap = new ArrayList<Map<String,Object>>();

		if ( depth > 10 ) return null;
		for (SimplePageItem i : sitePages) {
			if ( structureList.size() > 50 ) return structureMap;
            // System.out.println("d="+depth+" o="+structureList.size()+" Page ="+i.getSakaiId()+" title="+i.getName());
			if (i.getType() != SimplePageItem.PAGE) continue;
			Long pageNum = Long.valueOf(i.getSakaiId());

			String title = i.getName();
			if ( structureList.size() == 50 ) title = " ... ";
			structureList.add(i.getId());

			Map<String,Object> cMap = new TreeMap<String,Object>();
			cMap.put("/folderId",i.getSakaiId());
			cMap.put("/title",title);
			cMap.put("/description",title);
			cMap.put("/type","folder");

			List<SimplePageItem> items = LessonsFacade.findItemsOnPage(pageNum);
            // System.out.println("Items="+items);
		    List<Map<String,Object>> subMap = iteratePagesXML(items, structureList, depth+1);
            if (subMap != null && subMap.size() > 0 ) {
			    cMap.put("/resources/resource",subMap);
            }
			structureMap.add(cMap);
		}
        return structureMap;
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
				doErrorXML(request, response, pox, "outcome.site.membership", "", e);
				return;
			}

			// Make sure the placement is configured to receive grades
			String assignment = pitch.getProperty("assignment");
			M_log.debug("ASSN="+assignment);
			if ( assignment == null ) {
				doErrorXML(request, response, pox, "outcome.no.assignment", "", null);
				return;
			}

			// Look up the assignment so we can find the max points
			GradebookService g = (GradebookService)  ComponentManager
				.get("org.sakaiproject.service.gradebook.GradebookService");

			pushAdvisor();
			Assignment assignmentObject = getOrMakeAssignment(assignment, siteId, g);
			popAdvisor();

			if ( assignmentObject == null ) {
				doErrorXML(request, response, pox, "outcome.no.assignment", "", null);
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
				doErrorXML(request, response, pox, "outcomes.missing", "result_resultscore_textstring", null);
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
					if ( theGrade != null && theGrade.length() > 0 ) {
						dGrade = new Double(theGrade);
						dGrade = dGrade / assignmentObject.getPoints();
						sGrade = dGrade.toString();
					}
					theMap.put("/readResultResponse/result/sourcedId", sourced_id);
					theMap.put("/readResultResponse/result/resultScore/textString", sGrade);
					theMap.put("/readResultResponse/result/resultScore/language", "en");
					message = "Result read";
				} else if ( isDelete ) { 
					// It would be nice to empty it out but we can't
					g.setAssignmentScore(siteId, assignment, user_id, null, "External Outcome");
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
				doErrorXML(request, response, pox, "outcome.grade.fail", e.getMessage()+" siteId="+siteId, e);
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
				"toolsetting", "allowlori"};

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

			// Leave off the siteId - bypass all checking - because we need to 
			// finde the siteId from the content item
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
