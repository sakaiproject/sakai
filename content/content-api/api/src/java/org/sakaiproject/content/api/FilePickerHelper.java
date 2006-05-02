/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.api;

public interface FilePickerHelper
{
	/**
	 * Name of the attribute used in the tool session or state to pass a List of References back and forth to the picker.
	 * (Corresponds to ResourcesAction.STATE_ATTACHMENTS)
	 */
	static final String FILE_PICKER_ATTACHMENTS = "sakaiproject.filepicker.attachments";

	/**
	 * The name of the state or tool-session attribute indicating that the file picker should return links to existing resources 
	 * in an existing collection rather than copying them to the hidden attachments area. If this value is not set, 
	 * all attachments are to copies in the hidden attachments area.
	 */
	static final String FILE_PICKER_ATTACH_LINKS = "sakaiproject.filepicker.attachLinks";

	/** 
	 * The name of the tool-session or state attribute for the maximum number of items to attach. The attribute value will be an Integer, 
	 * usually CARDINALITY_SINGLE or CARDINALITY_MULTIPLE. (corresponds to ResourcesAction.STATE_ATTACH_CARDINALITY)
	 */
	static final String FILE_PICKER_MAX_ATTACHMENTS = "sakaiproject.filepicker.maxAttachments";
	
	/** A constant indicating maximum of one item can be attached. */
	public static final Integer CARDINALITY_SINGLE = new Integer(1);

	/** A constant indicating any the number of attachments is unlimited. */
	public static final Integer CARDINALITY_MULTIPLE = new Integer(Integer.MAX_VALUE);

	/**
	 * title for the file selection helper (Corresponds to ResourcesAction.STATE_ATTACH_TITLE)
	 */
	static final String FILE_PICKER_TITLE_TEXT = "sakaiproject.filepicker.title";

	/** 
	 * The name of the tool-session or state attribute for the instructions when a tool uses Resources as attachment helper 
	 * (for create or attach but not for edit mode). (Corresponds to ResourcesAction.STATE_ATTACH_INSTRUCTION).
	 */
	static final String FILE_PICKER_INSTRUCTION_TEXT = "sakaiproject.filepicker.instructions";

	/**
	 * State or Tool-Session Attribute for the ContentResourceFilter object that the current filter should honor. 
	 * If this is set to null, then all files will be selectable and viewable. 
	 * (Corresponds to ResourcesAction.STATE_RESOURCE_FILTER).
	 */
	static final String FILE_PICKER_RESOURCE_FILTER = "sakaiproject.filepicker.contentResourceFilter";

	/**
	 * Name of the attribute used in the tool session to tell the consumer if this resulted in a cancel. This will be "true" or non-existent
	 * (Corresponds to ResourcesAction.STATE_HELPER_CANCELED_BY_USER).
	 */
	static final String FILE_PICKER_CANCEL = "sakaiproject.filepicker.cancel";

	/**
	 * @deprecated use FILE_PICKER_TITLE_TEXT and FILE_PICKER_INSTRUCTION_TEXT instead
	 */
	static final String FILE_PICKER_FROM_TEXT = "sakaiproject.filepicker.from";

	/**
	 * Not yet implemented.
	 */
	static final String FILE_PICKER_SUBTITLE_TEXT = "sakaiproject.filepicker.subtitle";

	/**
	 *  The name of the state or tool-session attribute indicating that dropboxes should be shown as places from which
	 *  to select attachments. The value should be a List of user-id's.  The file picker will attempt to show 
	 *  the dropbox for each user whose id is included in the list. 
	 */
	public static final String FILE_PICKER_SHOW_DROPBOXES = "sakaiproject.filepicker.show_dropboxes";

	/**
	 *  The name of the state or tool-session attribute indicating that the current user's workspace Resources collection 
	 *  should be shown as places from which to select attachments. The value should be "true".  The file picker will attempt to show 
	 *  the dropbox for each user whose id is included in the list. 
	 */
	public static final String FILE_PICKER_SHOW_WORKSPACE = "sakaiproject.filepicker.show_workspace";


}
