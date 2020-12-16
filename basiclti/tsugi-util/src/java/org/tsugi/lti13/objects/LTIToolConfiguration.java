package org.tsugi.lti13.objects;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")

/*
    // "https://purl.imsglobal.org/spec/lti-tool-configuration": {
    //     "domain": "client.example.org",
    //     "description": "Learn Botany by tending to your little (virtual) garden.",
    //     "description#ja": "小さな（仮想）庭に行くことで植物学を学びましょう。",
    //     "target_link_uri": "https://client.example.org/lti",
    //     "custom_parameters": {
    //         "context_history": "$Context.id.history"
    //     },
    //     "claims": ["iss", "sub", "name", "given_name", "family_name"],
    //     "messages": [
    //         {
    //             "type": "LtiDeepLinkingRequest",
    //             "target_link_uri": "https://client.example.org/lti/dl",
    //             "label": "Add a virtual garden",
    //             "label#ja": "バーチャルガーデンを追加する",
    //         }
    //     ]
    // }
 */
public class LTIToolConfiguration {

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

    // "custom_parameters": {
    //     "context_history": "$Context.id.history"
    // },
	@JsonProperty("custom_parameters")
	public Map<String, String> custom_parameters = new TreeMap<String, String>();

	@JsonProperty("variables")
	public List<String> variables = new ArrayList<String>();
}
