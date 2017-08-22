/**
 * Copyright (c) 2009-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lti2;

import org.tsugi.lti2.LTI2Config;

/*
# A globally unique identifier for the service.  The domain name is typical.
# The scope for this is tenant/customer

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

public class SakaiLTI2Base implements LTI2Config {

	// A globally unique identifier for the service.  The domain name is typical.
	// The scope for this is tenant/customer
	public String getGuid() {
		return "sakai.school.edu";
	}
	public String getSupport_email() {
		return "sakai-support@school.edu";
	}
	
	// In a multi-tenant environment this data describes the tenant / customer.
	public String getService_owner_id() {
		return "https://sakai.school.edu";
	}
	public String getService_owner_owner_name() {
		return "ETS";
	}
	public String getService_owner_description() {
		return "The Ed. Tech Services Division.";
	}
	public String getService_owner_support_email() {
		return "ets-support@sakai.school.edu";
	}

	// This represents the service provider that hosts a product. 
	// If this is self hosted, it is reasonable that these values
	// are the same as the "owner" values above.
	public String getService_provider_id() {
		return "https://www.asca.edu/";
	}
	public String getService_provider_provider_name() {
		return "ASCA, Inc.";
	}
	public String getService_provider_description() {
		return "A Sakai Commercial Affiliate";
	}
	public String getService_provider_support_email() {
		return "sales@asca.com";
	}

	// This section is about the software product
	// Likely all Sakai schools will leave this alone
	public String getProduct_family_product_code() {
		return "sakai";
	}
	public String getProduct_family_vendor_code() {
		return "sakai";
	}
	public String getProduct_family_vendor_name() {
		return "Sakai Project";
	}
	public String getProduct_family_vendor_description() {
		return "Sakai is an Apereo Project.";
	}
	public String getProduct_family_vendor_website() {
		return "http://www.sakaiproject.org";
	}
	public String getProduct_family_vendor_contact() {
		return "info@sakaiproject.org";
	}

	// This is about one particular version of a product.
	public String getProduct_info_product_name() {
		return "Sakai";
	}
	public String getProduct_info_product_version() {
		return "10.0";
	}
	public String getProduct_info_product_description() {
		return "Sakai 10.0";
	}
}
