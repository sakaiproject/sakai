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

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

/**
 * This is a sub-class of property column that in the simplest case works exactly like property column.
 * However, if "action" objects are added to it then it will display them as links with one primary
 * cell item and several supplemental items displayed below, after the style of the Sakai 2.4 Assignments
 * data grid.
 * 
 * @author jrenfro
 *
 */
public class ActionColumn extends PropertyColumn
{
	private static final long serialVersionUID = 1L;

	private List<Action> actions = new LinkedList<>();

	public ActionColumn(IModel headerDisplayModel)
	{
		super(headerDisplayModel, null);
	}

	public ActionColumn(IModel headerDisplayModel, String propertyExpression)
	{
		super(headerDisplayModel, propertyExpression);
	}

	public ActionColumn(IModel headerDisplayModel, String sortProperty, String propertyExpression)
	{
		super(headerDisplayModel, sortProperty, propertyExpression);
	}

	@Override
	public void populateItem(Item item, String componentId, IModel model)
	{
		final Object modelObject = model.getObject();

		if (actions != null && actions.size() > 0)
		{
			item.add(new ActionPanel(componentId, actions, modelObject));
		}
		else
		{
			super.populateItem(item, componentId, model);
		}
	}

	/**
	 * This method should be called once for each "action". The first action added
	 * will appear as an h4 link, and any addition actions will appear below that 
	 * in smaller markup as determined by the ActionPanel and its associated css 
	 * 
	 * @param action
	 */
	public void addAction(Action action)
	{
		actions.add(action);
	}
}
