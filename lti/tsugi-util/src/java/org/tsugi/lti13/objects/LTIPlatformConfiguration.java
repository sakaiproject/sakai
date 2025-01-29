package org.tsugi.lti13.objects;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

/*
     "https://purl.imsglobal.org/spec/lti-platform-configuration": {
        "product_family_code": "ExampleLMS",
        "messages_supported": [
            {"type": "LtiResourceLinkRequest"},
            {"type": "LtiDeepLinkingRequest"}],
        "variables": ["CourseSection.timeFrame.end", "CourseSection.timeFrame.begin", "Context.id.history", "ResourceLink.id.history"]
    }
 */
public class LTIPlatformConfiguration extends org.tsugi.jackson.objects.JacksonBase {

	// Product identifier for the platform. 
	@JsonProperty("product_family_code")
	public String product_family_code;

	// Version of the software running the platform. 
	@JsonProperty("version")
	public String version;

	// An array of all supported LTI message types.
	@JsonProperty("messages_supported")
	public List<LTILaunchMessage> messages_supported = new ArrayList<LTILaunchMessage>();

	// An array of all variables supported for use as substitution parameters (optional)
	@JsonProperty("variables")
	public List<String> variables = new ArrayList<String>();
}
