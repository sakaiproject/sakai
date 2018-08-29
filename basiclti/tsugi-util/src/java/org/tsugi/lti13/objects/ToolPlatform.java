package org.tsugi.lti13.objects;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")

/*
    "https:\/\/purl.imsglobal.org\/spec\/lti\/claim\/tool_platform": {
        "name": "LILI Hackathon game thing",
        "contact_email": "",
        "description": "",
        "url": "",
        "product_family_code": "",
        "version": "1.0"
    },
 */
public class ToolPlatform {

	@JsonProperty("name")
	public String name;
	@JsonProperty("contact_email")
	public String contact_email;
	@JsonProperty("description")
	public String description;
	@JsonProperty("url")
	public String url;
	@JsonProperty("product_family_code")
	public String product_family_code;
	@JsonProperty("version")
	public String version;
}
