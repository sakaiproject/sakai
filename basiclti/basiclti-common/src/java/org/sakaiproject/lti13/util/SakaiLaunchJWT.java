package org.sakaiproject.lti13.util;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.tsugi.lti13.objects.LaunchJWT;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Generated("com.googlecode.jsonschema2pojo")

public class SakaiLaunchJWT extends LaunchJWT {

	@JsonProperty("https://www.sakailms.org/spec/lti/claim/extension")
    public SakaiExtension sakai_extension;

    @JsonProperty("https://purl.sakailms.org/spec/lti/claim/origin")
    public String origin;

    @JsonProperty("https://purl.sakailms.org/spec/lti/claim/postverify")
    public String postverify;

}

