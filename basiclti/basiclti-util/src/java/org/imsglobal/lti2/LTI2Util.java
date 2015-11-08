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
import java.util.HashMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.imsglobal.basiclti.BasicLTIUtil;
import org.imsglobal.lti2.objects.StandardServices;
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
		while( parameters.hasNext() ) {
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
