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

import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;

import org.sakaiproject.wicket.markup.html.link.IconLink;

public class ImageLinkColumn extends AbstractColumn
{
	private static final long serialVersionUID = 1L;

	private Class<?> pageClass;
	private String[] paramPropertyExpressions;
	private ResourceReference iconReference;
	private String popupWindowName;
	private String iconProperty = null;

	/**
	 * Convenience constructor for non-sortable column. Alternatively, you could call one of the other constructors and pass 'null' for the sortProperty param.
	 */
	public ImageLinkColumn(IModel displayModel, Class<?> pageClass, String[] paramPropertyExpressions, PackageResourceReference iconReference)
	{
		this(displayModel, pageClass, paramPropertyExpressions, iconReference, null, null);
	}

	public ImageLinkColumn(IModel displayModel, Class<?> pageClass, String[] paramPropertyExpressions, PackageResourceReference iconReference, String sortProperty)
	{
		this(displayModel, pageClass, paramPropertyExpressions, iconReference, null, sortProperty);
	}

	public ImageLinkColumn(IModel displayModel, Class<?> pageClass, String[] paramPropertyExpressions, String iconProperty, String sortProperty)
	{
		this(displayModel, pageClass, paramPropertyExpressions, null, null, sortProperty);
		this.iconProperty = iconProperty;
	}

	public ImageLinkColumn(IModel displayModel, Class<?> pageClass, String[] paramPropertyExpressions, PackageResourceReference iconReference, String popupWindowName, String sortProperty)
	{
		super(displayModel, sortProperty);
		this.pageClass = pageClass;
		this.paramPropertyExpressions = paramPropertyExpressions;
		this.iconReference = iconReference;
		this.popupWindowName = popupWindowName;
	}

	@Override
	public void populateItem(Item cellItem, String componentId, IModel model)
	{
		Object bean = model.getObject();

		final PageParameters params = buildPageParameters(paramPropertyExpressions, bean);

		if (iconProperty != null)
		{
			String iconPropertyValue = String.valueOf(PropertyResolver.getValue(iconProperty, bean));
			iconReference = getIconPropertyReference(iconPropertyValue);
		}

		cellItem.add(new IconLink("cell", pageClass, params, iconReference, popupWindowName));
	}

	protected ResourceReference getIconPropertyReference(String iconPropertyValue)
	{
		return iconReference;
	}

	private PageParameters buildPageParameters(String[] propertyExpressions, Object object)
	{
		PageParameters params = new PageParameters();

		if (propertyExpressions != null)
		{
			for( String propertyExpression : propertyExpressions )
			{
				String paramValue = String.valueOf( PropertyResolver.getValue( propertyExpression, object ) );
				params.add( propertyExpression, paramValue );
			}
		}

		return params;
	}
}
