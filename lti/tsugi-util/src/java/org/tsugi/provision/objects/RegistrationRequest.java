
package org.tsugi.provision.objects;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.tsugi.jackson.objects.JacksonBase;

import org.tsugi.lti13.objects.BaseJWT;
import org.tsugi.deeplink.objects.ContentItem;

// https://www.imsglobal.org/spec/lti-dr/v1p0

/*

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

*/

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

public class RegistrationRequest extends BaseJWT {

	@JsonProperty("https://purl.imsglobal.org/spec/lti/claim/deployment_id")
    public String deployment_id;
    @JsonProperty("https://purl.imsglobal.org/spec/lti/claim/message_type")
    public String message_type = "LtiDeepLinkingResponse";
    @JsonProperty("https://purl.imsglobal.org/spec/lti/claim/version")
	public String version = "1.3.0";

    @JsonProperty("https://purl.imsglobal.org/spec/lti-dl/claim/content_items")
	public List<ContentItem> content_items = new ArrayList<ContentItem>();

	@JsonProperty("https://purl.imsglobal.org/spec/lti-dl/claim/data")
	public String data;
}

