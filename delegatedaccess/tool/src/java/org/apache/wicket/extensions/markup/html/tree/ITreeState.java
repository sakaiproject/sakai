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
import java.util.Collection;

/**
 * Tree state holds information about a tree such as which nodes are expanded / collapsed and which
 * nodes are selected, It can also fire callbacks on listener in case any of the information
 * changed.
 * 
 * @author Matej Knopp
 */
@Deprecated
public interface ITreeState extends Serializable
{
	/**
	 * Adds a tree state listener. On state change events on the listener are fired.
	 * 
	 * @param l
	 *            Listener to add
	 */
	void addTreeStateListener(ITreeStateListener l);

	/**
	 * Collapses all nodes of the tree.
	 */
	void collapseAll();

	/**
	 * Collapses the given node.
	 * 
	 * @param node
	 *            Node to collapse
	 */
	void collapseNode(Object node);

	/**
	 * Expands all nodes of the tree.
	 */
	void expandAll();

	/**
	 * Expands the given node.
	 * 
	 * @param node
	 *            Node to expand
	 */
	void expandNode(Object node);

	/**
	 * Returns the collection of all selected nodes.
	 * 
	 * @return The collection of selected nodes
	 */
	Collection<Object> getSelectedNodes();

	/**
	 * Returns whether multiple nodes can be selected.
	 * 
	 * @return True if multiple nodes can be selected
	 */
	boolean isAllowSelectMultiple();

	/**
	 * Returns true if the given node is expanded.
	 * 
	 * @param node
	 *            The node to inspect
	 * @return True if the node is expanded
	 */
	boolean isNodeExpanded(Object node);

	/**
	 * Returns true if the given node is selected, false otherwise.
	 * 
	 * @param node
	 *            The node to inspect
	 * @return True if the node is selected
	 */
	boolean isNodeSelected(Object node);

	/**
	 * Removes a tree state listener.
	 * 
	 * @param l
	 *            The listener to remove
	 */
	void removeTreeStateListener(ITreeStateListener l);


	/**
	 * Marks given node as selected (or unselected) according to the selected value.
	 * <p>
	 * If tree is in single selection mode and a new node is selected, old node is automatically
	 * unselected (and the event is fired on listeners).
	 * 
	 * @param node
	 *            The node to select or deselect
	 * @param selected
	 *            If true, the node will be selected, otherwise, the node will be unselected
	 */
	void selectNode(Object node, boolean selected);

	/**
	 * Sets whether multiple nodes can be selected.
	 * 
	 * @param value
	 *            If true, multiple nodes can be selected. If false, only one node at a time can be
	 *            selected
	 */
	void setAllowSelectMultiple(boolean value);
}
