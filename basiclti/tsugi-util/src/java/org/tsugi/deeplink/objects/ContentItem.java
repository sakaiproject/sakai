
package org.tsugi.deeplink.objects;

import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.tsugi.jackson.objects.JacksonBase;

import org.tsugi.shared.objects.DateRange;
import org.tsugi.shared.objects.SizedUrl;
import org.tsugi.ags2.objects.LineItem;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({
	"type",
	"title",
	"url",
	"text",
	"icon"
})

// This needs all the possible fields to avoid @SubType complexity
// And keep Jon Postel happy whilst parsing.
public class ContentItem extends JacksonBase {

	@JsonProperty("type")
    public String type;
    public static final String TYPE_LTIRESOURCELINK = "ltiResourceLink";

    @JsonProperty("title")
    public String title;

    @JsonProperty("url")
    public String url;

	@JsonProperty("text")
	public String text;

	@JsonProperty("html")
	public String html;

	@JsonProperty("icon")
	public SizedUrl icon;

	@JsonProperty("thumbnail")
	public SizedUrl thumbnail;

	@JsonProperty("lineItem")
	public MiniLineItem lineItem;

	@JsonProperty("available")
	public DateRange available;

	@JsonProperty("submission")
	public DateRange submission;

	@JsonProperty("custom")
	public Map<String, String> custom;

	// Define in more detail later
	@JsonProperty("window")
	public Map<String, String> window;
	public static final String WINDOW_TARGETNAME = "targetName";

	public void setWindowTarget(String target) {
		if ( this.window == null )  this.window = new HashMap<String, String>();
		this.window.put(LtiResourceLink.WINDOW_TARGETNAME, target);
	}

	// Define in more detail later
	@JsonProperty("iframe")
	public Map<String, String> iframe;
	public static final String IFRAME_HEIGHT = "height";
	public static final String IFRAME_WIDTH = "width";

	// Define in more detail later
	@JsonProperty("embed")
	public Map<String, String> embed;
	public static final String EMBED_HTML = "html";

}

/*
 
{
"type": "ltiResourceLink",
"title": "A title",
"text": "This is a link to an activity that will be graded",
"url": "https://lti.example.com/launchMe",
"icon": {
  "url": "https://lti.example.com/image.jpg",
  "width": 100,
  "height": 100
},
"thumbnail": {
  "url": "https://lti.example.com/thumb.jpg",
  "width": 90,
  "height": 90
},
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
"custom": {
  "quiz_id": "az-123",
  "duedate": "$Resource.submission.endDateTime"
},
"window": {
  "targetName": "examplePublisherContent"
},
"iframe": {
  "height": 890
}
},

 */

