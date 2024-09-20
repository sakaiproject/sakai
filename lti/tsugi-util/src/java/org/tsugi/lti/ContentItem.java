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

package org.tsugi.basiclti;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.server.OAuthServlet;
import net.oauth.signature.OAuthSignatureMethod;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import static org.tsugi.basiclti.BasicLTIUtil.getArray;
import static org.tsugi.basiclti.BasicLTIUtil.getObject;
import static org.tsugi.basiclti.BasicLTIUtil.getString;
import static org.tsugi.basiclti.BasicLTIUtil.getDouble;

// https://www.imsglobal.org/specs/lticiv1p0/specification
/* {
  "@context" : [
    "http://purl.imsglobal.org/ctx/lti/v1/ContentItem",
    {
      "lineItem" : "http://purl.imsglobal.org/ctx/lis/v2/LineItem",
      "res" : "http://purl.imsglobal.org/ctx/lis/v2p1/Result#"
    }
  ],
  "@graph" : [
    { "@type" : "LtiLinkItem",
      "mediaType" : "application/vnd.ims.lti.v1.ltilink",
      "title" : "Chapter 12 quiz",
      "lineItem" : {
        "@type" : "LineItem",
        "label" : "Chapter 12 quiz",
        "reportingMethod" : "res:totalScore",
        "assignedActivity" : {
          "@id" : "http://toolprovider.example.com/assessment/66400",
          "activity_id" : "a-9334df-33"
        },
        "scoreConstraints" : {
          "@type" : "NumericLimits",
          "normalMaximum" : 100,
          "extraCreditMaximum" : 10,
          "totalMaximum" : 110
        }
      },
      { "@type" : "FileItem",
        "url" : "http://www.imsglobal.org/xsd/qti/qtiv2p1/imsqti_v2p1.xsd",
        "copyAdvice" : "true",
        "expiresAt" : "2014-03-05T00:00:00Z",
        "mediaType" : "application/xml",
        "title" : "QTI v2.1 Specification Information Model",
        "placementAdvice" : {
          "windowTarget" : "_blank"
        }
      }
  ]
} */

public class ContentItem {

	public static final String ACCEPT_MEDIA_TYPES = "accept_media_types";
	public static String MEDIA_LTILINKITEM = "application/vnd.ims.lti.v1.ltilink";
	public static String MEDIA_ALL = "*/*";

	public static String ACCEPT_MULTIPLE = "accept_multiple";

	// http://www.iana.org/assignments/media-types/media-types.xhtml
	public static final String MEDIA_CC_1_1 = "application/vnd.ims.imsccv1p1";
	public static final String MEDIA_CC_1_2 = "application/vnd.ims.imsccv1p2";
	public static final String MEDIA_CC_1_3 = "application/vnd.ims.imsccv1p3";
	public static final String MEDIA_CC = MEDIA_CC_1_1+","+MEDIA_CC_1_2+","+MEDIA_CC_1_3;

	public static final String TYPE_LTILINKITEM = "LtiLinkItem";
	public static final String TYPE_CONTENTITEM = "ContentItem";
	public static final String TYPE_FILEITEM = "FileItem";
	public static final String TYPE_IMPORTITEM = "ImportItem";

	public static final String TITLE = "title";
	public static final String TEXT = "text";
	public static final String URL = "url";
	public static final String LINEITEM = "lineItem";
	public static final String SCORE_CONSTRAINTS = "scoreConstraints";
	public static final String SCORE_CONSTRAINTS_NORMAL_MAXIMUM = "normalMaximum";
	public static final String SCORE_CONSTRAINTS_EXTRA_CREDIT_MAXIMUM = "extraCreditMaximum";
	public static final String SCORE_CONSTRAINTS_TOTAL_MAXIMUM = "totalMaximum";
	public static final String CUSTOM = "custom";
	public static final String ICON = "icon";
	public static final String CONTENT_ITEMS = "content_items";
	public static final String NO_CONTENT_ITEMS = "Missing content_items= parameter from ContentItem return";
	public static final String BAD_CONTENT_MESSAGE = "CONTENT_ITEMS is wrong type ";
	public static final String NO_DATA_MESSAGE = "Missing data= parameter from ContentItem return";
	public static final String NO_GRAPH_MESSAGE = "A content_item must include a @graph";
	public static final String BAD_DATA_MESSAGE = "data= parameter is wrong type ";

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

		String contentItems = req.getParameter(CONTENT_ITEMS);
		if ( StringUtils.isEmpty(contentItems) ) {
			throw new java.lang.RuntimeException(NO_CONTENT_ITEMS);
		}

		Object cit = JSONValue.parse(contentItems);
		if ( cit != null && cit instanceof JSONObject ) {
			contentItem = (JSONObject) cit;
		} else {
			throw new java.lang.RuntimeException(BAD_CONTENT_MESSAGE + cit.getClass().getName());
		}

		String returnedData = req.getParameter("data");
		if ( StringUtils.isEmpty(returnedData) ) {
			throw new java.lang.RuntimeException(NO_DATA_MESSAGE);
		}

		returnedData = StringEscapeUtils.unescapeJson(returnedData);

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

		graph = getArray(contentItem,BasicLTIConstants.GRAPH);
		if ( graph == null ) {
			throw new java.lang.RuntimeException(NO_GRAPH_MESSAGE);
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
			String type = getString(item, BasicLTIConstants.TYPE);
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

	/**
	 * Get the maximum score from a lineItem
	 *
	 * This logic is to produce the DeepLink equivalent of maxScore from a Content Item
	 */
	public static Double getScoreMaximum(JSONObject lineItem) {
        if ( lineItem == null ) return null;
        JSONObject scoreConstraints = getObject(lineItem, SCORE_CONSTRAINTS);
        if ( scoreConstraints == null ) return null;
		Double normalMaximum = getDouble(scoreConstraints, SCORE_CONSTRAINTS_NORMAL_MAXIMUM);
		Double totalMaximum = getDouble(scoreConstraints, SCORE_CONSTRAINTS_TOTAL_MAXIMUM);

		if ( totalMaximum == null ) return normalMaximum;
		if ( normalMaximum == null ) return totalMaximum;
		if ( normalMaximum > totalMaximum ) return normalMaximum;
		return totalMaximum;
	}

}
