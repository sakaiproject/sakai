package org.tsugi.lti13.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;
import java.util.ArrayList;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

/* Spec in Draft - This is from Paul G
 
      $pnpHost = 'https://pnp.amp-up.io';
      $pnpBase = '/ims/afapnp/v1p0';
      $requestparams['launch_pnp_settings_service_url'] = $pnpHost . $pnpBase . '/users/' . $USER->id . '/afapnprecords';

      $pnp_launch_settings = array(
        'pnp_settings_service_url' => $parms['launch_pnp_settings_service_url'],
        'scope' => array('https://purl.imsglobal.org/spec/lti-pnp/scope/pnpsettings.readonly'),
        'pnp_supported_versions'  => array('http://purl.imsglobal.org/spec/afapnp/v1p0/schema/openapi/afapnpv1p0service_openapi3_v1p0')
      );
      $payload['https://purl.imsglobal.org/spec/lti-pnp/claim/pnpservice'] = $pnp_launch_settings;
*/

public class PNPService extends org.tsugi.jackson.objects.JacksonBase {
    public static String SCOPE_PNP_READONLY = "https://purl.imsglobal.org/spec/lti-pnp/scope/pnpsettings.readonly";
    public static String VERSION_1_0 = "http://purl.imsglobal.org/spec/afapnp/v1p0/schema/openapi/afapnpv1p0service_openapi3_v1p0";

    @JsonProperty("scope")
    public List<String> scope = new ArrayList<String>();
    @JsonProperty("pnp_settings_service_url")
    public String pnp_settings_service_url;
    @JsonProperty("pnp_supported_versions")
    public List<String> pnp_supported_versions = new ArrayList<String>();

    public PNPService() {
        this.scope.add(SCOPE_PNP_READONLY);
        this.pnp_supported_versions.add(VERSION_1_0);
    }

}
