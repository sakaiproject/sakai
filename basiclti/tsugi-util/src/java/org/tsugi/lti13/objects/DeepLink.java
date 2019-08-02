package org.tsugi.lti13.objects;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")

/*
 * Specification:
 * https://www.imsglobal.org/spec/lti-dl/v2p0

 "https://purl.imsglobal.org/spec/lti-dl/claim/deep_linking_settings": {
	"accept_types": ["link", "file", "html", "ltiResourceLink", "image"],
	"accept_media_types": "image/*,text/html",
	"accept_presentation_document_targets": ["iframe", "window", "embed"],
	"accept_multiple": true,
	"auto_create": true,
	"title": "This is the default title",
	"text": "This is the default text",
	"data": "Some random opaque data that MUST be sent back",
	"deep_link_return_url": "https://platform.example/deep_links"
  }
 */

public class DeepLink {

	// TODO: What do these mean?
	public static final String ACCEPT_TYPE_LINK = "link";
	public static final String ACCEPT_TYPE_FILE = "file";
	public static final String ACCEPT_TYPE_HTML = "html";
	public static final String ACCEPT_TYPE_LTILINK = "ltiResourceLink";
	public static final String ACCEPT_TYPE_IMAGE = "image";
	public static String[] ACCEPT_TYPE_ALL = {ACCEPT_TYPE_LINK, ACCEPT_TYPE_FILE,
	   ACCEPT_TYPE_HTML, ACCEPT_TYPE_LTILINK, ACCEPT_TYPE_IMAGE};


	public static String TARGET_IFRAME = "iframe";
	public static String TARGET_WINDOW = "window";
	public static String TARGET_EMBED = "embed";

	@JsonProperty("deep_link_return_url")
	public String deep_link_return_url;
	@JsonProperty("accept_types")
	public List<String> accept_types = new ArrayList<String>();
	@JsonProperty("accept_media_types")
	public String accept_media_types;
	@JsonProperty("accept_presentation_document_targets")
	public List<String> accept_presentation_document_targets = new ArrayList<String>();
	@JsonProperty("accept_multiple")
	public Boolean accept_multiple;
	@JsonProperty("auto_create")
	public Boolean auto_create;
	@JsonProperty("title")
	public String title;
	@JsonProperty("text")
	public String text;
	@JsonProperty("data")
	public String data;
}
