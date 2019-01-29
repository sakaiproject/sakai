package org.tsugi.lti13.objects;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")

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
public class Endpoint {

	/**
	 * Tool can access the results for its line items
	 */
	public static String SCOPE_RESULT_READONLY = "https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly";
	/**
	 * Tool can publish score updates to the line items
	 */
	public static String SCOPE_SCORE = "https://purl.imsglobal.org/spec/lti-ags/scope/score";
	/**
	 * Tool can fully manage its line items including, adding and removing line items
	 */
	public static String SCOPE_LINEITEM = "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem";
	/**
	 * Tool can query its line line items - no modification allowed
	 */
	public static String SCOPE_LINEITEM_READONLY = "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem.readonly";

	@JsonProperty("scope")
	public List<String> scope = new ArrayList<String>();
	@JsonProperty("lineitems")
	public String lineitems;
	@JsonProperty("lineitem")
	public String lineitem;
}
