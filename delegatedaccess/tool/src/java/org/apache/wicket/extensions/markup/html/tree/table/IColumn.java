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
import org.apache.wicket.util.io.IClusterable;


/**
 * Interface that represents a column in {@link TreeTable}.
 * 
 * @author Matej Knopp
 */
@Deprecated
public interface IColumn extends IClusterable
{
	/**
	 * Returns a location of this column. Location specifies how is column aligned and what is it's
	 * size.
	 * <p>
	 * In case location of a column changes, it is necessary to call the <code>invalidateAll</code>
	 * methods on the {@link TreeTable} to prevent incorrect rendering.
	 * 
	 * @return The location of this column
	 */
	ColumnLocation getLocation();

	/**
	 * Returns the span for this cell. This method is called only for cells that are aligned in the
	 * middle.
	 * <p>
	 * The returned value implicates, over how many cells the cell in this column (in row determined
	 * by node) should span. This is analogical to colspan property of html element td.
	 * 
	 * @param node
	 *            The tree node
	 * @return The span of the column
	 */
	int getSpan(TreeNode node);

	/**
	 * Returns, whether the column is visible.
	 * <p>
	 * In case the visibility changes, it is necessary to call the <code>invalidateAll</code>
	 * methods on the {@link TreeTable} to prevent incorrect rendering.
	 * 
	 * @return Whether the column is visible
	 */
	boolean isVisible();

	/**
	 * This method is used to populate the cell for given node in case when
	 * {@link IColumn#newCell(TreeNode, int)} returned null.
	 * 
	 * @param parent
	 *            The parent to which the cell must be added. Can also be used to find the TreeTable
	 *            instance (using <code>parent.findParent(TreeTable.cass)</code>)
	 * @param id
	 *            The component id
	 * 
	 * @param node
	 *            TreeNode for the cell
	 * 
	 * @param level
	 *            Convenience parameter that indicates how deep the node is in hierarchy
	 * @return The populated cell component
	 */
	Component newCell(MarkupContainer parent, String id, TreeNode node, int level);

	/**
	 * Creates the {@link IRenderable} instance for given node. {@link IRenderable} can be used as
	 * lightweight alternative to regular Component for cells, that don't require user interaction
	 * (just display data).
	 * <p>
	 * If this method returns null, {@link IColumn#newCell(MarkupContainer, String, TreeNode, int)}
	 * is used to popuplate the cell.
	 * 
	 * @param node
	 *            TreeNode for the cell
	 * 
	 * @param level
	 *            Convenience parameter that indicates how deep the node is in hierarchy
	 * @return The cell renderer
	 */
	IRenderable newCell(TreeNode node, int level);

	/**
	 * Creates the header element for this column. In most situations this will be just a simple
	 * label showing column title.
	 * 
	 * @param parent
	 *            The parent component
	 * @param id
	 *            The component id
	 * @return The header component
	 */
	Component newHeader(MarkupContainer parent, String id);

	/**
	 * Sets the tree table this cell belongs to. This function is guaranteed to be called before any
	 * other function. The treeTable instance is fully initialized.
	 * 
	 * @param treeTable
	 *            The tree table
	 */
	void setTreeTable(TreeTable treeTable);
}
