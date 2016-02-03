
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
    "guid",
    "product_info",
    "support"
})
public class Product_instance {

    @JsonProperty("guid")
    private String guid;
    @JsonProperty("product_info")
    private Product_info product_info;
    @JsonProperty("service_owner")
    private Service_owner service_owner;
    @JsonProperty("service_provider")
    private Service_provider service_provider;
    @JsonProperty("support")
    private Support support;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Product_instance(LTI2Config cnf) {
        this.guid = cnf.getGuid();
        this.product_info = new Product_info(cnf);
        this.service_owner = new Service_owner(cnf);
        this.service_provider = new Service_provider(cnf);
        this.support = new Support(cnf.getSupport_email());
    }

    @JsonProperty("guid")
    public String getGuid() {
        return guid;
    }

    @JsonProperty("guid")
    public void setGuid(String guid) {
        this.guid = guid;
    }

    @JsonProperty("product_info")
    public Product_info getProduct_info() {
        return product_info;
    }

    @JsonProperty("product_info")
    public void setProduct_info(Product_info product_info) {
        this.product_info = product_info;
    }

    @JsonProperty("service_owner")
    public Service_owner getService_owner() {
        return service_owner;
    }

    @JsonProperty("service_owner")
    public void setService_owner(Service_owner service_owner) {
        this.service_owner = service_owner;
    }

    @JsonProperty("service_provider")
    public Service_provider getService_provider() {
        return service_provider;
    }

    @JsonProperty("service_provider")
    public void setService_provider(Service_provider service_provider) {
        this.service_provider = service_provider;
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
