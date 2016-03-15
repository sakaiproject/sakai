
package org.tsugi.casa.objects;

import java.util.ArrayList;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.tsugi.jackson.objects.JacksonBase;
import org.tsugi.casa.CASAUtil;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "launch_url",
    "registration_url"
})

public class Launch extends JacksonBase {

    @JsonProperty("launch_url")
    private String launch_url;
    @JsonProperty("registration_url")
    private String registration_url;

    // Constructor
    public Launch() {
    }

    @JsonProperty("launch_url")
    public String getLaunch_url() {
        return launch_url;
    }

    @JsonProperty("launch_url")
    public void setLaunch_url(String launch_url) {
        this.launch_url = launch_url;
    }

    @JsonProperty("registration_url")
    public String getRegistration_url() {
        return registration_url;
    }

    @JsonProperty("registration_url")
    public void setRegistration_url(String registration_url) {
        this.registration_url = registration_url;
    }

}

