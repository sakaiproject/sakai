/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/login/tags/sakai-10.5/login-tool/tool/src/java/org/sakaiproject/login/tool/Oauth2ServerConfiguration.java $
 * $Id: Oauth2ServerConfiguration.java fsaez@entornosdeformacion.com $
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

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;

public class Oauth2ServerConfiguration {
	
	public static String AUTH_TYPE_SECRET_BASIC = "client_secret_basic";
	public static String AUTH_TYPE_SECRET_POST = "client_secret_post";
	public static String AUTH_TYPE_NONE = "none";

	private static ServerConfigurationService serverConfigurationService = null;
	
	private static ServerConfigurationService getServerConfigurationService(){
		if(serverConfigurationService == null)
			serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
		return serverConfigurationService;
	}
	
	public static boolean isEnabled() {
		return getServerConfigurationService().getBoolean("sakai.login.oauth2.enabled", false);
	}
	
	public static boolean isCreateUser() {
		return getServerConfigurationService().getBoolean("sakai.login.oauth2.user.create", false);
	}
	
	public static String getIssuer() {
		return getServerConfigurationService().getString("sakai.login.oauth2.issuer", "");
	}
	
	public static String getAuthorizationEndpointUri() {
		return getServerConfigurationService().getString("sakai.login.oauth2.authorizationEndpointUri", "");
	}
	
	public static String getTokenEndpointUri() {
		return getServerConfigurationService().getString("sakai.login.oauth2.tokenEndpointUri", "");
	}
	
	public static String getUserInfoEndpointUri() {
		return getServerConfigurationService().getString("sakai.login.oauth2.userInfoEndpointUri", "");
	}
	
	public static String getRevokeEndpointUri() {
		return getServerConfigurationService().getString("sakai.login.oauth2.revokeEndpointUri", "");
	}
	
	public static String getLogoutURL() {
		return getServerConfigurationService().getString("sakai.login.oauth2.logout.url", null);
	}
	
	public static String getScope() {
		return getServerConfigurationService().getString("sakai.login.oauth2.scope", "");
	}
	
	public static String getClientId() {
		return getServerConfigurationService().getString("sakai.login.oauth2.client.id", "");
	}
	
	public static String getClientSecret() {
		return getServerConfigurationService().getString("sakai.login.oauth2.client.secret", "");
	}

	public static String getAuthorizationType() {
		return getServerConfigurationService().getString("sakai.login.oauth2.authentication.type", AUTH_TYPE_SECRET_BASIC);
	}
	
	public static String getUserType() {
		return getServerConfigurationService().getString("sakai.login.oauth2.user.type", "oauth2");
	}
	
	public static String getProperty_userId() {
		return getServerConfigurationService().getString("sakai.login.oauth2.userinfo.property.userid", "email");
	}
	
	public static String getProperty_firstName() {
		return getServerConfigurationService().getString("sakai.login.oauth2.userinfo.property.firstname", null);
	}
	
	public static String getProperty_lastName() {
		return getServerConfigurationService().getString("sakai.login.oauth2.userinfo.property.lastname", null);
	}
	
	public static String getProperty_userName() {
		return getServerConfigurationService().getString("sakai.login.oauth2.userinfo.property.username", "name");
	}
	
	public static String getProperty_email() {
		return getServerConfigurationService().getString("sakai.login.oauth2.userinfo.property.email", "email");
	}
}

