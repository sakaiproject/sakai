
package org.imsglobal.lti2.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    "@context",
    "@type",
    "@id",
    "lti_version",
    "guid",
    "product_instance",
    "capability_enabled",
    "service_offered"
})
public class ToolConsumer {

    @JsonProperty("@context")
    private List<String> _context = new ArrayList<String>();
    @JsonProperty("@type")
    private String _type;
    @JsonProperty("@id")
    private String _id;
    @JsonProperty("lti_version")
    private String lti_version;
    @JsonProperty("guid")
    private String guid;
    @JsonProperty("product_instance")
    private Product_instance product_instance;
    @JsonProperty("capability_enabled")
    private List<String> capability_enabled = new ArrayList<String>();
    @JsonProperty("service_offered")
    private List<Service_offered> service_offered = new ArrayList<Service_offered>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public static String[] STANDARD_CAPABILITIES = {
        "Person.name.given" , "Person.name.family" , "Person.email.primary" ,
        "User.image" , "Result.sourcedId" , "basic-lti-launch-request" , 
        "Result.autocreate",
        "Result.sourcedGUID" } ;

    // Constructor
    public ToolConsumer(String guid, Product_instance product_instance) {
        this._context.add("http://www.imsglobal.org/imspurl/lti/v2/ctx/ToolConsumerProfile");
        this._type = "ToolConsumerProfile";
        this.lti_version = "LTI-2p0";
        this.product_instance = product_instance;
    }

    @JsonProperty("@context")
    public List<String> get_context() {
        return _context;
    }

    @JsonProperty("@context")
    public void set_context(List<String> _context) {
        this._context = _context;
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

    @JsonProperty("lti_version")
    public String getLti_version() {
        return lti_version;
    }

    @JsonProperty("lti_version")
    public void setLti_version(String lti_version) {
        this.lti_version = lti_version;
    }

    @JsonProperty("guid")
    public String getGuid() {
        return guid;
    }

    @JsonProperty("guid")
    public void setGuid(String guid) {
        this.guid = guid;
    }

    @JsonProperty("product_instance")
    public Product_instance getProduct_instance() {
        return product_instance;
    }

    @JsonProperty("product_instance")
    public void setProduct_instance(Product_instance product_instance) {
        this.product_instance = product_instance;
    }

    @JsonProperty("capability_enabled")
    public List<String> getCapability_enabled() {
        return capability_enabled;
    }

    @JsonProperty("capability_enabled")
    public void setCapability_enabled(List<String> capability_enabled) {
        this.capability_enabled = capability_enabled;
    }

    @JsonProperty("service_offered")
    public List<Service_offered> getService_offered() {
        return service_offered;
    }

    @JsonProperty("service_offered")
    public void setService_offered(List<Service_offered> service_offered) {
        this.service_offered = service_offered;
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
