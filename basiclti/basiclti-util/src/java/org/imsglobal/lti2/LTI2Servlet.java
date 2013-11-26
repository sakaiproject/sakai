/**
 * Copyright (c) 2013 IMS GLobal Learning Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * Author: Charles Severance <csev@umich.edu>
 */

package org.imsglobal.lti2;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.imsglobal.basiclti.BasicLTIUtil;
import org.imsglobal.basiclti.BasicLTIConstants;
import org.imsglobal.lti2.LTI2Constants;
import org.imsglobal.lti2.LTI2Config;
import org.imsglobal.lti2.LTI2Util;
import org.imsglobal.lti2.LTI2SampleData;
import org.imsglobal.lti2.objects.*;

import org.imsglobal.json.IMSJSONRequest;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 * Notes:
 * 
 * This is a sample "Hello World" servlet for LTI2.  It is a simple UI - mostly 
 * intended to exercise the APIs and show the way for servlet-based LTI2 code.
 * 
 * Here are the web.xml entries:
 *
 *  <servlet>
 *    <servlet-name>SampleServlet</servlet-name>
 *    <servlet-class>org.imsglobal.lti2.LTI2Servlet</servlet-class>
 *  </servlet>
 *  <servlet-mapping>
 *    <servlet-name>SampleServlet</servlet-name>
 *    <url-pattern>/sample/*</url-pattern>
 *  </servlet-mapping>
 *
 *  The navigate to:
 *  http://localhost/testservlet/sample/register
 * 
 *  A PHP endpoint is available at:
 * 
 *  https://source.sakaiproject.org/svn/basiclti/trunk/basiclti-docs/resources/docs/sakai-api-test
 * 
 *  The tp.php script is the Tool Provider registration endpoint in the PHP code
 * 
 */

@SuppressWarnings("deprecation")
public class LTI2Servlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Log M_log = LogFactory.getLog(LTI2Servlet.class);

	protected Service_offered LTI2ResultItem = null;
	protected Service_offered LTI2LtiLinkSettings = null;
	protected Service_offered LTI2ToolProxyBindingSettings = null;
	protected Service_offered LTI2ToolProxySettings = null;

	private static final String SVC_tc_profile = "tc_profile";
	private static final String SVC_tc_registration = "tc_registration";
	private static final String SVC_Settings = "Settings";
	private static final String SVC_Result = "Result";

	private static final String EMPTY_JSON_OBJECT = "{\n}\n";

	private static final String APPLICATION_JSON = "application/json";

	// Normally these would be in a database
	private static String TEST_KEY = "42";
	private static String TEST_SECRET = "zaphod";

	// Pretending to be a database row :)
	private static Map<String, String> PERSIST = new TreeMap<String, String> ();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException 
	{
		try { 
			doRequest(request, response);
		} catch (Exception e) {
			String ipAddress = request.getRemoteAddr();
			String uri = request.getRequestURI();
			M_log.warn("General LTI2 Failure URI="+uri+" IP=" + ipAddress);
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
			doErrorJSON(request, response, null, "General failure", e);
		}
	}

	@SuppressWarnings("unchecked")
	protected void doRequest(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException 
	{
		System.out.println("getServiceURL="+getServiceURL(request));

		String ipAddress = request.getRemoteAddr();
		System.out.println("LTI Service request from IP=" + ipAddress);

		String rpi = request.getPathInfo();
		String uri = request.getRequestURI();
		String [] parts = uri.split("/");
		if ( parts.length < 4 ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); 
			doErrorJSON(request, response, null, "Incorrect url format", null);
			return;
		}
		String controller = parts[3];
		if ( "register".equals(controller) ) {
			doRegister(request,response);
			return;
		} else if ( "launch".equals(controller) ) {
			doLaunch(request,response);
			return;
		} else if ( SVC_tc_profile.equals(controller) && parts.length == 5 ) {
			String profile_id = parts[4];
			getToolConsumerProfile(request,response,profile_id);
			return;
		} else if ( SVC_tc_registration.equals(controller) && parts.length == 5 ) {
			String profile_id = parts[4];
			registerToolProviderProfile(request, response, profile_id);
			return;
		} else if ( SVC_Result.equals(controller) && parts.length == 5 ) {
			String sourcedid = parts[4];
			handleResultRequest(request, response, sourcedid);
			return;
		} else if ( SVC_Settings.equals(controller) && parts.length >= 6 ) {
			handleSettingsRequest(request, response, parts);
			return;
		}

		IMSJSONRequest jsonRequest = new IMSJSONRequest(request);
		if ( jsonRequest.valid ) {
			System.out.println(jsonRequest.getPostBody());
		}

		response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED); 
		M_log.warn("Unknown request="+uri);
		doErrorJSON(request, response, null, "Unknown request="+uri, null);
	}

	protected void doRegister(HttpServletRequest request, HttpServletResponse response)
	{
		// Reset our database
		PERSIST.clear();
		String launch_url = request.getParameter("launch_url");
		response.setContentType("text/html");

		String output = null;
		if ( launch_url != null ) {
			Properties ltiProps = new Properties();

			ltiProps.setProperty(BasicLTIConstants.LTI_VERSION, LTI2Constants.LTI2_VERSION_STRING);
			ltiProps.setProperty(LTI2Constants.REG_KEY,TEST_KEY);
			ltiProps.setProperty(LTI2Constants.REG_PASSWORD,TEST_SECRET);
			ltiProps.setProperty(BasicLTIUtil.BASICLTI_SUBMIT, "Press to Launch External Tool");
			ltiProps.setProperty(BasicLTIConstants.LTI_MESSAGE_TYPE, BasicLTIConstants.LTI_MESSAGE_TYPE_TOOLPROXYREGISTRATIONREQUEST);

			String serverUrl = getServiceURL(request);
			ltiProps.setProperty(LTI2Constants.TC_PROFILE_URL,serverUrl + SVC_tc_profile + "/" + TEST_KEY);
			ltiProps.setProperty(BasicLTIConstants.LAUNCH_PRESENTATION_RETURN_URL, serverUrl + "launch");
			System.out.println("ltiProps="+ltiProps);

			boolean dodebug = true;
			output = BasicLTIUtil.postLaunchHTML(ltiProps, launch_url, dodebug);
		} else {
			output = "<form>Register URL:<br/><input type=\"text\" name=\"launch_url\" size=\"80\"\n" + 
				"value=\"http://localhost:8888/sakai-api-test/tp.php\"><input type=\"submit\">\n";
		}

		try {
			PrintWriter out = response.getWriter();
			out.println(output);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// We are actually bypassing the activation step.  Usually activation will parse
	// the profile, and install a tool if the admin is happy.  For us we just parse 
	// the profile and do a launch.
	protected void doLaunch(HttpServletRequest request, HttpServletResponse response)
	{
		
		String profile = PERSIST.get("profile");
		response.setContentType("text/html");

		String output = null;
		if ( profile == null ) {
			output = "Missing profile";
		} else {
	        JSONObject providerProfile = (JSONObject) JSONValue.parse(profile);

			List<Properties> profileTools = new ArrayList<Properties> ();
	        Properties info = new Properties();
			String retval = LTI2Util.parseToolProfile(profileTools, info, providerProfile);
			String launch = null;
			String parameter = null;
			for ( Properties profileTool : profileTools ) {
				launch = (String) profileTool.get("launch");
				parameter = (String) profileTool.get("parameter");
			}
			JSONObject security_contract = (JSONObject) providerProfile.get(LTI2Constants.SECURITY_CONTRACT);

			String shared_secret = (String) security_contract.get(LTI2Constants.SHARED_SECRET);
			System.out.println("launch="+launch);
			System.out.println("shared_secret="+shared_secret);
			output = "YO";

			Properties ltiProps = LTI2SampleData.getLaunch();
			ltiProps.setProperty(BasicLTIConstants.LTI_VERSION,BasicLTIConstants.LTI_VERSION_2);

			Properties lti2subst = LTI2SampleData.getSubstitution();
			String settings_url = getServiceURL(request) + SVC_Settings + "/";
			lti2subst.setProperty("LtiLink.custom.url", settings_url + LTI2Util.SCOPE_LtiLink + "/"
					+ ltiProps.getProperty(BasicLTIConstants.RESOURCE_LINK_ID));
			lti2subst.setProperty("ToolProxyBinding.custom.url", settings_url + LTI2Util.SCOPE_ToolProxyBinding + "/" 
					+ ltiProps.getProperty(BasicLTIConstants.CONTEXT_ID));
			lti2subst.setProperty("ToolProxy.custom.url", settings_url + LTI2Util.SCOPE_ToolProxy + "/" 
					+ TEST_KEY);
			lti2subst.setProperty("Result.url", getServiceURL(request) + SVC_Result + "/"
					+ ltiProps.getProperty(BasicLTIConstants.RESOURCE_LINK_ID));

			// Do the substitutions
			Properties custom = new Properties();
			LTI2Util.mergeLTI2Parameters(custom, parameter);
			LTI2Util.substituteCustom(custom, lti2subst);

			// Place the custom values into the launch
			LTI2Util.addCustomToLaunch(ltiProps, custom);

			ltiProps = BasicLTIUtil.signProperties(ltiProps, launch, "POST",
                TEST_KEY, shared_secret, null, null, null);

			boolean dodebug = true;
			output = BasicLTIUtil.postLaunchHTML(ltiProps, launch, dodebug);
		}

		try {
			PrintWriter out = response.getWriter();
			out.println(output);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void getToolConsumerProfile(HttpServletRequest request, 
			HttpServletResponse response,String profile_id)
	{
		// Map<String,Object> deploy = ltiService.getDeployForConsumerKeyDao(profile_id);
		Map<String,Object> deploy = null;

		ToolConsumer consumer = buildToolConsumerProfile(request, deploy, profile_id);

		ObjectMapper mapper = new ObjectMapper();
		try {
			// http://stackoverflow.com/questions/6176881/how-do-i-make-jackson-pretty-print-the-json-content-it-generates
			ObjectWriter writer = mapper.defaultPrettyPrintingWriter();
			// ***IMPORTANT!!!*** for Jackson 2.x use the line below instead of the one above: 
			// ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
			// System.out.println(mapper.writeValueAsString(consumer));
			response.setContentType(APPLICATION_JSON);
			PrintWriter out = response.getWriter();
			out.println(writer.writeValueAsString(consumer));
			// System.out.println(writer.writeValueAsString(consumer));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Normally deploy would have the data about the deployment - for this test
	// it is always null and we allow everything
	protected ToolConsumer buildToolConsumerProfile(HttpServletRequest request, Map<String, Object> deploy, String profile_id)
	{
		// Load the configuration data
		LTI2Config cnf = new org.imsglobal.lti2.LTI2ConfigSample();

		ToolConsumer consumer = new ToolConsumer(profile_id+"", getServiceURL(request), cnf);

		// Normally we would check permissions before we offer capabilities
		List<String> capabilities = consumer.getCapability_offered();
		LTI2Util.allowEmail(capabilities);
		LTI2Util.allowName(capabilities);
		LTI2Util.allowSettings(capabilities);
		LTI2Util.allowResult(capabilities);

		// Normally we would check permissions before we offer services
		List<Service_offered> services = consumer.getService_offered();
		services.add(StandardServices.LTI2Registration(getServiceURL(request) + 
			SVC_tc_registration + "/" + profile_id));
		services.add(StandardServices.LTI2ResultItem(getServiceURL(request) + 
			SVC_Result + "/{" + BasicLTIConstants.LIS_RESULT_SOURCEDID + "}"));
		services.add(StandardServices.LTI2LtiLinkSettings(getServiceURL(request) + 
			SVC_Settings + "/" + LTI2Util.SCOPE_LtiLink + "/{" + BasicLTIConstants.RESOURCE_LINK_ID + "}"));
		services.add(StandardServices.LTI2ToolProxySettings(getServiceURL(request) + 
			SVC_Settings + "/" + LTI2Util.SCOPE_ToolProxyBinding + "/{" + BasicLTIConstants.CONTEXT_ID + "}"));
		services.add(StandardServices.LTI2ToolProxySettings(getServiceURL(request) + 
			SVC_Settings + "/" + LTI2Util.SCOPE_ToolProxy + "/{" + LTI2Constants.TOOL_PROXY_GUID + "}"));
		return consumer;
	}

	public void registerToolProviderProfile(HttpServletRequest request,HttpServletResponse response, 
			String profile_id) throws java.io.IOException
	{
		// Normally we would look up the deployment descriptor
		if ( ! TEST_KEY.equals(profile_id) ) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND); 
			return;
		}

		String key = TEST_KEY;
		String secret = TEST_SECRET;

		IMSJSONRequest jsonRequest = new IMSJSONRequest(request);

		if ( ! jsonRequest.valid ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, "Request is not in a valid format", null);
			return;
		}

		System.out.println(jsonRequest.getPostBody());

		// Lets check the signature
		if ( key == null || secret == null ) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); 
			doErrorJSON(request, response, jsonRequest, "Deployment is missing credentials", null);
			return;
		}

		jsonRequest.validateRequest(key, secret, request);
		if ( !jsonRequest.valid ) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); 
			doErrorJSON(request, response, jsonRequest, "OAuth signature failure", null);
			return;
		}

		JSONObject providerProfile = (JSONObject) JSONValue.parse(jsonRequest.getPostBody());
		// System.out.println("OBJ:"+providerProfile);
		if ( providerProfile == null  ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, "JSON parse failed", null);
			return;
		}

		JSONObject default_custom = (JSONObject) providerProfile.get(LTI2Constants.CUSTOM);

		JSONObject security_contract = (JSONObject) providerProfile.get(LTI2Constants.SECURITY_CONTRACT);
		if ( security_contract == null  ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, "JSON missing security_contract", null);
			return;
		}

		String shared_secret = (String) security_contract.get(LTI2Constants.SHARED_SECRET);
		System.out.println("shared_secret="+shared_secret);
		if ( shared_secret == null  ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, "JSON missing shared_secret", null);
			return;
		}

		// Make sure that the requested services are a subset of the offered services
		ToolConsumer consumer = buildToolConsumerProfile(request, null, profile_id);

		JSONArray tool_services = (JSONArray) security_contract.get(LTI2Constants.TOOL_SERVICE);
		String retval = LTI2Util.validateServices(consumer, providerProfile);
		if ( retval != null ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, retval, null);
			return;
		}

		// Parse the tool profile bit and extract the tools with error checking
		retval = LTI2Util.validateCapabilities(consumer, providerProfile);
		if ( retval != null ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, retval, null);
			return;
		}

		// Pass the profile to the launch process
		PERSIST.put("profile", providerProfile.toString());

		// Share our happiness with the Tool Provider
		Map jsonResponse = new TreeMap();
		jsonResponse.put(LTI2Constants.CONTEXT,StandardServices.TOOLPROXY_ID_CONTEXT);
		jsonResponse.put(LTI2Constants.TYPE, StandardServices.TOOLPROXY_ID_TYPE);
		jsonResponse.put(LTI2Constants.JSONLD_ID, getServiceURL(request) + SVC_tc_registration + "/" +profile_id);
		jsonResponse.put(LTI2Constants.TOOL_PROXY_GUID, profile_id);
		jsonResponse.put(LTI2Constants.CUSTOM_URL, getServiceURL(request) + SVC_Settings + "/" + LTI2Util.SCOPE_ToolProxy + "/" +profile_id);
		response.setContentType(StandardServices.TOOLPROXY_ID_FORMAT);
		response.setStatus(HttpServletResponse.SC_CREATED);
		String jsonText = JSONValue.toJSONString(jsonResponse);
		M_log.debug(jsonText);
		PrintWriter out = response.getWriter();
		out.println(jsonText);
	}

	public String getServiceURL(HttpServletRequest request) {
		String scheme = request.getScheme();             // http
		String serverName = request.getServerName();     // localhost
		int serverPort = request.getServerPort();        // 80
		String contextPath = request.getContextPath();   // /imsblis
		String servletPath = request.getServletPath();   // /ltitest
		String url = scheme+"://"+serverName+":"+serverPort+contextPath+servletPath+"/";
		return url;
	}

	public void handleResultRequest(HttpServletRequest request,HttpServletResponse response, 
			String sourcedid) throws java.io.IOException
	{
/*
		Object retval = null;
		IMSJSONRequest jsonRequest = null;
		if ( "GET".equals(request.getMethod()) ) { 
			retval = SakaiBLTIUtil.getGrade(sourcedid, request, ltiService);
			if ( ! (retval instanceof Map) ) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				doErrorJSON(request,response, jsonRequest, (String) retval, null);
				return;
			}
			Map grade = (Map) retval;
			Map jsonResponse = new TreeMap();
			Map resultScore = new TreeMap();
	
			jsonResponse.put(LTI2Constants.CONTEXT,StandardServices.RESULT_CONTEXT);
			jsonResponse.put(LTI2Constants.TYPE, StandardServices.RESULT_TYPE);
			jsonResponse.put(LTI2Constants.COMMENT, grade.get(LTI2Constants.COMMENT));
			resultScore.put(LTI2Constants.TYPE, LTI2Constants.GRADE_TYPE_DECIMAL);
			resultScore.put(LTI2Constants.VALUE, grade.get(LTI2Constants.GRADE));
			jsonResponse.put(LTI2Constants.RESULTSCORE,resultScore);
			response.setContentType(StandardServices.RESULT_FORMAT);
			response.setStatus(HttpServletResponse.SC_OK);
			String jsonText = JSONValue.toJSONString(jsonResponse);
			M_log.debug(jsonText);
			PrintWriter out = response.getWriter();
			out.println(jsonText);
		} else if ( "PUT".equals(request.getMethod()) ) { 
			retval = "Error parsing input data";
			try {
				jsonRequest = new IMSJSONRequest(request);
				// System.out.println(jsonRequest.getPostBody());
				JSONObject requestData = (JSONObject) JSONValue.parse(jsonRequest.getPostBody());
				String comment = (String) requestData.get(LTI2Constants.COMMENT);
				JSONObject resultScore = (JSONObject) requestData.get(LTI2Constants.RESULTSCORE);
				String sGrade = (String) resultScore.get(LTI2Constants.VALUE);
				Double dGrade = new Double(sGrade);
				retval = SakaiBLTIUtil.setGrade(sourcedid, request, ltiService, dGrade, comment);
			} catch (Exception e) {
				retval = "Error: "+ e.getMessage();
			}
			if ( retval instanceof Boolean && (Boolean) retval ) {
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		} else {
			retval = "Unsupported operation:" + request.getMethod();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	
		if ( retval instanceof String ) {
			doErrorJSON(request,response, jsonRequest, (String) retval, null);
			return;
		}
*/
	}

	// If this code looks like a hack - it is because the spec is a hack.
	// There are five possible scenarios for GET and two possible scenarios
    // for PUT.  I begged to simplify the business logic but was overrulled.
	// So we write obtuse code.
	public void handleSettingsRequest(HttpServletRequest request,HttpServletResponse response, 
			String[] parts) throws java.io.IOException
	{

/*

		String URL = SakaiBLTIUtil.getOurServletPath(request);
		String scope = parts[4];

		// Check to see if we are doing the bubble
		String bubbleStr = request.getParameter("bubble");
		String acceptHdr = request.getHeader("Accept");
		String contentHdr = request.getContentType();
System.out.println("accept="+acceptHdr+" bubble="+bubbleStr);

		if ( bubbleStr != null && bubbleStr.equals("all") &&
			acceptHdr.indexOf(StandardServices.TOOLSETTINGS_FORMAT) < 0 ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, null, "Simple format does not allow bubble=all", null);
			return;
		}

		boolean bubble = bubbleStr != null && "GET".equals(request.getMethod());
		boolean distinct = bubbleStr != null && "distinct".equals(bubbleStr) && "GET".equals(request.getMethod());
		boolean bubbleAll = bubbleStr != null && "all".equals(bubbleStr) && "GET".equals(request.getMethod());

		// Check our input and output formats
		boolean acceptSimple = acceptHdr == null || acceptHdr.indexOf(StandardServices.TOOLSETTINGS_SIMPLE_FORMAT) >= 0 ;
		boolean acceptComplex = acceptHdr == null || acceptHdr.indexOf(StandardServices.TOOLSETTINGS_FORMAT) >= 0 ;
		boolean inputSimple = contentHdr == null || contentHdr.indexOf(StandardServices.TOOLSETTINGS_SIMPLE_FORMAT) >= 0 ;
		boolean inputComplex = contentHdr != null && contentHdr.indexOf(StandardServices.TOOLSETTINGS_FORMAT) >= 0 ;
System.out.println("as="+acceptSimple+" ac="+acceptComplex+" is="+inputSimple+" ic="+inputComplex);

		// Check the JSON on PUT and check the oauth_body_hash
		IMSJSONRequest jsonRequest = null;
		JSONObject requestData = null;
		if ( "PUT".equals(request.getMethod()) ) {
			try {
				jsonRequest = new IMSJSONRequest(request);
				requestData = (JSONObject) JSONValue.parse(jsonRequest.getPostBody());
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				doErrorJSON(request,response, jsonRequest, "Could not parse JSON", e);
				return;
			}
		}

		String consumer_key = null;
		String siteId = null;
		String placement_id = null;

		Map<String,Object> content = null;
		Long contentKey = null;
		Map<String,Object> tool = null;
		Long toolKey = null;
		Map<String,Object> proxyBinding = null;
		Long proxyBindingKey = null;
		Map<String,Object> deploy = null;
		Long deployKey = null;

		if ( LTI2Util.SCOPE_LtiLink.equals(scope) || LTI2Util.SCOPE_ToolProxyBinding.equals(scope) ) {
			placement_id = parts[5];
System.out.println("placement_id="+placement_id);
			String contentStr = placement_id.substring(8);
			contentKey = SakaiBLTIUtil.getLongKey(contentStr);
			if ( contentKey  >= 0 ) {
				// Leave off the siteId - bypass all checking - because we need to 
				// find the siteId from the content item
				content = ltiService.getContentDao(contentKey);
				if ( content != null ) siteId = (String) content.get(LTIService.LTI_SITE_ID);
			}
	
			if ( content == null || siteId == null ) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				doErrorJSON(request,response, jsonRequest, "Bad content item", null);
				return;
			}
	
			toolKey = SakaiBLTIUtil.getLongKey(content.get(LTIService.LTI_TOOL_ID));
			if ( toolKey >= 0 ) {
				tool = ltiService.getToolDao(toolKey, siteId);
			}
		
			if ( tool == null ) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				doErrorJSON(request,response, jsonRequest, "Bad tool item", null);
				return;
			}
	
			// Adjust the content items based on the tool items
			ltiService.filterContent(content, tool);

			// Check settings to see if we are allowed to do this 
			if (foorm.getLong(content.get(LTIService.LTI_ALLOWOUTCOMES)) > 0 ||
				foorm.getLong(tool.get(LTIService.LTI_ALLOWOUTCOMES)) > 0 ) {
				// Good news 
			} else {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				doErrorJSON(request,response, jsonRequest, "Item does not allow tool settings", null);
				return;
			}

		}

		if ( LTI2Util.SCOPE_ToolProxyBinding.equals(scope) || LTI2Util.SCOPE_LtiLink.equals(scope) ) {
			proxyBinding = ltiService.getProxyBindingDao(toolKey,siteId);
			if ( proxyBinding != null ) {
				proxyBindingKey = SakaiBLTIUtil.getLongKey(proxyBinding.get(LTIService.LTI_ID));
			}
		}

		// Retrieve the deployment if needed
		if ( LTI2Util.SCOPE_ToolProxy.equals(scope) ) {
			consumer_key = parts[5];
			deploy = ltiService.getDeployForConsumerKeyDao(consumer_key);
			if ( deploy == null ) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				doErrorJSON(request,response, jsonRequest, "Bad deploy item", null);
				return;
			}
			deployKey = SakaiBLTIUtil.getLongKey(deploy.get(LTIService.LTI_ID));
		} else if ( bubble ) {
			deployKey = SakaiBLTIUtil.getLongKey(tool.get(LTIService.LTI_DEPLOYMENT_ID));
			if ( deployKey >= 0 ) {
				deploy = ltiService.getDeployDao(deployKey);
			}
			if ( deploy == null ) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				doErrorJSON(request,response, jsonRequest, "Bad deploy item", null);
				return;
			}
			consumer_key = (String) deploy.get(LTIService.LTI_CONSUMERKEY);
		}

		// Check settings to see if we are allowed to do this 
		if ( deploy != null ) {
			if (foorm.getLong(deploy.get(LTIService.LTI_ALLOWOUTCOMES)) > 0 ) {
				// Good news 
			} else {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				doErrorJSON(request,response, jsonRequest, "Deployment does not allow tool settings", null);
				return;
			}
		}

		// The URLs for the various settings resources
		String settingsUrl = SakaiBLTIUtil.getOurServerUrl() + LTI2_PATH + SVC_Settings;
		String proxy_url = settingsUrl + "/" + LTI2Util.SCOPE_ToolProxy + "/" + consumer_key;
		String binding_url = settingsUrl + "/" + LTI2Util.SCOPE_ToolProxyBinding + "/" + placement_id;
		String link_url = settingsUrl + "/" + LTI2Util.SCOPE_LtiLink + "/" + placement_id;

		// Load and parse the old settings...
		JSONObject link_settings = new JSONObject ();
		JSONObject binding_settings = new JSONObject ();
		JSONObject proxy_settings = new JSONObject();
		if ( content != null ) {
			link_settings = parseSettings((String) content.get(LTIService.LTI_SETTINGS));
		}
		if ( proxyBinding != null ) {
			binding_settings = parseSettings((String) proxyBinding.get(LTIService.LTI_SETTINGS));
		}
		if ( deploy != null ) {
			proxy_settings = parseSettings((String) deploy.get(LTIService.LTI_SETTINGS));
		}

		// Get the secret for the request...
		String oauth_secret = null;
		if ( LTI2Util.SCOPE_LtiLink.equals(scope) ) {
			oauth_secret = (String) content.get(LTIService.LTI_SECRET);
			if ( oauth_secret == null || oauth_secret.length() < 1 ) {
				oauth_secret = (String) tool.get(LTIService.LTI_SECRET);
			}
		} else if ( LTI2Util.SCOPE_ToolProxyBinding.equals(scope) ) {
			oauth_secret = (String) tool.get(LTIService.LTI_SECRET);
		} else if ( LTI2Util.SCOPE_ToolProxy.equals(scope) ) {
			oauth_secret = (String) deploy.get(LTIService.LTI_SECRET);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request,response, jsonRequest, "Bad Setttings Scope="+scope, null);
			return;
		}

		// Validate the incoming message
		Object retval = SakaiBLTIUtil.validateMessage(request, URL, oauth_secret, consumer_key);
		if ( retval instanceof String ) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN); 
			doErrorJSON(request,response, jsonRequest, (String) retval, null);
			return;
		}

		// For a GET request we depend on LTI2Util to do the GET logic
		if ( "GET".equals(request.getMethod()) ) { 
			Object obj = LTI2Util.getSettings(request, scope,
				link_settings, binding_settings, proxy_settings,
				link_url, binding_url, proxy_url);

			if ( obj instanceof String ) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				doErrorJSON(request,response, jsonRequest, (String) obj, null);
				return;
			}

			if ( acceptComplex ) {
				response.setContentType(StandardServices.TOOLSETTINGS_FORMAT);
			} else {
				response.setContentType(StandardServices.TOOLSETTINGS_SIMPLE_FORMAT);
			}

			JSONObject jsonResponse = (JSONObject) obj;
			response.setStatus(HttpServletResponse.SC_OK); 
			PrintWriter out = response.getWriter();
System.out.println("jsonResponse="+jsonResponse);
			out.println(jsonResponse.toString());
			return;
		} else if ( "PUT".equals(request.getMethod()) ) {
			// This is assuming the rule that a PUT of the complex settings
			// format that there is only one entry in the graph and it is
			// the same as our current URL.  We parse without much checking.
			String settings = null;
			try {
				JSONArray graph = (JSONArray) requestData.get(LTI2Constants.GRAPH);
				if ( graph.size() != 1 ) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					doErrorJSON(request,response, jsonRequest, "Only one graph entry allowed", null);
					return;
				}
				JSONObject firstChild = (JSONObject) graph.get(0);
				JSONObject custom = (JSONObject) firstChild.get(LTI2Constants.CUSTOM);
				settings = custom.toString();
			} catch (Exception e) {
				settings = jsonRequest.getPostBody();
			}

			retval = null;
			if ( LTI2Util.SCOPE_LtiLink.equals(scope) ) {
				content.put(LTIService.LTI_SETTINGS, settings);
				retval = ltiService.updateContentDao(contentKey,content,siteId);
			} else if ( LTI2Util.SCOPE_ToolProxyBinding.equals(scope) ) {
				if ( proxyBinding != null ) {
					proxyBinding.put(LTIService.LTI_SETTINGS, settings);
					retval = ltiService.updateProxyBindingDao(proxyBindingKey,proxyBinding);
				} else { 
					Properties proxyBindingNew = new Properties();
					proxyBindingNew.setProperty(LTIService.LTI_SITE_ID, siteId);
					proxyBindingNew.setProperty(LTIService.LTI_TOOL_ID, toolKey+"");
					proxyBindingNew.setProperty(LTIService.LTI_SETTINGS, settings);
					retval = ltiService.insertProxyBindingDao(proxyBindingNew);
					M_log.info("inserted ProxyBinding setting="+proxyBindingNew);
				}
			} else if ( LTI2Util.SCOPE_ToolProxy.equals(scope) ) {
				deploy.put(LTIService.LTI_SETTINGS, settings);
				retval = ltiService.updateDeployDao(deployKey,deploy);
			}
			if ( retval instanceof String || 
				( retval instanceof Boolean && ((Boolean) retval != Boolean.TRUE) ) ) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				doErrorJSON(request,response, jsonRequest, (String) retval, null);
				return;
			}
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request,response, jsonRequest, "Method not handled="+request.getMethod(), null);
		}
*/
	}

	/* IMS JSON version of Errors */
	public void doErrorJSON(HttpServletRequest request,HttpServletResponse response, 
			IMSJSONRequest json, String message, Exception e) 
		throws java.io.IOException 
	{
		if (e != null) {
			M_log.error(e.getLocalizedMessage(), e);
		}
        M_log.info(message);
		String output = IMSJSONRequest.doErrorJSON(request, response, json, message, e);
System.out.println(output);
    }

	public void destroy() {
	}

}
