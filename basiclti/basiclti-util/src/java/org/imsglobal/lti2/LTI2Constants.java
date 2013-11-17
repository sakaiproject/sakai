/*
 * Copyright (c) 2013 IMS GLobal Learning Consortium
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
package org.imsglobal.lti2;

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
	public static final String SECURITY_CONTRACT = "security_contract";
	public static final String SERVICE = "service";
	public static final String TOOL_SERVICE = "tool_service";
	public static final String GRADE = "grade";
	public static final String GRADE_TYPE_DECIMAL = "decimal";
	public static final String COMMENT = "comment";
	public static final String RESULTSCORE = "resultScore";

	/**
	 * Utility array useful for validating property names when building launch
	 * data.
	 */
	public static final String[] validPropertyNames = { 
        REG_KEY, REG_PASSWORD, TC_PROFILE_URL };

}
