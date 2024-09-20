package org.tsugi.lti13.objects;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

/*
    "https://purl.imsglobal.org/spec/lti/claim/lis": {
      "person_sourcedid": "example.edu:71ee7e42-f6d2-414a-80db-b69ac2defd4",
      "course_offering_sourcedid": "example.edu:SI182-F16",
      "course_section_sourcedid": "example.edu:SI182-001-F16"
    }
 */
public class LaunchLIS extends org.tsugi.jackson.objects.JacksonBase {

	@JsonProperty("person_sourcedid")
	public String person_sourcedid;
	@JsonProperty("course_offering_sourcedid")
	public String course_offering_sourcedid;
	@JsonProperty("course_section_sourcedid")
	public String course_section_sourcedid;
	@JsonProperty("version")
	public List<String> version;
}
