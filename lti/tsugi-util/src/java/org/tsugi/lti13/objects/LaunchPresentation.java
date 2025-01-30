package org.tsugi.lti13.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

/*
    "https:\/\/purl.imsglobal.org\/spec\/lti\/claim\/launch_presentation": {
        "document_target": "iframe",
        "height": 320,
        "width": 240,
        "return_url": "https:\/\/lti-ri.imsglobal.org\/platforms\/7\/returns"
    },
 */
public class LaunchPresentation extends org.tsugi.jackson.objects.JacksonBase {

	@JsonProperty("document_target")
	public String document_target = "iframe";
	@JsonProperty("height")
	public Integer height;
	@JsonProperty("width")
	public Integer width;
	@JsonProperty("return_url")
	public String return_url;

	@JsonProperty("css_url")
	public String css_url;

}
