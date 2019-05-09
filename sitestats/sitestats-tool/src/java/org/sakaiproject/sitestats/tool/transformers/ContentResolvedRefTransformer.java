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
package org.sakaiproject.sitestats.tool.transformers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.content.ContentData;
import org.sakaiproject.sitestats.api.event.detailed.content.FileData;
import org.sakaiproject.sitestats.api.event.detailed.content.FolderData;
import org.sakaiproject.util.ResourceLoader;

/**
 * View-layer logic for presenting the data contained in the ResolvedEventData object,
 * default mechanism of presentation is a simple K/V list
 * @author plukasew
 */
public class ContentResolvedRefTransformer
{
	private static final String FOLDER = "de_content_folder";
	private static final String FILE = "de_content_file_resource";

	/**
	 * Transforms ContentData for presentation to the user
	 * @param data the data
	 * @param rl resource loader for i18n
	 * @return EventDetails for presentation
	 */
	public static List<EventDetail> transform(ContentData data, ResourceLoader rl)
	{
		if (data instanceof ContentData.Deleted)
		{
			return Collections.singletonList(EventDetail.newText(rl.getString("de_error"), rl.getString("de_content_error_deleted")));
		}
		else if (data instanceof FolderData)
		{
			FolderData folder = (FolderData) data;
			return Collections.singletonList(EventDetail.newLink(rl.getString(FOLDER), folder.name, folder.url));
		}
		else if (data instanceof FolderData.AttachmentFolderData)
		{
			// httpHandler for /access/content/ treats attachments as a special case and delivers a 404 on the containing collections,
			// so we treat them like hidden folders
			return Collections.singletonList(EventDetail.newText(rl.getString(FOLDER), rl.getString("de_content_folder_hidden_attachment")));
		}
		else if (data instanceof FolderData.HiddenFolderData)
		{
			return Collections.singletonList(EventDetail.newText(rl.getString(FOLDER), rl.getString("de_content_folder_hidden")));
		}
		else if (data instanceof FileData.AttachmentFileData)
		{
			// Attachment filenames might be revealing too much if the user doesn't have read permissions in the tool
			// that the attachment pertains to, so treat them as hidden
			return Collections.singletonList(EventDetail.newText(rl.getString(FILE), rl.getString("de_content_file_hidden_attachment")));
		}
		else if (data instanceof FileData.HiddenFileData)
		{
			return Collections.singletonList(EventDetail.newText(rl.getString(FILE), rl.getString("de_content_file_hidden")));
		}

		FileData file = (FileData) data;

		List<EventDetail> details = new ArrayList<>(2);
		String resName = file.displayName.orElse(file.name);
		details.add(EventDetail.newLink(rl.getString(FILE), resName, file.folder.url));
		if (file.displayName.isPresent()) // show the true filename also
		{
			details.add(EventDetail.newText(rl.getString("de_content_file_name"), file.name));
		}

		return details;
	}
}
