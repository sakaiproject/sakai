/**
 * Copyright (c) 2006-2018 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.api.event.detailed.content;

/**
 * Data about folders in the Content tool (Resources/Drop Box/attachments)
 * @author plukasew
 */
public class FolderData implements ContentData
{
	public static final HiddenFolderData HIDDEN = new HiddenFolderData();
	public static final AttachmentFolderData ATTACHMENT = new AttachmentFolderData();

	public final String name;
	public final String url;

	/**
	 * Constructor
	 * @param name the name of the folder
	 * @param url url to the folder
	 */
	public FolderData(String name, String url)
	{
		this.name = name;
		this.url = url;
	}

	// hidden folders and attachment folders are treated the same way, details are not revealed
	public static class HiddenFolderData implements ContentData { }
	public static class AttachmentFolderData implements ContentData { }
}
