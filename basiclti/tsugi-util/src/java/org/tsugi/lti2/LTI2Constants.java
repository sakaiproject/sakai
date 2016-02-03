/*
 * Copyright (c) 2013- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 */
package org.tsugi.lti2;

public class LTI2Constants {
	/**
	 * LTI2_VERSION_STRING=LTI-2p0
	 * <p>
	 * This indicates an LTI 2.0 launch.
	 */
	public static final String LTI2_VERSION_STRING = "LTI-2p0";

	/**
	 * REG_KEY="9875"
	 * <p>
	 * This is the registration key for the callback.
	 */
	public static final String REG_KEY = "reg_key";

	/**
	 * REG_PASSWORD="9875"
	 * <p>
	 * This is the registration password for the callback.
	 */
	public static final String REG_PASSWORD = "reg_password";

        public static final String TC_HALF_SHARED_SECRET = "tc_half_shared_secret";
        public static final String TP_HALF_SHARED_SECRET = "tp_half_shared_secret";

	/**
	 * TC_PROFILE_URL
	 * <p>
	 * This is the profile URL.
	 */
	public static final String TC_PROFILE_URL = "tc_profile_url";

	public static final String JSONLD_ID = "@id";
	public static final String CONTEXT = "@context";
	public static final String TYPE = "@type";
	public static final String VALUE = "@value";
	public static final String GRAPH = "@graph";

	public static final String CUSTOM_URL = "custom_url";
	public static final String TOOL_PROXY_GUID = "tool_proxy_guid";
	public static final String SHARED_SECRET = "shared_secret";
	public static final String CUSTOM = "custom";
	public static final String ENABLED_CAPABILITY = "enabled_capability";
	public static final String SECURITY_CONTRACT = "security_contract";
	public static final String SERVICE = "service";
	public static final String TOOL_SERVICE = "tool_service";
	public static final String GRADE = "grade";
	public static final String GRADE_TYPE_DECIMAL = "decimal";
	public static final String COMMENT = "comment";
	public static final String RESULTSCORE = "resultScore";
	public static final String TOOL_PROXY = "tool_proxy";
	public static final String TOOL_PROXY_BINDING = "tool_proxy_binding";
	public static final String RESOURCE_HANDLER = "resource_handler";
	public static final String ICON_INFO = "icon_info";
	public static final String PARAMETER = "parameter";
	public static final String DESCRIPTION = "description";

	public static final String TOOL_PROFILE = "tool_profile";
	public static final String MESSAGE = "message";
	public static final String MESSAGE_TYPE = "message_type";
	public static final String ICON_STYLE = "icon_style";
	public static final String DEFAULT_LOCATION = "default_location";
	public static final String PATH = "path";
	public static final String GUID = "guid";
	public static final String PRODUCT_INSTANCE = "product_instance";
	public static final String PRODUCT_INFO = "product_info";
	public static final String PRODUCT_NAME = "product_name";
	public static final String DEFAULT_VALUE = "default_value";
	public static final String PRODUCT_FAMILY = "product_family";
	public static final String CODE = "code";
	public static final String VENDOR = "vendor";
	public static final String BASE_URL_CHOICE = "base_url_choice";
	public static final String SECURE_BASE_URL = "secure_base_url";
	public static final String RESOURCE_TYPE = "resource_type";
	public static final String SHORT_NAME = "short_name";
	public static final String DEFAULT_BASE_URL = "default_base_url";
	public static final String RESOURCE_NAME = "resource_name";

}
