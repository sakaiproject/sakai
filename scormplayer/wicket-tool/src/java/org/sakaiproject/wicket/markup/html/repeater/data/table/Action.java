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

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.PropertyResolver;
import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;

/**
 * This is basically an abstraction of a wrapper for a specialized BookmarkablePageLink
 * component. It allows the {@link ActionColumn} to populate those components based on
 * properties of the bean object of each row. 
 * 
 * @author jrenfro
 *
 */
public class Action implements Serializable {
	private static final long serialVersionUID = 1L;
	
	protected IModel displayModel;
	protected Class<?> pageClass;
	protected String labelPropertyExpression;
	protected String[] paramPropertyExpressions;
	protected String popupWindowName;
	protected boolean isEnabled = true;
	protected boolean isVisible = true;
	
	// Constructors
	public Action() {
		
	}
	
	public Action(IModel displayModel) {
		this.displayModel = displayModel;
	}
	
	public Action(String labelPropertyExpression) {
		this.labelPropertyExpression = labelPropertyExpression;
	}
	
	public Action(String labelPropertyExpression, String[] paramPropertyExpressions) {
		this.labelPropertyExpression = labelPropertyExpression;
		this.paramPropertyExpressions = paramPropertyExpressions;
	}
	
	public Action(IModel displayModel, String[] paramPropertyExpressions) {
		this.displayModel = displayModel;
		this.paramPropertyExpressions = paramPropertyExpressions;
	}
	
	public Action(String labelPropertyExpression, Class<?> pageClass, String[] paramPropertyExpressions) {
		this(labelPropertyExpression, pageClass, paramPropertyExpressions, null);
	}
		
	public Action(String labelPropertyExpression, Class<?> pageClass, String[] paramPropertyExpressions, String popupWindowName) {
		this.labelPropertyExpression = labelPropertyExpression;
		this.pageClass = pageClass;
		this.paramPropertyExpressions = paramPropertyExpressions;
		this.popupWindowName = popupWindowName;
	}
	
	public Action(IModel displayModel, Class<?> pageClass, String[] paramPropertyExpressions) {
		this(displayModel, pageClass, paramPropertyExpressions, null);
	}
	
	public Action(IModel displayModel, Class<?> pageClass, String[] paramPropertyExpressions, String popupWindowName) {
		this.displayModel = displayModel;
		this.pageClass = pageClass;
		this.paramPropertyExpressions = paramPropertyExpressions;
		this.popupWindowName = popupWindowName;
	}
	
	// Public methods
	
	public Component newLink(String id, Object bean) {
		IModel labelModel = null;
		if (displayModel != null) {
			labelModel = displayModel;
 		} else {
 			String labelValue = String.valueOf(PropertyResolver.getValue(labelPropertyExpression, bean));
 			labelModel = new Model(labelValue);
 		}
		
		PageParameters params = buildPageParameters(paramPropertyExpressions, bean);
		Link link = new BookmarkablePageLabeledLink(id, labelModel, pageClass, params);

		if (popupWindowName != null) {
			PopupSettings popupSettings = new PopupSettings(PageMap.forName(popupWindowName), PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS);
			
			popupSettings.setWindowName(popupWindowName);
			
 			link.setPopupSettings(popupSettings);
		}
		
		link.setEnabled(isEnabled(bean));
		link.setVisible(isVisible(bean));
			
 		return link;
	}
	
	// Helper methods
	
	protected PageParameters buildPageParameters(String[] propertyExpressions, Object object) {
		PageParameters params = new PageParameters();
		
		for (int i=0;i<propertyExpressions.length;i++) {
			String paramValue = String.valueOf(PropertyResolver.getValue(propertyExpressions[i], object));	
			params.add(propertyExpressions[i], paramValue);
		}
		
		return params;
	}
	
	// Accessors
	
	public IModel getDisplayModel() {
		return displayModel;
	}
	
	public void setDisplayModel(IModel displayModel) {
		this.displayModel = displayModel;
	}
	
	public Class<?> getPageClass() {
		return pageClass;
	}
	
	public void setPageClass(Class<?> pageClass) {
		this.pageClass = pageClass;
	}

	public String[] getParamPropertyExpressions() {
		return paramPropertyExpressions;
	}

	public void setParamPropertyExpressions(String[] paramPropertyExpressions) {
		this.paramPropertyExpressions = paramPropertyExpressions;
	}

	public String getPopupWindowName() {
		return popupWindowName;
	}

	public void setPopupWindowName(String popupWindowName) {
		this.popupWindowName = popupWindowName;
	}
	
	public boolean isEnabled(Object bean) {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public boolean isVisible(Object bean) {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}
	
}
