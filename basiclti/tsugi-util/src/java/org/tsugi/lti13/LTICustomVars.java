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
package org.tsugi.lti13;

/**
 * A class to capture the names of the valid LTI 1.3 substitution variables
 *
 * These are also capabilities - when used as a capability it indicates a willingness
 * to subsitute these variables on launches.
 *
 * Taken from: http://www.imsglobal.org/spec/lti/v1p3/#lti-user-variables
 */

public class LTICustomVars {

	/**
	 * USER_ID
	 *
	 *  Corresponds to the user_id launch parameterfrom the
	 *  <code>LaunchMixin</code> class.  This is the local identifier for the
	 *  user within the Tool Consumer system.
	 */
	public static final String USER_ID = "User.id";

	/**
	 * USER_IMAGE
	 *
	 *  The URL for an image of the user suitable for use as a profile picture
	 *  or avatar.
	 */
	public static final String USER_IMAGE = "User.image";

	/**
	 * USER_USERNAME
	 *
	 *  The username that identifies the user within the Tool Consumer system.
	 */
	public static final String USER_USERNAME = "User.username";

	/**
	 * USER_ORG
	 *
	 *  A URI describing the user's organisational properties; for example, an
	 *  ldap:// URI such as <code>ldap://host.com:6666/uid=user,ou=people,dc=e
	 *  xample,dc=com</code>.  If more than one format of organisational URI
	 *  is specified, each should be separated with a space.
	 */
	public static final String USER_ORG = "User.org";

	/**
	 * USER_SCOPE_MENTOR
	 *
	 *  role_scope_mentor message property value.
	 */
	public static final String USER_SCOPE_MENTOR = "User.scope.mentor";

	/**
	 * PERSON_ADDRESS_COUNTRY
	 *
	 *  The country within the user's address.  In the LIS Database, this
	 *  value corresponds to   <code>personRecord/person/address/[addressType/
	 *  instanceValue/text="Preferred"]addressPart /nameValuePair
	 *  /[instanceName/text="Country"]/instanceValue/text  </code>
	 */
	public static final String PERSON_ADDRESS_COUNTRY = "Person.address.country";

	/**
	 * PERSON_ADDRESS_LOCALITY
	 *
	 *  The city, town, or other locality within the user's address.  In the
	 *  LIS Database, this value corresponds to   <code>personRecord/person/ad
	 *  dress/[addressType/instanceValue/text="Preferred"]addressPart
	 *  /nameValuePair /[instanceName/text="Locality"]/instanceValue/text
	 *  </code>
	 */
	public static final String PERSON_ADDRESS_LOCALITY = "Person.address.locality";

	/**
	 * PERSON_ADDRESS_POSTCODE
	 *
	 *  The postal code within the user's address.  In the LIS Database, this
	 *  value corresponds to   <code>personRecord/person/address/[addressType/
	 *  instanceValue/text="Preferred"]addressPart /nameValuePair
	 *  /[instanceName/text="Postcode"]/instanceValue/text  </code>
	 */
	public static final String PERSON_ADDRESS_POSTCODE = "Person.address.postcode";

	/**
	 * PERSON_ADDRESS_STATEPR
	 *
	 *  The state or province within the user's address.  In the LIS Database,
	 *  this value corresponds to   <code>personRecord/person/address/[address
	 *  Type/instanceValue/text="Preferred"]addressPart
	 *  /nameValuePair/[instanceName/text="Statepr"]/instanceValue/text
	 *  </code>
	 */
	public static final String PERSON_ADDRESS_STATEPR = "Person.address.statepr";

	/**
	 * PERSON_ADDRESS_STREET1
	 *
	 *  The first line of the user's street address.  In the LIS Database,
	 *  this value corresponds to   <code>personRecord/person/address/[address
	 *  Type/instanceValue/text="Preferred"]addressPart /nameValuePair
	 *  /[instanceName/text="NonFieldedStreetAddress1"]/instanceValue /text
	 *  </code>
	 */
	public static final String PERSON_ADDRESS_STREET1 = "Person.address.street1";

	/**
	 * PERSON_ADDRESS_STREET2
	 *
	 *  The second line of the user's street address.  In the LIS Database,
	 *  this value corresponds to   <code>personRecord/person/address/[address
	 *  Type/instanceValue/text="Preferred"]  addressPart
	 *  /nameValuePair[instanceName/text="NonFieldedStreetAddress2"]
	 *  /instanceValue/text   </code>
	 */
	public static final String PERSON_ADDRESS_STREET2 = "Person.address.street2";

	/**
	 * PERSON_ADDRESS_STREET3
	 *
	 *  The third line of the user's street address.  In the LIS Database,
	 *  this value corresponds to   <code>personRecord/person/address/[address
	 *  Type/instanceValue/text="Preferred"]addressPart /nameValuePair
	 *  /[instanceName/text="NonFieldedStreetAddress3"] /instanceValue/text
	 *  </code>
	 */
	public static final String PERSON_ADDRESS_STREET3 = "Person.address.street3";

	/**
	 * PERSON_ADDRESS_STREET4
	 *
	 *  The fourth line of the user's street address.  In the LIS Database,
	 *  this value corresponds to   <code>personRecord/person/address/[address
	 *  Type/instanceValue/text="Preferred"]addressPart /nameValuePair
	 *  /[instanceName/text="NonFieldedStreetAddress4"] /instanceValue/text
	 *  </code>
	 */
	public static final String PERSON_ADDRESS_STREET4 = "Person.address.street4";

	/**
	 * PERSON_ADDRESS_TIMEZONE
	 *
	 *  The user's time zone.  In the LIS Database, this value corresponds to
	 *  <code>personRecord/person/address/[addressType/instanceValue/text="Pre
	 *  ferred"]addressPart /nameValuePair
	 *  /[instanceName/text="Timezone"]/instanceValue/text  </code>
	 */
	public static final String PERSON_ADDRESS_TIMEZONE = "Person.address.timezone";

	/**
	 * PERSON_EMAIL_PERSONAL
	 *
	 *  The user's personal email address  In the LIS Database, this value
	 *  corresponds to   <code>person/contactinfo[contactinfoType/instanceValu
	 *  e/text="Email_Personal"]/contactinfoValue /text  </code>
	 */
	public static final String PERSON_EMAIL_PERSONAL = "Person.email.personal";

	/**
	 * PERSON_EMAIL_PRIMARY
	 *
	 *  The user's primary email address.  In the LIS Database, this value
	 *  corresponds to   <code>personRecord/person/contactinfo[contactinfoType
	 *  /instanceValue/text="Email_Primary"] /contactinfoValue/text  </code>
	 */
	public static final String PERSON_EMAIL_PRIMARY = "Person.email.primary";

	/**
	 * PERSON_NAME_FAMILY
	 *
	 *  The family name of the user.  In the LIS Database, this value
	 *  corresponds to   <code>personRecord/person/name/partName[instanceName/
	 *  text="Family]/instanceValue/text   </code>
	 */
	public static final String PERSON_NAME_FAMILY = "Person.name.family";

	/**
	 * PERSON_NAME_FULL
	 *
	 *  The full name of the user.  In the LIS Database, this value
	 *  corresponds to   <code>personRecord/person/formname/[formnameType/inst
	 *  anceValue/text="Full"] /formattedName/text  </code>
	 */
	public static final String PERSON_NAME_FULL = "Person.name.full";

	/**
	 * PERSON_NAME_GIVEN
	 *
	 *  The given name of the user.  In the LIS Database, this value
	 *  corresponds to   <code>personRecord/person/name/partName[instanceName/
	 *  text="Given]/instanceValue/text  </code>
	 */
	public static final String PERSON_NAME_GIVEN = "Person.name.given";

	/**
	 * PERSON_NAME_MIDDLE
	 *
	 *  The middle name of the user.  In the LIS Database, this value
	 *  corresponds to   <code>personRecord/person/name/partName[instanceName/
	 *  text="Middle]/instanceValue/text   </code>
	 */
	public static final String PERSON_NAME_MIDDLE = "Person.name.middle";

	/**
	 * PERSON_NAME_PREFIX
	 *
	 *  The prefix for the user's name, such as <em>Dr.</em>, <em>Mr.</em>,
	 *  <em>Ms.</em> etc.  In the LIS Database, this value corresponds to   <c
	 *  ode>personRecord/person/name/partName[instanceName/text="Prefix]/inst
	 *  anceValue/text   </code>
	 */
	public static final String PERSON_NAME_PREFIX = "Person.name.prefix";

	/**
	 * PERSON_NAME_SUFFIX
	 *
	 *  The suffix for the user's name, such as <em>Jr.</em>, <em>II</em>,
	 *  etc.  In the LIS Database, this value corresponds to   <code>personRec
	 *  ord/person/name/partName[instanceName/text="Suffix]/instanceValue/tex
	 *  t   </code>
	 */
	public static final String PERSON_NAME_SUFFIX = "Person.name.suffix";

	/**
	 * PERSON_PHONE_HOME
	 *
	 *  The user's home phone number  In the LIS Database, this value
	 *  corresponds to   <code>personRecord/person/contactinfo
	 *  [contactinfoType/instanceValue/text="Telephone_Home"]/contactinfoValue
	 *  /text  </code>
	 */
	public static final String PERSON_PHONE_HOME = "Person.phone.home";

	/**
	 * PERSON_PHONE_MOBILE
	 *
	 *  The user's mobile phone number  In the LIS Database, this value
	 *  corresponds to   <code>personRecord/person/contactinfo[contactinfoType
	 *  /instanceValue/text="Mobile"] /contactInfoValue/text  </code>
	 */
	public static final String PERSON_PHONE_MOBILE = "Person.phone.mobile";

	/**
	 * PERSON_PHONE_PRIMARY
	 *
	 *  The user's primary phone number  In the LIS Database, this value
	 *  corresponds to   <code>personRecord/person/contactinfo [contactinfoTyp
	 *  e/instanceValue/text="Telephone_Primary"]/contactinfoValue /text
	 *  </code>
	 */
	public static final String PERSON_PHONE_PRIMARY = "Person.phone.primary";

	/**
	 * PERSON_PHONE_WORK
	 *
	 *  The user's work phone number.  In the LIS Database, this value
	 *  corresponds to   <code>personRecord/person/contactinfo
	 *  [contactinfoType/instanceValue/text="Telephone_Work"]/contactinfoValue
	 *  /text  </code>
	 */
	public static final String PERSON_PHONE_WORK = "Person.phone.work";

	/**
	 * PERSON_SMS
	 *
	 *  The number at which the user prefers to receive SMS text messages.  In
	 *  the LIS Database, this value corresponds to   <code>personRecord/perso
	 *  n/contactinfo[contactinfoType/instanceValue/text="SMS"]
	 *  /contactinfoValue/text  </code>
	 */
	public static final String PERSON_SMS = "Person.sms";

	/**
	 * PERSON_SOURCEDID
	 *
	 *  The LIS identifier for the user.    In the LIS Database, this value
	 *  corresponds to <code>personRecord/sourcedId</code>
	 */
	public static final String PERSON_SOURCEDID = "Person.sourcedId";

	/**
	 * PERSON_WEBADDRESS
	 *
	 *  The user's web address.  This could be a facebook address, a blog, or
	 *  any other web address linked to the user.  The value should be a URL.
	 *  In the LIS Database, this value corresponds to   <code>personRecord/pe
	 *  rson/contactinfo[contactinfoType/instanceValue/text="Web-Address"]
	 *  /contactinfoValue/text  </code>
	 */
	public static final String PERSON_WEBADDRESS = "Person.webaddress";

	/**
	 * CONTEXT_ID
	 *
	 *  context.id property
	 */
	public static final String CONTEXT_ID = "Context.id";

	/**
	 * CONTEXT_ORG
	 *
	 *  A URI describing the organisational properties of the context from
	 *  which a launch request originates (typically a CourseSection); for
	 *  example, an ldap:// URI such as <code>ldap://host.com:6666/cid=abc123,
	 *  ou=dept,dc=plainjoe,dc=org</code>. If more than one format of
	 *  organisational URI is specified, each should be separated with a
	 *  space.
	 */
	public static final String CONTEXT_ORG = "Context.org";

	/**
	 * CONTEXT_TYPE
	 *
	 *  context.type property
	 */
	public static final String CONTEXT_TYPE = "Context.type";
	public static final String CONTEXT_TYPE_DEFAULT = "CourseSection";

	/**
	 * CONTEXT_LABEL
	 *
	 *  context.label property
	 */
	public static final String CONTEXT_LABEL = "Context.label";

	/**
	 * CONTEXT_TITLE
	 *
	 *  context.title property
	 */
	public static final String CONTEXT_TITLE = "Context.title";

	/**
	 * CONTEXT_SOURCEDID
	 *
	 *  The sourcedID of the context
	 */
	public static final String CONTEXT_SOURCEDID = "Context.sourcedId";

	/**
	 * CONTEXT_ID_HISTORY
	 *
	 * A comma-separated list of URL-encoded context ID values representing previous
	 * copies of the context; the ID of most recent copy should appear first in the
	 * list followed by any earlier IDs in reverse chronological order. If the
	 * context was created from scratch, not as a copy of an existing context,
	 * then this variable should have an empty value.
	 */
	public static final String CONTEXT_ID_HISTORY = "Context.id.history";

	/**
	 * RESOURCELINK_ID
	 *
	 *  This is the local identifier for the resource link within the Tool
	 *  Consumer system from which the launch occurred.
	 */
	public static final String RESOURCELINK_ID = "ResourceLink.id";

	/**
	 * RESOURCELINK_TITLE
	 *
	 *  A plain text title for the resource.
	 */
	public static final String RESOURCELINK_TITLE = "ResourceLink.title";

	/**
	 * RESOURCELINK_DESCRIPTION
	 *
	 *  A plain text description of the links destination, suitable for
	 *  display alongside the link.
	 */
	public static final String RESOURCELINK_DESCRIPTION = "ResourceLink.description";

	/**
	 * RESOURCELINK_ID_HISTORY
	 *
	 * A comma-separated list of URL-encoded resource link ID values representing
	 * the ID of the link from a previous copy of the context; the most recent
	 * copy should appear first in the list followed by any earlier IDs in reverse
	 * chronological order. If the link was first added to the current context
	 * then this variable should have an empty value.
	 */
	public static final String RESOURCELINK_ID_HISTORY = "ResourceLink.id.history";

	/**
	 * COURSETEMPLATE_SOURCEDID
	 *
	 *  The LIS identifier for the Course Template.  In the LIS Database, this
	 *  value corresponds to   <code>courseTemplateRecord/sourcedId  </code>
	 */
	public static final String COURSETEMPLATE_SOURCEDID = "CourseTemplate.sourcedId";

	/**
	 * COURSETEMPLATE_LABEL
	 *
	 *  A human readable label used to help identify the Course Template.  In
	 *  the LIS Database, this value corresponds to
	 *  <code>courseTemplateRecord/courseTemplate/label/textString  </code>
	 */
	public static final String COURSETEMPLATE_LABEL = "CourseTemplate.label";

	/**
	 * COURSETEMPLATE_TITLE
	 *
	 *  The title of the Course Template.  In the LIS Database, this value
	 *  corresponds to
	 *  <code>courseTemplateRecord/courseTemplate/title/textString  </code>
	 */
	public static final String COURSETEMPLATE_TITLE = "CourseTemplate.title";

	/**
	 * COURSETEMPLATE_SHORTDESCRIPTION
	 *
	 *  A short description of the Course Template.  In the LIS Database, this
	 *  value corresponds to   <code>courseTemplateRecord/courseTemplate/catal
	 *  ogDescription/shortDescription  </code>
	 */
	public static final String COURSETEMPLATE_SHORTDESCRIPTION = "CourseTemplate.shortDescription";

	/**
	 * COURSETEMPLATE_LONGDESCRIPTION
	 *
	 *  A long description of the Course Template.  In the LIS Database, this
	 *  value corresponds to   <code>courseTemplateRecord/courseTemplate/catal
	 *  ogDescription/longDescription  </code>
	 */
	public static final String COURSETEMPLATE_LONGDESCRIPTION = "CourseTemplate.longDescription";

	/**
	 * COURSETEMPLATE_COURSENUMBER
	 *
	 *  The course number, such as "Biology 101".  In general, this number is
	 *  not just a numeric value.  In the LIS Database, this value corresponds
	 *  to   <code>courseTemplateRecord/courseTemplate/courseNumber/textString
	 *  </code>
	 */
	public static final String COURSETEMPLATE_COURSENUMBER = "CourseTemplate.courseNumber";

	/**
	 * COURSETEMPLATE_CREDITS
	 *
	 *  The default credits set for this Course Template.  In the LIS
	 *  Database, this value corresponds to
	 *  <code>courseTemplateRecord/courseTemplate/defaultCredits/textString
	 *  </code>
	 */
	public static final String COURSETEMPLATE_CREDITS = "CourseTemplate.credits";

	/**
	 * COURSEOFFERING_SOURCEDID
	 *
	 *  The LIS identifier for the Course Offering.  In the LIS Database, this
	 *  value corresponds to   <code>courseOfferingRecord/sourcedId  </code>
	 */
	public static final String COURSEOFFERING_SOURCEDID = "CourseOffering.sourcedId";

	/**
	 * COURSEOFFERING_LABEL
	 *
	 *  A human readable label for the Course Offering  In the LIS Database,
	 *  this value corresponds to
	 *  <code>courseOfferingRecord/courseOffering/label  </code>
	 */
	public static final String COURSEOFFERING_LABEL = "CourseOffering.label";

	/**
	 * COURSEOFFERING_TITLE
	 *
	 *  The title of the Course Offering.  In the LIS Database, this value
	 *  corresponds to   <code>courseOfferingRecord/courseOffering/title
	 *  </code>
	 */
	public static final String COURSEOFFERING_TITLE = "CourseOffering.title";

	/**
	 * COURSEOFFERING_SHORTDESCRIPTION
	 *
	 *  A short description of the Course Offering.  In the LIS Database, this
	 *  value corresponds to   <code>courseOfferingRecord/courseOffering/catal
	 *  ogDescription/shortDescription  </code>
	 */
	public static final String COURSEOFFERING_SHORTDESCRIPTION = "CourseOffering.shortDescription";

	/**
	 * COURSEOFFERING_LONGDESCRIPTION
	 *
	 *  A long description of the Course Offering.  In the LIS Database, this
	 *  value corresponds to   <code>courseOfferingRecord/courseOffering/catal
	 *  ogDescription/longDescription  </code>
	 */
	public static final String COURSEOFFERING_LONGDESCRIPTION = "CourseOffering.longDescription";

	/**
	 * COURSEOFFERING_COURSENUMBER
	 *
	 *  The course number, such as "Biology 101".  In general, this number is
	 *  not just a numeric value.  In the LIS Database, this value corresponds
	 *  to   <code>courseSectionRecord/courseSection/courseNumber/textString
	 *  </code>
	 */
	public static final String COURSEOFFERING_COURSENUMBER = "CourseSection.courseNumber";

	/**
	 * COURSEOFFERING_CREDITS
	 *
	 *  The default credits set for this Course Offering  In the LIS Database,
	 *  this value corresponds to
	 *  <code>courseOfferingRecord/courseOffering/defaultCredits/textString
	 *  </code>
	 */
	public static final String COURSEOFFERING_CREDITS = "CourseOffering.credits";

	/**
	 * COURSEOFFERING_ACADEMICSESSION
	 *
	 *  The text data that is used to describe the academic session for the
	 *  course offering.  In the LIS Database, this value corresponds to
	 *  <code>courseOfferingRecord/courseOffering/defaultCredits/textString
	 *  </code>
	 */
	public static final String COURSEOFFERING_ACADEMICSESSION = "CourseOffering.academicSession";

	/**
	 * COURSESECTION_SOURCEDID
	 *
	 *  The LIS identifier for the Course Section  In the LIS Database, this
	 *  value corresponds to   <code>courseSection/sourcedId  </code>
	 */
	public static final String COURSESECTION_SOURCEDID = "CourseSection.sourcedId";

	/**
	 * COURSESECTION_LABEL
	 *
	 *  A human readable label for the Course Section.  In the LIS Database,
	 *  this value corresponds to
	 *  <code>courseSectionRecord/courseSection/label  </code>
	 */
	public static final String COURSESECTION_LABEL = "CourseSection.label";

	/**
	 * COURSESECTION_TITLE
	 *
	 *  The title of the Course Section.  In the LIS Database, this value
	 *  corresponds to   <code>courseSectionRecord/courseSection/title
	 *  </code>
	 */
	public static final String COURSESECTION_TITLE = "CourseSection.title";

	/**
	 * COURSESECTION_SHORTDESCRIPTION
	 *
	 *  A short description of the Course Section.  In the LIS Database, this
	 *  value corresponds to   <code>courseSectionRecord/courseSection/catalog
	 *  Description/shortDescription  </code>
	 */
	public static final String COURSESECTION_SHORTDESCRIPTION = "CourseSection.shortDescription";

	/**
	 * COURSESECTION_LONGDESCRIPTION
	 *
	 *  A long description of the Course Section.  In the LIS Database, this
	 *  value corresponds to   <code>courseSectionRecord/courseSection/catalog
	 *  Description/longDescription  </code>
	 */
	public static final String COURSESECTION_LONGDESCRIPTION = "CourseSection.longDescription";

	/**
	 * COURSESECTION_COURSENUMBER
	 *
	 *  The course number, such as "Biology 101".  In general, this number is
	 *  not just a numeric value.  In the LIS Database, this value corresponds
	 *  to   <code>courseSectionRecord/courseSection/courseNumber/textString
	 *  </code>
	 */
	public static final String COURSESECTION_COURSENUMBER = "CourseSection.courseNumber";

	/**
	 * COURSESECTION_CREDITS
	 *
	 *  The default credits set for the Course Section.  In the LIS Database,
	 *  this value corresponds to
	 *  <code>courseSectionRecord/courseSection/defaultCredits/textString
	 *  </code>
	 */
	public static final String COURSESECTION_CREDITS = "CourseSection.credits";

	/**
	 * COURSESECTION_MAXNUMBEROFSTUDENTS
	 *
	 *  The maximum number of students that can be enrolled in the Course
	 *  Section.  In the LIS Database, this value corresponds to
	 *  <code>courseSectionRecord/courseSection/maxNumberofStudents  </code>
	 */
	public static final String COURSESECTION_MAXNUMBEROFSTUDENTS = "CourseSection.maxNumberofStudents";

	/**
	 * COURSESECTION_NUMBEROFSTUDENTS
	 *
	 *  The number of students who are enrolled in the Course Section.  In the
	 *  LIS Database, this value corresponds to
	 *  <code>courseSectionRecord/courseSection/numberofStudents  </code>
	 */
	public static final String COURSESECTION_NUMBEROFSTUDENTS = "CourseSection.numberofStudents";

	/**
	 * COURSESECTION_DEPT
	 *
	 *  The department within which the Course Section is offered.  In the LIS
	 *  Database, this value corresponds to   <code>courseSectionRecord/course
	 *  Section/org[type/textString="Dept"]/orgName/textString  </code>
	 */
	public static final String COURSESECTION_DEPT = "CourseSection.dept";

	/**
	 * COURSESECTION_TIMEFRAME_BEGIN
	 *
	 *  The date and time when the Course Section becomes available.  In the
	 *  LIS Database, this value corresponds to
	 *  <code>courseSectionRecord/courseSection/timeFrame/begin  </code>
	 */
	public static final String COURSESECTION_TIMEFRAME_BEGIN = "CourseSection.timeFrame.begin";

	/**
	 * COURSESECTION_TIMEFRAME_END
	 *
	 *  The date and time after which the Course Section is no longer
	 *  available.  In the LIS Database, this value corresponds to
	 *  <code>courseSectionRecord/courseSection/timeFrame/end  </code>
	 */
	public static final String COURSESECTION_TIMEFRAME_END = "CourseSection.timeFrame.end";

	/**
	 * COURSESECTION_ENROLLCONTROLL_ACCEPT
	 *
	 *  A boolean value that specifies whether the Course Section is accepting
	 *  enrollments.  In the LIS Database, this value corresponds to
	 *  <code>courseSectionRecord/courseSection/enrollControl/enrollAccept
	 *  </code>
	 */
	public static final String COURSESECTION_ENROLLCONTROLL_ACCEPT = "CourseSection.enrollControll.accept";

	/**
	 * COURSESECTION_ENROLLCONTROL_ALLOWED
	 *
	 *  A boolean value that specifies whether the Tool Provider can enroll
	 *  people in the Course Section.  The value <code>false</code> indicates
	 *  that only the source system can enroll people.  In the LIS Database,
	 *  this value corresponds to
	 *  <code>courseSectionRecord/courseSection/enrollControl/enrollAllowed
	 *  </code>
	 */
	public static final String COURSESECTION_ENROLLCONTROL_ALLOWED = "CourseSection.enrollControl.allowed";

	/**
	 * COURSESECTION_DATASOURCE
	 *
	 *  An identifier for the original source system of the CourseSection
	 *  object.  In the LIS Database, this value corresponds to
	 *  <code>courseSectionRecord/courseSection/dataSource  </code>
	 */
	public static final String COURSESECTION_DATASOURCE = "CourseSection.dataSource";

	/**
	 * COURSESECTION_SOURCESECTIONID
	 *
	 *  The identifier for the source Course Section from which the target
	 *  Course Section was cloned.  In the LIS Database, this value
	 *  corresponds to
	 *  <code>createCourseSectionFromCourseSectionRequest/sourcedId  </code>
	 */
	public static final String COURSESECTION_SOURCESECTIONID = "CourseSection.sourceSectionId";

	/**
	 * GROUP_SOURCEDID
	 *
	 *  The LIS identifier for the Group.  In the LIS Database, this value
	 *  corresponds to   <code>groupRecord/sourcedId  </code>
	 */
	public static final String GROUP_SOURCEDID = "Group.sourcedId";

	/**
	 * GROUP_TYPEVALUE
	 *
	 *  groupRecord/group/groupType/typevalue/textString
	 */
	public static final String GROUP_TYPEVALUE = "Group.typevalue";

	/**
	 * GROUP_EMAIL
	 *
	 *  An email address used for posting messages to members of the group.
	 *  In the LIS Database, this value corresponds to
	 *  <code>groupRecord/group/email  </code>
	 */
	public static final String GROUP_EMAIL = "Group.email";

	/**
	 * GROUP_ENROLLCONTROL_ACCEPT
	 *
	 *  A boolean value that specifies whether the Group is accepting
	 *  enrollments.  In the LIS Database, this value corresponds to
	 *  <code>groupRecord/group/enrollControl/enrollAccept  </code>
	 */
	public static final String GROUP_ENROLLCONTROL_ACCEPT = "Group.enrollControl.accept";

	/**
	 * GROUP_ENROLLCONTROL_ALLOWED
	 *
	 *  A boolean value that specifies whether the Tool Provider can enroll
	 *  people in the Group.  The value <code>false</code> indicates that only
	 *  the source system can enroll people.  In the LIS Database, this value
	 *  corresponds to   <code>groupRecord/group/enrollControl/enrollAllowed
	 *  </code>
	 */
	public static final String GROUP_ENROLLCONTROL_ALLOWED = "Group.enrollControl.allowed";

	/**
	 * GROUP_LONGDESCRIPTION
	 *
	 *  A long description of the Group.  In the LIS Database, this value
	 *  corresponds to   <code>groupRecord/group/description/longDescription
	 *  </code>
	 */
	public static final String GROUP_LONGDESCRIPTION = "Group.longDescription";

	/**
	 * GROUP_PARENTID
	 *
	 *  An identifier for the parent group within which the target group is
	 *  nested.  In the LIS Database, this value corresponds to
	 *  <code>groupRecord/group/relationship[relation="Parent"]/sourcedId
	 *  </code>
	 */
	public static final String GROUP_PARENTID = "Group.parentId";

	/**
	 * GROUP_SHORTDESCRIPTION
	 *
	 *  A short description of the Group.  In the LIS Database, this value
	 *  corresponds to   <code>groupRecord/group/description/shortDescription
	 *  </code>
	 */
	public static final String GROUP_SHORTDESCRIPTION = "Group.shortDescription";

	/**
	 * GROUP_TIMEFRAME_BEGIN
	 *
	 *  The date and time when access to Group resources begins.  In the LIS
	 *  Database, this value corresponds to
	 *  <code>groupRecord/group/timeframe/begin  </code>
	 */
	public static final String GROUP_TIMEFRAME_BEGIN = "Group.timeFrame.begin";

	/**
	 * GROUP_TIMEFRAME_END
	 *
	 *  The date and time when access to Group resources ends.  In the LIS
	 *  Database, this value corresponds to
	 *  <code>groupRecord/group/timeframe/end  </code>
	 */
	public static final String GROUP_TIMEFRAME_END = "Group.timeFrame.end";

	/**
	 * GROUP_URL
	 *
	 *  The web address of the Group.  In the LIS Database, this value
	 *  corresponds to   <code>groupRecord/group/url  </code>
	 */
	public static final String GROUP_URL = "Group.url";

	/**
	 * MEMBERSHIP_SOURCEDID
	 *
	 *  The LIS identifier for the Membership.  In the LIS Database, this
	 *  value corresponds to   <code>membershipRecord/sourcedId  </code>
	 */
	public static final String MEMBERSHIP_SOURCEDID = "Membership.sourcedId";

	/**
	 * MEMBERSHIP_COLLECTIONSOURCEDID
	 *
	 *  The LIS identifier for the organizational unit (Course Section, Group,
	 *  etc.) to which the Membership pertains.  In the LIS Database, this
	 *  value corresponds to
	 *  <code>membershipRecord/membership/collectionSourcedId  </code>
	 */
	public static final String MEMBERSHIP_COLLECTIONSOURCEDID = "Membership.collectionSourcedId";

	/**
	 * MEMBERSHIP_CREATEDTIMESTAMP
	 *
	 *  The date and time when the membership role was created.  If the Person
	 *  has more than one role within the organizational unit, then this value
	 *  is a comma separated list corresponding to the roles listed by the
	 *  Membership.role variable.  In the LIS Database, this value corresponds
	 *  to   <code>membershipRecord/membership/member/role/dateTime  </code>
	 */
	public static final String MEMBERSHIP_CREATEDTIMESTAMP = "Membership.createdTimestamp";

	/**
	 * MEMBERSHIP_DATASOURCE
	 *
	 *  An identifier for the original source system of the Membership record.
	 *  In the LIS Database, this value corresponds to
	 *  <code>membershipRecord/membership/member/role/dataSource  </code>
	 */
	public static final String MEMBERSHIP_DATASOURCE = "Membership.dataSource";

	/**
	 * MEMBERSHIP_PERSONSOURCEDID
	 *
	 *  The LIS identifier for the Person associated with the Membership.  In
	 *  the LIS Database, this value corresponds to
	 *  <code>membershipRecord/membership/member/personSourcedId  </code>
	 */
	public static final String MEMBERSHIP_PERSONSOURCEDID = "Membership.personSourcedId";

	/**
	 * MEMBERSHIP_ROLE
	 *
	 *  A comma separated list of roles that the Person has within the
	 *  organizational unit.  In the LIS Database, this value corresponds to
	 *  <code>membershipRecord/membership/member/role/roleType  </code>
	 */
	public static final String MEMBERSHIP_ROLE = "Membership.role";
	public static final String MEMBERSHIP_ROLE_LEARNER = "Learner";
	public static final String MEMBERSHIP_ROLE_INSTRUCTOR = "Instructor";

	/**
	 * MEMBERSHIP_STATUS
	 *
	 *  Indicates if the membership is active or inactive.  In accordance with
	 *  the LIS specification, the value should be either <code>Active</code>
	 *  or <code>Inactive</code>.  If the Person has more than one role within
	 *  the organizational unit, then this value is a comma separated list,
	 *  where the values are ordered in correspondence with the roles named by
	 *  the Membership.role variable.  In the LIS Database, this value
	 *  corresponds to   <code>membershipRecord/membership/member/role/status
	 *  </code>
	 */
	public static final String MEMBERSHIP_STATUS = "Membership.status";

	/**
	 * MESSAGE_RETURNURL
	 *
	 * URL for returning the user to the platform (for example, the launch_presentation.return_url property).
	 */
	public static final String MESSAGE_RETURNURL = "Message.returnUrl";

	/**
	 * MESSAGE_DOCUMENTTARGET
	 *
	 * launch_presentation.document_target property.
	 */
	public static final String MESSAGE_DOCUMENTTARGET = "Message.documentTarget";

	/**
	 * MESSAGE_HEIGHT
	 *
	 * launch_presentation.height property.
	 */
	public static final String MESSAGE_HEIGHT = "Message.height";

	/**
	 * MESSAGE_WIDTH
	 *
	 * launch_presentation.width property.
	 */
	public static final String MESSAGE_WIDTH = "Message.width";

	/**
	 * MESSAGE_LOCALE
	 *
	 *  The locale of this launch
	 */
	public static final String MESSAGE_LOCALE = "Message.locale";

	/**
	 * TOOLPLATFORM_PRODUCTFAMILYCODE
	 *
	 *  tool_platform.product_family_code property
	 */
	public static final String TOOLPLATFORM_PRODUCTFAMILYCODE = "ToolPlatform.productFamilyCode";

	/**
	 * TOOLPLATFORM_VERSION
	 *
	 *  tool_platform.version property
	 */
	public static final String TOOLPLATFORM_VERSION = "ToolPlatform.version";

	/**
	 * TOOLPLATFORMINSTANCE_GUID
	 *
	 *  tool_platform.instance_guid property
	 */
	public static final String  TOOLPLATFORMINSTANCE_GUID = "ToolPlatformInstance.guid";

	/**
	 * TOOLPLATFORMINSTANCE_NAME
	 *
	 *  tool_platform.instance_name property
	 */
	public static final String  TOOLPLATFORMINSTANCE_NAME = "ToolPlatformInstance.name";

	/**
	 * TOOLPLATFORMINSTANCE_DESCRIPTION
	 *
	 *  tool_platform.instance_description property
	 */
	public static final String  TOOLPLATFORMINSTANCE_DESCRIPTION = "ToolPlatformInstance.description";

	/**
	 * TOOLPLATFORMINSTANCE_URL
	 *
	 *  tool_platform.instance_url property
	 */
	public static final String  TOOLPLATFORMINSTANCE_URL = "ToolPlatformInstance.url";

	/**
	 * TOOLPLATFORMINSTANCE_CONTACTEMAIL
	 *
	 *  tool_platform.instance_contact_email property
	 */
	public static final String  TOOLPLATFORMINSTANCE_CONTACTEMAIL = "ToolPlatformInstance.contactEmail";

	/**
	 * LINEITEM_DATASOURCE
	 *
	 *  An identifier for the original source system of the LineItem record.
	 *  In the LIS Database, this value corresponds to
	 *  <code>lineItemRecord/lineItem/dataSource  </code>
	 */
	public static final String LINEITEM_DATASOURCE = "LineItem.dataSource";

	/**
	 * LINEITEM_RESULTVALUE_MAX
	 *
	 *  The maximum numeric score that a learner may earn on the assignment
	 *  associated with this LineItem.  In the LIS Database, this value
	 *  corresponds to   <code>resultValueRecord/resultValue/valueRange/max
	 *  </code>  where  <code>resultValueRecord.sourcedId =
	 *  lineItemRecord/lineItem/resultValueSourcedId
	 */
	public static final String LINEITEM_RESULTVALUE_MAX = "LineItem.resultValue.max";

	/**
	 * LINEITEM_SOURCEDID
	 *
	 *  The LIS identifier for the LineItem  In the LIS Database, this value
	 *  corresponds to   <code>lineItemRecord/sourcedId  </code>
	 */
	public static final String LINEITEM_SOURCEDID = "LineItem.sourcedId";

	/**
	 * LINEITEM_TYPE
	 *
	 *  A URI that uniquely identifies the LineItem type.  This convention
	 *  differs from the LIS convention of using a structured object to
	 *  describe LineItem types.   The URI <em>should</em> resolve to a JSON-
	 *  LD resource that describes the LineItem type.  As a best practice the
	 *  URI should start with a base URL that identifies the LineItemType
	 *  vocabulary and end with a relative URL for a type within that
	 *  vocabulary.  In the LIS Database, the LineItem type is given by
	 *  <code>lineItemRecord/lineItem/lineItemType  </code>
	 */
	public static final String LINEITEM_TYPE = "LineItem.type";

	/**
	 * LINEITEM_TYPE_DISPLAYNAME
	 *
	 *  The display name for the LineItemType.  In the LIS Database, this
	 *  value corresponds to
	 *  <code>lineItemTypeRecord/lineItemType/displayName  </code>
	 */
	public static final String LINEITEM_TYPE_DISPLAYNAME = "LineItem.type.displayName";

	/**
	 * LTILINK_CUSTOM_URL
	 *
	 *  The endpoint URL for accessing link-level tool settings.
	 */
	public static final String LTILINK_CUSTOM_URL = "LtiLink.custom.url";


	/**
	 * RESULT_COMMENT
	 *
	 *  A comment associated with the outcome which may be made visible to the
	 *  student.
	 */
	public static final String RESULT_COMMENT = "Result.comment";

	/**
	 * RESULT_CREATEDTIMESTAMP
	 *
	 *  The date and time when the Result was created.  In the LIS Database,
	 *  this value corresponds to   <code>  </code>
	 */
	public static final String RESULT_CREATEDTIMESTAMP = "Result.createdTimestamp";

	/**
	 * RESULT_DATASOURCE
	 *
	 *  An identifier for the original source system of the Result record.  In
	 *  the LIS Database, this value corresponds to
	 *  <code>resultRecord/result/dataSource  </code>
	 */
	public static final String RESULT_DATASOURCE = "Result.dataSource";

	/**
	 * RESULT_RESULTSCORE
	 *
	 *  The score that the learner earned on the assignment or activity to
	 *  which this Result pertains.  In the LIS Database, this value
	 *  corresponds to   <code>resultRecord/result/resultScore/textString
	 *  </code>
	 */
	public static final String RESULT_RESULTSCORE = "Result.resultScore";

	/**
	 * RESULT_SOURCEDID
	 *
	 *  The LIS identifier for the Result resource.
	 */
	public static final String RESULT_SOURCEDID = "Result.sourcedId";

	/**
	 * RESULT_URL
	 *
	 *  The URL of the Result resource.  Client applications may issue an HTTP
	 *  request to read, update or delete the resource at this URL.
	 */
	public static final String RESULT_URL = "Result.url";

	/**
	 * BASICOUTCOME_URL
	 *
	 * Enables the substitution variable $BasicOutcome.url
	 */
	public static final String BASICOUTCOME_URL = "BasicOutcome.url";

	/**
	 * BASICOUTCOME_SOURCEDID
	 *
	 * Enables the substitution variable $BasicOutcome.sourcedId
	 */
	public static final String BASICOUTCOME_SOURCEDID = "BasicOutcome.sourcedId";

	/**
	 * TOOLCONSUMERINFO_PRODUCTFAMILYCODE
	 *
	 *  The code for the product (i.e. like "sakai" or "learn")
	 */
	public static final String TOOLCONSUMERINFO_PRODUCTFAMILYCODE = "ToolConsumerInfo.productFamilyCode";

	/**
	 * TOOLCONSUMERINFO_VERSION
	 *
	 *  The code for the product (i.e. like "sakai" or "learn")
	 */
	public static final String TOOLCONSUMERINFO_VERSION = "ToolConsumerInfo.version";

	/**
	 * ALL_VARIABLES - A list of all of the subsitution variables
	 */
	public static final String ALL_VARIABLES [] = {
		USER_ID, USER_IMAGE, USER_USERNAME, USER_ORG, USER_SCOPE_MENTOR,
		PERSON_ADDRESS_COUNTRY, PERSON_ADDRESS_LOCALITY, PERSON_ADDRESS_POSTCODE,
		PERSON_ADDRESS_STATEPR, PERSON_ADDRESS_STREET1, PERSON_ADDRESS_STREET2,
		PERSON_ADDRESS_STREET3, PERSON_ADDRESS_STREET4, PERSON_ADDRESS_TIMEZONE,
		PERSON_EMAIL_PERSONAL, PERSON_EMAIL_PRIMARY,
		PERSON_NAME_FAMILY, PERSON_NAME_FULL, PERSON_NAME_GIVEN,
		PERSON_NAME_MIDDLE, PERSON_NAME_PREFIX, PERSON_NAME_SUFFIX,
		PERSON_PHONE_HOME, PERSON_PHONE_MOBILE,
		PERSON_PHONE_PRIMARY, PERSON_PHONE_WORK,
		PERSON_SMS,
		PERSON_SOURCEDID,
		PERSON_WEBADDRESS,
		CONTEXT_ID, CONTEXT_ORG, CONTEXT_TYPE, CONTEXT_TYPE_DEFAULT,
		CONTEXT_LABEL, CONTEXT_TITLE, CONTEXT_SOURCEDID,
		CONTEXT_ID_HISTORY,
		RESOURCELINK_ID, RESOURCELINK_TITLE,
		RESOURCELINK_DESCRIPTION, RESOURCELINK_ID_HISTORY,
		COURSETEMPLATE_SOURCEDID, COURSETEMPLATE_LABEL, COURSETEMPLATE_TITLE,
		COURSETEMPLATE_SHORTDESCRIPTION, COURSETEMPLATE_LONGDESCRIPTION,
		COURSETEMPLATE_COURSENUMBER, COURSETEMPLATE_CREDITS,
		COURSEOFFERING_SOURCEDID, COURSEOFFERING_LABEL, COURSEOFFERING_TITLE,
		COURSEOFFERING_SHORTDESCRIPTION, COURSEOFFERING_LONGDESCRIPTION,
		COURSEOFFERING_COURSENUMBER, COURSEOFFERING_CREDITS,
		COURSEOFFERING_ACADEMICSESSION,
		COURSESECTION_SOURCEDID, COURSESECTION_LABEL, COURSESECTION_TITLE,
		COURSESECTION_SHORTDESCRIPTION, COURSESECTION_LONGDESCRIPTION,
		COURSESECTION_COURSENUMBER, COURSESECTION_CREDITS,
		COURSESECTION_MAXNUMBEROFSTUDENTS, COURSESECTION_NUMBEROFSTUDENTS,
		COURSESECTION_DEPT, COURSESECTION_TIMEFRAME_BEGIN,
		COURSESECTION_TIMEFRAME_END, COURSESECTION_ENROLLCONTROLL_ACCEPT,
		COURSESECTION_ENROLLCONTROL_ALLOWED, COURSESECTION_DATASOURCE,
		COURSESECTION_SOURCESECTIONID,
		GROUP_SOURCEDID, GROUP_TYPEVALUE, GROUP_EMAIL, GROUP_ENROLLCONTROL_ACCEPT,
		GROUP_ENROLLCONTROL_ALLOWED, GROUP_LONGDESCRIPTION, GROUP_PARENTID,
		GROUP_SHORTDESCRIPTION, GROUP_TIMEFRAME_BEGIN,
		GROUP_TIMEFRAME_END, GROUP_URL,
		MEMBERSHIP_SOURCEDID, MEMBERSHIP_COLLECTIONSOURCEDID,
		MEMBERSHIP_CREATEDTIMESTAMP, MEMBERSHIP_DATASOURCE,
		MEMBERSHIP_PERSONSOURCEDID, MEMBERSHIP_ROLE,
		MEMBERSHIP_ROLE_LEARNER, MEMBERSHIP_ROLE_INSTRUCTOR,
		MEMBERSHIP_STATUS,
		MESSAGE_RETURNURL, MESSAGE_DOCUMENTTARGET, MESSAGE_HEIGHT,
		MESSAGE_WIDTH, MESSAGE_LOCALE,
		TOOLPLATFORM_PRODUCTFAMILYCODE, TOOLPLATFORM_VERSION,
		TOOLPLATFORMINSTANCE_GUID, TOOLPLATFORMINSTANCE_NAME,
		TOOLPLATFORMINSTANCE_DESCRIPTION, TOOLPLATFORMINSTANCE_URL,
		TOOLPLATFORMINSTANCE_CONTACTEMAIL, LINEITEM_RESULTVALUE_MAX,
		LINEITEM_DATASOURCE, LINEITEM_SOURCEDID, LINEITEM_TYPE,
		LINEITEM_TYPE_DISPLAYNAME, LTILINK_CUSTOM_URL,
		RESULT_COMMENT, RESULT_CREATEDTIMESTAMP,
		RESULT_DATASOURCE, RESULT_RESULTSCORE,
		RESULT_SOURCEDID, RESULT_URL,
		BASICOUTCOME_URL, BASICOUTCOME_SOURCEDID,
		TOOLCONSUMERINFO_PRODUCTFAMILYCODE, TOOLCONSUMERINFO_VERSION
	};

}
