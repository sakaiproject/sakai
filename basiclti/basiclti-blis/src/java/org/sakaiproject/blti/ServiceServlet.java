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
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Properties;
import java.util.Set;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.server.OAuthServlet;
import net.oauth.signature.OAuthSignatureMethod;

import org.tsugi.basiclti.BasicLTIConstants;
import org.tsugi.basiclti.BasicLTIUtil;
import org.tsugi.basiclti.XMLMap;
import org.tsugi.pox.IMSPOXRequest;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.basiclti.util.LegacyShaUtil;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.user.api.User;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;


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
@Slf4j
public class ServiceServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static ResourceLoader rb = new ResourceLoader("blis");

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

	public void doError(HttpServletRequest request,HttpServletResponse response, 
			Map<String, Object> theMap, String s, String message, Exception e) 
	throws java.io.IOException 
	{
		if (e != null) {
			log.error(e.getLocalizedMessage(), e);
		}
		theMap.put("/message_response/statusinfo/codemajor", "Fail");
		theMap.put("/message_response/statusinfo/severity", "Error");
		String msg = rb.getString(s) + ": " + message;
		log.info(msg);
		theMap.put("/message_response/statusinfo/description", FormattedText.escapeHtmlFormattedText(msg));
		String theXml = XMLMap.getXML(theMap, true);
		PrintWriter out = response.getWriter();
		out.println(theXml);
		log.info("doError={}", theXml);
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
		if ( lti_errorlog != null ) log.error(lti_errorlog);
		String lti_errormsg = request.getParameter("lti_errormsg");
		if ( lti_errormsg != null ) log.error(lti_errormsg);
		String lti_log = request.getParameter("lti_log");
		if ( lti_log != null ) log.info(lti_log);
		String lti_msg = request.getParameter("lti_msg");
		if ( lti_msg != null ) log.info(lti_msg);
		
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

		log.debug("Basic LTI Service Form request from IP={}", ipAddress);

		String allowOutcomes = ServerConfigurationService.getString(
				SakaiBLTIUtil.BASICLTI_OUTCOMES_ENABLED, SakaiBLTIUtil.BASICLTI_OUTCOMES_ENABLED_DEFAULT);
		if ( ! "true".equals(allowOutcomes) ) allowOutcomes = null;

		String allowSettings = ServerConfigurationService.getString(
				SakaiBLTIUtil.BASICLTI_SETTINGS_ENABLED, SakaiBLTIUtil.BASICLTI_SETTINGS_ENABLED_DEFAULT);
		if ( ! "true".equals(allowSettings) ) allowSettings = null;

		String allowRoster = ServerConfigurationService.getString(
				SakaiBLTIUtil.BASICLTI_ROSTER_ENABLED, SakaiBLTIUtil.BASICLTI_ROSTER_ENABLED_DEFAULT);
		if ( ! "true".equals(allowRoster) ) allowRoster = null;

		if (allowOutcomes == null && allowSettings == null && allowRoster == null ) {
			log.warn("LTI Services are disabled IP={}", ipAddress);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		// Lets return an XML Response
		Map<String,Object> theMap = new TreeMap<String,Object>();

		Map<String,String[]> params = (Map<String,String[]>)request.getParameterMap();
		for (Map.Entry<String,String[]> param : params.entrySet()) {
			log.debug("{}:{}", param.getKey(), param.getValue()[0]);
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

		// This is for the pre-LTI 1.x "Sakai Basic Outcomes" and is probably never used
		// Perform the Outcome here because we use SakaiBLTIUtil.handleGradebook()
		if ( "basicoutcome".equals(message_type) ) {
			processOutcome(request, response, lti_message_type, sourcedid, theMap);
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
			// no parameter for key, check header
			final String authorizationHeader = request.getHeader("authorization");
			if(authorizationHeader.contains("oauth_consumer_key") ) {
				String[] keys = authorizationHeader.split(",");
				for(String key : keys) {
					if(key.startsWith("oauth_consumer_key")) {
						int end = key.length() - 1;
						oauth_consumer_key = key.substring(20, end);
					}
				}
			}
			if(BasicLTIUtil.isBlank(oauth_consumer_key)) {
				doError(request, response, theMap, "outcomes.missing", "oauth_consumer_key", null);
				return;
			}
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
			// Logger some detail for ourselves
			log.warn("Unable to decrypt result_sourcedid IP={} Error={}", ipAddress, e.getMessage());
			signature = null;
			placement_id = null;
			user_id = null;
		}

		// Send a more generic message back to the caller
		if ( placement_id == null || user_id == null ) {
			doError(request, response, theMap, "outcomes.sourcedid", "sourcedid", null);
			return;
		}

		log.debug("signature={}", signature);
		log.debug("user_id={}", user_id);
		log.debug("placement_id={}", placement_id);

		Properties pitch = SakaiBLTIUtil.getPropertiesFromPlacement(placement_id, ltiService);
		if ( pitch == null ) {
			log.debug("Error retrieving result_sourcedid information");
			doError(request, response, theMap, "outcomes.sourcedid", "sourcedid", null);
			return;
		}

		String siteId = pitch.getProperty(LTIService.LTI_SITE_ID);
		Site site = null;
		try { 
			site = SiteService.getSite(siteId);
		} catch (Exception e) {
			log.debug("Error retrieving result_sourcedid site: {}", e.getLocalizedMessage());
		}

		// Send a more generic message back to the caller
		if (  site == null ) {
			doError(request, response, theMap, "outcomes.sourcedid", "sourcedid", null);
			return;
		}

		// Check the message signature using OAuth
		String oauth_secret = pitch.getProperty(LTIService.LTI_SECRET);
		log.debug("oauth_secret: {}", oauth_secret);
		oauth_secret = SakaiBLTIUtil.decryptSecret(oauth_secret);
		log.debug("oauth_secret (decrypted): {}", oauth_secret);

		String URL = SakaiBLTIUtil.getOurServletPath(request);
		OAuthMessage oam = OAuthServlet.getMessage(request, URL);
		OAuthValidator oav = new SimpleOAuthValidator();
		OAuthConsumer cons = new OAuthConsumer("about:blank#OAuth+CallBack+NotUsed", oauth_consumer_key,oauth_secret, null);

		OAuthAccessor acc = new OAuthAccessor(cons);

		String base_string = null;
		try {
			base_string = OAuthSignatureMethod.getBaseString(oam);
			log.debug("base_string={}",base_string);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
			base_string = null;
		}

		try {
			oav.validateMessage(oam, acc);
		} catch (Exception e) {
			log.warn("Provider failed to validate message");
			log.warn(e.getLocalizedMessage(), e);
			if (base_string != null) {
				log.warn(base_string);
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
		String received_signature = LegacyShaUtil.sha256Hash(pre_hash);
		log.debug("Received signature={} received={}", signature, received_signature);
		boolean matched = signature.equals(received_signature);

		String old_placement_secret  = pitch.getProperty(LTIService.LTI_OLDPLACEMENTSECRET);
		if ( old_placement_secret != null && ! matched ) {
			pre_hash = placement_secret + ":::" + user_id + ":::" + placement_id;
			received_signature = LegacyShaUtil.sha256Hash(pre_hash);
			log.debug("Received signature II={} received={}", signature, received_signature);
			matched = signature.equals(received_signature);
		}

		// Send a message back to the caller
		if ( ! matched ) {
			doError(request, response, theMap, "outcomes.sourcedid", "sourcedid", null);
			return;
		}

		// These are the Sakai-post form extensions
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

		SakaiBLTIUtil.pushAdvisor();
		boolean success = false;
		try { 
			if ( "basic-lti-loadsetting".equals(lti_message_type) ) {
				setting = pitch.getProperty(LTIService.LTI_SETTINGS_EXT);
				// Remove this after the DB conversion for SAK-25621 is completed
				// It is harmless until LTI 2.0 starts to get heavy use.
				if ( setting == null ) {
					setting = pitch.getProperty(LTIService.LTI_SETTINGS);
				}
				if ( setting != null ) {
					theMap.put("/message_response/setting/value", setting);
				}
				success = true;
			} else {
				if ( SakaiBLTIUtil.isPlacement(placement_id) ) {
					ToolConfiguration placement = SiteService.findTool(placement_id);
					if ( "basic-lti-savesetting".equals(lti_message_type) ) {
						setting = request.getParameter("setting");
						if ( setting == null ) {
							log.warn("No setting parameter");
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
					Long contentKey = SakaiBLTIUtil.getLongKey(contentStr);
					if ( contentKey >= 0 ) content = ltiService.getContentDao(contentKey, siteId);
					if ( content != null ) {
						if ( "basic-lti-savesetting".equals(lti_message_type) ) {
							setting = request.getParameter("setting");
							if ( setting == null ) {
								log.warn("No setting parameter");
								doError(request, response, theMap, "setting.empty", "", null);
							} else {
								if ( setting.length() > 8096) setting = setting.substring(0,8096);
								content.put(LTIService.LTI_SETTINGS_EXT,setting);
								success = true;
							}
						} else if ( "basic-lti-deletesetting".equals(lti_message_type) ) {
							content.put(LTIService.LTI_SETTINGS_EXT,null);
							success = true;
						}
						if ( success ) {
							Object result = ltiService.updateContentDao(contentKey,content, siteId);
							if ( result instanceof String ) {
								log.warn("Setting update failed: {}", result);
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
			SakaiBLTIUtil.popAdvisor();
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
			String lti_message_type, String sourcedid, Map<String, Object> theMap)
		throws java.io.IOException
	{
		// Things look good - time to process the grade
		boolean isRead = BasicLTIUtil.equals(lti_message_type, "basic-lis-readresult");
		boolean isDelete = BasicLTIUtil.equals(lti_message_type, "basic-lis-deleteresult");

		String result_resultscore_textstring = request.getParameter("result_resultscore_textstring");
		String result_resultdata_text = request.getParameter("result_resultdata_text");

		if(BasicLTIUtil.isBlank(result_resultscore_textstring) && ! isRead ) {
			doError(request, response, theMap, "outcomes.missing", "result_resultscore_textstring", null);
			return;
		}

		String theGrade = null;
		boolean success = false;
		Object retval = null;

		try {
			Double dGrade;
			if ( isRead ) {
				retval = SakaiBLTIUtil.getGrade(sourcedid, request, ltiService);
				if ( retval instanceof Map ) {
					Map grade = (Map) retval;
					dGrade = (Double) grade.get("grade");
					theMap.put("/message_response/result/resultscore/textstring", dGrade.toString());
					theMap.put("/message_response/result/resultdata/text", (String) grade.get("comment"));
				} else {
					// Read fail with Good SourceDID is treated as empty
					Object check = SakaiBLTIUtil.checkSourceDid(sourcedid, request, ltiService);
					if ( check instanceof Boolean && ((Boolean) check) ) {
						theMap.put("/message_response/result/resultscore/textstring", "");
						theMap.put("/message_response/result/resultdata/text", "");
					} else {
						doError(request, response, theMap, "outcome.fail", (String) retval, null);
						return;
					}
				}
		    } else if ( isDelete ) {
				retval = SakaiBLTIUtil.deleteGrade(sourcedid, request, ltiService);
			} else { 
				dGrade = new Double(result_resultscore_textstring);
				retval = SakaiBLTIUtil.setGrade(sourcedid, request, ltiService, dGrade, result_resultdata_text);
			}
			success = true;
			theMap.put("/message_response/statusinfo/codemajor", "Success");
			theMap.put("/message_response/statusinfo/severity", "Status");
			theMap.put("/message_response/statusinfo/codeminor", "fullsuccess");
		} catch (Exception e) {
			doError(request, response, theMap, "outcome.grade.fail", "", e);
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

		String roleMapProp = pitch.getProperty("rolemap");
		String releaseName = pitch.getProperty(LTIService.LTI_SENDNAME);
		String releaseEmail = pitch.getProperty(LTIService.LTI_SENDEMAILADDR);
		String assignment = pitch.getProperty("assignment");
		String allowOutcomes = ServerConfigurationService.getString(
				SakaiBLTIUtil.BASICLTI_OUTCOMES_ENABLED, SakaiBLTIUtil.BASICLTI_OUTCOMES_ENABLED_DEFAULT);
		if ( ! "true".equals(allowOutcomes) ) allowOutcomes = null;

		String maintainRole = site.getMaintainRole();

		SakaiBLTIUtil.pushAdvisor();
		boolean success = false;
		try { 
			List<Map<String,Object>> lm = new ArrayList<Map<String,Object>>();
			Map<String, String> roleMap = SakaiBLTIUtil.convertRoleMapPropToMap(roleMapProp);

			// Get users for each of the members. UserDirectoryService.getUsers will skip any undefined users.
			Set<Member> members = site.getMembers();
			Map<String, Member> memberMap = new HashMap<String, Member>();
			List<String> userIds = new ArrayList<String>();
			for (Member member : members) {
				userIds.add(member.getUserId());
				memberMap.put(member.getUserId(), member);
			}
			List<User> users = UserDirectoryService.getUsers(userIds);

			for (User user : users ) {
				Member member = memberMap.get(user.getId());
				Map<String,Object> mm = new TreeMap<String,Object>();
				Role role = member.getRole();
				String ims_user_id = member.getUserId();
				mm.put("/user_id",ims_user_id);
				String ims_role = "Learner";

				// If there is a role mapping, it has precedence over site.update
				if ( roleMap.containsKey(role.getId()) ) {
					ims_role = roleMap.get(role.getId());
				} 
				else if (ComponentManager.get(AuthzGroupService.class).isAllowed(ims_user_id, SiteService.SECURE_UPDATE_SITE, "/site/" + siteId))
				{
					ims_role = "Instructor";
				}

				// Using "/role" is inconsistent with to
				// http://developers.imsglobal.org/ext_membership.html. It
				// should be roles. If we can determine that nobody is using
				// the role tag, we should remove it.

				mm.put("/role",ims_role);
				mm.put("/roles",ims_role);
				if ( "true".equals(allowOutcomes) && assignment != null ) {
					String placement_secret  = pitch.getProperty(LTIService.LTI_PLACEMENTSECRET);
					String result_sourcedid = SakaiBLTIUtil.getSourceDID(user, placement_id, placement_secret);
					if ( result_sourcedid != null ) mm.put("/lis_result_sourcedid",result_sourcedid);
				}

				if ( "on".equals(releaseName) || "on".equals(releaseEmail) ) {
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

				Collection groups = site.getGroupsWithMember(ims_user_id);

				if (groups.size() > 0) {
					List<Map<String, Object>> lgm = new ArrayList<Map<String, Object>>();
					for (Iterator i = groups.iterator();i.hasNext();) {
						Group group = (Group) i.next();
						Map<String, Object> groupMap = new HashMap<String, Object>();
						groupMap.put("/id", group.getId());
						groupMap.put("/title", group.getTitle());
						groupMap.put("/set", new HashMap(groupMap));
						lgm.add(groupMap);
					}
					mm.put("/groups/group", lgm);
				}

				lm.add(mm);
			}
			theMap.put("/message_response/members/member", lm);
			success = true;
		} catch (Exception e) {
			doError(request, response, theMap, "memberships.fail", "", e);
		} finally {
			SakaiBLTIUtil.popAdvisor();
		}

		if ( ! success ) return;

		theMap.put("/message_response/statusinfo/codemajor", "Success");
		theMap.put("/message_response/statusinfo/severity", "Status");
		theMap.put("/message_response/statusinfo/codeminor", "fullsuccess");
		String theXml = XMLMap.getXML(theMap, true);
		PrintWriter out = response.getWriter();
		out.println(theXml);
		log.debug(theXml);
	}

	/* IMS POX XML versions of this service */
	public void doErrorXML(HttpServletRequest request,HttpServletResponse response, 
			IMSPOXRequest pox, String s, String message, Exception e) 
		throws java.io.IOException 
	{
		if (e != null) {
			log.error(e.getLocalizedMessage(), e);
		}
		String msg = rb.getString(s) + ": " + message;
		log.info(msg);
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
		log.debug(output);
	}


	@SuppressWarnings("unchecked")
	protected void doPostJSON(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException 
	{
		String ipAddress = request.getRemoteAddr();

		log.warn("LTI JSON Services not implemented IP={}", ipAddress);
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		return;
	}

	@SuppressWarnings("unchecked")
	protected void doPostXml(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String ipAddress = request.getRemoteAddr();

		log.debug("LTI POX Service request from IP={}", ipAddress);

		String allowOutcomes = ServerConfigurationService.getString(
				SakaiBLTIUtil.BASICLTI_OUTCOMES_ENABLED, SakaiBLTIUtil.BASICLTI_OUTCOMES_ENABLED_DEFAULT);
		if ( ! "true".equals(allowOutcomes) ) allowOutcomes = null;

		if (allowOutcomes == null ) {
			log.warn("LTI Services are disabled IP={}", ipAddress);
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
		if ( log.isDebugEnabled() ) log.debug("POST\n{}", XMLMap.prettyPrint(pox.postBody));
		Map<String,String> bodyMap = pox.getBodyMap();
		if ( ( "replaceResultRequest".equals(lti_message_type) || "readResultRequest".equals(lti_message_type) ||
			  "deleteResultRequest".equals(lti_message_type) )  && allowOutcomes != null ) {
			sourcedid = bodyMap.get("/resultRecord/sourcedGUID/sourcedId");
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
			doErrorXML(request, response, pox, "outcomes.missing", "sourcedid", null);
			return;
		}

		// Handle the LTI 1.x Outcomes
		// Perform the Outcome here because we use SakaiBLTIUtil.handleGradebook()
		if ( allowOutcomes != null && "basicoutcome".equals(message_type) ) {
			processOutcomeXml(request, response, lti_message_type, sourcedid, pox);
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
			// Logger some detail for ourselves
			log.warn("Unable to decrypt result_sourcedid IP={} Error={}, {}", ipAddress, e.getMessage(), e);
			signature = null;
			placement_id = null;
			user_id = null;
		}

		// Send a more generic message back to the caller
		if ( placement_id == null || user_id == null ) {
			doErrorXML(request, response, pox, "outcomes.sourcedid", "missing user_id or placement_id", null);
			return;
		}

		log.debug("signature={}", signature);
		log.debug("user_id={}", user_id);
		log.debug("placement_id={}", placement_id);

		Properties pitch = SakaiBLTIUtil.getPropertiesFromPlacement(placement_id, ltiService);
		if ( pitch == null ) {
			log.debug("Error retrieving result_sourcedid information");
			doErrorXML(request, response, pox, "outcomes.sourcedid", "sourcedid", null);
			return;
		}

		String siteId = pitch.getProperty(LTIService.LTI_SITE_ID);
		Site site = null;
		try { 
			site = SiteService.getSite(siteId);
		} catch (Exception e) {
			log.debug("Error retrieving result_sourcedid site: {}, error: {}", e.getLocalizedMessage(), e);
		}

		// Send a more generic message back to the caller
		if (  site == null ) {
			doErrorXML(request, response, pox, "outcomes.sourcedid", "sourcedid", null);
			return;
		}

		// Check the message signature using OAuth
		String oauth_consumer_key = pox.getOAuthConsumerKey();
		String oauth_secret = pitch.getProperty(LTIService.LTI_SECRET);
		log.debug("oauth_secret: {}", oauth_secret);
		oauth_secret = SakaiBLTIUtil.decryptSecret(oauth_secret);
		log.debug("oauth_secret (decrypted): {}", oauth_secret);

		String URL = SakaiBLTIUtil.getOurServletPath(request);
		pox.validateRequest(oauth_consumer_key, oauth_secret, request, URL);
		if ( ! pox.valid ) {
			if (pox.base_string != null) {
				log.warn(pox.base_string);
			}
			doErrorXML(request, response, pox, "outcome.no.validate", oauth_consumer_key, null);
			return;
		}

		// Check the signature of the sourcedid to make sure it was not altered
		String placement_secret  = pitch.getProperty(LTIService.LTI_PLACEMENTSECRET);

		// Send a generic message back to the caller
		if ( placement_secret ==null ) {
			log.debug("placement_secret is null");
			doErrorXML(request, response, pox, "outcomes.sourcedid", "sourcedid", null);
			return;
		}

		String pre_hash = placement_secret + ":::" + user_id + ":::" + placement_id;
		String received_signature = LegacyShaUtil.sha256Hash(pre_hash);
		log.debug("Received signature={} received={}", signature, received_signature);
		boolean matched = signature.equals(received_signature);

		String old_placement_secret  = pitch.getProperty(LTIService.LTI_OLDPLACEMENTSECRET);
		if ( old_placement_secret != null && ! matched ) {
			pre_hash = placement_secret + ":::" + user_id + ":::" + placement_id;
			received_signature = LegacyShaUtil.sha256Hash(pre_hash);
			log.debug("Received signature II={} received={}", signature, received_signature);
			matched = signature.equals(received_signature);
		}

		// Send a message back to the caller
		if ( ! matched ) {
			doErrorXML(request, response, pox, "outcomes.sourcedid", "sourcedid", null);
			return;
		}

		response.setContentType("application/xml");
		PrintWriter writer = response.getWriter();
		String desc = "Message received and validated operation="+pox.getOperation();
		String output = pox.getResponseUnsupported(desc);
		writer.println(output);
	}

	protected void processOutcomeXml(HttpServletRequest request, HttpServletResponse response, 
			String lti_message_type, String sourcedid, IMSPOXRequest pox)
		throws java.io.IOException
	{
		// Things look good - time to process the grade
		boolean isRead = BasicLTIUtil.equals(lti_message_type, "readResultRequest");
		boolean isDelete = BasicLTIUtil.equals(lti_message_type, "deleteResultRequest");

		Map<String,String> bodyMap = pox.getBodyMap();
		String result_resultscore_textstring = bodyMap.get("/resultRecord/result/resultScore/textString");
		String result_resultdata_text = bodyMap.get("/resultRecord/result/resultData/text");
		String sourced_id = bodyMap.get("/resultRecord/result/sourcedId");
		log.debug("comment={}", result_resultdata_text);
		log.debug("grade={}", result_resultscore_textstring);

		if(BasicLTIUtil.isBlank(result_resultscore_textstring) && ! isRead && ! isDelete ) {
			doErrorXML(request, response, pox, "outcomes.missing", "result_resultscore_textstring", null);
			return;
		}

		// Lets return an XML Response
		Map<String,Object> theMap = new TreeMap<String,Object>();
		String theGrade = null;
		boolean success = false;
		String message = null;
		Object retval = null;
		boolean strict = ServerConfigurationService.getBoolean(SakaiBLTIUtil.LTI_STRICT, false);

		try {
			Double dGrade;
			if ( isRead ) {
				retval = SakaiBLTIUtil.getGrade(sourcedid, request, ltiService);
				String sGrade = "";
				String comment = "";
				if ( retval instanceof Map ) {
					Map grade = (Map) retval;
					comment = (String) grade.get("comment");
					dGrade = (Double) grade.get("grade");
					if ( dGrade != null ) {
						sGrade = dGrade.toString();
					}
				} else {
					Object check = SakaiBLTIUtil.checkSourceDid(sourcedid, request, ltiService);
					if ( check instanceof Boolean && ((Boolean) check) ) {
						// Read fail with Good SourceDID is treated as empty
					} else {
						doErrorXML(request, response, pox, "outcomes.fail", (String) retval, null);
						return;
					}

				}

				theMap.put("/readResultResponse/result/sourcedId", sourced_id);
				theMap.put("/readResultResponse/result/resultScore/textString", sGrade);
				theMap.put("/readResultResponse/result/resultScore/language", "en");
				if ( ! strict ) {
					theMap.put("/readResultResponse/result/resultData/text", comment);
				}
				message = "Result read";
			} else if ( isDelete ) { 
				retval = SakaiBLTIUtil.deleteGrade(sourcedid, request, ltiService);
				if ( retval instanceof String ) {
					doErrorXML(request, response, pox, "outcomes.fail", (String) retval, null);
					return;
				}
				theMap.put("/deleteResultResponse", "");
				message = "Result deleted";
			} else { 
				dGrade = new Double(result_resultscore_textstring);
				if ( dGrade < 0.0 || dGrade > 1.0 ) {
					throw new Exception("Grade out of range");
				}
				dGrade = new Double(result_resultscore_textstring);
				retval = SakaiBLTIUtil.setGrade(sourcedid, request, ltiService, dGrade, result_resultdata_text);
				if ( retval instanceof String ) {
					doErrorXML(request, response, pox, "outcomes.fail", (String) retval, null);
					return;
				}
				theMap.put("/replaceResultResponse", "");
				message = "Result replaced";
			}

			success = true;
		} catch (Exception e) {
			doErrorXML(request, response, pox, "outcome.grade.fail", e.getMessage(), e);
		}

		if ( !success ) return;

		String output = null;
		String theXml = "";
		if ( theMap.size() > 0 ) theXml = XMLMap.getXMLFragment(theMap, true);
		output = pox.getResponseSuccess(message, theXml);

		response.setContentType("application/xml");
		PrintWriter out = response.getWriter();
		out.println(output);
		log.debug(output);
	}


	public void destroy() {

	}

}
