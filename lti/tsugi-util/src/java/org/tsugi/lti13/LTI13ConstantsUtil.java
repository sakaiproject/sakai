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

import java.util.List;

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
	public static final String MEMBERSHIP_BASE = "http://purl.imsglobal.org/vocab/lis/v2/membership";
	public static final String ROLE_LEARNER = MEMBERSHIP_BASE + "#Learner";
	public static final String ROLE_INSTRUCTOR = MEMBERSHIP_BASE + "#Instructor";
	public static final String ROLE_MENTOR = MEMBERSHIP_BASE + "#Mentor";
	public static final String ROLE_MEMBER = MEMBERSHIP_BASE + "#Member";
	public static final String ROLE_ADMINISTRATOR = MEMBERSHIP_BASE + "#Administrator";
	public static final String ROLE_MANAGER = MEMBERSHIP_BASE + "#Manager";
	public static final String ROLE_OFFICER = MEMBERSHIP_BASE + "#Officer";
	public static final String ROLE_CONTENTDEVELOPER = MEMBERSHIP_BASE + "#ContentDeveloper";
	public static final String ROLE_CONTEXT_ADMIN = MEMBERSHIP_BASE + "#Administrator";
	public static final String ROLE_SYSTEM_ADMIN = "http://purl.imsglobal.org/vocab/lis/v2/system/person#Administrator";
	public static final String ROLE_INSTITUTION_ADMIN = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator";

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
	// Context Launch (Draft)
	public static final String MESSAGE_TYPE_LTI_CONTEXT = "LtiContextLaunchRequest";
	// Submission Review (Draft)
	public static final String MESSAGE_TYPE_LTI_SUBMISSION_REVIEW_REQUEST = "LtiSubmissionReviewRequest";
	// Data Privacy Launch (Draft)
	public static final String MESSAGE_TYPE_LTI_DATA_PRIVACY_LAUNCH_REQUEST = "LtiDataPrivacyLaunchRequest";
	public static final String CONTENT_ITEM_DOC_TARGET_IFRAME = "iframe";
	public static final String CONTENT_ITEM_DOC_TARGET_WINDOW = "window";
	public static final String CONTENT_ITEM_MEDIA_TYPES = "*/*";

	//Deep Linking
	public static final String DEEP_LINKING_RETURN_URL = "return_url";

	// Access Token
	public static final String SCOPE_RESULT_READONLY = "https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly";
	public static final String SCOPE_SCORE = "https://purl.imsglobal.org/spec/lti-ags/scope/score";
	public static final String SCOPE_LINEITEM = "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem";
	public static final String SCOPE_LINEITEM_READONLY = "https://purl.imsglobal.org/spec/lti-ags/scope/lineitem.readonly";
	public static final String SCOPE_NAMES_AND_ROLES = "https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly";
	public static final String SCOPE_CONTEXTGROUP_READONLY = "https://purl.imsglobal.org/spec/lti-gs/scope/contextgroup.readonly";
	public static final List<String> SCOPES_SUPPORTED = List.of(
	    SCOPE_RESULT_READONLY, SCOPE_SCORE, SCOPE_LINEITEM,
	    SCOPE_LINEITEM_READONLY, SCOPE_NAMES_AND_ROLES, SCOPE_CONTEXTGROUP_READONLY
	);

	public static final String MEDIA_TYPE_MEMBERSHIPS = "application/vnd.ims.lti-nrps.v2.membershipcontainer+json";
	public static final String MEDIA_TYPE_LINEITEM = "application/vnd.ims.lis.v2.lineitem+json";
	public static final String MEDIA_TYPE_LINEITEMS = "application/vnd.ims.lis.v2.lineitemcontainer+json";
	public static final String SCORE_TYPE = "application/vnd.ims.lis.v1.score+json";
	public static final String RESULTS_TYPE = "application/vnd.ims.lis.v2.resultcontainer+json";
	public static final String CONTEXTGROUPCONTAINER_TYPE = "application/vnd.ims.lti-gs.v1.contextgroupcontainer+json";

}
