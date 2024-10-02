package org.tsugi.nrps.objects;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.tsugi.jackson.objects.JacksonBase;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

// https://www.imsglobal.org/spec/lti-nrps/v2p0
/*  

{
  "status": "Active",
  "name": "Jane Q. Public",
  "picture": "https://platform.example.edu/jane.jpg",
  "given_name": "Jane",
  "family_name": "Doe",
  "middle_name": "Marie",
  "email": "jane@platform.example.edu",
  "user_id": "0ae836b9-7fc9-4060-006f-27b2066ac545",
  "lis_person_sourcedid": "59254-6782-12ab",
  "lti11_legacy_user_id": "668321221-2879",
  "roles": [
    "Instructor",
    "Mentor"
  ],
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
}

 */
public class Member extends JacksonBase  {

	@JsonProperty("status")
	public String status;
	public final String STATUS_ACTIVE = "active";

	@JsonProperty("given_name")
	public String given_name;
	@JsonProperty("family_name")
	public String family_name;
	@JsonProperty("middle_name")
	public String middle_name;
	@JsonProperty("picture")
	public String picture;
	@JsonProperty("email")
	public String email;
	@JsonProperty("name")
	public String name;
	@JsonProperty("locale")
	public String locale;

	// a.k.a Subject
	@JsonProperty("user_id")
	public String user_id;

	@JsonProperty("lis_person_sourcedid")
	public String lis_person_sourcedid;

	@JsonProperty("lti11_legacy_user_id")
	public String lti11_legacy_user_id;

	@JsonProperty("roles")
	public List<String> roles = new ArrayList<String>();

	@JsonProperty("message")
	public List<MemberMessage> message;

}
