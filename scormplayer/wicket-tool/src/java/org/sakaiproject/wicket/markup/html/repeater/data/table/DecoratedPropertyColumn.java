/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.wicket.markup.html.repeater.data.table;

import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

public abstract class DecoratedPropertyColumn extends PropertyColumn
{
	private static final long serialVersionUID = 1L;

	public DecoratedPropertyColumn(IModel displayModel, String propertyExpression)
	{
		super(displayModel, propertyExpression);
	}

	public DecoratedPropertyColumn(IModel displayModel, String sortProperty, String propertyExpression)
	{
		super(displayModel, sortProperty, propertyExpression);
	}

	protected IModel createLabelModel(IModel embeddedModel)
	{
		return new PropertyModel(embeddedModel, getPropertyExpression())
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Object getObject()
			{
				Object object = super.getObject();
				return convertObject(object);
			}
			
		};
	}

	public abstract Object convertObject(Object object);
}
