/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool.components;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

public class IconWithToolTip extends Panel{
	
	public IconWithToolTip(String id, String iconClass, IModel textModel) {
		super(id);
			
		//link
		AjaxFallbackLink link = new AjaxFallbackLink("link") {
			public void onClick(AjaxRequestTarget target) {
				//nothing
			}
		};
		link.add(new AttributeModifier("title", textModel));
		link.add(new AttributeModifier("aria-label", new ResourceModel("accessibility.tooltip")));
		
		//icon
		Label icon = new Label("icon", new Model<String>(""));
		
		icon.add(new AttributeModifier("class", new Model(iconClass)));
		
		link.add(icon);
		
		add(link);
	}

}
