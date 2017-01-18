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

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.sakaiproject.wicket.markup.html.link.AjaxIconLink;

public abstract class AjaxImageLinkColumn extends AbstractColumn {

	private static final long serialVersionUID = 1L;
	
	public AjaxImageLinkColumn(IModel displayModel) {
		super(displayModel);
	}

	public abstract void onClick(Object bean, AjaxRequestTarget target);
	
	public abstract ResourceReference getIconReference(Object bean);
	
	public void populateItem(Item cellItem, String componentId, IModel model) {
		final Object bean = model.getObject();
	 		
		ResourceReference iconReference = getIconReference(bean);
		
		cellItem.add(new AjaxIconLink(componentId, iconReference) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				AjaxImageLinkColumn.this.onClick(bean, target);
			}
			
		});
	}

}
