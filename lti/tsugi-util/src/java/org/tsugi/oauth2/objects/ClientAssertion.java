package org.tsugi.oauth2.objects;

import org.tsugi.lti13.objects.BaseJWT;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * The client_assertion part of a Client-Credentials Grant
 *
 * This  JWT will be signed, compacted, and sent to the oauth_token_url as a POST
 * request along with the following values
 *
 *   grant_type=client_credentials
 *   client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer
 *   client_assertion=signed_client_assertion_jwt
 *   scope=blank separated list of scopes
 *
 * The audience originially was expected to be the token_url on these requests
 * But D2L felt like there was supposed to be a separate audience value
 * for these tokens in IMS that is part of the contract so we all added
 * another column for it :)
 *
 * Later the IMS working group led by Backboard decided to eventually require the
 * deployment_id on this - which I think is a great idea and should have been there
 * all along but I still don't get why we need both an audience value
 * and a deployment_id - but D2L is rarely wrong on these matters.
 *
 * https://tools.ietf.org/html/rfc6750
 * https://www.imsglobal.org/spec/security/v1p0#using-json-web-tokens-with-oauth-2-0-client-credentials-grant
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ClientAssertion extends BaseJWT {

	public static final String GRANT_TYPE = "grant_type";
	public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
	public static final String CLIENT_ASSERTION_TYPE = "client_assertion_type";
	public static final String CLIENT_ASSERTION_TYPE_JWT = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
	public static final String CLIENT_ASSERTION = "client_assertion";
	public static final String SCOPE = "scope";

	// The IMS LTI Advantage Extension

    @JsonProperty("https://purl.imsglobal.org/spec/lti/claim/deployment_id")
    public String deployment_id;

}
