/*
 * Copyright (c) 2018- Charles R. Severance
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
package org.tsugi.lti13;

public class LTI13ConstantsUtil {

	public static final String KEY_ISS = "iss";
	public static final String KEY_SUB = "sub";
	public static final String KEY_AUD = "aud";
	public static final String KEY_EXP = "exp";
	public static final String KEY_IAT = "iat";
	public static final String KEY_AZP = "azp";
	public static final String KEY_NONCE = "nonce";
	public static final String KEY_NAME = "name";
	public static final String KEY_GNAME = "given_name";
	public static final String KEY_FNAME = "family_name";
	public static final String KEY_MNAME = "middle_name";
	public static final String KEY_PICTURE = "picture";
	public static final String KEY_EMAIL = "email";
	public static final String KEY_LOCALE = "locale";
	public static final String KEY_ID = "id";
	public static final String KEY_LABEL = "label";
	public static final String KEY_TITLE = "title";
	public static final String KEY_TYPE = "type";
	public static final String KEY_DESC = "description";
	public static final String KEY_GUID = "guid";
	public static final String KEY_CONTACT_EMAIL = "contact_email";
	public static final String KEY_URL = "url";
	public static final String KEY_PRODUCT_FAMILY_CODE = "product_family_code";
	public static final String KEY_VERSION = "version";
	public static final String KEY_DOC_TARGET = "document_target";
	public static final String KEY_HEIGHT = "height";
	public static final String KEY_WIDTH = "width";
	public static final String KEY_RETURN_URL = "return_url";
	public static final String KEY_XSTART = "xstart";
	public static final String KEY_PERSON_SOURCEDID = "person_sourcedid";
	public static final String KEY_COURSE_OFFERING_SOURCEDID = "course_offering_souredid";
	public static final String KEY_COURSE_SECTION_SOURCEDID = "course_section_sourcedid";
	public static final String KEY_ACCEPT_MULTIPLE = "accept_multiple";
	public static final String KEY_AUTO_CREATE = "auto_create";
	public static final String KEY_ACCEPT_COPY_ADVICE = "accept_copy_advice";
	public static final String KEY_DATA = "data";

	// Roles
	public static String ROLE_LEARNER = "http://purl.imsglobal.org/vocab/lis/v2/membership#Learner";
	public static String ROLE_INSTRUCTOR = "http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor";
	public static String ROLE_CONTEXT_ADMIN = "http://purl.imsglobal.org/vocab/lis/v2/membership#Administrator";
	public static String ROLE_SYSTEM_ADMIN = "http://purl.imsglobal.org/vocab/lis/v2/system/person#Administrator";
	public static String ROLE_INSTITUTION_ADMIN = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator";

	// Deep Linking types
	public static final String LTI_DL_TYPE_LTILINK = "ltiResourceLink";
	public static final String LTI_DL_TYPE_LINK = "link";
	public static final String LTI_DL_TYPE_FILE = "file";
	public static final String LTI_DL_TYPE_HTML = "html";
	public static final String LTI_DL_TYPE_IMAGE = "image";

	// Misc
	public static final String MESSAGE_TYPE = "https://purl.imsglobal.org/spec/lti/claim/message_type";
	public static final String MESSAGE_TYPE_LTI_RESOURCE = "LtiResourceLinkRequest";
	public static final String MESSAGE_TYPE_LTI_DEEP_LINKING_REQUEST = "LtiDeepLinkingRequest";
	public static final String MESSAGE_TYPE_LTI_DEEP_LINKING_RESPONSE = "LtiDeepLinkingResponse";
	public static final String CONTENT_ITEM_DOC_TARGET_IFRAME = "iframe";
	public static final String CONTENT_ITEM_DOC_TARGET_WINDOW = "window";
	public static final String CONTENT_ITEM_MEDIA_TYPES = "*/*";

	//Deep Linking
	public static final String DEEP_LINKING_RETURN_URL = "return_url";


}
