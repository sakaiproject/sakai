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
package org.apache.wicket.extensions.markup.html.tree;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;


/**
 * A complete tree implementation where three item consists of junction link, icon and label.
 * 
 * @author Matej Knopp
 */
@Deprecated
public class Tree extends DefaultAbstractTree
{
	private static final long serialVersionUID = 1L;

	/**
	 * Tree constructor.
	 * 
	 * @param id
	 *            The component id
	 */
	public Tree(final String id)
	{
		super(id);
	}

	/**
	 * Tree constructor.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The tree model
	 */
	public Tree(final String id, final IModel<? extends TreeModel> model)
	{
		super(id, model);
	}

	/**
	 * Tree constructor.
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The tree model
	 */
	public Tree(final String id, final TreeModel model)
	{
		super(id, model);
	}

	/**
	 * Populates the tree item. It creates all necesary components for the tree to work properly.
	 * 
	 * @param item
	 * @param level
	 */
	@Override
	protected void populateTreeItem(final WebMarkupContainer item, final int level)
	{
		final TreeNode node = (TreeNode)item.getDefaultModelObject();

		item.add(newIndentation(item, "indent", (TreeNode)item.getDefaultModelObject(), level));

		item.add(newJunctionLink(item, "link", "image", node));

		MarkupContainer nodeLink = newNodeLink(item, "nodeLink", node);
		item.add(nodeLink);

		nodeLink.add(newNodeIcon(nodeLink, "icon", node));

		nodeLink.add(new Label("label", new IModel<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject()
			{
				return renderNode(node);
			}
		}));

		// do distinguish between selected and unselected rows we add an
		// behavior
		// that modifies row css class.
		item.add(new Behavior()
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see org.apache.wicket.behavior.Behavior#onComponentTag(Component, ComponentTag)
			 */
			@Override
			public void onComponentTag(final Component component, final ComponentTag tag)
			{
				super.onComponentTag(component, tag);
				if (getTreeState().isNodeSelected(node))
				{
					tag.put("class", "row-selected");
				}
				else
				{
					tag.put("class", "row");
				}
			}
		});
	}

	/**
	 * This method is called for every node to get it's string representation.
	 * 
	 * @param node
	 *            The tree node to get the string representation of
	 * @return The string representation
	 */
	protected String renderNode(final TreeNode node)
	{
		return node.toString();
	}
}
