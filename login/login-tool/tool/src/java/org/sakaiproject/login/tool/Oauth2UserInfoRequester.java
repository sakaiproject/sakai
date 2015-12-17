/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/login/tags/sakai-10.5/login-tool/tool/src/java/org/sakaiproject/login/tool/Oauth2UserInfoRequester.java $
 * $Id: Oauth2UserInfoRequester.java fsaez@entornosdeformacion.com $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.login.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class Oauth2UserInfoRequester {
	
	private static Log M_log = LogFactory.getLog(Oauth2UserInfoRequester.class);
	
	private String userInfoUri = null;
	
	public Oauth2UserInfoRequester(String userInfoUri) {
		this.userInfoUri = userInfoUri;
	}

	public JsonObject getUserInfo(String accessTokenValue) {

		try {
			HttpClient client = new HttpClient();
			HttpState state = client.getState();
			
			GetMethod method = new GetMethod(userInfoUri);
			String authHeaderStr = String.format("Bearer %s", accessTokenValue);
			method.addRequestHeader("Authorization", authHeaderStr);
			    
			client.executeMethod( method );
			//String response = method.getResponseBodyAsString();
			BufferedReader br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
	        String readLine;
	        StringBuilder sb = new StringBuilder();
	        while(((readLine = br.readLine()) != null)) {
	          sb.append(readLine);
	        }
			
	        String response = sb.toString();
	        M_log.debug("USER INFO response : "+response);
			
			if (!StringUtils.isEmpty(response)) {

				JsonObject userInfoJson = new JsonParser().parse(response).getAsJsonObject();

				return userInfoJson;
			} else {
				// didn't get anything, return null
				return null;
			}
			
			
		} catch(Exception e) { 
			M_log.error("ERROR getting user info : "+e.getMessage());
		}
		
		return null;
	}		
}

