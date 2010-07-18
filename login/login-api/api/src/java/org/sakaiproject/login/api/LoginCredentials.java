/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.login.api;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class LoginCredentials {

	private String identifier;
	private String password;
	private String remoteAddr;
	private Map parameterMap;
	private String sessionId;
	private HttpServletRequest request;
	
	public LoginCredentials(HttpServletRequest request) {
		this.identifier = request.getParameter("eid");
		this.password = request.getParameter("pw");
		this.remoteAddr = request.getRemoteAddr();
		this.parameterMap = request.getParameterMap();
		this.request = request;
	}

	public LoginCredentials(String identifier, String password, String remoteAddr) {
		this.identifier = identifier;
		this.password = password;
		this.remoteAddr = remoteAddr;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRemoteAddr() {
		return remoteAddr;
	}
	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}
	public Map getParameterMap() {
		return parameterMap;
	}
	public void setParameterMap(Map parameterMap) {
		this.parameterMap = parameterMap;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	
}
