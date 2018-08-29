package org.tsugi.lti13.objects;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
public class BaseJWT {

	@JsonProperty("iss")
	public String issuer;  // The url of the LMS or product
	@JsonProperty("aud")
	public String audience;  // The Client ID
	@JsonProperty("sub")
	public String subject;   // The user_id
	@JsonProperty("nonce")
	public String nonce;
	@JsonProperty("iat")
	public Long issued;
	@JsonProperty("exp")
	public Long expires;

}
