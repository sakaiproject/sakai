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
import org.apache.wicket.model.IModel;

/**
 * Simple tree component that uses label to render tree node.
 * 
 * @author Matej Knopp
 */
@Deprecated
public class LabelTree extends BaseTree
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public LabelTree(String id)
	{
		super(id);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 *            model that provides the {@link TreeModel}
	 */
	public LabelTree(String id, IModel<? extends TreeModel> model)
	{
		super(id, model);
	}

	/**
	 * Construct.
	 * 
	 * @param id
	 * @param model
	 *            Tree model
	 */
	public LabelTree(String id, TreeModel model)
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
		return new LabelIconPanel(id, model, this)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Component newContentComponent(String componentId, BaseTree tree,
				IModel<?> model)
			{
				return super.newContentComponent(componentId, tree, getNodeTextModel(model));
			}
		};
	}

	/**
	 * Provides the model that will be used to feed the node text.
	 * 
	 * Example implementation: <code>return new PropertyModel(nodeModel, "object.name");</code>
	 * which will translate to <code>YourTreeNodeSubclass.getObject().getName();</code>
	 * 
	 * NOTE: remember that the nodeModel represents the TreeNode object, not the model object inside
	 * it
	 * 
	 * NOTE: this method is called from the default implementation of
	 * {@link #newNodeComponent(String, IModel)}, so if it is subclassed this method may no longer
	 * be called unless the subclassing code maintains the callback explicitly
	 * 
	 * @param nodeModel
	 *            model representing the current tree node
	 * @return model used for text
	 */
	protected IModel<?> getNodeTextModel(IModel<?> nodeModel)
	{
		return nodeModel;
	}
}
