/*
 * Copyright (c) 2015- Charles R. Severance
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

/**
 * A class to capture the names of the valid LTI 2.0 capabilities
 *
 * Taken from: http://www.imsglobal.org/lti/ltiv2p0/uml/purl.imsglobal.org/vocab/lti/v2/capability/index.html
 */

public class LTI2Caps {

	/**
	 * LTI_LAUNCH - Support launching
	 */
	public static final String LTI_LAUNCH = "basic-lti-launch-request";

	/**
	 * RESULT_AUTOCREATE - Can automatically create gradebook entries
	 */
	public static final String RESULT_AUTOCREATE = "Result.autocreate";

	/**
	 * OAUTH_SPLITSECRET - Can handle LTI 2.1 style split secrets
	 */
	public static final String OAUTH_SPLITSECRET = "OAuth.splitSecret";

	/**
	 * OAUTH_HMAC256 - Can handle signing using HMAC-SHA256
	 */
	public static final String OAUTH_HMAC256 = "OAuth.hmac-sha256";

	/**
	 * ALL_CAPABILITIES - All of the capabilities
         */
	public static final String [] ALL_CAPABILITIES = {
		LTI_LAUNCH, RESULT_AUTOCREATE, OAUTH_SPLITSECRET, OAUTH_HMAC256
	};

}
