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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * @author Colin Hebert
 */
public class ListMetadataType<T> extends MetadataType<List<T>>
{

	private static final long serialVersionUID = 1L;
	private final MetadataType<T> metadataType;
	private List<T> defaultValue;

	public ListMetadataType(MetadataType<T> metadataType)
	{
		this.metadataType = metadataType;
	}

	public MetadataType<T> getMetadataType()
	{
		return metadataType;
	}

	@Override
	public String getUniqueName()
	{
		return metadataType.getUniqueName();
	}

	@Override
	public void setUniqueName(String uniqueName)
	{
		metadataType.setUniqueName(uniqueName);
	}

	public String getName()
	{
		return metadataType.getName();
	}

	public void setName(String name)
	{
		metadataType.setName(name);
	}

	public String getDescription()
	{
		return metadataType.getDescription();
	}

	public void setDescription(String description)
	{
		metadataType.setDescription(description);
	}

	public List<T> getDefaultValue()
	{
		return defaultValue;
	}

	public void setDefaultValue(List<T> defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public boolean isRequired()
	{
		return metadataType.isRequired();
	}

	public void setRequired(boolean required)
	{
		metadataType.setRequired(required);
	}

	public List<String> getContentTypeApplicable()
	{
		return metadataType.getContentTypeApplicable();
	}

	public void setContentTypeApplicable(List<String> contentTypeApplicable)
	{
		metadataType.setContentTypeApplicable(contentTypeApplicable);
	}

	@Override
	public MetadataRenderer getRenderer()
	{
		return new ListMetadataRenderer();
	}

	@Override
	public MetadataConverter<List<T>> getConverter()
	{
		return new ListMetadataConverter();
	}

	@Override
	public MetadataValidator<List<T>> getValidator()
	{
		return new ListMetadataValidator();
	}

	//Convert lists to JSon because it's readable and it can be stored in a simple String
	private final class ListMetadataConverter implements MetadataConverter<List<T>>
	{
		MetadataConverter<T> metadataConverter = metadataType.getConverter();

		public String toString(List<T> metadataValues)
		{
			try
			{
				List<String> stringValues = new ArrayList<String>();

				if (metadataValues != null && !metadataValues.isEmpty())
				{
					for (T metadataValue : metadataValues)
					{
						String stringValue = metadataConverter.toString(metadataValue);
						if (stringValue != null)
							stringValues.add(stringValue);
					}
				}
				return new ObjectMapper().writeValueAsString(stringValues);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public List<T> fromString(String string)
		{
			List<T> metadataValues = new ArrayList<T>();
			try
			{

				if (string != null)
				{
					List<String> stringValues = new ObjectMapper().readValue(string, new TypeReference<List<String>>()
					{
					});

					for (String stringValue : stringValues)
					{
						T metadataValue = metadataConverter.fromString(stringValue);
						if (metadataValue != null)
							metadataValues.add(metadataValue);
					}
				}
				
			}
			catch (IOException e)
			{
				// Ignore any problems parsing them.
			}
			return metadataValues;
		}

		public Map<String, ?> toProperties(List<T> metadataValues)
		{
			List<String> stringValues = new ArrayList<String>();

			if (metadataValues != null && !metadataValues.isEmpty())
			{
				for (T metadataValue : metadataValues)
				{
					String stringValue = metadataConverter.toString(metadataValue);
					if (stringValue != null)
						stringValues.add(stringValue);
				}
			}

			return Collections.singletonMap(getUniqueName(), stringValues);
		}

		public List<T> fromProperties(Map<String, ?> properties)
		{
			List<T> metadataValues = new ArrayList<T>();
			Object value = properties.get(getUniqueName());
			if (value instanceof List<?>) {
				List<?> stringValues = (List<?>) value;
				if (stringValues != null)
				{
					for (Object stringValue : stringValues)
						if (stringValue instanceof String) {
							metadataValues.add(metadataConverter.fromString((String)stringValue));
						}
				}
			}
			return metadataValues;
		}

		public List<T> fromHttpForm(Map<String, ?> parameters, String parameterSuffix)
		{
			List<T> metadataValues = new ArrayList<T>();
			String[] stringValues;
			Object httpValue = parameters.get(getUniqueName() + parameterSuffix);
			if (httpValue == null)
				return metadataValues;
			else if (httpValue instanceof String)
				stringValues = new String[]{(String) httpValue};
			else
				stringValues = (String[]) httpValue;

			//Workaround to confuse the Metadata, making it think that it's getting a parameters map
			for (String stringValue : stringValues)
			{
				T metadataValue = metadataType.getConverter().fromHttpForm(Collections.<String, Object>singletonMap(getUniqueName(), stringValue), "");
				if (metadataValue != null)
					metadataValues.add(metadataValue);
			}
			return metadataValues;
		}
	}

	private final class ListMetadataValidator implements MetadataValidator<List<T>>
	{
		MetadataValidator<T> metadataValidator = metadataType.getValidator();

		public boolean validate(List<T> metadataValue)
		{
			for (T item : metadataValue)
			{
				if (!metadataValidator.validate(item))
					return false;
			}
			return true;
		}
	}

	private class ListMetadataRenderer implements MetadataRenderer
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
			return "meta_edit_list";
		}

		public String getMetadataValueDisplayTemplate()
		{
			return "meta_display_list";
		}
	}
}
