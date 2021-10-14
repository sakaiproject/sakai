package org.tsugi.ags2.objects;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Generated("com.googlecode.jsonschema2pojo")

/*  
       "submissionReview": {
            "reviewableStatus": ["InProgress", "Submitted", "Completed"],
            "label": "Open My Tool Viewer",
            "url": "https://platform.example.com/act/849023/sub",
            "custom": {
                    "action": "review",
                    "a_id": "23942"
            }
        }
 */
public class SubmissionReview extends org.tsugi.jackson.objects.JacksonBase {

	@JsonProperty("reviewableStatus")
	public List<String> reviewableStatus = new ArrayList<String>();
	@JsonProperty("label")
	public String label;
	@JsonProperty("resourceId")
	public String resourceId;
	@JsonProperty("url")
	public String url;

    @JsonProperty("custom")
    public Map<String, String> custom = new TreeMap<String, String>();
}
