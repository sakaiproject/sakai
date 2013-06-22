
package org.imsglobal.lti2.objects;

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
    "product_name",
    "product_version",
    "description",
    "technical_description",
    "product_family"
})
public class Product_info {

    @JsonProperty("product_name")
    private Product_name product_name;
    @JsonProperty("product_version")
    private String product_version;
    @JsonProperty("description")
    private Description description;
    @JsonProperty("technical_description")
    private Technical_description technical_description;
    @JsonProperty("product_family")
    private Product_family product_family;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Product_info(String instance_name, String instance_version, String instance_description, 
        Product_family product_family) {
        this.product_name = new Product_name(instance_name);
        this.product_version = instance_version;
        this.description = new Description(instance_description);
        this.product_family = product_family;
    }

    @JsonProperty("product_name")
    public Product_name getProduct_name() {
        return product_name;
    }

    @JsonProperty("product_name")
    public void setProduct_name(Product_name product_name) {
        this.product_name = product_name;
    }

    @JsonProperty("product_version")
    public String getProduct_version() {
        return product_version;
    }

    @JsonProperty("product_version")
    public void setProduct_version(String product_version) {
        this.product_version = product_version;
    }

    @JsonProperty("description")
    public Description getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(Description description) {
        this.description = description;
    }

    @JsonProperty("technical_description")
    public Technical_description getTechnical_description() {
        return technical_description;
    }

    @JsonProperty("technical_description")
    public void setTechnical_description(Technical_description technical_description) {
        this.technical_description = technical_description;
    }

    @JsonProperty("product_family")
    public Product_family getProduct_family() {
        return product_family;
    }

    @JsonProperty("product_family")
    public void setProduct_family(Product_family product_family) {
        this.product_family = product_family;
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
