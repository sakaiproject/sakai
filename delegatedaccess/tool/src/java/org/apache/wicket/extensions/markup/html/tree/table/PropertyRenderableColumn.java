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

import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.string.Strings;


/**
 * Convenience class for creating non-interactive lightweight (IRenderable based) columns.
 * 
 * @author Matej Knopp
 * @param <T>
 *            the type of the property that is rendered in this column
 */
public class PropertyRenderableColumn<T> extends AbstractPropertyColumn<T>
{
	private static final long serialVersionUID = 1L;

	private boolean contentAsTooltip = false;

	private boolean escapeContent = true;

	/**
	 * Creates the column
	 * 
	 * @param location
	 *            Specifies how the column should be aligned and what his size should be
	 * @param header
	 *            Header caption
	 * @param propertyExpression
	 *            Expression for property access
	 */
	public PropertyRenderableColumn(final ColumnLocation location, final String header,
		final String propertyExpression)
	{
		this(location, Model.of(header), propertyExpression);
	}

	/**
	 * Creates the column
	 *
	 * @param location
	 *            Specifies how the column should be aligned and what his size should be
	 * @param header
	 *            Header caption
	 * @param propertyExpression
	 *            Expression for property access
	 */
	public PropertyRenderableColumn(final ColumnLocation location, final IModel<String> header,
	                                final String propertyExpression)
	{
		super(location, header, propertyExpression);
	}

	/**
	 * Returns whether the content should also be visible as tooltip of the cell.
	 * 
	 * @return whether the content should also be visible as tooltip
	 */
	public boolean isContentAsTooltip()
	{
		return contentAsTooltip;
	}

	/**
	 * Returns whether the special html characters of content will be escaped.
	 * 
	 * @return Whether html characters should be escaped
	 */
	public boolean isEscapeContent()
	{
		return escapeContent;
	}

	/**
	 * @see IColumn#newCell(MarkupContainer, String, TreeNode, int)
	 */
	@Override
	public Component newCell(final MarkupContainer parent, final String id, final TreeNode node,
		final int level)
	{
		return null;
	}

	/**
	 * @see IColumn#newCell(TreeNode, int)
	 */
	@Override
	public IRenderable newCell(final TreeNode node, final int level)
	{
		return new IRenderable()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void render(final TreeNode node, final Response response)
			{
				String content = getNodeValue(node);
				if (content == null)
				{
					content = "";
				}

				// escape if necessary
				if (isEscapeContent())
				{
					content = Strings.escapeMarkup(content).toString();
				}

				response.write("<span");
				if (isContentAsTooltip())
				{
					response.write(" title=\"" + content + "\"");
				}
				response.write(">");
				response.write(content);
				response.write("</span>");
			}
		};
	}

	/**
	 * Sets whether the content should also be visible as tooltip (html title attribute) of the
	 * cell.
	 * 
	 * @param contentAsTooltip
	 *            whether the content should also be visible as tooltip
	 */
	public void setContentAsTooltip(final boolean contentAsTooltip)
	{
		this.contentAsTooltip = contentAsTooltip;
	}

	/**
	 * Sets whether the special html characters of content should be escaped.
	 * 
	 * @param escapeContent
	 *            Whether to espcape html characters
	 */
	public void setEscapeContent(final boolean escapeContent)
	{
		this.escapeContent = escapeContent;
	}
}
