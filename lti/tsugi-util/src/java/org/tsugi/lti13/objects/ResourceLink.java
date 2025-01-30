package org.tsugi.lti13.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

/*
    "https://purl.imsglobal.org/spec/lti/claim/resource_link": {
        "id": "5",
        "title": "poiuytrewq",
        "description": ""
    },
 */
public class ResourceLink extends org.tsugi.jackson.objects.JacksonBase {

	@JsonProperty("id")
	public String id;
	@JsonProperty("title")
	public String title;
	@JsonProperty("description")
	public String description;
}
