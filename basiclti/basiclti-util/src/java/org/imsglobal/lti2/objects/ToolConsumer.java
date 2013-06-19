
package org.imsglobal.lti2.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;

@Generated("com.googlecode.jsonschema2pojo")
public class ToolConsumer {

    private List<String> _context = new ArrayList<String>();
    private String _type;
    private String _id;
    private String lti_version;
    private String guid;
    private Product_instance product_instance;
    private List<String> capability_enabled = new ArrayList<String>();
    private List<Service_offered> service_offered = new ArrayList<Service_offered>();
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public List<String> get_context() {
        return _context;
    }

    public void set_context(List<String> _context) {
        this._context = _context;
    }

    public String get_type() {
        return _type;
    }

    public void set_type(String _type) {
        this._type = _type;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getLti_version() {
        return lti_version;
    }

    public void setLti_version(String lti_version) {
        this.lti_version = lti_version;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Product_instance getProduct_instance() {
        return product_instance;
    }

    public void setProduct_instance(Product_instance product_instance) {
        this.product_instance = product_instance;
    }

    public List<String> getCapability_enabled() {
        return capability_enabled;
    }

    public void setCapability_enabled(List<String> capability_enabled) {
        this.capability_enabled = capability_enabled;
    }

    public List<Service_offered> getService_offered() {
        return service_offered;
    }

    public void setService_offered(List<Service_offered> service_offered) {
        this.service_offered = service_offered;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperties(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
