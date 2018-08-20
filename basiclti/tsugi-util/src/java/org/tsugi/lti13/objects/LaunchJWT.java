
package org.tsugi.lti13.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")

public class LaunchJWT extends BaseJWT {

    public static String CLAIM_PREFIX = "https://purl.imsglobal.org/spec/lti/claim/";

    public static String ROLE_LEARNER = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Learner";
    public static String ROLE_INSTRUCTOR = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Instructor";

    @JsonProperty("https://purl.imsglobal.org/spec/lti/claim/deployment_id")
    public String deployment_id;

    @JsonProperty("https://purl.imsglobal.org/spec/lti/claim/message_type")
    public String message_type;
    @JsonProperty("https://purl.imsglobal.org/spec/lti/claim/version")
    public String version;
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

    @JsonProperty("https://purl.imsglobal.org/spec/lti/claim/roles")
    public List<String> roles = new ArrayList<String>();
    @JsonProperty("https://purl.imsglobal.org/spec/lti/claim/role_scope_mentor")
    public List<String> role_scope_mentor = new ArrayList<String>();

    @JsonProperty("https://purl.imsglobal.org/spec/lti/claim/launch_presentation")
    public LaunchPresentation launch_presentation;

    @JsonProperty("https://purl.imsglobal.org/spec/lti/claim/resource_link")
    public ResourceLink resource_link;

    @JsonProperty("https://purl.imsglobal.org/spec/lti/claim/context")
    public Context context;

    @JsonProperty("https://purl.imsglobal.org/spec/lti/claim/tool_platform")
    public ToolPlatform tool_platform;

    @JsonProperty("https://purl.imsglobal.org/spec/lti/claim/lis")
    public LaunchLIS lis;

    @JsonProperty("https://purl.imsglobal.org/spec/lti-ags/claim/endpoint")
    public Endpoint endpoint;

    @JsonProperty("https://purl.imsglobal.org/spec/lti-bo/claim/basicoutcome")
    public BasicOutcome basicoutcome;

    // Constructor
    public LaunchJWT() {
        this.message_type = "LtiResourceLinkRequest";
        this.version = "1.3.0";
        this.launch_presentation = new LaunchPresentation();
        this.resource_link = new ResourceLink();
        this.context = new Context();
        this.tool_platform = new ToolPlatform();
    }

}
