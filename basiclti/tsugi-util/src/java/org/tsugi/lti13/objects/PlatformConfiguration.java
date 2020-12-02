package org.tsugi.lti13.objects;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.tsugi.lti13.LTI13ConstantsUtil;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")

/*
{
    "issuer": "https://server.example.com",
    "authorization_endpoint":  "https://server.example.com/connect/authorize",
    "token_endpoint": "https://server.example.com/connect/token",
    "token_endpoint_auth_methods_supported": ["private_key_jwt"],
    "token_endpoint_auth_signing_alg_values_supported": ["RS256"],
    "jwks_uri": "https://server.example.com/jwks.json",
    "registration_endpoint": "https://server.example.com/connect/register",
    "scopes_supported": ["openid", "https://purl.imsglobal.org/spec/lti-gs/scope/contextgroup.readonly",
       "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem",
       "https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly",
       "https://purl.imsglobal.org/spec/lti-ags/scope/score",
       "https://purl.imsglobal.org/spec/lti-reg/scope/registration"],
    "response_types_supported": ["id_token"],
    "subject_types_supported": ["public", "pairwise"],
    "id_token_signing_alg_values_supported":
      ["RS256", "ES256"],
    "claims_supported":
      ["sub", "iss", "name", "given_name", "family_name", "nickname", "picture", "email", "locale"],
     "https://purl.imsglobal.org/spec/lti-platform-configuration ": {
        "product_family_code": "ExampleLMS",
        "messages_supported": [
            {"type": "LtiResourceLinkRequest"},
            {"type": "LtiDeepLinkingRequest"}],
        "variables": ["CourseSection.timeFrame.end", "CourseSection.timeFrame.begin", "Context.id.history", "ResourceLink.id.history"]
    }
}
 */
public class PlatformConfiguration {

	// Platform's issuer value. As per IMS Security Framework and LTI Specification, the Issuer Identifier is 
	// a case-sensitive URL, using the HTTPS scheme, that contains scheme, host, and optionally, port number,
	// and path components, and no query or fragment components. 
	@JsonProperty("issuer")
	public String issuer;

	// URL of the OAuth 2.0 Authorization Endpoint. 
	@JsonProperty("authorization_endpoint")
	public String authorization_endpoint;

	// URL of the endpoint for the tool to request a token to access LTI (and possibly other) services. 
	@JsonProperty("token_endpoint")
	public String token_endpoint;

	// Must contain private_key_jwt may offer additional values
	@JsonProperty("token_endpoint_auth_methods_supported")
	public List<String> token_endpoint_auth_methods_supported = new ArrayList<String>();

	// Must contain RS256; may offer additional values. 
	@JsonProperty("token_endpoint_auth_signing_alg_values_supported")
	public List<String> token_endpoint_auth_signing_alg_values_supported = new ArrayList<String>();

	// URL of the Platform JWK Set endpoint; may be specific per registration if the platform's issued a dedicated discovery end-point for that registration. 
	@JsonProperty("jwks_uri")
	public String jwks_uri;

	// URL of the registration endpoint; may be a one time use only end-point and/or protected by access token.
	@JsonProperty("registration_endpoint")
	public String registration_endpoint;

	// Must contain openid and the scopes of the supported LTI services; for example https://purl.imsglobal.org/spec/lti-ags/scope/score. It may contain other non LTI related scopes. 
	@JsonProperty("scopes_supported")
	public List<String> scopes_supported = new ArrayList<String>();

	// Must contain id_token; may offer additional values. 
	@JsonProperty("response_types_supported")
	public List<String> response_types_supported = new ArrayList<String>();

	// ?? "public", "pairwise"
	// TODO: Document this in the spec
	@JsonProperty("subject_types_supported")
	public List<String> subject_types_supported = new ArrayList<String>();

	// Must contain RS256; may offer additional values. LTI requires the use of asymmetric cryptographic signing algorithms. 
	@JsonProperty("id_token_signing_alg_values_supported")
	public List<String> id_token_signing_alg_values_supported = new ArrayList<String>();

	// opendid claims supported by this platform. LTI related claims should not be included unless specified otherwise as those are inferred by the message types. 
	@JsonProperty("claims_supported")
	public List<String> claims_supported = new ArrayList<String>();

	// The (optional) authorization server identifier to be used as the aud when requesting an access token.
	// If not specified, the tool must use the token_endpoint as the aud value when requesting an access token. 
	@JsonProperty("authorization_server")
	public String authorization_server;

	@JsonProperty("https://purl.imsglobal.org/spec/lti-platform-configuration")
	public LTIPlatformConfiguration lti_platform_configuration;

   // Constructor
    public PlatformConfiguration() {
		this.token_endpoint_auth_methods_supported.add("private_key_jwt");
		this.token_endpoint_auth_signing_alg_values_supported.add("RS256");
		this.scopes_supported.add("openid");
		this.response_types_supported.add("id_token");
		this.id_token_signing_alg_values_supported.add("RS256");
		this.claims_supported.add(LTI13ConstantsUtil.KEY_ISS);
		this.claims_supported.add(LTI13ConstantsUtil.KEY_AUD);
		this.subject_types_supported.add("public");
		this.subject_types_supported.add("pairwise");
    }

}
