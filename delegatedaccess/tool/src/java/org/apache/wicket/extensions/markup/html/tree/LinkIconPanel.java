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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

/**
 * Simple panel that contains a link with icon and a link with a label.
 * 
 * @author Matej Knopp
 */
public class LinkIconPanel extends LabelIconPanel
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs the panel.
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            model that is used to access the TreeNode
	 * @param tree
	 */
	public LinkIconPanel(String id, IModel<Object> model, BaseTree tree)
	{
		super(id, model, tree);
	}

	/**
	 * @see org.apache.wicket.extensions.markup.html.tree.LabelIconPanel#addComponents(IModel,
	 *      org.apache.wicket.extensions.markup.html.tree.BaseTree)
	 */
	@Override
	protected void addComponents(final IModel<Object> model, final BaseTree tree)
	{
		BaseTree.ILinkCallback callback = new BaseTree.ILinkCallback()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				onNodeLinkClicked(model.getObject(), tree, target);
			}
		};

		MarkupContainer link = tree.newLink("iconLink", callback);
		add(link);
		link.add(newImageComponent("icon", tree, model));

		link = tree.newLink("contentLink", callback);
		add(link);
		link.add(newContentComponent("content", tree, model));
	}

	/**
	 * Handler invoked when the link is clicked. By default makes the node selected
	 * 
	 * @param node
	 * @param tree
	 * @param target
	 */
	protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target)
	{
		tree.getTreeState().selectNode(node, !tree.getTreeState().isNodeSelected(node));

		if (target != null)
		{
			tree.updateTree(target);
		}
	}
}
