package org.tsugi.lti2.objects;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.tsugi.lti2.LTI2Config;
import org.tsugi.basiclti.BasicLTIUtil;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "@id",
    "service_owner_name",
    "description",
    "timestamp",
    "support"
})
public class Service_owner {

    @JsonProperty("@id")
    private String _id;
    @JsonProperty("service_owner_name")
    private Service_owner_name service_owner_name;
    @JsonProperty("description")
    private Description description;
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("support")
    private Support support;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Service_owner(LTI2Config cnf) {
        this._id = cnf.getService_owner_id();
        this.service_owner_name = new Service_owner_name(cnf.getService_owner_owner_name());
        this.description = new Description(cnf.getService_owner_description());
        this.support = new Support(cnf.getService_owner_support_email());
        this.timestamp = BasicLTIUtil.getISO8601(null);
    }

    @JsonProperty("@id")
    public String get_id() {
        return _id;
    }

    @JsonProperty("@id")
    public void set_id(String _id) {
        this._id = _id;
    }

    @JsonProperty("service_owner_name")
    public Service_owner_name getService_owner_name() {
        return service_owner_name;
    }

    @JsonProperty("service_owner_name")
    public void setService_name(Service_owner_name service_owner_name) {
        this.service_owner_name = service_owner_name;
    }

    @JsonProperty("description")
    public Description getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(Description description) {
        this.description = description;
    }

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("support")
    public Support getSupport() {
        return support;
    }

    @JsonProperty("support")
    public void setSupport(Support support) {
        this.support = support;
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
