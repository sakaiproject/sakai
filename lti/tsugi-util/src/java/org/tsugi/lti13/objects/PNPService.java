package org.tsugi.lti13.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;
import java.util.ArrayList;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

/* Spec in Draft - This is from Paul G
 
   "https://purl.imsglobal.org/spec/lti-afapnp/claim/afapnp-endpoint-service": {
		"afapnp_endpoint_url" : "https://pnp.amp-up.io/ims/afapnp/v1p0/users/2/activities/default/afapnprecords",
 		"scopes": [
			"https://purl.imsglobal.org/spec/lti-afapnp/scope/afapnprecord.readonly"
		]
	}
*/

public class PNPService extends org.tsugi.jackson.objects.JacksonBase {
    public static String CLAIM = "https://purl.imsglobal.org/spec/lti-afapnp/claim/afapnp-endpoint-service";
    public static String SCOPE_PNP_READONLY = "https://purl.imsglobal.org/spec/lti-afapnp/scope/afapnprecord.readonly";

    @JsonProperty("afapnp_endpoint_url")
    public String afapnp_endpoint_url;
    @JsonProperty("scope")
    public List<String> scope = new ArrayList<String>();

    public PNPService() {
        this.scope.add(SCOPE_PNP_READONLY);
    }

}
