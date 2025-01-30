package org.tsugi.lti13.objects;

import java.util.UUID;

import org.tsugi.jackson.objects.JacksonBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class BaseJWT extends JacksonBase {

	// Put in the basic values - which can be removed or replaced in out calling code
	public BaseJWT()
	{
		this.issued = new Long(System.currentTimeMillis() / 1000L);
		this.expires = this.issued + 3600L;
		this.jti = UUID.randomUUID().toString();
		// Move this to LaunchJWT and DeepLinkResponse
		// this.nonce = this.jti;
	}

	@JsonProperty("iss")   // A unique identifier for the entity that issued the JWT
	public String issuer;  // The url of the LMS or product
	@JsonProperty("aud")   // Authorization server identifier (s)
	public String audience;
	@JsonProperty("sub")     // "client_id" of the OAuth Consumer
	public String subject;   // The user_id

	@JsonProperty("iat")
	public Long issued;
	@JsonProperty("exp")
	public Long expires;
	@JsonProperty("jti")
	public String jti;   

	// Move this to LaunchJWT and DeepLinkResponse where it is explicitly needed
	// @JsonProperty("nonce")
	// public String nonce;

}
