package org.tsugi.oauth2.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A returned Access Token from a Client-Credentials Grant
 */
// https://www.imsglobal.org/spec/security/v1p0#using-json-web-tokens-with-oauth-2-0-client-credentials-grant
// https://tools.ietf.org/html/rfc6750
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class AccessToken {

	public static final String TOKEN_TYPE_BEARER = "Bearer";
	
	@JsonProperty("access_token")
	public String access_token;
	// I think the use of "stenotype" in the IMS security spec above is a typo
	@JsonProperty("token_type")
	public String token_type;
	@JsonProperty("expires_in")
	public Long expires_in;
	@JsonProperty("refresh_token")
	public String refresh_token;
	@JsonProperty("scope")
	public String scope;

	public AccessToken() {
		this.token_type = TOKEN_TYPE_BEARER;
		this.expires_in = new Long(3600);
	}

	// Here it is a comma separated list of scopes - see RFC6750
	public void addScope(String scope) {
		if ( this.scope == null ) {
			this.scope = scope;
			return;
		}
		this.scope = this.scope + " " + scope;
	}

}
