package org.sakaiproject.microsoft.api.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
@Data
@Builder
public class MicrosoftCredentials {


	private static final String KEY_PREFIX = "CREDENTIALS:";
	public static final String KEY_CLIENT_ID = KEY_PREFIX + "CLIENT_ID";
	public static final String KEY_AUTHORITY = KEY_PREFIX + "AUTHORITY";
	public static final String KEY_SECRET = KEY_PREFIX + "SECRET";
	public static final String KEY_SCOPE = KEY_PREFIX + "SCOPE";
	public static final String KEY_EMAIL = KEY_PREFIX + "EMAIL";

	private String clientId;

	private String authority;

	private String secret;

	private String scope;

	private String email;
	
	public String getScope() {
		if(scope == null) {
			return "https://graph.microsoft.com/.default";
		}
		return scope;
	}

	public boolean hasValue() {
		return StringUtils.isNotBlank(clientId) && 
				StringUtils.isNotBlank(authority) && 
				StringUtils.isNotBlank(secret) && 
				StringUtils.isNotBlank(scope) &&
				StringUtils.isNotBlank(email);
	}
}
