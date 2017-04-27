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
public class NumberMetadataType extends MetadataType<Number>
{

	private static final long serialVersionUID = 1L;
	private boolean acceptFloat;
	private boolean acceptNegative;
	private Number minimumValue;
	private Number maximumValue;
	private Number step;
	private Number width;

	public Number getMinimumValue() {
		return minimumValue;
	}

	public void setMinimumValue(Number minimumValue) {
		this.minimumValue = minimumValue;
	}

	public Number getMaximumValue() {
		return maximumValue;
	}

	public void setMaximumValue(Number maximumValue) {
		this.maximumValue = maximumValue;
	}

	public Number getStep() {
		return step;
	}

	public void setStep(Number step) {
		this.step = step;
	}

	public Number getWidth() {
		return width;
	}

	public void setWidth(Number width) {
		this.width = width;
	}

	@Override
	public MetadataRenderer getRenderer()
	{
		return new NumberMetadataRenderer();
		//return new StringMetadataType.StringMetadataRenderer();
	}

	@Override
	public MetadataConverter<Number> getConverter()
	{
		return new NumberMetadataConverter();
	}

	@Override
	public MetadataValidator<Number> getValidator()
	{
		return new NumberMetadataValidator();
	}

	private final class NumberMetadataValidator implements MetadataValidator<Number>
	{

		public boolean validate(Number metadataValue)
		{
			if (metadataValue == null)
				return !isRequired();
			if (!acceptFloat && metadataValue instanceof Float)
				return false;
			if (!acceptNegative && metadataValue.doubleValue() < 0)
				return false;
			if (minimumValue != null && minimumValue.doubleValue() > metadataValue.doubleValue())
				return false;
			if (maximumValue != null && maximumValue.doubleValue() < metadataValue.doubleValue())
				return false;

			return true;
		}
	}

	private final class NumberMetadataConverter implements MetadataConverter<Number>
	{

		public String toString(Number metadataValue)
		{
			return metadataValue != null ? metadataValue.toString() : null;
		}

		public Number fromString(String stringValue)
		{
			if (stringValue == null || stringValue.isEmpty())
				return null;
			try {
				if (acceptFloat)
					return Float.parseFloat(stringValue);
				else
					return Integer.parseInt(stringValue);
			} catch(NumberFormatException e) {
				return 0;
			}
		}

		public Map<String, ?> toProperties(Number metadataValue)
		{
			String stringValue = toString(metadataValue);
			return Collections.singletonMap(getUniqueName(), stringValue);
		}

		public Number fromProperties(Map<String, ?> properties)
		{
			return fromString((String) properties.get(getUniqueName()));
		}

		public Number fromHttpForm(Map<String, ?> parameters, String parameterSuffix)
		{
			return fromString((String) parameters.get(getUniqueName() + parameterSuffix));
		}
	}
	
	private final static class NumberMetadataRenderer implements MetadataRenderer
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
			return "meta_edit_number";
		}

		public String getMetadataValueDisplayTemplate()
		{
			return "meta_display_number";
		}
	}
}
