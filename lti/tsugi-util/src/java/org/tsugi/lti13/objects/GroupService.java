package org.tsugi.lti13.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;
import java.util.ArrayList;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

/*

   https://www.imsglobal.org/spec/lti-gs/v1p0#claim-for-inclusion-in-lti-messages

   "https://purl.imsglobal.org/spec/lti-gs/claim/groupsservice": {
       "scope": [
         "https://purl.imsglobal.org/spec/lti-gs/scope/contextgroup.readonly"
       ],
       "context_groups_url": "https://www.myuniv.example.com/2344/groups",
       "context_group_sets_url": "https://www.myuniv.example.com/2344/groups/sets",
       "service_versions": ["1.0"]
   }

*/
public class GroupService extends org.tsugi.jackson.objects.JacksonBase {
    public static String SCOPE_CONTEXTGROUP_READONLY = "https://purl.imsglobal.org/spec/lti-gs/scope/contextgroup.readonly";

    @JsonProperty("scope")
    public List<String> scope = new ArrayList<String>();
    @JsonProperty("context_groups_url")
    public String context_groups_url;
    @JsonProperty("context_group_sets_url")
    public String context_group_sets_url;
    @JsonProperty("service_versions")
    public List<String> service_versions = new ArrayList<String>();

    public GroupService() {
        this.scope.add(SCOPE_CONTEXTGROUP_READONLY);
        this.service_versions.add("1.0");
    }

}
