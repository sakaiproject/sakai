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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


/**
 * Convenience class for building tree columns.
 * 
 * @author Matej Knopp
 */
@Deprecated
public abstract class AbstractColumn implements IColumn
{
	private static final long serialVersionUID = 1L;

	private final IModel<String> header;

	private final ColumnLocation location;

	private TreeTable treeTable = null;

	/**
	 * Creates the tree column.
	 * 
	 * @param location
	 *            Specifies how the column should be aligned and what his size should be
	 * 
	 * @param header
	 *            Header caption
	 */
	public AbstractColumn(final ColumnLocation location, final String header)
	{
		this(location, Model.of(header));
	}

	/**
	 * Creates the tree column.
	 *
	 * @param location
	 *            Specifies how the column should be aligned and what his size should be
	 *
	 * @param header
	 *            Header caption
	 */
	public AbstractColumn(final ColumnLocation location, final IModel<String> header)
	{
		this.location = location;
		this.header = header;
	}

	/**
	 * @see IColumn#getLocation()
	 */
	@Override
	public ColumnLocation getLocation()
	{
		return location;
	}

	/**
	 * @see IColumn#getSpan(TreeNode)
	 */
	@Override
	public int getSpan(final TreeNode node)
	{
		return 0;
	}

	/**
	 * @see IColumn#isVisible()
	 */
	@Override
	public boolean isVisible()
	{
		return true;
	}

	/**
	 * @see IColumn#newHeader(MarkupContainer, String)
	 */
	@Override
	public Component newHeader(final MarkupContainer parent, final String id)
	{
		return new Label(id, header);
	}

	/**
	 * @see IColumn#setTreeTable(TreeTable)
	 */
	@Override
	public void setTreeTable(final TreeTable treeTable)
	{
		if ((this.treeTable != null) && (this.treeTable != treeTable))
		{
			throw new IllegalStateException("You can't add single IColumn to multiple tree tables.");
		}
		this.treeTable = treeTable;
	}

	/**
	 * Returns the tree table that this columns belongs to. If you call this method from constructor
	 * it will return null, as the column is constructed before the tree is.
	 * 
	 * @return The tree table this column belongs to
	 */
	protected TreeTable getTreeTable()
	{
		return treeTable;
	}
}
