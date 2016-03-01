/*
 * Copyright (c) 2008-2016 Charles R. Severance
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
 */
package org.tsugi.basiclti;

public class BasicLTIConstants {
	/**
	 * context_id=8213060-006f-27b2066ac545
	 * <p>
	 * This is an opaque identifier that uniquely identifies the context that
	 * contains the link being launched.
	 */
	public static final String CONTEXT_ID = "context_id";
	/**
	 * context_label=SI182
	 * <p>
	 * A label for the context - intended to fit in a column.
	 */
	public static final String CONTEXT_LABEL = "context_label";
	/**
	 * context_title=Design of Personal Environments
	 * <p>
	 * A title of the context - it should be about the length of a line.
	 */
	public static final String CONTEXT_TITLE = "context_title";

	/**
	 * context_type=CourseSection
	 * <p>
	 * This string is a comma-separated list of URN values that identify the type
	 * of context. At a minimum, the list MUST include a URN value drawn from the
	 * LIS vocabulary (see Appendix A). The assumed namespace of these URNs is the
	 * LIS vocabulary so TCs can use the handles when the intent is to refer to an
	 * LIS context type. If the TC wants to include a context type from another
	 * namespace, a fully-qualified URN should be used.
	 */
	public static final String CONTEXT_TYPE = "context_type";
	public static final String CONTEXT_TYPE_COURSE_OFFERING = "CourseOffering";
	public static final String CONTEXT_TYPE_COURSE_SECTION = "CourseSection";
	public static final String CONTEXT_TYPE_COURSE_TEMPLATE = "CourseTemplate";
	public static final String CONTEXT_TYPE_GROUP = "Group";

	/**
	 * ext_param=value
	 * <p>
	 * Systems can add their own values to the launch but should prefix
	 * any extensions with "ext_".
	 */
	public static final String EXTENSION_PREFIX = "ext_";
	/**
	 * custom_keyname=value
	 * <p>
	 * The creator of a Basic LTI link can add custom key/value parameters to a
	 * launch which are to be included with the launch of the Basic LTI link. The
	 * Common Cartridge section below describes how these parameters are
	 * represented when storing custom parameters in a Common Cartridge.
	 * <p>
	 * When there are custom name / value parameters in the launch, a POST
	 * parameter is included for each custom parameter. The parameter names are
	 * mapped to lower case and any character that is neither a number nor letter
	 * in a parameter name is replaced with an "underscore". So if a custom entry
	 * was as follows:
	 * <p>
	 * Review:Chapter=1.2.56
	 * <p>
	 * Would map to: custom_review_chapter=1.2.56
	 * <p>
	 * Creators of Basic LTI links would be well served to limit their parameter
	 * names to lower case and to use no punctuation other than underscores. If
	 * these custom parameters are included in the Basic LTI link, the TC must
	 * include them in the launch data or the TP may fail to function.
	 */
	public static final String CUSTOM_PREFIX = "custom_";
	/**
	 * Parameters with the OAuth prefix are also acceptible.
	 */
	public static final String OAUTH_PREFIX = "oauth_";
	/**
	 * launch_presentation_document_target=iframe
	 * <p>
	 * The value should be either 'frame', 'iframe' or 'window'. This field
	 * communicates the kind of browser window/frame where the TC has launched the
	 * tool.
	 */
	public static final String LAUNCH_PRESENTATION_DOCUMENT_TARGET = "launch_presentation_document_target";
	/**
	 * launch_presentation_height=240
	 * <p>
	 * The height of the window or frame where the content from the tool will be
	 * displayed.
	 */
	public static final String LAUNCH_PRESENTATION_HEIGHT = "launch_presentation_height";
	/**
	 * launch_presentation_locale=en_US_variant
	 * <p>
	 * Language, country and variant separated by underscores. Language is the
	 * lower-case, two-letter code as defined by ISO-639 (list of codes available
	 * at http://www.ics.uci.edu/pub/ietf/http/related/iso639.txt). Country is the
	 * upper-case, two-letter code as defined by ISO-3166 (list of codes available
	 * at http://www.chemie.fu- berlin.de/diverse/doc/ISO_3166.html). Country and
	 * variant codes are optional.
	 */
	public static final String LAUNCH_PRESENTATION_LOCALE = "launch_presentation_locale";
	/**
	 * launch_presentation_return_url=http://lmsng.school.edu/portal/123/page/988/
	 * <p>
	 * Fully qualified URL where the TP can redirect the user back to the TC interface. This
	 * URL can be used once the TP is finished or if the TP cannot start or has some
	 * technical difficulty. In the case of an error, the TP may add a parameter called
	 * lti_errormsg that includes some detail as to the nature of the error. The
	 * lti_errormsg value should make sense if displayed to the user. If the tool has
	 * displayed a message to the end user and only wants to give the TC a message to log,
	 * use the parameter lti_errorlog instead of lti_errormsg. If the tool is terminating
	 * normally, and wants a message displayed to the user it can include a text message as
	 * the lti_msg parameter to the return URL. If the tool is terminating normally and
	 * wants to give the TC a message to log, use the parameter lti_log. This data should be
	 * sent on the URL as a GET - so the TP should take care to keep the overall length of
	 * the parameters small enough to fit within the limitations of a GET request.
	 */
	public static final String LAUNCH_PRESENTATION_RETURN_URL = "launch_presentation_return_url";
	/**
	 * launch_presentation_width=320
	 * <p>
	 * The width of the window or frame where the content from the tool will be
	 * displayed.
	 */
	public static final String LAUNCH_PRESENTATION_WIDTH = "launch_presentation_width";
	/**
	 * launch_presentation_css_url=http://www.toolconsumer.url/path/to/lti.css
	 * <p>
	 * This points to a fully qualified URL for a CSS which can be used to style the tool.
	 * There are no officially defined CSS classes for this file, but the Consumer can
	 * apply styles to paragraphs, body, and the various HTML elements.  It is up to the
	 * tool as to whether this CSS is used or not, and in what order this is included relative
	 * to the tool-specific CSS.
	 */
	public static final String LAUNCH_PRESENTATION_CSS_URL = "launch_presentation_css_url";
	/**
	 * lis_person_contact_email_primary=user@school.edu
	 * <p>
	 * These fields contain information about the user account that is performing
	 * this launch. The names of these data items are taken from LIS. The precise
	 * meaning of the content in these fields is defined by LIS.
	 */
	public static final String LIS_PERSON_CONTACT_EMAIL_PRIMARY = "lis_person_contact_email_primary";
	/**
	 * lis_person_name_family=Public
	 * <p>
	 * These fields contain information about the user account that is performing
	 * this launch. The names of these data items are taken from LIS. The precise
	 * meaning of the content in these fields is defined by LIS.
	 */
	public static final String LIS_PERSON_NAME_FAMILY = "lis_person_name_family";
	/**
	 * lis_person_name_full=Jane Q. Public
	 * <p>
	 * These fields contain information about the user account that is performing
	 * this launch. The names of these data items are taken from LIS. The precise
	 * meaning of the content in these fields is defined by LIS.
	 */
	public static final String LIS_PERSON_NAME_FULL = "lis_person_name_full";
	/**
	 * lis_person_name_given=Jane
	 * <p>
	 * These fields contain information about the user account that is performing
	 * this launch. The names of these data items are taken from LIS. The precise
	 * meaning of the content in these fields is defined by LIS.
	 */
	public static final String LIS_PERSON_NAME_GIVEN = "lis_person_name_given";

	/**
	 * lis_person_sourcedid=school.edu:user
	 * <p>
	 * This field contains the LIS identifier for the user account that is
	 * performing this launch.    The example syntax of "school:user"
	 * is not the required format – lis_person_sourcedid is simply a
	 * unique identifier (i.e., a normalized string). This field
	 * is optional and its content and meaning are defined by LIS.
	 */
	public static final String LIS_PERSON_SOURCEDID = "lis_person_sourcedid";

	/**
	 * lis_course_offering_sourcedid=school.edu:SI182-F08 <br/>
	 * lis_course_section_sourcedid=school.edu:SI182-001-F08
	 * <p>
	 * These fields contain LIS course identifiers associated with the
	 * context of this launch.  These fields are optional and their
	 * content and meaning are defined by LIS.
	 */
	public static final String LIS_COURSE_OFFERING_SOURCEDID = "lis_course_offering_sourcedid";
	public static final String LIS_COURSE_SECTION_SOURCEDID = "lis_course_section_sourcedid";

	/**
     * lis_outcome_service_url=http://lmsng.school.edu/service/ltiout/
     * <p>
     * This field should be no more than 1023 characters long and the TP
     * should assume that the URL might change from launch to launch and
     * allow for the fact that a TC might change their service URL from
     * launch to launch and only use the most recent value for this
     * parameter.  The service URL may support various operations / services.
     * The TC will respond with a response of 'unimplemented' for services
     * it does not support.
	 */
	public static final String LIS_OUTCOME_SERVICE_URL = "lis_outcome_service_url";

	/**
	 * lis_result_sourcedid=83873872987329873264783687634
	 * <p>
	 * This field contains an identifier that indicates the LIS Result
	 * Identifier (if any) associated with this launch. This field is
	 * optional and its content and meaning is defined by LIS.
	 */
	public static final String LIS_RESULT_SOURCEDID = "lis_result_sourcedid";

	/**
	 * lti_message_type=basic-lti-launch-request
	 * <p>
	 * This indicates that this is a Basic LTI Launch Message. This allows a TP to
	 * accept a number of different LTI message types at the same launch URL. This
	 * parameter is required.
	 */
	public static final String LTI_MESSAGE_TYPE = "lti_message_type";
	public static final String LTI_MESSAGE_TYPE_TOOLPROXYREGISTRATIONREQUEST = "ToolProxyRegistrationRequest";
	public static final String LTI_MESSAGE_TYPE_TOOLPROXY_RE_REGISTRATIONREQUEST = "ToolProxyReregistrationRequest";
	public static final String LTI_MESSAGE_TYPE_BASICLTILAUNCHREQUEST = "basic-lti-launch-request";
	public static final String LTI_MESSAGE_TYPE_CONTENTITEMSELECTIONREQUEST = "ContentItemSelectionRequest";
	public static final String LTI_MESSAGE_TYPE_CONTENTITEMSELECTION = "ContentItemSelection";
	/**
	 * lti_version=LTI-1p0
	 * <p>
	 * This indicates which version of the specification is being used for this
	 * particular message. This parameter is required.
	 */
	public static final String LTI_VERSION = "lti_version";
	public static final String LTI_VERSION_1 = "LTI-1p0";
	public static final String LTI_VERSION_2 = "LTI-2p0";

   /**
    * tool_consumer_info_product_family_code=desire2learn
    * <p>
    * In order to better assist tools in using extensions and also making their user
    * interface fit into the TC's user interface that they are being called from,
    * each TC is encouraged to include the this parameter.   Possible example values
    * for this field might be:
    *
    * 	learn
    * 	desire2learn
    * 	sakai
    * 	eracer
    * 	olat
    * 	webct
    * This parameter is optional but recommended.
    */
	public static final String TOOL_CONSUMER_INFO_PRODUCT_FAMILY_CODE = "tool_consumer_info_product_family_code";

   /**
    * tool_consumer_info_version=9.2.4
    * <p>
    * This field should have a major release number followed by a period.  The format of the minor release is flexible.  Possible vaues for this field might be:
    *
    * 	9.1.7081
    * 	2.8-01
    * 	7.1
    * 	8
    * The Tool Provider should be flexible when parsing this field.  This parameter is optional but recommended.

     */
	public static final String TOOL_CONSUMER_INFO_VERSION = "tool_consumer_info_version";

	/**
	 * resource_link_id=88391-e1919-bb3456
	 * <p>
	 * This is an opaque unique identifier that the TC guarantees will be unique
	 * within the TC for every placement of the link. If the tool / activity is
	 * placed multiple times in the same context, each of those placements will be
	 * distinct. This value will also change if the item is exported from one
	 * system or context and imported into another system or context. This
	 * parameter is required.
	 */
	public static final String RESOURCE_LINK_ID = "resource_link_id";

	/**
	 * resource_link_title=My Weekly Wiki
	 * <p>
	 * A title for the resource. This is the clickable text that appears
	 * in the link. This parameter is recommended.
	 */
	public static final String RESOURCE_LINK_TITLE = "resource_link_title";

	/**
	 * resource_link_description=…
	 * <p>
	 * A plain text description of the link’s destination, suitable for
	 * display alongside the link. Typically no more than several lines
	 * long. This parameter is optional.
	 */
	public static final String RESOURCE_LINK_DESCRIPTION = "resource_link_description";

	/**
	 * roles=Instructor,Student
	 * <p>
	 * A comma-separated list of URN values for roles. If this list is non-empty,
	 * it should contain at least one role from the LIS System Role, LIS
	 * Institution Role, or LIS Context Role vocabularies (See Appendix A). The
	 * assumed namespace of these URNs is the LIS vocabulary of LIS Context Roles
	 * so TCs can use the handles when the intent is to refer to an LIS context
	 * role. If the TC wants to include a role from another namespace, a
	 * fully-qualified URN should be used. Usage of roles from non-LIS
	 * vocabularies is discouraged as it may limit interoperability. This
	 * parameter is recommended.
	 */
	public static final String ROLES = "roles";

	/**
	 * tc_profile_url=http://...
	 * <p>
	 * This URL specifies the address where the Tool Provider can retrieve
	 * the Tool Consumer Profile.   This URL must be retrievable by a GET
	 * request by the Tool Provider.  If the URL is protected from retrieval
	 * in general, the Tool Consumer must append the necessary parameters to
	 * allow the Tool Provider to retrieve the URL with nothing more than
	 * a GET request.  It is legal for this URL to contain a security token
	 * that is changed for each ToolProxyRegistrationRequest so the Tool
	 * Provider must retrieve the tc_profile_url on each request.
	 */
	public static final String TC_PROFILE_URL = "tc_profile_url";

	/**
	 * tool_consumer_instance_contact_email=System.Admin@school.edu
	 * <p>
	 * An email contact for the TC instance.
	 */
	public static final String TOOL_CONSUMER_INSTANCE_CONTACT_EMAIL = "tool_consumer_instance_contact_email";
	/**
	 * tool_consumer_instance_description=University of School (LMSng)
	 * <p>
	 * This is a user visible field - it should be about the length of a line.
	 */
	public static final String TOOL_CONSUMER_INSTANCE_DESCRIPTION = "tool_consumer_instance_description";
	// global settings
	/**
	 * tool_consumer_instance_guid=lmsng.school.edu
	 * <p>
	 * This is a key to be used when setting a TC-wide password. The TP uses this
	 * as a key to look up the TC-wide secret when validating a message. A common
	 * practice is to use the DNS of the organization or the DNS of the TC
	 * instance. If the organization has multiple TC instances, then the best
	 * practice is to prefix the domain name with a locally unique identifier for
	 * the TC instance. This parameter is recommended.
	 */
	public static final String TOOL_CONSUMER_INSTANCE_GUID = "tool_consumer_instance_guid";
	/**
	 * tool_consumer_instance_name=SchoolU
	 * <p>
	 * This is a user visible field - it should be about the length of a column.
	 */
	public static final String TOOL_CONSUMER_INSTANCE_NAME = "tool_consumer_instance_name";
	/**
	 * Missing from implementation guide. Needs documentation. Not required, but
	 * "tasty".
	 */
	public static final String TOOL_CONSUMER_INSTANCE_URL = "tool_consumer_instance_url";

	/**
	 * user_id=0ae836b9-7fc9-4060-006f-27b2066ac545
	 * <p>
	 * Uniquely identifies the user. This should not contain any identifying
	 * information for the user. Best practice is that this field should be a
	 * TC-generated long-term "primary key" to the user record - not the logical
	 * key. This parameter is recommended.
	 */
	public static final String USER_ID = "user_id";

	/**
	 * user_image=http://my.sakai.org/direct/profile/0ae836b9-7fc9-4060-006f-27b2066ac545/image
	 * <p>
	 * This attribute specifies the URI for an image of the user who launched this request.
	 * This image is suitable for use as a "profile picture" or an avatar representing the user.
	 * It is expected to be a relatively small graphic image file using a widely supported image
	 * format (i.e. PNG, JPG, or GIF) with a square aspect ratio. This parameter is optional.
	 */
	public static final String USER_IMAGE = "user_image";

	/**
	 * accept_media_types=application/vnd.ims.lti.v1.ltilink,image/*,text/html  (Required)
	 *
	 * A comma-separated list of MIME types which can be accepted on the return URL.
	 * A MIME type of application/vnd.ims.lti.v1.ltilink is used to represent an LTI launch
	 * request to a TP.  This parameter should use the same format as the Accept header in
	 * HTTP requests [HTTP, 14].  For example, a value of “image/*; q=0.5, image/png”
	 * indicates that a PNG image is preferred, but any type of image will be accepted if
	 * one is not available.  To accept any media type except an LTI link,
	 * a header of “application/vnd.ims.lti.v1.ltilink; q=0, * /*” could be used.
	 */
	public static final String ACCEPT_MEDIA_TYPES = "accept_media_types";

	/**
	 * accept_presentation_document_targets=embed,frame,iframe,window,popup,overlay (Required)
	 *
	 * A comma-separated list of ways in which the selected content item(s)
	 * can be requested to be opened (via the presentationDocumentTarget element for
	 * a returned content item).  The possible values for this parameter are:
	 * embed – insert the item at the current insertion point (for example, using an img tag or an object tag);
	 * frame – open the item in the same frame as the link;
	 * iframe –  open the item within an iframe within the same page/frame as the link;
	 * window – open the item in a new window (or tab);
	 * popup – open the item in a popup window (using the dimensions provided, if any);
	 * overlay – open the item over the top of the page where the link exists (for example, using a lightbox).
	 * none – the item is not intended for display but for storing or processing
	 * (for example, an assignment submission may just be stored without a link to
	 * it being added to the course).
	 */
	public static final String ACCEPT_PRESENTATION_DOCUMENT_TARGETS = "accept_presentation_document_targets";

	/**
	 * content_item_return_url=http://lmsng.school.edu/portal/123/page/988/item/261 (Required)
	 *
	 * Fully qualified URL where the TP redirects the user back to the TC interface.
	 * This URL can be used once the TP is finished or if the TP cannot start or
	 * has some technical difficulty.
	 */
	public static final String CONTENT_ITEM_RETURN_URL = "content_item_return_url";

	/**
	 * accept_unsigned=false | true (Optional)
	 *
	 * This indicates whether the TC is willing to accept an unsigned return
	 * message, or not.  A signed message should always be required when the
	 * content item is being created automatically in the Tool Consumer without
	 * further interaction from the user. This parameter is optional; when omitted
	 * a value of false should be assumed (i.e. the return message should be signed).
	 */
	public static final String ACCEPT_UNSIGNED = "accept_unsigned";

	/**
	 * accept_multiple=false | true (Optional)
	 *
	 * This indicates whether the user should be permitted to select more than
	 * one item.  This parameter is optional; when omitted  a value of false
	 * should be assumed (i.e. only a single item may be returned).
	 */
	public static final String ACCEPT_MULTIPLE = "accept_multiple";

	/**
	 * accept_copy_advice=false | true (Optional)
	 *
	 * This indicates whether the TC is able and willing to make a local copy
	 * of a content item.  The return message may include a expiresAt parameter
	 * to indicate that the URL provided will expire and so a copy of the content
	 * item should be stored locally before the expiry time passes.  Use a value
	 * of false (the default) to indicate that the TC has no capacity for storing
	 * local copies of content items.
	 */
	public static final String ACCEPT_COPY_ADVICE = "accept_copy_advice";

	/**
	 * auto_create=false | true (Optional)
	 *
	 * This indicates whether any content items returned by the TP would be automatically
	 * persisted without any option for the user to cancel the operation. This
	 * parameter is optional; when omitted  a value of false should be assumed
	 * (i.e. items returned may not be persisted at the TC end).
	 */
	public static final String AUTO_CREATE = "auto_create";

	/**
	 * can_confirm=false | true (Optional)
	 *
	 * This indicates whether the TC supports the feature for confirming the
	 * persistence of content items received.  When a value of true is passed,
	 * the TP may include a confirm_url parameter in its return message containing
	 * the endpoint to which a confirmation request should be sent (see below).
	 * If the TP does not support this feature or does not wish to receive a confirmation
	 * it may just omit the confirm_url parameter from its return message.
	 * This option may be used even when the auto_create parameter is set to true
	 * if a TC is willing to offer the additional reassurance that items have been
	 * persisted, or to allow a TP to be notified of resource link IDs for any
	 * LTI links which have been created.
	 */
	public static final String CAN_CONFIRM = "can_confirm";

	/**
	 * title=... (Optional)
	 *
	 * Default text to be used as the title or alt text for the content item returned
	 * by the TP.  This value should normally be short in length, for example,
	 * suitable for use as a heading.
	 */
	public static final String TITLE = "title";

	/**
	 * text=... (Optional)
	 *
	 * Default text to be used as the visible text for the content item returned by the
	 * TP.  If no text is returned by the TP, the TC may use the value of the title
	 * parameter instead (if any).  This value may be a long description of the
	 * content item.
	 */
	public static final String TEXT = "text";

	/**
	 * data=... (Optional)
	 *
	 * An opaque value which should be returned by the TP in its response.
	 */
	public static final String DATA = "data";

	/**
	 * ext_sakai_provider_eid=jsmith26
	 * <p>
	 * If set, this will signal that the external application has provided an eid which
	 * should be used preferentially. Many external applications will not have access to a user's uuid
	 * in Sakai, so this allows integrations with those systems.
	 * This parameter is optional and is unique to the Sakai Basic LTI provider.
	 */
	public static final String EXT_SAKAI_PROVIDER_EID = "ext_sakai_provider_eid";
	
	/**
	 * ext_sakai_provider_displayid=john.smith
	 * <p>
	 * If set, this will indicate to an external application that the user is normally
	 * known by this ID and when displaying the ID to the user this ID should be used instead of the
	 * user_id and ext_sakai_provider_eid.
	 * This parameter is optional and is unique to the Sakai Basic LTI provider.
	 */
	public static final String EXT_SAKAI_PROVIDER_DISPLAYID = "ext_sakai_provider_displayid";

	/**
	 * The default site type to use if a site needs to be created. Can be overriden in sakai.properties or as part of the launch.
	 * This contains a number of preconfigured roles, so that the IMS role vocabulary can be used.
	 * See BLTI-151
	 */
	public static final String NEW_SITE_TYPE = "lti";

}
