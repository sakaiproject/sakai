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
import org.sakaiproject.lti2.SakaiLTI2Base;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;

import org.sakaiproject.basiclti.util.SakaiBLTIUtil;

public class SakaiLTI2Config implements LTI2Config {

	private String guid;
	private String support_email;
	private String service_owner_id;
	private String service_owner_owner_name;
	private String service_owner_description;
	private String service_owner_support_email;
	private String service_provider_id;
	private String service_provider_provider_name;
	private String service_provider_description;
	private String service_provider_support_email;
	private String product_family_product_code;
	private String product_family_vendor_code;
	private String product_family_vendor_name;
	private String product_family_vendor_description;
	private String product_family_vendor_website;
	private String product_family_vendor_contact;
	private String product_info_product_name;
	private String product_info_product_version;
	private String product_info_product_description;

	public String getGuid() { return guid; } 
	public String getSupport_email() { return support_email; } 
	public String getService_owner_id() { return service_owner_id; } 
	public String getService_owner_owner_name() { return service_owner_owner_name; } 
	public String getService_owner_description() { return service_owner_description; } 
	public String getService_owner_support_email() { return service_owner_support_email; } 
	public String getService_provider_id() { return service_provider_id; } 
	public String getService_provider_provider_name() { return service_provider_provider_name; } 
	public String getService_provider_description() { return service_provider_description; } 
	public String getService_provider_support_email() { return service_provider_support_email; } 
	public String getProduct_family_product_code() { return product_family_product_code; } 
	public String getProduct_family_vendor_code() { return product_family_vendor_code; } 
	public String getProduct_family_vendor_name() { return product_family_vendor_name; } 
	public String getProduct_family_vendor_description() { return product_family_vendor_description; } 
	public String getProduct_family_vendor_website() { return product_family_vendor_website; } 
	public String getProduct_family_vendor_contact() { return product_family_vendor_contact; } 
	public String getProduct_info_product_name() { return product_info_product_name; } 
	public String getProduct_info_product_version() { return product_info_product_version; } 
	public String getProduct_info_product_description() { return product_info_product_description; } 

	public SakaiLTI2Config() {
		ServerConfigurationService cnf = (ServerConfigurationService) ComponentManager
						.get(ServerConfigurationService.class);

		String serverUrl = SakaiBLTIUtil.getOurServerUrl();
		LTI2Config base = new SakaiLTI2Base();

		guid = cnf.getString("lti2.guid", null);
		support_email = cnf.getString("lti2.support_email", null);
		if ( support_email == null ) support_email = cnf.getString("mail.support", null);

		service_owner_id = cnf.getString("lti2.service_owner.id", null);
		if ( service_owner_id == null ) service_owner_id = serverUrl;
		service_owner_owner_name = cnf.getString("lti2.service_owner.owner_name", null);
		service_owner_description = cnf.getString("lti2.service_owner.description", null);
		service_owner_support_email = cnf.getString("lti2.service_owner.support_email", null);
		if ( service_owner_support_email == null ) service_owner_support_email = support_email;

		service_provider_id = cnf.getString("lti2.service_provider.id", null);
		if ( service_provider_id == null ) service_provider_id = serverUrl;
		service_provider_provider_name = cnf.getString("lti2.service_provider.provider_name", null);
		if ( service_provider_provider_name == null ) service_provider_provider_name = service_owner_owner_name;
		service_provider_description = cnf.getString("lti2.service_provider.description", null);
		if ( service_provider_description == null ) service_provider_description = service_owner_description;
		service_provider_support_email = cnf.getString("lti2.service_provider.support_email", null);
		if ( service_provider_support_email == null ) service_provider_support_email = service_owner_support_email;

		product_family_product_code = cnf.getString("lti2.product_family.product_code", base.getProduct_family_product_code());
		product_family_vendor_code = cnf.getString("lti2.product_family.vendor_code", base.getProduct_family_vendor_code());
		product_family_vendor_name = cnf.getString("lti2.product_family.vendor_name", base.getProduct_family_vendor_name());
		product_family_vendor_description = cnf.getString("lti2.product_family.vendor_description", base.getProduct_family_vendor_description());
		product_family_vendor_website = cnf.getString("lti2.product_family.vendor_website", base.getProduct_family_vendor_website());
		product_family_vendor_contact = cnf.getString("lti2.product_family.vendor_contact", base.getProduct_family_vendor_contact());

		product_info_product_name = cnf.getString("lti2.product_info.product_name", base.getProduct_info_product_name());
		product_info_product_version = cnf.getString("lti2.product_info.product_version", null);
		if ( product_info_product_version == null ) product_info_product_version = cnf.getString("version.sakai", null);
		product_info_product_description = cnf.getString("lti2.product_info.product_description", null);
		if ( product_info_product_description == null ) product_info_product_description = "Sakai "+product_info_product_version;
	}
} 

