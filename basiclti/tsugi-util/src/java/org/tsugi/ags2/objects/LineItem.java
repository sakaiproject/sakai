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

/*  application/vnd.ims.lis.v2.lineitem+json

    {
		"id" : "https://lms.example.com/context/2923/lineitems/1",
		"scoreMaximum" : 60,
		"label" : "Chapter 5 Test",
		"resourceId" : "a-9334df-33",
		"tag" : "grade",
		"resourceLinkId" : "1g3k4dlk49fk"
	}
 */
// TODO: Where did the scoreUrl and resultUrl end up?
public class LineItem {

	public static final String MIME_TYPE = "application/vnd.ims.lis.v2.lineitem+json";
	public static final String MIME_TYPE_CONTAINER = "application/vnd.ims.lis.v2.lineitemcontainer+json";

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

	@JsonProperty("id")
	public String id;			// Output only
	@JsonProperty("resourceLinkId")
	public String resourceLinkId;  // Output only
}
