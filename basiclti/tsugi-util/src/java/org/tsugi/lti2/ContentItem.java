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
import java.util.logging.Logger;
import java.lang.StringBuffer;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

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


import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.server.OAuthServlet;
import net.oauth.signature.OAuthSignatureMethod;

/* {
	"@context": "http:\/\/purl.imsglobal.org\/ctx\/lti\/v1\/ContentItem",
	"@graph": [ {
		"@type": "LtiLinkItem",
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

	public static final String ACCEPT_MEDIA_TYPES = "accept_media_types";
	public static String MEDIA_LTILINKITEM = "application/vnd.ims.lti.v1.ltilink";
	// http://www.iana.org/assignments/media-types/media-types.xhtml
	public static final String MEDIA_CC_1_1 = "application/vnd.ims.imsccv1p1";
	public static final String MEDIA_CC_1_2 = "application/vnd.ims.imsccv1p2";
	public static final String MEDIA_CC_1_3 = "application/vnd.ims.imsccv1p3";
	public static final String MEDIA_CC = MEDIA_CC_1_3+","+MEDIA_CC_1_2+","+MEDIA_CC_1_3;

	// TYPE is in LTI2Constants.TYPE (since it is JSON_LD really)
	public static final String TYPE_LTILINKITEM = "LtiLinkItem";
	public static final String TYPE_LTILINK_OLD = "LtiLink";
	public static final String TYPE_CONTENTITEM = "ContentItem";
	public static final String TYPE_FILEITEM = "FileItem";
	public static final String TYPE_IMPORTITEM = "ImportItem";

	public static final String TITLE = "title";
	public static final String TEXT = "text";
	public static final String URL = "url";
	public static final String LINEITEM = "lineItem";
	public static final String CUSTOM = "custom";
	public static final String ICON = "icon";

	HttpServletRequest servletRequest = null;

	private JSONObject contentItem = null;

	private JSONArray graph = null;

	private Properties dataProps = new Properties();

	private String errorMessage = null;

	private String base_string = null;

	/**
	 * We check for the fields essential for the class to operate here.
	 */
	public ContentItem(HttpServletRequest req)
	{
		this.servletRequest = req;

		String content_items = req.getParameter("content_items");
		if ( content_items == null || content_items.length() < 1 ) {
			throw new java.lang.RuntimeException("Missing content_items= parameter from ContentItem return");
		}

		Object cit = JSONValue.parse(content_items);
		if ( cit != null && cit instanceof JSONObject ) {
			contentItem = (JSONObject) cit;
		} else {
			throw new java.lang.RuntimeException("content_items is wrong type "+cit.getClass().getName());
		}

		String returnedData = req.getParameter("data");
		if ( returnedData == null || returnedData.length() < 1 ) {
			throw new java.lang.RuntimeException("Missing data= parameter from ContentItem return");
		}

		Object dat = JSONValue.parse(returnedData);
		JSONObject dataJson = null;
		if ( dat != null && dat instanceof JSONObject ) {
			dataJson = (JSONObject) dat;
		} else {
			throw new java.lang.RuntimeException("data= parameter is wrong type "+dat.getClass().getName());
		}

		Iterator it = dataJson.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Object value = dataJson.get(key);
			if ( value == null || ! (value instanceof String) ) continue;
			dataProps.setProperty(key, (String) value);
		}

		graph = getArray(contentItem,LTI2Constants.GRAPH);
		if ( graph == null ) {
			throw new java.lang.RuntimeException("A content_item must include a @graph");
		}
	}

	/**
	 * Validate the incoming request
	 *
	 * @param URL The URL of the incoming request.  If this in null, the URL is taken
	 * from the request object.  Sometimes the request object can be misleading when 
	 * sitting behind a load balancer or some kind of proxy.
	 */
	public boolean validate(String oauth_consumer_key, String oauth_secret, String URL)
	{
		String req_oauth_consumer_key = servletRequest.getParameter("oauth_consumer_key");
		if ( req_oauth_consumer_key == null || req_oauth_consumer_key.length() < 1 ) {
			errorMessage = "Missing oauth_consumer_key from incoming request";
			return true;
		}
		if ( ! req_oauth_consumer_key.equals(oauth_consumer_key) ) {
			errorMessage = "Mis-match of oauth_consumer_key from incoming request";
			return false;
		}

		// A URL of null is OK
		OAuthMessage oam = OAuthServlet.getMessage(servletRequest, URL);
		OAuthValidator oav = new SimpleOAuthValidator();
		OAuthConsumer cons = new OAuthConsumer("about:blank#OAuth+CallBack+NotUsed", oauth_consumer_key,oauth_secret, null);

		OAuthAccessor acc = new OAuthAccessor(cons);
		base_string = null;
		try {
			base_string = OAuthSignatureMethod.getBaseString(oam);
		} catch (Exception e) {
			base_string = null;
		}

		try {
			oav.validateMessage(oam, acc);
		} catch (Exception e) {
			errorMessage = e.getLocalizedMessage();
			return false;
		}
		return true;
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

	/**
	 * Get the data properties from the incoming request
	 */
	public Properties getDataProperties()
	{
		return dataProps;
	}

	/**
	 * Return the base string
	 */
	public String getBaseString()
	{
		return base_string;
	}

	/**
	 * Return the error message
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}

	/**
	 * Build up a ContentItem launch URL from a base url, return url and extra data
	 */
	public static String buildLaunch(String contentLaunch, String contentReturn, Properties contentData)
	{
		StringBuffer sb = new StringBuffer(contentLaunch);
		if ( contentLaunch.indexOf("?") > 1 ) {
			sb.append("&");
		} else {
			sb.append("?");
		}
		sb.append("contentReturn=");
		sb.append(URLEncoder.encode(contentReturn));
		if ( contentData == null ) return sb.toString();

		Enumeration en = contentData.keys();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			String value = contentData.getProperty(key);
			if ( value == null ) continue;

			sb.append("&");
			sb.append(URLEncoder.encode(key));
			sb.append("=");
			sb.append(URLEncoder.encode(value));
		}
		return sb.toString();
	}

}
