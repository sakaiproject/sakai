
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

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

public class RegistrationResponse extends BaseJWT {

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

