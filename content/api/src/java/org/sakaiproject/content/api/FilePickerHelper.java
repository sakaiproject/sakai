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
	 * Name of the attribute used in the tool session to pass a org.sakaiproject.service.legacy.entity.ReferenceVector back and forth to the picker.
	 */
	public static final String FILE_PICKER_ATTACHMENTS = "sakaiproject.filepicker.attachments";

	/**
	 * Name of the attribute used in the tool session to tell the consumer if this resulted in a cancel. This will be "true" or non-existent
	 */
	public static final String FILE_PICKER_CANCEL = "sakaiproject.filepicker.cancel";

	/**
	 * @deprecated use FILE_PICKER_TITLE_TEXT and FILE_PICKER_INSTRUCTION_TEXT instead
	 */
	public static final String FILE_PICKER_FROM_TEXT = "sakaiproject.filepicker.from";

	/**
	 * title for the file selection helper
	 */
	public static final String FILE_PICKER_TITLE_TEXT = "sakaiproject.filepicker.title";

	public static final String FILE_PICKER_INSTRUCTION_TEXT = "sakaiproject.filepicker.instructions";

	public static final String FILE_PICKER_SUBTITLE_TEXT = "sakaiproject.filepicker.subtitle";

	/**
	 * The name of the state attribute indicating that the file picker should return links to existing resources in an existing collection rather than copying it to the hidden attachments area. If this value is not set, all attachments are to copies in
	 * the hidden attachments area.
	 */
	public static final String FILE_PICKER_ATTACH_LINKS = "sakaiproject.filepicker.attachLinks";

	/**
	 * State Attribute for the org.sakaiproject.service.legacy.content.ContentResourceFilter object that the current filter should honor. If this is set to null, then all files will be selectable and viewable
	 */
	public static final String FILE_PICKER_RESOURCE_FILTER = "sakaiproject.filepicker.contentResourceFilter";

	public static final String FILE_PICKER_MAX_ATTACHMENTS = "sakaiproject.filepicker.maxAttachments";

}
