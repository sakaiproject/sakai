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
/* {
    "https:\/\/purl.imsglobal.org\/spec\/lti\/claim\/deployment_id": "07940580-b309-415e-a37c-914d387c1150",
    "https:\/\/purl.imsglobal.org\/spec\/lti\/claim\/message_type": "LtiDeepLinkingResponse",
    "https:\/\/purl.imsglobal.org\/spec\/lti\/claim\/version": "1.3.0",
    "https:\/\/purl.imsglobal.org\/spec\/lti-dl\/claim\/content_items": [
        [
            {
                "type": "ltiResourceLink",
                "title": "Breakout",
                "url": "http:\/\/localhost:8888\/tsugi\/mod\/breakout\/",
                "presentation": {
                    "documentTarget": "iframe",
                    "width": 500,
                    "height": 600
                },
                "icon": {
                    "url": "http:\/\/localhost:8888\/tsugi-static\/font-awesome-4.7.0\/png\/gamepad.png",
                    "fa_icon": "fa-gamepad",
                    "width": 100,
                    "height": 100
                },
                "thumbnail": {
                    "url": "https:\/\/lti.example.com\/thumb.jpg",
                    "width": 90,
                    "height": 90
                },
                "lineItem": {
                    "scoreMaximum": 10,
                    "label": "Breakout",
                    "resourceId": "breakout",
                    "tag": "originality",
                    "guid": "http:\/\/localhost:8888\/tsugi\/lti\/activity\/breakout"
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
            }
        ]
    ],
    "https:\/\/purl.imsglobal.org\/spec\/lti-dl\/data": "{\"remember\":\"always bring a towel\"}",
    "iss": "issuer",
    "sub": "subject",
    "iat": 1537411883,
    "exp": 1537411943,
    "jti": "issuer5ba30b2b062bf"
} */

public class DeepLinkResponse {
	
	public static final String DEEP_LINKS = "https://purl.imsglobal.org/spec/lti-dl/claim/content_items";
	public static final String DATA = "https://purl.imsglobal.org/spec/lti-dl/claim/data";
	public static final String ACCEPT_MEDIA_TYPES = "accept_media_types";
	public static String MEDIA_LTILINKITEM = "application/vnd.ims.lti.v1.ltilink";
	
	// http://www.iana.org/assignments/media-types/media-types.xhtml
	public static final String MEDIA_CC_1_1 = "application/vnd.ims.imsccv1p1";
	public static final String MEDIA_CC_1_2 = "application/vnd.ims.imsccv1p2";
	public static final String MEDIA_CC_1_3 = "application/vnd.ims.imsccv1p3";
	public static final String MEDIA_CC = MEDIA_CC_1_3+","+MEDIA_CC_1_2+","+MEDIA_CC_1_3;

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
