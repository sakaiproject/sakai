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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.PropertyResolver;

public class ExternalAction extends Action {
	
	private static final long serialVersionUID = 1L;
	
	private String urlPropertyExpression;
	private String targetAttribute;
	
	public ExternalAction(String labelPropertyExpression, String urlPropertyExpression) {
		this(labelPropertyExpression, urlPropertyExpression, null);
	}
	
	public ExternalAction(String labelPropertyExpression, String urlPropertyExpression, String targetAttribute) {
		super(labelPropertyExpression);
		this.urlPropertyExpression = urlPropertyExpression;
		this.targetAttribute = targetAttribute;
	}
	
	public Component newLink(String id, Object bean) {
		IModel labelModel = null;
		if (displayModel != null) {
			labelModel = displayModel;
 		} else {
 			String labelValue = String.valueOf(PropertyResolver.getValue(labelPropertyExpression, bean));
 			labelModel = new Model(labelValue);
 		}

		String urlValue = String.valueOf(PropertyResolver.getValue(urlPropertyExpression, bean));
		
		ExternalLink link = new ExternalLink(id, new Model(urlValue), labelModel);

		if (popupWindowName != null)
 			link.setPopupSettings(new PopupSettings(PopupSettings.RESIZABLE | 
					PopupSettings.SCROLLBARS).setWindowName(popupWindowName));
		
		link.setEnabled(isEnabled(bean));
		
		if (targetAttribute == null)
			link.add(new AttributeModifier("target", true, new Model(targetAttribute)));
		
 		return link;
	}
	
	
	
}
