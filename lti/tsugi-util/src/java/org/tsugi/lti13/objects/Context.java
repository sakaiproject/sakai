package org.tsugi.lti13.objects;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

/*
    "https://purl.imsglobal.org/spec/lti/claim/context": {
        "id": "6",
        "label": "12345",
        "title": "qwertyuio",
        "type": [
            "0987654321"
        ]
    },
 */
public class Context extends org.tsugi.jackson.objects.JacksonBase {

	// Per Viktor, the short form is deprecated in LTI 1.3
	public static String COURSE_OFFERING = "http://purl.imsglobal.org/vocab/lis/v2/course#CourseOffering";

	@JsonProperty("id")
	public String id;
	@JsonProperty("label")
	public String label;
	@JsonProperty("title")
	public String title;
	@JsonProperty("type")
	public List<String> type = new ArrayList<String>();
}
