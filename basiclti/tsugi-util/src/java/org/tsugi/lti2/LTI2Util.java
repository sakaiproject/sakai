/*
 * $URL$
 * $Id$
 *
 * Copyright (c) 2013- Charles R. Severance
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

package org.tsugi.lti2;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.tsugi.basiclti.BasicLTIUtil;
import org.tsugi.basiclti.BasicLTIConstants;
import org.tsugi.lti2.objects.StandardServices;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

@Slf4j
public class LTI2Util {

	public static final String SCOPE_LtiLink = "LtiLink";
	public static final String SCOPE_ToolProxyBinding = "ToolProxyBinding";
	public static final String SCOPE_ToolProxy = "ToolProxy";

	private static final String EMPTY_JSON_OBJECT = "{\n}\n";

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

		// These are likely needed in 2.1 but cause LTI 2.0 cert to fail the test - sigh
		// if (binding_settings != null ) binding_settings.put(LTI2Constants.JSONLD_ID,binding_url+"/custom");
		// if (link_settings != null ) link_settings.put(LTI2Constants.JSONLD_ID,link_url+"/custom");
		// if (proxy_settings != null ) proxy_settings.put(LTI2Constants.JSONLD_ID,proxy_url+"/custom");

		// These cause LTI 2.0 cert to fail the test
		if (binding_settings != null ) binding_settings.remove(LTI2Constants.JSONLD_ID);
		if (link_settings != null ) link_settings.remove(LTI2Constants.JSONLD_ID);
		if (proxy_settings != null ) proxy_settings.remove(LTI2Constants.JSONLD_ID);
		log.debug("link_settings={}", link_settings);
		log.debug("proxy_settings={}", proxy_settings);
		log.debug("binding_settings={}", binding_settings);

		// Lets get this party started...
		JSONObject jsonResponse = null;
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

		} else if ( distinct ) { // Simple format output
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
	public static JSONArray forceArray(Object obj) 
	{
		if ( obj == null ) return null;
		if ( obj instanceof JSONArray ) return (JSONArray) obj;
		JSONArray retval = new JSONArray();
		retval.add(obj);
		return retval;
	}

	// Return a JSONArray or null. Promote a JSONObject to an array
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

		// If this is a java.lang (i.e. String, Long, etc)
		String className = o.getClass().getName();
		if ( className.startsWith("java.lang") ) {
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

	/**
	 * Parse a string and reliably return a JSONObject or null
	 * 
	 * @param String toolProfileString - The string representation tool_profile.
	 * This has a single resource_handler object since this is scoped to a single
	 * tool.
	 */
	public static JSONObject parseJSONObject(String toolProfileString)
	{
		if ( toolProfileString == null || toolProfileString.length() < 1 ) {
			return null;
		}
		Object tp = JSONValue.parse(toolProfileString);
		if ( tp == null || ! (tp instanceof JSONObject) ) return null;
		return (JSONObject) tp;
	}

	public static JSONObject parseSettings(String settings)
	{
		if ( settings == null || settings.length() < 1 ) {
			settings = EMPTY_JSON_OBJECT;
		}
		return (JSONObject) JSONValue.parse(settings);
	}

	/**
	 * For LTI 2.x launches we do not support the semicolon-separated launches
	*/
	public static boolean mergeLTI1Custom(Properties custom, String customstr) 
	{
		if ( customstr == null || customstr.length() < 1 ) return true;

		String splitChar = "\n";
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
			log.warn("mergeLTI2Custom could not parse\n{}", customstr);
			log.warn(e.getLocalizedMessage());
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
			log.warn("mergeLTI2Parameters could not parse\n{}", customstr);
			log.warn(e.getLocalizedMessage());
			return false;
		}
		Iterator<?> parameters = json.iterator();
		while( parameters.hasNext() ) {
			Object o = parameters.next();
			JSONObject parameter = null;
			try {
				parameter = (JSONObject) o;
			} catch(Exception e) {
				log.warn("mergeLTI2Parameters did not find list of objects\n{}", customstr);
				log.warn(e.getLocalizedMessage());
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
			// Allow both User.id and $User.id
			if ( value.startsWith("$") && value.length() > 1 ) {
				value = value.substring(1);
			}
			String newValue = lti2subst.getProperty(value);
			if ( newValue == null ||  newValue.length() < 1 ) continue;
			setProperty(custom, key, (String) newValue);
		}
	}

	// Place the custom values into the launch
	public static void addCustomToLaunch(Properties ltiProps, Properties custom, boolean isLTI1) 
	{
		Enumeration<?> e = custom.propertyNames();
		while (e.hasMoreElements()) {
			String keyStr = (String) e.nextElement();
			String value =  custom.getProperty(keyStr);
			setProperty(ltiProps,"custom_"+keyStr,value);
			String mapKeyStr = mapKeyName(keyStr);
			if ( isLTI1 && ! mapKeyStr.equals(keyStr) ) {
				setProperty(ltiProps,"custom_"+mapKeyStr,value);
			}
		}	
	}

	/**
	 * Make sure we never pass any un-requested LTI1 parameters to an LTI2 launch
	 *
	 * @param ltiProps - the properties as constructed by the LMS
	 * @param enabledCapabilities - the list of capabilities requested by the tool
	 * @param allowExt - indicates whether or not to allow the "ext_" prefixed parameters
         */
	public static void filterLTI1LaunchProperties(Properties ltiProps, 
		JSONArray enabledCapabilities, boolean allowExt) 
	{

		// Get the non-standard mappings
		Properties mapping = property2CapabilityMapping();

		// Loop through property names
		Properties oldProps = new Properties(ltiProps);
		Enumeration<?> e = oldProps.propertyNames();
		while (e.hasMoreElements()) {
			String keyStr = (String) e.nextElement();
			// Always allow this to happen
			if ( keyStr.equals(BasicLTIConstants.RESOURCE_LINK_ID) ) continue;
			if ( keyStr.equals(BasicLTIConstants.LTI_VERSION) ) continue;
			if ( allowExt && keyStr.startsWith("ext_") ) continue;

			String capStr = property2Capability(keyStr);
			String mapStr = mapping.getProperty(keyStr, null);
			if ( enabledCapabilities.contains(keyStr) || 
			     (capStr != null && enabledCapabilities.contains(capStr) ) || 
			     (mapStr != null && enabledCapabilities.contains(mapStr) ) ) {
				// Allowed to stay...
			} else {
				ltiProps.remove(keyStr);
			}
		}
	}

	public static String property2Capability(String propString) {
		if ( propString == null ) return null;
		propString = propString.trim();
		if ( propString.length() == 0 ) return "";

		// Handle cases like context_id => Context.id
		StringBuffer capStr = new StringBuffer();
		capStr.append(propString.substring(0,1).toUpperCase());
		for ( int i=1; i < propString.length(); i++ ) {
			char ch = propString.charAt(i);
			if ( ch == ' ' ) continue;
			if ( ch == '_') {
				capStr.append('.');
				continue;
			}
			capStr.append(ch);
		}
		return capStr.toString();
	}

	/* From Stephen Vickers - mod/lti/locallib.php in Moodle
	      'Context.id' => 'context_id',
	      'Context.type' => 'context_type',
	      'CourseSection.title' => 'context_title',
	      'CourseSection.label' => 'context_label',
	      'CourseSection.sourcedId' => 'lis_course_section_sourcedid',
	      'ResourceLink.id' => 'resource_link_id',
	      'ResourceLink.title' => 'resource_link_title',
	      'ResourceLink.description' => 'resource_link_description',
	      'User.id' => 'user_id',
	      'Person.name.full' => 'lis_person_name_full',
	      'Person.name.given' => 'lis_person_name_given',
	      'Person.name.family' => 'lis_person_name_family',
	      'Person.email.primary' => 'lis_person_contact_email_primary',
	      'Person.sourcedId' => 'lis_person_sourcedid',
	      'Membership.role' => 'roles',
	      'BasicOutcome.sourcedId' => 'lis_result_sourcedid',
	      'BasicOutcome.url' => 'lis_outcome_service_url'
	*/
	public static Properties property2CapabilityMapping() {
		Properties mapping = new Properties();
		mapping.setProperty("context_title", "CourseSection.title");
		mapping.setProperty("context_label", "CourseSection.label");
		mapping.setProperty("context_type", "Context.type");
		mapping.setProperty("lis_course_section_sourcedid", "CourseSection.sourcedId");
		mapping.setProperty("resource_link_id", "ResourceLink.id");
		mapping.setProperty("resource_link_title", "ResourceLink.title");
		mapping.setProperty("resource_link_description", "ResourceLink.description");
		mapping.setProperty("lis_person_name_full", "Person.name.full");
		mapping.setProperty("lis_person_name_given", "Person.name.given");
		mapping.setProperty("lis_person_name_family", "Person.name.family");
		mapping.setProperty("lis_person_contact_email_primary", "Person.email.primary");
		mapping.setProperty("lis_person_sourcedid", "Person.sourcedId");
		mapping.setProperty("lis_result_sourcedid", LTI2Vars.BASICOUTCOME_SOURCEDID);
		mapping.setProperty("lis_outcome_service_url", LTI2Vars.BASICOUTCOME_URL);
		mapping.setProperty("launch_presentation_locale", LTI2Vars.MESSAGE_LOCALE);
		mapping.setProperty("roles", "Membership.role");
		return mapping;
	}

	@SuppressWarnings("deprecation")
	public static void setProperty(Properties props, String key, String value) {
		BasicLTIUtil.setProperty(props, key, value);
	}

	public static String mapKeyName(String keyname) {
		return BasicLTIUtil.mapKeyName(keyname);
	}

        /**
         * Compare two service ids by suffix
         *
         * tcp:Result.item
         * #Result.item
         * http://sakai.ngrok.com/imsblis/lti2/#Result.item
         *
         * @return boolean True if they match.
         */
        public static boolean compareServiceIds(String id1, String id2)
        {
                if ( id1 == null && id2 == null ) return true;
                if ( id1 == null ) return false;
                if ( id2 == null ) return false;
		// Split on color or #
                String[] pieces1 = id1.split("[:#]");
                String[] pieces2 = id2.split("[:#]");
                if ( pieces1.length == 0 && pieces2.length == 0 ) return true;
                if ( pieces1.length == 0 ) return false;
                if ( pieces2.length == 0 ) return false;
                String last1 = pieces1[pieces1.length-1];
                String last2 = pieces2[pieces2.length-1];
                return last1.equals(last2);
        }

}
