package org.imsglobal.lti2.objects;

import org.imsglobal.lti2.LTI2Config;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

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
