package org.tsugi.lti13.objects;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")

/*
 
    https://www.imsglobal.org/spec/lti/v1p3/migr#lti-1-1-migration-claim

    sign=base64(hmac_sha256(utf8bytes('179248902&689302&https://lmsvendor.com&PM48OJSfGDTAzAo&1551290856&172we8671fd8z'), utf8bytes('my-lti11-secret')))

	{
		"nonce": "172we8671fd8z",
		"iat": 1551290796,
		"exp": 1551290856,
		"iss": "https://lmsvendor.com",
		"aud": "PM48OJSfGDTAzAo",
		"sub": "3",
		"https://purl.imsglobal.org/spec/lti/claim/deployment_id": "689302",
		"https://purl.imsglobal.org/spec/lti/claim/lti1p1": {
			"user_id": "34212",
			"oauth_consumer_key": "179248902",
			"oauth_consumer_key_sign": "lWd54kFo5qU7xshAna6v8BwoBm6tmUjc6GTax6+12ps="
		}
	}

*/
public class LTI11Transition {
	@JsonProperty("user_id")
	public String user_id;
	@JsonProperty("oauth_consumer_key")
	public String oauth_consumer_key;
	@JsonProperty("oauth_consumer_key_sign")
	public String oauth_consumer_key_sign;
}
