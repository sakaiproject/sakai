package org.tsugi.lti13.objects;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")

/*
     "https://purl.imsglobal.org/spec/lti-platform-configuration ": {
        "product_family_code": "ExampleLMS",
        "messages_supported": [
            {"type": "LtiResourceLinkRequest"},
            {"type": "LtiDeepLinkingRequest"}],
        "variables": ["CourseSection.timeFrame.end", "CourseSection.timeFrame.begin", "Context.id.history", "ResourceLink.id.history"]
    }

	"messages": [
            {
                "type": "LtiDeepLinkingRequest",
                "target_link_uri": "https://client.example.org/lti/dl",
                "label": "Add a virtual garden",
				"placements": ["resourceLink", ... (TBD)]
            }
        ]

 */
public class LTIPlatformMessage {
	// Defined values in org.tsugi.lti13.objects.LaunchJWT.MESSAGE_TYPE_LAUNCH;
	@JsonProperty("type")
	public String type;
	@JsonProperty("target_link_uri")
	public String target_link_uri;
	@JsonProperty("label")
	public String label;
	// Array of placements indicating where the platform support this link type to be added when the tool is made available.
	// TODO: Define the constants that belong here
	@JsonProperty("placements")
    public List<String> placements = new ArrayList<String>();
}
