
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
    "code",
    "vendor"
})
public class Product_family {

    @JsonProperty("code")
    private String code;
    @JsonProperty("vendor")
    private Vendor vendor;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Product_family(String product_code, String vendor_code, String vendor_name, 
        String vendor_description, String vendor_website, String vendor_contact) {
        this.code = product_code;
        this.vendor = new Vendor(vendor_code, vendor_name, vendor_description, vendor_website, vendor_contact);
    }

    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    @JsonProperty("vendor")
    public Vendor getVendor() {
        return vendor;
    }

    @JsonProperty("vendor")
    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
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
