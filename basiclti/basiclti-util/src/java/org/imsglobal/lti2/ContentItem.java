/*
 * $URL$
 * $Id$
 *
 * Copyright (c) 2015 IMS GLobal Learning Consortium
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
import org.imsglobal.lti2.objects.Service_offered;
import org.imsglobal.lti2.objects.StandardServices;
import org.imsglobal.lti2.objects.ToolConsumer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import static org.imsglobal.lti2.LTI2Util.getArray;
import static org.imsglobal.lti2.LTI2Util.getObject;
import static org.imsglobal.lti2.LTI2Util.getString;

/* {
	"@context": "http:\/\/purl.imsglobal.org\/ctx\/lti\/v1\/ContentItem",
	"@graph": [ {
		"@type": "LtiLink",
		"@id": ":item2",
		"text": "The mascot for the Sakai Project",
		"title": "The fearsome mascot of the Sakai Project",
		"url": "http:\/\/localhost:8888\/sakai-api-test\/tool.php?sakai=98765",
		"icon": {
			"@id": "fa-bullseye",
			"width": 50,
			"height": 50
		}
	} ]
} */

public class ContentItem {

	// We use the built-in Java logger because this code needs to be very generic
	private static Logger M_log = Logger.getLogger(ContentItem.class.toString());

	private JSONObject contentItem = null;
	private JSONArray graph = null;

	/**
	 * We check for the fields essential for the class to operate here.
	 */
	public ContentItem(String content_items)
	{
		if ( content_items == null || content_items.trim().length() < 1 ) {
			throw new java.lang.RuntimeException("Cannot initialize with empty string");
		}
		Object cit = JSONValue.parse(content_items);
		if ( cit != null && cit instanceof JSONObject ) {
			contentItem = (JSONObject) cit;
		} else {
			throw new java.lang.RuntimeException("content item is wrong type "+cit.getClass().getName());
		}

		graph = getArray(contentItem,LTI2Constants.GRAPH);
		if ( graph == null ) {
			throw new java.lang.RuntimeException("A content_item must include a @graph");
		}
	}

	/**
	 * Return a string
	 */
	public String toString()
	{
		return contentItem.toString();
	}

	/**
	 * Retrieve the parsed tool_proxy
	 */
	public JSONObject getContentItem()
	{
		return contentItem;
	}

	/**
	 * Retrieve the @graph from the content item
	 */
	public JSONArray getGraph()
	{
		return graph;
	}

	/**
	 * Retrieve a particular type from the graph
	 * 
	 * @param String messageType - Which item type you are looking for
	 */
	public JSONObject getItemOfType(String itemType)
	{
		for ( Object i : graph ) {
			if ( ! (i instanceof JSONObject) ) continue;
			JSONObject item = (JSONObject) i;
			String type = getString(item,LTI2Constants.TYPE);
			if ( type == null ) continue;
			if ( type.equals(itemType) ) return item;
		}
		return null;
	}

}
