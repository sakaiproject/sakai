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

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import static org.sakaiproject.scorm.api.ScormConstants.*;
import org.sakaiproject.scorm.model.api.LearnerExperience;

public class AccessStatusColumn extends AbstractColumn
{
	private static final long serialVersionUID = 1L;

	public AccessStatusColumn(IModel displayModel, String sortProperty)
	{
		super(displayModel, sortProperty);
	}

	@Override
	public void populateItem(Item item, String componentId, IModel model)
	{
		item.add(new Label(componentId, createLabelModel(model)));
	}

	protected IModel createLabelModel(IModel embeddedModel)
	{
		String resourceId = "access.status.not.accessed";
		Object target = embeddedModel.getObject();

		if (target instanceof LearnerExperience)
		{
			LearnerExperience experience = (LearnerExperience)target;

			switch (experience.getStatus())
			{
				case NOT_ACCESSED:
					resourceId = "access.status.not.accessed";
					break;
				case INCOMPLETE:
					resourceId = "access.status.incomplete";
					break;
				case COMPLETED:
					resourceId = "access.status.completed";
					break;
				case GRADED:
					resourceId = "access.status.graded";
					break;
			};
		}

		return new ResourceModel(resourceId);
	}
}