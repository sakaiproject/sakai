package org.tsugi.lti13.objects;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

/*
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
 */

// https://www.imsglobal.org/spec/lti-dr/v1p0#step-3-client-registration
// https://www.imsglobal.org/spec/lti-dr/v1p0#client-registration-response
public class LTIToolConfiguration extends org.tsugi.jackson.objects.JacksonBase {

	// "domain": "client.example.org",
	@JsonProperty("domain")
	public String domain;

	// "description": "Learn Botany by tending to your little (virtual) garden.",
	// "description#ja": "小さな（仮想）庭に行くことで植物学を学びましょう。",
	@JsonProperty("description")
	public String description;

	// "target_link_uri": "https://client.example.org/lti",
	@JsonProperty("target_link_uri")
	public String target_link_uri;

	/*
	 * In the case where a platform is combining registration and deployment of a tool, the platform may pass the
	 * LTI deployment_id associated with this client registration's deployment.  Response only.
	 */
	// "deployment_id": "42",
	@JsonProperty("deployment_id")
	public String deployment_id;

	// "custom_parameters": {
	//	 "context_history": "$Context.id.history"
	// },
	@JsonProperty("custom_parameters")
	public Map<String, String> custom_parameters = new TreeMap<String, String>();

	@JsonProperty("claims")
	public List<String> claims = new ArrayList<String>();

	@JsonProperty("variables")
	public List<String> variables = new ArrayList<String>();

	@JsonProperty("messages")
	public List<LTILaunchMessage> messages = new ArrayList<LTILaunchMessage>();

	public void addCommonClaims() {
		this.claims.add("iss");
		this.claims.add("sub");
		this.claims.add("name");
		this.claims.add("given_name");
		this.claims.add("family_name");
		this.claims.add("email");
	}
}
