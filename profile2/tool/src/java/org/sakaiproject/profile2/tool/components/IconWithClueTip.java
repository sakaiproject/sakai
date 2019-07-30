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
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

/*
 * 
 * Eventually this will be done via setters on this object
 * 
 * 
 */

public class IconWithClueTip extends Panel{
	
	public IconWithClueTip(String id, String iconUrl, IModel textModel) {
		super(id);
			
		//link
		AjaxFallbackLink link = new AjaxFallbackLink("link") {
			public void onClick(AjaxRequestTarget target) {
				//nothing
			}
		};
		link.add(new AttributeModifier("title", true, textModel));
		
		//image
		ContextImage image = new ContextImage("icon",new Model(iconUrl));
		image.add(new AttributeModifier("alt", true, new ResourceModel("accessibility.tooltip")));
		link.add(image);
		
		add(link);
	
	}
	
	
}
