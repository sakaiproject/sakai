package org.sakaiproject.lti13.util;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.tsugi.ags2.objects.LineItem;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Generated("com.googlecode.jsonschema2pojo")

// https://www.imsglobal.org/spec/lti-ags/v2p0#line-item-service
//   2.1.4 Extensions
/*  application/vnd.ims.lis.v2.lineitem+json

    {
		"id" : "https://lms.example.com/context/2923/lineitems/1",
		"scoreMaximum" : 60,
		"label" : "Chapter 5 Test",
		"resourceId" : "a-9334df-33",
		"tag" : "grade",
		"resourceLinkId" : "1g3k4dlk49fk"
		"startDateTime": "2018-03-06T20:05:02Z",
		"endDateTime": "2018-04-06T22:05:03Z",
		"https://www.toolexample.com/lti/score": {
			"originality": 94,
			"submissionUrl": "https://www.toolexample.com/lti/score/54/5893/essay.pdf"
		}
	}
*/

public class SakaiLineItem extends LineItem {

	// SAK-40043
	@JsonProperty("https://www.sakailms.org/spec/lti-ags/v2p0/releaseToStudent")
	public Boolean releaseToStudent;

	@JsonProperty("https://www.sakailms.org/spec/lti-ags/v2p0/includeInComputation")
	public Boolean includeInComputation;
}

