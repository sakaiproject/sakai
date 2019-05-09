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
package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.content.ContentData;
import org.sakaiproject.sitestats.api.event.detailed.content.FileData;
import org.sakaiproject.sitestats.api.event.detailed.content.FolderData;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.GenericRefParser;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.utils.RefResolverUtils;

@Slf4j
public class ContentReferenceResolver
{
	public static final String RESOURCES_TOOL_ID = "sakai.resources";
	public static final String DROPBOX_TOOL_ID = "sakai.dropbox";

	/**
	 * Resolves an event reference into meaningful details about the event
	 * @param ref the event reference
	 * @param tips tips for parsing out the components of the reference
	 * @param contentHostServ the content hosting service
	 * @return one of the ContentData variants, or ResolvedEventData.ERROR/PERM_ERROR
	 */
	public static ResolvedEventData resolveReference(String ref, List<EventParserTip> tips, ContentHostingService contentHostServ)
	{
		if (StringUtils.isEmpty(ref) || contentHostServ == null)
		{
			log.warn("Cannot resolve reference. Reference is null/empty or service(s) are not initialized.");
			return ResolvedEventData.ERROR;
		}

		GenericRefParser.GenericEventRef parsedRef = GenericRefParser.parse(ref, tips);
		try
		{
			// get the folder (and file if necessary)
			ContentCollection cc;
			ContentResource cr = null;
			boolean isFolder = contentHostServ.isCollection(parsedRef.entityId);
			if (!isFolder)
			{
				cr = contentHostServ.getResource(parsedRef.entityId);
				if (cr == null)
				{
					log.warn("Unexpected null returned by content hosting service for resource id " + parsedRef.entityId);
					return ResolvedEventData.ERROR;
				}
				cc = cr.getContainingCollection();
			}
			else
			{
				cc = contentHostServ.getCollection(parsedRef.entityId);
			}
			if (cc == null)
			{
				log.warn("Unexpected null returned by content hosting service for collection id " + parsedRef.entityId);
				return ResolvedEventData.ERROR;
			}

			// get the folder details
			String name = getDirectoryName(cc.getId());
			String url = StringUtils.trimToEmpty(cc.getUrl());
			if (name.isEmpty() || url.isEmpty())
			{
				return ResolvedEventData.ERROR;
			}

			FolderData folder = new FolderData(name, url);
			if (isFolder)
			{
				if (contentHostServ.isAttachmentResource(cc.getId()))
				{
					return FolderData.ATTACHMENT;
				}
				if (!contentHostServ.isAvailable(cc.getId()))
				{
					return FolderData.HIDDEN;
				}

				return folder;
			}

			// it is a file resource, get the details
			String resourceName = RefResolverUtils.getResourceName(cr);
			String fileName = RefResolverUtils.getResourceFileName(cr.getId());
			ContentCollection parent = cr.getContainingCollection();
			if (resourceName == null || fileName == null || parent == null)
			{
				return ResolvedEventData.ERROR;
			}
			if (contentHostServ.isAttachmentResource(cr.getId()))
			{
				return FileData.ATTACHMENT;
			}
			if (!contentHostServ.isAvailable(cr.getId()))
			{
				return FileData.HIDDEN;
			}

			if (resourceName.equals(fileName))
			{
				return new FileData(fileName, folder);
			}

			return new FileData(fileName, resourceName, folder);
		}
		catch (IdUnusedException | TypeException e)
		{
			// Resource was most likely deleted (hard or soft delete)
			return ContentData.DELETED;
		}
		catch (PermissionException pe)
		{
			log.warn("Permission exception trying to retrieve collection/resource.", pe);
			return ResolvedEventData.PERM_ERROR;
		}
	}

	private static String getDirectoryName(String collectionId)
	{
		if (collectionId == null)
		{
			return "";
		}

		String[] delims = {"/", "\\"};
		for (String delim : delims)
		{
			int lastIndex = collectionId.lastIndexOf(delim);
			if (lastIndex >= 0)
			{
				if (lastIndex == collectionId.length() - 1)
				{
					// ends with "/"; find second last "/"
					lastIndex = collectionId.lastIndexOf(delim, lastIndex - 1);
					collectionId = collectionId.substring(lastIndex + 1, collectionId.length() - 1);
				}
				else
				{
					collectionId = collectionId.substring(lastIndex + 1);
				}
			}
		}

		return collectionId;
	}
}
