package org.tsugi.lti13.objects;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

/*
    "https:\/\/purl.imsglobal.org\/spec\/lti-ags\/claim\/endpoint": {
        "scope": [
            "https:\/\/purl.imsglobal.org\/spec\/lti-ags\/scope\/result.readonly",
            "https:\/\/purl.imsglobal.org\/spec\/lti-ags\/scope\/score",
            "https:\/\/purl.imsglobal.org\/spec\/lti-ags\/scope\/lineitem.readonly",
            "https:\/\/purl.imsglobal.org\/spec\/lti-ags\/scope\/lineitem",
     ],
        "lineitems": "https:\/\/lti-ri.imsglobal.org\/platforms\/7\/contexts\/6\/line_items",
        "lineitem": "https:\/\/lti-ri.imsglobal.org\/platforms\/7\/contexts\/6\/line_items\/9"
    },
 */
public class Endpoint extends org.tsugi.jackson.objects.JacksonBase {

	@JsonProperty("scope")
	public List<String> scope = new ArrayList<String>();
	@JsonProperty("lineitems")
	public String lineitems;
	@JsonProperty("lineitem")
	public String lineitem;
}
