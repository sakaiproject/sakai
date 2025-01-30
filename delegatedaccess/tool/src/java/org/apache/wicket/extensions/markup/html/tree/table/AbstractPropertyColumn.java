/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.extensions.markup.html.tree.table;

import java.util.Locale;

import javax.swing.tree.TreeNode;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;


/**
 * Convenience class for building tree columns.
 * 
 * @author Matej Knopp
 * @param <T>
 *            the type of the property that is rendered in this column
 */
public abstract class AbstractPropertyColumn<T> extends AbstractColumn
{
	private static final long serialVersionUID = 1L;

	private IConverter<T> converter;

	private Locale locale;

	private final String propertyExpression;

	/**
	 * Creates the tree column.
	 * 
	 * @param location
	 *            Specifies how the column should be aligned and what his size should be
	 * @param header
	 *            Header caption
	 * @param propertyExpression
	 *            Expression for property access
	 */
	public AbstractPropertyColumn(final ColumnLocation location, final String header,
		final String propertyExpression)
	{
		this(location, Model.of(header), propertyExpression);
	}

	/**
	 * Creates the tree column.
	 *
	 * @param location
	 *            Specifies how the column should be aligned and what his size should be
	 * @param header
	 *            Header caption
	 * @param propertyExpression
	 *            Expression for property access
	 */
	public AbstractPropertyColumn(final ColumnLocation location, final IModel<String> header,
	                              final String propertyExpression)
	{
		super(location, header);

		this.propertyExpression = propertyExpression;
	}

	/**
	 * Returns the converter or null if no converter is specified.
	 * 
	 * @return The converter or null
	 */
	public IConverter<T> getConverter()
	{
		return converter;
	}

	/**
	 * Returns the locale or null if no locale is specified.
	 * 
	 * @return The locale or null
	 */
	public Locale getLocale()
	{
		return locale;
	}

	/**
	 * By default the property is converted to string using <code>toString</code> method. If you
	 * want to alter this behavior, you can specify a custom converter.
	 * 
	 * @param converter
	 *            any converter
	 */
	public void setConverter(final IConverter<T> converter)
	{
		this.converter = converter;
	}

	/**
	 * Sets the locale to be used as parameter for custom converter (if one is specified). If no
	 * locale is set, session locale is used.
	 * 
	 * @param locale
	 *            Any locale
	 */
	public void setLocale(final Locale locale)
	{
		this.locale = locale;
	}

	/**
	 * Returns the property expression.
	 * 
	 * @return The property expression
	 */
	protected String getPropertyExpression()
	{
		return propertyExpression;
	}

	/**
	 * Returns the string representation of the node.
	 * 
	 * @param node
	 *            The node
	 * @return The string representation of the node
	 */
	public String getNodeValue(final TreeNode node)
	{
		Object result = PropertyResolver.getValue(propertyExpression, node);

		@SuppressWarnings("rawtypes")
		IConverter converter = getConverter();
		if (converter == null && result != null)
		{
			converter = Application.get().getConverterLocator().getConverter(result.getClass());
		}

		if (converter != null)
		{
			Locale locale = this.locale;
			if (locale == null)
			{
				locale = Session.get().getLocale();
			}

			@SuppressWarnings("unchecked")
			String string = converter.convertToString(result, locale);
			return string;
		}
		else
		{
			return result != null ? result.toString() : null;
		}
	}
}
