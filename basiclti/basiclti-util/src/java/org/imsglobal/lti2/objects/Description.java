
package org.imsglobal.lti2.objects;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "default_value",
    "key"
})
public class Description {

    @JsonProperty("default_value")
    private String default_value;
    @JsonProperty("key")
    private String key;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Description(String description) {
        this.default_value = description;
        this.key =  "product.vendor.description";
    }

    @JsonProperty("default_value")
    public String getDefault_value() {
        return default_value;
    }

    @JsonProperty("default_value")
    public void setDefault_value(String default_value) {
        this.default_value = default_value;
    }

    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
