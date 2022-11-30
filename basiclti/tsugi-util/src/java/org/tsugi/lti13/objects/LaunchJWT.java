package org.tsugi.lti13.objects;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;

import org.tsugi.lti13.LTI13ConstantsUtil;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

public class LaunchJWT extends BaseJWT {

	public static String CLAIM_PREFIX = "https://purl.imsglobal.org/spec/lti/claim/";

	public static final String MESSAGE_TYPE_LAUNCH = LTI13ConstantsUtil.MESSAGE_TYPE_LTI_RESOURCE;
	public static final String MESSAGE_TYPE_DEEP_LINK = LTI13ConstantsUtil.MESSAGE_TYPE_LTI_DEEP_LINKING_REQUEST;
	public static final String ROLE_LEARNER = LTI13ConstantsUtil.ROLE_LEARNER;
	public static final String ROLE_INSTRUCTOR = LTI13ConstantsUtil.ROLE_INSTRUCTOR;
	public static final String MESSAGE_TYPE_LTI_SUBMISSION_REVIEW_REQUEST = LTI13ConstantsUtil.MESSAGE_TYPE_LTI_SUBMISSION_REVIEW_REQUEST;
	public static final String MESSAGE_TYPE_LTI_DATA_PRIVACY_LAUNCH_REQUEST = LTI13ConstantsUtil.MESSAGE_TYPE_LTI_DATA_PRIVACY_LAUNCH_REQUEST;
	public static final String MESSAGE_TYPE_LTI_CONTEXT = LTI13ConstantsUtil.MESSAGE_TYPE_LTI_CONTEXT;

	@JsonProperty("https://purl.imsglobal.org/spec/lti/claim/deployment_id")
	public String deployment_id;

	@JsonProperty("https://purl.imsglobal.org/spec/lti/claim/target_link_uri")
	public String target_link_uri;
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
	@JsonProperty("locale")
	public String locale;
	@JsonProperty("https://purl.imsglobal.org/spec/lti/claim/custom")
	public Map<String, String> custom;

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

	@JsonProperty("https://purl.imsglobal.org/spec/lti-nrps/claim/namesroleservice")
	public NamesAndRoles names_and_roles;

	@JsonProperty("https://purl.imsglobal.org/spec/lti-dl/claim/deep_linking_settings")
	public DeepLink deep_link;

	@JsonProperty("https://purl.imsglobal.org/spec/lti/claim/lti1p1")
	public LTI11Transition lti11_transition;

	@JsonProperty("https://purl.imsglobal.org/spec/lti/claim/for_user")
	public ForUser for_user;

	// This is in LaunchJWTs
	@JsonProperty("nonce")
	public String nonce;

	// Constructor
	public LaunchJWT() {
		this(MESSAGE_TYPE_LAUNCH);
	}

	// Constructor
	public LaunchJWT(String messageType) {
		super();
		this.message_type = messageType;
		this.version = "1.3.0";
		this.launch_presentation = new LaunchPresentation();
		this.nonce = this.jti;
	}

	// Encode the rules for constructing a name
	@JsonIgnore
	public String getDisplayName() {
		if ( name != null ) return name;

		String display_name = "";
		if ( given_name != null ) display_name = given_name;
		if ( middle_name != null ) {
			if ( display_name.length() > 0 ) display_name = display_name + " ";
			display_name = display_name + middle_name;
		}
		if ( family_name != null ) {
			if ( display_name.length() > 0 ) display_name = display_name + " ";
			display_name = display_name + family_name;
		}
		display_name = display_name.trim();
		if ( display_name.length() < 1 ) display_name = null;
		return display_name;
	}

	@JsonIgnore
	public boolean isInstructor() {
		if ( roles == null ) return false;
		return roles.contains(ROLE_INSTRUCTOR);
	}

	@JsonIgnore
	public String getLTI11Roles() {
		if ( roles == null ) return null;

		StringBuilder roleStr = new StringBuilder();
		for (String role : roles) {
			if ( roleStr.length() > 0 ) roleStr.append(',');
			roleStr.append(role);
		}
		return roleStr.toString();
	}

}
