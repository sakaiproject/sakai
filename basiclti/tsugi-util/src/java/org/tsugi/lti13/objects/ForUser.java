package org.tsugi.lti13.objects;

import java.util.List;
import java.util.ArrayList;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")

/*
 * user_id (Required): id of the graded user, as identified by sub claim for launches done by that user.
 *
 * person_sourcedid: LIS sourced id of the graded user.
 *
 * given_name: Per OIDC specifcations, given name(s) or first name(s) of the graded user. Note that in some cultures, people can have multiple given names; all can be present, with the names being separated by space characters.
 *
 * family_name: Per OIDC specifcations, surname(s) or last name(s) of the graded user. Note that in some cultures, people can have multiple family names or no family name; all can be present, with the names being separated by space characters.
 *
 * name: Per OIDC specifcations, graded user's full name in displayable form including all name parts, possibly including titles and suffixes, ordered according to the graded user's locale and preferences.
 *
 * email: Per OIDC specifcations, graded user's preferred e-mail address.
 *
 * roles: Roles in the context as defined in LTI 1.3 Core specifications.
 */

public class ForUser {
	// Required
	@JsonProperty("user_id")
	public String user_id;

	// Optional
    @JsonProperty("given_name")
    public String given_name;
    @JsonProperty("family_name")
    public String family_name;
    @JsonProperty("picture")
    public String picture;
    @JsonProperty("email")
    public String email;
    @JsonProperty("name")
    public String name;
    @JsonProperty("https://purl.imsglobal.org/spec/lti/claim/roles")
    public List<String> roles = null;

	// TODO: Ask IMS why these are not there
    @JsonProperty("middle_name")
    public String middle_name;
    @JsonProperty("locale")
    public String locale;
}
