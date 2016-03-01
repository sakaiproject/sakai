
package org.tsugi.casa.objects;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.tsugi.jackson.objects.JacksonBase;
import org.tsugi.casa.CASAUtil;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "timestamp",
    "uri",
    "share",
    "propagate",
    "use" 
})

public class Original extends JacksonBase {

    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("uri")
    private String uri;
    @JsonProperty("share")
    private Boolean share = Boolean.TRUE;
    @JsonProperty("propagate")
    private Boolean propagate = Boolean.TRUE;
    @JsonProperty("use")
    private Use use;

    // Constructor
    public Original(Use use) {
	this.use = use;
    }

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }

    @JsonProperty("uri")
    public void setUri(String uri) {
        this.uri = uri;
    }

    @JsonProperty("share")
    public Boolean getShare() {
        return share;
    }

    @JsonProperty("share")
    public void setShare(Boolean share) {
        this.share = share;
    }

    @JsonProperty("propagate")
    public Boolean getPropagate() {
        return propagate;
    }

    @JsonProperty("propagate")
    public void setPropagate(Boolean propagate) {
        this.propagate = propagate;
    }




/*

From: https://gist.github.com/pfgray/acca0e7966e452cb7c58

"original":{
    "timestamp":"2015-01-02T22:17:00.371Z",
    "uri":"http://lti-provider.paulgray.net",
    "share":true,
    "propagate":true,
    "use":{ 
      "f6820326-5ea3-4a02-840d-7f91e75eb01b":{
        "registration_url":"http://lti-provider.paulgray.net/register",
        "launch_url":"http://www.google.com"
      },
      "1f2625c2-615f-11e3-bf13-d231feb1dc81":"Local Mock Lti2 Provider",
      "d59e3a1f-c034-4309-a282-60228089194e":[{"name":"Paul Gray","email":"pfbgray@gmail.com"}],
      "c80df319-d5da-4f59-8ca3-c89b234c5055":["dev","lti"],
      "c6e33506-b170-475b-83e9-4ecd6b6dd42a":["lti"],
      "d25b3012-1832-4843-9ecf-3002d3434155":"http://www.iconsdb.com/icons/preview/green/literature-xxl.png",

    },
*/

}

