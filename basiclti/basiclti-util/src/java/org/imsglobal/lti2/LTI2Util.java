/*
 * $URL$
 * $Id$
 *
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
 */

package org.imsglobal.lti2;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.imsglobal.basiclti.BasicLTIUtil;
import org.imsglobal.lti2.objects.Service_offered;
import org.imsglobal.lti2.objects.StandardServices;
import org.imsglobal.lti2.objects.ToolConsumer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class LTI2Util {

	// We use the built-in Java logger because this code needs to be very generic
	private static Logger M_log = Logger.getLogger(LTI2Util.class.toString());

	public static final String SCOPE_LtiLink = "LtiLink";
	public static final String SCOPE_ToolProxyBinding = "ToolProxyBinding";
	public static final String SCOPE_ToolProxy = "ToolProxy";

    private static final String EMPTY_JSON_OBJECT = "{\n}\n";

	// Validate the incoming tool_services against a tool consumer
	public static String validateServices(ToolConsumer consumer, JSONObject providerProfile) 
	{
		// Mostly to catch casting errors from bad JSON
		try {
			JSONObject security_contract = (JSONObject) providerProfile.get(LTI2Constants.SECURITY_CONTRACT);
			if ( security_contract == null  ) {
				return "JSON missing security_contract";
			}
			JSONArray tool_services = (JSONArray) security_contract.get(LTI2Constants.TOOL_SERVICE);

			List<Service_offered> services_offered = consumer.getService_offered();

			if ( tool_services != null ) for (Object o : tool_services) {
				JSONObject tool_service = (JSONObject) o;
				String json_service = (String) tool_service.get(LTI2Constants.SERVICE);

				boolean found = false;
				for (Service_offered service : services_offered ) {
					String service_id = service.get_id();
					if ( service_id.equals(json_service) ) {
						found = true;
						break;
					}
				}
				if ( ! found ) return "Service not allowed: "+json_service;
			}
			return null;
		}
		catch (Exception e) {
			return "Exception:"+ e.getLocalizedMessage();
		}
	}

	// Validate incoming capabilities requested against our ToolConsumer
	public static String validateCapabilities(ToolConsumer consumer, JSONObject providerProfile) 
	{
		List<Properties> theTools = new ArrayList<Properties> ();
		Properties info = new Properties();

		// Mostly to catch casting errors from bad JSON
		try {
			String retval = parseToolProfile(theTools, info, providerProfile);
			if ( retval != null )  return retval;

			if ( theTools.size() < 1 ) return "No tools found in profile";

			// Check all the capabilities requested by all the tools comparing against consumer
			List<String> capabilities = consumer.getCapability_offered();
			for ( Properties theTool : theTools ) {
				String ec = (String) theTool.get(LTI2Constants.ENABLED_CAPABILITY);
				JSONArray enabled_capability = (JSONArray) JSONValue.parse(ec);
				if ( enabled_capability != null ) for (Object o : enabled_capability) {
					ec = (String) o;
					if ( capabilities.contains(ec) ) continue;
					return "Capability not permitted="+ec;
				}
			}
			return null;
		}
		catch (Exception e ) {
			return "Exception:"+ e.getLocalizedMessage();
		}
	}

	// If this code looks like a hack - it is because the spec is a hack.
	// There are five possible scenarios for GET and two possible scenarios
	// for PUT.  I begged to simplify the business logic but was overrulled.
	// So we write obtuse code.
	@SuppressWarnings({ "unchecked", "unused" })
	public static Object getSettings(HttpServletRequest request, String scope,
		JSONObject link_settings, JSONObject binding_settings, JSONObject proxy_settings,
		String link_url, String binding_url, String proxy_url)
	{
		// Check to see if we are doing the bubble
		String bubbleStr = request.getParameter("bubble");
		String acceptHdr = request.getHeader("Accept");
		String contentHdr = request.getContentType();

		if ( bubbleStr != null && bubbleStr.equals("all") &&
			acceptHdr.indexOf(StandardServices.TOOLSETTINGS_FORMAT) < 0 ) {
			return "Simple format does not allow bubble=all";
		}

		if ( SCOPE_LtiLink.equals(scope) || SCOPE_ToolProxyBinding.equals(scope) 
			|| SCOPE_ToolProxy.equals(scope) ) {
			// All good
		} else {
			return "Bad Setttings Scope="+scope;
		}

		boolean bubble = bubbleStr != null && "GET".equals(request.getMethod());
		boolean distinct = bubbleStr != null && "distinct".equals(bubbleStr);
		boolean bubbleAll = bubbleStr != null && "all".equals(bubbleStr);

		// Check our output format
		boolean acceptComplex = acceptHdr == null || acceptHdr.indexOf(StandardServices.TOOLSETTINGS_FORMAT) >= 0;

		if ( distinct && link_settings != null && scope.equals(SCOPE_LtiLink) ) {
			Iterator<String> i = link_settings.keySet().iterator();
			while ( i.hasNext() ) {
				String key = (String) i.next();
				if ( binding_settings != null ) binding_settings.remove(key);
				if ( proxy_settings != null ) proxy_settings.remove(key);
			}
		}

		if ( distinct && binding_settings != null && scope.equals(SCOPE_ToolProxyBinding) ) {
			Iterator<String> i = binding_settings.keySet().iterator();
			while ( i.hasNext() ) {
				String key = (String) i.next();
				if ( proxy_settings != null ) proxy_settings.remove(key);
			}
		}

		// Lets get this party started...
		JSONObject jsonResponse =  null;
		if ( (distinct || bubbleAll) && acceptComplex ) { 
			jsonResponse = new JSONObject();	
			jsonResponse.put(LTI2Constants.CONTEXT,StandardServices.TOOLSETTINGS_CONTEXT);
			JSONArray graph = new JSONArray();
			boolean started = false;
			if ( link_settings != null && SCOPE_LtiLink.equals(scope) ) {
				JSONObject cjson = new JSONObject();
				cjson.put(LTI2Constants.JSONLD_ID,link_url);
				cjson.put(LTI2Constants.TYPE,SCOPE_LtiLink);
				cjson.put(LTI2Constants.CUSTOM,link_settings);
				graph.add(cjson);
				started = true;
			} 
			if ( binding_settings != null && ( started || SCOPE_ToolProxyBinding.equals(scope) ) ) {
				JSONObject cjson = new JSONObject();
				cjson.put(LTI2Constants.JSONLD_ID,binding_url);
				cjson.put(LTI2Constants.TYPE,SCOPE_ToolProxyBinding);
				cjson.put(LTI2Constants.CUSTOM,binding_settings);
				graph.add(cjson);
				started = true;
			} 
			if ( proxy_settings != null && ( started || SCOPE_ToolProxy.equals(scope) ) ) {
				JSONObject cjson = new JSONObject();
				cjson.put(LTI2Constants.JSONLD_ID,proxy_url);
				cjson.put(LTI2Constants.TYPE,SCOPE_ToolProxy);
				cjson.put(LTI2Constants.CUSTOM,proxy_settings);
				graph.add(cjson);
			}
			jsonResponse.put(LTI2Constants.GRAPH,graph);

		} else if ( distinct ) {  // Simple format output
			jsonResponse = proxy_settings;
			if ( SCOPE_LtiLink.equals(scope) ) {
				jsonResponse.putAll(binding_settings);
				jsonResponse.putAll(link_settings);
			} else if ( SCOPE_ToolProxyBinding.equals(scope) ) {
				jsonResponse.putAll(binding_settings);
			}
		} else { // bubble not specified
			jsonResponse = new JSONObject();	
			jsonResponse.put(LTI2Constants.CONTEXT,StandardServices.TOOLSETTINGS_CONTEXT);
			JSONObject theSettings = null;
			String endpoint = null;
			if ( SCOPE_LtiLink.equals(scope) ) {
				endpoint = link_url;
				theSettings = link_settings;
			} else if ( SCOPE_ToolProxyBinding.equals(scope) ) {
				endpoint = binding_url;
				theSettings = binding_settings;
			} 
			if ( SCOPE_ToolProxy.equals(scope) ) {
				endpoint = proxy_url;
				theSettings = proxy_settings;
			}
			if ( acceptComplex ) {
				JSONArray graph = new JSONArray();
				JSONObject cjson = new JSONObject();
				cjson.put(LTI2Constants.JSONLD_ID,endpoint);
				cjson.put(LTI2Constants.TYPE,scope);
				cjson.put(LTI2Constants.CUSTOM,theSettings);
				graph.add(cjson);
				jsonResponse.put(LTI2Constants.GRAPH,graph);
			} else {
				jsonResponse = theSettings;
			}
		}
		return jsonResponse;
	}

	// Parse a provider profile with lots of error checking...
	public static String parseToolProfile(List<Properties> theTools, Properties info, JSONObject jsonObject)
	{
		try {
			return parseToolProfileInternal(theTools, info, jsonObject);
		} catch (Exception e) {
			M_log.warning("Internal error parsing tool proxy\n"+jsonObject.toString());
			e.printStackTrace();
			return "Internal error parsing tool proxy:"+e.getLocalizedMessage();
		}
	}

	// Parse a provider profile with lots of error checking...
	public static JSONArray forceArray(Object obj) 
	{
		if ( obj == null ) return null;
		if ( obj instanceof JSONArray ) return (JSONArray) obj;
		JSONArray retval = new JSONArray();
		retval.add(obj);
		return retval;
	}

	// Return a JSONArray or null.  Promote a JSONObject to an array
	public static JSONArray getArray(JSONObject obj, String key)
	{
		if ( obj == null ) return null;
		Object o = obj.get(key);
		if ( o == null ) return null;
		if ( o instanceof JSONArray ) return (JSONArray) o;
		if ( o instanceof JSONObject ) {
			JSONArray retval = new JSONArray();
			retval.add(o);
			return retval;
		}
		return null;
	}

	// Return a JSONObject or null
	public static JSONObject getObject(JSONObject obj, String key)
	{
		if ( obj == null ) return null;
		Object o = obj.get(key);
		if ( o == null ) return null;
		if ( o instanceof JSONObject ) return (JSONObject) o;
		return null;
	}

	// Return a String or null
	public static String getString(JSONObject obj, String key)
	{
		if ( obj == null ) return null;
		Object o = obj.get(key);
		if ( o == null ) return null;
		if ( o instanceof String ) return (String) o;
		return null;
	}

	// Parse a provider profile with lots of error checking...
	@SuppressWarnings("unused")
	private static String parseToolProfileInternal(List<Properties> theTools, Properties info, JSONObject jsonObject)
	{
		Object o = null;
		JSONArray overall_enabled_capability = getArray(jsonObject, LTI2Constants.ENABLED_CAPABILITY);
		if ( overall_enabled_capability == null  ) {
			overall_enabled_capability = new JSONArray ();	
		}

		JSONObject tool_profile = (JSONObject) jsonObject.get("tool_profile");
		if ( tool_profile == null  ) {
			return "JSON missing tool_profile";
		}
		JSONObject product_instance = (JSONObject) tool_profile.get("product_instance");
		if ( product_instance == null  ) {
			return "JSON missing product_instance";
		}

		String instance_guid = (String) product_instance.get("guid");
		if ( instance_guid == null  ) {
			return "JSON missing product_info / guid";
		}
		info.put("instance_guid",instance_guid);

		JSONObject product_info = (JSONObject) product_instance.get("product_info");
		if ( product_info == null  ) {
			return "JSON missing product_info";
		}

		// Look for required fields
		JSONObject product_name = product_info == null ? null : (JSONObject) product_info.get("product_name");
		String productTitle = product_name == null ? null : (String) product_name.get("default_value");
		JSONObject description = product_info == null ? null : (JSONObject) product_info.get("description");
		String productDescription = description == null ? null : (String) description.get("default_value");

		JSONObject product_family = product_info == null ? null : (JSONObject) product_info.get("product_family");
		String productCode = product_family == null ? null : (String) product_family.get("code");
		JSONObject product_vendor = product_family == null ? null : (JSONObject) product_family.get("vendor");
		description = product_vendor == null ? null : (JSONObject) product_vendor.get("description");
		String vendorDescription = description == null ? null : (String) description.get("default_value");
		String vendorCode = product_vendor == null ? null : (String) product_vendor.get("code");

		if ( productTitle == null || productDescription == null ) {
			return "JSON missing product_name or description ";
		}
		if ( productCode == null || vendorCode == null || vendorDescription == null ) {
			return "JSON missing product code, vendor code or description";
		}

		info.put("product_name", productTitle);
		info.put("description", productDescription);  // Backwards compatibility
		info.put("product_description", productDescription);
		info.put("product_code", productCode);
		info.put("vendor_code", vendorCode);
		info.put("vendor_description", vendorDescription);

		JSONArray base_url_choices = getArray(tool_profile,"base_url_choice");
		if ( base_url_choices == null  ) {
			return "JSON missing base_url_choices";
		}

		String secure_base_url = null;
		String default_base_url = null;
		for ( Object i : base_url_choices ) {
			JSONObject url_choice = (JSONObject) i;
			secure_base_url = (String) url_choice.get("secure_base_url");
			default_base_url = (String) url_choice.get("default_base_url");
		}
		
		String launch_url = secure_base_url;
		if ( launch_url == null ) launch_url = default_base_url;
		if ( launch_url == null ) {
			return "Unable to determine launch URL";
		}

		o = (JSONArray) tool_profile.get("resource_handler");
		if ( ! (o instanceof JSONArray)|| o == null  ) {
			return "JSON missing resource_handlers";
		}
		JSONArray resource_handlers = (JSONArray) o;

		// Loop through resource handlers, read, and check for errors
		for(Object i : resource_handlers ) {
			JSONObject resource_handler = (JSONObject) i;
			JSONObject resource_type_json = (JSONObject) resource_handler.get("resource_type");
			String resource_type_code = (String) resource_type_json.get("code");
			if ( resource_type_code == null ) {
				return "JSON missing resource_type code";
			}

			JSONArray messages = getArray(resource_handler, "message");
			if ( messages == null ) {
				return "JSON missing resource_handler / message";
			}

			JSONObject titleObject = (JSONObject) resource_handler.get("resource_name");
			String title = titleObject == null ? null : (String) titleObject.get("default_value");
			if ( title == null || titleObject == null ) {
				return "JSON missing resource_handler / resource_name / default_value";
			}

			JSONObject buttonObject = (JSONObject) resource_handler.get("short_name");
			String button = buttonObject == null ? null : (String) buttonObject.get("default_value");
		
			JSONObject descObject = (JSONObject) resource_handler.get("description");
			String resourceDescription = getString(descObject, "default_value");

			// Simplify the icon data structure
			JSONArray iconInfo = getArray(resource_handler,"icon_info");

			String path = null;
			JSONArray parameter = null;
			JSONArray enabled_capability = null; 
			for ( Object m : messages ) {
				JSONObject message = (JSONObject) m;
				String message_type = (String) message.get("message_type");
				if ( ! "basic-lti-launch-request".equals(message_type) ) continue;
				if ( path != null ) {
					return "A resource_handler cannot have more than one basic-lti-launch-request message RT="+resource_type_code;
				}
				path = (String) message.get("path");
				if ( path == null ) {
					return "A basic-lti-launch-request message must have a path RT="+resource_type_code;
				} 
				parameter = getArray(message,"parameter");
				enabled_capability = getArray(message, LTI2Constants.ENABLED_CAPABILITY);
				Iterator<String> iterator = overall_enabled_capability.iterator();
				while (iterator.hasNext()) {
					String overall_capability = iterator.next();
					if ( ! enabled_capability.contains(overall_capability) ) {
						enabled_capability.add(overall_capability);
					}
				}
			}

			// Ignore everything except launch handlers
			if ( path == null ) continue;

			// Check the URI
			String thisLaunch = launch_url;
			thisLaunch = thisLaunch + path;
			try {
				URL url = new URL(thisLaunch);
			} catch ( Exception e ) {
				return "Bad launch URL="+thisLaunch;
			}

			// Passed all the tests...  Lets keep it...
			Properties theTool = new Properties();

			theTool.put("resource_type", resource_type_code); // Backwards compatibility
			theTool.put("resource_type_code", resource_type_code);
			if ( title == null ) title = productTitle;
			if ( title != null ) theTool.put("title", title);
			if ( button != null ) theTool.put("button", button);
			if ( resourceDescription == null ) resourceDescription = productDescription;
			if ( resourceDescription != null ) theTool.put("description", resourceDescription);
			if ( parameter != null ) theTool.put("parameter", parameter.toString());
			if ( iconInfo != null ) theTool.put("icon_info",iconInfo.toString());
			theTool.put(LTI2Constants.ENABLED_CAPABILITY, enabled_capability.toString());
			theTool.put("launch", thisLaunch);
			if ( secure_base_url != null ) theTool.put("secure_base_url", secure_base_url);
			if ( default_base_url != null ) theTool.put("default_base_url", default_base_url);
			theTools.add(theTool);
		}
		return null;  // All good
	}

	/**
	 * Check if a particular capability is enabled
	 * 
	 * @param String enabledCapabilityString - The string representation of the JSON
	 * array of capabilities.
	 * @param String capability - The capability to look for
	 */
	public static boolean enabledCapability(String enabledCapabilityString, String capability)
	{
		if ( enabledCapabilityString == null || capability == null ) return false;
		// For now just look for the string in quotes.
		return enabledCapabilityString.indexOf("\""+capability+"\"") >= 0 ;
	}

	/**
	 * Extract an icon path by icon_style from an icon_info string
	 * 
	 * @param String icon_info - The string representation of the icon_info
	 * @param String icon_style - The style to look for
	 */
	public static String getIconPath(String icon_info, String icon_style)
	{
		if ( icon_info == null || icon_info.length() < 1 ) {
			return null;
		}
		Object ii = JSONValue.parse(icon_info);
		if ( ii == null || ! (ii instanceof JSONArray) ) return null;
		JSONArray iconInfo = (JSONArray) ii;

		for (Object m : iconInfo) {
			if ( ! ( m instanceof JSONObject) ) continue;
			JSONObject jm = (JSONObject) m;
			JSONArray icon_styles = getArray(jm, "icon_style");
			if ( icon_styles == null ) continue;
			if ( ! icon_styles.contains(icon_style) ) continue;
			JSONObject default_location = getObject(jm, "default_location");
			if ( default_location == null ) continue;
			String default_location_path = getString(default_location,"path");
			if ( default_location_path == null ) continue;
			return default_location_path;
		}
		return null;
	}

	public static JSONObject parseSettings(String settings)
	{
		if ( settings == null || settings.length() < 1 ) {
			settings = EMPTY_JSON_OBJECT;
		}
		return (JSONObject) JSONValue.parse(settings);
	}

	/* Two possible formats:
		
		key=val
		key2=val2

		key=val;key2=val2;
	*/
	public static boolean mergeLTI1Custom(Properties custom, String customstr) 
	{
		if ( customstr == null || customstr.length() < 1 ) return true;

		String splitChar = "\n";
		if ( customstr.trim().indexOf("\n") == -1 ) splitChar = ";";
		String [] params = customstr.split(splitChar);
		for (int i = 0 ; i < params.length; i++ ) {
			String param = params[i];
			if ( param == null ) continue;
			if ( param.length() < 1 ) continue;

			int pos = param.indexOf("=");
			if ( pos < 1 ) continue;
			if ( pos+1 > param.length() ) continue;
			String key = mapKeyName(param.substring(0,pos));
			if ( key == null ) continue;

			if ( custom.containsKey(key) ) continue;

			String value = param.substring(pos+1);
			if ( value == null ) continue;
			value = value.trim();
			if ( value.length() < 1 ) continue;
			setProperty(custom, key, value);
		}
		return true;
	}

	/*
	  "custom" : 
	  {
		"isbn" : "978-0321558145",
		"style" : "jazzy"
	  }
	*/
	public static boolean mergeLTI2Custom(Properties custom, String customstr) 
	{
		if ( customstr == null || customstr.length() < 1 ) return true;
		JSONObject json = null;
		try {
			json = (JSONObject) JSONValue.parse(customstr.trim());
		} catch(Exception e) {
			M_log.warning("mergeLTI2Custom could not parse\n"+customstr);
			M_log.warning(e.getLocalizedMessage());
			return false;
		}

		// This could happen if the old settings service was used
		// on an LTI 2.x placement to put in settings that are not
		// JSON - we just ignore it.
		if ( json == null ) return false;
		Iterator<?> keys = json.keySet().iterator();
		while( keys.hasNext() ){
			String key = (String)keys.next();
			if ( custom.containsKey(key) ) continue;
			Object value = json.get(key);
			if ( value instanceof String ){
				setProperty(custom, key, (String) value);
			}
		}
		return true;
	}

	/*
	  "parameter" : 
	  [
		{ "name" : "result_url",
		  "variable" : "Result.url"
		},
		{ "name" : "discipline",
		  "fixed" : "chemistry"
		}
	  ]
	*/
	public static boolean mergeLTI2Parameters(Properties custom, String customstr) {
		if ( customstr == null || customstr.length() < 1 ) return true;
		JSONArray json = null;
		try {
			json = (JSONArray) JSONValue.parse(customstr.trim());
		} catch(Exception e) {
			M_log.warning("mergeLTI2Parameters could not parse\n"+customstr);
			M_log.warning(e.getLocalizedMessage());
			return false;
		}
		Iterator<?> parameters = json.iterator();
		while( parameters.hasNext() ){
			Object o = parameters.next();
			JSONObject parameter = null;
			try {
				parameter = (JSONObject) o;
			} catch(Exception e) {
				M_log.warning("mergeLTI2Parameters did not find list of objects\n"+customstr);
				M_log.warning(e.getLocalizedMessage());
				return false;
			}

			String name = (String) parameter.get("name");

			if ( name == null ) continue;
			if ( custom.containsKey(name) ) continue;
			String fixed = (String) parameter.get("fixed");
			String variable = (String) parameter.get("variable");
			if ( variable != null ) {
				setProperty(custom, name, variable);
				continue;
			}
			if ( fixed != null ) {
				setProperty(custom, name, fixed);
			}
		}
		return true;
	}

	public static void substituteCustom(Properties custom, Properties lti2subst) 
	{
		if ( custom == null || lti2subst == null ) return;	
		Enumeration<?> e = custom.propertyNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value =  custom.getProperty(key);
			if ( value == null || value.length() < 1 ) continue;
			String newValue = lti2subst.getProperty(value);
			if ( newValue == null ||  newValue.length() < 1 ) continue;
			setProperty(custom, key, (String) newValue);
		}
	}

	// Place the custom values into the launch
	public static void addCustomToLaunch(Properties ltiProps, Properties custom) 
	{
        Enumeration<?> e = custom.propertyNames();
        while (e.hasMoreElements()) {
            String keyStr = (String) e.nextElement();
            String value =  custom.getProperty(keyStr);
            setProperty(ltiProps,"custom_"+keyStr,value);
        }           
	}

	@SuppressWarnings("deprecation")
	public static void setProperty(Properties props, String key, String value) {
		BasicLTIUtil.setProperty(props, key, value);
	}

	public static String mapKeyName(String keyname) {
		return BasicLTIUtil.mapKeyName(keyname);
	}

}
