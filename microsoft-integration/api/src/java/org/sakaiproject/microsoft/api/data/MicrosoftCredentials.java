package org.sakaiproject.microsoft.api.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.microsoft.api.MicrosoftAuthorizationService;

@AllArgsConstructor
@Data
@Builder
public class MicrosoftCredentials {


	private static final String KEY_PREFIX = "CREDENTIALS:";
	public static final String KEY_CLIENT_ID = KEY_PREFIX + "CLIENT_ID";
	public static final String KEY_AUTHORITY = KEY_PREFIX + "AUTHORITY";
	public static final String KEY_SECRET = KEY_PREFIX + "SECRET";
	public static final String KEY_SCOPE = KEY_PREFIX + "SCOPE";
	public static final String KEY_DELEGATED_SCOPE = KEY_PREFIX + "DELEGATED_SCOPE";
	public static final String KEY_EMAIL = KEY_PREFIX + "EMAIL";

	private String clientId;

	private String authority;

	private String secret;

	private String scope;
	
	private String delegatedScope;

	private String email;
	
	public String getAuthority() {
		if(StringUtils.isNotBlank(authority) && !authority.endsWith("/")) {
			return authority + "/";
		}
		return authority;
	}
	
	public String getScope() {
		if(scope == null) {
			return MicrosoftAuthorizationService.SCOPE_DEFAULT;
		}
		return scope;
	}
	
	public String getDelegatedScope() {
		if(delegatedScope == null) {
			return MicrosoftAuthorizationService.DELEGATED_SCOPE_DEFAULT;
		}
		return delegatedScope;
	}

	public boolean hasValue() {
		return StringUtils.isNotBlank(clientId) && 
				StringUtils.isNotBlank(authority) && 
				StringUtils.isNotBlank(secret) && 
				StringUtils.isNotBlank(getScope()) &&
				StringUtils.isNotBlank(getDelegatedScope()) &&
				StringUtils.isNotBlank(email);
	}
}
