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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

/**
 * Simple tree component that provides node panel with link allowing user to select individual
 * nodes.
 * 
 * @author Matej Knopp
 */
@Deprecated
public class LinkTree extends LabelTree
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public LinkTree(String id)
	{
		super(id);
	}

	/**
	 * 
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 *            model that provides the {@link TreeModel}
	 */
	public LinkTree(String id, IModel<? extends TreeModel> model)
	{
		super(id, model);
	}

	/**
	 * 
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 *            Tree model
	 */
	public LinkTree(String id, TreeModel model)
	{
		super(id, new WicketTreeModel());
		setModelObject(model);
	}

	/**
	 * @see org.apache.wicket.extensions.markup.html.tree.BaseTree#newNodeComponent(String,
	 *      IModel)
	 */
	@Override
	protected Component newNodeComponent(String id, IModel<Object> model)
	{
		return new LinkIconPanel(id, model, LinkTree.this)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target)
			{
				super.onNodeLinkClicked(node, tree, target);
				LinkTree.this.onNodeLinkClicked(node, tree, target);
			}

			@Override
			protected Component newContentComponent(String componentId, BaseTree tree,
				IModel<?> model)
			{
				return new Label(componentId, getNodeTextModel(model));
			}
		};
	}

	/**
	 * Method invoked after the node has been selected / unselected.
	 * 
	 * @param node
	 * @param tree
	 * @param target
	 */
	protected void onNodeLinkClicked(Object node, BaseTree tree, AjaxRequestTarget target)
	{
	}
}
