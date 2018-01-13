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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ToolProxyBinding extends ToolProxy {

	private JSONObject resourceHandler = null;

	/*
	 * Constructor from a string
	 */
	public ToolProxyBinding(String tool_proxy_binding)
	{
		super(tool_proxy_binding);

		// Sanity-check the number of resource_handler (s)
		JSONArray ja = getResourceHandlers();
		if ( ja == null || ja.size() != 1 ) {
			throw new java.lang.RuntimeException("A ToolProxyBinding must have exactly one resource_handler");
		}

		Object rh = ja.get(0);
		if ( ! (rh instanceof JSONObject ) ) {
			throw new java.lang.RuntimeException("resource handler is wrong type "+rh.getClass().getName());
		}
		resourceHandler = (JSONObject) rh;
	}

	/**
	 * Retrieve the tool_proxy_binding
	 */
	public JSONObject getToolProxyBinding()
	{
		return getToolProxy();
	}

	/**
	 * Retrieve the (single) resource_type from a tool_profile (binding)
	 */
	public JSONObject getResourceHandler()
	{
		return this.resourceHandler;
	}

	/**
	 * Retrieve a particular message type from an individual tool_profile
	 * 
	 * @param String messageType - Which message type you are looking for
	 */
	public JSONObject getMessageOfType(String messageType)
	{
		return getMessageOfType(resourceHandler, messageType);
	}

	/**
	 * Get all enabled capabilities
	 * 
	 * @param String messageType - Which message type you are looking for
	 * @param String capability - The capability to look for
	 * @return JSONArray the array of capabilities
	 */
	public JSONArray enabledCapabilities(String messageType)
	{
		return enabledCapabilities(resourceHandler, messageType);
	}

	/**
	 * Check if a particular capability is enabled
	 * 
	 * @param String messageType - Which message type you are looking for
	 * @param String capability - The capability to look for
	 */
	public boolean enabledCapability(String messageType, String capability)
	{
		return enabledCapability(resourceHandler, messageType, capability);
	}

	/**
	 * Extract an icon path by icon_style from an icon_info string
	 * 
	 * @param String icon_style - The style to look for
	 */
	public String getIconPath(String icon_style) {
		return getIconPath(resourceHandler, icon_style);
	}

}
