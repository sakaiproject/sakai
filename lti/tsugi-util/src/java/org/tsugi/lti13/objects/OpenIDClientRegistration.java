package org.tsugi.lti13.objects;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

// https://www.imsglobal.org/spec/lti-dr/v1p0#client-registration-request
// https://openid.net/specs/openid-connect-registration-1_0.html#RegistrationRequest

/*

Per https://www.imsglobal.org/spec/lti-dr/v1p0#successful-registration

As per https://openid.net/specs/openid-connect-registration-1_0.html upon successful registration
a application/json the platform must return a response containing the newly created
client_id. It then echoes the client configuration as recorded in the platform, which
may differ from the configuration passed in the request based on the actual platform's
capabilities and restrictions.

The registration response may include a Client Configuration Endpoint and a Registration
Access Token to allow a tool to read or update its configuration.

In the case where a Platform is combining the client registration with the tool's actual
deployment, it may also include the deployment_id in the LTI Tool Configuration section.

POST /connect/register HTTP/1.1
Content-Type: application/json
Accept: application/json
Host: server.example.com
Authorization: Bearer eyJhbGciOiJSUzI1NiJ9.eyJ .

{
    "application_type": "web",
    "response_types": ["id_token"],
    "grant_types": ["implict", "client_credentials"],
    "initiate_login_uri": "https://client.example.org/lti",
    "redirect_uris":
      ["https://client.example.org/callback",
       "https://client.example.org/callback2"],
    "client_name": "Virtual Garden",
    "client_name#ja": "バーチャルガーデン",
    "jwks_uri": "https://client.example.org/.well-known/jwks.json",
    "logo_uri": "https://client.example.org/logo.png",
    "policy_uri": "https://client.example.org/privacy",
    "policy_uri#ja": "https://client.example.org/privacy?lang=ja",
    "tos_uri": "https://client.example.org/tos",
    "tos_uri#ja": "https://client.example.org/tos?lang=ja",
    "token_endpoint_auth_method": "private_key_jwt",
    "contacts": ["ve7jtb@example.org", "mary@example.org"],
    "scope": "https://purl.imsglobal.org/spec/lti-ags/scope/score https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly",
    "https://purl.imsglobal.org/spec/lti-tool-configuration": {
        "domain": "client.example.org",
        "description": "Learn Botany by tending to your little (virtual) garden.",
        "description#ja": "小さな（仮想）庭に行くことで植物学を学びましょう。",
        "target_link_uri": "https://client.example.org/lti",
        "custom_parameters": {
            "context_history": "$Context.id.history"
        },
        "claims": ["iss", "sub", "name", "given_name", "family_name"],
        "messages": [
            {
                "type": "LtiDeepLinkingRequest",
                "target_link_uri": "https://client.example.org/lti/dl",
                "label": "Add a virtual garden",
                "label#ja": "バーチャルガーデンを追加する",
            }
        ]
    }
}

{
    "client_id": "709sdfnjkds12",
    "registration_client_uri":
      "https://server.example.com/connect/register?client_id=709sdfnjkds12",
    "application_type": "web",
    "response_types": ["id_token"],
    "grant_types": ["implict", "client_credentials"],
    "initiate_login_uri": "https://client.example.org/lti",
    "redirect_uris":
      ["https://client.example.org/callback",
       "https://client.example.org/callback2"],
    "client_name": "Virtual Garden",
    "jwks_uri": "https://client.example.org/.well-known/jwks.json",
    "logo_uri": "https://client.example.org/logo.png",
    "token_endpoint_auth_method": "private_key_jwt",
    "contacts": ["ve7jtb@example.org", "mary@example.org"],
    "scope": "https://purl.imsglobal.org/spec/lti-ags/scope/score",
    "https://purl.imsglobal.org/spec/lti-tool-configuration": {
        "domain": "client.example.org",
		"deploymemt_id": "12094390",
        "target_link_uri": "https://client.example.org/lti",
        "custom_parameters": {
            "context_history": "$Context.id.history"
        },
        "claims": ["iss", "sub"],
        "messages": [
            {
                "type": "LtiDeepLinkingRequest",
                "target_link_uri": "https://client.example.org/lti/dl",
                "label": "Add a virtual garden"
            }
        ]
    }
}

*/

public class OpenIDClientRegistration extends org.tsugi.jackson.objects.JacksonBase {

    // "client_id": "709sdfnjkds12", (returned value)
	@JsonProperty("client_id")
	public String client_id;

    // "registration_client_uri": "https://server.example.com/connect/register?client_id=709sdfnjkds12",
	// (returned value)
	@JsonProperty("registration_client_uri")
	public String registration_client_uri;

	// "application_type": "web",
	@JsonProperty("application_type")
	public String application_type;

	// "response_types": ["id_token"],
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
	@JsonProperty("response_types")
	public List<String> response_types = new ArrayList<String>();

	// "grant_types": ["implict", "client_credentials"],
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
	@JsonProperty("grant_types")
	public List<String> grant_types = new ArrayList<String>();

	// "initiate_login_uri": "https://client.example.org/lti",
	@JsonProperty("initiate_login_uri")
	public String initiate_login_uri;

    // "redirect_uris":
    //   ["https://client.example.org/callback",
    //    "https://client.example.org/callback2"],
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
	@JsonProperty("redirect_uris")
	public List<String> redirect_uris = new ArrayList<String>();

    // "client_name": "Virtual Garden",
    // "client_name#ja": "バーチャルガーデン",
	@JsonProperty("client_name")
	public String client_name;

    // "jwks_uri": "https://client.example.org/.well-known/jwks.json",
	@JsonProperty("jwks_uri")
	public String jwks_uri;

    // "logo_uri": "https://client.example.org/logo.png",
	@JsonProperty("logo_uri")
	public String logo_uri;

    // "client_uri": "https://client.example.org",
    // "client_uri#ja": "https://client.example.org?lang=ja",
	@JsonProperty("client_uri")
	public String client_uri;

    // "policy_uri": "https://client.example.org/privacy",
    // "policy_uri#ja": "https://client.example.org/privacy?lang=ja",
	@JsonProperty("policy_uri")
	public String policy_uri;

    // "tos_uri": "https://client.example.org/tos",
    // "tos_uri#ja": "https://client.example.org/tos?lang=ja",
	@JsonProperty("tos_uri")
	public String tos_uri;

    // "token_endpoint_auth_method": "private_key_jwt",
	@JsonProperty("token_endpoint_auth_method")
	public String token_endpoint_auth_method;

    // "contacts": ["ve7jtb@example.org", "mary@example.org"],
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
	@JsonProperty("contacts")
	public List<String> contacts = new ArrayList<String>();

    // "scope": "https://purl.imsglobal.org/spec/lti-ags/scope/score",
	// This is a space-separated list of scopes - it is not an array
	@JsonProperty("scope")
	public String scope;

	@JsonProperty("https://purl.imsglobal.org/spec/lti-tool-configuration")
	public LTIToolConfiguration lti_tool_configuration;

   // Constructor for LTI requirements
    public OpenIDClientRegistration() {
		this.application_type = "web";
		this.response_types.add("id_token");
		this.grant_types.add("implict");
		this.grant_types.add("client_credentials");
		this.token_endpoint_auth_method = "private_key_jwt";
    }

}
