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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.sakaiproject.content.metadata.model.MetadataConverter;
import org.sakaiproject.content.metadata.model.MetadataRenderer;
import org.sakaiproject.content.metadata.model.MetadataType;
import org.sakaiproject.content.metadata.model.MetadataValidator;

/**
 * This allows you to group metadata items into a set.
 * <p>
 * One gotcha with this type is that in the rendering with Velocity it converts all <code>null</code>
 * values into empty strings.
 * @author Colin Hebert
 */
public class GroupMetadataType extends MetadataType<Map<String, ?>>
{

	private static final long serialVersionUID = 1L;

	/**
	 * Content of the group
	 */
	private List<MetadataType<?>> metadataTypes;

	/**
	 * Automatically expand the group
	 */
	private boolean expanded;

	public List<MetadataType<?>> getMetadataTypes()
	{
		return metadataTypes;
	}

	public void setMetadataTypes(List<MetadataType<?>> metadataTypes)
	{
		this.metadataTypes = metadataTypes;
	}

	public boolean isExpanded()
	{
		return expanded;
	}

	public void setExpanded(boolean expanded)
	{
		this.expanded = expanded;
	}

	@Override
	public Map<String, ?> getDefaultValue()
	{
		Map<String, Object> defaultValue = new HashMap<String, Object>();
		for (MetadataType<?> metadataType : metadataTypes)
		{
			defaultValue.put(metadataType.getUniqueName(), metadataType.getDefaultValue());
		}
		return defaultValue;
	}

	@Override
	public MetadataRenderer getRenderer()
	{
		return new GroupMetadataRenderer();
	}

	@Override
	public MetadataConverter<Map<String, ?>> getConverter()
	{
		return new GroupMetadataConverter();
	}

	@Override
	public MetadataValidator<Map<String, ?>> getValidator()
	{
		return new GroupMetadataValidator();
	}

	private final class GroupMetadataConverter implements MetadataConverter<Map<String, ?>>
	{

		/**
		 * {@inheritDoc}
		 * <p/>
		 * INFO: For the suppress warning, see the content of the method
		 *
		 * @param metadataValues {@inheritDoc}
		 * @return {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public String toString(Map<String, ?> metadataValues)
		{
			try
			{
				Map<String, String> stringValues = new HashMap<String, String>(metadataTypes.size());
				if (metadataValues != null)
				{
					for (MetadataType metadataType : metadataTypes)
					{
						String id = metadataType.getUniqueName();
						/*
						 * There is no way to be sure of the metadata type of the entry, so a "cast" is required.
						 * In this case we can't cast to "?" so here goes some unchecked operations.
						 */
						String stringValue = metadataType.getConverter().toString(metadataValues.get(id)); //We do it live!
						if (stringValue != null)
							stringValues.put(id, stringValue);
					}
				}
				return new ObjectMapper().writeValueAsString(stringValues);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		public Map<String, ?> fromString(String stringValue)
		{
			try
			{
				Map<String, Object> metadataValues = new HashMap<String, Object>(metadataTypes.size());

				if (stringValue == null || stringValue.isEmpty())
					return metadataValues;

				Map<String, String> stringValues = new ObjectMapper().readValue(stringValue, new TypeReference<Map<String, String>>()
				{
				});

				for (MetadataType metadataType : metadataTypes)
				{
					String uniqueName = metadataType.getUniqueName();
					Object metadataValue = metadataType.getConverter().fromString(stringValues.get(uniqueName));
					if (metadataValue != null)
						metadataValues.put(uniqueName, metadataValue);
				}

				return metadataValues;
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		/**
		 * {@inheritDoc}
		 * <p/>
		 * INFO: For the suppress warning, see the content of the method
		 *
		 * @param metadataValues {@inheritDoc}
		 * @return {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public Map<String, ?> toProperties(Map<String, ?> metadataValues)
		{
			Map<String, ?> properties = new HashMap<String, Object>();
			for (MetadataType metadataType : metadataTypes)
			{
				/*
				 * There is no way to be sure of the metadata type of the entry, so a "cast" is required.
				 * In this case we can't cast to "?" so here goes some unchecked operations.
				 */
				properties.putAll(metadataType.getConverter().toProperties(metadataValues.get(metadataType.getUniqueName())));
			}
			return properties;
		}

		public Map<String, ?> fromProperties(Map<String, ?> properties)
		{
			Map<String, Object> metadataValues = new HashMap<String, Object>(metadataTypes.size());

			for (MetadataType metadataType : metadataTypes)
			{
				String uniqueName = metadataType.getUniqueName();
				Object metadataValue = metadataType.getConverter().fromProperties(properties);
				metadataValues.put(uniqueName, metadataValue);
			}

			return metadataValues;
		}

		public Map<String, ?> fromHttpForm(Map parameters, String parameterSuffix)
		{
			Map<String, Object> metadataValues = new HashMap<String, Object>(metadataTypes.size());
			for (MetadataType metadataType : metadataTypes)
			{
				String uniqueName = metadataType.getUniqueName();
				Object metadataValue = metadataType.getConverter().fromHttpForm(parameters, parameterSuffix);
				if (metadataValue != null)
					metadataValues.put(uniqueName, metadataValue);
			}
			return metadataValues;
		}
	}

	private final class GroupMetadataValidator implements MetadataValidator<Map<String, ?>>
	{
		/**
		 * {@inheritDoc}
		 * <p/>
		 * INFO: For the suppress warning, see the content of the method
		 *
		 * @param metadataValue {@inheritDoc}
		 * @return {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public boolean validate(Map<String, ?> metadataValue)
		{
			for (MetadataType metadataType : metadataTypes)
			{
				String uniqueName = metadataType.getUniqueName();
				/*
				 * There is no way to be sure of the metadata type of the entry, so a "cast" is required.
				 * In this case we can't cast to "?" so here goes some unchecked operations.
				 */
				if (metadataType.getValidator() != null && !metadataType.getValidator().validate(metadataValue.get(uniqueName)))
				{
					return false;
				}
			}

			return true;
		}
	}

	private class GroupMetadataRenderer implements MetadataRenderer
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
			return "meta_edit_group";
		}

		public String getMetadataValueDisplayTemplate()
		{
			return "meta_display_group";
		}
	}
}
