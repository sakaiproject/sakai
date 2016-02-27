
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
    "identity",
    "original"
    // TODO: "requires"
})

public class Application extends JacksonBase {

    @JsonProperty("identity")
    private Identity identity;
    @JsonProperty("original")
    private Original original;

    // Constructor
    public Application(Identity identity, Original original) {
	this.identity = identity;
	this.original = original;
    }

    @JsonProperty("identity")
    public Identity getIdentity() {
        return identity;
    }

    @JsonProperty("identity")
    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    @JsonProperty("original")
    public Original getOriginal() {
        return original;
    }

    @JsonProperty("original")
    public void setOriginal(Original original) {
        this.original = original;
    }


/*
From: https://gist.github.com/pfgray/acca0e7966e452cb7c58

[{
  "identity":{
    "originator_id":"a9a860ae-7c0f-4c12-a1cf-9fe490ee1f49",
    "id":"local-lti-provider"
  },
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
    "require":{}
  }
}]

*/
}

