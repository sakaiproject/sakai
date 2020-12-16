package org.tsugi.lti13.objects;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")

/*
 * This is F-ed up.   The subitted tool contifuration is different than the retrieved tool configuration.
 *
 * Request:
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
    "client_uri": "https://client.example.org",
    "client_uri#ja": "https://client.example.org?lang=ja",
    "policy_uri": "https://client.example.org/privacy",
    "policy_uri#ja": "https://client.example.org/privacy?lang=ja",
    "tos_uri": "https://client.example.org/tos",
    "tos_uri#ja": "https://client.example.org/tos?lang=ja",
    "token_endpoint_auth_method": "private_key_jwt",
       "contacts": ["ve7jtb@example.org", "mary@example.org"],
    "scope": "https://purl.imsglobal.org/spec/lti-ags/scope/score",
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

 * Response:
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
        "target_link_uri": "https://client.example.org/lti",
        "custom_parameters": {
            "context_history": "$Context.id.history"
        },
        "claims": ["iss", "sub", "name", "given_name", "family_name"],
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
public class ToolConfiguration {

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
	@JsonProperty("response_types")
	public List<String> response_types = new ArrayList<String>();

	// "grant_types": ["implict", "client_credentials"],
	@JsonProperty("grant_types")
	public List<String> grant_types = new ArrayList<String>();

	// "initiate_login_uri": "https://client.example.org/lti",
	@JsonProperty("initiate_login_uri")
	public String initiate_login_uri;

    // "redirect_uris":
    //   ["https://client.example.org/callback",
    //    "https://client.example.org/callback2"],
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
	@JsonProperty("contacts")
	public List<String> contacts = new ArrayList<String>();

    // "scope": "https://purl.imsglobal.org/spec/lti-ags/scope/score",
	// TODO: Should this be an array?
	@JsonProperty("scope")
	public String scope;

	@JsonProperty("https://purl.imsglobal.org/spec/lti-tool-configuration")
	public LTIToolConfiguration lti_tool_configuration;

   // Constructor
    public ToolConfiguration() {
		this.application_type = "web";
		this.response_types.add("id_token");
		this.grant_types.add("implict");
		this.grant_types.add("client_credentials");
		this.token_endpoint_auth_method = "private_key_jwt";
    }

}
