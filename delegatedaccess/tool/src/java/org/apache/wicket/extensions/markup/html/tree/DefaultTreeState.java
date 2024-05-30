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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.util.io.IClusterable;

/**
 * Default implementation of TreeState.
 * <p>
 * This implementation tries to be as lightweight as possible. By default all nodes are collapsed.
 * 
 * @author Matej Knopp
 */
public class DefaultTreeState implements ITreeState, IClusterable, IDetachable
{
	private static final long serialVersionUID = 1L;

	/** Whether multiple selections can be done. */
	private boolean allowSelectMultiple = false;

	/** Tree state listeners. */
	private final List<ITreeStateListener> listeners = new ArrayList<ITreeStateListener>(1);

	/**
	 * set of nodes which are collapsed or expanded (depends on nodesCollapsed variable).
	 */
	private final Set<Object> nodes = new HashSet<Object>();

	/** Whether the nodes set should be treated as set of collapsed or expanded nodes. */
	private boolean nodesCollapsed = false; // by default treat the node set as expanded nodes

	/** Set selected nodes. */
	private final Set<Object> selectedNodes = new HashSet<Object>();

	/**
	 * @see org.apache.wicket.extensions.markup.html.tree.ITreeState#addTreeStateListener(org.apache.wicket.extensions.markup.html.tree.ITreeStateListener)
	 */
	@Override
	public void addTreeStateListener(ITreeStateListener l)
	{
		if (listeners.contains(l) == false)
		{
			listeners.add(l);
		}
	}

	/**
	 * @see org.apache.wicket.extensions.markup.html.tree.ITreeState#collapseAll()
	 */
	@Override
	public void collapseAll()
	{
		if (nodes.isEmpty() && nodesCollapsed == false)
		{
			// all nodes are already collapsed, do nothing
		}
		else
		{
			// clear all nodes from the set and sets the nodes as expanded
			nodes.clear();
			nodesCollapsed = false;

			for (ITreeStateListener listener : listeners)
				listener.allNodesCollapsed();
		}
	}

	@Override
	public void collapseNode(Object node)
	{
		if (nodesCollapsed == true)
		{
			nodes.add(node);
		}
		else
		{
			nodes.remove(node);
		}

		for (ITreeStateListener listener : listeners)
			listener.nodeCollapsed(node);
	}

	/**
	 * @see org.apache.wicket.extensions.markup.html.tree.ITreeState#expandAll()
	 */
	@Override
	public void expandAll()
	{
		if (nodes.isEmpty() && nodesCollapsed == true)
		{
			// all nodes are already expanded, do nothing
		}
		else
		{
			// clear node set and set nodes policy as collapsed
			nodes.clear();
			nodesCollapsed = true;

			for (ITreeStateListener listener : listeners)
				listener.allNodesExpanded();
		}
	}

	@Override
	public void expandNode(Object node)
	{
		if (nodesCollapsed == false)
		{
			nodes.add(node);
		}
		else
		{
			nodes.remove(node);
		}

		for (ITreeStateListener listener : listeners)
			listener.nodeExpanded(node);
	}

	/**
	 * @see org.apache.wicket.extensions.markup.html.tree.ITreeState#getSelectedNodes()
	 */
	@Override
	public Collection<Object> getSelectedNodes()
	{
		return Collections.unmodifiableList(new ArrayList<Object>(selectedNodes));
	}

	protected void removeSelectedNodeSilent(Object node)
	{
		selectedNodes.remove(node);
	}

	/**
	 * @see org.apache.wicket.extensions.markup.html.tree.ITreeState#isAllowSelectMultiple()
	 */
	@Override
	public boolean isAllowSelectMultiple()
	{
		return allowSelectMultiple;
	}

	@Override
	public boolean isNodeExpanded(Object node)
	{
		if (nodesCollapsed == false)
		{
			return nodes.contains(node);
		}
		else
		{
			return nodes.contains(node) == false;
		}
	}

	@Override
	public boolean isNodeSelected(Object node)
	{
		return selectedNodes.contains(node);
	}

	/**
	 * @see org.apache.wicket.extensions.markup.html.tree.ITreeState#removeTreeStateListener(org.apache.wicket.extensions.markup.html.tree.ITreeStateListener)
	 */
	@Override
	public void removeTreeStateListener(ITreeStateListener l)
	{
		listeners.remove(l);
	}


	/**
	 * If <code>node</code> is currently selected, it will be deselected and the
	 * <code>nodeUnselected</code> method will be called on all registered
	 * <code>ITreeStateListeners</code>.
	 * 
	 * @param node
	 *            the node to be deselected
	 */
	private void deselectNode(Object node)
	{
		if (selectedNodes.remove(node))
		{
			for (ITreeStateListener listener : listeners.toArray(new ITreeStateListener[listeners.size()]))
			{
				listener.nodeUnselected(node);
			}
		}
	}

	/**
	 * Selects <code>node</code> and calls the <code>nodeSelected</code> method on all registered
	 * <code>ITreeStateListeners</code>. If <code>isAllowSelectMultiple</code> is <code>false</code>
	 * , any currently selected nodes are deselected.
	 * 
	 * @param node
	 *            the node to be selected
	 */
	private void selectNode(Object node)
	{
		// if multiple selections are not allowed, deselect current selections
		if (selectedNodes.size() > 0 && !isAllowSelectMultiple())
		{
			for (Object currentlySelectedNode : selectedNodes.toArray())
			{
				if (!currentlySelectedNode.equals(node))
				{
					deselectNode(currentlySelectedNode);
				}
			}
		}

		if (!selectedNodes.contains(node))
		{
			selectedNodes.add(node);
			for (ITreeStateListener listener : listeners.toArray(new ITreeStateListener[listeners.size()]))
			{
				listener.nodeSelected(node);
			}
		}
	}

	/**
	 * Selects or deselects <code>node</code> and calls the corresponding method on all registered
	 * <code>ITreeStateListeners</code>. If <code>isAllowSelectMultiple</code> is <code>false</code>
	 * , any currently selected nodes are deselected.
	 * 
	 * @param node
	 *            the node to be selected
	 * @param selected
	 *            true if node is to be selected, false if node is to be deselected
	 */
	@Override
	public void selectNode(Object node, boolean selected)
	{
		if (selected)
		{
			selectNode(node);
		}
		else
		{
			deselectNode(node);
		}
	}


	/**
	 * @see org.apache.wicket.extensions.markup.html.tree.ITreeState#setAllowSelectMultiple(boolean)
	 */
	@Override
	public void setAllowSelectMultiple(boolean value)
	{
		allowSelectMultiple = value;
	}

	@Override
	public void detach()
	{
		for (Object node : nodes)
		{
			if (node instanceof IDetachable)
			{
				((IDetachable)node).detach();
			}
		}
		for (Object node : selectedNodes)
		{
			if (node instanceof IDetachable)
			{
				((IDetachable)node).detach();
			}
		}
	}
}
