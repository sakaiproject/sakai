
package org.sakaiproject.lti2;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;

import org.sakaiproject.basiclti.util.SakaiBLTIUtil;

// Encapsulate the rules about loading the LTI2 config data from properties

/* Properties detail:

# A globally unique identifier for the service.  The domain name is typical.
# The scope for this is tenant/customer
lti2.guid=sakai.school.edu

# In a multi-tenant environment this data describes the tenant / customer.
# if lti2.service_owner.support_email is not provided, mail.support will be used
# if lti2.service_owner.id is not provided, serverUrl will be used
lti2.support_email=sakai-support@school.edu
lti2.service_owner.id=http://sakai.school.edu/
lti2.service_owner.owner_name=ETS
lti2.service_owner.description=Edtech Services
lti2.service_owner.support_email=ets-support@school.edu

# This represents the service provider that hosts a product. 
# If these are not provided, they will be the same as service_owner
lti2.service_provider.id=http://sakai.school.edu/
lti2.service_provider.provider_name=ASCA, Inc.
lti2.service_provider.description=A Sakai Commercial Affiliate
lti2.service_provider.support_email=sakai@asca.com

# This section is about the general information about the software product
# The values below will be the defaults unless they are overridden
lti2.product_family.product_code=sakai
lti2.product_family.vendor_code=sakai
lti2.product_family.vendor_name=Sakai Project
lti2.product_family.vendor_description=Sakai is an Apereo project.
lti2.product_family.vendor_website=http://www.sakaiproject.org/
lti2.product_family.vendor_contact=info@sakaiproject.org

# product_info is about one particular version of a product.
# if lti2.product_info.product_version is not provided, version.sakai is used
lti2.product_info.product_name=Sakai
lti2.product_info.product_version=10.0
lti2.product_info.product_description=Sakai 10.0

*/

public class SakaiLTI2Config {

	public String guid;
	public String support_email;
	public String service_owner_id;
	public String service_owner_owner_name;
	public String service_owner_description;
	public String service_owner_support_email;
	public String service_provider_id;
	public String service_provider_provider_name;
	public String service_provider_description;
	public String service_provider_support_email;
	public String product_family_product_code;
	public String product_family_vendor_code;
	public String product_family_vendor_name;
	public String product_family_vendor_description;
	public String product_family_vendor_website;
	public String product_family_vendor_contact;
	public String product_info_product_name;
	public String product_info_product_version;
	public String product_info_product_description;

    public SakaiLTI2Config() {
        String serverUrl = SakaiBLTIUtil.getOurServerUrl();
		ServerConfigurationService cnf = (ServerConfigurationService) ComponentManager
                        .get(ServerConfigurationService.class);

		guid = cnf.getString("lti2.guid");
		support_email = cnf.getString("lti2.support_email");
		if ( support_email == null ) support_email = cnf.getString("mail.support");

		service_owner_id = cnf.getString("lti2.service_owner.id");
		if ( service_owner_id == null ) service_owner_id = serverUrl;
		service_owner_owner_name = cnf.getString("lti2.service_owner.owner_name");
		service_owner_description = cnf.getString("lti2.service_owner.description");
		service_owner_support_email = cnf.getString("lti2.service_owner.support_email");
		if ( service_owner_support_email == null ) service_owner_support_email = support_email;

		service_provider_id = cnf.getString("lti2.service_provider.id");
		if ( service_provider_id == null ) service_provider_id = serverUrl;
		service_provider_provider_name = cnf.getString("lti2.service_provider.provider_name");
		if ( service_provider_provider_name == null ) service_provider_provider_name = service_owner_owner_name;
		service_provider_description = cnf.getString("lti2.service_provider.description");
		if ( service_provider_description == null ) service_provider_description = service_owner_description;
		service_provider_support_email = cnf.getString("lti2.service_provider.support_email");
		if ( service_provider_support_email == null ) service_provider_support_email = service_owner_support_email;

		product_family_product_code = cnf.getString("lti2.product_family.product_code", "sakai");
		product_family_vendor_code = cnf.getString("lti2.product_family.vendor_code", "sakai");
		product_family_vendor_name = cnf.getString("lti2.product_family.vendor_name", "Sakai Project");
		product_family_vendor_description = cnf.getString("lti2.product_family.vendor_description", "Sakai is an Apereo project.");
		product_family_vendor_website = cnf.getString("lti2.product_family.vendor_website", "http://www.sakaiproject.org/");
		product_family_vendor_contact = cnf.getString("lti2.product_family.vendor_contact", "info@sakaiproject.org");

		product_info_product_name = cnf.getString("lti2.product_info.product_name", "Sakai");
		product_info_product_version = cnf.getString("lti2.product_info.product_version");
		if ( product_info_product_version == null ) product_info_product_version = cnf.getString("version.sakai");
		product_info_product_description = cnf.getString("lti2.product_info.product_description");
		if ( product_info_product_description == null ) product_info_product_description = "Sakai "+product_info_product_version;
	}
} 

