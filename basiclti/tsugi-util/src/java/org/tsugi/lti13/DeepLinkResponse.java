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

package org.tsugi.lti13;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.security.Key;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.tsugi.basiclti.BasicLTIUtil;

// https://www.imsglobal.org/spec/lti-dl/v2p0
/*

{
"iss": "962fa4d8-bcbf-49a0-94b2-2de05ad274af",
"aud": "https://platform.example.org",
"exp": 1510185728,
"iat": 1510185228,
"nonce": "fc5fdc6d-5dd6-47f4-b2c9-5d1216e9b771",
"azp": "962fa4d8-bcbf-49a0-94b2-2de05ad274af",
"https://purl.imsglobal.org/spec/lti/claim/deployment_id":
"07940580-b309-415e-a37c-914d387c1150",
"https://purl.imsglobal.org/spec/lti/claim/message_type":
"LtiDeepLinkingResponse",
"https://purl.imsglobal.org/spec/lti/claim/version": "1.3.0",
"https://purl.imsglobal.org/spec/lti-dl/claim/content_items": [
{
"type": "link",
"title": "My Home Page",
"url": "https://something.example.com/page.html",
"icon": {
  "url": "https://lti.example.com/image.jpg",
  "width": 100,
  "height": 100
},
"thumbnail": {
  "url": "https://lti.example.com/thumb.jpg",
  "width": 90,
  "height": 90
}
},
{
"type": "html",
"html": "<h1>A Custom Title</h1>"
},
{
"type": "link",
"url": "https://www.youtube.com/watch?v=corV3-WsIro",
"embed": {
  "html":
    "<iframe width="560" height="315" src="https://www.youtube.com/embed/corV3-WsIro" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>"
},
"window": {
  "targetName": "youtube-corV3-WsIro",
  "windowFeatures": "height=560,width=315,menubar=no"
},
"iframe": {
  "width": 560,
  "height": 315,
  "src": "https://www.youtube.com/embed/corV3-WsIro"
}
},
{
"type": "image",
"url": "https://www.example.com/image.png",
"https://www.example.com/resourceMetadata": {
  "license": "CCBY4.0"
}
},
{
"type": "ltiResourceLink",
"title": "A title",
"text": "This is a link to an activity that will be graded",
"url": "https://lti.example.com/launchMe",
"icon": {
  "url": "https://lti.example.com/image.jpg",
  "width": 100,
  "height": 100
},
"thumbnail": {
  "url": "https://lti.example.com/thumb.jpg",
  "width": 90,
  "height": 90
},
"lineItem": {
  "scoreMaximum": 87,
  "label": "Chapter 12 quiz",
  "resourceId": "xyzpdq1234",
  "tag": "originality"
},
"available": {
  "startDateTime": "2018-02-06T20:05:02Z",
  "endDateTime": "2018-03-07T20:05:02Z"
},
"submission": {
  "endDateTime": "2018-03-06T20:05:02Z"
},
"custom": {
  "quiz_id": "az-123",
  "duedate": "$Resource.submission.endDateTime"
},
"window": {
  "targetName": "examplePublisherContent"
},
"iframe": {
  "height": 890
}
},
{
"type": "file",
"title": "A file like a PDF that is my assignment submissions",
"url": "https://my.example.com/assignment1.pdf",
"mediaType": "application/pdf",
"expiresAt": "2018-03-06T20:05:02Z"
},
{
"type": "https://www.example.com/custom_type",
"data": "somedata"
}
],
"https://purl.imsglobal.org/spec/lti-dl/claim/data":
"csrftoken:c7fbba78-7b75-46e3-9201-11e6d5f36f53"
}

*/

public class DeepLinkResponse {
	
	public static final String DEEP_LINKS = "https://purl.imsglobal.org/spec/lti-dl/claim/content_items";
	public static final String DATA = "https://purl.imsglobal.org/spec/lti-dl/claim/data";
	public static final String ACCEPT_MEDIA_TYPES = "accept_media_types";
	public static String MEDIA_LTILINKITEM = "application/vnd.ims.lti.v1.ltilink";
	
	// http://www.iana.org/assignments/media-types/media-types.xhtml
	public static final String MEDIA_CC_1_1 = "application/vnd.ims.imsccv1p1";
	public static final String MEDIA_CC_1_2 = "application/vnd.ims.imsccv1p2";
	public static final String MEDIA_CC_1_3 = "application/vnd.ims.imsccv1p3";
	public static final String MEDIA_CC = MEDIA_CC_1_1+","+MEDIA_CC_1_2+","+MEDIA_CC_1_3;

	public static final String TYPE = "type";
	public static final String TYPE_LINKITEM = "link";
	public static final String TYPE_LTILINKITEM = "ltiResourceLink";
	public static final String TYPE_CONTENTITEM = "html";
	public static final String TYPE_FILEITEM = "file";

	public static final String TITLE = "title";
	public static final String TEXT = "text";
	public static final String URL = "url";
	public static final String LINEITEM = "lineItem";
	public static final String CUSTOM = "custom";
	public static final String ICON = "icon";

	/**
	 * Indicates the initial start and end time this activity
	 * should be made available to learners. A platform may choose
	 * to make an item not accessible by hiding it, or by
	 * disabling the link, or some other method which prevents
	 * the link from being opened by a learner. The initial value
	 * may subsequently be changed within the platform and the
	 * tool may use the ResourceLink.available.startDateTime
	 * and ResourceLink.available.endDateTime substitution
	 * parameters defined in LTI Core specification [LTI-13]
	 * within custom parameters to get the actual values at launch time.
	 * ISO 8601 date and time
	 */
	public static final String AVAILABLE = "available";
	public static final String AVAILABLE_STARTDATETIME = "startDateTime";
	public static final String AVAILABLE_ENDDATETIME = "endDateTime";

	public static final String RESOURCELINK_AVAILABLE_STARTDATETIME = "ResourceLink.available.startDateTime";
	public static final String RESOURCELINK_AVAILABLE_ENDDATETIME = "ResourceLink.available.endDateTime";

	/**
	 * Indicates the initial start and end time submissions
	 * for this activity can be made by learners. The initial value
	 * may subsequently be changed within the platform and the
	 * tool may use the ResourceLink.submission.startDateTime
	 * and ResourceLink.submission.endDateTime substitution
	 * parameters defined in LTI Core specification [LTI-13]
	 * within custom parameters to get the actual values at launch time.
	 *
	 * ISO 8601 date and time
	 */
	public static final String SUBMISSION = "submission";
	public static final String SUBMISSION_STARTDATETIME = "startDateTime";
	public static final String SUBMISSION_ENDDATETIME = "endDateTime";

	public static final String RESOURCELINK_SUBMISSION_STARTDATETIME = "ResourceLink.submission.startDateTime";
	public static final String RESOURCELINK_SUBMISSION_ENDDATETIME = "ResourceLink.submission.endDateTime";

	private String id_token = null;
	
	private JSONObject body = null;

	private JSONArray deep_links = null;

	private String returnedData = null;


	/**
	 * We check for the fields essential for the class to operate here.
	 */
	public DeepLinkResponse(String id_token)
	{
		this.id_token = id_token;
		
		body = (JSONObject) LTI13JwtUtil.jsonJwtBody(id_token);
		if ( body == null ) {
			throw new java.lang.RuntimeException("Could not extract body from id_token");
		}
		String message_type = (String) body.get(LTI13ConstantsUtil.MESSAGE_TYPE);
		if ( ! LTI13ConstantsUtil.MESSAGE_TYPE_LTI_DEEP_LINKING_RESPONSE.equals(message_type) ) {
			throw new java.lang.RuntimeException("Incorrect MESSAGE_TYPE");
		}
		
		deep_links = BasicLTIUtil.getArray(body, DEEP_LINKS);
		if ( deep_links == null || deep_links.size() < 1 ) {
			throw new java.lang.RuntimeException("A deep link response must include at least one content_item");
		}

		String returnedData = (String) body.get(DATA);
		if ( returnedData == null || returnedData.length() < 1 ) {
			throw new java.lang.RuntimeException("Missing data element from ContentItem return");
		}
	}
	
	/**
	 * Check if the incoming request is a DeepLinkResponse
	 */
	public static boolean isRequest(HttpServletRequest req)
	{
		String id_token = req.getParameter(LTI13JwtUtil.ID_TOKEN);
		return isRequest(id_token);
	}
	
	/**
	 * Check if the incoming request is a DeepLinkResponse
	 */
	public static boolean isRequest(String id_token)
	{
		if ( ! LTI13JwtUtil.isRequest(id_token) ) return false;
		
		JSONObject body = (JSONObject) LTI13JwtUtil.jsonJwtBody(id_token);
		String message_type = (String) body.get(LTI13ConstantsUtil.MESSAGE_TYPE);
		return LTI13ConstantsUtil.MESSAGE_TYPE_LTI_DEEP_LINKING_RESPONSE.equals(message_type);
	}

	/**
	 * Validate the incoming request
	 *
	 * @param URL The URL of the incoming request.  If this in null, the URL is taken
	 * from the request object.  Sometimes the request object can be misleading when
	 * sitting behind a load balancer or some kind of proxy.
	 */
	public boolean validate(Key publicKey)
	{
		// Validate security
		Jws<Claims> claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(id_token);
		return claims != null;
	}

	/**
	 * Return a string
	 */
	public String toString()
	{
		return body.toString();
	}

	/**
	 * Retrieve the parsed tool_proxy
	 */
	public JSONObject getContentItem()
	{
		return body;
	}

	/**
	 * Retrieve the @graph from the content item
	 */
	public JSONArray getDeepLinks()
	{
		return deep_links;
	}

	/**
	 * Retrieve a particular type from the graph
	 *
	 * @param String messageType - Which item type you are looking for
	 */
	public JSONObject getItemOfType(String itemType)
	{
		for ( Object i : deep_links ) {
			if ( ! (i instanceof JSONObject) ) continue;
			JSONObject item = (JSONObject) i;
			String type = BasicLTIUtil.getString(item,"type");
			if ( type == null ) continue;
			if ( type.equals(itemType) ) return item;
		}
		return null;
	}

}
