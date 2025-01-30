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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.list.AbstractItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;


/**
 * This class encapsulates the logic for displaying and (partial) updating the tree. Actual
 * presentation is out of scope of this class. User should derive they own tree (if needed) from
 * {@link BaseTree} (recommended).
 * 
 * @author Matej Knopp
 */
@Deprecated
public abstract class AbstractTree extends Panel
	implements
		ITreeStateListener,
		TreeModelListener,
		AjaxRequestTarget.ITargetRespondListener
{
	private static final long serialVersionUID = 1L;

	/**
	 * Interface for visiting individual tree items.
	 */
	private static interface IItemCallback
	{
		/**
		 * Visits the tree item.
		 * 
		 * @param item
		 *            the item to visit
		 */
		void visitItem(TreeItem item);
	}

	/**
	 * This class represents one row in rendered tree (TreeNode). Only TreeNodes that are visible
	 * (all their parent are expanded) have TreeItem created for them.
	 */
	private final class TreeItem extends AbstractItem
	{
		/**
		 * whether this tree item should also render it's children to response. this is set if we
		 * need the whole subtree rendered as one component in ajax response, so that we can replace
		 * it in one step (replacing individual rows is very slow in javascript, therefore we
		 * replace the whole subtree)
		 */
		private final static int FLAG_RENDER_CHILDREN = FLAG_RESERVED8;

		private static final long serialVersionUID = 1L;

		/**
		 * tree item children - we need this to traverse items in correct order when rendering
		 */
		private List<TreeItem> children = null;

		/** tree item level - how deep is this item in tree */
		private final int level;

		private final TreeItem parent;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            The component id
		 * @param node
		 *            tree node
		 * @param level
		 *            current level
		 * @param parent
		 */
		public TreeItem(TreeItem parent, String id, final Object node, int level)
		{
			super(id, new Model<Serializable>((Serializable)node));

			this.parent = parent;

			nodeToItemMap.put(node, this);
			this.level = level;
			setOutputMarkupId(true);

			// if this isn't a root item in rootless mode
			if (level != -1)
			{
				populateTreeItem(this, level);
			}
		}

		public TreeItem getParentItem()
		{
			return parent;
		}

		/**
		 * @return The children
		 */
		public List<TreeItem> getChildren()
		{
			return children;
		}

		/**
		 * @return The current level
		 */
		public int getLevel()
		{
			return level;
		}

		/**
		 * @see Component#getMarkupId()
		 */
		@Override
		public String getMarkupId()
		{
			// this is overridden to produce id that begins with id of tree
			// if the tree has set (shorter) id in markup, we can use it to
			// shorten the id of individual TreeItems
			return AbstractTree.this.getMarkupId() + "_" + getId();
		}

		/**
		 * Sets the children.
		 * 
		 * @param children
		 *            The children
		 */
		public void setChildren(List<TreeItem> children)
		{
			this.children = children;
		}

		/**
		 * Whether to render children.
		 * 
		 * @return whether to render children
		 */
		protected final boolean isRenderChildren()
		{
			return getFlag(FLAG_RENDER_CHILDREN);
		}

		/**
		 * Whether the TreeItem has any child TreeItems
		 * 
		 * @return true if there are one or more child TreeItems; false otherwise
		 */
		public boolean hasChildTreeItems()
		{
			return children != null && !children.isEmpty();
		}

		/**
		 * @see MarkupContainer#onRender()
		 */
		@Override
		protected void onRender()
		{
			// is this root and tree is in rootless mode?
			if (this == rootItem && isRootLess() == true)
			{
				// yes, write empty div with id
				// this is necessary for createElement js to work correctly
				String tagName = ((ComponentTag)getMarkup().get(0)).getName();
				Response response = getResponse();
				response.write("<" + tagName + " style=\"display:none\" id=\"" + getMarkupId() +
					"\">");
				if ("table".equals(tagName))
				{
					response.write("<tbody><tr><td></td></tr></tbody>");
				}
				response.write("</" + tagName + ">");
			}
			else
			{
				// render the item
				super.onRender();

				// should we also render children (ajax response)
				if (isRenderChildren())
				{
					// visit every child
					visitItemChildren(this, new IItemCallback()
					{
						@Override
						public void visitItem(TreeItem item)
						{
							// render child
							item.onRender();

							// go through the behaviors and invoke IBehavior.afterRender
							List<? extends Behavior> behaviors = item.getBehaviors();
							for (Behavior behavior : behaviors)
							{
								behavior.afterRender(item);
							}
						}
					});
				}
			}
		}

		/**
		 * 
		 * @return model object
		 */
		public Object getModelObject()
		{
			return getDefaultModelObject();
		}

		@Override
		public void internalRenderHead(final HtmlHeaderContainer container) {
			super.internalRenderHead(container);

			if (isRenderChildren())
			{
				// visit every child
				visitItemChildren(this, new IItemCallback()
				{
					@Override
					public void visitItem(TreeItem item)
					{
						if (item.isVisible())
						{
							item.internalRenderHead(container);
						}

						// write header contributions from the children of item
						item.visitChildren(new IVisitor<Component, Void>()
						{
							@Override
							public void component(final Component component,
								final IVisit<Void> visit)
							{
								if (component.isVisible())
								{
									component.internalRenderHead(container);
								}
								else
								{
									visit.dontGoDeeper();
								}
							}
						});
					}
				});
			}
		}

		protected final void setRenderChildren(boolean value)
		{
			setFlag(FLAG_RENDER_CHILDREN, value);
		}

		@Override
		protected void onDetach()
		{
			super.onDetach();
			Object object = getModelObject();
			if (object instanceof IDetachable)
			{
				((IDetachable)object).detach();
			}

			if (isRenderChildren())
			{
				// visit every child
				visitItemChildren(this, new IItemCallback()
				{
					@Override
					public void visitItem(TreeItem item)
					{
						item.detach();
					}
				});
			}

			// children are rendered, clear the flag
			setRenderChildren(false);
		}

		@Override
		protected void onBeforeRender()
		{
			onBeforeRenderInternal();
			super.onBeforeRender();

			if (isRenderChildren())
			{
				// visit every child
				visitItemChildren(this, new IItemCallback()
				{
					@Override
					public void visitItem(TreeItem item)
					{
						item.beforeRender();
					}
				});
			}
		}

		@Override
		protected void onAfterRender()
		{
			super.onAfterRender();
			if (isRenderChildren())
			{
				// visit every child
				visitItemChildren(this, new IItemCallback()
				{
					@Override
					public void visitItem(TreeItem item)
					{
						item.onAfterRender();
					}
				});
			}
		}

		private boolean hasParentWithChildrenMarkedToRecreation()
		{
			return getParentItem() != null &&
				(getParentItem().getChildren() == null || getParentItem().hasParentWithChildrenMarkedToRecreation());
		}
	}

	/**
	 * Components that holds tree items. This is similar to ListView, but it renders tree items in
	 * the right order.
	 */
	private class TreeItemContainer extends WebMarkupContainer
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            The component id
		 */
		public TreeItemContainer(String id)
		{
			super(id);
		}

		/**
		 * @see MarkupContainer#remove(Component)
		 */
		@Override
		public TreeItemContainer remove(Component component)
		{
			// when a treeItem is removed, remove reference to it from
			// nodeToItemMAp
			if (component instanceof TreeItem)
			{
				nodeToItemMap.remove(((TreeItem)component).getModelObject());
			}
			super.remove(component);
			return this;
		}

		/**
		 * @see MarkupContainer#onRender()
		 */
		@Override
		protected void onRender()
		{
			// is there a root item? (non-empty tree)
			if (rootItem != null)
			{
				IItemCallback callback = new IItemCallback()
				{
					@Override
					public void visitItem(TreeItem item)
					{
						// render component
						item.render();
					}
				};

				// visit item and it's children
				visitItemAndChildren(rootItem, callback);
			}
		}

		@Override
		public IMarkupFragment getMarkup(final Component child)
		{
			// The childs markup is always equal to the parents markup.
			return getMarkup();
		}
	}

	private boolean attached = false;

	/** comma separated list of ids of elements to be deleted. */
	private final AppendingStringBuffer deleteIds = new AppendingStringBuffer();

	/**
	 * whether the whole tree is dirty (so the whole tree needs to be refreshed).
	 */
	private boolean dirtyAll = false;

	/**
	 * list of dirty items. if children property of these items is null, the children will be
	 * rebuild.
	 */
	private final Set<TreeItem> dirtyItems = new HashSet<TreeItem>();

	/**
	 * list of dirty items which need the DOM structure to be created for them (added items)
	 */
	private final Set<TreeItem> dirtyItemsCreateDOM = new HashSet<TreeItem>();

	/** counter for generating unique ids of every tree item. */
	private int idCounter = 0;

	/** Component whose children are tree items. */
	private TreeItemContainer itemContainer;

	/**
	 * map that maps TreeNode to TreeItem. TreeItems only exists for TreeNodes, that are visible
	 * (their parents are not collapsed).
	 */
	// TODO this field is not serializable but nested inside an serializable component
	private final Map<Object, TreeItem> nodeToItemMap = new HashMap<Object, TreeItem>();

	/**
	 * we need to track previous model. if the model changes, we unregister the tree from listeners
	 * of old model and register the tree as listener of new model.
	 */

	// TODO this field is not serializable but nested inside an serializable component
	private TreeModel previousModel = null;

	/** root item of the tree. */
	private TreeItem rootItem = null;

	/** whether the tree root is shown. */
	private boolean rootLess = false;

	/** stores reference to tree state. */
	private ITreeState state;

	/**
	 * Tree constructor
	 * 
	 * @param id
	 *            The component id
	 */
	public AbstractTree(String id)
	{
		super(id);
		init();
	}

	/**
	 * Tree constructor
	 * 
	 * @param id
	 *            The component id
	 * @param model
	 *            The tree model
	 */
	public AbstractTree(String id, IModel<? extends TreeModel> model)
	{
		super(id, model);
		init();
	}

	/** called when all nodes are collapsed. */
	@Override
	public final void allNodesCollapsed()
	{
		invalidateAll();
	}

	/** called when all nodes are expanded. */
	@Override
	public final void allNodesExpanded()
	{
		invalidateAll();
	}

	/**
	 * 
	 * @return model
	 */
	@SuppressWarnings("unchecked")
	public IModel<? extends TreeModel> getModel()
	{
		return (IModel<? extends TreeModel>)getDefaultModel();
	}

	/**
	 * @return treemodel
	 */
	public TreeModel getModelObject()
	{
		return (TreeModel)getDefaultModelObject();
	}

	/**
	 * 
	 * @param model
	 * @return this
	 */
	public MarkupContainer setModel(IModel<? extends TreeModel> model)
	{
		setDefaultModel(model);
		return this;
	}

	/**
	 * 
	 * @param model
	 * @return this
	 */
	public MarkupContainer setModelObject(TreeModel model)
	{
		setDefaultModelObject(model);
		return this;
	}

	/**
	 * Returns the TreeState of this tree.
	 * 
	 * @return Tree state instance
	 */
	public ITreeState getTreeState()
	{
		if (state == null)
		{
			state = newTreeState();

			// add this object as listener of the state
			state.addTreeStateListener(this);
		}
		return state;
	}

	/**
	 * This method is called before the onAttach is called. Code here gets executed before the items
	 * have been populated.
	 */
	protected void onBeforeAttach()
	{
	}

	// This is necessary because MarkupContainer.onBeforeRender involves calling
	// beforeRender on children, which results in stack overflow when called from TreeItem
	private void onBeforeRenderInternal()
	{
		if (attached == false)
		{
			onBeforeAttach();

			checkModel();

			// Do we have to rebuild the whole tree?
			if (dirtyAll && rootItem != null)
			{
				clearAllItem();
			}
			else
			{
				// rebuild children of dirty nodes that need it
				rebuildDirty();
			}

			// is root item created? (root item is null if the items have not
			// been created yet, or the whole tree was dirty and clearAllITem
			// has been called
			if (rootItem == null)
			{
				Object rootNode = getModelObject().getRoot();
				if (rootNode != null)
				{
					if (isRootLess())
					{
						rootItem = newTreeItem(null, rootNode, -1);
					}
					else
					{
						rootItem = newTreeItem(null, rootNode, 0);
					}
					itemContainer.add(rootItem);
					buildItemChildren(rootItem);
				}
			}

			attached = true;
		}
	}

	/**
	 * Called at the beginning of the request (not ajax request, unless we are rendering the entire
	 * component)
	 */
	@Override
	public void onBeforeRender()
	{
		onBeforeRenderInternal();
		super.onBeforeRender();
	}

	/**
	 * @see MarkupContainer#onDetach()
	 */
	@Override
	public void onDetach()
	{
		attached = false;
		super.onDetach();
		if (getTreeState() instanceof IDetachable)
		{
			((IDetachable)getTreeState()).detach();
		}
	}

	/**
	 * Call to refresh the whole tree. This should only be called when the roodNode has been
	 * replaced or the entiry tree model changed.
	 */
	public final void invalidateAll()
	{
		updated();
		dirtyAll = true;
	}

	/**
	 * @return whether the tree root is shown
	 */
	public final boolean isRootLess()
	{
		return rootLess;
	}

	@Override
	public final void nodeCollapsed(Object node)
	{
		if (isNodeVisible(node) == true)
		{
			invalidateNodeWithChildren(node);
		}
	}

	@Override
	public final void nodeExpanded(Object node)
	{
		if (isNodeVisible(node) == true)
		{
			invalidateNodeWithChildren(node);
		}
	}

	@Override
	public final void nodeSelected(Object node)
	{
		if (isNodeVisible(node))
		{
			invalidateNode(node, isForceRebuildOnSelectionChange());
		}
	}

	@Override
	public final void nodeUnselected(Object node)
	{
		if (isNodeVisible(node))
		{
			invalidateNode(node, isForceRebuildOnSelectionChange());
		}
	}

	/**
	 * Determines whether the TreeNode needs to be rebuilt if it is selected or deselected
	 * 
	 * @return true if the node should be rebuilt after (de)selection, false otherwise
	 */
	protected boolean isForceRebuildOnSelectionChange()
	{
		return true;
	}

	/**
	 * Sets whether the root of the tree should be visible.
	 * 
	 * @param rootLess
	 *            whether the root should be visible
	 */
	public void setRootLess(boolean rootLess)
	{
		if (this.rootLess != rootLess)
		{
			this.rootLess = rootLess;
			invalidateAll();

			// if the tree is in rootless mode, make sure the root node is
			// expanded
			if (rootLess == true && getModelObject() != null)
			{
				getTreeState().expandNode(getModelObject().getRoot());
			}
		}
	}

	/**
	 * @see TreeModelListener#treeNodesChanged(TreeModelEvent)
	 */
	@Override
	public final void treeNodesChanged(TreeModelEvent e)
	{
		if (dirtyAll)
		{
			return;
		}
		// has root node changed?
		if (e.getChildren() == null)
		{
			if (rootItem != null)
			{
				invalidateNode(rootItem.getModelObject(), true);
			}
		}
		else
		{
			// go through all changed nodes
			Object[] children = e.getChildren();
			if (children != null)
			{
				for (Object node : children)
				{
					if (isNodeVisible(node))
					{
						// if the nodes is visible invalidate it
						invalidateNode(node, true);
					}
				}
			}
		}
	}

	/**
	 * Marks the last but one visible child node of the given item as dirty, if give child is the
	 * last item of parent.
	 * 
	 * We need this to refresh the previous visible item in case the inserted / deleted item was
	 * last. The reason is that the line shape of previous item changes from L to |- .
	 * 
	 * @param parent
	 * @param child
	 */
	private void markTheLastButOneChildDirty(TreeItem parent, TreeItem child)
	{
		if (parent.getChildren().indexOf(child) == parent.getChildren().size() - 1)
		{
			// go through the children backwards, start at the last but one
			// item
			for (int i = parent.getChildren().size() - 2; i >= 0; --i)
			{
				TreeItem item = parent.getChildren().get(i);

				// invalidate the node and it's children, so that they are
				// redrawn
				invalidateNodeWithChildren(item.getModelObject());

			}
		}
	}

	/**
	 * @see TreeModelListener#treeNodesInserted(TreeModelEvent)
	 */
	@Override
	public final void treeNodesInserted(TreeModelEvent e)
	{
		if (dirtyAll)
		{
			return;
		}

		// get the parent node of inserted nodes
		Object parentNode = e.getTreePath().getLastPathComponent();
		TreeItem parentItem = nodeToItemMap.get(parentNode);


		if (parentItem != null && isNodeVisible(parentNode))
		{
			List<?> eventChildren = Arrays.asList(e.getChildren());

			// parentNode was a leaf before this insertion event only if every one of
			// its current children is in the event's list of children
			boolean wasLeaf = true;
			int nodeChildCount = getChildCount(parentNode);
			for (int i = 0; wasLeaf && i < nodeChildCount; i++)
			{
				wasLeaf = eventChildren.contains(getChildAt(parentNode, i));
			}

			boolean addingToHiddedRoot = parentItem.getParentItem() == null && isRootLess();
			// if parent was a presented leaf
			if (wasLeaf && !addingToHiddedRoot)
			{
				// parentNode now has children for the first time, so we may need to invalidate
				// grandparent so that parentNode's junctionLink gets rebuilt with a plus/minus link
				Object grandparentNode = getParentNode(parentNode);
				boolean addingToHiddedRootSon = grandparentNode != null &&
					getParentNode(grandparentNode) == null && isRootLess();
				// if visible, invalidate the grandparent
				if (grandparentNode != null && !addingToHiddedRootSon)
				{
					invalidateNodeWithChildren(grandparentNode);
				}
				else
				{
					// if not, simply invalidating the parent node
					// OBS.: forcing rebuild since unlike the grandparent, the old
					// leaf parent needs to rebuild with plus/minus link
					invalidateNode(parentNode, true);
				}
				getTreeState().expandNode(parentNode);
			}
			else
			{
				if (isNodeExpanded(parentNode))
				{
					List<TreeItem> itemChildren = parentItem.getChildren();
					int childLevel = parentItem.getLevel() + 1;
					final int[] childIndices = e.getChildIndices();
					for (int i = 0; i < eventChildren.size(); ++i)
					{
						TreeItem item = newTreeItem(parentItem, eventChildren.get(i), childLevel);
						itemContainer.add(item);

						if (itemChildren != null)
						{
							itemChildren.add(childIndices[i], item);
							markTheLastButOneChildDirty(parentItem, item);
						}

						if (!dirtyItems.contains(item))
						{
							dirtyItems.add(item);
						}

						if (!dirtyItemsCreateDOM.contains(item) &&
							!item.hasParentWithChildrenMarkedToRecreation())
						{
							dirtyItemsCreateDOM.add(item);
						}
					}
				}
			}
		}
	}

	/**
	 * @see TreeModelListener#treeNodesRemoved(TreeModelEvent)
	 */
	@Override
	public final void treeNodesRemoved(TreeModelEvent removalEvent)
	{
		if (dirtyAll)
		{
			return;
		}

		// get the parent node of deleted nodes
		Object parentNode = removalEvent.getTreePath().getLastPathComponent();
		TreeItem parentItem = nodeToItemMap.get(parentNode);

		// unselect all removed items
		List<Object> selection = new ArrayList<Object>(getTreeState().getSelectedNodes());
		List<Object> removed = Arrays.asList(removalEvent.getChildren());
		for (Object selectedNode : selection)
		{
			Object cursor = selectedNode;
			while (cursor != null)
			{
				if (removed.contains(cursor))
				{
					getTreeState().selectNode(selectedNode, false);
				}
				if (cursor instanceof TreeNode)
				{
					cursor = ((TreeNode)cursor).getParent();
				}
				else
				{
					cursor = null;
				}
			}
		}

		if (parentItem != null && isNodeVisible(parentNode))
		{
			if (isNodeExpanded(parentNode))
			{
				// deleted nodes were visible; we need to delete their TreeItems
				for (Object deletedNode : removalEvent.getChildren())
				{
					TreeItem itemToDelete = nodeToItemMap.get(deletedNode);
					if (itemToDelete != null)
					{
						markTheLastButOneChildDirty(parentItem, itemToDelete);

						// remove all the deleted item's children
						visitItemChildren(itemToDelete, new IItemCallback()
						{
							@Override
							public void visitItem(TreeItem item)
							{
								removeItem(item);
							}
						});

						parentItem.getChildren().remove(itemToDelete);
						removeItem(itemToDelete);
					}
				}
			}

			if (!parentItem.hasChildTreeItems())
			{
				// rebuild parent's icon to show it no longer has children
				invalidateNode(parentNode, true);
			}
		}
	}

	/**
	 * @see TreeModelListener#treeStructureChanged(TreeModelEvent)
	 */
	@Override
	public final void treeStructureChanged(TreeModelEvent e)
	{
		if (dirtyAll)
		{
			return;
		}

		// get the parent node of changed nodes
		Object node = e.getTreePath() != null ? e.getTreePath().getLastPathComponent() : null;

		// has the tree root changed?
		if (node == null || e.getTreePath().getPathCount() == 1)
		{
			invalidateAll();
		}
		else
		{
			invalidateNodeWithChildren(node);
		}
	}

	/**
	 * Allows to intercept adding dirty components to AjaxRequestTarget.
	 * 
	 * @param target
	 * @param component
	 */
	protected void addComponent(AjaxRequestTarget target, Component component)
	{
		target.add(component);
	}

	@Override
	public void onTargetRespond(AjaxRequestTarget target)
	{
		// check whether the model hasn't changed
		checkModel();

		// is the whole tree dirty
		if (dirtyAll)
		{
			// render entire tree component
			addComponent(target, this);
		}
		else
		{
			// remove DOM elements that need to be removed
			if (deleteIds.length() != 0)
			{
				String js = getElementsDeleteJavaScript();

				// add the javascript to target
				target.prependJavaScript(js);
			}

			// We have to repeat this as long as there are any dirty items to be
			// created.
			// The reason why we can't do this in one pass is that some of the
			// items
			// may need to be inserted after items that has not been inserted
			// yet, so we have
			// to detect those and wait until the items they depend on are
			// inserted.
			while (dirtyItemsCreateDOM.isEmpty() == false)
			{
				for (Iterator<TreeItem> i = dirtyItemsCreateDOM.iterator(); i.hasNext();)
				{
					TreeItem item = i.next();
					TreeItem parent = item.getParentItem();
					int index = parent.getChildren().indexOf(item);
					TreeItem previous;
					// we need item before this (in dom structure)

					if (index == 0)
					{
						previous = parent;
					}
					else
					{
						previous = parent.getChildren().get(index - 1);
						// get the last item of previous item subtree
						while (previous.getChildren() != null && previous.getChildren().size() > 0)
						{
							previous = previous.getChildren()
								.get(previous.getChildren().size() - 1);
						}
					}
					// check if the previous item isn't waiting to be inserted
					if (dirtyItemsCreateDOM.contains(previous) == false)
					{
						// it's already in dom, so we can use it as point of
						// insertion
						target.prependJavaScript("Wicket.Tree.createElement(\"" +
							item.getMarkupId() + "\"," + "\"" + previous.getMarkupId() + "\")");

						// remove the item so we don't process it again
						i.remove();
					}
					else
					{
						// we don't do anything here, inserting this item will
						// have to wait
						// until the previous item gets inserted
					}
				}
			}

			// iterate through dirty items
			for (TreeItem item : dirtyItems)
			{
				// does the item need to rebuild children?
				if (item.getChildren() == null)
				{
					// rebuild the children
					buildItemChildren(item);

					// set flag on item so that it renders itself together with
					// it's children
					item.setRenderChildren(true);
				}

				// add the component to target
				addComponent(target, item);
			}

			// clear dirty flags
			updated();
		}
	}

	/**
	 * Convenience method that updates changed portions on tree. You can call this method during
	 * Ajax response, where calling {@link #updateTree(AjaxRequestTarget)}
	 * would be appropriate, but you don't have the AjaxRequestTarget instance. However, it is also
	 * safe to call this method outside Ajax response.
	 */
	public final void updateTree()
	{
		Optional<AjaxRequestTarget> target = getRequestCycle().find(AjaxRequestTarget.class);
		if (target.isPresent() == false)
		{
			throw new WicketRuntimeException(
				"No AjaxRequestTarget available to execute updateTree(ART target)");
		}

		updateTree(target.get());
	}

	/**
	 * Updates the changed portions of the tree using given AjaxRequestTarget. Call this method if
	 * you modified the tree model during an ajax request target and you want to partially update
	 * the component on page. Make sure that the tree model has fired the proper listener functions.
	 * <p>
	 * <b>You can only call this method once in a request.</b>
	 * 
	 * @param target
	 *            Ajax request target used to send the update to the page
	 */
	public final void updateTree(final AjaxRequestTarget target)
	{
		Args.notNull(target, "target");
		target.registerRespondListener(this);
	}

	/**
	 * Returns whether the given node is expanded.
	 * 
	 * @param node
	 *            The node to inspect
	 * @return true if the node is expanded, false otherwise
	 */
	protected final boolean isNodeExpanded(Object node)
	{
		// In root less mode the root node is always expanded
		if (isRootLess() && rootItem != null && rootItem.getModelObject().equals(node))
		{
			return true;
		}

		return getTreeState().isNodeExpanded(node);
	}

	/**
	 * Creates the TreeState, which is an object where the current state of tree (which nodes are
	 * expanded / collapsed, selected, ...) is stored.
	 * 
	 * @return Tree state instance
	 */
	protected ITreeState newTreeState()
	{
		return new DefaultTreeState();
	}

	/**
	 * Called after the rendering of tree is complete. Here we clear the dirty flags.
	 */
	@Override
	protected void onAfterRender()
	{
		super.onAfterRender();
		// rendering is complete, clear all dirty flags and items
		updated();
	}

	/**
	 * This method is called after creating every TreeItem. This is the place for adding components
	 * on item (junction links, labels, icons...)
	 * 
	 * @param item
	 *            newly created tree item. The node can be obtained as item.getModelObject()
	 * 
	 * @param level
	 *            how deep the component is in tree hierarchy (0 for root item)
	 */
	protected abstract void populateTreeItem(WebMarkupContainer item, int level);

	/**
	 * Builds the children for given TreeItem. It recursively traverses children of it's TreeNode
	 * and creates TreeItem for every visible TreeNode.
	 * 
	 * @param item
	 *            The parent tree item
	 */
	private void buildItemChildren(TreeItem item)
	{
		List<TreeItem> items;

		// if the node is expanded
		if (isNodeExpanded(item.getModelObject()))
		{
			// build the items for children of the items' treenode.
			items = buildTreeItems(item, nodeChildren(item.getModelObject()), item.getLevel() + 1);
		}
		else
		{
			// it's not expanded, just set children to an empty list
			items = new ArrayList<TreeItem>(0);
		}

		item.setChildren(items);
	}

	/**
	 * Builds (recursively) TreeItems for the given Iterator of TreeNodes.
	 * 
	 * @param parent
	 * @param nodes
	 *            The nodes to build tree items for
	 * @param level
	 *            The current level
	 * @return List with new tree items
	 */
	private List<TreeItem> buildTreeItems(TreeItem parent, Iterator<Object> nodes, int level)
	{
		List<TreeItem> result = new ArrayList<TreeItem>();

		// for each node
		while (nodes.hasNext())
		{
			Object node = nodes.next();
			// create tree item
			TreeItem item = newTreeItem(parent, node, level);
			itemContainer.add(item);

			// builds it children (recursively)
			buildItemChildren(item);

			// add item to result
			result.add(item);
		}

		return result;
	}

	/**
	 * Checks whether the model has been changed, and if so unregister and register listeners.
	 */
	private void checkModel()
	{
		// find out whether the model object (the TreeModel) has been changed
		TreeModel model = getModelObject();
		if (model != previousModel)
		{
			if (previousModel != null)
			{
				previousModel.removeTreeModelListener(this);
			}

			previousModel = model;

			if (model != null)
			{
				model.addTreeModelListener(this);
			}
			// model has been changed, redraw whole tree
			invalidateAll();
		}
	}

	/**
	 * Removes all TreeItem components.
	 */
	private void clearAllItem()
	{
		visitItemAndChildren(rootItem, new IItemCallback()
		{
			@Override
			public void visitItem(TreeItem item)
			{
				item.remove();
			}
		});
		rootItem = null;
	}

	/**
	 * Returns the javascript used to delete removed elements.
	 * 
	 * @return The javascript
	 */
	private String getElementsDeleteJavaScript()
	{
		// build the javascript call
		final AppendingStringBuffer buffer = new AppendingStringBuffer(100);

		buffer.append("Wicket.Tree.removeNodes(\"");

		// first parameter is the markup id of tree (will be used as prefix to
		// build ids of child items
		buffer.append(getMarkupId() + "_\",[");

		// append the ids of elements to be deleted
		buffer.append(deleteIds);

		// does the buffer end if ','?
		if (buffer.endsWith(","))
		{
			// it does, trim it
			buffer.setLength(buffer.length() - 1);
		}

		buffer.append("]);");

		return buffer.toString();
	}

	//
	// State and Model callbacks
	//

	/**
	 * returns the short version of item id (just the number part).
	 * 
	 * @param item
	 *            The tree item
	 * @return The id
	 */
	private String getShortItemId(TreeItem item)
	{
		// show much of component id can we skip? (to minimize the length of
		// javascript being sent)
		final int skip = getMarkupId().length() + 1; // the length of id of
		// tree and '_'.
		return item.getMarkupId().substring(skip);
	}

	private final static ResourceReference JAVASCRIPT = new JavaScriptResourceReference(
		AbstractTree.class, "res/tree.js");

	/**
	 * Initialize the component.
	 */
	private void init()
	{
		setVersioned(false);

		// we need id when we are replacing the whole tree
		setOutputMarkupId(true);

		// create container for tree items
		itemContainer = new TreeItemContainer("i");
		add(itemContainer);

		checkModel();
	}

	/**
	 * INTERNAL
	 * 
	 * @param node
	 */
	public final void markNodeDirty(Object node)
	{
		invalidateNode(node, false);
	}

	/**
	 * INTERNAL
	 * 
	 * @param node
	 */
	public final void markNodeChildrenDirty(Object node)
	{
		TreeItem item = nodeToItemMap.get(node);
		if (item != null)
		{
			visitItemChildren(item, new IItemCallback()
			{
				@Override
				public void visitItem(TreeItem item)
				{
					invalidateNode(item.getModelObject(), false);
				}
			});
		}
	}

	/**
	 * Invalidates single node (without children). On the next render, this node will be updated.
	 * Node will not be rebuilt, unless forceRebuild is true.
	 * 
	 * @param node
	 *            The node to invalidate
	 * @param forceRebuild
	 */
	private void invalidateNode(Object node, boolean forceRebuild)
	{
		if (dirtyAll == false)
		{
			// get item for this node
			TreeItem item = nodeToItemMap.get(node);

			if (item != null)
			{
				boolean createDOM = false;

				if (forceRebuild)
				{
					// recreate the item
					int level = item.getLevel();
					List<TreeItem> children = item.getChildren();
					String id = item.getId();

					// store the parent of old item
					TreeItem parent = item.getParentItem();

					// if the old item has a parent, store it's index
					int index = parent != null ? parent.getChildren().indexOf(item) : -1;

					createDOM = dirtyItemsCreateDOM.contains(item);

					dirtyItems.remove(item);
					dirtyItemsCreateDOM.remove(item);

					item.remove();

					item = newTreeItem(parent, node, level, id);
					itemContainer.add(item);

					item.setChildren(children);

					// was the item an root item?
					if (parent == null)
					{
						rootItem = item;
					}
					else
					{
						parent.getChildren().set(index, item);
					}
				}

				if (!dirtyItems.contains(item))
				{
					dirtyItems.add(item);
				}

				if (createDOM && !dirtyItemsCreateDOM.contains(item))
				{
					dirtyItemsCreateDOM.add(item);
				}
			}
		}
	}

	/**
	 * Invalidates node and it's children. On the next render, the node and children will be
	 * updated. Node children will be rebuilt.
	 * 
	 * @param node
	 *            The node to invalidate
	 */
	private void invalidateNodeWithChildren(Object node)
	{
		if (dirtyAll == false)
		{
			// get item for this node
			TreeItem item = nodeToItemMap.get(node);

			// is the item visible?
			if (item != null)
			{
				// go though item children and remove every one of them
				visitItemChildren(item, new IItemCallback()
				{
					@Override
					public void visitItem(TreeItem item)
					{
						removeItem(item);
					}
				});

				// set children to null so that they get rebuild
				item.setChildren(null);

				if (!dirtyItems.contains(item))
				{
					// add item to dirty items
					dirtyItems.add(item);
				}
			}
		}
	}

	/**
	 * Returns whether the given node is visible, e.g. all it's parents are expanded.
	 * 
	 * @param node
	 *            The node to inspect
	 * @return true if the node is visible, false otherwise
	 */
	private boolean isNodeVisible(Object node)
	{
		if (node == null)
		{
			return false;
		}
		Object parent = getParentNode(node);
		while (parent != null)
		{
			if (isNodeExpanded(parent) == false)
			{
				return false;
			}
			parent = getParentNode(parent);
		}
		return true;
	}

	/**
	 * Returns parent node of given node.
	 * 
	 * @param node
	 * @return parent node
	 */
	public Object getParentNode(Object node)
	{
		TreeItem item = nodeToItemMap.get(node);
		if (item == null)
		{
			return null;
		}
		else
		{
			TreeItem parent = item.getParentItem();
			return parent == null ? null : parent.getModelObject();
		}
	}

	/**
	 * Creates a tree item for given node.
	 * 
	 * @param parent
	 * @param node
	 *            The tree node
	 * @param level
	 *            The level *
	 * @return The new tree item
	 */
	private TreeItem newTreeItem(TreeItem parent, Object node, int level)
	{
		return new TreeItem(parent, "" + idCounter++, node, level);
	}

	/**
	 * Creates a tree item for given node with specified id.
	 * 
	 * @param parent
	 * @param node
	 *            The tree node
	 * @param level
	 *            The level
	 * @param id
	 *            the component id
	 * @return The new tree item
	 */
	private TreeItem newTreeItem(TreeItem parent, Object node, int level, String id)
	{
		return new TreeItem(parent, id, node, level);
	}

	/**
	 * Return the representation of node children as Iterator interface.
	 * 
	 * @param node
	 *            The tree node
	 * @return iterable presentation of node children
	 */
	public final Iterator<Object> nodeChildren(Object node)
	{
		TreeModel model = getTreeModel();
		int count = model.getChildCount(node);
		List<Object> nodes = new ArrayList<Object>(count);
		for (int i = 0; i < count; ++i)
		{
			nodes.add(model.getChild(node, i));
		}
		return nodes.iterator();
	}

	/**
	 * @param parent
	 * @param index
	 * @return child
	 */
	public final Object getChildAt(Object parent, int index)
	{
		return getTreeModel().getChild(parent, index);
	}

	/**
	 * 
	 * @param node
	 * @return boolean
	 */
	public final boolean isLeaf(Object node)
	{
		return getTreeModel().isLeaf(node);
	}

	/**
	 * @param parent
	 * @return child count
	 */
	public final int getChildCount(Object parent)
	{
		return getTreeModel().getChildCount(parent);
	}

	private TreeModel getTreeModel()
	{
		return getModelObject();
	}

	/**
	 * Rebuilds children of every item in dirtyItems that needs it. This method is called for
	 * non-partial update.
	 */
	private void rebuildDirty()
	{
		// go through dirty items
		for (TreeItem item : dirtyItems)
		{
			// item children need to be rebuilt
			if (item.getChildren() == null)
			{
				buildItemChildren(item);
			}
		}
	}

	/**
	 * Removes the item, appends it's id to deleteIds. This is called when a items parent is being
	 * deleted or rebuilt.
	 * 
	 * @param item
	 *            The item to remove
	 */
	private void removeItem(TreeItem item)
	{
		// even if the item is dirty it's no longer necessary to update id
		dirtyItems.remove(item);

		// if the item was about to be created
		if (dirtyItemsCreateDOM.contains(item))
		{
			// we needed to create DOM element, we no longer do
			dirtyItemsCreateDOM.remove(item);
		}
		else
		{
			// add items id (it's short version) to ids of DOM elements that
			// will be
			// removed
			deleteIds.append(getShortItemId(item));
			deleteIds.append(",");
		}

		if (item.getParent() != null)
		{
			// remove the id
			// note that this doesn't update item's parent's children list
			item.remove();
		}
	}

	/**
	 * Calls after the tree has been rendered. Clears all dirty flags.
	 */
	private void updated()
	{
		dirtyAll = false;
		dirtyItems.clear();
		dirtyItemsCreateDOM.clear();
		deleteIds.clear();
	}

	/**
	 * Call the callback#visitItem method for the given item and all it's children.
	 * 
	 * @param item
	 *            The tree item
	 * @param callback
	 *            item call back
	 */
	private void visitItemAndChildren(TreeItem item, IItemCallback callback)
	{
		callback.visitItem(item);
		visitItemChildren(item, callback);
	}

	/**
	 * Call the callback#visitItem method for every child of given item.
	 * 
	 * @param item
	 *            The tree item
	 * @param callback
	 *            The callback
	 */
	private void visitItemChildren(TreeItem item, IItemCallback callback)
	{
		if (item.getChildren() != null)
		{
			for (TreeItem child : item.getChildren())
			{
				visitItemAndChildren(child, callback);
			}
		}
	}

	/**
	 * Returns the component associated with given node, or null, if node is not visible. This is
	 * useful in situations when you want to touch the node element in html.
	 * 
	 * @param node
	 *            Tree node
	 * @return Component associated with given node, or null if node is not visible.
	 */
	public Component getNodeComponent(Object node)
	{
		return nodeToItemMap.get(node);
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		response.render(JavaScriptHeaderItem.forReference(JAVASCRIPT));
	}
}
