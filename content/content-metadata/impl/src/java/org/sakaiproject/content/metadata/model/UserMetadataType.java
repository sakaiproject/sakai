/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.content.metadata.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.sakaiproject.content.metadata.model.MetadataConverter;
import org.sakaiproject.content.metadata.model.MetadataRenderer;
import org.sakaiproject.content.metadata.model.MetadataType;
import org.sakaiproject.content.metadata.model.MetadataValidator;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * @author Colin Hebert
 */
public class UserMetadataType extends MetadataType<User>
{

	private static final long serialVersionUID = 1L;
	private static UserDirectoryService userDirectoryService;
	private static SiteService siteService;

	public UserMetadataType(UserDirectoryService userDirectoryService, SiteService siteService)
	{
		UserMetadataType.userDirectoryService = userDirectoryService;
		UserMetadataType.siteService = siteService;
	}

	public UserMetadataType()
	{
	}

	public Collection<User> getAllowedValues(String siteId)
	{
		try
		{
			return userDirectoryService.getUsers(siteService.getSite(siteId).getUsers());
		}
		catch (IdUnusedException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public MetadataRenderer getRenderer()
	{
		return new UserMetadataRenderer();
	}

	@Override
	public MetadataConverter<User> getConverter()
	{
		return new UserMetadataConverter();
	}

	@Override
	public MetadataValidator<User> getValidator()
	{
		return new UserMetadataValidator();
	}


	private final class UserMetadataValidator implements MetadataValidator<User>
	{
		public boolean validate(User metadataValue)
		{
			return true;
		}
	}

	private final class UserMetadataRenderer implements MetadataRenderer
	{
		public String getMetadataTypeEditTemplate()
		{
			return null;	//To change body of implemented methods use File | Settings | File Templates.
		}

		public String getMetadataTypeDisplayTemplate()
		{
			return null;	//To change body of implemented methods use File | Settings | File Templates.
		}

		public String getMetadataValueEditTemplate()
		{
			return "meta_edit_user";
		}

		public String getMetadataValueDisplayTemplate()
		{
			return "meta_display_user";
		}
	}

	private final class UserMetadataConverter implements MetadataConverter<User>
	{

		public String toString(User metadataValue)
		{
			return metadataValue != null ? metadataValue.getId() : null;
		}

		public User fromString(String stringValue)
		{
			try
			{
				if (stringValue == null || stringValue.isEmpty())
				{
					return null;
				}

				return userDirectoryService.getUser(stringValue);
			}
			catch (UserNotDefinedException e)
			{
				throw new RuntimeException(e);
			}
		}

		public Map<String, ?> toProperties(User metadataValue)
		{
			String value = toString(metadataValue);
			return Collections.singletonMap(getUniqueName(), value);
		}

		public User fromProperties(Map<String, ?> properties)
		{
			return fromString((String) properties.get(getUniqueName()));
		}

		public User fromHttpForm(Map<String, ?> parameters, String parameterSuffix)
		{
			return fromString((String) parameters.get(getUniqueName() + parameterSuffix));
		}
	}
}
