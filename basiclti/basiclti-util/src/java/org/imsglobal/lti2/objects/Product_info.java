
package org.imsglobal.lti2.objects;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

@Generated("com.googlecode.jsonschema2pojo")
public class Product_info {

    private Product_name product_name;
    private String product_version;
    private Description description;
    private Technical_description technical_description;
    private Product_family product_family;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Product_name getProduct_name() {
        return product_name;
    }

    public void setProduct_name(Product_name product_name) {
        this.product_name = product_name;
    }

    public String getProduct_version() {
        return product_version;
    }

    public void setProduct_version(String product_version) {
        this.product_version = product_version;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public Technical_description getTechnical_description() {
        return technical_description;
    }

    public void setTechnical_description(Technical_description technical_description) {
        this.technical_description = technical_description;
    }

    public Product_family getProduct_family() {
        return product_family;
    }

    public void setProduct_family(Product_family product_family) {
        this.product_family = product_family;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperties(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
