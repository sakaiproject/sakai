package org.tsugi.lti13.objects;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")

/*
	"https://purl.imsglobal.org/spec/lti-nrps/claim/namesroleservice": {
		"context_memberships_url": "https://www.myuniv.example.com/2344/memberships",
		"service_version": "2.0"
	}
 */
public class NamesAndRoles {

	// TODO: What do these mean?
	public static String SERVICE_VERSION_LTI13 = "2.0";  // Like WTF?  But tis true.

	@JsonProperty("context_memberships_url")
	public String context_memberships_url;
	@JsonProperty("service_version")
	public String service_version;

	public NamesAndRoles() {
		this.service_version = SERVICE_VERSION_LTI13;
	}
}
