package org.tsugi.nrps.objects;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.tsugi.jackson.objects.JacksonBase;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

// https://www.imsglobal.org/spec/lti-nrps/v2p0
/*  

  "message": [
    {
      "https://purl.imsglobal.org/spec/lti/claim/message_type": "LtiResourceLinkRequest",
      "https://purl.imsglobal.org/spec/lti-bo/claim/basicoutcome": {
        "lis_result_sourcedid": "example.edu:71ee7e42-f6d2-414a-80db-b69ac2defd4",
        "lis_outcome_service_url": "https://www.example.com/2344"
      },
      "https://purl.imsglobal.org/spec/lti/claim/custom": {
        "country": "Canada",
        "user_mobile": "123-456-7890"
      }
    }
  ]

 */
public class MemberMessage extends JacksonBase  {

	@JsonProperty("https://purl.imsglobal.org/spec/lti/claim/message_type")
	public String message_type;

	@JsonProperty("https://purl.imsglobal.org/spec/lti-bo/claim/basicoutcome")
    public Map<String, String> basicoutcome;

	@JsonProperty("https://purl.imsglobal.org/spec/lti/claim/custom")
    public Map<String, String> custom;

}
