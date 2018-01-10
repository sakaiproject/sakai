/*
 * $URL$
 * $Id$
 *
 * Copyright (c) 2015- Charles R. Severance
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.tsugi.basiclti.BasicLTIUtil;
import org.tsugi.lti2.objects.Service_offered;
import org.tsugi.lti2.objects.StandardServices;
import org.tsugi.lti2.objects.ToolConsumer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import static org.tsugi.lti2.LTI2Util.getArray;
import static org.tsugi.lti2.LTI2Util.getObject;
import static org.tsugi.lti2.LTI2Util.getString;
import static org.tsugi.lti2.LTI2Util.compareServiceIds;

@Slf4j
public class ToolProxy {

	private JSONObject toolProxy = null;

	/**
	 * We check for the fields essential for the class to operate here.
	 */
	public ToolProxy(String tool_proxy)
	{
		if ( tool_proxy == null || tool_proxy.trim().length() < 1 ) {
			throw new java.lang.RuntimeException("Cannot initialize with empty string");
		}
		Object prox = JSONValue.parse(tool_proxy);
		if ( prox != null && prox instanceof JSONObject ) {
			toolProxy = (JSONObject) prox;
		} else {
			throw new java.lang.RuntimeException("tool proxy is wrong type "+prox.getClass().getName());
		}

		JSONObject tp = getToolProfile();
		if ( tp == null ) {
			throw new java.lang.RuntimeException("A tool_proxy must include a tool_profile");
		}
	}

	/**
	 * Return a string
	 */
	public String toString()
	{
		return toolProxy.toString();
	}

	/**
	 * Retrieve the parsed tool_proxy
	 */
	public JSONObject getToolProxy()
	{
		return toolProxy;
	}

	/**
	 * Retrieve the tool_profile from the tool_proxy
	 */
	public JSONObject getToolProfile()
	{
		return getObject(toolProxy, LTI2Constants.TOOL_PROFILE);
	}

	/**
	 * Retrieve the security_contract from the tool_proxy
	 */
	public JSONObject getSecurityContract()
	{
		return getObject(toolProxy, LTI2Constants.SECURITY_CONTRACT);
	}

	/**
	 * Retrieve the custom from the tool_proxy
	 */
	public JSONObject getCustom()
	{
		return getObject(toolProxy, LTI2Constants.CUSTOM);
	}

	/**
	 * Retrieve the resource_handlers from a tool_profile
	 */
	public JSONArray getResourceHandlers()
	{
		return getArray(getToolProfile(), LTI2Constants.RESOURCE_HANDLER);
	}

	/**
	 * Retrieve a global message type from  the ToolProxy
	 * 
	 * @param String messageType - Which message type you are looking for
	 */
	public JSONObject getMessageOfType(String messageType)
	{
		return getMessageOfType(getToolProfile(), messageType);
	}

	/**
	 * Retrieve a path for a message from  the ToolProxy
	 * 
	 * @param JSONObject message - The message entry
	 */
	public String getPathFromMessage(JSONObject message)
	{
		if ( message == null ) return null;
		String path = getString(message,"path");
		return path;
	}

	/**
	 * Retrieve a parameter bundle for a message from  the ToolProxy
	 * 
	 * @param JSONObject message - The message entry
	 */
	public JSONArray getParameterFromMessage(JSONObject message)
	{
		if ( message == null ) return null;
		JSONArray parameter = getArray(message,"parameter");
		return parameter;
	}

	/**
	 * Retrieve a particular message type from an individual resource handler
	 * 
	 * @param String resourceHandler - JSONObject for the resource_handler
	 * @param String messageType - Which message type you are looking for
	 */
	public JSONObject getMessageOfType(JSONObject resourceHandler, String messageType)
	{
	       if ( resourceHandler == null || messageType == null || messageType.length() < 1 ) {
			return null;
		}

		JSONArray messages = getArray(resourceHandler, LTI2Constants.MESSAGE);
		if ( messages == null ) return null;
		for ( Object m : messages ) {
			if ( ! (m instanceof JSONObject) ) return null;
			JSONObject message = (JSONObject) m;
			JSONArray message_type_array = getArray(message,LTI2Constants.MESSAGE_TYPE);
			if ( message_type_array == null ) continue;
			// String message_type = getString(message,LTI2Constants.MESSAGE_TYPE);
			for ( Object message_type : message_type_array ) {
				if ( ! (message_type instanceof String) ) continue;
				if ( ((String) message_type).equals(messageType) ) return message;
			}
		}
		return null;
	}

	/**
	 * Return all enabled capabilities
	 * 
	 * @param String resourceHandler - JSONObject for the resource_handler
	 * @param String messageType - Which message type you are looking for
	 * @return JSONArray An array of the enabled capabilities
	 */
	public JSONArray enabledCapabilities(JSONObject resourceHandler, String messageType)
	{
		JSONObject message = getMessageOfType(resourceHandler, messageType);
		JSONArray enabled_capability = getArray(message, LTI2Constants.ENABLED_CAPABILITY);
		if ( enabled_capability == null ) enabled_capability = new JSONArray();
		return enabled_capability;
	}

	/**
	 * Check if a particular capability is enabled
	 * 
	 * @param String resourceHandler - JSONObject for the resource_handler
	 * @param String messageType - Which message type you are looking for
	 * @param String capability - The capability to look for
	 */
	public boolean enabledCapability(JSONObject resourceHandler, String messageType, String capability)
	{
		JSONArray enabled_capability = enabledCapabilities(resourceHandler, messageType);
		if ( enabled_capability == null ) return false;
		return enabled_capability.contains(capability);
	}

	/**
	 * Extract an icon path by icon_style from an icon_info string
	 * 
	 * @param String resourceHandler - JSONObject for the resource_handler
	 * @param String icon_style - The style to look for
	 */
	public String getIconPath(JSONObject resourceHandler, String icon_style) {
		JSONArray icon_info = getArray(resourceHandler, LTI2Constants.ICON_INFO);
		if ( icon_info == null ) return null;

		for (Object m : icon_info) {
			if ( ! ( m instanceof JSONObject) ) continue;
			JSONObject jm = (JSONObject) m;
			JSONArray icon_styles = getArray(jm, LTI2Constants.ICON_STYLE);
			if ( icon_styles == null ) continue;
			if ( ! icon_styles.contains(icon_style) ) continue;
			JSONObject default_location = getObject(jm, LTI2Constants.DEFAULT_LOCATION);
			if ( default_location == null ) continue;
			String default_location_path = getString(default_location,LTI2Constants.PATH);
			if ( default_location_path == null ) continue;
			return default_location_path;
		}
		return null;
	}

	/**
	 * Validate our tool_services against a tool consumer
	 */
	public String validateServices(ToolConsumer consumer) 
	{
		// Mostly to catch casting errors from bad JSON
		try {
			JSONObject security_contract = (JSONObject) toolProxy.get(LTI2Constants.SECURITY_CONTRACT);
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
					// if ( service_id.equals(json_service) ) {
					if ( compareServiceIds(service_id,json_service) ) {
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

	/**
	 * Validate enabled_capabilities agains our ToolConsumer
	 */

	public String validateCapabilities(ToolConsumer consumer) 
	{
		List<Properties> theTools = new ArrayList<Properties> ();
		Properties info = new Properties();

		// Mostly to catch casting errors from bad JSON
		try {
			String retval = parseToolProfile(theTools, info);
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

	/**
	 * Parse a provider profile with lots of error checking...
	 */
	public String parseToolProfile(List<Properties> theTools, Properties info)
	{
		try {
			return parseToolProfileInternal(theTools, info);
		} catch (Exception e) {
			log.warn("Internal error parsing tool proxy\n{}", toolProxy.toString());
			log.error(e.getMessage(), e);
			return "Internal error parsing tool proxy:"+e.getLocalizedMessage();
		}
	}

	/**
	 * Parse a provider profile with lots of error checking...
	 */
	@SuppressWarnings("unused")
	private String parseToolProfileInternal(List<Properties> theTools, Properties info)
	{
		Object o = null;

		JSONObject tool_profile = getToolProfile();
		if ( tool_profile == null  ) {
			return "JSON missing tool_profile";
		}
	
		JSONObject product_instance = (JSONObject) tool_profile.get(LTI2Constants.PRODUCT_INSTANCE);
		if ( product_instance == null  ) {
			return "JSON missing product_instance";
		}

		String instance_guid = (String) product_instance.get(LTI2Constants.GUID);
		if ( instance_guid == null  ) {
			return "JSON missing product_info / guid";
		}
		info.put("instance_guid",instance_guid);

		JSONObject product_info = (JSONObject) product_instance.get(LTI2Constants.PRODUCT_INFO);
		if ( product_info == null  ) {
			return "JSON missing product_info";
		}

		// Look for required fields
		JSONObject product_name = getObject(product_info,LTI2Constants.PRODUCT_NAME);
		String productTitle = getString(product_name,LTI2Constants.DEFAULT_VALUE);
		JSONObject description = getObject(product_info,LTI2Constants.DESCRIPTION);
		String productDescription = getString(description,LTI2Constants.DEFAULT_VALUE);

		JSONObject product_family = getObject(product_info,LTI2Constants.PRODUCT_FAMILY);
		String productCode = getString(product_family,LTI2Constants.CODE);
		JSONObject product_vendor = getObject(product_family,LTI2Constants.VENDOR);
		description = getObject(product_vendor,LTI2Constants.DESCRIPTION);
		String vendorDescription = getString(description,LTI2Constants.DEFAULT_VALUE);
		String vendorCode = getString(product_vendor,LTI2Constants.CODE);

		if ( productTitle == null ) {
			return "JSON missing product_name";
		}
		if ( productCode == null || vendorCode == null ) {
			return "JSON missing product code or vendor code";
		}

		info.put("product_name", productTitle);
		if ( productDescription != null ) info.put("description", productDescription);  // Backwards compatibility
		if ( productDescription != null ) info.put("product_description", productDescription);
		info.put("product_code", productCode);
		info.put("vendor_code", vendorCode);
		if ( vendorDescription != null ) info.put("vendor_description", vendorDescription);

		JSONArray base_url_choices = getArray(tool_profile,LTI2Constants.BASE_URL_CHOICE);
		if ( base_url_choices == null  ) {
			return "JSON missing base_url_choices";
		}

		String secure_base_url = null;
		String default_base_url = null;
		for ( Object i : base_url_choices ) {
			JSONObject url_choice = (JSONObject) i;
			secure_base_url = (String) url_choice.get(LTI2Constants.SECURE_BASE_URL);
			default_base_url = (String) url_choice.get(LTI2Constants.DEFAULT_BASE_URL);
		}
		
		String launch_url = secure_base_url;
		if ( launch_url == null ) launch_url = default_base_url;
		if ( launch_url == null ) {
			return "Unable to determine launch URL";
		}

		JSONArray resource_handlers = getResourceHandlers();
		if ( resource_handlers == null  ) {
			return "JSON missing resource_handlers";
		}

		// Loop through resource handlers, read, and check for errors
		for(Object i : resource_handlers ) {
			JSONObject resource_handler = (JSONObject) i;
			JSONObject resource_type_json = (JSONObject) resource_handler.get(LTI2Constants.RESOURCE_TYPE);
			String resource_type_code = (String) resource_type_json.get(LTI2Constants.CODE);
			if ( resource_type_code == null ) {
				return "JSON missing resource_type code";
			}

			JSONArray messages = getArray(resource_handler, LTI2Constants.MESSAGE);
			if ( messages == null ) {
				return "JSON missing resource_handler / message";
			}

			JSONObject titleObject = (JSONObject) resource_handler.get(LTI2Constants.RESOURCE_NAME);
			String title = titleObject == null ? null : (String) titleObject.get(LTI2Constants.DEFAULT_VALUE);
			if ( title == null || titleObject == null ) {
				return "JSON missing resource_handler / resource_name / default_value";
			}

			JSONObject buttonObject = (JSONObject) resource_handler.get(LTI2Constants.SHORT_NAME);
			String button = buttonObject == null ? null : (String) buttonObject.get(LTI2Constants.DEFAULT_VALUE);
		
			JSONObject descObject = (JSONObject) resource_handler.get(LTI2Constants.DESCRIPTION);
			String resourceDescription = getString(descObject, LTI2Constants.DEFAULT_VALUE);

			// Simplify the icon data structure
			JSONArray iconInfo = getArray(resource_handler,LTI2Constants.ICON_INFO);

			String path = null;
			JSONArray parameter = null;
			JSONArray enabled_capability = null; 
			for ( Object m : messages ) {
				JSONObject message = (JSONObject) m;
				String message_type = (String) message.get(LTI2Constants.MESSAGE_TYPE);
				if ( LTI2Messages.BASIC_LTI_LAUNCH_REQUEST.equals(message_type) || 
				     LTI2Messages.CONTENT_ITEM_SELECTION_REQUEST.equals(message_type) ) {
					path = (String) message.get(LTI2Constants.PATH);
					if ( path == null ) {
						return "A launch message must have a path RT="+resource_type_code;
					} 

					// Check the URI
					String thisLaunch = launch_url;
					thisLaunch = thisLaunch + path;
					try {
						URL url = new URL(thisLaunch);
					} catch ( Exception e ) {
						return "Bad launch URL="+thisLaunch;
					}

					parameter = getArray(message,LTI2Constants.PARAMETER);
					enabled_capability = getArray(message, LTI2Constants.ENABLED_CAPABILITY);

					// Passed all the tests...  Lets keep it...
					Properties theTool = new Properties();

					theTool.put("resource_type", resource_type_code); // Backwards compatibility
					theTool.put("resource_type_code", resource_type_code);
					if ( title == null ) title = productTitle;
					if ( title != null ) theTool.put("title", title);
					if ( button != null ) theTool.put("button", button);
					if ( resourceDescription == null ) resourceDescription = productDescription;
					if ( resourceDescription != null ) theTool.put("description", resourceDescription);
					if ( parameter != null ) theTool.put(LTI2Constants.PARAMETER, parameter.toString());
					if ( iconInfo != null ) theTool.put(LTI2Constants.ICON_INFO,iconInfo.toString());
					theTool.put(LTI2Constants.ENABLED_CAPABILITY, enabled_capability.toString());
					theTool.put("launch", thisLaunch);
					if ( secure_base_url != null ) theTool.put("secure_base_url", secure_base_url);
					if ( default_base_url != null ) theTool.put("default_base_url", default_base_url);

					// Guess the Placement given the message type
					if ( LTI2Messages.BASIC_LTI_LAUNCH_REQUEST.equals(message_type) ){
						 theTool.put("pl_launch","1");
					}
					if (LTI2Messages.CONTENT_ITEM_SELECTION_REQUEST.equals(message_type)) {
						theTool.put("pl_linkselection","1");
					}

					// Turn the tool_proxy into a tool_proxy_binding by having a single 
					// resource_handler that is the one that corresponds to this tool.
					// and a single message that corresponds to this message
					ToolProxy local_tool_proxy = new ToolProxy(toolProxy.toString());
					JSONObject local_tool_profile = local_tool_proxy.getToolProfile();
					local_tool_profile.remove(LTI2Constants.RESOURCE_HANDLER);

					JSONObject local_resource_handler = (JSONObject) JSONValue.parse(resource_handler.toString());
					local_resource_handler.remove(LTI2Constants.MESSAGE);

					JSONArray local_messages = new JSONArray();
					local_messages.add(message);
					local_resource_handler.put(LTI2Constants.MESSAGE, local_messages);

					JSONArray handlers = new JSONArray ();
					handlers.add(local_resource_handler);
					local_tool_profile.put(LTI2Constants.RESOURCE_HANDLER, handlers);

					// With a single resource handler it is now a TPB
					ToolProxyBinding tool_proxy_binding = new ToolProxyBinding(local_tool_proxy.toString());
					theTool.put(LTI2Constants.TOOL_PROXY_BINDING, tool_proxy_binding.toString());
					theTools.add(theTool);

				} else if ( LTI2Messages.TOOLPROXY_REGISTRATION_REQUEST.equals(message_type) || 
				     LTI2Messages.TOOLPROXY_RE_REGISTRATION_REQUEST.equals(message_type) ) {
                                        continue;
				} else {
					return "Only "+LTI2Messages.BASIC_LTI_LAUNCH_REQUEST+" and "+LTI2Messages.CONTENT_ITEM_SELECTION_REQUEST+ " are allowed message_types RT="+resource_type_code;
				}
			}

		}
		return null;  // All good
	}

}
