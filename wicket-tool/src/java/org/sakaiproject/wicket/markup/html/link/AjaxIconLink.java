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
package org.sakaiproject.wicket.markup.html.link;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class AjaxIconLink extends Panel {

	private static final long serialVersionUID = 1L;

	
	public AjaxIconLink(String id, ResourceReference iconReference) {
		super(id);
		
		add(newLink("link", iconReference));
	}
	
	public abstract void onClick(AjaxRequestTarget target);
	
	private AjaxLink newLink(String baseId, final ResourceReference icon) {
		AjaxLink actionLink = new AjaxLink(baseId) {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				AjaxIconLink.this.onClick(target);
			}
			
		};

		// Add the passed icon 
		if (icon != null) {
			String iconId = new StringBuilder().append(baseId).append("Icon").toString();
			Image iconImage = new Image(iconId)
			{
				private static final long serialVersionUID = 1L;
	
				protected ResourceReference getImageResourceReference()
				{
					return icon;
				}
			};
				
			actionLink.add(iconImage);
		}
		
		return actionLink;
	}
	
}
