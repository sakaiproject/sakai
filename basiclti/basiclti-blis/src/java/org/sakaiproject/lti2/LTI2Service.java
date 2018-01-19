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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import java.security.SecureRandom;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.tsugi.basiclti.BasicLTIConstants;
import org.tsugi.json.IMSJSONRequest;
import org.tsugi.lti2.LTI2Config;
import org.tsugi.lti2.LTI2Constants;
import org.tsugi.lti2.LTI2Messages;
import org.tsugi.lti2.LTI2Util;
import org.tsugi.lti2.ToolProxy;
import org.tsugi.lti2.ContentItem;
import org.tsugi.lti2.objects.Service_offered;
import org.tsugi.lti2.objects.StandardServices;
import org.tsugi.lti2.objects.ToolConsumer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
import org.sakaiproject.basiclti.util.PortableShaUtil;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.util.ResourceLoader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Notes:
 * 
 * This program is directly exposed as a URL to receive IMS Basic LTI messages
 * so it must be carefully reviewed and any changes must be looked at carefully.
 * 
 */

@SuppressWarnings("deprecation")
@Slf4j
public class LTI2Service extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static ResourceLoader rb = new ResourceLoader("blis");

	protected static LTIService ltiService = null;

	protected String resourceUrl = null;
	protected Service_offered LTI2ResultItem = null;
	protected Service_offered LTI2LtiLinkSettings = null;
	protected Service_offered LTI2ToolProxyBindingSettings = null;
	protected Service_offered LTI2ToolProxySettings = null;

	// Copy these in...
	private static final String SVC_tc_profile = SakaiBLTIUtil.SVC_tc_profile;
	private static final String SVC_tc_registration = SakaiBLTIUtil.SVC_tc_registration;
	private static final String SVC_Settings = SakaiBLTIUtil.SVC_Settings;
	private static final String SVC_Result = SakaiBLTIUtil.SVC_Result;

	private static final String LTI1_PATH = SakaiBLTIUtil.LTI1_PATH;
	private static final String LTI2_PATH = SakaiBLTIUtil.LTI2_PATH;

	private static final String APPLICATION_JSON = "application/json";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if ( ltiService == null ) ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");

		resourceUrl = SakaiBLTIUtil.getOurServerUrl() + LTI2_PATH;
		LTI2ResultItem = StandardServices.LTI2ResultItem(resourceUrl 
			+ SVC_Result + "/{" + BasicLTIConstants.LIS_RESULT_SOURCEDID + "}");
		LTI2LtiLinkSettings = StandardServices.LTI2LtiLinkSettings(resourceUrl 
			+ SVC_Settings + "/" + LTI2Util.SCOPE_LtiLink + "/{" + BasicLTIConstants.RESOURCE_LINK_ID + "}");
		LTI2ToolProxyBindingSettings = StandardServices.LTI2ToolProxySettings(resourceUrl 
			+ SVC_Settings + "/" + LTI2Util.SCOPE_ToolProxyBinding + "/{" + BasicLTIConstants.RESOURCE_LINK_ID + "}");
		LTI2ToolProxySettings = StandardServices.LTI2ToolProxySettings(resourceUrl 
			+ SVC_Settings + "/" + LTI2Util.SCOPE_ToolProxy + "/{" + LTI2Constants.TOOL_PROXY_GUID + "}");
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
			log.warn("General LTI2 Failure URI={} IP={}", uri, ipAddress);
			log.error(e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); 
			doErrorJSON(request, response, null, "General failure", e);
		}
	}

	@SuppressWarnings("unchecked")
	protected void doRequest(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException 
	{
		String ipAddress = request.getRemoteAddr();
		log.debug("LTI Service request from IP={}", ipAddress);

		String rpi = request.getPathInfo();
		String uri = request.getRequestURI();
		String [] parts = uri.split("/");
		if ( parts.length < 4 ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST); 
			doErrorJSON(request, response, null, "Incorrect url format", null);
			return;
		}
		String controller = parts[3];
		if ( SVC_tc_profile.equals(controller) && parts.length == 5 ) {
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
			log.debug(jsonRequest.getPostBody());
		}

		response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED); 
		log.warn("Unknown request={}", uri);
		doErrorJSON(request, response, null, "Unknown request="+uri, null);
	}

	protected void getToolConsumerProfile(HttpServletRequest request, 
			HttpServletResponse response,String profile_id)
	{
		Map<String,Object> deploy = ltiService.getDeployForConsumerKeyDao(profile_id);
		if ( deploy == null ) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND); 
			return;
		}

		ToolConsumer consumer = getToolConsumerProfile(deploy, profile_id);

		ObjectMapper mapper = new ObjectMapper();
		try {
			// http://stackoverflow.com/questions/6176881/how-do-i-make-jackson-pretty-print-the-json-content-it-generates
			ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
			// ***IMPORTANT!!!*** for Jackson 2.x use the line below instead of the one above: 
			// ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
			response.setContentType(APPLICATION_JSON);
			PrintWriter out = response.getWriter();
			out.println(writer.writeValueAsString(consumer));
			log.debug(writer.writeValueAsString(consumer));
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	protected ToolConsumer getToolConsumerProfile(Map<String, Object> deploy, String profile_id)
	{
		// Load the configuration data
		LTI2Config cnf = new SakaiLTI2Config();
		if ( cnf.getGuid() == null ) {
			log.error("*********************************************");
			log.error("* LTI2 NOT CONFIGURED - Using Sample Data   *");
			log.error("* Do not use this in production.  Test only *");
			log.error("*********************************************");
			// cnf = new org.tsugi.lti2.LTI2ConfigSample();
			cnf = new SakaiLTI2Base();
		}

		String serverUrl = SakaiBLTIUtil.getOurServerUrl();

		ToolConsumer consumer = new ToolConsumer(profile_id+"", resourceUrl+"#", cnf);
		consumer.allowSplitSecret();
		consumer.allowHmac256();
		consumer.addCapability(SakaiBLTIUtil.SAKAI_EXTENSIONS_ALL);
		consumer.addCapability(SakaiBLTIUtil.CANVAS_PLACEMENTS_COURSENAVIGATION);
		consumer.addCapability(SakaiBLTIUtil.CANVAS_PLACEMENTS_ASSIGNMENTSELECTION);
		// Not yet supported in Sakai
		// consumer.addCapability(SakaiBLTIUtil.CANVAS_PLACEMENTS_ACCOUNTNAVIGATION);

		if ( SakaiBLTIUtil.getLong(deploy.get(LTIService.LTI_ALLOWCONTENTITEM)) > 0 ) {
			consumer.addCapability(LTI2Messages.CONTENT_ITEM_SELECTION_REQUEST);
			// Not yet supported in Sakai
			// consumer.addCapability(SakaiBLTIUtil.SAKAI_CONTENTITEM_SELECTANY);
			consumer.addCapability(SakaiBLTIUtil.SAKAI_CONTENTITEM_SELECTFILE);
			consumer.addCapability(SakaiBLTIUtil.SAKAI_CONTENTITEM_SELECTLINK);
			consumer.addCapability(SakaiBLTIUtil.SAKAI_CONTENTITEM_SELECTIMPORT);
			consumer.addCapability(SakaiBLTIUtil.CANVAS_PLACEMENTS_LINKSELECTION);
			consumer.addCapability(SakaiBLTIUtil.CANVAS_PLACEMENTS_CONTENTIMPORT);
		}

		if (SakaiBLTIUtil.getLong(deploy.get(LTIService.LTI_SENDEMAILADDR)) > 0 ) {
			consumer.allowEmail();
		}

		if (SakaiBLTIUtil.getLong(deploy.get(LTIService.LTI_SENDNAME)) > 0 ) {
			consumer.allowName();
		}

		List<Service_offered> services = consumer.getService_offered();
		services.add(StandardServices.LTI2Registration(serverUrl + LTI2_PATH + SVC_tc_registration + "/" + profile_id));

		String allowOutcomes = ServerConfigurationService.getString(SakaiBLTIUtil.BASICLTI_OUTCOMES_ENABLED, SakaiBLTIUtil.BASICLTI_OUTCOMES_ENABLED_DEFAULT);
		if ("true".equals(allowOutcomes) && SakaiBLTIUtil.getLong(deploy.get(LTIService.LTI_ALLOWOUTCOMES)) > 0 ) {
			consumer.allowResult();

			services.add(LTI2ResultItem);
			services.add(StandardServices.LTI1Outcomes(serverUrl+LTI1_PATH));
			services.add(SakaiLTI2Services.BasicOutcomes(serverUrl+LTI1_PATH));
		}

		String allowRoster = ServerConfigurationService.getString(SakaiBLTIUtil.BASICLTI_ROSTER_ENABLED, SakaiBLTIUtil.BASICLTI_ROSTER_ENABLED_DEFAULT);
		if ("true".equals(allowRoster) && SakaiBLTIUtil.getLong(deploy.get(LTIService.LTI_ALLOWROSTER)) > 0 ) {
			services.add(SakaiLTI2Services.BasicRoster(serverUrl+LTI1_PATH));
		}

		String allowSettings = ServerConfigurationService.getString(SakaiBLTIUtil.BASICLTI_SETTINGS_ENABLED, SakaiBLTIUtil.BASICLTI_SETTINGS_ENABLED_DEFAULT);
		if ("true".equals(allowSettings) && SakaiBLTIUtil.getLong(deploy.get(LTIService.LTI_ALLOWSETTINGS)) > 0 ) {
			consumer.allowSettings();

			services.add(SakaiLTI2Services.BasicSettings(serverUrl+LTI1_PATH));
			services.add(LTI2LtiLinkSettings);
			services.add(LTI2ToolProxySettings);
			services.add(LTI2ToolProxyBindingSettings);
		}

		return consumer;
	}

	public void registerToolProviderProfile(HttpServletRequest request,HttpServletResponse response, 
			String profile_id) throws java.io.IOException
	{
		// Parse the JSON
		IMSJSONRequest jsonRequest = new IMSJSONRequest(request);

		if ( ! jsonRequest.valid ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, "Request is not in a valid format:"+jsonRequest.errorMessage, null);
			return;
		}

		Map<String,Object> deploy = ltiService.getDeployForConsumerKeyDao(profile_id);
		if ( deploy == null ) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND); 
			return;
		}
		Long deployKey = SakaiBLTIUtil.getLong(deploy.get(LTIService.LTI_ID));

		// See if we can even register...
		Long reg_state = SakaiBLTIUtil.getLong(deploy.get(LTIService.LTI_REG_STATE));
		String key = null;
		String secret = null;
		String new_secret = null;
		String ack = null;
		if ( reg_state == 0 ) {
			key = (String) deploy.get(LTIService.LTI_REG_KEY);
			secret = (String) deploy.get(LTIService.LTI_REG_PASSWORD);
		} else {
			key = (String) deploy.get(LTIService.LTI_CONSUMERKEY);
			secret = (String) deploy.get(LTIService.LTI_SECRET);
			secret = SakaiBLTIUtil.decryptSecret(secret);
			ack = request.getHeader("VND-IMS-CONFIRM-URL");
			if ( ack == null || ack.length() < 1 ) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				doErrorJSON(request, response, jsonRequest, "Re-registration requires VND-IMS-CONFIRM-URL header", null);
				return;
			}
		}

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

		ToolProxy toolProxy = null;
		try {
			toolProxy = new ToolProxy(jsonRequest.getPostBody());
			log.debug("OBJ: {}", toolProxy);
		} catch (Throwable t ) {
			log.error(t.getMessage(), t);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, "JSON parse failed", null);
			return;
		}


		JSONObject default_custom = toolProxy.getCustom();

		JSONObject security_contract = toolProxy.getSecurityContract();
		if ( security_contract == null  ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, "JSON missing security_contract", null);
			return;
		}

		String shared_secret = (String) security_contract.get(LTI2Constants.SHARED_SECRET);
		String tp_half_shared_secret = (String) security_contract.get(LTI2Constants.TP_HALF_SHARED_SECRET);
		String tc_half_shared_secret = null;

		if ( tp_half_shared_secret != null ) {
			if ( ! tp_half_shared_secret.matches("^[a-f0-9]*$") ) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				doErrorJSON(request, response, jsonRequest, "tp_half_shared secret lower-case hex only", null);
				return;
			}
			if ( tp_half_shared_secret.length() != 128 ) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				doErrorJSON(request, response, jsonRequest, "tp_half_shared secret must be 128 characters", null);
				return;
			}
			SecureRandom random = new SecureRandom();
			byte bytes[] = new byte[512/8];
			random.nextBytes(bytes);
			tc_half_shared_secret =  PortableShaUtil.bin2hex(bytes);
			if ( shared_secret != null ) security_contract.put(LTI2Constants.SHARED_SECRET, "*********");
			shared_secret = tc_half_shared_secret + tp_half_shared_secret;
			security_contract.put(LTI2Constants.TP_HALF_SHARED_SECRET, "*********");
		} else {
			if ( shared_secret != null ) security_contract.put(LTI2Constants.SHARED_SECRET, "*********");
			if ( tp_half_shared_secret != null ) security_contract.put(LTI2Constants.TP_HALF_SHARED_SECRET, "*********");
		}

		if ( shared_secret == null  ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, "JSON missing shared_secret", null);
			return;
		}

		// Make sure that the requested services are a subset of the offered services
		ToolConsumer consumer = getToolConsumerProfile(deploy, profile_id);

		JSONArray tool_services = (JSONArray) security_contract.get(LTI2Constants.TOOL_SERVICE);
		String retval = toolProxy.validateServices(consumer);
		if ( retval != null ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, retval, null);
			return;
		}

		// Parse the tool profile bit and extract the tools with error checking
		retval = toolProxy.validateCapabilities(consumer);
		if ( retval != null ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, retval, null);
			return;
		}

		// Passed all the tests, lets commit this...
		Map<String, Object> deployUpdate = new TreeMap<String, Object> ();
		shared_secret = SakaiBLTIUtil.encryptSecret(shared_secret);
		if ( reg_state == 0 ) {
			deployUpdate.put(LTIService.LTI_SECRET, shared_secret);
		} else {
			// In Re-Registration, the new secret is not committed until Activation
			deployUpdate.put(LTIService.LTI_NEW_SECRET, shared_secret);
		}

		// Indicate ready to validate and kill the interim info
		deployUpdate.put(LTIService.LTI_REG_STATE, LTIService.LTI_REG_STATE_REGISTERED);
		deployUpdate.put(LTIService.LTI_REG_KEY, "");
		deployUpdate.put(LTIService.LTI_REG_ACK, ack);
		deployUpdate.put(LTIService.LTI_REG_PASSWORD, "");
		if ( default_custom != null ) deployUpdate.put(LTIService.LTI_SETTINGS, default_custom.toString());
		deployUpdate.put(LTIService.LTI_REG_PROFILE, toolProxy.toString());

		log.debug("deployUpdate={}", deployUpdate);

		Object obj = ltiService.updateDeployDao(deployKey, deployUpdate);
		boolean success = ( obj instanceof Boolean ) && ( (Boolean) obj == Boolean.TRUE);
		if ( ! success ) {
			log.warn("updateDeployDao fail deployKey={}\nretval={}\ndata={}", deployKey, obj, deployUpdate);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			doErrorJSON(request, response, jsonRequest, "Failed update of deployment="+deployKey, null);
			return;
		}

		// Share our happiness with the Tool Provider
		Map jsonResponse = new TreeMap();
		jsonResponse.put(LTI2Constants.CONTEXT,StandardServices.TOOLPROXY_ID_CONTEXT);
		jsonResponse.put(LTI2Constants.TYPE, StandardServices.TOOLPROXY_ID_TYPE);
		String serverUrl = SakaiBLTIUtil.getOurServerUrl();
		jsonResponse.put(LTI2Constants.JSONLD_ID, resourceUrl + SVC_tc_registration + "/" +profile_id);
		jsonResponse.put(LTI2Constants.TOOL_PROXY_GUID, profile_id);
		// TODO: Check if this is needed in LTI 2.1
		// jsonResponse.put(LTI2Constants.CUSTOM_URL, resourceUrl + SVC_Settings + "/" + LTI2Util.SCOPE_ToolProxy + "/" +profile_id);
		if ( tc_half_shared_secret != null ) jsonResponse.put(LTI2Constants.TC_HALF_SHARED_SECRET, tc_half_shared_secret);
		response.setContentType(StandardServices.TOOLPROXY_ID_FORMAT);
		response.setStatus(HttpServletResponse.SC_CREATED);
		String jsonText = JSONValue.toJSONString(jsonResponse);
		log.debug(jsonText);
		PrintWriter out = response.getWriter();
		out.println(jsonText);
	}

	public void handleResultRequest(HttpServletRequest request,HttpServletResponse response, 
			String sourcedid) throws java.io.IOException
	{
		String allowOutcomes = ServerConfigurationService.getString(SakaiBLTIUtil.BASICLTI_OUTCOMES_ENABLED, SakaiBLTIUtil.BASICLTI_OUTCOMES_ENABLED_DEFAULT);
		if ( ! "true".equals(allowOutcomes) ) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			doErrorJSON(request,response, null, "Result resources not available", null);
			return;
		}

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
			log.debug(jsonText);
			PrintWriter out = response.getWriter();
			out.println(jsonText);
		} else if ( "PUT".equals(request.getMethod()) ) { 
			retval = "Error parsing input data";
			try {
				jsonRequest = new IMSJSONRequest(request);
				JSONObject requestData = (JSONObject) JSONValue.parse(jsonRequest.getPostBody());
				String comment = (String) requestData.get(LTI2Constants.COMMENT);
				JSONObject resultScore = (JSONObject) requestData.get(LTI2Constants.RESULTSCORE);
				Object oGrade = resultScore.get(LTI2Constants.VALUE);
				Double dGrade = null;
				if ( oGrade instanceof String ) {
					dGrade = new Double((String) oGrade);
				} else if ( oGrade instanceof Number ) {
					dGrade = (Double) oGrade;
				}
				if ( dGrade != null ) {
					retval = SakaiBLTIUtil.setGrade(sourcedid, request, ltiService, dGrade, comment);
				} else { 
					retval = "Unable to parse grade="+oGrade;
				}
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
	}

	// If this code looks like a hack - it is because the spec is a hack.
	// There are five possible scenarios for GET and two possible scenarios
	// for PUT.  I begged to simplify the business logic but was overrulled.
	// So we write obtuse code.
	public void handleSettingsRequest(HttpServletRequest request,HttpServletResponse response, 
			String[] parts) throws java.io.IOException
	{
		String allowSettings = ServerConfigurationService.getString(SakaiBLTIUtil.BASICLTI_SETTINGS_ENABLED, SakaiBLTIUtil.BASICLTI_SETTINGS_ENABLED_DEFAULT);
		if ( ! "true".equals(allowSettings) ) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			doErrorJSON(request,response, null, "Tool settings not available", null);
			return;
		}

		String URL = SakaiBLTIUtil.getOurServletPath(request);
		String scope = parts[4];

		// Check to see if we are doing the bubble
		String bubbleStr = request.getParameter("bubble");
		String acceptHdr = request.getHeader("Accept");
		String contentHdr = request.getContentType();
		log.debug("accept={} bubble={}", acceptHdr, bubbleStr);

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
		log.debug("as={} ac={} is={} ic={}", acceptSimple, acceptComplex, inputSimple, inputComplex);

		// Check the JSON on PUT and check the oauth_body_hash
		IMSJSONRequest jsonRequest = null;
		JSONObject requestData = null;
		if ( "PUT".equals(request.getMethod()) ) {
			try {
				jsonRequest = new IMSJSONRequest(request);
				log.debug("Settings PUT {}", jsonRequest.getPostBody());
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
			log.debug("placement_id={}", placement_id);
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
			if (SakaiBLTIUtil.getLong(content.get(LTIService.LTI_ALLOWOUTCOMES)) > 0 ||
				SakaiBLTIUtil.getLong(tool.get(LTIService.LTI_ALLOWOUTCOMES)) > 0 ) {
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
		} else {
			if ( tool == null ) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				doErrorJSON(request,response, jsonRequest, "Bad tool item", null);
				return;
			}
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
			if (SakaiBLTIUtil.getLong(deploy.get(LTIService.LTI_ALLOWOUTCOMES)) > 0 ) {
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
			link_settings = LTI2Util.parseSettings((String) content.get(LTIService.LTI_SETTINGS));
		}
		if ( proxyBinding != null ) {
			binding_settings = LTI2Util.parseSettings((String) proxyBinding.get(LTIService.LTI_SETTINGS));
		}
		if ( deploy != null ) {
			proxy_settings = LTI2Util.parseSettings((String) deploy.get(LTIService.LTI_SETTINGS));
		}

/*
		if ( distinct && link_settings != null && scope.equals(LTI2Util.SCOPE_LtiLink) ) {
			Iterator i = link_settings.keySet().iterator();
			while ( i.hasNext() ) {
				String key = (String) i.next();
				if ( binding_settings != null ) binding_settings.remove(key);
				if ( proxy_settings != null ) proxy_settings.remove(key);
			}
		}

		if ( distinct && binding_settings != null && scope.equals(LTI2Util.SCOPE_ToolProxyBinding) ) {
			Iterator i = binding_settings.keySet().iterator();
			while ( i.hasNext() ) {
				String key = (String) i.next();
				if ( proxy_settings != null ) proxy_settings.remove(key);
			}
		}
*/

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

		// Make sure we have a key and secret
		if ( oauth_secret == null || consumer_key == null ) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			doErrorJSON(request,response, jsonRequest, "Key or secret is null, key="+consumer_key, null);
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
			log.debug("jsonResponse={}", jsonResponse);
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
					log.info("inserted ProxyBinding setting={}", proxyBindingNew);
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
	}

	/* IMS JSON version of Errors */
	public void doErrorJSON(HttpServletRequest request,HttpServletResponse response, 
			IMSJSONRequest json, String message, Exception e) 
		throws java.io.IOException 
		{
			if (e != null) {
				log.error(e.getLocalizedMessage(), e);
			}
			log.info(message);
			if ( json != null ) log.info(json.postBody);

			String jsonText = IMSJSONRequest.doErrorJSON(request, response, json, message, e);
			log.info(jsonText);
		}

	public void destroy() {
	}

}
