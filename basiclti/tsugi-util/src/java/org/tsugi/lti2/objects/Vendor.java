
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
    "code",
    "vendor_name",
    "description",
    "website",
    "timestamp",
    "contact"
})
public class Vendor {

    public static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ssz";

    @JsonProperty("code")
    private String code;
    @JsonProperty("vendor_name")
    private Name vendor_name;
    @JsonProperty("description")
    private Description description;
    @JsonProperty("website")
    private String website;
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("contact")
    private Contact contact;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Vendor(LTI2Config cnf) {
        this.code = cnf.getProduct_family_vendor_code();
        this.vendor_name = new Name(cnf.getProduct_family_vendor_name());
        this.description = new Description(cnf.getProduct_family_vendor_description());
        this.website = cnf.getProduct_family_vendor_website();
        this.contact = new Contact(cnf.getProduct_family_vendor_contact());
        this.timestamp = BasicLTIUtil.getISO8601(null);
    }
    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    @JsonProperty("vendor_name")
    public Name getVendor_name() {
        return vendor_name;
    }

    @JsonProperty("vendor_name")
    public void setVendor_name(Name vendor_name) {
        this.vendor_name = vendor_name;
    }

    @JsonProperty("description")
    public Description getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(Description description) {
        this.description = description;
    }

    @JsonProperty("website")
    public String getWebsite() {
        return website;
    }

    @JsonProperty("website")
    public void setWebsite(String website) {
        this.website = website;
    }

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("contact")
    public Contact getContact() {
        return contact;
    }

    @JsonProperty("contact")
    public void setContact(Contact contact) {
        this.contact = contact;
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
