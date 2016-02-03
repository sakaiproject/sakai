
package org.tsugi.lti2;

// Capture the information needed to build a tool profile
public interface LTI2Config {
    // A globally unique identifier for the service.  The domain name is typical.
    // The scope for this is tenant/customer
	public String getGuid();
	public String getSupport_email();

    // In a multi-tenant environment this data describes the tenant / customer.
	public String getService_owner_id();
	public String getService_owner_owner_name();
	public String getService_owner_description();
	public String getService_owner_support_email();

    // This represents the service provider that hosts a product. 
    // If this is self hosted, it is reasonable that these values
    // are the same as the "owner" values above.
	public String getService_provider_id();
	public String getService_provider_provider_name();
	public String getService_provider_description();
	public String getService_provider_support_email();

    // This section is general information about the software product
	public String getProduct_family_product_code();
	public String getProduct_family_vendor_code();
	public String getProduct_family_vendor_name();
	public String getProduct_family_vendor_description();
	public String getProduct_family_vendor_website();
	public String getProduct_family_vendor_contact();

    // This is about one particular version of a product
	public String getProduct_info_product_name();
	public String getProduct_info_product_version();
	public String getProduct_info_product_description();
}
