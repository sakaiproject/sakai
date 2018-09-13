package org.tsugi.oauth2.objects;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

// https://tools.ietf.org/html/rfc6750
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
public class AccessToken {

	public static final String BEARER = "Bearer";
	public static final String GRANT_TYPE = "grant_type";
	public static final String CLIENT_ASSERTION = "client_assertion";
	public static final String SCOPE = "scope";
	public static final String SCOPE_LINEITEM = "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem";
	public static final String SCOPE_SCORE = "https://purl.imsglobal.org/spec/lti-ags/scope/score";
	public static final String SCOPE_RESULT_READONLY = "https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly";

	@JsonProperty("access_token")
	public String access_token;
	@JsonProperty("token_type")
	public String token_type;
	@JsonProperty("expires_in")
	public Long expires_in;
	@JsonProperty("refresh_token")
	public String refresh_token;

	public AccessToken() {
		this.token_type = BEARER;
		this.expires_in = new Long(3600);
	}

}
