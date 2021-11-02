package org.sakaiproject.lti13.util;

import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.tsugi.lti13.objects.DeepLink;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Generated("com.googlecode.jsonschema2pojo")

/*  https://www.imsglobal.org/spec/lti-dl/v2p0

"lineItem": {
  "scoreMaximum": 87,
  "label": "Chapter 12 quiz",
  "resourceId": "xyzpdq1234",
  "tag": "originality"
},
"available": {
  "startDateTime": "2018-02-06T20:05:02Z",
  "endDateTime": "2018-03-07T20:05:02Z"
},
"submission": {
  "endDateTime": "2018-03-06T20:05:02Z"
},

*/

public class SakaiDeepLink extends DeepLink {

	// SAK-44380 - Add Sakai extensions around placement for Deep  Link requests

	// Parameters for Content Item Flows
	public static String PLACEMENT_LESSONS = "lessons";
	public static String PLACEMENT_EDITOR = "editor";
	public static String PLACEMENT_ASSIGNMENT = "assignment";

	@JsonProperty("https://www.sakailms.org/spec/lti-dl/extensions")
	public Map<String, String> sakai_extensions;

	@JsonProperty("https://www.sakailms.org/spec/lti-dl/placement")
	public String sakai_placement;

	// This is moving toward being an official accept_lineitem claim
	@JsonProperty("https://www.sakailms.org/spec/lti-dl/accept_lineitem")
	public Boolean sakai_accept_lineitem;

	@JsonProperty("https://www.sakailms.org/spec/lti-dl/accept_available")
	public Boolean sakai_accept_available;

	@JsonProperty("https://www.sakailms.org/spec/lti-dl/accept_submission")
	public Boolean sakai_accept_submission;

}

