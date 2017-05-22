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
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.sakaiproject.profile2.util.ProfileConstants;

public class CloseButton extends Panel{
	
	
	public CloseButton(String id, final Component parent) {
		super(id);
			
		//container
		WebMarkupContainer closeButton = new WebMarkupContainer("closeButton");
		closeButton.setOutputMarkupId(true);
		
		//image
		ContextImage image = new ContextImage("img",new Model(ProfileConstants.CLOSE_IMAGE));
		image.add(new AttributeModifier("alt",""));
		
		AjaxFallbackLink link = new AjaxFallbackLink("link") {
			public void onClick(AjaxRequestTarget target) {
				if(target != null) {
					
					target.appendJavaScript("$('#" + parent.getMarkupId() + "').slideUp();");
					target.appendJavaScript("setMainFrameHeight(window.name);");

					//do we also need to remove the component as well?
					
				}
			}
						
		};
		
		
		link.add(image);
		
		closeButton.add(link);
		
		add(closeButton);
		
		
	
		
	
		
		//extend this to allow a behaviour to be set so that when its clicked, something happens
	}
	
	
}
