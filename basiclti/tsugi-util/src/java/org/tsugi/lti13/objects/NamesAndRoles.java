package org.tsugi.lti13.objects;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

/*
	"https://purl.imsglobal.org/spec/lti-nrps/claim/namesroleservice": {
		"context_memberships_url": "https://www.myuniv.example.com/2344/memberships",
		"service_versions" : ["2.0"]
	}
 */
public class NamesAndRoles extends org.tsugi.jackson.objects.JacksonBase {

	// TODO: What do these mean?
	public static String SERVICE_VERSION_LTI13 = "2.0";  // Like WTF?  But tis true.

	@JsonProperty("context_memberships_url")
	public String context_memberships_url;
	@JsonProperty("service_versions")
	public List<String> service_versions;

	public NamesAndRoles() {
		this.service_versions = new ArrayList<String>();
		this.service_versions.add(SERVICE_VERSION_LTI13);
	}
}
