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
 *			 http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.lti2;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Properties;

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
import org.sakaiproject.component.cover.ServerConfigurationService;
// import org.sakaiproject.tool.api.Tool;
// import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
import org.imsglobal.basiclti.BasicLTIConstants;
import org.imsglobal.lti2.LTI2Constants;
import org.imsglobal.lti2.objects.*;
import org.sakaiproject.lti2.SakaiLTI2Services;

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

    protected String resourceUrl = null;
    protected Service_offered LTI2ResultItem = null;
    protected Service_offered LTI2LtiLinkSettings = null;
    protected Service_offered LTI2ToolProxySettings = null;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if ( ltiService == null ) ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");

		resourceUrl = SakaiBLTIUtil.getOurServerUrl() + "/imsblis/lti2";
        LTI2ResultItem = StandardServices.LTI2ResultItem(resourceUrl);
        LTI2LtiLinkSettings = StandardServices.LTI2LtiLinkSettings(resourceUrl);
        LTI2ToolProxySettings = StandardServices.LTI2ToolProxySettings(resourceUrl);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	protected void getToolConsumerProfile(HttpServletRequest request, 
			HttpServletResponse response,String profile_id)
	{
System.out.println("profile_id="+profile_id);
		Map<String,Object> deploy = ltiService.getDeployForConsumerKeyDao(profile_id);
		if ( deploy == null ) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND); 
			return;
		}
System.out.println("deploy="+deploy);

		String serverUrl = SakaiBLTIUtil.getOurServerUrl();
		Product_family fam = new Product_family("SakaiCLE", "CLE", "Sakai Project",
				"Amazing open source Collaboration and Learning Environment.", 
				"http://www.sakaiproject.org", "support@sakaiproject.org");

		Product_info info = new Product_info("CTools", "4.0", "The Sakai installation for UMich", fam);
		Service_owner sowner = new Service_owner("https://ctools.umich.edu/", "CTools", "Description", "support@ctools.umich.edu");
		Service_provider powner = new Service_provider("https://ctools.umich.edu/", "CTools", "Description", "support@ctools.umich.edu");

		Product_instance instance = new Product_instance("ctools-001", info, sowner, powner, "support@ctools.umich.edu");

		ToolConsumer consumer = new ToolConsumer(profile_id+"", resourceUrl, instance);
		List<String> capabilities = consumer.getCapability_offered();

		if (foorm.getLong(deploy.get(LTIService.LTI_SENDEMAILADDR)) > 0 ) {
			capabilities.add("Person.email.primary");
		}

		if (foorm.getLong(deploy.get(LTIService.LTI_SENDNAME)) > 0 ) {
            capabilities.add("User.username");
			capabilities.add("Person.name.fullname");
			capabilities.add("Person.name.given");
			capabilities.add("Person.name.family");
            capabilities.add("Person.name.full");
		}

		List<Service_offered> services = consumer.getService_offered();
		services.add(StandardServices.LTI2Registration(serverUrl+"/imsblis/lti2/tc_registration/"+profile_id));

		if (foorm.getLong(deploy.get(LTIService.LTI_ALLOWOUTCOMES)) > 0 ) {
			services.add(LTI2ResultItem);
			services.add(StandardServices.LTI1Outcomes(serverUrl+"/imsblis/service/"));
			services.add(SakaiLTI2Services.BasicOutcomes(serverUrl+"/imsblis/service/"));
			capabilities.add("Result.sourcedId");
			capabilities.add("Result.autocreate");
		}
		if (foorm.getLong(deploy.get(LTIService.LTI_ALLOWROSTER)) > 0 ) {
			services.add(SakaiLTI2Services.BasicRoster(serverUrl+"/imsblis/service/"));
		}
		if (foorm.getLong(deploy.get(LTIService.LTI_ALLOWSETTINGS)) > 0 ) {
			services.add(SakaiLTI2Services.BasicSettings(serverUrl+"/imsblis/service/"));
			services.add(LTI2LtiLinkSettings);
			services.add(LTI2ToolProxySettings);
		}

		if (foorm.getLong(deploy.get(LTIService.LTI_ALLOWLORI)) > 0 ) {
			services.add(SakaiLTI2Services.LORI_XML(serverUrl+"/imsblis/service/"));
		}

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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException 
	{
		String ipAddress = request.getRemoteAddr();
		M_log.debug("Basic LTI Service request from IP=" + ipAddress);

		String rpi = request.getPathInfo();
		String uri = request.getRequestURI();
		String [] parts = uri.split("/");
		if ( parts.length < 4 ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); 
			doErrorJSON(request, response, null, "request.bad.url", "Incorrect url format", null);
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
		} else if ( "Result".equals(controller) && parts.length == 5 ) {
			String sourcedid = parts[4];
			handleResultRequest(request, response, sourcedid);
			return;
		}

System.out.println("Controller="+controller);
		IMSJSONRequest jsonRequest = new IMSJSONRequest(request);
		if ( jsonRequest.valid ) {
		    System.out.println(jsonRequest.getPostBody());
		}

		response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED); 
		doErrorJSON(request, response, null, "request.not.implemented", "Unknown request", null);
	}

	public void registerToolProviderProfile(HttpServletRequest request,HttpServletResponse response, 
			String profile_id) throws java.io.IOException
	{
System.out.println("profile_id="+profile_id);
		Map<String,Object> deploy = ltiService.getDeployForConsumerKeyDao(profile_id);
		if ( deploy == null ) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND); 
			return;
		}
System.out.println("deploy="+deploy);
		Long deployKey = foorm.getLong(deploy.get(LTIService.LTI_ID));
System.out.println("deployKey="+deployKey);

		// See if we can even register...
		Long reg_state = foorm.getLong(deploy.get(LTIService.LTI_REG_STATE));
		String key = null;
		String secret = null;
		if ( reg_state == 0 ) {
			key = (String) deploy.get(LTIService.LTI_REG_KEY);
			secret = (String) deploy.get(LTIService.LTI_REG_PASSWORD);
		} else {
			key = (String) deploy.get(LTIService.LTI_CONSUMERKEY);
			secret = (String) deploy.get(LTIService.LTI_SECRET);
		}

		IMSJSONRequest jsonRequest = new IMSJSONRequest(request);

		if ( ! jsonRequest.valid ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, "deploy.register.valid", "Request is not in a valid format", null);
			return;
		}
		// System.out.println(jsonRequest.getPostBody());

		// Lets check the signature
		if ( key == null || secret == null ) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); 
			doErrorJSON(request, response, jsonRequest, "deploy.register.credentials", "Deployment is missing credentials", null);
			return;
		}

		jsonRequest.validateRequest(key, secret, request);
		if ( !jsonRequest.valid ) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); 
			doErrorJSON(request, response, jsonRequest, "deploy.register.signature", "OAuth signature failure", null);
			return;
		}

		JSONObject providerProfile = (JSONObject) JSONValue.parse(jsonRequest.getPostBody());
		// System.out.println("OBJ:"+providerProfile);
		if ( providerProfile == null  ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, "deploy.register.parse", "JSON parse failed", null);
			return;
		}

		JSONObject security_contract = (JSONObject) providerProfile.get("security_contract");
		if ( security_contract == null  ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, "deploy.register.parse", "JSON missing security_contract", null);
			return;
		}

		String shared_secret = (String) security_contract.get("shared_secret");
		if ( shared_secret == null  ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, "deploy.register.parse", "JSON missing shared_secret", null);
			return;
		}
		// Blank out the new shared secret
		security_contract.put("shared_secret", "*********");

		// Parse the tool profile bit and extract the tools with error checking
		List<Properties> theTools = new ArrayList<Properties> ();
		Properties info = new Properties();
		try {
			String [] retval = BasicLTIUtil.parseToolProfile(theTools, info, providerProfile);
System.out.println("info = " + info);
			if ( retval != null ) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				doErrorJSON(request, response, jsonRequest, retval[0], retval[1], null);
				return;
			}
		}
		catch (Exception e ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, "deploy.parse.exception", "Exception:"+ e.getLocalizedMessage(), e);
			return;
		}

		if ( theTools.size() < 1 ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, "deploy.register.notools", "No tools found in profile", null);
			return;
		}

		// TODO: Loop through and validate all of the launch urls in the tools

		// TODO: Check all the services to make sure we like them ....

		Map<String, Object> deployUpdate = new TreeMap<String, Object> ();

		// TODO: Make sure to encrypt that password...
		deployUpdate.put(LTIService.LTI_SECRET, shared_secret);

		// Indicate ready to validate and kill the interim info
		deployUpdate.put(LTIService.LTI_REG_STATE, "1");
		deployUpdate.put(LTIService.LTI_REG_KEY, "");
		deployUpdate.put(LTIService.LTI_REG_PASSWORD, "");
System.out.println("deployUpdate="+deployUpdate);

		deployUpdate.put(LTIService.LTI_REG_PROFILE, providerProfile.toString());
		Object obj = ltiService.updateDeployDao(deployKey, deployUpdate);
		boolean success = ( obj instanceof Boolean ) && ( (Boolean) obj == Boolean.TRUE);

		Map jsonResponse = new TreeMap();
		jsonResponse.put("@context","http://purl.imsglobal.org/ctx/lti/v2/ToolProxyId");
		jsonResponse.put("@type", "ToolProxy");
		String serverUrl = ServerConfigurationService.getServerUrl();
		jsonResponse.put("@id", serverUrl+"/imsblis/lti2/tc_registration/"+profile_id);
		jsonResponse.put("tool_proxy_guid", profile_id);
		response.setContentType(StandardServices.FORMAT_TOOLPROXY_ID);
		response.setStatus(HttpServletResponse.SC_CREATED); // TODO: Get this right
		String jsonText = JSONValue.toJSONString(jsonResponse);
		M_log.debug(jsonText);
		PrintWriter out = response.getWriter();
		out.println(jsonText);
	}

	public void handleResultRequest(HttpServletRequest request,HttpServletResponse response, 
			String sourcedid) throws java.io.IOException
	{
System.out.println("sourcedid="+sourcedid);

		Object retval = SakaiBLTIUtil.checkSourceDid(sourcedid, request, ltiService);
System.out.println("retval="+retval);
/*
		Map jsonResponse = new TreeMap();
		jsonResponse.put("@context","http://purl.imsglobal.org/ctx/lti/v2/ToolProxyId");
		jsonResponse.put("@type", "ToolProxy");
		String serverUrl = ServerConfigurationService.getServerUrl();
		jsonResponse.put("@id", serverUrl+"/imsblis/lti2/tc_registration/"+profile_id);
		jsonResponse.put("tool_proxy_guid", profile_id);
		response.setContentType(StandardServices.FORMAT_TOOLPROXY_ID);
		response.setStatus(HttpServletResponse.SC_CREATED); // TODO: Get this right
		String jsonText = JSONValue.toJSONString(jsonResponse);
		M_log.debug(jsonText);
		PrintWriter out = response.getWriter();
		out.println(jsonText);
*/
	}

	/* IMS JSON version of Errors */
	public void doErrorJSON(HttpServletRequest request,HttpServletResponse response, 
			IMSJSONRequest json, String s, String message, Exception e) 
		throws java.io.IOException 
		{
			if (e != null) {
				M_log.error(e.getLocalizedMessage(), e);
			}
			M_log.info(message);
			response.setContentType("application/json");
			Map jsonResponse = new TreeMap();
			jsonResponse.put("ext_sakai_code", s);
			jsonResponse.put("ext_sakai_code_text", rb.getString(s));

			Map status = null;
			if ( json == null ) {
				status = IMSJSONRequest.getStatusFailure(message);
			} else {
				status = json.getStatusFailure(message);
				if ( json.base_string != null ) {
					jsonResponse.put("base_string", json.base_string);
				}
			}
			jsonResponse.put(IMSJSONRequest.STATUS, status);
			if ( e != null ) {
				jsonResponse.put("exception", e.getLocalizedMessage());
				try {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw, true);
					e.printStackTrace(pw);
					pw.flush();
					sw.flush();
					jsonResponse.put("traceback", sw.toString() );
				} catch ( Exception f ) {
					jsonResponse.put("traceback", f.getLocalizedMessage());
				}
			}
			String jsonText = JSONValue.toJSONString(jsonResponse);
System.out.print(jsonText);
			PrintWriter out = response.getWriter();
			out.println(jsonText);
		}

	public void destroy() {
	}

}
