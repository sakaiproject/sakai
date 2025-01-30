package org.tsugi.shared.objects;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

/*  https://www.imsglobal.org/spec/lti-ags/v2p0#line-item-service
    "something": {
		"startDateTime": "2018-03-06T20:05:02Z",
		"endDateTime": "2018-04-06T22:05:03Z"
	}
 */
// TODO: Where did the scoreUrl and resultUrl end up?
public class DateRange extends org.tsugi.jackson.objects.JacksonBase {

	@JsonProperty("startDateTime")
	public String startDateTime;

	@JsonProperty("endDateTime")
	public String endDateTime;

}
