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

public class LTI2Messages {

	public static final String BASIC_LTI_LAUNCH_REQUEST = "basic-lti-launch-request";

	public static final String TOOLPROXY_REGISTRATION_REQUEST = "ToolProxyRegistrationRequest";

	public static final String TOOLPROXY_RE_REGISTRATION_REQUEST = "ToolProxyReregistrationRequest";

	public static final String CONTENT_ITEM_SELECTION_REQUEST = "ContentItemSelectionRequest";

}
