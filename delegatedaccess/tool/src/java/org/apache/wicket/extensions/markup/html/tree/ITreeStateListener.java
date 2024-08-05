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

/**
 * Methods this interface are called when tree state is changing.
 * 
 * @author Matej Knopp
 */
@Deprecated
public interface ITreeStateListener extends Serializable
{
	/**
	 * Fired when all nodes are collapsed.
	 */
	void allNodesCollapsed();

	/**
	 * Fired when all nodes are expanded.
	 */
	void allNodesExpanded();

	/**
	 * Fired when given node is collapsed.
	 * 
	 * @param node
	 *            The node that was collapsed
	 */
	void nodeCollapsed(Object node);

	/**
	 * Fired when given node is expanded.
	 * 
	 * @param node
	 */
	void nodeExpanded(Object node);

	/**
	 * Fired when given node gets selected.
	 * 
	 * @param node
	 *            The node that was selected
	 */
	void nodeSelected(Object node);

	/**
	 * Fired when given node gets unselected.
	 * 
	 * @param node
	 *            The node that was unselected
	 */
	void nodeUnselected(Object node);
}
