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
 *             http://www.osedu.org/licenses/ECL-2.0
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
import java.util.Map;
import java.util.TreeMap;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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
import org.sakaiproject.basiclti.util.ShaUtil;
import org.sakaiproject.portlet.util.FormattedText;

import org.sakaiproject.service.gradebook.shared.AssignmentHasIllegalPointsException;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingExternalIdException;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;

/**
 * Notes:
 * 
 * This program is directly exposed as a URL to receive IMS Basic LTI launches
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
public class SimpleOutcomesServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Log M_log = LogFactory.getLog(SimpleOutcomesServlet.class);
	private static ResourceLoader rb = new ResourceLoader("basiclti");
	private static final String BASICLTI_RESOURCE_LINK = "blti:resource_link_id";
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
		Map<String, String> theMap, String s, String message, Exception e) 
		throws java.io.IOException 
	{
		if (e != null) {
			M_log.error(e.getLocalizedMessage(), e);
		}
		theMap.put("/simpleoutcome/statusinfo/codemajor", "Fail");
		theMap.put("/simpleoutcome/statusinfo/severity", "Error");
		String msg = rb.getString(s) + ": " + message;
		M_log.info(msg);
		theMap.put("/simpleoutcome/statusinfo/description", FormattedText.escapeHtmlFormattedText(msg));
		String theXml = XMLMap.getXML(theMap, true);
		PrintWriter out = response.getWriter();
		out.println(theXml);
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ipAddress = request.getRemoteAddr();

		M_log.debug("Basic LTI Outcome request from IP=" + ipAddress);

		String enabled = ServerConfigurationService.getString(
				"basiclti.outcomes.enabled", null);
		if (enabled == null || !("true".equals(enabled))) {
			M_log.warn("Basic LTI Outcomes are Disabled IP=" + ipAddress);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		boolean success = false;
		
		Map<String,String> theMap = new TreeMap<String,String>();

		Map<String,String[]> params = (Map<String,String[]>)request.getParameterMap();
		for (Map.Entry<String,String[]> param : params.entrySet()) {
			M_log.debug(param.getKey() + ":" + param.getValue()[0]);
		}

		String lti_version = request.getParameter("lti_version");
		String lti_message_type = request.getParameter("lti_message_type");
		String oauth_consumer_key = request.getParameter("oauth_consumer_key");

		theMap.put("/simpleoutcome/lti_message_type", lti_message_type);

		// Sadly not supported easily using the Gradebook API - may have to dig
		// deeper later
		if ( BasicLTIUtil.equals(lti_message_type, "simple-lis-deleteresult") ) {
			theMap.put("/simpleoutcome/statusinfo/codemajor", "Unsupported");
			theMap.put("/simpleoutcome/statusinfo/severity", "Error");
			theMap.put("/simpleoutcome/statusinfo/codeminor", "cannotdelete");
			String theXml = XMLMap.getXML(theMap, true);
			PrintWriter out = response.getWriter();
			out.println(theXml);
			return;
                }

		//check message type
		if( BasicLTIUtil.equals(lti_message_type, "simple-lis-replaceresult") || 
		    BasicLTIUtil.equals(lti_message_type, "simple-lis-createresult") || 
		    BasicLTIUtil.equals(lti_message_type, "simple-lis-updateresult") || 
		    BasicLTIUtil.equals(lti_message_type, "simple-lis-readresult") ) {
			// OK
                } else {
			doError(request, response, theMap, "outcomes.invalid", "lti_message_type="+lti_message_type, null);
			return;
		}
		if(!BasicLTIUtil.equals(lti_version, "LTI-1p0")) {
			doError(request, response, theMap, "outcomes.invalid", "lti_version="+lti_version, null);
			return;
		}

		if(BasicLTIUtil.isBlank(oauth_consumer_key)) {
			doError(request, response, theMap, "outcomes.missing", "oauth_consumer_key", null);
			return;
		}

		String sourcedid = request.getParameter("sourcedid");
		if(BasicLTIUtil.isBlank(sourcedid)) {
			doError(request, response, theMap, "outcomes.missing", "sourcedid", null);
			return;
		}

		String placement_id = null;
		String signature = null;
		String user_id = null;
		// Attempt to parse the sourcedid, any failure,if fatal
		try {
                	int pos = sourcedid.indexOf(":::");
                	if ( pos > 0 ) {
				signature = sourcedid.substring(0, pos);
                    		String dec2 = sourcedid.substring(pos+3);
		    		pos = dec2.indexOf(":::");
                    		placement_id = dec2.substring(0,pos);
                    		user_id = dec2.substring(pos+3);
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

System.out.println("signature="+signature);
System.out.println("placement_id="+placement_id);
System.out.println("user_id="+user_id);

		M_log.debug("signature="+signature);
		M_log.debug("user_id="+user_id);
		M_log.debug("placement_id="+placement_id);

		ToolConfiguration placement = SiteService.findTool(placement_id);
		Properties config = placement.getConfig();
		String siteId = null;
		Site site = null;
		try { 
			placement = SiteService.findTool(placement_id);
			config = placement.getConfig();
			siteId = placement.getSiteId();
			site = SiteService.getSite(siteId);
		} catch (Exception e) {
			M_log.debug("Error retrieving result_sourcedid information: "+e.getLocalizedMessage(), e);
                        placement = null;
		}

		// Send a more generic message back to the caller
		if ( placement == null || config == null || siteId == null || site == null ) {
			doError(request, response, theMap, "outcomes.sourcedid", "sourcedid", null);
			return;
		}

		// Check the message signature using OAuth
		String oauth_secret = SakaiBLTIUtil.toNull(SakaiBLTIUtil.getCorrectProperty(config,"secret", placement));

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
		String grade_secret  = SakaiBLTIUtil.toNull(SakaiBLTIUtil.getCorrectProperty(config,"gradesecret", placement));

		// Send a generic message back to the caller
		if ( grade_secret ==null ) {
			doError(request, response, theMap, "outcomes.sourcedid", "sourcedid", null);
			return;
		}

		String pre_hash = grade_secret + ":::" + placement_id + ":::" + user_id;
		String received_signature = ShaUtil.sha1Hash(pre_hash);
System.out.println("Received signature="+signature+" received="+received_signature);
		boolean matched = signature.equals(received_signature);

		String old_grade_secret  = SakaiBLTIUtil.toNull(SakaiBLTIUtil.getCorrectProperty(config,"oldgradesecret", placement));
		if ( old_grade_secret != null && ! matched ) {
			pre_hash = grade_secret + ":::" + placement_id + ":::" + user_id;
			received_signature = ShaUtil.sha1Hash(pre_hash);
System.out.println("Received signature II="+signature+" received="+received_signature);
			matched = signature.equals(received_signature);
		}

		// Send a message back to the caller
		if ( ! matched ) {
			doError(request, response, theMap, "outcomes.sourcedid", "sourcedid", null);
			return;
		}

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
		String assignment = SakaiBLTIUtil.toNull(SakaiBLTIUtil.getCorrectProperty(config,"assignment", placement));
System.out.println("ASSN="+assignment);
		if ( assignment == null ) {
                        doError(request, response, theMap, "outcome.no.assignment", "", null);
                        return;
		}

		boolean isRead = BasicLTIUtil.equals(lti_message_type, "simple-lis-readresult");

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
                try {
			// Indicate "who" is setting this grade - needs to be a real user account
			String gb_user_id = ServerConfigurationService.getString(
				"basiclti.outcomes.userid", "admin");
			String gb_user_eid = ServerConfigurationService.getString(
				"basiclti.outcomes.usereid", gb_user_id);
                        sess.setUserId(gb_user_id);
                        sess.setUserEid(gb_user_eid);
                	GradebookService g = (GradebookService)  ComponentManager
                                .get("org.sakaiproject.service.gradebook.GradebookService");
			if ( isRead ) {
				theGrade = g.getAssignmentScoreString(siteId, assignment, user_id);
				theMap.put("/simpleoutcome/result/resultscore/textstring", theGrade);
System.out.println("HELLO READ GRADE= "+theGrade);
			} else { 

				g.setAssignmentScoreString(siteId, assignment, user_id, result_resultscore_textstring, "External Outcome");
				M_log.info("Stored Score=" + siteId + " assignment="+ assignment + " user_id=" + user_id + " score="+ result_resultscore_textstring);
			}
               		success = true;
			theMap.put("/simpleoutcome/statusinfo/codemajor", "Success");
			theMap.put("/simpleoutcome/statusinfo/severity", "Status");
			theMap.put("/simpleoutcome/statusinfo/codeminor", "fullsuccess");
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

	public void destroy() {

	}
	
}
