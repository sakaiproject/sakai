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

/**
 * @author Colin Hebert
 */
public class EnumMetadataType extends MetadataType<String>
{

	private static final long serialVersionUID = 1L;
	private Collection<String> allowedValues;

	public Collection<String> getAllowedValues()
	{
		return allowedValues;
	}

	public void setAllowedValues(Collection<String> allowedValues)
	{
		this.allowedValues = allowedValues;
	}

	/**
	 * This gets the label that should used when displaying the values.
	 * @param value The value to lookup the label for.
	 * @return The label to show the user or the i18n key if it's a translated type.
	 */
	public String getValueLabel(String value) {
		if (isTranslated()) {
			return getName() + "." + value;
		} else {
			return value;
		}
	}

	@Override
	public MetadataRenderer getRenderer()
	{
		return new EnumMetadataRenderer();
	}

	@Override
	public MetadataConverter<String> getConverter()
	{
		return new EnumMetadataConverter();
	}

	@Override
	public MetadataValidator<String> getValidator()
	{
		return new EnumMetadataValidator();
	}


	private final class EnumMetadataValidator implements MetadataValidator<String>
	{
		public boolean validate(String metadataValue)
		{
			if (metadataValue == null)
				return !isRequired();
			if (allowedValues != null && !allowedValues.contains(metadataValue))
				return false;

			return true;
		}
	}

	private final class EnumMetadataRenderer implements MetadataRenderer
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
			return "meta_edit_enum";
		}

		public String getMetadataValueDisplayTemplate()
		{
			return "meta_display_string";
		}
	}

	protected final class EnumMetadataConverter implements MetadataConverter<String>
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
}
