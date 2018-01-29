/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.entity.api;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import org.sakaiproject.time.api.Time;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/**
 * <p>
 * ResourceProperties models the open-ended propeties of a Sakai Entity.
 * </p>
 */
public interface ResourceProperties extends Serializable
{
	/** Property for resource creator (uploader) (automatic). [user id string] */
	static final String PROP_CREATOR = "CHEF:creator";

	/** Property for resource last one to modify (automatic). [user id string] */
	static final String PROP_MODIFIED_BY = "CHEF:modifiedby";

	/** Property for creation (upload) date (live, from DAV:). [Time] */
	static final String PROP_CREATION_DATE = "DAV:creationdate";

	/** Property for the display name (description) (dead, from DAV:). [String] */
	static final String PROP_DISPLAY_NAME = "DAV:displayname";

	/** Property for the original filename (automatic). [String] */
	static final String PROP_ORIGINAL_FILENAME = "CHEF:originalfilename";

	/** Property for the copyright attribution (user settable). [String] */
	static final String PROP_COPYRIGHT = "CHEF:copyright";

	/** Property for the copyright choice attribution (user settable). [String] */
	static final String PROP_COPYRIGHT_CHOICE = "CHEF:copyrightchoice";

	/** Property for the copyright alert attribution (user settable). [String] */
	static final String PROP_COPYRIGHT_ALERT = "CHEF:copyrightalert";

	/** Property for the content length (live, from DAV:). [Long] */
	static final String PROP_CONTENT_LENGTH = "DAV:getcontentlength";

	/** Property for the content type (live, from DAV:). [MIME type string] */
	static final String PROP_CONTENT_TYPE = "DAV:getcontenttype";

	/** Property for the last modified date (live, from DAV:, set when anything changes). [Time] */
	static final String PROP_MODIFIED_DATE = "DAV:getlastmodified";

	/** Property that distinguishes a collection from a non-collection resource (automatic). [Boolean] */
	static final String PROP_IS_COLLECTION = "CHEF:is-collection";

	/** Property that holds a ContentHosting collection body bytes quota, in K (user settable). [long] */
	static final String PROP_COLLECTION_BODY_QUOTA = "CHEF:collection-body-quota";

	/** Property to associate a chat message with a chat room (user settable). [String] */
	static final String PROP_CHAT_ROOM = "CHEF:chat-room";

	/** Property to target a message to a specific user (user settable). [User] */
	static final String PROP_TO = "CHEF:to";

	/** Property for long open description (user settable). [String] */
	static final String PROP_DESCRIPTION = "CHEF:description";

	/** Property for calendar event types (user settable). [String] */
	static final String PROP_CALENDAR_TYPE = "CHEF:calendar-type";

	/** Property for calendar event location (user settable). [String] */
	static final String PROP_CALENDAR_LOCATION = "CHEF:calendar-location";

	/** Property for the channel to categories names inside a discussion channel (user settable). [String] */
	static final String PROP_DISCUSSION_CATEGORIES = "CHEF:discussion-categories";

	/** Property that discussion reply message style is star or thread (automatic) [String] */
	static final String PROP_REPLY_STYLE = "CHEF:discussion-reply-style";

	/** Property for a message channel indicating if the channel is 'enabled' (user settable) [Boolean] */
	static final String PROP_CHANNEL_ENABLED = "CHEF:channel-enabled";

	/** Property for a site storing the email notification id associated with the site's mailbox (user settable) [String] */
	static final String PROP_SITE_EMAIL_NOTIFICATION_ID = "CHEF:site-email-notification-id";

	/** Property for a site indicating if email archiveing is enabled for the site (user settable) [Boolean] */
	static final String PROP_SITE_EMAIL_ARCHIVE = "CHEF:site-email-archive";

	/** Property for a ToolRegistration, the title of the tool (user settable) [String] */
	static final String PROP_TOOL_TITLE = "CHEF:tool-title";

	/** Property for a ToolRegistration, description of the tool (user settable) [String] */
	static final String PROP_TOOL_DESCRIPTION = "CHEF:tool-description";

	/** Property for a ToolRegistration, category of the tool (user settable) [String] %%% list desired -ggolden */
	static final String PROP_TOOL_CATEGORY = "CHEF:tool-category";

	/** Property for calendar event extra fields (user settable). [String] */
	static final String PROP_CALENDAR_EVENT_FIELDS = "CHEF:calendar-fields";

	/** Property for whether an assignment's open date will be announced (user settable). [String] */
	static final String NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE = "new_assignment_check_auto_announce";

	/** Property for whether an assignment's due date will be added into schedule as an event(user settable). [String] */
	static final String NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE = "new_assignment_check_add_due_date";

	/** Property for calendar event associated with an assignment's due date (user settable). [String] */
	static final String PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID = "CHEF:assignment_duedate_calender_event_id";

	/** Property for additional calendar event associated with an assignment's due date (user settable). [String] */
	static final String PROP_ASSIGNMENT_DUEDATE_ADDITIONAL_CALENDAR_EVENT_ID = "CHEF:assignment_duedate_additional_calendar_event_id";
	
	/** Property for announcement message id associated with an assignment's open date (user settable). [String] */
	static final String PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID = "CHEF:assignment_opendate_announcement_message_id";

	/** Property for assignment submission's previous grade (user settable). [String] */
	static final String PROP_SUBMISSION_PREVIOUS_GRADES = "CHEF:submission_previous_grades";

	/** Property for assignment submission's scaled previous grade (user settable). [String] */
	static final String PROP_SUBMISSION_SCALED_PREVIOUS_GRADES = "CHEF:submission_scaled_previous_grades";

	/** Property for assignment submission's previous inline feedback text (user settable). [String] */
	static final String PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT = "CHEF:submission_previous_feedback_text";

	/** Property for assignment submission's previous feedback comment (user settable). [String] */
	static final String PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT = "CHEF:submission_previous_feedback_comment";

	/** Property for assignment been deleted status(user settable) [String] */
	static final String PROP_ASSIGNMENT_DELETED = "CHEF:assignment_deleted";

	/** Property indicating viewable (manual). [Boolean] */
	static final String PROP_PUBVIEW = "SAKAI:pubview";

	/** URL MIME type */
	static final String TYPE_URL = "text/url";

	/** The encoding of the resource - UTF-8 or ISO-8559-1 for example */
	static final String PROP_CONTENT_ENCODING = "encoding";

	/** Property for "object type" of a structured artifact */
	static final String PROP_STRUCTOBJ_TYPE = "SAKAI:structobj_type";

	/** Used to find non structured object ContentResources (files, url's, etc.) */
	static final String FILE_TYPE = "fileResource";

	/** Property name on a ContentEntity indicating its rank if a custom priority sort is used. */
	static final String PROP_CONTENT_PRIORITY = "SAKAI:content_priority";

	/** Property name on a ContentCollection indicating that a custom priority sort is to be used. */
	public static final String PROP_HAS_CUSTOM_SORT = "SAKAI:has_custom_sort";

	/** Property name on a ContentEntity indicating the ResourceType that defines its properties. */
	static final String PROP_RESOURCE_TYPE = "SAKAI:resource_type";
	
	/** Property name on a ContentEntity indicating if we should add HTML header/footer.*/
	static final String PROP_ADD_HTML = "SAKAI:add_html";
	
	/** Property name on a ContentEntity indicating if the item is hidden but it's content is public.*/
	static final String PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT = "SAKAI:hidden_accessible_content";

	/**
	 * Property name on a Resource or Collection which will allow resources with
	 * a text/html content type to be output with an inline content-disposition
	 * (implies this resource is trusted not to include malicious javascript or
	 * other unwanted html elements) [Boolean]
	 */
	public static final String PROP_ALLOW_INLINE = "SAKAI:allow_inline";

	/**
	 * Access an iterator on the names of the defined properties (Strings).
	 * 
	 * @return An iterator on the names of the defined properties (Strings) (may be empty).
	 */
	Iterator<String> getPropertyNames();

	/**
	 * Access a named property as a string (won't find multi-valued ones.)
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value, or null if not found.
	 */
	String getProperty(String name);

	/**
	 * Access a named property as a List of (String), good for single or multi-valued properties.
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value, or null if not found.
	 */
	List<String> getPropertyList(String name);

	/**
	 * Access a named property; as a String if it's single valued, or a List of (String) if it's multi-valued (or null if it's not defined).
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value, or null if not found.
	 */
	Object get(String name);

	/**
	 * Access a named property as a properly formatted string.
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value, or an empty string if not found.
	 */
	String getPropertyFormatted(String name);

	/**
	 * Check if a named property is a live one (auto updated).
	 * 
	 * @param name
	 *        The property name.
	 * @return True if the property is a live one, false if not.
	 */
	boolean isLiveProperty(String name);

	/**
	 * Access a named property as a boolean.
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value.
	 * @exception EntityPropertyNotDefinedException
	 *            if not found.
	 * @exception EntityPropertyTypeException
	 *            if the property is found but not a boolean.
	 */
	boolean getBooleanProperty(String name) throws EntityPropertyNotDefinedException, EntityPropertyTypeException;

	/**
	 * Access a named property as a long.
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value.
	 * @exception EntityPropertyNotDefinedException
	 *            if not found.
	 * @exception EntityPropertyTypeException
	 *            if the property is found but not a long.
	 */
	long getLongProperty(String name) throws EntityPropertyNotDefinedException, EntityPropertyTypeException;

	/**
	 * Access a named property as a Time.
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value
	 * @exception EntityPropertyNotDefinedException
	 *            if not found.
	 * @exception EntityPropertyTypeException
	 *            if the property is found but not a Time.
	 * @deprecated use {@link #getDateProperty(String)}
	 */
	Time getTimeProperty(String name) throws EntityPropertyNotDefinedException, EntityPropertyTypeException;

	/**
	 * Access a named property as a Instant
	 * @param name The property name.
	 * @return the property value
	 * @throws EntityPropertyNotDefinedException if not found
	 * @throws EntityPropertyTypeException if the property is not a date 
	 */
	Instant getInstantProperty(String name) throws EntityPropertyNotDefinedException, EntityPropertyTypeException;
	
	/**
	 * Access a named property as a Date
	 * @param name The property name.
	 * @return the property value
	 * @throws EntityPropertyNotDefinedException if not found
	 * @throws EntityPropertyTypeException if the property is not a date 
	 */
	Date getDateProperty(String name) throws EntityPropertyNotDefinedException, EntityPropertyTypeException;
	/**
	 * Access a named property as a User.
	 * 
	 * @param name
	 *        The property name.
	 * @return the property value
	 * @exception EntityPropertyNotDefinedException
	 *            if not found.
	 * @exception EntityPropertyTypeException
	 *            if the property is found but not a User.
	 */
	// TODO: -ggolden User getUserProperty(String name) throws EntityPropertyNotDefinedException, EntityPropertyTypeException;

	/**
	 * Get the static String of PROP_CREATOR
	 * 
	 * @return The static String of PROP_CREATOR
	 */
	String getNamePropCreator();

	/**
	 * Get the static String of PROP_MODIFIED_BY
	 * 
	 * @return The static String of PROP_MODIFIED_BY
	 */
	String getNamePropModifiedBy();

	/**
	 * Get the static String of PROP_CREATION_DATE
	 * 
	 * @return The static String of PROP_CREATION_DATE
	 */
	String getNamePropCreationDate();

	/**
	 * Get the static String of PROP_DISPLAY_NAME
	 * 
	 * @return The static String of PROP_DISPLAY_NAME
	 */
	String getNamePropDisplayName();

	/**
	 * Get the static String of PROP_COPYRIGHT_CHOICE
	 * 
	 * @return The static String of PROP_COPYRIGHT_CHOICE
	 */
	String getNamePropCopyrightChoice();

	/**
	 * Get the static String of PROP_COPYRIGHT_ALERT
	 * 
	 * @return The static String of PROP_COPYRIGHT_ALERT
	 */
	String getNamePropCopyrightAlert();

	/**
	 * Get the static String of PROP_COPYRIGHT
	 * 
	 * @return The static String of PROP_COPYRIGHT
	 */
	String getNamePropCopyright();

	/**
	 * Get the static String of PROP_CONTENT_LENGTH
	 * 
	 * @return The static String of PROP_CONTENT_LENGTH
	 */
	String getNamePropContentLength();

	/**
	 * Get the static String of PROP_CONTENT_TYPE
	 * 
	 * @return The static String of PROP_CONTENT_TYPE
	 */
	String getNamePropContentType();

	/**
	 * Get the static String of PROP_MODIFIED_DATE
	 * 
	 * @return The static String of PROP_MODIFIED_DATE
	 */
	String getNamePropModifiedDate();

	/**
	 * Get the static String of PROP_IS_COLLECTION
	 * 
	 * @return The static String of PROP_IS_COLLECTION
	 */
	String getNamePropIsCollection();

	/**
	 * Get the static String of PROP_COLLECTION_BODY_QUOTA
	 * 
	 * @return The static String of PROP_COLLECTION_BODY_QUOTA
	 */
	String getNamePropCollectionBodyQuota();

	/**
	 * Get the static String of PROP_CHAT_ROOM
	 * 
	 * @return The static String of PROP_CHAT_ROOM
	 */
	String getNamePropChatRoom();

	/**
	 * Get the static String of PROP_TO
	 * 
	 * @return The static String of PROP_TO
	 */
	String getNamePropTo();

	/**
	 * Get the static String of PROP_DESCRIPTION
	 * 
	 * @return The static String of PROP_DESCRIPTION
	 */
	String getNamePropDescription();

	/**
	 * Get the static String of PROP_CALENDAR_TYPE
	 * 
	 * @return The static String of PROP_CALENDAR_TYPE
	 */
	String getNamePropCalendarType();

	/**
	 * Get the static String of PROP_CALENDAR_LOCATION
	 * 
	 * @return The static String of PROP_CALENDAR_LOCATION
	 */
	String getNamePropCalendarLocation();

	/**
	 * Get the static String of PROP_REPLY_STYLE
	 * 
	 * @return The static String of PROP_REPLY_STYLE
	 */
	String getNamePropReplyStyle();

	/**
	 * Get the static String of NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE
	 * 
	 * @return The static String of NEW_ASSIGNMENT_CHECK_ADD_DUE_DATE
	 */
	String getNamePropNewAssignmentCheckAddDueDate();

	/**
	 * Get the static String of NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE
	 * 
	 * @return The static String of NEW_ASSIGNMENT_CHECK_AUTO_ANNOUNCE
	 */
	String getNamePropNewAssignmentCheckAutoAnnounce();

	/**
	 * Get the static String of PROP_SUBMISSION_PREVIOUS_GRADES
	 * 
	 * @return The static String of PROP_SUBMISSION_PREVIOUS_GRADES
	 */
	String getNamePropSubmissionPreviousGrades();

	/**
	 * Get the static String of PROP_SUBMISSION_SCALED_PREVIOUS_GRADES
	 * 
	 * @return The static String of PROP_SUBMISSION_SCALED_PREVIOUS_GRADES
	 */
	String getNamePropSubmissionScaledPreviousGrades();

	/**
	 * Get the static String of PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT
	 * 
	 * @return The static String of PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT
	 */
	String getNamePropSubmissionPreviousFeedbackText();

	/**
	 * Get the static String of PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT
	 * 
	 * @return The static String of PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT
	 */
	String getNamePropSubmissionPreviousFeedbackComment();

	/**
	 * Get the static String of PROP_ASSIGNMENT_DELETED
	 * 
	 * @return The static String of PROP_ASSIGNMENT_DELETED
	 */
	String getNamePropAssignmentDeleted();

	/**
	 * Get the static String of PROP_STRUCTOBJ_TYPE
	 * 
	 * @return The static String of PROP_STRUCTOBJ_TYPE
	 */
	String getNamePropStructObjType();

	/**
	 * Get the static String of TYPE_URL
	 * 
	 * @return The static String of TYPE_URL
	 */
	String getTypeUrl();

	/**
	 * Serialize the resource into XML, adding an element to the doc under the top of the stack element.
	 * 
	 * @param doc
	 *        The DOM doc to contain the XML (or null for a string return).
	 * @param stack
	 *        The DOM elements, the top of which is the containing element of the new "resource" element.
	 * @return The newly added element.
	 */
	Element toXml(Document doc, Stack<Element> stack);

	/**
	 * Add a single valued property.
	 * 
	 * @param name
	 *        The property name.
	 * @param value
	 *        The property value.
	 */
	void addProperty(String name, String value);

	/**
	 * Add a value to a multi-valued property.
	 * 
	 * @param name
	 *        The property name.
	 * @param value
	 *        The property value.
	 */
	void addPropertyToList(String name, String value);

	/**
	 * Add all the properties from the other ResourceProperties object.
	 * 
	 * @param other
	 *        The ResourceProperties to add.
	 */
	void addAll(ResourceProperties other);

	/**
	 * Add all the properties from the Properties object.
	 * 
	 * @param props
	 *        The Properties to add.
	 */
	void addAll(Properties props);

	/**
	 * Remove all properties.
	 */
	void clear();

	/**
	 * Remove a property.
	 * 
	 * @param name
	 *        The property name.
	 */
	void removeProperty(String name);

	/**
	 * Take all values from this object.
	 * 
	 * @param other
	 *        The ResourceProperties object to take values from.
	 */
	void set(ResourceProperties other);

	/**
	 * Get a ContentHandler to handle SAX parsing of properties
	 * @return
	 */
	ContentHandler getContentHander();
}
