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


/**
 * Convenience class for building tree columns, i.e. columns that contain the actual tree.
 * 
 * @author Matej Knopp
 * @param <T>
 *            the type of the property that is rendered in this column
 */
public class PropertyTreeColumn<T> extends AbstractPropertyColumn<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates new column. Checks if the column is not aligned in middle. In case it is, throws an
	 * exception.
	 * 
	 * @param location
	 *            Specifies how the column should be aligned and what his size should be
	 * @param header
	 *            Header caption
	 * @param propertyExpression
	 *            Expression for property access
	 */
	public PropertyTreeColumn(final ColumnLocation location, final String header,
		final String propertyExpression)
	{
		this(location, Model.of(header), propertyExpression);
	}

	/**
	 * Creates new column. Checks if the column is not aligned in middle. In case it is, throws an
	 * exception.
	 *
	 * @param location
	 *            Specifies how the column should be aligned and what his size should be
	 * @param header
	 *            Header caption
	 * @param propertyExpression
	 *            Expression for property access
	 */
	public PropertyTreeColumn(final ColumnLocation location, final IModel<String> header,
	                          final String propertyExpression)
	{
		super(location, header, propertyExpression);
	}

	/**
	 * @see IColumn#newCell(MarkupContainer, String, TreeNode, int)
	 */
	@Override
	public Component newCell(final MarkupContainer parent, final String id, final TreeNode node,
		final int level)
	{
		return TreeTable.newTreeCell(parent, id, node, level, new TreeTable.IRenderNodeCallback()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String renderNode(final TreeNode node)
			{
				return PropertyTreeColumn.this.getNodeValue(node);
			}
		}, getTreeTable());
	}

	/**
	 * @see IColumn#newCell(TreeNode, int)
	 */
	@Override
	public IRenderable newCell(final TreeNode node, final int level)
	{
		return null;
	}
}
