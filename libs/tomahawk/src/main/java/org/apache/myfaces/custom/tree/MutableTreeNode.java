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
package org.apache.myfaces.custom.tree;

/**
 * Defines the requirements for a tree node object that can change -- by adding or removing
 * child nodes, or by changing the contents of a user object stored in the node.
 * (inspired by javax.swing.tree.MutableTreeNode).
 *
 * @author <a href="mailto:oliver@rossmueller.com">Oliver Rossmueller</a>
 * @version $Revision: 472638 $ $Date: 2006-11-08 15:54:13 -0500 (Wed, 08 Nov 2006) $
 */
public interface MutableTreeNode
        extends TreeNode
{

    /**
     * Add the given child to the children of this node.
     * This will set this node as the parent of the child using {#setParent}.
     */
    void insert(MutableTreeNode child);


    /**
     * Add the given child to the children of this node at index.
     * This will set this node as the parent of the child using {#setParent}.
     */
    void insert(MutableTreeNode child, int index);


    /**
     * Remove the child at the given index.
     */
    void remove(int index);


    /**
     * Remove the given node.
     */
    void remove(MutableTreeNode node);


    /**
     * Sets the user object of this node.
     */
    void setUserObject(Object object);


    /**
     * Remove this node from its parent.
     */
    void removeFromParent();


    /**
     * Set the parent node.
     */
    void setParent(MutableTreeNode parent);
}
