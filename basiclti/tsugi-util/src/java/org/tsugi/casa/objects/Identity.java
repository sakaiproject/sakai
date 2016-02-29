
package org.tsugi.casa.objects;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.tsugi.jackson.objects.JacksonBase;
import org.tsugi.casa.CASAUtil;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "originator_id",
    "id"
})

public class Identity extends JacksonBase {

    @JsonProperty("originator_id")
    private String originator_id;
    @JsonProperty("id")
    private String id;

    // Constructor
    public Identity(String originator_id, String id) {
	this.originator_id = originator_id;
	this.id = id;
    }

    @JsonProperty("originator_id")
    public String getOriginator_id() {
        return originator_id;
    }

    @JsonProperty("originator_id")
    public void setOriginator_id(String originator_id) {
        this.originator_id = originator_id;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

}

