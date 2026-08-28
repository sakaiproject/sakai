/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.custom.tree.model;

import java.util.Collection;


/**
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 * @version $Revision: 472638 $ $Date: 2006-11-08 15:54:13 -0500 (Wed, 08 Nov 2006) $
 */
public interface TreeModel
{

    /**
     * Return the root of the tree.
     *
     * @return the root of the tree or null, it this tree has no nodes
     */
    public Object getRoot();


    /**
     * Return the child of <code>parent</code> at index <code>index</code>
     * in the parent's child array.
     *
     * @param parent a node in the tree
     * @return the child of <code>parent</code> at index <code>index</code>
     */
    public Object getChild(Object parent, int index);


    /**
     * Answer the number of children of <code>parent</code>.
     *
     * @param parent a node in the tree
     * @return the number of children of the node <code>parent</code>
     */
    public int getChildCount(Object parent);


    /**
     * Answer <code>true</code> if <code>node</code> is a leaf.
     *
     * @param node a node in the tree
     * @return true if <code>node</code> is a leaf
     */
    public boolean isLeaf(Object node);


    /**
     * Called when the value for the item identified
     * by <code>path</code> has changed to <code>newValue</code>.
     * If <code>newValue</code> signifies a truly new value
     * the model should post a <code>treeNodesChanged</code> event.
     *
     * @param path     path to the node that has been altered
     * @param newValue the new value from the TreeCellEditor
     */
    public void valueForPathChanged(TreePath path, Object newValue);


    /**
     * Return the index of child in parent.
     *
     * @param parent a node in the tree
     * @param child  the node we are interested in
     * @return the index of the child in the parent, or -1 if either
     *         <code>child</code> or <code>parent</code> are <code>null</code>
     */
    public int getIndexOfChild(Object parent, Object child);


    /**
     * Answer the mutable collection of tree model listeners.
     *
     * @return Collection
     */
    Collection getTreeModelListeners();
}
