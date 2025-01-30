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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * Common functionality for column views
 */
public abstract class AbstractColumnsView extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	protected final List<IColumn> columns = new ArrayList<IColumn>();

	protected final List<Component> components = new ArrayList<Component>();

	protected final TreeNode node;

	protected final List<IRenderable> renderables = new ArrayList<IRenderable>();

	/**
	 * The position where to put the column.
	 */
	protected enum Position {
		/**
		 * Put the column at the beginning of the table (left-hand side)
		 */
		PREPEND,
		/**
		 * Put the column at the end of the table (right-hand side)
		 */
		APPEND;
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 * @param node
	 *            The tree node
	 */
	public AbstractColumnsView(String id, final TreeNode node)
	{
		super(id);

		this.node = node;
	}

	/**
	 * Adds a column to be rendered at the right side of the table.
	 * 
	 * @param column
	 *            The column to add
	 * @param component
	 *            The component
	 * @param renderable
	 *            The renderer
	 */
	public void addColumn(final IColumn column, Component component, final IRenderable renderable)
	{
		addColumn(column, component, renderable, Position.APPEND);
	}

	/**
	 * Adds a column to be rendered at the right side of the table.
	 * 
	 * @param column
	 *            The column to add
	 * @param component
	 *            The component
	 * @param renderable
	 *            The renderer
	 * @param position
	 *            where to put the column - at the right or left side
	 */
	public void addColumn(final IColumn column, Component component, final IRenderable renderable,
		Position position)
	{
		if (component != null)
		{
			add(component);
		}

		if (column.isVisible())
		{
			if (position == Position.APPEND)
			{
				columns.add(column);
				components.add(component);
				renderables.add(renderable);
			}
			else
			{
				columns.add(0, column);
				components.add(0, component);
				renderables.add(0, renderable);
			}
		}
	}

}
