package org.tsugi.ags2.objects;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Generated("com.googlecode.jsonschema2pojo")

/*  https://www.imsglobal.org/spec/lti-ags/v2p0#line-item-service
    application/vnd.ims.lis.v2.lineitem+json

    {
		"id" : "https://lms.example.com/context/2923/lineitems/1",
		"scoreMaximum" : 60,
		"label" : "Chapter 5 Test",
		"resourceId" : "a-9334df-33",
		"tag" : "grade",
		"resourceLinkId" : "1g3k4dlk49fk",
		"startDateTime": "2018-03-06T20:05:02Z",
		"endDateTime": "2018-04-06T22:05:03Z",
		"submissionReview": {
			"reviewableStatus": ["InProgress", "Submitted", "Completed"],
			"label": "Open My Tool Viewer",
			"url": "https://platform.example.com/act/849023/sub",
			"custom": {
					"action": "review",
					"a_id": "23942"
			}
		}
	}
 */
// TODO: Where did the scoreUrl and resultUrl end up?
public class LineItem extends org.tsugi.jackson.objects.JacksonBase {

	public static final String MIME_TYPE = "application/vnd.ims.lis.v2.lineitem+json";
	public static final String MIME_TYPE_CONTAINER = "application/vnd.ims.lis.v2.lineitemcontainer+json";

	public static final String CONTENT_TYPE = MIME_TYPE + "; charset=utf-8";
	public static final String CONTENT_TYPE_CONTAINER = MIME_TYPE_CONTAINER + "; charset=utf-8";

	@JsonProperty("scoreMaximum")
	public Double scoreMaximum;
	@JsonProperty("label")
	public String label;
	@JsonProperty("resourceId")
	public String resourceId;
	@JsonProperty("tag")
	public String tag;

	@JsonProperty("startDateTime")
	public String startDateTime;

	@JsonProperty("endDateTime")
	public String endDateTime;

	@JsonProperty("submissionReview")
	public SubmissionReview submissionReview;

	@JsonProperty("id")
	public String id;			// Output only
	@JsonProperty("resourceLinkId")
	public String resourceLinkId;  // Output only
}
