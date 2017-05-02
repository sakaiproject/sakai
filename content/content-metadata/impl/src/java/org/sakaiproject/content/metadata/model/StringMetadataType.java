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

import java.util.Collections;
import java.util.Map;

import org.sakaiproject.content.metadata.model.MetadataConverter;
import org.sakaiproject.content.metadata.model.MetadataRenderer;
import org.sakaiproject.content.metadata.model.MetadataType;
import org.sakaiproject.content.metadata.model.MetadataValidator;

/**
 * @author Colin Hebert
 */
public class StringMetadataType extends MetadataType<String>
{

	private static final long serialVersionUID = 1L;
	private int minLength;
	private int maxLength;
	private String regularExpression;
	//Renders textareas instead of textboxes
	private boolean longText;

	public int getMinLength()
	{
		return minLength;
	}

	public void setMinLength(int minLength)
	{
		this.minLength = minLength;
	}

	public int getMaxLength()
	{
		return maxLength;
	}

	public void setMaxLength(int maxLength)
	{
		this.maxLength = maxLength;
	}

	public String getRegularExpression()
	{
		return regularExpression;
	}

	public void setRegularExpression(String regularExpression)
	{
		this.regularExpression = regularExpression;
	}

	public boolean isLongText()
	{
		return longText;
	}

	public void setLongText(boolean longText)
	{
		this.longText = longText;
	}

	@Override
	public MetadataRenderer getRenderer()
	{
		return new StringMetadataRenderer();
	}

	@Override
	public MetadataConverter<String> getConverter()
	{
		return new StringMetadataConverter();
	}

	@Override
	public MetadataValidator<String> getValidator()
	{
		return new StringMetadataValidator();
	}

	private final class StringMetadataValidator implements MetadataValidator<String>
	{
		public boolean validate(String metadataValue)
		{
			if (metadataValue == null || metadataValue.isEmpty())
				return !isRequired();
			if (minLength > 0 && metadataValue.length() < minLength)
				return false;
			if (maxLength > 0 && metadataValue.length() > maxLength)
				return false;
			if (regularExpression != null && !metadataValue.matches(regularExpression))
				return false;
			return true;
		}
	}

	protected final class StringMetadataConverter implements MetadataConverter<String>
	{
		public String toString(String metadataValue)
		{
			return (metadataValue != null && !metadataValue.isEmpty()) ? metadataValue : null;
		}

		public String fromString(String stringValue)
		{
			return (stringValue != null && !stringValue.isEmpty()) ? stringValue : null;
		}

		public Map<String, ?> toProperties(String metadataValue)
		{
			String stringValue = toString(metadataValue);
			return Collections.singletonMap(getUniqueName(), stringValue);
		}

		public String fromProperties(Map<String, ?> properties)
		{
			return fromString((String) properties.get(getUniqueName()));
		}

		public String fromHttpForm(Map<String, ?> parameters, String parameterSuffix)
		{
			return fromString((String) parameters.get(getUniqueName() + parameterSuffix));
		}
	}

	public final static class StringMetadataRenderer implements MetadataRenderer
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
			return "meta_edit_string";
		}

		public String getMetadataValueDisplayTemplate()
		{
			return "meta_display_string";
		}
	}
}
