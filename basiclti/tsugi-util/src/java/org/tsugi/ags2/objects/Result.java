package org.tsugi.ags2.objects;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")

/*  application/vnd.ims.lis.v2.resultcontainer+json

    {
		"id": "https://lms.example.com/context/2923/lineitems/1/results/5323497",
		"scoreOf": "https://lms.example.com/context/2923/lineitems/1",
		"userId": "5323497",
		"resultScore": 0.83,
		"resultMaximum": 1,
		"comment": "This is exceptional work."
	  }
 */

public class Result { // This is all output-only
	@JsonProperty("id")
	public String id;
	@JsonProperty("scoreOf")
	public String scoreOf;
	@JsonProperty("userId")
	public String userId;
	@JsonProperty("resultScore")
	public Double resultScore;
	@JsonProperty("resultMaximum")
	public Double resultMaximum;
}
