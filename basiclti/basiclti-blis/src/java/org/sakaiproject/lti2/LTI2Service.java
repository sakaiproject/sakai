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

package org.sakaiproject.lti2;

import java.lang.StringBuffer;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
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

import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

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
import org.imsglobal.lti2.LTI2Constants;
import org.imsglobal.lti2.objects.*;
import org.sakaiproject.basiclti.util.ShaUtil;
import org.sakaiproject.util.FormattedText;

import org.imsglobal.json.IMSJSONRequest;

import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.util.foorm.SakaiFoorm;
import org.sakaiproject.util.foorm.FoormUtil;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 * Notes:
 * 
 * This program is directly exposed as a URL to receive IMS Basic LTI messages
 * so it must be carefully reviewed and any changes must be looked at carefully.
 * 
 */

@SuppressWarnings("deprecation")
public class LTI2Service extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Log M_log = LogFactory.getLog(LTI2Service.class);
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
		doPost(request,response);
	}

	protected void getToolConsumerProfile(HttpServletRequest request, 
		HttpServletResponse response,String profile_id)
	{
System.out.println("profile_id="+profile_id);
		String search = LTIService.LTI_CONSUMERKEY + " = '" + profile_id + "'";
		List<Map<String, Object>> deploys = ltiService.getDeploysDao(search, null, 0, 0);

        Map<String,Object> deploy = null;
		if ( deploys.size() == 1 ) deploy = deploys.get(0);

		if ( deploy == null ) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); // TODO: Get this right
			return;
		}
System.out.println("deploy="+deploy);
		Long deployKey = foorm.getLong(deploy.get(LTIService.LTI_ID));
System.out.println("deployKey="+deployKey);

		if ( deploy == null ) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); // TODO: Get this right
			return;
		}

		String serverUrl = ServerConfigurationService.getServerUrl();
        Product_family fam = new Product_family("SakaiCLE", "CLE", "Sakai Project",
            "Amazing open source Collaboration and Learning Environment.", 
            "http://www.sakaiproject.org", "support@sakaiproject.org");

        Product_info info = new Product_info("CTools", "4.0", "The Sakai installation for UMich", fam);

        Product_instance instance = new Product_instance("ctools-001", info, "support@ctools.umich.edu");

        ToolConsumer consumer = new ToolConsumer(profile_id+"", instance);
        List<Service_offered> services = consumer.getService_offered();
        services.add(StandardServices.LTI2Registration(serverUrl+"/imsblis/lti2/tc_registration/"+profile_id));
        services.add(StandardServices.LTI1Outcomes(serverUrl+"/imsblis/service/"));
        List<String> capabilities = consumer.getCapability_enabled();
        Collections.addAll(capabilities,ToolConsumer.STANDARD_CAPABILITIES);

        ObjectMapper mapper = new ObjectMapper();
        try {
            // http://stackoverflow.com/questions/6176881/how-do-i-make-jackson-pretty-print-the-json-content-it-generates
            ObjectWriter writer = mapper.defaultPrettyPrintingWriter();
            // ***IMPORTANT!!!*** for Jackson 2.x use the line below instead of the one above: 
            // ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
            // System.out.println(mapper.writeValueAsString(consumer));
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			out.println(writer.writeValueAsString(consumer));
            // System.out.println(writer.writeValueAsString(consumer));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

	}

	@SuppressWarnings("unchecked")
	// /imsblis/lti2/part3/part4
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String ipAddress = request.getRemoteAddr();
		M_log.debug("Basic LTI Service request from IP=" + ipAddress);

		String rpi = request.getPathInfo();
		String uri = request.getRequestURI();
		String [] parts = uri.split("/");
		if ( parts.length < 4 ) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); // TODO: Get this right
			return;
		}
		String controller = parts[3];
		if ( "tc_profile".equals(controller) && parts.length == 5 ) {
			String profile_id = parts[4];
			getToolConsumerProfile(request,response,profile_id);
			return;
		} else if ( "tc_registration".equals(controller) && parts.length == 5 ) {
			String profile_id = parts[4];
			registerToolProviderProfile(request, response, profile_id);
			return;
		}
		response.setStatus(HttpServletResponse.SC_FORBIDDEN); // TODO: Get this right
			String contentType = request.getContentType();
			if ( contentType != null && contentType.startsWith("application/json") ) {
				doPostJSON(request, response);
			}
            // TODO: OOPS

		}

	public void registerToolProviderProfile(HttpServletRequest request,HttpServletResponse response, 
		String profile_id) throws java.io.IOException
	{
System.out.println("profile_id="+profile_id);
		// TODO: Need FOORM.escape
		String search = LTIService.LTI_CONSUMERKEY + " = '" + profile_id + "'";
		List<Map<String, Object>> deploys = ltiService.getDeploysDao(search, null, 0, 0);

        Map<String,Object> deploy = null;
		if ( deploys.size() == 1 ) deploy = deploys.get(0);

		if ( deploy == null ) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); // TODO: Get this right
			return;
		}

		// TODO: Check the signature with REG_KEY and REG_SECRET (make sure to encrypt)

		Long reg_state = foorm.getLong(deploy.get(LTIService.LTI_REG_STATE));
		if ( reg_state != 1 ) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); // TODO: Get this right
			return;
		}
		
System.out.println("deploy="+deploy);
		Long deployKey = foorm.getLong(deploy.get(LTIService.LTI_ID));
System.out.println("deployKey="+deployKey);

		IMSJSONRequest jsonRequest = new IMSJSONRequest(request);

		if ( ! jsonRequest.valid ) {
			System.out.println("CRAP:"+ jsonRequest.errorMessage);
			return;
		}
		// System.out.println(jsonRequest.getPostBody());
		JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonRequest.getPostBody());
		// System.out.println("OBJ:"+jsonObject);

		String deploy_proxy_guid = (String) jsonObject.get("tool_proxy_guid");
		System.out.println("TPG="+deploy_proxy_guid);

		JSONObject security_contract = (JSONObject) jsonObject.get("security_contract");
		String shared_secret = (String) security_contract.get("shared_secret");
		// Blank out the new shared secret
		security_contract.put("shared_secret", "*********");

		JSONObject tool_profile = (JSONObject) jsonObject.get("tool_profile");
		JSONArray base_url_choices = (JSONArray) tool_profile.get("base_url_choice");
		String secure_base_url = null;
		String default_base_url = null;
		for ( Object o : base_url_choices ) {
			JSONObject url_choice = (JSONObject) o;
			secure_base_url = (String) url_choice.get("secure_base_url");
			default_base_url = (String) url_choice.get("default_base_url");
		}
		
		String path = null;
		JSONArray resource_handlers = (JSONArray) tool_profile.get("resource_handler");
		JSONArray parameters = null;

		// TODO: Handle more than one tool registration message
		for(Object o : resource_handlers ) {
			JSONObject resource_handler = (JSONObject) o;
			JSONArray messages = (JSONArray) resource_handler.get("message");
			for ( Object m : messages ) {
				JSONObject message = (JSONObject) m;
				String message_type = (String) message.get("message_type");
				if ( ! "basic-lti-launch-request".equals(message_type) ) continue;
				path = (String) message.get("path");
				parameters = (JSONArray) message.get("parameter");
			}
		}
		System.out.println("secret="+shared_secret);
		System.out.println("BU="+secure_base_url+" DBU="+default_base_url);
		System.out.println("path="+path);
		System.out.println("parameters="+parameters);

		String launch_url = secure_base_url;
		if ( launch_url == null ) launch_url = default_base_url;
		if ( ! launch_url.endsWith("/") && ! path.startsWith("/") ) launch_url = launch_url + "/";
		launch_url = launch_url + path;
		System.out.println("Launch="+launch_url);

		Map<String, Object> newProps = new TreeMap<String, Object> ();

		// TODO: Make sure to encrypt that password...
		newProps.put(LTIService.LTI_SECRET, shared_secret);
		newProps.put(LTIService.LTI_LAUNCH, launch_url);
		newProps.put(LTIService.LTI_REG_PARAMETERS, parameters.toString());

		// Indicate registration complete and kill the interim info
		newProps.put(LTIService.LTI_REG_STATE, "2");
		newProps.put(LTIService.LTI_REG_KEY, "");
		newProps.put(LTIService.LTI_REG_PASSWORD, "");
		System.out.println("newProps="+newProps);

		newProps.put(LTIService.LTI_REG_PROFILE, jsonObject.toString());
		Object obj = ltiService.updateDeployDao(deployKey, newProps);
		boolean success = ( obj instanceof Boolean ) && ( (Boolean) obj == Boolean.TRUE);
		System.out.println("YO..."+success);

		/* http://www.imsglobal.org/lti/v2p0pd/ltiIMGv2p0pd.html section 10.1
		
		HTTP/1.0 201 Created
		Date: Thu, 10 May 2012 11:09:42 GMT
		Content-Type: application/vnd.ims.lti.v2.ToolProxy.id+json
		Content-Length: 256
		Location: https://lms.server.com/ToolProxy/869e5ce5-214c-4e85-86c6-b99e8458a592
 
		{
			"@context" : "http://www.imsglobal.org/imspurl/lti/v2/ctx/ToolProxyId",
			"@type" : "ToolProxy",
			"@id" : "https://lms.server.com/ToolProxy/869e5ce5-214c-4e85-86c6-b99e8458a592",
			"tool_proxy_guid" : "869e5ce5-214c-4e85-86c6-b99e8458a592"
		}
		*/
		Map jsonResponse = new TreeMap();
		jsonResponse.put("@context","http://www.imsglobal.org/imspurl/lti/v2/ctx/ToolProxyId");
		jsonResponse.put("@type", "ToolProxy");
		String serverUrl = ServerConfigurationService.getServerUrl();
        jsonResponse.put("@id", serverUrl+"/imsblis/lti2/tc_registration/"+profile_id);
		jsonResponse.put("tool_proxy_guid", profile_id);
		response.setContentType("application/vnd.ims.lti.v2.ToolProxy.id+json");
		response.setStatus(201); // TODO: Get this right
		String jsonText = JSONValue.toJSONString(jsonResponse);
		System.out.println(jsonText);
		PrintWriter out = response.getWriter();
		out.println(jsonText);
	}

    /* IMS JSON version of this service */
	public void doErrorJSON(HttpServletRequest request,HttpServletResponse response, 
			IMSJSONRequest json, String s, String message, Exception e) 
		throws java.io.IOException 
		{
			if (e != null) {
				M_log.error(e.getLocalizedMessage(), e);
			}
			String msg = rb.getString(s) + ": " + message;
			M_log.info(msg);
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
            Map status = null;
            if ( json == null ) {
                status = IMSJSONRequest.getStatusFailure(msg);
            } else {
                status = json.getStatusFailure(msg);
            }
			Map jsonResponse = new TreeMap();
			jsonResponse.put(IMSJSONRequest.STATUS, status);
			String jsonText = JSONValue.toJSONString(jsonResponse);
			System.out.print(jsonText);
			out.println(jsonText);
		}

	@SuppressWarnings("unchecked")
		protected void doPostJSON(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			String ipAddress = request.getRemoteAddr();
			M_log.debug("Basic LTI Service request from IP=" + ipAddress);

			String allowLori = ServerConfigurationService.getString(
					SakaiBLTIUtil.BASICLTI_LORI_ENABLED, null);
			if ( ! "true".equals(allowLori) ) allowLori = null;

			if (allowLori == null ) {
				M_log.warn("LTI Services are disabled IP=" + ipAddress);
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			IMSJSONRequest jsonRequest = new IMSJSONRequest(request);

			if ( ! jsonRequest.valid ) {
				System.out.println("CRAP:"+ jsonRequest.errorMessage);
				return;
			}

			System.out.println(jsonRequest.getPostBody());
			JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonRequest.getPostBody());
			System.out.println("OBJ:"+jsonObject);
			System.out.println("gg:"+jsonObject.get("context_id"));
			String lori_api_token = (String) jsonObject.get("lori_api_token");

			// No point continuing without a lori_api_token
			if(BasicLTIUtil.isBlank(lori_api_token)) {
				doErrorJSON(request, response, jsonRequest, "outcomes.missing", "lori_api_token", null);
				return;
			}

			// Truncate this to the maximum length to insure no cruft at the end
			if ( lori_api_token.length() > 2048) lori_api_token = lori_api_token.substring(0,2048);

			// Attempt to parse the lori_api_token, any failure is fatal
			String placement_id = null;
			String signature = null;
			String user_id = null;
			try {
				int pos = lori_api_token.indexOf(":::");
				if ( pos > 0 ) {
					signature = lori_api_token.substring(0, pos);
					String dec2 = lori_api_token.substring(pos+3);
					pos = dec2.indexOf(":::");
					user_id = dec2.substring(0,pos);
					placement_id = dec2.substring(pos+3);
				}
			} catch (Exception e) {
				// Log some detail for ourselves
				M_log.warn("Unable to decrypt result_lori_api_token IP=" + ipAddress + " Error=" + e.getMessage(),e);
				signature = null;
				placement_id = null;
				user_id = null;
			}

			// Send a more generic message back to the caller
			if ( placement_id == null || user_id == null ) {
				doErrorJSON(request, response, jsonRequest, "outcomes.lori_api_token_01", "lori_api_token", null);
				return;
			}

			M_log.debug("signature="+signature);
			System.out.println("signature="+signature);
			M_log.debug("user_id="+user_id);
			M_log.debug("placement_id="+placement_id);

			Properties pitch = getPropertiesFromPlacement(placement_id);
			if ( pitch == null ) {
				M_log.debug("Error retrieving result_lori_api_token information");
				doErrorJSON(request, response, jsonRequest, "outcomes.lori_api_token_02", "lori_api_token", null);
				return;
			}
	
			String siteId = pitch.getProperty(LTIService.LTI_SITE_ID);
			String context_id = (String) jsonObject.get("context_id");
			if ( siteId == null || ! siteId.equals(context_id)) {
				doErrorJSON(request, response, jsonRequest, "outcomes.lori_api_token_03", "lori_api_token", null);
				return;
			}

			Site site = null;
			try { 
				site = SiteService.getSite(siteId);
			} catch (Exception e) {
				M_log.debug("Error retrieving result_lori_api_token site: "+e.getLocalizedMessage(), e);
			}

			// Send a more generic message back to the caller
			if (  site == null ) {
				doErrorJSON(request, response, jsonRequest, "outcomes.lori_api_token_04", "lori_api_token", null);
				return;
			}

			// Check the message signature using OAuth
			String oauth_consumer_key = jsonRequest.getOAuthConsumerKey();
			String oauth_secret = pitch.getProperty(LTIService.LTI_SECRET);

			jsonRequest.validateRequest(oauth_consumer_key, oauth_secret, request);
			if ( ! jsonRequest.valid ) {
				if (jsonRequest.base_string != null) {
					M_log.warn(jsonRequest.base_string);
				}
				doErrorJSON(request, response, jsonRequest, "outcome.no.validate", oauth_consumer_key, null);
				return;
			}

			// Check the signature of the lori_api_token to make sure it was not altered
			String placement_secret  = pitch.getProperty(LTIService.LTI_PLACEMENTSECRET);

			// Send a generic message back to the caller
			if ( placement_secret ==null ) {
				doErrorJSON(request, response, jsonRequest, "outcomes.lori_api_token_05", "lori_api_token", null);
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
				M_log.warn("Received signature="+signature+" received="+received_signature);
				doErrorJSON(request, response, jsonRequest, "outcomes.lori_api_token", "lori_api_token_06", null);
			}

			String uri = request.getRequestURI();
			String [] parts = uri.split("/");
			String operation = null;
			if ( parts.length >= 4 ) operation = parts[3];

/*
            String placementLori = pitch.getProperty("allowlori");
			if ( allowLori != null && "on".equals(placementLori) && "coursestructure".equals(operation) ) {
			    processCourseStructureJSON(request, response, jsonObject, jsonRequest);
			} else if ( allowLori != null && "on".equals(placementLori) && "addcourseresources".equals(operation) ) {
			    processAddResourcesJSON(request, response, jsonObject, jsonRequest);
*/
            if ( false ) {
                // TODO
			} else { 
				response.setContentType("application/json");
				PrintWriter out = response.getWriter();
				Map status = jsonRequest.getStatusUnsupported("Operation not supported:" + operation);
				Map jsonResponse = new TreeMap();
				jsonResponse.put(IMSJSONRequest.STATUS, status);
				String jsonText = JSONValue.toJSONString(jsonResponse);
				System.out.print(jsonText);
				out.println(jsonText);
			}

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
