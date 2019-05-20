/**
 * Copyright (c) 2006-2019 The Apereo Foundation
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
package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import static org.sakaiproject.sitestats.tool.wicket.components.SakaiResponsiveAbstractColumn.DATA_LABEL_ATTR;

/**
 * A responsive PropertyColumn (adds CardTable support)
 * @author plukasew
 */
public class SakaiResponsivePropertyColumn<T,S> extends PropertyColumn<T,S>
{
	/**
	 * A PropertyColumn with CardTable support
	 * @param displayModel display model
	 * @param sortProperty sort property
	 * @param propertyExpression wicket property expression used by PropertyModel
	 */
	public SakaiResponsivePropertyColumn(IModel<String> displayModel, S sortProperty, String propertyExpression)
	{
		super(displayModel, sortProperty, propertyExpression);
	}

	/**
	 * A PropertyColumn with CardTable support
	 * @param displayModel display model
	 * @param propertyExpression wicket property expression used by PropertyModel
	 */
	public SakaiResponsivePropertyColumn(IModel<String> displayModel, String propertyExpression)
	{
		super(displayModel, propertyExpression);
	}

	@Override
	public void populateItem(Item<ICellPopulator<T>> item, String componentId, IModel<T> rowModel)
	{
		super.populateItem(item, componentId, rowModel);
		item.add(AttributeAppender.append(DATA_LABEL_ATTR, getDisplayModel()));
	}
}
