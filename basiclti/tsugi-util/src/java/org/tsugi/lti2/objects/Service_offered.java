
package org.tsugi.lti2.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    "@type",
    "@id",
    "endpoint",
    "format",
    "action"
})
public class Service_offered {

    @JsonProperty("@type")
    private String _type;
    @JsonProperty("@id")
    private String _id;
    @JsonProperty("endpoint")
    private String endpoint;
    @JsonProperty("format")
    private List<String> format = new ArrayList<String>();
    @JsonProperty("action")
    private List<String> action = new ArrayList<String>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Service_offered() { }

    public Service_offered(String endpoint, String id, String type, String format, String action) {
        this.endpoint = endpoint;
        this._id = id;
        this._type = type;
        this.format.add(format);
        this.action.add(action);
    }

    public Service_offered(String endpoint, String id, String type, String[] format, String[] action) {
        this.endpoint = endpoint;
        this._id = id;
        this._type = type;
        Collections.addAll(this.format, format);
        Collections.addAll(this.action, action);
    }
    
    @JsonProperty("@type")
    public String get_type() {
        return _type;
    }

    @JsonProperty("@type")
    public void set_type(String _type) {
        this._type = _type;
    }

    @JsonProperty("@id")
    public String get_id() {
        return _id;
    }

    @JsonProperty("@id")
    public void set_id(String _id) {
        this._id = _id;
    }

    @JsonProperty("endpoint")
    public String getEndpoint() {
        return endpoint;
    }

    @JsonProperty("endpoint")
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @JsonProperty("format")
    public List<String> getFormat() {
        return format;
    }

    @JsonProperty("format")
    public void setFormat(List<String> format) {
        this.format = format;
    }

    @JsonProperty("action")
    public List<String> getAction() {
        return action;
    }

    @JsonProperty("action")
    public void setAction(List<String> action) {
        this.action = action;
    }

	// Convienence methods
    public void setAction(String action) {
		this.action = new ArrayList<String>();
        this.action.add(action);
    }

    public void setAction(String [] actions) {
		this.action = new ArrayList<String>();
        Collections.addAll(this.action, actions);
    }

    public void setFormat(String format) {
		this.format = new ArrayList<String>();
        this.format.add(format);
    }

    public void setFormat(String [] format) {
		this.format = new ArrayList<String>();
        Collections.addAll(this.format, format);
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
