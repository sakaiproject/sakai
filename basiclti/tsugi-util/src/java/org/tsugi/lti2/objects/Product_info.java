
package org.tsugi.lti2.objects;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.tsugi.lti2.LTI2Config;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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

    public Product_info(LTI2Config cnf) {
        this.product_name = new Product_name(cnf.getProduct_info_product_name());
        this.product_version = cnf.getProduct_info_product_version();
        this.description = new Description(cnf.getProduct_info_product_description());
        this.product_family = new Product_family(cnf);
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
