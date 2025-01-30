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
package org.sakaiproject.scorm.ui.console.components;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.model.IModel;

import org.sakaiproject.wicket.markup.html.repeater.data.table.DecoratedPropertyColumn;

public class DecoratedDatePropertyColumn extends DecoratedPropertyColumn
{
	private static final long serialVersionUID = 1L;

	private SimpleDateFormat dateFormat;

	public DecoratedDatePropertyColumn(IModel displayModel, String sortProperty, String propertyExpression)
	{
		super(displayModel, sortProperty, propertyExpression);
		this.dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
	}

	@Override
	public Object convertObject(Object object)
	{
		if (object instanceof Date)
		{
			return dateFormat.format(object);
		}

		return object;
	}
}
