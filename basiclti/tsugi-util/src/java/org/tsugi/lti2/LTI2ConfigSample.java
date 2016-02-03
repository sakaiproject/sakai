package org.tsugi.lti2;

public class LTI2ConfigSample implements LTI2Config {

	// A globally unique identifier for the service.  The domain name is typical.
	// The scope for this is tenant/customer
	public String getGuid() {
		return "edunext.school.edu";
	}
	public String getSupport_email() {
		return "support@edunext.school.edu";
	}
	
	// In a multi-tenant environment this data describes the tenant / customer.
	public String getService_owner_id() {
		return "https://edunext.school.edu";
	}
	public String getService_owner_owner_name() {
		return "ETS at SchoolEdu";
	}
	public String getService_owner_description() {
		return "The Ed. Tech Services Division of SchoolEdu";
	}
	public String getService_owner_support_email() {
		return "edunext@school.edu";
	}

	// This represents the service provider that hosts a product. 
	// If this is self hosted, it is reasonable that these values
	// are the same as the "owner" values above.
	public String getService_provider_id() {
		return "https://hosting.example.com";
	}
	public String getService_provider_provider_name() {
		return "Example Hosting Services";
	}
	public String getService_provider_description() {
		return "We are the best example of a hosting services for EduNext.";
	}
	public String getService_provider_support_email() {
		return "sales@hosting.example.com";
	}

	// This section is about the software product
	public String getProduct_family_product_code() {
		return "edunext";
	}
	public String getProduct_family_vendor_code() {
		return "edunext";
	}
	public String getProduct_family_vendor_name() {
		return "Edu Next Project";
	}
	public String getProduct_family_vendor_description() {
		return "EduNext is whats next in education.";
	}
	public String getProduct_family_vendor_website() {
		return "http://www.edunext.example.com";
	}
	public String getProduct_family_vendor_contact() {
		return "sales@edunext.sample.com";
	}

	// This is about one particular version of a product.
	public String getProduct_info_product_name() {
		return "Classes";
	}
	public String getProduct_info_product_version() {
		return "2.0";
	}
	public String getProduct_info_product_description() {
		return "Classes 2.0";
	}
}
