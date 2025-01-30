package org.tsugi.nrps.objects;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.tsugi.jackson.objects.JacksonBase;

import org.tsugi.lti13.objects.Context;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

// https://www.imsglobal.org/spec/lti-nrps/v2p0
/*  

{
"id" : "https://lms.example.com/sections/2923/memberships?rlid=49566-rkk96",
"context": {
  "id": "2923-abc",
  "label": "CPS 435",
  "title": "CPS 435 Learning Analytics"
},
"members" : [
  {
    "status" : "Active",
    "name": "Jane Q. Public",
    "picture" : "https://platform.example.edu/jane.jpg",
    "given_name" : "Jane",
    "family_name" : "Doe",
    "middle_name" : "Marie",
    "email": "jane@platform.example.edu",
    "user_id" : "0ae836b9-7fc9-4060-006f-27b2066ac545",
    "lis_person_sourcedid": "59254-6782-12ab",
    "lti11_legacy_user_id": "668321221-2879",
    "roles": [
      "Instructor",
      "Mentor"
    ],
    "message" : [
      {
        "https://purl.imsglobal.org/spec/lti/claim/message_type" : "LtiResourceLinkRequest",
        "https://purl.imsglobal.org/spec/lti-bo/claim/basicoutcome" : {
          "lis_result_sourcedid": "example.edu:71ee7e42-f6d2-414a-80db-b69ac2defd4",
          "lis_outcome_service_url": "https://www.example.com/2344"
        },
        "https://purl.imsglobal.org/spec/lti/claim/custom": {
          "country" : "Canada",
          "user_mobile" : "123-456-7890"
        }
      }
    ]
  }
]
}

 */
public class Container extends JacksonBase  {

	@JsonProperty("id")
	public String id;

	@JsonProperty("context")
	public Context context;

	@JsonProperty("members")
	public List<Member> members;

}
