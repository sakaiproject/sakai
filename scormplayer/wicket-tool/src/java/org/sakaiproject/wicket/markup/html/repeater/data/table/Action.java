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

import lombok.Getter;
import lombok.Setter;

import org.apache.wicket.Component;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.sakaiproject.wicket.markup.html.link.BookmarkablePageLabeledLink;

/**
 * This is basically an abstraction of a wrapper for a specialized BookmarkablePageLink
 * component. It allows the {@link ActionColumn} to populate those components based on
 * properties of the bean object of each row. 
 * 
 * @author jrenfro
 *
 */
public class Action implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Getter @Setter protected IModel displayModel;
	@Getter @Setter protected Class<?> pageClass;
	@Getter @Setter protected String[] paramPropertyExpressions;
	@Getter @Setter protected String popupWindowName;
	@Getter @Setter protected boolean isEnabled = true;
	@Getter @Setter protected boolean isVisible = true;

	protected String labelPropertyExpression;

	// Constructors
	public Action() {}

	public Action(IModel displayModel)
	{
		this.displayModel = displayModel;
	}

	public Action(String labelPropertyExpression)
	{
		this.labelPropertyExpression = labelPropertyExpression;
	}

	public Action(String labelPropertyExpression, String[] paramPropertyExpressions)
	{
		this.labelPropertyExpression = labelPropertyExpression;
		this.paramPropertyExpressions = paramPropertyExpressions;
	}

	public Action(IModel displayModel, String[] paramPropertyExpressions)
	{
		this.displayModel = displayModel;
		this.paramPropertyExpressions = paramPropertyExpressions;
	}

	public Action(String labelPropertyExpression, Class<?> pageClass, String[] paramPropertyExpressions)
	{
		this(labelPropertyExpression, pageClass, paramPropertyExpressions, null);
	}

	public Action(String labelPropertyExpression, Class<?> pageClass, String[] paramPropertyExpressions, String popupWindowName)
	{
		this.labelPropertyExpression = labelPropertyExpression;
		this.pageClass = pageClass;
		this.paramPropertyExpressions = paramPropertyExpressions;
		this.popupWindowName = popupWindowName;
	}

	public Action(IModel displayModel, Class<?> pageClass, String[] paramPropertyExpressions)
	{
		this(displayModel, pageClass, paramPropertyExpressions, null);
	}

	public Action(IModel displayModel, Class<?> pageClass, String[] paramPropertyExpressions, String popupWindowName)
	{
		this.displayModel = displayModel;
		this.pageClass = pageClass;
		this.paramPropertyExpressions = paramPropertyExpressions;
		this.popupWindowName = popupWindowName;
	}

	// Public methods

	public Component newLink(String id, Object bean)
	{
		IModel labelModel = null;
		if (displayModel != null)
		{
			labelModel = displayModel;
		}
		else
		{
 			String labelValue = String.valueOf(PropertyResolver.getValue(labelPropertyExpression, bean));
 			labelModel = new Model(labelValue);
 		}

		PageParameters params = buildPageParameters(paramPropertyExpressions, bean);
		Link link = new BookmarkablePageLabeledLink(id, labelModel, pageClass, params);

		if (popupWindowName != null)
		{
			PopupSettings popupSettings = new PopupSettings(popupWindowName, PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS);
			
			popupSettings.setWindowName(popupWindowName);
			
			link.setPopupSettings(popupSettings);
		}

		link.setEnabled(isEnabled());
		link.setVisible(isVisible());

		return link;
	}

	// Helper methods

	protected PageParameters buildPageParameters(String[] propertyExpressions, Object object)
	{
		PageParameters params = new PageParameters();

		for( String propertyExpression : propertyExpressions )
		{
			String paramValue = String.valueOf( PropertyResolver.getValue( propertyExpression, object ) );
			params.add( propertyExpression, paramValue );
		}

		return params;
	}
}
